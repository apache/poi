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

import static org.apache.poi.util.Units.EMU_PER_PIXEL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.util.Units;
import org.junit.jupiter.api.Test;

/**
 * Common superclass for testing implementations of
 * {@link Comment}
 */
public abstract class BaseTestCellComment {

    private final ITestDataProvider _testDataProvider;

    protected BaseTestCellComment(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    @Test
    public final void find() throws IOException {
        Workbook book = _testDataProvider.createWorkbook();
        Sheet sheet = book.createSheet();
        assertNull(sheet.getCellComment(new CellAddress(0, 0)));

        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        assertNull(sheet.getCellComment(new CellAddress(0, 0)));
        assertNull(cell.getCellComment());
        book.close();
    }

    @Test
    public final void create() throws IOException {
        String cellText = "Hello, World";
        String commentText = "We can set comments in POI";
        String commentAuthor = "Apache Software Foundation";
        int cellRow = 3;
        int cellColumn = 1;

        Workbook wb1 = _testDataProvider.createWorkbook();
        CreationHelper factory = wb1.getCreationHelper();

        Sheet sheet = wb1.createSheet();
        assertNull(sheet.getCellComment(new CellAddress(cellRow, cellColumn)));

        Cell cell = sheet.createRow(cellRow).createCell(cellColumn);
        cell.setCellValue(factory.createRichTextString(cellText));
        assertNull(cell.getCellComment());
        assertNull(sheet.getCellComment(new CellAddress(cellRow, cellColumn)));

        Drawing<?> patr = sheet.createDrawingPatriarch();
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(2);
        anchor.setCol2(5);
        anchor.setRow1(1);
        anchor.setRow2(2);
        Comment comment = patr.createCellComment(anchor);
        assertFalse(comment.isVisible());
        comment.setVisible(true);
        assertTrue(comment.isVisible());
        RichTextString string1 = factory.createRichTextString(commentText);
        comment.setString(string1);
        comment.setAuthor(commentAuthor);
        cell.setCellComment(comment);
        assertNotNull(cell.getCellComment());
        assertNotNull(sheet.getCellComment(new CellAddress(cellRow, cellColumn)));

        //verify our settings
        assertEquals(commentAuthor, comment.getAuthor());
        assertEquals(commentText, comment.getString().getString());
        assertEquals(cellRow, comment.getRow());
        assertEquals(cellColumn, comment.getColumn());

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();

        sheet = wb2.getSheetAt(0);
        cell = sheet.getRow(cellRow).getCell(cellColumn);
        comment = cell.getCellComment();

        assertNotNull(comment);
        assertEquals(commentAuthor, comment.getAuthor());
        assertEquals(commentText, comment.getString().getString());
        assertEquals(cellRow, comment.getRow());
        assertEquals(cellColumn, comment.getColumn());
        assertTrue(comment.isVisible());

        // Change slightly, and re-test
        comment.setString(factory.createRichTextString("New Comment Text"));
        comment.setVisible(false);

        Workbook wb3 = _testDataProvider.writeOutAndReadBack(wb2);
        wb2.close();

        sheet = wb3.getSheetAt(0);
        cell = sheet.getRow(cellRow).getCell(cellColumn);
        comment = cell.getCellComment();

        assertNotNull(comment);
        assertEquals(commentAuthor, comment.getAuthor());
        assertEquals("New Comment Text", comment.getString().getString());
        assertEquals(cellRow, comment.getRow());
        assertEquals(cellColumn, comment.getColumn());
        assertFalse(comment.isVisible());

        // Test Comment.equals and Comment.hashCode
        assertEquals(comment, cell.getCellComment());
        assertEquals(comment.hashCode(), cell.getCellComment().hashCode());

        wb3.close();
    }

    /**
     * test that we can read cell comments from an existing workbook.
     */
    @Test
    public final void readComments() throws IOException {

        Workbook wb = _testDataProvider.openSampleWorkbook("SimpleWithComments." + _testDataProvider.getStandardFileNameExtension());

        Sheet sheet = wb.getSheetAt(0);

        Cell cell;
        Row row;
        Comment comment;

        for (int rownum = 0; rownum < 3; rownum++) {
            row = sheet.getRow(rownum);
            cell = row.getCell(0);
            comment = cell.getCellComment();
            assertNull(comment, "Cells in the first column are not commented");
            assertNull(sheet.getCellComment(new CellAddress(rownum, 0)));
        }

        for (int rownum = 0; rownum < 3; rownum++) {
            row = sheet.getRow(rownum);
            cell = row.getCell(1);
            comment = cell.getCellComment();
            assertNotNull(comment, "Cells in the second column have comments");
            assertNotNull(sheet.getCellComment(new CellAddress(rownum, 1)), "Cells in the second column have comments");

            assertEquals("Yegor Kozlov", comment.getAuthor());
            assertFalse(comment.getString().getString().isEmpty());
            assertEquals(rownum, comment.getRow());
            assertEquals(cell.getColumnIndex(), comment.getColumn());
        }

        wb.close();
    }

    /**
     * test that we can modify existing cell comments
     */
    @Test
    public final void modifyComments() throws IOException {

        Workbook wb1 = _testDataProvider.openSampleWorkbook("SimpleWithComments." + _testDataProvider.getStandardFileNameExtension());
        CreationHelper factory = wb1.getCreationHelper();

        Sheet sheet = wb1.getSheetAt(0);

        Cell cell;
        Row row;
        Comment comment;

        for (int rownum = 0; rownum < 3; rownum++) {
            row = sheet.getRow(rownum);
            cell = row.getCell(1);
            comment = cell.getCellComment();
            comment.setAuthor("Mofified[" + rownum + "] by Yegor");
            comment.setString(factory.createRichTextString("Modified comment at row " + rownum));
        }

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
        sheet = wb2.getSheetAt(0);

        for (int rownum = 0; rownum < 3; rownum++) {
            row = sheet.getRow(rownum);
            cell = row.getCell(1);
            comment = cell.getCellComment();

            assertEquals("Mofified[" + rownum + "] by Yegor", comment.getAuthor());
            assertEquals("Modified comment at row " + rownum, comment.getString().getString());
        }

        wb2.close();
    }

    @Test
    public final void deleteComments() throws IOException {
        Workbook wb1 = _testDataProvider.openSampleWorkbook("SimpleWithComments." + _testDataProvider.getStandardFileNameExtension());
        Sheet sheet = wb1.getSheetAt(0);

        // Zap from rows 1 and 3
        assertNotNull(sheet.getRow(0).getCell(1).getCellComment());
        assertNotNull(sheet.getRow(1).getCell(1).getCellComment());
        assertNotNull(sheet.getRow(2).getCell(1).getCellComment());

        sheet.getRow(0).getCell(1).removeCellComment();
        sheet.getRow(2).getCell(1).setCellComment(null);

        // Check gone so far
        assertNull(sheet.getRow(0).getCell(1).getCellComment());
        assertNotNull(sheet.getRow(1).getCell(1).getCellComment());
        assertNull(sheet.getRow(2).getCell(1).getCellComment());

        // Save and re-load
        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
        sheet = wb2.getSheetAt(0);
        // Check
        assertNull(sheet.getRow(0).getCell(1).getCellComment());
        assertNotNull(sheet.getRow(1).getCell(1).getCellComment());
        assertNull(sheet.getRow(2).getCell(1).getCellComment());

        wb2.close();
    }

    /**
     * code from the quick guide
     */
    @Test
    void quickGuide() throws IOException {
        Workbook wb1 = _testDataProvider.createWorkbook();

        CreationHelper factory = wb1.getCreationHelper();

        Sheet sheet = wb1.createSheet();

        Cell cell = sheet.createRow(3).createCell(5);
        cell.setCellValue("F4");

        Drawing<?> drawing = sheet.createDrawingPatriarch();

        ClientAnchor anchor = factory.createClientAnchor();
        Comment comment = drawing.createCellComment(anchor);
        RichTextString str = factory.createRichTextString("Hello, World!");
        comment.setString(str);
        comment.setAuthor("Apache POI");
        //assign the comment to the cell
        cell.setCellComment(comment);

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
        sheet = wb2.getSheetAt(0);
        cell = sheet.getRow(3).getCell(5);
        comment = cell.getCellComment();
        assertNotNull(comment);
        assertEquals("Hello, World!", comment.getString().getString());
        assertEquals("Apache POI", comment.getAuthor());
        assertEquals(3, comment.getRow());
        assertEquals(5, comment.getColumn());

        wb2.close();
    }

    @Test
    void getClientAnchor() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();

        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(10);
        Cell cell = row.createCell(5);
        CreationHelper factory = wb.getCreationHelper();

        Drawing<?> drawing = sheet.createDrawingPatriarch();

        double r_mul, c_mul;
        if (sheet instanceof HSSFSheet) {
            double rowheight = Units.toEMU(row.getHeightInPoints())/(double)EMU_PER_PIXEL;
            r_mul = 256.0/rowheight;
            double colwidth = sheet.getColumnWidthInPixels(2);
            c_mul = 1024.0/colwidth;
        } else {
            r_mul = c_mul = EMU_PER_PIXEL;
        }

        int dx1 = (int)Math.round(10*c_mul);
        int dy1 = (int)Math.round(10*r_mul);
        int dx2 = (int)Math.round(3*c_mul);
        int dy2 = (int)Math.round(4*r_mul);
        int col1 = cell.getColumnIndex()+1;
        int row1 = row.getRowNum();
        int col2 = cell.getColumnIndex()+2;
        int row2 = row.getRowNum()+1;

        ClientAnchor anchor = drawing.createAnchor(dx1, dy1, dx2, dy2, col1, row1, col2, row2);
        Comment comment = drawing.createCellComment(anchor);
        comment.setVisible(true);
        cell.setCellComment(comment);

        anchor = comment.getClientAnchor();
        assertEquals(dx1, anchor.getDx1());
        assertEquals(dy1, anchor.getDy1());
        assertEquals(dx2, anchor.getDx2());
        assertEquals(dy2, anchor.getDy2());
        assertEquals(col1, anchor.getCol1());
        assertEquals(row1, anchor.getRow1());
        assertEquals(col2, anchor.getCol2());
        assertEquals(row2, anchor.getRow2());

        anchor = factory.createClientAnchor();
        comment = drawing.createCellComment(anchor);
        cell.setCellComment(comment);
        anchor = comment.getClientAnchor();

        if (sheet instanceof HSSFSheet) {
            assertEquals(0, anchor.getCol1());
            assertEquals(0, anchor.getDx1());
            assertEquals(0, anchor.getRow1());
            assertEquals(0, anchor.getDy1());
            assertEquals(0, anchor.getCol2());
            assertEquals(0, anchor.getDx2());
            assertEquals(0, anchor.getRow2());
            assertEquals(0, anchor.getDy2());
        } else {
            // when anchor is initialized without parameters, the comment anchor attributes default to
            // "1, 15, 0, 2, 3, 15, 3, 16" ... see XSSFVMLDrawing.newCommentShape()
            assertEquals( 1, anchor.getCol1());
            assertEquals(15*EMU_PER_PIXEL, anchor.getDx1());
            assertEquals( 0, anchor.getRow1());
            assertEquals( 2*EMU_PER_PIXEL, anchor.getDy1());
            assertEquals( 3, anchor.getCol2());
            assertEquals(15*EMU_PER_PIXEL, anchor.getDx2());
            assertEquals( 3, anchor.getRow2());
            assertEquals(16*EMU_PER_PIXEL, anchor.getDy2());
        }

        wb.close();
    }

