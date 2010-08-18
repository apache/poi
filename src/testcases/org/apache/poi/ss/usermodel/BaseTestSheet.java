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

import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Common superclass for testing {@link org.apache.poi.xssf.usermodel.XSSFCell}  and
 * {@link org.apache.poi.hssf.usermodel.HSSFCell}
 */
public abstract class BaseTestSheet extends TestCase {

    private final ITestDataProvider _testDataProvider;

    protected BaseTestSheet(ITestDataProvider testDataProvider) {
    _testDataProvider = testDataProvider;
    }

    public void testCreateRow() {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();
        assertEquals(0, sheet.getPhysicalNumberOfRows());

        //Test that we get null for undefined rownumber
        assertNull(sheet.getRow(1));

        // Test row creation with consecutive indexes
        Row row1 = sheet.createRow(0);
        Row row2 = sheet.createRow(1);
        assertEquals(0, row1.getRowNum());
        assertEquals(1, row2.getRowNum());
        Iterator<Row> it = sheet.rowIterator();
        assertTrue(it.hasNext());
        assertSame(row1, it.next());
        assertTrue(it.hasNext());
        assertSame(row2, it.next());
        assertEquals(1, sheet.getLastRowNum());

        // Test row creation with non consecutive index
        Row row101 = sheet.createRow(100);
        assertNotNull(row101);
        assertEquals(100, sheet.getLastRowNum());
        assertEquals(3, sheet.getPhysicalNumberOfRows());

        // Test overwriting an existing row
        Row row2_ovrewritten = sheet.createRow(1);
        Cell cell = row2_ovrewritten.createCell(0);
        cell.setCellValue(100);
        Iterator<Row> it2 = sheet.rowIterator();
        assertTrue(it2.hasNext());
        assertSame(row1, it2.next());
        assertTrue(it2.hasNext());
        Row row2_ovrewritten_ref = it2.next();
        assertSame(row2_ovrewritten, row2_ovrewritten_ref);
        assertEquals(100.0, row2_ovrewritten_ref.getCell(0).getNumericCellValue(), 0.0);
    }


    public void testRemoveRow() {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet1 = workbook.createSheet();
        assertEquals(0, sheet1.getPhysicalNumberOfRows());
        assertEquals(0, sheet1.getFirstRowNum());
        assertEquals(0, sheet1.getLastRowNum());

        Row row0 = sheet1.createRow(0);
        assertEquals(1, sheet1.getPhysicalNumberOfRows());
        assertEquals(0, sheet1.getFirstRowNum());
        assertEquals(0, sheet1.getLastRowNum());
        sheet1.removeRow(row0);
        assertEquals(0, sheet1.getPhysicalNumberOfRows());
        assertEquals(0, sheet1.getFirstRowNum());
        assertEquals(0, sheet1.getLastRowNum());

        sheet1.createRow(1);
        Row row2 = sheet1.createRow(2);
        assertEquals(2, sheet1.getPhysicalNumberOfRows());
        assertEquals(1, sheet1.getFirstRowNum());
        assertEquals(2, sheet1.getLastRowNum());

        assertNotNull(sheet1.getRow(1));
        assertNotNull(sheet1.getRow(2));
        sheet1.removeRow(row2);
        assertNotNull(sheet1.getRow(1));
        assertNull(sheet1.getRow(2));
        assertEquals(1, sheet1.getPhysicalNumberOfRows());
        assertEquals(1, sheet1.getFirstRowNum());
        assertEquals(1, sheet1.getLastRowNum());

        Row row3 = sheet1.createRow(3);
        Sheet sheet2 = workbook.createSheet();
        try {
            sheet2.removeRow(row3);
            fail("Expected exception");
        } catch (IllegalArgumentException e){
            assertEquals("Specified row does not belong to this sheet", e.getMessage());
        }
    }

