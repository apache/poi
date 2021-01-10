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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.Units;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlip;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STOnOff1;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STVerticalAlignRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBrClear;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STEm;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHighlightColor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STThemeColor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STUnderline;

/**
 * Tests for XWPF Run
 */
class TestXWPFRun {
    private CTR ctRun;
    private XWPFParagraph p;
    private IRunBody irb;
    private XWPFDocument doc;

    @BeforeEach
    void setUp() {
        doc = new XWPFDocument();
        p = doc.createParagraph();
        irb = p;

        this.ctRun = CTR.Factory.newInstance();
    }

    @AfterEach
    void tearDown() throws Exception {
        doc.close();
    }

    @Test
    void testSetGetText() {
        ctRun.addNewT().setStringValue("TEST STRING");
        ctRun.addNewT().setStringValue("TEST2 STRING");
        ctRun.addNewT().setStringValue("TEST3 STRING");

        assertEquals(3, ctRun.sizeOfTArray());
        XWPFRun run = new XWPFRun(ctRun, irb);

        assertEquals("TEST2 STRING", run.getText(1));

        run.setText("NEW STRING", 0);
        assertEquals("NEW STRING", run.getText(0));

        //run.setText("xxx",14);
        //fail("Position wrong");
    }

    /*
     * bug 59208
     * Purpose: test all valid boolean-like values
     * exercise isCTOnOff(CTOnOff) through all valid permutations
     */
    @Test
    void testCTOnOff() {
        CTRPr rpr = ctRun.addNewRPr();
        CTOnOff bold = rpr.addNewB();
        XWPFRun run = new XWPFRun(ctRun, irb);

        // True values: "true", "1", "on"
        bold.setVal(STOnOff1.ON);
        assertTrue(run.isBold());

        bold.setVal(STOnOff1.ON);
        assertTrue(run.isBold());

        bold.setVal(STOnOff1.ON);
        assertTrue(run.isBold());

        // False values: "false", "0", "off"
        bold.setVal(STOnOff1.OFF);
        assertFalse(run.isBold());

        bold.setVal(STOnOff1.OFF);
        assertFalse(run.isBold());

        bold.setVal(STOnOff1.OFF);
        assertFalse(run.isBold());
    }

    @Test
    void testSetGetBold() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewB().setVal(STOnOff1.ON);

        XWPFRun run = new XWPFRun(ctRun, irb);
        assertTrue(run.isBold());

