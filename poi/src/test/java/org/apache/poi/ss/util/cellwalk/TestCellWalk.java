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
package org.apache.poi.ss.util.cellwalk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.SheetBuilder;
import org.junit.jupiter.api.Test;

class TestCellWalk {

    private static Object[][] testData = new Object[][]{
            {1, 2, null},
            {null, new Date(), null},
            {null, null, "str"}
    };

    @Test
    void testNotTraverseEmptyCells() {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = new SheetBuilder(wb, testData).build();
        CellRangeAddress range = CellRangeAddress.valueOf("A1:C3");

        CellWalk cellWalk = new CellWalk(sheet, range);
		int[] cellsVisited = { 0 };
		long[] ordinalNumberSum = { 0 };
        cellWalk.traverse((cell,ctx) -> {
			cellsVisited[0]++;
			ordinalNumberSum[0] += ctx.getOrdinalNumber();
		});

        assertEquals(4, cellsVisited[0]);
        /* 1 + 2 + 5 + 9 */
        assertEquals(17L, ordinalNumberSum[0]);
    }
}