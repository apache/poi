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

/**
 * XSSF wrapper for a sheet under evaluation
 */
final class XSSFEvaluationSheet implements EvaluationSheet {

    private final XSSFSheet _xs;
    private Map<CellKey, EvaluationCell> _cellCache;

    public XSSFEvaluationSheet(XSSFSheet sheet) {
        _xs = sheet;
    }

    public XSSFSheet getXSSFSheet() {
        return _xs;
    }

    public EvaluationCell getCell(int rowIndex, int columnIndex) {
        // cache for performance: ~30% speedup due to caching
        if (_cellCache == null) {
            _cellCache = new HashMap<CellKey, EvaluationCell>(_xs.getLastRowNum()*3);
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
        
        return _cellCache.get(new CellKey(rowIndex, columnIndex));
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
