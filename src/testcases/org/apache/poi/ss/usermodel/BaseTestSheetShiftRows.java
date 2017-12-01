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

import static org.apache.poi.POITestCase.skipTest;
import static org.apache.poi.POITestCase.testPassesNow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.junit.Test;

/**
 * Tests row shifting capabilities.
 *
 * @author Shawn Laubach (slaubach at apache dot com)
 * @author Toshiaki Kamoshida (kamoshida.toshiaki at future dot co dot jp)
 */
public abstract class BaseTestSheetShiftRows {

    private final ITestDataProvider _testDataProvider;

    protected BaseTestSheetShiftRows(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    /**
     * Tests the shiftRows function.  Does three different shifts.
     * After each shift, writes the workbook to file and reads back to
     * check.  This ensures that if some changes code that breaks
     * writing or what not, they realize it.
     */
    @Test
    public final void testShiftRows() throws IOException {
        // Read initial file in
        String sampleName = "SimpleMultiCell." + _testDataProvider.getStandardFileNameExtension();
        Workbook wb1 = _testDataProvider.openSampleWorkbook(sampleName);
        Sheet s = wb1.getSheetAt( 0 );

        // Shift the second row down 1 and write to temp file
        s.shiftRows( 1, 1, 1 );

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();

        // Read from temp file and check the number of cells in each
        // row (in original file each row was unique)
        s = wb2.getSheetAt( 0 );

        assertEquals(s.getRow(0).getPhysicalNumberOfCells(), 1);
        confirmEmptyRow(s, 1);
        assertEquals(s.getRow(2).getPhysicalNumberOfCells(), 2);
        assertEquals(s.getRow(3).getPhysicalNumberOfCells(), 4);
        assertEquals(s.getRow(4).getPhysicalNumberOfCells(), 5);

        // Shift rows 1-3 down 3 in the current one.  This tests when
        // 1 row is blank.  Write to a another temp file
        s.shiftRows( 0, 2, 3 );
        Workbook wb3 = _testDataProvider.writeOutAndReadBack(wb2);
        wb2.close();

        // Read and ensure things are where they should be
        s = wb3.getSheetAt(0);
        confirmEmptyRow(s, 0);
        confirmEmptyRow(s, 1);
        confirmEmptyRow(s, 2);
        assertEquals(s.getRow(3).getPhysicalNumberOfCells(), 1);
        confirmEmptyRow(s, 4);
        assertEquals(s.getRow(5).getPhysicalNumberOfCells(), 2);

        wb3.close();
        
        // Read the first file again
        Workbook wb4 = _testDataProvider.openSampleWorkbook(sampleName);
        s = wb4.getSheetAt( 0 );

        // Shift rows 3 and 4 up and write to temp file
        s.shiftRows( 2, 3, -2 );
        Workbook wb5 = _testDataProvider.writeOutAndReadBack(wb4);
        wb4.close();
        s = wb5.getSheetAt( 0 );
        assertEquals(s.getRow(0).getPhysicalNumberOfCells(), 3);
        assertEquals(s.getRow(1).getPhysicalNumberOfCells(), 4);
        confirmEmptyRow(s, 2);
        confirmEmptyRow(s, 3);
        assertEquals(s.getRow(4).getPhysicalNumberOfCells(), 5);
        wb5.close();
    }
    private static void confirmEmptyRow(Sheet s, int rowIx) {
        Row row = s.getRow(rowIx);
        assertTrue(row == null || row.getPhysicalNumberOfCells() == 0);
    }

    /**
     * Tests when rows are null.
     */
    @Test
    public final void testShiftRow() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet s = wb.createSheet();
        s.createRow(0).createCell(0).setCellValue("TEST1");
        s.createRow(3).createCell(0).setCellValue("TEST2");
        s.shiftRows(0,4,1);
        wb.close();
    }

