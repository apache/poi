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

package org.apache.poi.ss.formula.eval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.Function;
import org.apache.poi.ss.util.Utils;
import org.junit.jupiter.api.Test;

/**
 * Tests that the arithmetic operators behave as in Excel: plain IEEE 754 double
 * precision, no subnormal numbers, and an addition/subtraction that lands very close
 * to zero gives exactly zero.
 *
 * @see <a href="https://learn.microsoft.com/en-us/office/troubleshoot/excel/floating-point-arithmetic-inaccurate-result">Floating-point arithmetic may give inaccurate result in Excel</a>
 */
final class TestTwoOperandNumericOperation {

    private static double eval(Function op, double d0, double d1) {
        ValueEval result = op.evaluate(new ValueEval[] { new NumberEval(d0), new NumberEval(d1) }, 0, 0);
        return assertInstanceOf(NumberEval.class, result).getNumberValue();
    }

    @Test
    void testMultiplyAndDivideAreIeee754() {
        assertEquals(880000000 * 0.00849, eval(TwoOperandNumericOperation.MultiplyEval, 880000000, 0.00849));
        assertEquals(1.1 * 3, eval(TwoOperandNumericOperation.MultiplyEval, 1.1, 3));
        assertEquals(7 / 3.0, eval(TwoOperandNumericOperation.DivideEval, 7, 3));
        assertEquals(0.3 / 0.1, eval(TwoOperandNumericOperation.DivideEval, 0.3, 0.1));
        assertSame(ErrorEval.DIV_ZERO, TwoOperandNumericOperation.DivideEval.evaluate(
                new ValueEval[] { new NumberEval(1), new NumberEval(0) }, 0, 0));
    }

    @Test
    void testNoSubnormalsNoInfinities() {
        // Excel does not support subnormal numbers: underflow gives 0 ...
        assertSame(NumberEval.ZERO, TwoOperandNumericOperation.MultiplyEval.evaluate(
                new ValueEval[] { new NumberEval(1e-200), new NumberEval(1e-200) }, 0, 0));
        assertSame(NumberEval.ZERO, TwoOperandNumericOperation.DivideEval.evaluate(
                new ValueEval[] { new NumberEval(Double.MIN_VALUE), new NumberEval(2) }, 0, 0));
        // ... and overflow gives #NUM!
        assertSame(ErrorEval.NUM_ERROR, TwoOperandNumericOperation.MultiplyEval.evaluate(
                new ValueEval[] { new NumberEval(1e200), new NumberEval(1e200) }, 0, 0));
        assertSame(ErrorEval.NUM_ERROR, TwoOperandNumericOperation.DivideEval.evaluate(
                new ValueEval[] { new NumberEval(1), new NumberEval(Double.MIN_VALUE) }, 0, 0));
    }

    @Test
    void testAddSubtractCancelToZero() {
        assertEquals(0.0, eval(TwoOperandNumericOperation.SubtractEval, 0.5 - 0.4, 0.1));
        assertEquals(0.0, eval(TwoOperandNumericOperation.AddEval, 0.5 - 0.4, -0.1));
        assertEquals(43.1 - 43.2, eval(TwoOperandNumericOperation.SubtractEval, 43.1, 43.2));
        assertEquals(0.5 + 0.4, eval(TwoOperandNumericOperation.AddEval, 0.5, 0.4));
    }

    @Test
    void testFormulas() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            // examples from the Microsoft article
            Utils.assertDouble(fe, cell, "1.333+1.225-1.333-1.225", 0.0);
            Utils.assertDouble(fe, cell, "(43.1-43.2)+1", 0.8999999999999986);
            Utils.assertDouble(fe, cell, "0.5-0.4-0.1", 0.0);
            Utils.assertBoolean(fe, cell, "(0.1+0.2)=0.3", true);
            Utils.assertDouble(fe, cell, "1.2E+200+1E+100", 1.2E+200);
            // plain IEEE 754 for * and /, 15-digit view for INT
            Utils.assertDouble(fe, cell, "880000000*0.00849", 7471199.999999999);
            Utils.assertDouble(fe, cell, "INT(880000000*0.00849/3)", 2490400.0);
            Utils.assertDouble(fe, cell, "MOD(880000000*0.00849,3)", 0.0);
            Utils.assertDouble(fe, cell, "FLOOR(880000000*0.00849/3,1)", 2490400.0);
            Utils.assertDouble(fe, cell, "CEILING(2490399.9999999995,1)", 2490400.0);
            Utils.assertDouble(fe, cell, "FLOOR.MATH(2490399.9999999995)", 2490400.0);
            Utils.assertDouble(fe, cell, "CEILING.PRECISE(2490400.0000000005)", 2490400.0);
        }
    }
}
