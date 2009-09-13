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

package org.apache.poi.hssf.record.formula.eval;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
/**
 * Tests HSSFFormulaEvaluator for its handling of cell formula circular references.
 *
 * @author Josh Micich
 */
public final class TestCircularReferences extends TestCase {
	/**
	 * Translates StackOverflowError into AssertionFailedError
	 */
	private static CellValue evaluateWithCycles(HSSFWorkbook wb, HSSFCell testCell)
			throws AssertionFailedError {
		HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
		try {
			return evaluator.evaluate(testCell);
		} catch (StackOverflowError e) {
			throw new AssertionFailedError( "circular reference caused stack overflow error");
		}
	}
	/**
	 * Makes sure that the specified evaluated cell value represents a circular reference error.
	 */
	private static void confirmCycleErrorCode(CellValue cellValue) {
		assertTrue(cellValue.getCellType() == HSSFCell.CELL_TYPE_ERROR);
		assertEquals(ErrorEval.CIRCULAR_REF_ERROR.getErrorCode(), cellValue.getErrorValue());
	}


	/**
	 * ASF Bugzilla Bug 44413
	 * "INDEX() formula cannot contain its own location in the data array range"
	 */
	public void testIndexFormula() {

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Sheet1");

		int colB = 1;
		sheet.createRow(0).createCell(colB).setCellValue(1);
		sheet.createRow(1).createCell(colB).setCellValue(2);
		sheet.createRow(2).createCell(colB).setCellValue(3);
		HSSFRow row4 = sheet.createRow(3);
		HSSFCell testCell = row4.createCell(0);
		// This formula should evaluate to the contents of B2,
		testCell.setCellFormula("INDEX(A1:B4,2,2)");
		// However the range A1:B4 also includes the current cell A4.  If the other parameters
		// were 4 and 1, this would represent a circular reference.  Prior to v3.2 POI would
		// 'fully' evaluate ref arguments before invoking operators, which raised the possibility of
		// cycles / StackOverflowErrors.


		CellValue cellValue = evaluateWithCycles(wb, testCell);

		assertTrue(cellValue.getCellType() == HSSFCell.CELL_TYPE_NUMERIC);
		assertEquals(2, cellValue.getNumberValue(), 0);
	}

	/**
	 * Cell A1 has formula "=A1"
	 */
	public void testSimpleCircularReference() {

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Sheet1");

		HSSFRow row = sheet.createRow(0);
		HSSFCell testCell = row.createCell(0);
		testCell.setCellFormula("A1");

		CellValue cellValue = evaluateWithCycles(wb, testCell);

		confirmCycleErrorCode(cellValue);
	}

	/**
	 * A1=B1, B1=C1, C1=D1, D1=A1
	 */
	public void testMultiLevelCircularReference() {

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Sheet1");

		HSSFRow row = sheet.createRow(0);
		row.createCell(0).setCellFormula("B1");
		row.createCell(1).setCellFormula("C1");
		row.createCell(2).setCellFormula("D1");
		HSSFCell testCell = row.createCell(3);
		testCell.setCellFormula("A1");

		CellValue cellValue = evaluateWithCycles(wb, testCell);

		confirmCycleErrorCode(cellValue);
	}

	public void testIntermediateCircularReferenceResults_bug46898() {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Sheet1");

		HSSFRow row = sheet.createRow(0);

		HSSFCell cellA1 = row.createCell(0);
		HSSFCell cellB1 = row.createCell(1);
		HSSFCell cellC1 = row.createCell(2);
		HSSFCell cellD1 = row.createCell(3);
		HSSFCell cellE1 = row.createCell(4);

		cellA1.setCellFormula("IF(FALSE, 1+B1, 42)");
		cellB1.setCellFormula("1+C1");
		cellC1.setCellFormula("1+D1");
		cellD1.setCellFormula("1+E1");
		cellE1.setCellFormula("1+A1");

		HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
		CellValue cv;

		// Happy day flow - evaluate A1 first
		cv = fe.evaluate(cellA1);
		assertEquals(Cell.CELL_TYPE_NUMERIC, cv.getCellType());
		assertEquals(42.0, cv.getNumberValue(), 0.0);
		cv = fe.evaluate(cellB1); // no circ-ref-error because A1 result is cached
		assertEquals(Cell.CELL_TYPE_NUMERIC, cv.getCellType());
		assertEquals(46.0, cv.getNumberValue(), 0.0);

		// Show the bug - evaluate another cell from the loop first
		fe.clearAllCachedResultValues();
		cv = fe.evaluate(cellB1);
		if (cv.getCellType() == ErrorEval.CIRCULAR_REF_ERROR.getErrorCode()) {
			throw new AssertionFailedError("Identified bug 46898");
		}
		assertEquals(Cell.CELL_TYPE_NUMERIC, cv.getCellType());
		assertEquals(46.0, cv.getNumberValue(), 0.0);

		// start evaluation on another cell
		fe.clearAllCachedResultValues();
		cv = fe.evaluate(cellE1);
		assertEquals(Cell.CELL_TYPE_NUMERIC, cv.getCellType());
		assertEquals(43.0, cv.getNumberValue(), 0.0);


	}
}
