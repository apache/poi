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

import java.util.Arrays;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.RecordFormatException;
import org.apache.poi.util.StringUtil;

/**
 * Title: Write Access Record (0x005C)<p>
 * 
 * Description: Stores the username of that who owns the spreadsheet generator (on unix the user's 
 * login, on Windoze its the name you typed when you installed the thing)<p>
 * 
 * REFERENCE: PG 424 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)
 */
public final class WriteAccessRecord extends StandardRecord {
	public final static short sid = 0x005C;

	private static final byte PAD_CHAR = (byte) ' ';
	private static final int DATA_SIZE = 112;
	private String field_1_username;
	/** this record is always padded to a constant length */
	private static final byte[] PADDING = new byte[DATA_SIZE];
	static {
		Arrays.fill(PADDING, PAD_CHAR);
	}

	public WriteAccessRecord() {
		setUsername("");
	}

	public WriteAccessRecord(RecordInputStream in) {
		if (in.remaining() > DATA_SIZE) {
			throw new RecordFormatException("Expected data size (" + DATA_SIZE + ") but got ("
					+ in.remaining() + ")");
		}
		// The string is always 112 characters (padded with spaces), therefore
		// this record can not be continued.

		int nChars = in.readUShort();
		int is16BitFlag = in.readUByte();
		if (nChars > DATA_SIZE || (is16BitFlag & 0xFE) != 0) {
			// String header looks wrong (probably missing)
			// OOO doc says this is optional anyway.
			// reconstruct data
			byte[] data = new byte[3 + in.remaining()];
			LittleEndian.putUShort(data, 0, nChars);
			LittleEndian.putByte(data, 2, is16BitFlag);
			in.readFully(data, 3, data.length-3);
			String rawValue = new String(data, StringUtil.UTF8);
			setUsername(rawValue.trim());
			return;
		}

		String rawText;
		if ((is16BitFlag & 0x01) == 0x00) {
			rawText = StringUtil.readCompressedUnicode(in, nChars);
		} else {
			rawText = StringUtil.readUnicodeLE(in, nChars);
		}
		field_1_username = rawText.trim();

		// consume padding
		int padSize = in.remaining();
		while (padSize > 0) {
			// in some cases this seems to be garbage (non spaces)
			in.readUByte();
			padSize--;
		}
	}

	/**
	 * set the username for the user that created the report. HSSF uses the
	 * logged in user.
	 * 
	 * @param username of the user who is logged in (probably "tomcat" or "apache")
	 */
	public void setUsername(String username) {
		boolean is16bit = StringUtil.hasMultibyte(username);
		int encodedByteCount = 3 + username.length() * (is16bit ? 2 : 1);
		int paddingSize = DATA_SIZE - encodedByteCount;
		if (paddingSize < 0) {
			throw new IllegalArgumentException("Name is too long: " + username);
		}

		field_1_username = username;
	}

	/**
	 * get the username for the user that created the report. HSSF uses the
	 * logged in user. On natively created M$ Excel sheet this would be the name
	 * you typed in when you installed it in most cases.
	 * 
	 * @return username of the user who is logged in (probably "tomcat" or "apache")
	 */
	public String getUsername() {
		return field_1_username;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("[WRITEACCESS]\n");
		buffer.append("    .name = ").append(field_1_username).append("\n");
		buffer.append("[/WRITEACCESS]\n");
		return buffer.toString();
	}

	public void serialize(LittleEndianOutput out) {
		String username = getUsername();
		boolean is16bit = StringUtil.hasMultibyte(username);

		out.writeShort(username.length());
		out.writeByte(is16bit ? 0x01 : 0x00);
		if (is16bit) {
			StringUtil.putUnicodeLE(username, out);
		} else {
			StringUtil.putCompressedUnicode(username, out);
		}
		int encodedByteCount = 3 + username.length() * (is16bit ? 2 : 1);
		int paddingSize = DATA_SIZE - encodedByteCount;
		out.write(PADDING, 0, paddingSize);
	}

	protected int getDataSize() {
		return DATA_SIZE;
	}

	public short getSid() {
		return sid;
	}
}
