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

/**
 * Arithmetic helpers that reproduce how Excel computes with IEEE 754 doubles.
 * <p>
 * Excel stores and calculates in double precision, but only ever exposes 15
 * significant decimal digits, and it compensates for binary representation error
 * in a few well-defined places. These helpers implement those places so that
 * formula evaluation matches what a user sees in Excel.
 *
 * @see <a href="https://learn.microsoft.com/en-us/office/troubleshoot/excel/floating-point-arithmetic-inaccurate-result">Floating-point arithmetic may give inaccurate result in Excel</a>
 * @since 6.0.0
 */
public final class ExcelArithmetic {

    private ExcelArithmetic() {
    }

    /**
     * Returns the value as Excel sees it: rounded to 15 significant decimal digits.
     * Functions that truncate (INT, FLOOR, CEILING, MOD, ...) act on this view rather
     * than on the raw binary value - e.g. {@code INT(2490399.9999999995)} is 2490400 in
     * Excel. Integers and values with few fractional bits are exactly representable
     * and are returned unchanged.
     */
    public static double approxValue(double d) {
        if (d == 0.0 || Double.isNaN(d) || Double.isInfinite(d) || d == Math.rint(d)) {
            return d;
        }
        // at most 11 fractional bits: a short exact binary fraction, nothing to approximate
        double scaled = d * 2048.0;
        if (scaled == Math.rint(scaled)) {
            return d;
        }
        return Double.parseDouble(NumberToTextConverter.toText(d));
    }

    /**
     * Truncates towards zero on the 15-digit view of the value, which is what Excel does with
     * the non-integer arguments of QUOTIENT, GCD, LCM, FACT, FACTDOUBLE, COMBIN, ISEVEN, ISODD
     * and friends ("if number is not an integer, it is truncated"): {@code 2.9999999999999996}
     * and {@code -2.9999999999999996} become 3 and -3, {@code 2.5} becomes 2.
     *
     * @return the truncated value as a double, so that magnitudes beyond the long range work
     * @see #approxValue(double)
     * @since 6.0.0
     */
    public static double truncate(double d) {
        double a = approxValue(d);
        return a < 0 ? Math.ceil(a) : Math.floor(a);
    }

    /**
     * Excel 97 and later compensate for the error introduced by converting decimal
     * operands to binary when an addition or subtraction lands at or very close to
     * zero: if the two magnitudes are equal to Excel (same 15 significant digits,
     * see {@link NumberComparer}), the result is exactly zero.
     *
     * @return {@code a + b}, or exactly 0 when Excel would cancel the operands
     */
    public static double approxAdd(double a, double b) {
        if (cancelsToZero(a, -b)) {
            return 0.0;
        }
        return a + b;
    }

    /**
     * @return {@code a - b}, or exactly 0 when Excel would cancel the operands
     * @see #approxAdd(double, double)
     */
    public static double approxSub(double a, double b) {
        if (cancelsToZero(a, b)) {
            return 0.0;
        }
        return a - b;
    }

    private static boolean cancelsToZero(double a, double b) {
        if (a == 0.0 || b == 0.0 || (a < 0.0) != (b < 0.0)) {
            return false;
        }
        if (Double.isNaN(a) || Double.isInfinite(a) || Double.isNaN(b) || Double.isInfinite(b)) {
            return false;
        }
        return NumberComparer.compare(a, b) == 0;
    }
}
