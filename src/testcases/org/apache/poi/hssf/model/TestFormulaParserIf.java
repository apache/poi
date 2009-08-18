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

import org.apache.poi.hssf.record.formula.AddPtg;
import org.apache.poi.hssf.record.formula.AttrPtg;
import org.apache.poi.hssf.record.formula.BoolPtg;
import org.apache.poi.hssf.record.formula.FuncPtg;
import org.apache.poi.hssf.record.formula.FuncVarPtg;
import org.apache.poi.hssf.record.formula.IntPtg;
import org.apache.poi.hssf.record.formula.LessEqualPtg;
import org.apache.poi.hssf.record.formula.LessThanPtg;
import org.apache.poi.hssf.record.formula.MultiplyPtg;
import org.apache.poi.hssf.record.formula.NotEqualPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.RefPtg;
import org.apache.poi.hssf.record.formula.StringPtg;

/**
 * Tests <tt>FormulaParser</tt> specifically with respect to IF() functions
 */
public final class TestFormulaParserIf extends TestCase {
	private static Ptg[] parseFormula(String formula) {
		return TestFormulaParser.parseFormula(formula);
	}

	private static Ptg[] confirmTokenClasses(String formula, Class<?>[] expectedClasses) {
		return TestFormulaParser.confirmTokenClasses(formula, expectedClasses);
	}

	private static void confirmAttrData(Ptg[] ptgs, int i, int expectedData) {
		Ptg ptg = ptgs[i];
		if (!(ptg instanceof AttrPtg)) {
			throw new AssertionFailedError("Token[" + i + "] was not AttrPtg as expected");
		}
		AttrPtg attrPtg = (AttrPtg) ptg;
		assertEquals(expectedData, attrPtg.getData());
	}

	public void testSimpleIf() {

		Class<?>[] expClss;

		expClss = new Class[] {
				RefPtg.class,
				AttrPtg.class, // tAttrIf
				IntPtg.class,
				AttrPtg.class, // tAttrSkip
				IntPtg.class,
				AttrPtg.class, // tAttrSkip
				FuncVarPtg.class,
		};

		Ptg[] ptgs = confirmTokenClasses("if(A1,1,2)", expClss);

		confirmAttrData(ptgs, 1, 7);
		confirmAttrData(ptgs, 3, 10);
		confirmAttrData(ptgs, 5, 3);
	}

	public void testSimpleIfNoFalseParam() {

		Class<?>[] expClss;

		expClss = new Class[] {
				RefPtg.class,
				AttrPtg.class, // tAttrIf
				RefPtg.class,
				AttrPtg.class, // tAttrSkip
				FuncVarPtg.class,
		};

		Ptg[] ptgs = confirmTokenClasses("if(A1,B1)", expClss);

		confirmAttrData(ptgs, 1, 9);
		confirmAttrData(ptgs, 3, 3);
	}

	public void testIfWithLargeParams() {

		Class<?>[] expClss;

		expClss = new Class[] {
				RefPtg.class,
				AttrPtg.class, // tAttrIf

				RefPtg.class,
				IntPtg.class,
				MultiplyPtg.class,
				RefPtg.class,
				IntPtg.class,
				AddPtg.class,
				FuncPtg.class,
				AttrPtg.class, // tAttrSkip

				RefPtg.class,
				RefPtg.class,
				FuncPtg.class,

				AttrPtg.class, // tAttrSkip
				FuncVarPtg.class,
		};

		Ptg[] ptgs = confirmTokenClasses("if(A1,round(B1*100,C1+2),round(B1,C1))", expClss);

		confirmAttrData(ptgs, 1, 25);
		confirmAttrData(ptgs, 9, 20);
		confirmAttrData(ptgs, 13, 3);
	}

	public void testNestedIf() {

		Class<?>[] expClss;

		expClss = new Class[] {

				RefPtg.class,
				AttrPtg.class,	  // A tAttrIf
				RefPtg.class,
				AttrPtg.class,    //   B tAttrIf
				IntPtg.class,
				AttrPtg.class,    //   B tAttrSkip
				IntPtg.class,
				AttrPtg.class,    //   B tAttrSkip
				FuncVarPtg.class,
				AttrPtg.class,    // A tAttrSkip
				RefPtg.class,
				AttrPtg.class,    //   C tAttrIf
				IntPtg.class,
				AttrPtg.class,    //   C tAttrSkip
				IntPtg.class,
				AttrPtg.class,    //   C tAttrSkip
				FuncVarPtg.class,
				AttrPtg.class,    // A tAttrSkip
				FuncVarPtg.class,
		};

		Ptg[] ptgs = confirmTokenClasses("if(A1,if(B1,1,2),if(C1,3,4))", expClss);
		confirmAttrData(ptgs, 1, 31);
		confirmAttrData(ptgs, 3, 7);
		confirmAttrData(ptgs, 5, 10);
		confirmAttrData(ptgs, 7, 3);
		confirmAttrData(ptgs, 9, 34);
		confirmAttrData(ptgs, 11, 7);
		confirmAttrData(ptgs, 13, 10);
		confirmAttrData(ptgs, 15, 3);
		confirmAttrData(ptgs, 17, 3);
	}

	public void testEmbeddedIf() {
		Ptg[] ptgs = parseFormula("IF(3>=1,\"*\",IF(4<>1,\"first\",\"second\"))");
		assertEquals(17, ptgs.length);

		assertEquals("6th Ptg is not a goto (Attr) ptg",AttrPtg.class,ptgs[5].getClass());
		assertEquals("9th Ptg is not a not equal ptg",NotEqualPtg.class,ptgs[8].getClass());
		assertEquals("15th Ptg is not the inner IF variable function ptg",FuncVarPtg.class,ptgs[14].getClass());
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
		assertEquals("IF", funif.toFormulaString());
		assertTrue("Goto ptg exists", goto1.isGoto());
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
}
