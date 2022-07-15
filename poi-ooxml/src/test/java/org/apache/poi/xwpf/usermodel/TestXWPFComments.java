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

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestXWPFComments {

    @Test
    void testAddCommentsToDoc() throws IOException {
        BigInteger cId = BigInteger.ZERO;
        try (XWPFDocument docOut = new XWPFDocument()) {
            assertNull(docOut.getDocComments());

            // create comments
            XWPFComments comments = docOut.createComments();
            assertNotNull(comments);
            assertSame(comments, docOut.createComments());

            // create comment
            XWPFComment comment = comments.createComment(cId);
            comment.setAuthor("Author");
            comment.createParagraph().createRun().setText("comment paragraph");

            // apply comment to run text
            XWPFParagraph paragraph = docOut.createParagraph();
            paragraph.getCTP().addNewCommentRangeStart().setId(cId);
            paragraph.getCTP().addNewR().addNewT().setStringValue("HelloWorld");
            paragraph.getCTP().addNewCommentRangeEnd().setId(cId);
            paragraph.getCTP().addNewR().addNewCommentReference().setId(cId);

            // check
            XWPFDocument docIn = XWPFTestDataSamples.writeOutAndReadBack(docOut);
            assertNotNull(docIn.getDocComments());
            assertEquals(1, docIn.getComments().length);
            comment = docIn.getCommentByID("0");
            assertNotNull(comment);
            assertEquals("Author", comment.getAuthor());
        }
    }

    @Test
    void testReadComments() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("testComment.docx")) {
            XWPFComments docComments = doc.getDocComments();
            assertNotNull(docComments);
            XWPFComment[] comments = doc.getComments();
            assertEquals(1, comments.length);

            List<XWPFPictureData> allPictures = docComments.getAllPictures();
            assertEquals(1, allPictures.size());
        }
    }

}
