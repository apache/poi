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

import java.util.Arrays;

import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Stores details about the current evaluation of a cell.<br>
 */
final class CellEvaluationFrame {

    private final FormulaCellCacheEntry _cce;
    /**
     * the cells read so far, in reading order. A cell the formula reads twice appears twice; the
     * duplicates are dropped when the entry registers itself with its inputs, which is cheaper
     * than de-duplicating every read here (most formulas read each cell once)
     */
    private CellCacheEntry[] _sensitiveInputCells = CellCacheEntry.EMPTY_ARRAY;
    private int _sensitiveInputCellCount;
    private FormulaUsedBlankCellSet _usedBlankCellGroup;

    public CellEvaluationFrame(FormulaCellCacheEntry cce) {
        _cce = cce;
    }
    public CellCacheEntry getCCE() {
        return _cce;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append(getClass().getName()).append(" [");
        sb.append(']');
        return sb.toString();
    }
    /**
     * @param inputCell a cell directly used by the formula of this evaluation frame
     */
    public void addSensitiveInputCell(CellCacheEntry inputCell) {
        int n = _sensitiveInputCellCount;
        if (n == _sensitiveInputCells.length) {
            _sensitiveInputCells = Arrays.copyOf(_sensitiveInputCells, n == 0 ? 8 : n * 2);
        }
        _sensitiveInputCells[n] = inputCell;
        _sensitiveInputCellCount = n + 1;
    }
    /**
     * @return never <code>null</code>, (possibly empty) array of all cells directly used while
     * evaluating the formula of this frame, possibly with duplicates. The frame is done with the
     * array afterwards, so the caller may keep it.
     */
    private CellCacheEntry[] getSensitiveInputCells() {
        int nItems = _sensitiveInputCellCount;
        if (nItems < 1) {
            return CellCacheEntry.EMPTY_ARRAY;
        }
        return nItems == _sensitiveInputCells.length
                ? _sensitiveInputCells
                : Arrays.copyOf(_sensitiveInputCells, nItems);
    }
    public void addUsedBlankCell(EvaluationWorkbook evalWorkbook, int bookIndex, int sheetIndex, int rowIndex, int columnIndex) {
        if (_usedBlankCellGroup == null) {
            _usedBlankCellGroup = new FormulaUsedBlankCellSet();
        }
        _usedBlankCellGroup.addCell(evalWorkbook, bookIndex, sheetIndex, rowIndex, columnIndex);
    }

    public void updateFormulaResult(ValueEval result) {
        _cce.updateFormulaResult(result, getSensitiveInputCells(), _usedBlankCellGroup);
    }
}