    @Test
    void attemptToSave2CommentsWithSameCoordinates() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sh = wb.createSheet();
            CreationHelper factory = wb.getCreationHelper();
            Drawing<?> patriarch = sh.createDrawingPatriarch();
            patriarch.createCellComment(factory.createClientAnchor());

            RuntimeException e = assertThrows(RuntimeException.class, () -> {
                patriarch.createCellComment(factory.createClientAnchor());
                _testDataProvider.writeOutAndReadBack(wb);
            }, "Should not be able to create a corrupted workbook with multiple cell comments in one cell");

            if (wb instanceof HSSFWorkbook) {
                // HSSFWorkbooks fail when writing out workbook
                assertTrue(e instanceof IllegalStateException);
                assertEquals("found multiple cell comments for cell $A$1", e.getMessage());
            } else {
                // XSSFWorkbooks fail when creating and setting the cell address of the comment
                assertTrue(e instanceof IllegalArgumentException);
                assertEquals("Multiple cell comments in one cell are not allowed, cell: A1", e.getMessage());
            }
        }
    }

    @Test
    void getAddress() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sh = wb.createSheet();
        CreationHelper factory = wb.getCreationHelper();
        Drawing<?> patriarch = sh.createDrawingPatriarch();
        Comment comment = patriarch.createCellComment(factory.createClientAnchor());

        assertEquals(CellAddress.A1, comment.getAddress());
        Cell C2 = sh.createRow(1).createCell(2);
        C2.setCellComment(comment);
        assertEquals(new CellAddress("C2"), comment.getAddress());
    }

    @Test
    void setAddress() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sh = wb.createSheet();
        CreationHelper factory = wb.getCreationHelper();
        Drawing<?> patriarch = sh.createDrawingPatriarch();
        Comment comment = patriarch.createCellComment(factory.createClientAnchor());

        assertEquals(CellAddress.A1, comment.getAddress());
        CellAddress C2 = new CellAddress("C2");
        assertEquals("C2", C2.formatAsString());
        comment.setAddress(C2);
        assertEquals(C2, comment.getAddress());

        CellAddress E10 = new CellAddress(9, 4);
        assertEquals("E10", E10.formatAsString());
        comment.setAddress(9, 4);
        assertEquals(E10, comment.getAddress());
    }
}
