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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.TestMathX;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests formulas and operators as loaded from a test data spreadsheet.<p>
 * This class does not test implementors of <tt>Function</tt> and <tt>OperationEval</tt> in
 * isolation.  Much of the evaluation engine (i.e. <tt>HSSFFormulaEvaluator</tt>, ...) gets
 * exercised as well.  Tests for bug fixes and specific/tricky behaviour can be found in the
 * corresponding test class (<tt>TestXxxx</tt>) of the target (<tt>Xxxx</tt>) implementor,
 * where execution can be observed more easily.
 */
public final class TestFormulasFromSpreadsheet {

    private static HSSFWorkbook workbook;
    private static Sheet sheet;
    private static HSSFFormulaEvaluator evaluator;
    private static Locale userLocale;

    /**
	 * This class defines constants for navigating around the test data spreadsheet used for these tests.
	 */
	private interface SS {

		/**
		 * Name of the test spreadsheet (found in the standard test data folder)
		 */
		String FILENAME = "FormulaEvalTestData.xls";
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

    public static Stream<Arguments> data() {
        // Function "Text" uses custom-formats which are locale specific
        // can't set the locale on a per-testrun execution, as some settings have been
        // already set, when we would try to change the locale by then
        userLocale = LocaleUtil.getUserLocale();
        LocaleUtil.setUserLocale(Locale.ROOT);

        workbook = HSSFTestDataSamples.openSampleWorkbook(SS.FILENAME);
        sheet = workbook.getSheetAt( 0 );
        evaluator = new HSSFFormulaEvaluator(workbook);

        List<Arguments> data = new ArrayList<>();

        processFunctionGroup(data, SS.START_OPERATORS_ROW_INDEX);
        processFunctionGroup(data, SS.START_FUNCTIONS_ROW_INDEX);
        // example for debugging individual functions/operators:
        // processFunctionGroup(data, SS.START_OPERATORS_ROW_INDEX, "ConcatEval");
        // processFunctionGroup(data, SS.START_FUNCTIONS_ROW_INDEX, "Text");

        return data.stream();
    }

    /**
     * @param startRowIndex row index in the spreadsheet where the first function/operator is found
     * Typically pass <code>null</code> to test all functions
     */
    private static void processFunctionGroup(List<Arguments> data, int startRowIndex) {
        for (int rowIndex = startRowIndex; true; rowIndex += SS.NUMBER_OF_ROWS_PER_FUNCTION) {
            Row r = sheet.getRow(rowIndex);
            String targetFunctionName = getTargetFunctionName(r);
            assertNotNull(targetFunctionName, "Test spreadsheet cell empty on row ("
                    + (rowIndex+1) + "). Expected function name or '"
                    + SS.FUNCTION_NAMES_END_SENTINEL + "'");
            if(targetFunctionName.equals(SS.FUNCTION_NAMES_END_SENTINEL)) {
                // found end of functions list
                break;
            }

            // expected results are on the row below
            Row expectedValuesRow = sheet.getRow(rowIndex + 1);
            int missingRowNum = rowIndex + 2; //+1 for 1-based, +1 for next row
            assertNotNull(expectedValuesRow, "Missing expected values row for function '"
                    + targetFunctionName + " (row " + missingRowNum + ")");

            data.add(Arguments.of(targetFunctionName, rowIndex, rowIndex + 1));
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    void processFunctionRow(String targetFunctionName, int formulasRowIdx, int expectedValuesRowIdx) {
        Row formulasRow = sheet.getRow(formulasRowIdx);
        Row expectedValuesRow = sheet.getRow(expectedValuesRowIdx);

        short endcolnum = formulasRow.getLastCellNum();

       // iterate across the row for all the evaluation cases
       for (int colnum=SS.COLUMN_INDEX_FIRST_TEST_VALUE; colnum < endcolnum; colnum++) {
           Cell c = formulasRow.getCell(colnum);
           if (c == null || c.getCellType() != CellType.FORMULA) {
               continue;
           }

           CellValue actValue = evaluator.evaluate(c);
           Cell expValue = (expectedValuesRow == null) ? null : expectedValuesRow.getCell(colnum);

           String msg = String.format(Locale.ROOT, "Function '%s': Formula: %s @ %d:%d"
                   , targetFunctionName, c.getCellFormula(), formulasRow.getRowNum(), colnum);

           assertNotNull(expValue, msg + " - Bad setup data expected value is null");
           assertNotNull(actValue, msg + " - actual value was null");

           final CellType cellType = expValue.getCellType();
           switch (cellType) {
               case BLANK:
                   assertEquals(CellType.BLANK, actValue.getCellType(), msg);
                   break;
               case BOOLEAN:
                   assertEquals(CellType.BOOLEAN, actValue.getCellType(), msg);
                   assertEquals(expValue.getBooleanCellValue(), actValue.getBooleanValue(), msg);
                   break;
               case ERROR:
                   assertEquals(CellType.ERROR, actValue.getCellType(), msg);
                   assertEquals(ErrorEval.getText(expValue.getErrorCellValue()), ErrorEval.getText(actValue.getErrorValue()), msg);
                   break;
               case FORMULA: // will never be used, since we will call method after formula evaluation
                   fail("Cannot expect formula as result of formula evaluation: " + msg);
               case NUMERIC:
                   assertEquals(CellType.NUMERIC, actValue.getCellType(), msg);
                   TestMathX.assertDouble(msg, expValue.getNumericCellValue(), actValue.getNumberValue(), TestMathX.POS_ZERO, TestMathX.DIFF_TOLERANCE_FACTOR);
                   break;
               case STRING:
                   assertEquals(CellType.STRING, actValue.getCellType(), msg);
                   assertEquals(expValue.getRichStringCellValue().getString(), actValue.getStringValue(), msg);
                   break;
               default:
                   fail("Unexpected cell type: " + cellType);
           }
       }
   }
	/**
	 * @return <code>null</code> if cell is missing, empty or blank
	 */
	private static String getTargetFunctionName(Row r) {
		if(r == null) {
			System.err.println("Warning - given null row, can't figure out function name");
			return null;
		}
		Cell cell = r.getCell(SS.COLUMN_INDEX_FUNCTION_NAME);
		if(cell == null) {
			System.err.println("Warning - Row " + r.getRowNum() + " has no cell " + SS.COLUMN_INDEX_FUNCTION_NAME + ", can't figure out function name");
			return null;
		}

        CellType ct = cell.getCellType();
        assertTrue(ct == CellType.BLANK || ct == CellType.STRING,
            "Bad cell type for 'function name' column: (" + cell.getCellType() + ") row (" + (r.getRowNum() +1) + ")");

        return (ct == CellType.STRING) ? cell.getRichStringCellValue().getString() : null;
	}
}
