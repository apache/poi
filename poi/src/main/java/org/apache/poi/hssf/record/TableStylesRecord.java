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

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.StringUtil;

/**
 * TABLESTYLES (0x088E)
 */
public final class TableStylesRecord extends StandardRecord {
	public static final short sid = 0x088E;

	private int rt;
	private int grbitFrt;
	private final byte[] unused = new byte[8];
	private int cts;

	private String rgchDefListStyle;
	private String rgchDefPivotStyle;


	public TableStylesRecord(TableStylesRecord other) {
		super(other);
		rt = other.rt;
		grbitFrt = other.grbitFrt;
		System.arraycopy(other.unused, 0, unused, 0, unused.length);
		cts = other.cts;
		rgchDefListStyle = other.rgchDefListStyle;
		rgchDefPivotStyle = other.rgchDefPivotStyle;
	}

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
	public TableStylesRecord copy() {
		return new TableStylesRecord(this);
	}

	@Override
	public HSSFRecordTypes getGenericRecordType() {
		return HSSFRecordTypes.TABLE_STYLES;
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return GenericRecordUtil.getGenericProperties(
			"rt", () -> rt,
			"grbitFrt", () -> grbitFrt,
			"unused", () -> unused,
			"cts", () -> cts,
			"rgchDefListStyle", () -> rgchDefListStyle,
			"rgchDefPivotStyle", () -> rgchDefPivotStyle
		);
	}
}
