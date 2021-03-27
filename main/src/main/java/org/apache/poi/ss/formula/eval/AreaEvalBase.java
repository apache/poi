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

package org.apache.poi.ss.formula.eval;

import org.apache.poi.ss.formula.SheetRange;
import org.apache.poi.ss.formula.ptg.AreaI;

/**
 * @author Josh Micich
 */
public abstract class AreaEvalBase implements AreaEval {

    private final int _firstSheet;
	private final int _firstColumn;
	private final int _firstRow;
    private final int _lastSheet;
	private final int _lastColumn;
	private final int _lastRow;
	private final int _nColumns;
	private final int _nRows;

	protected AreaEvalBase(SheetRange sheets, int firstRow, int firstColumn, int lastRow, int lastColumn) {
		_firstColumn = firstColumn;
		_firstRow = firstRow;
		_lastColumn = lastColumn;
		_lastRow = lastRow;

		_nColumns = _lastColumn - _firstColumn + 1;
		_nRows = _lastRow - _firstRow + 1;
		
		if (sheets != null) {
		    _firstSheet = sheets.getFirstSheetIndex();
		    _lastSheet = sheets.getLastSheetIndex();
		} else {
		    _firstSheet = -1;
		    _lastSheet = -1;
		}
	}
    protected AreaEvalBase(int firstRow, int firstColumn, int lastRow, int lastColumn) {
        this(null, firstRow, firstColumn, lastRow, lastColumn);
    }

	protected AreaEvalBase(AreaI ptg) {
	    this(ptg, null);
	}
    protected AreaEvalBase(AreaI ptg, SheetRange sheets) {
	    this(sheets, ptg.getFirstRow(), ptg.getFirstColumn(), ptg.getLastRow(), ptg.getLastColumn());
	}

	public final int getFirstColumn() {
		return _firstColumn;
	}

	public final int getFirstRow() {
		return _firstRow;
	}

	public final int getLastColumn() {
		return _lastColumn;
	}

	public final int getLastRow() {
		return _lastRow;
	}
	
	public int getFirstSheetIndex() {
	    return _firstSheet;
    }
    public int getLastSheetIndex() {
        return _lastSheet;
    }
    
    public final ValueEval getAbsoluteValue(int row, int col) {
		int rowOffsetIx = row - _firstRow;
		int colOffsetIx = col - _firstColumn;

		if(rowOffsetIx < 0 || rowOffsetIx >= _nRows) {
			throw new IllegalArgumentException("Specified row index (" + row
					+ ") is outside the allowed range (" + _firstRow + ".." + _lastRow + ")");
		}
		if(colOffsetIx < 0 || colOffsetIx >= _nColumns) {
			throw new IllegalArgumentException("Specified column index (" + col
					+ ") is outside the allowed range (" + _firstColumn + ".." + col + ")");
		}
		return getRelativeValue(rowOffsetIx, colOffsetIx);
	}

	public final boolean contains(int row, int col) {
		return _firstRow <= row && _lastRow >= row
			&& _firstColumn <= col && _lastColumn >= col;
	}

	public final boolean containsRow(int row) {
		return _firstRow <= row && _lastRow >= row;
	}

	public final boolean containsColumn(int col) {
		return _firstColumn <= col && _lastColumn >= col;
	}

	public final boolean isColumn() {
		return _firstColumn == _lastColumn;
	}

	public final boolean isRow() {
		return _firstRow == _lastRow;
	}
	public int getHeight() {
		return _lastRow-_firstRow+1;
	}

	public final ValueEval getValue(int row, int col) {
		return getRelativeValue(row, col);
	}
    public final ValueEval getValue(int sheetIndex, int row, int col) {
        return getRelativeValue(sheetIndex, row, col);
    }

	public abstract ValueEval getRelativeValue(int relativeRowIndex, int relativeColumnIndex);
    public abstract ValueEval getRelativeValue(int sheetIndex, int relativeRowIndex, int relativeColumnIndex);

	public int getWidth() {
		return _lastColumn-_firstColumn+1;
	}

    /**
     * @return  whether cell at rowIndex and columnIndex is a subtotal.
     * By default return false which means 'don't care about subtotals'
    */
    public boolean isSubTotal(int rowIndex, int columnIndex) {
        return false;
    }

    /**
     * @return false by default, meaning all rows are calculated
     * @see org.apache.poi.ss.formula.TwoDEval#isRowHidden(int)
     */
    public boolean isRowHidden(int rowIndex) {
        return false;
    }
}