    public void testCloneSheet() {
        Workbook workbook = _testDataProvider.createWorkbook();
        CreationHelper factory = workbook.getCreationHelper();
        Sheet sheet = workbook.createSheet("Test Clone");
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        Cell cell2 = row.createCell(1);
        cell.setCellValue(factory.createRichTextString("clone_test"));
        cell2.setCellFormula("SIN(1)");

        Sheet clonedSheet = workbook.cloneSheet(0);
        Row clonedRow = clonedSheet.getRow(0);

        //Check for a good clone
        assertEquals(clonedRow.getCell(0).getRichStringCellValue().getString(), "clone_test");

        //Check that the cells are not somehow linked
        cell.setCellValue(factory.createRichTextString("Difference Check"));
        cell2.setCellFormula("cos(2)");
        if ("Difference Check".equals(clonedRow.getCell(0).getRichStringCellValue().getString())) {
            fail("string cell not properly cloned");
        }
        if ("COS(2)".equals(clonedRow.getCell(1).getCellFormula())) {
            fail("formula cell not properly cloned");
        }
        assertEquals(clonedRow.getCell(0).getRichStringCellValue().getString(), "clone_test");
        assertEquals(clonedRow.getCell(1).getCellFormula(), "SIN(1)");
    }

    /** tests that the sheet name for multiple clones of the same sheet is unique
     * BUG 37416
     */
    public void testCloneSheetMultipleTimes() {
        Workbook workbook = _testDataProvider.createWorkbook();
        CreationHelper factory = workbook.getCreationHelper();
        Sheet sheet = workbook.createSheet("Test Clone");
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(factory.createRichTextString("clone_test"));
        //Clone the sheet multiple times
        workbook.cloneSheet(0);
        workbook.cloneSheet(0);

        assertNotNull(workbook.getSheet("Test Clone"));
        assertNotNull(workbook.getSheet("Test Clone (2)"));
        assertEquals("Test Clone (3)", workbook.getSheetName(2));
        assertNotNull(workbook.getSheet("Test Clone (3)"));

        workbook.removeSheetAt(0);
        workbook.removeSheetAt(0);
        workbook.removeSheetAt(0);
        workbook.createSheet("abc ( 123)");
        workbook.cloneSheet(0);
        assertEquals("abc (124)", workbook.getSheetName(1));
    }

    /**
     * Setting landscape and portrait stuff on new sheets
     */
    public void testPrintSetupLandscapeNew() {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheetL = workbook.createSheet("LandscapeS");
        Sheet sheetP = workbook.createSheet("LandscapeP");

        // Check two aspects of the print setup
        assertFalse(sheetL.getPrintSetup().getLandscape());
        assertFalse(sheetP.getPrintSetup().getLandscape());
        assertEquals(1, sheetL.getPrintSetup().getCopies());
        assertEquals(1, sheetP.getPrintSetup().getCopies());

        // Change one on each
        sheetL.getPrintSetup().setLandscape(true);
        sheetP.getPrintSetup().setCopies((short)3);

        // Check taken
        assertTrue(sheetL.getPrintSetup().getLandscape());
        assertFalse(sheetP.getPrintSetup().getLandscape());
        assertEquals(1, sheetL.getPrintSetup().getCopies());
        assertEquals(3, sheetP.getPrintSetup().getCopies());

        // Save and re-load, and check still there
        workbook = _testDataProvider.writeOutAndReadBack(workbook);
        sheetL = workbook.getSheet("LandscapeS");
        sheetP = workbook.getSheet("LandscapeP");

        assertTrue(sheetL.getPrintSetup().getLandscape());
        assertFalse(sheetP.getPrintSetup().getLandscape());
        assertEquals(1, sheetL.getPrintSetup().getCopies());
        assertEquals(3, sheetP.getPrintSetup().getCopies());
    }

    /**
     * Test adding merged regions. If the region's bounds are outside of the allowed range
     * then an IllegalArgumentException should be thrown
     *
     */
    public void testAddMerged() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
        assertEquals(0, sheet.getNumMergedRegions());
        SpreadsheetVersion ssVersion = _testDataProvider.getSpreadsheetVersion();

        CellRangeAddress region = new CellRangeAddress(0, 1, 0, 1);
        sheet.addMergedRegion(region);
        assertEquals(1, sheet.getNumMergedRegions());

