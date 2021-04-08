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

import static org.apache.logging.log4j.util.Unbox.box;
import static org.apache.poi.xssf.usermodel.TestMultiSheetFormulaEvaluatorOnXSSF.SS.COLUMN_INDEX_FUNCTION_NAME;
import static org.apache.poi.xssf.usermodel.TestMultiSheetFormulaEvaluatorOnXSSF.SS.COLUMN_INDEX_TEST_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.formula.functions.BaseTestNumeric;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests formulas for multi sheet reference (i.e. SUM(Sheet1:Sheet5!A1))
 */
public final class TestMultiSheetFormulaEvaluatorOnXSSF {
    private static final Logger LOG = LogManager.getLogger(TestMultiSheetFormulaEvaluatorOnXSSF.class);

	private static XSSFWorkbook workbook;
    private static Sheet sheet;
    private static FormulaEvaluator evaluator;

	/**
	 * This class defines constants for navigating around the test data spreadsheet used for these tests.
	 */
	interface SS {

		/**
		 * Name of the test spreadsheet (found in the standard test data folder)
		 */
		String FILENAME = "FormulaSheetRange.xlsx";
		/**
		 * Row (zero-based) in the test spreadsheet where the function examples start.
		 */
		int START_FUNCTIONS_ROW_INDEX = 10; // Row '11'
		/**
		 * Index of the column that contains the function names
		 */
		int COLUMN_INDEX_FUNCTION_NAME = 0; // Column 'A'
		/**
		 * Index of the column that contains the test names
		 */
		int COLUMN_INDEX_TEST_NAME = 1; // Column 'B'
		/**
		 * Used to indicate when there are no more functions left
		 */
		String FUNCTION_NAMES_END_SENTINEL = "<END>";

		/**
		 * Index of the column where the test expected value is present
		 */
		short COLUMN_INDEX_EXPECTED_VALUE = 2; // Column 'C'
		/**
		 * Index of the column where the test actual value is present
		 */
		short COLUMN_INDEX_ACTUAL_VALUE = 3; // Column 'D'
		/**
		 * Test sheet name (sheet with all test formulae)
		 */
		String TEST_SHEET_NAME = "test";
	}


    @AfterAll
    public static void closeResource() throws Exception {
        workbook.close();
    }

    public static Stream<Arguments> data() throws Exception {
        workbook = new XSSFWorkbook( OPCPackage.open(HSSFTestDataSamples.getSampleFile(SS.FILENAME), PackageAccess.READ) );
        sheet = workbook.getSheet( SS.TEST_SHEET_NAME );
        evaluator = new XSSFFormulaEvaluator(workbook);

        List<Arguments> data = new ArrayList<>();

        processFunctionGroup(data, SS.START_FUNCTIONS_ROW_INDEX, null);

        return data.stream();
    }

    /**
     * @param startRowIndex row index in the spreadsheet where the first function/operator is found
     * @param testFocusFunctionName name of a single function/operator to test alone.
     * Typically pass <code>null</code> to test all functions
     */
    private static void processFunctionGroup(List<Arguments> data, int startRowIndex, String testFocusFunctionName) {
        for (int rowIndex = startRowIndex; true; rowIndex++) {
            Row r = sheet.getRow(rowIndex);

            // only evaluate non empty row
            if(r == null) continue;

            String targetFunctionName = getTargetFunctionName(r);
            assertNotNull(targetFunctionName,
				"Test spreadsheet cell empty on row ("
                + (rowIndex+1) + "). Expected function name or '"
                + SS.FUNCTION_NAMES_END_SENTINEL + "'");

            if(targetFunctionName.equals(SS.FUNCTION_NAMES_END_SENTINEL)) {
                // found end of functions list
                break;
            }

            String targetTestName = getTargetTestName(r);
            if(testFocusFunctionName == null || targetFunctionName.equalsIgnoreCase(testFocusFunctionName)) {

                // expected results are on the row below
                Cell expectedValueCell = r.getCell(SS.COLUMN_INDEX_EXPECTED_VALUE);
                assertNotNull(expectedValueCell,
					"Missing expected values cell for function '"
                    + targetFunctionName + ", test" + targetTestName + " (row " +
                    rowIndex + 1 + ")");

                data.add(Arguments.of(targetTestName, targetFunctionName, rowIndex));
            }
        }
    }

