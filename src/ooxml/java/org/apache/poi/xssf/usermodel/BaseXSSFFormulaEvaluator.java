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
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.util.Internal;

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
     * @return The type of the formula result (the cell's type remains as CellType.FORMULA however)
     */
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
     * int evaluatedCellType = evaluator.evaluateFormulaCell(cell);
     * </pre>
     * Be aware that your cell will hold both the formula,
     *  and the result. If you want the cell replaced with
     *  the result of the formula, use {@link #evaluate(org.apache.poi.ss.usermodel.Cell)} }
     * @param cell The cell to evaluate
     * @return The type of the formula result (the cell's type remains as CellType.FORMULA however)
     * @deprecated POI 3.15 beta 3. Will be deleted when we make the CellType enum transition. See bug 59791.
     */
    @Internal
    public CellType evaluateFormulaCellEnum(Cell cell) {
        if (cell == null || cell.getCellTypeEnum() != CellType.FORMULA) {
            return CellType._UNINITIALIZED;
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
        if (cell.getCellTypeEnum() == CellType.FORMULA) {
            CellValue cv = evaluateFormulaCellValue(cell);
            setCellType(cell, cv); // cell will no longer be a formula cell
            setCellValue(cell, cv);
        }
    }
    private static void setCellType(Cell cell, CellValue cv) {
        CellType cellType = cv.getCellType();
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

    private static void setCellValue(Cell cell, CellValue cv) {
        CellType cellType = cv.getCellType();
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
                cell.setCellValue(new XSSFRichTextString(cv.getStringValue()));
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