    /**
     * When shifting rows, the page breaks should go with it
     */
    @Test
    public void testShiftRowBreaks() throws IOException { // TODO - enable XSSF test
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet s = wb.createSheet();
        Row row = s.createRow(4);
        row.createCell(0).setCellValue("test");
        s.setRowBreak(4);

        s.shiftRows(4, 4, 2);
        assertTrue("Row number 6 should have a pagebreak", s.isRowBroken(6));
        wb.close();
    }

    @Test
    public void testShiftWithComments() throws IOException {
        Workbook wb1 = _testDataProvider.openSampleWorkbook("comments." + _testDataProvider.getStandardFileNameExtension());

        Sheet sheet = wb1.getSheet("Sheet1");
        assertEquals(3, sheet.getLastRowNum());

        // Verify comments are in the position expected
        assertNotNull(sheet.getCellComment(new CellAddress(0,0)));
        assertNull(sheet.getCellComment(new CellAddress(1,0)));
        assertNotNull(sheet.getCellComment(new CellAddress(2,0)));
        assertNotNull(sheet.getCellComment(new CellAddress(3,0)));

        String comment1 = sheet.getCellComment(new CellAddress(0,0)).getString().getString();
        assertEquals(comment1,"comment top row1 (index0)\n");
        String comment3 = sheet.getCellComment(new CellAddress(2,0)).getString().getString();
        assertEquals(comment3,"comment top row3 (index2)\n");
        String comment4 = sheet.getCellComment(new CellAddress(3,0)).getString().getString();
        assertEquals(comment4,"comment top row4 (index3)\n");

        //Workbook wbBack = _testDataProvider.writeOutAndReadBack(wb);

        // Shifting all but first line down to test comments shifting
        sheet.shiftRows(1, sheet.getLastRowNum(), 1, true, true);

        // Test that comments were shifted as expected
        assertEquals(4, sheet.getLastRowNum());
        assertNotNull(sheet.getCellComment(new CellAddress(0,0)));
        assertNull(sheet.getCellComment(new CellAddress(1,0)));
        assertNull(sheet.getCellComment(new CellAddress(2,0)));
        assertNotNull(sheet.getCellComment(new CellAddress(3,0)));
        assertNotNull(sheet.getCellComment(new CellAddress(4,0)));

        String comment1_shifted = sheet.getCellComment(new CellAddress(0,0)).getString().getString();
        assertEquals(comment1,comment1_shifted);
        String comment3_shifted = sheet.getCellComment(new CellAddress(3,0)).getString().getString();
        assertEquals(comment3,comment3_shifted);
        String comment4_shifted = sheet.getCellComment(new CellAddress(4,0)).getString().getString();
        assertEquals(comment4,comment4_shifted);

        // Write out and read back in again
        // Ensure that the changes were persisted
        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
        
        sheet = wb2.getSheet("Sheet1");
        assertEquals(4, sheet.getLastRowNum());

        // Verify comments are in the position expected after the shift
        assertNotNull(sheet.getCellComment(new CellAddress(0,0)));
        assertNull(sheet.getCellComment(new CellAddress(1,0)));
        assertNull(sheet.getCellComment(new CellAddress(2,0)));
        assertNotNull(sheet.getCellComment(new CellAddress(3,0)));
        assertNotNull(sheet.getCellComment(new CellAddress(4,0)));

        comment1_shifted = sheet.getCellComment(new CellAddress(0,0)).getString().getString();
        assertEquals(comment1,comment1_shifted);
        comment3_shifted = sheet.getCellComment(new CellAddress(3,0)).getString().getString();
        assertEquals(comment3,comment3_shifted);
        comment4_shifted = sheet.getCellComment(new CellAddress(4,0)).getString().getString();
        assertEquals(comment4,comment4_shifted);

        // Shifting back up again, now two rows
        sheet.shiftRows(2, sheet.getLastRowNum(), -2, true, true);

        // TODO: it seems HSSFSheet does not correctly remove comments from rows that are overwritten
        // by shifting rows...
        if(!(wb2 instanceof HSSFWorkbook)) {
            assertEquals(2, sheet.getLastRowNum());
            
            // Verify comments are in the position expected
            assertNull("Had: " + (sheet.getCellComment(new CellAddress(0,0)) == null ? "null" : sheet.getCellComment(new CellAddress(0,0)).getString()),
                    sheet.getCellComment(new CellAddress(0,0)));
            assertNotNull(sheet.getCellComment(new CellAddress(1,0)));
            assertNotNull(sheet.getCellComment(new CellAddress(2,0)));
        }

        comment1 = sheet.getCellComment(new CellAddress(1,0)).getString().getString();
        assertEquals(comment1,"comment top row3 (index2)\n");
        String comment2 = sheet.getCellComment(new CellAddress(2,0)).getString().getString();
        assertEquals(comment2,"comment top row4 (index3)\n");
        
        wb2.close();
    }

