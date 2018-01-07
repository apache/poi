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

import org.apache.poi.hslf.record.SlideAtomLayout.SlideLayoutType;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * A Slide Atom (type 1007). Holds information on the parent Slide, what
 * Master Slide it uses, what Notes is attached to it, that sort of thing.
 * It also has a SSlideLayoutAtom embedded in it, but without the Atom header
 */

public final class SlideAtom extends RecordAtom {
    public static final int USES_MASTER_SLIDE_ID  =  0x80000000;
    // private static final int MASTER_SLIDE_ID      =  0x00000000;

	//arbitrarily selected; may need to increase
	private static final int MAX_RECORD_LENGTH = 1_000_000;

	private byte[] _header;
	private static long _type = 1007l;

	private int masterID;
	private int notesID;

	private boolean followMasterObjects;
	private boolean followMasterScheme;
	private boolean followMasterBackground;
	private SlideAtomLayout layoutAtom;
	private byte[] reserved;


	/** Get the ID of the master slide used. 0 if this is a master slide, otherwise -2147483648 */
	public int getMasterID() { return masterID; }
    /** Change slide master.  */
    public void setMasterID(int id) { masterID = id; }
	/** Get the ID of the notes for this slide. 0 if doesn't have one */
	public int getNotesID()  { return notesID; }
	/** Get the embedded SSlideLayoutAtom */
	public SlideAtomLayout getSSlideLayoutAtom() { return layoutAtom; }

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
		layoutAtom = new SlideAtomLayout(SSlideLayoutAtomData);

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
		reserved = IOUtils.safelyAllocate(len-30, MAX_RECORD_LENGTH);
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
		layoutAtom = new SlideAtomLayout(ssdate);
		layoutAtom.setGeometryType(SlideLayoutType.BLANK_SLIDE);

		followMasterObjects = true;
		followMasterScheme = true;
		followMasterBackground = true;
		masterID = USES_MASTER_SLIDE_ID; // -2147483648;
		notesID = 0;
		reserved = new byte[2];
	}

	/**
	 * We are of type 1007
	 */
	@Override
    public long getRecordType() { return _type; }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	@Override
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
}
