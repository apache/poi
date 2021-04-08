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

/*
 * HSSF Chart Title Format Record Type
 */
package org.apache.poi.hssf.record.chart;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.hssf.record.HSSFRecordTypes;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * CHARTTITLEFORMAT (0x1050)<p>
 * Describes the formatting runs associated with a chart title.
 */
public class ChartTitleFormatRecord extends StandardRecord {
	public static final short sid = 0x1050;

	private final CTFormat[] _formats;

	private static final class CTFormat implements GenericRecord {
		public static final int ENCODED_SIZE=4;
		private int _offset;
		private int _fontIndex;

		public CTFormat(CTFormat other) {
			_offset = other._offset;
			_fontIndex = other._fontIndex;
		}

		public CTFormat(RecordInputStream in) {
			_offset = in.readShort();
			_fontIndex = in.readShort();
		}

		public int getOffset(){
			return _offset;
		}
		public void setOffset(int newOff){
			_offset = newOff;
		}
		public int getFontIndex() {
			return _fontIndex;
		}

		public void serialize(LittleEndianOutput out) {
			out.writeShort(_offset);
			out.writeShort(_fontIndex);
		}

		@Override
		public Map<String, Supplier<?>> getGenericProperties() {
			return GenericRecordUtil.getGenericProperties(
				"offset", this::getOffset,
				"fontIndex", this::getFontIndex
			);
		}
	}

	public ChartTitleFormatRecord(ChartTitleFormatRecord other) {
		super(other);
		_formats = Stream.of(other._formats).map(CTFormat::new).toArray(CTFormat[]::new);
	}

	public ChartTitleFormatRecord(RecordInputStream in) {
		int nRecs = in.readUShort();
		_formats = new CTFormat[nRecs];

		for(int i=0;i<nRecs;i++) {
			_formats[i] = new CTFormat(in);
		}
	}

	public void serialize(LittleEndianOutput out) {
        out.writeShort(_formats.length);
		for (CTFormat format : _formats) {
			format.serialize(out);
		}
    }

    protected int getDataSize() {
        return 2 + CTFormat.ENCODED_SIZE * _formats.length;
    }

	public short getSid() {
		return sid;
	}

	public int getFormatCount() {
		return _formats.length;
	}

	public void modifyFormatRun(short oldPos, short newLen) {
		int shift = 0;
		for(int i=0; i < _formats.length; i++) {
			CTFormat ctf = _formats[i];
			if (shift != 0) {
				ctf.setOffset(ctf.getOffset() + shift);
			} else if (oldPos == ctf.getOffset() && i < _formats.length - 1){
				CTFormat nextCTF = _formats[i + 1];
				shift = newLen - (nextCTF.getOffset() - ctf.getOffset());
			}
		}
	}

	@Override
	public ChartTitleFormatRecord copy() {
		return new ChartTitleFormatRecord(this);
	}

	@Override
	public HSSFRecordTypes getGenericRecordType() {
		return HSSFRecordTypes.CHART_TITLE_FORMAT;
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return GenericRecordUtil.getGenericProperties("formats", () -> _formats);
	}
}
