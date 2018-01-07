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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.BaseTestCellComment;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.xmlbeans.XmlObject;
import org.junit.Ignore;
import org.junit.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRPrElt;

import com.microsoft.schemas.vml.CTShape;

public final class TestXSSFComment extends BaseTestCellComment  {

    private static final String TEST_RICHTEXTSTRING = "test richtextstring";

    public TestXSSFComment() {
        super(XSSFITestDataProvider.instance);
    }

    /**
     * test properties of a newly constructed comment
     */
    @Test
    public void constructor() {
        CommentsTable sheetComments = new CommentsTable();
        assertNotNull(sheetComments.getCTComments().getCommentList());
        assertNotNull(sheetComments.getCTComments().getAuthors());
        assertEquals(1, sheetComments.getCTComments().getAuthors().sizeOfAuthorArray());
        assertEquals(1, sheetComments.getNumberOfAuthors());

        CTComment ctComment = sheetComments.newComment(CellAddress.A1);
        CTShape vmlShape = CTShape.Factory.newInstance();

        XSSFComment comment = new XSSFComment(sheetComments, ctComment, vmlShape);
        assertEquals(null, comment.getString());
        assertEquals(0, comment.getRow());
        assertEquals(0, comment.getColumn());
        assertEquals("", comment.getAuthor());
        assertEquals(false, comment.isVisible());
    }

    @Test
    public void getSetCol() {
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
    public void getSetRow() {
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
    public void setString() {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sh = wb.createSheet();
        XSSFComment comment = sh.createDrawingPatriarch().createCellComment(new XSSFClientAnchor());

        //passing HSSFRichTextString is incorrect
        try {
            comment.setString(new HSSFRichTextString(TEST_RICHTEXTSTRING));
            fail("expected exception");
        } catch (IllegalArgumentException e){
            assertEquals("Only XSSFRichTextString argument is supported", e.getMessage());
        }

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
        assertEquals(true, rPr.getIArray(0).getVal());
        assertEquals(8.5, rPr.getSzArray(0).getVal(), 0);
        assertEquals(IndexedColors.BLUE_GREY.getIndex(), rPr.getColorArray(0).getIndexed());
        assertEquals("Tahoma", rPr.getRFontArray(0).getVal());
        
        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
    }

    @Test
    public void author() {
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
    public void testBug58175() throws IOException {
        Workbook wb = new SXSSFWorkbook();
        try {
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
            CommentsTable comments = ((SXSSFWorkbook) wb).getXSSFWorkbook()
                    .getSheetAt(0).getCommentsTable(true);
            XSSFVMLDrawing vml = ((SXSSFWorkbook) wb).getXSSFWorkbook()
                    .getSheetAt(0).getVMLDrawing(true);
            CTShape vmlShape1 = vml.newCommentShape();
            if (ca.isSet()) {
                String position = ca.getCol1() + ", 0, " + ca.getRow1()
                        + ", 0, " + ca.getCol2() + ", 0, " + ca.getRow2()
                        + ", 0";
                vmlShape1.getClientDataArray(0).setAnchorArray(0, position);
            }

            // create the comment in two different ways and verify that there is no difference
            XSSFComment shape1 = new XSSFComment(comments, comments.newComment(CellAddress.A1), vmlShape1);
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
            XSSFComment shape2 = new XSSFComment(comments, comments.newComment(ref), vmlShape2);
        
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
            
            assertEquals("The vmlShapes should have equal content afterwards", 
                    vmlShape1.toString().replaceAll("_x0000_s\\d+", "_x0000_s0000"), vmlShape2.toString().replaceAll("_x0000_s\\d+", "_x0000_s0000"));
        } finally {
            wb.close();
        }
    }

    @Ignore("Used for manual testing with opening the resulting Workbook in Excel")
    @Test
    public void testBug58175a() throws IOException {
        Workbook wb = new SXSSFWorkbook();
        try {
            Sheet sheet = wb.createSheet();

            Row row = sheet.createRow(1);
            Cell cell = row.createCell(3);

            cell.setCellValue("F4");

            Drawing<?> drawing = sheet.createDrawingPatriarch();

            CreationHelper factory = wb.getCreationHelper();

            // When the comment box is visible, have it show in a 1x3 space
            ClientAnchor anchor = factory.createClientAnchor();
            anchor.setCol1(cell.getColumnIndex());
            anchor.setCol2(cell.getColumnIndex() + 1);
            anchor.setRow1(row.getRowNum());
            anchor.setRow2(row.getRowNum() + 3);

            // Create the comment and set the text+author
            Comment comment = drawing.createCellComment(anchor);
            RichTextString str = factory.createRichTextString("Hello, World!");
            comment.setString(str);
            comment.setAuthor("Apache POI");

            /* fixed the problem as well 
             * comment.setColumn(cell.getColumnIndex());
             * comment.setRow(cell.getRowIndex());
             */

            // Assign the comment to the cell
            cell.setCellComment(comment);

            OutputStream out = new FileOutputStream("C:\\temp\\58175.xlsx");
            try {
                wb.write(out);
            } finally {
                out.close();
            }
        } finally {
            wb.close();
        }
    }

    @Test
    public void testBug55814() throws IOException {
		try (Workbook wb = XSSFTestDataSamples.openSampleWorkbook("55814.xlsx")) {

            int oldsheetIndex = wb.getSheetIndex("example");
            Sheet oldsheet = wb.getSheetAt(oldsheetIndex);

            Comment comment = oldsheet.getRow(0).getCell(0).getCellComment();
            assertEquals("Comment Here\n", comment.getString().getString());

            Sheet newsheet = wb.cloneSheet(oldsheetIndex);

            wb.removeSheetAt(oldsheetIndex);

            //wb.write(new FileOutputStream("/tmp/outnocomment.xlsx"));

            comment = newsheet.getRow(0).getCell(0).getCellComment();
            assertNotNull("Should have a comment on A1 in the new sheet", comment);
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
    public void bug57838DeleteRowsWthCommentsBug() throws IOException {
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
}
