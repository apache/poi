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
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * A Notes Atom (type 1009). Holds information on the parent Notes, such
 *  as what slide it is tied to
 *
 * @author Nick Burch
 */

public final class NotesAtom extends RecordAtom
{

	//arbitrarily selected; may need to increase
	private static final int MAX_RECORD_LENGTH = 1_000_000;

	private byte[] _header;
	private static long _type = 1009l;

	private int slideID;
	private boolean followMasterObjects;
	private boolean followMasterScheme;
	private boolean followMasterBackground;
	private byte[] reserved;


	public int getSlideID() { return slideID; }
	public void setSlideID(int id) { slideID = id; }

	public boolean getFollowMasterObjects()    { return followMasterObjects; }
	public boolean getFollowMasterScheme()     { return followMasterScheme; }
	public boolean getFollowMasterBackground() { return followMasterBackground; }
	public void setFollowMasterObjects(boolean flag)    { followMasterObjects = flag; }
	public void setFollowMasterScheme(boolean flag)     { followMasterScheme = flag; }
	public void setFollowMasterBackground(boolean flag) { followMasterBackground = flag; }


	/* *************** record code follows ********************** */

	/**
	 * For the Notes Atom
	 */
	protected NotesAtom(byte[] source, int start, int len) {
		// Sanity Checking
		if(len < 8) { len = 8; }

		// Get the header
		_header = Arrays.copyOfRange(source, start, start+8);

		// Get the slide ID
		slideID = LittleEndian.getInt(source,start+8);

		// Grok the flags, stored as bits
		int flags = LittleEndian.getUShort(source,start+12);
		followMasterBackground = (flags & 4) == 4;
		followMasterScheme = (flags & 2) == 2;
		followMasterObjects = (flags & 1) == 1;

		// There might be 2 more bytes, which are a reserved field
		reserved = IOUtils.safelyClone(source, start+14, len-14, MAX_RECORD_LENGTH);
	}

	/**
	 * We are of type 1009
	 */
	public long getRecordType() { return _type; }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		// Header
		out.write(_header);

		// Slide ID
		writeLittleEndian(slideID,out);

		// Flags
		short flags = 0;
		if(followMasterObjects)    { flags += 1; }
		if(followMasterScheme)     { flags += 2; }
		if(followMasterBackground) { flags += 4; }
		writeLittleEndian(flags,out);

		// Reserved fields
		out.write(reserved);
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return GenericRecordUtil.getGenericProperties(
			"slideId", this::getSlideID,
			"followMasterObjects", this::getFollowMasterObjects,
			"followMasterScheme", this::getFollowMasterScheme,
			"followMasterBackground", this::getFollowMasterBackground
		);
	}
}
