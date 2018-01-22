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
import static org.junit.Assert.assertArrayEquals;
import java.util.Set;
import java.util.HashSet;
import org.apache.commons.collections4.IteratorUtils;

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
    private static final CellRangeAddress A1_B2 = new CellRangeAddress(0, 1, 0, 1);
    private static final CellRangeAddress A1_B1 = new CellRangeAddress(0, 0, 0, 1);
    private static final CellRangeAddress A1_A2 = new CellRangeAddress(0, 1, 0, 0);
    
    @Test
    public void testMergeCellRanges() {
        // Note that the order of the output array elements does not matter
        // And that there may be more than one valid outputs for a given input. Any valid output is accepted.
        // POI should use a strategy that is consistent and predictable (it currently is not).

        // Fully mergeable
        //    A B
        //  1 x x   A1,A2,B1,B2 --> A1:B2
        //  2 x x
        assertCellRangesEqual(asArray(A1_B2), merge(A1, B1, A2, B2));
        assertCellRangesEqual(asArray(A1_B2), merge(A1, B2, A2, B1));

        // Partially mergeable: multiple possible merges
        //    A B
        //  1 x x   A1,A2,B1 --> A1:B1,A2 or A1:A2,B1
        //  2 x 
        assertCellRangesEqual(asArray(A1_B1, A2), merge(A1, B1, A2));
        assertCellRangesEqual(asArray(A1_A2, B1), merge(A2, A1, B1));
        assertCellRangesEqual(asArray(A1_B1, A2), merge(B1, A2, A1));

        // Not mergeable
        //    A B
        //  1 x     A1,B2 --> A1,B2
        //  2   x
        assertCellRangesEqual(asArray(A1, B2), merge(A1, B2));
        assertCellRangesEqual(asArray(B2, A1), merge(B2, A1));
    }

    private void assertCellRangesEqual(CellRangeAddress[] a, CellRangeAddress[] b) {
        assertEquals(getCellAddresses(a), getCellAddresses(b));
        assertArrayEquals(a, b);
    }

    private static Set<CellAddress> getCellAddresses(CellRangeAddress[] ranges) {
        final Set<CellAddress> set = new HashSet<>();
        for (final CellRangeAddress range : ranges) {
            set.addAll(IteratorUtils.toList(range.iterator()));
        }
        return set;
    }

    private static CellRangeAddress[] asArray(CellRangeAddress...ts) {
        return ts;
    }

    private static CellRangeAddress[] merge(CellRangeAddress... ranges) {
        return CellRangeUtil.mergeCellRanges(ranges);
    }
}
