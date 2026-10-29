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

package org.apache.poi.xssf.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Test;

final class TestArrayFormulaRanges {

    @Test
    void findsRangeHoldingCell() {
        ArrayFormulaRanges ranges = new ArrayFormulaRanges();
        ranges.add(CellRangeAddress.valueOf("B2:C3"));
        ranges.add(CellRangeAddress.valueOf("E2"));

        assertEquals(CellRangeAddress.valueOf("B2:C3"), ranges.find(1, 1));
        assertEquals(CellRangeAddress.valueOf("B2:C3"), ranges.find(2, 2));
        assertEquals(CellRangeAddress.valueOf("E2"), ranges.find(1, 4));
        assertNull(ranges.find(0, 1), "row above the range");
        assertNull(ranges.find(1, 3), "column between the ranges");
        assertNull(ranges.find(3, 1), "row below the range");
    }

    @Test
    void findsRangeSpanningRowBlocks() {
        ArrayFormulaRanges ranges = new ArrayFormulaRanges();
        // rows 50 to 300 cross several 64-row blocks
        CellRangeAddress tall = CellRangeAddress.valueOf("A51:B301");
        ranges.add(tall);

        for (int row = 50; row <= 300; row++) {
            assertEquals(tall, ranges.find(row, 0));
            assertEquals(tall, ranges.find(row, 1));
        }
        assertNull(ranges.find(49, 0));
        assertNull(ranges.find(301, 0));
        assertNull(ranges.find(100, 2));

        ranges.remove(tall);
        for (int row = 50; row <= 300; row++) {
            assertNull(ranges.find(row, 0));
        }
    }

    @Test
    void removeOnlyForgetsThatRange() {
        ArrayFormulaRanges ranges = new ArrayFormulaRanges();
        ranges.add(CellRangeAddress.valueOf("A1:A100"));
        ranges.add(CellRangeAddress.valueOf("B10:B20"));

        ranges.remove(ranges.find(15, 0));

        assertNull(ranges.find(15, 0));
        assertEquals(CellRangeAddress.valueOf("B10:B20"), ranges.find(15, 1));
    }

    @Test
    void addingAnEqualRangeTwiceRecordsItOnce() {
        ArrayFormulaRanges ranges = new ArrayFormulaRanges();
        ranges.add(CellRangeAddress.valueOf("A1:A2"));
        ranges.add(CellRangeAddress.valueOf("A1:A2"));

        ranges.remove(CellRangeAddress.valueOf("A1:A2"));

        assertNull(ranges.find(0, 0));
    }

    @Test
    void laterChangesToTheAddedRangeAreIgnored() {
        ArrayFormulaRanges ranges = new ArrayFormulaRanges();
        CellRangeAddress range = CellRangeAddress.valueOf("A1:A2");
        ranges.add(range);

        range.setLastRow(500);

        assertEquals(CellRangeAddress.valueOf("A1:A2"), ranges.find(1, 0));
        assertNull(ranges.find(400, 0));
    }
}
