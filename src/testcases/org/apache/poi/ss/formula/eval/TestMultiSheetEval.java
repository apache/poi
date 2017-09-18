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

import java.io.PrintStream;
import java.util.Collection;
import java.util.Locale;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.TestMathX;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Tests formulas for multi sheet reference (i.e. SUM(Sheet1:Sheet5!A1))
 */
public final class TestMultiSheetEval extends TestCase {
    private static final POILogger logger = POILogFactory.getLogger(TestFormulasFromSpreadsheet.class);

	private static final class Result {
		public static final int SOME_EVALUATIONS_FAILED = -1;
		public static final int ALL_EVALUATIONS_SUCCEEDED = +1;
		public static final int NO_EVALUATIONS_FOUND = 0;
	}

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

	private HSSFWorkbook workbook;
	private Sheet sheet;
	// Note - multiple failures are aggregated before ending.
	// If one or more functions fail, a single AssertionFailedError is thrown at the end
	private int _functionFailureCount;
	private int _functionSuccessCount;
	private int _evaluationFailureCount;
	private int _evaluationSuccessCount;

	private static void confirmExpectedResult(String msg, Cell expected, CellValue actual) {
		if (expected == null) {
			throw new AssertionFailedError(msg + " - Bad setup data expected value is null");
		}
		if(actual == null) {
			throw new AssertionFailedError(msg + " - actual value was null");
		}
		
		final CellType cellType = expected.getCellType();

		switch (cellType) {
			case BLANK:
				assertEquals(msg, CellType.BLANK, actual.getCellType());
				break;
			case BOOLEAN:
				assertEquals(msg, CellType.BOOLEAN, actual.getCellType());
				assertEquals(msg, expected.getBooleanCellValue(), actual.getBooleanValue());
				break;
			case ERROR:
				assertEquals(msg, CellType.ERROR, actual.getCellType());
				assertEquals(msg, ErrorEval.getText(expected.getErrorCellValue()), ErrorEval.getText(actual.getErrorValue()));
				break;
			case FORMULA: // will never be used, since we will call method after formula evaluation
				throw new AssertionFailedError("Cannot expect formula as result of formula evaluation: " + msg);
			case NUMERIC:
				assertEquals(msg, CellType.NUMERIC, actual.getCellType());
				TestMathX.assertEquals(msg, expected.getNumericCellValue(), actual.getNumberValue(), TestMathX.POS_ZERO, TestMathX.DIFF_TOLERANCE_FACTOR);
				break;
			case STRING:
				assertEquals(msg, CellType.STRING, actual.getCellType());
				assertEquals(msg, expected.getRichStringCellValue().getString(), actual.getStringValue());
				break;
			default:
				throw new AssertionFailedError("Unexpected cell type: " + cellType);
		}
	}


	@Override
    protected void setUp() {
		if (workbook == null) {
			workbook = HSSFTestDataSamples.openSampleWorkbook(SS.FILENAME);
			sheet = workbook.getSheet( SS.TEST_SHEET_NAME );
		}
		_functionFailureCount = 0;
		_functionSuccessCount = 0;
		_evaluationFailureCount = 0;
		_evaluationSuccessCount = 0;
	}

	public void testFunctionsFromTestSpreadsheet() {

		processFunctionGroup(SS.START_FUNCTIONS_ROW_INDEX, null);

		// confirm results
		String successMsg = "There were "
				+ _evaluationSuccessCount + " successful evaluation(s) and "
				+ _functionSuccessCount + " function(s) without error";
		if(_functionFailureCount > 0) {
			String msg = _functionFailureCount + " function(s) failed in "
			+ _evaluationFailureCount + " evaluation(s).  " + successMsg;
			throw new AssertionFailedError(msg);
		}
        logger.log(POILogger.INFO, getClass().getName() + ": " + successMsg);
	}

