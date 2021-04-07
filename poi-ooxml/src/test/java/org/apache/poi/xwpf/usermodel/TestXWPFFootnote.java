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

class TestXWPFFootnote {

    private XWPFDocument docOut;
    private String p1Text;
    private String p2Text;
    private BigInteger footnoteId;
    private XWPFFootnote footnote;

    @BeforeEach
    void setUp() {
        docOut = new XWPFDocument();
        p1Text = "First paragraph in footnote";
        p2Text = "Second paragraph in footnote";

        // NOTE: XWPFDocument.createFootnote() delegates directly
        //       to XWPFFootnotes.createFootnote() so this tests
        //       both creation of new XWPFFootnotes in document
        //       and XWPFFootnotes.createFootnote();

        // NOTE: Creating the footnote does not automatically
        //       create a first paragraph.
        footnote = docOut.createFootnote();
        footnoteId = footnote.getId();

    }

    @Test
    void testAddParagraphsToFootnote() throws IOException {

        // Add a run to the first paragraph:

        XWPFParagraph p1 = footnote.createParagraph();
        p1.createRun().setText(p1Text);

        // Create a second paragraph:

        XWPFParagraph p = footnote.createParagraph();
        assertNotNull(p, "Paragraph is null");
        p.createRun().setText(p2Text);

        XWPFDocument docIn = XWPFTestDataSamples.writeOutAndReadBack(docOut);

        XWPFFootnote testFootnote = docIn.getFootnoteByID(footnoteId.intValue());
        assertNotNull(testFootnote);

        assertEquals(2, testFootnote.getParagraphs().size());
        XWPFParagraph testP1 = testFootnote.getParagraphs().get(0);
        assertEquals(p1Text, testP1.getText());

        XWPFParagraph testP2 = testFootnote.getParagraphs().get(1);
        assertEquals(p2Text, testP2.getText());

        // The first paragraph added using createParagraph() should
        // have the required footnote reference added to the first
        // run.

        // Verify that we have a footnote reference in the first paragraph and not
        // in the second paragraph.

        XWPFRun r1 = testP1.getRuns().get(0);
        assertNotNull(r1);
        assertTrue(r1.getCTR().getFootnoteRefList().size() > 0, "No footnote reference in testP1");
        assertNotNull(r1.getCTR().getFootnoteRefArray(0), "No footnote reference in testP1");

        XWPFRun r2 = testP2.getRuns().get(0);
        assertNotNull(r2, "Expected a run in testP2");
        assertEquals(0, r2.getCTR().getFootnoteRefList().size(), "Found a footnote reference in testP2");

    }

    @Test
    void testAddTableToFootnote() throws IOException {
        XWPFTable table = footnote.createTable();
        assertNotNull(table);

        XWPFDocument docIn = XWPFTestDataSamples.writeOutAndReadBack(docOut);

        XWPFFootnote testFootnote = docIn.getFootnoteByID(footnoteId.intValue());
        XWPFTable testTable = testFootnote.getTableArray(0);
        assertNotNull(testTable);

        table = footnote.createTable(2, 3);
        assertEquals(2, table.getNumberOfRows());
        assertEquals(3, table.getRow(0).getTableCells().size());

        // If the table is the first body element of the footnote then
        // a paragraph with the footnote reference should have been
        // added automatically.

        assertEquals(3, footnote.getBodyElements().size(), "Expected 3 body elements");
        IBodyElement testP1 = footnote.getBodyElements().get(0);
        assertTrue(testP1 instanceof XWPFParagraph, "Expected a paragraph, got " + testP1.getClass().getSimpleName());
        XWPFRun r1 = ((XWPFParagraph)testP1).getRuns().get(0);
        assertNotNull(r1);
        assertTrue(r1.getCTR().getFootnoteRefList().size() > 0, "No footnote reference in testP1");
        assertNotNull(r1.getCTR().getFootnoteRefArray(0), "No footnote reference in testP1");
    }

    @Test
    void testRemoveFootnote() {
        // NOTE: XWPFDocument.removeFootnote() delegates directly to
        //       XWPFFootnotes.
        docOut.createFootnote();
        assertEquals(2, docOut.getFootnotes().size(), "Expected 2 footnotes");
        assertNotNull(docOut.getFootnotes().get(1), "Didn't get second footnote");
        boolean result = docOut.removeFootnote(0);
        assertTrue(result, "Remove footnote did not return true");
        assertEquals(1, docOut.getFootnotes().size(), "Expected 1 footnote after removal");
    }

    @Test
    void testAddFootnoteRefToParagraph() {
        XWPFParagraph p = docOut.createParagraph();
        List<XWPFRun> runs = p.getRuns();
        assertEquals(0, runs.size(), "Expected no runs in new paragraph");
        p.addFootnoteReference(footnote);
        XWPFRun run = p.getRuns().get(0);
        CTR ctr = run.getCTR();
        assertNotNull(run, "Expected a run");
        CTFtnEdnRef ref = ctr.getFootnoteReferenceList().get(0);
        assertNotNull(ref);
        // FIXME: Verify that the footnote reference is w:endnoteReference, not w:footnoteReference
        assertEquals(footnote.getId(), ref.getId(), "Footnote ID and reference ID did not match");



    }

}
