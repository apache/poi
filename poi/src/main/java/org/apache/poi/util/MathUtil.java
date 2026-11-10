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

/**
 * Utility methods for dealing with conversions
 *
 * @since 6.0.0
 */
public class MathUtil {
    private MathUtil() {}

    public static int safeFloatToInt(float f) {
        if (Float.isNaN(f)) {
            throw new IllegalArgumentException("Cannot convert NaN to int");
        }
        if (Float.isInfinite(f)) {
            throw new IllegalArgumentException("Cannot convert infinity to int");
        }
        if (f > Integer.MAX_VALUE || f < Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Value out of range: " + f);
        }
        return (int) f;
    }

    public static int safeDoubleToInt(double d) {
        if (Double.isNaN(d)) {
            throw new IllegalArgumentException("Cannot convert NaN to int");
        }
        if (Double.isInfinite(d)) {
            throw new IllegalArgumentException("Cannot convert infinity to int");
        }
        if (d > Integer.MAX_VALUE || d < Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Value out of range: " + d);
        }
        return (int) d;
    }
}
