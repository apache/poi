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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture;
import org.openxmlformats.schemas.drawingml.x2006.picture.PicDocument;
import org.openxmlformats.schemas.drawingml.x2006.picture.impl.PicDocumentImpl;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STOnOff1;
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
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTextAlignment;

/**
 * Tests for XWPF Paragraphs
 */
public final class TestXWPFParagraph {

    /**
     * Check that we get the right paragraph from the header
     */
    @Test
    void testHeaderParagraph() throws IOException {
        try (XWPFDocument xml = XWPFTestDataSamples.openSampleDocument("ThreeColHead.docx")) {

            XWPFHeader hdr = xml.getHeaderFooterPolicy().getDefaultHeader();
            assertNotNull(hdr);

            List<XWPFParagraph> ps = hdr.getParagraphs();
            assertEquals(1, ps.size());
            XWPFParagraph p = ps.get(0);

            assertEquals(5, p.getCTP().sizeOfRArray());
            assertEquals("First header column!\tMid header\tRight header!", p.getText());
        }
    }

    /**
     * Check that we get the right paragraphs from the document
     */
    @Test
    void testDocumentParagraph() throws IOException {
        try (XWPFDocument xml = XWPFTestDataSamples.openSampleDocument("ThreeColHead.docx")) {
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
    }

    @Test
    void testSetGetBorderTop() throws IOException {
        //new clean instance of paragraph
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p = doc.createParagraph();

            assertEquals(STBorder.NONE.intValue(), p.getBorderTop().getValue());

            CTP ctp = p.getCTP();
            CTPPr ppr = ctp.getPPr() == null ? ctp.addNewPPr() : ctp.getPPr();

            CTPBdr bdr = ppr.addNewPBdr();
            CTBorder borderTop = bdr.addNewTop();
            borderTop.setVal(STBorder.DOUBLE);
            bdr.setTop(borderTop);

            assertEquals(Borders.DOUBLE, p.getBorderTop());
            p.setBorderTop(Borders.SINGLE);
            assertEquals(STBorder.SINGLE, borderTop.getVal());
        }
    }

