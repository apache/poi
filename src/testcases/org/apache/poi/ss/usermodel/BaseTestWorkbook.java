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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.NullOutputStream;
import org.apache.poi.util.TempFile;
import org.junit.Test;

public abstract class BaseTestWorkbook {

    protected final ITestDataProvider _testDataProvider;

    protected BaseTestWorkbook(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }
    
    @Test
    public void sheetIterator_forEach() throws IOException {
        final Workbook wb = _testDataProvider.createWorkbook();
        wb.createSheet("Sheet0");
        wb.createSheet("Sheet1");
        wb.createSheet("Sheet2");
        int i = 0;
        for (Sheet sh : wb) {
            assertEquals("Sheet"+i, sh.getSheetName());
            i++;
        }
        wb.close();
    }
    
    /**
     * Expected ConcurrentModificationException:
     * should not be able to advance an iterator when the
     * underlying data has been reordered
     */
    @Test(expected=ConcurrentModificationException.class)
    public void sheetIterator_sheetsReordered() throws IOException {
        final Workbook wb = _testDataProvider.createWorkbook();
        wb.createSheet("Sheet0");
        wb.createSheet("Sheet1");
        wb.createSheet("Sheet2");
        
        Iterator<Sheet> it = wb.sheetIterator();
        it.next();
        wb.setSheetOrder("Sheet2", 1);
        
        // Iterator order should be fixed when iterator is created
        try {
            assertEquals("Sheet1", it.next().getSheetName());
        } finally {
            wb.close();
        }
    }
    
    /**
     * Expected ConcurrentModificationException:
     * should not be able to advance an iterator when the
     * underlying data has been reordered
     */
    @Test(expected=ConcurrentModificationException.class)
    public void sheetIterator_sheetRemoved() throws IOException {
        final Workbook wb = _testDataProvider.createWorkbook();
        wb.createSheet("Sheet0");
        wb.createSheet("Sheet1");
        wb.createSheet("Sheet2");
        
        Iterator<Sheet> it = wb.sheetIterator();
        wb.removeSheetAt(1);
        
        // Iterator order should be fixed when iterator is created
        try {
            it.next();
        } finally {
            wb.close();
        }
    }
    
    /**
     * Expected UnsupportedOperationException:
     * should not be able to remove sheets from the sheet iterator
     */
    @Test(expected=UnsupportedOperationException.class)
    public void sheetIterator_remove() throws IOException {
        final Workbook wb = _testDataProvider.createWorkbook();
        wb.createSheet("Sheet0");
        
        Iterator<Sheet> it = wb.sheetIterator();
        it.next(); //Sheet0
        try {
            it.remove();
        } finally {
            wb.close();
        }
    }


    @Test
    public void createSheet() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        assertEquals(0, wb.getNumberOfSheets());

        //getting a sheet by invalid index or non-existing name
        assertNull(wb.getSheet("Sheet1"));
        try {
            wb.getSheetAt(0);
            fail("should have thrown exceptiuon due to invalid sheet index");
        } catch (IllegalArgumentException e) {
            // expected during successful test
            // no negative index in the range message
            assertFalse(e.getMessage().contains("-1"));
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
            fail("Identified bug 44892");
        }
        assertEquals("Sheet3", fetchedSheet.getSheetName());
        assertEquals(3, wb.getNumberOfSheets());
        assertSame(originalSheet, fetchedSheet);
        try {
            wb.createSheet("sHeeT3");
            fail("should have thrown exceptiuon due to duplicate sheet name");
        } catch (IllegalArgumentException e) {
            // expected during successful test
            assertEquals("The workbook already contains a sheet named 'sHeeT3'", e.getMessage());
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

        //try to assign an invalid name to the 2nd sheet
        try {
            wb.createSheet(null);
            fail("should have thrown exceptiuon due to invalid sheet name");
        } catch (IllegalArgumentException e) {
            // expected during successful test
        }

        try {
            wb.setSheetName(2, null);

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
        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb);
        wb.close();
        assertEquals(3, wb2.getNumberOfSheets());
        assertEquals(0, wb2.getSheetIndex("sheet0"));
        assertEquals(1, wb2.getSheetIndex("sheet1"));
        assertEquals(2, wb2.getSheetIndex("I changed!"));
        wb2.close();
    }

