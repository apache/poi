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

public class TestXWPFEndnote {
    
    private XWPFDocument docOut;
    private String p1Text;
    private String p2Text;
    private BigInteger endnoteId;
    private XWPFEndnote endnote;

    @Before
    public void setUp() {
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
    public void testAddParagraphsToFootnote() throws IOException {

        // Add a run to the first paragraph:    
        
        XWPFParagraph p1 = endnote.createParagraph();
        p1.createRun().setText(p1Text);
        
        // Create a second paragraph:
        
        XWPFParagraph p = endnote.createParagraph();
        assertNotNull("Paragraph is null", p);
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
        assertTrue("No endnote reference in testP1", r1.getCTR().getEndnoteRefList().size() > 0);
        assertNotNull("No endnote reference in testP1", r1.getCTR().getEndnoteRefArray(0));

        XWPFRun r2 = testP2.getRuns().get(0);
        assertNotNull("Expected a run in testP2", r2);
        assertTrue("Found an endnote reference in testP2", r2.getCTR().getEndnoteRefList().size() == 0);
        
    }
    
    @Test
    public void testAddTableToFootnote() throws IOException {
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
        
        assertEquals("Expected 3 body elements", 3, endnote.getBodyElements().size());
        IBodyElement testP1 = endnote.getBodyElements().get(0);
        assertTrue("Expected a paragraph, got " + testP1.getClass().getSimpleName() , testP1 instanceof XWPFParagraph);
        XWPFRun r1 = ((XWPFParagraph)testP1).getRuns().get(0);
        assertNotNull(r1);
        assertTrue("No footnote reference in testP1", r1.getCTR().getEndnoteRefList().size() > 0);
        assertNotNull("No footnote reference in testP1", r1.getCTR().getEndnoteRefArray(0));

    }
    
    @Test
    public void testRemoveEndnote() {
        // NOTE: XWPFDocument.removeEndnote() delegates directly to 
        //       XWPFEndnotes.
        docOut.createEndnote();
        assertEquals("Expected 2 endnotes", 2, docOut.getEndnotes().size());
        assertNotNull("Didn't get second endnote", docOut.getEndnotes().get(1));
        boolean result = docOut.removeEndnote(0);
        assertTrue("Remove endnote did not return true", result);
        assertEquals("Expected 1 endnote after removal", 1, docOut.getEndnotes().size());
    }

    @Test
    public void testAddFootnoteRefToParagraph() {
        XWPFParagraph p = docOut.createParagraph();
        List<XWPFRun> runs = p.getRuns();
        assertEquals("Expected no runs in new paragraph", 0, runs.size());
        p.addFootnoteReference(endnote);
        XWPFRun run = p.getRuns().get(0);
        CTR ctr = run.getCTR();
        assertNotNull("Expected a run", run);
        List<CTFtnEdnRef> endnoteRefList = ctr.getEndnoteReferenceList();
        assertNotNull(endnoteRefList);
        CTFtnEdnRef ref = endnoteRefList.get(0);
        assertNotNull(ref);
        assertEquals("Endnote ID and reference ID did not match", endnote.getId(), ref.getId());
        
        
    }

}
