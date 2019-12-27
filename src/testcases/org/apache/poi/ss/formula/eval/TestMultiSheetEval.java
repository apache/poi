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

package org.apache.poi.ss.formula.eval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.TestMathX;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

/**
 * Tests formulas for multi sheet reference (i.e. SUM(Sheet1:Sheet5!A1))
 */
@RunWith(Parameterized.class)
public final class TestMultiSheetEval {
	/**
	 * This class defines constants for navigating around the test data spreadsheet used for these tests.
	 */
	private static final class SS {

		/**
		 * Name of the test spreadsheet (found in the standard test data folder)
		 */
		public final static String FILENAME = "FormulaSheetRange.xls";
		/**
		 * Row (zero-based) in the test spreadsheet where the function examples start.
		 */
		public static final int START_FUNCTIONS_ROW_INDEX = 10; // Row '11'
		/**
		 * Index of the column that contains the function names
		 */
		public static final int COLUMN_INDEX_FUNCTION_NAME = 0; // Column 'A'
		/**
		 * Index of the column that contains the test names
		 */
		public static final int COLUMN_INDEX_TEST_NAME = 1; // Column 'B'
		/**
		 * Used to indicate when there are no more functions left
		 */
		public static final String FUNCTION_NAMES_END_SENTINEL = "<END>";

		/**
		 * Index of the column where the test expected value is present
		 */
		public static final short COLUMN_INDEX_EXPECTED_VALUE = 2; // Column 'C'
		/**
		 * Index of the column where the test actual value is present
		 */
		public static final short COLUMN_INDEX_ACTUAL_VALUE = 3; // Column 'D'
		/**
		 * Test sheet name (sheet with all test formulae)
		 */
		public static final String TEST_SHEET_NAME = "test";
	}

	private static HSSFFormulaEvaluator evaluator;
	private static Collection<String> funcs;

	@SuppressWarnings("DefaultAnnotationParam")
	@Parameter(value = 0)
	public String testName;

	@Parameter(value = 1)
	public String functionName;

	@Parameter(value = 2)
	public Cell expected;

	@Parameter(value = 3)
	public Row testRow;

	@Parameterized.Parameters(name="{0}")
	public static Collection<Object[]> data() {
		HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook(SS.FILENAME);
		Sheet sheet = workbook.getSheet(SS.TEST_SHEET_NAME);
		evaluator = new HSSFFormulaEvaluator(workbook);
		funcs = FunctionEval.getSupportedFunctionNames();

		List<Object[]> data = new ArrayList<>();
		for (int rowIndex = SS.START_FUNCTIONS_ROW_INDEX;true;rowIndex++) {
			Row r = sheet.getRow(rowIndex);

			// only evaluate non empty row
			if (r == null) {
				continue;
			}

			String targetFunctionName = getTargetFunctionName(r);
			assertNotNull("Expected function name or '" + SS.FUNCTION_NAMES_END_SENTINEL + "'", targetFunctionName);
			if (targetFunctionName.equals(SS.FUNCTION_NAMES_END_SENTINEL)) {
				// found end of functions list
				break;
			}
			String targetTestName = getTargetTestName(r);

			// expected results are on the row below
			Cell expectedValueCell = r.getCell(SS.COLUMN_INDEX_EXPECTED_VALUE);
			assertNotNull("Missing expected values cell for function '" + targetFunctionName, expectedValueCell);

			data.add(new Object[]{ targetTestName, targetFunctionName, expectedValueCell, r });
		}

		return data;
	}

	@Test
	public void testFunction() {

		Cell c = testRow.getCell(SS.COLUMN_INDEX_ACTUAL_VALUE);
		if (c == null || c.getCellType() != CellType.FORMULA) {
			// missing test data
			assertTrue(testRow.getRowNum() >= SS.START_FUNCTIONS_ROW_INDEX);
			assertTrue("unsupported function", funcs.contains(functionName.toUpperCase(Locale.ROOT)));
			return;
		}

		CellValue actual = evaluator.evaluate(c);

		assertNotNull("Bad setup data expected value is null", expected);
		assertNotNull("actual value was null", actual);

		final CellType cellType = expected.getCellType();

		switch (cellType) {
			case BLANK:
				assertEquals(CellType.BLANK, actual.getCellType());
				break;
			case BOOLEAN:
				assertEquals(CellType.BOOLEAN, actual.getCellType());
				assertEquals(expected.getBooleanCellValue(), actual.getBooleanValue());
				break;
			case ERROR:
				assertEquals(CellType.ERROR, actual.getCellType());
				assertEquals(ErrorEval.getText(expected.getErrorCellValue()), ErrorEval.getText(actual.getErrorValue()));
				break;
			case FORMULA: // will never be used, since we will call method after formula evaluation
				fail("Cannot expect formula as result of formula evaluation.");
				break;
			case NUMERIC:
				assertEquals(CellType.NUMERIC, actual.getCellType());
				TestMathX.assertEquals("", expected.getNumericCellValue(), actual.getNumberValue(), TestMathX.POS_ZERO, TestMathX.DIFF_TOLERANCE_FACTOR);
				break;
			case STRING:
				assertEquals(CellType.STRING, actual.getCellType());
				assertEquals(expected.getRichStringCellValue().getString(), actual.getStringValue());
				break;
			default:
				fail("Unexpected cell type: " + cellType);
				break;
		}
	}

	private static String getTargetFunctionName(Row r) {
		assertNotNull("given null row, can't figure out function name", r);
		Cell cell = r.getCell(SS.COLUMN_INDEX_FUNCTION_NAME);
		assertNotNull("Row " + r.getRowNum() + " has no cell " + SS.COLUMN_INDEX_FUNCTION_NAME + ", can't figure out function name", cell);
		assertEquals(CellType.STRING, cell.getCellType());
		return cell.getRichStringCellValue().getString();
	}

	private static String getTargetTestName(Row r) {
		assertNotNull("Given null row, can't figure out test name", r);
		Cell cell = r.getCell(SS.COLUMN_INDEX_TEST_NAME);
		assertNotNull("Row " + r.getRowNum() + " has no cell " + SS.COLUMN_INDEX_TEST_NAME + ", can't figure out test name", cell);
		assertEquals(CellType.STRING, cell.getCellType());
		return cell.getRichStringCellValue().getString();
	}
}
