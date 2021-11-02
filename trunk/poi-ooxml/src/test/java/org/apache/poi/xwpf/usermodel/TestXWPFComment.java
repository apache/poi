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
package org.apache.poi.xwpf.usermodel;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

class TestXWPFComment {

    @Test
    void testText() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("comment.docx")) {
            assertEquals(1, doc.getComments().length);
            XWPFComment comment = doc.getComments()[0];
            assertEquals("Unbekannter Autor", comment.getAuthor());
            assertEquals("0", comment.getId());
            assertEquals("This is the first line\n\nThis is the second line", comment.getText());
        }
    }

    @Test
    public void testAddComment() throws IOException {
        BigInteger cId = BigInteger.valueOf(0);
        Calendar date = LocaleUtil.getLocaleCalendar();
        try (XWPFDocument docOut = new XWPFDocument()) {
            assertNull(docOut.getDocComments());

            XWPFComments comments = docOut.createComments();
            XWPFComment comment = comments.createComment(cId);
            comment.setAuthor("Author");
            comment.setInitials("s");
            comment.setDate(date);

            XWPFDocument docIn = XWPFTestDataSamples.writeOutAndReadBack(docOut);
            assertEquals(1, docIn.getComments().length);
            comment = docIn.getCommentByID(cId.toString());
            assertNotNull(comment);
            assertEquals("Author", comment.getAuthor());
            assertEquals("s", comment.getInitials());
            assertEquals(date.getTimeInMillis(), comment.getDate().getTimeInMillis());
        }
    }

    @Test
    void testRemoveComment() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("comment.docx")) {
            assertEquals(1, doc.getComments().length);

            doc.getDocComments().removeComment(0);

            XWPFDocument docIn = XWPFTestDataSamples.writeOutAndReadBack(doc);
            assertEquals(0, docIn.getComments().length);
        }
    }

    @Test
    void testCreateParagraph() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFComments comments = doc.createComments();
            XWPFComment comment = comments.createComment(BigInteger.ONE);
            XWPFParagraph paragraph = comment.createParagraph();
            paragraph.createRun().setText("comment paragraph text");

            XWPFDocument docIn = XWPFTestDataSamples.writeOutAndReadBack(doc);
            XWPFComment xwpfComment = docIn.getCommentByID("1");
            assertEquals(1, xwpfComment.getParagraphs().size());
            String text = xwpfComment.getParagraphArray(0).getText();
            assertEquals("comment paragraph text", text);
        }
    }

    @Test
    void testAddPicture() throws IOException, InvalidFormatException {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFComments comments = doc.createComments();
            XWPFComment comment = comments.createComment(BigInteger.ONE);
            XWPFParagraph paragraph = comment.createParagraph();
            XWPFRun r = paragraph.createRun();
            r.addPicture(new ByteArrayInputStream(new byte[0]),
                    Document.PICTURE_TYPE_JPEG, "test.jpg", 21, 32);

            assertEquals(1, comments.getAllPictures().size());
            assertEquals(1, doc.getAllPackagePictures().size());
        }
    }

    @Test
    void testCreateTable() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFComments comments = doc.createComments();
            XWPFComment comment = comments.createComment(BigInteger.ONE);
            comment.createTable(1, 1);

            XWPFDocument docIn = XWPFTestDataSamples.writeOutAndReadBack(doc);
            XWPFComment xwpfComment = docIn.getCommentByID("1");
            assertEquals(1, xwpfComment.getTables().size());
        }
    }
}
