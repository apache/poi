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

package org.apache.poi.ss.formula.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;

import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.ptg.AbstractFunctionPtg;
import org.apache.poi.ss.formula.ptg.FuncPtg;
import org.apache.poi.ss.formula.ptg.FuncVarPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.junit.jupiter.api.Test;

/**
 * Tests parsing of some built-in functions that were not properly
 * registered in POI as of bug #44675, #44733 (March/April 2008).
 */
final class TestParseMissingBuiltInFuncs {

	private static Ptg[] parse(String formula) throws IOException {
		HSSFWorkbook book = new HSSFWorkbook();
		Ptg[] ptgs = HSSFFormulaParser.parse(formula, book);
		book.close();
		return ptgs;
	}

	private static void confirmFunc(String formula, int expPtgArraySize, boolean isVarArgFunc, int funcIx)
	throws IOException {
		Ptg[] ptgs = parse(formula);
		Ptg ptgF = ptgs[ptgs.length-1];  // func is last RPN token in all these formulas

		// Check critical things in the Ptg array encoding.
		if(!(ptgF instanceof AbstractFunctionPtg)) {
		    throw new RuntimeException("function token missing");
		}
		AbstractFunctionPtg func = (AbstractFunctionPtg) ptgF;
		assertNotEquals(255, func.getFunctionIndex(), "Failed to recognise built-in function in formula");
		assertEquals(expPtgArraySize, ptgs.length);
		assertEquals(funcIx, func.getFunctionIndex());
		Class<? extends AbstractFunctionPtg> expCls = isVarArgFunc ? FuncVarPtg.class : FuncPtg.class;
		assertEquals(expCls, ptgF.getClass());

		// check that parsed Ptg array converts back to formula text OK
		HSSFWorkbook book = new HSSFWorkbook();
		String reRenderedFormula = HSSFFormulaParser.toFormulaString(book, ptgs);
		assertEquals(formula, reRenderedFormula);
		book.close();
	}

	@Test
	void testDatedif() throws IOException {
		int expSize = 4;   // NB would be 5 if POI added tAttrVolatile properly
		confirmFunc("DATEDIF(NOW(),NOW(),\"d\")", expSize, false, 351);
	}

	@Test
	void testDdb() throws IOException {
		confirmFunc("DDB(1,1,1,1,1)", 6, true, 144);
	}

	@Test
	void testAtan() throws IOException {
		confirmFunc("ATAN(1)", 2, false, 18);
	}

	@Test
	void testUsdollar() throws IOException {
		confirmFunc("USDOLLAR(1)", 2, true, 204);
	}

	@Test
	void testDBCS() throws IOException {
		confirmFunc("DBCS(\"abc\")", 2, false, 215);
	}

	@Test
	void testIsnontext() throws IOException {
		confirmFunc("ISNONTEXT(\"abc\")", 2, false, 190);
	}

	@Test
	void testDproduct() throws IOException {
		confirmFunc("DPRODUCT(C1:E5,\"HarvestYield\",G1:H2)", 4, false, 189);
	}
}
