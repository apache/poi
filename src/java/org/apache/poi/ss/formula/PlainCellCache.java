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

/**
 *
 * @author Josh Micich
 */
final class PlainCellCache {

	public static final class Loc {

		private final int _bookSheetColumn;

		private final int _rowIndex;

		public Loc(int bookIndex, int sheetIndex, int rowIndex, int columnIndex) {
			_bookSheetColumn = toBookSheetColumn(bookIndex, sheetIndex, columnIndex);
			_rowIndex = rowIndex;
		}

		public static int toBookSheetColumn(int bookIndex, int sheetIndex, int columnIndex) {
			return ((bookIndex & 0x00FF) << 24) + ((sheetIndex & 0x00FF) << 16)
					+ ((columnIndex & 0xFFFF) << 0);
		}

		public Loc(int bookSheetColumn, int rowIndex) {
			_bookSheetColumn = bookSheetColumn;
			_rowIndex = rowIndex;
		}

		public int hashCode() {
			return _bookSheetColumn + 17 * _rowIndex;
		}

		public boolean equals(Object obj) {
			assert obj instanceof Loc : "these package-private cache key instances are only compared to themselves";
			Loc other = (Loc) obj;
			return _bookSheetColumn == other._bookSheetColumn && _rowIndex == other._rowIndex;
		}

		public int getRowIndex() {
			return _rowIndex;
		}
		public int getColumnIndex() {
			return _bookSheetColumn & 0x000FFFF;
		}
	}

	private Map<Loc, PlainValueCellCacheEntry> _plainValueEntriesByLoc;

	public PlainCellCache() {
		_plainValueEntriesByLoc = new HashMap<Loc, PlainValueCellCacheEntry>();
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
