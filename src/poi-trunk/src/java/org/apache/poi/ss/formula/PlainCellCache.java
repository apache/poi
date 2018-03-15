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

import java.util.HashMap;
import java.util.Map;

final class PlainCellCache {

	public static final class Loc {

		private final long _bookSheetColumn;

		private final int _rowIndex;

		public Loc(int bookIndex, int sheetIndex, int rowIndex, int columnIndex) {
			_bookSheetColumn = toBookSheetColumn(bookIndex, sheetIndex, columnIndex);
			_rowIndex = rowIndex;
		}

		public static long toBookSheetColumn(int bookIndex, int sheetIndex, int columnIndex) {
			return ((bookIndex   & 0xFFFFl) << 48)  +
                   ((sheetIndex  & 0xFFFFl) << 32) +
                   ((columnIndex & 0xFFFFl) << 0);
		}

		public Loc(long bookSheetColumn, int rowIndex) {
			_bookSheetColumn = bookSheetColumn;
			_rowIndex = rowIndex;
		}

		@Override
        public int hashCode() {
			return (int)(_bookSheetColumn ^ (_bookSheetColumn >>> 32)) + 17 * _rowIndex;
		}

		@Override
        public boolean equals(Object obj) {
		    if (!(obj instanceof Loc)) {
		        return false;
		    }
			Loc other = (Loc) obj;
			return _bookSheetColumn == other._bookSheetColumn && _rowIndex == other._rowIndex;
		}

		public int getRowIndex() {
			return _rowIndex;
		}

		public int getColumnIndex() {
            return (int)(_bookSheetColumn & 0x000FFFF);
		}

        public int getSheetIndex() {
            return (int)((_bookSheetColumn >> 32) & 0xFFFF);
        }

        public int getBookIndex() {
            return (int)((_bookSheetColumn >> 48) & 0xFFFF);
        }
	}

	private Map<Loc, PlainValueCellCacheEntry> _plainValueEntriesByLoc;

	public PlainCellCache() {
		_plainValueEntriesByLoc = new HashMap<>();
	}
	public void put(Loc key, PlainValueCellCacheEntry cce) {
		_plainValueEntriesByLoc.put(key, cce);
	}
	public void clear() {
		_plainValueEntriesByLoc.clear();
	}
	public PlainValueCellCacheEntry get(Loc key) {
		return _plainValueEntriesByLoc.get(key);
	}
	public void remove(Loc key) {
		_plainValueEntriesByLoc.remove(key);
	}
}
