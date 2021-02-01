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

import static org.apache.logging.log4j.util.Unbox.box;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;

class TestXSSFSheetMergeRegions {

    private static final Logger LOG = LogManager.getLogger(TestXSSFSheetMergeRegions.class);

    @Test
    void testMergeRegionsSpeed() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("57893-many-merges.xlsx")) {
            long millis = Long.MAX_VALUE;

            // in order to reduce the number of false positives we run it a few times before we fail,
            // sometimes it fails on machines that are busy at the moment.
            for (int i = 0; i < 5; i++) {
                millis = runTest(wb);
                if (millis < 2000) {
                    break;
                }
                LOG.atInfo().log("Retry {} because run-time is too high: {}", box(i),box(millis));
            }

            boolean inGump = false;
            String version = System.getProperty("version.id");
            if (version != null && version.startsWith("gump-")) {
                inGump = true;
            }

            // This time is typically ~800ms, versus ~7800ms to iterate getMergedRegion(int).
            // when running in Gump, the VM is very slow, so we should allow much more time
            assertTrue(inGump ? millis < 8000 : millis < 2000,
                "Should have taken <2000 ms to iterate 50k merged regions but took " + millis);
        }
    }

    private long runTest(final XSSFWorkbook wb) {
        final long start = System.currentTimeMillis();
        final XSSFSheet sheet = wb.getSheetAt(0);
        final List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        assertEquals(50000, mergedRegions.size());
        for (CellRangeAddress cellRangeAddress : mergedRegions) {
            assertEquals(cellRangeAddress.getFirstRow(), cellRangeAddress.getLastRow());
            assertEquals(2, cellRangeAddress.getNumberOfCells());
        }
        return System.currentTimeMillis() - start;
    }
}