    @Test
    void testSetGetAlignment() throws IOException {
        //new clean instance of paragraph
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p = doc.createParagraph();

            assertEquals(STJc.LEFT.intValue(), p.getAlignment().getValue());

            CTP ctp = p.getCTP();
            CTPPr ppr = ctp.getPPr() == null ? ctp.addNewPPr() : ctp.getPPr();

            CTJc align = ppr.addNewJc();
            align.setVal(STJc.CENTER);
            assertEquals(ParagraphAlignment.CENTER, p.getAlignment());

            p.setAlignment(ParagraphAlignment.BOTH);
            assertEquals(STJc.BOTH, ppr.getJc().getVal());
        }
    }

    @Test
    void testSetGetSpacing() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
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
            assertEquals("100", spacing.xgetAfter().getStringValue());
            p.setSpacingBefore(100);
            assertEquals("100", spacing.xgetBefore().getStringValue());

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
        }
    }

    @Test
    void testSetGetSpacingLineRule() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p = doc.createParagraph();

            CTP ctp = p.getCTP();
            CTPPr ppr = ctp.getPPr() == null ? ctp.addNewPPr() : ctp.getPPr();

            assertEquals(STLineSpacingRule.INT_AUTO, p.getSpacingLineRule().getValue());

            CTSpacing spacing = ppr.addNewSpacing();
            spacing.setLineRule(STLineSpacingRule.AT_LEAST);
            assertEquals(LineSpacingRule.AT_LEAST, p.getSpacingLineRule());

            p.setSpacingAfter(100);
            assertEquals("100", spacing.xgetAfter().getStringValue());
        }
    }

    @Test
    void testSetGetIndentationChars() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p = doc.createParagraph();

            assertEquals(-1, p.getIndentationLeftChars());
            assertEquals(-1, p.getIndentationRightChars());
            // set 1.5 characters
            p.setIndentationLeftChars(150);
            assertEquals(150, p.getIndentationLeftChars());

            p.setIndentationRightChars(250);
            assertEquals(250, p.getIndentationRightChars());
        }
    }

    @Test
    void testSetGetIndentation() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p = doc.createParagraph();

            assertEquals(-1, p.getIndentationLeft());

            CTP ctp = p.getCTP();
            CTPPr ppr = ctp.getPPr() == null ? ctp.addNewPPr() : ctp.getPPr();

            assertEquals(-1, p.getIndentationLeft());

            CTInd ind = ppr.addNewInd();
            ind.setLeft(new BigInteger("10"));
            assertEquals(10, p.getIndentationLeft());

            p.setIndentationLeft(100);
            assertEquals("100", ind.xgetLeft().getStringValue());
        }
    }
    @Test
    void testSetGetVerticalAlignment() throws IOException {
        //new clean instance of paragraph
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p = doc.createParagraph();

            CTP ctp = p.getCTP();
            CTPPr ppr = ctp.getPPr() == null ? ctp.addNewPPr() : ctp.getPPr();

            CTTextAlignment txtAlign = ppr.addNewTextAlignment();
            txtAlign.setVal(STTextAlignment.CENTER);
            assertEquals(TextAlignment.CENTER, p.getVerticalAlignment());

            p.setVerticalAlignment(TextAlignment.BOTTOM);
            assertEquals(STTextAlignment.BOTTOM, ppr.getTextAlignment().getVal());
        }
    }

    @Test
    void testSetGetWordWrap() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p = doc.createParagraph();

            CTP ctp = p.getCTP();
            CTPPr ppr = ctp.getPPr() == null ? ctp.addNewPPr() : ctp.getPPr();

            CTOnOff wordWrap = ppr.addNewWordWrap();
            wordWrap.setVal(STOnOff1.OFF);
            assertFalse(p.isWordWrap());

            p.setWordWrapped(true);
            assertEquals("on", ppr.getWordWrap().getVal());
        }
    }

    @Test
    void testSetGetPageBreak() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p = doc.createParagraph();

            CTP ctp = p.getCTP();
            CTPPr ppr = ctp.getPPr() == null ? ctp.addNewPPr() : ctp.getPPr();

            CTOnOff pageBreak = ppr.addNewPageBreakBefore();
            pageBreak.setVal(STOnOff1.OFF);
            assertFalse(p.isPageBreak());

            p.setPageBreak(true);
            assertEquals("on", ppr.getPageBreakBefore().getVal());
        }
    }

    @Test
    void testBookmarks() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("bookmarks.docx")) {
            XWPFParagraph paragraph = doc.getParagraphs().get(0);
            assertEquals("Sample Word Document", paragraph.getText());
            assertEquals(1, paragraph.getCTP().sizeOfBookmarkStartArray());
            assertEquals(0, paragraph.getCTP().sizeOfBookmarkEndArray());
            CTBookmark ctBookmark = paragraph.getCTP().getBookmarkStartArray(0);
            assertEquals("poi", ctBookmark.getName());
            for (CTBookmark bookmark : paragraph.getCTP().getBookmarkStartList()) {
                assertEquals("poi", bookmark.getName());
            }
        }
    }

    @Test
    void testGetSetNumID() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p = doc.createParagraph();

            p.setNumID(new BigInteger("10"));
            assertEquals("10", p.getNumID().toString());
        }
    }

    @Test
    void testGetSetILvl() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p = doc.createParagraph();

            p.setNumILvl(new BigInteger("1"));
            assertEquals("1", p.getNumIlvl().toString());
        }
    }

    @Test
    void testAddingRuns() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("sample.docx")) {

            XWPFParagraph p = doc.getParagraphs().get(0);
            assertEquals(2, p.getRuns().size());

            XWPFRun r = p.createRun();
            assertEquals(3, p.getRuns().size());
            assertEquals(2, p.getRuns().indexOf(r));

            XWPFRun r2 = p.insertNewRun(1);
            assertEquals(4, p.getRuns().size());
            assertEquals(1, p.getRuns().indexOf(r2));
            assertEquals(3, p.getRuns().indexOf(r));
        }
    }

    @Test
    void testCreateNewRuns() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {

            XWPFParagraph p = doc.createParagraph();
            XWPFHyperlinkRun h = p.createHyperlinkRun("http://poi.apache.org");
            XWPFFieldRun fieldRun = p.createFieldRun();
            XWPFRun r = p.createRun();

            assertEquals(3, p.getRuns().size());
            assertEquals(0, p.getRuns().indexOf(h));
            assertEquals(1, p.getRuns().indexOf(fieldRun));
            assertEquals(2, p.getRuns().indexOf(r));

            assertEquals(3, p.getIRuns().size());
            assertEquals(0, p.getIRuns().indexOf(h));
            assertEquals(1, p.getIRuns().indexOf(fieldRun));
            assertEquals(2, p.getIRuns().indexOf(r));
        }
    }

    @Test
    void testInsertNewRuns() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {

            XWPFParagraph p = doc.createParagraph();
            XWPFRun r = p.createRun();
            assertEquals(1, p.getRuns().size());
            assertEquals(0, p.getRuns().indexOf(r));

            XWPFHyperlinkRun h = p.insertNewHyperlinkRun(0, "http://poi.apache.org");
            assertEquals(2, p.getRuns().size());
            assertEquals(0, p.getRuns().indexOf(h));
            assertEquals(1, p.getRuns().indexOf(r));

            XWPFFieldRun fieldRun2 = p.insertNewFieldRun(2);
            assertEquals(3, p.getRuns().size());
            assertEquals(2, p.getRuns().indexOf(fieldRun2));
        }
    }

    @Test
    void testRemoveRuns() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {

            XWPFParagraph p = doc.createParagraph();
            XWPFRun r = p.createRun();
            p.createRun();
            XWPFHyperlinkRun hyperlinkRun = p
                    .createHyperlinkRun("http://poi.apache.org");
            XWPFFieldRun fieldRun = p.createFieldRun();

            assertEquals(4, p.getRuns().size());
            assertEquals(2, p.getRuns().indexOf(hyperlinkRun));
            assertEquals(3, p.getRuns().indexOf(fieldRun));

            p.removeRun(2);
            assertEquals(3, p.getRuns().size());
            assertEquals(-1, p.getRuns().indexOf(hyperlinkRun));
            assertEquals(2, p.getRuns().indexOf(fieldRun));

            p.removeRun(0);
            assertEquals(2, p.getRuns().size());
            assertEquals(-1, p.getRuns().indexOf(r));
            assertEquals(1, p.getRuns().indexOf(fieldRun));

            p.removeRun(1);
            assertEquals(1, p.getRuns().size());
            assertEquals(-1, p.getRuns().indexOf(fieldRun));
        }
    }

    @Test
    void testRemoveAndInsertRunsWithOtherIRunElement()
            throws IOException {
        XWPFDocument doc = new XWPFDocument();

        XWPFParagraph p = doc.createParagraph();
        p.createRun();
        // add other run element
        p.getCTP().addNewSdt();
        // add two CTR in hyperlink
        XWPFHyperlinkRun hyperlinkRun = p
                .createHyperlinkRun("http://poi.apache.org");
        hyperlinkRun.getCTHyperlink().addNewR();
        p.createFieldRun();

        XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc);
        XWPFParagraph paragraph = doc2.getParagraphArray(0);

        assertEquals(4, paragraph.getRuns().size());
        assertEquals(5, paragraph.getIRuns().size());

        assertTrue(paragraph.getRuns().get(1) instanceof XWPFHyperlinkRun);
        assertTrue(paragraph.getRuns().get(2) instanceof XWPFHyperlinkRun);
        assertTrue(paragraph.getRuns().get(3) instanceof XWPFFieldRun);

        assertTrue(paragraph.getIRuns().get(1) instanceof XWPFSDT);
        assertTrue(paragraph.getIRuns().get(2) instanceof XWPFHyperlinkRun);

        paragraph.removeRun(1);
        assertEquals(3, paragraph.getRuns().size());
        assertTrue(paragraph.getRuns().get(1) instanceof XWPFHyperlinkRun);
        assertTrue(paragraph.getRuns().get(2) instanceof XWPFFieldRun);

        assertTrue(paragraph.getIRuns().get(1) instanceof XWPFSDT);
        assertTrue(paragraph.getIRuns().get(2) instanceof XWPFHyperlinkRun);

        paragraph.removeRun(1);
        assertEquals(2, paragraph.getRuns().size());
        assertTrue(paragraph.getRuns().get(1) instanceof XWPFFieldRun);

        assertTrue(paragraph.getIRuns().get(1) instanceof XWPFSDT);
        assertTrue(paragraph.getIRuns().get(2) instanceof XWPFFieldRun);

        paragraph.removeRun(0);
        assertEquals(1, paragraph.getRuns().size());
        assertTrue(paragraph.getRuns().get(0) instanceof XWPFFieldRun);

        assertTrue(paragraph.getIRuns().get(0) instanceof XWPFSDT);
        assertTrue(paragraph.getIRuns().get(1) instanceof XWPFFieldRun);

        XWPFRun newRun = paragraph.insertNewRun(0);
        assertEquals(2, paragraph.getRuns().size());

        assertEquals(3, paragraph.getIRuns().size());
        assertEquals(0, paragraph.getRuns().indexOf(newRun));

        doc.close();
        doc2.close();
    }

    @Test
    void testPictures() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("VariousPictures.docx")) {
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
        }
    }

    @Test
    void testTika792() throws Exception {
        //This test forces the loading of CTMoveBookmark and
        //CTMoveBookmarkImpl into ooxml-lite.
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Tika-792.docx")) {
            XWPFParagraph paragraph = doc.getParagraphs().get(0);
            assertEquals("", paragraph.getText());
            paragraph = doc.getParagraphs().get(1);
            assertEquals("b", paragraph.getText());
        }
    }

    @Test
    void testSettersGetters() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
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

            assertFalse(p.isKeepNext());
            p.setKeepNext(true);
            assertTrue(p.isKeepNext());
            p.setKeepNext(false);
            assertFalse(p.isKeepNext());

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
            p.setStyle("testStyle");
            assertEquals("testStyle", p.getStyle());

            p.addRun(CTR.Factory.newInstance());

            //assertTrue(p.removeRun(0));

            assertNotNull(p.getBody());
            assertEquals(BodyElementType.PARAGRAPH, p.getElementType());
            assertEquals(BodyType.DOCUMENT, p.getPartType());
        }
    }

    @Test
    void testSearchTextNotFound() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p = doc.createParagraph();

            assertNull(p.searchText("test", new PositionInParagraph()));
            assertEquals("", p.getText());
        }
    }

    @Test
    void testSearchTextFound() throws IOException {
        try (XWPFDocument xml = XWPFTestDataSamples.openSampleDocument("ThreeColHead.docx")) {

            List<XWPFParagraph> ps = xml.getParagraphs();
            assertEquals(10, ps.size());

            XWPFParagraph p = ps.get(0);

            TextSegment segment = p.searchText("sample word document", new PositionInParagraph());
            assertNotNull(segment);

            assertEquals("sample word document", p.getText(segment));

            assertTrue(p.removeRun(0));
        }
    }

    @Test
    void testFieldRuns() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("FldSimple.docx")) {
            List<XWPFParagraph> ps = doc.getParagraphs();
            assertEquals(1, ps.size());

            XWPFParagraph p = ps.get(0);
            assertEquals(1, p.getRuns().size());
            assertEquals(1, p.getIRuns().size());

            XWPFRun r = p.getRuns().get(0);
            assertEquals(XWPFFieldRun.class, r.getClass());

            XWPFFieldRun fr = (XWPFFieldRun) r;
            assertEquals(" FILENAME   \\* MERGEFORMAT ", fr.getFieldInstruction());
            assertEquals("FldSimple.docx", fr.text());
            assertEquals("FldSimple.docx", p.getText());
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    void testRuns() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p = doc.createParagraph();

            CTR run = CTR.Factory.newInstance();
            XWPFRun r = new XWPFRun(run, doc.createParagraph());
            p.addRun(r);
            p.addRun(r);

            assertNotNull(p.getRun(run));
            assertNull(p.getRun(null));
        }
    }

    @Test
    void test58067() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("58067.docx")) {

            StringBuilder str = new StringBuilder();
            for (XWPFParagraph par : doc.getParagraphs()) {
                str.append(par.getText()).append("\n");
            }
            assertEquals("This is a test.\n\n\n\n3\n4\n5\n\n\n\nThis is a whole paragraph where one word is deleted.\n", str.toString());
        }
    }

    @Test
    void test61787() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("61787.docx")) {

            StringBuilder str = new StringBuilder();
            for (XWPFParagraph par : doc.getParagraphs()) {
                str.append(par.getText()).append("\n");
            }
            String s = str.toString();
            assertTrue(s.trim().length() > 0, "Having text: \n" + s + "\nTrimmed length: " + s.trim().length());
        }
    }

    /**
     * Tests for numbered lists
     *
     * See also https://github.com/jimklo/apache-poi-sample/blob/master/src/main/java/com/sri/jklo/StyledDocument.java
     * for someone else trying a similar thing
     */
    @Test
    void testNumberedLists() throws Exception {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("ComplexNumberedLists.docx")) {
            XWPFParagraph p;

            p = doc.getParagraphArray(0);
            assertEquals("This is a document with numbered lists", p.getText());
            assertNull(p.getNumID());
            assertNull(p.getNumIlvl());
            assertNull(p.getNumStartOverride());

            p = doc.getParagraphArray(1);
            assertEquals("Entry #1", p.getText());
            assertEquals(BigInteger.valueOf(1), p.getNumID());
            assertEquals(BigInteger.valueOf(0), p.getNumIlvl());
            assertNull(p.getNumStartOverride());

            p = doc.getParagraphArray(2);
            assertEquals("Entry #2, with children", p.getText());
            assertEquals(BigInteger.valueOf(1), p.getNumID());
            assertEquals(BigInteger.valueOf(0), p.getNumIlvl());
            assertNull(p.getNumStartOverride());

            p = doc.getParagraphArray(3);
            assertEquals("2-a", p.getText());
            assertEquals(BigInteger.valueOf(1), p.getNumID());
            assertEquals(BigInteger.valueOf(1), p.getNumIlvl());
            assertNull(p.getNumStartOverride());

            p = doc.getParagraphArray(4);
            assertEquals("2-b", p.getText());
            assertEquals(BigInteger.valueOf(1), p.getNumID());
            assertEquals(BigInteger.valueOf(1), p.getNumIlvl());
            assertNull(p.getNumStartOverride());

            p = doc.getParagraphArray(5);
            assertEquals("2-c", p.getText());
            assertEquals(BigInteger.valueOf(1), p.getNumID());
            assertEquals(BigInteger.valueOf(1), p.getNumIlvl());
            assertNull(p.getNumStartOverride());

            p = doc.getParagraphArray(6);
            assertEquals("Entry #3", p.getText());
            assertEquals(BigInteger.valueOf(1), p.getNumID());
            assertEquals(BigInteger.valueOf(0), p.getNumIlvl());
            assertNull(p.getNumStartOverride());

            p = doc.getParagraphArray(7);
            assertEquals("Entry #4", p.getText());
            assertEquals(BigInteger.valueOf(1), p.getNumID());
            assertEquals(BigInteger.valueOf(0), p.getNumIlvl());
            assertNull(p.getNumStartOverride());

            // New list
            p = doc.getParagraphArray(8);
            assertEquals("Restarted to 1 from 5", p.getText());
            assertEquals(BigInteger.valueOf(2), p.getNumID());
            assertEquals(BigInteger.valueOf(0), p.getNumIlvl());
            assertNull(p.getNumStartOverride());

            p = doc.getParagraphArray(9);
            assertEquals("Restarted @ 2", p.getText());
            assertEquals(BigInteger.valueOf(2), p.getNumID());
            assertEquals(BigInteger.valueOf(0), p.getNumIlvl());
            assertNull(p.getNumStartOverride());

            p = doc.getParagraphArray(10);
            assertEquals("Restarted @ 3", p.getText());
            assertEquals(BigInteger.valueOf(2), p.getNumID());
            assertEquals(BigInteger.valueOf(0), p.getNumIlvl());
            assertNull(p.getNumStartOverride());

            // New list starting at 10
            p = doc.getParagraphArray(11);
            assertEquals("Jump to new list at 10", p.getText());
            assertEquals(BigInteger.valueOf(6), p.getNumID());
            assertEquals(BigInteger.valueOf(0), p.getNumIlvl());
            // TODO Why isn't this seen as 10?
            assertNull(p.getNumStartOverride());

            // TODO Shouldn't we use XWPFNumbering or similar here?
            // TODO Make it easier to change
        }
    }
}
