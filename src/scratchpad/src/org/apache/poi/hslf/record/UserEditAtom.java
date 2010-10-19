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
import java.util.Hashtable;

/**
 * A UserEdit Atom (type 4085). Holds information which bits of the file
 *  were last used by powerpoint, the version of powerpoint last used etc.
 *
 * ** WARNING ** stores byte offsets from the start of the PPT stream to
 *  other records! If you change the size of any elements before one of
 *  these, you'll need to update the offsets!
 *
 * @author Nick Burch
 */

public final class UserEditAtom extends PositionDependentRecordAtom
{
	public static final int LAST_VIEW_NONE = 0;
	public static final int LAST_VIEW_SLIDE_VIEW = 1;
	public static final int LAST_VIEW_OUTLINE_VIEW = 2;
	public static final int LAST_VIEW_NOTES = 3;

	private byte[] _header;
	private static long _type = 4085l;
	private byte[] reserved;

	private int lastViewedSlideID;
	private int pptVersion;
	private int lastUserEditAtomOffset;
	private int persistPointersOffset;
	private int docPersistRef;
	private int maxPersistWritten;
	private short lastViewType;

	// Somewhat user facing getters
	public int getLastViewedSlideID() { return lastViewedSlideID; }
	public short getLastViewType()    { return lastViewType; }

	// Scary internal getters
	public int getLastUserEditAtomOffset() { return lastUserEditAtomOffset; }
	public int getPersistPointersOffset()  { return persistPointersOffset; }
	public int getDocPersistRef()          { return docPersistRef; }
	public int getMaxPersistWritten()      { return maxPersistWritten; }

	// More scary internal setters
	public void setLastUserEditAtomOffset(int offset) { lastUserEditAtomOffset = offset; }
	public void setPersistPointersOffset(int offset)  { persistPointersOffset = offset; }
	public void setLastViewType(short type)           { lastViewType=type; }
    public void setMaxPersistWritten(int max)           { maxPersistWritten=max; }

	/* *************** record code follows ********************** */

	/**
	 * For the UserEdit Atom
	 */
	protected UserEditAtom(byte[] source, int start, int len) {
		// Sanity Checking
		if(len < 34) { len = 34; }

		// Get the header
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		// Get the last viewed slide ID
		lastViewedSlideID = LittleEndian.getInt(source,start+0+8);

		// Get the PPT version
		pptVersion = LittleEndian.getInt(source,start+4+8);

		// Get the offset to the previous incremental save's UserEditAtom
		// This will be the byte offset on disk where the previous one
		//  starts, or 0 if this is the first one
		lastUserEditAtomOffset = LittleEndian.getInt(source,start+8+8);

		// Get the offset to the persist pointers
		// This will be the byte offset on disk where the preceding
		//  PersistPtrFullBlock or PersistPtrIncrementalBlock starts
		persistPointersOffset = LittleEndian.getInt(source,start+12+8);

		// Get the persist reference for the document persist object
		// Normally seems to be 1
		docPersistRef = LittleEndian.getInt(source,start+16+8);

		// Maximum number of persist objects written
		maxPersistWritten = LittleEndian.getInt(source,start+20+8);

		// Last view type
		lastViewType = LittleEndian.getShort(source,start+24+8);

		// There might be a few more bytes, which are a reserved field
		reserved = new byte[len-26-8];
		System.arraycopy(source,start+26+8,reserved,0,reserved.length);
	}

	/**
	 * We are of type 4085
	 */
	public long getRecordType() { return _type; }

	/**
	 * At write-out time, update the references to PersistPtrs and
	 *  other UserEditAtoms to point to their new positions
	 */
	public void updateOtherRecordReferences(Hashtable<Integer,Integer> oldToNewReferencesLookup) {
		// Look up the new positions of our preceding UserEditAtomOffset
		if(lastUserEditAtomOffset != 0) {
			Integer newLocation = oldToNewReferencesLookup.get(Integer.valueOf(lastUserEditAtomOffset));
			if(newLocation == null) {
				throw new RuntimeException("Couldn't find the new location of the UserEditAtom that used to be at " + lastUserEditAtomOffset);
			}
			lastUserEditAtomOffset = newLocation.intValue();
		}

		// Ditto for our PersistPtr
		Integer newLocation = oldToNewReferencesLookup.get(Integer.valueOf(persistPointersOffset));
		if(newLocation == null) {
			throw new RuntimeException("Couldn't find the new location of the PersistPtr that used to be at " + persistPointersOffset);
		}
		persistPointersOffset = newLocation.intValue();
	}

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		// Header
		out.write(_header);

		// Write out the values
		writeLittleEndian(lastViewedSlideID,out);
		writeLittleEndian(pptVersion,out);
		writeLittleEndian(lastUserEditAtomOffset,out);
		writeLittleEndian(persistPointersOffset,out);
		writeLittleEndian(docPersistRef,out);
		writeLittleEndian(maxPersistWritten,out);
		writeLittleEndian(lastViewType,out);

		// Reserved fields
		out.write(reserved);
	}
}
