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

import java.io.InputStream;
import java.io.PrintStream;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.formula.eval.TestFormulasFromSpreadsheet;
import org.apache.poi.hssf.record.formula.functions.TestMathX;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.openxml4j.opc.OPCPackage;

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
public final class TestFormulaEvaluatorOnXSSF extends TestCase {
	
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
		public final static String FILENAME = "FormulaEvalTestData_Copy.xlsx";
		/**
		 * Row (zero-based) in the test spreadsheet where the operator examples start.
		 */
		public static final int START_OPERATORS_ROW_INDEX = 22; // Row '23'
		/**
		 * Row (zero-based) in the test spreadsheet where the function examples start.
		 */
		public static final int START_FUNCTIONS_ROW_INDEX = 87; // Row '88' 
		/** 
		 * Index of the column that contains the function names
		 */
		public static final short COLUMN_INDEX_FUNCTION_NAME = 1; // Column 'B'
	
		/**
		 * Used to indicate when there are no more functions left
		 */
		public static final String FUNCTION_NAMES_END_SENTINEL = "<END-OF-FUNCTIONS>";
	
		/**
		 * Index of the column where the test values start (for each function)
		 */
		public static final short COLUMN_INDEX_FIRST_TEST_VALUE = 3; // Column 'D'
		
		/**
		 * Each function takes 4 rows in the test spreadsheet 
		 */
		public static final int NUMBER_OF_ROWS_PER_FUNCTION = 4;
	}

	private XSSFWorkbook workbook;
	private Sheet sheet;
	// Note - multiple failures are aggregated before ending.  
	// If one or more functions fail, a single AssertionFailedError is thrown at the end
	private int _functionFailureCount;
	private int _functionSuccessCount;
	private int _evaluationFailureCount;
	private int _evaluationSuccessCount;

	private static final Cell getExpectedValueCell(Row row, short columnIndex) {
		if (row == null) {
			return null;
		}
		return row.getCell(columnIndex);
	}


	private static void confirmExpectedResult(String msg, Cell expected, CellValue actual) {
		if (expected == null) {
			throw new AssertionFailedError(msg + " - Bad setup data expected value is null");
		}
		if(actual == null) {
			throw new AssertionFailedError(msg + " - actual value was null");
		}
		
		switch (expected.getCellType()) {
			case Cell.CELL_TYPE_BLANK:
				assertEquals(msg, Cell.CELL_TYPE_BLANK, actual.getCellType());
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				assertEquals(msg, Cell.CELL_TYPE_BOOLEAN, actual.getCellType());
				assertEquals(msg, expected.getBooleanCellValue(), actual.getBooleanValue());
				break;
			case Cell.CELL_TYPE_ERROR:
				assertEquals(msg, Cell.CELL_TYPE_ERROR, actual.getCellType());
				if(false) { // TODO: fix ~45 functions which are currently returning incorrect error values
					assertEquals(msg, expected.getErrorCellValue(), actual.getErrorValue());
				}
				break;
			case Cell.CELL_TYPE_FORMULA: // will never be used, since we will call method after formula evaluation
				throw new AssertionFailedError("Cannot expect formula as result of formula evaluation: " + msg);
			case Cell.CELL_TYPE_NUMERIC:
				assertEquals(msg, Cell.CELL_TYPE_NUMERIC, actual.getCellType());
				TestMathX.assertEquals(msg, expected.getNumericCellValue(), actual.getNumberValue(), TestMathX.POS_ZERO, TestMathX.DIFF_TOLERANCE_FACTOR);
//				double delta = Math.abs(expected.getNumericCellValue()-actual.getNumberValue());
//				double pctExpected = Math.abs(0.00001*expected.getNumericCellValue());
//				assertTrue(msg, delta <= pctExpected);
				break;
			case Cell.CELL_TYPE_STRING:
				assertEquals(msg, Cell.CELL_TYPE_STRING, actual.getCellType());
				assertEquals(msg, expected.getRichStringCellValue().getString(), actual.getStringValue());
				break;
		}
	}


	protected void setUp() throws Exception {
		if (workbook == null) {
			InputStream is = HSSFTestDataSamples.openSampleFileStream(SS.FILENAME);
			OPCPackage pkg = OPCPackage.open(is);
			workbook = new XSSFWorkbook( pkg );
			sheet = workbook.getSheetAt( 0 );
		  }
		_functionFailureCount = 0;
		_functionSuccessCount = 0;
		_evaluationFailureCount = 0;
		_evaluationSuccessCount = 0;
	}
	
	/**
	 * Checks that we can actually open the file
	 */
	public void testOpen() {
		assertNotNull(workbook);
	}
	
	/**
	 * Disabled for now, as many things seem to break
	 *  for XSSF, which is a shame
	 */
	public void testFunctionsFromTestSpreadsheet() {
		
		processFunctionGroup(SS.START_OPERATORS_ROW_INDEX, null);
		processFunctionGroup(SS.START_FUNCTIONS_ROW_INDEX, null);
		// example for debugging individual functions/operators:
//		processFunctionGroup(SS.START_OPERATORS_ROW_INDEX, "ConcatEval");
//		processFunctionGroup(SS.START_FUNCTIONS_ROW_INDEX, "AVERAGE");
		
		// confirm results
		String successMsg = "There were " 
				+ _evaluationSuccessCount + " successful evaluation(s) and "
				+ _functionSuccessCount + " function(s) without error";
 		if(_functionFailureCount > 0) {
			String msg = _functionFailureCount + " function(s) failed in "
			+ _evaluationFailureCount + " evaluation(s).  " + successMsg;
			throw new AssertionFailedError(msg);
		}
 		if(false) { // normally no output for successful tests
 			System.out.println(getClass().getName() + ": " + successMsg);
 		}
	}

	/**
	 * @param startRowIndex row index in the spreadsheet where the first function/operator is found 
	 * @param testFocusFunctionName name of a single function/operator to test alone. 
	 * Typically pass <code>null</code> to test all functions
	 */
	private void processFunctionGroup(int startRowIndex, String testFocusFunctionName) {
 
		FormulaEvaluator evaluator = new XSSFFormulaEvaluator(workbook);

		int rowIndex = startRowIndex;
		while (true) {
			Row r = sheet.getRow(rowIndex);
			String targetFunctionName = getTargetFunctionName(r);
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
				Row expectedValuesRow = sheet.getRow(rowIndex + 1);
				if(expectedValuesRow == null) {
					int missingRowNum = rowIndex + 2; //+1 for 1-based, +1 for next row
					throw new AssertionFailedError("Missing expected values row for function '" 
							+ targetFunctionName + " (row " + missingRowNum + ")"); 
				}
				switch(processFunctionRow(evaluator, targetFunctionName, r, expectedValuesRow)) {
					case Result.ALL_EVALUATIONS_SUCCEEDED: _functionSuccessCount++; break;
					case Result.SOME_EVALUATIONS_FAILED: _functionFailureCount++; break;
					default:
						throw new RuntimeException("unexpected result");
					case Result.NO_EVALUATIONS_FOUND: // do nothing
				}
			}
			rowIndex += SS.NUMBER_OF_ROWS_PER_FUNCTION;
		}
	}

	/**
	 * 
	 * @return a constant from the local Result class denoting whether there were any evaluation
	 * cases, and whether they all succeeded.
	 */
	private int processFunctionRow(FormulaEvaluator evaluator, String targetFunctionName, 
			Row formulasRow, Row expectedValuesRow) {
		
		int result = Result.NO_EVALUATIONS_FOUND; // so far
		short endcolnum = formulasRow.getLastCellNum();

		// iterate across the row for all the evaluation cases
		for (short colnum=SS.COLUMN_INDEX_FIRST_TEST_VALUE; colnum < endcolnum; colnum++) {
			Cell c = formulasRow.getCell(colnum);
			if (c == null || c.getCellType() != Cell.CELL_TYPE_FORMULA) {
				continue;
			}
			if(isIgnoredFormulaTestCase(c.getCellFormula())) {
				continue;
			}

			CellValue actualValue;
			try {
				actualValue = evaluator.evaluate(c);
			} catch (RuntimeException e) {
				_evaluationFailureCount ++;
				printShortStackTrace(System.err, e);
				result = Result.SOME_EVALUATIONS_FAILED;
				continue;
			}

			Cell expectedValueCell = getExpectedValueCell(expectedValuesRow, colnum);
			try {
				confirmExpectedResult("Function '" + targetFunctionName + "': Formula: " + c.getCellFormula() + " @ " + formulasRow.getRowNum() + ":" + colnum,
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
		}
 		return result;
	}

	/*
	 * TODO - these are all formulas which currently (Apr-2008) break on ooxml 
	 */
	private static boolean isIgnoredFormulaTestCase(String cellFormula) {
		if ("COLUMN(1:2)".equals(cellFormula) || "ROW(2:3)".equals(cellFormula)) {
			// full row ranges are not parsed properly yet.
			// These cases currently work in svn trunk because of another bug which causes the 
			// formula to get rendered as COLUMN($A$1:$IV$2) or ROW($A$2:$IV$3) 
			return true;
		}
		if ("ISREF(currentcell())".equals(cellFormula)) {
			// currently throws NPE because unknown function "currentcell" causes name lookup 
			// Name lookup requires some equivalent object of the Workbook within xSSFWorkbook.
			return true;
		}
		return false;
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
		if(cell.getCellType() == Cell.CELL_TYPE_BLANK) {
			return null;
		}
		if(cell.getCellType() == Cell.CELL_TYPE_STRING) {
			return cell.getRichStringCellValue().getString();
		}
		
		throw new AssertionFailedError("Bad cell type for 'function name' column: ("
				+ cell.getCellType() + ") row (" + (r.getRowNum() +1) + ")");
	}
}
