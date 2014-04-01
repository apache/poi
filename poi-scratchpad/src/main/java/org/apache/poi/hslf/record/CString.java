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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * A CString (type 4026). Holds a unicode string, and the first two bytes
 *  of the record header normally encode the count. Typically attached to
 *  some complex sequence of records, eg Commetns.
 *
 * @author Nick Burch
 */

public final class CString extends RecordAtom {
	private byte[] _header;
	private static long _type = 4026l;

	/** The bytes that make up the text */
	private byte[] _text;

	/** Grabs the text. Never <code>null</code> */
	public String getText() {
		return StringUtil.getFromUnicodeLE(_text);
	}

	/** Updates the text in the Atom. */
	public void setText(String text) {
		// Convert to little endian unicode
		_text = new byte[text.length()*2];
		StringUtil.putUnicodeLE(text,_text,0);

		// Update the size (header bytes 5-8)
		LittleEndian.putInt(_header,4,_text.length);
	}

	/**
	 * Grabs the count, from the first two bytes of the header.
	 * The meaning of the count is specific to the type of the parent record
	 */
	public int getOptions() {
		return LittleEndian.getShort(_header);
	}

	/**
	 * Sets the count
	 * The meaning of the count is specific to the type of the parent record
	 */
	public void setOptions(int count) {
		LittleEndian.putShort(_header, (short)count);
	}

	/* *************** record code follows ********************** */

	/**
	 * For the CStrubg Atom
	 */
	protected CString(byte[] source, int start, int len) {
		// Sanity Checking
		if(len < 8) { len = 8; }

		// Get the header
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		// Grab the text
		_text = new byte[len-8];
		System.arraycopy(source,start+8,_text,0,len-8);
	}
	/**
	 * Create an empty CString
	 */
	public CString() {
		// 0 length header
		_header = new byte[] {  0, 0, 0xBA-256, 0x0f, 0, 0, 0, 0 };
		// Empty text
		_text = new byte[0];
	}

	/**
	 * We are of type 4026
	 */
	public long getRecordType() { return _type; }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		// Header - size or type unchanged
		out.write(_header);

		// Write out our text
		out.write(_text);
	}

    /**
     * Gets a string representation of this object, primarily for debugging.
     * @return a string representation of this object.
     */
    public String toString() {
        return getText();
    }
}
