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

package org.apache.poi.xssf.util;

public class NumericRanges {

    public static final int NO_OVERLAPS = -1;
    public static final int OVERLAPS_1_MINOR = 0;
    public static final int OVERLAPS_2_MINOR = 1;
    public static final int OVERLAPS_1_WRAPS = 2;
    public static final int OVERLAPS_2_WRAPS = 3;
    
    public static long[] getOverlappingRange(long[] range1, long[] range2) {
        switch(getOverlappingType(range1, range2)) {
            case OVERLAPS_1_MINOR:
                return new long[]{range2[0], range1[1]};
            case OVERLAPS_2_MINOR:
                return new long[]{range1[0], range2[1]};
            case OVERLAPS_2_WRAPS:
                return range1;
            case OVERLAPS_1_WRAPS:
                return range2;
            default:
            case NO_OVERLAPS:
                return new long[]{-1, -1};
        }
    }
    
    public static int getOverlappingType(long[] range1, long[] range2) {
        long min1 = range1[0];
        long max1 = range1[1];
        long min2 = range2[0];
        long max2 = range2[1];
        if (min1 >= min2) {
            if (max1 <= max2) {
                return OVERLAPS_2_WRAPS;
            } else if (min1 <= max2) {
                return OVERLAPS_2_MINOR;
            }
        } else {
            if (max1 >= max2) {
                return OVERLAPS_1_WRAPS;
            } else if (max1 >= min2) {
                return OVERLAPS_1_MINOR;
            }
        }
        return NO_OVERLAPS;
        
    }
    
}
