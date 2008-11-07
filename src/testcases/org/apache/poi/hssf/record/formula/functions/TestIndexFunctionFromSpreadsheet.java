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
 * Tests INDEX() as loaded from a test data spreadsheet.<p/>
 *
 * @author Josh Micich
 */
public final class TestIndexFunctionFromSpreadsheet extends TestCase {

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
		public final static String FILENAME = "IndexFunctionTestCaseData.xls";

		public static final int COLUMN_INDEX_EVALUATION = 2; // Column 'C'
		public static final int COLUMN_INDEX_EXPECTED_RESULT = 3; // Column 'D'

	}

	// Note - multiple failures are aggregated before ending.
	// If one or more functions fail, a single AssertionFailedError is thrown at the end
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
				throw new AssertionFailedError("Cannot expect formula as result of formula evaluation: " + msg);
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
		_evaluationFailureCount = 0;
		_evaluationSuccessCount = 0;
	}

	public void testFunctionsFromTestSpreadsheet() {
		HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook(SS.FILENAME);

		processTestSheet(workbook, workbook.getSheetName(0));

		// confirm results
		String successMsg = "There were "
				+ _evaluationSuccessCount + " function(s) without error";
		if(_evaluationFailureCount > 0) {
			String msg = _evaluationFailureCount + " evaluation(s) failed.  " + successMsg;
			throw new AssertionFailedError(msg);
		}
		if(false) { // normally no output for successful tests
			System.out.println(getClass().getName() + ": " + successMsg);
		}
	}

	private void processTestSheet(HSSFWorkbook workbook, String sheetName) {
		HSSFSheet sheet = workbook.getSheetAt(0);
		HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(workbook);
		int maxRows = sheet.getLastRowNum()+1;
		int result = Result.NO_EVALUATIONS_FOUND; // so far

		for(int rowIndex=0; rowIndex<maxRows; rowIndex++) {
			HSSFRow r = sheet.getRow(rowIndex);
			if(r == null) {
				continue;
			}
			HSSFCell c = r.getCell(SS.COLUMN_INDEX_EVALUATION);
			if (c == null || c.getCellType() != HSSFCell.CELL_TYPE_FORMULA) {
				continue;
			}
			CellValue actualValue = evaluator.evaluate(c);
			HSSFCell expectedValueCell = r.getCell(SS.COLUMN_INDEX_EXPECTED_RESULT);

			String msgPrefix = formatTestCaseDetails(sheetName, r.getRowNum(), c);
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
	}


	private static String formatTestCaseDetails(String sheetName, int rowIndex, HSSFCell c) {

		StringBuffer sb = new StringBuffer();
		CellReference cr = new CellReference(sheetName, rowIndex, c.getColumnIndex(), false, false);
		sb.append(cr.formatAsString());
		sb.append(" {=").append(c.getCellFormula()).append("}");
		return sb.toString();
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
}

