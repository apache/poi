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
 * A SlidePersist Atom (type 1011). Holds information on the text of a
 *  given slide, which are stored in the same SlideListWithText
 *
 * @author Nick Burch
 */
public final class SlidePersistAtom extends RecordAtom {
	private byte[] _header;
	private static long _type = 1011l;

	/**
	 * Slide reference ID. Should correspond to the PersistPtr
	 *  "sheet ID" of the matching slide/notes record
	 */
	private int refID;
	private boolean hasShapesOtherThanPlaceholders;
	/** Number of placeholder texts that will follow in the SlideListWithText */
	private int numPlaceholderTexts;
	/**
	 * The internal identifier (256+), which is used to tie slides
	 *  and notes together
	 */
	private int slideIdentifier;
	/** Reserved fields. Who knows what they do */
	private byte[] reservedFields;

	public int getRefID() { return refID; }
	public int getSlideIdentifier() { return slideIdentifier; }
	public int getNumPlaceholderTexts() { return numPlaceholderTexts; }
	public boolean getHasShapesOtherThanPlaceholders() { return hasShapesOtherThanPlaceholders; }

	// Only set these if you know what you're doing!
	public void setRefID(int id) {
		refID = id;
	}
	public void setSlideIdentifier(int id) {
		slideIdentifier = id;
	}

	/* *************** record code follows ********************** */

	/**
	 * For the SlidePersist Atom
	 */
	protected SlidePersistAtom(byte[] source, int start, int len) {
		// Sanity Checking
		if(len < 8) { len = 8; }

		// Get the header
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		// Grab the reference ID
		refID = LittleEndian.getInt(source,start+8);

		// Next up is a set of flags, but only bit 3 is used!
		int flags = LittleEndian.getInt(source,start+12);
		if(flags == 4) {
			hasShapesOtherThanPlaceholders = true;
		} else {
			hasShapesOtherThanPlaceholders = false;
		}

		// Now the number of Placeholder Texts
		numPlaceholderTexts = LittleEndian.getInt(source,start+16);

		// Last useful one is the unique slide identifier
		slideIdentifier = LittleEndian.getInt(source,start+20);

		// Finally you have typically 4 or 8 bytes of reserved fields,
		//  all zero running from 24 bytes in to the end
		reservedFields = new byte[len-24];
		System.arraycopy(source,start+24,reservedFields,0,reservedFields.length);
	}

	/**
	 * Create a new SlidePersistAtom, for use with a new Slide
	 */
	public SlidePersistAtom(){
		_header = new byte[8];
		LittleEndian.putUShort(_header, 0, 0);
		LittleEndian.putUShort(_header, 2, (int)_type);
		LittleEndian.putInt(_header, 4, 20);

		hasShapesOtherThanPlaceholders = true;
		reservedFields = new byte[4];
	}

	/**
	 * We are of type 1011
	 */
	public long getRecordType() { return _type; }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		// Header - size or type unchanged
		out.write(_header);

		// Compute the flags part - only bit 3 is used
		int flags = 0;
		if(hasShapesOtherThanPlaceholders) {
			flags = 4;
		}

		// Write out our fields
		writeLittleEndian(refID,out);
		writeLittleEndian(flags,out);
		writeLittleEndian(numPlaceholderTexts,out);
		writeLittleEndian(slideIdentifier,out);
		out.write(reservedFields);
	}
}
