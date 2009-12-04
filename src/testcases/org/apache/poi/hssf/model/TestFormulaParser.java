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

package org.apache.poi.hssf.model;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.constant.ErrorConstant;
import org.apache.poi.hssf.record.formula.AbstractFunctionPtg;
import org.apache.poi.hssf.record.formula.AddPtg;
import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.AreaI;
import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.hssf.record.formula.AreaPtgBase;
import org.apache.poi.hssf.record.formula.ArrayPtg;
import org.apache.poi.hssf.record.formula.AttrPtg;
import org.apache.poi.hssf.record.formula.BoolPtg;
import org.apache.poi.hssf.record.formula.ConcatPtg;
import org.apache.poi.hssf.record.formula.DividePtg;
import org.apache.poi.hssf.record.formula.EqualPtg;
import org.apache.poi.hssf.record.formula.ErrPtg;
import org.apache.poi.hssf.record.formula.FuncPtg;
import org.apache.poi.hssf.record.formula.FuncVarPtg;
import org.apache.poi.hssf.record.formula.IntPtg;
import org.apache.poi.hssf.record.formula.MemAreaPtg;
import org.apache.poi.hssf.record.formula.MemFuncPtg;
import org.apache.poi.hssf.record.formula.MissingArgPtg;
import org.apache.poi.hssf.record.formula.MultiplyPtg;
import org.apache.poi.hssf.record.formula.NamePtg;
import org.apache.poi.hssf.record.formula.NumberPtg;
import org.apache.poi.hssf.record.formula.ParenthesisPtg;
import org.apache.poi.hssf.record.formula.PercentPtg;
import org.apache.poi.hssf.record.formula.PowerPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.RangePtg;
import org.apache.poi.hssf.record.formula.Ref3DPtg;
import org.apache.poi.hssf.record.formula.RefPtg;
import org.apache.poi.hssf.record.formula.StringPtg;
import org.apache.poi.hssf.record.formula.SubtractPtg;
import org.apache.poi.hssf.record.formula.UnaryMinusPtg;
import org.apache.poi.hssf.record.formula.UnaryPlusPtg;
import org.apache.poi.hssf.record.formula.UnionPtg;
import org.apache.poi.hssf.usermodel.FormulaExtractor;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFErrorConstants;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.TestHSSFName;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.usermodel.BaseTestBugzillaIssues;
import org.apache.poi.ss.usermodel.Name;

/**
 * Test the low level formula parser functionality. High level tests are to
 * be done via usermodel/HSSFCell.setFormulaValue().
 */
public final class TestFormulaParser extends TestCase {

	/**
	 * @return parsed token array already confirmed not <code>null</code>
	 */
	/* package */ static Ptg[] parseFormula(String formula) {
		Ptg[] result = HSSFFormulaParser.parse(formula, (HSSFWorkbook)null);
		assertNotNull("Ptg array should not be null", result);
		return result;
	}
	private static String toFormulaString(Ptg[] ptgs) {
		return HSSFFormulaParser.toFormulaString((HSSFWorkbook)null, ptgs);
	}

	public void testSimpleFormula() {
		confirmTokenClasses("2+2",IntPtg.class, IntPtg.class, AddPtg.class);
	}

	public void testFormulaWithSpace1() {
		confirmTokenClasses(" 2 + 2 ",IntPtg.class, IntPtg.class, AddPtg.class);
	}

	public void testFormulaWithSpace2() {
		Ptg[] ptgs = parseFormula("2+ sum( 3 , 4) ");
		assertEquals(5, ptgs.length);
	}

	public void testFormulaWithSpaceNRef() {
		Ptg[] ptgs = parseFormula("sum( A2:A3 )");
		assertEquals(2, ptgs.length);
	}

	public void testFormulaWithString() {
		Ptg[] ptgs = parseFormula("\"hello\" & \"world\" ");
		assertEquals(3, ptgs.length);
	}

	public void testTRUE() {
		Ptg[] ptgs = parseFormula("TRUE");
		assertEquals(1, ptgs.length);
		BoolPtg flag  = (BoolPtg) ptgs[0];
		assertEquals(true, flag.getValue());
	}

	public void testSumIf() {
		Ptg[] ptgs = parseFormula("SUMIF(A1:A5,\">4000\",B1:B5)");
		assertEquals(4, ptgs.length);
	}

	/**
	 * Bug Reported by xt-jens.riis@nokia.com (Jens Riis)
	 * Refers to Bug <a href="http://issues.apache.org/bugzilla/show_bug.cgi?id=17582">#17582</a>
	 *
	 */
	public void testNonAlphaFormula() {
		Ptg[] ptgs = parseFormula("\"TOTAL[\"&F3&\"]\"");
		confirmTokenClasses(ptgs, StringPtg.class, RefPtg.class, ConcatPtg.class, StringPtg.class, ConcatPtg.class);
		assertEquals("TOTAL[", ((StringPtg)ptgs[0]).getValue());
	}

	public void testMacroFunction() {
		// testNames.xls contains a VB function called 'myFunc'
		HSSFWorkbook w = HSSFTestDataSamples.openSampleWorkbook("testNames.xls");
		HSSFEvaluationWorkbook book = HSSFEvaluationWorkbook.create(w);

		Ptg[] ptg = HSSFFormulaParser.parse("myFunc()", w);
		// myFunc() actually takes 1 parameter.  Don't know if POI will ever be able to detect this problem

		// the name gets encoded as the first arg
		NamePtg tname = (NamePtg) ptg[0];
		assertEquals("myFunc", tname.toFormulaString(book));

		AbstractFunctionPtg tfunc = (AbstractFunctionPtg) ptg[1];
		assertTrue(tfunc.isExternalFunction());
	}

	public void testEmbeddedSlash() {
		confirmTokenClasses("HYPERLINK(\"http://www.jakarta.org\",\"Jakarta\")",
						StringPtg.class, StringPtg.class, FuncVarPtg.class);
	}

