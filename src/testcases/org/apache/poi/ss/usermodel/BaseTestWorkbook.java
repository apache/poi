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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * @author Yegor Kozlov
 */
public abstract class BaseTestWorkbook extends TestCase {

    private final ITestDataProvider _testDataProvider;

    protected BaseTestWorkbook(ITestDataProvider testDataProvider) {
    _testDataProvider = testDataProvider;
    }

    public final void testCreateSheet() {
        Workbook wb = _testDataProvider.createWorkbook();
        assertEquals(0, wb.getNumberOfSheets());

        //getting a sheet by invalid index or non-existing name
        assertNull(wb.getSheet("Sheet1"));
        try {
            wb.getSheetAt(0);
            fail("should have thrown exceptiuon due to invalid sheet index");
        } catch (IllegalArgumentException e) {
            // expected during successful test
        }

        Sheet sheet0 = wb.createSheet();
        Sheet sheet1 = wb.createSheet();
        assertEquals("Sheet0", sheet0.getSheetName());
        assertEquals("Sheet1", sheet1.getSheetName());
        assertEquals(2, wb.getNumberOfSheets());

        //fetching sheets by name is case-insensitive
        Sheet originalSheet = wb.createSheet("Sheet3");
        Sheet fetchedSheet = wb.getSheet("sheet3");
        if (fetchedSheet == null) {
            throw new AssertionFailedError("Identified bug 44892");
        }
        assertEquals("Sheet3", fetchedSheet.getSheetName());
        assertEquals(3, wb.getNumberOfSheets());
        assertSame(originalSheet, fetchedSheet);
        try {
            wb.createSheet("sHeeT3");
            fail("should have thrown exceptiuon due to duplicate sheet name");
        } catch (IllegalArgumentException e) {
            // expected during successful test
            assertEquals("The workbook already contains a sheet of this name", e.getMessage());
        }

        //names cannot be blank or contain any of /\*?[]
        String[] invalidNames = {"", "Sheet/", "Sheet\\",
                "Sheet?", "Sheet*", "Sheet[", "Sheet]", "'Sheet'",
                "My:Sheet"};
        for (String sheetName : invalidNames) {
            try {
                wb.createSheet(sheetName);
                fail("should have thrown exception due to invalid sheet name: " + sheetName);
            } catch (IllegalArgumentException e) {
                // expected during successful test
            }
        }
        //still have 3 sheets
        assertEquals(3, wb.getNumberOfSheets());

        //change the name of the 3rd sheet
        wb.setSheetName(2, "I changed!");

        //try to assign an invalid name to the 2nd sheet
        try {
            wb.setSheetName(1, "[I'm invalid]");
            fail("should have thrown exceptiuon due to invalid sheet name");
        } catch (IllegalArgumentException e) {
            // expected during successful test
        }

        //check
        assertEquals(0, wb.getSheetIndex("sheet0"));
        assertEquals(1, wb.getSheetIndex("sheet1"));
        assertEquals(2, wb.getSheetIndex("I changed!"));

        assertSame(sheet0, wb.getSheet("sheet0"));
        assertSame(sheet1, wb.getSheet("sheet1"));
        assertSame(originalSheet, wb.getSheet("I changed!"));
        assertNull(wb.getSheet("unknown"));

        //serialize and read again
        wb = _testDataProvider.writeOutAndReadBack(wb);
        assertEquals(3, wb.getNumberOfSheets());
        assertEquals(0, wb.getSheetIndex("sheet0"));
        assertEquals(1, wb.getSheetIndex("sheet1"));
        assertEquals(2, wb.getSheetIndex("I changed!"));
    }

