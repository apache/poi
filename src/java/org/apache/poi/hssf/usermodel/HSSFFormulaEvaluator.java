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

package org.apache.poi.hssf.usermodel;

import java.util.Map;

import org.apache.poi.ss.formula.BaseFormulaEvaluator;
import org.apache.poi.ss.formula.CollaboratingWorkbooksEnvironment;
import org.apache.poi.ss.formula.IStabilityClassifier;
import org.apache.poi.ss.formula.WorkbookEvaluator;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumericValueEval;
import org.apache.poi.ss.formula.eval.StringValueEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Evaluates formula cells.<p>
 *
 * For performance reasons, this class keeps a cache of all previously calculated intermediate
 * cell values.  Be sure to call {@link #clearAllCachedResultValues()} if any workbook cells are changed between
 * calls to evaluate~ methods on this class.
 */
public class HSSFFormulaEvaluator extends BaseFormulaEvaluator {
    private final HSSFWorkbook _book;

    public HSSFFormulaEvaluator(HSSFWorkbook workbook) {
        this(workbook, null);
    }
    /**
     * @param workbook  The workbook to perform the formula evaluations in
     * @param stabilityClassifier used to optimise caching performance. Pass <code>null</code>
     * for the (conservative) assumption that any cell may have its definition changed after
     * evaluation begins.
     */
    public HSSFFormulaEvaluator(HSSFWorkbook workbook, IStabilityClassifier stabilityClassifier) {
        this(workbook, stabilityClassifier, null);
    }

    /**
     * @param workbook  The workbook to perform the formula evaluations in
     * @param stabilityClassifier used to optimise caching performance. Pass <code>null</code>
     * for the (conservative) assumption that any cell may have its definition changed after
     * evaluation begins.
     * @param udfFinder pass <code>null</code> for default (AnalysisToolPak only)
     */
    private HSSFFormulaEvaluator(HSSFWorkbook workbook, IStabilityClassifier stabilityClassifier, UDFFinder udfFinder) {
        super(new WorkbookEvaluator(HSSFEvaluationWorkbook.create(workbook), stabilityClassifier, udfFinder));
        _book = workbook;
    }

    /**
     * @param workbook  The workbook to perform the formula evaluations in
     * @param stabilityClassifier used to optimise caching performance. Pass <code>null</code>
     * for the (conservative) assumption that any cell may have its definition changed after
     * evaluation begins.
     * @param udfFinder pass <code>null</code> for default (AnalysisToolPak only)
     */
    public static HSSFFormulaEvaluator create(HSSFWorkbook workbook, IStabilityClassifier stabilityClassifier, UDFFinder udfFinder) {
        return new HSSFFormulaEvaluator(workbook, stabilityClassifier, udfFinder);
    }
    
    @Override
    protected RichTextString createRichTextString(String str) {
        return new HSSFRichTextString(str);
    }


    /**
     * Coordinates several formula evaluators together so that formulas that involve external
     * references can be evaluated.
     * @param workbookNames the simple file names used to identify the workbooks in formulas
     * with external links (for example "MyData.xls" as used in a formula "[MyData.xls]Sheet1!A1")
     * @param evaluators all evaluators for the full set of workbooks required by the formulas.
     */
    public static void setupEnvironment(String[] workbookNames, HSSFFormulaEvaluator[] evaluators) {
        BaseFormulaEvaluator.setupEnvironment(workbookNames, evaluators);
    }

    @Override
    public void setupReferencedWorkbooks(Map<String, FormulaEvaluator> evaluators) {
        CollaboratingWorkbooksEnvironment.setupFormulaEvaluator(evaluators);
    }

    /**
     * Should be called to tell the cell value cache that the specified (value or formula) cell
     * has changed.
     * Failure to call this method after changing cell values will cause incorrect behaviour
     * of the evaluate~ methods of this class
     */
    public void notifyUpdateCell(HSSFCell cell) {
        _bookEvaluator.notifyUpdateCell(new HSSFEvaluationCell(cell));
    }
    @Override
    public void notifyUpdateCell(Cell cell) {
        _bookEvaluator.notifyUpdateCell(new HSSFEvaluationCell((HSSFCell)cell));
    }
    /**
     * Should be called to tell the cell value cache that the specified cell has just been
     * deleted.
     * Failure to call this method after changing cell values will cause incorrect behaviour
     * of the evaluate~ methods of this class
     */
    public void notifyDeleteCell(HSSFCell cell) {
        _bookEvaluator.notifyDeleteCell(new HSSFEvaluationCell(cell));
    }
    @Override
    public void notifyDeleteCell(Cell cell) {
        _bookEvaluator.notifyDeleteCell(new HSSFEvaluationCell((HSSFCell)cell));
    }

    /**
     * Should be called to tell the cell value cache that the specified (value or formula) cell
     * has changed.
     * Failure to call this method after changing cell values will cause incorrect behaviour
     * of the evaluate~ methods of this class
     */
    @Override
    public void notifySetFormula(Cell cell) {
        _bookEvaluator.notifyUpdateCell(new HSSFEvaluationCell((HSSFCell)cell));
    }
    
    @Override
    public HSSFCell evaluateInCell(Cell cell) {
        return (HSSFCell) super.evaluateInCell(cell);
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
    public static void evaluateAllFormulaCells(HSSFWorkbook wb) {
        evaluateAllFormulaCells(wb, new HSSFFormulaEvaluator(wb));
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
        BaseFormulaEvaluator.evaluateAllFormulaCells(wb);
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
    @Override
    public void evaluateAll() {
        evaluateAllFormulaCells(_book, this);
    }

    /**
     * Returns a CellValue wrapper around the supplied ValueEval instance.
     * @param cell The cell with the formula
     */
    protected CellValue evaluateFormulaCellValue(Cell cell) {
        ValueEval eval = _bookEvaluator.evaluate(new HSSFEvaluationCell((HSSFCell)cell));
        if (eval instanceof BoolEval) {
            BoolEval be = (BoolEval) eval;
            return CellValue.valueOf(be.getBooleanValue());
        }
        if (eval instanceof NumericValueEval) {
            NumericValueEval ne = (NumericValueEval) eval;
            return new CellValue(ne.getNumberValue());
        }
        if (eval instanceof StringValueEval) {
            StringValueEval ne = (StringValueEval) eval;
            return new CellValue(ne.getStringValue());
        }
        if (eval instanceof ErrorEval) {
            return CellValue.getError(((ErrorEval)eval).getErrorCode());
        }
        throw new RuntimeException("Unexpected eval class (" + eval.getClass().getName() + ")");
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
