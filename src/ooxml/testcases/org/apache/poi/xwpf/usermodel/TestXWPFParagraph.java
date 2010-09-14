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

import java.math.BigInteger;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTInd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPBdr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTextAlignment;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTextAlignment;
import        org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture;
import org.openxmlformats.schemas.drawingml.x2006.picture.PicDocument;
import org.openxmlformats.schemas.drawingml.x2006.picture.impl.PicDocumentImpl;

/**
 * Tests for XWPF Paragraphs
 */
public final class TestXWPFParagraph extends TestCase {

    /**
     * Check that we get the right paragraph from the header
     */
    public void disabled_testHeaderParagraph() {
        XWPFDocument xml = XWPFTestDataSamples.openSampleDocument("ThreeColHead.docx");

        XWPFHeader hdr = xml.getHeaderFooterPolicy().getDefaultHeader();
        assertNotNull(hdr);

       List<XWPFParagraph> ps =  hdr.getParagraphs();
        assertEquals(1, ps.size());
        XWPFParagraph p = ps.get(0);

        assertEquals(5, p.getCTP().getRList().size());
        assertEquals("First header column!\tMid header\tRight header!", p
                .getText());
    }

    /**
     * Check that we get the right paragraphs from the document
     */
    public void disabled_testDocumentParagraph() {
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
    }

    public void testSetGetBorderTop() {
        //new clean instance of paragraph
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        assertEquals(STBorder.NONE.intValue(), p.getBorderTop().getValue());

        CTP ctp = p.getCTP();
        CTPPr ppr = ctp.getPPr()== null? ctp.addNewPPr() : ctp.getPPr();

        //bordi
        CTPBdr bdr = ppr.addNewPBdr();
        CTBorder borderTop = bdr.addNewTop();
        borderTop.setVal(STBorder.DOUBLE);
        bdr.setTop(borderTop);

        assertEquals(Borders.DOUBLE, p.getBorderTop());
        p.setBorderTop(Borders.SINGLE);
        assertEquals(STBorder.SINGLE, borderTop.getVal());
    }

    public void testSetGetAlignment() {
        //new clean instance of paragraph
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        assertEquals(STJc.LEFT.intValue(), p.getAlignment().getValue());

        CTP ctp = p.getCTP();
        CTPPr ppr = ctp.getPPr()== null? ctp.addNewPPr() : ctp.getPPr();

        CTJc align = ppr.addNewJc();
        align.setVal(STJc.CENTER);
        assertEquals(ParagraphAlignment.CENTER, p.getAlignment());

        p.setAlignment(ParagraphAlignment.BOTH);
        assertEquals(STJc.BOTH, ppr.getJc().getVal());
    }


    public void testSetGetSpacing() {
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        CTP ctp = p.getCTP();
        CTPPr ppr = ctp.getPPr()== null? ctp.addNewPPr() : ctp.getPPr();

        assertEquals(-1, p.getSpacingAfter());

        CTSpacing spacing = ppr.addNewSpacing();
        spacing.setAfter(new BigInteger("10"));
        assertEquals(10, p.getSpacingAfter());

        p.setSpacingAfter(100);
        assertEquals(100, spacing.getAfter().intValue());
    }

    public void testSetGetSpacingLineRule() {
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        CTP ctp = p.getCTP();
        CTPPr ppr = ctp.getPPr()== null? ctp.addNewPPr() : ctp.getPPr();

        assertEquals(STLineSpacingRule.INT_AUTO, p.getSpacingLineRule().getValue());

        CTSpacing spacing = ppr.addNewSpacing();
        spacing.setLineRule(STLineSpacingRule.AT_LEAST);
        assertEquals(LineSpacingRule.AT_LEAST, p.getSpacingLineRule());

        p.setSpacingAfter(100);
        assertEquals(100, spacing.getAfter().intValue());
    }

    public void testSetGetIndentation() {
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        assertEquals(-1, p.getIndentationLeft());

        CTP ctp = p.getCTP();
        CTPPr ppr = ctp.getPPr()== null? ctp.addNewPPr() : ctp.getPPr();

        assertEquals(-1, p.getIndentationLeft());

        CTInd ind = ppr.addNewInd();
        ind.setLeft(new BigInteger("10"));
        assertEquals(10, p.getIndentationLeft());

        p.setIndentationLeft(100);
        assertEquals(100, ind.getLeft().intValue());
    }

    public void testSetGetVerticalAlignment() {
        //new clean instance of paragraph
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        CTP ctp = p.getCTP();
        CTPPr ppr = ctp.getPPr()== null? ctp.addNewPPr() : ctp.getPPr();

        CTTextAlignment txtAlign = ppr.addNewTextAlignment();
        txtAlign.setVal(STTextAlignment.CENTER);
        assertEquals(TextAlignment.CENTER, p.getVerticalAlignment());

        p.setVerticalAlignment(TextAlignment.BOTTOM);
        assertEquals(STTextAlignment.BOTTOM, ppr.getTextAlignment().getVal());
    }

    public void testSetGetWordWrap() {
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        CTP ctp = p.getCTP();
        CTPPr ppr = ctp.getPPr()== null? ctp.addNewPPr() : ctp.getPPr();

        CTOnOff wordWrap = ppr.addNewWordWrap();
        wordWrap.setVal(STOnOff.FALSE);
        assertEquals(false, p.isWordWrap());

        p.setWordWrap(true);
        assertEquals(STOnOff.TRUE, ppr.getWordWrap().getVal());
    }


    public void testSetGetPageBreak() {
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        CTP ctp = p.getCTP();
        CTPPr ppr = ctp.getPPr()== null? ctp.addNewPPr() : ctp.getPPr();

        CTOnOff pageBreak = ppr.addNewPageBreakBefore();
        pageBreak.setVal(STOnOff.FALSE);
        assertEquals(false, p.isPageBreak());

        p.setPageBreak(true);
        assertEquals(STOnOff.TRUE, ppr.getPageBreakBefore().getVal());
    }

    public void testBookmarks() {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("bookmarks.docx");
        XWPFParagraph paragraph = doc.getParagraphs().get(0);
        assertEquals("Sample Word Document", paragraph.getText());
        assertEquals(1, paragraph.getCTP().sizeOfBookmarkStartArray());
        assertEquals(0, paragraph.getCTP().sizeOfBookmarkEndArray());
        CTBookmark ctBookmark = paragraph.getCTP().getBookmarkStartArray(0);
        assertEquals("poi", ctBookmark.getName());
        for(CTBookmark bookmark : paragraph.getCTP().getBookmarkStartList()) {
           assertEquals("poi", bookmark.getName());
        }
    }

    public void testGetSetNumID() {
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();

        p.setNumID(new BigInteger("10"));
        assertEquals("10", p.getNumID().toString());
    }
    
    public void testPictures() throws Exception {
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
    }
}