    @ParameterizedTest
	@MethodSource("data")
    void processFunctionRow(String targetTestName, String targetFunctionName, int formulasRowIdx) {
        Row r = sheet.getRow(formulasRowIdx);

        Cell expValue = r.getCell(SS.COLUMN_INDEX_EXPECTED_VALUE);
        assertNotNull(expValue,
			"Missing expected values cell for function '"
            + targetFunctionName + ", test" + targetTestName + " (row " +
            formulasRowIdx + 1 + ")");

        Cell c = r.getCell(SS.COLUMN_INDEX_ACTUAL_VALUE);
        assumeTrue(c != null);
        assumeTrue(c.getCellType() == CellType.FORMULA);

        CellValue actValue = evaluator.evaluate(c);

        String msg = String.format(Locale.ROOT, "Function '%s': Test: '%s': Formula: %s @ %d:%d",
            targetFunctionName, targetTestName, c.getCellFormula(), formulasRowIdx, SS.COLUMN_INDEX_ACTUAL_VALUE);

        assertNotNull(actValue, msg + " - actual value was null");

        final CellType expectedCellType = expValue.getCellType();
        switch (expectedCellType) {
            case BLANK:
                assertEquals(CellType.BLANK, actValue.getCellType(), msg);
                break;
            case BOOLEAN:
                assertEquals(CellType.BOOLEAN, actValue.getCellType(), msg);
                assertEquals(expValue.getBooleanCellValue(), actValue.getBooleanValue(), msg);
                break;
            case ERROR:
                assertEquals(CellType.ERROR, actValue.getCellType(), msg);
//              if(false) { // TODO: fix ~45 functions which are currently returning incorrect error values
//                  assertEquals(msg, expected.getErrorCellValue(), actual.getErrorValue());
//              }
                break;
            case FORMULA: // will never be used, since we will call method after formula evaluation
                fail("Cannot expect formula as result of formula evaluation: " + msg);
            case NUMERIC:
                assertEquals(CellType.NUMERIC, actValue.getCellType(), msg);
				BaseTestNumeric.assertDouble(msg, expValue.getNumericCellValue(), actValue.getNumberValue(), BaseTestNumeric.POS_ZERO, BaseTestNumeric.DIFF_TOLERANCE_FACTOR);
//              double delta = Math.abs(expected.getNumericCellValue()-actual.getNumberValue());
//              double pctExpected = Math.abs(0.00001*expected.getNumericCellValue());
//              assertTrue(msg, delta <= pctExpected);
                break;
            case STRING:
                assertEquals(CellType.STRING, actValue.getCellType(), msg);
                assertEquals(expValue.getRichStringCellValue().getString(), actValue.getStringValue(), msg);
                break;
            default:
                fail("Unexpected cell type: " + expectedCellType);
        }
    }

    /**
	 * @return <code>null</code> if cell is missing, empty or blank
	 */
	private static String getTargetFunctionName(Row r) {
		if(r == null) {
		    LOG.atWarn().log("Given null row, can't figure out function name");
			return null;
		}
		Cell cell = r.getCell(COLUMN_INDEX_FUNCTION_NAME);
		if(cell == null) {
			LOG.atWarn().log("Row {} has no cell " + COLUMN_INDEX_FUNCTION_NAME + ", can't figure out function name", box(r.getRowNum()));
			return null;
		}

		CellType ct = cell.getCellType();
		assertTrue(ct == CellType.BLANK || ct == CellType.STRING,
			"Bad cell type for 'function name' column: (" + cell.getCellType() + ") row (" + (r.getRowNum() +1) + ")");

		return (ct == CellType.STRING) ? cell.getRichStringCellValue().getString() : null;
	}

	/**
	 * @return <code>null</code> if cell is missing, empty or blank
	 */
	private static String getTargetTestName(Row r) {
		if(r == null) {
            LOG.atWarn().log("Given null row, can't figure out test name");
			return null;
		}
		Cell cell = r.getCell(COLUMN_INDEX_TEST_NAME);
		if(cell == null) {
			LOG.atWarn().log("Row {} has no cell " + COLUMN_INDEX_TEST_NAME + ", can't figure out test name", box(r.getRowNum()));
			return null;
		}
		CellType ct = cell.getCellType();
		assertTrue(ct == CellType.BLANK || ct == CellType.STRING,
			"Bad cell type for 'test name' column: (" + cell.getCellType() + ") row (" + (r.getRowNum() +1) + ")");

		return (ct == CellType.STRING) ? cell.getRichStringCellValue().getString() : null;
	}
}