    /**
     * POI allows creating sheets with names longer than 31 characters.
     *
     * Excel opens files with long sheet names without error or warning.
     * However, long sheet names are silently truncated to 31 chars.  In order to
     * avoid funny duplicate sheet name errors, POI enforces uniqueness on only the first 31 chars.
     * but for the purpose of uniqueness long sheet names are silently truncated to 31 chars.
     */
    public final void testCreateSheetWithLongNames() {
        Workbook wb = _testDataProvider.createWorkbook();

        String sheetName1 = "My very long sheet name which is longer than 31 chars";
        Sheet sh1 = wb.createSheet(sheetName1);
        assertEquals(sheetName1, sh1.getSheetName());
        assertSame(sh1, wb.getSheet(sheetName1));

        String sheetName2 = "My very long sheet name which is longer than 31 chars " +
                "and sheetName2.substring(0, 31) == sheetName1.substring(0, 31)";
        try {
            Sheet sh2 = wb.createSheet(sheetName2);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            // expected during successful test
            assertEquals("The workbook already contains a sheet of this name", e.getMessage());
        }

        String sheetName3 = "POI allows creating sheets with names longer than 31 characters";
        Sheet sh3 = wb.createSheet(sheetName3);
        assertEquals(sheetName3, sh3.getSheetName());
        assertSame(sh3, wb.getSheet(sheetName3));

        //serialize and read again
        wb = _testDataProvider.writeOutAndReadBack(wb);
        assertEquals(2, wb.getNumberOfSheets());
        assertEquals(0, wb.getSheetIndex(sheetName1));
        assertEquals(1, wb.getSheetIndex(sheetName3));
    }

    public final void testRemoveSheetAt() {
        Workbook workbook = _testDataProvider.createWorkbook();
        workbook.createSheet("sheet1");
        workbook.createSheet("sheet2");
        workbook.createSheet("sheet3");
        assertEquals(3, workbook.getNumberOfSheets());
        workbook.removeSheetAt(1);
        assertEquals(2, workbook.getNumberOfSheets());
        assertEquals("sheet3", workbook.getSheetName(1));
        workbook.removeSheetAt(0);
        assertEquals(1, workbook.getNumberOfSheets());
        assertEquals("sheet3", workbook.getSheetName(0));
        workbook.removeSheetAt(0);
        assertEquals(0, workbook.getNumberOfSheets());

        //re-create the sheets
        workbook.createSheet("sheet1");
        workbook.createSheet("sheet2");
        workbook.createSheet("sheet3");
        assertEquals(3, workbook.getNumberOfSheets());
    }

    public final void testDefaultValues() {
        Workbook b = _testDataProvider.createWorkbook();
        assertEquals(0, b.getActiveSheetIndex());
        assertEquals(0, b.getFirstVisibleTab());
        assertEquals(0, b.getNumberOfNames());
        assertEquals(0, b.getNumberOfSheets());
    }

    public final void testSheetSelection() {
        Workbook b = _testDataProvider.createWorkbook();
        b.createSheet("Sheet One");
        b.createSheet("Sheet Two");
        b.setActiveSheet(1);
        b.setSelectedTab(1);
        b.setFirstVisibleTab(1);
        assertEquals(1, b.getActiveSheetIndex());
        assertEquals(1, b.getFirstVisibleTab());
    }

    public final void testPrintArea() {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet1 = workbook.createSheet("Test Print Area");
        String sheetName1 = sheet1.getSheetName();

        // workbook.setPrintArea(0, reference);
        workbook.setPrintArea(0, 1, 5, 4, 9);
        String retrievedPrintArea = workbook.getPrintArea(0);
        assertEquals("'" + sheetName1 + "'!$B$5:$F$10", retrievedPrintArea);

        String reference = "$A$1:$B$1";
        workbook.setPrintArea(0, reference);
        retrievedPrintArea = workbook.getPrintArea(0);
        assertEquals("'" + sheetName1 + "'!" + reference, retrievedPrintArea);

        workbook.removePrintArea(0);
        assertNull(workbook.getPrintArea(0));
    }

    public final void testGetSetActiveSheet(){
        Workbook workbook = _testDataProvider.createWorkbook();
        assertEquals(0, workbook.getActiveSheetIndex());

        workbook.createSheet("sheet1");
        workbook.createSheet("sheet2");
        workbook.createSheet("sheet3");
        // set second sheet
        workbook.setActiveSheet(1);
        // test if second sheet is set up
        assertEquals(1, workbook.getActiveSheetIndex());

        workbook.setActiveSheet(0);
        // test if second sheet is set up
        assertEquals(0, workbook.getActiveSheetIndex());
    }

