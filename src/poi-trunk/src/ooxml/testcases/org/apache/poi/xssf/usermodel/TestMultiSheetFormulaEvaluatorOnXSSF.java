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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.formula.eval.TestFormulasFromSpreadsheet;
import org.apache.poi.ss.formula.functions.TestMathX;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests formulas for multi sheet reference (i.e. SUM(Sheet1:Sheet5!A1))
 */
@RunWith(Parameterized.class)
public final class TestMultiSheetFormulaEvaluatorOnXSSF {
    private static final POILogger logger = POILogFactory.getLogger(TestFormulasFromSpreadsheet.class);

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

    @Parameter(value = 0)
	public String targetTestName;
	@Parameter(value = 1)
    public String targetFunctionName;
    @Parameter(value = 2)
    public int formulasRowIdx;

    @AfterClass
    public static void closeResource() throws Exception {
        workbook.close();
    }

    @Parameters(name="{0}")
    public static Collection<Object[]> data() throws Exception {
        workbook = new XSSFWorkbook( OPCPackage.open(HSSFTestDataSamples.getSampleFile(SS.FILENAME), PackageAccess.READ) );
        sheet = workbook.getSheet( SS.TEST_SHEET_NAME );
        evaluator = new XSSFFormulaEvaluator(workbook);

        List<Object[]> data = new ArrayList<>();

        processFunctionGroup(data, SS.START_FUNCTIONS_ROW_INDEX, null);

        return data;
    }

    /**
     * @param startRowIndex row index in the spreadsheet where the first function/operator is found
     * @param testFocusFunctionName name of a single function/operator to test alone.
     * Typically pass <code>null</code> to test all functions
     */
    private static void processFunctionGroup(List<Object[]> data, int startRowIndex, String testFocusFunctionName) {
        for (int rowIndex = startRowIndex; true; rowIndex++) {
            Row r = sheet.getRow(rowIndex);

            // only evaluate non empty row
            if(r == null) continue;

            String targetFunctionName = getTargetFunctionName(r);
            assertNotNull("Test spreadsheet cell empty on row ("
                + (rowIndex+1) + "). Expected function name or '"
                + SS.FUNCTION_NAMES_END_SENTINEL + "'", targetFunctionName);

            if(targetFunctionName.equals(SS.FUNCTION_NAMES_END_SENTINEL)) {
                // found end of functions list
                break;
            }

            String targetTestName = getTargetTestName(r);
            if(testFocusFunctionName == null || targetFunctionName.equalsIgnoreCase(testFocusFunctionName)) {

                // expected results are on the row below
                Cell expectedValueCell = r.getCell(SS.COLUMN_INDEX_EXPECTED_VALUE);
                assertNotNull("Missing expected values cell for function '"
                    + targetFunctionName + ", test" + targetTestName + " (row " +
                    rowIndex + 1 + ")", expectedValueCell);

                data.add(new Object[]{targetTestName, targetFunctionName, rowIndex});
            }
        }
    }

    /**
    *
    * @return a constant from the local Result class denoting whether there were any evaluation
    * cases, and whether they all succeeded.
    */
    @Test
    public void processFunctionRow() {
        Row r = sheet.getRow(formulasRowIdx);

        Cell expValue = r.getCell(SS.COLUMN_INDEX_EXPECTED_VALUE);
        assertNotNull("Missing expected values cell for function '"
            + targetFunctionName + ", test" + targetTestName + " (row " +
            formulasRowIdx + 1 + ")", expValue);

        Cell c = r.getCell(SS.COLUMN_INDEX_ACTUAL_VALUE);
        assumeNotNull(c);
        assumeTrue(c.getCellType() == CellType.FORMULA);

        CellValue actValue = evaluator.evaluate(c);

        String msg = String.format(Locale.ROOT, "Function '%s': Test: '%s': Formula: %s @ %d:%d",
            targetFunctionName, targetTestName, c.getCellFormula(), formulasRowIdx, SS.COLUMN_INDEX_ACTUAL_VALUE);

        assertNotNull(msg + " - actual value was null", actValue);

        final CellType expectedCellType = expValue.getCellType();
        switch (expectedCellType) {
            case BLANK:
                assertEquals(msg, CellType.BLANK, actValue.getCellType());
                break;
            case BOOLEAN:
                assertEquals(msg, CellType.BOOLEAN, actValue.getCellType());
                assertEquals(msg, expValue.getBooleanCellValue(), actValue.getBooleanValue());
                break;
            case ERROR:
                assertEquals(msg, CellType.ERROR, actValue.getCellType());
//              if(false) { // TODO: fix ~45 functions which are currently returning incorrect error values
//                  assertEquals(msg, expected.getErrorCellValue(), actual.getErrorValue());
//              }
                break;
            case FORMULA: // will never be used, since we will call method after formula evaluation
                fail("Cannot expect formula as result of formula evaluation: " + msg);
            case NUMERIC:
                assertEquals(msg, CellType.NUMERIC, actValue.getCellType());
                TestMathX.assertEquals(msg, expValue.getNumericCellValue(), actValue.getNumberValue(), TestMathX.POS_ZERO, TestMathX.DIFF_TOLERANCE_FACTOR);
//              double delta = Math.abs(expected.getNumericCellValue()-actual.getNumberValue());
//              double pctExpected = Math.abs(0.00001*expected.getNumericCellValue());
//              assertTrue(msg, delta <= pctExpected);
                break;
            case STRING:
                assertEquals(msg, CellType.STRING, actValue.getCellType());
                assertEquals(msg, expValue.getRichStringCellValue().getString(), actValue.getStringValue());
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
		    logger.log(POILogger.WARN, "Warning - given null row, can't figure out function name");
			return null;
		}
		Cell cell = r.getCell(SS.COLUMN_INDEX_FUNCTION_NAME);
		if(cell == null) {
            logger.log(POILogger.WARN, "Warning - Row " + r.getRowNum() + " has no cell " + SS.COLUMN_INDEX_FUNCTION_NAME + ", can't figure out function name");
			return null;
		}
		if(cell.getCellType() == CellType.BLANK) {
			return null;
		}
		if(cell.getCellType() == CellType.STRING) {
			return cell.getRichStringCellValue().getString();
		}

		fail("Bad cell type for 'function name' column: ("
			+ cell.getCellType() + ") row (" + (r.getRowNum() +1) + ")");
		return "";
	}
	/**
	 * @return <code>null</code> if cell is missing, empty or blank
	 */
	private static String getTargetTestName(Row r) {
		if(r == null) {
            logger.log(POILogger.WARN, "Warning - given null row, can't figure out test name");
			return null;
		}
		Cell cell = r.getCell(SS.COLUMN_INDEX_TEST_NAME);
		if(cell == null) {
		    logger.log(POILogger.WARN, "Warning - Row " + r.getRowNum() + " has no cell " + SS.COLUMN_INDEX_TEST_NAME + ", can't figure out test name");
			return null;
		}
		if(cell.getCellType() == CellType.BLANK) {
			return null;
		}
		if(cell.getCellType() == CellType.STRING) {
			return cell.getRichStringCellValue().getString();
		}

		fail("Bad cell type for 'test name' column: ("
			+ cell.getCellType() + ") row (" + (r.getRowNum() +1) + ")");
		return "";
	}

}
