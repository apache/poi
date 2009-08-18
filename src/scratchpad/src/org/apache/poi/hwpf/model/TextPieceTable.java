/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hwpf.model;

import org.apache.poi.hwpf.model.io.HWPFOutputStream;
import org.apache.poi.poifs.common.POIFSConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * The piece table for matching up character positions to bits of text. This
 * mostly works in bytes, but the TextPieces themselves work in characters. This
 * does the icky convertion.
 *
 * @author Ryan Ackley
 */
public final class TextPieceTable implements CharIndexTranslator {
	protected ArrayList _textPieces = new ArrayList();
	// int _multiple;
	int _cpMin;

	public TextPieceTable() {
	}

	public TextPieceTable(byte[] documentStream, byte[] tableStream, int offset, int size, int fcMin) {
		// get our plex of PieceDescriptors
		PlexOfCps pieceTable = new PlexOfCps(tableStream, offset, size, PieceDescriptor
				.getSizeInBytes());

		int length = pieceTable.length();
		PieceDescriptor[] pieces = new PieceDescriptor[length];

		// iterate through piece descriptors raw bytes and create
		// PieceDescriptor objects
		for (int x = 0; x < length; x++) {
			GenericPropertyNode node = pieceTable.getProperty(x);
			pieces[x] = new PieceDescriptor(node.getBytes(), 0);
		}

		// Figure out the cp of the earliest text piece
		// Note that text pieces don't have to be stored in order!
		_cpMin = pieces[0].getFilePosition() - fcMin;
		for (int x = 0; x < pieces.length; x++) {
			int start = pieces[x].getFilePosition() - fcMin;
			if (start < _cpMin) {
				_cpMin = start;
			}
		}

		// using the PieceDescriptors, build our list of TextPieces.
		for (int x = 0; x < pieces.length; x++) {
			int start = pieces[x].getFilePosition();
			PropertyNode node = pieceTable.getProperty(x);

			// Grab the start and end, which are in characters
			int nodeStartChars = node.getStart();
			int nodeEndChars = node.getEnd();

			// What's the relationship between bytes and characters?
			boolean unicode = pieces[x].isUnicode();
			int multiple = 1;
			if (unicode) {
				multiple = 2;
			}

			// Figure out the length, in bytes and chars
			int textSizeChars = (nodeEndChars - nodeStartChars);
			int textSizeBytes = textSizeChars * multiple;

			// Grab the data that makes up the piece
			byte[] buf = new byte[textSizeBytes];
			System.arraycopy(documentStream, start, buf, 0, textSizeBytes);

			// And now build the piece
			_textPieces.add(new TextPiece(nodeStartChars, nodeEndChars, buf, pieces[x], node
					.getStart()));
		}

		// In the interest of our sanity, now sort the text pieces
		// into order, if they're not already
		TextPiece[] tp = (TextPiece[]) _textPieces.toArray(new TextPiece[_textPieces.size()]);
		Arrays.sort(tp);
		for (int i = 0; i < tp.length; i++) {
			_textPieces.set(i, tp[i]);
		}
	}

	public int getCpMin() {
		return _cpMin;
	}

	public List getTextPieces() {
		return _textPieces;
	}

	/**
	 * Is the text at the given Character offset unicode, or plain old ascii? In
	 * a very evil fashion, you have to actually know this to make sense of
	 * character and paragraph properties :(
	 *
	 * @param cp
	 *            The character offset to check about
	 */
	public boolean isUnicodeAtCharOffset(int cp) {
		boolean lastWas = false;

		Iterator it = _textPieces.iterator();
		while (it.hasNext()) {
			TextPiece tp = (TextPiece) it.next();
			// If the text piece covers the character, all good
			if (tp.getStart() <= cp && tp.getEnd() >= cp) {
				return tp.isUnicode();
			}
			// Otherwise keep track for the last one
			lastWas = tp.isUnicode();
		}

		// If they ask off the end, just go with the last one...
		return lastWas;
	}

	public boolean isUnicodeAtByteOffset(int bytePos) {
		boolean lastWas = false;

		Iterator it = _textPieces.iterator();
		while (it.hasNext()) {
			TextPiece tp = (TextPiece) it.next();
			int curByte = tp.getPieceDescriptor().getFilePosition();
			int pieceEnd = curByte + tp.bytesLength();

			// If the text piece covers the character, all good
			if (curByte <= bytePos && pieceEnd > bytePos) {
				return tp.isUnicode();
			}
			// Otherwise keep track for the last one
			lastWas = tp.isUnicode();
			// Move along
			curByte = pieceEnd;
		}

		// If they ask off the end, just go with the last one...
		return lastWas;
	}

	public byte[] writeTo(HWPFOutputStream docStream) throws IOException {

		PlexOfCps textPlex = new PlexOfCps(PieceDescriptor.getSizeInBytes());
		// int fcMin = docStream.getOffset();

		int size = _textPieces.size();
		for (int x = 0; x < size; x++) {
			TextPiece next = (TextPiece) _textPieces.get(x);
			PieceDescriptor pd = next.getPieceDescriptor();

			int offset = docStream.getOffset();
			int mod = (offset % POIFSConstants.BIG_BLOCK_SIZE);
			if (mod != 0) {
				mod = POIFSConstants.BIG_BLOCK_SIZE - mod;
				byte[] buf = new byte[mod];
				docStream.write(buf);
			}

			// set the text piece position to the current docStream offset.
			pd.setFilePosition(docStream.getOffset());

			// write the text to the docstream and save the piece descriptor to
			// the
			// plex which will be written later to the tableStream.
			docStream.write(next.getRawBytes());

			// The TextPiece is already in characters, which
			// makes our life much easier
			int nodeStart = next.getStart();
			int nodeEnd = next.getEnd();
			textPlex.addProperty(new GenericPropertyNode(nodeStart, nodeEnd, pd.toByteArray()));
		}

		return textPlex.toByteArray();

	}

	/**
	 * Adjust all the text piece after inserting some text into one of them
	 *
	 * @param listIndex
	 *            The TextPiece that had characters inserted into
	 * @param length
	 *            The number of characters inserted
	 */
	public int adjustForInsert(int listIndex, int length) {
		int size = _textPieces.size();

		TextPiece tp = (TextPiece) _textPieces.get(listIndex);

		// Update with the new end
		tp.setEnd(tp.getEnd() + length);

		// Now change all subsequent ones
		for (int x = listIndex + 1; x < size; x++) {
			tp = (TextPiece) _textPieces.get(x);
			tp.setStart(tp.getStart() + length);
			tp.setEnd(tp.getEnd() + length);
		}

		// All done
		return length;
	}

	public boolean equals(Object o) {
		TextPieceTable tpt = (TextPieceTable) o;

		int size = tpt._textPieces.size();
		if (size == _textPieces.size()) {
			for (int x = 0; x < size; x++) {
				if (!tpt._textPieces.get(x).equals(_textPieces.get(x))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public int getCharIndex(int bytePos) {
		int charCount = 0;

		Iterator it = _textPieces.iterator();
		while (it.hasNext()) {
			TextPiece tp = (TextPiece) it.next();
			int pieceStart = tp.getPieceDescriptor().getFilePosition();
			if (pieceStart >= bytePos) {
				break;
			}

			int bytesLength = tp.bytesLength();
			int pieceEnd = pieceStart + bytesLength;

			int toAdd = bytePos > pieceEnd ? bytesLength : bytesLength - (pieceEnd - bytePos);

			if (tp.isUnicode()) {
				charCount += toAdd / 2;
			} else {
				charCount += toAdd;
			}
		}

		return charCount;
	}

}
