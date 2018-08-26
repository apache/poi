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

import static org.apache.poi.sl.TestCommonSL.sameColor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.apache.poi.xslf.usermodel.TestXSLFSimpleShape.getSpPr;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.sl.usermodel.TextParagraph.TextAlign;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.xddf.usermodel.text.XDDFBodyProperties;
import org.apache.poi.xddf.usermodel.text.XDDFTextBody;
import org.apache.poi.xslf.XSLFTestDataSamples;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBodyProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextAlignType;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.STPlaceholderType;

public class TestXSLFTextShape {
    private static boolean xslfOnly;

    @BeforeClass
    public static void checkHslf() {
        try {
            Class.forName("org.apache.poi.hslf.usermodel.HSLFSlideShow");
        } catch (Exception e) {
            xslfOnly = true;
        }
    }

    @Test
    public void testLayouts() throws IOException {
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("layouts.pptx");

        List<XSLFSlide> slide = ppt.getSlides();

        verifySlide1(slide.get(0));
        verifySlide2(slide.get(1));
        verifySlide3(slide.get(2));
        verifySlide4(slide.get(3));
        verifySlide7(slide.get(6));
        verifySlide8(slide.get(7));
        verifySlide10(slide.get(9));
        
        ppt.close();
    }

    void verifySlide1(XSLFSlide slide){
        XSLFSlideLayout layout = slide.getSlideLayout();
        List<XSLFShape> shapes = slide.getShapes();
        assertEquals("Title Slide",layout.getName());

        XSLFTextShape shape1 = (XSLFTextShape)shapes.get(0);
        XDDFTextBody tb1 = shape1.getTextBody();
        XDDFBodyProperties tbp1 = tb1.getBodyProperties();
        CTPlaceholder ph1 = shape1.getPlaceholderDetails().getCTPlaceholder(false);
        assertEquals(STPlaceholderType.CTR_TITLE, ph1.getType());
        // anchor is not defined in the shape
        assertNull(getSpPr(shape1).getXfrm());

        XSLFTextShape masterShape1 = (XSLFTextShape)layout.getPlaceholder(ph1);
        assertNotNull(getSpPr(masterShape1).getXfrm());
        assertEquals(masterShape1.getAnchor(), shape1.getAnchor());

        CTTextBodyProperties bodyPr1 = shape1.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr1.isSetLIns() && !bodyPr1.isSetRIns() &&
                !bodyPr1.isSetBIns() && !bodyPr1.isSetTIns() &&
                !bodyPr1.isSetAnchor()
        );
        assertEquals(7.2, shape1.getLeftInset(), 0);  // 0.1"
        assertEquals(7.2, shape1.getRightInset(), 0); // 0.1"
        assertEquals(3.6, shape1.getTopInset(), 0);  // 0.05"
        assertEquals(3.6, shape1.getBottomInset(), 0); // 0.05"
        assertEquals(VerticalAlignment.MIDDLE, shape1.getVerticalAlignment());
        assertNull(tbp1.getLeftInset());
        assertNull(tbp1.getRightInset());
        assertNull(tbp1.getBottomInset());
        assertNull(tbp1.getTopInset());
        assertNull(tbp1.getAnchoring());

        // now check text properties
        assertEquals("Centered Title", shape1.getText());
        assertEquals("Centered Title",
            tb1.getParagraphs().stream().map(p -> p.getText()).collect(Collectors.joining("\n")));
        XSLFTextRun r1 = shape1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals("Calibri", r1.getFontFamily());
        assertEquals(44.0, r1.getFontSize(), 0);
        assertTrue(sameColor(Color.black, r1.getFontColor()));

        XSLFTextShape shape2 = (XSLFTextShape)shapes.get(1);
        XDDFTextBody tb2 = shape2.getTextBody();
        XDDFBodyProperties tbp2 = tb2.getBodyProperties();
        CTPlaceholder ph2 = shape2.getPlaceholderDetails().getCTPlaceholder(false);
        assertEquals(STPlaceholderType.SUB_TITLE, ph2.getType());
        // anchor is not defined in the shape
        assertNull(getSpPr(shape2).getXfrm());

        XSLFTextShape masterShape2 = (XSLFTextShape)layout.getPlaceholder(ph2);
        assertNotNull(getSpPr(masterShape2).getXfrm());
        assertEquals(masterShape2.getAnchor(), shape2.getAnchor());

