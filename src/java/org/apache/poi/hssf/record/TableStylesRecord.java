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
import org.apache.poi.util.StringUtil;

/**
 * TABLESTYLES (0x088E)<br/>
 * 
 * @author Patrick Cheng
 */
public final class TableStylesRecord extends StandardRecord {
	public static final short sid = 0x088E;
	
	private int rt;
	private int grbitFrt;
	private byte[] unused = new byte[8];
	private int cts;
	
	private String rgchDefListStyle;
	private String rgchDefPivotStyle;
	
	
	public TableStylesRecord(RecordInputStream in) {
		rt = in.readUShort();
		grbitFrt = in.readUShort();
		in.readFully(unused);
		cts = in.readInt();
		int cchDefListStyle = in.readUShort();
		int cchDefPivotStyle = in.readUShort();
		
		rgchDefListStyle = in.readUnicodeLEString(cchDefListStyle);
		rgchDefPivotStyle = in.readUnicodeLEString(cchDefPivotStyle);
	}
	
	@Override
	protected void serialize(LittleEndianOutput out) {
		out.writeShort(rt);
		out.writeShort(grbitFrt);
		out.write(unused);
		out.writeInt(cts);
		
		out.writeShort(rgchDefListStyle.length());
		out.writeShort(rgchDefPivotStyle.length());
		
		StringUtil.putUnicodeLE(rgchDefListStyle, out);
		StringUtil.putUnicodeLE(rgchDefPivotStyle, out);
	}

	@Override
	protected int getDataSize() {
		return 2 + 2 + 8 + 4 + 2 + 2
			+ (2*rgchDefListStyle.length()) + (2*rgchDefPivotStyle.length());
	}

	@Override
	public short getSid() {
		return sid;
	}


	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("[TABLESTYLES]\n");
		buffer.append("    .rt      =").append(HexDump.shortToHex(rt)).append('\n');
		buffer.append("    .grbitFrt=").append(HexDump.shortToHex(grbitFrt)).append('\n');
		buffer.append("    .unused  =").append(HexDump.toHex(unused)).append('\n');
		buffer.append("    .cts=").append(HexDump.intToHex(cts)).append('\n');
		buffer.append("    .rgchDefListStyle=").append(rgchDefListStyle).append('\n');
		buffer.append("    .rgchDefPivotStyle=").append(rgchDefPivotStyle).append('\n');

		buffer.append("[/TABLESTYLES]\n");
		return buffer.toString();
	}
}
