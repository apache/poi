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

import static org.apache.poi.xssf.usermodel.XSSFRelation.NS_SPREADSHEETML;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.microsoft.schemas.vml.CTShape;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.Comments;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRPrElt;

public final class TestXSSFComment extends BaseTestCellComment {

    private static final String TEST_RICHTEXTSTRING = "test richtextstring";

    public TestXSSFComment() {
        super(XSSFITestDataProvider.instance);
    }

    /**
     * test properties of a newly constructed comment
     */
    @Test
    void constructor() {
        CommentsTable sheetComments = new CommentsTable();
        assertNotNull(sheetComments.getCTComments().getCommentList());
        assertNotNull(sheetComments.getCTComments().getAuthors());
        assertEquals(1, sheetComments.getCTComments().getAuthors().sizeOfAuthorArray());
        assertEquals(1, sheetComments.getNumberOfAuthors());

        CTComment ctComment = sheetComments.newComment(CellAddress.A1);
        CTShape vmlShape = CTShape.Factory.newInstance();

        XSSFComment comment = new XSSFComment(sheetComments, ctComment, vmlShape);
        assertNull(comment.getString());
        assertEquals(0, comment.getRow());
        assertEquals(0, comment.getColumn());
        assertEquals("", comment.getAuthor());
        assertFalse(comment.isVisible());
    }

    @Test
    void getSetCol() {
        CommentsTable sheetComments = new CommentsTable();
        XSSFVMLDrawing vml = new XSSFVMLDrawing();
        CTComment ctComment = sheetComments.newComment(CellAddress.A1);
        CTShape vmlShape = vml.newCommentShape();

        XSSFComment comment = new XSSFComment(sheetComments, ctComment, vmlShape);
        comment.setColumn(1);
        assertEquals(1, comment.getColumn());
        assertEquals(1, new CellReference(ctComment.getRef()).getCol());
        assertEquals(1, vmlShape.getClientDataArray(0).getColumnArray(0).intValue());

        comment.setColumn(5);
        assertEquals(5, comment.getColumn());
        assertEquals(5, new CellReference(ctComment.getRef()).getCol());
        assertEquals(5, vmlShape.getClientDataArray(0).getColumnArray(0).intValue());
    }

    @Test
    void getSetRow() {
        CommentsTable sheetComments = new CommentsTable();
        XSSFVMLDrawing vml = new XSSFVMLDrawing();
        CTComment ctComment = sheetComments.newComment(CellAddress.A1);
        CTShape vmlShape = vml.newCommentShape();

        XSSFComment comment = new XSSFComment(sheetComments, ctComment, vmlShape);
        comment.setRow(1);
        assertEquals(1, comment.getRow());
        assertEquals(1, new CellReference(ctComment.getRef()).getRow());
        assertEquals(1, vmlShape.getClientDataArray(0).getRowArray(0).intValue());

        comment.setRow(5);
        assertEquals(5, comment.getRow());
        assertEquals(5, new CellReference(ctComment.getRef()).getRow());
        assertEquals(5, vmlShape.getClientDataArray(0).getRowArray(0).intValue());
    }

    @Test
    void setString() {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sh = wb.createSheet();
        XSSFComment comment = sh.createDrawingPatriarch().createCellComment(new XSSFClientAnchor());

        //passing HSSFRichTextString is incorrect
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
            () -> comment.setString(new HSSFRichTextString(TEST_RICHTEXTSTRING)));
        assertEquals("Only XSSFRichTextString argument is supported", e.getMessage());

        //simple string argument
        comment.setString(TEST_RICHTEXTSTRING);
        assertEquals(TEST_RICHTEXTSTRING, comment.getString().getString());

        //if the text is already set, it should be overridden, not added twice!
        comment.setString(TEST_RICHTEXTSTRING);

        CTComment ctComment = comment.getCTComment();
        XmlObject[] obj = ctComment.selectPath(
                "declare namespace w='"+NS_SPREADSHEETML+"' .//w:text");
        assertEquals(1, obj.length);
        assertEquals(TEST_RICHTEXTSTRING, comment.getString().getString());

