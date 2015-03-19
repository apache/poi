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
package org.apache.poi.xslf.usermodel;

import static org.junit.Assert.*;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import org.apache.poi.sl.draw.DrawTextFragment;
import org.apache.poi.sl.draw.DrawTextParagraph;
import org.apache.poi.sl.usermodel.TextParagraph.TextAlign;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xslf.XSLFTestDataSamples;
import org.junit.Test;

/**
 * @author Yegor Kozlov
 */
public class TestXSLFTextParagraph {
    private static POILogger _logger = POILogFactory.getLogger(XSLFTextParagraph.class);

    static class DrawTextParagraphProxy extends DrawTextParagraph<XSLFTextRun> {
        DrawTextParagraphProxy(XSLFTextParagraph p) {
            super(p);
        }
        
        public void breakText(Graphics2D graphics) {
            super.breakText(graphics);
        }
        
        public double getWrappingWidth(boolean firstLine, Graphics2D graphics) {
            return super.getWrappingWidth(firstLine, graphics);
        }
        
        public List<DrawTextFragment> getLines() {
            return lines;
        }
    }
    
    @Test
    public void testWrappingWidth() throws Exception {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();
        XSLFTextShape sh = slide.createAutoShape();
        sh.setLineColor(Color.black);

        XSLFTextParagraph p = sh.addNewTextParagraph();
        p.addNewTextRun().setText(
                "Paragraph formatting allows for more granular control " +
                "of text within a shape. Properties here apply to all text " +
                "residing within the corresponding paragraph.");

        Rectangle2D anchor = new Rectangle(50, 50, 300, 200);
        sh.setAnchor(anchor);
        
        DrawTextParagraphProxy dtp = new DrawTextParagraphProxy(p);

        double leftInset = sh.getLeftInset();
        double rightInset = sh.getRightInset();
        assertEquals(7.2, leftInset, 0);
        assertEquals(7.2, rightInset, 0);

        double leftMargin = p.getLeftMargin();
        assertEquals(0.0, leftMargin, 0);

        double indent = p.getIndent();
        assertEquals(0.0, indent, 0); // default

        double expectedWidth;

        // Case 1: bullet=false, leftMargin=0, indent=0.
        expectedWidth = anchor.getWidth() - leftInset - rightInset - leftMargin;
        assertEquals(285.6, expectedWidth, 0); // 300 - 7.2 - 7.2 - 0
        assertEquals(expectedWidth, dtp.getWrappingWidth(true, null), 0);
        assertEquals(expectedWidth, dtp.getWrappingWidth(false, null), 0);

        p.setLeftMargin(36); // 0.5"
        leftMargin = p.getLeftMargin();
        assertEquals(36.0, leftMargin, 0);
        expectedWidth = anchor.getWidth() - leftInset - rightInset - leftMargin;
        assertEquals(249.6, expectedWidth, 1E-5); // 300 - 7.2 - 7.2 - 36
        assertEquals(expectedWidth, dtp.getWrappingWidth(true, null), 0);
        assertEquals(expectedWidth, dtp.getWrappingWidth(false, null), 0);

        // increase insets, the wrapping width should get smaller
        sh.setLeftInset(10);
        sh.setRightInset(10);
        leftInset = sh.getLeftInset();
        rightInset = sh.getRightInset();
        assertEquals(10.0, leftInset, 0);
        assertEquals(10.0, rightInset, 0);
        expectedWidth = anchor.getWidth() - leftInset - rightInset - leftMargin;
        assertEquals(244.0, expectedWidth, 0); // 300 - 10 - 10 - 36
        assertEquals(expectedWidth, dtp.getWrappingWidth(true, null), 0);
        assertEquals(expectedWidth, dtp.getWrappingWidth(false, null), 0);

        // set a positive indent of a 0.5 inch. This means "First Line" indentation:
        // |<---  indent -->|Here goes first line of the text
        // Here go other lines (second and subsequent)

        p.setIndent(36.0);  // 0.5"
        indent = p.getIndent();
        assertEquals(36.0, indent, 0);
        expectedWidth = anchor.getWidth() - leftInset - rightInset - leftMargin - indent;
        assertEquals(208.0, expectedWidth, 0); // 300 - 10 - 10 - 36 - 6.4
        assertEquals(expectedWidth, dtp.getWrappingWidth(true, null), 0); // first line is indented
        // other lines are not indented
        expectedWidth = anchor.getWidth() - leftInset - rightInset - leftMargin;
        assertEquals(244.0, expectedWidth, 0); // 300 - 10 - 10 - 36
        assertEquals(expectedWidth, dtp.getWrappingWidth(false, null), 0);

        // set a negative indent of a 1 inch. This means "Hanging" indentation:
        // Here goes first line of the text
        // |<---  indent -->|Here go other lines (second and subsequent)
        p.setIndent(-72.0);  // 1"
        indent = p.getIndent();
        assertEquals(-72.0, indent, 0);
        expectedWidth = anchor.getWidth() - leftInset - rightInset;
        assertEquals(280.0, expectedWidth, 0); // 300 - 10 - 10 
        assertEquals(expectedWidth, dtp.getWrappingWidth(true, null), 0); // first line is NOT indented
        // other lines are indented by leftMargin (the value of indent is not used)
        expectedWidth = anchor.getWidth() - leftInset - rightInset - leftMargin;
        assertEquals(244.0, expectedWidth, 0); // 300 - 10 - 10 - 36 
        assertEquals(expectedWidth, dtp.getWrappingWidth(false, null), 0);
     }

