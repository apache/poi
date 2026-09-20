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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.BaseTestSheetShiftRows;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTDrawing;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTOneCellAnchor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRow;

public final class TestXSSFSheetShiftRows extends BaseTestSheetShiftRows {

    public TestXSSFSheetShiftRows(){
        super(XSSFITestDataProvider.instance);
    }

    @Override
    protected void testShiftRowBreaks() {
        // disabled test from superclass
        // TODO - support shifting of page breaks
    }

    /** Error occurred at FormulaShifter#rowMoveAreaPtg while shift rows upward. */
    @Test
    void testBug54524() throws IOException {
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
    void testBug53798() throws IOException {
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
        if(readCell.getCellType() == CellType.NUMERIC) {
            assertEquals(expect, Double.toString(readCell.getNumericCellValue()));
        } else {
            assertEquals(expect, readCell.getStringCellValue());
        }
    }

    /** negative row shift causes corrupted data or throws exception */
    @Test
    void testBug53798a() throws IOException {
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
    void testBug56017() throws IOException {
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
    void test57171() throws IOException {
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
    void test57163() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("57171_57163_57165.xlsx");
        assertEquals(5, wb.getActiveSheetIndex());
        wb.removeSheetAt(0);
        assertEquals(4, wb.getActiveSheetIndex());

        wb.close();
    }

    @Test
    void testSetSheetOrderAndAdjustActiveSheet() throws IOException {
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
    void testRemoveSheetAndAdjustActiveSheet() throws IOException {
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
        assertThrows(IllegalArgumentException.class, () -> wb.removeSheetAt(0),
            "Should catch exception as no more sheets are there");
        assertEquals(0, wb.getActiveSheetIndex());

        wb.createSheet();
        assertEquals(0, wb.getActiveSheetIndex());

        wb.removeSheetAt(0);
        assertEquals(0, wb.getActiveSheetIndex());

        wb.close();
    }

    /** Failed to clone a sheet from an Excel 2010 */
    @Test
    void test57165() throws IOException {
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
    void testBug57828_OnlyOneCommentShiftedInRow() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("57828.xlsx")) {
            XSSFSheet sheet = wb.getSheetAt(0);

            Comment comment1 = sheet.getCellComment(new CellAddress(2, 1));
            assertNotNull(comment1);

            Comment comment2 = sheet.getCellComment(new CellAddress(2, 2));
            assertNotNull(comment2);

            Comment comment3 = sheet.getCellComment(new CellAddress(1, 1));
            assertNull(comment3, "NO comment in (1,1) and it should be null");

            sheet.shiftRows(2, 2, -1);

            comment3 = sheet.getCellComment(new CellAddress(1, 1));
            assertNotNull(comment3, "Comment in (2,1) moved to (1,1) so its not null now.");

            comment1 = sheet.getCellComment(new CellAddress(2, 1));
            assertNull(comment1, "No comment currently in (2,1) and hence it is null");

            comment2 = sheet.getCellComment(new CellAddress(1, 2));
            assertNotNull(comment2, "Comment in (2,2) should have moved as well because of shift rows. But its not");
        }
    }

    @Test
    void bug59733() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        for (int r=0; r<4; r++) {
            sheet.createRow(r);
        }

        // Shift the 2nd row on top of the 0th row
        sheet.shiftRows(2, 2, -2);

        sheet.removeRow(sheet.getRow(0));
        assertEquals(1, sheet.getRow(1).getRowNum());

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
    void testSharedFormulas() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("TestShiftRowSharedFormula.xlsx")) {
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
        }
    }

    // bug 59983:  Wrong update of shared formulas after shiftRow
    @Test
    void testShiftSharedFormulas() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("TestShiftRowSharedFormula.xlsx")) {
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
        }
    }

    // bug 60260: shift rows or rename a sheet containing a named range
    // that refers to formula with a unicode (non-ASCII) sheet name formula
    @Test
    void shiftRowsWithUnicodeNamedRange() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("unicodeSheetName.xlsx")) {
            XSSFSheet sheet = wb.getSheetAt(0);
            sheet.shiftRows(1, 2, 3);

            Integer[] exp = { 1, null, null, 4, 2, 3, 7, 8, 9 };
            IntStream.rangeClosed(0, 8).forEach(i -> {
                Row row = sheet.getRow(i);
                if (exp[i] == null) {
                    assertNull(row);
                } else {
                    assertEquals(exp[i], (int)row.getCell(0).getNumericCellValue());
                }
            });
        }
    }

    @Test
    void test60384() throws IOException {
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
    void test60709() throws IOException {
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

    // bug 70139: shifting rows rebuilt every row and cell from the XML, which was slow and
    // invalidated the XSSFRow/XSSFCell instances the caller holds
    @Test
    void testShiftRowsKeepsRowAndCellInstances() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            for (int r = 0; r < 10; r++) {
                XSSFRow row = sheet.createRow(r);
                row.createCell(0).setCellValue("r" + r);
                row.setHeightInPoints(12 + r);
            }
            XSSFRow row3 = sheet.getRow(3);
            XSSFCell cell3 = row3.getCell(0);
            XSSFRow row9 = sheet.getRow(9);

            sheet.shiftRows(3, 9, 2);

            assertNull(sheet.getRow(3));
            assertNull(sheet.getRow(4));
            assertSame(row3, sheet.getRow(5));
            assertSame(cell3, sheet.getRow(5).getCell(0));
            assertEquals("r3", cell3.getStringCellValue());
            assertEquals(5, cell3.getRowIndex());
            assertEquals(15, row3.getHeightInPoints(), 0);
            assertSame(row9, sheet.getRow(11));
            assertEquals(11, sheet.getLastRowNum());
            assertRowsInOrder(sheet);

            // the shifted row can be used further
            row3.createCell(1).setCellValue("added");
            XSSFRow inserted = sheet.createRow(3);
            inserted.createCell(0).setCellValue("inserted");
            assertRowsInOrder(sheet);

            try (XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb)) {
                XSSFSheet sheet2 = wb2.getSheetAt(0);
                assertEquals("inserted", sheet2.getRow(3).getCell(0).getStringCellValue());
                assertNull(sheet2.getRow(4));
                assertEquals("r3", sheet2.getRow(5).getCell(0).getStringCellValue());
                assertEquals("added", sheet2.getRow(5).getCell(1).getStringCellValue());
                assertEquals(15, sheet2.getRow(5).getHeightInPoints(), 0);
                assertEquals("r9", sheet2.getRow(11).getCell(0).getStringCellValue());
                assertRowsInOrder(sheet2);
            }
        }
    }

    // rows jumping over other rows (bug 64516) still need the XML rows to be reordered
    @Test
    void testShiftRowsOverOtherRowsKeepsSheetDataInOrder() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            for (int r = 0; r < 6; r++) {
                sheet.createRow(r).createCell(0).setCellValue("r" + r);
            }

            // move row 5 on top of row 0, rows 1-4 are jumped over
            sheet.shiftRows(5, 5, -5);

            assertRowsInOrder(sheet);
            assertEquals("r5", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("r1", sheet.getRow(1).getCell(0).getStringCellValue());
            assertNull(sheet.getRow(5));
            sheet.removeRow(sheet.getRow(0));
            assertEquals("r1", sheet.getRow(1).getCell(0).getStringCellValue());

            try (XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb)) {
                XSSFSheet sheet2 = wb2.getSheetAt(0);
                assertNull(sheet2.getRow(0));
                assertEquals("r1", sheet2.getRow(1).getCell(0).getStringCellValue());
                assertEquals("r4", sheet2.getRow(4).getCell(0).getStringCellValue());
                assertRowsInOrder(sheet2);
            }
        }
    }

    @Test
    void testShiftSharedFormulasTwice() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("TestShiftRowSharedFormula.xlsx")) {
            XSSFSheet sheet = wb.getSheetAt(0);
            XSSFRow row5 = sheet.getRow(4);

            sheet.shiftRows(3, sheet.getLastRowNum(), 1);
            assertSame(row5, sheet.getRow(5));
            assertEquals("SUM(C2:C5)", getCellFormula(sheet, "C6"));
            assertEquals("SUM(E3:E6)", getCellFormula(sheet, "E7"));

            sheet.shiftRows(3, sheet.getLastRowNum(), 2);
            assertSame(row5, sheet.getRow(7));
            assertEquals("SUM(C2:C7)", getCellFormula(sheet, "C8"));
            assertEquals("SUM(D2:D7)", getCellFormula(sheet, "D8"));
            assertEquals("SUM(E3:E8)", getCellFormula(sheet, "E9"));
            assertRowsInOrder(sheet);

            try (XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb)) {
                XSSFSheet sheet2 = wb2.getSheetAt(0);
                assertEquals("SUM(C2:C7)", getCellFormula(sheet2, "C8"));
                assertEquals("SUM(E3:E8)", getCellFormula(sheet2, "E9"));
            }
        }
    }

    private static void assertRowsInOrder(XSSFSheet sheet) {
        long prev = 0;
        int count = 0;
        for (CTRow ctRow : sheet.getCTWorksheet().getSheetData().getRowList()) {
            assertTrue(ctRow.getR() > prev, "row " + ctRow.getR() + " after row " + prev);
            prev = ctRow.getR();
            count++;
        }
        assertEquals(sheet.getPhysicalNumberOfRows(), count);
        int i = 0;
        for (Row row : sheet) {
            assertSame(sheet.getRow(row.getRowNum()), row);
            i++;
        }
        assertEquals(count, i);
    }

    @Test
    public void testBug69154() throws Exception {
        // this does not appear to work for HSSF but let's get it working for XSSF anyway
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();
            for (int i = 0; i < 6; i++) {
                Row row = sheet.createRow(i);
                for (int j = 0; j < 6; j++) {
                    String value = new CellAddress(i, j).formatAsString();
                    row.createCell(j).setCellValue(value);
                }
            }
            final int firstCol = 1;
            final int secondCol = firstCol + 1;
            final int thirdCol = secondCol + 1;
            sheet.addMergedRegion(new CellRangeAddress(0, 0, firstCol, secondCol));
            sheet.addMergedRegion(new CellRangeAddress(1, 2, firstCol, firstCol));
            sheet.addMergedRegion(new CellRangeAddress(3, 3, secondCol, thirdCol));
            assertEquals(3, sheet.getNumMergedRegions());
            sheet.shiftColumns(2, 5, -1);
            // only the 3rd merged region should remain
            assertEquals(1, sheet.getNumMergedRegions());
            CellRangeAddress mr = sheet.getMergedRegion(0);
            CellRangeAddress expectedMR = new CellRangeAddress(3, 3, secondCol - 1, thirdCol - 1);
            assertEquals(expectedMR, mr);
        }
    }

    @Test
    void bug60072ShiftRowsMovesDrawingAnchors() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            for (int i = 0; i < 30; i++) {
                sheet.createRow(i).createCell(0).setCellValue(i);
            }
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            int picIdx = wb.addPicture("test jpeg data".getBytes(LocaleUtil.CHARSET_1252), XSSFWorkbook.PICTURE_TYPE_JPEG);

            // shape above the shifted area: rows 0-1
            XSSFSimpleShape shape = drawing.createSimpleShape(new XSSFClientAnchor(0, 0, 0, 0, 0, 0, 3, 1));
            // chart at the start of the shifted area: rows 2-10
            XSSFChart chart = drawing.createChart(new XSSFClientAnchor(0, 0, 0, 0, 1, 2, 8, 10));
            // picture further down: rows 12-15
            XSSFPicture picture = drawing.createPicture(new XSSFClientAnchor(0, 0, 0, 0, 1, 12, 4, 15), picIdx);
            // a one-cell anchor at row 20 and an absolute anchor, added on the XML level as the API only creates two-cell anchors
            CTDrawing ctDrawing = drawing.getCTDrawing();
            CTOneCellAnchor oneCell = ctDrawing.addNewOneCellAnchor();
            oneCell.addNewFrom().setRow(20);
            oneCell.getFrom().setCol(2);
            oneCell.addNewExt().setCx(100);
            oneCell.getExt().setCy(100);
            oneCell.addNewClientData();
            ctDrawing.addNewAbsoluteAnchor().addNewPos().setX(10);
            ctDrawing.getAbsoluteAnchorArray(0).getPos().setY(10);
            ctDrawing.getAbsoluteAnchorArray(0).addNewExt().setCx(100);
            ctDrawing.getAbsoluteAnchorArray(0).getExt().setCy(100);
            ctDrawing.getAbsoluteAnchorArray(0).addNewClientData();

            // insert 3 rows at row 2
            sheet.shiftRows(2, sheet.getLastRowNum(), 3);

            assertAnchorRows(0, 1, shape.getAnchor());
            assertAnchorRows(5, 13, chart.getGraphicFrame().getAnchor());
            assertAnchorRows(15, 18, picture.getAnchor());
            assertEquals(23, oneCell.getFrom().getRow());
            assertEquals(10L, ctDrawing.getAbsoluteAnchorArray(0).getPos().getY());

            // move rows 15-18 (the picture) up by 10; the chart's top-left row is not in the range and stays
            sheet.shiftRows(15, 18, -10);

            assertAnchorRows(5, 13, chart.getGraphicFrame().getAnchor());
            assertAnchorRows(5, 8, picture.getAnchor());
            assertEquals(23, oneCell.getFrom().getRow());

            try (XSSFWorkbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb)) {
                XSSFDrawing drawingBack = wbBack.getSheetAt(0).getDrawingPatriarch();
                assertNotNull(drawingBack);
                assertAnchorRows(0, 1, drawingBack.getShapes().get(0).getAnchor());
                assertAnchorRows(5, 13, drawingBack.getShapes().get(1).getAnchor());
                assertAnchorRows(5, 8, drawingBack.getShapes().get(2).getAnchor());
                assertEquals(23, drawingBack.getCTDrawing().getOneCellAnchorArray(0).getFrom().getRow());
            }
        }
    }

    private static void assertAnchorRows(int row1, int row2, XSSFAnchor anchor) {
        ClientAnchor clientAnchor = (ClientAnchor) anchor;
        assertEquals(row1, clientAnchor.getRow1(), "row1");
        assertEquals(row2, clientAnchor.getRow2(), "row2");
    }
}
