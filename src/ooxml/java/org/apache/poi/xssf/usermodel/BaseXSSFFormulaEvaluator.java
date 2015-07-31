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

import java.util.Map;

import org.apache.poi.ss.formula.CollaboratingWorkbooksEnvironment;
import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.WorkbookEvaluator;
import org.apache.poi.ss.formula.WorkbookEvaluatorProvider;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

/**
 * Internal POI use only - parent of XSSF and SXSSF formula evaluators
 */
public abstract class BaseXSSFFormulaEvaluator implements FormulaEvaluator, WorkbookEvaluatorProvider {
    private WorkbookEvaluator _bookEvaluator;

    protected BaseXSSFFormulaEvaluator(WorkbookEvaluator bookEvaluator) {
        _bookEvaluator = bookEvaluator;
    }

    /**
     * Should be called whenever there are major changes (e.g. moving sheets) to input cells
     * in the evaluated workbook.
     * Failure to call this method after changing cell values will cause incorrect behaviour
     * of the evaluate~ methods of this class
     */
    public void clearAllCachedResultValues() {
        _bookEvaluator.clearAllCachedResultValues();
    }
    public void notifySetFormula(Cell cell) {
        _bookEvaluator.notifyUpdateCell(new XSSFEvaluationCell((XSSFCell)cell));
    }
    public void notifyDeleteCell(Cell cell) {
        _bookEvaluator.notifyDeleteCell(new XSSFEvaluationCell((XSSFCell)cell));
    }
    public void notifyUpdateCell(Cell cell) {
        _bookEvaluator.notifyUpdateCell(new XSSFEvaluationCell((XSSFCell)cell));
    }

    /**
     * If cell contains a formula, the formula is evaluated and returned,
     * else the CellValue simply copies the appropriate cell value from
     * the cell and also its cell type. This method should be preferred over
     * evaluateInCell() when the call should not modify the contents of the
     * original cell.
     * @param cell
     */
    public CellValue evaluate(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case XSSFCell.CELL_TYPE_BOOLEAN:
                return CellValue.valueOf(cell.getBooleanCellValue());
            case XSSFCell.CELL_TYPE_ERROR:
                return CellValue.getError(cell.getErrorCellValue());
            case XSSFCell.CELL_TYPE_FORMULA:
                return evaluateFormulaCellValue(cell);
            case XSSFCell.CELL_TYPE_NUMERIC:
                return new CellValue(cell.getNumericCellValue());
            case XSSFCell.CELL_TYPE_STRING:
                return new CellValue(cell.getRichStringCellValue().getString());
            case XSSFCell.CELL_TYPE_BLANK:
                return null;
        }
        throw new IllegalStateException("Bad cell type (" + cell.getCellType() + ")");
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
     * int evaluatedCellType = evaluator.evaluateFormulaCell(cell);
     * </pre>
     * Be aware that your cell will hold both the formula,
     *  and the result. If you want the cell replaced with
     *  the result of the formula, use {@link #evaluate(org.apache.poi.ss.usermodel.Cell)} }
     * @param cell The cell to evaluate
     * @return The type of the formula result (the cell's type remains as HSSFCell.CELL_TYPE_FORMULA however)
     */
    public int evaluateFormulaCell(Cell cell) {
        if (cell == null || cell.getCellType() != XSSFCell.CELL_TYPE_FORMULA) {
            return -1;
        }
        CellValue cv = evaluateFormulaCellValue(cell);
        // cell remains a formula cell, but the cached value is changed
        setCellValue(cell, cv);
        return cv.getCellType();
    }

    /**
     * If cell contains formula, it evaluates the formula, and
     *  puts the formula result back into the cell, in place
     *  of the old formula.
     * Else if cell does not contain formula, this method leaves
     *  the cell unchanged.
     */
    protected void doEvaluateInCell(Cell cell) {
        if (cell == null) return;
        if (cell.getCellType() == XSSFCell.CELL_TYPE_FORMULA) {
            CellValue cv = evaluateFormulaCellValue(cell);
            setCellType(cell, cv); // cell will no longer be a formula cell
            setCellValue(cell, cv);
        }
    }
    private static void setCellType(Cell cell, CellValue cv) {
        int cellType = cv.getCellType();
        switch (cellType) {
            case XSSFCell.CELL_TYPE_BOOLEAN:
            case XSSFCell.CELL_TYPE_ERROR:
            case XSSFCell.CELL_TYPE_NUMERIC:
            case XSSFCell.CELL_TYPE_STRING:
                cell.setCellType(cellType);
                return;
            case XSSFCell.CELL_TYPE_BLANK:
                // never happens - blanks eventually get translated to zero
            case XSSFCell.CELL_TYPE_FORMULA:
                // this will never happen, we have already evaluated the formula
        }
        throw new IllegalStateException("Unexpected cell value type (" + cellType + ")");
    }

    private static void setCellValue(Cell cell, CellValue cv) {
        int cellType = cv.getCellType();
        switch (cellType) {
            case XSSFCell.CELL_TYPE_BOOLEAN:
                cell.setCellValue(cv.getBooleanValue());
                break;
            case XSSFCell.CELL_TYPE_ERROR:
                cell.setCellErrorValue(cv.getErrorValue());
                break;
            case XSSFCell.CELL_TYPE_NUMERIC:
                cell.setCellValue(cv.getNumberValue());
                break;
            case XSSFCell.CELL_TYPE_STRING:
                cell.setCellValue(new XSSFRichTextString(cv.getStringValue()));
                break;
            case XSSFCell.CELL_TYPE_BLANK:
                // never happens - blanks eventually get translated to zero
            case XSSFCell.CELL_TYPE_FORMULA:
                // this will never happen, we have already evaluated the formula
            default:
                throw new IllegalStateException("Unexpected cell value type (" + cellType + ")");
        }
    }

    /**
     * Turns a XSSFCell / SXSSFCell into a XSSFEvaluationCell
     */
    protected abstract EvaluationCell toEvaluationCell(Cell cell);
    
    /**
     * Returns a CellValue wrapper around the supplied ValueEval instance.
     */
    private CellValue evaluateFormulaCellValue(Cell cell) {
        EvaluationCell evalCell = toEvaluationCell(cell);
        ValueEval eval = _bookEvaluator.evaluate(evalCell);
        if (eval instanceof NumberEval) {
            NumberEval ne = (NumberEval) eval;
            return new CellValue(ne.getNumberValue());
        }
        if (eval instanceof BoolEval) {
            BoolEval be = (BoolEval) eval;
            return CellValue.valueOf(be.getBooleanValue());
        }
        if (eval instanceof StringEval) {
            StringEval ne = (StringEval) eval;
            return new CellValue(ne.getStringValue());
        }
        if (eval instanceof ErrorEval) {
            return CellValue.getError(((ErrorEval)eval).getErrorCode());
        }
        throw new RuntimeException("Unexpected eval class (" + eval.getClass().getName() + ")");
    }

    public void setupReferencedWorkbooks(Map<String, FormulaEvaluator> evaluators) {
        CollaboratingWorkbooksEnvironment.setupFormulaEvaluator(evaluators);
    }

    public WorkbookEvaluator _getWorkbookEvaluator() {
        return _bookEvaluator;
    }

    /** {@inheritDoc} */
    public void setIgnoreMissingWorkbooks(boolean ignore){
        _bookEvaluator.setIgnoreMissingWorkbooks(ignore);
    }

    /** {@inheritDoc} */
    public void setDebugEvaluationOutputForNextEval(boolean value){
        _bookEvaluator.setDebugEvaluationOutputForNextEval(value);
    }
}