        CTTextBodyProperties bodyPr2 = shape2.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr2.isSetLIns() && !bodyPr2.isSetRIns() &&
                !bodyPr2.isSetBIns() && !bodyPr2.isSetTIns() &&
                !bodyPr2.isSetAnchor()
        );
        assertEquals(7.2, shape2.getLeftInset(), 0);  // 0.1"
        assertEquals(7.2, shape2.getRightInset(), 0); // 0.1"
        assertEquals(3.6, shape2.getTopInset(), 0);  // 0.05"
        assertEquals(3.6, shape2.getBottomInset(), 0); // 0.05"
        assertEquals(VerticalAlignment.TOP, shape2.getVerticalAlignment());
        assertNull(tbp2.getLeftInset());
        assertNull(tbp2.getRightInset());
        assertNull(tbp2.getBottomInset());
        assertNull(tbp2.getTopInset());
        assertNull(tbp2.getAnchoring());

        assertEquals("subtitle", shape2.getText());
        assertEquals("subtitle", tb2.getParagraphs().stream().map(p -> p.getText()).collect(Collectors.joining("\n")));
        XSLFTextRun r2 = shape2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals("Calibri", r2.getFontFamily());
        assertEquals(32.0, r2.getFontSize(), 0);
        // TODO fix calculation of tint
        //assertEquals(new Color(137, 137, 137), r2.getFontColor());
    }

    void verifySlide2(XSLFSlide slide){
        XSLFSlideLayout layout = slide.getSlideLayout();
        List<XSLFShape> shapes = slide.getShapes();
        assertEquals("Title and Content",layout.getName());

        XSLFTextShape shape1 = (XSLFTextShape)shapes.get(0);
        CTPlaceholder ph1 = shape1.getPlaceholderDetails().getCTPlaceholder(false);
        assertEquals(STPlaceholderType.TITLE, ph1.getType());
        // anchor is not defined in the shape
        assertNull(getSpPr(shape1).getXfrm());

        XSLFTextShape masterShape1 = (XSLFTextShape)layout.getPlaceholder(ph1);
        // layout does not have anchor info either, it is in the slide master
        assertNull(getSpPr(masterShape1).getXfrm());
        masterShape1 = (XSLFTextShape)layout.getSlideMaster().getPlaceholder(ph1);
        assertNotNull(getSpPr(masterShape1).getXfrm());
        assertEquals(masterShape1.getAnchor(), shape1.getAnchor());

        CTTextBodyProperties bodyPr1 = shape1.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr1.isSetLIns() && !bodyPr1.isSetRIns() &&
                !bodyPr1.isSetBIns() && !bodyPr1.isSetTIns() &&
                !bodyPr1.isSetAnchor()
        );
        assertEquals(7.2, shape1.getLeftInset(), 0);  // 0.1"
        assertEquals(7.2, shape1.getRightInset(), 0); // 0.1"
        assertEquals(3.6, shape1.getTopInset(), 0);  // 0.05"
        assertEquals(3.6, shape1.getBottomInset(), 0); // 0.05"
        assertEquals(VerticalAlignment.MIDDLE, shape1.getVerticalAlignment());

        // now check text properties
        assertEquals("Title", shape1.getText());
        XSLFTextRun r1 = shape1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals("Calibri", r1.getFontFamily());
        assertEquals(44.0, r1.getFontSize(), 0);
        assertTrue(sameColor(Color.black, r1.getFontColor()));

        XSLFTextShape shape2 = (XSLFTextShape)shapes.get(1);
        CTPlaceholder ph2 = shape2.getPlaceholderDetails().getCTPlaceholder(false);
        assertFalse(ph2.isSetType()); // <p:ph idx="1"/>
        assertTrue(ph2.isSetIdx());
        assertEquals(1, ph2.getIdx());
        // anchor is not defined in the shape
        assertNull(getSpPr(shape2).getXfrm());

        XSLFTextShape masterShape2 = (XSLFTextShape)layout.getPlaceholder(ph2);
        // anchor of the body text is missing in the slide layout, llokup in the slide master
        assertNull(getSpPr(masterShape2).getXfrm());
        masterShape2 = (XSLFTextShape)layout.getSlideMaster().getPlaceholder(ph2);
        assertNotNull(getSpPr(masterShape2).getXfrm());
        assertEquals(masterShape2.getAnchor(), shape2.getAnchor());

        CTTextBodyProperties bodyPr2 = shape2.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr2.isSetLIns() && !bodyPr2.isSetRIns() &&
                !bodyPr2.isSetBIns() && !bodyPr2.isSetTIns() &&
                !bodyPr2.isSetAnchor()
        );
        assertEquals(7.2, shape2.getLeftInset(), 0);  // 0.1"
        assertEquals(7.2, shape2.getRightInset(), 0); // 0.1"
        assertEquals(3.6, shape2.getTopInset(), 0);  // 0.05"
        assertEquals(3.6, shape2.getBottomInset(), 0); // 0.05"
        assertEquals(VerticalAlignment.TOP, shape2.getVerticalAlignment());

        XSLFTextRun pr1 = shape2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(0, pr1.getParentParagraph().getIndentLevel());
        assertEquals("Content", pr1.getRawText());
        assertEquals("Calibri", pr1.getFontFamily());
        assertEquals(32.0, pr1.getFontSize(), 0);
        assertEquals(27.0, pr1.getParentParagraph().getLeftMargin(), 0); 
        assertEquals("\u2022", pr1.getParentParagraph().getBulletCharacter()); 
        assertEquals("Arial", pr1.getParentParagraph().getBulletFont());

        XSLFTextRun pr2 = shape2.getTextParagraphs().get(1).getTextRuns().get(0);
        assertEquals(1, pr2.getParentParagraph().getIndentLevel());
        assertEquals("Level 2", pr2.getRawText());
        assertEquals("Calibri", pr2.getFontFamily());
        assertEquals(28.0, pr2.getFontSize(), 0);
        assertEquals(58.5, pr2.getParentParagraph().getLeftMargin(), 0);
        assertEquals("\u2013", pr2.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr2.getParentParagraph().getBulletFont());

        XSLFTextRun pr3 = shape2.getTextParagraphs().get(2).getTextRuns().get(0);
        assertEquals(2, pr3.getParentParagraph().getIndentLevel());
        assertEquals("Level 3", pr3.getRawText());
        assertEquals("Calibri", pr3.getFontFamily());
        assertEquals(24.0, pr3.getFontSize(), 0);
        assertEquals(90.0, pr3.getParentParagraph().getLeftMargin(), 0);
        assertEquals("\u2022", pr3.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr3.getParentParagraph().getBulletFont());

        XSLFTextRun pr4 = shape2.getTextParagraphs().get(3).getTextRuns().get(0);
        assertEquals(3, pr4.getParentParagraph().getIndentLevel());
        assertEquals("Level 4", pr4.getRawText());
        assertEquals("Calibri", pr4.getFontFamily());
        assertEquals(20.0, pr4.getFontSize(), 0);
        assertEquals(126.0, pr4.getParentParagraph().getLeftMargin(), 0);
        assertEquals("\u2013", pr4.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr4.getParentParagraph().getBulletFont());

        XSLFTextRun pr5 = shape2.getTextParagraphs().get(4).getTextRuns().get(0);
        assertEquals(4, pr5.getParentParagraph().getIndentLevel());
        assertEquals("Level 5", pr5.getRawText());
        assertEquals("Calibri", pr5.getFontFamily());
        assertEquals(20.0, pr5.getFontSize(), 0);
        assertEquals(162.0, pr5.getParentParagraph().getLeftMargin(), 0);
        assertEquals("\u00bb", pr5.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr5.getParentParagraph().getBulletFont());

    }

    void verifySlide3(XSLFSlide slide){
        XSLFSlideLayout layout = slide.getSlideLayout();
        List<XSLFShape> shapes = slide.getShapes();
        assertEquals("Section Header",layout.getName());

        XSLFTextShape shape1 = (XSLFTextShape)shapes.get(0);
        CTPlaceholder ph1 = shape1.getPlaceholderDetails().getCTPlaceholder(false);
        assertEquals(STPlaceholderType.TITLE, ph1.getType());
        // anchor is not defined in the shape
        assertNull(getSpPr(shape1).getXfrm());

        XSLFTextShape masterShape1 = (XSLFTextShape)layout.getPlaceholder(ph1);
        assertNotNull(getSpPr(masterShape1).getXfrm());
        assertEquals(masterShape1.getAnchor(), shape1.getAnchor());

        CTTextBodyProperties bodyPr1 = shape1.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr1.isSetLIns() && !bodyPr1.isSetRIns() &&
                !bodyPr1.isSetBIns() && !bodyPr1.isSetTIns() &&
                !bodyPr1.isSetAnchor()
        );
        assertEquals(7.2, shape1.getLeftInset(), 0);  // 0.1"
        assertEquals(7.2, shape1.getRightInset(), 0); // 0.1"
        assertEquals(3.6, shape1.getTopInset(), 0);  // 0.05"
        assertEquals(3.6, shape1.getBottomInset(), 0); // 0.05"
        assertEquals(VerticalAlignment.TOP, shape1.getVerticalAlignment());

        // now check text properties
        assertEquals("Section Title", shape1.getText());
        XSLFTextRun r1 = shape1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(TextAlign.LEFT, r1.getParentParagraph().getTextAlign());
        assertEquals("Calibri", r1.getFontFamily());
        assertEquals(40.0, r1.getFontSize(), 0);
        assertTrue(sameColor(Color.black, r1.getFontColor()));
        assertTrue(r1.isBold());
        assertFalse(r1.isItalic());
        assertFalse(r1.isUnderlined());

        XSLFTextShape shape2 = (XSLFTextShape)shapes.get(1);
        CTPlaceholder ph2 = shape2.getPlaceholderDetails().getCTPlaceholder(false);
        assertEquals(STPlaceholderType.BODY, ph2.getType());
        // anchor is not defined in the shape
        assertNull(getSpPr(shape2).getXfrm());

        XSLFTextShape masterShape2 = (XSLFTextShape)layout.getPlaceholder(ph2);
        assertNotNull(getSpPr(masterShape2).getXfrm());
        assertEquals(masterShape2.getAnchor(), shape2.getAnchor());

        CTTextBodyProperties bodyPr2 = shape2.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr2.isSetLIns() && !bodyPr2.isSetRIns() &&
                !bodyPr2.isSetBIns() && !bodyPr2.isSetTIns() &&
                !bodyPr2.isSetAnchor()
        );
        assertEquals(7.2, shape2.getLeftInset(), 0);  // 0.1"
        assertEquals(7.2, shape2.getRightInset(), 0); // 0.1"
        assertEquals(3.6, shape2.getTopInset(), 0);  // 0.05"
        assertEquals(3.6, shape2.getBottomInset(), 0); // 0.05"
        assertEquals(VerticalAlignment.BOTTOM, shape2.getVerticalAlignment());

        assertEquals("Section Header", shape2.getText());
        XSLFTextRun r2 = shape2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(TextAlign.LEFT, r2.getParentParagraph().getTextAlign());
        assertEquals("Calibri", r2.getFontFamily());
        assertEquals(20.0, r2.getFontSize(), 0);
        // TODO fix calculation of tint
        //assertEquals(new Color(137, 137, 137), r2.getFontColor());
    }

    void verifySlide4(XSLFSlide slide){
        XSLFSlideLayout layout = slide.getSlideLayout();
        List<XSLFShape> shapes = slide.getShapes();
        assertEquals("Two Content",layout.getName());

        XSLFTextShape shape1 = (XSLFTextShape)shapes.get(0);
        CTPlaceholder ph1 = shape1.getPlaceholderDetails().getCTPlaceholder(false);
        assertEquals(STPlaceholderType.TITLE, ph1.getType());
        // anchor is not defined in the shape
        assertNull(getSpPr(shape1).getXfrm());

        XSLFTextShape masterShape1 = (XSLFTextShape)layout.getPlaceholder(ph1);
        // layout does not have anchor info either, it is in the slide master
        assertNull(getSpPr(masterShape1).getXfrm());
        masterShape1 = (XSLFTextShape)layout.getSlideMaster().getPlaceholder(ph1);
        assertNotNull(getSpPr(masterShape1).getXfrm());
        assertEquals(masterShape1.getAnchor(), shape1.getAnchor());

        CTTextBodyProperties bodyPr1 = shape1.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr1.isSetLIns() && !bodyPr1.isSetRIns() &&
                !bodyPr1.isSetBIns() && !bodyPr1.isSetTIns() &&
                !bodyPr1.isSetAnchor()
        );
        assertEquals(7.2, shape1.getLeftInset(), 0);  // 0.1"
        assertEquals(7.2, shape1.getRightInset(), 0); // 0.1"
        assertEquals(3.6, shape1.getTopInset(), 0);  // 0.05"
        assertEquals(3.6, shape1.getBottomInset(), 0); // 0.05"
        assertEquals(VerticalAlignment.MIDDLE, shape1.getVerticalAlignment());

        // now check text properties
        assertEquals("Title", shape1.getText());
        XSLFTextRun r1 = shape1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(TextAlign.CENTER, r1.getParentParagraph().getTextAlign());
        assertEquals("Calibri", r1.getFontFamily());
        assertEquals(44.0, r1.getFontSize(), 0);
        assertTrue(sameColor(Color.black, r1.getFontColor()));

        XSLFTextShape shape2 = (XSLFTextShape)shapes.get(1);
        CTPlaceholder ph2 = shape2.getPlaceholderDetails().getCTPlaceholder(false);
        assertFalse(ph2.isSetType());
        assertTrue(ph2.isSetIdx());
        assertEquals(1, ph2.getIdx());  //<p:ph sz="half" idx="1"/>
        // anchor is not defined in the shape
        assertNull(getSpPr(shape2).getXfrm());

        XSLFTextShape masterShape2 = (XSLFTextShape)layout.getPlaceholder(ph2);
        assertNotNull(getSpPr(masterShape2).getXfrm());
        assertEquals(masterShape2.getAnchor(), shape2.getAnchor());

        CTTextBodyProperties bodyPr2 = shape2.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr2.isSetLIns() && !bodyPr2.isSetRIns() &&
                !bodyPr2.isSetBIns() && !bodyPr2.isSetTIns() &&
                !bodyPr2.isSetAnchor()
        );
        assertEquals(7.2, shape2.getLeftInset(), 0);  // 0.1"
        assertEquals(7.2, shape2.getRightInset(), 0); // 0.1"
        assertEquals(3.6, shape2.getTopInset(), 0);  // 0.05"
        assertEquals(3.6, shape2.getBottomInset(), 0); // 0.05"
        assertEquals(VerticalAlignment.TOP, shape2.getVerticalAlignment());

        XSLFTextRun pr1 = shape2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(0, pr1.getParentParagraph().getIndentLevel());
        assertEquals("Left", pr1.getRawText());
        assertEquals("Calibri", pr1.getFontFamily());
        assertEquals(28.0, pr1.getFontSize(), 0);
        assertEquals(27.0, pr1.getParentParagraph().getLeftMargin(), 0);
        assertEquals("\u2022", pr1.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr1.getParentParagraph().getBulletFont());

        XSLFTextRun pr2 = shape2.getTextParagraphs().get(1).getTextRuns().get(0);
        assertEquals(1, pr2.getParentParagraph().getIndentLevel());
        assertEquals("Level 2", pr2.getParentParagraph().getText());
        assertEquals("Calibri", pr2.getFontFamily());
        assertEquals(24.0, pr2.getFontSize(), 0);
        assertEquals(58.5, pr2.getParentParagraph().getLeftMargin(), 0);
        assertEquals("\u2013", pr2.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr2.getParentParagraph().getBulletFont());

        XSLFTextRun pr3 = shape2.getTextParagraphs().get(2).getTextRuns().get(0);
        assertEquals(2, pr3.getParentParagraph().getIndentLevel());
        assertEquals("Level 3", pr3.getParentParagraph().getText());
        assertEquals("Calibri", pr3.getFontFamily());
        assertEquals(20.0, pr3.getFontSize(), 0);
        assertEquals(90.0, pr3.getParentParagraph().getLeftMargin(), 0);
        assertEquals("\u2022", pr3.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr3.getParentParagraph().getBulletFont());

        XSLFTextRun pr4 = shape2.getTextParagraphs().get(3).getTextRuns().get(0);
        assertEquals(3, pr4.getParentParagraph().getIndentLevel());
        assertEquals("Level 4", pr4.getParentParagraph().getText());
        assertEquals("Calibri", pr4.getFontFamily());
        assertEquals(18.0, pr4.getFontSize(), 0);
        assertEquals(126.0, pr4.getParentParagraph().getLeftMargin(), 0);
        assertEquals("\u2013", pr4.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr4.getParentParagraph().getBulletFont());

        XSLFTextShape shape3 = (XSLFTextShape)shapes.get(2);
        XSLFTextRun pr5 = shape3.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(0, pr5.getParentParagraph().getIndentLevel());
        assertEquals("Right", pr5.getRawText());
        assertEquals("Calibri", pr5.getFontFamily());
        assertTrue(sameColor(Color.black, pr5.getFontColor()));
    }

    @SuppressWarnings("unused")
    void verifySlide5(XSLFSlide slide){
        XSLFSlideLayout layout = slide.getSlideLayout();
        List<XSLFShape> shapes = slide.getShapes();
        // TODO
    }    

    void verifySlide7(XSLFSlide slide){
        XSLFSlideLayout layout = slide.getSlideLayout();
        List<XSLFShape> shapes = slide.getShapes();
        assertEquals("Blank",layout.getName());

        XSLFTextShape shape1 = (XSLFTextShape)shapes.get(0);
        CTPlaceholder ph1 = shape1.getPlaceholderDetails().getCTPlaceholder(false);
        assertEquals(STPlaceholderType.TITLE, ph1.getType());
        // anchor is not defined in the shape
        assertNull(getSpPr(shape1).getXfrm());

        CTTextBodyProperties bodyPr1 = shape1.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr1.isSetLIns() && !bodyPr1.isSetRIns() &&
                !bodyPr1.isSetBIns() && !bodyPr1.isSetTIns() &&
                !bodyPr1.isSetAnchor()
        );
        assertEquals(7.2, shape1.getLeftInset(), 0);  // 0.1"
        assertEquals(7.2, shape1.getRightInset(), 0); // 0.1"
        assertEquals(3.6, shape1.getTopInset(), 0);  // 0.05"
        assertEquals(3.6, shape1.getBottomInset(), 0); // 0.05"
        assertEquals(VerticalAlignment.MIDDLE, shape1.getVerticalAlignment());

        // now check text properties
        assertEquals("Blank with Default Title", shape1.getText());
        XSLFTextRun r1 = shape1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(TextAlign.CENTER, r1.getParentParagraph().getTextAlign());
        assertEquals("Calibri", r1.getFontFamily());
        assertEquals(44.0, r1.getFontSize(), 0);
        assertTrue(sameColor(Color.black, r1.getFontColor()));
        assertFalse(r1.isBold());

        XSLFTextShape shape2 = (XSLFTextShape)shapes.get(1);

        CTTextBodyProperties bodyPr2 = shape2.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr2.isSetLIns() && !bodyPr2.isSetRIns() &&
                !bodyPr2.isSetBIns() && !bodyPr2.isSetTIns() &&
                !bodyPr2.isSetAnchor()
        );
        assertEquals(7.2, shape2.getLeftInset(), 0);  // 0.1"
        assertEquals(7.2, shape2.getRightInset(), 0); // 0.1"
        assertEquals(3.6, shape2.getTopInset(), 0);  // 0.05"
        assertEquals(3.6, shape2.getBottomInset(), 0); // 0.05"
        assertEquals(VerticalAlignment.TOP, shape2.getVerticalAlignment());

        XSLFTextRun pr1 = shape2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(0, pr1.getParentParagraph().getIndentLevel());
        assertEquals("Default Text", pr1.getRawText());
        assertEquals("Calibri", pr1.getFontFamily());
        assertEquals(18.0, pr1.getFontSize(), 0);

        XSLFTextShape shape3 = (XSLFTextShape)shapes.get(2);
        assertEquals("Default", shape3.getTextParagraphs().get(0).getText());
        assertEquals("Text with levels", shape3.getTextParagraphs().get(1).getText());
        assertEquals("Level 1", shape3.getTextParagraphs().get(2).getText());
        assertEquals("Level 2", shape3.getTextParagraphs().get(3).getText());
        assertEquals("Level 3", shape3.getTextParagraphs().get(4).getText());

        for(int p = 0; p < 5; p++) {
            XSLFTextParagraph pr = shape3.getTextParagraphs().get(p);
            assertEquals("Calibri", pr.getTextRuns().get(0).getFontFamily());
            assertEquals(18.0, pr.getTextRuns().get(0).getFontSize(), 0);
        }
    }

    void verifySlide8(XSLFSlide slide){
        XSLFSlideLayout layout = slide.getSlideLayout();
        List<XSLFShape> shapes = slide.getShapes();
        assertEquals("Content with Caption",layout.getName());

        XSLFTextShape shape1 = (XSLFTextShape)shapes.get(0);
        CTPlaceholder ph1 = shape1.getPlaceholderDetails().getCTPlaceholder(false);
        assertEquals(STPlaceholderType.TITLE, ph1.getType());
        // anchor is not defined in the shape
        assertNull(getSpPr(shape1).getXfrm());

        XSLFTextShape masterShape1 = (XSLFTextShape)layout.getPlaceholder(ph1);
        // layout does not have anchor info either, it is in the slide master
        assertNotNull(getSpPr(masterShape1).getXfrm());
        assertEquals(masterShape1.getAnchor(), shape1.getAnchor());

        CTTextBodyProperties bodyPr1 = shape1.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr1.isSetLIns() && !bodyPr1.isSetRIns() &&
                !bodyPr1.isSetBIns() && !bodyPr1.isSetTIns() &&
                !bodyPr1.isSetAnchor()
        );
        assertEquals(7.2, shape1.getLeftInset(), 0);  // 0.1"
        assertEquals(7.2, shape1.getRightInset(), 0); // 0.1"
        assertEquals(3.6, shape1.getTopInset(), 0);  // 0.05"
        assertEquals(3.6, shape1.getBottomInset(), 0); // 0.05"
        assertEquals(VerticalAlignment.BOTTOM, shape1.getVerticalAlignment());

        // now check text properties
        assertEquals("Caption", shape1.getText());
        XSLFTextRun r1 = shape1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(TextAlign.LEFT, r1.getParentParagraph().getTextAlign());
        assertEquals("Calibri", r1.getFontFamily());
        assertEquals(20.0, r1.getFontSize(), 0);
        assertTrue(sameColor(Color.black, r1.getFontColor()));
        assertTrue(r1.isBold());

        XSLFTextShape shape2 = (XSLFTextShape)shapes.get(1);
        CTPlaceholder ph2 = shape2.getPlaceholderDetails().getCTPlaceholder(false);
        assertFalse(ph2.isSetType());
        assertTrue(ph2.isSetIdx());
        assertEquals(1, ph2.getIdx());
        // anchor is not defined in the shape
        assertNull(getSpPr(shape2).getXfrm());

        XSLFTextShape masterShape2 = (XSLFTextShape)layout.getPlaceholder(ph2);
        assertNotNull(getSpPr(masterShape2).getXfrm());
        assertEquals(masterShape2.getAnchor(), shape2.getAnchor());

        CTTextBodyProperties bodyPr2 = shape2.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr2.isSetLIns() && !bodyPr2.isSetRIns() &&
                !bodyPr2.isSetBIns() && !bodyPr2.isSetTIns() &&
                !bodyPr2.isSetAnchor()
        );
        assertEquals(7.2, shape2.getLeftInset(), 0);  // 0.1"
        assertEquals(7.2, shape2.getRightInset(), 0); // 0.1"
        assertEquals(3.6, shape2.getTopInset(), 0);  // 0.05"
        assertEquals(3.6, shape2.getBottomInset(), 0); // 0.05"
        assertEquals(VerticalAlignment.TOP, shape2.getVerticalAlignment());

        XSLFTextRun pr1 = shape2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(0, pr1.getParentParagraph().getIndentLevel());
        assertEquals("Level 1", pr1.getRawText());
        assertEquals("Calibri", pr1.getFontFamily());
        assertEquals(32.0, pr1.getFontSize(), 0);
        assertEquals(27.0, pr1.getParentParagraph().getLeftMargin(), 0);
        assertEquals("\u2022", pr1.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr1.getParentParagraph().getBulletFont());

        XSLFTextRun pr2 = shape2.getTextParagraphs().get(1).getTextRuns().get(0);
        assertEquals(1, pr2.getParentParagraph().getIndentLevel());
        assertEquals("Level 2", pr2.getParentParagraph().getText());
        assertEquals("Calibri", pr2.getFontFamily());
        assertEquals(28.0, pr2.getFontSize(), 0);
        assertEquals(58.5, pr2.getParentParagraph().getLeftMargin(), 0);
        assertEquals("\u2013", pr2.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr2.getParentParagraph().getBulletFont());

        XSLFTextRun pr3 = shape2.getTextParagraphs().get(2).getTextRuns().get(0);
        assertEquals(2, pr3.getParentParagraph().getIndentLevel());
        assertEquals("Level 3", pr3.getParentParagraph().getText());
        assertEquals("Calibri", pr3.getFontFamily());
        assertEquals(24.0, pr3.getFontSize(), 0);
        assertEquals(90.0, pr3.getParentParagraph().getLeftMargin(), 0);
        assertEquals("\u2022", pr3.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr3.getParentParagraph().getBulletFont());

        XSLFTextRun pr4 = shape2.getTextParagraphs().get(3).getTextRuns().get(0);
        assertEquals(3, pr4.getParentParagraph().getIndentLevel());
        assertEquals("Level 4", pr4.getParentParagraph().getText());
        assertEquals("Calibri", pr4.getFontFamily());
        assertEquals(20.0, pr4.getFontSize(), 0);
        assertEquals(126.0, pr4.getParentParagraph().getLeftMargin(), 0);
        assertEquals("\u2013", pr4.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr4.getParentParagraph().getBulletFont());

        XSLFTextShape shape3 = (XSLFTextShape)shapes.get(2);
        assertEquals(VerticalAlignment.TOP, shape3.getVerticalAlignment());
        assertEquals("Content with caption", shape3.getText());

        pr1 = shape3.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(0, pr1.getParentParagraph().getIndentLevel());
        assertEquals("Content with caption", pr1.getRawText());
        assertEquals("Calibri", pr1.getFontFamily());
        assertEquals(14.0, pr1.getFontSize(), 0);

    }

    void verifySlide10(XSLFSlide slide){
        XSLFTextShape footer = (XSLFTextShape)slide.getPlaceholderByType(STPlaceholderType.INT_FTR);

        // now check text properties
        assertEquals("Apache Software Foundation", footer.getText());
        assertEquals(VerticalAlignment.MIDDLE, footer.getVerticalAlignment());

        XSLFTextRun r1 = footer.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(TextAlign.CENTER, r1.getParentParagraph().getTextAlign());
        assertEquals("Calibri", r1.getFontFamily());
        assertEquals(12.0, r1.getFontSize(), 0);
        // TODO calculation of tint might be incorrect
        assertTrue(sameColor(new Color(191,191,191), r1.getFontColor()));

        XSLFTextShape dt = (XSLFTextShape)slide.getPlaceholderByType(STPlaceholderType.INT_DT);
        assertEquals("Friday, October 21, 2011", dt.getText());

        XSLFTextShape sldNum = (XSLFTextShape)slide.getPlaceholderByType(STPlaceholderType.INT_SLD_NUM);
        assertEquals("10", sldNum.getText());
    }

    @Test
    public void testTitleStyles() throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();

        XSLFSlideMaster master = ppt.getSlideMasters().get(0);
        XSLFTheme theme = master.getTheme();
        XSLFSlideLayout layout = master.getLayout(SlideLayout.TITLE);
        XSLFSlide slide = ppt.createSlide(layout) ;
        assertSame(layout, slide.getSlideLayout());
        assertSame(master, slide.getSlideMaster());

        XSLFTextShape titleShape = slide.getPlaceholder(0);
        titleShape.setText("Apache POI");
        XSLFTextParagraph paragraph = titleShape.getTextParagraphs().get(0);
        XSLFTextRun textRun = paragraph.getTextRuns().get(0);

        // level 1 : default title style on the master slide
        // /p:sldMaster/p:txStyles/p:titleStyle/a:lvl1pPr
        CTTextParagraphProperties lv1PPr = master.getXmlObject().getTxStyles().getTitleStyle().getLvl1PPr();
        CTTextCharacterProperties lv1CPr = lv1PPr.getDefRPr();
        assertEquals(4400, lv1CPr.getSz());
        assertEquals(44.0, textRun.getFontSize(), 0);
        assertEquals("+mj-lt", lv1CPr.getLatin().getTypeface());
        assertEquals("Calibri", theme.getMajorFont());
        assertEquals("Calibri", textRun.getFontFamily());
        lv1CPr.setSz(3200);
        assertEquals(32.0, textRun.getFontSize(), 0);
        lv1CPr.getLatin().setTypeface("Arial");
        assertEquals("Arial", textRun.getFontFamily());
        assertEquals(STTextAlignType.CTR, lv1PPr.getAlgn());
        assertEquals(TextAlign.CENTER, paragraph.getTextAlign());
        lv1PPr.setAlgn(STTextAlignType.L);
        assertEquals(TextAlign.LEFT, paragraph.getTextAlign());

        // level 2: title placeholder on the master slide
        // /p:sldMaster/p:cSld/p:spTree/p:sp/p:nvPr/p:ph[@type="title"]
        XSLFTextShape tx2 = master.getPlaceholder(0);
        CTTextParagraphProperties lv2PPr = tx2.getTextBody(true).getLstStyle().addNewLvl1PPr();
        CTTextCharacterProperties lv2CPr = lv2PPr.addNewDefRPr();
        lv2CPr.setSz(3300);
        assertEquals(33.0, textRun.getFontSize(), 0);
        lv2CPr.addNewLatin().setTypeface("Times");
        assertEquals("Times", textRun.getFontFamily());
        lv2PPr.setAlgn(STTextAlignType.R);
        assertEquals(TextAlign.RIGHT, paragraph.getTextAlign());


        // level 3: title placeholder on the slide layout
        // /p:sldLayout /p:cSld/p:spTree/p:sp/p:nvPr/p:ph[@type="ctrTitle"]
        XSLFTextShape tx3 = layout.getPlaceholder(0);
        CTTextParagraphProperties lv3PPr = tx3.getTextBody(true).getLstStyle().addNewLvl1PPr();
        CTTextCharacterProperties lv3CPr = lv3PPr.addNewDefRPr();
        lv3CPr.setSz(3400);
        assertEquals(34.0, textRun.getFontSize(), 0);
        lv3CPr.addNewLatin().setTypeface("Courier New");
        assertEquals("Courier New", textRun.getFontFamily());
        lv3PPr.setAlgn(STTextAlignType.CTR);
        assertEquals(TextAlign.CENTER, paragraph.getTextAlign());

        // level 4: default text properties in the shape itself
        // ./p:sp/p:txBody/a:lstStyle/a:lvl1pPr
        CTTextParagraphProperties lv4PPr = titleShape.getTextBody(true).getLstStyle().addNewLvl1PPr();
        CTTextCharacterProperties lv4CPr = lv4PPr.addNewDefRPr();
        lv4CPr.setSz(3500);
        assertEquals(35.0, textRun.getFontSize(), 0);
        lv4CPr.addNewLatin().setTypeface("Arial");
        assertEquals("Arial", textRun.getFontFamily());
        lv4PPr.setAlgn(STTextAlignType.L);
        assertEquals(TextAlign.LEFT, paragraph.getTextAlign());

        // level 5: text properties are defined in the text run
        CTTextParagraphProperties lv5PPr = paragraph.getXmlObject().addNewPPr();
        CTTextCharacterProperties lv5CPr = textRun.getRPr(false);
        lv5CPr.setSz(3600);
        assertEquals(36.0, textRun.getFontSize(), 0);
        lv5CPr.addNewLatin().setTypeface("Calibri");
        assertEquals("Calibri", textRun.getFontFamily());
        lv5PPr.setAlgn(STTextAlignType.CTR);
        assertEquals(TextAlign.CENTER, paragraph.getTextAlign());
        
        ppt.close();
    }

    @Test
    public void testBodyStyles() throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();

        XSLFSlideMaster master = ppt.getSlideMasters().get(0);
        XSLFTheme theme = master.getTheme();
        XSLFSlideLayout layout = master.getLayout(SlideLayout.TITLE_AND_CONTENT);
        XSLFSlide slide = ppt.createSlide(layout) ;
        assertSame(layout, slide.getSlideLayout());
        assertSame(master, slide.getSlideMaster());

        XSLFTextShape tx1 = slide.getPlaceholder(1);
        tx1.clearText();

        XSLFTextParagraph p1 = tx1.addNewTextParagraph();
        assertEquals(0, p1.getIndentLevel());
        XSLFTextRun r1 = p1.addNewTextRun();
        r1.setText("Apache POI");

        XSLFTextParagraph p2 = tx1.addNewTextParagraph();
        p2.setIndentLevel(1);
        assertEquals(1, p2.getIndentLevel());
        XSLFTextRun r2 = p2.addNewTextRun();
        r2.setText("HSLF");

        XSLFTextParagraph p3 = tx1.addNewTextParagraph();
        p3.setIndentLevel(2);
        assertEquals(2, p3.getIndentLevel());
        XSLFTextRun r3 = p3.addNewTextRun();
        r3.setText("XSLF");

        // level 1 : default title style on the master slide
        // /p:sldMaster/p:txStyles/p:bodyStyle/a:lvl1pPr
        CTTextParagraphProperties lv1PPr = master.getXmlObject().getTxStyles().getBodyStyle().getLvl1PPr();
        CTTextCharacterProperties lv1CPr = lv1PPr.getDefRPr();
        CTTextParagraphProperties lv2PPr = master.getXmlObject().getTxStyles().getBodyStyle().getLvl2PPr();
        CTTextCharacterProperties lv2CPr = lv2PPr.getDefRPr();
        CTTextParagraphProperties lv3PPr = master.getXmlObject().getTxStyles().getBodyStyle().getLvl3PPr();
        CTTextCharacterProperties lv3CPr = lv3PPr.getDefRPr();
        // lv1
        assertEquals(3200, lv1CPr.getSz());
        assertEquals(32.0, r1.getFontSize(), 0);
        assertEquals("+mn-lt", lv1CPr.getLatin().getTypeface());
        assertEquals("Calibri", theme.getMinorFont());
        assertEquals("Calibri", r1.getFontFamily());
        lv1CPr.setSz(3300);
        assertEquals(33.0, r1.getFontSize(), 0);
        lv1CPr.getLatin().setTypeface("Arial");
        assertEquals("Arial", r1.getFontFamily());
        assertEquals(STTextAlignType.L, lv1PPr.getAlgn());
        assertEquals(TextAlign.LEFT, p1.getTextAlign());
        lv1PPr.setAlgn(STTextAlignType.R);
        assertEquals(TextAlign.RIGHT, p1.getTextAlign());
        //lv2
        assertEquals(2800, lv2CPr.getSz());
        assertEquals(28.0, r2.getFontSize(), 0);
        lv2CPr.setSz(3300);
        assertEquals(33.0, r2.getFontSize(), 0);
        lv2CPr.getLatin().setTypeface("Times");
        assertEquals("Times", r2.getFontFamily());
        assertEquals(STTextAlignType.L, lv2PPr.getAlgn());
        assertEquals(TextAlign.LEFT, p2.getTextAlign());
        lv2PPr.setAlgn(STTextAlignType.R);
        assertEquals(TextAlign.RIGHT, p2.getTextAlign());
        //lv3
        assertEquals(2400, lv3CPr.getSz());
        assertEquals(24.0, r3.getFontSize(), 0);
        lv3CPr.setSz(2500);
        assertEquals(25.0, r3.getFontSize(), 0);
        lv3CPr.getLatin().setTypeface("Courier New");
        assertEquals("Courier New", r3.getFontFamily());
        assertEquals(STTextAlignType.L, lv3PPr.getAlgn());
        assertEquals(TextAlign.LEFT, p3.getTextAlign());
        lv3PPr.setAlgn(STTextAlignType.R);
        assertEquals(TextAlign.RIGHT, p3.getTextAlign());


        // level 2: body placeholder on the master slide
        // /p:sldMaster/p:cSld/p:spTree/p:sp/p:nvPr/p:ph[@type="body"]
        XSLFTextShape tx2 = master.getPlaceholder(1);
        assertEquals(Placeholder.BODY, tx2.getTextType());

        lv1PPr = tx2.getTextBody(true).getLstStyle().addNewLvl1PPr();
        lv1CPr = lv1PPr.addNewDefRPr();
        lv2PPr = tx2.getTextBody(true).getLstStyle().addNewLvl2PPr();
        lv2CPr = lv2PPr.addNewDefRPr();
        lv3PPr = tx2.getTextBody(true).getLstStyle().addNewLvl3PPr();
        lv3CPr = lv3PPr.addNewDefRPr();

        lv1CPr.setSz(3300);
        assertEquals(33.0, r1.getFontSize(), 0);
        lv1CPr.addNewLatin().setTypeface("Times");
        assertEquals("Times", r1.getFontFamily());
        lv1PPr.setAlgn(STTextAlignType.L);
        assertEquals(TextAlign.LEFT, p1.getTextAlign());

        lv2CPr.setSz(3300);
        assertEquals(33.0, r2.getFontSize(), 0);
        lv2CPr.addNewLatin().setTypeface("Times");
        assertEquals("Times", r2.getFontFamily());
        lv2PPr.setAlgn(STTextAlignType.L);
        assertEquals(TextAlign.LEFT, p2.getTextAlign());

        lv3CPr.setSz(3300);
        assertEquals(33.0, r3.getFontSize(), 0);
        lv3CPr.addNewLatin().setTypeface("Times");
        assertEquals("Times", r3.getFontFamily());
        lv3PPr.setAlgn(STTextAlignType.L);
        assertEquals(TextAlign.LEFT, p3.getTextAlign());

        // level 3: body placeholder on the slide layout
        // /p:sldLayout /p:cSld/p:spTree/p:sp/p:nvPr/p:ph[@type="ctrTitle"]
        XSLFTextShape tx3 = layout.getPlaceholder(1);
        assertEquals(Placeholder.BODY, tx2.getTextType());
        lv1PPr = tx3.getTextBody(true).getLstStyle().addNewLvl1PPr();
        lv1CPr = lv1PPr.addNewDefRPr();
        lv2PPr = tx3.getTextBody(true).getLstStyle().addNewLvl2PPr();
        lv2CPr = lv2PPr.addNewDefRPr();
        lv3PPr = tx3.getTextBody(true).getLstStyle().addNewLvl3PPr();
        lv3CPr = lv3PPr.addNewDefRPr();

        lv1CPr.setSz(3400);
        assertEquals(34.0, r1.getFontSize(), 0);
        lv1CPr.addNewLatin().setTypeface("Courier New");
        assertEquals("Courier New", r1.getFontFamily());
        lv1PPr.setAlgn(STTextAlignType.CTR);
        assertEquals(TextAlign.CENTER, p1.getTextAlign());

        lv2CPr.setSz(3400);
        assertEquals(34.0, r2.getFontSize(), 0);
        lv2CPr.addNewLatin().setTypeface("Courier New");
        assertEquals("Courier New", r2.getFontFamily());
        lv2PPr.setAlgn(STTextAlignType.CTR);
        assertEquals(TextAlign.CENTER, p2.getTextAlign());

        lv3CPr.setSz(3400);
        assertEquals(34.0, r3.getFontSize(), 0);
        lv3CPr.addNewLatin().setTypeface("Courier New");
        assertEquals("Courier New", r3.getFontFamily());
        lv3PPr.setAlgn(STTextAlignType.CTR);
        assertEquals(TextAlign.CENTER, p3.getTextAlign());

        // level 4: default text properties in the shape itself
        // ./p:sp/p:txBody/a:lstStyle/a:lvl1pPr
        lv1PPr = tx1.getTextBody(true).getLstStyle().addNewLvl1PPr();
        lv1CPr = lv1PPr.addNewDefRPr();
        lv2PPr = tx1.getTextBody(true).getLstStyle().addNewLvl2PPr();
        lv2CPr = lv2PPr.addNewDefRPr();
        lv3PPr = tx1.getTextBody(true).getLstStyle().addNewLvl3PPr();
        lv3CPr = lv3PPr.addNewDefRPr();

        lv1CPr.setSz(3500);
        assertEquals(35.0, r1.getFontSize(), 0);
        lv1CPr.addNewLatin().setTypeface("Arial");
        assertEquals("Arial", r1.getFontFamily());
        lv1PPr.setAlgn(STTextAlignType.L);
        assertEquals(TextAlign.LEFT, p1.getTextAlign());

        lv2CPr.setSz(3500);
        assertEquals(35.0, r2.getFontSize(), 0);
        lv2CPr.addNewLatin().setTypeface("Arial");
        assertEquals("Arial", r2.getFontFamily());
        lv2PPr.setAlgn(STTextAlignType.L);
        assertEquals(TextAlign.LEFT, p2.getTextAlign());

        lv3CPr.setSz(3500);
        assertEquals(35.0, r3.getFontSize(), 0);
        lv3CPr.addNewLatin().setTypeface("Arial");
        assertEquals("Arial", r3.getFontFamily());
        lv3PPr.setAlgn(STTextAlignType.L);
        assertEquals(TextAlign.LEFT, p3.getTextAlign());

        // level 5: text properties are defined in the text run
        lv1PPr = p1.getXmlObject().isSetPPr() ? p1.getXmlObject().getPPr() : p1.getXmlObject().addNewPPr();
        lv1CPr = r1.getRPr(false);
        lv2PPr = p2.getXmlObject().isSetPPr() ? p2.getXmlObject().getPPr() : p2.getXmlObject().addNewPPr();
        lv2CPr = r2.getRPr(false);
        lv3PPr = p3.getXmlObject().isSetPPr() ? p3.getXmlObject().getPPr() : p3.getXmlObject().addNewPPr();
        lv3CPr = r3.getRPr(false);

        lv1CPr.setSz(3600);
        assertEquals(36.0, r1.getFontSize(), 0);
        lv1CPr.addNewLatin().setTypeface("Calibri");
        assertEquals("Calibri", r1.getFontFamily());
        lv1PPr.setAlgn(STTextAlignType.CTR);
        assertEquals(TextAlign.CENTER, p1.getTextAlign());

        lv2CPr.setSz(3600);
        assertEquals(36.0, r2.getFontSize(), 0);
        lv2CPr.addNewLatin().setTypeface("Calibri");
        assertEquals("Calibri", r2.getFontFamily());
        lv2PPr.setAlgn(STTextAlignType.CTR);
        assertEquals(TextAlign.CENTER, p2.getTextAlign());

        lv3CPr.setSz(3600);
        assertEquals(36.0, r3.getFontSize(), 0);
        lv3CPr.addNewLatin().setTypeface("Calibri");
        assertEquals("Calibri", r3.getFontFamily());
        lv3PPr.setAlgn(STTextAlignType.CTR);
        assertEquals(TextAlign.CENTER, p3.getTextAlign());
        
        ppt.close();
    }
    
    @Test
    public void metroBlob() throws IOException {
        assumeFalse(xslfOnly);
        File f = POIDataSamples.getSlideShowInstance().getFile("bug52297.ppt");
        SlideShow<?,?> ppt = SlideShowFactory.create(f);
        HSLFTextShape sh = (HSLFTextShape)ppt.getSlides().get(1).getShapes().get(3);
        XSLFAutoShape xsh = (XSLFAutoShape)sh.getMetroShape();
        String textExp = " ___ ___ ___ ________ __  _______ ___  ___________  __________ __ _____ ___ ___ ___ _______ ____ ______ ___________  _____________ ___ _______ ______  ____ ______ __ ___________  __________ ___ _________  _____ ________ __________  ___ _______ __________ ";
        String textAct = xsh.getText();
        ppt.close();
        assertEquals(textExp, textAct);
    }
}