    public final void testSetSheetOrder() {
        Workbook wb = _testDataProvider.createWorkbook();

        for (int i=0; i < 10; i++) {
            wb.createSheet("Sheet " + i);
        }

        // Check the initial order
        assertEquals(0, wb.getSheetIndex("Sheet 0"));
        assertEquals(1, wb.getSheetIndex("Sheet 1"));
        assertEquals(2, wb.getSheetIndex("Sheet 2"));
        assertEquals(3, wb.getSheetIndex("Sheet 3"));
        assertEquals(4, wb.getSheetIndex("Sheet 4"));
        assertEquals(5, wb.getSheetIndex("Sheet 5"));
        assertEquals(6, wb.getSheetIndex("Sheet 6"));
        assertEquals(7, wb.getSheetIndex("Sheet 7"));
        assertEquals(8, wb.getSheetIndex("Sheet 8"));
        assertEquals(9, wb.getSheetIndex("Sheet 9"));

        // Change
        wb.setSheetOrder("Sheet 6", 0);
        wb.setSheetOrder("Sheet 3", 7);
        wb.setSheetOrder("Sheet 1", 9);

        // Check they're currently right
        assertEquals(0, wb.getSheetIndex("Sheet 6"));
        assertEquals(1, wb.getSheetIndex("Sheet 0"));
        assertEquals(2, wb.getSheetIndex("Sheet 2"));
        assertEquals(3, wb.getSheetIndex("Sheet 4"));
        assertEquals(4, wb.getSheetIndex("Sheet 5"));
        assertEquals(5, wb.getSheetIndex("Sheet 7"));
        assertEquals(6, wb.getSheetIndex("Sheet 3"));
        assertEquals(7, wb.getSheetIndex("Sheet 8"));
        assertEquals(8, wb.getSheetIndex("Sheet 9"));
        assertEquals(9, wb.getSheetIndex("Sheet 1"));

        Workbook wbr = _testDataProvider.writeOutAndReadBack(wb);

        assertEquals(0, wbr.getSheetIndex("Sheet 6"));
        assertEquals(1, wbr.getSheetIndex("Sheet 0"));
        assertEquals(2, wbr.getSheetIndex("Sheet 2"));
        assertEquals(3, wbr.getSheetIndex("Sheet 4"));
        assertEquals(4, wbr.getSheetIndex("Sheet 5"));
        assertEquals(5, wbr.getSheetIndex("Sheet 7"));
        assertEquals(6, wbr.getSheetIndex("Sheet 3"));
        assertEquals(7, wbr.getSheetIndex("Sheet 8"));
        assertEquals(8, wbr.getSheetIndex("Sheet 9"));
        assertEquals(9, wbr.getSheetIndex("Sheet 1"));

        // Now get the index by the sheet, not the name
        for(int i=0; i<10; i++) {
        	Sheet s = wbr.getSheetAt(i);
        	assertEquals(i, wbr.getSheetIndex(s));
        }
    }

