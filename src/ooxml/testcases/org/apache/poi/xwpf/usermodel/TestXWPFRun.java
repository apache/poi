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

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlip;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBrClear;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHighlightColor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STUnderline;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalAlignRun;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests for XWPF Run
 */
public class TestXWPFRun {
    private CTR ctRun;
    private XWPFParagraph p;
    private IRunBody irb;
    private XWPFDocument doc;

    @Before
    public void setUp() {
        doc = new XWPFDocument();
        p = doc.createParagraph();
        irb = p;

        this.ctRun = CTR.Factory.newInstance();
    }

    @After
    public void tearDown() throws Exception {
        doc.close();
    }

    @Test
    public void testSetGetText() {
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
    public void testCTOnOff() {
        CTRPr rpr = ctRun.addNewRPr();
        CTOnOff bold = rpr.addNewB();        
        XWPFRun run = new XWPFRun(ctRun, irb);

        // True values: "true", "1", "on"
        bold.setVal(STOnOff.TRUE);
        assertEquals(true, run.isBold());    

        bold.setVal(STOnOff.X_1);
        assertEquals(true, run.isBold());

        bold.setVal(STOnOff.ON);
        assertEquals(true, run.isBold());

        // False values: "false", "0", "off"
        bold.setVal(STOnOff.FALSE);
        assertEquals(false, run.isBold());

        bold.setVal(STOnOff.X_0);
        assertEquals(false, run.isBold());

        bold.setVal(STOnOff.OFF);
        assertEquals(false, run.isBold());
    }

    @Test
    public void testSetGetBold() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewB().setVal(STOnOff.TRUE);

        XWPFRun run = new XWPFRun(ctRun, irb);
        assertEquals(true, run.isBold());

        run.setBold(false);
        // Implementation detail: POI natively prefers <w:b w:val="false"/>,
        // but should correctly read val="0" and val="off"
        assertEquals(STOnOff.FALSE, rpr.getB().getVal());
    }

    @Test
    public void testSetGetItalic() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewI().setVal(STOnOff.TRUE);

        XWPFRun run = new XWPFRun(ctRun, irb);
        assertEquals(true, run.isItalic());

