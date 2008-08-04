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

package org.apache.poi.ss.util;

import org.apache.poi.hssf.record.SelectionRecord;
import org.apache.poi.util.LittleEndian;

/**
 * See OOO documentation: excelfileformat.pdf sec 2.5.14 - 'Cell Range Address'<p/>
 * 
 * Note - {@link SelectionRecord} uses the BIFF5 version of this structure
 * @author Dragos Buleandra (dragos.buleandra@trade2b.ro)
 */
public class CellRangeAddress {
	/*
	 * TODO - replace  org.apache.poi.hssf.util.Region
	 */
	public static final int ENCODED_SIZE = 8;

	/** max 65536 rows in BIFF8 */
	public static final int LAST_ROW_INDEX = 0x00FFFF; 
	/** max 256 columns in BIFF8 */
	public static final int LAST_COLUMN_INDEX = 0x00FF;
	
	
	protected int _firstRow;
	protected int _firstCol;
	protected int _lastRow;
	protected int _lastCol;

	protected CellRangeAddress() {}
	public CellRangeAddress(int firstRow, int lastRow, int firstCol, int lastCol) {
		if(!isValid(firstRow, lastRow, firstCol, lastCol)) {
			throw new IllegalArgumentException("invalid cell range (" + firstRow + ", " + lastRow 
					+ ", " + firstCol + ", " + lastCol + ")");
		}
		_firstRow = firstRow;
		_lastRow = convertM1ToMax(lastRow, LAST_ROW_INDEX);
		_firstCol = firstCol;
		_lastCol = convertM1ToMax(lastCol, LAST_COLUMN_INDEX);
	}

	private static boolean isValid(int firstRow, int lastRow, int firstColumn, int lastColumn)
	{
		if(lastRow < 0 || lastRow > LAST_ROW_INDEX) {
			return false;
		}
		if(firstRow < 0 || firstRow > LAST_ROW_INDEX) {
			return false;
		}
		
		if(lastColumn < 0 || lastColumn > LAST_COLUMN_INDEX) {
			return false;
		}
		if(firstColumn < 0 || firstColumn > LAST_COLUMN_INDEX) {
			return false;
		}
		return true;
	}
	/** 
	 * Range arithmetic is easier when using a large positive number for 'max row or column' 
	 * instead of <tt>-1</tt>. 
	 */
	private static int convertM1ToMax(int lastIx, int maxIndex) {
		if(lastIx < 0) {
			return maxIndex;
		}
		return lastIx;
	}

	public boolean isFullColumnRange() {
		return _firstRow == 0 && _lastRow == LAST_ROW_INDEX;
	}
	public boolean isFullRowRange() {
		return _firstCol == 0 && _lastCol == LAST_COLUMN_INDEX;
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

	public CellRangeAddress copy() {
		return new CellRangeAddress(_firstRow, _lastRow, _firstCol, _lastCol);
	}

	public static int getEncodedSize(int numberOfItems) {
		return numberOfItems * ENCODED_SIZE;
	}
	
	public String toString() {
		return getClass().getName() + " ["+_firstRow+", "+_lastRow+", "+_firstCol+", "+_lastCol+"]";
	}

	public int serialize(int offset, byte[] data) {
		LittleEndian.putUShort(data, offset + 0, _firstRow);
		LittleEndian.putUShort(data, offset + 2, _lastRow);
		LittleEndian.putUShort(data, offset + 4, _firstCol);
		LittleEndian.putUShort(data, offset + 6, _lastCol);
		return ENCODED_SIZE;
	}
}
