/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hssf.util;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.util.LittleEndian;

/**
 * See OOO documentation: excelfileformat.pdf sec 2.5.14 - 'Cell Range Address'
 * 
 * @author Dragos Buleandra (dragos.buleandra@trade2b.ro)
 */
public final class CellRangeAddress {
	private static final int ENCODED_SIZE = 8;

	private int _firstRow;
	private int _firstCol;
	private int _lastRow;
	private int _lastCol;

	/*
	 * TODO - replace other incarnations of 'Cell Range Address' throughout POI:
	 * org.apache.poi.hssf.util.CellRange
	 * org.apache.poi.hssf.record.cf.CellRange
	 * org.apache.poi.hssf.util.HSSFCellRangeAddress.AddrStructure
	 * org.apache.poi.hssf.record.MergeCellsRecord.MergedRegion
	 * org.apache.poi.hssf.record.SelectionRecord.Reference
	 * 
	 */
	
	public CellRangeAddress(int firstRow, int lastRow, int firstCol, int lastCol) {
		_firstRow = firstRow;
		_lastRow = lastRow;
		_firstCol = firstCol;
		_lastCol = lastCol;
	}

	public CellRangeAddress(RecordInputStream in) {
		if (in.remaining() < ENCODED_SIZE) {
			// Ran out of data
			throw new RuntimeException("Ran out of data reading CellRangeAddress");
		} 
		_firstRow = in.readUShort();
		_lastRow = in.readUShort();
		_firstCol = in.readUShort();
		_lastCol = in.readUShort();
	}

	/**
	 * @return column number for the upper left hand corner
	 */
	public int getFirstColumn() {
		return _firstCol;
	}

	/**
	 * @return row number for the upper left hand corner
	 */
	public int getFirstRow() {
		return _firstRow;
	}

	/**
	 * @return column number for the lower right hand corner
	 */
	public int getLastColumn() {
		return _lastCol;
	}

	/**
	 * @return row number for the lower right hand corner
	 */
	public int getLastRow() {
		return _lastRow;
	}

	/**
	 * @param _firstCol column number for the upper left hand corner
	 */
	public void setFirstColumn(int firstCol) {
		_firstCol = firstCol;
	}

	/**
	 * @param rowFrom row number for the upper left hand corner
	 */
	public void setFirstRow(int firstRow) {
		_firstRow = firstRow;
	}

	/**
	 * @param colTo column number for the lower right hand corner
	 */
	public void setLastColumn(int lastCol) {
		_lastCol = lastCol;
	}

	/**
	 * @param rowTo row number for the lower right hand corner
	 */
	public void setLastRow(int lastRow) {
		_lastRow = lastRow;
	}

	/* package */ int serialize(byte[] data, int offset) {
		LittleEndian.putUShort(data, offset + 0, _firstRow);
		LittleEndian.putUShort(data, offset + 2, _lastRow);
		LittleEndian.putUShort(data, offset + 4, _firstCol);
		LittleEndian.putUShort(data, offset + 6, _lastCol);
		return ENCODED_SIZE;
	}

	public static int getEncodedSize(int numberOfItems) {
		return numberOfItems * ENCODED_SIZE;
	}
}