        //sequential call of comment.getString() should return the same XSSFRichTextString object
        assertSame(comment.getString(), comment.getString());

        XSSFRichTextString richText = new XSSFRichTextString(TEST_RICHTEXTSTRING);
        XSSFFont font1 = wb.createFont();
        font1.setFontName("Tahoma");
        font1.setFontHeight(8.5);
        font1.setItalic(true);
        font1.setColor(IndexedColors.BLUE_GREY.getIndex());
        richText.applyFont(0, 5, font1);

        //check the low-level stuff
        comment.setString(richText);
        obj = ctComment.selectPath(
                "declare namespace w='"+NS_SPREADSHEETML+"' .//w:text");
        assertEquals(1, obj.length);
        assertSame(comment.getString(), richText);
        //check that the rich text is set in the comment
        CTRPrElt rPr = richText.getCTRst().getRArray(0).getRPr();
        assertTrue(rPr.getIArray(0).getVal());
        assertEquals(8.5, rPr.getSzArray(0).getVal(), 0);
        assertEquals(IndexedColors.BLUE_GREY.getIndex(), rPr.getColorArray(0).getIndexed());
        assertEquals("Tahoma", rPr.getRFontArray(0).getVal());

        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
    }

    @Test
    void author() {
        CommentsTable sheetComments = new CommentsTable();
        CTComment ctComment = sheetComments.newComment(CellAddress.A1);

        assertEquals(1, sheetComments.getNumberOfAuthors());
        XSSFComment comment = new XSSFComment(sheetComments, ctComment, null);
        assertEquals("", comment.getAuthor());
        comment.setAuthor("Apache POI");
        assertEquals("Apache POI", comment.getAuthor());
        assertEquals(2, sheetComments.getNumberOfAuthors());
        comment.setAuthor("Apache POI");
        assertEquals(2, sheetComments.getNumberOfAuthors());
        comment.setAuthor("");
        assertEquals("", comment.getAuthor());
        assertEquals(2, sheetComments.getNumberOfAuthors());
    }

    @Test
    void testBug58175() throws IOException {
        try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
            Sheet sheet = wb.createSheet();

            Row row = sheet.createRow(1);
            Cell cell = row.createCell(3);

            cell.setCellValue("F4");

            CreationHelper factory = wb.getCreationHelper();

            // When the comment box is visible, have it show in a 1x3 space
            ClientAnchor anchor = factory.createClientAnchor();
            anchor.setCol1(cell.getColumnIndex());
            anchor.setCol2(cell.getColumnIndex() + 1);
            anchor.setRow1(row.getRowNum());
            anchor.setRow2(row.getRowNum() + 3);

            XSSFClientAnchor ca = (XSSFClientAnchor) anchor;

            // create comments and vmlDrawing parts if they don't exist
            Comments comments = wb.getXSSFWorkbook()
                    .getSheetAt(0).getCommentsTable(true);
            XSSFVMLDrawing vml = wb.getXSSFWorkbook()
                    .getSheetAt(0).getVMLDrawing(true);
            CTShape vmlShape1 = vml.newCommentShape();
            if (ca.isSet()) {
                String position = ca.getCol1() + ", 0, " + ca.getRow1()
                        + ", 0, " + ca.getCol2() + ", 0, " + ca.getRow2()
                        + ", 0";
                vmlShape1.getClientDataArray(0).setAnchorArray(0, position);
            }

            // create the comment in two different ways and verify that there is no difference
            CommentsTable commentsTable = (CommentsTable)comments;
            XSSFComment shape1 = new XSSFComment(comments, commentsTable.newComment(CellAddress.A1), vmlShape1);
            shape1.setColumn(ca.getCol1());
            shape1.setRow(ca.getRow1());

            CTShape vmlShape2 = vml.newCommentShape();
            if (ca.isSet()) {
                String position = ca.getCol1() + ", 0, " + ca.getRow1()
                        + ", 0, " + ca.getCol2() + ", 0, " + ca.getRow2()
                        + ", 0";
                vmlShape2.getClientDataArray(0).setAnchorArray(0, position);
            }

            CellAddress ref = new CellAddress(ca.getRow1(), ca.getCol1());
            XSSFComment shape2 = new XSSFComment(comments, commentsTable.newComment(ref), vmlShape2);

            assertEquals(shape1.getAuthor(), shape2.getAuthor());
            assertEquals(shape1.getClientAnchor(), shape2.getClientAnchor());
            assertEquals(shape1.getColumn(), shape2.getColumn());
            assertEquals(shape1.getRow(), shape2.getRow());
            assertEquals(shape1.getCTComment().toString(), shape2.getCTComment().toString());
            assertEquals(shape1.getCTComment().getRef(), shape2.getCTComment().getRef());

            /*CommentsTable table1 = shape1.getCommentsTable();
            CommentsTable table2 = shape2.getCommentsTable();
            assertEquals(table1.getCTComments().toString(), table2.getCTComments().toString());
            assertEquals(table1.getNumberOfComments(), table2.getNumberOfComments());
            assertEquals(table1.getRelations(), table2.getRelations());*/

            assertEquals(vmlShape1.toString().replaceAll("_x0000_s\\d+", "_x0000_s0000"), vmlShape2.toString().replaceAll("_x0000_s\\d+", "_x0000_s0000"),
                "The vmlShapes should have equal content afterwards");
        }
    }

    @Test
    void testBug55814() throws IOException {
        try (Workbook wb = XSSFTestDataSamples.openSampleWorkbook("55814.xlsx")) {

            int oldsheetIndex = wb.getSheetIndex("example");
            Sheet oldsheet = wb.getSheetAt(oldsheetIndex);

            Comment comment = oldsheet.getRow(0).getCell(0).getCellComment();
            assertEquals("Comment Here\n", comment.getString().getString());

            Sheet newsheet = wb.cloneSheet(oldsheetIndex);

            wb.removeSheetAt(oldsheetIndex);

            //wb.write(new FileOutputStream("/tmp/outnocomment.xlsx"));

            comment = newsheet.getRow(0).getCell(0).getCellComment();
            assertNotNull(comment, "Should have a comment on A1 in the new sheet");
            assertEquals("Comment Here\n", comment.getString().getString());

            Workbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb);
            assertNotNull(wbBack);
            wbBack.close();
        }

        try (Workbook wb = XSSFTestDataSamples.openSampleWorkbook("55814.xlsx")) {
            int oldsheetIndex = wb.getSheetIndex("example");
            Sheet newsheet = wb.getSheetAt(oldsheetIndex);
            Comment comment = newsheet.getRow(0).getCell(0).getCellComment();
            assertEquals("Comment Here\n", comment.getString().getString());
        }
    }

    @Test
    void bug57838DeleteRowsWthCommentsBug() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("57838.xlsx");
        Sheet sheet=wb.getSheetAt(0);
        Comment comment1 = sheet.getCellComment(new CellAddress(2, 1));
        assertNotNull(comment1);
        Comment comment2 = sheet.getCellComment(new CellAddress(2, 2));
        assertNotNull(comment2);
        Row row=sheet.getRow(2);
        assertNotNull(row);

        sheet.removeRow(row); // Remove row from index 2

        row=sheet.getRow(2);
        assertNull(row); // Row is null since we deleted it.

        comment1 = sheet.getCellComment(new CellAddress(2, 1));
        assertNull(comment1); // comment should be null but will fail due to bug
        comment2 = sheet.getCellComment(new CellAddress(2, 2));
        assertNull(comment2); // comment should be null but will fail due to bug

        wb.close();
    }

    @Test
    void bug59388CommentVisible() throws IOException {
        try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
            try (Workbook wb = XSSFTestDataSamples.openSampleWorkbook("59388.xlsx")) {
                Sheet sheet = wb.getSheetAt(0);
                Cell a1 = sheet.getRow(0).getCell(0);
                Cell d1 = sheet.getRow(0).getCell(3);

                Comment commentA1 = a1.getCellComment();
                Comment commentD1 = d1.getCellComment();

                // assert original visibility
                assertTrue(commentA1.isVisible());
                assertFalse(commentD1.isVisible());

                commentA1.setVisible(false);
                commentD1.setVisible(true);

                // assert after changing
                assertFalse(commentA1.isVisible());
                assertTrue(commentD1.isVisible());

                // check result
                wb.write(bos);

                try (Workbook wb2 = new XSSFWorkbook(bos.toInputStream())) {
                    Sheet sheetWb2 = wb2.getSheetAt(0);
                    Cell a1Wb2 = sheetWb2.getRow(0).getCell(0);
                    Cell d1Wb2 = sheetWb2.getRow(0).getCell(3);

                    assertFalse(a1Wb2.getCellComment().isVisible());
                    assertTrue(d1Wb2.getCellComment().isVisible());
                }
            }
        }
    }

    @Test
    void testMoveComment() throws Exception {
        _testMove(new XSSFWorkbook());
    }

    @Test
    void testMoveCommentSXSSF() throws Exception {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        try {
            _testMove(workbook);
        } finally {
            workbook.dispose();
        }
    }

    @Test
    void testMoveCommentCopy() throws Exception {
        _testMoveCopy(new XSSFWorkbook());
    }

    @Test
    void testMoveCommentCopySXSSF() throws Exception {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        try {
            _testMoveCopy(workbook);
        } finally {
            workbook.dispose();
        }
    }

    @Test
    void testMoveCommentIsSaved() throws Exception {
        _testMoveIsSaved(new XSSFWorkbook());
    }

    @Test
    void testMoveCommentIsSavedSXSSF() throws Exception {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        try {
            _testMoveIsSaved(workbook);
        } finally {
            workbook.dispose();
        }
    }

    @Test
    void testModifiedCommentIsSaved() throws Exception {
        _testModificationIsSaved(new XSSFWorkbook());
    }

    @Test
    void testModifiedCommentIsSavedSXSSF() throws Exception {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        try {
            _testModificationIsSaved(workbook);
        } finally {
            workbook.dispose();
        }
    }

    private void _testMove(Workbook workbook) throws Exception {
        CommentsTable commentsTable = new CommentsTable();
        try {
            CreationHelper factory = workbook.getCreationHelper();
            Sheet sheet = workbook.createSheet();
            commentsTable.setSheet(sheet);
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("CellA1");
            ClientAnchor anchor = factory.createClientAnchor();
            anchor.setCol1(0);
            anchor.setCol2(1);
            anchor.setRow1(row.getRowNum());
            anchor.setRow2(row.getRowNum());
            XSSFComment comment = commentsTable.createNewComment(anchor);
            String uniqueText = UUID.randomUUID().toString();
            comment.setString(uniqueText);
            comment.setAuthor("author" + uniqueText);

            XSSFComment comment1 = commentsTable.findCellComment(new CellAddress("A1"));
            assertEquals(comment.getString().getString(), comment1.getString().getString());

            comment.setAddress(1, 1);
            assertNull(commentsTable.findCellComment(new CellAddress("A1")),
                    "no longer a comment on cell A1?");

            XSSFComment comment2 = commentsTable.findCellComment(new CellAddress("B2"));
            assertEquals(comment.getString().getString(), comment2.getString().getString());
        } finally {
            workbook.close();
        }
    }

    private void _testMoveCopy(Workbook workbook) throws Exception {
        CommentsTable commentsTable = new CommentsTable();
        try {
            CreationHelper factory = workbook.getCreationHelper();
            Sheet sheet = workbook.createSheet();
            commentsTable.setSheet(sheet);
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("CellA1");
            ClientAnchor anchor = factory.createClientAnchor();
            anchor.setCol1(0);
            anchor.setCol2(1);
            anchor.setRow1(row.getRowNum());
            anchor.setRow2(row.getRowNum());
            XSSFComment comment = commentsTable.createNewComment(anchor);
            String uniqueText = UUID.randomUUID().toString();
            comment.setString(uniqueText);
            comment.setAuthor("author" + uniqueText);

            XSSFComment comment1 = commentsTable.findCellComment(new CellAddress("A1"));
            assertEquals(comment.getString().getString(), comment1.getString().getString());

            //like testMoveComment but moves the copy of the comment (comment1) instead
            comment1.setAddress(1, 1);
            assertNull(commentsTable.findCellComment(new CellAddress("A1")),
                    "no longer a comment on cell A1?");

            XSSFComment comment2 = commentsTable.findCellComment(new CellAddress("B2"));
            assertEquals(comment.getString().getString(), comment2.getString().getString());
        } finally {
            workbook.close();
        }
    }

    private void _testMoveIsSaved(Workbook workbook) throws Exception {
        try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
            CreationHelper factory = workbook.getCreationHelper();
            Sheet sheet = workbook.createSheet();
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("CellA1");
            ClientAnchor anchor = factory.createClientAnchor();
            anchor.setCol1(0);
            anchor.setCol2(1);
            anchor.setRow1(row.getRowNum());
            anchor.setRow2(row.getRowNum());
            Drawing drawing = sheet.createDrawingPatriarch();
            Comment comment = drawing.createCellComment(anchor);
            String uniqueText = UUID.randomUUID().toString();
            comment.setString(factory.createRichTextString(uniqueText));
            comment.setAuthor("author" + uniqueText);
            Comment comment1 = sheet.getCellComment(new CellAddress("A1"));
            assertEquals(comment.getString().getString(), comment1.getString().getString());

            comment.setAddress(1, 1);
            Comment comment2 = sheet.getCellComment(new CellAddress("B2"));
            assertEquals(comment.getString().getString(), comment2.getString().getString());
            assertNull( sheet.getCellComment(new CellAddress("A1")), "comment still found on A1?");

            workbook.write(bos);

            try (XSSFWorkbook workbook2 = new XSSFWorkbook(bos.toInputStream())) {
                XSSFSheet wb2Sheet = workbook2.getSheetAt(0);
                Map<CellAddress, XSSFComment> cellComments = wb2Sheet.getCellComments();
                assertEquals(1, cellComments.size());
                CellAddress address0 = (CellAddress) cellComments.keySet().toArray()[0];
                assertEquals("B2", address0.formatAsString());
            }
        }
    }

    private void _testModificationIsSaved(Workbook workbook) throws Exception {
        try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
            CreationHelper factory = workbook.getCreationHelper();
            Sheet sheet = workbook.createSheet();
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("CellA1");
            ClientAnchor anchor = factory.createClientAnchor();
            anchor.setCol1(0);
            anchor.setCol2(1);
            anchor.setRow1(row.getRowNum());
            anchor.setRow2(row.getRowNum());
            Drawing drawing = sheet.createDrawingPatriarch();
            Comment comment = drawing.createCellComment(anchor);
            comment.setString(factory.createRichTextString("initText"));
            comment.setAuthor("initAuthor");

            String uniqueText = UUID.randomUUID().toString();
            comment.setString(factory.createRichTextString(uniqueText));
            comment.setAuthor("author" + uniqueText);

            workbook.write(bos);

            try (XSSFWorkbook workbook2 = new XSSFWorkbook(bos.toInputStream())) {
                XSSFSheet wb2Sheet = workbook2.getSheetAt(0);
                Map<CellAddress, XSSFComment> cellComments = wb2Sheet.getCellComments();
                assertEquals(1, cellComments.size());
                CellAddress address0 = (CellAddress) cellComments.keySet().toArray()[0];
                assertEquals("A1", address0.formatAsString());
                XSSFComment savedComment = cellComments.get(address0);
                assertEquals(comment.getString().getString(), savedComment.getString().getString());
                assertEquals(comment.getAuthor(), savedComment.getAuthor());
            }
        } finally {
            workbook.close();
        }
    }

}
