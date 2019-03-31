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

package org.apache.poi.ss.formula.eval.forked;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationSheet;
import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Internal;

/**
 * Represents a sheet being used for forked evaluation.  Initially, objects of this class contain
 * only the cells from the master workbook. By calling {@link #getOrCreateUpdatableCell(int, int)},
 * the master cell object is logically replaced with a {@link ForkedEvaluationCell} instance, which
 * will be used in all subsequent evaluations.<br>
 *
 * For POI internal use only
 */
@Internal
final class ForkedEvaluationSheet implements EvaluationSheet {

    private final EvaluationSheet _masterSheet;
    
    /**
     * Only cells which have been split are put in this map.  (This has been done to conserve memory).
     */
    private final Map<RowColKey, ForkedEvaluationCell> _sharedCellsByRowCol;

    public ForkedEvaluationSheet(EvaluationSheet masterSheet) {
        _masterSheet = masterSheet;
        _sharedCellsByRowCol = new HashMap<>();
    }
    
    /* (non-Javadoc)
     * @see org.apache.poi.ss.formula.EvaluationSheet#getlastRowNum()
     * @since POI 4.0.0
     */
    @Override
    public int getLastRowNum() {
        return _masterSheet.getLastRowNum();
    }
    
    /* (non-Javadoc)
     * @see org.apache.poi.ss.formula.EvaluationSheet#isRowHidden(int)
     * @since POI 4.1.0
     */
    public boolean isRowHidden(int rowIndex) {
        return _masterSheet.isRowHidden(rowIndex);
    }
    
    @Override
    public EvaluationCell getCell(int rowIndex, int columnIndex) {
        RowColKey key = new RowColKey(rowIndex, columnIndex);

        ForkedEvaluationCell result = _sharedCellsByRowCol.get(key);
        if (result == null) {
            return _masterSheet.getCell(rowIndex, columnIndex);
        }
        return result;
    }

    public ForkedEvaluationCell getOrCreateUpdatableCell(int rowIndex, int columnIndex) {
        RowColKey key = new RowColKey(rowIndex, columnIndex);

        ForkedEvaluationCell result = _sharedCellsByRowCol.get(key);
        if (result == null) {
            EvaluationCell mcell = _masterSheet.getCell(rowIndex, columnIndex);
            if (mcell == null) {
                CellReference cr = new CellReference(rowIndex, columnIndex);
                throw new UnsupportedOperationException("Underlying cell '"
                        + cr.formatAsString() + "' is missing in master sheet.");
            }
            result = new ForkedEvaluationCell(this, mcell);
            _sharedCellsByRowCol.put(key, result);
        }
        return result;
    }

    public void copyUpdatedCells(Sheet sheet) {
        RowColKey[] keys = new RowColKey[_sharedCellsByRowCol.size()];
        _sharedCellsByRowCol.keySet().toArray(keys);
        Arrays.sort(keys);
        for (int i = 0; i < keys.length; i++) {
            RowColKey key = keys[i];
            Row row = sheet.getRow(key.getRowIndex());
            if (row == null) {
                row = sheet.createRow(key.getRowIndex());
            }
            Cell destCell = row.getCell(key.getColumnIndex());
            if (destCell == null) {
                destCell = row.createCell(key.getColumnIndex());
            }

            ForkedEvaluationCell srcCell = _sharedCellsByRowCol.get(key);
            srcCell.copyValue(destCell);
        }
    }

    public int getSheetIndex(EvaluationWorkbook mewb) {
        return mewb.getSheetIndex(_masterSheet);
    }

    /* (non-Javadoc)
     * leave the map alone, if it needs resetting, reusing this class is probably a bad idea.
     * @see org.apache.poi.ss.formula.EvaluationSheet#clearAllCachedResultValues()
     * 
     * @since POI 3.15 beta 3
     */
    @Override
    public void clearAllCachedResultValues() {
        _masterSheet.clearAllCachedResultValues();
    }
    
    // FIXME: serves same purpose as org.apache.poi.xssf.usermodel.XSSFEvaluationSheet$CellKey
    private static final class RowColKey implements Comparable<RowColKey>{
        private final int _rowIndex;
        private final int _columnIndex;

        public RowColKey(int rowIndex, int columnIndex) {
            _rowIndex = rowIndex;
            _columnIndex = columnIndex;
        }
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof RowColKey)) {
                return false;
            }
            RowColKey other = (RowColKey) obj;
            return _rowIndex == other._rowIndex && _columnIndex == other._columnIndex;
        }
        @Override
        public int hashCode() {
            return _rowIndex ^ _columnIndex;
        }
        @Override
        public int compareTo(RowColKey o) {
            int cmp = _rowIndex - o._rowIndex;
            if (cmp != 0) {
                return cmp;
            }
            return _columnIndex - o._columnIndex;
        }
        public int getRowIndex() {
            return _rowIndex;
        }
        public int getColumnIndex() {
            return _columnIndex;
        }
    }
}
