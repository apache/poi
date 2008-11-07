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

package org.apache.poi.hssf.record.formula.functions;

import java.io.PrintStream;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.CellValue;

/**
 * Tests lookup functions (VLOOKUP, HLOOKUP, LOOKUP, MATCH) as loaded from a test data spreadsheet.<p/>
 * These tests have been separated from the common function and operator tests because the lookup
 * functions have more complex test cases and test data setup.
 *
 * Tests for bug fixes and specific/tricky behaviour can be found in the corresponding test class
 * (<tt>TestXxxx</tt>) of the target (<tt>Xxxx</tt>) implementor, where execution can be observed
 *  more easily.
 *
 * @author Josh Micich
 */
public final class TestLookupFunctionsFromSpreadsheet extends TestCase {

	private static final class Result {
		public static final int SOME_EVALUATIONS_FAILED = -1;
		public static final int ALL_EVALUATIONS_SUCCEEDED = +1;
		public static final int NO_EVALUATIONS_FOUND = 0;
	}

	/**
	 * This class defines constants for navigating around the test data spreadsheet used for these tests.
	 */
	private static final class SS {

		/** Name of the test spreadsheet (found in the standard test data folder) */
		public final static String FILENAME = "LookupFunctionsTestCaseData.xls";

		/** Name of the first sheet in the spreadsheet (contains comments) */
		public final static String README_SHEET_NAME = "Read Me";


		/** Row (zero-based) in each sheet where the evaluation cases start.   */
		public static final int START_TEST_CASES_ROW_INDEX = 4; // Row '5'
		/**  Index of the column that contains the function names */
		public static final int COLUMN_INDEX_MARKER = 0; // Column 'A'
		public static final int COLUMN_INDEX_EVALUATION = 1; // Column 'B'
		public static final int COLUMN_INDEX_EXPECTED_RESULT = 2; // Column 'C'
		public static final int COLUMN_ROW_COMMENT = 3; // Column 'D'

		/** Used to indicate when there are no more test cases on the current sheet   */
		public static final String TEST_CASES_END_MARKER = "<end>";
		/** Used to indicate that the test on the current row should be ignored */
		public static final String SKIP_CURRENT_TEST_CASE_MARKER = "<skip>";

	}

	// Note - multiple failures are aggregated before ending.
	// If one or more functions fail, a single AssertionFailedError is thrown at the end
	private int _sheetFailureCount;
	private int _sheetSuccessCount;
	private int _evaluationFailureCount;
	private int _evaluationSuccessCount;



	private static void confirmExpectedResult(String msg, HSSFCell expected, CellValue actual) {
		if (expected == null) {
			throw new AssertionFailedError(msg + " - Bad setup data expected value is null");
		}
		if(actual == null) {
			throw new AssertionFailedError(msg + " - actual value was null");
		}
		if(expected.getCellType() == HSSFCell.CELL_TYPE_ERROR) {
			confirmErrorResult(msg, expected.getErrorCellValue(), actual);
			return;
		}
		if(actual.getCellType() == HSSFCell.CELL_TYPE_ERROR) {
			throw unexpectedError(msg, expected, actual.getErrorValue());
		}
		if(actual.getCellType() != expected.getCellType()) {
			throw wrongTypeError(msg, expected, actual);
		}


		switch (expected.getCellType()) {
			case HSSFCell.CELL_TYPE_BOOLEAN:
				assertEquals(msg, expected.getBooleanCellValue(), actual.getBooleanValue());
				break;
			case HSSFCell.CELL_TYPE_FORMULA: // will never be used, since we will call method after formula evaluation
				throw new IllegalStateException("Cannot expect formula as result of formula evaluation: " + msg);
			case HSSFCell.CELL_TYPE_NUMERIC:
				assertEquals(expected.getNumericCellValue(), actual.getNumberValue(), 0.0);
				break;
			case HSSFCell.CELL_TYPE_STRING:
				assertEquals(msg, expected.getRichStringCellValue().getString(), actual.getStringValue());
				break;
		}
	}


