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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.ss.ITestDataProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class BaseTestSheetHiding {

	protected final ITestDataProvider _testDataProvider;
    protected Workbook wbH;
    protected Workbook wbU;

    private final String _file1, _file2;

    /**
	 * @param testDataProvider an object that provides test data in HSSF /  specific way
	 */
	protected BaseTestSheetHiding(ITestDataProvider testDataProvider,
                                  String file1, String file2) {
		_testDataProvider = testDataProvider;
        _file1 = file1;
        _file2 = file2;
    }

	@BeforeEach
    void setUp() {
        wbH = _testDataProvider.openSampleWorkbook(_file1);
        wbU = _testDataProvider.openSampleWorkbook(_file2);
    }

	@AfterEach
	void teadDown() throws IOException {
	    wbH.close();
	    wbU.close();
	}

    @Test
    public final void testSheetVisibility() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            wb.createSheet("MySheet");

            assertFalse(wb.isSheetHidden(0));
            assertFalse(wb.isSheetVeryHidden(0));
            assertEquals(SheetVisibility.VISIBLE, wb.getSheetVisibility(0));

            wb.setSheetVisibility(0, SheetVisibility.HIDDEN);
            assertTrue(wb.isSheetHidden(0));
            assertFalse(wb.isSheetVeryHidden(0));
            assertEquals(SheetVisibility.HIDDEN, wb.getSheetVisibility(0));

            wb.setSheetVisibility(0, SheetVisibility.VERY_HIDDEN);
            assertFalse(wb.isSheetHidden(0));
            assertTrue(wb.isSheetVeryHidden(0));
            assertEquals(SheetVisibility.VERY_HIDDEN, wb.getSheetVisibility(0));

            wb.setSheetVisibility(0, SheetVisibility.VISIBLE);
            assertFalse(wb.isSheetHidden(0));
            assertFalse(wb.isSheetVeryHidden(0));
            assertEquals(SheetVisibility.VISIBLE, wb.getSheetVisibility(0));

            // verify limits-check

            // check sheet-index with one more => throws exception
            assertThrows(IllegalArgumentException.class, () -> wb.setSheetVisibility(1, SheetVisibility.HIDDEN));

            // check sheet-index with index out of bounds => throws exception
            assertThrows(IllegalArgumentException.class, () -> wb.setSheetVisibility(10, SheetVisibility.HIDDEN));
        }
    }

    /**
     * Test that we get the right number of sheets,
     *  with the right text on them, no matter what
     *  the hidden flags are
     */
	@Test
    void testTextSheets() {
        // Both should have two sheets
        assertEquals(2, wbH.getNumberOfSheets());
        assertEquals(2, wbU.getNumberOfSheets());

        // All sheets should have one row
        assertEquals(0, wbH.getSheetAt(0).getLastRowNum());
        assertEquals(0, wbH.getSheetAt(1).getLastRowNum());
        assertEquals(0, wbU.getSheetAt(0).getLastRowNum());
        assertEquals(0, wbU.getSheetAt(1).getLastRowNum());

        // All rows should have one column
        assertEquals(1, wbH.getSheetAt(0).getRow(0).getLastCellNum());
        assertEquals(1, wbH.getSheetAt(1).getRow(0).getLastCellNum());
        assertEquals(1, wbU.getSheetAt(0).getRow(0).getLastCellNum());
        assertEquals(1, wbU.getSheetAt(1).getRow(0).getLastCellNum());

        // Text should be sheet based
        assertEquals("Sheet1A1", wbH.getSheetAt(0).getRow(0).getCell(0).getRichStringCellValue().getString());
        assertEquals("Sheet2A1", wbH.getSheetAt(1).getRow(0).getCell(0).getRichStringCellValue().getString());
        assertEquals("Sheet1A1", wbU.getSheetAt(0).getRow(0).getCell(0).getRichStringCellValue().getString());
        assertEquals("Sheet2A1", wbU.getSheetAt(1).getRow(0).getCell(0).getRichStringCellValue().getString());
    }

    /**
     * Check that we can get and set the hidden flags
     *  as expected
     */
	@Test
    void testHideUnHideFlags() {
        assertTrue(wbH.isSheetHidden(0));
        assertFalse(wbH.isSheetHidden(1));
        assertFalse(wbU.isSheetHidden(0));
        assertFalse(wbU.isSheetHidden(1));
    }

    /**
     * Turn the sheet with none hidden into the one with
     *  one hidden
     */
	@Test
    void testHide() throws IOException {
        wbU.setSheetHidden(0, true);
        assertTrue(wbU.isSheetHidden(0));
        assertFalse(wbU.isSheetHidden(1));
        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wbU);
        assertTrue(wb2.isSheetHidden(0));
        assertFalse(wb2.isSheetHidden(1));
        wb2.close();
    }

    /**
     * Turn the sheet with one hidden into the one with
     *  none hidden
     */
	@Test
    void testUnHide() throws IOException {
        wbH.setSheetHidden(0, false);
        assertFalse(wbH.isSheetHidden(0));
        assertFalse(wbH.isSheetHidden(1));
        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wbH);
        assertFalse(wb2.isSheetHidden(0));
        assertFalse(wb2.isSheetHidden(1));
        wb2.close();
    }
}