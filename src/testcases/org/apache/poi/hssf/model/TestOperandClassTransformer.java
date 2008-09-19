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

import org.apache.poi.hssf.record.formula.AbstractFunctionPtg;
import org.apache.poi.hssf.record.formula.FuncVarPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Tests specific formula examples in <tt>OperandClassTransformer</tt>.
 * 
 * @author Josh Micich
 */
public final class TestOperandClassTransformer extends TestCase {

	private static Ptg[] parseFormula(String formula) {
		Ptg[] result = HSSFFormulaParser.parse(formula, (HSSFWorkbook)null);
		assertNotNull("Ptg array should not be null", result);
		return result;
	}
	
	public void testMdeterm() {
		String formula = "MDETERM(ABS(A1))";
		Ptg[] ptgs = parseFormula(formula);

		confirmTokenClass(ptgs, 0, Ptg.CLASS_ARRAY);
		confirmFuncClass(ptgs, 1, "ABS", Ptg.CLASS_ARRAY);
		confirmFuncClass(ptgs, 2, "MDETERM", Ptg.CLASS_VALUE);
	}

	/**
	 * In the example: <code>INDEX(PI(),1)</code>, Excel encodes PI() as 'array'.  It is not clear
	 * what rule justifies this. POI currently encodes it as 'value' which Excel(2007) seems to 
	 * tolerate. Changing the metadata for INDEX to have first parameter as 'array' class breaks 
	 * other formulas involving INDEX.  It seems like a special case needs to be made.  Perhaps an 
	 * important observation is that INDEX is one of very few functions that returns 'reference' type.
	 * 
	 * This test has been added but disabled in order to document this issue.
	 */
	public void DISABLED_testIndexPi1() {
		String formula = "INDEX(PI(),1)";
		Ptg[] ptgs = parseFormula(formula);

		confirmFuncClass(ptgs, 1, "PI", Ptg.CLASS_ARRAY); // fails as of POI 3.1
		confirmFuncClass(ptgs, 2, "INDEX", Ptg.CLASS_VALUE);
	}

	/**
	 * Even though count expects args of type R, because A1 is a direct operand of a
	 * value operator it must get type V
	 */
	public void testDirectOperandOfValueOperator() {
		String formula = "COUNT(A1*1)";
		Ptg[] ptgs = parseFormula(formula);
		if (ptgs[0].getPtgClass() == Ptg.CLASS_REF) {
			throw new AssertionFailedError("Identified bug 45348");
		}

		confirmTokenClass(ptgs, 0, Ptg.CLASS_VALUE);
		confirmTokenClass(ptgs, 3, Ptg.CLASS_VALUE);
	}
	
	/**
	 * A cell ref passed to a function expecting type V should be converted to type V
	 */
	public void testRtoV() {

		String formula = "lookup(A1, A3:A52, B3:B52)";
		Ptg[] ptgs = parseFormula(formula);
		confirmTokenClass(ptgs, 0, Ptg.CLASS_VALUE);
	}
	
	public void testComplexIRR_bug45041() {
		String formula = "(1+IRR(SUMIF(A:A,ROW(INDIRECT(MIN(A:A)&\":\"&MAX(A:A))),B:B),0))^365-1";
		Ptg[] ptgs = parseFormula(formula);

		FuncVarPtg rowFunc = (FuncVarPtg) ptgs[10];
		FuncVarPtg sumifFunc = (FuncVarPtg) ptgs[12];
		assertEquals("ROW", rowFunc.getName());
		assertEquals("SUMIF", sumifFunc.getName());

		if (rowFunc.getPtgClass() == Ptg.CLASS_VALUE || sumifFunc.getPtgClass() == Ptg.CLASS_VALUE) {
			throw new AssertionFailedError("Identified bug 45041");
		}
		confirmTokenClass(ptgs, 1, Ptg.CLASS_REF);
		confirmTokenClass(ptgs, 2, Ptg.CLASS_REF);
		confirmFuncClass(ptgs, 3, "MIN", Ptg.CLASS_VALUE);
		confirmTokenClass(ptgs, 6, Ptg.CLASS_REF);
		confirmFuncClass(ptgs, 7, "MAX", Ptg.CLASS_VALUE);
		confirmFuncClass(ptgs, 9, "INDIRECT", Ptg.CLASS_REF);
		confirmFuncClass(ptgs, 10, "ROW", Ptg.CLASS_ARRAY);
		confirmTokenClass(ptgs, 11, Ptg.CLASS_REF);
		confirmFuncClass(ptgs, 12, "SUMIF", Ptg.CLASS_ARRAY);
		confirmFuncClass(ptgs, 14, "IRR", Ptg.CLASS_VALUE);
	}

	private void confirmFuncClass(Ptg[] ptgs, int i, String expectedFunctionName, byte operandClass) {
		confirmTokenClass(ptgs, i, operandClass);
		AbstractFunctionPtg afp = (AbstractFunctionPtg) ptgs[i];
		assertEquals(expectedFunctionName, afp.getName());
	}

	private void confirmTokenClass(Ptg[] ptgs, int i, byte operandClass) {
		Ptg ptg = ptgs[i];
		if (ptg.isBaseToken()) {
			throw new AssertionFailedError("ptg[" + i + "] is a base token");
		}
		if (operandClass != ptg.getPtgClass()) {
			throw new AssertionFailedError("Wrong operand class for ptg ("
					+ ptg.toString() + "). Expected " + getOperandClassName(operandClass)
					+ " but got " + getOperandClassName(ptg.getPtgClass()));
		}
	}

	private static String getOperandClassName(byte ptgClass) {
		switch (ptgClass) {
			case Ptg.CLASS_REF:
				return "R";
			case Ptg.CLASS_VALUE:
				return "V";
			case Ptg.CLASS_ARRAY:
				return "A";
		}
		throw new RuntimeException("Unknown operand class (" + ptgClass + ")");
	}
}
