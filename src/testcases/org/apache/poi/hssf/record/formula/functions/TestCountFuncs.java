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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.functions.CountUtils.I_MatchPredicate;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator.CellValue;

/**
 * Test cases for COUNT(), COUNTA() COUNTIF(), COUNTBLANK()
 *
 * @author Josh Micich
 */
public final class TestCountFuncs extends TestCase {

	public void testCountA() {

		Eval[] args;

		args = new Eval[] {
			new NumberEval(0),
		};
		confirmCountA(1, args);

		args = new Eval[] {
			new NumberEval(0),
			new NumberEval(0),
			new StringEval(""),
		};
		confirmCountA(3, args);

		args = new Eval[] {
			EvalFactory.createAreaEval("D2:F5", new ValueEval[12]),
		};
		confirmCountA(12, args);

		args = new Eval[] {
			EvalFactory.createAreaEval("D1:F5", new ValueEval[15]),
			EvalFactory.createRefEval("A1"),
			EvalFactory.createAreaEval("A1:G6", new ValueEval[42]),
			new NumberEval(0),
		};
		confirmCountA(59, args);
	}

	public void testCountIf() {

		AreaEval range;
		ValueEval[] values;

		// when criteria is a boolean value
		values = new ValueEval[] {
				new NumberEval(0),
				new StringEval("TRUE"),	// note - does not match boolean TRUE
				BoolEval.TRUE,
				BoolEval.FALSE,
				BoolEval.TRUE,
				BlankEval.INSTANCE,
		};
		range = EvalFactory.createAreaEval("A1:B3", values);
		confirmCountIf(2, range, BoolEval.TRUE);

		// when criteria is numeric
		values = new ValueEval[] {
				new NumberEval(0),
				new StringEval("2"),
				new StringEval("2.001"),
				new NumberEval(2),
				new NumberEval(2),
				BoolEval.TRUE,
		};
		range = EvalFactory.createAreaEval("A1:B3", values);
		confirmCountIf(3, range, new NumberEval(2));
		// note - same results when criteria is a string that parses as the number with the same value
		confirmCountIf(3, range, new StringEval("2.00"));

		if (false) { // not supported yet:
			// when criteria is an expression (starting with a comparison operator)
			confirmCountIf(4, range, new StringEval(">1"));
		}
	}
	/**
	 * special case where the criteria argument is a cell reference
	 */
	public void testCountIfWithCriteriaReference() {

		ValueEval[] values = {
				new NumberEval(22),
				new NumberEval(25),
				new NumberEval(21),
				new NumberEval(25),
				new NumberEval(25),
				new NumberEval(25),
		};
		AreaEval arg0 = EvalFactory.createAreaEval("C1:C6", values);

		ValueEval criteriaArg = EvalFactory.createRefEval("A1", new NumberEval(25));
		Eval[] args=  { arg0, criteriaArg, };

		double actual = NumericFunctionInvoker.invoke(new Countif(), args);
		assertEquals(4, actual, 0D);
	}

	private static void confirmCountA(int expected, Eval[] args) {
		double result = NumericFunctionInvoker.invoke(new Counta(), args);
		assertEquals(expected, result, 0);
	}
	private static void confirmCountIf(int expected, AreaEval range, Eval criteria) {

		Eval[] args = { range, criteria, };
		double result = NumericFunctionInvoker.invoke(new Countif(), args);
		assertEquals(expected, result, 0);
	}

	public void testCountIfEmptyStringCriteria() {
		I_MatchPredicate mp;

		// pred '=' matches blank cell but not empty string
		mp = Countif.createCriteriaPredicate(new StringEval("="));
		confirmPredicate(false, mp, "");
		confirmPredicate(true, mp, null);

		// pred '' matches both blank cell but not empty string
		mp = Countif.createCriteriaPredicate(new StringEval(""));
		confirmPredicate(true, mp, "");
		confirmPredicate(true, mp, null);

		// pred '<>' matches empty string but not blank cell
		mp = Countif.createCriteriaPredicate(new StringEval("<>"));
		confirmPredicate(false, mp, null);
		confirmPredicate(true, mp, "");
	}

