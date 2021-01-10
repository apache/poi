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

package org.apache.poi.xssf.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCommentList;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComments;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;


class TestCommentsTable {

    private static final String TEST_A2_TEXT = "test A2 text";
    private static final String TEST_A1_TEXT = "test A1 text";
    private static final String TEST_AUTHOR = "test author";

    @Test
    void findAuthor() {
        CommentsTable sheetComments = new CommentsTable();
        assertEquals(1, sheetComments.getNumberOfAuthors());
        assertEquals(0, sheetComments.findAuthor(""));
        assertEquals("", sheetComments.getAuthor(0));

        assertEquals(1, sheetComments.findAuthor(TEST_AUTHOR));
        assertEquals(2, sheetComments.findAuthor("another author"));
        assertEquals(1, sheetComments.findAuthor(TEST_AUTHOR));
        assertEquals(3, sheetComments.findAuthor("YAA"));
        assertEquals(2, sheetComments.findAuthor("another author"));
    }

    @Test
    void getCellComment() {
        CommentsTable sheetComments = new CommentsTable();

        CTComments comments = sheetComments.getCTComments();
        CTCommentList commentList = comments.getCommentList();

        // Create 2 comments for A1 and A" cells
        CTComment comment0 = commentList.insertNewComment(0);
        comment0.setRef("A1");
        CTRst ctrst0 = CTRst.Factory.newInstance();
        ctrst0.setT(TEST_A1_TEXT);
        comment0.setText(ctrst0);
        CTComment comment1 = commentList.insertNewComment(0);
        comment1.setRef("A2");
        CTRst ctrst1 = CTRst.Factory.newInstance();
        ctrst1.setT(TEST_A2_TEXT);
        comment1.setText(ctrst1);

        // test finding the right comment for a cell
        assertSame(comment0, sheetComments.getCTComment(new CellAddress("A1")));
        assertSame(comment1, sheetComments.getCTComment(new CellAddress("A2")));
        assertNull(sheetComments.getCTComment(new CellAddress("A3")));
    }


    @Test
    void existing() throws IOException {
        try (Workbook workbook = XSSFTestDataSamples.openSampleWorkbook("WithVariousData.xlsx")) {
            Sheet sheet1 = workbook.getSheetAt(0);
            Sheet sheet2 = workbook.getSheetAt(1);

            assertTrue(((XSSFSheet) sheet1).hasComments());
            assertFalse(((XSSFSheet) sheet2).hasComments());

            // Comments should be in C5 and C7
            Row r5 = sheet1.getRow(4);
            Row r7 = sheet1.getRow(6);
            assertNotNull(r5.getCell(2).getCellComment());
            assertNotNull(r7.getCell(2).getCellComment());

            // Check they have what we expect
            // TODO: Rich text formatting
            Comment cc5 = r5.getCell(2).getCellComment();
            Comment cc7 = r7.getCell(2).getCellComment();

            assertEquals("Nick Burch", cc5.getAuthor());
            assertEquals("Nick Burch:\nThis is a comment", cc5.getString().getString());
            assertEquals(4, cc5.getRow());
            assertEquals(2, cc5.getColumn());

            assertEquals("Nick Burch", cc7.getAuthor());
            assertEquals("Nick Burch:\nComment #1\n", cc7.getString().getString());
            assertEquals(6, cc7.getRow());
            assertEquals(2, cc7.getColumn());
        }
    }

    @Test
    void writeRead() throws IOException {
        try (XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("WithVariousData.xlsx")) {
            XSSFSheet sheet1 = workbook.getSheetAt(0);
            XSSFSheet sheet2 = workbook.getSheetAt(1);

            assertTrue(sheet1.hasComments());
            assertFalse(sheet2.hasComments());

            // Change on comment on sheet 1, and add another into
            //  sheet 2
            Row r5 = sheet1.getRow(4);
            Comment cc5 = r5.getCell(2).getCellComment();
            cc5.setAuthor("Apache POI");
            cc5.setString(new XSSFRichTextString("Hello!"));

            Row r2s2 = sheet2.createRow(2);
            Cell c1r2s2 = r2s2.createCell(1);
            assertNull(c1r2s2.getCellComment());

            Drawing<?> dg = sheet2.createDrawingPatriarch();
            Comment cc2 = dg.createCellComment(new XSSFClientAnchor());
            cc2.setAuthor("Also POI");
            cc2.setString(new XSSFRichTextString("A new comment"));
            c1r2s2.setCellComment(cc2);

            // Save, and re-load the file
            try (XSSFWorkbook workbookBack = XSSFTestDataSamples.writeOutAndReadBack(workbook)) {
                // Check we still have comments where we should do
                sheet1 = workbookBack.getSheetAt(0);
                sheet2 = workbookBack.getSheetAt(1);
                assertNotNull(sheet1.getRow(4).getCell(2).getCellComment());
                assertNotNull(sheet1.getRow(6).getCell(2).getCellComment());
                assertNotNull(sheet2.getRow(2).getCell(1).getCellComment());

                // And check they still have the contents they should do
                assertEquals("Apache POI",
                        sheet1.getRow(4).getCell(2).getCellComment().getAuthor());
                assertEquals("Nick Burch",
                        sheet1.getRow(6).getCell(2).getCellComment().getAuthor());
                assertEquals("Also POI",
                        sheet2.getRow(2).getCell(1).getCellComment().getAuthor());

                assertEquals("Hello!",
                        sheet1.getRow(4).getCell(2).getCellComment().getString().getString());
            }
        }
    }

