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

import junit.framework.TestCase;


public class TestNumericRanges extends TestCase {
    
    public void testGetOverlappingType() {
        long[] r1 = {3, 8};
        long[] r2 = {6, 11};
        long[] r3 = {1, 5};
        long[] r4 = {2, 20};
        long[] r5 = {5, 6};
        long[] r6 = {20, 23};
        assertEquals(NumericRanges.OVERLAPS_1_MINOR, NumericRanges.getOverlappingType(r1, r2));
        assertEquals(NumericRanges.OVERLAPS_2_MINOR, NumericRanges.getOverlappingType(r1, r3));
        assertEquals(NumericRanges.OVERLAPS_2_WRAPS, NumericRanges.getOverlappingType(r1, r4));
        assertEquals(NumericRanges.OVERLAPS_1_WRAPS, NumericRanges.getOverlappingType(r1, r5));
        assertEquals(NumericRanges.NO_OVERLAPS, NumericRanges.getOverlappingType(r1, r6));
    }
    
    public void testGetOverlappingRange() {
        long[] r1 = {3, 8};
        long[] r2 = {6, 11};
        long[] r3 = {1, 5};
        long[] r4 = {2, 20};
        long[] r5 = {5, 6};
        long[] r6 = {20, 23};
        assertEquals(6, NumericRanges.getOverlappingRange(r1, r2)[0]);
        assertEquals(8, NumericRanges.getOverlappingRange(r1, r2)[1]);
        assertEquals(3, NumericRanges.getOverlappingRange(r1, r3)[0]);
        assertEquals(5, NumericRanges.getOverlappingRange(r1, r3)[1]);
        assertEquals(3, NumericRanges.getOverlappingRange(r1, r4)[0]);
        assertEquals(8, NumericRanges.getOverlappingRange(r1, r4)[1]);
        assertEquals(5, NumericRanges.getOverlappingRange(r1, r5)[0]);
        assertEquals(6, NumericRanges.getOverlappingRange(r1, r5)[1]);
        assertEquals(-1, NumericRanges.getOverlappingRange(r1, r6)[0]);
        assertEquals(-1, NumericRanges.getOverlappingRange(r1, r6)[1]);
    }
    
}