	public void testConcatenate() {
		confirmTokenClasses("CONCATENATE(\"first\",\"second\")",
				StringPtg.class, StringPtg.class, FuncVarPtg.class);
	}

	public void testWorksheetReferences() {
		HSSFWorkbook wb = new HSSFWorkbook();

		wb.createSheet("NoQuotesNeeded");
		wb.createSheet("Quotes Needed Here &#$@");

		HSSFSheet sheet = wb.createSheet("Test");
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell;

		cell = row.createCell(0);
		cell.setCellFormula("NoQuotesNeeded!A1");

		cell = row.createCell(1);
		cell.setCellFormula("'Quotes Needed Here &#$@'!A1");
	}

	public void testUnaryMinus() {
		confirmTokenClasses("-A1", RefPtg.class, UnaryMinusPtg.class);
	}

	public void testUnaryPlus() {
		confirmTokenClasses("+A1", RefPtg.class, UnaryPlusPtg.class);
	}

	/**
	 * There may be multiple ways to encode an expression involving {@link UnaryPlusPtg}
	 * or {@link UnaryMinusPtg}.  These may be perfectly equivalent from a formula
	 * evaluation perspective, or formula rendering.  However, differences in the way
	 * POI encodes formulas may cause unnecessary confusion.  These non-critical tests
	 * check that POI follows the same encoding rules as Excel.
	 */
	public void testExactEncodingOfUnaryPlusAndMinus() {
		// as tested in Excel:
		confirmUnary("-3", -3, NumberPtg.class);
		confirmUnary("--4", -4, NumberPtg.class, UnaryMinusPtg.class);
		confirmUnary("+++5", 5, IntPtg.class, UnaryPlusPtg.class, UnaryPlusPtg.class);
		confirmUnary("++-6", -6, NumberPtg.class, UnaryPlusPtg.class, UnaryPlusPtg.class);

		// Spaces muck things up a bit.  It would be clearer why the following cases are
		// reasonable if POI encoded tAttrSpace in the right places.
		// Otherwise these differences look capricious.
		confirmUnary("+ 12", 12, IntPtg.class, UnaryPlusPtg.class);
		confirmUnary("- 13", 13, IntPtg.class, UnaryMinusPtg.class);
	}

	private static void confirmUnary(String formulaText, double val, Class<?>...expectedTokenTypes) {
		Ptg[] ptgs = parseFormula(formulaText);
		confirmTokenClasses(ptgs, expectedTokenTypes);
		Ptg ptg0 = ptgs[0];
		if (ptg0 instanceof IntPtg) {
			IntPtg intPtg = (IntPtg) ptg0;
			assertEquals((int)val, intPtg.getValue());
		} else if (ptg0 instanceof NumberPtg) {
			NumberPtg numberPtg = (NumberPtg) ptg0;
			assertEquals(val, numberPtg.getValue(), 0.0);
		} else {
			fail("bad ptg0 " + ptg0);
		}
	}


	public void testLeadingSpaceInString() {
		String value = "  hi  ";
		Ptg[] ptgs = parseFormula("\"" + value + "\"");
		confirmTokenClasses(ptgs, StringPtg.class);
		assertTrue("ptg0 contains exact value", ((StringPtg)ptgs[0]).getValue().equals(value));
	}

	public void testLookupAndMatchFunctionArgs() {
		Ptg[] ptgs = parseFormula("lookup(A1, A3:A52, B3:B52)");
		confirmTokenClasses(ptgs, RefPtg.class, AreaPtg.class, AreaPtg.class, FuncVarPtg.class);
		assertTrue("ptg0 has Value class", ptgs[0].getPtgClass() == Ptg.CLASS_VALUE);

		ptgs = parseFormula("match(A1, A3:A52)");
		confirmTokenClasses(ptgs, RefPtg.class, AreaPtg.class, FuncVarPtg.class);
		assertTrue("ptg0 has Value class", ptgs[0].getPtgClass() == Ptg.CLASS_VALUE);
	}

	/** bug 33160*/
	public void testLargeInt() {
		confirmTokenClasses("40", IntPtg.class);
		confirmTokenClasses("40000", IntPtg.class);
	}

	/** bug 33160, testcase by Amol Deshmukh*/
	public void testSimpleLongFormula() {
		confirmTokenClasses("40000/2", IntPtg.class, IntPtg.class, DividePtg.class);
	}

	/** bug 35027, underscore in sheet name */
	public void testUnderscore() {
		HSSFWorkbook wb = new HSSFWorkbook();

		wb.createSheet("Cash_Flow");

		HSSFSheet sheet = wb.createSheet("Test");
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell;

		cell = row.createCell(0);
		cell.setCellFormula("Cash_Flow!A1");
	}

	// bug 38396 : Formula with exponential numbers not parsed correctly.
	public void testExponentialParsing() {
		confirmTokenClasses("1.3E21/2",  NumberPtg.class, IntPtg.class, DividePtg.class);
		confirmTokenClasses("1322E21/2", NumberPtg.class, IntPtg.class, DividePtg.class);
		confirmTokenClasses("1.3E1/2",   NumberPtg.class, IntPtg.class, DividePtg.class);
	}