	public void testCountifComparisons() {
		I_MatchPredicate mp;

		mp = Countif.createCriteriaPredicate(new StringEval(">5"));
		confirmPredicate(false, mp, 4);
		confirmPredicate(false, mp, 5);
		confirmPredicate(true, mp, 6);

		mp = Countif.createCriteriaPredicate(new StringEval("<=5"));
		confirmPredicate(true, mp, 4);
		confirmPredicate(true, mp, 5);
		confirmPredicate(false, mp, 6);
		confirmPredicate(true, mp, "4.9");
		confirmPredicate(false, mp, "4.9t");
		confirmPredicate(false, mp, "5.1");
		confirmPredicate(false, mp, null);

		mp = Countif.createCriteriaPredicate(new StringEval("=abc"));
		confirmPredicate(true, mp, "abc");

		mp = Countif.createCriteriaPredicate(new StringEval("=42"));
		confirmPredicate(false, mp, 41);
		confirmPredicate(true, mp, 42);
		confirmPredicate(true, mp, "42");

		mp = Countif.createCriteriaPredicate(new StringEval(">abc"));
		confirmPredicate(false, mp, 4);
		confirmPredicate(false, mp, "abc");
		confirmPredicate(true, mp, "abd");

		mp = Countif.createCriteriaPredicate(new StringEval(">4t3"));
		confirmPredicate(false, mp, 4);
		confirmPredicate(false, mp, 500);
		confirmPredicate(true, mp, "500");
		confirmPredicate(true, mp, "4t4");
	}

	public void testWildCards() {
		I_MatchPredicate mp;

		mp = Countif.createCriteriaPredicate(new StringEval("a*b"));
		confirmPredicate(false, mp, "abc");
		confirmPredicate(true, mp, "ab");
		confirmPredicate(true, mp, "axxb");
		confirmPredicate(false, mp, "xab");

		mp = Countif.createCriteriaPredicate(new StringEval("a?b"));
		confirmPredicate(false, mp, "abc");
		confirmPredicate(false, mp, "ab");
		confirmPredicate(false, mp, "axxb");
		confirmPredicate(false, mp, "xab");
		confirmPredicate(true, mp, "axb");

		mp = Countif.createCriteriaPredicate(new StringEval("a~?"));
		confirmPredicate(false, mp, "a~a");
		confirmPredicate(false, mp, "a~?");
		confirmPredicate(true, mp, "a?");

		mp = Countif.createCriteriaPredicate(new StringEval("~*a"));
		confirmPredicate(false, mp, "~aa");
		confirmPredicate(false, mp, "~*a");
		confirmPredicate(true, mp, "*a");

		mp = Countif.createCriteriaPredicate(new StringEval("12?12"));
		confirmPredicate(false, mp, 12812);
		confirmPredicate(true, mp, "12812");
		confirmPredicate(false, mp, "128812");
	}
	public void testNotQuiteWildCards() {
		I_MatchPredicate mp;

		// make sure special reg-ex chars are treated like normal chars
		mp = Countif.createCriteriaPredicate(new StringEval("a.b"));
		confirmPredicate(false, mp, "aab");
		confirmPredicate(true, mp, "a.b");


		mp = Countif.createCriteriaPredicate(new StringEval("a~b"));
		confirmPredicate(false, mp, "ab");
		confirmPredicate(false, mp, "axb");
		confirmPredicate(false, mp, "a~~b");
		confirmPredicate(true, mp, "a~b");

		mp = Countif.createCriteriaPredicate(new StringEval(">a*b"));
		confirmPredicate(false, mp, "a(b");
		confirmPredicate(true, mp, "aab");
		confirmPredicate(false, mp, "a*a");
		confirmPredicate(true, mp, "a*c");
	}

	private static void confirmPredicate(boolean expectedResult, I_MatchPredicate matchPredicate, int value) {
		assertEquals(expectedResult, matchPredicate.matches(new NumberEval(value)));
	}
	private static void confirmPredicate(boolean expectedResult, I_MatchPredicate matchPredicate, String value) {
		Eval ev = value == null ? (Eval)BlankEval.INSTANCE : new StringEval(value);
		assertEquals(expectedResult, matchPredicate.matches(ev));
	}

	public void testCountifFromSpreadsheet() {
		final String FILE_NAME = "countifExamples.xls";
		final int START_ROW_IX = 1;
		final int COL_IX_ACTUAL = 2;
		final int COL_IX_EXPECTED = 3;

		int failureCount = 0;
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook(FILE_NAME);
		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
		int maxRow = sheet.getLastRowNum();
		for (int rowIx=START_ROW_IX; rowIx<maxRow; rowIx++) {
			HSSFRow row = sheet.getRow(rowIx);
			if(row == null) {
				continue;
			}
			HSSFCell cell = row.getCell(COL_IX_ACTUAL);
			CellValue cv = fe.evaluate(cell);
			double actualValue = cv.getNumberValue();
			double expectedValue = row.getCell(COL_IX_EXPECTED).getNumericCellValue();
			if (actualValue != expectedValue) {
				System.err.println("Problem with test case on row " + (rowIx+1) + " "
						+ "Expected = (" + expectedValue + ") Actual=(" + actualValue + ") ");
				failureCount++;
			}
		}

		if (failureCount > 0) {
			throw new AssertionFailedError(failureCount + " countif evaluations failed. See stderr for more details");
		}
	}
}
