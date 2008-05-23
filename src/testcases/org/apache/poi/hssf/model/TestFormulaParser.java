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

import org.apache.poi.hssf.model.FormulaParser.FormulaParseException;
import org.apache.poi.hssf.record.formula.AbstractFunctionPtg;
import org.apache.poi.hssf.record.formula.AddPtg;
import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.hssf.record.formula.AttrPtg;
import org.apache.poi.hssf.record.formula.BoolPtg;
import org.apache.poi.hssf.record.formula.ConcatPtg;
import org.apache.poi.hssf.record.formula.DividePtg;
import org.apache.poi.hssf.record.formula.EqualPtg;
import org.apache.poi.hssf.record.formula.ErrPtg;
import org.apache.poi.hssf.record.formula.FuncPtg;
import org.apache.poi.hssf.record.formula.FuncVarPtg;
import org.apache.poi.hssf.record.formula.IntPtg;
import org.apache.poi.hssf.record.formula.LessEqualPtg;
import org.apache.poi.hssf.record.formula.LessThanPtg;
import org.apache.poi.hssf.record.formula.MissingArgPtg;
import org.apache.poi.hssf.record.formula.MultiplyPtg;
import org.apache.poi.hssf.record.formula.NamePtg;
import org.apache.poi.hssf.record.formula.NotEqualPtg;
import org.apache.poi.hssf.record.formula.NumberPtg;
import org.apache.poi.hssf.record.formula.PercentPtg;
import org.apache.poi.hssf.record.formula.PowerPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.ReferencePtg;
import org.apache.poi.hssf.record.formula.StringPtg;
import org.apache.poi.hssf.record.formula.SubtractPtg;
import org.apache.poi.hssf.record.formula.UnaryMinusPtg;
import org.apache.poi.hssf.record.formula.UnaryPlusPtg;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Test the low level formula parser functionality. High level tests are to
 *  be done via usermodel/HSSFCell.setFormulaValue() .
 * Some tests are also done in scratchpad, if they need
 *  HSSFFormulaEvaluator, which is there
 */
public final class TestFormulaParser extends TestCase {

	/**
	 * @return parsed token array already confirmed not <code>null</code>
	 */
	private static Ptg[] parseFormula(String s) {
		FormulaParser fp = new FormulaParser(s, null);
		fp.parse();
		Ptg[] result = fp.getRPNPtg();
		assertNotNull("Ptg array should not be null", result);
		return result;
	}

