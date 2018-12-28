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

package org.apache.poi.hslf.record;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndianByteArrayInputStream;

/**
 * A Document Atom (type 1001). Holds misc information on the PowerPoint
 * document, lots of them size and scale related.
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public final class DocumentAtom extends RecordAtom
{
	//arbitrarily selected; may need to increase
	private static final int MAX_RECORD_LENGTH = 1_000_000;

	private final byte[] _header = new byte[8];
	private static long _type = RecordTypes.DocumentAtom.typeID;

	private long slideSizeX; // PointAtom, assume 1st 4 bytes = X
	private long slideSizeY; // PointAtom, assume 2nd 4 bytes = Y
	private long notesSizeX; // PointAtom, assume 1st 4 bytes = X
	private long notesSizeY; // PointAtom, assume 2nd 4 bytes = Y
	private long serverZoomFrom; // RatioAtom, assume 1st 4 bytes = from
	private long serverZoomTo;   // RatioAtom, assume 2nd 4 bytes = to

	private long notesMasterPersist; // ref to NotesMaster, 0 if none
	private long handoutMasterPersist; // ref to HandoutMaster, 0 if none

	private int firstSlideNum;
	private int slideSizeType; // see DocumentAtom.SlideSize

	private byte saveWithFonts;
	private byte omitTitlePlace;
	private byte rightToLeft;
	private byte showComments;

	private byte[] reserved;


	public long getSlideSizeX() { return slideSizeX; }
	public long getSlideSizeY() { return slideSizeY; }
	public long getNotesSizeX() { return notesSizeX; }
	public long getNotesSizeY() { return notesSizeY; }
	public void setSlideSizeX(long x) { slideSizeX = x; }
	public void setSlideSizeY(long y) { slideSizeY = y; }
	public void setNotesSizeX(long x) { notesSizeX = x; }
	public void setNotesSizeY(long y) { notesSizeY = y; }

	public long getServerZoomFrom() { return serverZoomFrom; }
	public long getServerZoomTo()   { return serverZoomTo; }
	public void setServerZoomFrom(long zoom) { serverZoomFrom = zoom; }
	public void setServerZoomTo(long zoom)   { serverZoomTo   = zoom; }

	/** Returns a reference to the NotesMaster, or 0 if none */
	public long getNotesMasterPersist() { return notesMasterPersist; }
	/** Returns a reference to the HandoutMaster, or 0 if none */
	public long getHandoutMasterPersist() { return handoutMasterPersist; }

	public int getFirstSlideNum() { return firstSlideNum; }

	/** The Size of the Document's slides, @see DocumentAtom.SlideSize for values */
	public int getSlideSizeType() { return slideSizeType; }

	/** Was the document saved with True Type fonts embeded? */
	public boolean getSaveWithFonts() {
		return saveWithFonts != 0;
	}

	/** Set the font embedding state */
	public void setSaveWithFonts(boolean saveWithFonts) {
		this.saveWithFonts = (byte)(saveWithFonts ? 1 : 0);
	}

	/** Have the placeholders on the title slide been omitted? */
	public boolean getOmitTitlePlace() {
		return omitTitlePlace != 0;
	}

	/** Is this a Bi-Directional PPT Doc? */
	public boolean getRightToLeft() {
		return rightToLeft != 0;
	}

	/** Are comment shapes visible? */
	public boolean getShowComments() {
		return showComments != 0;
	}


	/* *************** record code follows ********************** */

	/**
	 * For the Document Atom
	 */
	/* package */ DocumentAtom(byte[] source, int start, int len) {
		final int maxLen = Math.max(len, 48);
		LittleEndianByteArrayInputStream leis =
			new LittleEndianByteArrayInputStream(source, start, maxLen);

		// Get the header
		leis.readFully(_header);

		// Get the sizes and zoom ratios
		slideSizeX = leis.readInt();
		slideSizeY = leis.readInt();
		notesSizeX = leis.readInt();
		notesSizeY = leis.readInt();
		serverZoomFrom = leis.readInt();
		serverZoomTo = leis.readInt();

		// Get the master persists
		notesMasterPersist = leis.readInt();
		handoutMasterPersist = leis.readInt();

		// Get the ID of the first slide
		firstSlideNum = leis.readShort();

		// Get the slide size type
		slideSizeType = leis.readShort();

		// Get the booleans as bytes
		saveWithFonts = leis.readByte();
		omitTitlePlace = leis.readByte();
		rightToLeft = leis.readByte();
		showComments = leis.readByte();

		// If there's any other bits of data, keep them about
		reserved = IOUtils.safelyAllocate(maxLen-48, MAX_RECORD_LENGTH);
		leis.readFully(reserved);
	}

	/**
	 * We are of type 1001
	 */
	public long getRecordType() { return _type; }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		// Header
		out.write(_header);

		// The sizes and zoom ratios
		writeLittleEndian((int)slideSizeX,out);
		writeLittleEndian((int)slideSizeY,out);
		writeLittleEndian((int)notesSizeX,out);
		writeLittleEndian((int)notesSizeY,out);
		writeLittleEndian((int)serverZoomFrom,out);
		writeLittleEndian((int)serverZoomTo,out);

		// The master persists
		writeLittleEndian((int)notesMasterPersist,out);
		writeLittleEndian((int)handoutMasterPersist,out);

		// The ID of the first slide
		writeLittleEndian((short)firstSlideNum,out);

		// The slide size type
		writeLittleEndian((short)slideSizeType,out);

		// The booleans as bytes
		out.write(saveWithFonts);
		out.write(omitTitlePlace);
		out.write(rightToLeft);
		out.write(showComments);

		// Reserved data
		out.write(reserved);
	}

	/**
	 * Holds the different Slide Size values
	 */
	public static final class SlideSize {
		public static final int ON_SCREEN = 0;
		public static final int LETTER_SIZED_PAPER = 1;
		public static final int A4_SIZED_PAPER = 2;
		public static final int ON_35MM = 3;
		public static final int OVERHEAD = 4;
		public static final int BANNER = 5;
		public static final int CUSTOM = 6;
	}
}