	private static AssertionFailedError wrongTypeError(String msgPrefix, HSSFCell expectedCell, CellValue actualValue) {
		return new AssertionFailedError(msgPrefix + " Result type mismatch. Evaluated result was "
				+ actualValue.formatAsString()
				+ " but the expected result was "
				+ formatValue(expectedCell)
				);
	}
	private static AssertionFailedError unexpectedError(String msgPrefix, HSSFCell expected, int actualErrorCode) {
		return new AssertionFailedError(msgPrefix + " Error code ("
				+ ErrorEval.getText(actualErrorCode)
				+ ") was evaluated, but the expected result was "
				+ formatValue(expected)
				);
	}


	private static void confirmErrorResult(String msgPrefix, int expectedErrorCode, CellValue actual) {
		if(actual.getCellType() != HSSFCell.CELL_TYPE_ERROR) {
			throw new AssertionFailedError(msgPrefix + " Expected cell error ("
					+ ErrorEval.getText(expectedErrorCode) + ") but actual value was "
					+ actual.formatAsString());
		}
		if(expectedErrorCode != actual.getErrorValue()) {
			throw new AssertionFailedError(msgPrefix + " Expected cell error code ("
					+ ErrorEval.getText(expectedErrorCode)
					+ ") but actual error code was ("
					+ ErrorEval.getText(actual.getErrorValue())
					+ ")");
		}
	}


	private static String formatValue(HSSFCell expecedCell) {
		switch (expecedCell.getCellType()) {
			case HSSFCell.CELL_TYPE_BLANK: return "<blank>";
			case HSSFCell.CELL_TYPE_BOOLEAN: return String.valueOf(expecedCell.getBooleanCellValue());
			case HSSFCell.CELL_TYPE_NUMERIC: return String.valueOf(expecedCell.getNumericCellValue());
			case HSSFCell.CELL_TYPE_STRING: return expecedCell.getRichStringCellValue().getString();
		}
		throw new RuntimeException("Unexpected cell type of expected value (" + expecedCell.getCellType() + ")");
	}


	protected void setUp() {
		_sheetFailureCount = 0;
		_sheetSuccessCount = 0;
		_evaluationFailureCount = 0;
		_evaluationSuccessCount = 0;
	}

	public void testFunctionsFromTestSpreadsheet() {
		HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook(SS.FILENAME);

		confirmReadMeSheet(workbook);
		int nSheets = workbook.getNumberOfSheets();
		for(int i=1; i< nSheets; i++) {
			int sheetResult = processTestSheet(workbook, i, workbook.getSheetName(i));
			switch(sheetResult) {
				case Result.ALL_EVALUATIONS_SUCCEEDED: _sheetSuccessCount ++; break;
				case Result.SOME_EVALUATIONS_FAILED: _sheetFailureCount ++; break;
			}
		}

		// confirm results
		String successMsg = "There were "
				+ _sheetSuccessCount + " successful sheets(s) and "
				+ _evaluationSuccessCount + " function(s) without error";
		if(_sheetFailureCount > 0) {
			String msg = _sheetFailureCount + " sheets(s) failed with "
			+ _evaluationFailureCount + " evaluation(s).  " + successMsg;
			throw new AssertionFailedError(msg);
		}
		if(false) { // normally no output for successful tests
			System.out.println(getClass().getName() + ": " + successMsg);
		}
	}

