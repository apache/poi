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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.POIXMLDocumentPart;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAuthors;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComments;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CommentsDocument;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;


public class TestXSSFComment extends TestCase {

    private static final String TEST_RICHTEXTSTRING = "test richtextstring";
    private static final String TEST_AUTHOR = "test_author";

    public void testConstructors() {
        CommentsTable sheetComments = new CommentsTable();
        XSSFComment comment = sheetComments.addComment();
        assertNotNull(comment);

        CTComment ctComment = CTComment.Factory.newInstance();
        XSSFComment comment2 = new XSSFComment(sheetComments, ctComment);
        assertNotNull(comment2);
    }

    public void testGetColumn() {
        CommentsTable sheetComments = new CommentsTable();
        CTComment ctComment = CTComment.Factory.newInstance();
        ctComment.setRef("A1");
        XSSFComment comment = new XSSFComment(sheetComments, ctComment);
        assertNotNull(comment);
        assertEquals(0, comment.getColumn());
        ctComment.setRef("C10");
        assertEquals(2, comment.getColumn());
    }

    public void testGetRow() {
        CommentsTable sheetComments = new CommentsTable();
        CTComment ctComment = CTComment.Factory.newInstance();
        ctComment.setRef("A1");
        XSSFComment comment = new XSSFComment(sheetComments, ctComment);
        assertNotNull(comment);
        assertEquals(0, comment.getRow());
        ctComment.setRef("C10");
        assertEquals(9, comment.getRow());
    }

    public void testGetAuthor() throws Exception {
        CommentsDocument doc = CommentsDocument.Factory.newInstance();
        CTComments ctComments = CTComments.Factory.newInstance();
        CTComment ctComment = ctComments.addNewCommentList().addNewComment();
        CTAuthors ctAuthors = ctComments.addNewAuthors();
        ctAuthors.insertAuthor(0, TEST_AUTHOR);
        ctComment.setAuthorId(0);
        doc.setComments(ctComments);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.save(out, POIXMLDocumentPart.DEFAULT_XML_OPTIONS);

        CommentsTable sheetComments = new CommentsTable();
        sheetComments.readFrom(new ByteArrayInputStream(out.toByteArray()));
        XSSFComment comment = new XSSFComment(sheetComments, ctComment);
        assertEquals(TEST_AUTHOR, comment.getAuthor());
    }

    public void testSetColumn() {
        CommentsTable sheetComments = new CommentsTable();
        CTComment ctComment = CTComment.Factory.newInstance();
        XSSFComment comment = new XSSFComment(sheetComments, ctComment);
        comment.setColumn((short)3);
        assertEquals(3, comment.getColumn());
        assertEquals(3, (new CellReference(ctComment.getRef()).getCol()));
        assertEquals("D1", ctComment.getRef());

        comment.setColumn((short)13);
        assertEquals(13, comment.getColumn());
    }

    public void testSetRow() {
        CommentsTable sheetComments = new CommentsTable();
        CTComment ctComment = CTComment.Factory.newInstance();
        XSSFComment comment = new XSSFComment(sheetComments, ctComment);
        comment.setRow(20);
        assertEquals(20, comment.getRow());
        assertEquals(20, (new CellReference(ctComment.getRef()).getRow()));
        assertEquals("A21", ctComment.getRef());

        comment.setRow(19);
        assertEquals(19, comment.getRow());
    }

    public void testSetAuthor() {
        CommentsTable sheetComments = new CommentsTable();
        CTComment ctComment = CTComment.Factory.newInstance();
        XSSFComment comment = new XSSFComment(sheetComments, ctComment);
        comment.setAuthor(TEST_AUTHOR);
        assertEquals(TEST_AUTHOR, comment.getAuthor());
    }

    public void testSetString() {
        CommentsTable sheetComments = new CommentsTable();
        CTComment ctComment = CTComment.Factory.newInstance();
        XSSFComment comment = new XSSFComment(sheetComments, ctComment);
        RichTextString richTextString = new HSSFRichTextString(TEST_RICHTEXTSTRING);
        comment.setString(richTextString);
        assertEquals(TEST_RICHTEXTSTRING, ctComment.getText().getT());
    }

    /**
     * Tests that we can add comments to a new
     *  file, save, load, and still see them
     * @throws Exception
     */
    public void testCreateSave() {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet s1 = wb.createSheet();
        Row r1 = s1.createRow(0);
        Cell r1c1 = r1.createCell(0);
        r1c1.setCellValue(2.2);

        assertEquals(0, s1.getNumberOfComments());

        Comment c1 = s1.createComment();
        c1.setAuthor("Author 1");
        c1.setString(new XSSFRichTextString("Comment 1"));
        r1c1.setCellComment(c1);

        assertEquals(1, s1.getNumberOfComments());

        // Save and re-load
        wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        s1 = wb.getSheetAt(0);

        assertEquals(1, s1.getNumberOfComments());
        assertNotNull(s1.getRow(0).getCell(0).getCellComment());
        assertEquals("Author 1", s1.getRow(0).getCell(0).getCellComment().getAuthor());
        assertEquals("Comment 1", s1.getRow(0).getCell(0).getCellComment().getString().getString());

        // Now add an orphaned one
        Comment c2 = s1.createComment();
        c2.setAuthor("Author 2");
        c2.setString(new XSSFRichTextString("Second Comment"));
        c2.setRow(0);
        c2.setColumn((short)1);
        assertEquals(2, s1.getNumberOfComments());

        // Save and re-load

        wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        s1 = wb.getSheetAt(0);

        assertEquals(2, s1.getNumberOfComments());
        assertNotNull(s1.getCellComment(0, 0));
        assertNotNull(s1.getCellComment(0, 1));

        assertEquals("Author 1", s1.getCellComment(0, 0).getAuthor());
        assertEquals("Author 2", s1.getCellComment(0, 1).getAuthor());
    }
}
