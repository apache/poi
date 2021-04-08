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

package org.apache.poi.xwpf.model;

import java.io.IOException;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHyperlinkRun;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the various XWPF decorators
 */
class TestXWPFDecorators {
    private XWPFDocument simple;
    private XWPFDocument hyperlink;
    private XWPFDocument comments;

    @BeforeEach
    void setUp() throws IOException {
        simple = XWPFTestDataSamples.openSampleDocument("SampleDoc.docx");
        hyperlink = XWPFTestDataSamples.openSampleDocument("TestDocument.docx");
        comments = XWPFTestDataSamples.openSampleDocument("WordWithAttachments.docx");
    }

    @AfterEach
    void tearDown() throws IOException {
        simple.close();
        hyperlink.close();
        comments.close();
    }

    @Test
    void testHyperlink() {
        XWPFParagraph ps;
        XWPFParagraph ph;
        assertEquals(7, simple.getParagraphs().size());
        assertEquals(5, hyperlink.getParagraphs().size());

        // Simple text
        ps = simple.getParagraphs().get(0);
        assertEquals("I am a test document", ps.getParagraphText());
        assertEquals(1, ps.getRuns().size());

        ph = hyperlink.getParagraphs().get(4);
        assertEquals("We have a hyperlink here, and another.", ph.getParagraphText());
        assertEquals(3, ph.getRuns().size());


        // The proper way to do hyperlinks(!)
        assertFalse(ps.getRuns().get(0) instanceof XWPFHyperlinkRun);
        assertFalse(ph.getRuns().get(0) instanceof XWPFHyperlinkRun);
        assertTrue(ph.getRuns().get(1) instanceof XWPFHyperlinkRun);
        assertFalse(ph.getRuns().get(2) instanceof XWPFHyperlinkRun);

        XWPFHyperlinkRun link = (XWPFHyperlinkRun) ph.getRuns().get(1);
        assertEquals("http://poi.apache.org/", link.getHyperlink(hyperlink).getURL());
    }

    @Test
    void testComments() {
        int numComments = 0;
        for (XWPFParagraph p : comments.getParagraphs()) {
            XWPFCommentsDecorator d = new XWPFCommentsDecorator(p, null);
            if (d.getCommentText().length() > 0) {
                numComments++;
                assertEquals("\tComment by", d.getCommentText().substring(0, 11));
            }
        }
        assertEquals(3, numComments);
    }
}
