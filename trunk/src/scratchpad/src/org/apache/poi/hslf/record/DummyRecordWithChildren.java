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
 * If we come across a record we know has children of (potential)
 *  interest, but where the record itself is boring, we create one
 *  of these. It allows us to get at the children, but not much else
 *
 * @author Nick Burch
 */

public final class DummyRecordWithChildren extends RecordContainer
{
	private byte[] _header;
	private long _type;

	/**
	 * Create a new holder for a boring record with children
	 */
	protected DummyRecordWithChildren(byte[] source, int start, int len) {
		// Just grab the header, not the whole contents
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);
		_type = LittleEndian.getUShort(_header,2);

		// Find our children
		_children = Record.findChildRecords(source,start+8,len-8);
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
		writeOut(_header[0],_header[1],_type,_children,out);
	}
}
