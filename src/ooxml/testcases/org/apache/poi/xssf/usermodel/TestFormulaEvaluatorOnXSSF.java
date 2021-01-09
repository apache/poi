/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.poi.xssf.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.formula.eval.TestFormulasFromSpreadsheet;
import org.apache.poi.ss.formula.functions.BaseTestNumeric;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Performs much the same role as {@link TestFormulasFromSpreadsheet},
 *  except for a XSSF spreadsheet, not a HSSF one.
 * This allows us to check that all our Formula Evaluation code
 *  is able to work for XSSF, as well as for HSSF.
 *
 * Periodically, you should open FormulaEvalTestData.xls in
 *  Excel 2007, and re-save it as FormulaEvalTestData_Copy.xlsx
 *
 */
public final class TestFormulaEvaluatorOnXSSF {
    private static final POILogger logger = POILogFactory.getLogger(TestFormulaEvaluatorOnXSSF.class);

    private static XSSFWorkbook workbook;
    private static Sheet sheet;
    private static FormulaEvaluator evaluator;
    private static Locale userLocale;

	/**
	 * This class defines constants for navigating around the test data spreadsheet used for these tests.
	 */
	private interface SS {

		/**
		 * Name of the test spreadsheet (found in the standard test data folder)
		 */
		String FILENAME = "FormulaEvalTestData_Copy.xlsx";
        /**
         * Row (zero-based) in the test spreadsheet where the operator examples start.
         */
        int START_OPERATORS_ROW_INDEX = 22; // Row '23'
        /**
         * Row (zero-based) in the test spreadsheet where the function examples start.
         */
        int START_FUNCTIONS_ROW_INDEX = 95; // Row '96'
        /**
         * Index of the column that contains the function names
         */
        int COLUMN_INDEX_FUNCTION_NAME = 1; // Column 'B'

        /**
         * Used to indicate when there are no more functions left
         */
        String FUNCTION_NAMES_END_SENTINEL = "<END-OF-FUNCTIONS>";

        /**
         * Index of the column where the test values start (for each function)
         */
        short COLUMN_INDEX_FIRST_TEST_VALUE = 3; // Column 'D'

        /**
         * Each function takes 4 rows in the test spreadsheet
         */
        int NUMBER_OF_ROWS_PER_FUNCTION = 4;
	}

    @AfterAll
    public static void closeResource() throws Exception {
        LocaleUtil.setUserLocale(userLocale);
        workbook.close();
    }

    public static Stream<Arguments> data() throws Exception {
        // Function "Text" uses custom-formats which are locale specific
        // can't set the locale on a per-testrun execution, as some settings have been
        // already set, when we would try to change the locale by then
        userLocale = LocaleUtil.getUserLocale();
        LocaleUtil.setUserLocale(Locale.ROOT);

        workbook = new XSSFWorkbook( OPCPackage.open(HSSFTestDataSamples.getSampleFile(SS.FILENAME), PackageAccess.READ) );
        sheet = workbook.getSheetAt( 0 );
        evaluator = new XSSFFormulaEvaluator(workbook);

        List<Arguments> data = new ArrayList<>();

        processFunctionGroup(data, SS.START_OPERATORS_ROW_INDEX, null);
        processFunctionGroup(data, SS.START_FUNCTIONS_ROW_INDEX, null);
        // example for debugging individual functions/operators:
        // processFunctionGroup(data, SS.START_OPERATORS_ROW_INDEX, "ConcatEval");
        // processFunctionGroup(data, SS.START_FUNCTIONS_ROW_INDEX, "Text");

        return data.stream();
    }

