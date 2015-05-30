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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBrClear;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STUnderline;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalAlignRun;

/**
 * Tests for XWPF Run
 */
public class TestXWPFRun extends TestCase {
    public CTR ctRun;
    public XWPFParagraph p;

    protected void setUp() {
        XWPFDocument doc = new XWPFDocument();
        p = doc.createParagraph();

        this.ctRun = CTR.Factory.newInstance();
    }

    public void testSetGetText() {
        ctRun.addNewT().setStringValue("TEST STRING");
        ctRun.addNewT().setStringValue("TEST2 STRING");
        ctRun.addNewT().setStringValue("TEST3 STRING");

        assertEquals(3, ctRun.sizeOfTArray());
        XWPFRun run = new XWPFRun(ctRun, p);

        assertEquals("TEST2 STRING", run.getText(1));

        run.setText("NEW STRING", 0);
        assertEquals("NEW STRING", run.getText(0));

        //run.setText("xxx",14);
        //fail("Position wrong");
    }

    public void testSetGetBold() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewB().setVal(STOnOff.TRUE);

        XWPFRun run = new XWPFRun(ctRun, p);
        assertEquals(true, run.isBold());

        run.setBold(false);
        assertEquals(STOnOff.FALSE, rpr.getB().getVal());
    }

    public void testSetGetItalic() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewI().setVal(STOnOff.TRUE);

        XWPFRun run = new XWPFRun(ctRun, p);
        assertEquals(true, run.isItalic());

        run.setItalic(false);
        assertEquals(STOnOff.FALSE, rpr.getI().getVal());
    }

    public void testSetGetStrike() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewStrike().setVal(STOnOff.TRUE);

        XWPFRun run = new XWPFRun(ctRun, p);
        assertEquals(true, run.isStrike());

        run.setStrike(false);
        assertEquals(STOnOff.FALSE, rpr.getStrike().getVal());
    }

    public void testSetGetUnderline() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewU().setVal(STUnderline.DASH);

        XWPFRun run = new XWPFRun(ctRun, p);
        assertEquals(UnderlinePatterns.DASH.getValue(), run.getUnderline()
                .getValue());

        run.setUnderline(UnderlinePatterns.NONE);
        assertEquals(STUnderline.NONE.intValue(), rpr.getU().getVal()
                .intValue());
    }


    public void testSetGetVAlign() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewVertAlign().setVal(STVerticalAlignRun.SUBSCRIPT);

        XWPFRun run = new XWPFRun(ctRun, p);
        assertEquals(VerticalAlign.SUBSCRIPT, run.getSubscript());

        run.setSubscript(VerticalAlign.BASELINE);
        assertEquals(STVerticalAlignRun.BASELINE, rpr.getVertAlign().getVal());
    }


    public void testSetGetFontFamily() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewRFonts().setAscii("Times New Roman");

        XWPFRun run = new XWPFRun(ctRun, p);
        assertEquals("Times New Roman", run.getFontFamily());

        run.setFontFamily("Verdana");
        assertEquals("Verdana", rpr.getRFonts().getAscii());
    }


    public void testSetGetFontSize() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewSz().setVal(new BigInteger("14"));

        XWPFRun run = new XWPFRun(ctRun, p);
        assertEquals(7, run.getFontSize());

        run.setFontSize(24);
        assertEquals(48, rpr.getSz().getVal().longValue());
    }


    public void testSetGetTextForegroundBackground() {
        CTRPr rpr = ctRun.addNewRPr();
        rpr.addNewPosition().setVal(new BigInteger("4000"));

        XWPFRun run = new XWPFRun(ctRun, p);
        assertEquals(4000, run.getTextPosition());

        run.setTextPosition(2400);
        assertEquals(2400, rpr.getPosition().getVal().longValue());
    }

    public void testSetGetColor() {
        XWPFRun run = new XWPFRun(ctRun, p);
        run.setColor("0F0F0F");
        String clr = run.getColor();
        assertEquals("0F0F0F", clr);
    }

    public void testAddCarriageReturn() {
        ctRun.addNewT().setStringValue("TEST STRING");
        ctRun.addNewCr();
        ctRun.addNewT().setStringValue("TEST2 STRING");
        ctRun.addNewCr();
        ctRun.addNewT().setStringValue("TEST3 STRING");
        assertEquals(2, ctRun.sizeOfCrArray());

        XWPFRun run = new XWPFRun(CTR.Factory.newInstance(), p);
        run.setText("T1");
        run.addCarriageReturn();
        run.addCarriageReturn();
        run.setText("T2");
        run.addCarriageReturn();
        assertEquals(3, run.getCTR().sizeOfCrArray());

        assertEquals("T1\n\nT2\n", run.toString());
    }

    public void testAddTabsAndLineBreaks() {
        ctRun.addNewT().setStringValue("TEST STRING");
        ctRun.addNewCr();
        ctRun.addNewT().setStringValue("TEST2 STRING");
        ctRun.addNewTab();
        ctRun.addNewT().setStringValue("TEST3 STRING");
        assertEquals(1, ctRun.sizeOfCrArray());
        assertEquals(1, ctRun.sizeOfTabArray());

        XWPFRun run = new XWPFRun(CTR.Factory.newInstance(), p);
        run.setText("T1");
        run.addCarriageReturn();
        run.setText("T2");
        run.addTab();
        run.setText("T3");
        assertEquals(1, run.getCTR().sizeOfCrArray());
        assertEquals(1, run.getCTR().sizeOfTabArray());

        assertEquals("T1\nT2\tT3", run.toString());
    }

    public void testAddPageBreak() {
        ctRun.addNewT().setStringValue("TEST STRING");
        ctRun.addNewBr();
        ctRun.addNewT().setStringValue("TEST2 STRING");
        CTBr breac = ctRun.addNewBr();
        breac.setClear(STBrClear.LEFT);
        ctRun.addNewT().setStringValue("TEST3 STRING");
        assertEquals(2, ctRun.sizeOfBrArray());

        XWPFRun run = new XWPFRun(CTR.Factory.newInstance(), p);
        run.setText("TEXT1");
        run.addBreak();
        run.setText("TEXT2");
        run.addBreak(BreakType.TEXT_WRAPPING);
        assertEquals(2, run.getCTR().sizeOfBrArray());
    }

    /**
     * Test that on an existing document, we do the
     * right thing with it
     *
     * @throws IOException
     */
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
        assertEquals(false, run.isStrike());
        assertEquals(null, run.getCTR().getRPr());

        run = p.getRuns().get(1);
        assertEquals(".", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrike());
        assertEquals(null, run.getCTR().getRPr());


        // Next paragraph is all in one style, but a different one
        p = doc.getParagraphArray(1);
        assertEquals("This bit is in bold and italic", p.getText());
        assertEquals(1, p.getRuns().size());

        run = p.getRuns().get(0);
        assertEquals("This bit is in bold and italic", run.toString());
        assertEquals(true, run.isBold());
        assertEquals(true, run.isItalic());
        assertEquals(false, run.isStrike());
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
        assertEquals(false, run.isStrike());
        assertEquals(null, run.getCTR().getRPr());


        // Different styles in one paragraph
        p = doc.getParagraphArray(3);
        assertEquals("This contains BOLD, ITALIC and BOTH, as well as RED and YELLOW text.", p.getText());
        assertEquals(11, p.getRuns().size());

        run = p.getRuns().get(0);
        assertEquals("This contains ", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrike());
        assertEquals(null, run.getCTR().getRPr());

        run = p.getRuns().get(1);
        assertEquals("BOLD", run.toString());
        assertEquals(true, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrike());

        run = p.getRuns().get(2);
        assertEquals(", ", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrike());
        assertEquals(null, run.getCTR().getRPr());

        run = p.getRuns().get(3);
        assertEquals("ITALIC", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(true, run.isItalic());
        assertEquals(false, run.isStrike());

        run = p.getRuns().get(4);
        assertEquals(" and ", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrike());
        assertEquals(null, run.getCTR().getRPr());

        run = p.getRuns().get(5);
        assertEquals("BOTH", run.toString());
        assertEquals(true, run.isBold());
        assertEquals(true, run.isItalic());
        assertEquals(false, run.isStrike());

        run = p.getRuns().get(6);
        assertEquals(", as well as ", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrike());
        assertEquals(null, run.getCTR().getRPr());

        run = p.getRuns().get(7);
        assertEquals("RED", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrike());

        run = p.getRuns().get(8);
        assertEquals(" and ", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrike());
        assertEquals(null, run.getCTR().getRPr());

        run = p.getRuns().get(9);
        assertEquals("YELLOW", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrike());

        run = p.getRuns().get(10);
        assertEquals(" text.", run.toString());
        assertEquals(false, run.isBold());
        assertEquals(false, run.isItalic());
        assertEquals(false, run.isStrike());
        assertEquals(null, run.getCTR().getRPr());
    }

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
    }

    public void testAddPicture() throws Exception {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("TestDocument.docx");
        XWPFParagraph p = doc.getParagraphArray(2);
        XWPFRun r = p.getRuns().get(0);

        assertEquals(0, doc.getAllPictures().size());
        assertEquals(0, r.getEmbeddedPictures().size());

        r.addPicture(new ByteArrayInputStream(new byte[0]), Document.PICTURE_TYPE_JPEG, "test.jpg", 21, 32);

        assertEquals(1, doc.getAllPictures().size());
        assertEquals(1, r.getEmbeddedPictures().size());
    }

    /**
     * Bugzilla #52288 - setting the font family on the
     * run mustn't NPE
     */
    public void testSetFontFamily_52288() throws Exception {
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
    }
}
