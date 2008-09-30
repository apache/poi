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

import org.apache.poi.hssf.util.CellRangeAddress8Bit;
import org.apache.poi.util.LittleEndian;

/**
 * Common base class for {@link SharedFormulaRecord}, {@link ArrayRecord} and
 * {@link TableRecord} which are have similarities.
 * 
 * @author Josh Micich
 */
public abstract class SharedValueRecordBase extends Record {

	private CellRangeAddress8Bit _range;

	protected SharedValueRecordBase(CellRangeAddress8Bit range) {
		_range = range;
	}

	protected SharedValueRecordBase() {
		this(new CellRangeAddress8Bit(0, 0, 0, 0));
	}

	/**
	 * reads only the range (1 {@link CellRangeAddress8Bit}) from the stream
	 */
	public SharedValueRecordBase(RecordInputStream in) {
		_range = new CellRangeAddress8Bit(in);
	}

	public final CellRangeAddress8Bit getRange() {
		return _range;
	}

	public final int getFirstRow() {
		return _range.getFirstRow();
	}

	public final int getLastRow() {
		return _range.getLastRow();
	}

	public final int getFirstColumn() {
		return (short) _range.getFirstColumn();
	}

	public final int getLastColumn() {
		return (short) _range.getLastColumn();
	}

	public final int getRecordSize() {
		return 4 + CellRangeAddress8Bit.ENCODED_SIZE + getExtraDataSize();
	}

	protected abstract int getExtraDataSize();

	protected abstract void serializeExtraData(int offset, byte[] data);

	public final int serialize(int offset, byte[] data) {
		int dataSize = CellRangeAddress8Bit.ENCODED_SIZE + getExtraDataSize();

		LittleEndian.putShort(data, 0 + offset, getSid());
		LittleEndian.putUShort(data, 2 + offset, dataSize);

		int pos = offset + 4;
		_range.serialize(pos, data);
		pos += CellRangeAddress8Bit.ENCODED_SIZE;
		serializeExtraData(pos, data);
		return dataSize + 4;
	}

	/**
	 * @return <code>true</code> if (rowIx, colIx) is within the range ({@link #getRange()})
	 * of this shared value object.
	 */
	public final boolean isInRange(int rowIx, int colIx) {
		CellRangeAddress8Bit r = _range;
		return r.getFirstRow() <= rowIx 
			&& r.getLastRow() >= rowIx
			&& r.getFirstColumn() <= colIx 
			&& r.getLastColumn() >= colIx;
	}
	/**
	 * @return <code>true</code> if (rowIx, colIx) describes the first cell in this shared value 
	 * object's range ({@link #getRange()})
	 */
	public final boolean isFirstCell(int rowIx, int colIx) {
		CellRangeAddress8Bit r = getRange();
		return r.getFirstRow() == rowIx && r.getFirstColumn() == colIx;
	}
}
