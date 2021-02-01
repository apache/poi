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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdnRef;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;

class TestXWPFEndnote {

    private XWPFDocument docOut;
    private String p1Text;
    private String p2Text;
    private BigInteger endnoteId;
    private XWPFEndnote endnote;

    @BeforeEach
    void setUp() {
        docOut = new XWPFDocument();
        p1Text = "First paragraph in footnote";
        p2Text = "Second paragraph in footnote";

        // NOTE: XWPFDocument.createEndnote() delegates directly
        //       to XWPFEndnotes.createEndnote() so this tests
        //       both creation of new XWPFEndnotes in document
        //       and XWPFEndnotes.createEndnote();

        // NOTE: Creating the endnote does not automatically
        //       create a first paragraph.
        endnote = docOut.createEndnote();
        endnoteId = endnote.getId();

    }

    @Test
    void testAddParagraphsToFootnote() throws IOException {

        // Add a run to the first paragraph:

        XWPFParagraph p1 = endnote.createParagraph();
        p1.createRun().setText(p1Text);

        // Create a second paragraph:

        XWPFParagraph p = endnote.createParagraph();
        assertNotNull(p, "Paragraph is null");
        p.createRun().setText(p2Text);

        XWPFDocument docIn = XWPFTestDataSamples.writeOutAndReadBack(docOut);

        XWPFEndnote testEndnote = docIn.getEndnoteByID(endnoteId.intValue());
        assertNotNull(testEndnote);

        assertEquals(2, testEndnote.getParagraphs().size());
        XWPFParagraph testP1 = testEndnote.getParagraphs().get(0);
        assertEquals(p1Text, testP1.getText());

        XWPFParagraph testP2 = testEndnote.getParagraphs().get(1);
        assertEquals(p2Text, testP2.getText());

        // The first paragraph added using createParagraph() should
        // have the required footnote reference added to the first
        // run.

        // Verify that we have a footnote reference in the first paragraph and not
        // in the second paragraph.

        XWPFRun r1 = testP1.getRuns().get(0);
        assertNotNull(r1);
        assertTrue(r1.getCTR().getEndnoteRefList().size() > 0, "No endnote reference in testP1");
        assertNotNull(r1.getCTR().getEndnoteRefArray(0), "No endnote reference in testP1");

        XWPFRun r2 = testP2.getRuns().get(0);
        assertNotNull(r2, "Expected a run in testP2");
        assertEquals(0, r2.getCTR().getEndnoteRefList().size(), "Found an endnote reference in testP2");

    }

    @Test
    void testAddTableToFootnote() throws IOException {
        XWPFTable table = endnote.createTable();
        assertNotNull(table);

        XWPFDocument docIn = XWPFTestDataSamples.writeOutAndReadBack(docOut);

        XWPFEndnote testFootnote = docIn.getEndnoteByID(endnoteId.intValue());
        XWPFTable testTable = testFootnote.getTableArray(0);
        assertNotNull(testTable);

        table = endnote.createTable(2, 3);
        assertEquals(2, table.getNumberOfRows());
        assertEquals(3, table.getRow(0).getTableCells().size());

        // If the table is the first body element of the footnote then
        // a paragraph with the footnote reference should have been
        // added automatically.

        assertEquals(3, endnote.getBodyElements().size(), "Expected 3 body elements");
        IBodyElement testP1 = endnote.getBodyElements().get(0);
        assertTrue(testP1 instanceof XWPFParagraph, "Expected a paragraph, got " + testP1.getClass().getSimpleName());
        XWPFRun r1 = ((XWPFParagraph)testP1).getRuns().get(0);
        assertNotNull(r1);
        assertTrue(r1.getCTR().getEndnoteRefList().size() > 0, "No footnote reference in testP1");
        assertNotNull(r1.getCTR().getEndnoteRefArray(0), "No footnote reference in testP1");

    }

    @Test
    void testRemoveEndnote() {
        // NOTE: XWPFDocument.removeEndnote() delegates directly to
        //       XWPFEndnotes.
        docOut.createEndnote();
        assertEquals(2, docOut.getEndnotes().size(), "Expected 2 endnotes");
        assertNotNull(docOut.getEndnotes().get(1), "Didn't get second endnote");
        boolean result = docOut.removeEndnote(0);
        assertTrue(result, "Remove endnote did not return true");
        assertEquals(1, docOut.getEndnotes().size(), "Expected 1 endnote after removal");
    }

    @Test
    void testAddFootnoteRefToParagraph() {
        XWPFParagraph p = docOut.createParagraph();
        List<XWPFRun> runs = p.getRuns();
        assertEquals(0, runs.size(), "Expected no runs in new paragraph");
        p.addFootnoteReference(endnote);
        XWPFRun run = p.getRuns().get(0);
        CTR ctr = run.getCTR();
        assertNotNull(run, "Expected a run");
        List<CTFtnEdnRef> endnoteRefList = ctr.getEndnoteReferenceList();
        assertNotNull(endnoteRefList);
        CTFtnEdnRef ref = endnoteRefList.get(0);
        assertNotNull(ref);
        assertEquals(endnote.getId(), ref.getId(), "Endnote ID and reference ID did not match");
    }
}
