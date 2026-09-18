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
}