	public void testExponentialInSheet() {
		HSSFWorkbook wb = new HSSFWorkbook();

		wb.createSheet("Cash_Flow");

		HSSFSheet sheet = wb.createSheet("Test");
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell = row.createCell(0);
		String formula = null;

		cell.setCellFormula("1.3E21/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "1.3E+21/3", formula);

		cell.setCellFormula("-1.3E21/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "-1.3E+21/3", formula);

		cell.setCellFormula("1322E21/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "1.322E+24/3", formula);

		cell.setCellFormula("-1322E21/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "-1.322E+24/3", formula);

		cell.setCellFormula("1.3E1/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "13/3", formula);

		cell.setCellFormula("-1.3E1/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "-13/3", formula);

		cell.setCellFormula("1.3E-4/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "0.00013/3", formula);

		cell.setCellFormula("-1.3E-4/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "-0.00013/3", formula);

		cell.setCellFormula("13E-15/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "0.000000000000013/3", formula);

		cell.setCellFormula("-13E-15/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "-0.000000000000013/3", formula);

		cell.setCellFormula("1.3E3/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "1300/3", formula);

		cell.setCellFormula("-1.3E3/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "-1300/3", formula);

		cell.setCellFormula("1300000000000000/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "1300000000000000/3", formula);

		cell.setCellFormula("-1300000000000000/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "-1300000000000000/3", formula);

		cell.setCellFormula("-10E-1/3.1E2*4E3/3E4");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "-1/310*4000/30000", formula);
	}

	public void testNumbers() {
		HSSFWorkbook wb = new HSSFWorkbook();

		wb.createSheet("Cash_Flow");

		HSSFSheet sheet = wb.createSheet("Test");
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell = row.createCell(0);
		String formula = null;

		// starts from decimal point

		cell.setCellFormula(".1");
		formula = cell.getCellFormula();
		assertEquals("0.1", formula);

		cell.setCellFormula("+.1");
		formula = cell.getCellFormula();
		assertEquals("0.1", formula);

		cell.setCellFormula("-.1");
		formula = cell.getCellFormula();
		assertEquals("-0.1", formula);

		// has exponent

		cell.setCellFormula("10E1");
		formula = cell.getCellFormula();
		assertEquals("100", formula);

		cell.setCellFormula("10E+1");
		formula = cell.getCellFormula();
		assertEquals("100", formula);

		cell.setCellFormula("10E-1");
		formula = cell.getCellFormula();
		assertEquals("1", formula);
	}

	public void testRanges() {
		HSSFWorkbook wb = new HSSFWorkbook();

		wb.createSheet("Cash_Flow");

		HSSFSheet sheet = wb.createSheet("Test");
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell = row.createCell(0);
		String formula = null;

		cell.setCellFormula("A1.A2");
		formula = cell.getCellFormula();
		assertEquals("A1:A2", formula);

		cell.setCellFormula("A1..A2");
		formula = cell.getCellFormula();
		assertEquals("A1:A2", formula);

		cell.setCellFormula("A1...A2");
		formula = cell.getCellFormula();
		assertEquals("A1:A2", formula);
	}

	/**
	 * Test for bug observable at svn revision 618865 (5-Feb-2008)<br/>
	 * a formula consisting of a single no-arg function got rendered without the function braces
	 */
	public void testToFormulaStringZeroArgFunction() {
		HSSFWorkbook book = new HSSFWorkbook();

		Ptg[] ptgs = {
				FuncPtg.create(10),
		};
		assertEquals("NA()", HSSFFormulaParser.toFormulaString(book, ptgs));
	}

	public void testPercent() {

		confirmTokenClasses("5%", IntPtg.class, PercentPtg.class);
		// spaces OK
		confirmTokenClasses(" 250 % ", IntPtg.class, PercentPtg.class);
		// double percent OK
		confirmTokenClasses("12345.678%%", NumberPtg.class, PercentPtg.class, PercentPtg.class);

		// percent of a bracketed expression
		confirmTokenClasses("(A1+35)%*B1%", RefPtg.class, IntPtg.class, AddPtg.class, ParenthesisPtg.class,
				PercentPtg.class, RefPtg.class, PercentPtg.class, MultiplyPtg.class);

		// percent of a text quantity
		confirmTokenClasses("\"8.75\"%", StringPtg.class, PercentPtg.class);

		// percent to the power of
		confirmTokenClasses("50%^3", IntPtg.class, PercentPtg.class, IntPtg.class, PowerPtg.class);

		// things that parse OK but would *evaluate* to an error
		confirmTokenClasses("\"abc\"%", StringPtg.class, PercentPtg.class);
		confirmTokenClasses("#N/A%", ErrPtg.class, PercentPtg.class);
	}

	/**
	 * Tests combinations of various operators in the absence of brackets
	 */
	public void testPrecedenceAndAssociativity() {

		// TRUE=TRUE=2=2  evaluates to FALSE
		confirmTokenClasses("TRUE=TRUE=2=2", BoolPtg.class, BoolPtg.class, EqualPtg.class,
				IntPtg.class, EqualPtg.class, IntPtg.class, EqualPtg.class);

		//  2^3^2	evaluates to 64 not 512
		confirmTokenClasses("2^3^2", IntPtg.class, IntPtg.class, PowerPtg.class,
				IntPtg.class, PowerPtg.class);

		// "abc" & 2 + 3 & "def"   evaluates to "abc5def"
		confirmTokenClasses("\"abc\"&2+3&\"def\"", StringPtg.class, IntPtg.class, IntPtg.class,
				AddPtg.class, ConcatPtg.class, StringPtg.class, ConcatPtg.class);

		//  (1 / 2) - (3 * 4)
		confirmTokenClasses("1/2-3*4", IntPtg.class, IntPtg.class, DividePtg.class,
				IntPtg.class, IntPtg.class, MultiplyPtg.class, SubtractPtg.class);

		// 2 * (2^2)
		// NOT: (2 *2) ^ 2 -> int int multiply int power
		confirmTokenClasses("2*2^2", IntPtg.class, IntPtg.class, IntPtg.class, PowerPtg.class, MultiplyPtg.class);

		//  2^200% -> 2 not 1.6E58
		confirmTokenClasses("2^200%", IntPtg.class, IntPtg.class, PercentPtg.class, PowerPtg.class);
	}

	/* package */ static Ptg[] confirmTokenClasses(String formula, Class<?>...expectedClasses) {
		Ptg[] ptgs = parseFormula(formula);
		confirmTokenClasses(ptgs, expectedClasses);
		return ptgs;
	}

	private static void confirmTokenClasses(Ptg[] ptgs, Class<?>...expectedClasses) {
		assertEquals(expectedClasses.length, ptgs.length);
		for (int i = 0; i < expectedClasses.length; i++) {
			if(expectedClasses[i] != ptgs[i].getClass()) {
				fail("difference at token[" + i + "]: expected ("
					+ expectedClasses[i].getName() + ") but got ("
					+ ptgs[i].getClass().getName() + ")");
			}
		}
	}

	public void testPower() {
		confirmTokenClasses("2^5", IntPtg.class, IntPtg.class, PowerPtg.class);
	}

	private static Ptg parseSingleToken(String formula, Class<? extends Ptg> ptgClass) {
		Ptg[] ptgs = parseFormula(formula);
		assertEquals(1, ptgs.length);
		Ptg result = ptgs[0];
		assertEquals(ptgClass, result.getClass());
		return result;
	}

	public void testParseNumber() {
		IntPtg ip;

		// bug 33160
		ip = (IntPtg) parseSingleToken("40", IntPtg.class);
		assertEquals(40, ip.getValue());
		ip = (IntPtg) parseSingleToken("40000", IntPtg.class);
		assertEquals(40000, ip.getValue());

		// check the upper edge of the IntPtg range:
		ip = (IntPtg) parseSingleToken("65535", IntPtg.class);
		assertEquals(65535, ip.getValue());
		NumberPtg np = (NumberPtg) parseSingleToken("65536", NumberPtg.class);
		assertEquals(65536, np.getValue(), 0);

		np = (NumberPtg) parseSingleToken("65534.6", NumberPtg.class);
		assertEquals(65534.6, np.getValue(), 0);
	}

	public void testMissingArgs() {

		confirmTokenClasses("if(A1, ,C1)",
				RefPtg.class,
				AttrPtg.class, // tAttrIf
				MissingArgPtg.class,
				AttrPtg.class, // tAttrSkip
				RefPtg.class,
				AttrPtg.class, // tAttrSkip
				FuncVarPtg.class
		);

		confirmTokenClasses("counta( , A1:B2, )", MissingArgPtg.class, AreaPtg.class, MissingArgPtg.class,
				FuncVarPtg.class);
	}

	public void testParseErrorLiterals() {

		confirmParseErrorLiteral(ErrPtg.NULL_INTERSECTION, "#NULL!");
		confirmParseErrorLiteral(ErrPtg.DIV_ZERO, "#DIV/0!");
		confirmParseErrorLiteral(ErrPtg.VALUE_INVALID, "#VALUE!");
		confirmParseErrorLiteral(ErrPtg.REF_INVALID, "#REF!");
		confirmParseErrorLiteral(ErrPtg.NAME_INVALID, "#NAME?");
		confirmParseErrorLiteral(ErrPtg.NUM_ERROR, "#NUM!");
		confirmParseErrorLiteral(ErrPtg.N_A, "#N/A");
		parseFormula("HLOOKUP(F7,#REF!,G7,#REF!)");
	}

	private static void confirmParseErrorLiteral(ErrPtg expectedToken, String formula) {
		assertEquals(expectedToken, parseSingleToken(formula, ErrPtg.class));
	}

	/**
	 * To aid readability the parameters have been encoded with single quotes instead of double
	 * quotes.  This method converts single quotes to double quotes before performing the parse
	 * and result check.
	 */
	private static void confirmStringParse(String singleQuotedValue) {
		// formula: internal quotes become double double, surround with double quotes
		String formula = '"' + singleQuotedValue.replaceAll("'", "\"\"") + '"';
		String expectedValue = singleQuotedValue.replace('\'', '"');

		StringPtg sp = (StringPtg) parseSingleToken(formula, StringPtg.class);
		assertEquals(expectedValue, sp.getValue());
	}
	public void testParseStringLiterals_bug28754() {

		StringPtg sp;
		try {
			sp = (StringPtg) parseSingleToken("\"test\"\"ing\"", StringPtg.class);
		} catch (RuntimeException e) {
			if(e.getMessage().startsWith("Cannot Parse")) {
				throw new AssertionFailedError("Identified bug 28754a");
			}
			throw e;
		}
		assertEquals("test\"ing", sp.getValue());

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		wb.setSheetName(0, "Sheet1");

		HSSFRow row = sheet.createRow(0);
		HSSFCell cell = row.createCell(0);
		cell.setCellFormula("right(\"test\"\"ing\", 3)");
		String actualCellFormula = cell.getCellFormula();
		if("RIGHT(\"test\"ing\",3)".equals(actualCellFormula)) {
			throw new AssertionFailedError("Identified bug 28754b");
		}
		assertEquals("RIGHT(\"test\"\"ing\",3)", actualCellFormula);
	}

	public void testParseStringLiterals() {
		confirmStringParse("goto considered harmful");

		confirmStringParse("goto 'considered' harmful");

		confirmStringParse("");
		confirmStringParse("'");
		confirmStringParse("''");
		confirmStringParse("' '");
		confirmStringParse(" ' ");
	}

	public void testParseSumIfSum() {
		String formulaString;
		Ptg[] ptgs;
		ptgs = parseFormula("sum(5, 2, if(3>2, sum(A1:A2), 6))");
		formulaString = toFormulaString(ptgs);
		assertEquals("SUM(5,2,IF(3>2,SUM(A1:A2),6))", formulaString);

		ptgs = parseFormula("if(1<2,sum(5, 2, if(3>2, sum(A1:A2), 6)),4)");
		formulaString = toFormulaString(ptgs);
		assertEquals("IF(1<2,SUM(5,2,IF(3>2,SUM(A1:A2),6)),4)", formulaString);
	}
	public void testParserErrors() {
		parseExpectedException("1 2");
		parseExpectedException(" 12 . 345  ");
		parseExpectedException("1 .23  ");

		parseExpectedException("sum(#NAME)");
		parseExpectedException("1 + #N / A * 2");
		parseExpectedException("#value?");
		parseExpectedException("#DIV/ 0+2");


		parseExpectedException("IF(TRUE)");
		parseExpectedException("countif(A1:B5, C1, D1)");
	}

	private static void parseExpectedException(String formula) {
		try {
			parseFormula(formula);
			throw new AssertionFailedError("expected parse exception");
		} catch (FormulaParseException e) {
			// expected during successful test
			assertNotNull(e.getMessage());
		}
	}

	public void testSetFormulaWithRowBeyond32768_Bug44539() {

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		wb.setSheetName(0, "Sheet1");

		HSSFRow row = sheet.createRow(0);
		HSSFCell cell = row.createCell(0);
		cell.setCellFormula("SUM(A32769:A32770)");
		if("SUM(A-32767:A-32766)".equals(cell.getCellFormula())) {
			fail("Identified bug 44539");
		}
		assertEquals("SUM(A32769:A32770)", cell.getCellFormula());
	}

	public void testSpaceAtStartOfFormula() {
		// Simulating cell formula of "= 4" (note space)
		// The same Ptg array can be observed if an excel file is saved with that exact formula

		AttrPtg spacePtg = AttrPtg.createSpace(AttrPtg.SpaceType.SPACE_BEFORE, 1);
		Ptg[] ptgs = { spacePtg, new IntPtg(4), };
		String formulaString;
		try {
			formulaString = toFormulaString(ptgs);
		} catch (IllegalStateException e) {
			if(e.getMessage().equalsIgnoreCase("too much stuff left on the stack")) {
				throw new AssertionFailedError("Identified bug 44609");
			}
			// else some unexpected error
			throw e;
		}
		// FormulaParser strips spaces anyway
		assertEquals("4", formulaString);

		ptgs = new Ptg[] { new IntPtg(3), spacePtg, new IntPtg(4), spacePtg, AddPtg.instance, };
		formulaString = toFormulaString(ptgs);
		assertEquals("3+4", formulaString);
	}

	/**
	 * Checks some internal error detecting logic ('stack underflow error' in toFormulaString)
	 */
	public void testTooFewOperandArgs() {
		// Simulating badly encoded cell formula of "=/1"
		// Not sure if Excel could ever produce this
		Ptg[] ptgs = {
				// Excel would probably have put tMissArg here
				new IntPtg(1),
				DividePtg.instance,
		};
		try {
			toFormulaString(ptgs);
			fail("Expected exception was not thrown");
		} catch (IllegalStateException e) {
			// expected during successful test
			assertTrue(e.getMessage().startsWith("Too few arguments supplied to operation"));
		}
	}
	/**
	 * Make sure that POI uses the right Func Ptg when encoding formulas.  Functions with variable
	 * number of args should get FuncVarPtg, functions with fixed args should get FuncPtg.<p/>
	 *
	 * Prior to the fix for bug 44675 POI would encode FuncVarPtg for all functions.  In many cases
	 * Excel tolerates the wrong Ptg and evaluates the formula OK (e.g. SIN), but in some cases
	 * (e.g. COUNTIF) Excel fails to evaluate the formula, giving '#VALUE!' instead.
	 */
	public void testFuncPtgSelection() {

		Ptg[] ptgs = parseFormula("countif(A1:A2, 1)");
		assertEquals(3, ptgs.length);
		if(ptgs[2] instanceof FuncVarPtg) {
			throw new AssertionFailedError("Identified bug 44675");
		}
		confirmTokenClasses(ptgs, AreaPtg.class, IntPtg.class, FuncPtg.class);

		confirmTokenClasses("sin(1)", IntPtg.class, FuncPtg.class);
	}

	public void testWrongNumberOfFunctionArgs() {
		confirmArgCountMsg("sin()", "Too few arguments to function 'SIN'. Expected 1 but got 0.");
		confirmArgCountMsg("countif(1, 2, 3, 4)", "Too many arguments to function 'COUNTIF'. Expected 2 but got 4.");
		confirmArgCountMsg("index(1, 2, 3, 4, 5, 6)", "Too many arguments to function 'INDEX'. At most 4 were expected but got 6.");
		confirmArgCountMsg("vlookup(1, 2)", "Too few arguments to function 'VLOOKUP'. At least 3 were expected but got 2.");
	}

	private static void confirmArgCountMsg(String formula, String expectedMessage) {
		HSSFWorkbook book = new HSSFWorkbook();
		try {
			HSSFFormulaParser.parse(formula, book);
			throw new AssertionFailedError("Didn't get parse exception as expected");
		} catch (FormulaParseException e) {
			confirmParseException(e, expectedMessage);
		}
	}

	public void testParseErrorExpectedMsg() {

		try {
			parseFormula("round(3.14;2)");
			throw new AssertionFailedError("Didn't get parse exception as expected");
		} catch (FormulaParseException e) {
			confirmParseException(e,
					"Parse error near char 10 ';' in specified formula 'round(3.14;2)'. Expected ',' or ')'");
		}

		try {
			parseFormula(" =2+2");
			throw new AssertionFailedError("Didn't get parse exception as expected");
		} catch (FormulaParseException e) {
			confirmParseException(e,
					"The specified formula ' =2+2' starts with an equals sign which is not allowed.");
		}
	}

	/**
	 * this function name has a dot in it.
	 */
	public void testParseErrorTypeFunction() {

		Ptg[] ptgs;
		try {
			ptgs = parseFormula("error.type(A1)");
		} catch (IllegalArgumentException e) {
			if (e.getMessage().equals("Invalid Formula cell reference: 'error'")) {
				throw new AssertionFailedError("Identified bug 45334");
			}
			throw e;
		}
		confirmTokenClasses(ptgs, RefPtg.class, FuncPtg.class);
		assertEquals("ERROR.TYPE", ((FuncPtg) ptgs[1]).getName());
	}

	public void testNamedRangeThatLooksLikeCell() {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Sheet1");
		HSSFName name = wb.createName();
		name.setRefersToFormula("Sheet1!B1");
		name.setNameName("pfy1");

		Ptg[] ptgs;
		try {
			ptgs = HSSFFormulaParser.parse("count(pfy1)", wb);
		} catch (IllegalArgumentException e) {
			if (e.getMessage().equals("Specified colIx (1012) is out of range")) {
				throw new AssertionFailedError("Identified bug 45354");
			}
			throw e;
		}
		confirmTokenClasses(ptgs, NamePtg.class, FuncVarPtg.class);

		HSSFCell cell = sheet.createRow(0).createCell(0);
		cell.setCellFormula("count(pfy1)");
		assertEquals("COUNT(pfy1)", cell.getCellFormula());
		try {
			cell.setCellFormula("count(pf1)");
			throw new AssertionFailedError("Expected formula parse execption");
		} catch (FormulaParseException e) {
			confirmParseException(e,
					"Specified named range 'pf1' does not exist in the current workbook.");
		}
		cell.setCellFormula("count(fp1)"); // plain cell ref, col is in range
	}

	public void testParseAreaRefHighRow_bug45358() {
		Ptg[] ptgs;
		AreaI aptg;

		HSSFWorkbook book = new HSSFWorkbook();
		book.createSheet("Sheet1");

		ptgs = HSSFFormulaParser.parse("Sheet1!A10:A40000", book);
		aptg = (AreaI) ptgs[0];
		if (aptg.getLastRow() == -25537) {
			throw new AssertionFailedError("Identified bug 45358");
		}
		assertEquals(39999, aptg.getLastRow());

		ptgs = HSSFFormulaParser.parse("Sheet1!A10:A65536", book);
		aptg = (AreaI) ptgs[0];
		assertEquals(65535, aptg.getLastRow());

		// plain area refs should be ok too
		ptgs = parseFormula("A10:A65536");
		aptg = (AreaI) ptgs[0];
		assertEquals(65535, aptg.getLastRow());

	}
	public void testParseArray()  {
		Ptg[] ptgs;
		ptgs = parseFormula("mode({1,2,2,#REF!;FALSE,3,3,2})");
		confirmTokenClasses(ptgs, ArrayPtg.class, FuncVarPtg.class);
		assertEquals("{1.0,2.0,2.0,#REF!;FALSE,3.0,3.0,2.0}", ptgs[0].toFormulaString());

		ArrayPtg aptg = (ArrayPtg) ptgs[0];
		Object[][] values = aptg.getTokenArrayValues();
		assertEquals(ErrorConstant.valueOf(HSSFErrorConstants.ERROR_REF), values[0][3]);
		assertEquals(Boolean.FALSE, values[1][0]);
	}

	public void testRangeOperator() {

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		HSSFCell cell = sheet.createRow(0).createCell(0);

		wb.setSheetName(0, "Sheet1");
		cell.setCellFormula("Sheet1!B$4:Sheet1!$C1"); // explicit range ':' operator
		assertEquals("Sheet1!B$4:Sheet1!$C1", cell.getCellFormula());

		cell.setCellFormula("Sheet1!B$4:$C1"); // plain area ref
		assertEquals("Sheet1!B1:$C$4", cell.getCellFormula()); // note - area ref is normalised

		cell.setCellFormula("Sheet1!$C1...B$4"); // different syntax for plain area ref
		assertEquals("Sheet1!B1:$C$4", cell.getCellFormula());

		// with funny sheet name
		wb.setSheetName(0, "A1...A2");
		cell.setCellFormula("A1...A2!B1");
		assertEquals("A1...A2!B1", cell.getCellFormula());
	}

	public void testBooleanNamedSheet() {

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("true");
		HSSFCell cell = sheet.createRow(0).createCell(0);
		cell.setCellFormula("'true'!B2");

		assertEquals("'true'!B2", cell.getCellFormula());
	}

	public void testParseExternalWorkbookReference() {
		HSSFWorkbook wbA = HSSFTestDataSamples.openSampleWorkbook("multibookFormulaA.xls");
		HSSFCell cell = wbA.getSheetAt(0).getRow(0).getCell(0);

		// make sure formula in sample is as expected
		assertEquals("[multibookFormulaB.xls]BSheet1!B1", cell.getCellFormula());
		Ptg[] expectedPtgs = FormulaExtractor.getPtgs(cell);
		confirmSingle3DRef(expectedPtgs, 1);

		// now try (re-)parsing the formula
		Ptg[] actualPtgs = HSSFFormulaParser.parse("[multibookFormulaB.xls]BSheet1!B1", wbA);
		confirmSingle3DRef(actualPtgs, 1); // externalSheetIndex 1 -> BSheet1

		// try parsing a formula pointing to a different external sheet
		Ptg[] otherPtgs = HSSFFormulaParser.parse("[multibookFormulaB.xls]AnotherSheet!B1", wbA);
		confirmSingle3DRef(otherPtgs, 0); // externalSheetIndex 0 -> AnotherSheet

		// try setting the same formula in a cell
		cell.setCellFormula("[multibookFormulaB.xls]AnotherSheet!B1");
		assertEquals("[multibookFormulaB.xls]AnotherSheet!B1", cell.getCellFormula());
	}
	private static void confirmSingle3DRef(Ptg[] ptgs, int expectedExternSheetIndex) {
		assertEquals(1, ptgs.length);
		Ptg ptg0 = ptgs[0];
		assertTrue(ptg0 instanceof Ref3DPtg);
		assertEquals(expectedExternSheetIndex, ((Ref3DPtg)ptg0).getExternSheetIndex());
	}

	public void testUnion() {
		String formula = "Sheet1!$B$2:$C$3,OFFSET(Sheet1!$E$2:$E$4,1,Sheet1!$A$1),Sheet1!$D$6";
		HSSFWorkbook wb = new HSSFWorkbook();
		wb.createSheet("Sheet1");
		Ptg[] ptgs = FormulaParser.parse(formula, HSSFEvaluationWorkbook.create(wb), FormulaType.CELL, -1);

		confirmTokenClasses(ptgs,
				// TODO - AttrPtg.class, // Excel prepends this
				MemFuncPtg.class,
				Area3DPtg.class,
				Area3DPtg.class,
				IntPtg.class,
				Ref3DPtg.class,
				FuncVarPtg.class,
				UnionPtg.class,
				Ref3DPtg.class,
				UnionPtg.class
		);
		MemFuncPtg mf = (MemFuncPtg)ptgs[0];
		assertEquals(45, mf.getLenRefSubexpression());
	}

	public void testRange_bug46643() {
		String formula = "Sheet1!A1:Sheet1!B3";
		HSSFWorkbook wb = new HSSFWorkbook();
		wb.createSheet("Sheet1");
		Ptg[] ptgs = FormulaParser.parse(formula, HSSFEvaluationWorkbook.create(wb), FormulaType.CELL, -1);

		if (ptgs.length == 3) {
			confirmTokenClasses(ptgs, Ref3DPtg.class, Ref3DPtg.class, RangePtg.class);
			throw new AssertionFailedError("Identified bug 46643");
		}

		confirmTokenClasses(ptgs,
				MemFuncPtg.class,
				Ref3DPtg.class,
				Ref3DPtg.class,
				RangePtg.class
		);
		MemFuncPtg mf = (MemFuncPtg)ptgs[0];
		assertEquals(15, mf.getLenRefSubexpression());
	}

	/** Named ranges with backslashes, e.g. 'POI\\2009' */
	public void testBackSlashInNames() {
		HSSFWorkbook wb = new HSSFWorkbook();

		HSSFName name = wb.createName();
		name.setNameName("POI\\2009");
		name.setRefersToFormula("Sheet1!$A$1");

		HSSFSheet sheet = wb.createSheet();
		HSSFRow row = sheet.createRow(0);

		HSSFCell cell_C1 =  row.createCell(2);
		cell_C1.setCellFormula("POI\\2009");
		assertEquals("POI\\2009", cell_C1.getCellFormula());

		HSSFCell cell_D1 = row.createCell(2);
		cell_D1.setCellFormula("NOT(POI\\2009=\"3.5-final\")");
		assertEquals("NOT(POI\\2009=\"3.5-final\")", cell_D1.getCellFormula());
	}

	/**
	 * TODO - delete equiv test:
	 * {@link BaseTestBugzillaIssues#test42448()}
	 */
	public void testParseAbnormalSheetNamesAndRanges_bug42448() {
		HSSFWorkbook wb = new HSSFWorkbook();
		wb.createSheet("A");
		try {
			HSSFFormulaParser.parse("SUM(A!C7:A!C67)", wb);
		} catch (StringIndexOutOfBoundsException e) {
			throw new AssertionFailedError("Identified bug 42448");
		}
		// the exact example from the bugzilla description:
		HSSFFormulaParser.parse("SUMPRODUCT(A!C7:A!C67, B8:B68) / B69", wb);
	}

	public void testRangeFuncOperand_bug46951() {
		HSSFWorkbook wb = new HSSFWorkbook();
		Ptg[] ptgs;
		try {
			ptgs = HSSFFormulaParser.parse("SUM(C1:OFFSET(C1,0,B1))", wb);
		} catch (RuntimeException e) {
			if (e.getMessage().equals("Specified named range 'OFFSET' does not exist in the current workbook.")) {
				throw new AssertionFailedError("Identified bug 46951");
			}
			throw e;
		}
		confirmTokenClasses(ptgs,
			MemFuncPtg.class, // [len=23]
			RefPtg.class, // [C1]
			RefPtg.class, // [C1]
			IntPtg.class, // [0]
			RefPtg.class, // [B1]
			FuncVarPtg.class, // [OFFSET nArgs=3]
			RangePtg.class, //
			AttrPtg.class // [sum ]
		);

	}

	public void testUnionOfFullCollFullRowRef() {
		Ptg[] ptgs;
		ptgs = parseFormula("3:4");
		ptgs = parseFormula("$Z:$AC");
		confirmTokenClasses(ptgs, AreaPtg.class);
		ptgs = parseFormula("B:B");

		ptgs = parseFormula("$11:$13");
		confirmTokenClasses(ptgs, AreaPtg.class);

		ptgs = parseFormula("$A:$A,$1:$4");
		confirmTokenClasses(ptgs, MemAreaPtg.class,
				AreaPtg.class,
				AreaPtg.class,
				UnionPtg.class
		);

		HSSFWorkbook wb = new HSSFWorkbook();
		wb.createSheet("Sheet1");
		ptgs = HSSFFormulaParser.parse("Sheet1!$A:$A,Sheet1!$1:$4", wb);
		confirmTokenClasses(ptgs, MemFuncPtg.class,
				Area3DPtg.class,
				Area3DPtg.class,
				UnionPtg.class
		);

		ptgs = HSSFFormulaParser.parse("'Sheet1'!$A:$A,'Sheet1'!$1:$4", wb);
		confirmTokenClasses(ptgs,
				MemFuncPtg.class,
				Area3DPtg.class,
				Area3DPtg.class,
				UnionPtg.class
		);
	}


	public void testExplicitRangeWithTwoSheetNames() {
		HSSFWorkbook wb = new HSSFWorkbook();
		wb.createSheet("Sheet1");
		Ptg[] ptgs = HSSFFormulaParser.parse("Sheet1!F1:Sheet1!G2", wb);
		confirmTokenClasses(ptgs,
				MemFuncPtg.class,
				Ref3DPtg.class,
				Ref3DPtg.class,
				RangePtg.class
		);
		MemFuncPtg mf;
		mf = (MemFuncPtg)ptgs[0];
		assertEquals(15, mf.getLenRefSubexpression());
	}

	/**
	 * Checks that the area-ref and explicit range operators get the right associativity
	 * and that the {@link MemFuncPtg} / {@link MemAreaPtg} is added correctly
	 */
	public void testComplexExplicitRangeEncodings() {

		Ptg[] ptgs;
		ptgs = parseFormula("SUM(OFFSET(A1,0,0):B2:C3:D4:E5:OFFSET(F6,1,1):G7)");
		confirmTokenClasses(ptgs,
			// AttrPtg.class, // [volatile ] // POI doesn't do this yet (Apr 2009)
			MemFuncPtg.class, // len 57
			RefPtg.class, // [A1]
			IntPtg.class, // [0]
			IntPtg.class, // [0]
			FuncVarPtg.class, // [OFFSET nArgs=3]
			AreaPtg.class, // [B2:C3]
			RangePtg.class,
			AreaPtg.class, // [D4:E5]
			RangePtg.class,
			RefPtg.class, // [F6]
			IntPtg.class, // [1]
			IntPtg.class, // [1]
			FuncVarPtg.class, // [OFFSET nArgs=3]
			RangePtg.class,
			RefPtg.class, // [G7]
			RangePtg.class,
			AttrPtg.class // [sum ]
		);

		MemFuncPtg mf = (MemFuncPtg)ptgs[0];
		assertEquals(57, mf.getLenRefSubexpression());
		assertEquals("D4:E5", ((AreaPtgBase)ptgs[7]).toFormulaString());
		assertTrue(((AttrPtg)ptgs[16]).isSum());

		ptgs = parseFormula("SUM(A1:B2:C3:D4)");
		confirmTokenClasses(ptgs,
			// AttrPtg.class, // [volatile ] // POI doesn't do this yet (Apr 2009)
				MemAreaPtg.class, // len 19
				AreaPtg.class, // [A1:B2]
				AreaPtg.class, // [C3:D4]
				RangePtg.class,
				AttrPtg.class // [sum ]
		);
		MemAreaPtg ma = (MemAreaPtg)ptgs[0];
		assertEquals(19, ma.getLenRefSubexpression());
	}


	/**
	 * Mostly confirming that erroneous conditions are detected.  Actual error message wording is not critical.
	 *
	 */
	public void testEdgeCaseParserErrors() {
		HSSFWorkbook wb = new HSSFWorkbook();
		wb.createSheet("Sheet1");

		confirmParseError(wb, "A1:ROUND(B1,1)", "The RHS of the range operator ':' at position 3 is not a proper reference.");

		confirmParseError(wb, "Sheet1!Sheet1", "Cell reference expected after sheet name at index 8.");
		confirmParseError(wb, "Sheet1!F:Sheet1!G", "'Sheet1!F' is not a proper reference.");
		confirmParseError(wb, "Sheet1!F..foobar", "Complete area reference expected after sheet name at index 11.");
		confirmParseError(wb, "Sheet1!A .. B", "Dotted range (full row or column) expression 'A .. B' must not contain whitespace.");
		confirmParseError(wb, "Sheet1!A...B", "Dotted range (full row or column) expression 'A...B' must have exactly 2 dots.");
		confirmParseError(wb, "Sheet1!A foobar", "Second part of cell reference expected after sheet name at index 10.");

		confirmParseError(wb, "foobar", "Specified named range 'foobar' does not exist in the current workbook.");
		confirmParseError(wb, "A1:1", "The RHS of the range operator ':' at position 3 is not a proper reference.");
	}

	private static void confirmParseError(HSSFWorkbook wb, String formula, String expectedMessage) {

		try {
			HSSFFormulaParser.parse(formula, wb);
			throw new AssertionFailedError("Expected formula parse execption");
		} catch (FormulaParseException e) {
			confirmParseException(e, expectedMessage);
		}
	}

	/**
	 * In bug 47078, POI had trouble evaluating a defined name flagged as 'complex'.
	 * POI should also be able to parse such defined names.
	 */
	public void testParseComplexName() {

		// Mock up a spreadsheet to match the critical details of the sample
		HSSFWorkbook wb = new HSSFWorkbook();
		wb.createSheet("Sheet1");
		HSSFName definedName = wb.createName();
		definedName.setNameName("foo");
		definedName.setRefersToFormula("Sheet1!B2");

		// Set the complex flag - POI doesn't usually manipulate this flag
		NameRecord nameRec = TestHSSFName.getNameRecord(definedName);
		nameRec.setOptionFlag((short)0x10); // 0x10 -> complex

		Ptg[] result;
		try {
			result = HSSFFormulaParser.parse("1+foo", wb);
		} catch (FormulaParseException e) {
			if (e.getMessage().equals("Specified name 'foo' is not a range as expected.")) {
				throw new AssertionFailedError("Identified bug 47078c");
			}
			throw e;
		}
		confirmTokenClasses(result, IntPtg.class, NamePtg.class, AddPtg.class);
	}

	/**
	 * Zero is not a valid row number so cell references like 'A0' are not valid.
	 * Actually, they should be treated like defined names.
	 * <br/>
	 * In addition, leading zeros (on the row component) should be removed from cell
	 * references during parsing.
	 */
	public void testZeroRowRefs() {
		String badCellRef = "B0"; // bad because zero is not a valid row number
		String leadingZeroCellRef = "B000001"; // this should get parsed as "B1"
		HSSFWorkbook wb = new HSSFWorkbook();

		try {
			HSSFFormulaParser.parse(badCellRef, wb);
			throw new AssertionFailedError("Identified bug 47312b - Shouldn't be able to parse cell ref '"
					+ badCellRef + "'.");
		} catch (FormulaParseException e) {
			// expected during successful test
			confirmParseException(e, "Specified named range '"
					+ badCellRef + "' does not exist in the current workbook.");
		}

		Ptg[] ptgs;
		try {
			ptgs = HSSFFormulaParser.parse(leadingZeroCellRef, wb);
			assertEquals("B1", ((RefPtg) ptgs[0]).toFormulaString());
		} catch (FormulaParseException e) {
			confirmParseException(e, "Specified named range '"
					+ leadingZeroCellRef + "' does not exist in the current workbook.");
			// close but no cigar
			throw new AssertionFailedError("Identified bug 47312c - '"
					+ leadingZeroCellRef + "' should parse as 'B1'.");
		}

		// create a defined name called 'B0' and try again
		Name n = wb.createName();
		n.setNameName("B0");
		n.setRefersToFormula("1+1");
		ptgs = HSSFFormulaParser.parse("B0", wb);
		confirmTokenClasses(ptgs, NamePtg.class);
	}

	private static void confirmParseException(FormulaParseException e, String expMsg) {
		assertEquals(expMsg, e.getMessage());
	}
}
