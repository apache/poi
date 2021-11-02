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

package org.apache.poi.hssf.util;

/**
 * Utility class for helping convert RK numbers.
 *
 * @see org.apache.poi.hssf.record.MulRKRecord
 * @see org.apache.poi.hssf.record.RKRecord
 */
public final class RKUtil {
    private RKUtil() {
        // no instances of this class
    }

    /**
     * Do the dirty work of decoding; made a private static method to
     * facilitate testing the algorithm
     */
    public static double decodeNumber(int number) {
        long raw_number = number;

        // mask off the two low-order bits, 'cause they're not part of
        // the number
        raw_number = raw_number >> 2;
        double rvalue = 0;

        if ((number & 0x02) == 0x02)
        {
            // ok, it's just a plain ol' int; we can handle this
            // trivially by casting
            rvalue = raw_number;
        }
        else
        {

            // also trivial, but not as obvious ... left shift the
            // bits high and use that clever static method in Double
            // to convert the resulting bit image to a double
            rvalue = Double.longBitsToDouble(raw_number << 34);
        }
        if ((number & 0x01) == 0x01)
        {

            // low-order bit says divide by 100, and so we do. Why?
            // 'cause that's what the algorithm says. Can't fight city
            // hall, especially if it's the city of Redmond
            rvalue /= 100;
        }

        return rvalue;
    }
}
