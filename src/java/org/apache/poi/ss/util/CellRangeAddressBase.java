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

package org.apache.poi.ss.util;

import org.apache.poi.ss.SpreadsheetVersion;


/**
 * See OOO documentation: excelfileformat.pdf sec 2.5.14 - 'Cell Range Address'<p/>
 * 
 * Common subclass of 8-bit and 16-bit versions
 * 
 * @author Josh Micich
 */
public abstract class CellRangeAddressBase {

	private int _firstRow;
	private int _firstCol;
	private int _lastRow;
	private int _lastCol;

	protected CellRangeAddressBase(int firstRow, int lastRow, int firstCol, int lastCol) {
		_firstRow = firstRow;
		_lastRow = lastRow;
		_firstCol = firstCol;
		_lastCol = lastCol;
	}

    /**
     * Validate the range limits against the supplied version of Excel
     *
     * @param ssVersion the version of Excel to validate against
     * @throws IllegalArgumentException if the range limits are outside of the allowed range
     */
    public void validate(SpreadsheetVersion ssVersion) {
        validateRow(_firstRow, ssVersion);
		validateRow(_lastRow, ssVersion);
        validateColumn(_firstCol, ssVersion);
		validateColumn(_lastCol, ssVersion);
	}
    /**
     * Runs a bounds check for row numbers
     * @param row
     */
    private static void validateRow(int row, SpreadsheetVersion ssVersion) {
        int maxrow = ssVersion.getLastRowIndex();
        if (row > maxrow) throw new IllegalArgumentException("Maximum row number is " + maxrow);
        if (row < 0) throw new IllegalArgumentException("Minumum row number is 0");
    }

    /**
     * Runs a bounds check for column numbers
     * @param column
     */
    private static void validateColumn(int column, SpreadsheetVersion ssVersion) {
        int maxcol = ssVersion.getLastColumnIndex();
        if (column > maxcol) throw new IllegalArgumentException("Maximum column number is " + maxcol);
        if (column < 0)    throw new IllegalArgumentException("Minimum column number is 0");
    }


    //TODO use the correct SpreadsheetVersion
    public final boolean isFullColumnRange() {
		return _firstRow == 0 && _lastRow == SpreadsheetVersion.EXCEL97.getLastRowIndex();
	}
    //TODO use the correct SpreadsheetVersion
	public final boolean isFullRowRange() {
		return _firstCol == 0 && _lastCol == SpreadsheetVersion.EXCEL97.getLastColumnIndex();
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
    /**
     * @return the size of the range (number of cells in the area).
     */
    public int getNumberOfCells() {
        return (_lastRow - _firstRow + 1) * (_lastCol - _firstCol + 1);
    }

	public final String toString() {
		CellReference crA = new CellReference(_firstRow, _firstCol);
		CellReference crB = new CellReference(_lastRow, _lastCol);
		return getClass().getName() + " [" + crA.formatAsString() + ":" + crB.formatAsString() +"]";
	}
}
