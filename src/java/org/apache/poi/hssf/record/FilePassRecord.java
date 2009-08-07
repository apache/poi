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

package org.apache.poi.hssf.record;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title: File Pass Record (0x002F) <p/>
 *
 * Description: Indicates that the record after this record are encrypted.
 *
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class FilePassRecord extends StandardRecord {
	public final static short sid = 0x002F;
	private int _encryptionType;
	private int _encryptionInfo;
	private int _minorVersionNo;
	private byte[] _docId;
	private byte[] _saltData;
	private byte[] _saltHash;

	private static final int ENCRYPTION_XOR = 0;
	private static final int ENCRYPTION_OTHER = 1;

	private static final int ENCRYPTION_OTHER_RC4 = 1;
	private static final int ENCRYPTION_OTHER_CAPI_2 = 2;
	private static final int ENCRYPTION_OTHER_CAPI_3 = 3;


	public FilePassRecord(RecordInputStream in) {
		_encryptionType = in.readUShort();

		switch (_encryptionType) {
			case ENCRYPTION_XOR:
				throw new RecordFormatException("HSSF does not currently support XOR obfuscation");
			case ENCRYPTION_OTHER:
				// handled below
				break;
			default:
				throw new RecordFormatException("Unknown encryption type " + _encryptionType);
		}
		_encryptionInfo = in.readUShort();
		switch (_encryptionInfo) {
			case ENCRYPTION_OTHER_RC4:
				// handled below
				break;
			case ENCRYPTION_OTHER_CAPI_2:
			case ENCRYPTION_OTHER_CAPI_3:
				throw new RecordFormatException(
						"HSSF does not currently support CryptoAPI encryption");
			default:
				throw new RecordFormatException("Unknown encryption info " + _encryptionInfo);
		}
		_minorVersionNo = in.readUShort();
		if (_minorVersionNo!=1) {
			throw new RecordFormatException("Unexpected VersionInfo number for RC4Header " + _minorVersionNo);
		}
		_docId = read(in, 16);
		_saltData = read(in, 16);
		_saltHash = read(in, 16);
	}

	private static byte[] read(RecordInputStream in, int size) {
		byte[] result = new byte[size];
		in.readFully(result);
		return result;
	}

	public void serialize(LittleEndianOutput out) {
		out.writeShort(_encryptionType);
		out.writeShort(_encryptionInfo);
		out.writeShort(_minorVersionNo);
		out.write(_docId);
		out.write(_saltData);
		out.write(_saltHash);
	}

	protected int getDataSize() {
		return 54;
	}



	public byte[] getDocId() {
		return _docId.clone();
	}

	public void setDocId(byte[] docId) {
		_docId = docId.clone();
	}

	public byte[] getSaltData() {
		return _saltData.clone();
	}

	public void setSaltData(byte[] saltData) {
		_saltData = saltData.clone();
	}

	public byte[] getSaltHash() {
		return _saltHash.clone();
	}

	public void setSaltHash(byte[] saltHash) {
		_saltHash = saltHash.clone();
	}

	public short getSid() {
		return sid;
	}

	public Object clone() {
		// currently immutable
		return this;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("[FILEPASS]\n");
		buffer.append("    .type = ").append(HexDump.shortToHex(_encryptionType)).append("\n");
		buffer.append("    .info = ").append(HexDump.shortToHex(_encryptionInfo)).append("\n");
		buffer.append("    .ver  = ").append(HexDump.shortToHex(_minorVersionNo)).append("\n");
		buffer.append("    .docId= ").append(HexDump.toHex(_docId)).append("\n");
		buffer.append("    .salt = ").append(HexDump.toHex(_saltData)).append("\n");
		buffer.append("    .hash = ").append(HexDump.toHex(_saltHash)).append("\n");
		buffer.append("[/FILEPASS]\n");
		return buffer.toString();
	}
}
