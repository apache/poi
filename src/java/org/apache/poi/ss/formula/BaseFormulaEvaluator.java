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

import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Common functionality across file formats for evaluating formula cells.<p>
 */
public abstract class BaseFormulaEvaluator implements FormulaEvaluator, WorkbookEvaluatorProvider {
    protected final WorkbookEvaluator _bookEvaluator;

    protected BaseFormulaEvaluator(WorkbookEvaluator bookEvaluator) {
        this._bookEvaluator = bookEvaluator;
    }

    /**
     * Coordinates several formula evaluators together so that formulas that involve external
     * references can be evaluated.
     * @param workbookNames the simple file names used to identify the workbooks in formulas
     * with external links (for example "MyData.xls" as used in a formula "[MyData.xls]Sheet1!A1")
     * @param evaluators all evaluators for the full set of workbooks required by the formulas.
     */
    public static void setupEnvironment(String[] workbookNames, BaseFormulaEvaluator[] evaluators) {
        WorkbookEvaluator[] wbEvals = new WorkbookEvaluator[evaluators.length];
        for (int i = 0; i < wbEvals.length; i++) {
            wbEvals[i] = evaluators[i]._bookEvaluator;
        }
        CollaboratingWorkbooksEnvironment.setup(workbookNames, wbEvals);
    }

    @Override
    public void setupReferencedWorkbooks(Map<String, FormulaEvaluator> evaluators) {
        CollaboratingWorkbooksEnvironment.setupFormulaEvaluator(evaluators);
    }

    @Override
    public WorkbookEvaluator _getWorkbookEvaluator() {
        return _bookEvaluator;
    }

    /**
     * Should be called whenever there are major changes (e.g. moving sheets) to input cells
     * in the evaluated workbook.  If performance is not critical, a single call to this method
     * may be used instead of many specific calls to the notify~ methods.
     *
     * Failure to call this method after changing cell values will cause incorrect behaviour
     * of the evaluate~ methods of this class
     */
    @Override
    public void clearAllCachedResultValues() {
        _bookEvaluator.clearAllCachedResultValues();
    }