    /**
     * @param startRowIndex row index in the spreadsheet where the first function/operator is found
     * @param testFocusFunctionName name of a single function/operator to test alone.
     * Typically pass <code>null</code> to test all functions
     */
    private static void processFunctionGroup(List<Arguments> data, int startRowIndex, String testFocusFunctionName) {
        for (int rowIndex = startRowIndex; true; rowIndex += SS.NUMBER_OF_ROWS_PER_FUNCTION) {
            Row r = sheet.getRow(rowIndex);

            // only evaluate non empty row
            if(r == null) continue;

            String targetFunctionName = getTargetFunctionName(r);
            assertNotNull(targetFunctionName, "Test spreadsheet cell empty on row ("
                + (rowIndex+1) + "). Expected function name or '"
                + SS.FUNCTION_NAMES_END_SENTINEL + "'");

            if(targetFunctionName.equals(SS.FUNCTION_NAMES_END_SENTINEL)) {
                // found end of functions list
                break;
            }
            if(testFocusFunctionName == null || targetFunctionName.equalsIgnoreCase(testFocusFunctionName)) {

                // expected results are on the row below
                Row expectedValuesRow = sheet.getRow(rowIndex + 1);
                // +1 for 1-based, +1 for next row
                assertNotNull(expectedValuesRow, "Missing expected values row for function '"
                    + targetFunctionName + " (row " + rowIndex + 2 + ")");

                data.add(Arguments.of(targetFunctionName, rowIndex, rowIndex + 1));
            }
        }
    }


	@ParameterizedTest
	@MethodSource("data")
	void processFunctionRow(String targetFunctionName, int formulasRowIdx, int expectedValuesRowIdx) {
	    Row formulasRow = sheet.getRow(formulasRowIdx);
	    Row expectedValuesRow = sheet.getRow(expectedValuesRowIdx);

		short endcolnum = formulasRow.getLastCellNum();

		// iterate across the row for all the evaluation cases
		for (short colnum=SS.COLUMN_INDEX_FIRST_TEST_VALUE; colnum < endcolnum; colnum++) {
			Cell c = formulasRow.getCell(colnum);
			assumeTrue(c != null);
			assumeTrue(c.getCellType() == CellType.FORMULA);
			ignoredFormulaTestCase(c.getCellFormula());

			CellValue actValue = evaluator.evaluate(c);
			Cell expValue = (expectedValuesRow == null) ? null : expectedValuesRow.getCell(colnum);

			String msg = String.format(Locale.ROOT, "Function '%s': Formula: %s @ %d:%d"
		        , targetFunctionName, c.getCellFormula(), formulasRow.getRowNum(), colnum);

			assertNotNull(expValue, msg + " - Bad setup data expected value is null");
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
//	              if(false) { // TODO: fix ~45 functions which are currently returning incorrect error values
//	                  assertEquals(msg, expValue.getErrorCellValue(), actValue.getErrorValue());
//	              }
	                break;
	            case FORMULA: // will never be used, since we will call method after formula evaluation
	                fail("Cannot expect formula as result of formula evaluation: " + msg);
	            case NUMERIC:
	                assertEquals(CellType.NUMERIC, actValue.getCellType(), msg);
					BaseTestNumeric.assertDouble(msg, expValue.getNumericCellValue(), actValue.getNumberValue(), BaseTestNumeric.POS_ZERO, BaseTestNumeric.DIFF_TOLERANCE_FACTOR);
//	              double delta = Math.abs(expValue.getNumericCellValue()-actValue.getNumberValue());
//	              double pctExpValue = Math.abs(0.00001*expValue.getNumericCellValue());
//	              assertTrue(msg, delta <= pctExpValue);
	                break;
	            case STRING:
	                assertEquals(CellType.STRING, actValue.getCellType(), msg);
	                assertEquals(expValue.getRichStringCellValue().getString(), actValue.getStringValue(), msg);
	                break;
	            default:
	                fail("Unexpected cell type: " + expectedCellType);
	        }
		}
	}

	/*
	 * TODO - these are all formulas which currently (Apr-2008) break on ooxml
	 */
	private static void ignoredFormulaTestCase(String cellFormula) {
        // full row ranges are not parsed properly yet.
        // These cases currently work in svn trunk because of another bug which causes the
        // formula to get rendered as COLUMN($A$1:$IV$2) or ROW($A$2:$IV$3)
	    assumeFalse("COLUMN(1:2)".equals(cellFormula));
	    assumeFalse("ROW(2:3)".equals(cellFormula));

        // currently throws NPE because unknown function "currentcell" causes name lookup
        // Name lookup requires some equivalent object of the Workbook within xSSFWorkbook.
	    assumeFalse("ISREF(currentcell())".equals(cellFormula));
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

		fail("Bad cell type for 'function name' column: ("+cell.getColumnIndex()+") row ("+(r.getRowNum()+1)+")");
		return null;
	}
}
