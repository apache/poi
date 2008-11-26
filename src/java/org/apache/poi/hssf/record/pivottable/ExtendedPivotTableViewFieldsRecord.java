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

package org.apache.poi.hssf.record.pivottable;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.StringUtil;

/**
 * SXVDEX - Extended PivotTable View Fields (0x0100)<br/>
 * 
 * @author Patrick Cheng
 */
public final class ExtendedPivotTableViewFieldsRecord extends StandardRecord {
	public static final short sid = 0x0100;

	/** the value of the <tt>cchSubName</tt> field when the subName is not present */
	private static final int STRING_NOT_PRESENT_LEN = -1;
	
	private int grbit1;
	private int grbit2;
	private int citmShow;
	private int isxdiSort;
	private int isxdiShow;
	private int reserved1;
	private int reserved2;
	private String subName;
	
	public ExtendedPivotTableViewFieldsRecord(RecordInputStream in) {
		
		grbit1 = in.readInt();
		grbit2 = in.readUByte();
		citmShow = in.readUByte();
		isxdiSort = in.readUShort();
		isxdiShow = in.readUShort();
		int cchSubName = in.readUShort();
		reserved1 = in.readInt();
		reserved2 = in.readInt();
		if (cchSubName != STRING_NOT_PRESENT_LEN) {
			subName = in.readUnicodeLEString(cchSubName);
		}
	}
	
	@Override
	protected void serialize(LittleEndianOutput out) {
		
		out.writeInt(grbit1);
		out.writeByte(grbit2);
		out.writeByte(citmShow);
		out.writeShort(isxdiSort);
		out.writeShort(isxdiShow);
		
		if (subName == null) {
			out.writeShort(STRING_NOT_PRESENT_LEN);
		} else {
			out.writeShort(subName.length());
		}
		
		out.writeInt(reserved1);
		out.writeInt(reserved2);
		if (subName != null) {
			StringUtil.putUnicodeLE(subName, out);
		}
		
	}

	@Override
	protected int getDataSize() {
		
		return 4 + 1 + 1 + 2 + 2 + 2 +  4 + 4 +
					(subName == null ? 0 : (2*subName.length())); // in unicode
	}

	@Override
	public short getSid() {
		return sid;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("[SXVDEX]\n");

		buffer.append("    .grbit1 =").append(HexDump.intToHex(grbit1)).append("\n");
		buffer.append("    .grbit2 =").append(HexDump.byteToHex(grbit2)).append("\n");
		buffer.append("    .citmShow =").append(HexDump.byteToHex(citmShow)).append("\n");
		buffer.append("    .isxdiSort =").append(HexDump.shortToHex(isxdiSort)).append("\n");
		buffer.append("    .isxdiShow =").append(HexDump.shortToHex(isxdiShow)).append("\n");
		buffer.append("    .subName =").append(subName).append("\n");
		buffer.append("[/SXVDEX]\n");
		return buffer.toString();
	}
}
