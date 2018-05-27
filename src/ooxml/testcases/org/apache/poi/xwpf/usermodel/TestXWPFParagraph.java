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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.junit.Test;
import org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture;
import org.openxmlformats.schemas.drawingml.x2006.picture.PicDocument;
import org.openxmlformats.schemas.drawingml.x2006.picture.impl.PicDocumentImpl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTInd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPBdr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTextAlignment;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTextAlignment;

/**
 * Tests for XWPF Paragraphs
 */
public final class TestXWPFParagraph {

    /**
     * Check that we get the right paragraph from the header
     *
     * @throws IOException
     */
    @Test
    public void testHeaderParagraph() throws IOException {
        XWPFDocument xml = XWPFTestDataSamples.openSampleDocument("ThreeColHead.docx");

        XWPFHeader hdr = xml.getHeaderFooterPolicy().getDefaultHeader();
        assertNotNull(hdr);

        List<XWPFParagraph> ps = hdr.getParagraphs();
        assertEquals(1, ps.size());
        XWPFParagraph p = ps.get(0);

        assertEquals(5, p.getCTP().sizeOfRArray());
        assertEquals("First header column!\tMid header\tRight header!", p.getText());
        
        xml.close();
    }

    /**
     * Check that we get the right paragraphs from the document
     *
     * @throws IOException
     */
    @Test
    public void testDocumentParagraph() throws IOException {
        XWPFDocument xml = XWPFTestDataSamples.openSampleDocument("ThreeColHead.docx");
        List<XWPFParagraph> ps = xml.getParagraphs();
        assertEquals(10, ps.size());

        assertFalse(ps.get(0).isEmpty());
        assertEquals(
                "This is a sample word document. It has two pages. It has a three column heading, but no footer.",
                ps.get(0).getText());

        assertTrue(ps.get(1).isEmpty());
        assertEquals("", ps.get(1).getText());

        assertFalse(ps.get(2).isEmpty());
        assertEquals("HEADING TEXT", ps.get(2).getText());

        assertTrue(ps.get(3).isEmpty());
        assertEquals("", ps.get(3).getText());

        assertFalse(ps.get(4).isEmpty());
        assertEquals("More on page one", ps.get(4).getText());
        
        xml.close();
    }

    @Test
    public void testSetGetBorderTop() throws IOException {
        //new clean instance of paragraph
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        assertEquals(STBorder.NONE.intValue(), p.getBorderTop().getValue());

        CTP ctp = p.getCTP();
        CTPPr ppr = ctp.getPPr() == null ? ctp.addNewPPr() : ctp.getPPr();

        //bordi
        CTPBdr bdr = ppr.addNewPBdr();
        CTBorder borderTop = bdr.addNewTop();
        borderTop.setVal(STBorder.DOUBLE);
        bdr.setTop(borderTop);

        assertEquals(Borders.DOUBLE, p.getBorderTop());
        p.setBorderTop(Borders.SINGLE);
        assertEquals(STBorder.SINGLE, borderTop.getVal());
        
        doc.close();
    }

    @Test
    public void testSetGetAlignment() throws IOException {
        //new clean instance of paragraph
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        assertEquals(STJc.LEFT.intValue(), p.getAlignment().getValue());

        CTP ctp = p.getCTP();
        CTPPr ppr = ctp.getPPr() == null ? ctp.addNewPPr() : ctp.getPPr();

        CTJc align = ppr.addNewJc();
        align.setVal(STJc.CENTER);
        assertEquals(ParagraphAlignment.CENTER, p.getAlignment());

        p.setAlignment(ParagraphAlignment.BOTH);
        assertEquals(STJc.BOTH, ppr.getJc().getVal());
        
        doc.close();
    }

