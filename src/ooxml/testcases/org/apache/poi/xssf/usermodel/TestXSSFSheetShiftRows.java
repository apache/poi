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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;
import org.junit.Test;

import java.io.IOException;

import static org.apache.poi.POITestCase.skipTest;
import static org.apache.poi.POITestCase.testPassesNow;
import static org.junit.Assert.*;

public final class TestXSSFSheetShiftRows extends BaseTestSheetShiftRows {

    public TestXSSFSheetShiftRows(){
        super(XSSFITestDataProvider.instance);
    }

    @Override
    @Test
    public void testShiftRowBreaks() {
        // disabled test from superclass
        // TODO - support shifting of page breaks
    }

    /** Error occurred at FormulaShifter#rowMoveAreaPtg while shift rows upward. */
    @Test
    public void testBug54524() throws IOException {
        XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("54524.xlsx");
        XSSFSheet sheet = workbook.getSheetAt(0);
        sheet.shiftRows(3, 5, -1);

        Cell cell = CellUtil.getCell(sheet.getRow(1), 0);
        assertEquals(1.0, cell.getNumericCellValue(), 0);
        cell = CellUtil.getCell(sheet.getRow(2), 0);
        assertEquals("SUM(A2:A2)", cell.getCellFormula());
        cell = CellUtil.getCell(sheet.getRow(3), 0);
        assertEquals("X", cell.getStringCellValue());
        workbook.close();
    }

    /**  negative row shift causes corrupted data or throws exception */
    @Test
    public void testBug53798() throws IOException {
        // NOTE that for HSSF (.xls) negative shifts combined with positive ones do work as expected  
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("53798.xlsx");

        Sheet testSheet    = wb.getSheetAt(0);
        // 1) corrupted xlsx (unreadable data in the first row of a shifted group) already comes about  
        // when shifted by less than -1 negative amount (try -2)
        testSheet.shiftRows(3, 3, -2);
        
        // 2) attempt to create a new row IN PLACE of a removed row by a negative shift causes corrupted
        // xlsx file with  unreadable data in the negative shifted row. 
        // NOTE it's ok to create any other row.
        Row newRow = testSheet.createRow(3);
        Cell newCell = newRow.createCell(0);
        newCell.setCellValue("new Cell in row "+newRow.getRowNum());
        
        // 3) once a negative shift has been made any attempt to shift another group of rows 
        // (note: outside of previously negative shifted rows) by a POSITIVE amount causes POI exception: 
        // org.apache.xmlbeans.impl.values.XmlValueDisconnectedException.
        // NOTE: another negative shift on another group of rows is successful, provided no new rows in  
        // place of previously shifted rows were attempted to be created as explained above.

        // -- CHANGE the shift to positive once the behaviour of the above has been tested
        testSheet.shiftRows(6, 7, 1); 
        
        Workbook read = XSSFTestDataSamples.writeOutAndReadBack(wb);
        wb.close();
        assertNotNull(read);
        
        Sheet readSheet = read.getSheetAt(0);
        verifyCellContent(readSheet, 0, "0.0");
        verifyCellContent(readSheet, 1, "3.0");
        verifyCellContent(readSheet, 2, "2.0");
        verifyCellContent(readSheet, 3, "new Cell in row 3");
        verifyCellContent(readSheet, 4, "4.0");
        verifyCellContent(readSheet, 5, "5.0");
        verifyCellContent(readSheet, 6, null);
        verifyCellContent(readSheet, 7, "6.0");
        verifyCellContent(readSheet, 8, "7.0");
        read.close();
    }

    private void verifyCellContent(Sheet readSheet, int row, String expect) {
        Row readRow = readSheet.getRow(row);
        if(expect == null) {
            assertNull(readRow);
            return;
        }
        Cell readCell = readRow.getCell(0);
        //noinspection deprecation
        if(readCell.getCellType() == CellType.NUMERIC) {
            assertEquals(expect, Double.toString(readCell.getNumericCellValue()));
        } else {
            assertEquals(expect, readCell.getStringCellValue());
        }
    }
    
