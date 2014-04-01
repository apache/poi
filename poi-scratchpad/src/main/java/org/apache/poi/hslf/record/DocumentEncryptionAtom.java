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

import org.apache.poi.util.StringUtil;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A Document Encryption Atom (type 12052). Holds information
 *  on the Encryption of a Document
 *
 * @author Nick Burch
 */
public final class DocumentEncryptionAtom extends RecordAtom {
	private byte[] _header;
	private static long _type = 12052l;

	private byte[] data;
	private String encryptionProviderName;

	/**
	 * For the Document Encryption Atom
	 */
	protected DocumentEncryptionAtom(byte[] source, int start, int len) {
		// Get the header
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		// Grab everything else, for now
		data = new byte[len-8];
		System.arraycopy(source, start+8, data, 0, len-8);

		// Grab the provider, from byte 8+44 onwards
		// It's a null terminated Little Endian String
		int endPos = -1;
		int pos = start + 8+44;
		while(pos < (start+len) && endPos < 0) {
			if(source[pos] == 0 && source[pos+1] == 0) {
				// Hit the end
				endPos = pos;
			}
			pos += 2;
		}
		pos = start + 8+44;
		int stringLen = (endPos-pos) / 2;
		encryptionProviderName = StringUtil.getFromUnicodeLE(source, pos, stringLen);
	}

	/**
	 * Return the length of the encryption key, in bits
	 */
	public int getKeyLength() {
		return data[28];
	}

	/**
	 * Return the name of the encryption provider used
	 */
	public String getEncryptionProviderName() {
		return encryptionProviderName;
	}


	/**
	 * We are of type 12052
	 */
	public long getRecordType() { return _type; }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		// Header
		out.write(_header);

		// Data
		out.write(data);
	}
}