    @Test
    public final void testShiftWithNames() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet1 = wb.createSheet("Sheet1");
        wb.createSheet("Sheet2");
        Row row = sheet1.createRow(0);
        row.createCell(0).setCellValue(1.1);
        row.createCell(1).setCellValue(2.2);

        Name name1 = wb.createName();
        name1.setNameName("name1");
        name1.setRefersToFormula("Sheet1!$A$1+Sheet1!$B$1");

        Name name2 = wb.createName();
        name2.setNameName("name2");
        name2.setRefersToFormula("Sheet1!$A$1");

        //refers to A1 but on Sheet2. Should stay unaffected.
        Name name3 = wb.createName();
        name3.setNameName("name3");
        name3.setRefersToFormula("Sheet2!$A$1");

        //The scope of this one is Sheet2. Should stay unaffected.
        Name name4 = wb.createName();
        name4.setNameName("name4");
        name4.setRefersToFormula("A1");
        name4.setSheetIndex(1);

        sheet1.shiftRows(0, 1, 2);  //shift down the top row on Sheet1.
        name1 = wb.getName("name1");
        assertEquals("Sheet1!$A$3+Sheet1!$B$3", name1.getRefersToFormula());

        name2 = wb.getName("name2");
        assertEquals("Sheet1!$A$3", name2.getRefersToFormula());

        //name3 and name4 refer to Sheet2 and should not be affected
        name3 = wb.getName("name3");
        assertEquals("Sheet2!$A$1", name3.getRefersToFormula());

        name4 = wb.getName("name4");
        assertEquals("A1", name4.getRefersToFormula());
        
