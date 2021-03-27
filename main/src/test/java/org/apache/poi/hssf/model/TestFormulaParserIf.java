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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ss.formula.ptg.AddPtg;
import org.apache.poi.ss.formula.ptg.AttrPtg;
import org.apache.poi.ss.formula.ptg.BoolPtg;
import org.apache.poi.ss.formula.ptg.FuncPtg;
import org.apache.poi.ss.formula.ptg.FuncVarPtg;
import org.apache.poi.ss.formula.ptg.IntPtg;
import org.apache.poi.ss.formula.ptg.LessEqualPtg;
import org.apache.poi.ss.formula.ptg.LessThanPtg;
import org.apache.poi.ss.formula.ptg.MultiplyPtg;
import org.apache.poi.ss.formula.ptg.NotEqualPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtg;
import org.apache.poi.ss.formula.ptg.StringPtg;
import org.junit.jupiter.api.Test;

/**
 * Tests <tt>FormulaParser</tt> specifically with respect to IF() functions
 */
final class TestFormulaParserIf {
	private static Ptg[] parseFormula(String formula) {
		return TestFormulaParser.parseFormula(formula);
	}

	private static Ptg[] confirmTokenClasses(String formula, Class<?>[] expectedClasses) {
		return TestFormulaParser.confirmTokenClasses(formula, expectedClasses);
	}

	private static void confirmAttrData(Ptg[] ptgs, int i, int expectedData) {
		Ptg ptg = ptgs[i];
		assertTrue(ptg instanceof AttrPtg, "Token[" + i + "] was not AttrPtg as expected");
		AttrPtg attrPtg = (AttrPtg) ptg;
		assertEquals(expectedData, attrPtg.getData());
	}

	@Test
	void testSimpleIf() {

		Class<?>[] expClss = {
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

	@Test
	void testSimpleIfNoFalseParam() {

		Class<?>[] expClss = {
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

	@Test
	void testIfWithLargeParams() {

		Class<?>[] expClss = {
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

	@Test
	void testNestedIf() {

		Class<?>[] expClss = {
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

	@Test
	void testEmbeddedIf() {
		Ptg[] ptgs = parseFormula("IF(3>=1,\"*\",IF(4<>1,\"first\",\"second\"))");
		assertEquals(17, ptgs.length);

		assertEquals(AttrPtg.class, ptgs[5].getClass(), "6th Ptg is not a goto (Attr) ptg");
		assertEquals(NotEqualPtg.class, ptgs[8].getClass(), "9th Ptg is not a not equal ptg");
		assertEquals(FuncVarPtg.class, ptgs[14].getClass(), "15th Ptg is not the inner IF variable function ptg");
	}

	@Test
	void testSimpleLogical() {
	 Ptg[] ptgs = parseFormula("IF(A1<A2,B1,B2)");
	 assertEquals(9, ptgs.length);
	 assertEquals(LessThanPtg.class, ptgs[2].getClass(), "3rd Ptg is less than");
	}

	@Test
	void testParenIf() {
		Ptg[] ptgs = parseFormula("IF((A1+A2)<=3,\"yes\",\"no\")");
		assertEquals(12, ptgs.length);
		assertEquals(LessEqualPtg.class, ptgs[5].getClass(), "6th Ptg is less than equal");
		assertEquals(AttrPtg.class, ptgs[10].getClass(), "11th Ptg is not a goto (Attr) ptg");
	}

	@Test
	void testYN() {
		Ptg[] ptgs = parseFormula("IF(TRUE,\"Y\",\"N\")");
		assertEquals(7, ptgs.length);

		BoolPtg flag  = (BoolPtg) ptgs[0];
		AttrPtg funif = (AttrPtg) ptgs[1];
		StringPtg y = (StringPtg) ptgs[2];
		AttrPtg goto1 = (AttrPtg) ptgs[3];
		StringPtg n = (StringPtg) ptgs[4];


        assertTrue(flag.getValue());
		assertEquals("Y", y.getValue());
		assertEquals("N", n.getValue());
		assertEquals("IF", funif.toFormulaString());
		assertTrue(goto1.isSkip(), "tAttrSkip ptg exists");
	}

	/**
	 * Make sure the ptgs are generated properly with two functions embedded
	 */
	@Test
	void testNestedFunctionIf() {
		Ptg[] ptgs = parseFormula("IF(A1=B1,AVERAGE(A1:B1),AVERAGE(A2:B2))");
		assertEquals(11, ptgs.length);

		assertTrue((ptgs[3] instanceof AttrPtg), "IF Attr set correctly");
		AttrPtg ifFunc = (AttrPtg)ptgs[3];
		assertTrue(ifFunc.isOptimizedIf(), "It is not an if");

		assertTrue((ptgs[5] instanceof FuncVarPtg), "Average Function set correctly");
	}

	@Test
	void testIfSingleCondition(){
		Ptg[] ptgs = parseFormula("IF(1=1,10)");
		assertEquals(7, ptgs.length);

		assertTrue((ptgs[3] instanceof AttrPtg), "IF Attr set correctly");
		AttrPtg ifFunc = (AttrPtg)ptgs[3];
		assertTrue(ifFunc.isOptimizedIf(), "It is not an if");

		assertTrue((ptgs[4] instanceof IntPtg), "Single Value is not an IntPtg");
		IntPtg intPtg = (IntPtg)ptgs[4];
		assertEquals((short)10, intPtg.getValue(), "Result");

		assertTrue((ptgs[6] instanceof FuncVarPtg), "Ptg is not a Variable Function");
		FuncVarPtg funcPtg = (FuncVarPtg)ptgs[6];
		assertEquals(2, funcPtg.getNumberOfOperands(), "Arguments");
	}
}
