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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The piece table for matching up character positions to bits of text. This
 * mostly works in bytes, but the TextPieces themselves work in characters. This
 * does the icky convertion.
 *
 * @author Ryan Ackley
 */
public class TextPieceTable implements CharIndexTranslator {
	protected ArrayList<TextPiece> _textPieces = new ArrayList<TextPiece>();
    protected ArrayList<TextPiece> _textPiecesFCOrder = new ArrayList<TextPiece>();
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
        Collections.sort(_textPieces);
        _textPiecesFCOrder = new ArrayList<TextPiece>(_textPieces);
        Collections.sort(_textPiecesFCOrder, new FCComparator());
	}

	public int getCpMin() {
		return _cpMin;
	}

	public List<TextPiece> getTextPieces() {
		return _textPieces;
	}

    public void add(TextPiece piece) {
        _textPieces.add(piece);
        _textPiecesFCOrder.add(piece);
        Collections.sort(_textPieces);
        Collections.sort(_textPiecesFCOrder, new FCComparator());
    }

	public byte[] writeTo(HWPFOutputStream docStream) throws IOException {

		PlexOfCps textPlex = new PlexOfCps(PieceDescriptor.getSizeInBytes());
		// int fcMin = docStream.getOffset();

		int size = _textPieces.size();
		for (int x = 0; x < size; x++) {
			TextPiece next = _textPieces.get(x);
			PieceDescriptor pd = next.getPieceDescriptor();

			int offset = docStream.getOffset();
			int mod = (offset % POIFSConstants.SMALLER_BIG_BLOCK_SIZE);
			if (mod != 0) {
				mod = POIFSConstants.SMALLER_BIG_BLOCK_SIZE - mod;
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

		TextPiece tp = _textPieces.get(listIndex);

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
        return getCharIndex(bytePos, 0);
    }

    public int getCharIndex(int bytePos, int startCP) {
        int charCount = 0;

        bytePos = lookIndexForward(bytePos);

        for(TextPiece tp : _textPieces) {
            int pieceStart = tp.getPieceDescriptor().getFilePosition();

            int bytesLength = tp.bytesLength();
            int pieceEnd = pieceStart + bytesLength;

            int toAdd;

            if (bytePos< pieceStart || bytePos > pieceEnd) {
                toAdd = bytesLength;
            } else if (bytePos > pieceStart && bytePos < pieceEnd) {
               toAdd = (bytePos - pieceStart);
            } else {
                toAdd = bytesLength - (pieceEnd - bytePos);
            }

            if (tp.isUnicode()) {
                charCount += toAdd / 2;
            } else {
                charCount += toAdd;
            }

            if (bytePos>=pieceStart && bytePos<=pieceEnd && charCount>=startCP) {
                break;
            }
        }

        return charCount;
    }

    public int lookIndexForward(int bytePos) {
        for(TextPiece tp : _textPiecesFCOrder) {
			int pieceStart = tp.getPieceDescriptor().getFilePosition();

            if (bytePos > pieceStart + tp.bytesLength()) {
                continue;
            }

			if (pieceStart > bytePos) {
				bytePos = pieceStart;
			}

            break;
        }
        return bytePos;
    }

    public int lookIndexBackward(int bytePos) {
        int lastEnd = 0;

        for(TextPiece tp : _textPiecesFCOrder) {
			int pieceStart = tp.getPieceDescriptor().getFilePosition();

            if (bytePos > pieceStart + tp.bytesLength()) {
                lastEnd = pieceStart + tp.bytesLength();
                continue;
            }

			if (pieceStart > bytePos) {
				bytePos = lastEnd;
			}

            break;
        }

        return bytePos;
    }

    public boolean isIndexInTable(int bytePos) {
        for(TextPiece tp : _textPiecesFCOrder) {
			int pieceStart = tp.getPieceDescriptor().getFilePosition();

            if (bytePos > pieceStart + tp.bytesLength()) {
                continue;
            }

			if (pieceStart > bytePos) {
				return false;
			}

            return true;
        }

        return false;
    }

    private static class FCComparator implements Comparator<TextPiece> {
        public int compare(TextPiece textPiece, TextPiece textPiece1) {
            if (textPiece.getPieceDescriptor().fc>textPiece1.getPieceDescriptor().fc) {
                return 1;
            } else if (textPiece.getPieceDescriptor().fc<textPiece1.getPieceDescriptor().fc) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