        wb.close();
    }

    @Test
    public final void testShiftWithMergedRegions() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(1.1);
        row.createCell(1).setCellValue(2.2);
        CellRangeAddress region = new CellRangeAddress(0, 0, 0, 2);
        assertEquals("A1:C1", region.formatAsString());

        sheet.addMergedRegion(region);

        sheet.shiftRows(0, 1, 2);
        region = sheet.getMergedRegion(0);
        assertEquals("A3:C3", region.formatAsString());
        wb.close();
    }

    //@Ignore("bug 56454: Incorrectly handles merged regions that do not contain column 0")
    @Test
    public final void shiftWithMergedRegions_bug56454() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
        // populate sheet cells
        for (int i = 0; i < 10; i++) {
            Row row = sheet.createRow(i);
            
            for (int j = 0; j < 10; j++) {
                Cell cell = row.createCell(j, CellType.STRING);
                cell.setCellValue(i + "x" + j);
            }
        }
        
        CellRangeAddress A4_B7 = CellRangeAddress.valueOf("A4:B7");
        CellRangeAddress C4_D7 = CellRangeAddress.valueOf("C4:D7");
        
        sheet.addMergedRegion(A4_B7);
        sheet.addMergedRegion(C4_D7);
        
        assumeTrue(sheet.getLastRowNum() > 8);
        
        // Insert a row in the middle of both merged regions.
        sheet.shiftRows(4, sheet.getLastRowNum(), 1);
        
        // all regions should still start at row 3, and elongate by 1 row
        List<CellRangeAddress> expectedMergedRegions = new ArrayList<>();
        CellRangeAddress A4_B8 = CellRangeAddress.valueOf("A4:B8"); //A4:B7 should be elongated by 1 row
        CellRangeAddress C4_D8 = CellRangeAddress.valueOf("C4:D8"); //C4:B7 should be elongated by 1 row
        expectedMergedRegions.add(A4_B8);
        expectedMergedRegions.add(C4_D8);
        
        // This test is written as expected-to-fail and should be rewritten
        // as expected-to-pass when the bug is fixed.
        // FIXME: remove try, catch, and testPassesNow, skipTest when test passes
        try {
            assertEquals(expectedMergedRegions, sheet.getMergedRegions());
            testPassesNow(56454);
        } catch (AssertionError e) {
            skipTest(e);
        }
        wb.close();
    }
    
    

    /**
     * See bug #34023
     */
    @Test
    public final void testShiftWithFormulas() throws IOException {
        Workbook wb = _testDataProvider.openSampleWorkbook("ForShifting." + _testDataProvider.getStandardFileNameExtension());

        Sheet sheet = wb.getSheet("Sheet1");
        assertEquals(20, sheet.getLastRowNum());

        confirmRow(sheet, 0, 1, 171, 1, "ROW(D1)", "100+B1", "COUNT(D1:E1)");
        confirmRow(sheet, 1, 2, 172, 1, "ROW(D2)", "100+B2", "COUNT(D2:E2)");
        confirmRow(sheet, 2, 3, 173, 1, "ROW(D3)", "100+B3", "COUNT(D3:E3)");

        confirmCell(sheet, 6, 1, 271, "200+B1");
        confirmCell(sheet, 7, 1, 272, "200+B2");
        confirmCell(sheet, 8, 1, 273, "200+B3");

        confirmCell(sheet, 14, 0, 0.0, "A12"); // the cell referred to by this formula will be replaced

        // -----------
        // Row index 1 -> 11 (row "2" -> row "12")
        sheet.shiftRows(1, 1, 10);

        // Now check what sheet looks like after move

        // no changes on row "1"
        confirmRow(sheet, 0, 1, 171, 1, "ROW(D1)", "100+B1", "COUNT(D1:E1)");

        // row "2" is now empty
        confirmEmptyRow(sheet, 1);

        // Row "2" moved to row "12", and the formula has been updated.
        // note however that the cached formula result (2) has not been updated. (POI differs from Excel here)
        confirmRow(sheet, 11, 2, 172, 1, "ROW(D12)", "100+B12", "COUNT(D12:E12)");

        // no changes on row "3"
        confirmRow(sheet, 2, 3, 173, 1, "ROW(D3)", "100+B3", "COUNT(D3:E3)");


        confirmCell(sheet, 14, 0, 0.0, "#REF!");


        // Formulas on rows that weren't shifted:
        confirmCell(sheet, 6, 1, 271, "200+B1");
        confirmCell(sheet, 7, 1, 272, "200+B12"); // this one moved
        confirmCell(sheet, 8, 1, 273, "200+B3");

        // check formulas on other sheets
        Sheet sheet2 = wb.getSheet("Sheet2");
        confirmCell(sheet2,  0, 0, 371, "300+Sheet1!B1");
        confirmCell(sheet2,  1, 0, 372, "300+Sheet1!B12");
        confirmCell(sheet2,  2, 0, 373, "300+Sheet1!B3");

        confirmCell(sheet2, 11, 0, 300, "300+Sheet1!#REF!");


        // Note - named ranges formulas have not been updated
        wb.close();
    }

    private static void confirmRow(Sheet sheet, int rowIx, double valA, double valB, double valC,
                String formulaA, String formulaB, String formulaC) {
        confirmCell(sheet, rowIx, 4, valA, formulaA);
        confirmCell(sheet, rowIx, 5, valB, formulaB);
        confirmCell(sheet, rowIx, 6, valC, formulaC);
    }

    private static void confirmCell(Sheet sheet, int rowIx, int colIx,
            double expectedValue, String expectedFormula) {
        Cell cell = sheet.getRow(rowIx).getCell(colIx);
        assertEquals(expectedValue, cell.getNumericCellValue(), 0.0);
        assertEquals(expectedFormula, cell.getCellFormula());
    }

    @Test
    public final void testShiftSharedFormulasBug54206() throws IOException {
        Workbook wb = _testDataProvider.openSampleWorkbook("54206." + _testDataProvider.getStandardFileNameExtension());

        Sheet sheet = wb.getSheetAt(0);
        assertEquals("SUMIF($B$19:$B$82,$B4,G$19:G$82)", sheet.getRow(3).getCell(6).getCellFormula());
        assertEquals("SUMIF($B$19:$B$82,$B4,H$19:H$82)", sheet.getRow(3).getCell(7).getCellFormula());
        assertEquals("SUMIF($B$19:$B$82,$B4,I$19:I$82)", sheet.getRow(3).getCell(8).getCellFormula());

        assertEquals("SUMIF($B$19:$B$82,$B15,G$19:G$82)", sheet.getRow(14).getCell(6).getCellFormula());
        assertEquals("SUMIF($B$19:$B$82,$B15,H$19:H$82)", sheet.getRow(14).getCell(7).getCellFormula());
        assertEquals("SUMIF($B$19:$B$82,$B15,I$19:I$82)", sheet.getRow(14).getCell(8).getCellFormula());

        // now the whole block G4L:15
        for(int i = 3; i <= 14; i++){
            for(int j = 6; j <= 8; j++){
                String col = CellReference.convertNumToColString(j);
                String expectedFormula = "SUMIF($B$19:$B$82,$B"+(i+1)+","+col+"$19:"+col+"$82)";
                assertEquals(expectedFormula, sheet.getRow(i).getCell(j).getCellFormula());
            }
        }

        assertEquals("SUM(G24:I24)", sheet.getRow(23).getCell(9).getCellFormula());
        assertEquals("SUM(G25:I25)", sheet.getRow(24).getCell(9).getCellFormula());
        assertEquals("SUM(G26:I26)", sheet.getRow(25).getCell(9).getCellFormula());

        sheet.shiftRows(24, sheet.getLastRowNum(), 4, true, false);

        assertEquals("SUMIF($B$19:$B$86,$B4,G$19:G$86)", sheet.getRow(3).getCell(6).getCellFormula());
        assertEquals("SUMIF($B$19:$B$86,$B4,H$19:H$86)", sheet.getRow(3).getCell(7).getCellFormula());
        assertEquals("SUMIF($B$19:$B$86,$B4,I$19:I$86)", sheet.getRow(3).getCell(8).getCellFormula());

        assertEquals("SUMIF($B$19:$B$86,$B15,G$19:G$86)", sheet.getRow(14).getCell(6).getCellFormula());
        assertEquals("SUMIF($B$19:$B$86,$B15,H$19:H$86)", sheet.getRow(14).getCell(7).getCellFormula());
        assertEquals("SUMIF($B$19:$B$86,$B15,I$19:I$86)", sheet.getRow(14).getCell(8).getCellFormula());

        // now the whole block G4L:15
        for(int i = 3; i <= 14; i++){
            for(int j = 6; j <= 8; j++){
                String col = CellReference.convertNumToColString(j);
                String expectedFormula = "SUMIF($B$19:$B$86,$B"+(i+1)+","+col+"$19:"+col+"$86)";
                assertEquals(expectedFormula, sheet.getRow(i).getCell(j).getCellFormula());
            }
        }

        assertEquals("SUM(G24:I24)", sheet.getRow(23).getCell(9).getCellFormula());

        // shifted rows
        assertTrue( sheet.getRow(24) == null || sheet.getRow(24).getCell(9) == null);
        assertTrue( sheet.getRow(25) == null || sheet.getRow(25).getCell(9) == null);
        assertTrue( sheet.getRow(26) == null || sheet.getRow(26).getCell(9) == null);
        assertTrue( sheet.getRow(27) == null || sheet.getRow(27).getCell(9) == null);

        assertEquals("SUM(G29:I29)", sheet.getRow(28).getCell(9).getCellFormula());
        assertEquals("SUM(G30:I30)", sheet.getRow(29).getCell(9).getCellFormula());
        wb.close();
    }

    @Test
    public void testBug55280() throws IOException {
        Workbook w = _testDataProvider.createWorkbook();
        Sheet s = w.createSheet();
        for (int row = 0; row < 5000; ++row)
            s.addMergedRegion(new CellRangeAddress(row, row, 0, 3));

        s.shiftRows(0, 4999, 1);        // takes a long time...
        w.close();
    }

    @Test
    public void test47169() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
        sheet.createRow(30);
        sheet.shiftRows(29, 29, 1, true, true);
        sheet.createRow(30);

        wb.close();
    }
    
    /**
     * Unified test for:
     * bug 46742: XSSFSheet.shiftRows should shift hyperlinks
     * bug 52903: HSSFSheet.shiftRows should shift hyperlinks
     */
    @Test
    public void testBug46742_52903_shiftHyperlinks() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet("test");
        Row row = sheet.createRow(0);
        
        // How to create hyperlinks
        // https://poi.apache.org/spreadsheet/quick-guide.html#Hyperlinks
        CreationHelper helper = wb.getCreationHelper();
        CellStyle hlinkStyle = wb.createCellStyle();
        Font hlinkFont = wb.createFont();
        hlinkFont.setUnderline(Font.U_SINGLE);
        hlinkFont.setColor(IndexedColors.BLUE.getIndex());
        hlinkStyle.setFont(hlinkFont);

        // 3D relative document link
        // CellAddress=A1, shifted to A4
        Cell cell = row.createCell(0);
        cell.setCellStyle(hlinkStyle);
        createHyperlink(helper, cell, HyperlinkType.DOCUMENT, "test!E1");
        
        // URL
        cell = row.createCell(1);
        // CellAddress=B1, shifted to B4
        cell.setCellStyle(hlinkStyle);
        createHyperlink(helper, cell, HyperlinkType.URL, "http://poi.apache.org/");
        
        // row0 will be shifted on top of row1, so this URL should be removed from the workbook
        Row overwrittenRow = sheet.createRow(3);
        cell = overwrittenRow.createCell(2);
        // CellAddress=C4, will be overwritten (deleted)
        cell.setCellStyle(hlinkStyle);
        createHyperlink(helper, cell, HyperlinkType.EMAIL, "mailto:poi@apache.org");
        
        // hyperlinks on this row are unaffected by the row shifting, so the hyperlinks should not move
        Row unaffectedRow = sheet.createRow(20);
        cell = unaffectedRow.createCell(3);
        // CellAddress=D21, will be unaffected
        cell.setCellStyle(hlinkStyle);
        createHyperlink(helper, cell, HyperlinkType.FILE, "54524.xlsx");
        
        cell = wb.createSheet("other").createRow(0).createCell(0);
        // CellAddress=Other!A1, will be unaffected
        cell.setCellStyle(hlinkStyle);
        createHyperlink(helper, cell, HyperlinkType.URL, "http://apache.org/");
        
        int startRow = 0;
        int endRow = 0;
        int n = 3;
        sheet.shiftRows(startRow, endRow, n);
        
        Workbook read = _testDataProvider.writeOutAndReadBack(wb);
        wb.close();
        
        Sheet sh = read.getSheet("test");
        
        Row shiftedRow = sh.getRow(3);
        
        // document link anchored on a shifted cell should be moved
        // Note that hyperlinks do not track what they point to, so this hyperlink should still refer to test!E1
        verifyHyperlink(shiftedRow.getCell(0), HyperlinkType.DOCUMENT, "test!E1");
        
        // URL, EMAIL, and FILE links anchored on a shifted cell should be moved
        verifyHyperlink(shiftedRow.getCell(1), HyperlinkType.URL, "http://poi.apache.org/");
        
        // Make sure hyperlinks were moved and not copied
        assertNull("Document hyperlink should be moved, not copied", sh.getHyperlink(0, 0));
        assertNull("URL hyperlink should be moved, not copied", sh.getHyperlink(0, 1));
        
        // Make sure hyperlink in overwritten row is deleted
        assertEquals(3, sh.getHyperlinkList().size());
        CellAddress unexpectedLinkAddress = new CellAddress("C4");
        for (Hyperlink link : sh.getHyperlinkList()) {
            final CellAddress linkAddress = new CellAddress(link.getFirstRow(), link.getFirstColumn());
            if (linkAddress.equals(unexpectedLinkAddress)) {
                fail("Row 4, including the hyperlink at C4, should have " +
                     "been deleted when Row 1 was shifted on top of it.");
            }
        }
        
        // Make sure unaffected rows are not shifted
        Cell unaffectedCell = sh.getRow(20).getCell(3);
        assertTrue(cellHasHyperlink(unaffectedCell));
        verifyHyperlink(unaffectedCell, HyperlinkType.FILE, "54524.xlsx");
        
        // Make sure cells on other sheets are not affected
        unaffectedCell = read.getSheet("other").getRow(0).getCell(0);
        assertTrue(cellHasHyperlink(unaffectedCell));
        verifyHyperlink(unaffectedCell, HyperlinkType.URL, "http://apache.org/");
        
        read.close();
    }
    
    //@Ignore("bug 56454: Incorrectly handles merged regions that do not contain column 0")
    @Test
    public void shiftRowsWithMergedRegionsThatDoNotContainColumnZero() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet("test");
        
        // populate sheet cells
        for (int i = 0; i < 10; i++) {
            Row row = sheet.createRow(i);
            for (int j = 0; j < 12; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(i + "x" + j);
            }
        }

        CellRangeAddress A4_B7 = new CellRangeAddress(3, 6, 0, 1);
        CellRangeAddress C5_D7 = new CellRangeAddress(4, 6, 2, 3);

        sheet.addMergedRegion(A4_B7);
        sheet.addMergedRegion(C5_D7);

        // A4:B7 will elongate vertically
        // C5:D7 will be shifted down with same size
        sheet.shiftRows(4, sheet.getLastRowNum(), 1);

        // This test is written as expected-to-fail and should be rewritten
        // as expected-to-pass when the bug is fixed.
        // FIXME: remove try, catch, and testPassesNow, skipTest when test passes
        try {
            assertEquals(2, sheet.getNumMergedRegions());
            assertEquals(CellRangeAddress.valueOf("A4:B8"), sheet.getMergedRegion(0));
            assertEquals(CellRangeAddress.valueOf("C5:D8"), sheet.getMergedRegion(1));
            testPassesNow(56454);
        } catch (AssertionError e) {
            skipTest(e);
        }
        
        wb.close();
    }

    @Test
    public void shiftMergedRowsToMergedRowsUp() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet("test");
        populateSheetCells(sheet, 2);


        CellRangeAddress A1_E1 = new CellRangeAddress(0, 0, 0, 4);
        CellRangeAddress A2_C2 = new CellRangeAddress(1, 1, 0, 2);

        sheet.addMergedRegion(A1_E1);
        sheet.addMergedRegion(A2_C2);

        // A1:E1 should be removed
        // A2:C2 will be A1:C1
        sheet.shiftRows(1, sheet.getLastRowNum(), -1);

        assertEquals(1, sheet.getNumMergedRegions());
        assertEquals(CellRangeAddress.valueOf("A1:C1"), sheet.getMergedRegion(0));

        wb.close();
    }

    @Test
    public void shiftMergedRowsToMergedRowsOverlappingMergedRegion() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet("test");
        populateSheetCells(sheet, 10);

        CellRangeAddress A1_E1 = new CellRangeAddress(0, 0, 0, 4);
        CellRangeAddress A2_C2 = new CellRangeAddress(1, 7, 0, 2);

        sheet.addMergedRegion(A1_E1);
        sheet.addMergedRegion(A2_C2);

        // A1:E1 should move to A5:E5
        // A2:C2 should be removed
        sheet.shiftRows(0, 0, 4);

        assertEquals(1, sheet.getNumMergedRegions());
        assertEquals(CellRangeAddress.valueOf("A5:E5"), sheet.getMergedRegion(0));

        wb.close();
    }

    @Test
    public void bug60384ShiftMergedRegion() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet("test");
        populateSheetCells(sheet, 9);


        CellRangeAddress A8_E8 = new CellRangeAddress(7, 7, 0, 4);
        CellRangeAddress A9_C9 = new CellRangeAddress(8, 8, 0, 2);

        sheet.addMergedRegion(A8_E8);
        sheet.addMergedRegion(A9_C9);

        // A1:E1 should be removed
        // A2:C2 will be A1:C1
        sheet.shiftRows(3, sheet.getLastRowNum(), 1);

        assertEquals(2, sheet.getNumMergedRegions());
        assertEquals(CellRangeAddress.valueOf("A9:E9"), sheet.getMergedRegion(0));
        assertEquals(CellRangeAddress.valueOf("A10:C10"), sheet.getMergedRegion(1));

        wb.close();
    }

    private void populateSheetCells(Sheet sheet, int rowCount) {
        // populate sheet cells
        for (int i = 0; i < rowCount; i++) {
            Row row = sheet.createRow(i);
            for (int j = 0; j < 5; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(i + "x" + j);
            }
        }
    }

    @Test
    public void shiftMergedRowsToMergedRowsDown() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet("test");

        // populate sheet cells
        populateSheetCells(sheet, 2);

        CellRangeAddress A1_E1 = new CellRangeAddress(0, 0, 0, 4);
        CellRangeAddress A2_C2 = new CellRangeAddress(1, 1, 0, 2);

        sheet.addMergedRegion(A1_E1);
        sheet.addMergedRegion(A2_C2);

        // A1:E1 should be moved to A2:E2
        // A2:C2 will be removed
        sheet.shiftRows(0, 0, 1);

        assertEquals(1, sheet.getNumMergedRegions());
        assertEquals(CellRangeAddress.valueOf("A2:E2"), sheet.getMergedRegion(0));

        wb.close();
    }
    
    @Test
    public void test61840_shifting_rows_up_does_not_produce_REF_errors() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
        Cell cell = sheet.createRow(4).createCell(0);
        
        cell.setCellFormula("(B5-C5)/B5");
        sheet.shiftRows(4, 4, -1);
        
        // Cell objects created before a row shift are still valid.
        // The row number of those cell references will be shifted if
        // the cell is within the shift range.
        assertEquals("(B4-C4)/B4", cell.getCellFormula());
        
        // New cell references are also valid.
        Cell shiftedCell = sheet.getRow(3).getCell(0);
        assertNotNull(shiftedCell);
        assertEquals("(B4-C4)/B4", shiftedCell.getCellFormula());
        
        wb.close();
    }
    
    
    
    

    private void createHyperlink(CreationHelper helper, Cell cell, HyperlinkType linkType, String ref) {
        cell.setCellValue(ref);
        Hyperlink link = helper.createHyperlink(linkType);
        link.setAddress(ref);
        cell.setHyperlink(link);
    }
    
    private void verifyHyperlink(Cell cell, HyperlinkType linkType, String ref) {
        assertTrue(cellHasHyperlink(cell));
        Hyperlink link = cell.getHyperlink();
        assertEquals(linkType, link.getType());
        assertEquals(ref, link.getAddress());
    }
    
    private boolean cellHasHyperlink(Cell cell) {
        return (cell != null) && (cell.getHyperlink() != null);
    }
}
