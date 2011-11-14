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

import junit.framework.TestCase;
import org.apache.poi.xslf.XSLFTestDataSamples;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBodyProperties;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.STPlaceholderType;

import java.awt.Color;

/**
 * @author Yegor Kozlov
 */
public class TestXSLFTextShape extends TestCase {

    public void testLayouts(){
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("layouts.pptx");

        XSLFSlide[] slide = ppt.getSlides();

        verifySlide1(slide[0]);
        verifySlide2(slide[1]);
        verifySlide3(slide[2]);
        verifySlide4(slide[3]);
        verifySlide7(slide[6]);
        verifySlide8(slide[7]);
        verifySlide10(slide[9]);
    }

    void verifySlide1(XSLFSlide slide){
        XSLFSlideLayout layout = slide.getSlideLayout();
        XSLFShape[] shapes = slide.getShapes();
        assertEquals("Title Slide",layout.getName());

        XSLFTextShape shape1 = (XSLFTextShape)shapes[0];
        CTPlaceholder ph1 = shape1.getCTPlaceholder();
        assertEquals(STPlaceholderType.CTR_TITLE, ph1.getType());
        // anchor is not defined in the shape
        assertNull(shape1.getSpPr().getXfrm());

        XSLFTextShape masterShape1 = (XSLFTextShape)layout.getPlaceholder(ph1);
        assertNotNull(masterShape1.getSpPr().getXfrm());
        assertEquals(masterShape1.getAnchor(), shape1.getAnchor());

        CTTextBodyProperties bodyPr1 = shape1.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr1.isSetLIns() && !bodyPr1.isSetRIns() &&
                !bodyPr1.isSetBIns() && !bodyPr1.isSetTIns() &&
                !bodyPr1.isSetAnchor()
        );
        assertEquals(7.2, shape1.getLeftInset());  // 0.1"
        assertEquals(7.2, shape1.getRightInset()); // 0.1"
        assertEquals(3.6, shape1.getTopInset());  // 0.05"
        assertEquals(3.6, shape1.getBottomInset()); // 0.05"
        assertEquals(VerticalAlignment.MIDDLE, shape1.getVerticalAlignment());

        // now check text properties
        assertEquals("Centered Title", shape1.getText());
        XSLFTextRun r1 = shape1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals("Calibri", r1.getFontFamily());
        assertEquals(44.0, r1.getFontSize());
        assertEquals(Color.black, r1.getFontColor());

        XSLFTextShape shape2 = (XSLFTextShape)shapes[1];
        CTPlaceholder ph2 = shape2.getCTPlaceholder();
        assertEquals(STPlaceholderType.SUB_TITLE, ph2.getType());
        // anchor is not defined in the shape
        assertNull(shape2.getSpPr().getXfrm());

        XSLFTextShape masterShape2 = (XSLFTextShape)layout.getPlaceholder(ph2);
        assertNotNull(masterShape2.getSpPr().getXfrm());
        assertEquals(masterShape2.getAnchor(), shape2.getAnchor());

