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

package org.apache.poi.hssf.record.formula.function;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.formula.AbstractFunctionPtg;
import org.apache.poi.hssf.record.formula.FuncPtg;
import org.apache.poi.hssf.record.formula.FuncVarPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
/**
 * Tests parsing of some built-in functions that were not properly
 * registered in POI as of bug #44675, #44733 (March/April 2008).
 * 
 * @author Josh Micich
 */
public final class TestParseMissingBuiltInFuncs extends TestCase {

	private static Ptg[] parse(String formula) {
		HSSFWorkbook book = new HSSFWorkbook();
		return HSSFFormulaParser.parse(formula, book);
	}
	private static void confirmFunc(String formula, int expPtgArraySize, boolean isVarArgFunc, int funcIx) {
		Ptg[] ptgs = parse(formula);
		Ptg ptgF = ptgs[ptgs.length-1];  // func is last RPN token in all these formulas
		
		// Check critical things in the Ptg array encoding.
		if(!(ptgF instanceof AbstractFunctionPtg)) {
		    throw new RuntimeException("function token missing");
		}
		AbstractFunctionPtg func = (AbstractFunctionPtg) ptgF;
		if(func.getFunctionIndex() == 255) {
			throw new AssertionFailedError("Failed to recognise built-in function in formula '" 
					+ formula + "'");
		}
		assertEquals(expPtgArraySize, ptgs.length);
		assertEquals(funcIx, func.getFunctionIndex());
		Class expCls = isVarArgFunc ? FuncVarPtg.class : FuncPtg.class;
		assertEquals(expCls, ptgF.getClass());
		
		// check that parsed Ptg array converts back to formula text OK
		HSSFWorkbook book = new HSSFWorkbook();
		String reRenderedFormula = HSSFFormulaParser.toFormulaString(book, ptgs);
		assertEquals(formula, reRenderedFormula);
	}
	
	public void testDatedif() {
		int expSize = 4;   // NB would be 5 if POI added tAttrVolatile properly
		confirmFunc("DATEDIF(NOW(),NOW(),\"d\")", expSize, false, 351);
	}

	public void testDdb() {
		confirmFunc("DDB(1,1,1,1,1)", 6, true, 144);
	}
	public void testAtan() {
		confirmFunc("ATAN(1)", 2, false, 18);
	}
	
	public void testUsdollar() {
		confirmFunc("USDOLLAR(1)", 2, true, 204);
	}

	public void testDBCS() {
		confirmFunc("DBCS(\"abc\")", 2, false, 215);
	}
	public void testIsnontext() {
		confirmFunc("ISNONTEXT(\"abc\")", 2, false, 190);
	}
	public void testDproduct() {
		confirmFunc("DPRODUCT(C1:E5,\"HarvestYield\",G1:H2)", 4, false, 189);
	}
}
