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

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.hssf.record.HSSFRecordTypes;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.StringUtil;

/**
 * SXDI - Data Item (0x00C5)
 */
public final class DataItemRecord extends StandardRecord {
	public static final short sid = 0x00C5;

	private int isxvdData;
	private int iiftab;
	private int df;
	private int isxvd;
	private int isxvi;
	private int ifmt;
	private String name;

	public DataItemRecord(DataItemRecord other) {
		super(other);
		isxvdData = other.isxvdData;
		iiftab = other.iiftab;
		df = other.df;
		isxvd = other.isxvd;
		isxvi = other.isxvi;
		ifmt = other.ifmt;
		name = other.name;
	}

	public DataItemRecord(RecordInputStream in) {
		isxvdData = in.readUShort();
		iiftab = in.readUShort();
		df = in.readUShort();
		isxvd = in.readUShort();
		isxvi = in.readUShort();
		ifmt = in.readUShort();

		name = in.readString();
	}

	@Override
	protected void serialize(LittleEndianOutput out) {

		out.writeShort(isxvdData);
		out.writeShort(iiftab);
		out.writeShort(df);
		out.writeShort(isxvd);
		out.writeShort(isxvi);
		out.writeShort(ifmt);

		StringUtil.writeUnicodeString(out, name);
	}

	@Override
	protected int getDataSize() {
		return 2 + 2 + 2 + 2 + 2 + 2 + StringUtil.getEncodedSize(name);
	}

	@Override
	public short getSid() {
		return sid;
	}

	@Override
	public DataItemRecord copy() {
		return new DataItemRecord(this);
	}

	@Override
	public HSSFRecordTypes getGenericRecordType() {
		return HSSFRecordTypes.DATA_ITEM;
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return GenericRecordUtil.getGenericProperties(
			"isxvdData", () -> isxvdData,
			"iiftab", () -> iiftab,
			"df", () -> df,
			"isxvd", () -> isxvd,
			"isxvi", () -> isxvi,
			"ifmt", () -> ifmt
		);
	}
}