        run.setItalic(false);
        assertEquals(STOnOff.FALSE, rpr.getI().getVal());
    }

    @Test
    public void testSetGetStrike() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewStrike().setVal(STOnOff.TRUE);

        XWPFRun run = new XWPFRun(ctRun, irb);
        assertEquals(true, run.isStrikeThrough());

        run.setStrikeThrough(false);
        assertEquals(STOnOff.FALSE, rpr.getStrike().getVal());
    }

    @Test
    public void testSetGetUnderline() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewU().setVal(STUnderline.DASH);

        XWPFRun run = new XWPFRun(ctRun, irb);
        assertEquals(UnderlinePatterns.DASH.getValue(), run.getUnderline()
                .getValue());

        run.setUnderline(UnderlinePatterns.NONE);
        assertEquals(STUnderline.NONE.intValue(), rpr.getU().getVal()
                .intValue());
    }

    @Test
    public void testSetGetVAlign() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewVertAlign().setVal(STVerticalAlignRun.SUBSCRIPT);

        XWPFRun run = new XWPFRun(ctRun, irb);
        assertEquals(VerticalAlign.SUBSCRIPT, run.getSubscript());

        run.setSubscript(VerticalAlign.BASELINE);
        assertEquals(STVerticalAlignRun.BASELINE, rpr.getVertAlign().getVal());
    }

    @Test
    public void testSetGetFontFamily() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewRFonts().setAscii("Times New Roman");

        XWPFRun run = new XWPFRun(ctRun, irb);
        assertEquals("Times New Roman", run.getFontFamily());

        run.setFontFamily("Verdana");
        assertEquals("Verdana", rpr.getRFonts().getAscii());
    }

    @Test
    public void testSetGetFontSize() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewSz().setVal(new BigInteger("14"));

        XWPFRun run = new XWPFRun(ctRun, irb);
        assertEquals(7, run.getFontSize());

        run.setFontSize(24);
        assertEquals(48, rpr.getSz().getVal().longValue());
    }

    @Test
    public void testSetGetTextForegroundBackground() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewPosition().setVal(new BigInteger("4000"));

        XWPFRun run = new XWPFRun(ctRun, irb);
        assertEquals(4000, run.getTextPosition());

        run.setTextPosition(2400);
        assertEquals(2400, rpr.getPosition().getVal().longValue());
    }

    @Test
    public void testSetGetColor() {
        XWPFRun run = new XWPFRun(ctRun, irb);
        run.setColor("0F0F0F");
        String clr = run.getColor();
        assertEquals("0F0F0F", clr);
    }

    @Test
    public void testAddCarriageReturn() {
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
    public void testAddTabsAndLineBreaks() {
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
    public void testAddPageBreak() {
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
    public void testExisting() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("TestDocument.docx");
        XWPFParagraph p;
        XWPFRun run;


        // First paragraph is simple
        p = doc.getParagraphArray(0);
        assertEquals("This is a test document.", p.getText());
        assertEquals(2, p.getRuns().size());

        run = p.getRuns().get(0);
        assertEquals("This is a test document", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrikeThrough());
        assertEquals(null, run.getCTR().getRPr());

        run = p.getRuns().get(1);
        assertEquals(".", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrikeThrough());
        assertEquals(null, run.getCTR().getRPr());


        // Next paragraph is all in one style, but a different one
        p = doc.getParagraphArray(1);
        assertEquals("This bit is in bold and italic", p.getText());
        assertEquals(1, p.getRuns().size());

        run = p.getRuns().get(0);
        assertEquals("This bit is in bold and italic", run.toString());
        assertEquals(true, run.isBold());
        assertEquals(true, run.isItalic());
        assertEquals(false, run.isStrikeThrough());
        assertEquals(true, run.getCTR().getRPr().isSetB());
        assertEquals(false, run.getCTR().getRPr().getB().isSetVal());


        // Back to normal
        p = doc.getParagraphArray(2);
        assertEquals("Back to normal", p.getText());
        assertEquals(1, p.getRuns().size());

        run = p.getRuns().get(0);
        assertEquals("Back to normal", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrikeThrough());
        assertEquals(null, run.getCTR().getRPr());


        // Different styles in one paragraph
        p = doc.getParagraphArray(3);
        assertEquals("This contains BOLD, ITALIC and BOTH, as well as RED and YELLOW text.", p.getText());
        assertEquals(11, p.getRuns().size());

        run = p.getRuns().get(0);
        assertEquals("This contains ", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrikeThrough());
        assertEquals(null, run.getCTR().getRPr());

        run = p.getRuns().get(1);
        assertEquals("BOLD", run.toString());
        assertEquals(true, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrikeThrough());

        run = p.getRuns().get(2);
        assertEquals(", ", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrikeThrough());
        assertEquals(null, run.getCTR().getRPr());

        run = p.getRuns().get(3);
        assertEquals("ITALIC", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(true, run.isItalic());
        assertEquals(false, run.isStrikeThrough());

        run = p.getRuns().get(4);
        assertEquals(" and ", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrikeThrough());
        assertEquals(null, run.getCTR().getRPr());

        run = p.getRuns().get(5);
        assertEquals("BOTH", run.toString());
        assertEquals(true, run.isBold());
        assertEquals(true, run.isItalic());
        assertEquals(false, run.isStrikeThrough());

        run = p.getRuns().get(6);
        assertEquals(", as well as ", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrikeThrough());
        assertEquals(null, run.getCTR().getRPr());

        run = p.getRuns().get(7);
        assertEquals("RED", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrikeThrough());

        run = p.getRuns().get(8);
        assertEquals(" and ", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrikeThrough());
        assertEquals(null, run.getCTR().getRPr());

        run = p.getRuns().get(9);
        assertEquals("YELLOW", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrikeThrough());

        run = p.getRuns().get(10);
        assertEquals(" text.", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrikeThrough());
        assertEquals(null, run.getCTR().getRPr());
        
        doc.close();
    }

    @Test
    public void testPictureInHeader() throws IOException {
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
    public void testSetGetLang() {
        XWPFRun run = p.createRun();
        assertNull(run.getLang());

        run.getCTR().addNewRPr().addNewLang().setVal("en-CA");
        assertEquals("en-CA", run.getLang());

        run.getCTR().getRPr().getLang().setVal("fr-CA");
        assertEquals("fr-CA", run.getLang());

        run.getCTR().getRPr().getLang().setVal(null);
        assertNull(run.getLang());
    }

    @Test
    public void testSetGetHighlight() {
        XWPFRun run = p.createRun();
        assertEquals(false, run.isHighlighted());
        
        // TODO Do this using XWPFRun methods
        run.getCTR().addNewRPr().addNewHighlight().setVal(STHighlightColor.NONE);
        assertEquals(false, run.isHighlighted());
        run.getCTR().getRPr().getHighlight().setVal(STHighlightColor.CYAN);
        assertEquals(true, run.isHighlighted());
        run.getCTR().getRPr().getHighlight().setVal(STHighlightColor.NONE);
        assertEquals(false, run.isHighlighted());
    }

    @Test
    public void testAddPicture() throws Exception {
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
    public void testAddPictureInHeader() throws IOException, InvalidFormatException {
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
    public void testSetFontFamily_52288() throws IOException {
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
    public void testBug55476() throws IOException, InvalidFormatException {
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
    public void testBug58922() throws IOException {
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
    public void testSetters() throws IOException {
        XWPFDocument document = new XWPFDocument();
        final XWPFRun run = document.createParagraph().createRun();

        // at least trigger some of the setters to ensure classes are included in
        // the poi-ooxml-schemas
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

        document.close();
    }
    
    @Test
    public void testSetStyleId() throws IOException {
        XWPFDocument document = XWPFTestDataSamples.openSampleDocument("SampleDoc.docx");
        final XWPFRun run = document.createParagraph().createRun();
        
        String styleId = "bolditalic";
        run.setStyle(styleId);
        String candStyleId = run.getCTR().getRPr().getRStyle().getVal();
        assertNotNull("Expected to find a run style ID", candStyleId);
        assertEquals(styleId, candStyleId);
        
    }

}