	private int processTestSheet(HSSFWorkbook workbook, int sheetIndex, String sheetName) {
		HSSFSheet sheet = workbook.getSheetAt(sheetIndex);
		HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(workbook);
		int maxRows = sheet.getLastRowNum()+1;
		int result = Result.NO_EVALUATIONS_FOUND; // so far

		String currentGroupComment = null;
		for(int rowIndex=SS.START_TEST_CASES_ROW_INDEX; rowIndex<maxRows; rowIndex++) {
			HSSFRow r = sheet.getRow(rowIndex);
			String newMarkerValue = getMarkerColumnValue(r);
			if(r == null) {
				continue;
			}
			if(SS.TEST_CASES_END_MARKER.equalsIgnoreCase(newMarkerValue)) {
				// normal exit point
				return result;
			}
			if(SS.SKIP_CURRENT_TEST_CASE_MARKER.equalsIgnoreCase(newMarkerValue)) {
				// currently disabled test case row
				continue;
			}
			if(newMarkerValue != null) {
				currentGroupComment = newMarkerValue;
			}
			HSSFCell c = r.getCell(SS.COLUMN_INDEX_EVALUATION);
			if (c == null || c.getCellType() != HSSFCell.CELL_TYPE_FORMULA) {
				continue;
			}
			CellValue actualValue = evaluator.evaluate(c);
			HSSFCell expectedValueCell = r.getCell(SS.COLUMN_INDEX_EXPECTED_RESULT);
			String rowComment = getRowCommentColumnValue(r);

			String msgPrefix = formatTestCaseDetails(sheetName, r.getRowNum(), c, currentGroupComment, rowComment);
			try {
				confirmExpectedResult(msgPrefix, expectedValueCell, actualValue);
				_evaluationSuccessCount ++;
				if(result != Result.SOME_EVALUATIONS_FAILED) {
					result = Result.ALL_EVALUATIONS_SUCCEEDED;
				}
			} catch (RuntimeException e) {
				_evaluationFailureCount ++;
				printShortStackTrace(System.err, e);
				result = Result.SOME_EVALUATIONS_FAILED;
			} catch (AssertionFailedError e) {
				_evaluationFailureCount ++;
				printShortStackTrace(System.err, e);
				result = Result.SOME_EVALUATIONS_FAILED;
			}

		}
		throw new RuntimeException("Missing end marker '" + SS.TEST_CASES_END_MARKER
				+ "' on sheet '" + sheetName + "'");

	}


	private static String formatTestCaseDetails(String sheetName, int rowIndex, HSSFCell c, String currentGroupComment,
			String rowComment) {

		StringBuffer sb = new StringBuffer();
		CellReference cr = new CellReference(sheetName, rowIndex, c.getColumnIndex(), false, false);
		sb.append(cr.formatAsString());
		sb.append(" {=").append(c.getCellFormula()).append("}");

		if(currentGroupComment != null) {
			sb.append(" '");
			sb.append(currentGroupComment);
			if(rowComment != null) {
				sb.append(" - ");
				sb.append(rowComment);
			}
			sb.append("' ");
		} else {
			if(rowComment != null) {
				sb.append(" '");
				sb.append(rowComment);
				sb.append("' ");
			}
		}

		return sb.toString();
	}

	/**
	 * Asserts that the 'read me' comment page exists, and has this class' name in one of the
	 * cells.  This back-link is to make it easy to find this class if a reader encounters the
	 * spreadsheet first.
	 */
	private void confirmReadMeSheet(HSSFWorkbook workbook) {
		String firstSheetName = workbook.getSheetName(0);
		if(!firstSheetName.equalsIgnoreCase(SS.README_SHEET_NAME)) {
			throw new RuntimeException("First sheet's name was '" + firstSheetName + "' but expected '" + SS.README_SHEET_NAME + "'");
		}
		HSSFSheet sheet = workbook.getSheetAt(0);
		String specifiedClassName = sheet.getRow(2).getCell(0).getRichStringCellValue().getString();
		assertEquals("Test class name in spreadsheet comment", getClass().getName(), specifiedClassName);
	}


	/**
	 * Useful to keep output concise when expecting many failures to be reported by this test case
	 */
	private static void printShortStackTrace(PrintStream ps, Throwable e) {
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
		ps.println(e.toString());
		for(int i=startIx; i<endIx; i++) {
			ps.println("\tat " + stes[i].toString());
		}

	}

	private static String getRowCommentColumnValue(HSSFRow r) {
		return getCellTextValue(r, SS.COLUMN_ROW_COMMENT, "row comment");
	}

	private static String getMarkerColumnValue(HSSFRow r) {
		return getCellTextValue(r, SS.COLUMN_INDEX_MARKER, "marker");
	}

	/**
	 * @return <code>null</code> if cell is missing, empty or blank
	 */
	private static String getCellTextValue(HSSFRow r, int colIndex, String columnName) {
		if(r == null) {
			return null;
		}
		HSSFCell cell = r.getCell(colIndex);
		if(cell == null) {
			return null;
		}
		if(cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
			return null;
		}
		if(cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
			return cell.getRichStringCellValue().getString();
		}

		throw new RuntimeException("Bad cell type for '" + columnName + "' column: ("
				+ cell.getCellType() + ") row (" + (r.getRowNum() +1) + ")");
	}
}
