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

package org.apache.poi.ss.formula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.util.CellReference;

/**
 * Optimization - compacts many blank cell references used by a single formula.
 */
final class FormulaUsedBlankCellSet {
	public static final class BookSheetKey {

		private final int _bookIndex;
		private final int _sheetIndex;

		public BookSheetKey(int bookIndex, int sheetIndex) {
			_bookIndex = bookIndex;
			_sheetIndex = sheetIndex;
		}
		@Override
        public int hashCode() {
			return _bookIndex * 17 + _sheetIndex;
		}
		@Override
        public boolean equals(Object obj) {
		    if (!(obj instanceof BookSheetKey)) {
		        return false;
		    }
			BookSheetKey other = (BookSheetKey) obj;
			return _bookIndex == other._bookIndex && _sheetIndex == other._sheetIndex;
		}
	}

	private static final class BlankCellSheetGroup {
		private final List<BlankCellRectangleGroup> _rectangleGroups;
		private int _currentRowIndex;
		private int _firstColumnIndex;
		private int _lastColumnIndex;
		private BlankCellRectangleGroup _currentRectangleGroup;
        private int _lastDefinedRow;

		public BlankCellSheetGroup(int lastDefinedRow) {
			_rectangleGroups = new ArrayList<>();
			_currentRowIndex = -1;
			_lastDefinedRow = lastDefinedRow;
		}

		public void addCell(int rowIndex, int columnIndex) {
		    if (rowIndex > _lastDefinedRow) return;
		    
			if (_currentRowIndex == -1) {
				_currentRowIndex = rowIndex;
				_firstColumnIndex = columnIndex;
				_lastColumnIndex = columnIndex;
			} else {
				if (_currentRowIndex == rowIndex && _lastColumnIndex+1 == columnIndex) {
					_lastColumnIndex = columnIndex;
				} else {
					// cell does not fit on end of current row
					if (_currentRectangleGroup == null) {
						_currentRectangleGroup = new BlankCellRectangleGroup(_currentRowIndex, _firstColumnIndex, _lastColumnIndex);
					} else {
						if (!_currentRectangleGroup.acceptRow(_currentRowIndex, _firstColumnIndex, _lastColumnIndex)) {
							_rectangleGroups.add(_currentRectangleGroup);
							_currentRectangleGroup = new BlankCellRectangleGroup(_currentRowIndex, _firstColumnIndex, _lastColumnIndex);
						}
					}
					_currentRowIndex = rowIndex;
					_firstColumnIndex = columnIndex;
					_lastColumnIndex = columnIndex;
				}
			}
		}

		public boolean containsCell(int rowIndex, int columnIndex) {
		    if (rowIndex > _lastDefinedRow) return true;
		    
			for (int i=_rectangleGroups.size()-1; i>=0; i--) {
				BlankCellRectangleGroup bcrg = _rectangleGroups.get(i);
				if (bcrg.containsCell(rowIndex, columnIndex)) {
					return true;
				}
			}
			if(_currentRectangleGroup != null && _currentRectangleGroup.containsCell(rowIndex, columnIndex)) {
				return true;
			}
			if (_currentRowIndex != -1 && _currentRowIndex == rowIndex) {
				if (_firstColumnIndex <=  columnIndex && columnIndex <= _lastColumnIndex) {
					return true;
				}
			}
			return false;
		}
	}

	private static final class BlankCellRectangleGroup {

		private final int _firstRowIndex;
		private final int _firstColumnIndex;
		private final int _lastColumnIndex;
		private int _lastRowIndex;

		public BlankCellRectangleGroup(int firstRowIndex, int firstColumnIndex, int lastColumnIndex) {
			_firstRowIndex = firstRowIndex;
			_firstColumnIndex = firstColumnIndex;
			_lastColumnIndex = lastColumnIndex;
			_lastRowIndex = firstRowIndex;
		}

		public boolean containsCell(int rowIndex, int columnIndex) {
			if (columnIndex < _firstColumnIndex) {
				return false;
			}
			if (columnIndex > _lastColumnIndex) {
				return false;
			}
			if (rowIndex < _firstRowIndex) {
				return false;
			}
			if (rowIndex > _lastRowIndex) {
				return false;
			}
			return true;
		}

		public boolean acceptRow(int rowIndex, int firstColumnIndex, int lastColumnIndex) {
			if (firstColumnIndex != _firstColumnIndex) {
				return false;
			}
			if (lastColumnIndex != _lastColumnIndex) {
				return false;
			}
			if (rowIndex != _lastRowIndex+1) {
				return false;
			}
			_lastRowIndex = rowIndex;
			return true;
		}
		@Override
        public String toString() {
			StringBuffer sb = new StringBuffer(64);
			CellReference crA = new CellReference(_firstRowIndex, _firstColumnIndex, false, false);
			CellReference crB = new CellReference(_lastRowIndex, _lastColumnIndex, false, false);
			sb.append(getClass().getName());
			sb.append(" [").append(crA.formatAsString()).append(':').append(crB.formatAsString()).append("]");
			return sb.toString();
		}
	}

	private final Map<BookSheetKey, BlankCellSheetGroup> _sheetGroupsByBookSheet;

	public FormulaUsedBlankCellSet() {
		_sheetGroupsByBookSheet = new HashMap<>();
	}

	public void addCell(EvaluationWorkbook evalWorkbook, int bookIndex, int sheetIndex, int rowIndex, int columnIndex) {
		BlankCellSheetGroup sbcg = getSheetGroup(evalWorkbook, bookIndex, sheetIndex);
		sbcg.addCell(rowIndex, columnIndex);
	}

	private BlankCellSheetGroup getSheetGroup(EvaluationWorkbook evalWorkbook, int bookIndex, int sheetIndex) {
		BookSheetKey key = new BookSheetKey(bookIndex, sheetIndex);

		BlankCellSheetGroup result = _sheetGroupsByBookSheet.get(key);
		if (result == null) {
			result = new BlankCellSheetGroup(evalWorkbook.getSheet(sheetIndex).getLastRowNum());
			_sheetGroupsByBookSheet.put(key, result);
		}
		return result;
	}

	public boolean containsCell(BookSheetKey key, int rowIndex, int columnIndex) {
		BlankCellSheetGroup bcsg = _sheetGroupsByBookSheet.get(key);
		if (bcsg == null) {
			return false;
		}
		return bcsg.containsCell(rowIndex, columnIndex);
	}

	public boolean isEmpty() {
		return _sheetGroupsByBookSheet.isEmpty();
	}
}
