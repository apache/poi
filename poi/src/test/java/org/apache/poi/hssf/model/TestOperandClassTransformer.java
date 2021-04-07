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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.EvalFactory;
import org.apache.poi.ss.formula.functions.MatrixFunction;
import org.apache.poi.ss.formula.ptg.AbstractFunctionPtg;
import org.apache.poi.ss.formula.ptg.FuncVarPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests specific formula examples in <tt>OperandClassTransformer</tt>.
 */
final class TestOperandClassTransformer {

    private static Ptg[] parseFormula(String formula) {
        Ptg[] result = HSSFFormulaParser.parse(formula, null);
        assertNotNull(result, "Ptg array should not be null");
        return result;
    }

    @Test
    void testMdeterm() {
        String formula = "MDETERM(ABS(A1))";
        Ptg[] ptgs = parseFormula(formula);

        confirmTokenClass(ptgs, 0, Ptg.CLASS_ARRAY);
        confirmFuncClass(ptgs, 1, "ABS", Ptg.CLASS_ARRAY);
        confirmFuncClass(ptgs, 2, "MDETERM", Ptg.CLASS_VALUE);
    }

    @Test
    void testMdetermReturnsValueInvalidOnABlankCell() {
        ValueEval matrixRef = EvalFactory.createAreaEval("A1:B2",
                new ValueEval[]{
                        BlankEval.instance,
                        new NumberEval(1),
                        new NumberEval(2),
                        new NumberEval(3)
                }
        );
        ValueEval result = MatrixFunction.MDETERM.evaluate(new ValueEval[]{matrixRef} , 0, 0);
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }

    /**
     * In the example: <code>INDEX(PI(),1)</code>, Excel encodes PI() as 'array'.  It is not clear
     * what rule justifies this. POI currently encodes it as 'value' which Excel(2007) seems to
     * tolerate. Changing the metadata for INDEX to have first parameter as 'array' class breaks
     * other formulas involving INDEX.  It seems like a special case needs to be made.  Perhaps an
     * important observation is that INDEX is one of very few functions that returns 'reference' type.
     * <p>
     * This test has been added but disabled in order to document this issue.
     */
    @Test
    @Disabled
    void testIndexPi1() {
        String formula = "INDEX(PI(),1)";
        Ptg[] ptgs = parseFormula(formula);

        confirmFuncClass(ptgs, 1, "PI", Ptg.CLASS_ARRAY); // fails as of POI 3.1
        confirmFuncClass(ptgs, 2, "INDEX", Ptg.CLASS_VALUE);
    }

    /**
     * Even though count expects args of type R, because A1 is a direct operand of a
     * value operator it must get type V
     */
    @Test
    void testDirectOperandOfValueOperator() {
        String formula = "COUNT(A1*1)";
        Ptg[] ptgs = parseFormula(formula);
        assertNotEquals(Ptg.CLASS_REF, ptgs[0].getPtgClass());
        confirmTokenClass(ptgs, 0, Ptg.CLASS_VALUE);
        confirmTokenClass(ptgs, 3, Ptg.CLASS_VALUE);
    }

    /**
     * A cell ref passed to a function expecting type V should be converted to type V
     */
    @Test
    void testRtoV() {

        String formula = "lookup(A1, A3:A52, B3:B52)";
        Ptg[] ptgs = parseFormula(formula);
        confirmTokenClass(ptgs, 0, Ptg.CLASS_VALUE);
    }

    @Test
    void testComplexIRR_bug45041() {
        String formula = "(1+IRR(SUMIF(A:A,ROW(INDIRECT(MIN(A:A)&\":\"&MAX(A:A))),B:B),0))^365-1";
        Ptg[] ptgs = parseFormula(formula);

        FuncVarPtg rowFunc = (FuncVarPtg) ptgs[10];
        FuncVarPtg sumifFunc = (FuncVarPtg) ptgs[12];
        assertEquals("ROW", rowFunc.getName());
        assertEquals("SUMIF", sumifFunc.getName());

        assertNotEquals(Ptg.CLASS_VALUE, rowFunc.getPtgClass());
        assertNotEquals(Ptg.CLASS_VALUE, sumifFunc.getPtgClass());
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
        assertFalse(ptg.isBaseToken(), "ptg[" + i + "] is a base token");
        assertEquals(operandClass, ptg.getPtgClass());
    }
}
