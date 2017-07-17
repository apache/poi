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
 * SXVIEW - View Definition (0x00B0)<br>
 */
public final class ViewDefinitionRecord extends StandardRecord {
	public static final short sid = 0x00B0;

	private int rwFirst;
	private int rwLast;
	private int colFirst;
	private int colLast;
	private int rwFirstHead;
	private int rwFirstData;
	private int colFirstData;
	private int iCache;
	private int reserved;
	
	private int sxaxis4Data;
	private int ipos4Data;
	private int cDim;
	
	private int cDimRw;
	
	private int cDimCol;
	private int cDimPg;
	
	private int cDimData;
	private int cRw;
	private int cCol;
	private int grbit;
	private int itblAutoFmt;
	
	private String dataField;
	private String name;

	
	public ViewDefinitionRecord(RecordInputStream in) {
		rwFirst = in.readUShort();
		rwLast = in.readUShort();
		colFirst = in.readUShort();
		colLast = in.readUShort();
		rwFirstHead = in.readUShort();
		rwFirstData = in.readUShort();
		colFirstData = in.readUShort();
		iCache = in.readUShort();
		reserved = in.readUShort();
		sxaxis4Data = in.readUShort();
		ipos4Data = in.readUShort();
		cDim = in.readUShort();
		cDimRw = in.readUShort();
		cDimCol = in.readUShort();
		cDimPg = in.readUShort();
		cDimData = in.readUShort();
		cRw = in.readUShort();
		cCol = in.readUShort();
		grbit = in.readUShort();
		itblAutoFmt = in.readUShort();
		int cchName = in.readUShort();
		int cchData = in.readUShort();

		name = StringUtil.readUnicodeString(in, cchName);
		dataField = StringUtil.readUnicodeString(in, cchData);
	}
	
	@Override
	protected void serialize(LittleEndianOutput out) {
		out.writeShort(rwFirst);
		out.writeShort(rwLast);
		out.writeShort(colFirst);
		out.writeShort(colLast);
		out.writeShort(rwFirstHead);
		out.writeShort(rwFirstData);
		out.writeShort(colFirstData);
		out.writeShort(iCache);
		out.writeShort(reserved);
		out.writeShort(sxaxis4Data);
		out.writeShort(ipos4Data);
		out.writeShort(cDim);
		out.writeShort(cDimRw);
		out.writeShort(cDimCol);
		out.writeShort(cDimPg);
		out.writeShort(cDimData);
		out.writeShort(cRw);
		out.writeShort(cCol);
		out.writeShort(grbit);
		out.writeShort(itblAutoFmt);
		out.writeShort(name.length());
		out.writeShort(dataField.length());

		StringUtil.writeUnicodeStringFlagAndData(out, name);
		StringUtil.writeUnicodeStringFlagAndData(out, dataField);		
	}

	@Override
	protected int getDataSize() {
		return 40 + // 20 short fields (rwFirst ... itblAutoFmt)
			StringUtil.getEncodedSize(name) + StringUtil.getEncodedSize(dataField) ;
	}

	@Override
	public short getSid() {
		return sid;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("[SXVIEW]\n");
		buffer.append("    .rwFirst      =").append(HexDump.shortToHex(rwFirst)).append('\n');
		buffer.append("    .rwLast       =").append(HexDump.shortToHex(rwLast)).append('\n');
		buffer.append("    .colFirst     =").append(HexDump.shortToHex(colFirst)).append('\n');
		buffer.append("    .colLast      =").append(HexDump.shortToHex(colLast)).append('\n');
		buffer.append("    .rwFirstHead  =").append(HexDump.shortToHex(rwFirstHead)).append('\n');
		buffer.append("    .rwFirstData  =").append(HexDump.shortToHex(rwFirstData)).append('\n');
		buffer.append("    .colFirstData =").append(HexDump.shortToHex(colFirstData)).append('\n');
		buffer.append("    .iCache       =").append(HexDump.shortToHex(iCache)).append('\n');
		buffer.append("    .reserved     =").append(HexDump.shortToHex(reserved)).append('\n');
		buffer.append("    .sxaxis4Data  =").append(HexDump.shortToHex(sxaxis4Data)).append('\n');
		buffer.append("    .ipos4Data    =").append(HexDump.shortToHex(ipos4Data)).append('\n');
		buffer.append("    .cDim         =").append(HexDump.shortToHex(cDim)).append('\n');
		buffer.append("    .cDimRw       =").append(HexDump.shortToHex(cDimRw)).append('\n');
		buffer.append("    .cDimCol      =").append(HexDump.shortToHex(cDimCol)).append('\n');
		buffer.append("    .cDimPg       =").append(HexDump.shortToHex(cDimPg)).append('\n');
		buffer.append("    .cDimData     =").append(HexDump.shortToHex(cDimData)).append('\n');
		buffer.append("    .cRw          =").append(HexDump.shortToHex(cRw)).append('\n');
		buffer.append("    .cCol         =").append(HexDump.shortToHex(cCol)).append('\n');
		buffer.append("    .grbit        =").append(HexDump.shortToHex(grbit)).append('\n');
		buffer.append("    .itblAutoFmt  =").append(HexDump.shortToHex(itblAutoFmt)).append('\n');
		buffer.append("    .name         =").append(name).append('\n');
		buffer.append("    .dataField    =").append(dataField).append('\n');

		buffer.append("[/SXVIEW]\n");
		return buffer.toString();
	}
}
