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

import org.apache.poi.util.Removal;

import java.util.Map;

/**
 * Evaluates formula cells.<p>
 * 
 * For performance reasons, this class keeps a cache of all previously calculated intermediate
 * cell values.  Be sure to call {@link #clearAllCachedResultValues()} if any workbook cells are changed between
 * calls to evaluate~ methods on this class.
 * 
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * @author Josh Micich
 */
public interface FormulaEvaluator {

    /**
     * Should be called whenever there are changes to input cells in the evaluated workbook.
     * Failure to call this method after changing cell values will cause incorrect behaviour
     * of the evaluate~ methods of this class
     */
    void clearAllCachedResultValues();
	/**
	 * Should be called to tell the cell value cache that the specified (value or formula) cell 
	 * has changed.
	 * Failure to call this method after changing cell values will cause incorrect behaviour
	 * of the evaluate~ methods of this class
	 */
    void notifySetFormula(Cell cell);
	/**
	 * Should be called to tell the cell value cache that the specified cell has just become a
	 * formula cell, or the formula text has changed 
	 */
    void notifyDeleteCell(Cell cell);

    /**
     * Should be called to tell the cell value cache that the specified (value or formula) cell
     * has changed.
     * Failure to call this method after changing cell values will cause incorrect behaviour
     * of the evaluate~ methods of this class
     */
    void notifyUpdateCell(Cell cell);

    /**
    * Loops over all cells in all sheets of the associated workbook.
    * For cells that contain formulas, their formulas are evaluated, 
    *  and the results are saved. These cells remain as formula cells.
    * For cells that do not contain formulas, no changes are made.
    * This is a helpful wrapper around looping over all cells, and 
    *  calling evaluateFormulaCell on each one.
     */
    void evaluateAll();
    
    /**
     * If cell contains a formula, the formula is evaluated and returned,
     * else the CellValue simply copies the appropriate cell value from
     * the cell and also its cell type. This method should be preferred over
     * evaluateInCell() when the call should not modify the contents of the
     * original cell.
     * @param cell The {@link Cell} to evaluate
     */
    CellValue evaluate(Cell cell);


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
     *  the result of the formula, use {@link #evaluateInCell(Cell)}
     * @param cell The cell to evaluate
     * @return The type of the formula result, i.e. -1 if the cell is not a formula, 
     *      or one of {@link CellType#NUMERIC}, {@link CellType#STRING},
     *      {@link CellType#BOOLEAN}, {@link CellType#ERROR}
     *      Note: the cell's type remains as CellType.FORMULA however.
     */
    CellType evaluateFormulaCell(Cell cell);
    
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
     * CellType evaluatedCellType = evaluator.evaluateFormulaCell(cell);
     * </pre>
     * Be aware that your cell will hold both the formula,
     *  and the result. If you want the cell replaced with
     *  the result of the formula, use {@link #evaluateInCell(Cell)}
     * @param cell The cell to evaluate
     * @return The type of the formula result, i.e. -1 if the cell is not a formula, 
     *      or one of {@link CellType#NUMERIC}, {@link CellType#STRING},
     *      {@link CellType#BOOLEAN}, {@link CellType#ERROR}
     *      Note: the cell's type remains as CellType.FORMULA however.
     * @deprecated use <code>evaluateFormulaCell(cell)</code>
     */
    @Deprecated
    @Removal(version = "4.2")
    CellType evaluateFormulaCellEnum(Cell cell);

    /**
     * If cell contains formula, it evaluates the formula, and
     *  puts the formula result back into the cell, in place
     *  of the old formula.
     * Else if cell does not contain formula, this method leaves
     *  the cell unchanged.
     * Note that the same instance of Cell is returned to
     * allow chained calls like:
     * <pre>
     * int evaluatedCellType = evaluator.evaluateInCell(cell).getCellType();
     * </pre>
     * Be aware that your cell value will be changed to hold the
     *  result of the formula. If you simply want the formula
     *  value computed for you, use {@link #evaluateFormulaCell(Cell)}
     * @param cell The {@link Cell} to evaluate and modify.
     */
    Cell evaluateInCell(Cell cell);
    
    /**
     * Sets up the Formula Evaluator to be able to reference and resolve
     *  links to other workbooks, eg [Test.xls]Sheet1!A1.
     * <p>For a workbook referenced as [Test.xls]Sheet1!A1, you should
     *  supply a map containing the key Test.xls (no square brackets),
     *  and an open FormulaEvaluator onto that Workbook.
     * @param workbooks Map of workbook names (no square brackets) to an evaluator on that workbook
     */
    void setupReferencedWorkbooks(Map<String,FormulaEvaluator> workbooks);

    /**
     * Whether to ignore missing references to external workbooks and
     * use cached formula results in the main workbook instead.
     * <p>
     * In some cases external workbooks referenced by formulas in the main workbook are not available.
     * With this method you can control how POI handles such missing references:
     * <ul>
     *     <li>by default ignoreMissingWorkbooks=false and POI throws 
     *     {@link org.apache.poi.ss.formula.CollaboratingWorkbooksEnvironment.WorkbookNotFoundException}
     *     if an external reference cannot be resolved</li>
     *     <li>if ignoreMissingWorkbooks=true then POI uses cached formula result
     *     that already exists in the main workbook</li>
     * </ul>
     *
     * @param ignore whether to ignore missing references to external workbooks
     */
    void setIgnoreMissingWorkbooks(boolean ignore);
    
    /**
     * Perform detailed output of formula evaluation for next evaluation only?
     * Is for developer use only (also developers using POI for their XLS files).
     * Log-Level WARN is for basic info, INFO for detailed information. These quite
     * high levels are used because you have to explicitly enable this specific logging.
	 
     * @param value whether to perform detailed output
     */
    void setDebugEvaluationOutputForNextEval(boolean value);
}
