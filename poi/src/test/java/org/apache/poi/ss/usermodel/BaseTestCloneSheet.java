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

package org.apache.poi.ss.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Test;

/**
 * Common superclass for testing implementations of
 * Workbook.cloneSheet()
 */
public abstract class BaseTestCloneSheet {

    private final ITestDataProvider _testDataProvider;

    protected BaseTestCloneSheet(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    @Test
    void testCloneSheetBasic() throws IOException{
        Workbook b = _testDataProvider.createWorkbook();
        Sheet s = b.createSheet("Test");
        assertEquals(0, s.addMergedRegion(new CellRangeAddress(0, 1, 0, 1)));
        Sheet clonedSheet = b.cloneSheet(0);

        assertEquals(1, clonedSheet.getNumMergedRegions(), "One merged area");

        b.close();
    }

    /**
     * Ensures that pagebreak cloning works properly
     */
    @Test
    void testPageBreakClones() throws IOException {
        Workbook b = _testDataProvider.createWorkbook();
        Sheet s = b.createSheet("Test");
        s.setRowBreak(3);
        s.setColumnBreak((short) 6);

        Sheet clone = b.cloneSheet(0);
        assertTrue(clone.isRowBroken(3), "Row 3 not broken");
        assertTrue(clone.isColumnBroken((short) 6), "Column 6 not broken");

        s.removeRowBreak(3);

        assertTrue(clone.isRowBroken(3), "Row 3 still should be broken");

        b.close();
    }

    @Test
    void testCloneSheetIntValid() {
        Workbook wb = _testDataProvider.createWorkbook();
        wb.createSheet("Sheet01");
        wb.cloneSheet(0);
        assertEquals(2, wb.getNumberOfSheets());
        assertThrows(IllegalArgumentException.class, () -> wb.cloneSheet(2));
    }

    @Test
    void testCloneSheetIntInvalid() {
        Workbook wb = _testDataProvider.createWorkbook();
        wb.createSheet("Sheet01");
        assertThrows(IllegalArgumentException.class, () -> wb.cloneSheet(1));
        assertEquals(1, wb.getNumberOfSheets());
    }
}