    /** negative row shift causes corrupted data or throws exception */
    @Test
    public void testBug53798a() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("53798.xlsx");

        Sheet testSheet    = wb.getSheetAt(0);
        testSheet.shiftRows(3, 3, -1);
        for (Row r : testSheet) {
            r.getRowNum();
        }
        testSheet.shiftRows(6, 6, 1);
        
        Workbook read = XSSFTestDataSamples.writeOutAndReadBack(wb);
        wb.close();
        assertNotNull(read);
        
        Sheet readSheet = read.getSheetAt(0);
        verifyCellContent(readSheet, 0, "0.0");
        verifyCellContent(readSheet, 1, "1.0");
        verifyCellContent(readSheet, 2, "3.0");
        verifyCellContent(readSheet, 3, null);
        verifyCellContent(readSheet, 4, "4.0");
        verifyCellContent(readSheet, 5, "5.0");
        verifyCellContent(readSheet, 6, null);
        verifyCellContent(readSheet, 7, "6.0");
        verifyCellContent(readSheet, 8, "8.0");
        read.close();
    }
    
    /** Shifting rows with comment result - Unreadable content error and comment deletion */
    @Test
    public void testBug56017() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("56017.xlsx");

        Sheet sheet = wb.getSheetAt(0);

        Comment comment = sheet.getCellComment(new CellAddress(0, 0));
        assertNotNull(comment);
        assertEquals("Amdocs", comment.getAuthor());
        assertEquals("Amdocs:\ntest\n", comment.getString().getString());
        
        sheet.shiftRows(0, 1, 1);

        // comment in row 0 is gone
        comment = sheet.getCellComment(new CellAddress(0, 0));
        assertNull(comment);
        
        // comment is now in row 1
        comment = sheet.getCellComment(new CellAddress(1, 0));
        assertNotNull(comment);
        assertEquals("Amdocs", comment.getAuthor());
        assertEquals("Amdocs:\ntest\n", comment.getString().getString());
        
        Workbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb);
        wb.close();
        assertNotNull(wbBack);

        Sheet sheetBack = wbBack.getSheetAt(0);

        // comment in row 0 is gone
        comment = sheetBack.getCellComment(new CellAddress(0, 0));
        assertNull(comment);

        // comment is now in row 1
        comment = sheetBack.getCellComment(new CellAddress(1, 0));
        assertNotNull(comment);
        assertEquals("Amdocs", comment.getAuthor());
        assertEquals("Amdocs:\ntest\n", comment.getString().getString());
        wbBack.close();
    }

    /** Moving the active sheet and deleting the others results in a corrupted file */
    @Test
    public void test57171() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("57171_57163_57165.xlsx");
        assertEquals(5, wb.getActiveSheetIndex());
        removeAllSheetsBut(5, wb); // 5 is the active / selected sheet
        assertEquals(0, wb.getActiveSheetIndex());

        Workbook wbRead = XSSFTestDataSamples.writeOutAndReadBack(wb);
        wb.close();
        assertEquals(0, wbRead.getActiveSheetIndex());

        wbRead.removeSheetAt(0);
        assertEquals(0, wbRead.getActiveSheetIndex());

        wbRead.close();
    }

    /**  Cannot delete an arbitrary sheet in an XLS workbook (only the last one) */
    @Test
    public void test57163() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("57171_57163_57165.xlsx");
        assertEquals(5, wb.getActiveSheetIndex());
        wb.removeSheetAt(0);
        assertEquals(4, wb.getActiveSheetIndex());

        wb.close();
    }

    @Test
    public void testSetSheetOrderAndAdjustActiveSheet() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("57171_57163_57165.xlsx");
        
        assertEquals(5, wb.getActiveSheetIndex());

        // move the sheets around in all possible combinations to check that the active sheet
        // is set correctly in all cases
        wb.setSheetOrder(wb.getSheetName(5), 4);
        assertEquals(4, wb.getActiveSheetIndex());
        
        wb.setSheetOrder(wb.getSheetName(5), 5);
        assertEquals(4, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(3), 5);
        assertEquals(3, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(4), 5);
        assertEquals(3, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(2), 2);
        assertEquals(3, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(2), 1);
        assertEquals(3, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(3), 5);
        assertEquals(5, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(0), 5);
        assertEquals(4, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(0), 5);
        assertEquals(3, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(0), 5);
        assertEquals(2, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(0), 5);
        assertEquals(1, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(0), 5);
        assertEquals(0, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(0), 5);
        assertEquals(5, wb.getActiveSheetIndex());
        
        wb.close();
    }   

    @Test
    public void testRemoveSheetAndAdjustActiveSheet() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("57171_57163_57165.xlsx");
        
        assertEquals(5, wb.getActiveSheetIndex());
        
        wb.removeSheetAt(0);
        assertEquals(4, wb.getActiveSheetIndex());
        
        wb.setActiveSheet(3);
        assertEquals(3, wb.getActiveSheetIndex());
        
        wb.removeSheetAt(4);
        assertEquals(3, wb.getActiveSheetIndex());

        wb.removeSheetAt(3);
        assertEquals(2, wb.getActiveSheetIndex());

        wb.removeSheetAt(0);
        assertEquals(1, wb.getActiveSheetIndex());

        wb.removeSheetAt(1);
        assertEquals(0, wb.getActiveSheetIndex());

        wb.removeSheetAt(0);
        assertEquals(0, wb.getActiveSheetIndex());

        try {
            wb.removeSheetAt(0);
            fail("Should catch exception as no more sheets are there");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(0, wb.getActiveSheetIndex());
        
        wb.createSheet();
        assertEquals(0, wb.getActiveSheetIndex());
        
        wb.removeSheetAt(0);
        assertEquals(0, wb.getActiveSheetIndex());
        
        wb.close();
    }

    /** Failed to clone a sheet from an Excel 2010 */
    @Test
    public void test57165() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("57171_57163_57165.xlsx");
        assertEquals(5, wb.getActiveSheetIndex());
        removeAllSheetsBut(3, wb);
        assertEquals(0, wb.getActiveSheetIndex());
        wb.createSheet("New Sheet1");
        assertEquals(0, wb.getActiveSheetIndex());
        wb.cloneSheet(0); // Throws exception here
        wb.setSheetName(1, "New Sheet");
        assertEquals(0, wb.getActiveSheetIndex());

        wb.close();
    }

    private static void removeAllSheetsBut(int sheetIndex, Workbook wb) {
        int sheetNb = wb.getNumberOfSheets();
        // Move this sheet at the first position
        wb.setSheetOrder(wb.getSheetName(sheetIndex), 0);
        // Must make this sheet active (otherwise, for XLSX, Excel might protest that active sheet no longer exists)
        // I think POI should automatically handle this case when deleting sheets...
        // wb.setActiveSheet(0);
        for (int sn = sheetNb - 1; sn > 0; sn--) {
            wb.removeSheetAt(sn);
        }
    }

    /** Shifting rows with cell comments only shifts comments from first such cell. Other cell comments not shifted */
    @Test
    public void testBug57828_OnlyOneCommentShiftedInRow() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("57828.xlsx");
        XSSFSheet sheet = wb.getSheetAt(0);

        Comment comment1 = sheet.getCellComment(new CellAddress(2, 1));
        assertNotNull(comment1);

        Comment comment2 = sheet.getCellComment(new CellAddress(2, 2));
        assertNotNull(comment2);

        Comment comment3 = sheet.getCellComment(new CellAddress(1, 1));
        assertNull("NO comment in (1,1) and it should be null", comment3);

        sheet.shiftRows(2, 2, -1);

        comment3 = sheet.getCellComment(new CellAddress(1, 1));
        assertNotNull("Comment in (2,1) moved to (1,1) so its not null now.", comment3);

        comment1 = sheet.getCellComment(new CellAddress(2, 1));
        assertNull("No comment currently in (2,1) and hence it is null", comment1);

        comment2 = sheet.getCellComment(new CellAddress(1, 2));
        assertNotNull("Comment in (2,2) should have moved as well because of shift rows. But its not", comment2);
        
        wb.close();
    }
    
    // This test is written as expected-to-fail and should be rewritten
    // as expected-to-pass when the bug is fixed.
    //@Ignore("Bug 59733 - shiftRows() causes org.apache.xmlbeans.impl.values.XmlValueDisconnectedException")
    @Test
    public void bug59733() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        for (int r=0; r<4; r++) {
            sheet.createRow(r);
        }

        // Shift the 2nd row on top of the 0th row
        sheet.shiftRows(2, 2, -2);
        
        /*
         * The following error is thrown when shifting the 3rd row on top of the 0th row
         * If the rows are not created, the error does not occur

        org.apache.xmlbeans.impl.values.XmlValueDisconnectedException
            at org.apache.xmlbeans.impl.values.XmlObjectBase.check_orphaned(XmlObjectBase.java:1258)
            at org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTRowImpl.getR(Unknown Source)
            at org.apache.poi.xssf.usermodel.XSSFRow.getRowNum(XSSFRow.java:363)
            at org.apache.poi.xssf.usermodel.TestXSSFSheetShiftRows.bug59733(TestXSSFSheetShiftRows.java:393)
         */
        // FIXME: remove try, catch, and testPassesNow, skipTest when test passes
        try {
            sheet.removeRow(sheet.getRow(0));
            assertEquals(1, sheet.getRow(1).getRowNum());
            testPassesNow(59733);
        } catch (XmlValueDisconnectedException e) {
            skipTest(e);
        }
        
        workbook.close();
    }

    private static String getCellFormula(Sheet sheet, String address) {
        CellAddress cellAddress = new CellAddress(address);
        Row row = sheet.getRow(cellAddress.getRow());
        assertNotNull(row);
        Cell cell = row.getCell(cellAddress.getColumn());
        assertNotNull(cell);
        assertEquals(CellType.FORMULA, cell.getCellType());
        return cell.getCellFormula();
    }

    // bug 59983:  Wrong update of shared formulas after shiftRow
    @Test
    public void testSharedFormulas() throws Exception {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("TestShiftRowSharedFormula.xlsx");
        XSSFSheet sheet = wb.getSheetAt(0);
        assertEquals("SUM(C2:C4)", getCellFormula(sheet, "C5"));
        assertEquals("SUM(D2:D4)", getCellFormula(sheet, "D5"));
        assertEquals("SUM(E2:E4)", getCellFormula(sheet, "E5"));

        assertEquals("SUM(C3:C5)", getCellFormula(sheet, "C6"));
        assertEquals("SUM(D3:D5)", getCellFormula(sheet, "D6"));
        assertEquals("SUM(E3:E5)", getCellFormula(sheet, "E6"));

        sheet.shiftRows(3, sheet.getLastRowNum(), 1);

        assertEquals("SUM(C2:C5)", getCellFormula(sheet, "C6"));
        assertEquals("SUM(D2:D5)", getCellFormula(sheet, "D6"));
        assertEquals("SUM(E2:E5)", getCellFormula(sheet, "E6"));

        assertEquals("SUM(C3:C6)", getCellFormula(sheet, "C7"));
        assertEquals("SUM(D3:D6)", getCellFormula(sheet, "D7"));
        assertEquals("SUM(E3:E6)", getCellFormula(sheet, "E7"));
        wb.close();
    }

    // bug 59983:  Wrong update of shared formulas after shiftRow
    @Test
    public void testShiftSharedFormulas() throws Exception {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("TestShiftRowSharedFormula.xlsx");
        XSSFSheet sheet = wb.getSheetAt(0);
        assertEquals("SUM(C2:C4)", getCellFormula(sheet, "C5"));
        assertEquals("SUM(D2:D4)", getCellFormula(sheet, "D5"));
        assertEquals("SUM(E2:E4)", getCellFormula(sheet, "E5"));

        assertEquals("SUM(C3:C5)", getCellFormula(sheet, "C6"));
        assertEquals("SUM(D3:D5)", getCellFormula(sheet, "D6"));
        assertEquals("SUM(E3:E5)", getCellFormula(sheet, "E6"));

        sheet.shiftRows(sheet.getFirstRowNum(), 4, -1);

        assertEquals("SUM(C1:C3)", getCellFormula(sheet, "C4"));
        assertEquals("SUM(D1:D3)", getCellFormula(sheet, "D4"));
        assertEquals("SUM(E1:E3)", getCellFormula(sheet, "E4"));

        assertEquals("SUM(C2:C4)", getCellFormula(sheet, "C6"));
        assertEquals("SUM(D2:D4)", getCellFormula(sheet, "D6"));
        assertEquals("SUM(E2:E4)", getCellFormula(sheet, "E6"));
        wb.close();
    }
    
    // bug 60260: shift rows or rename a sheet containing a named range
    // that refers to formula with a unicode (non-ASCII) sheet name formula
    @Test
    public void shiftRowsWithUnicodeNamedRange() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("unicodeSheetName.xlsx");
        XSSFSheet sheet = wb.getSheetAt(0);
        sheet.shiftRows(1, 2, 3);
        IOUtils.closeQuietly(wb);
    }

    @Test
    public void test60384() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("60384.xlsx");
        XSSFSheet sheet = wb.getSheetAt(0);

        assertEquals(2, sheet.getMergedRegions().size());
        assertEquals(7, sheet.getMergedRegion(0).getFirstRow());
        assertEquals(7, sheet.getMergedRegion(0).getLastRow());
        assertEquals(8, sheet.getMergedRegion(1).getFirstRow());
        assertEquals(8, sheet.getMergedRegion(1).getLastRow());

        sheet.shiftRows(3, 8, 1);

        // after shifting, the two named regions should still be there as they
        // are fully inside the shifted area
        assertEquals(2, sheet.getMergedRegions().size());
        assertEquals(8, sheet.getMergedRegion(0).getFirstRow());
        assertEquals(8, sheet.getMergedRegion(0).getLastRow());
        assertEquals(9, sheet.getMergedRegion(1).getFirstRow());
        assertEquals(9, sheet.getMergedRegion(1).getLastRow());

        /*OutputStream out = new FileOutputStream("/tmp/60384.xlsx");
        try {
            wb.write(out);
        } finally {
            out.close();
        }*/

        wb.close();
    }

    @Test
    public void test60709() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("60709.xlsx");
        XSSFSheet sheet = wb.getSheetAt(0);

        assertEquals(1, sheet.getMergedRegions().size());
        assertEquals(2, sheet.getMergedRegion(0).getFirstRow());
        assertEquals(2, sheet.getMergedRegion(0).getLastRow());

        sheet.shiftRows(1, sheet.getLastRowNum()+1, -1, true, false);

        // after shifting, the two named regions should still be there as they
        // are fully inside the shifted area
        assertEquals(1, sheet.getMergedRegions().size());
        assertEquals(1, sheet.getMergedRegion(0).getFirstRow());
        assertEquals(1, sheet.getMergedRegion(0).getLastRow());

        /*OutputStream out = new FileOutputStream("/tmp/60709.xlsx");
        try {
            wb.write(out);
        } finally {
            out.close();
        }*/

        wb.close();
    }
}