        run.setBold(false);
        // Implementation detail: POI natively prefers <w:b w:val="false"/>,
        // but should correctly read val="0" and val="off"
        assertEquals("off", rpr.getBArray(0).getVal());
    }

    @Test
    void testSetGetItalic() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewI().setVal(STOnOff1.ON);

        XWPFRun run = new XWPFRun(ctRun, irb);
        assertTrue(run.isItalic());

        run.setItalic(false);
        assertEquals("off", rpr.getIArray(0).getVal());
    }

    @Test
    void testSetGetStrike() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewStrike().setVal(STOnOff1.ON);

        XWPFRun run = new XWPFRun(ctRun, irb);
        assertTrue(run.isStrikeThrough());

        run.setStrikeThrough(false);
        assertEquals("off", rpr.getStrikeArray(0).getVal());
    }

    @Test
    void testSetGetUnderline() {
        CTRPr rpr = ctRun.addNewRPr();
        XWPFRun run = new XWPFRun(ctRun, irb);
        rpr.addNewU().setVal(STUnderline.DASH);

        assertEquals(UnderlinePatterns.DASH.getValue(), run.getUnderline()
                .getValue());

        run.setUnderline(UnderlinePatterns.NONE);
        assertEquals(STUnderline.NONE.intValue(), rpr.getUArray(0).getVal()
                .intValue());
    }

    @Test
    void testSetGetFontFamily() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewRFonts().setAscii("Times New Roman");

        XWPFRun run = new XWPFRun(ctRun, irb);
        assertEquals("Times New Roman", run.getFontFamily());

        run.setFontFamily("Verdana");
        assertEquals("Verdana", rpr.getRFontsArray(0).getAscii());
    }

    @Test
    void testSetGetFontSize() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewSz().setVal(BigInteger.valueOf(14));

        XWPFRun run = new XWPFRun(ctRun, irb);
        assertEquals(7, run.getFontSize());
        assertEquals(7.0, run.getFontSizeAsDouble(), 0.01);

        run.setFontSize(24);
        assertEquals("48", rpr.getSzArray(0).getVal().toString());

        run.setFontSize(24.5f);
        assertEquals("49", rpr.getSzArray(0).getVal().toString());
        assertEquals(25, run.getFontSize());
        assertEquals(24.5, run.getFontSizeAsDouble(), 0.01);
    }

    @Test
    void testSetGetTextForegroundBackground() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewPosition().setVal(new BigInteger("4000"));

        XWPFRun run = new XWPFRun(ctRun, irb);
        assertEquals(4000, run.getTextPosition());

        run.setTextPosition(2400);
        assertEquals("2400", rpr.getPositionArray(0).getVal().toString());
    }

    @Test
    void testSetGetColor() {
        XWPFRun run = new XWPFRun(ctRun, irb);
        run.setColor("0F0F0F");
        String clr = run.getColor();
        assertEquals("0F0F0F", clr);
    }

    @Test
    void testAddCarriageReturn() {
        ctRun.addNewT().setStringValue("TEST STRING");
        ctRun.addNewCr();
        ctRun.addNewT().setStringValue("TEST2 STRING");
        ctRun.addNewCr();
        ctRun.addNewT().setStringValue("TEST3 STRING");
        assertEquals(2, ctRun.sizeOfCrArray());

        XWPFRun run = new XWPFRun(CTR.Factory.newInstance(), irb);
        run.setText("T1");
        run.addCarriageReturn();
        run.addCarriageReturn();
        run.setText("T2");
        run.addCarriageReturn();
        assertEquals(3, run.getCTR().sizeOfCrArray());

        assertEquals("T1\n\nT2\n", run.toString());
    }

    @Test
    void testAddTabsAndLineBreaks() {
        ctRun.addNewT().setStringValue("TEST STRING");
        ctRun.addNewCr();
        ctRun.addNewT().setStringValue("TEST2 STRING");
        ctRun.addNewTab();
        ctRun.addNewT().setStringValue("TEST3 STRING");
        assertEquals(1, ctRun.sizeOfCrArray());
        assertEquals(1, ctRun.sizeOfTabArray());

        XWPFRun run = new XWPFRun(CTR.Factory.newInstance(), irb);
        run.setText("T1");
        run.addCarriageReturn();
        run.setText("T2");
        run.addTab();
        run.setText("T3");
        assertEquals(1, run.getCTR().sizeOfCrArray());
        assertEquals(1, run.getCTR().sizeOfTabArray());

        assertEquals("T1\nT2\tT3", run.toString());
    }

    @Test
    void testAddPageBreak() {
        ctRun.addNewT().setStringValue("TEST STRING");
        ctRun.addNewBr();
        ctRun.addNewT().setStringValue("TEST2 STRING");
        CTBr breac = ctRun.addNewBr();
        breac.setClear(STBrClear.LEFT);
        ctRun.addNewT().setStringValue("TEST3 STRING");
        assertEquals(2, ctRun.sizeOfBrArray());

        XWPFRun run = new XWPFRun(CTR.Factory.newInstance(), irb);
        run.setText("TEXT1");
        run.addBreak();
        run.setText("TEXT2");
        run.addBreak(BreakType.TEXT_WRAPPING);
        assertEquals(2, run.getCTR().sizeOfBrArray());
    }

    /**
     * Test that on an existing document, we do the
     * right thing with it
     */
    @Test
    void testExisting() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("TestDocument.docx");
        XWPFParagraph p;
        XWPFRun run;


        // First paragraph is simple
        p = doc.getParagraphArray(0);
        assertEquals("This is a test document.", p.getText());
        assertEquals(2, p.getRuns().size());

        run = p.getRuns().get(0);
        assertEquals("This is a test document", run.toString());
        assertFalse(run.isBold());
        assertFalse(run.isItalic());
        assertFalse(run.isStrikeThrough());
        assertNull(run.getCTR().getRPr());

        run = p.getRuns().get(1);
        assertEquals(".", run.toString());
        assertFalse(run.isBold());
        assertFalse(run.isItalic());
        assertFalse(run.isStrikeThrough());
        assertNull(run.getCTR().getRPr());


        // Next paragraph is all in one style, but a different one
        p = doc.getParagraphArray(1);
        assertEquals("This bit is in bold and italic", p.getText());
        assertEquals(1, p.getRuns().size());

        run = p.getRuns().get(0);
        assertEquals("This bit is in bold and italic", run.toString());
        assertTrue(run.isBold());
        assertTrue(run.isItalic());
        assertFalse(run.isStrikeThrough());
        assertTrue(run.getCTR().getRPr().sizeOfBArray() > 0);
        assertFalse(run.getCTR().getRPr().getBArray(0).isSetVal());


        // Back to normal
        p = doc.getParagraphArray(2);
        assertEquals("Back to normal", p.getText());
        assertEquals(1, p.getRuns().size());

        run = p.getRuns().get(0);
        assertEquals("Back to normal", run.toString());
        assertFalse(run.isBold());
        assertFalse(run.isItalic());
        assertFalse(run.isStrikeThrough());
        assertNull(run.getCTR().getRPr());


        // Different styles in one paragraph
        p = doc.getParagraphArray(3);
        assertEquals("This contains BOLD, ITALIC and BOTH, as well as RED and YELLOW text.", p.getText());
        assertEquals(11, p.getRuns().size());

        run = p.getRuns().get(0);
        assertEquals("This contains ", run.toString());
        assertFalse(run.isBold());
        assertFalse(run.isItalic());
        assertFalse(run.isStrikeThrough());
        assertNull(run.getCTR().getRPr());

        run = p.getRuns().get(1);
        assertEquals("BOLD", run.toString());
        assertTrue(run.isBold());
        assertFalse(run.isItalic());
        assertFalse(run.isStrikeThrough());

        run = p.getRuns().get(2);
        assertEquals(", ", run.toString());
        assertFalse(run.isBold());
        assertFalse(run.isItalic());
        assertFalse(run.isStrikeThrough());
        assertNull(run.getCTR().getRPr());

        run = p.getRuns().get(3);
        assertEquals("ITALIC", run.toString());
        assertFalse(run.isBold());
        assertTrue(run.isItalic());
        assertFalse(run.isStrikeThrough());

        run = p.getRuns().get(4);
        assertEquals(" and ", run.toString());
        assertFalse(run.isBold());
        assertFalse(run.isItalic());
        assertFalse(run.isStrikeThrough());
        assertNull(run.getCTR().getRPr());

        run = p.getRuns().get(5);
        assertEquals("BOTH", run.toString());
        assertTrue(run.isBold());
        assertTrue(run.isItalic());
        assertFalse(run.isStrikeThrough());

        run = p.getRuns().get(6);
        assertEquals(", as well as ", run.toString());
        assertFalse(run.isBold());
        assertFalse(run.isItalic());
        assertFalse(run.isStrikeThrough());
        assertNull(run.getCTR().getRPr());

        run = p.getRuns().get(7);
        assertEquals("RED", run.toString());
        assertFalse(run.isBold());
        assertFalse(run.isItalic());
        assertFalse(run.isStrikeThrough());

        run = p.getRuns().get(8);
        assertEquals(" and ", run.toString());
        assertFalse(run.isBold());
        assertFalse(run.isItalic());
        assertFalse(run.isStrikeThrough());
        assertNull(run.getCTR().getRPr());

        run = p.getRuns().get(9);
        assertEquals("YELLOW", run.toString());
        assertFalse(run.isBold());
        assertFalse(run.isItalic());
        assertFalse(run.isStrikeThrough());

        run = p.getRuns().get(10);
        assertEquals(" text.", run.toString());
        assertFalse(run.isBold());
        assertFalse(run.isItalic());
        assertFalse(run.isStrikeThrough());
        assertNull(run.getCTR().getRPr());

        doc.close();
    }

    @Test
    void testPictureInHeader() throws IOException {
        XWPFDocument sampleDoc = XWPFTestDataSamples.openSampleDocument("headerPic.docx");
        XWPFHeaderFooterPolicy policy = sampleDoc.getHeaderFooterPolicy();

        XWPFHeader header = policy.getDefaultHeader();

        int count = 0;

        for (XWPFParagraph p : header.getParagraphs()) {
            for (XWPFRun r : p.getRuns()) {
                List<XWPFPicture> pictures = r.getEmbeddedPictures();

                for (XWPFPicture pic : pictures) {
                    assertNotNull(pic.getPictureData());
                    assertEquals("DOZOR", pic.getDescription());
                }

                count += pictures.size();
            }
        }

        assertEquals(1, count);
        sampleDoc.close();
    }

    @Test
    void testSetGetLang() {
        XWPFRun run = p.createRun();
        assertNull(run.getLang());

        run.setLang("en-CA");
        assertEquals("en-CA", run.getLang());

        run.setLang("fr-CA");
        assertEquals("fr-CA", run.getLang());

        run.setLang(null);
        assertNull(run.getLang());
    }

    @Test
    void testSetGetLang2() {
        XWPFRun run = p.createRun();
        assertNull(run.getLang());

        run.getCTR().addNewRPr().addNewLang().setVal("en-CA");
        assertEquals("en-CA", run.getLang());

        run.getCTR().getRPr().getLangArray(0).setVal("fr-CA");
        assertEquals("fr-CA", run.getLang());

        run.getCTR().getRPr().getLangArray(0).setVal(null);
        assertNull(run.getLang());
    }

    @Test
    void testAddPicture() throws Exception {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("TestDocument.docx");
        XWPFParagraph p = doc.getParagraphArray(2);
        XWPFRun r = p.getRuns().get(0);

        assertEquals(0, doc.getAllPictures().size());
        assertEquals(0, r.getEmbeddedPictures().size());

        r.addPicture(new ByteArrayInputStream(new byte[0]), Document.PICTURE_TYPE_JPEG, "test.jpg", 21, 32);

        assertEquals(1, doc.getAllPictures().size());
        assertEquals(1, r.getEmbeddedPictures().size());

        XWPFDocument docBack = XWPFTestDataSamples.writeOutAndReadBack(doc);
        XWPFParagraph pBack = docBack.getParagraphArray(2);
        XWPFRun rBack = pBack.getRuns().get(0);

        assertEquals(1, docBack.getAllPictures().size());
        assertEquals(1, rBack.getEmbeddedPictures().size());
        docBack.close();
        doc.close();
    }

    /**
     * Bugzilla #58237 - Unable to add image to word document header
     */
    @Test
    void testAddPictureInHeader() throws IOException, InvalidFormatException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("TestDocument.docx");
        XWPFHeader hdr = doc.createHeader(HeaderFooterType.DEFAULT);
        XWPFParagraph p = hdr.createParagraph();
        XWPFRun r = p.createRun();

        assertEquals(0, hdr.getAllPictures().size());
        assertEquals(0, r.getEmbeddedPictures().size());

        r.addPicture(new ByteArrayInputStream(new byte[0]), Document.PICTURE_TYPE_JPEG, "test.jpg", 21, 32);

        assertEquals(1, hdr.getAllPictures().size());
        assertEquals(1, r.getEmbeddedPictures().size());

        XWPFPicture pic = r.getEmbeddedPictures().get(0);
        CTPicture ctPic = pic.getCTPicture();
        CTBlipFillProperties ctBlipFill = ctPic.getBlipFill();

        assertNotNull(ctBlipFill);

        CTBlip ctBlip = ctBlipFill.getBlip();

        assertNotNull(ctBlip);
        assertEquals("rId1", ctBlip.getEmbed());

        XWPFDocument docBack = XWPFTestDataSamples.writeOutAndReadBack(doc);
        XWPFHeader hdrBack = docBack.getHeaderArray(0);
        XWPFParagraph pBack = hdrBack.getParagraphArray(0);
        XWPFRun rBack = pBack.getRuns().get(0);

        assertEquals(1, hdrBack.getAllPictures().size());
        assertEquals(1, rBack.getEmbeddedPictures().size());
        docBack.close();
        doc.close();
    }

    /**
     * Bugzilla #52288 - setting the font family on the
     * run mustn't NPE
     */
    @Test
    void testSetFontFamily_52288() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("52288.docx");
        final Iterator<XWPFParagraph> paragraphs = doc.getParagraphsIterator();
        while (paragraphs.hasNext()) {
            final XWPFParagraph paragraph = paragraphs.next();
            for (final XWPFRun run : paragraph.getRuns()) {
                if (run != null) {
                    final String text = run.getText(0);
                    if (text != null) {
                        run.setFontFamily("Times New Roman");
                    }
                }
            }
        }
        doc.close();
    }

    @Test
    void testBug55476() throws IOException, InvalidFormatException {
        byte[] image = XWPFTestDataSamples.getImage("abstract1.jpg");
        XWPFDocument document = new XWPFDocument();

        document.createParagraph().createRun().addPicture(
                new ByteArrayInputStream(image), Document.PICTURE_TYPE_JPEG, "test.jpg", Units.toEMU(300), Units.toEMU(100));

        XWPFDocument docBack = XWPFTestDataSamples.writeOutAndReadBack(document);
        List<XWPFPicture> pictures = docBack.getParagraphArray(0).getRuns().get(0).getEmbeddedPictures();
        assertEquals(1, pictures.size());
        docBack.close();

        /*OutputStream stream = new FileOutputStream("c:\\temp\\55476.docx");
        try {
            document.write(stream);
        } finally {
            stream.close();
        }*/

        document.close();
    }

    @Test
    void testBug58922() throws IOException {
        XWPFDocument document = new XWPFDocument();

        final XWPFRun run = document.createParagraph().createRun();


        assertEquals(-1, run.getFontSize());

        run.setFontSize(10);
        assertEquals(10, run.getFontSize());

        run.setFontSize(Short.MAX_VALUE-1);
        assertEquals(Short.MAX_VALUE-1, run.getFontSize());

        run.setFontSize(Short.MAX_VALUE);
        assertEquals(Short.MAX_VALUE, run.getFontSize());

        run.setFontSize(Short.MAX_VALUE+1);
        assertEquals(Short.MAX_VALUE+1, run.getFontSize());

        run.setFontSize(Integer.MAX_VALUE-1);
        assertEquals(Integer.MAX_VALUE-1, run.getFontSize());

        run.setFontSize(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, run.getFontSize());

        run.setFontSize(-1);
        assertEquals(-1, run.getFontSize());


        assertEquals(-1, run.getTextPosition());

        run.setTextPosition(10);
        assertEquals(10, run.getTextPosition());

        run.setTextPosition(Short.MAX_VALUE-1);
        assertEquals(Short.MAX_VALUE-1, run.getTextPosition());

        run.setTextPosition(Short.MAX_VALUE);
        assertEquals(Short.MAX_VALUE, run.getTextPosition());

        run.setTextPosition(Short.MAX_VALUE+1);
        assertEquals(Short.MAX_VALUE+1, run.getTextPosition());

        run.setTextPosition(Short.MAX_VALUE+1);
        assertEquals(Short.MAX_VALUE+1, run.getTextPosition());

        run.setTextPosition(Integer.MAX_VALUE-1);
        assertEquals(Integer.MAX_VALUE-1, run.getTextPosition());

        run.setTextPosition(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, run.getTextPosition());

        run.setTextPosition(-1);
        assertEquals(-1, run.getTextPosition());

        document.close();
    }

    @Test
    void testSetters() {
        XWPFDocument document = new XWPFDocument();
        final XWPFRun run = document.createParagraph().createRun();

        // at least trigger some of the setters to ensure classes are included in
        // the poi-ooxml-lite
        run.setBold(true);
        run.setCapitalized(true);
        run.setCharacterSpacing(2);
        assertEquals(2, run.getCharacterSpacing());
        run.setColor("000000");
        run.setDoubleStrikethrough(true);
        run.setEmbossed(true);
        run.setFontFamily("Calibri");
        assertEquals("Calibri", run.getFontFamily());
        run.setFontSize(10);
        assertEquals(10, run.getFontSize());
        run.setImprinted(true);
        run.setItalic(true);
    }

    @Test
    void testSetGetTextScale() throws IOException {
        XWPFDocument document = new XWPFDocument();
        final XWPFRun run = document.createParagraph().createRun();
        assertEquals(100, run.getTextScale());
        run.setTextScale(200);
        assertEquals(200, run.getTextScale());
        document.close();
    }

    @Test
    void testSetGetTextHighlightColor() throws IOException {
        XWPFDocument document = new XWPFDocument();
        final XWPFRun run = document.createParagraph().createRun();
        assertEquals(STHighlightColor.NONE, run.getTextHightlightColor());
        assertFalse(run.isHighlighted());
        run.setTextHighlightColor("darkGreen"); // See 17.18.40 ST_HighlightColor (Text Highlight Colors)
        assertEquals(STHighlightColor.DARK_GREEN, run.getTextHightlightColor());
        assertTrue(run.isHighlighted());
        run.setTextHighlightColor("none");
        assertFalse(run.isHighlighted());

        document.close();
    }

    @Test
    void testSetGetVanish() throws IOException {
        XWPFDocument document = new XWPFDocument();
        final XWPFRun run = document.createParagraph().createRun();
        assertFalse(run.isVanish());
        run.setVanish(true);
        assertTrue(run.isVanish());
        run.setVanish(false);
        assertFalse(run.isVanish());
        document.close();
    }

    @Test
    void testSetGetVerticalAlignment() throws IOException {
        XWPFDocument document = new XWPFDocument();
        XWPFRun run = document.createParagraph().createRun();
        assertEquals(STVerticalAlignRun.BASELINE, run.getVerticalAlignment());
        // Reset to a fresh run so we test case of run not having vertical alignment at all
        run = document.createParagraph().createRun();
        run.setVerticalAlignment("subscript");
        assertEquals(STVerticalAlignRun.SUBSCRIPT, run.getVerticalAlignment());
        run.setVerticalAlignment("superscript");
        assertEquals(STVerticalAlignRun.SUPERSCRIPT, run.getVerticalAlignment());
        document.close();
    }

    @Test
    void testSetGetVAlign() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewVertAlign().setVal(STVerticalAlignRun.SUBSCRIPT);

        XWPFRun run = new XWPFRun(ctRun, irb);

        run.setSubscript(VerticalAlign.BASELINE);
        assertEquals(STVerticalAlignRun.BASELINE, rpr.getVertAlignArray(0).getVal());
    }

    @Test
    void testSetGetEmphasisMark() throws IOException {
        XWPFDocument document = new XWPFDocument();
        XWPFRun run = document.createParagraph().createRun();
        assertEquals(STEm.NONE, run.getEmphasisMark());
        // Reset to a fresh run so we test case of run not having property at all
        run = document.createParagraph().createRun();
        run.setEmphasisMark("dot");
        assertEquals(STEm.DOT, run.getEmphasisMark());
        document.close();
    }

    @Test
    void testSetGetUnderlineColor() throws IOException {
        XWPFDocument document = new XWPFDocument();
        XWPFRun run = document.createParagraph().createRun();
        assertEquals("auto", run.getUnderlineColor());
        // Reset to a fresh run so we test case of run not having property at all
        run = document.createParagraph().createRun();
        String colorRgb = "C0F1a2";
        run.setUnderlineColor(colorRgb);
        assertEquals(colorRgb.toUpperCase(LocaleUtil.getUserLocale()), run.getUnderlineColor());
        run.setUnderlineColor("auto");
        assertEquals("auto", run.getUnderlineColor());
        document.close();
    }

    @Test
    void testSetGetUnderlineThemeColor() throws IOException {
        XWPFDocument document = new XWPFDocument();
        XWPFRun run = document.createParagraph().createRun();
        assertEquals(STThemeColor.NONE, run.getUnderlineThemeColor());
        // Reset to a fresh run so we test case of run not having property at all
        run = document.createParagraph().createRun();
        String colorName = "accent4";
        run.setUnderlineThemeColor(colorName);
        assertEquals(STThemeColor.Enum.forString(colorName), run.getUnderlineThemeColor());
        run.setUnderlineThemeColor("none");
        assertEquals(STThemeColor.NONE, run.getUnderlineThemeColor());
        document.close();
    }


    @Test
    void testSetStyleId() throws IOException {
        XWPFDocument document = XWPFTestDataSamples.openSampleDocument("SampleDoc.docx");
        final XWPFRun run = document.createParagraph().createRun();

        String styleId = "bolditalic";
        run.setStyle(styleId);
        String candStyleId = run.getCTR().getRPr().getRStyleArray(0).getVal();
        assertNotNull( candStyleId, "Expected to find a run style ID" );
        assertEquals(styleId, candStyleId);

        assertEquals(styleId, run.getStyle());

        document.close();
    }

    @Test
    void testGetDepthWidth() throws IOException, InvalidFormatException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("TestDocument.docx")) {
            XWPFHeader hdr = doc.createHeader(HeaderFooterType.DEFAULT);
            XWPFParagraph p = hdr.createParagraph();
            XWPFRun r = p.createRun();

            assertEquals(0, hdr.getAllPictures().size());
            assertEquals(0, r.getEmbeddedPictures().size());

            r.addPicture(new ByteArrayInputStream(new byte[0]), Document.PICTURE_TYPE_JPEG, "test.jpg", 21, 32);

            assertEquals(1, hdr.getAllPictures().size());
            assertEquals(1, r.getEmbeddedPictures().size());

            XWPFPicture pic = r.getEmbeddedPictures().get(0);

            assertEquals(pic.getWidth(), Units.toPoints(21), 0.0);
            assertEquals(pic.getDepth(), Units.toPoints(32), 0.0);
        }
    }

    @Test
    void testWhitespace() throws IOException {
        String[] text = new String[] {
                "  The quick brown fox",
                "\t\tjumped over the lazy dog"
        };
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (XWPFDocument doc = new XWPFDocument();) {
            for(String s : text) {
                XWPFParagraph p1 = doc.createParagraph();
                XWPFRun r1 = p1.createRun();
                r1.setText(s);
            }

            doc.write(bos);
            bos.flush();
        }

        try (
                ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                XWPFDocument doc = new XWPFDocument(bis)
        ) {
            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            assertEquals(2, paragraphs.size());
            for (int i = 0; i < text.length; i++) {
                XWPFParagraph p1 = paragraphs.get(i);
                String expected = text[i];
                assertEquals(expected, p1.getText());
                CTP ctp = p1.getCTP();
                CTR ctr = ctp.getRArray(0);
                CTText ctText = ctr.getTArray(0);
                // if text has leading whitespace then expect xml-fragment to have xml:space="preserve" set
                // <xml-fragment xml:space="preserve" xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                boolean isWhitespace = Character.isWhitespace(expected.charAt(0));
                assertEquals(isWhitespace, ctText.isSetSpace());
            }
        }
    }
}
