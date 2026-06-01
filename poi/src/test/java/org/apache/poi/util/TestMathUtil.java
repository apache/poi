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

package org.apache.poi.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MathUtil}
 */
class TestMathUtil {

    // ---- safeFloatToInt ----

    @Test
    void testSafeFloatToInt_normalValues() {
        assertEquals(0, MathUtil.safeFloatToInt(0.0f));
        assertEquals(42, MathUtil.safeFloatToInt(42.0f));
        assertEquals(-42, MathUtil.safeFloatToInt(-42.0f));
        assertEquals(100, MathUtil.safeFloatToInt(100.5f)); // truncation
        assertEquals(-100, MathUtil.safeFloatToInt(-100.5f)); // truncation
    }

    @Test
    void testSafeFloatToInt_maxValue() {
        // Integer.MAX_VALUE as float is 2^31, which rounds to Integer.MAX_VALUE+1 in float,
        // so we use a value just within range
        assertEquals(2147483520, MathUtil.safeFloatToInt(2147483520.0f));
    }

    @Test
    void testSafeFloatToInt_minValue() {
        assertEquals(Integer.MIN_VALUE, MathUtil.safeFloatToInt((float) Integer.MIN_VALUE));
    }

    @Test
    void testSafeFloatToInt_overflowPositive() {
        assertThrows(IllegalArgumentException.class,
                () -> MathUtil.safeFloatToInt(Float.MAX_VALUE));
    }

    @Test
    void testSafeFloatToInt_overflowNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> MathUtil.safeFloatToInt(-Float.MAX_VALUE));
    }

    @Test
    void testSafeFloatToInt_positiveInfinity() {
        assertThrows(IllegalArgumentException.class,
                () -> MathUtil.safeFloatToInt(Float.POSITIVE_INFINITY));
    }

    @Test
    void testSafeFloatToInt_negativeInfinity() {
        assertThrows(IllegalArgumentException.class,
                () -> MathUtil.safeFloatToInt(Float.NEGATIVE_INFINITY));
    }

    @Test
    void testSafeFloatToInt_nan() {
        assertThrows(IllegalArgumentException.class,
                () -> MathUtil.safeFloatToInt(Float.NaN));
    }

    // ---- safeDoubleToInt ----

    @Test
    void testSafeDoubleToInt_normalValues() {
        assertEquals(0, MathUtil.safeDoubleToInt(0.0));
        assertEquals(42, MathUtil.safeDoubleToInt(42.0));
        assertEquals(-42, MathUtil.safeDoubleToInt(-42.0));
        assertEquals(100, MathUtil.safeDoubleToInt(100.9)); // truncation
        assertEquals(-100, MathUtil.safeDoubleToInt(-100.9)); // truncation
    }

    @Test
    void testSafeDoubleToInt_maxValue() {
        assertEquals(Integer.MAX_VALUE, MathUtil.safeDoubleToInt((double) Integer.MAX_VALUE));
    }

    @Test
    void testSafeDoubleToInt_minValue() {
        assertEquals(Integer.MIN_VALUE, MathUtil.safeDoubleToInt((double) Integer.MIN_VALUE));
    }

    @Test
    void testSafeDoubleToInt_justBelowMax() {
        assertEquals(Integer.MAX_VALUE - 1, MathUtil.safeDoubleToInt((double) (Integer.MAX_VALUE - 1)));
    }

    @Test
    void testSafeDoubleToInt_justAboveMin() {
        assertEquals(Integer.MIN_VALUE + 1, MathUtil.safeDoubleToInt((double) (Integer.MIN_VALUE + 1)));
    }

    @Test
    void testSafeDoubleToInt_overflowPositive() {
        assertThrows(IllegalArgumentException.class,
                () -> MathUtil.safeDoubleToInt((double) Integer.MAX_VALUE + 1.0));
    }

    @Test
    void testSafeDoubleToInt_overflowNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> MathUtil.safeDoubleToInt((double) Integer.MIN_VALUE - 1.0));
    }

    @Test
    void testSafeDoubleToInt_largePositive() {
        assertThrows(IllegalArgumentException.class,
                () -> MathUtil.safeDoubleToInt(Double.MAX_VALUE));
    }

    @Test
    void testSafeDoubleToInt_largeNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> MathUtil.safeDoubleToInt(-Double.MAX_VALUE));
    }

    @Test
    void testSafeDoubleToInt_positiveInfinity() {
        assertThrows(IllegalArgumentException.class,
                () -> MathUtil.safeDoubleToInt(Double.POSITIVE_INFINITY));
    }

    @Test
    void testSafeDoubleToInt_negativeInfinity() {
        assertThrows(IllegalArgumentException.class,
                () -> MathUtil.safeDoubleToInt(Double.NEGATIVE_INFINITY));
    }

    @Test
    void testSafeDoubleToInt_nan() {
        assertThrows(IllegalArgumentException.class,
                () -> MathUtil.safeDoubleToInt(Double.NaN));
    }
}
