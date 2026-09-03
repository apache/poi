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

package org.apache.poi.ooxml.util;

import org.apache.poi.util.Internal;

/**
 * Helper class for number related operations.
 * <p>Note: This class is for internal POI usage only.</p>
 *
 * @since 5.5.0
 */
@Internal
public class NumberHelper {
    private NumberHelper() {
        // no instances of this class
    }

    /**
     * @param number the number to convert
     * @return the double representation of the number
     * @throws IllegalArgumentException if the number cannot be converted
     */
    public static double toDouble(Object number) {
        if (number instanceof Number num) {
            return num.doubleValue();
        } else if (number instanceof String str) {
            return Double.parseDouble(str);
        }
        throw new IllegalArgumentException("Cannot convert of class" + number.getClass().getName() +
                " to double");
    }

}
