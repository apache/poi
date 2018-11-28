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

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.junit.Before;
import org.junit.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdnRef;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;

public class TestXWPFFootnote {
    
    private XWPFDocument docOut;
    private String p1Text;
    private String p2Text;
    private BigInteger footnoteId;
    private XWPFFootnote footnote;

    @Before
    public void setUp() {
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
    public void testAddParagraphsToFootnote() throws IOException {

        // Add a run to the first paragraph:    
        
        XWPFParagraph p1 = footnote.createParagraph();
        p1.createRun().setText(p1Text);
        
        // Create a second paragraph:
        
        XWPFParagraph p = footnote.createParagraph();
        assertNotNull("Paragraph is null", p);
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
        assertTrue("No footnote reference in testP1", r1.getCTR().getFootnoteRefList().size() > 0);
        assertNotNull("No footnote reference in testP1", r1.getCTR().getFootnoteRefArray(0));

        XWPFRun r2 = testP2.getRuns().get(0);
        assertNotNull("Expected a run in testP2", r2);
        assertTrue("Found a footnote reference in testP2", r2.getCTR().getFootnoteRefList().size() == 0);
        
    }
    
    @Test
    public void testAddTableToFootnote() throws IOException {
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
        
        assertEquals("Expected 3 body elements", 3, footnote.getBodyElements().size());
        IBodyElement testP1 = footnote.getBodyElements().get(0);
        assertTrue("Expected a paragraph, got " + testP1.getClass().getSimpleName() , testP1 instanceof XWPFParagraph);
        XWPFRun r1 = ((XWPFParagraph)testP1).getRuns().get(0);
        assertNotNull(r1);
        assertTrue("No footnote reference in testP1", r1.getCTR().getFootnoteRefList().size() > 0);
        assertNotNull("No footnote reference in testP1", r1.getCTR().getFootnoteRefArray(0));

    }
    
    @Test
    public void testRemoveFootnote() {
        // NOTE: XWPFDocument.removeFootnote() delegates directly to 
        //       XWPFFootnotes.
        docOut.createFootnote();
        assertEquals("Expected 2 footnotes", 2, docOut.getFootnotes().size());
        assertNotNull("Didn't get second footnote", docOut.getFootnotes().get(1));
        boolean result = docOut.removeFootnote(0);
        assertTrue("Remove footnote did not return true", result);
        assertEquals("Expected 1 footnote after removal", 1, docOut.getFootnotes().size());
    }

    @Test
    public void testAddFootnoteRefToParagraph() {
        XWPFParagraph p = docOut.createParagraph();
        List<XWPFRun> runs = p.getRuns();
        assertEquals("Expected no runs in new paragraph", 0, runs.size());
        p.addFootnoteReference(footnote);
        XWPFRun run = p.getRuns().get(0);
        CTR ctr = run.getCTR();
        assertNotNull("Expected a run", run);
        CTFtnEdnRef ref = ctr.getFootnoteReferenceList().get(0);
        assertNotNull(ref);
        // FIXME: Verify that the footnote reference is w:endnoteReference, not w:footnoteReference
        assertEquals("Footnote ID and reference ID did not match", footnote.getId(), ref.getId());
        
        
        
    }

}