        try {
            region = new CellRangeAddress(-1, -1, -1, -1);
            sheet.addMergedRegion(region);
            fail("Expected exception");
        } catch (IllegalArgumentException e){
// TODO           assertEquals("Minimum row number is 0.", e.getMessage());
        }
        try {
            region = new CellRangeAddress(0, 0, 0, ssVersion.getLastColumnIndex() + 1);
            sheet.addMergedRegion(region);
            fail("Expected exception");
        } catch (IllegalArgumentException e){
            assertEquals("Maximum column number is " + ssVersion.getLastColumnIndex(), e.getMessage());
        }
        try {
            region = new CellRangeAddress(0, ssVersion.getLastRowIndex() + 1, 0, 1);
            sheet.addMergedRegion(region);
            fail("Expected exception");
        } catch (IllegalArgumentException e){
            assertEquals("Maximum row number is " + ssVersion.getLastRowIndex(), e.getMessage());
        }
        assertEquals(1, sheet.getNumMergedRegions());
    }

    /**
     * When removing one merged region, it would break
     *
     */
    public void testRemoveMerged() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
        CellRangeAddress region = new CellRangeAddress(0, 1, 0, 1);
        sheet.addMergedRegion(region);
        region = new CellRangeAddress(1, 2, 0, 1);
        sheet.addMergedRegion(region);

        sheet.removeMergedRegion(0);

        region = sheet.getMergedRegion(0);
        assertEquals("Left over region should be starting at row 1", 1, region.getFirstRow());

        sheet.removeMergedRegion(0);

        assertEquals("there should be no merged regions left!", 0, sheet.getNumMergedRegions());

        //an, add, remove, get(0) would null pointer
        sheet.addMergedRegion(region);
        assertEquals("there should now be one merged region!", 1, sheet.getNumMergedRegions());
        sheet.removeMergedRegion(0);
        assertEquals("there should now be zero merged regions!", 0, sheet.getNumMergedRegions());
        //add it again!
        region.setLastRow(4);

        sheet.addMergedRegion(region);
        assertEquals("there should now be one merged region!", 1, sheet.getNumMergedRegions());

        //should exist now!
        assertTrue("there isn't more than one merged region in there", 1 <= sheet.getNumMergedRegions());
        region = sheet.getMergedRegion(0);
        assertEquals("the merged row to doesnt match the one we put in ", 4, region.getLastRow());
    }

    public void testShiftMerged() {
        Workbook wb = _testDataProvider.createWorkbook();
        CreationHelper factory = wb.getCreationHelper();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(factory.createRichTextString("first row, first cell"));

        row = sheet.createRow(1);
        cell = row.createCell(1);
        cell.setCellValue(factory.createRichTextString("second row, second cell"));

        CellRangeAddress region = new CellRangeAddress(1, 1, 0, 1);
        sheet.addMergedRegion(region);

        sheet.shiftRows(1, 1, 1);

        region = sheet.getMergedRegion(0);
        assertEquals("Merged region not moved over to row 2", 2, region.getFirstRow());
    }

    /**
     * Tests the display of gridlines, formulas, and rowcolheadings.
     * @author Shawn Laubach (slaubach at apache dot org)
     */
    public void testDisplayOptions() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();

        assertEquals(sheet.isDisplayGridlines(), true);
        assertEquals(sheet.isDisplayRowColHeadings(), true);
        assertEquals(sheet.isDisplayFormulas(), false);
        assertEquals(sheet.isDisplayZeros(), true);

        sheet.setDisplayGridlines(false);
        sheet.setDisplayRowColHeadings(false);
        sheet.setDisplayFormulas(true);
        sheet.setDisplayZeros(false);

        wb = _testDataProvider.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);

        assertEquals(sheet.isDisplayGridlines(), false);
        assertEquals(sheet.isDisplayRowColHeadings(), false);
        assertEquals(sheet.isDisplayFormulas(), true);
        assertEquals(sheet.isDisplayZeros(), false);
    }

    public void testColumnWidth() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();

        //default column width measured in characters
        sheet.setDefaultColumnWidth(10);
        assertEquals(10, sheet.getDefaultColumnWidth());
        //columns A-C have default width
        assertEquals(256*10, sheet.getColumnWidth(0));
        assertEquals(256*10, sheet.getColumnWidth(1));
        assertEquals(256*10, sheet.getColumnWidth(2));

        //set custom width for D-F
        for (char i = 'D'; i <= 'F'; i++) {
            //Sheet#setColumnWidth accepts the width in units of 1/256th of a character width
            int w = 256*12;
            sheet.setColumnWidth(i, w);
            assertEquals(w, sheet.getColumnWidth(i));
        }
        //reset the default column width, columns A-C change, D-F still have custom width
        sheet.setDefaultColumnWidth(20);
        assertEquals(20, sheet.getDefaultColumnWidth());
        assertEquals(256*20, sheet.getColumnWidth(0));
        assertEquals(256*20, sheet.getColumnWidth(1));
        assertEquals(256*20, sheet.getColumnWidth(2));
        for (char i = 'D'; i <= 'F'; i++) {
            int w = 256*12;
            assertEquals(w, sheet.getColumnWidth(i));
        }

        // check for 16-bit signed/unsigned error:
        sheet.setColumnWidth(10, 40000);
        assertEquals(40000, sheet.getColumnWidth(10));

        //The maximum column width for an individual cell is 255 characters
        try {
            sheet.setColumnWidth(9, 256*256);
            fail("expected exception");
        } catch(IllegalArgumentException e){
            assertEquals("The maximum column width for an individual cell is 255 characters.", e.getMessage());
        }

        //serialize and read again
        wb = _testDataProvider.writeOutAndReadBack(wb);

        sheet = wb.getSheetAt(0);
        assertEquals(20, sheet.getDefaultColumnWidth());
        //columns A-C have default width
        assertEquals(256*20, sheet.getColumnWidth(0));
        assertEquals(256*20, sheet.getColumnWidth(1));
        assertEquals(256*20, sheet.getColumnWidth(2));
        //columns D-F have custom width
        for (char i = 'D'; i <= 'F'; i++) {
            short w = (256*12);
            assertEquals(w, sheet.getColumnWidth(i));
        }
        assertEquals(40000, sheet.getColumnWidth(10));
    }

    public void testDefaultRowHeight() {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();
        sheet.setDefaultRowHeightInPoints(15);
        assertEquals((short) 300, sheet.getDefaultRowHeight());
        assertEquals(15.0F, sheet.getDefaultRowHeightInPoints(), 0F);

        // Set a new default row height in twips and test getting the value in points
        sheet.setDefaultRowHeight((short) 360);
        assertEquals(18.0f, sheet.getDefaultRowHeightInPoints(), 0F);
        assertEquals((short) 360, sheet.getDefaultRowHeight());

        // Test that defaultRowHeight is a truncated short: E.G. 360inPoints -> 18; 361inPoints -> 18
        sheet.setDefaultRowHeight((short) 361);
        assertEquals((float)361/20, sheet.getDefaultRowHeightInPoints(), 0F);
        assertEquals((short) 361, sheet.getDefaultRowHeight());

        // Set a new default row height in points and test getting the value in twips
        sheet.setDefaultRowHeightInPoints(17.5f);
        assertEquals(17.5f, sheet.getDefaultRowHeightInPoints(), 0F);
        assertEquals((short)(17.5f*20), sheet.getDefaultRowHeight());
    }

    /** cell with formula becomes null on cloning a sheet*/
     public void test35084() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet s = wb.createSheet("Sheet1");
        Row r = s.createRow(0);
        r.createCell(0).setCellValue(1);
        r.createCell(1).setCellFormula("A1*2");
        Sheet s1 = wb.cloneSheet(0);
        r = s1.getRow(0);
        assertEquals("double", r.getCell(0).getNumericCellValue(), 1, 0); // sanity check
        assertNotNull(r.getCell(1));
        assertEquals("formula", r.getCell(1).getCellFormula(), "A1*2");
    }

    /** test that new default column styles get applied */
    public void testDefaultColumnStyle() {
        Workbook wb = _testDataProvider.createWorkbook();
        CellStyle style = wb.createCellStyle();
        Sheet sheet = wb.createSheet();
        sheet.setDefaultColumnStyle(0, style);
        assertNotNull(sheet.getColumnStyle(0));
        assertEquals(style.getIndex(), sheet.getColumnStyle(0).getIndex());

        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        CellStyle style2 = cell.getCellStyle();
        assertNotNull(style2);
        assertEquals("style should match", style.getIndex(), style2.getIndex());
    }

    public void testOutlineProperties() {
        Workbook wb = _testDataProvider.createWorkbook();

        Sheet sheet = wb.createSheet();

        //TODO defaults are different in HSSF and XSSF
        //assertTrue(sheet.getRowSumsBelow());
        //assertTrue(sheet.getRowSumsRight());

        sheet.setRowSumsBelow(false);
        sheet.setRowSumsRight(false);

        assertFalse(sheet.getRowSumsBelow());
        assertFalse(sheet.getRowSumsRight());

        sheet.setRowSumsBelow(true);
        sheet.setRowSumsRight(true);

        assertTrue(sheet.getRowSumsBelow());
        assertTrue(sheet.getRowSumsRight());

        wb = _testDataProvider.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);
        assertTrue(sheet.getRowSumsBelow());
        assertTrue(sheet.getRowSumsRight());
    }

    /**
     * Test basic display properties
     */
    public void testSheetProperties() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();

        assertFalse(sheet.getHorizontallyCenter());
        sheet.setHorizontallyCenter(true);
        assertTrue(sheet.getHorizontallyCenter());
        sheet.setHorizontallyCenter(false);
        assertFalse(sheet.getHorizontallyCenter());

        assertFalse(sheet.getVerticallyCenter());
        sheet.setVerticallyCenter(true);
        assertTrue(sheet.getVerticallyCenter());
        sheet.setVerticallyCenter(false);
        assertFalse(sheet.getVerticallyCenter());

        assertFalse(sheet.isPrintGridlines());
        sheet.setPrintGridlines(true);
        assertTrue(sheet.isPrintGridlines());

        assertFalse(sheet.isDisplayFormulas());
        sheet.setDisplayFormulas(true);
        assertTrue(sheet.isDisplayFormulas());

        assertTrue(sheet.isDisplayGridlines());
        sheet.setDisplayGridlines(false);
        assertFalse(sheet.isDisplayGridlines());

        //TODO: default "guts" is different in HSSF and XSSF
        //assertTrue(sheet.getDisplayGuts());
        sheet.setDisplayGuts(false);
        assertFalse(sheet.getDisplayGuts());

        assertTrue(sheet.isDisplayRowColHeadings());
        sheet.setDisplayRowColHeadings(false);
        assertFalse(sheet.isDisplayRowColHeadings());

        //TODO: default "autobreaks" is different in HSSF and XSSF
        //assertTrue(sheet.getAutobreaks());
        sheet.setAutobreaks(false);
        assertFalse(sheet.getAutobreaks());

        assertFalse(sheet.getScenarioProtect());

        //TODO: default "fit-to-page" is different in HSSF and XSSF
        //assertFalse(sheet.getFitToPage());
        sheet.setFitToPage(true);
        assertTrue(sheet.getFitToPage());
        sheet.setFitToPage(false);
        assertFalse(sheet.getFitToPage());
    }

    public void baseTestGetSetMargin(double[] defaultMargins) {
        double marginLeft = defaultMargins[0];
        double marginRight = defaultMargins[1];
        double marginTop = defaultMargins[2];
        double marginBottom = defaultMargins[3];
        double marginHeader = defaultMargins[4];
        double marginFooter = defaultMargins[5];

        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        assertEquals(marginLeft, sheet.getMargin(Sheet.LeftMargin), 0.0);
        sheet.setMargin(Sheet.LeftMargin, 10.0);
        //left margin is custom, all others are default
        assertEquals(10.0, sheet.getMargin(Sheet.LeftMargin), 0.0);
        assertEquals(marginRight, sheet.getMargin(Sheet.RightMargin), 0.0);
        assertEquals(marginTop, sheet.getMargin(Sheet.TopMargin), 0.0);
        assertEquals(marginBottom, sheet.getMargin(Sheet.BottomMargin), 0.0);
        sheet.setMargin(Sheet.RightMargin, 11.0);
        assertEquals(11.0, sheet.getMargin(Sheet.RightMargin), 0.0);
        sheet.setMargin(Sheet.TopMargin, 12.0);
        assertEquals(12.0, sheet.getMargin(Sheet.TopMargin), 0.0);
        sheet.setMargin(Sheet.BottomMargin, 13.0);
        assertEquals(13.0, sheet.getMargin(Sheet.BottomMargin), 0.0);

        // incorrect margin constant
        try {
            sheet.setMargin((short) 65, 15);
            fail("Expected exception");
        } catch (IllegalArgumentException e){
            assertEquals("Unknown margin constant:  65", e.getMessage());
        }
    }

    public void testRowBreaks() {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();
        //Sheet#getRowBreaks() returns an empty array if no row breaks are defined
        assertNotNull(sheet.getRowBreaks());
        assertEquals(0, sheet.getRowBreaks().length);

        sheet.setRowBreak(1);
        assertEquals(1, sheet.getRowBreaks().length);
        sheet.setRowBreak(15);
        assertEquals(2, sheet.getRowBreaks().length);
        assertEquals(1, sheet.getRowBreaks()[0]);
        assertEquals(15, sheet.getRowBreaks()[1]);
        sheet.setRowBreak(1);
        assertEquals(2, sheet.getRowBreaks().length);
        assertTrue(sheet.isRowBroken(1));
        assertTrue(sheet.isRowBroken(15));

        //now remove the created breaks
        sheet.removeRowBreak(1);
        assertEquals(1, sheet.getRowBreaks().length);
        sheet.removeRowBreak(15);
        assertEquals(0, sheet.getRowBreaks().length);

        assertFalse(sheet.isRowBroken(1));
        assertFalse(sheet.isRowBroken(15));
    }

    public void testColumnBreaks() {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();
        assertNotNull(sheet.getColumnBreaks());
        assertEquals(0, sheet.getColumnBreaks().length);

        assertFalse(sheet.isColumnBroken(0));

        sheet.setColumnBreak(11);
        assertNotNull(sheet.getColumnBreaks());
        assertEquals(11, sheet.getColumnBreaks()[0]);
        sheet.setColumnBreak(12);
        assertEquals(2, sheet.getColumnBreaks().length);
        assertTrue(sheet.isColumnBroken(11));
        assertTrue(sheet.isColumnBroken(12));

        sheet.removeColumnBreak((short) 11);
        assertEquals(1, sheet.getColumnBreaks().length);
        sheet.removeColumnBreak((short) 15); //remove non-existing
        assertEquals(1, sheet.getColumnBreaks().length);
        sheet.removeColumnBreak((short) 12);
        assertEquals(0, sheet.getColumnBreaks().length);

        assertFalse(sheet.isColumnBroken(11));
        assertFalse(sheet.isColumnBroken(12));
    }

    public void testGetFirstLastRowNum() {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        sheet.createRow(9);
        sheet.createRow(0);
        sheet.createRow(1);
        assertEquals(0, sheet.getFirstRowNum());
        assertEquals(9, sheet.getLastRowNum());
    }

    public void testGetFooter() {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        assertNotNull(sheet.getFooter());
        sheet.getFooter().setCenter("test center footer");
        assertEquals("test center footer", sheet.getFooter().getCenter());
    }

    public void testGetSetColumnHidden() {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        sheet.setColumnHidden(2, true);
        assertTrue(sheet.isColumnHidden(2));
    }

    public void testProtectSheet() {

    	Workbook wb = _testDataProvider.createWorkbook();
    	Sheet sheet = wb.createSheet();
        assertFalse(sheet.getProtect());
    	sheet.protectSheet("Test");  
    	assertTrue(sheet.getProtect());
    	sheet.protectSheet(null);
    	assertFalse(sheet.getProtect());

    }


}
