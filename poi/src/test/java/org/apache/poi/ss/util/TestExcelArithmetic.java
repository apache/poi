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

package org.apache.poi.ss.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class TestExcelArithmetic {

    @Test
    void testApproxValue() {
        // rounded to 15 significant digits
        assertEquals(2490400.0, ExcelArithmetic.approxValue(2490399.9999999995));
        assertEquals(7471200.0, ExcelArithmetic.approxValue(7471199.999999999));
        assertEquals(3.0, ExcelArithmetic.approxValue(3.0000000000000004));
        assertEquals(0.3, ExcelArithmetic.approxValue(0.1 + 0.2));
        assertEquals(1.23456789012346, ExcelArithmetic.approxValue(1.2345678901234567));
        // integers and short binary fractions are exact and left alone
        assertEquals(0.0, ExcelArithmetic.approxValue(0.0));
        assertEquals(2490399.5, ExcelArithmetic.approxValue(2490399.5));
        assertEquals(Math.pow(2, 53), ExcelArithmetic.approxValue(Math.pow(2, 53)));
        assertEquals(123456789012345.5, ExcelArithmetic.approxValue(123456789012345.5));
        assertEquals(1e20, ExcelArithmetic.approxValue(1e20));
        assertEquals(-0.0625, ExcelArithmetic.approxValue(-0.0625));
        // values that already have 15 or fewer significant digits round-trip
        assertEquals(0.1, ExcelArithmetic.approxValue(0.1));
        assertEquals(1.1, ExcelArithmetic.approxValue(1.1));
        assertEquals(-3.4, ExcelArithmetic.approxValue(-3.4));
        // specials pass through
        assertEquals(Double.NaN, ExcelArithmetic.approxValue(Double.NaN));
        assertEquals(Double.POSITIVE_INFINITY, ExcelArithmetic.approxValue(Double.POSITIVE_INFINITY));
    }

    @Test
    void testApproxAddAndSub() {
        // Excel 97+: an addition/subtraction that lands very close to zero gives exactly zero
        assertEquals(0.0, ExcelArithmetic.approxSub(0.5 - 0.4, 0.1));
        assertEquals(0.0, ExcelArithmetic.approxAdd(0.5 - 0.4, -0.1));
        assertEquals(0.0, ExcelArithmetic.approxSub(1.333 + 1.225 - 1.333, 1.225));
        assertEquals(0.0, ExcelArithmetic.approxAdd(0.1 + 0.2, -0.3));
        // but only when the operands cancel: everything else is plain IEEE 754
        assertEquals(0.5 + 0.4, ExcelArithmetic.approxAdd(0.5, 0.4));
        assertEquals(43.1 - 43.2, ExcelArithmetic.approxSub(43.1, 43.2));
        assertEquals(0.5 - 0.4, ExcelArithmetic.approxSub(0.5, 0.4));
        assertEquals(1e-20, ExcelArithmetic.approxSub(1e-20, 0.0));
        assertEquals(-1e-20, ExcelArithmetic.approxSub(0.0, 1e-20));
        assertEquals(2.0, ExcelArithmetic.approxAdd(1.0, 1.0));
        assertEquals(0.0, ExcelArithmetic.approxSub(1.0, 1.0));
        // operands that differ in the 15th digit are not equal to Excel and do not cancel
        assertEquals(1.0 - 0.999999999999999, ExcelArithmetic.approxSub(1.0, 0.999999999999999));
        // specials are left to IEEE
        assertEquals(Double.NaN, ExcelArithmetic.approxSub(Double.NaN, 1.0));
        assertEquals(Double.NaN, ExcelArithmetic.approxSub(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
    }

    @Test
    void testApproxValueBoundaries() {
        // 15 significant digits round-trip, the 16th is rounded away
        assertEquals(12345678.9012345, ExcelArithmetic.approxValue(12345678.9012345));
        assertEquals(12345678.9012346, ExcelArithmetic.approxValue(12345678.90123456));
        assertEquals(0.123456789012346, ExcelArithmetic.approxValue(0.1234567890123456));
        assertEquals(1.23456789012346E-10, ExcelArithmetic.approxValue(1.234567890123456E-10));
        assertEquals(1.23456789012346, ExcelArithmetic.approxValue(1.2345678901234567));
        // huge values are integers in binary and therefore untouched
        assertEquals(1.234567890123456E+100, ExcelArithmetic.approxValue(1.234567890123456E+100));
        // rounding carries across the integer boundary
        assertEquals(10.0, ExcelArithmetic.approxValue(9.999999999999998));
        assertEquals(1.0, ExcelArithmetic.approxValue(0.9999999999999999));
        assertEquals(100.0, ExcelArithmetic.approxValue(99.99999999999999));
        // sign-symmetric
        for (double d : new double[] { 2490399.9999999995, 0.1 + 0.2, 9.999999999999998, 1.2345678901234567, 0.375 }) {
            assertEquals(-ExcelArithmetic.approxValue(d), ExcelArithmetic.approxValue(-d));
        }
        // the largest and smallest magnitudes are handled (MAX_VALUE is an integer and so untouched)
        assertEquals(Double.MAX_VALUE, ExcelArithmetic.approxValue(Double.MAX_VALUE));
        assertEquals(2.2250738585072E-308, ExcelArithmetic.approxValue(Double.MIN_NORMAL));
        // subnormals do not exist in Excel and render as 0
        assertEquals(0.0, ExcelArithmetic.approxValue(Double.MIN_VALUE));
        // idempotent
        for (double d : new double[] { 2490399.9999999995, 0.1 + 0.2, 1.2345678901234567, 4.35 * 100, 1e-10 / 3 }) {
            double once = ExcelArithmetic.approxValue(d);
            assertEquals(once, ExcelArithmetic.approxValue(once));
        }
    }

    @Test
    void testApproxValueLeavesShortBinaryFractionsAlone() {
        // up to 11 fractional bits are exact; these are neither rounded nor re-parsed
        double[] exact = { 0.5, 0.25, 0.125, 0.0625, 1.0 / 1024, 1.0 / 2048, 3.0 / 2048,
                4398046511104.5, 123456789012345.5, 1000000000000000.5, -0.75, 1e15 + 0.25 };
        for (double d : exact) {
            assertEquals(d, ExcelArithmetic.approxValue(d));
        }
        // 12 fractional bits and a long decimal expansion is approximated
        assertEquals(2.44140625E-4, ExcelArithmetic.approxValue(1.0 / 4096));
        assertEquals(0.000244140625, ExcelArithmetic.approxValue(1.0 / 4096));
        assertEquals(1.0 / 4096, ExcelArithmetic.approxValue(1.0 / 4096)); // still exact: only 12 digits
        assertEquals(1.19209289550781E-7, ExcelArithmetic.approxValue(1.0 / 8388608)); // 2^-23 has 23 digits
    }

    @Test
    void testApproxAddSubSigns() {
        // both directions and both signs cancel
        assertEquals(0.0, ExcelArithmetic.approxSub(0.1, 0.5 - 0.4));
        assertEquals(0.0, ExcelArithmetic.approxSub(-(0.5 - 0.4), -0.1));
        assertEquals(0.0, ExcelArithmetic.approxSub(-0.1, -(0.5 - 0.4)));
        assertEquals(0.0, ExcelArithmetic.approxAdd(-(0.5 - 0.4), 0.1));
        assertEquals(0.0, ExcelArithmetic.approxAdd(0.1, -(0.5 - 0.4)));
        // same-sign addition and opposite-sign subtraction never cancel, however close
        assertEquals(0.2, ExcelArithmetic.approxAdd(0.1, 0.1));
        assertEquals(0.1 + (0.5 - 0.4), ExcelArithmetic.approxAdd(0.1, 0.5 - 0.4));
        assertEquals(0.1 - (-(0.5 - 0.4)), ExcelArithmetic.approxSub(0.1, -(0.5 - 0.4)));
        assertEquals(2.0, ExcelArithmetic.approxSub(1.0, -1.0));
        // a zero operand is a plain IEEE operation
        assertEquals(0.5 - 0.4, ExcelArithmetic.approxAdd(0.5 - 0.4, 0.0));
        assertEquals(0.5 - 0.4, ExcelArithmetic.approxSub(0.5 - 0.4, 0.0));
        assertEquals(-(0.5 - 0.4), ExcelArithmetic.approxSub(0.0, 0.5 - 0.4));
        assertEquals(0.0, ExcelArithmetic.approxAdd(0.0, 0.0));
        // equal operands cancel exactly, as in IEEE
        assertEquals(0.0, ExcelArithmetic.approxSub(Math.PI, Math.PI));
        assertEquals(0.0, ExcelArithmetic.approxAdd(Math.PI, -Math.PI));
        // huge and tiny magnitudes
        assertEquals(0.0, ExcelArithmetic.approxSub(1e300 * 3, 3e300));
        assertEquals(0.0, ExcelArithmetic.approxSub(1e-300 * 3, 3e-300));
        assertEquals(0.0, ExcelArithmetic.approxSub(1e300 / 3 * 3, 1e300));
        // subnormals are compared exactly, never approximated
        assertEquals(Double.MIN_VALUE, ExcelArithmetic.approxSub(2 * Double.MIN_VALUE, Double.MIN_VALUE));
    }
}