    /**
     * If cell contains a formula, the formula is evaluated and returned,
     * else the CellValue simply copies the appropriate cell value from
     * the cell and also its cell type. This method should be preferred over
     * evaluateInCell() when the call should not modify the contents of the
     * original cell.
     *
     * @param cell may be <code>null</code> signifying that the cell is not present (or blank)
     * @return <code>null</code> if the supplied cell is <code>null</code> or blank
     */
    @Override
    public CellValue evaluate(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellTypeEnum()) {
            case BOOLEAN:
                return CellValue.valueOf(cell.getBooleanCellValue());
            case ERROR:
                return CellValue.getError(cell.getErrorCellValue());
            case FORMULA:
                return evaluateFormulaCellValue(cell);
            case NUMERIC:
                return new CellValue(cell.getNumericCellValue());
            case STRING:
                return new CellValue(cell.getRichStringCellValue().getString());
            case BLANK:
                return null;
            default:
                throw new IllegalStateException("Bad cell type (" + cell.getCellTypeEnum() + ")");
        }
    }
    
    /**
     * If cell contains formula, it evaluates the formula, and
     *  puts the formula result back into the cell, in place
     *  of the old formula.
     * Else if cell does not contain formula, this method leaves
     *  the cell unchanged.
     * Note that the same instance of HSSFCell is returned to
     * allow chained calls like:
     * <pre>
     * int evaluatedCellType = evaluator.evaluateInCell(cell).getCellType();
     * </pre>
     * Be aware that your cell value will be changed to hold the
     *  result of the formula. If you simply want the formula
     *  value computed for you, use {@link #evaluateFormulaCellEnum(Cell)}}
     * @param cell
     * @return the {@code cell} that was passed in, allowing for chained calls
     */
    @Override
    public Cell evaluateInCell(Cell cell) {
        if (cell == null) {
            return null;
        }
        Cell result = cell;
        if (cell.getCellTypeEnum() == CellType.FORMULA) {
            CellValue cv = evaluateFormulaCellValue(cell);
            setCellValue(cell, cv);
            setCellType(cell, cv); // cell will no longer be a formula cell
        }
        return result;
    }

    protected abstract CellValue evaluateFormulaCellValue(Cell cell);

    /**
     * If cell contains formula, it evaluates the formula, and saves the result of the formula. The
     * cell remains as a formula cell. If the cell does not contain formula, this method returns -1
     * and leaves the cell unchanged.
     *
     * Note that the type of the <em>formula result</em> is returned, so you know what kind of
     * cached formula result is also stored with  the formula.
     * <pre>
     * int evaluatedCellType = evaluator.evaluateFormulaCell(cell);
     * </pre>
     * Be aware that your cell will hold both the formula, and the result. If you want the cell
     * replaced with the result of the formula, use {@link #evaluateInCell(org.apache.poi.ss.usermodel.Cell)}
     * @param cell The cell to evaluate
     * @return -1 for non-formula cells, or the type of the <em>formula result</em>
     * @deprecated 3.15. Will return a {@link CellType} enum in the future.
     */
    @Override
    public int evaluateFormulaCell(Cell cell) {
        return evaluateFormulaCellEnum(cell).getCode();
    }
    
    /**
     * If cell contains formula, it evaluates the formula,
     *  and saves the result of the formula. The cell
     *  remains as a formula cell.
     * Else if cell does not contain formula, this method leaves
     *  the cell unchanged.
     * Note that the type of the formula result is returned,
     *  so you know what kind of value is also stored with
     *  the formula.
     * <pre>
     * CellType evaluatedCellType = evaluator.evaluateFormulaCellEnum(cell);
     * </pre>
     * Be aware that your cell will hold both the formula,
     *  and the result. If you want the cell replaced with
     *  the result of the formula, use {@link #evaluate(org.apache.poi.ss.usermodel.Cell)} }
     * @param cell The cell to evaluate
     * @return The type of the formula result (the cell's type remains as CellType.FORMULA however)
     *         If cell is not a formula cell, returns {@link CellType#_NONE} rather than throwing an exception.
     * @since POI 3.15 beta 3
     */
    @Override
    public CellType evaluateFormulaCellEnum(Cell cell) {
        if (cell == null || cell.getCellTypeEnum() != CellType.FORMULA) {
            return CellType._NONE;
        }
        CellValue cv = evaluateFormulaCellValue(cell);
        // cell remains a formula cell, but the cached value is changed
        setCellValue(cell, cv);
        return cv.getCellTypeEnum();
    }

    protected static void setCellType(Cell cell, CellValue cv) {
        CellType cellType = cv.getCellTypeEnum();
        switch (cellType) {
            case BOOLEAN:
            case ERROR:
            case NUMERIC:
            case STRING:
                cell.setCellType(cellType);
                return;
            case BLANK:
                // never happens - blanks eventually get translated to zero
                throw new IllegalArgumentException("This should never happen. Blanks eventually get translated to zero.");
            case FORMULA:
                // this will never happen, we have already evaluated the formula
                throw new IllegalArgumentException("This should never happen. Formulas should have already been evaluated.");
            default:
                throw new IllegalStateException("Unexpected cell value type (" + cellType + ")");
        }
    }
    
    protected abstract RichTextString createRichTextString(String str);
    
    protected void setCellValue(Cell cell, CellValue cv) {
        CellType cellType = cv.getCellTypeEnum();
        switch (cellType) {
            case BOOLEAN:
                cell.setCellValue(cv.getBooleanValue());
                break;
            case ERROR:
                cell.setCellErrorValue(cv.getErrorValue());
                break;
            case NUMERIC:
                cell.setCellValue(cv.getNumberValue());
                break;
            case STRING:
                cell.setCellValue(createRichTextString(cv.getStringValue()));
                break;
            case BLANK:
                // never happens - blanks eventually get translated to zero
            case FORMULA:
                // this will never happen, we have already evaluated the formula
            default:
                throw new IllegalStateException("Unexpected cell value type (" + cellType + ")");
        }
    }
    

    /**
     * Loops over all cells in all sheets of the supplied
     *  workbook.
     * For cells that contain formulas, their formulas are
     *  evaluated, and the results are saved. These cells
     *  remain as formula cells.
     * For cells that do not contain formulas, no changes
     *  are made.
     * This is a helpful wrapper around looping over all
     *  cells, and calling evaluateFormulaCell on each one.
     */
    public static void evaluateAllFormulaCells(Workbook wb) {
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        evaluateAllFormulaCells(wb, evaluator);
    }
    protected static void evaluateAllFormulaCells(Workbook wb, FormulaEvaluator evaluator) {
        for(int i=0; i<wb.getNumberOfSheets(); i++) {
            Sheet sheet = wb.getSheetAt(i);

            for(Row r : sheet) {
                for (Cell c : r) {
                    if (c.getCellTypeEnum() == CellType.FORMULA) {
                        evaluator.evaluateFormulaCellEnum(c);
                    }
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setIgnoreMissingWorkbooks(boolean ignore){
        _bookEvaluator.setIgnoreMissingWorkbooks(ignore);
    }

    /** {@inheritDoc} */
    @Override
    public void setDebugEvaluationOutputForNextEval(boolean value){
        _bookEvaluator.setDebugEvaluationOutputForNextEval(value);
    }
}
