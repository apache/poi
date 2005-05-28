
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

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
 * General holder for PersistPtrFullBlock and PersistPtrIncrementalBlock
 *  records. We need to handle them specially, since we have to go around
 *  updating UserEditAtoms if they shuffle about on disk
 *
 * @author Nick Burch
 */

public class PersistPtrHolder extends PositionDependentRecordAtom
{
	private byte[] _contents;
	private long _type;

	/** 
	 * Create a new holder for a PersistPtr record
	 */
	protected PersistPtrHolder(byte[] source, int start, int len) {
		// Sanity Checking - including whole header, so treat
		//  length as based of 0, not 8 (including header size based)
		if(len < 4) { len = 4; }

		// Store where we are found on disk
		myLastOnDiskOffset = start;

		// Treat as an atom, grab and hold everything
		_contents = new byte[len];
		System.arraycopy(source,start,_contents,0,len);
		_type = LittleEndian.getUShort(_contents,2);
	}

	/**
	 * Return the value we were given at creation
	 */
	public long getRecordType() { return _type; }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		out.write(_contents);
	}
}
