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

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

/**
 * Tests CellRangeUtil.
 *
 * @see org.apache.poi.ss.util.CellRangeUtil
 */
public final class TestCellRangeUtil {
    
    private static final CellRangeAddress A1 = new CellRangeAddress(0, 0, 0, 0);
    private static final CellRangeAddress B1 = new CellRangeAddress(0, 0, 1, 1);
    private static final CellRangeAddress A2 = new CellRangeAddress(1, 1, 0, 0);
    private static final CellRangeAddress B2 = new CellRangeAddress(1, 1, 1, 1);
    
    @Test
    public void testMergeCellRanges() {
        testMergeCellRange(asArray(A1, B1, A2, B2), 1);
        // suboptimal result for permuted arguments (2 ranges instead of one)
        testMergeCellRange(asArray(A1, B2, A2, A1), 2);
        
        testMergeCellRange(asArray(A1, B1, A2), 2);
        testMergeCellRange(asArray(A2, A1, B1), 2);
        testMergeCellRange(asArray(B1, A2, A1), 2);
        
        testMergeCellRange(asArray(A1, B2), 2);
        testMergeCellRange(asArray(B2, A1), 2);
    }
    
    private void testMergeCellRange(CellRangeAddress[] input, int expectedResultRangeLength) {
        CellRangeAddress[] result = CellRangeUtil.mergeCellRanges( input );
        assertEquals( expectedResultRangeLength, result.length );
        assertResultExactlyContainsInput(result, result);
    }
    
    private static void assertResultExactlyContainsInput(CellRangeAddress[] result, CellRangeAddress[] input) {
        for(CellRangeAddress inputEntry: input) {
            boolean isInResultRange = false;
            for(CellRangeAddress resultEntry: result) {
                isInResultRange |= resultEntry.intersects(inputEntry);
            }
            assumeTrue(isInResultRange);
        }
    }
    
    private static <T> T[] asArray(T...ts) {
        return ts;
    }
}
