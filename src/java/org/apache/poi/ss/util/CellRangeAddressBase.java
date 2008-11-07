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


/**
 * See OOO documentation: excelfileformat.pdf sec 2.5.14 - 'Cell Range Address'<p/>
 * 
 * Common subclass of 8-bit and 16-bit versions
 * 
 * @author Josh Micich
 */
public abstract class CellRangeAddressBase {

	/** max 65536 rows in BIFF8 */
	private static final int LAST_ROW_INDEX = 0x00FFFF; 
	/** max 256 columns in BIFF8 */
	private static final int LAST_COLUMN_INDEX = 0x00FF;
	
	private int _firstRow;
	private int _firstCol;
	private int _lastRow;
	private int _lastCol;

	protected CellRangeAddressBase(int firstRow, int lastRow, int firstCol, int lastCol) {
		if(!isValid(firstRow, lastRow, firstCol, lastCol)) {
			throw new IllegalArgumentException("invalid cell range (" + firstRow + ", " + lastRow 
					+ ", " + firstCol + ", " + lastCol + ")");
		}
		_firstRow = firstRow;
		_lastRow =lastRow;
		_firstCol = firstCol;
		_lastCol = lastCol;
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
	

	public final boolean isFullColumnRange() {
		return _firstRow == 0 && _lastRow == LAST_ROW_INDEX;
	}
	public final boolean isFullRowRange() {
		return _firstCol == 0 && _lastCol == LAST_COLUMN_INDEX;
	}

	/**
	 * @return column number for the upper left hand corner
	 */
	public final int getFirstColumn() {
		return _firstCol;
	}

	/**
	 * @return row number for the upper left hand corner
	 */
	public final int getFirstRow() {
		return _firstRow;
	}

	/**
	 * @return column number for the lower right hand corner
	 */
	public final int getLastColumn() {
		return _lastCol;
	}

	/**
	 * @return row number for the lower right hand corner
	 */
	public final int getLastRow() {
		return _lastRow;
	}

	/**
	 * @param firstCol column number for the upper left hand corner
	 */
	public final void setFirstColumn(int firstCol) {
		_firstCol = firstCol;
	}

	/**
	 * @param firstRow row number for the upper left hand corner
	 */
	public final void setFirstRow(int firstRow) {
		_firstRow = firstRow;
	}

	/**
	 * @param lastCol column number for the lower right hand corner
	 */
	public final void setLastColumn(int lastCol) {
		_lastCol = lastCol;
	}

	/**
	 * @param lastRow row number for the lower right hand corner
	 */
	public final void setLastRow(int lastRow) {
		_lastRow = lastRow;
	}

	public final String toString() {
		return getClass().getName() + " ["+_firstRow+", "+_lastRow+", "+_firstCol+", "+_lastCol+"]";
	}
}
