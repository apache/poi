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
import java.util.Random;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.ss.usermodel.FormulaError;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void testMultiplyDivideMatchJavaForRandomOperands() {
        Random r = new Random(1234);
        int checked = 0;
        while (checked < 20_000) {
            double d0 = randomOperand(r);
            double d1 = randomOperand(r);
            double product = d0 * d1;
            if (isNormalOrZero(product)) {
                assertEquals(product, eval(TwoOperandNumericOperation.MultiplyEval, d0, d1), () -> d0 + "*" + d1);
                checked++;
            }
            double quotient = d0 / d1;
            if (d1 != 0.0 && isNormalOrZero(quotient)) {
                assertEquals(quotient, eval(TwoOperandNumericOperation.DivideEval, d0, d1), () -> d0 + "/" + d1);
                checked++;
            }
        }
    }

    @Test
    void testAddSubtractMatchJavaUnlessOperandsCancel() {
        Random r = new Random(4321);
        int checked = 0, cancelled = 0;
        while (checked < 20_000) {
            double d0 = randomOperand(r);
            double d1 = randomOperand(r);
            double sum = eval(TwoOperandNumericOperation.AddEval, d0, d1);
            double diff = eval(TwoOperandNumericOperation.SubtractEval, d0, d1);
            if (sum != d0 + d1 || diff != d0 - d1) {
                // the only permitted deviation is cancelling to exactly zero when Excel sees
                // the operands as equal, and then both the raw result and its magnitude are tiny
                double ulp = Math.ulp(Math.max(Math.abs(d0), Math.abs(d1)));
                assertTrue(sum == 0.0 && d0 + d1 != 0.0 && Math.abs(d0 + d1) < ulp * 64
                        || diff == 0.0 && d0 - d1 != 0.0 && Math.abs(d0 - d1) < ulp * 64,
                        () -> d0 + " +/- " + d1);
                cancelled++;
            }
            checked++;
        }
        assertTrue(cancelled > 0, "expected some operands to cancel");
    }

    private static double randomOperand(Random r) {
        switch (r.nextInt(4)) {
            case 0:  return r.nextInt(2001) - 1000;                       // small integers
            case 1:  return (r.nextInt(200001) - 100000) / 100.0;          // 2-decimal values
            case 2:  return (r.nextDouble() - 0.5) * Math.pow(10, r.nextInt(40) - 20); // wide range
            default: return r.nextBoolean() ? 0.1 + 0.2 : 0.3;             // classic near-equal pair
        }
    }

    private static boolean isNormalOrZero(double d) {
        return d == 0.0 || (Math.abs(d) >= Double.MIN_NORMAL && !Double.isInfinite(d) && !Double.isNaN(d));
    }

    @Test
    void testNegativeZeroIsNormalised() {
        // Excel has no -0: '*', '/', '+' give +0 ...
        assertSame(NumberEval.ZERO, TwoOperandNumericOperation.MultiplyEval.evaluate(
                new ValueEval[] { new NumberEval(-2), new NumberEval(0) }, 0, 0));
        assertSame(NumberEval.ZERO, TwoOperandNumericOperation.MultiplyEval.evaluate(
                new ValueEval[] { new NumberEval(0), new NumberEval(-0.5) }, 0, 0));
        assertSame(NumberEval.ZERO, TwoOperandNumericOperation.DivideEval.evaluate(
                new ValueEval[] { new NumberEval(0), new NumberEval(-5) }, 0, 0));
        assertSame(NumberEval.ZERO, TwoOperandNumericOperation.AddEval.evaluate(
                new ValueEval[] { new NumberEval(-0.0), new NumberEval(0) }, 0, 0));
        // ... and a cancelling subtraction gives +0 whichever operand is larger
        assertEquals(0.0, eval(TwoOperandNumericOperation.SubtractEval, 0.5 - 0.4, 0.1));
        assertEquals(0.0, eval(TwoOperandNumericOperation.SubtractEval, 0.1, 0.5 - 0.4));
    }

    @Test
    void testCancellationFormulas() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            // chains of decimal fractions that should sum to zero do
            Utils.assertDouble(fe, cell, "1-0.9-0.1", 0.0);
            Utils.assertDouble(fe, cell, "0.3-0.1-0.1-0.1", 0.0);
            Utils.assertDouble(fe, cell, "(0.1+0.2)-0.3", 0.0);
            Utils.assertDouble(fe, cell, "-0.1-0.2+0.3", 0.0);
            Utils.assertDouble(fe, cell, "0.1-0.3+0.2", 0.0);
            Utils.assertDouble(fe, cell, "0.1*3-0.3", 0.0);
            Utils.assertDouble(fe, cell, "4.35*100-435", 0.0);
            Utils.assertDouble(fe, cell, "0.7/0.1-7", 0.0);
            // intermediate results that are not near zero are plain IEEE 754
            Utils.assertDouble(fe, cell, "1-0.9", 1 - 0.9);
            Utils.assertDouble(fe, cell, "0.1+0.7", 0.1 + 0.7);
            Utils.assertDouble(fe, cell, "0.1+0.2", 0.1 + 0.2);
            Utils.assertDouble(fe, cell, "4.35*100", 4.35 * 100);
            Utils.assertDouble(fe, cell, "1.005*1000", 1.005 * 1000);
            // operands that differ at the 15th significant digit are not equal to Excel
            Utils.assertDouble(fe, cell, "1-0.999999999999999", 1 - 0.999999999999999);
            Utils.assertDouble(fe, cell, "1E15-999999999999999", 1.0);
            // cancellation applies to cell references and to blanks in numeric context alike
            HSSFCell a1 = cell.getRow().createCell(1);
            HSSFCell b1 = cell.getRow().createCell(2);
            a1.setCellValue(0.5 - 0.4);
            b1.setCellValue(0.1);
            Utils.assertDouble(fe, cell, "B1-C1", 0.0);
            Utils.assertDouble(fe, cell, "C1-B1", 0.0);
            Utils.assertDouble(fe, cell, "B1+D1", 0.5 - 0.4);
            Utils.assertDouble(fe, cell, "D1-B1", -(0.5 - 0.4));
        }
    }

    @Test
    void testOverflowAndUnderflowFormulas() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            Utils.assertError(fe, cell, "1E308*10", FormulaError.NUM);
            Utils.assertError(fe, cell, "1E308+1E308", FormulaError.NUM);
            Utils.assertError(fe, cell, "-1E308-1E308", FormulaError.NUM);
            Utils.assertError(fe, cell, "1/1E-308/1E-10", FormulaError.NUM);
            Utils.assertError(fe, cell, "1/0", FormulaError.DIV0);
            Utils.assertError(fe, cell, "0/0", FormulaError.DIV0);
            // results below the smallest normal double are 0, as Excel has no subnormals
            Utils.assertDouble(fe, cell, "1E-300/1E10", 0.0);
            Utils.assertDouble(fe, cell, "1E-200*1E-200", 0.0);
            Utils.assertDouble(fe, cell, "-1E-200*1E-200", 0.0);
            // the smallest normal double itself survives
            Utils.assertDouble(fe, cell, "2.2250738585072014E-308*1", Double.MIN_NORMAL);
        }
    }

    @Test
    void testFifteenDigitDisplayOfIeeeResults() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            // the computed value is IEEE 754; what the user sees is the 15-digit view
            String[][] cases = {
                { "880000000*0.00849", "7471200" },
                { "0.1*3", "0.3" },
                { "0.1+0.7", "0.8" },
                { "4.35*100", "435" },
                { "1.005*1000", "1005" },
                { "0.7/0.1", "7" },
                { "1.2*SQRT(5.678)", "2.85942651592937" },
                { "(43.1-43.2)+1", "0.899999999999999" },
            };
            for (String[] c : cases) {
                cell.setCellFormula(c[0]);
                fe.notifyUpdateCell(cell);
                fe.evaluateFormulaCell(cell);
                assertEquals(c[1], NumberToTextConverter.toText(cell.getNumericCellValue()), c[0]);
            }
        }
    }
}
