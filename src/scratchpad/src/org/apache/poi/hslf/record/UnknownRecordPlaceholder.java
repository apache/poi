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
 * If we come across a record we don't know about, we create one of
 *  these. It allows us to keep track of what it contains, so we can
 *  write it back out to disk unchanged
 *
 * @author Nick Burch
 */

public final class UnknownRecordPlaceholder extends RecordAtom
{
	private byte[] _contents;
	private long _type;

	/**
	 * Create a new holder for a record we don't grok
	 */
	protected UnknownRecordPlaceholder(byte[] source, int start, int len) {
		// Sanity Checking - including whole header, so treat
		//  length as based of 0, not 8 (including header size based)
		if(len < 0) { len = 0; }

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