    /**
     * test breaking test into lines.
     * This test requires that the Arial font is available and will run only on windows
     */
    @Test
    public void testBreakLines(){
        String os = System.getProperty("os.name");
        if(os == null || !os.contains("Windows")) {
            _logger.log(POILogger.WARN, "Skipping testBreakLines(), it is executed only on Windows machines");
            return;
        }

        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();
        XSLFTextShape sh = slide.createAutoShape();

        XSLFTextParagraph p = sh.addNewTextParagraph();
        XSLFTextRun r = p.addNewTextRun();
        r.setFontFamily("Arial"); // this should always be available
        r.setFontSize(12);
        r.setText(
                "Paragraph formatting allows for more granular control " +
                "of text within a shape. Properties here apply to all text " +
                "residing within the corresponding paragraph.");

        sh.setAnchor(new Rectangle(50, 50, 300, 200));
        DrawTextParagraphProxy dtp = new DrawTextParagraphProxy(p);

        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();

        List<DrawTextFragment> lines;
        dtp.breakText(graphics);
        lines = dtp.getLines();
        assertEquals(4, lines.size());

        // descrease the shape width from 300 pt to 100 pt
        sh.setAnchor(new Rectangle(50, 50, 100, 200));
        dtp.breakText(graphics);
        lines = dtp.getLines();
        assertEquals(12, lines.size());

        // descrease the shape width from 300 pt to 100 pt
        sh.setAnchor(new Rectangle(50, 50, 600, 200));
        dtp.breakText(graphics);
        lines = dtp.getLines();
        assertEquals(2, lines.size());

        // set left and right margins to 200pt. This leaves 200pt for wrapping text
        sh.setLeftInset(200);
        sh.setRightInset(200);
        dtp.breakText(graphics);
        lines = dtp.getLines();
        assertEquals(5, lines.size());

        r.setText("Apache POI");
        dtp.breakText(graphics);
        lines = dtp.getLines();
        assertEquals(1, lines.size());
        assertEquals("Apache POI", lines.get(0).getString());

        r.setText("Apache\nPOI");
        dtp.breakText(graphics);
        lines = dtp.getLines();
        assertEquals(2, lines.size());
        assertEquals("Apache", lines.get(0).getString());
        assertEquals("POI", lines.get(1).getString());

        // trailing newlines are ignored
        r.setText("Apache\nPOI\n");
        dtp.breakText(graphics);
        lines = dtp.getLines();
        assertEquals(2, lines.size());
        assertEquals("Apache", lines.get(0).getString());
        assertEquals("POI", lines.get(1).getString());

        XSLFAutoShape sh2 = slide.createAutoShape();
        sh2.setAnchor(new Rectangle(50, 50, 300, 200));
        XSLFTextParagraph p2 = sh2.addNewTextParagraph();
        XSLFTextRun r2 = p2.addNewTextRun();
        r2.setFontFamily("serif"); // this should always be available
        r2.setFontSize(30);
        r2.setText("Apache\n");
        XSLFTextRun r3 = p2.addNewTextRun();
        r3.setFontFamily("serif"); // this should always be available
        r3.setFontSize(10);
        r3.setText("POI");
        dtp.breakText(graphics);
        lines = dtp.getLines();
        assertEquals(2, lines.size());
        assertEquals("Apache", lines.get(0).getString());
        assertEquals("POI", lines.get(1).getString());
        // the first line is at least two times higher than the second
        assertTrue(lines.get(0).getHeight() > lines.get(1).getHeight()*2);

    }

    @Test
    public void testThemeInheritance(){
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("prProps.pptx");
        XSLFShape[] shapes = ppt.getSlides()[0].getShapes();
        XSLFTextShape sh1 = (XSLFTextShape)shapes[0];
        assertEquals("Apache", sh1.getText());
        assertEquals(TextAlign.CENTER, sh1.getTextParagraphs().get(0).getTextAlign());
        XSLFTextShape sh2 = (XSLFTextShape)shapes[1];
        assertEquals("Software", sh2.getText());
        assertEquals(TextAlign.CENTER, sh2.getTextParagraphs().get(0).getTextAlign());
        XSLFTextShape sh3 = (XSLFTextShape)shapes[2];
        assertEquals("Foundation", sh3.getText());
        assertEquals(TextAlign.CENTER, sh3.getTextParagraphs().get(0).getTextAlign());
    }

