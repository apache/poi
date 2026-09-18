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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Random;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.Function;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.ss.util.Utils;
import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.jupiter.api.Test;

/**
 * Tests that the arithmetic operators behave as in Excel: plain IEEE 754 double
 * precision, no subnormal numbers, and an addition/subtraction that lands very close
 * to zero gives exactly zero.
 *
 * Also tests the {@link TwoOperandNumericOperation#isExactShortDecimal(double)}
 * fast path: whenever both operands pass the predicate the evaluator must produce
 * a result bit-identical to the BigDecimal-based reference implementation.
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
                new ValueEval[] { new NumberEval(1e200), new NumberEval(1e-200) }, 0, 0));
        // a subnormal operand is already zero (NumberEval flushes it), so dividing by it is #DIV/0!
        assertSame(ErrorEval.DIV_ZERO, TwoOperandNumericOperation.DivideEval.evaluate(
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
            Utils.assertError(fe, cell, "1E300/1E-300/1E-10", FormulaError.NUM);
            // the literal 1E-308 is below the smallest normal double, so it is 0 and dividing by it is #DIV/0!
            Utils.assertError(fe, cell, "1/1E-308", FormulaError.DIV0);
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

    // ===== Tests for isExactShortDecimal fast path =====

    @Test
    void testIsExactShortDecimal() {
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(0.0));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(1.0));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(-1.0));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(2.0));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(0.5));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(-0.5));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(0.25));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(0.75));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(0.125));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(2.5));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(-2.5));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(3.0));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(35.0));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(60.0));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(1024.0));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(65536.0));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(Math.pow(2, 20)));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(999_999_999_999_999.0));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(123_456_789_012_345.0));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(1_000_000_000_000_000.0));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(1e15));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(1e16));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(1_234_500_000_000_000.0));
        assertTrue(TwoOperandNumericOperation.isExactShortDecimal(123450000000000.0));

        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(-0.0));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(Double.NaN));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(Double.POSITIVE_INFINITY));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(Double.NEGATIVE_INFINITY));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(0.1));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(0.2));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(0.3));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(1.1));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(10.99));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(1.23456789012345));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(1.234567890123456));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(1.2345678901234567));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(Math.pow(2, 52)));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(Math.pow(2, 53)));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(Math.pow(2, 53) + 1));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(1e100));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(1e20));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(1_234_567_890_123_456.0));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(9.007199254740992E15));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(Double.MIN_VALUE));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(Double.MIN_NORMAL));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(Double.MAX_VALUE));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(1e-300));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(5e-324));
        assertFalse(TwoOperandNumericOperation.isExactShortDecimal(1e-10));
    }

    @Test
    void testFastPathBitIdenticalToBigDecimalReference() {
        double[] special = {
            0.0, -0.0, 1.0, -1.0, 2.0, -2.0, 0.5, -0.5, 0.25, 0.75, 0.125, 2.5, -2.5,
            0.1, 0.2, 0.3, 1.1, 10.99, 1.5, 3.0, -3.0, 35.0, 60.0,
            1.23456789012345, 1.234567890123456, 1.2345678901234567,
            Math.pow(2, 20), Math.pow(2, 52), Math.pow(2, 53), Math.pow(2, 53) + 1,
            999_999_999_999_999.0, 1_000_000_000_000_000.0, 1e15, 1e16, 1e20, 1e100,
            1_234_500_000_000_000.0, 123_456_789_012_345.0,
            Double.MIN_NORMAL, Math.pow(2, -1022) * 1.5,
            Double.MAX_VALUE, 1e-300,
            9_007_199_254_740_992.0, 1024.0, 65536.0, 1e-10
        };

        for (double d0 : special) {
            for (double d1 : special) {
                assertBitIdentical(d0, d1);
            }
        }

        Random r = new Random(42);
        for (int i = 0; i < 200_000; i++) {
            double d0 = Double.longBitsToDouble(r.nextLong());
            double d1 = Double.longBitsToDouble(r.nextLong());
            assertBitIdentical(d0, d1);
        }
    }

    @Test
    void testZeroDividendShortCircuitsDenormalDivisor() {
        // the legacy BigDecimal path cannot round-trip a subnormal divisor, so a
        // zero dividend must short-circuit before it instead of blowing up
        ValueEval result = EvalInstances.Divide.evaluate(new ValueEval[] {
            new NumberEval(0.0), new NumberEval(Double.MIN_VALUE),
        }, 0, (short) 0);
        assertTrue(result instanceof NumberEval);
        assertEquals(0.0, ((NumberEval) result).getNumberValue(), 0);
    }

    private static void assertBitIdentical(double d0, double d1) {
        boolean both = TwoOperandNumericOperation.isExactShortDecimal(d0)
                && TwoOperandNumericOperation.isExactShortDecimal(d1);
        assertTrue(!both || isFinite(d0) && isFinite(d1),
                "predicate must never accept NaN/Infinity: (" + d0 + ", " + d1 + ")");
        if (!isFinite(d0) || !isFinite(d1)) {
            // NaN and infinities are never produced by actual cell values; the
            // legacy BigDecimal reference cannot round-trip them either
            return;
        }
        double multiplyRef = normalizeZero(multiplyCurrent(d0, d1));
        assertOpParity(EvalInstances.Multiply, d0, d1, multiplyRef, both, "multiply");
        if (d1 != 0.0 && Math.abs(d1) >= Double.MIN_NORMAL) {
            // divisors below MIN_NORMAL cannot be represented by the legacy
            // BigDecimal fallback (rounded to zero), so only normal divisors compare
            assertOpParity(EvalInstances.Divide, d0, d1, normalizeZero(divideCurrent(d0, d1)), both, "divide");
        }
    }

    private static boolean isFinite(double d) {
        return !Double.isNaN(d) && !Double.isInfinite(d);
    }

    private static void assertOpParity(Function function, double d0, double d1, double ref, boolean both, String op) {
        ValueEval result = function.evaluate(new ValueEval[] {
            new NumberEval(d0), new NumberEval(d1),
        }, 0, (short) 0);
        if (result instanceof ErrorEval) {
            if (((ErrorEval) result).getErrorCode() == FormulaError.NUM.getCode() && !isFinite(ref)) {
                return; // overflow of the exact result, signalled as #NUM! by the evaluator
            }
            throw new AssertionError(op + "(" + d0 + ", " + d1 + ") unexpectedly " + result
                    + " (fast=" + both + ", ref=" + ref + ")");
        }
        if (!(result instanceof NumberEval)) {
            throw new AssertionError(op + "(" + d0 + ", " + d1 + ") unexpectedly " + result
                    + " (fast=" + both + ", ref=" + ref + ")");
        }
        double api = ((NumberEval) result).getNumberValue();
        assertTrue(Double.doubleToRawLongBits(ref) == Double.doubleToRawLongBits(api),
                op + "(" + d0 + ", " + d1 + ") fast=" + both);
    }

    private static double normalizeZero(double value) {
        return value == 0.0 ? 0.0 : value;
    }

    private static double multiplyCurrent(double d0, double d1) {
        BigDecimal bd0 = new BigDecimal(NumberToTextConverter.toText(d0));
        BigDecimal bd1 = new BigDecimal(NumberToTextConverter.toText(d1));
        return bd0.multiply(bd1).doubleValue();
    }

    private static double divideCurrent(double d0, double d1) {
        BigDecimal bd0 = new BigDecimal(NumberToTextConverter.toText(d0));
        BigDecimal bd1 = new BigDecimal(NumberToTextConverter.toText(d1));
        return bd0.divide(bd1, MathContext.DECIMAL128).doubleValue();
    }
}