    /**
     * POI allows creating sheets with names longer than 31 characters.
     *
     * Excel opens files with long sheet names without error or warning.
     * However, long sheet names are silently truncated to 31 chars.  In order to
     * avoid funny duplicate sheet name errors, POI enforces uniqueness on only the first 31 chars.
     * but for the purpose of uniqueness long sheet names are silently truncated to 31 chars.
     */
    @Test
    public void createSheetWithLongNames() throws IOException {
        Workbook wb1 = _testDataProvider.createWorkbook();

        String sheetName1 = "My very long sheet name which is longer than 31 chars";
        String truncatedSheetName1 = sheetName1.substring(0, 31);
        Sheet sh1 = wb1.createSheet(sheetName1);
        assertEquals(truncatedSheetName1, sh1.getSheetName());
        assertSame(sh1, wb1.getSheet(truncatedSheetName1));
        // now via wb.setSheetName
        wb1.setSheetName(0, sheetName1);
        assertEquals(truncatedSheetName1, sh1.getSheetName());
        assertSame(sh1, wb1.getSheet(truncatedSheetName1));

        String sheetName2 = "My very long sheet name which is longer than 31 chars " +
                "and sheetName2.substring(0, 31) == sheetName1.substring(0, 31)";
        try {
            /*Sheet sh2 =*/ wb1.createSheet(sheetName2);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            // expected during successful test
            assertEquals("The workbook already contains a sheet named 'My very long sheet name which is longer than 31 chars and sheetName2.substring(0, 31) == sheetName1.substring(0, 31)'", e.getMessage());
        }

        String sheetName3 = "POI allows creating sheets with names longer than 31 characters";
        String truncatedSheetName3 = sheetName3.substring(0, 31);
        Sheet sh3 = wb1.createSheet(sheetName3);
        assertEquals(truncatedSheetName3, sh3.getSheetName());
        assertSame(sh3, wb1.getSheet(truncatedSheetName3));

        //serialize and read again
        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
        assertEquals(2, wb2.getNumberOfSheets());
        assertEquals(0, wb2.getSheetIndex(truncatedSheetName1));
        assertEquals(1, wb2.getSheetIndex(truncatedSheetName3));
        wb2.close();
    }

    @Test
    public void removeSheetAt() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            workbook.createSheet("sheet1");
            workbook.createSheet("sheet2");
            workbook.createSheet("sheet3");
            assertEquals(3, workbook.getNumberOfSheets());

            assertEquals(0, workbook.getActiveSheetIndex());

            workbook.removeSheetAt(1);
            assertEquals(2, workbook.getNumberOfSheets());
            assertEquals("sheet3", workbook.getSheetName(1));
            assertEquals(0, workbook.getActiveSheetIndex());

            workbook.removeSheetAt(0);
            assertEquals(1, workbook.getNumberOfSheets());
            assertEquals("sheet3", workbook.getSheetName(0));
            assertEquals(0, workbook.getActiveSheetIndex());

            workbook.removeSheetAt(0);
            assertEquals(0, workbook.getNumberOfSheets());
            assertEquals(0, workbook.getActiveSheetIndex());

            //re-create the sheets
            workbook.createSheet("sheet1");
            workbook.createSheet("sheet2");
            workbook.createSheet("sheet3");
            workbook.createSheet("sheet4");
            assertEquals(4, workbook.getNumberOfSheets());

            assertEquals(0, workbook.getActiveSheetIndex());
            workbook.setActiveSheet(2);
            assertEquals(2, workbook.getActiveSheetIndex());

            workbook.removeSheetAt(2);
            assertEquals(2, workbook.getActiveSheetIndex());

            workbook.removeSheetAt(1);
            assertEquals(1, workbook.getActiveSheetIndex());

            workbook.removeSheetAt(0);
            assertEquals(0, workbook.getActiveSheetIndex());