    @Test
    public void testParagraphProperties(){
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();
        XSLFTextShape sh = slide.createAutoShape();

        XSLFTextParagraph p = sh.addNewTextParagraph();
        assertFalse(p.isBullet());
        p.setBullet(true);
        assertTrue(p.isBullet());

        assertEquals("\u2022", p.getBulletCharacter());
        p.setBulletCharacter("*");
        assertEquals("*", p.getBulletCharacter());

        assertEquals("Arial", p.getBulletFont());
        p.setBulletFont("Calibri");
        assertEquals("Calibri", p.getBulletFont());

        assertEquals(null, p.getBulletFontColor());
        p.setBulletFontColor(Color.red);
        assertEquals(Color.red, p.getBulletFontColor());

        assertEquals(100.0, p.getBulletFontSize(), 0);
        p.setBulletFontSize(200.);
        assertEquals(200., p.getBulletFontSize(), 0);
        p.setBulletFontSize(-20.);
        assertEquals(-20.0, p.getBulletFontSize(), 0);

        assertEquals(72.0, p.getDefaultTabSize(), 0);
        
        assertEquals(0.0, p.getIndent(), 0);
        p.setIndent(72.0);
        assertEquals(72.0, p.getIndent(), 0);
        p.setIndent(-1.0); // the value of -1.0 resets to the defaults
        assertEquals(0.0, p.getIndent(), 0);

        assertEquals(0.0, p.getLeftMargin(), 0);
        p.setLeftMargin(72.0);
        assertEquals(72.0, p.getLeftMargin(), 0);
        p.setLeftMargin(-1.0); // the value of -1.0 resets to the defaults
        assertEquals(0.0, p.getLeftMargin(), 0);

        assertEquals(0, p.getLevel());
        p.setLevel(1);
        assertEquals(1, p.getLevel());
        p.setLevel(2);
        assertEquals(2, p.getLevel());

        assertEquals(100., p.getLineSpacing(), 0);
        p.setLineSpacing(200.);
        assertEquals(200.0, p.getLineSpacing(), 0);
        p.setLineSpacing(-15.);
        assertEquals(-15.0, p.getLineSpacing(), 0);

        assertEquals(0., p.getSpaceAfter(), 0);
        p.setSpaceAfter(200.);
        assertEquals(200.0, p.getSpaceAfter(), 0);
        p.setSpaceAfter(-15.);
        assertEquals(-15.0, p.getSpaceAfter(), 0);

        assertEquals(0., p.getSpaceBefore(), 0);
        p.setSpaceBefore(200.);
        assertEquals(200.0, p.getSpaceBefore(), 0);
        p.setSpaceBefore(-15.);
        assertEquals(-15.0, p.getSpaceBefore(), 0);

        assertEquals(TextAlign.LEFT, p.getTextAlign());
        p.setTextAlign(TextAlign.RIGHT);
        assertEquals(TextAlign.RIGHT, p.getTextAlign());

        p.setBullet(false);
        assertFalse(p.isBullet());

        p.setBulletAutoNumber(ListAutoNumber.ALPHA_LC_PARENT_BOTH, 1);

        double tabStop = p.getTabStop(0);
        assertEquals(0.0, tabStop, 0);

        p.addTabStop(100.);
        assertEquals(100., p.getTabStop(0), 0);

        assertEquals(72.0, p.getDefaultTabSize(), 0);

    }

    @Test
    public void testLineBreak(){
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();
        XSLFTextShape sh = slide.createAutoShape();

        XSLFTextParagraph p = sh.addNewTextParagraph();
        XSLFTextRun r1 = p.addNewTextRun();
        r1.setText("Hello,");
        XSLFTextRun r2 = p.addLineBreak();
        assertEquals("\n", r2.getText());
        r2.setFontSize(10.0);
        assertEquals(10.0, r2.getFontSize(), 0);
        XSLFTextRun r3 = p.addNewTextRun();
        r3.setText("World!");
        r3.setFontSize(20.0);
        XSLFTextRun r4 = p.addLineBreak();
        assertEquals(20.0, r4.getFontSize(), 0);

        assertEquals("Hello,\nWorld!\n",sh.getText());

        try {
            r2.setText("aaa");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e){
            assertEquals("You cannot change text of a line break, it is always '\\n'", e.getMessage());
        }
    }
}
