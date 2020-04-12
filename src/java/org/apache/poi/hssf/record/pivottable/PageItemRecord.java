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
import java.util.stream.Stream;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.hssf.record.HSSFRecordTypes;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.RecordFormatException;

/**
 * SXPI - Page Item (0x00B6)
 */
public final class PageItemRecord extends StandardRecord {
	public static final short sid = 0x00B6;

	private static final class FieldInfo implements GenericRecord {
		public static final int ENCODED_SIZE = 6;
		/** Index to the View Item SXVI(0x00B2) record */
		private int _isxvi;
		/** Index to the {@link ViewFieldsRecord} SXVD(0x00B1) record */
		private int _isxvd;
		/** Object ID for the drop-down arrow */
		private int _idObj;

		public FieldInfo(FieldInfo other) {
			_isxvi = other._isxvi;
			_isxvd = other._isxvd;
			_idObj = other._idObj;
		}

		public FieldInfo(RecordInputStream in) {
			_isxvi = in.readShort();
			_isxvd = in.readShort();
			_idObj = in.readShort();
		}

		private void serialize(LittleEndianOutput out) {
			out.writeShort(_isxvi);
			out.writeShort(_isxvd);
			out.writeShort(_idObj);
		}

		@Override
		public Map<String, Supplier<?>> getGenericProperties() {
			return GenericRecordUtil.getGenericProperties(
				"isxvi", () -> _isxvi,
				"isxvd", () -> _isxvd,
				"idObj", () -> _idObj
			);
		}
	}

	private final FieldInfo[] _fieldInfos;

	public PageItemRecord(PageItemRecord other) {
		super(other);
		_fieldInfos = Stream.of(other._fieldInfos).map(FieldInfo::new).toArray(FieldInfo[]::new);
	}

	public PageItemRecord(RecordInputStream in) {
		int dataSize = in.remaining();
		if (dataSize % FieldInfo.ENCODED_SIZE != 0) {
			throw new RecordFormatException("Bad data size " + dataSize);
		}

		int nItems = dataSize / FieldInfo.ENCODED_SIZE;

		FieldInfo[] fis = new FieldInfo[nItems];
		for (int i = 0; i < fis.length; i++) {
			fis[i] = new FieldInfo(in);
		}
		_fieldInfos = fis;
	}

	@Override
	protected void serialize(LittleEndianOutput out) {
		for (FieldInfo fieldInfo : _fieldInfos) {
			fieldInfo.serialize(out);
		}
	}

	@Override
	protected int getDataSize() {
		return _fieldInfos.length * FieldInfo.ENCODED_SIZE;
	}

	@Override
	public short getSid() {
		return sid;
	}

	@Override
	public PageItemRecord copy() {
		return new PageItemRecord(this);
	}

	@Override
	public HSSFRecordTypes getGenericRecordType() {
		return HSSFRecordTypes.PAGE_ITEM;
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return GenericRecordUtil.getGenericProperties("fieldInfos", () -> _fieldInfos);
	}
}