    public final void testCloneSheet() {
        Workbook book = _testDataProvider.createWorkbook();
        Sheet sheet = book.createSheet("TEST");
        sheet.createRow(0).createCell(0).setCellValue("Test");
        sheet.createRow(1).createCell(0).setCellValue(36.6);
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 2));
        sheet.addMergedRegion(new CellRangeAddress(1, 2, 0, 2));
        assertTrue(sheet.isSelected());

        Sheet clonedSheet = book.cloneSheet(0);
        assertEquals("TEST (2)", clonedSheet.getSheetName());
        assertEquals(2, clonedSheet.getPhysicalNumberOfRows());
        assertEquals(2, clonedSheet.getNumMergedRegions());
        assertFalse(clonedSheet.isSelected());

        //cloned sheet is a deep copy, adding rows in the original does not affect the clone
        sheet.createRow(2).createCell(0).setCellValue(1);
        sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 2));
        assertEquals(2, clonedSheet.getPhysicalNumberOfRows());
        assertEquals(2, clonedSheet.getPhysicalNumberOfRows());

        clonedSheet.createRow(2).createCell(0).setCellValue(1);
        clonedSheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 2));
        assertEquals(3, clonedSheet.getPhysicalNumberOfRows());
        assertEquals(3, clonedSheet.getPhysicalNumberOfRows());

    }

    public final void testParentReferences(){
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();
        assertSame(workbook, sheet.getWorkbook());

        Row row = sheet.createRow(0);
        assertSame(sheet, row.getSheet());

        Cell cell = row.createCell(1);
        assertSame(sheet, cell.getSheet());
        assertSame(row, cell.getRow());

        workbook = _testDataProvider.writeOutAndReadBack(workbook);
        sheet = workbook.getSheetAt(0);
        assertSame(workbook, sheet.getWorkbook());

        row = sheet.getRow(0);
        assertSame(sheet, row.getSheet());

        cell = row.getCell(1);
        assertSame(sheet, cell.getSheet());
        assertSame(row, cell.getRow());
    }

    public final void testSetRepeatingRowsAnsColumns(){
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet1 = wb.createSheet();
        wb.setRepeatingRowsAndColumns(wb.getSheetIndex(sheet1), 0, 0, 0, 3);

        //must handle sheets with quotas, see Bugzilla #47294
        Sheet sheet2 = wb.createSheet("My' Sheet");
        wb.setRepeatingRowsAndColumns(wb.getSheetIndex(sheet2), 0, 0, 0, 3);
    }

    /**
     * Tests that all of the unicode capable string fields can be set, written and then read back
     */
    public final void testUnicodeInAll() {
        Workbook wb = _testDataProvider.createWorkbook();
        CreationHelper factory = wb.getCreationHelper();
        //Create a unicode dataformat (contains euro symbol)
        DataFormat df = wb.createDataFormat();
        final String formatStr = "_([$\u20ac-2]\\\\\\ * #,##0.00_);_([$\u20ac-2]\\\\\\ * \\\\\\(#,##0.00\\\\\\);_([$\u20ac-2]\\\\\\ *\\\"\\-\\\\\"??_);_(@_)";
        short fmt = df.getFormat(formatStr);

        //Create a unicode sheet name (euro symbol)
        Sheet s = wb.createSheet("\u20ac");

        //Set a unicode header (you guessed it the euro symbol)
        Header h = s.getHeader();
        h.setCenter("\u20ac");
        h.setLeft("\u20ac");
        h.setRight("\u20ac");

        //Set a unicode footer
        Footer f = s.getFooter();
        f.setCenter("\u20ac");
        f.setLeft("\u20ac");
        f.setRight("\u20ac");

        Row r = s.createRow(0);
        Cell c = r.createCell(1);
        c.setCellValue(12.34);
        c.getCellStyle().setDataFormat(fmt);

        Cell c2 = r.createCell(2); // TODO - c2 unused but changing next line ('c'->'c2') causes test to fail
        c.setCellValue(factory.createRichTextString("\u20ac"));

        Cell c3 = r.createCell(3);
        String formulaString = "TEXT(12.34,\"\u20ac###,##\")";
        c3.setCellFormula(formulaString);

        wb = _testDataProvider.writeOutAndReadBack(wb);

        //Test the sheetname
        s = wb.getSheet("\u20ac");
        assertNotNull(s);

        //Test the header
        h = s.getHeader();
        assertEquals(h.getCenter(), "\u20ac");
        assertEquals(h.getLeft(), "\u20ac");
        assertEquals(h.getRight(), "\u20ac");

        //Test the footer
        f = s.getFooter();
        assertEquals(f.getCenter(), "\u20ac");
        assertEquals(f.getLeft(), "\u20ac");
        assertEquals(f.getRight(), "\u20ac");

        //Test the dataformat
        r = s.getRow(0);
        c = r.getCell(1);
        df = wb.createDataFormat();
        assertEquals(formatStr, df.getFormat(c.getCellStyle().getDataFormat()));

        //Test the cell string value
        c2 = r.getCell(2);
        assertEquals(c.getRichStringCellValue().getString(), "\u20ac");

        //Test the cell formula
        c3 = r.getCell(3);
        assertEquals(c3.getCellFormula(), formulaString);
    }
}
