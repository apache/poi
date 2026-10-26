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

package org.apache.poi.ss.format;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

class TestSimpleFraction {

    private static void assertFraction(int numerator, int denominator, SimpleFraction actual) {
        assertEquals(numerator + "/" + denominator, actual.getNumerator() + "/" + actual.getDenominator());
    }

    /**
     * 1/0.00308641975308642 is exactly 324.0 as a double, so the remainder after the first step is zero
     * and the next partial quotient was Long.MAX_VALUE, which overflowed the denominator.
     */
    @Test
    void testZeroRemainder() {
        assertFraction(1, 324, SimpleFraction.buildFractionMaxDenominator(0.00308641975308642, 1000));
        assertFraction(0, 1, SimpleFraction.buildFractionMaxDenominator(0.00308641975308642, 100));
    }

    @Test
    void testReciprocals() {
        for (int k = 2; k < 10_000; k++) {
            double value = 1.0 / k;
            assertFraction(1, k, SimpleFraction.buildFractionMaxDenominator(value, 10_000));
        }
    }

    @Test
    void testTinyRemainder() {
        // a partial quotient too large for the limit leaves the previous convergent
        assertFraction(1, 3, SimpleFraction.buildFractionMaxDenominator(1.0 / 3 + 1e-15, 1000));
        assertFraction(7, 2, SimpleFraction.buildFractionMaxDenominator(3.5 - 1e-14, 100));
    }

    @Test
    void testNumeratorOverflow() {
        // the numerator passes Integer.MAX_VALUE first, leaving the previous convergent
        assertFraction(202510, 81, SimpleFraction.buildFractionMaxDenominator(2500.123456789, 1_000_000_000));
    }

    @Test
    void testMaxDenominator() {
        assertFraction(3, 5, SimpleFraction.buildFractionMaxDenominator(0.6, 10));
        assertFraction(22, 7, SimpleFraction.buildFractionMaxDenominator(Math.PI, 10));
        assertFraction(355, 113, SimpleFraction.buildFractionMaxDenominator(Math.PI, 1000));
    }

    /**
     * One ulp away from p/q, 1/(remainder) is sometimes exactly an integer, so the next remainder is zero.
     * About 0.12% of these draws overflowed before, so 200,000 draws all but always hit one.
     */
    @Test
    void testOneUlpFromRationals() {
        long seed = System.nanoTime();
        Random random = new Random(seed);
        for (int i = 0; i < 200_000; i++) {
            int q = 2 + random.nextInt(998);
            int p = 1 + random.nextInt(q - 1);
            int g = gcd(p, q);
            double exact = (double) p / q;
            double value = random.nextBoolean() ? Math.nextUp(exact) : Math.nextDown(exact);
            SimpleFraction f = SimpleFraction.buildFractionMaxDenominator(value, 1000);
            assertEquals(p / g + "/" + q / g, f.getNumerator() + "/" + f.getDenominator(),
                    "value " + value + ", seed " + seed);
        }
    }

    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
}
