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

package org.apache.poi.xssf.usermodel;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.util.Internal;

/**
 * XSSF wrapper for a sheet under evaluation
 */
@Internal
final class XSSFEvaluationSheet implements EvaluationSheet {

    private final XSSFSheet _xs;
    private Map<CellKey, EvaluationCell> _cellCache;
    private int _lastDefinedRow = -1;

    public XSSFEvaluationSheet(XSSFSheet sheet) {
        _xs = sheet;
        _lastDefinedRow = _xs.getLastRowNum();
    }

    public XSSFSheet getXSSFSheet() {
        return _xs;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.ss.formula.EvaluationSheet#getlastRowNum()
     * @since POI 4.0.0
     */
    @Override
    public int getLastRowNum() {
        return _lastDefinedRow;
    }
    
    /* (non-JavaDoc), inherit JavaDoc from EvaluationWorkbook
     * @since POI 3.15 beta 3
     */
    @Override
    public void clearAllCachedResultValues() {
        _cellCache = null;
        _lastDefinedRow = _xs.getLastRowNum();
    }
    
    @Override
    public EvaluationCell getCell(int rowIndex, int columnIndex) {
        // shortcut evaluation if reference is outside the bounds of existing data
        // see issue #61841 for impact on VLOOKUP in particular
        if (rowIndex > _lastDefinedRow) return null;
        
        // cache for performance: ~30% speedup due to caching
        if (_cellCache == null) {
            _cellCache = new HashMap<>(_xs.getLastRowNum() * 3);
            for (final Row row : _xs) {
                final int rowNum = row.getRowNum();
                for (final Cell cell : row) {
                    // cast is safe, the iterator is just defined using the interface
                    final CellKey key = new CellKey(rowNum, cell.getColumnIndex());
                    final EvaluationCell evalcell = new XSSFEvaluationCell((XSSFCell) cell, this);
                    _cellCache.put(key, evalcell);
                }
            }
        }
        
        final CellKey key = new CellKey(rowIndex, columnIndex);
        EvaluationCell evalcell = _cellCache.get(key);
        
        // If cache is stale, update cache with this one cell
        // This is a compromise between rebuilding the entire cache
        // (which would quickly defeat the benefit of the cache)
        // and not caching at all.
        // See bug 59958: Add cells on the fly to the evaluation sheet cache on cache miss
        if (evalcell == null) {
            XSSFRow row = _xs.getRow(rowIndex);
            if (row == null) {
                return null;
            }
            XSSFCell cell = row.getCell(columnIndex);
            if (cell == null) {
                return null;
            }
            evalcell = new XSSFEvaluationCell(cell, this);
            _cellCache.put(key, evalcell);
        }

        return evalcell;
    }
    
    private static class CellKey {
        private final int _row;
        private final int _col;
        private int _hash = -1; //lazily computed
        
        protected CellKey(int row, int col) {
            _row = row;
            _col = col;
        }
        
        @Override
        public int hashCode() {
            if ( _hash == -1 ) {
                 _hash = (17 * 37 + _row) * 37 + _col;
            }
            return _hash;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CellKey)) {
                return false;
            }
            // assumes other object is one of us, otherwise ClassCastException is thrown
            final CellKey oKey = (CellKey) obj;
            return _row == oKey._row && _col == oKey._col;
        }
    }
}
