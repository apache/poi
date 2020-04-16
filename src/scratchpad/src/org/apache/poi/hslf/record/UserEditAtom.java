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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * A UserEdit Atom (type 4085). Holds information which bits of the file
 *  were last used by powerpoint, the version of powerpoint last used etc.
 *
 * ** WARNING ** stores byte offsets from the start of the PPT stream to
 *  other records! If you change the size of any elements before one of
 *  these, you'll need to update the offsets!
 */

public final class UserEditAtom extends PositionDependentRecordAtom
{
	public static final int LAST_VIEW_NONE = 0;
	public static final int LAST_VIEW_SLIDE_VIEW = 1;
	public static final int LAST_VIEW_OUTLINE_VIEW = 2;
	public static final int LAST_VIEW_NOTES = 3;

	private byte[] _header;
	private static final long _type = RecordTypes.UserEditAtom.typeID;
	private short unused;

	private int lastViewedSlideID;
	private int pptVersion;
	private int lastUserEditAtomOffset;
	private int persistPointersOffset;
	private int docPersistRef;
	private int maxPersistWritten;
	private short lastViewType;
	private int encryptSessionPersistIdRef = -1;

	// Somewhat user facing getters
	public int getLastViewedSlideID() { return lastViewedSlideID; }
	public short getLastViewType()    { return lastViewType; }

	// Scary internal getters
	public int getLastUserEditAtomOffset() { return lastUserEditAtomOffset; }
	public int getPersistPointersOffset()  { return persistPointersOffset; }
	public int getDocPersistRef()          { return docPersistRef; }
	public int getMaxPersistWritten()      { return maxPersistWritten; }
	public int getEncryptSessionPersistIdRef() { return encryptSessionPersistIdRef; }

	// More scary internal setters
	public void setLastUserEditAtomOffset(int offset) { lastUserEditAtomOffset = offset; }
	public void setPersistPointersOffset(int offset)  { persistPointersOffset = offset; }
	public void setLastViewType(short type)           { lastViewType=type; }
    public void setMaxPersistWritten(int max)         { maxPersistWritten=max; }
    public void setEncryptSessionPersistIdRef(int id) {
        encryptSessionPersistIdRef=id;
        LittleEndian.putInt(_header,4,(id == -1 ? 28 : 32));
    }

	/* *************** record code follows ********************** */

	/**
	 * For the UserEdit Atom
	 */
	protected UserEditAtom(byte[] source, int start, int len) {
		// Sanity Checking
		if(len < 34) { len = 34; }

		int offset = start;
		// Get the header
		_header = Arrays.copyOfRange(source, start, start+8);
		offset += 8;

		// Get the last viewed slide ID
		lastViewedSlideID = LittleEndian.getInt(source,offset);
		offset += LittleEndianConsts.INT_SIZE;

		// Get the PPT version
		pptVersion = LittleEndian.getInt(source,offset);
		offset += LittleEndianConsts.INT_SIZE;

		// Get the offset to the previous incremental save's UserEditAtom
		// This will be the byte offset on disk where the previous one
		//  starts, or 0 if this is the first one
		lastUserEditAtomOffset = LittleEndian.getInt(source,offset);
		offset += LittleEndianConsts.INT_SIZE;

		// Get the offset to the persist pointers
		// This will be the byte offset on disk where the preceding
		//  PersistPtrFullBlock or PersistPtrIncrementalBlock starts
		persistPointersOffset = LittleEndian.getInt(source,offset);
		offset += LittleEndianConsts.INT_SIZE;

		// Get the persist reference for the document persist object
		// Normally seems to be 1
		docPersistRef = LittleEndian.getInt(source,offset);
		offset += LittleEndianConsts.INT_SIZE;

		// Maximum number of persist objects written
		maxPersistWritten = LittleEndian.getInt(source,offset);
		offset += LittleEndianConsts.INT_SIZE;

		// Last view type
		lastViewType = LittleEndian.getShort(source,offset);
		offset += LittleEndianConsts.SHORT_SIZE;

		// unused
		unused = LittleEndian.getShort(source,offset);
		offset += LittleEndianConsts.SHORT_SIZE;

		// There might be a few more bytes, which are a reserved field
		if (offset-start<len) {
		    encryptSessionPersistIdRef = LittleEndian.getInt(source,offset);
		    offset += LittleEndianConsts.INT_SIZE;
		}

		assert(offset-start == len);
	}

	/**
	 * We are of type 4085
	 */
	@Override
    public long getRecordType() { return _type; }

	/**
	 * At write-out time, update the references to PersistPtrs and
	 *  other UserEditAtoms to point to their new positions
	 */
	@Override
    public void updateOtherRecordReferences(Map<Integer,Integer> oldToNewReferencesLookup) {
		// Look up the new positions of our preceding UserEditAtomOffset
		if(lastUserEditAtomOffset != 0) {
			Integer newLocation = oldToNewReferencesLookup.get(Integer.valueOf(lastUserEditAtomOffset));
			if(newLocation == null) {
				throw new HSLFException("Couldn't find the new location of the UserEditAtom that used to be at " + lastUserEditAtomOffset);
			}
			lastUserEditAtomOffset = newLocation.intValue();
		}

		// Ditto for our PersistPtr
		Integer newLocation = oldToNewReferencesLookup.get(Integer.valueOf(persistPointersOffset));
		if(newLocation == null) {
			throw new HSLFException("Couldn't find the new location of the PersistPtr that used to be at " + persistPointersOffset);
		}
		persistPointersOffset = newLocation.intValue();
	}

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	@Override
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
		writeLittleEndian(unused,out);
		if (encryptSessionPersistIdRef != -1) {
		    // optional field
		    writeLittleEndian(encryptSessionPersistIdRef,out);
		}
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		final Map<String, Supplier<?>> m = new LinkedHashMap<>();
		m.put("lastViewedSlideID", this::getLastViewedSlideID);
		m.put("pptVersion", () -> pptVersion);
		m.put("lastUserEditAtomOffset", this::getLastUserEditAtomOffset);
		m.put("persistPointersOffset", this::getPersistPointersOffset);
		m.put("docPersistRef", this::getDocPersistRef);
		m.put("maxPersistWritten", this::getMaxPersistWritten);
		m.put("lastViewType", this::getLastViewType);
		m.put("encryptSessionPersistIdRef", this::getEncryptSessionPersistIdRef);
		return Collections.unmodifiableMap(m);
	}
}