    @Test
    void readWriteMultipleAuthors() throws IOException {
        try (XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("WithMoreVariousData.xlsx")) {
            XSSFSheet sheet1 = workbook.getSheetAt(0);
            XSSFSheet sheet2 = workbook.getSheetAt(1);

            assertTrue(sheet1.hasComments());
            assertFalse(sheet2.hasComments());

            assertEquals("Nick Burch",
                    sheet1.getRow(4).getCell(2).getCellComment().getAuthor());
            assertEquals("Nick Burch",
                    sheet1.getRow(6).getCell(2).getCellComment().getAuthor());
            assertEquals("Torchbox",
                    sheet1.getRow(12).getCell(2).getCellComment().getAuthor());

            // Save, and re-load the file
            try (XSSFWorkbook workbookBack = XSSFTestDataSamples.writeOutAndReadBack(workbook)) {

                // Check we still have comments where we should do
                sheet1 = workbookBack.getSheetAt(0);
                assertNotNull(sheet1.getRow(4).getCell(2).getCellComment());
                assertNotNull(sheet1.getRow(6).getCell(2).getCellComment());
                assertNotNull(sheet1.getRow(12).getCell(2).getCellComment());

                // And check they still have the contents they should do
                assertEquals("Nick Burch",
                        sheet1.getRow(4).getCell(2).getCellComment().getAuthor());
                assertEquals("Nick Burch",
                        sheet1.getRow(6).getCell(2).getCellComment().getAuthor());
                assertEquals("Torchbox",
                        sheet1.getRow(12).getCell(2).getCellComment().getAuthor());

                // Todo - check text too, once bug fixed
            }
        }
    }

    @Test
    void removeComment() {
        final CellAddress addrA1 = new CellAddress("A1");
        final CellAddress addrA2 = new CellAddress("A2");
        final CellAddress addrA3 = new CellAddress("A3");

        CommentsTable sheetComments = new CommentsTable();
        CTComment a1 = sheetComments.newComment(addrA1);
        CTComment a2 = sheetComments.newComment(addrA2);
        CTComment a3 = sheetComments.newComment(addrA3);

        assertSame(a1, sheetComments.getCTComment(addrA1));
        assertSame(a2, sheetComments.getCTComment(addrA2));
        assertSame(a3, sheetComments.getCTComment(addrA3));
        assertEquals(3, sheetComments.getNumberOfComments());

        assertTrue(sheetComments.removeComment(addrA1));
        assertEquals(2, sheetComments.getNumberOfComments());
        assertNull(sheetComments.getCTComment(addrA1));
        assertSame(a2, sheetComments.getCTComment(addrA2));
        assertSame(a3, sheetComments.getCTComment(addrA3));

        assertTrue(sheetComments.removeComment(addrA2));
        assertEquals(1, sheetComments.getNumberOfComments());
        assertNull(sheetComments.getCTComment(addrA1));
        assertNull(sheetComments.getCTComment(addrA2));
        assertSame(a3, sheetComments.getCTComment(addrA3));

        assertTrue(sheetComments.removeComment(addrA3));
        assertEquals(0, sheetComments.getNumberOfComments());
        assertNull(sheetComments.getCTComment(addrA1));
        assertNull(sheetComments.getCTComment(addrA2));
        assertNull(sheetComments.getCTComment(addrA3));
    }

    @Test
    void bug54920() throws IOException {
        final Workbook workbook = new XSSFWorkbook();
        final Sheet sheet = workbook.createSheet("sheet01");
        // create anchor
        CreationHelper helper = sheet.getWorkbook().getCreationHelper();
        ClientAnchor anchor = helper.createClientAnchor();

        // place comment in A1
        // NOTE - only occurs if a comment is placed in A1 first
        Cell A1 = getCell(sheet, 0, 0);
        //Cell A1 = getCell(sheet, 2, 2);
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        setComment(A1, drawing, "for A1", helper, anchor);

        // find comment in A1 before we set the comment in B2
        Comment commentA1 = A1.getCellComment();
        assertNotNull(commentA1, "Should still find the previous comment in A1, but had null");
        assertEquals("for A1", commentA1.getString().getString(), "should find correct comment in A1, but had null: " + commentA1);

        // place comment in B2, according to Bug 54920 this removes the comment in A1!
        Cell B2 = getCell(sheet, 1, 1);
        setComment(B2, drawing, "for B2", helper, anchor);

        // find comment in A1
        Comment commentB2 = B2.getCellComment();
        assertEquals("for B2", commentB2.getString().getString(), "should find correct comment in B2, but had null: " + commentB2);

        // find comment in A1
        commentA1 = A1.getCellComment();
        assertNotNull(commentA1, "Should still find the previous comment in A1, but had null");
        assertEquals("for A1", commentA1.getString().getString(), "should find correct comment in A1, but had null: " + commentA1);

        workbook.close();
    }

    // Set the comment on a sheet
    //
    private static void setComment(Cell cell, Drawing<?> drawing, String commentText, CreationHelper helper, ClientAnchor anchor) {
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex());
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex());

        // get comment, or create if it does not exist
        // NOTE - only occurs if getCellComment is called first
        Comment comment = cell.getCellComment();
        //Comment comment = null;
        if (comment == null) {
            comment = drawing.createCellComment(anchor);
        }
        comment.setAuthor("Test");

        // attach the comment to the cell
        comment.setString(helper.createRichTextString(commentText));
        cell.setCellComment(comment);
    }

    // Get a cell, create as needed
    //
    private static Cell getCell(Sheet sheet, int rowIndex, int colIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }

        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex);
        }

        return cell;
    }
}
