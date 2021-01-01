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

import org.apache.poi.ss.formula.BaseFormulaEvaluator;
import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.IStabilityClassifier;
import org.apache.poi.ss.formula.WorkbookEvaluator;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.Cell;

/**
 * Evaluates formula cells.<p>
 *
 * For performance reasons, this class keeps a cache of all previously calculated intermediate
 * cell values.  Be sure to call {@link #clearAllCachedResultValues()} if any workbook cells are changed between
 * calls to evaluate~ methods on this class.
 */
public final class XSSFFormulaEvaluator extends BaseXSSFFormulaEvaluator {
    private final XSSFWorkbook _book;

    public XSSFFormulaEvaluator(XSSFWorkbook workbook) {
        this(workbook, null, null);
    }
    private XSSFFormulaEvaluator(XSSFWorkbook workbook, IStabilityClassifier stabilityClassifier, UDFFinder udfFinder) {
        this(workbook, new WorkbookEvaluator(XSSFEvaluationWorkbook.create(workbook), stabilityClassifier, udfFinder));
    }
    protected XSSFFormulaEvaluator(XSSFWorkbook workbook, WorkbookEvaluator bookEvaluator) {
        super(bookEvaluator);
        _book = workbook;
    }

    /**
     * @param stabilityClassifier used to optimise caching performance. Pass <code>null</code>
     * for the (conservative) assumption that any cell may have its definition changed after
     * evaluation begins.
     * @param udfFinder pass <code>null</code> for default (AnalysisToolPak only)
     */
    public static XSSFFormulaEvaluator create(XSSFWorkbook workbook, IStabilityClassifier stabilityClassifier, UDFFinder udfFinder) {
        return new XSSFFormulaEvaluator(workbook, stabilityClassifier, udfFinder);
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
    public static void evaluateAllFormulaCells(XSSFWorkbook wb) {
        BaseFormulaEvaluator.evaluateAllFormulaCells(wb);
    }

    @Override
    public XSSFCell evaluateInCell(Cell cell) {
        return (XSSFCell) super.evaluateInCell(cell);
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
    public void evaluateAll() {
        evaluateAllFormulaCells(_book, this);
    }

    /**
     * Turns a XSSFCell into a XSSFEvaluationCell
     */
    protected EvaluationCell toEvaluationCell(Cell cell) {
        if (!(cell instanceof XSSFCell)){
            throw new IllegalArgumentException("Unexpected type of cell: " + cell.getClass() + "." +
                    " Only XSSFCells can be evaluated.");
        }

        return new XSSFEvaluationCell((XSSFCell)cell);
    }
}
