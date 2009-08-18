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

import org.apache.poi.util.LittleEndian;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A Document Atom (type 1001). Holds misc information on the PowerPoint
 * document, lots of them size and scale related.
 *
 * @author Nick Burch
 */

public final class DocumentAtom extends RecordAtom
{
	private byte[] _header;
	private static long _type = 1001l;

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
	protected DocumentAtom(byte[] source, int start, int len) {
		// Sanity Checking
		if(len < 48) { len = 48; }

		// Get the header
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		// Get the sizes and zoom ratios
		slideSizeX = LittleEndian.getInt(source,start+0+8);
		slideSizeY = LittleEndian.getInt(source,start+4+8);
		notesSizeX = LittleEndian.getInt(source,start+8+8);
		notesSizeY = LittleEndian.getInt(source,start+12+8);
		serverZoomFrom = LittleEndian.getInt(source,start+16+8);
		serverZoomTo   = LittleEndian.getInt(source,start+20+8);

		// Get the master persists
		notesMasterPersist = LittleEndian.getInt(source,start+24+8);
		handoutMasterPersist = LittleEndian.getInt(source,start+28+8);

		// Get the ID of the first slide
		firstSlideNum = LittleEndian.getShort(source,start+32+8);

		// Get the slide size type
		slideSizeType = LittleEndian.getShort(source,start+34+8);

		// Get the booleans as bytes
		saveWithFonts = source[start+36+8];
		omitTitlePlace = source[start+37+8];
		rightToLeft = source[start+38+8];
		showComments = source[start+39+8];

		// If there's any other bits of data, keep them about
		reserved = new byte[len-40-8];
		System.arraycopy(source,start+48,reserved,0,reserved.length);
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
