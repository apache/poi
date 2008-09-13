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

package org.apache.poi.hssf.record.formula.atp;

import java.io.PrintStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Tests YearFracCalculator using test-cases listed in a sample spreadsheet
 * 
 * @author Josh Micich
 */
public final class TestYearFracCalculatorFromSpreadsheet extends TestCase {
	
	private static final class SS {

		public static final int BASIS_COLUMN = 1; // "B"
		public static final int START_YEAR_COLUMN = 2; // "C"
		public static final int END_YEAR_COLUMN = 5; // "F"
		public static final int YEARFRAC_FORMULA_COLUMN = 11; // "L"
		public static final int EXPECTED_RESULT_COLUMN = 13; // "N"
	}

	public void testAll() {
		
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("yearfracExamples.xls");
		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFFormulaEvaluator formulaEvaluator = new HSSFFormulaEvaluator(wb);
		int nSuccess = 0;
		int nFailures = 0;
		int nUnexpectedErrors = 0;
		Iterator rowIterator = sheet.rowIterator();
		while(rowIterator.hasNext()) {
			HSSFRow row = (HSSFRow) rowIterator.next();
			
			HSSFCell cell = row.getCell(SS.YEARFRAC_FORMULA_COLUMN);
			if (cell == null || cell.getCellType() != HSSFCell.CELL_TYPE_FORMULA) {
				continue;
			}
			try {
				processRow(row, cell, formulaEvaluator);
				nSuccess++;
			} catch (RuntimeException e) {
				nUnexpectedErrors ++;
				printShortStackTrace(System.err, e);
			} catch (AssertionFailedError e) {
				nFailures ++;
				printShortStackTrace(System.err, e);
			}
		}
		if (nUnexpectedErrors + nFailures > 0) {
			String msg = nFailures + " failures(s) and " + nUnexpectedErrors 
				+ " unexpected errors(s) occurred. See stderr for details";
			throw new AssertionFailedError(msg);
		}
		if (nSuccess < 1) {
			throw new RuntimeException("No test sample cases found");
		}
	}
	
	private static void processRow(HSSFRow row, HSSFCell cell, HSSFFormulaEvaluator formulaEvaluator) {
		
		double startDate = makeDate(row, SS.START_YEAR_COLUMN);
		double endDate = makeDate(row, SS.END_YEAR_COLUMN);
		
		int basis = getIntCell(row, SS.BASIS_COLUMN);
		
		double expectedValue = getDoubleCell(row, SS.EXPECTED_RESULT_COLUMN);
		
		double actualValue;
		try {
			actualValue = YearFracCalculator.calculate(startDate, endDate, basis);
		} catch (EvaluationException e) {
			throw new RuntimeException(e);
		}
		if (expectedValue != actualValue) {
			throw new ComparisonFailure("Direct calculate failed - row " + (row.getRowNum()+1), 
					String.valueOf(expectedValue), String.valueOf(actualValue));
		}
		actualValue = formulaEvaluator.evaluate(cell).getNumberValue();
		if (expectedValue != actualValue) {
			throw new ComparisonFailure("Formula evaluate failed - row " + (row.getRowNum()+1), 
					String.valueOf(expectedValue), String.valueOf(actualValue));
		}
	}

	private static double makeDate(HSSFRow row, int yearColumn) {
		int year = getIntCell(row, yearColumn + 0);
		int month = getIntCell(row, yearColumn + 1);
		int day = getIntCell(row, yearColumn + 2);
		Calendar c = new GregorianCalendar(year, month-1, day, 0, 0, 0);
		c.set(Calendar.MILLISECOND, 0);
		return HSSFDateUtil.getExcelDate(c.getTime());
	}

	private static int getIntCell(HSSFRow row, int colIx) {
		double dVal = getDoubleCell(row, colIx);
		if (Math.floor(dVal) != dVal) {
			throw new RuntimeException("Non integer value (" + dVal 
					+ ") cell found at column " + (char)('A' + colIx));
		}
		return (int)dVal;
	}

	private static double getDoubleCell(HSSFRow row, int colIx) {
		HSSFCell cell = row.getCell(colIx);
		if (cell == null) {
			throw new RuntimeException("No cell found at column " + colIx);
		}
		double dVal = cell.getNumericCellValue();
		return dVal;
	}

	/**
	 * Useful to keep output concise when expecting many failures to be reported by this test case
	 * TODO - refactor duplicates in other Test~FromSpreadsheet classes
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
