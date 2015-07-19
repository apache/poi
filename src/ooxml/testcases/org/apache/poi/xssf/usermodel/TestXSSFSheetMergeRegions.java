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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Test;

public class TestXSSFSheetMergeRegions {
    @Test
    public void testMergeRegionsSpeed() throws IOException {
        final XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("57893-many-merges.xlsx");
        try {
            final XSSFSheet sheet = wb.getSheetAt(0);
            final long start = System.currentTimeMillis();
            final List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
            assertEquals(50000, mergedRegions.size());
            for (CellRangeAddress cellRangeAddress : mergedRegions) {
                assertEquals(cellRangeAddress.getFirstRow(), cellRangeAddress.getLastRow());
                assertEquals(2, cellRangeAddress.getNumberOfCells());
            }
            long millis = System.currentTimeMillis() - start;
            // This time is typically ~800ms, versus ~7800ms to iterate getMergedRegion(int).
            assertTrue("Should have taken <2000 ms to iterate 50k merged regions but took " + millis, millis < 2000);
        } finally {
            wb.close();
        }
    }
}
