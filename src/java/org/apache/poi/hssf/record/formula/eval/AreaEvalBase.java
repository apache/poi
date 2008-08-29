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

package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.AreaI;

/**
 * @author Josh Micich
 */
abstract class AreaEvalBase implements AreaEval {

	private final int _firstColumn;
	private final int _firstRow;
	private final int _lastColumn;
	private final int _lastRow;
	private final ValueEval[] _values;
	private final int _nColumns;
	private final int _nRows;

	protected AreaEvalBase(AreaI ptg, ValueEval[] values) {
		if (values == null) {
			throw new IllegalArgumentException("values must not be null");
		}
		_firstRow = ptg.getFirstRow();
		_firstColumn = ptg.getFirstColumn();
		_lastRow = ptg.getLastRow();
		_lastColumn = ptg.getLastColumn();
		
		_nColumns = _lastColumn - _firstColumn + 1;
		_nRows = _lastRow - _firstRow + 1;
		
		int expectedItemCount = _nRows * _nColumns;
		if ((values.length != expectedItemCount)) {
			// Note - this math may need alteration when POI starts to support full column or full row refs
			throw new IllegalArgumentException("Array size should be (" + expectedItemCount
					+ ") but was (" + values.length + ")");
		}
		


		for (int i = values.length - 1; i >= 0; i--) {
			if (values[i] == null) {
				throw new IllegalArgumentException("value array elements must not be null");
			}
		}
		_values = values;
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

	public final ValueEval[] getValues() {
		// TODO - clone() - but some junits rely on not cloning at the moment
		return _values;
	}

	public final ValueEval getValueAt(int row, int col) {
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
		return (_firstRow <= row) && (_lastRow >= row);
	}

	public final boolean containsColumn(short col) {
		return (_firstColumn <= col) && (_lastColumn >= col);
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

	public ValueEval getRelativeValue(int relativeRowIndex, int relativeColumnIndex) {
		int index = relativeRowIndex * _nColumns + relativeColumnIndex;
		ValueEval result = _values[index];
		if (result == null) {
			return BlankEval.INSTANCE;
		}
		return result;
	}

	public int getWidth() {
		return _lastColumn-_firstColumn+1;
	}
}