            workbook.removeSheetAt(0);
            assertEquals(0, workbook.getActiveSheetIndex());
        }
    }

    @Test
    public void testSetActiveCell() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet("new sheet");
            final CellAddress initialActiveCell = sheet.getActiveCell();
            assertTrue(initialActiveCell == null || new CellAddress("A1").equals(initialActiveCell));
            sheet.setActiveCell(new CellAddress("E11"));
            assertEquals(new CellAddress("E11"), sheet.getActiveCell());

            Workbook wbr = _testDataProvider.writeOutAndReadBack(wb);
            sheet = wbr.getSheet("new sheet");
            assertEquals(new CellAddress("E11"), sheet.getActiveCell());

            //wbr.write(new FileOutputStream("c:/temp/yyy." + _testDataProvider.getStandardFileNameExtension()));
        }
    }

    @Test
    public void defaultValues() throws IOException {
        Workbook b = _testDataProvider.createWorkbook();
        assertEquals(0, b.getActiveSheetIndex());
        assertEquals(0, b.getFirstVisibleTab());
        assertEquals(0, b.getNumberOfNames());
        assertEquals(0, b.getNumberOfSheets());
        b.close();
    }

    @Test
    public void sheetSelection() throws IOException {
        Workbook b = _testDataProvider.createWorkbook();
        b.createSheet("Sheet One");
        b.createSheet("Sheet Two");
        b.setActiveSheet(1);
        b.setSelectedTab(1);
        b.setFirstVisibleTab(1);
        assertEquals(1, b.getActiveSheetIndex());
        assertEquals(1, b.getFirstVisibleTab());
        b.close();
    }

    @Test
    public void printArea() throws IOException {
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
        workbook.close();
    }

    @Test
    public void getSetActiveSheet() throws IOException {
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
        workbook.close();
    }

    @Test
    public void setSheetOrder() throws IOException {
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

        // check active sheet
        assertEquals(0, wb.getActiveSheetIndex());
        
        // Change
        wb.setSheetOrder("Sheet 6", 0);
        assertEquals(1, wb.getActiveSheetIndex());
        wb.setSheetOrder("Sheet 3", 7);
        wb.setSheetOrder("Sheet 1", 9);
        
        // now the first sheet is at index 1
        assertEquals(1, wb.getActiveSheetIndex());

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
        wb.close();

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

        assertEquals(1, wb.getActiveSheetIndex());
        
        // Now get the index by the sheet, not the name
        for(int i=0; i<10; i++) {
        	Sheet s = wbr.getSheetAt(i);
        	assertEquals(i, wbr.getSheetIndex(s));
        }
        
        wbr.close();
    }

    @Test
    public void cloneSheet() throws IOException {
        Workbook book = _testDataProvider.createWorkbook();
        Sheet sheet = book.createSheet("TEST");
        sheet.createRow(0).createCell(0).setCellValue("Test");
        sheet.createRow(1).createCell(0).setCellValue(36.6);
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 2));
        sheet.addMergedRegion(new CellRangeAddress(2, 3, 0, 2));
        assertTrue(sheet.isSelected());

        Sheet clonedSheet = book.cloneSheet(0);
        assertEquals("TEST (2)", clonedSheet.getSheetName());
        assertEquals(2, clonedSheet.getPhysicalNumberOfRows());
        assertEquals(2, clonedSheet.getNumMergedRegions());
        assertFalse(clonedSheet.isSelected());

        //cloned sheet is a deep copy, adding rows or merged regions in the original does not affect the clone
        sheet.createRow(2).createCell(0).setCellValue(1);
        sheet.addMergedRegion(new CellRangeAddress(4, 5, 0, 2));
        assertEquals(2, clonedSheet.getPhysicalNumberOfRows());
        assertEquals(2, clonedSheet.getNumMergedRegions());

        clonedSheet.createRow(2).createCell(0).setCellValue(1);
        clonedSheet.addMergedRegion(new CellRangeAddress(6, 7, 0, 2));
        assertEquals(3, clonedSheet.getPhysicalNumberOfRows());
        assertEquals(3, clonedSheet.getNumMergedRegions());
        book.close();
    }

    @Test
    public void parentReferences() throws IOException {
        Workbook wb1 = _testDataProvider.createWorkbook();
        Sheet sheet = wb1.createSheet();
        assertSame(wb1, sheet.getWorkbook());

        Row row = sheet.createRow(0);
        assertSame(sheet, row.getSheet());

        Cell cell = row.createCell(1);
        assertSame(sheet, cell.getSheet());
        assertSame(row, cell.getRow());

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
        sheet = wb2.getSheetAt(0);
        assertSame(wb2, sheet.getWorkbook());

        row = sheet.getRow(0);
        assertSame(sheet, row.getSheet());

        cell = row.getCell(1);
        assertSame(sheet, cell.getSheet());
        assertSame(row, cell.getRow());
        wb2.close();
    }


    /**
     * Test to validate that replacement for removed setRepeatingRowsAnsColumns() methods
     * is still working correctly 
     */
    @Test
    public void setRepeatingRowsAnsColumns() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();

        CellRangeAddress cra = new CellRangeAddress(0, 3, 0, 0);
        String expRows = "1:4", expCols = "A:A";

        
        Sheet sheet1 = wb.createSheet();
        sheet1.setRepeatingRows(cra);
        sheet1.setRepeatingColumns(cra);
        assertEquals(expRows, sheet1.getRepeatingRows().formatAsString());
        assertEquals(expCols, sheet1.getRepeatingColumns().formatAsString());

        //must handle sheets with quotas, see Bugzilla #47294
        Sheet sheet2 = wb.createSheet("My' Sheet");
        sheet2.setRepeatingRows(cra);
        sheet2.setRepeatingColumns(cra);
        assertEquals(expRows, sheet2.getRepeatingRows().formatAsString());
        assertEquals(expCols, sheet2.getRepeatingColumns().formatAsString());
        wb.close();
    }

    /**
     * Tests that all of the unicode capable string fields can be set, written and then read back
     */
    @Test
    public void unicodeInAll() throws IOException {
        Workbook wb1 = _testDataProvider.createWorkbook();
        CreationHelper factory = wb1.getCreationHelper();
        //Create a unicode dataformat (contains euro symbol)
        DataFormat df = wb1.createDataFormat();
        final String formatStr = "_([$\u20ac-2]\\\\\\ * #,##0.00_);_([$\u20ac-2]\\\\\\ * \\\\\\(#,##0.00\\\\\\);_([$\u20ac-2]\\\\\\ *\\\"\\-\\\\\"??_);_(@_)";
        short fmt = df.getFormat(formatStr);

        //Create a unicode sheet name (euro symbol)
        Sheet s = wb1.createSheet("\u20ac");

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

        /*Cell c2 =*/ r.createCell(2); // TODO - c2 unused but changing next line ('c'->'c2') causes test to fail
        c.setCellValue(factory.createRichTextString("\u20ac"));

        Cell c3 = r.createCell(3);
        String formulaString = "TEXT(12.34,\"\u20ac###,##\")";
        c3.setCellFormula(formulaString);

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();

        //Test the sheetname
        s = wb2.getSheet("\u20ac");
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
        df = wb2.createDataFormat();
        assertEquals(formatStr, df.getFormat(c.getCellStyle().getDataFormat()));

        //Test the cell string value
        /*c2 =*/ r.getCell(2);
        assertEquals(c.getRichStringCellValue().getString(), "\u20ac");

        //Test the cell formula
        c3 = r.getCell(3);
        assertEquals(c3.getCellFormula(), formulaString);
        wb2.close();
    }

    private Workbook newSetSheetNameTestingWorkbook() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sh1 = wb.createSheet("Worksheet");
        Sheet sh2 = wb.createSheet("Testing 47100");
        Sheet sh3 = wb.createSheet("To be renamed");

        Name name1 = wb.createName();
        name1.setNameName("sale_1");
        name1.setRefersToFormula("Worksheet!$A$1");

        Name name2 = wb.createName();
        name2.setNameName("sale_2");
        name2.setRefersToFormula("'Testing 47100'!$A$1");

        Name name3 = wb.createName();
        name3.setNameName("sale_3");
        name3.setRefersToFormula("'Testing 47100'!$B$1");

        Name name4 = wb.createName();
        name4.setNameName("sale_4");
        name4.setRefersToFormula("'To be renamed'!$A$3");

        sh1.createRow(0).createCell(0).setCellFormula("SUM('Testing 47100'!A1:C1)");
        sh1.createRow(1).createCell(0).setCellFormula("SUM('Testing 47100'!A1:C1,'To be renamed'!A1:A5)");
        sh1.createRow(2).createCell(0).setCellFormula("sale_2+sale_3+'Testing 47100'!C1");

        sh2.createRow(0).createCell(0).setCellValue(1);
        sh2.getRow(0).createCell(1).setCellValue(2);
        sh2.getRow(0).createCell(2).setCellValue(3);

        sh3.createRow(0).createCell(0).setCellValue(1);
        sh3.createRow(1).createCell(0).setCellValue(2);
        sh3.createRow(2).createCell(0).setCellValue(3);
        sh3.createRow(3).createCell(0).setCellValue(4);
        sh3.createRow(4).createCell(0).setCellValue(5);
        sh3.createRow(5).createCell(0).setCellFormula("sale_3");
        sh3.createRow(6).createCell(0).setCellFormula("'Testing 47100'!C1");

        return wb;
    }

    /**
     * Ensure that Workbook#setSheetName updates all dependent formulas and named ranges
     *
     * @see <a href="https://issues.apache.org/bugzilla/show_bug.cgi?id=47100">Bugzilla 47100</a>
     */
    @Test
    public void setSheetName() throws IOException {

        Workbook wb1 = newSetSheetNameTestingWorkbook();

        Sheet sh1 = wb1.getSheetAt(0);

        Name sale_2 = wb1.getNameAt(1);
        Name sale_3 = wb1.getNameAt(2);
        Name sale_4 = wb1.getNameAt(3);

        assertEquals("sale_2", sale_2.getNameName());
        assertEquals("'Testing 47100'!$A$1", sale_2.getRefersToFormula());
        assertEquals("sale_3", sale_3.getNameName());
        assertEquals("'Testing 47100'!$B$1", sale_3.getRefersToFormula());
        assertEquals("sale_4", sale_4.getNameName());
        assertEquals("'To be renamed'!$A$3", sale_4.getRefersToFormula());

        FormulaEvaluator evaluator = wb1.getCreationHelper().createFormulaEvaluator();

        Cell cell0 = sh1.getRow(0).getCell(0);
        Cell cell1 = sh1.getRow(1).getCell(0);
        Cell cell2 = sh1.getRow(2).getCell(0);

        assertEquals("SUM('Testing 47100'!A1:C1)", cell0.getCellFormula());
        assertEquals("SUM('Testing 47100'!A1:C1,'To be renamed'!A1:A5)", cell1.getCellFormula());
        assertEquals("sale_2+sale_3+'Testing 47100'!C1", cell2.getCellFormula());

        assertEquals(6.0, evaluator.evaluate(cell0).getNumberValue(), 0);
        assertEquals(21.0, evaluator.evaluate(cell1).getNumberValue(), 0);
        assertEquals(6.0, evaluator.evaluate(cell2).getNumberValue(), 0);

        wb1.setSheetName(1, "47100 - First");
        wb1.setSheetName(2, "47100 - Second");

        assertEquals("sale_2", sale_2.getNameName());
        assertEquals("'47100 - First'!$A$1", sale_2.getRefersToFormula());
        assertEquals("sale_3", sale_3.getNameName());
        assertEquals("'47100 - First'!$B$1", sale_3.getRefersToFormula());
        assertEquals("sale_4", sale_4.getNameName());
        assertEquals("'47100 - Second'!$A$3", sale_4.getRefersToFormula());

        assertEquals("SUM('47100 - First'!A1:C1)", cell0.getCellFormula());
        assertEquals("SUM('47100 - First'!A1:C1,'47100 - Second'!A1:A5)", cell1.getCellFormula());
        assertEquals("sale_2+sale_3+'47100 - First'!C1", cell2.getCellFormula());

        evaluator.clearAllCachedResultValues();
        assertEquals(6.0, evaluator.evaluate(cell0).getNumberValue(), 0);
        assertEquals(21.0, evaluator.evaluate(cell1).getNumberValue(), 0);
        assertEquals(6.0, evaluator.evaluate(cell2).getNumberValue(), 0);

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();

        sh1 = wb2.getSheetAt(0);

        sale_2 = wb2.getNameAt(1);
        sale_3 = wb2.getNameAt(2);
        sale_4 = wb2.getNameAt(3);

        cell0 = sh1.getRow(0).getCell(0);
        cell1 = sh1.getRow(1).getCell(0);
        cell2 = sh1.getRow(2).getCell(0);

        assertEquals("sale_2", sale_2.getNameName());
        assertEquals("'47100 - First'!$A$1", sale_2.getRefersToFormula());
        assertEquals("sale_3", sale_3.getNameName());
        assertEquals("'47100 - First'!$B$1", sale_3.getRefersToFormula());
        assertEquals("sale_4", sale_4.getNameName());
        assertEquals("'47100 - Second'!$A$3", sale_4.getRefersToFormula());

        assertEquals("SUM('47100 - First'!A1:C1)", cell0.getCellFormula());
        assertEquals("SUM('47100 - First'!A1:C1,'47100 - Second'!A1:A5)", cell1.getCellFormula());
        assertEquals("sale_2+sale_3+'47100 - First'!C1", cell2.getCellFormula());

        evaluator = wb2.getCreationHelper().createFormulaEvaluator();
        assertEquals(6.0, evaluator.evaluate(cell0).getNumberValue(), 0);
        assertEquals(21.0, evaluator.evaluate(cell1).getNumberValue(), 0);
        assertEquals(6.0, evaluator.evaluate(cell2).getNumberValue(), 0);
        wb2.close();
    }

    protected void changeSheetNameWithSharedFormulas(String sampleFile) throws IOException {
        Workbook wb = _testDataProvider.openSampleWorkbook(sampleFile);

        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sheet = wb.getSheetAt(0);

        for (int rownum = 1; rownum <= 40; rownum++) {
            Cell cellA = sheet.getRow(1).getCell(0);
            Cell cellB = sheet.getRow(1).getCell(1);

            assertEquals(cellB.getStringCellValue(), evaluator.evaluate(cellA).getStringValue());
        }

        wb.setSheetName(0, "Renamed by POI");
        evaluator.clearAllCachedResultValues();

        for (int rownum = 1; rownum <= 40; rownum++) {
            Cell cellA = sheet.getRow(1).getCell(0);
            Cell cellB = sheet.getRow(1).getCell(1);

            assertEquals(cellB.getStringCellValue(), evaluator.evaluate(cellA).getStringValue());
        }
        wb.close();
    }

	protected void assertSheetOrder(Workbook wb, String... sheets) {
		StringBuilder sheetNames = new StringBuilder();
		for(int i = 0;i < wb.getNumberOfSheets();i++) {
			sheetNames.append(wb.getSheetAt(i).getSheetName()).append(",");
		}
		assertEquals("Had: " + sheetNames,
				sheets.length, wb.getNumberOfSheets());
		for(int i = 0;i < wb.getNumberOfSheets();i++) {
			assertEquals("Had: " + sheetNames,
					sheets[i], wb.getSheetAt(i).getSheetName());
		}
	}

    @Test
    public void test58499() throws IOException {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();
        for (int i = 0; i < 900; i++) {
            Row r = sheet.createRow(i);
            Cell c = r.createCell(0);
            CellStyle cs = workbook.createCellStyle();
            c.setCellStyle(cs);
            c.setCellValue("AAA");                
        }
        try (OutputStream os = new NullOutputStream()) {
            workbook.write(os);
        }
        //workbook.dispose();
        workbook.close();
    }


    @Test
    public void windowOneDefaults() throws IOException {
        Workbook b = _testDataProvider.createWorkbook();
        try {
            assertEquals(b.getActiveSheetIndex(), 0);
            assertEquals(b.getFirstVisibleTab(), 0);
        } catch (NullPointerException npe) {
            fail("WindowOneRecord in Workbook is probably not initialized");
        }
        
        b.close();
    }

    @Test
    public void getSpreadsheetVersion() throws IOException {
        final Workbook wb = _testDataProvider.createWorkbook();
        assertEquals(_testDataProvider.getSpreadsheetVersion(), wb.getSpreadsheetVersion());
        wb.close();
    }
    
    /* FIXME copied from {@link org.apache.poi.ss.TestWorkbookFactory} */
    protected static void assertCloseDoesNotModifyFile(String filename, Workbook wb) throws IOException {
        final byte[] before = HSSFTestDataSamples.getTestDataFileContent(filename);
        wb.close();
        final byte[] after = HSSFTestDataSamples.getTestDataFileContent(filename);
        assertArrayEquals(filename + " sample file was modified as a result of closing the workbook",
                before, after);
    }

    @Test
    public void sheetClone() throws IOException {
        // First up, try a simple file
        final Workbook b = _testDataProvider.createWorkbook();
        assertEquals(0, b.getNumberOfSheets());
        b.createSheet("Sheet One");
        b.createSheet("Sheet Two");

        assertEquals(2, b.getNumberOfSheets());
        b.cloneSheet(0);
        assertEquals(3, b.getNumberOfSheets());

        // Now try a problem one with drawing records in it
        Workbook bBack = HSSFTestDataSamples.openSampleWorkbook("SheetWithDrawing.xls");
        assertEquals(1, bBack.getNumberOfSheets());
        bBack.cloneSheet(0);
        assertEquals(2, bBack.getNumberOfSheets());

        bBack.close();
        b.close();
    }

    @Test
    public void getSheetIndex() throws IOException {
        final Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet1 = wb.createSheet("Sheet1");
        Sheet sheet2 = wb.createSheet("Sheet2");
        Sheet sheet3 = wb.createSheet("Sheet3");
        Sheet sheet4 = wb.createSheet("Sheet4");

        assertEquals(0, wb.getSheetIndex(sheet1));
        assertEquals(1, wb.getSheetIndex(sheet2));
        assertEquals(2, wb.getSheetIndex(sheet3));
        assertEquals(3, wb.getSheetIndex(sheet4));

        // remove sheets
        wb.removeSheetAt(0);
        wb.removeSheetAt(2);

        // ensure that sheets are moved up and removed sheets are not found any more
        assertEquals(-1, wb.getSheetIndex(sheet1));
        assertEquals(0, wb.getSheetIndex(sheet2));
        assertEquals(1, wb.getSheetIndex(sheet3));
        assertEquals(-1, wb.getSheetIndex(sheet4));

        wb.close();
    }

    @Test
    public void addSheetTwice() throws IOException {
        final Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet1 = wb.createSheet("Sheet1");
        assertNotNull(sheet1);
        try {
            wb.createSheet("Sheet1");
            fail("Should fail if we add the same sheet twice");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("already contains a sheet named 'Sheet1'"));
        }

        wb.close();
    }
    
    // bug 51233 and 55075: correctly size image if added to a row with a custom height
    @Test
    public void createDrawing() throws Exception {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet("Main Sheet");
        Row row0 = sheet.createRow(0);
        Row row1 = sheet.createRow(1);
        row1.createCell(0);
        row0.createCell(1);
        row1.createCell(0);
        row1.createCell(1);

        byte[] pictureData = _testDataProvider.getTestDataFileContent("logoKarmokar4.png");

        int handle = wb.addPicture(pictureData, Workbook.PICTURE_TYPE_PNG);
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        CreationHelper helper = wb.getCreationHelper();
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setAnchorType(AnchorType.DONT_MOVE_AND_RESIZE);
        anchor.setCol1(0);
        anchor.setRow1(0);
        Picture picture = drawing.createPicture(anchor, handle);

        row0.setHeightInPoints(144);
        // set a column width so that XSSF and SXSSF have the same width (default widths may be different otherwise)
        sheet.setColumnWidth(0, 100*256);
        picture.resize();
        
        // The actual dimensions don't matter as much as having XSSF and SXSSF produce the same size drawings
        
        // Check drawing height
        assertEquals(0, anchor.getRow1());
        assertEquals(0, anchor.getRow2());
        assertEquals(0, anchor.getDy1());
        assertEquals(1609725, anchor.getDy2()); //HSSF: 225
        
        // Check drawing width
        assertEquals(0, anchor.getCol1());
        assertEquals(0, anchor.getCol2());
        assertEquals(0, anchor.getDx1());
        assertEquals(1114425, anchor.getDx2()); //HSSF: 171
        
        final boolean writeOut = false;
        if (writeOut) {
            String ext = "." + _testDataProvider.getStandardFileNameExtension();
            String prefix = wb.getClass().getName() + "-createDrawing";
            File f = TempFile.createTempFile(prefix, ext);
            FileOutputStream out = new FileOutputStream(f);
            wb.write(out);
            out.close();
        }
        wb.close();
    }

}
