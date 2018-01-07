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

package org.apache.poi.ss.formula.eval.forked;

import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.udf.UDFFinder;

import java.lang.reflect.Method;

import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.CollaboratingWorkbooksEnvironment;
import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.formula.IStabilityClassifier;
import org.apache.poi.ss.formula.WorkbookEvaluator;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * An alternative workbook evaluator that saves memory in situations where a single workbook is
 * concurrently and independently evaluated many times.  With standard formula evaluation, around
 * 90% of memory consumption is due to loading of the {@link HSSFWorkbook} or {@link org.apache.poi.xssf.usermodel.XSSFWorkbook}.
 * This class enables a 'master workbook' to be loaded just once and shared between many evaluation
 * clients.  Each evaluation client creates its own {@link ForkedEvaluator} and can set cell values
 * that will be used for local evaluations (and don't disturb evaluations on other evaluators).
 */
public final class ForkedEvaluator {

	private WorkbookEvaluator _evaluator;
	private ForkedEvaluationWorkbook _sewb;

	private ForkedEvaluator(EvaluationWorkbook masterWorkbook, IStabilityClassifier stabilityClassifier, UDFFinder udfFinder) {
		_sewb = new ForkedEvaluationWorkbook(masterWorkbook);
		_evaluator = new WorkbookEvaluator(_sewb, stabilityClassifier, udfFinder);
	}
	private static EvaluationWorkbook createEvaluationWorkbook(Workbook wb) {
		if (wb instanceof HSSFWorkbook) {
			return HSSFEvaluationWorkbook.create((HSSFWorkbook) wb);
		} else {
		    try {
		        // TODO: check if this is Java 9 compatible ...
		        Class<?> evalWB = Class.forName("org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook");
		        Class<?> xssfWB = Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook");
		        Method createM = evalWB.getDeclaredMethod("create", xssfWB);
		        return (EvaluationWorkbook)createM.invoke(null, wb);
		    } catch (Exception e) {
		        throw new IllegalArgumentException("Unexpected workbook type (" + wb.getClass().getName() + ") - check for poi-ooxml and poi-ooxml schemas jar in the classpath", e);
		    }
		}
	}

	/**
	 * @param udfFinder pass <code>null</code> for default (AnalysisToolPak only)
	 */
	public static ForkedEvaluator create(Workbook wb, IStabilityClassifier stabilityClassifier, UDFFinder udfFinder) {
		return new ForkedEvaluator(createEvaluationWorkbook(wb), stabilityClassifier, udfFinder);
	}

	/**
	 * Sets the specified cell to the supplied <tt>value</tt>
	 * @param sheetName the name of the sheet containing the cell
	 * @param rowIndex zero based
	 * @param columnIndex zero based
	 */
	public void updateCell(String sheetName, int rowIndex, int columnIndex, ValueEval value) {

		ForkedEvaluationCell cell = _sewb.getOrCreateUpdatableCell(sheetName, rowIndex, columnIndex);
		cell.setValue(value);
		_evaluator.notifyUpdateCell(cell);
	}
	/**
	 * Copies the values of all updated cells (modified by calls to {@link
	 * #updateCell(String, int, int, ValueEval)}) to the supplied <tt>workbook</tt>.<br>
	 * Typically, the supplied <tt>workbook</tt> is a writable copy of the 'master workbook',
	 * but at the very least it must contain sheets with the same names.
	 */
	public void copyUpdatedCells(Workbook workbook) {
		_sewb.copyUpdatedCells(workbook);
	}

	/**
	 * If cell contains a formula, the formula is evaluated and returned,
	 * else the CellValue simply copies the appropriate cell value from
	 * the cell and also its cell type. This method should be preferred over
	 * evaluateInCell() when the call should not modify the contents of the
	 * original cell.
	 *
     * @param sheetName the name of the sheet containing the cell
     * @param rowIndex zero based
     * @param columnIndex zero based
	 * @return <code>null</code> if the supplied cell is <code>null</code> or blank
	 */
	public ValueEval evaluate(String sheetName, int rowIndex, int columnIndex) {
		EvaluationCell cell = _sewb.getEvaluationCell(sheetName, rowIndex, columnIndex);

		switch (cell.getCellType()) {
			case BOOLEAN:
				return BoolEval.valueOf(cell.getBooleanCellValue());
			case ERROR:
				return ErrorEval.valueOf(cell.getErrorCellValue());
			case FORMULA:
				return _evaluator.evaluate(cell);
			case NUMERIC:
				return new NumberEval(cell.getNumericCellValue());
			case STRING:
				return new StringEval(cell.getStringCellValue());
			case BLANK:
				return null;
			default:
				throw new IllegalStateException("Bad cell type (" + cell.getCellType() + ")");
		}
	}
	/**
	 * Coordinates several formula evaluators together so that formulas that involve external
	 * references can be evaluated.
	 * @param workbookNames the simple file names used to identify the workbooks in formulas
	 * with external links (for example "MyData.xls" as used in a formula "[MyData.xls]Sheet1!A1")
	 * @param evaluators all evaluators for the full set of workbooks required by the formulas.
	 */
	public static void setupEnvironment(String[] workbookNames, ForkedEvaluator[] evaluators) {
		WorkbookEvaluator[] wbEvals = new WorkbookEvaluator[evaluators.length];
		for (int i = 0; i < wbEvals.length; i++) {
			wbEvals[i] = evaluators[i]._evaluator;
		}
		CollaboratingWorkbooksEnvironment.setup(workbookNames, wbEvals);
	}
}
