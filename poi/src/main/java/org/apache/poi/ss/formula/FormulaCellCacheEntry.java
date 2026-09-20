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
import org.apache.poi.ss.formula.FormulaUsedBlankCellSet.BookSheetKey;


/**
 * Stores the cached result of a formula evaluation, along with the set of sensitive input cells
 */
final class FormulaCellCacheEntry extends CellCacheEntry {
    
    /**
     * Cells 'used' in the current evaluation of the formula corresponding to this cache entry
     *
     * If any of the following cells change, this cache entry needs to be cleared
     */
    private CellCacheEntry[] _sensitiveInputCells;

    private FormulaUsedBlankCellSet _usedBlankCellGroup;

    /** whether the formula calls SUBTOTAL: {@code null} until asked, forgotten when the formula changes */
    private Boolean _isSubTotal;

    public FormulaCellCacheEntry() {
        // leave fields un-set
    }

    /**
     * @return whether the formula of this cell calls SUBTOTAL, or {@code null} if that has not
     * been determined since the entry was created or the formula last changed
     * @since 6.0.0
     */
    public Boolean isSubTotal() {
        return _isSubTotal;
    }

    /**
     * @since 6.0.0
     */
    public void setSubTotal(Boolean isSubTotal) {
        _isSubTotal = isSubTotal;
    }
    
    public boolean isInputSensitive() {
        if (_sensitiveInputCells != null) {
            if (_sensitiveInputCells.length > 0 ) {
                return true;
            }
        }
        return _usedBlankCellGroup == null ? false : !_usedBlankCellGroup.isEmpty();
    }

    /**
     * Replaces the cells this formula depends on, registering the entry as a consumer of each of
     * them and unregistering it from the ones it no longer reads.
     *
     * @param sensitiveInputCells the cells the formula read, or {@code null} for none. The array is
     *        retained and compacted in place (a cell listed more than once is kept once), so the
     *        caller must not use it afterwards.
     */
    public void setSensitiveInputCells(CellCacheEntry[] sensitiveInputCells) {
        // need to tell all cells that were previously used, but no longer are,
        // that they are not consumed by this cell any more. Unregistering from all of them and
        // registering again with the new ones is set semantics either way, and the previous
        // inputs are almost always null here (they are cleared together with the cached value).
        clearConsumingCells();
        if (sensitiveInputCells == null) {
            _sensitiveInputCells = null;
            return;
        }
        int nKept = 0;
        for (CellCacheEntry usedCell : sensitiveInputCells) {
            if (usedCell.addConsumingCell(this)) {
                sensitiveInputCells[nKept++] = usedCell;
            }
            // else - already registered: the formula read this cell more than once
        }
        _sensitiveInputCells = nKept == sensitiveInputCells.length
                ? sensitiveInputCells
                : Arrays.copyOf(sensitiveInputCells, nKept);
    }

    public void clearFormulaEntry() {
        clearConsumingCells();
        _sensitiveInputCells = null;
        clearValue();
    }

    private void clearConsumingCells() {
        CellCacheEntry[] usedCells = _sensitiveInputCells;
        if (usedCells != null) {
            for (int i = usedCells.length-1; i>=0; i--) {
                usedCells[i].clearConsumingCell(this);
            }
        }
    }

    public void updateFormulaResult(ValueEval result, CellCacheEntry[] sensitiveInputCells, FormulaUsedBlankCellSet usedBlankAreas) {
        updateValue(result);
        setSensitiveInputCells(sensitiveInputCells);
        _usedBlankCellGroup = usedBlankAreas;
    }

    public void notifyUpdatedBlankCell(BookSheetKey bsk, int rowIndex, int columnIndex, IEvaluationListener evaluationListener) {
        if (_usedBlankCellGroup != null) {
            if (_usedBlankCellGroup.containsCell(bsk, rowIndex, columnIndex)) {
                clearFormulaEntry();
                recurseClearCachedFormulaResults(evaluationListener);
            }
        }
    }
}
