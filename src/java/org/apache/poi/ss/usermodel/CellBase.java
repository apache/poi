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

package org.apache.poi.ss.usermodel;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

/**
 * Common implementation-independent logic shared by all implementations of {@link Cell}.
 * @author Vladislav "gallon" Galas gallon at apache dot org
 */
public abstract class CellBase implements Cell {
    /**
     * {@inheritDoc}
     */
    @Override
    public final void setCellType(CellType cellType) {
        if (cellType == null || cellType == CellType._NONE) {
            throw new IllegalArgumentException("cellType shall not be null nor _NONE");
        }

        tryToDeleteArrayFormulaIfSet();

        setCellTypeImpl(cellType);
    }

    /**
     * Implementation-specific logic
     * @param cellType new cell type. Guaranteed non-null, not _NONE.
     */
    protected abstract void setCellTypeImpl(CellType cellType);

    /**
     * Called when this an array formula in this cell is deleted.
     * <p>The purpose of this method is to validate the cell state prior to modification.</p>
     *
     * @param message a customized exception message for the case if deletion of the cell is impossible. If null, a
     *                default message will be generated
     * @see #setCellType(CellType)
     * @see #setCellFormula(String)
     * @see Row#removeCell(org.apache.poi.ss.usermodel.Cell)
     * @see org.apache.poi.ss.usermodel.Sheet#removeRow(org.apache.poi.ss.usermodel.Row)
     * @see org.apache.poi.ss.usermodel.Sheet#shiftRows(int, int, int)
     * @see org.apache.poi.ss.usermodel.Sheet#addMergedRegion(org.apache.poi.ss.util.CellRangeAddress)
     * @throws IllegalStateException if modification is not allowed
     *
     * Note. Exposing this to public is ugly. Needed for methods like Sheet#shiftRows.
     */
    public final void tryToDeleteArrayFormula(String message) {
        assert isPartOfArrayFormulaGroup();

        CellRangeAddress arrayFormulaRange = getArrayFormulaRange();
        if(arrayFormulaRange.getNumberOfCells() > 1) {
            if (message == null) {
                message = "Cell " + new CellReference(this).formatAsString() + " is part of a multi-cell array formula. " +
                        "You cannot change part of an array.";
            }
            throw new IllegalStateException(message);
        }
        //un-register the single-cell array formula from the parent sheet through public interface
        getRow().getSheet().removeArrayFormula(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setCellFormula(String formula) throws FormulaParseException, IllegalStateException {
        if (formula == null) {
            removeFormula();
            return;
        }

        CellType previousValueType = getCellType() == CellType.FORMULA ? getCachedFormulaResultType() : getCellType();

        tryToDeleteArrayFormulaIfSet();

        setCellFormulaImpl(formula);

        if (previousValueType == CellType.BLANK) {
            setCellValue(0);
        }
    }

    /**
     * Implementation-specific setting the formula.
     * Shall not change the value.
     * @param formula
     */
    protected abstract void setCellFormulaImpl(String formula);

    /**
     * {@inheritDoc}
     */
    @Override
    public final void removeFormula() {
        if (getCellType() == CellType.BLANK) {
            return;
        }

        if (isPartOfArrayFormulaGroup()) {
            tryToDeleteArrayFormula(null);
            return;
        }

        removeFormulaImpl();
    }

    /**
     * Implementation-specific removal of the formula.
     * The cell is guaranteed to have a regular formula set.
     * Shall preserve the "cached" value.
     */
    protected abstract void removeFormulaImpl();

    private void tryToDeleteArrayFormulaIfSet() {
        if (isPartOfArrayFormulaGroup()) {
            tryToDeleteArrayFormula(null);
        }
    }
}
