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
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.functions.CountUtils.I_MatchPredicate;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellValue;

/**
 * Test cases for COUNT(), COUNTA() COUNTIF(), COUNTBLANK()
 *
 * @author Josh Micich
 */
public final class TestCountFuncs extends TestCase {

	private static final String NULL = null;

	public void testCountBlank() {

		AreaEval range;
		ValueEval[] values;

		values = new ValueEval[] {
				new NumberEval(0),
				new StringEval(""),	// note - does not match blank
				BoolEval.TRUE,
				BoolEval.FALSE,
				ErrorEval.DIV_ZERO,
				BlankEval.instance,
		};
		range = EvalFactory.createAreaEval("A1:B3", values);
		confirmCountBlank(1, range);

		values = new ValueEval[] {
				new NumberEval(0),
				new StringEval(""),	// note - does not match blank
				BlankEval.instance,
				BoolEval.FALSE,
				BoolEval.TRUE,
				BlankEval.instance,
		};
		range = EvalFactory.createAreaEval("A1:B3", values);
		confirmCountBlank(2, range);
	}

	public void testCountA() {

		ValueEval[] args;

		args = new ValueEval[] {
			new NumberEval(0),
		};
		confirmCountA(1, args);

		args = new ValueEval[] {
			new NumberEval(0),
			new NumberEval(0),
			new StringEval(""),
		};
		confirmCountA(3, args);

		args = new ValueEval[] {
			EvalFactory.createAreaEval("D2:F5", new ValueEval[12]),
		};
		confirmCountA(12, args);

		args = new ValueEval[] {
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
				BlankEval.instance,
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

		// when criteria is an expression (starting with a comparison operator)
		confirmCountIf(2, range, new StringEval(">1"));
		// when criteria is an expression (starting with a comparison operator)
		confirmCountIf(2, range, new StringEval(">0.5"));
	}

	public void testCriteriaPredicateNe_Bug46647() {
		I_MatchPredicate mp = Countif.createCriteriaPredicate(new StringEval("<>aa"), 0, 0);
		StringEval seA = new StringEval("aa"); // this should not match the criteria '<>aa'
		StringEval seB = new StringEval("bb"); // this should match
		if (mp.matches(seA) && !mp.matches(seB)) {
			throw new AssertionFailedError("Identified bug 46647");
		}
		assertFalse(mp.matches(seA));
		assertTrue(mp.matches(seB));

		// general tests for not-equal (<>) operator
		AreaEval range;
		ValueEval[] values;

		values = new ValueEval[] {
				new StringEval("aa"),
				new StringEval("def"),
				new StringEval("aa"),
				new StringEval("ghi"),
				new StringEval("aa"),
				new StringEval("aa"),
		};

		range = EvalFactory.createAreaEval("A1:A6", values);
		confirmCountIf(2, range, new StringEval("<>aa"));

		values = new ValueEval[] {
				new StringEval("ab"),
				new StringEval("aabb"),
				new StringEval("aa"), // match
				new StringEval("abb"),
				new StringEval("aab"),
				new StringEval("ba"), // match
		};

		range = EvalFactory.createAreaEval("A1:A6", values);
		confirmCountIf(2, range, new StringEval("<>a*b"));


		values = new ValueEval[] {
				new NumberEval(222),
				new NumberEval(222),
				new NumberEval(111),
				new StringEval("aa"),
				new StringEval("111"),
		};

		range = EvalFactory.createAreaEval("A1:A5", values);
		confirmCountIf(4, range, new StringEval("<>111"));
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
		ValueEval[] args=  { arg0, criteriaArg, };

		double actual = NumericFunctionInvoker.invoke(new Countif(), args);
		assertEquals(4, actual, 0D);
	}

	private static void confirmCountA(int expected, ValueEval[] args) {
		double result = NumericFunctionInvoker.invoke(new Counta(), args);
		assertEquals(expected, result, 0);
	}
	private static void confirmCountIf(int expected, AreaEval range, ValueEval criteria) {

		ValueEval[] args = { range, criteria, };
		double result = NumericFunctionInvoker.invoke(new Countif(), args);
		assertEquals(expected, result, 0);
	}
	private static void confirmCountBlank(int expected, AreaEval range) {

		ValueEval[] args = { range };
		double result = NumericFunctionInvoker.invoke(new Countblank(), args);
		assertEquals(expected, result, 0);
	}

	private static I_MatchPredicate createCriteriaPredicate(ValueEval ev) {
		return Countif.createCriteriaPredicate(ev, 0, 0);
	}

	/**
	 * the criteria arg is mostly handled by {@link OperandResolver#getSingleValue(Eval, int, short)}
	 */
	public void testCountifAreaCriteria() {
		int srcColIx = 2; // anything but column A

		ValueEval v0 = new NumberEval(2.0);
		ValueEval v1 = new StringEval("abc");
		ValueEval v2 = ErrorEval.DIV_ZERO;

		AreaEval ev = EvalFactory.createAreaEval("A10:A12", new ValueEval[] { v0, v1, v2, });

		I_MatchPredicate mp;
		mp = Countif.createCriteriaPredicate(ev, 9, srcColIx);
		confirmPredicate(true, mp, srcColIx);
		confirmPredicate(false, mp, "abc");
		confirmPredicate(false, mp, ErrorEval.DIV_ZERO);

		mp = Countif.createCriteriaPredicate(ev, 10, srcColIx);
		confirmPredicate(false, mp, srcColIx);
		confirmPredicate(true, mp, "abc");
		confirmPredicate(false, mp, ErrorEval.DIV_ZERO);

		mp = Countif.createCriteriaPredicate(ev, 11, srcColIx);
		confirmPredicate(false, mp, srcColIx);
		confirmPredicate(false, mp, "abc");
		confirmPredicate(true, mp, ErrorEval.DIV_ZERO);
		confirmPredicate(false, mp, ErrorEval.VALUE_INVALID);

		// tricky: indexing outside of A10:A12
		// even this #VALUE! error gets used by COUNTIF as valid criteria
		mp = Countif.createCriteriaPredicate(ev, 12, srcColIx);
		confirmPredicate(false, mp, srcColIx);
		confirmPredicate(false, mp, "abc");
		confirmPredicate(false, mp, ErrorEval.DIV_ZERO);
		confirmPredicate(true, mp, ErrorEval.VALUE_INVALID);
	}

	public void testCountifEmptyStringCriteria() {
		I_MatchPredicate mp;

		// pred '=' matches blank cell but not empty string
		mp = createCriteriaPredicate(new StringEval("="));
		confirmPredicate(false, mp, "");
		confirmPredicate(true, mp, NULL);

		// pred '' matches both blank cell but not empty string
		mp = createCriteriaPredicate(new StringEval(""));
		confirmPredicate(true, mp, "");
		confirmPredicate(true, mp, NULL);

		// pred '<>' matches empty string but not blank cell
		mp = createCriteriaPredicate(new StringEval("<>"));
		confirmPredicate(false, mp, NULL);
		confirmPredicate(true, mp, "");
	}

	public void testCountifComparisons() {
		I_MatchPredicate mp;

		mp = createCriteriaPredicate(new StringEval(">5"));
		confirmPredicate(false, mp, 4);
		confirmPredicate(false, mp, 5);
		confirmPredicate(true, mp, 6);

		mp = createCriteriaPredicate(new StringEval("<=5"));
		confirmPredicate(true, mp, 4);
		confirmPredicate(true, mp, 5);
		confirmPredicate(false, mp, 6);
		confirmPredicate(false, mp, "4.9");
		confirmPredicate(false, mp, "4.9t");
		confirmPredicate(false, mp, "5.1");
		confirmPredicate(false, mp, NULL);

		mp = createCriteriaPredicate(new StringEval("=abc"));
		confirmPredicate(true, mp, "abc");

		mp = createCriteriaPredicate(new StringEval("=42"));
		confirmPredicate(false, mp, 41);
		confirmPredicate(true, mp, 42);
		confirmPredicate(true, mp, "42");

		mp = createCriteriaPredicate(new StringEval(">abc"));
		confirmPredicate(false, mp, 4);
		confirmPredicate(false, mp, "abc");
		confirmPredicate(true, mp, "abd");

		mp = createCriteriaPredicate(new StringEval(">4t3"));
		confirmPredicate(false, mp, 4);
		confirmPredicate(false, mp, 500);
		confirmPredicate(true, mp, "500");
		confirmPredicate(true, mp, "4t4");
	}

	/**
	 * the criteria arg value can be an error code (the error does not
	 * propagate to the COUNTIF result).
	 */
	public void testCountifErrorCriteria() {
		I_MatchPredicate mp;

		mp = createCriteriaPredicate(new StringEval("#REF!"));
		confirmPredicate(false, mp, 4);
		confirmPredicate(false, mp, "#REF!");
		confirmPredicate(true, mp, ErrorEval.REF_INVALID);

		mp = createCriteriaPredicate(new StringEval("<#VALUE!"));
		confirmPredicate(false, mp, 4);
		confirmPredicate(false, mp, "#DIV/0!");
		confirmPredicate(false, mp, "#REF!");
		confirmPredicate(true, mp, ErrorEval.DIV_ZERO);
		confirmPredicate(false, mp, ErrorEval.REF_INVALID);

		// not quite an error literal, should be treated as plain text
		mp = createCriteriaPredicate(new StringEval("<=#REF!a"));
		confirmPredicate(false, mp, 4);
		confirmPredicate(true, mp, "#DIV/0!");
		confirmPredicate(true, mp, "#REF!");
		confirmPredicate(false, mp, ErrorEval.DIV_ZERO);
		confirmPredicate(false, mp, ErrorEval.REF_INVALID);
	}

	public void testWildCards() {
		I_MatchPredicate mp;

		mp = createCriteriaPredicate(new StringEval("a*b"));
		confirmPredicate(false, mp, "abc");
		confirmPredicate(true, mp, "ab");
		confirmPredicate(true, mp, "axxb");
		confirmPredicate(false, mp, "xab");

		mp = createCriteriaPredicate(new StringEval("a?b"));
		confirmPredicate(false, mp, "abc");
		confirmPredicate(false, mp, "ab");
		confirmPredicate(false, mp, "axxb");
		confirmPredicate(false, mp, "xab");
		confirmPredicate(true, mp, "axb");

		mp = createCriteriaPredicate(new StringEval("a~?"));
		confirmPredicate(false, mp, "a~a");
		confirmPredicate(false, mp, "a~?");
		confirmPredicate(true, mp, "a?");

		mp = createCriteriaPredicate(new StringEval("~*a"));
		confirmPredicate(false, mp, "~aa");
		confirmPredicate(false, mp, "~*a");
		confirmPredicate(true, mp, "*a");

		mp = createCriteriaPredicate(new StringEval("12?12"));
		confirmPredicate(false, mp, 12812);
		confirmPredicate(true, mp, "12812");
		confirmPredicate(false, mp, "128812");
	}
	public void testNotQuiteWildCards() {
		I_MatchPredicate mp;

		// make sure special reg-ex chars are treated like normal chars
		mp = createCriteriaPredicate(new StringEval("a.b"));
		confirmPredicate(false, mp, "aab");
		confirmPredicate(true, mp, "a.b");


		mp = createCriteriaPredicate(new StringEval("a~b"));
		confirmPredicate(false, mp, "ab");
		confirmPredicate(false, mp, "axb");
		confirmPredicate(false, mp, "a~~b");
		confirmPredicate(true, mp, "a~b");

		mp = createCriteriaPredicate(new StringEval(">a*b"));
		confirmPredicate(false, mp, "a(b");
		confirmPredicate(true, mp, "aab");
		confirmPredicate(false, mp, "a*a");
		confirmPredicate(true, mp, "a*c");
	}

	private static void confirmPredicate(boolean expectedResult, I_MatchPredicate matchPredicate, int value) {
		assertEquals(expectedResult, matchPredicate.matches(new NumberEval(value)));
	}
	private static void confirmPredicate(boolean expectedResult, I_MatchPredicate matchPredicate, String value) {
		ValueEval ev = value == null ? BlankEval.instance : new StringEval(value);
		assertEquals(expectedResult, matchPredicate.matches(ev));
	}
	private static void confirmPredicate(boolean expectedResult, I_MatchPredicate matchPredicate, ErrorEval value) {
		assertEquals(expectedResult, matchPredicate.matches(value));
	}

	public void testCountifFromSpreadsheet() {
		testCountFunctionFromSpreadsheet("countifExamples.xls", 1, 2, 3, "countif");
	}

	public void testCountBlankFromSpreadsheet() {
		testCountFunctionFromSpreadsheet("countblankExamples.xls", 1, 3, 4, "countblank");
	}

	private static void testCountFunctionFromSpreadsheet(String FILE_NAME, int START_ROW_IX, int COL_IX_ACTUAL, int COL_IX_EXPECTED, String functionName) {

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
			throw new AssertionFailedError(failureCount + " " + functionName
					+ " evaluations failed. See stderr for more details");
		}
	}
}