        CTTextBodyProperties bodyPr2 = shape2.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr2.isSetLIns() && !bodyPr2.isSetRIns() &&
                !bodyPr2.isSetBIns() && !bodyPr2.isSetTIns() &&
                !bodyPr2.isSetAnchor()
        );
        assertEquals(7.2, shape2.getLeftInset());  // 0.1"
        assertEquals(7.2, shape2.getRightInset()); // 0.1"
        assertEquals(3.6, shape2.getTopInset());  // 0.05"
        assertEquals(3.6, shape2.getBottomInset()); // 0.05"
        assertEquals(VerticalAlignment.TOP, shape2.getVerticalAlignment());

        assertEquals("subtitle", shape2.getText());
        XSLFTextRun r2 = shape2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals("Calibri", r2.getFontFamily());
        assertEquals(32.0, r2.getFontSize());
        // TODO fix calculation of tint
        //assertEquals(new Color(137, 137, 137), r2.getFontColor());
    }

    void verifySlide2(XSLFSlide slide){
        XSLFSlideLayout layout = slide.getSlideLayout();
        XSLFShape[] shapes = slide.getShapes();
        assertEquals("Title and Content",layout.getName());

        XSLFTextShape shape1 = (XSLFTextShape)shapes[0];
        CTPlaceholder ph1 = shape1.getCTPlaceholder();
        assertEquals(STPlaceholderType.TITLE, ph1.getType());
        // anchor is not defined in the shape
        assertNull(shape1.getSpPr().getXfrm());

        XSLFTextShape masterShape1 = (XSLFTextShape)layout.getPlaceholder(ph1);
        // layout does not have anchor info either, it is in the slide master
        assertNull(masterShape1.getSpPr().getXfrm());
        masterShape1 = (XSLFTextShape)layout.getSlideMaster().getPlaceholder(ph1);
        assertNotNull(masterShape1.getSpPr().getXfrm());
        assertEquals(masterShape1.getAnchor(), shape1.getAnchor());

        CTTextBodyProperties bodyPr1 = shape1.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr1.isSetLIns() && !bodyPr1.isSetRIns() &&
                !bodyPr1.isSetBIns() && !bodyPr1.isSetTIns() &&
                !bodyPr1.isSetAnchor()
        );
        assertEquals(7.2, shape1.getLeftInset());  // 0.1"
        assertEquals(7.2, shape1.getRightInset()); // 0.1"
        assertEquals(3.6, shape1.getTopInset());  // 0.05"
        assertEquals(3.6, shape1.getBottomInset()); // 0.05"
        assertEquals(VerticalAlignment.MIDDLE, shape1.getVerticalAlignment());

        // now check text properties
        assertEquals("Title", shape1.getText());
        XSLFTextRun r1 = shape1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals("Calibri", r1.getFontFamily());
        assertEquals(44.0, r1.getFontSize());
        assertEquals(Color.black, r1.getFontColor());

        XSLFTextShape shape2 = (XSLFTextShape)shapes[1];
        CTPlaceholder ph2 = shape2.getCTPlaceholder();
        assertFalse(ph2.isSetType()); // <p:ph idx="1"/>
        assertTrue(ph2.isSetIdx());
        assertEquals(1, ph2.getIdx());
        // anchor is not defined in the shape
        assertNull(shape2.getSpPr().getXfrm());

        XSLFTextShape masterShape2 = (XSLFTextShape)layout.getPlaceholder(ph2);
        // anchor of the body text is missing in the slide layout, llokup in the slide master
        assertNull(masterShape2.getSpPr().getXfrm());
        masterShape2 = (XSLFTextShape)layout.getSlideMaster().getPlaceholder(ph2);
        assertNotNull(masterShape2.getSpPr().getXfrm());
        assertEquals(masterShape2.getAnchor(), shape2.getAnchor());

        CTTextBodyProperties bodyPr2 = shape2.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr2.isSetLIns() && !bodyPr2.isSetRIns() &&
                !bodyPr2.isSetBIns() && !bodyPr2.isSetTIns() &&
                !bodyPr2.isSetAnchor()
        );
        assertEquals(7.2, shape2.getLeftInset());  // 0.1"
        assertEquals(7.2, shape2.getRightInset()); // 0.1"
        assertEquals(3.6, shape2.getTopInset());  // 0.05"
        assertEquals(3.6, shape2.getBottomInset()); // 0.05"
        assertEquals(VerticalAlignment.TOP, shape2.getVerticalAlignment());

        XSLFTextRun pr1 = shape2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(0, pr1.getParentParagraph().getLevel());
        assertEquals("Content", pr1.getText());
        assertEquals("Calibri", pr1.getFontFamily());
        assertEquals(32.0, pr1.getFontSize());
        assertEquals(27.0, pr1.getParentParagraph().getLeftMargin()); 
        assertEquals("\u2022", pr1.getParentParagraph().getBulletCharacter()); 
        assertEquals("Arial", pr1.getParentParagraph().getBulletFont());

        XSLFTextRun pr2 = shape2.getTextParagraphs().get(1).getTextRuns().get(0);
        assertEquals(1, pr2.getParentParagraph().getLevel());
        assertEquals("Level 2", pr2.getText());
        assertEquals("Calibri", pr2.getFontFamily());
        assertEquals(28.0, pr2.getFontSize());
        assertEquals(58.5, pr2.getParentParagraph().getLeftMargin());
        assertEquals("\u2013", pr2.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr2.getParentParagraph().getBulletFont());

        XSLFTextRun pr3 = shape2.getTextParagraphs().get(2).getTextRuns().get(0);
        assertEquals(2, pr3.getParentParagraph().getLevel());
        assertEquals("Level 3", pr3.getText());
        assertEquals("Calibri", pr3.getFontFamily());
        assertEquals(24.0, pr3.getFontSize());
        assertEquals(90.0, pr3.getParentParagraph().getLeftMargin());
        assertEquals("\u2022", pr3.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr3.getParentParagraph().getBulletFont());

        XSLFTextRun pr4 = shape2.getTextParagraphs().get(3).getTextRuns().get(0);
        assertEquals(3, pr4.getParentParagraph().getLevel());
        assertEquals("Level 4", pr4.getText());
        assertEquals("Calibri", pr4.getFontFamily());
        assertEquals(20.0, pr4.getFontSize());
        assertEquals(126.0, pr4.getParentParagraph().getLeftMargin());
        assertEquals("\u2013", pr4.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr4.getParentParagraph().getBulletFont());

        XSLFTextRun pr5 = shape2.getTextParagraphs().get(4).getTextRuns().get(0);
        assertEquals(4, pr5.getParentParagraph().getLevel());
        assertEquals("Level 5", pr5.getText());
        assertEquals("Calibri", pr5.getFontFamily());
        assertEquals(20.0, pr5.getFontSize());
        assertEquals(162.0, pr5.getParentParagraph().getLeftMargin());
        assertEquals("\u00bb", pr5.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr5.getParentParagraph().getBulletFont());

    }

    void verifySlide3(XSLFSlide slide){
        XSLFSlideLayout layout = slide.getSlideLayout();
        XSLFShape[] shapes = slide.getShapes();
        assertEquals("Section Header",layout.getName());

        XSLFTextShape shape1 = (XSLFTextShape)shapes[0];
        CTPlaceholder ph1 = shape1.getCTPlaceholder();
        assertEquals(STPlaceholderType.TITLE, ph1.getType());
        // anchor is not defined in the shape
        assertNull(shape1.getSpPr().getXfrm());

        XSLFTextShape masterShape1 = (XSLFTextShape)layout.getPlaceholder(ph1);
        assertNotNull(masterShape1.getSpPr().getXfrm());
        assertEquals(masterShape1.getAnchor(), shape1.getAnchor());

        CTTextBodyProperties bodyPr1 = shape1.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr1.isSetLIns() && !bodyPr1.isSetRIns() &&
                !bodyPr1.isSetBIns() && !bodyPr1.isSetTIns() &&
                !bodyPr1.isSetAnchor()
        );
        assertEquals(7.2, shape1.getLeftInset());  // 0.1"
        assertEquals(7.2, shape1.getRightInset()); // 0.1"
        assertEquals(3.6, shape1.getTopInset());  // 0.05"
        assertEquals(3.6, shape1.getBottomInset()); // 0.05"
        assertEquals(VerticalAlignment.TOP, shape1.getVerticalAlignment());

        // now check text properties
        assertEquals("Section Title", shape1.getText());
        XSLFTextRun r1 = shape1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(TextAlign.LEFT, r1.getParentParagraph().getTextAlign());
        assertEquals("Calibri", r1.getFontFamily());
        assertEquals(40.0, r1.getFontSize());
        assertEquals(Color.black, r1.getFontColor());
        assertTrue(r1.isBold());
        assertFalse(r1.isItalic());
        assertFalse(r1.isUnderline());

        XSLFTextShape shape2 = (XSLFTextShape)shapes[1];
        CTPlaceholder ph2 = shape2.getCTPlaceholder();
        assertEquals(STPlaceholderType.BODY, ph2.getType());
        // anchor is not defined in the shape
        assertNull(shape2.getSpPr().getXfrm());

        XSLFTextShape masterShape2 = (XSLFTextShape)layout.getPlaceholder(ph2);
        assertNotNull(masterShape2.getSpPr().getXfrm());
        assertEquals(masterShape2.getAnchor(), shape2.getAnchor());

        CTTextBodyProperties bodyPr2 = shape2.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr2.isSetLIns() && !bodyPr2.isSetRIns() &&
                !bodyPr2.isSetBIns() && !bodyPr2.isSetTIns() &&
                !bodyPr2.isSetAnchor()
        );
        assertEquals(7.2, shape2.getLeftInset());  // 0.1"
        assertEquals(7.2, shape2.getRightInset()); // 0.1"
        assertEquals(3.6, shape2.getTopInset());  // 0.05"
        assertEquals(3.6, shape2.getBottomInset()); // 0.05"
        assertEquals(VerticalAlignment.BOTTOM, shape2.getVerticalAlignment());

        assertEquals("Section Header", shape2.getText());
        XSLFTextRun r2 = shape2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(TextAlign.LEFT, r2.getParentParagraph().getTextAlign());
        assertEquals("Calibri", r2.getFontFamily());
        assertEquals(20.0, r2.getFontSize());
        // TODO fix calculation of tint
        //assertEquals(new Color(137, 137, 137), r2.getFontColor());
    }

    void verifySlide4(XSLFSlide slide){
        XSLFSlideLayout layout = slide.getSlideLayout();
        XSLFShape[] shapes = slide.getShapes();
        assertEquals("Two Content",layout.getName());

        XSLFTextShape shape1 = (XSLFTextShape)shapes[0];
        CTPlaceholder ph1 = shape1.getCTPlaceholder();
        assertEquals(STPlaceholderType.TITLE, ph1.getType());
        // anchor is not defined in the shape
        assertNull(shape1.getSpPr().getXfrm());

        XSLFTextShape masterShape1 = (XSLFTextShape)layout.getPlaceholder(ph1);
        // layout does not have anchor info either, it is in the slide master
        assertNull(masterShape1.getSpPr().getXfrm());
        masterShape1 = (XSLFTextShape)layout.getSlideMaster().getPlaceholder(ph1);
        assertNotNull(masterShape1.getSpPr().getXfrm());
        assertEquals(masterShape1.getAnchor(), shape1.getAnchor());

        CTTextBodyProperties bodyPr1 = shape1.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr1.isSetLIns() && !bodyPr1.isSetRIns() &&
                !bodyPr1.isSetBIns() && !bodyPr1.isSetTIns() &&
                !bodyPr1.isSetAnchor()
        );
        assertEquals(7.2, shape1.getLeftInset());  // 0.1"
        assertEquals(7.2, shape1.getRightInset()); // 0.1"
        assertEquals(3.6, shape1.getTopInset());  // 0.05"
        assertEquals(3.6, shape1.getBottomInset()); // 0.05"
        assertEquals(VerticalAlignment.MIDDLE, shape1.getVerticalAlignment());

        // now check text properties
        assertEquals("Title", shape1.getText());
        XSLFTextRun r1 = shape1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(TextAlign.CENTER, r1.getParentParagraph().getTextAlign());
        assertEquals("Calibri", r1.getFontFamily());
        assertEquals(44.0, r1.getFontSize());
        assertEquals(Color.black, r1.getFontColor());

        XSLFTextShape shape2 = (XSLFTextShape)shapes[1];
        CTPlaceholder ph2 = shape2.getCTPlaceholder();
        assertFalse(ph2.isSetType());
        assertTrue(ph2.isSetIdx());
        assertEquals(1, ph2.getIdx());  //<p:ph sz="half" idx="1"/>
        // anchor is not defined in the shape
        assertNull(shape2.getSpPr().getXfrm());

        XSLFTextShape masterShape2 = (XSLFTextShape)layout.getPlaceholder(ph2);
        assertNotNull(masterShape2.getSpPr().getXfrm());
        assertEquals(masterShape2.getAnchor(), shape2.getAnchor());

        CTTextBodyProperties bodyPr2 = shape2.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr2.isSetLIns() && !bodyPr2.isSetRIns() &&
                !bodyPr2.isSetBIns() && !bodyPr2.isSetTIns() &&
                !bodyPr2.isSetAnchor()
        );
        assertEquals(7.2, shape2.getLeftInset());  // 0.1"
        assertEquals(7.2, shape2.getRightInset()); // 0.1"
        assertEquals(3.6, shape2.getTopInset());  // 0.05"
        assertEquals(3.6, shape2.getBottomInset()); // 0.05"
        assertEquals(VerticalAlignment.TOP, shape2.getVerticalAlignment());

        XSLFTextRun pr1 = shape2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(0, pr1.getParentParagraph().getLevel());
        assertEquals("Left", pr1.getText());
        assertEquals("Calibri", pr1.getFontFamily());
        assertEquals(28.0, pr1.getFontSize());
        assertEquals(27.0, pr1.getParentParagraph().getLeftMargin());
        assertEquals("\u2022", pr1.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr1.getParentParagraph().getBulletFont());

        XSLFTextRun pr2 = shape2.getTextParagraphs().get(1).getTextRuns().get(0);
        assertEquals(1, pr2.getParentParagraph().getLevel());
        assertEquals("Level 2", pr2.getParentParagraph().getText());
        assertEquals("Calibri", pr2.getFontFamily());
        assertEquals(24.0, pr2.getFontSize());
        assertEquals(58.5, pr2.getParentParagraph().getLeftMargin());
        assertEquals("\u2013", pr2.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr2.getParentParagraph().getBulletFont());

        XSLFTextRun pr3 = shape2.getTextParagraphs().get(2).getTextRuns().get(0);
        assertEquals(2, pr3.getParentParagraph().getLevel());
        assertEquals("Level 3", pr3.getParentParagraph().getText());
        assertEquals("Calibri", pr3.getFontFamily());
        assertEquals(20.0, pr3.getFontSize());
        assertEquals(90.0, pr3.getParentParagraph().getLeftMargin());
        assertEquals("\u2022", pr3.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr3.getParentParagraph().getBulletFont());

        XSLFTextRun pr4 = shape2.getTextParagraphs().get(3).getTextRuns().get(0);
        assertEquals(3, pr4.getParentParagraph().getLevel());
        assertEquals("Level 4", pr4.getParentParagraph().getText());
        assertEquals("Calibri", pr4.getFontFamily());
        assertEquals(18.0, pr4.getFontSize());
        assertEquals(126.0, pr4.getParentParagraph().getLeftMargin());
        assertEquals("\u2013", pr4.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr4.getParentParagraph().getBulletFont());

        XSLFTextShape shape3 = (XSLFTextShape)shapes[2];
        XSLFTextRun pr5 = shape3.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(0, pr5.getParentParagraph().getLevel());
        assertEquals("Right", pr5.getText());
        assertEquals("Calibri", pr5.getFontFamily());
        assertEquals(Color.black, pr5.getFontColor());
    }

    void verifySlide5(XSLFSlide slide){
        XSLFSlideLayout layout = slide.getSlideLayout();
        XSLFShape[] shapes = slide.getShapes();
        // TODO
    }    

    void verifySlide7(XSLFSlide slide){
        XSLFSlideLayout layout = slide.getSlideLayout();
        XSLFShape[] shapes = slide.getShapes();
        assertEquals("Blank",layout.getName());

        XSLFTextShape shape1 = (XSLFTextShape)shapes[0];
        CTPlaceholder ph1 = shape1.getCTPlaceholder();
        assertEquals(STPlaceholderType.TITLE, ph1.getType());
        // anchor is not defined in the shape
        assertNull(shape1.getSpPr().getXfrm());

        CTTextBodyProperties bodyPr1 = shape1.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr1.isSetLIns() && !bodyPr1.isSetRIns() &&
                !bodyPr1.isSetBIns() && !bodyPr1.isSetTIns() &&
                !bodyPr1.isSetAnchor()
        );
        assertEquals(7.2, shape1.getLeftInset());  // 0.1"
        assertEquals(7.2, shape1.getRightInset()); // 0.1"
        assertEquals(3.6, shape1.getTopInset());  // 0.05"
        assertEquals(3.6, shape1.getBottomInset()); // 0.05"
        assertEquals(VerticalAlignment.MIDDLE, shape1.getVerticalAlignment());

        // now check text properties
        assertEquals("Blank with Default Title", shape1.getText());
        XSLFTextRun r1 = shape1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(TextAlign.CENTER, r1.getParentParagraph().getTextAlign());
        assertEquals("Calibri", r1.getFontFamily());
        assertEquals(44.0, r1.getFontSize());
        assertEquals(Color.black, r1.getFontColor());
        assertFalse(r1.isBold());

        XSLFTextShape shape2 = (XSLFTextShape)shapes[1];

        CTTextBodyProperties bodyPr2 = shape2.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr2.isSetLIns() && !bodyPr2.isSetRIns() &&
                !bodyPr2.isSetBIns() && !bodyPr2.isSetTIns() &&
                !bodyPr2.isSetAnchor()
        );
        assertEquals(7.2, shape2.getLeftInset());  // 0.1"
        assertEquals(7.2, shape2.getRightInset()); // 0.1"
        assertEquals(3.6, shape2.getTopInset());  // 0.05"
        assertEquals(3.6, shape2.getBottomInset()); // 0.05"
        assertEquals(VerticalAlignment.TOP, shape2.getVerticalAlignment());

        XSLFTextRun pr1 = shape2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(0, pr1.getParentParagraph().getLevel());
        assertEquals("Default Text", pr1.getText());
        assertEquals("Calibri", pr1.getFontFamily());
        assertEquals(18.0, pr1.getFontSize());

        XSLFTextShape shape3 = (XSLFTextShape)shapes[2];
        assertEquals("Default", shape3.getTextParagraphs().get(0).getText());
        assertEquals("Text with levels", shape3.getTextParagraphs().get(1).getText());
        assertEquals("Level 1", shape3.getTextParagraphs().get(2).getText());
        assertEquals("Level 2", shape3.getTextParagraphs().get(3).getText());
        assertEquals("Level 3", shape3.getTextParagraphs().get(4).getText());

        for(int p = 0; p < 5; p++) {
            XSLFTextParagraph pr = shape3.getTextParagraphs().get(p);
            assertEquals("Calibri", pr.getTextRuns().get(0).getFontFamily());
            assertEquals(18.0, pr.getTextRuns().get(0).getFontSize());
        }
    }

    void verifySlide8(XSLFSlide slide){
        XSLFSlideLayout layout = slide.getSlideLayout();
        XSLFShape[] shapes = slide.getShapes();
        assertEquals("Content with Caption",layout.getName());

        XSLFTextShape shape1 = (XSLFTextShape)shapes[0];
        CTPlaceholder ph1 = shape1.getCTPlaceholder();
        assertEquals(STPlaceholderType.TITLE, ph1.getType());
        // anchor is not defined in the shape
        assertNull(shape1.getSpPr().getXfrm());

        XSLFTextShape masterShape1 = (XSLFTextShape)layout.getPlaceholder(ph1);
        // layout does not have anchor info either, it is in the slide master
        assertNotNull(masterShape1.getSpPr().getXfrm());
        assertEquals(masterShape1.getAnchor(), shape1.getAnchor());

        CTTextBodyProperties bodyPr1 = shape1.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr1.isSetLIns() && !bodyPr1.isSetRIns() &&
                !bodyPr1.isSetBIns() && !bodyPr1.isSetTIns() &&
                !bodyPr1.isSetAnchor()
        );
        assertEquals(7.2, shape1.getLeftInset());  // 0.1"
        assertEquals(7.2, shape1.getRightInset()); // 0.1"
        assertEquals(3.6, shape1.getTopInset());  // 0.05"
        assertEquals(3.6, shape1.getBottomInset()); // 0.05"
        assertEquals(VerticalAlignment.BOTTOM, shape1.getVerticalAlignment());

        // now check text properties
        assertEquals("Caption", shape1.getText());
        XSLFTextRun r1 = shape1.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(TextAlign.LEFT, r1.getParentParagraph().getTextAlign());
        assertEquals("Calibri", r1.getFontFamily());
        assertEquals(20.0, r1.getFontSize());
        assertEquals(Color.black, r1.getFontColor());
        assertTrue(r1.isBold());

        XSLFTextShape shape2 = (XSLFTextShape)shapes[1];
        CTPlaceholder ph2 = shape2.getCTPlaceholder();
        assertFalse(ph2.isSetType());
        assertTrue(ph2.isSetIdx());
        assertEquals(1, ph2.getIdx());
        // anchor is not defined in the shape
        assertNull(shape2.getSpPr().getXfrm());

        XSLFTextShape masterShape2 = (XSLFTextShape)layout.getPlaceholder(ph2);
        assertNotNull(masterShape2.getSpPr().getXfrm());
        assertEquals(masterShape2.getAnchor(), shape2.getAnchor());

        CTTextBodyProperties bodyPr2 = shape2.getTextBodyPr();
        // none of the following properties are set in the shapes and fetched from the master shape
        assertTrue(
                !bodyPr2.isSetLIns() && !bodyPr2.isSetRIns() &&
                !bodyPr2.isSetBIns() && !bodyPr2.isSetTIns() &&
                !bodyPr2.isSetAnchor()
        );
        assertEquals(7.2, shape2.getLeftInset());  // 0.1"
        assertEquals(7.2, shape2.getRightInset()); // 0.1"
        assertEquals(3.6, shape2.getTopInset());  // 0.05"
        assertEquals(3.6, shape2.getBottomInset()); // 0.05"
        assertEquals(VerticalAlignment.TOP, shape2.getVerticalAlignment());

        XSLFTextRun pr1 = shape2.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(0, pr1.getParentParagraph().getLevel());
        assertEquals("Level 1", pr1.getText());
        assertEquals("Calibri", pr1.getFontFamily());
        assertEquals(32.0, pr1.getFontSize());
        assertEquals(27.0, pr1.getParentParagraph().getLeftMargin());
        assertEquals("\u2022", pr1.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr1.getParentParagraph().getBulletFont());

        XSLFTextRun pr2 = shape2.getTextParagraphs().get(1).getTextRuns().get(0);
        assertEquals(1, pr2.getParentParagraph().getLevel());
        assertEquals("Level 2", pr2.getParentParagraph().getText());
        assertEquals("Calibri", pr2.getFontFamily());
        assertEquals(28.0, pr2.getFontSize());
        assertEquals(58.5, pr2.getParentParagraph().getLeftMargin());
        assertEquals("\u2013", pr2.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr2.getParentParagraph().getBulletFont());

        XSLFTextRun pr3 = shape2.getTextParagraphs().get(2).getTextRuns().get(0);
        assertEquals(2, pr3.getParentParagraph().getLevel());
        assertEquals("Level 3", pr3.getParentParagraph().getText());
        assertEquals("Calibri", pr3.getFontFamily());
        assertEquals(24.0, pr3.getFontSize());
        assertEquals(90.0, pr3.getParentParagraph().getLeftMargin());
        assertEquals("\u2022", pr3.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr3.getParentParagraph().getBulletFont());

        XSLFTextRun pr4 = shape2.getTextParagraphs().get(3).getTextRuns().get(0);
        assertEquals(3, pr4.getParentParagraph().getLevel());
        assertEquals("Level 4", pr4.getParentParagraph().getText());
        assertEquals("Calibri", pr4.getFontFamily());
        assertEquals(20.0, pr4.getFontSize());
        assertEquals(126.0, pr4.getParentParagraph().getLeftMargin());
        assertEquals("\u2013", pr4.getParentParagraph().getBulletCharacter());
        assertEquals("Arial", pr4.getParentParagraph().getBulletFont());

        XSLFTextShape shape3 = (XSLFTextShape)shapes[2];
        assertEquals(VerticalAlignment.TOP, shape3.getVerticalAlignment());
        assertEquals("Content with caption", shape3.getText());

        pr1 = shape3.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(0, pr1.getParentParagraph().getLevel());
        assertEquals("Content with caption", pr1.getText());
        assertEquals("Calibri", pr1.getFontFamily());
        assertEquals(14.0, pr1.getFontSize());

    }

    void verifySlide10(XSLFSlide slide){
        XSLFTextShape footer = (XSLFTextShape)slide.getPlaceholderByType(STPlaceholderType.INT_FTR);

        // now check text properties
        assertEquals("Apache Software Foundation", footer.getText());
        assertEquals(VerticalAlignment.MIDDLE, footer.getVerticalAlignment());

        XSLFTextRun r1 = footer.getTextParagraphs().get(0).getTextRuns().get(0);
        assertEquals(TextAlign.CENTER, r1.getParentParagraph().getTextAlign());
        assertEquals("Calibri", r1.getFontFamily());
        assertEquals(12.0, r1.getFontSize());
        // TODO calculation of tint is incorrect
        assertEquals(new Color(64,64,64), r1.getFontColor());

        XSLFTextShape dt = (XSLFTextShape)slide.getPlaceholderByType(STPlaceholderType.INT_DT);
        assertEquals("Friday, October 21, 2011", dt.getText());

        XSLFTextShape sldNum = (XSLFTextShape)slide.getPlaceholderByType(STPlaceholderType.INT_SLD_NUM);
        assertEquals("10", sldNum.getText());
    }
}