    @Test
    public void testSetGetSpacing() throws IOException {
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        CTP ctp = p.getCTP();
        CTPPr ppr = ctp.getPPr() == null ? ctp.addNewPPr() : ctp.getPPr();

        assertEquals(-1, p.getSpacingBefore());
        assertEquals(-1, p.getSpacingAfter());
        assertEquals(-1, p.getSpacingBetween(), 0.1);
        assertEquals(LineSpacingRule.AUTO, p.getSpacingLineRule());

        CTSpacing spacing = ppr.addNewSpacing();
        spacing.setAfter(new BigInteger("10"));
        assertEquals(10, p.getSpacingAfter());
        spacing.setBefore(new BigInteger("10"));
        assertEquals(10, p.getSpacingBefore());

        p.setSpacingAfter(100);
        assertEquals(100, spacing.getAfter().intValue());
        p.setSpacingBefore(100);
        assertEquals(100, spacing.getBefore().intValue());
        
        p.setSpacingBetween(.25, LineSpacingRule.EXACT);
        assertEquals(.25, p.getSpacingBetween(), 0.01);
        assertEquals(LineSpacingRule.EXACT, p.getSpacingLineRule());
        p.setSpacingBetween(1.25, LineSpacingRule.AUTO);
        assertEquals(1.25, p.getSpacingBetween(), 0.01);
        assertEquals(LineSpacingRule.AUTO, p.getSpacingLineRule());
        p.setSpacingBetween(.5, LineSpacingRule.AT_LEAST);
        assertEquals(.5, p.getSpacingBetween(), 0.01);
        assertEquals(LineSpacingRule.AT_LEAST, p.getSpacingLineRule());
        p.setSpacingBetween(1.15);
        assertEquals(1.15, p.getSpacingBetween(), 0.01);
        assertEquals(LineSpacingRule.AUTO, p.getSpacingLineRule());
        
        doc.close();
    }

    @Test
    public void testSetGetSpacingLineRule() throws IOException {
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        CTP ctp = p.getCTP();
        CTPPr ppr = ctp.getPPr() == null ? ctp.addNewPPr() : ctp.getPPr();

        assertEquals(STLineSpacingRule.INT_AUTO, p.getSpacingLineRule().getValue());

        CTSpacing spacing = ppr.addNewSpacing();
        spacing.setLineRule(STLineSpacingRule.AT_LEAST);
        assertEquals(LineSpacingRule.AT_LEAST, p.getSpacingLineRule());

        p.setSpacingAfter(100);
        assertEquals(100, spacing.getAfter().intValue());
        
        doc.close();
    }

    @Test
    public void testSetGetIndentation() throws IOException {
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        assertEquals(-1, p.getIndentationLeft());

        CTP ctp = p.getCTP();
        CTPPr ppr = ctp.getPPr() == null ? ctp.addNewPPr() : ctp.getPPr();

        assertEquals(-1, p.getIndentationLeft());

        CTInd ind = ppr.addNewInd();
        ind.setLeft(new BigInteger("10"));
        assertEquals(10, p.getIndentationLeft());

        p.setIndentationLeft(100);
        assertEquals(100, ind.getLeft().intValue());
        
        doc.close();
    }

    @Test
    public void testSetGetVerticalAlignment() throws IOException {
        //new clean instance of paragraph
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        CTP ctp = p.getCTP();
        CTPPr ppr = ctp.getPPr() == null ? ctp.addNewPPr() : ctp.getPPr();

        CTTextAlignment txtAlign = ppr.addNewTextAlignment();
        txtAlign.setVal(STTextAlignment.CENTER);
        assertEquals(TextAlignment.CENTER, p.getVerticalAlignment());

        p.setVerticalAlignment(TextAlignment.BOTTOM);
        assertEquals(STTextAlignment.BOTTOM, ppr.getTextAlignment().getVal());
        
        doc.close();
    }

    @Test
    public void testSetGetWordWrap() throws IOException {
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        CTP ctp = p.getCTP();
        CTPPr ppr = ctp.getPPr() == null ? ctp.addNewPPr() : ctp.getPPr();

        CTOnOff wordWrap = ppr.addNewWordWrap();
        wordWrap.setVal(STOnOff.FALSE);
        assertEquals(false, p.isWordWrap());

        p.setWordWrapped(true);
        assertEquals(STOnOff.TRUE, ppr.getWordWrap().getVal());
        
        doc.close();
    }

    @Test
    public void testSetGetPageBreak() throws IOException {
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        CTP ctp = p.getCTP();
        CTPPr ppr = ctp.getPPr() == null ? ctp.addNewPPr() : ctp.getPPr();

        CTOnOff pageBreak = ppr.addNewPageBreakBefore();
        pageBreak.setVal(STOnOff.FALSE);
        assertEquals(false, p.isPageBreak());

        p.setPageBreak(true);
        assertEquals(STOnOff.TRUE, ppr.getPageBreakBefore().getVal());
        doc.close();
    }

