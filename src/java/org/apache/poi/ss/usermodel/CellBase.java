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

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Removal;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

        if (cellType == CellType.FORMULA) {
            if (getCellType() != CellType.FORMULA){
                throw new IllegalArgumentException("Calling Cell.setCellType(CellType.FORMULA) is illegal. " +
                        "Use setCellFormula(String) directly.");
            } else {
                return;
            }
        }

        tryToDeleteArrayFormulaIfSet();

        setCellTypeImpl(cellType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBlank() {
        setCellType(CellType.BLANK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellAddress getAddress() {
        return new CellAddress(this);
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
        // todo validate formula here, before changing the cell?
        tryToDeleteArrayFormulaIfSet();

        if (formula == null) {
            removeFormula();
            return;
        }

        // formula cells always have a value. If the cell is blank (either initially or after removing an
        // array formula), set value to 0
        if (getValueType() == CellType.BLANK) {
            setCellValue(0);
        }

        setCellFormulaImpl(formula);
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @Removal(version = "4.2")
    @Override
    public final CellType getCellTypeEnum() {
        return getCellType();
    }

    /**
     * Implementation-specific setting the formula. Formula is not null.
     * Shall not change the value.
     * @param formula
     */
    protected abstract void setCellFormulaImpl(String formula);

    /**
     * Get value type of this cell. Can return BLANK, NUMERIC, STRING, BOOLEAN or ERROR.
     * For current implementations where type is strongly coupled with formula, is equivalent to
     * <code>getCellType() == CellType.FORMULA ? getCachedFormulaResultType() : getCellType()</code>
     *
     * <p>This is meant as a temporary helper method until the time when value type is decoupled from the formula.</p>
     * @return value type
     */
    protected final CellType getValueType() {
        CellType type = getCellType();
        if (type != CellType.FORMULA) {
            return type;
        }
        return getCachedFormulaResultType();
    }

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCellValue(double value) {
        if(Double.isInfinite(value)) {
            // Excel does not support positive/negative infinities,
            // rather, it gives a #DIV/0! error in these cases.
            setCellErrorValue(FormulaError.DIV0.getCode());
        } else if (Double.isNaN(value)){
            setCellErrorValue(FormulaError.NUM.getCode());
        } else {
            setCellValueImpl(value);
        }
    }

    /**
     * Implementation-specific way to set a numeric value.
     * <code>value</code> is guaranteed to be a valid (non-NaN) double.
     * The implementation is expected to adjust the cell type accordingly, so that after this call
     * getCellType() or getCachedFormulaResultType() would return {@link CellType#NUMERIC}.
     * @param value the new value to set
     */
    protected abstract void setCellValueImpl(double value);

    @Override
    public void setCellValue(Date value) {
        if(value == null) {
            setBlank();
            return;
        }
        setCellValueImpl(value);
    }

    /**
     * Implementation-specific way to set a date value.
     * <code>value</code> is guaranteed to be non-null.
     * The implementation is expected to adjust the cell type accordingly, so that after this call
     * getCellType() or getCachedFormulaResultType() would return {@link CellType#NUMERIC}.
     * @param value the new date to set
     */
    protected abstract void setCellValueImpl(Date value);

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCellValue(Calendar value) {
        if(value == null) {
            setBlank();
            return;
        }
        setCellValueImpl(value);
    }

    /**
     * Implementation-specific way to set a calendar value.
     * <code>value</code> is guaranteed to be non-null.
     * The implementation is expected to adjust the cell type accordingly, so that after this call
     * getCellType() or getCachedFormulaResultType() would return {@link CellType#NUMERIC}.
     * @param value the new calendar value to set
     */
    protected abstract void setCellValueImpl(Calendar value);

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCellValue(String value) {
        if(value == null){
            setBlank();
            return;
        }

        checkLength(value);

        setCellValueImpl(value);
    }

    /**
     * Implementation-specific way to set a string value.
     * The value is guaranteed to be non-null and to satisfy the length limitation imposed by the spreadsheet version.
     * The implementation is expected to adjust cell type accordingly, so that after this call
     * getCellType() or getCachedFormulaResultType() (whichever appropriate) would return {@link CellType#STRING}.
     * @param value the new value to set.
     */
    protected abstract void setCellValueImpl(String value);

    private void checkLength(String value) {
        if(value.length() > getSpreadsheetVersion().getMaxTextLength()){
            final String message = String.format(Locale.ROOT,
                    "The maximum length of cell contents (text) is %d characters",
                    getSpreadsheetVersion().getMaxTextLength());
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCellValue(RichTextString value) {
        if(value == null || value.getString() == null){
            setBlank();
            return;
        }

        checkLength(value.getString());

        setCellValueImpl(value);
    }

    /**
     * Implementation-specific way to set a RichTextString value.
     * The value is guaranteed to be non-null, having non-null value, and to satisfy the length limitation imposed
     * by the spreadsheet version.
     * The implementation is expected to adjust cell type accordingly, so that after this call
     * getCellType() or getCachedFormulaResultType() (whichever appropriate) would return {@link CellType#STRING}.
     * @param value the new value to set.
     */
    protected abstract void setCellValueImpl(RichTextString value);

    /**
     * Get the spreadsheet version for the given implementation.
     * @return the spreadsheet version
     */
    protected abstract SpreadsheetVersion getSpreadsheetVersion();
}
