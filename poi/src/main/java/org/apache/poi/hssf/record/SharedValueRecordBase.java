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
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Common base class for {@link SharedFormulaRecord}, {@link ArrayRecord} and
 * {@link TableRecord} which are have similarities.
 */
public abstract class SharedValueRecordBase extends StandardRecord {

	private CellRangeAddress8Bit _range;

	protected SharedValueRecordBase(SharedValueRecordBase other) {
		super(other);
		_range = (other._range == null) ? null : other._range.copy();
	}

	protected SharedValueRecordBase(CellRangeAddress8Bit range) {
		if (range == null) {
			throw new IllegalArgumentException("range must be supplied.");
		}
		_range = range;
	}

	protected SharedValueRecordBase() {
		this(new CellRangeAddress8Bit(0, 0, 0, 0));
	}

	/**
	 * reads only the range (1 {@link CellRangeAddress8Bit}) from the stream
	 *
	 * @param in The interface for reading the record data.
	 */
	public SharedValueRecordBase(LittleEndianInput in) {
		_range = new CellRangeAddress8Bit(in);
	}

	/**
	 * @return the range of cells that this record is shared across.  Never <code>null</code>.
	 */
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

	protected int getDataSize() {
		return CellRangeAddress8Bit.ENCODED_SIZE + getExtraDataSize();
	}

	protected abstract int getExtraDataSize();

	protected abstract void serializeExtraData(LittleEndianOutput out);

	public void serialize(LittleEndianOutput out) {
		_range.serialize(out);
		serializeExtraData(out);
	}

	/**
     * @param rowIx the row index
     * @param colIx the column index
     *
	 * @return {@code true} if (rowIx, colIx) is within the range of this shared value object.
     *
     * @see #getRange()
	 */
	public final boolean isInRange(int rowIx, int colIx) {
		CellRangeAddress8Bit r = _range;
		return r.getFirstRow() <= rowIx
			&& r.getLastRow() >= rowIx
			&& r.getFirstColumn() <= colIx
			&& r.getLastColumn() >= colIx;
	}
	/**
	 * @param rowIx the row index
	 * @param colIx the column index
	 *
	 * @return {@code true} if (rowIx, colIx) describes the first cell in this shared value
	 * object's range
	 *
	 * @see #getRange()
	 */
	public final boolean isFirstCell(int rowIx, int colIx) {
		CellRangeAddress8Bit r = getRange();
		return r.getFirstRow() == rowIx && r.getFirstColumn() == colIx;
	}
}