    @Test
    public void testBookmarks() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("bookmarks.docx");
        XWPFParagraph paragraph = doc.getParagraphs().get(0);
        assertEquals("Sample Word Document", paragraph.getText());
        assertEquals(1, paragraph.getCTP().sizeOfBookmarkStartArray());
        assertEquals(0, paragraph.getCTP().sizeOfBookmarkEndArray());
        CTBookmark ctBookmark = paragraph.getCTP().getBookmarkStartArray(0);
        assertEquals("poi", ctBookmark.getName());
        for (CTBookmark bookmark : paragraph.getCTP().getBookmarkStartList()) {
            assertEquals("poi", bookmark.getName());
        }
        doc.close();
    }

    @Test
    public void testGetSetNumID() throws IOException {
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        p.setNumID(new BigInteger("10"));
        assertEquals("10", p.getNumID().toString());
        doc.close();
    }

    @Test
    public void testAddingRuns() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("sample.docx");

        XWPFParagraph p = doc.getParagraphs().get(0);
        assertEquals(2, p.getRuns().size());

        XWPFRun r = p.createRun();
        assertEquals(3, p.getRuns().size());
        assertEquals(2, p.getRuns().indexOf(r));

        XWPFRun r2 = p.insertNewRun(1);
        assertEquals(4, p.getRuns().size());
        assertEquals(1, p.getRuns().indexOf(r2));
        assertEquals(3, p.getRuns().indexOf(r));
        
        doc.close();
    }

    @Test
    public void testPictures() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("VariousPictures.docx");
        assertEquals(7, doc.getParagraphs().size());

        XWPFParagraph p;
        XWPFRun r;

        // Text paragraphs
        assertEquals("Sheet with various pictures", doc.getParagraphs().get(0).getText());
        assertEquals("(jpeg, png, wmf, emf and pict) ", doc.getParagraphs().get(1).getText());

        // Spacer ones
        assertEquals("", doc.getParagraphs().get(2).getText());
        assertEquals("", doc.getParagraphs().get(3).getText());
        assertEquals("", doc.getParagraphs().get(4).getText());

        // Image one
        p = doc.getParagraphs().get(5);
        assertEquals(6, p.getRuns().size());

        r = p.getRuns().get(0);
        assertEquals("", r.toString());
        assertEquals(1, r.getEmbeddedPictures().size());
        assertNotNull(r.getEmbeddedPictures().get(0).getPictureData());
        assertEquals("image1.wmf", r.getEmbeddedPictures().get(0).getPictureData().getFileName());

        r = p.getRuns().get(1);
        assertEquals("", r.toString());
        assertEquals(1, r.getEmbeddedPictures().size());
        assertNotNull(r.getEmbeddedPictures().get(0).getPictureData());
        assertEquals("image2.png", r.getEmbeddedPictures().get(0).getPictureData().getFileName());

        r = p.getRuns().get(2);
        assertEquals("", r.toString());
        assertEquals(1, r.getEmbeddedPictures().size());
        assertNotNull(r.getEmbeddedPictures().get(0).getPictureData());
        assertEquals("image3.emf", r.getEmbeddedPictures().get(0).getPictureData().getFileName());

        r = p.getRuns().get(3);
        assertEquals("", r.toString());
        assertEquals(1, r.getEmbeddedPictures().size());
        assertNotNull(r.getEmbeddedPictures().get(0).getPictureData());
        assertEquals("image4.emf", r.getEmbeddedPictures().get(0).getPictureData().getFileName());

        r = p.getRuns().get(4);
        assertEquals("", r.toString());
        assertEquals(1, r.getEmbeddedPictures().size());
        assertNotNull(r.getEmbeddedPictures().get(0).getPictureData());
        assertEquals("image5.jpeg", r.getEmbeddedPictures().get(0).getPictureData().getFileName());

        r = p.getRuns().get(5);
        assertEquals(" ", r.toString());
        assertEquals(0, r.getEmbeddedPictures().size());

        // Final spacer
        assertEquals("", doc.getParagraphs().get(6).getText());


        // Look in detail at one
        r = p.getRuns().get(4);
        XWPFPicture pict = r.getEmbeddedPictures().get(0);
        CTPicture picture = pict.getCTPicture();
        assertEquals("rId8", picture.getBlipFill().getBlip().getEmbed());

        // Ensure that the ooxml compiler finds everything we need
        r.getCTR().getDrawingArray(0);
        r.getCTR().getDrawingArray(0).getInlineArray(0);
        r.getCTR().getDrawingArray(0).getInlineArray(0).getGraphic();
        r.getCTR().getDrawingArray(0).getInlineArray(0).getGraphic().getGraphicData();
        PicDocument pd = new PicDocumentImpl(null);
        assertTrue(pd.isNil());
        
        doc.close();
    }

    @Test
    public void testTika792() throws Exception {
        //This test forces the loading of CTMoveBookmark and
        //CTMoveBookmarkImpl into ooxml-lite.
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Tika-792.docx");
        XWPFParagraph paragraph = doc.getParagraphs().get(0);
        assertEquals("", paragraph.getText());
        paragraph = doc.getParagraphs().get(1);
        assertEquals("b", paragraph.getText());
        doc.close();
    }

    @Test
    public void testSettersGetters() throws IOException {
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        assertTrue(p.isEmpty());
        assertFalse(p.removeRun(0));

        p.setBorderTop(Borders.BABY_PACIFIER);
        p.setBorderBetween(Borders.BABY_PACIFIER);
        p.setBorderBottom(Borders.BABY_RATTLE);

        assertNotNull(p.getIRuns());
        assertEquals(0, p.getIRuns().size());
        assertFalse(p.isEmpty());
        assertNull(p.getStyleID());
        assertNull(p.getStyle());

        assertNull(p.getNumID());
        p.setNumID(BigInteger.valueOf(12));
        assertEquals(BigInteger.valueOf(12), p.getNumID());
        p.setNumID(BigInteger.valueOf(13));
        assertEquals(BigInteger.valueOf(13), p.getNumID());

        assertNull(p.getNumFmt());

        assertNull(p.getNumIlvl());

        assertEquals("", p.getParagraphText());
        assertEquals("", p.getPictureText());
        assertEquals("", p.getFootnoteText());

        p.setBorderBetween(Borders.NONE);
        assertEquals(Borders.NONE, p.getBorderBetween());
        p.setBorderBetween(Borders.BASIC_BLACK_DASHES);
        assertEquals(Borders.BASIC_BLACK_DASHES, p.getBorderBetween());

        p.setBorderBottom(Borders.NONE);
        assertEquals(Borders.NONE, p.getBorderBottom());
        p.setBorderBottom(Borders.BABY_RATTLE);
        assertEquals(Borders.BABY_RATTLE, p.getBorderBottom());

        p.setBorderLeft(Borders.NONE);
        assertEquals(Borders.NONE, p.getBorderLeft());
        p.setBorderLeft(Borders.BASIC_WHITE_SQUARES);
        assertEquals(Borders.BASIC_WHITE_SQUARES, p.getBorderLeft());

        p.setBorderRight(Borders.NONE);
        assertEquals(Borders.NONE, p.getBorderRight());
        p.setBorderRight(Borders.BASIC_WHITE_DASHES);
        assertEquals(Borders.BASIC_WHITE_DASHES, p.getBorderRight());

        p.setBorderBottom(Borders.NONE);
        assertEquals(Borders.NONE, p.getBorderBottom());
        p.setBorderBottom(Borders.BASIC_WHITE_DOTS);
        assertEquals(Borders.BASIC_WHITE_DOTS, p.getBorderBottom());

        assertFalse(p.isPageBreak());
        p.setPageBreak(true);
        assertTrue(p.isPageBreak());
        p.setPageBreak(false);
        assertFalse(p.isPageBreak());

        assertEquals(-1, p.getSpacingAfter());
        p.setSpacingAfter(12);
        assertEquals(12, p.getSpacingAfter());

        assertEquals(-1, p.getSpacingAfterLines());
        p.setSpacingAfterLines(14);
        assertEquals(14, p.getSpacingAfterLines());

        assertEquals(-1, p.getSpacingBefore());
        p.setSpacingBefore(16);
        assertEquals(16, p.getSpacingBefore());

        assertEquals(-1, p.getSpacingBeforeLines());
        p.setSpacingBeforeLines(18);
        assertEquals(18, p.getSpacingBeforeLines());

        assertEquals(LineSpacingRule.AUTO, p.getSpacingLineRule());
        p.setSpacingLineRule(LineSpacingRule.EXACT);
        assertEquals(LineSpacingRule.EXACT, p.getSpacingLineRule());

        assertEquals(-1, p.getIndentationLeft());
        p.setIndentationLeft(21);
        assertEquals(21, p.getIndentationLeft());

        assertEquals(-1, p.getIndentationRight());
        p.setIndentationRight(25);
        assertEquals(25, p.getIndentationRight());

        assertEquals(-1, p.getIndentationHanging());
        p.setIndentationHanging(25);
        assertEquals(25, p.getIndentationHanging());

        assertEquals(-1, p.getIndentationFirstLine());
        p.setIndentationFirstLine(25);
        assertEquals(25, p.getIndentationFirstLine());

        assertFalse(p.isWordWrap());
        p.setWordWrapped(true);
        assertTrue(p.isWordWrap());
        p.setWordWrapped(false);
        assertFalse(p.isWordWrap());

        assertNull(p.getStyle());
        p.setStyle("teststyle");
        assertEquals("teststyle", p.getStyle());

        p.addRun(CTR.Factory.newInstance());

        //assertTrue(p.removeRun(0));

        assertNotNull(p.getBody());
        assertEquals(BodyElementType.PARAGRAPH, p.getElementType());
        assertEquals(BodyType.DOCUMENT, p.getPartType());
        
        doc.close();
    }

    @Test
    public void testSearchTextNotFound() throws IOException {
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        assertNull(p.searchText("test", new PositionInParagraph()));
        assertEquals("", p.getText());
        doc.close();
    }

    @Test
    public void testSearchTextFound() throws IOException {
        XWPFDocument xml = XWPFTestDataSamples.openSampleDocument("ThreeColHead.docx");

        List<XWPFParagraph> ps = xml.getParagraphs();
        assertEquals(10, ps.size());

        XWPFParagraph p = ps.get(0);

        TextSegment segment = p.searchText("sample word document", new PositionInParagraph());
        assertNotNull(segment);

        assertEquals("sample word document", p.getText(segment));

        assertTrue(p.removeRun(0));
        xml.close();
    }
    
    @Test
    public void testFieldRuns() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("FldSimple.docx");
        List<XWPFParagraph> ps = doc.getParagraphs();
        assertEquals(1, ps.size());
        
        XWPFParagraph p = ps.get(0);
        assertEquals(1, p.getRuns().size());
        assertEquals(1, p.getIRuns().size());
        
        XWPFRun r = p.getRuns().get(0);
        assertEquals(XWPFFieldRun.class, r.getClass());
        
        XWPFFieldRun fr = (XWPFFieldRun)r;
        assertEquals(" FILENAME   \\* MERGEFORMAT ", fr.getFieldInstruction());
        assertEquals("FldSimple.docx", fr.text());
        assertEquals("FldSimple.docx", p.getText());
        doc.close();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testRuns() throws IOException {
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        CTR run = CTR.Factory.newInstance();
        XWPFRun r = new XWPFRun(run, doc.createParagraph());
        p.addRun(r);
        p.addRun(r);

        assertNotNull(p.getRun(run));
        assertNull(p.getRun(null));
        doc.close();
    }

    @Test
    public void test58067() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("58067.docx");
        
        StringBuilder str = new StringBuilder();
        for(XWPFParagraph par : doc.getParagraphs()) {
            str.append(par.getText()).append("\n");
        }
        assertEquals("This is a test.\n\n\n\n3\n4\n5\n\n\n\nThis is a whole paragraph where one word is deleted.\n", str.toString());
    }

    @Test
    public void test61787() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("61787.docx");

        StringBuilder str = new StringBuilder();
        for(XWPFParagraph par : doc.getParagraphs()) {
            str.append(par.getText()).append("\n");
        }
        String s = str.toString();
        assertTrue("Having text: \n" + s + "\nTrimmed lenght: " + s.trim().length(), s.trim().length() > 0);
    }

    /**
     * Tests for numbered lists
     * 
     * See also https://github.com/jimklo/apache-poi-sample/blob/master/src/main/java/com/sri/jklo/StyledDocument.java
     * for someone else trying a similar thing
     */
    @Test
    public void testNumberedLists() throws Exception {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("ComplexNumberedLists.docx");
        XWPFParagraph p;
        
        p = doc.getParagraphArray(0);
        assertEquals("This is a document with numbered lists", p.getText());
        assertEquals(null, p.getNumID());
        assertEquals(null, p.getNumIlvl());
        assertEquals(null, p.getNumStartOverride());
        
        p = doc.getParagraphArray(1);
        assertEquals("Entry #1", p.getText());
        assertEquals(BigInteger.valueOf(1), p.getNumID());
        assertEquals(BigInteger.valueOf(0), p.getNumIlvl());
        assertEquals(null, p.getNumStartOverride());
        
        p = doc.getParagraphArray(2);
        assertEquals("Entry #2, with children", p.getText());
        assertEquals(BigInteger.valueOf(1), p.getNumID());
        assertEquals(BigInteger.valueOf(0), p.getNumIlvl());
        assertEquals(null, p.getNumStartOverride());
        
        p = doc.getParagraphArray(3);
        assertEquals("2-a", p.getText());
        assertEquals(BigInteger.valueOf(1), p.getNumID());
        assertEquals(BigInteger.valueOf(1), p.getNumIlvl());
        assertEquals(null, p.getNumStartOverride());
        
        p = doc.getParagraphArray(4);
        assertEquals("2-b", p.getText());
        assertEquals(BigInteger.valueOf(1), p.getNumID());
        assertEquals(BigInteger.valueOf(1), p.getNumIlvl());
        assertEquals(null, p.getNumStartOverride());
        
        p = doc.getParagraphArray(5);
        assertEquals("2-c", p.getText());
        assertEquals(BigInteger.valueOf(1), p.getNumID());
        assertEquals(BigInteger.valueOf(1), p.getNumIlvl());
        assertEquals(null, p.getNumStartOverride());
        
        p = doc.getParagraphArray(6);
        assertEquals("Entry #3", p.getText());
        assertEquals(BigInteger.valueOf(1), p.getNumID());
        assertEquals(BigInteger.valueOf(0), p.getNumIlvl());
        assertEquals(null, p.getNumStartOverride());
        
        p = doc.getParagraphArray(7);
        assertEquals("Entry #4", p.getText());
        assertEquals(BigInteger.valueOf(1), p.getNumID());
        assertEquals(BigInteger.valueOf(0), p.getNumIlvl());
        assertEquals(null, p.getNumStartOverride());
        
        // New list
        p = doc.getParagraphArray(8);
        assertEquals("Restarted to 1 from 5", p.getText());
        assertEquals(BigInteger.valueOf(2), p.getNumID());
        assertEquals(BigInteger.valueOf(0), p.getNumIlvl());
        assertEquals(null, p.getNumStartOverride());
        
        p = doc.getParagraphArray(9);
        assertEquals("Restarted @ 2", p.getText());
        assertEquals(BigInteger.valueOf(2), p.getNumID());
        assertEquals(BigInteger.valueOf(0), p.getNumIlvl());
        assertEquals(null, p.getNumStartOverride());
        
        p = doc.getParagraphArray(10);
        assertEquals("Restarted @ 3", p.getText());
        assertEquals(BigInteger.valueOf(2), p.getNumID());
        assertEquals(BigInteger.valueOf(0), p.getNumIlvl());
        assertEquals(null, p.getNumStartOverride());
        
        // New list starting at 10
        p = doc.getParagraphArray(11);
        assertEquals("Jump to new list at 10", p.getText());
        assertEquals(BigInteger.valueOf(6), p.getNumID());
        assertEquals(BigInteger.valueOf(0), p.getNumIlvl());
        // TODO Why isn't this seen as 10?
        assertEquals(null, p.getNumStartOverride());
        
        // TODO Shouldn't we use XWPFNumbering or similar here?
        // TODO Make it easier to change
    }
}
