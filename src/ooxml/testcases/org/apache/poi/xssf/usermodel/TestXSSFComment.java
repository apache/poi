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

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.BaseTestCellComment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRPrElt;

import schemasMicrosoftComVml.CTShape;

/**
 * @author Yegor Kozlov
 */
public final class TestXSSFComment extends BaseTestCellComment  {

    private static final String TEST_RICHTEXTSTRING = "test richtextstring";

    public TestXSSFComment() {
        super(XSSFITestDataProvider.instance);
    }

    /**
     * test properties of a newly constructed comment
     */
    public void testConstructor() {
        CommentsTable sheetComments = new CommentsTable();
        assertNotNull(sheetComments.getCTComments().getCommentList());
        assertNotNull(sheetComments.getCTComments().getAuthors());
        assertEquals(1, sheetComments.getCTComments().getAuthors().sizeOfAuthorArray());
        assertEquals(1, sheetComments.getNumberOfAuthors());

        CTComment ctComment = sheetComments.newComment();
        CTShape vmlShape = CTShape.Factory.newInstance();

        XSSFComment comment = new XSSFComment(sheetComments, ctComment, vmlShape);
        assertEquals(null, comment.getString());
        assertEquals(0, comment.getRow());
        assertEquals(0, comment.getColumn());
        assertEquals("", comment.getAuthor());
        assertEquals(false, comment.isVisible());
    }

    public void testGetSetCol() {
        CommentsTable sheetComments = new CommentsTable();
        XSSFVMLDrawing vml = new XSSFVMLDrawing();
        CTComment ctComment = sheetComments.newComment();
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

    public void testGetSetRow() {
        CommentsTable sheetComments = new CommentsTable();
        XSSFVMLDrawing vml = new XSSFVMLDrawing();
        CTComment ctComment = sheetComments.newComment();
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

    public void testSetString() {
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
                "declare namespace w='http://schemas.openxmlformats.org/spreadsheetml/2006/main' .//w:text");
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
                "declare namespace w='http://schemas.openxmlformats.org/spreadsheetml/2006/main' .//w:text");
        assertEquals(1, obj.length);
        assertSame(comment.getString(), richText);
        //check that the rich text is set in the comment
        CTRPrElt rPr = richText.getCTRst().getRArray(0).getRPr();
        assertEquals(true, rPr.getIArray(0).getVal());
        assertEquals(8.5, rPr.getSzArray(0).getVal());
        assertEquals(IndexedColors.BLUE_GREY.getIndex(), rPr.getColorArray(0).getIndexed());
        assertEquals("Tahoma", rPr.getRFontArray(0).getVal());
    }

    public void testAuthor() {
        CommentsTable sheetComments = new CommentsTable();
        CTComment ctComment = sheetComments.newComment();

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
}