	/**
	 * @param startRowIndex row index in the spreadsheet where the first function/operator is found
	 * @param testFocusFunctionName name of a single function/operator to test alone.
	 * Typically pass <code>null</code> to test all functions
	 */
	private void processFunctionGroup(int startRowIndex, String testFocusFunctionName) {
		HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(workbook);
        Collection<String> funcs = FunctionEval.getSupportedFunctionNames();

		int rowIndex = startRowIndex;
		while (true) {
			Row r = sheet.getRow(rowIndex);
			
			// only evaluate non empty row
			if( r != null )
			{
				String targetFunctionName = getTargetFunctionName(r);
				String targetTestName = getTargetTestName(r);
				if(targetFunctionName == null) {
					throw new AssertionFailedError("Test spreadsheet cell empty on row ("
							+ (rowIndex+1) + "). Expected function name or '"
							+ SS.FUNCTION_NAMES_END_SENTINEL + "'");
				}
				if(targetFunctionName.equals(SS.FUNCTION_NAMES_END_SENTINEL)) {
					// found end of functions list
					break;
				}
				if(testFocusFunctionName == null || targetFunctionName.equalsIgnoreCase(testFocusFunctionName)) {

					// expected results are on the row below
					Cell expectedValueCell = r.getCell(SS.COLUMN_INDEX_EXPECTED_VALUE);
					if(expectedValueCell == null) {
						int missingRowNum = rowIndex + 1;
						throw new AssertionFailedError("Missing expected values cell for function '"
								+ targetFunctionName + ", test" + targetTestName + " (row " + 
								missingRowNum + ")");
					}
					
					switch(processFunctionRow(evaluator, targetFunctionName, targetTestName, r, expectedValueCell)) {
						case Result.ALL_EVALUATIONS_SUCCEEDED: _functionSuccessCount++; break;
						case Result.SOME_EVALUATIONS_FAILED: _functionFailureCount++; break;
						default:
							throw new RuntimeException("unexpected result");
						case Result.NO_EVALUATIONS_FOUND: // do nothing
							String uname = targetFunctionName.toUpperCase(Locale.ROOT);
							if(startRowIndex >= SS.START_FUNCTIONS_ROW_INDEX &&
									funcs.contains(uname)) {
								logger.log(POILogger.WARN, uname + ": function is supported but missing test data");
							}
							break;
					}
				}
			}
			rowIndex ++;
		}
	}

	/**
	 *
	 * @return a constant from the local Result class denoting whether there were any evaluation
	 * cases, and whether they all succeeded.
	 */
	private int processFunctionRow(HSSFFormulaEvaluator evaluator, String targetFunctionName, 
			String targetTestName, Row formulasRow, Cell expectedValueCell) {

		int result = Result.NO_EVALUATIONS_FOUND; // so far

		Cell c = formulasRow.getCell(SS.COLUMN_INDEX_ACTUAL_VALUE);
		if (c == null || c.getCellType() != CellType.FORMULA) {
			return result;
		}

		CellValue actualValue = evaluator.evaluate(c);

		try {
			confirmExpectedResult("Function '" + targetFunctionName + "': Test: '" + targetTestName + "' Formula: " + c.getCellFormula() 
			+ " @ " + formulasRow.getRowNum() + ":" + SS.COLUMN_INDEX_ACTUAL_VALUE,
					expectedValueCell, actualValue);
			_evaluationSuccessCount ++;
			if(result != Result.SOME_EVALUATIONS_FAILED) {
				result = Result.ALL_EVALUATIONS_SUCCEEDED;
			}
		} catch (AssertionFailedError e) {
			_evaluationFailureCount ++;
			printShortStackTrace(System.err, e);
			result = Result.SOME_EVALUATIONS_FAILED;
		}
	
		return result;
	}

	/**
	 * Useful to keep output concise when expecting many failures to be reported by this test case
	 */
	private static void printShortStackTrace(PrintStream ps, AssertionFailedError e) {
		StackTraceElement[] stes = e.getStackTrace();

		int startIx = 0;
		// skip any top frames inside junit.framework.Assert
		while(startIx<stes.length) {
			if(!stes[startIx].getClassName().equals(Assert.class.getName())) {
				break;
			}
			startIx++;
		}
		// skip bottom frames (part of junit framework)
		int endIx = startIx+1;
		while(endIx < stes.length) {
			if(stes[endIx].getClassName().equals(TestCase.class.getName())) {
				break;
			}
			endIx++;
		}
		if(startIx >= endIx) {
			// something went wrong. just print the whole stack trace
			e.printStackTrace(ps);
		}
		endIx -= 4; // skip 4 frames of reflection invocation
		ps.println(e);
		for(int i=startIx; i<endIx; i++) {
			ps.println("\tat " + stes[i]);
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
		if(cell.getCellType() == CellType.BLANK) {
			return null;
		}
		if(cell.getCellType() == CellType.STRING) {
			return cell.getRichStringCellValue().getString();
		}

		throw new AssertionFailedError("Bad cell type for 'function name' column: ("
				+ cell.getCellType() + ") row (" + (r.getRowNum() +1) + ")");
	}
	/**
	 * @return <code>null</code> if cell is missing, empty or blank
	 */
	private static String getTargetTestName(Row r) {
		if(r == null) {
			System.err.println("Warning - given null row, can't figure out test name");
			return null;
		}
		Cell cell = r.getCell(SS.COLUMN_INDEX_TEST_NAME);
		if(cell == null) {
			System.err.println("Warning - Row " + r.getRowNum() + " has no cell " + SS.COLUMN_INDEX_TEST_NAME + ", can't figure out test name");
			return null;
		}
		if(cell.getCellType() == CellType.BLANK) {
			return null;
		}
		if(cell.getCellType() == CellType.STRING) {
			return cell.getRichStringCellValue().getString();
		}

		throw new AssertionFailedError("Bad cell type for 'test name' column: ("
				+ cell.getCellType() + ") row (" + (r.getRowNum() +1) + ")");
	}
	
}
