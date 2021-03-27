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

import org.apache.poi.util.LittleEndian;

/**
 * A container record that specifies information about the document and document display settings.
 */
public final class DocInfoListContainer extends RecordContainer {
	private byte[] _header;
	private static final long _type = RecordTypes.List.typeID;

	// Links to our more interesting children

	/**
	 * Set things up, and find our more interesting children
	 */
	protected DocInfoListContainer(byte[] source, int start, int len) {
		// Grab the header
		_header = Arrays.copyOfRange(source,start,start+8);

		// Find our children
		_children = Record.findChildRecords(source,start+8,len-8);
		findInterestingChildren();
	}

	/**
	 * Go through our child records, picking out the ones that are
	 *  interesting, and saving those for use by the easy helper
	 *  methods.
	 */
	private void findInterestingChildren() {

	}

	/**
	 * Create a new DocInfoListContainer, with blank fields - not yet supported
	 */
	private DocInfoListContainer() {
		_header = new byte[8];
		_children = new org.apache.poi.hslf.record.Record[0];

		// Setup our header block
		_header[0] = 0x0f; // We are a container record
		LittleEndian.putShort(_header, 2, (short)_type);

		// Setup our child records
		findInterestingChildren();
	}

	/**
	 * We are of type 0x7D0
	 */
	public long getRecordType() { return _type; }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		writeOut(_header[0],_header[1],_type,_children,out);
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return null;
	}
}
