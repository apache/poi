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
 * SXVD - View Fields (0x00B1)<br/>
 * 
 * @author Patrick Cheng
 */
public final class ViewFieldsRecord extends StandardRecord {
	public static final short sid = 0x00B1;

	/** the value of the <tt>cchName</tt> field when the name is not present */
	private static final int STRING_NOT_PRESENT_LEN = -1;

	private int sxaxis;
	private int cSub;
	private int grbitSub;
	private int cItm;
	
	private String name = null;
	
	/**
	 * values for the {@link ViewFieldsRecord#sxaxis} field
	 */
	private static final class Axis {
		public static final int NO_AXIS = 0;
		public static final int ROW = 1;
		public static final int COLUMN = 2;
		public static final int PAGE = 4;
		public static final int DATA = 8;
	}

	public ViewFieldsRecord(RecordInputStream in) {
		sxaxis = in.readShort();
		cSub = in.readShort();
		grbitSub = in.readShort();
		cItm = in.readShort();
		
		int cchName = in.readShort();
		if (cchName != STRING_NOT_PRESENT_LEN) {
			name = in.readCompressedUnicode(cchName);
		}
	}
	
	@Override
	protected void serialize(LittleEndianOutput out) {
		
		out.writeShort(sxaxis);
		out.writeShort(cSub);
		out.writeShort(grbitSub);
		out.writeShort(cItm);
		
		if (name != null) {
			StringUtil.writeUnicodeString(out, name);
		} else {
			out.writeShort(STRING_NOT_PRESENT_LEN);
		}
	}

	@Override
	protected int getDataSize() {
		
		int cchName = 0;
		if (name != null) {
			cchName = name.length();
		}
		return 2 +2 + 2 + 2 + 2 + cchName;
	}

	@Override
	public short getSid() {
		return sid;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[SXVD]\n");
		buffer.append("    .sxaxis    = ").append(HexDump.shortToHex(sxaxis)).append('\n');
		buffer.append("    .cSub      = ").append(HexDump.shortToHex(cSub)).append('\n');
		buffer.append("    .grbitSub  = ").append(HexDump.shortToHex(grbitSub)).append('\n');
		buffer.append("    .cItm      = ").append(HexDump.shortToHex(cItm)).append('\n');
		buffer.append("    .name      = ").append(name).append('\n');
		
		buffer.append("[/SXVD]\n");
		return buffer.toString();
	}
}