	public void testSimpleFormula() {
		Ptg[] ptgs = parseFormula("2+2");
		assertEquals(3, ptgs.length);
	}
	public void testFormulaWithSpace1() {
		Ptg[] ptgs = parseFormula(" 2 + 2 ");
		assertEquals(3, ptgs.length);
		assertTrue("",(ptgs[0] instanceof IntPtg));
		assertTrue("",(ptgs[1] instanceof IntPtg));
		assertTrue("",(ptgs[2] instanceof AddPtg));
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

	public void testYN() {
		Ptg[] ptgs = parseFormula("IF(TRUE,\"Y\",\"N\")");
		assertEquals(7, ptgs.length);

		BoolPtg flag  = (BoolPtg) ptgs[0];
		AttrPtg funif = (AttrPtg) ptgs[1];
		StringPtg y = (StringPtg) ptgs[2];
		AttrPtg goto1 = (AttrPtg) ptgs[3];
		StringPtg n = (StringPtg) ptgs[4];


		assertEquals(true, flag.getValue());
		assertEquals("Y", y.getValue());
		assertEquals("N", n.getValue());
		assertEquals("IF", funif.toFormulaString((HSSFWorkbook) null));
		assertTrue("Goto ptg exists", goto1.isGoto());
	}

	public void testSimpleIf() {
		String formula = "IF(1=1,0,1)";

		Class[] expectedClasses = {
			IntPtg.class,
			IntPtg.class,
			EqualPtg.class,
			AttrPtg.class,
			IntPtg.class,
			AttrPtg.class,
			IntPtg.class,
			AttrPtg.class,
			FuncVarPtg.class,
		};
		confirmTokenClasses(formula, expectedClasses);
		
		Ptg[] ptgs = parseFormula(formula);

		AttrPtg ifPtg = (AttrPtg) ptgs[3];
		AttrPtg ptgGoto= (AttrPtg) ptgs[5];
		assertEquals("Goto 1 Length", 10, ptgGoto.getData());

		AttrPtg ptgGoto2 = (AttrPtg) ptgs[7];
		assertEquals("Goto 2 Length", 3, ptgGoto2.getData());
		assertEquals("If FALSE offset", 7, ifPtg.getData());
	}

	/**
	 * Make sure the ptgs are generated properly with two functions embedded
	 *
	 */
	public void testNestedFunctionIf() {
		Ptg[] ptgs = parseFormula("IF(A1=B1,AVERAGE(A1:B1),AVERAGE(A2:B2))");
		assertEquals(11, ptgs.length);

		assertTrue("IF Attr set correctly", (ptgs[3] instanceof AttrPtg));
		AttrPtg ifFunc = (AttrPtg)ptgs[3];
		assertTrue("It is not an if", ifFunc.isOptimizedIf());

		assertTrue("Average Function set correctly", (ptgs[5] instanceof FuncVarPtg));
	}

	public void testIfSingleCondition(){
		Ptg[] ptgs = parseFormula("IF(1=1,10)");
		assertEquals(7, ptgs.length);

		assertTrue("IF Attr set correctly", (ptgs[3] instanceof AttrPtg));
		AttrPtg ifFunc = (AttrPtg)ptgs[3];
		assertTrue("It is not an if", ifFunc.isOptimizedIf());

		assertTrue("Single Value is not an IntPtg", (ptgs[4] instanceof IntPtg));
		IntPtg intPtg = (IntPtg)ptgs[4];
		assertEquals("Result", (short)10, intPtg.getValue());

		assertTrue("Ptg is not a Variable Function", (ptgs[6] instanceof FuncVarPtg));
		FuncVarPtg funcPtg = (FuncVarPtg)ptgs[6];
		assertEquals("Arguments", 2, funcPtg.getNumberOfOperands());
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
		String currencyCell = "F3";
		Ptg[] ptgs = parseFormula("\"TOTAL[\"&"+currencyCell+"&\"]\"");
		assertEquals(5, ptgs.length);
		assertTrue ("Ptg[0] is a string", (ptgs[0] instanceof StringPtg));
		StringPtg firstString = (StringPtg)ptgs[0];

		assertEquals("TOTAL[", firstString.getValue());
		//the PTG order isn't 100% correct but it still works - dmui
	}

	public void testSimpleLogical() {
	 Ptg[] ptgs = parseFormula("IF(A1<A2,B1,B2)");
	 assertEquals(9, ptgs.length);
	 assertEquals("3rd Ptg is less than", LessThanPtg.class, ptgs[2].getClass());
	}

	public void testParenIf() {
		Ptg[] ptgs = parseFormula("IF((A1+A2)<=3,\"yes\",\"no\")");
		assertEquals(12, ptgs.length);
		assertEquals("6th Ptg is less than equal",LessEqualPtg.class,ptgs[5].getClass());
		assertEquals("11th Ptg is not a goto (Attr) ptg",AttrPtg.class,ptgs[10].getClass());
	}

	public void testEmbeddedIf() {
		Ptg[] ptgs = parseFormula("IF(3>=1,\"*\",IF(4<>1,\"first\",\"second\"))");
		assertEquals(17, ptgs.length);

		assertEquals("6th Ptg is not a goto (Attr) ptg",AttrPtg.class,ptgs[5].getClass());
		assertEquals("9th Ptg is not a not equal ptg",NotEqualPtg.class,ptgs[8].getClass());
		assertEquals("15th Ptg is not the inner IF variable function ptg",FuncVarPtg.class,ptgs[14].getClass());
	}

	public void testMacroFunction() {
		HSSFWorkbook w = new HSSFWorkbook();
		FormulaParser fp = new FormulaParser("FOO()", w);
		fp.parse();
		Ptg[] ptg = fp.getRPNPtg();

		// the name gets encoded as the first arg
		NamePtg tname = (NamePtg) ptg[0];
		assertEquals("FOO", tname.toFormulaString(w));

		AbstractFunctionPtg tfunc = (AbstractFunctionPtg) ptg[1];
		assertTrue(tfunc.isExternalFunction());
	}

	public void testEmbeddedSlash() {
		Ptg[] ptgs = parseFormula("HYPERLINK(\"http://www.jakarta.org\",\"Jakarta\")");
		assertTrue("first ptg is string", ptgs[0] instanceof StringPtg);
		assertTrue("second ptg is string", ptgs[1] instanceof StringPtg);
	}

	public void testConcatenate() {
		Ptg[] ptgs = parseFormula("CONCATENATE(\"first\",\"second\")");
		assertTrue("first ptg is string", ptgs[0] instanceof StringPtg);
		assertTrue("second ptg is string", ptgs[1] instanceof StringPtg);
	}

	public void testWorksheetReferences() {
		HSSFWorkbook wb = new HSSFWorkbook();

		wb.createSheet("NoQuotesNeeded");
		wb.createSheet("Quotes Needed Here &#$@");

		HSSFSheet sheet = wb.createSheet("Test");
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell;

		cell = row.createCell((short)0);
		cell.setCellFormula("NoQuotesNeeded!A1");

		cell = row.createCell((short)1);
		cell.setCellFormula("'Quotes Needed Here &#$@'!A1");
	}

	public void testUnaryMinus() {
		Ptg[] ptgs = parseFormula("-A1");
		assertEquals(2, ptgs.length);
		assertTrue("first ptg is reference",ptgs[0] instanceof ReferencePtg);
		assertTrue("second ptg is Minus",ptgs[1] instanceof UnaryMinusPtg);
	}

	public void testUnaryPlus() {
		Ptg[] ptgs = parseFormula("+A1");
		assertEquals(2, ptgs.length);
		assertTrue("first ptg is reference",ptgs[0] instanceof ReferencePtg);
		assertTrue("second ptg is Plus",ptgs[1] instanceof UnaryPlusPtg);
	}

	public void testLeadingSpaceInString() {
		String value = "  hi  ";
		Ptg[] ptgs = parseFormula("\"" + value + "\"");

		assertEquals(1, ptgs.length);
		assertTrue("ptg0 is a StringPtg", ptgs[0] instanceof StringPtg);
		assertTrue("ptg0 contains exact value", ((StringPtg)ptgs[0]).getValue().equals(value));
	}

	public void testLookupAndMatchFunctionArgs() {
		Ptg[] ptgs = parseFormula("lookup(A1, A3:A52, B3:B52)");

		assertEquals(4, ptgs.length);
		assertTrue("ptg0 has Value class", ptgs[0].getPtgClass() == Ptg.CLASS_VALUE);

		ptgs = parseFormula("match(A1, A3:A52)");

		assertEquals(3, ptgs.length);
		assertTrue("ptg0 has Value class", ptgs[0].getPtgClass() == Ptg.CLASS_VALUE);
	}

	/** bug 33160*/
	public void testLargeInt() {
		Ptg[] ptgs = parseFormula("40");
		assertTrue("ptg is Int, is "+ptgs[0].getClass(),ptgs[0] instanceof IntPtg);

		ptgs = parseFormula("40000");
		assertTrue("ptg should be  IntPtg, is "+ptgs[0].getClass(), ptgs[0] instanceof IntPtg);
	}

	/** bug 33160, testcase by Amol Deshmukh*/
	public void testSimpleLongFormula() {
		Ptg[] ptgs = parseFormula("40000/2");
		assertEquals(3, ptgs.length);
		assertTrue("IntPtg", (ptgs[0] instanceof IntPtg));
		assertTrue("IntPtg", (ptgs[1] instanceof IntPtg));
		assertTrue("DividePtg", (ptgs[2] instanceof DividePtg));
	}

	/** bug 35027, underscore in sheet name */
	public void testUnderscore() {
		HSSFWorkbook wb = new HSSFWorkbook();

		wb.createSheet("Cash_Flow");

		HSSFSheet sheet = wb.createSheet("Test");
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell;

		cell = row.createCell((short)0);
		cell.setCellFormula("Cash_Flow!A1");
	}

	// bug 38396 : Formula with exponential numbers not parsed correctly.
	public void testExponentialParsing() {
		Ptg[] ptgs;
		ptgs = parseFormula("1.3E21/2");
		assertEquals(3, ptgs.length);
		assertTrue("NumberPtg", (ptgs[0] instanceof NumberPtg));
		assertTrue("IntPtg", (ptgs[1] instanceof IntPtg));
		assertTrue("DividePtg", (ptgs[2] instanceof DividePtg));

		ptgs = parseFormula("1322E21/2");
		assertEquals(3, ptgs.length);
		assertTrue("NumberPtg", (ptgs[0] instanceof NumberPtg));
		assertTrue("IntPtg", (ptgs[1] instanceof IntPtg));
		assertTrue("DividePtg", (ptgs[2] instanceof DividePtg));

		ptgs = parseFormula("1.3E1/2");
		assertEquals(3, ptgs.length);
		assertTrue("NumberPtg", (ptgs[0] instanceof NumberPtg));
		assertTrue("IntPtg", (ptgs[1] instanceof IntPtg));
		assertTrue("DividePtg", (ptgs[2] instanceof DividePtg));
	}

	public void testExponentialInSheet() {
		HSSFWorkbook wb = new HSSFWorkbook();

		wb.createSheet("Cash_Flow");

		HSSFSheet sheet = wb.createSheet("Test");
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell = row.createCell((short)0);
		String formula = null;

		cell.setCellFormula("1.3E21/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "1.3E21/3", formula);

		cell.setCellFormula("-1.3E21/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "-1.3E21/3", formula);

		cell.setCellFormula("1322E21/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "1.322E24/3", formula);

		cell.setCellFormula("-1322E21/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "-1.322E24/3", formula);

		cell.setCellFormula("1.3E1/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "13.0/3", formula);

		cell.setCellFormula("-1.3E1/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "-13.0/3", formula);

		cell.setCellFormula("1.3E-4/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "1.3E-4/3", formula);

		cell.setCellFormula("-1.3E-4/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "-1.3E-4/3", formula);

		cell.setCellFormula("13E-15/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "1.3E-14/3", formula);

		cell.setCellFormula("-13E-15/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "-1.3E-14/3", formula);

		cell.setCellFormula("1.3E3/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "1300.0/3", formula);

		cell.setCellFormula("-1.3E3/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "-1300.0/3", formula);

		cell.setCellFormula("1300000000000000/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "1.3E15/3", formula);

		cell.setCellFormula("-1300000000000000/3");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "-1.3E15/3", formula);

		cell.setCellFormula("-10E-1/3.1E2*4E3/3E4");
		formula = cell.getCellFormula();
		assertEquals("Exponential formula string", "-1.0/310.0*4000.0/30000.0", formula);
	}

	public void testNumbers() {
		HSSFWorkbook wb = new HSSFWorkbook();

		wb.createSheet("Cash_Flow");

		HSSFSheet sheet = wb.createSheet("Test");
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell = row.createCell((short)0);
		String formula = null;

		// starts from decimal point

		cell.setCellFormula(".1");
		formula = cell.getCellFormula();
		assertEquals("0.1", formula);

		cell.setCellFormula("+.1");
		formula = cell.getCellFormula();
		assertEquals("+0.1", formula);

		cell.setCellFormula("-.1");
		formula = cell.getCellFormula();
		assertEquals("-0.1", formula);

		// has exponent

		cell.setCellFormula("10E1");
		formula = cell.getCellFormula();
		assertEquals("100.0", formula);

		cell.setCellFormula("10E+1");
		formula = cell.getCellFormula();
		assertEquals("100.0", formula);

		cell.setCellFormula("10E-1");
		formula = cell.getCellFormula();
		assertEquals("1.0", formula);
	}

	public void testRanges() {
		HSSFWorkbook wb = new HSSFWorkbook();

		wb.createSheet("Cash_Flow");

		HSSFSheet sheet = wb.createSheet("Test");
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell = row.createCell((short)0);
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
				new FuncPtg(10),
		};
		assertEquals("NA()", FormulaParser.toFormulaString(book, ptgs));
	}

	public void testPercent() {
		Ptg[] ptgs;
		ptgs = parseFormula("5%");
		assertEquals(2, ptgs.length);
		assertEquals(ptgs[0].getClass(), IntPtg.class);
		assertEquals(ptgs[1].getClass(), PercentPtg.class);

		// spaces OK
		ptgs = parseFormula(" 250 % ");
		assertEquals(2, ptgs.length);
		assertEquals(ptgs[0].getClass(), IntPtg.class);
		assertEquals(ptgs[1].getClass(), PercentPtg.class);


		// double percent OK
		ptgs = parseFormula("12345.678%%");
		assertEquals(3, ptgs.length);
		assertEquals(ptgs[0].getClass(), NumberPtg.class);
		assertEquals(ptgs[1].getClass(), PercentPtg.class);
		assertEquals(ptgs[2].getClass(), PercentPtg.class);

		// percent of a bracketed expression
		ptgs = parseFormula("(A1+35)%*B1%");
		assertEquals(8, ptgs.length);
		assertEquals(ptgs[4].getClass(), PercentPtg.class);
		assertEquals(ptgs[6].getClass(), PercentPtg.class);

		// percent of a text quantity
		ptgs = parseFormula("\"8.75\"%");
		assertEquals(2, ptgs.length);
		assertEquals(ptgs[0].getClass(), StringPtg.class);
		assertEquals(ptgs[1].getClass(), PercentPtg.class);

		// percent to the power of
		ptgs = parseFormula("50%^3");
		assertEquals(4, ptgs.length);
		assertEquals(ptgs[0].getClass(), IntPtg.class);
		assertEquals(ptgs[1].getClass(), PercentPtg.class);
		assertEquals(ptgs[2].getClass(), IntPtg.class);
		assertEquals(ptgs[3].getClass(), PowerPtg.class);

		//
		// things that parse OK but would *evaluate* to an error

		ptgs = parseFormula("\"abc\"%");
		assertEquals(2, ptgs.length);
		assertEquals(ptgs[0].getClass(), StringPtg.class);
		assertEquals(ptgs[1].getClass(), PercentPtg.class);

		ptgs = parseFormula("#N/A%");
		assertEquals(2, ptgs.length);
		assertEquals(ptgs[0].getClass(), ErrPtg.class);
		assertEquals(ptgs[1].getClass(), PercentPtg.class);
	}

	/**
	 * Tests combinations of various operators in the absence of brackets
	 */
	public void testPrecedenceAndAssociativity() {

		Class[] expClss;

		// TRUE=TRUE=2=2  evaluates to FALSE
		expClss = new Class[] { BoolPtg.class, BoolPtg.class, EqualPtg.class,
				IntPtg.class, EqualPtg.class, IntPtg.class, EqualPtg.class,  };
		confirmTokenClasses("TRUE=TRUE=2=2", expClss);


		//  2^3^2	evaluates to 64 not 512
		expClss = new Class[] { IntPtg.class, IntPtg.class, PowerPtg.class,
				IntPtg.class, PowerPtg.class, };
		confirmTokenClasses("2^3^2", expClss);

		// "abc" & 2 + 3 & "def"   evaluates to "abc5def"
		expClss = new Class[] { StringPtg.class, IntPtg.class, IntPtg.class,
				AddPtg.class, ConcatPtg.class, StringPtg.class, ConcatPtg.class, };
		confirmTokenClasses("\"abc\"&2+3&\"def\"", expClss);


		//  (1 / 2) - (3 * 4)
		expClss = new Class[] { IntPtg.class, IntPtg.class, DividePtg.class,
				IntPtg.class, IntPtg.class, MultiplyPtg.class, SubtractPtg.class, };
		confirmTokenClasses("1/2-3*4", expClss);

		// 2 * (2^2)
		expClss = new Class[] { IntPtg.class, IntPtg.class, IntPtg.class, PowerPtg.class, MultiplyPtg.class, };
		// NOT: (2 *2) ^ 2 -> int int multiply int power
		confirmTokenClasses("2*2^2", expClss);

		//  2^200% -> 2 not 1.6E58
		expClss = new Class[] { IntPtg.class, IntPtg.class, PercentPtg.class, PowerPtg.class, };
		confirmTokenClasses("2^200%", expClss);
	}

	private static void confirmTokenClasses(String formula, Class[] expectedClasses) {
		Ptg[] ptgs = parseFormula(formula);
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
		confirmTokenClasses("2^5", new Class[] { IntPtg.class, IntPtg.class, PowerPtg.class, });
	}

	private static Ptg parseSingleToken(String formula, Class ptgClass) {
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

		Class[] expClss;

		expClss = new Class[] { ReferencePtg.class, MissingArgPtg.class, ReferencePtg.class,
				FuncVarPtg.class, };
		confirmTokenClasses("if(A1, ,C1)", expClss);

		expClss = new Class[] { MissingArgPtg.class, AreaPtg.class, MissingArgPtg.class,
				FuncVarPtg.class, };
		confirmTokenClasses("counta( , A1:B2, )", expClss);
	}

	public void testParseErrorLiterals() {

		confirmParseErrorLiteral(ErrPtg.NULL_INTERSECTION, "#NULL!");
		confirmParseErrorLiteral(ErrPtg.DIV_ZERO, "#DIV/0!");
		confirmParseErrorLiteral(ErrPtg.VALUE_INVALID, "#VALUE!");
		confirmParseErrorLiteral(ErrPtg.REF_INVALID, "#REF!");
		confirmParseErrorLiteral(ErrPtg.NAME_INVALID, "#NAME?");
		confirmParseErrorLiteral(ErrPtg.NUM_ERROR, "#NUM!");
		confirmParseErrorLiteral(ErrPtg.N_A, "#N/A");
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
		HSSFCell cell = row.createCell((short)0);
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
		formulaString = FormulaParser.toFormulaString(null, ptgs);
		assertEquals("SUM(5,2,IF(3>2,SUM(A1:A2),6))", formulaString);

		ptgs = parseFormula("if(1<2,sum(5, 2, if(3>2, sum(A1:A2), 6)),4)");
		formulaString = FormulaParser.toFormulaString(null, ptgs);
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
		} catch (RuntimeException e) {
			e.printStackTrace();
			fail("Wrong exception:" + e.getMessage());
		}
	}

	public void testSetFormulaWithRowBeyond32768_Bug44539() {

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		wb.setSheetName(0, "Sheet1");

		HSSFRow row = sheet.createRow(0);
		HSSFCell cell = row.createCell((short)0);
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
			formulaString = FormulaParser.toFormulaString(null, ptgs);
		} catch (IllegalStateException e) {
			if(e.getMessage().equalsIgnoreCase("too much stuff left on the stack")) {
				throw new AssertionFailedError("Identified bug 44609");
			}
			// else some unexpected error
			throw e;
		}
		// FormulaParser strips spaces anyway
		assertEquals("4", formulaString);

		ptgs = new Ptg[] { new IntPtg(3), spacePtg, new IntPtg(4), spacePtg, new AddPtg()};
		formulaString = FormulaParser.toFormulaString(null, ptgs);
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
				new DividePtg(),
		};
		try {
			FormulaParser.toFormulaString(null, ptgs);
			fail("Expected exception was not thrown");
		} catch (IllegalStateException e) {
			// expected during successful test
			assertTrue(e.getMessage().startsWith("Too few arguments suppled to operation token"));
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

		Ptg[] ptgs;
		ptgs = parseFormula("countif(A1:A2, 1)");
		assertEquals(3, ptgs.length);
		if(FuncVarPtg.class == ptgs[2].getClass()) {
			throw new AssertionFailedError("Identified bug 44675");
		}
		assertEquals(FuncPtg.class, ptgs[2].getClass());
		ptgs = parseFormula("sin(1)");
		assertEquals(2, ptgs.length);
		assertEquals(FuncPtg.class, ptgs[1].getClass());
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
			FormulaParser.parse(formula, book);
			throw new AssertionFailedError("Didn't get parse exception as expected");
		} catch (FormulaParseException e) {
			assertEquals(expectedMessage, e.getMessage());
		}
	}

	public void testParseErrorExpecteMsg() {

		try {
			parseFormula("round(3.14;2)");
			throw new AssertionFailedError("Didn't get parse exception as expected");
		} catch (FormulaParseException e) {
			assertEquals("Parse error near char 10 ';' in specified formula 'round(3.14;2)'. Expected ',' or ')'", e.getMessage());
		}
	}
}
