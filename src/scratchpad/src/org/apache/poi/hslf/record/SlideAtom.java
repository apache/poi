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
 * A Slide Atom (type 1007). Holds information on the parent Slide, what
 *  Master Slide it uses, what Notes is attached to it, that sort of thing.
 *  It also has a SSlideLayoutAtom embeded in it, but without the Atom header
 *
 * @author Nick Burch
 */

public final class SlideAtom extends RecordAtom
{
	private byte[] _header;
	private static long _type = 1007l;
	public static final int MASTER_SLIDE_ID = 0;
	public static final int USES_MASTER_SLIDE_ID = -2147483648;

	private int masterID;
	private int notesID;

	private boolean followMasterObjects;
	private boolean followMasterScheme;
	private boolean followMasterBackground;
	private SSlideLayoutAtom layoutAtom;
	private byte[] reserved;


	/** Get the ID of the master slide used. 0 if this is a master slide, otherwise -2147483648 */
	public int getMasterID() { return masterID; }
    /** Change slide master.  */
    public void setMasterID(int id) { masterID = id; }
	/** Get the ID of the notes for this slide. 0 if doesn't have one */
	public int getNotesID()  { return notesID; }
	/** Get the embeded SSlideLayoutAtom */
	public SSlideLayoutAtom getSSlideLayoutAtom() { return layoutAtom; }

	/** Change the ID of the notes for this slide. 0 if it no longer has one */
	public void setNotesID(int id) { notesID = id; }

	public boolean getFollowMasterObjects()    { return followMasterObjects; }
	public boolean getFollowMasterScheme()     { return followMasterScheme; }
	public boolean getFollowMasterBackground() { return followMasterBackground; }
	public void setFollowMasterObjects(boolean flag)    { followMasterObjects = flag; }
	public void setFollowMasterScheme(boolean flag)     { followMasterScheme = flag; }
	public void setFollowMasterBackground(boolean flag) { followMasterBackground = flag; }


	/* *************** record code follows ********************** */

	/**
	 * For the Slide Atom
	 */
	protected SlideAtom(byte[] source, int start, int len) {
		// Sanity Checking
		if(len < 30) { len = 30; }

		// Get the header
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		// Grab the 12 bytes that is "SSlideLayoutAtom"
		byte[] SSlideLayoutAtomData = new byte[12];
		System.arraycopy(source,start+8,SSlideLayoutAtomData,0,12);
		// Use them to build up the SSlideLayoutAtom
		layoutAtom = new SSlideLayoutAtom(SSlideLayoutAtomData);

		// Get the IDs of the master and notes
		masterID = LittleEndian.getInt(source,start+12+8);
		notesID = LittleEndian.getInt(source,start+16+8);

		// Grok the flags, stored as bits
		int flags = LittleEndian.getUShort(source,start+20+8);
		if((flags&4) == 4) {
			followMasterBackground = true;
		} else {
			followMasterBackground = false;
		}
		if((flags&2) == 2) {
			followMasterScheme = true;
		} else {
			followMasterScheme = false;
		}
		if((flags&1) == 1) {
			followMasterObjects = true;
		} else {
			followMasterObjects = false;
		}

		// If there's any other bits of data, keep them about
		// 8 bytes header + 20 bytes to flags + 2 bytes flags = 30 bytes
		reserved = new byte[len-30];
		System.arraycopy(source,start+30,reserved,0,reserved.length);
	}

	/**
	 * Create a new SlideAtom, to go with a new Slide
	 */
	public SlideAtom(){
		_header = new byte[8];
		LittleEndian.putUShort(_header, 0, 2);
		LittleEndian.putUShort(_header, 2, (int)_type);
		LittleEndian.putInt(_header, 4, 24);

		byte[] ssdate = new byte[12];
		layoutAtom = new SSlideLayoutAtom(ssdate);
		layoutAtom.setGeometryType(SSlideLayoutAtom.BLANK_SLIDE);

		followMasterObjects = true;
		followMasterScheme = true;
		followMasterBackground = true;
		masterID = -2147483648;
		notesID = 0;
		reserved = new byte[2];
	}

	/**
	 * We are of type 1007
	 */
	public long getRecordType() { return _type; }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		// Header
		out.write(_header);

		// SSSlideLayoutAtom stuff
		layoutAtom.writeOut(out);

		// IDs
		writeLittleEndian(masterID,out);
		writeLittleEndian(notesID,out);

		// Flags
		short flags = 0;
		if(followMasterObjects)    { flags += 1; }
		if(followMasterScheme)     { flags += 2; }
		if(followMasterBackground) { flags += 4; }
		writeLittleEndian(flags,out);

		// Reserved data
		out.write(reserved);
	}


	/**
	 * Holds the geometry of the Slide, and the ID of the placeholders
	 *  on the slide.
	 * (Embeded inside SlideAtom is a SSlideLayoutAtom, without the
	 *  usual record header. Since it's a fixed size and tied to
	 *  the SlideAtom, we'll hold it here.)
	 */
	public class SSlideLayoutAtom {
		// The different kinds of geometry
		public static final int TITLE_SLIDE = 0;
		public static final int TITLE_BODY_SLIDE = 1;
		public static final int TITLE_MASTER_SLIDE = 2;
		public static final int MASTER_SLIDE = 3;
		public static final int MASTER_NOTES = 4;
		public static final int NOTES_TITLE_BODY = 5;
		public static final int HANDOUT = 6; // Only header, footer and date placeholders
		public static final int TITLE_ONLY = 7;
		public static final int TITLE_2_COLUMN_BODY = 8;
		public static final int TITLE_2_ROW_BODY = 9;
		public static final int TITLE_2_COLUNM_RIGHT_2_ROW_BODY = 10;
		public static final int TITLE_2_COLUNM_LEFT_2_ROW_BODY = 11;
		public static final int TITLE_2_ROW_BOTTOM_2_COLUMN_BODY = 12;
		public static final int TITLE_2_ROW_TOP_2_COLUMN_BODY = 13;
		public static final int FOUR_OBJECTS = 14;
		public static final int BIG_OBJECT = 15;
		public static final int BLANK_SLIDE = 16;
		public static final int VERTICAL_TITLE_BODY_LEFT = 17;
		public static final int VERTICAL_TITLE_2_ROW_BODY_LEFT = 17;

		/** What geometry type we are */
		private int geometry;
		/** What placeholder IDs we have */
		private byte[] placeholderIDs;

		/** Retrieve the geometry type */
		public int getGeometryType() { return geometry; }
		/** Set the geometry type */
		public void setGeometryType(int geom) { geometry = geom; }

		/**
		 * Create a new Embeded SSlideLayoutAtom, from 12 bytes of data
		 */
		public SSlideLayoutAtom(byte[] data) {
			if(data.length != 12) {
				throw new RuntimeException("SSlideLayoutAtom created with byte array not 12 bytes long - was " + data.length + " bytes in size");
			}

			// Grab out our data
			geometry = LittleEndian.getInt(data,0);
			placeholderIDs = new byte[8];
			System.arraycopy(data,4,placeholderIDs,0,8);
		}

		/**
		 * Write the contents of the record back, so it can be written
		 *  to disk. Skips the record header
		 */
		public void writeOut(OutputStream out) throws IOException {
			// Write the geometry
			writeLittleEndian(geometry,out);
			// Write the placeholder IDs
			out.write(placeholderIDs);
		}
	}
}
