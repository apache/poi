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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.hssf.record.HSSFRecordTypes;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.StandardRecord;
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


	public ViewDefinitionRecord(ViewDefinitionRecord other) {
		super(other);
		rwFirst = other.rwFirst;
		rwLast = other.rwLast;
		colFirst = other.colFirst;
		colLast = other.colLast;
		rwFirstHead = other.rwFirstHead;
		rwFirstData = other.rwFirstData;
		colFirstData = other.colFirstData;
		iCache = other.iCache;
		reserved = other.reserved;
		sxaxis4Data = other.sxaxis4Data;
		ipos4Data = other.ipos4Data;
		cDim = other.cDim;
		cDimRw = other.cDimRw;
		cDimCol = other.cDimCol;
		cDimPg = other.cDimPg;
		cDimData = other.cDimData;
		cRw = other.cRw;
		cCol = other.cCol;
		grbit = other.grbit;
		itblAutoFmt = other.itblAutoFmt;
		name = other.name;
		dataField = other.dataField;
	}

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
	public ViewDefinitionRecord copy() {
		return new ViewDefinitionRecord(this);
	}

	@Override
	public HSSFRecordTypes getGenericRecordType() {
		return HSSFRecordTypes.VIEW_DEFINITION;
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		final Map<String,Supplier<?>> m = new LinkedHashMap<>();

		m.put("rwFirst", () -> rwFirst);
		m.put("rwLast", () -> rwLast);
		m.put("colFirst", () -> colFirst);
		m.put("colLast", () -> colLast);
		m.put("rwFirstHead", () -> rwFirstHead);
		m.put("rwFirstData", () -> rwFirstData);
		m.put("colFirstData", () -> colFirstData);
		m.put("iCache", () -> iCache);
		m.put("reserved", () -> reserved);
		m.put("sxaxis4Data", () -> sxaxis4Data);
		m.put("ipos4Data", () -> ipos4Data);
		m.put("cDim", () -> cDim);
		m.put("cDimRw", () -> cDimRw);
		m.put("cDimCol", () -> cDimCol);
		m.put("cDimPg", () -> cDimPg);
		m.put("cDimData", () -> cDimData);
		m.put("cRw", () -> cRw);
		m.put("cCol", () -> cCol);
		m.put("grbit", () -> grbit);
		m.put("itblAutoFmt", () -> itblAutoFmt);
		m.put("name", () -> name);
		m.put("dataField", () -> dataField);

		return Collections.unmodifiableMap(m);
	}
}
