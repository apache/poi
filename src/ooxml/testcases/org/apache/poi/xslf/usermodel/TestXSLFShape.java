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
import org.openxmlformats.schemas.drawingml.x2006.main.STTextUnderlineType;

import java.util.List;

/**
 * @author Yegor Kozlov
 */
public class TestXSLFShape extends TestCase {

    public void testReadTextShapes() {
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("shapes.pptx");
        XSLFSlide[] slides = ppt.getSlides();

        XSLFSlide slide1 = slides[0];
        XSLFShape[] shapes1 = slide1.getShapes();
        assertEquals(7, shapes1.length);
        assertEquals("TextBox 3", shapes1[0].getShapeName());
        XSLFAutoShape sh0 = (XSLFAutoShape) shapes1[0];
        assertEquals("Learning PPTX", sh0.getText());
        List<XSLFTextParagraph> paragraphs0 = sh0.getTextParagraphs();
        assertEquals(1, paragraphs0.size());
        XSLFTextParagraph p0 = paragraphs0.get(0);
        assertEquals("Learning PPTX", p0.getText());
        assertEquals(1, p0.getTextRuns().size());
        XSLFTextRun r0 = p0.getTextRuns().get(0);
        assertEquals("Learning PPTX", r0.getText());

        XSLFSlide slide2 = slides[1];
        XSLFShape[] shapes2 = slide2.getShapes();
        assertTrue(shapes2[0] instanceof XSLFAutoShape);
        assertEquals("PPTX Title", ((XSLFAutoShape) shapes2[0]).getText());
        XSLFAutoShape sh1 = (XSLFAutoShape) shapes2[0];
        List<XSLFTextParagraph> paragraphs1 = sh1.getTextParagraphs();
        assertEquals(1, paragraphs1.size());
        XSLFTextParagraph p1 = paragraphs1.get(0);
        assertEquals("PPTX Title", p1.getText());
        List<XSLFTextRun> r2 = paragraphs1.get(0).getTextRuns();
        assertEquals(2, r2.size());
        assertEquals("PPTX ", r2.get(0).getText());
        assertEquals("Title", r2.get(1).getText());
        // Title is underlined
        assertEquals(STTextUnderlineType.SNG, r2.get(1).getXmlObject().getRPr().getU());


        assertTrue(shapes2[1] instanceof XSLFAutoShape);
        assertEquals("Subtitle\nAnd second line", ((XSLFAutoShape) shapes2[1]).getText());
        XSLFAutoShape sh2 = (XSLFAutoShape) shapes2[1];
        List<XSLFTextParagraph> paragraphs2 = sh2.getTextParagraphs();
        assertEquals(2, paragraphs2.size());
        assertEquals("Subtitle", paragraphs2.get(0).getText());
        assertEquals("And second line", paragraphs2.get(1).getText());

        assertEquals(1, paragraphs2.get(0).getTextRuns().size());
        assertEquals(1, paragraphs2.get(1).getTextRuns().size());

        assertEquals("Subtitle", paragraphs2.get(0).getTextRuns().get(0).getText());
        assertTrue(paragraphs2.get(0).getTextRuns().get(0).getXmlObject().getRPr().getB());
        assertEquals("And second line", paragraphs2.get(1).getTextRuns().get(0).getText());
    }

    public void testCreateShapes() {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();
        assertEquals(0, slide.getShapes().length);

        XSLFTextBox textBox = slide.createTextBox();

        assertEquals(1, slide.getShapes().length);
        assertSame(textBox, slide.getShapes()[0]);

        assertEquals("", textBox.getText());
        assertEquals(0, textBox.getTextParagraphs().size());
        textBox.addNewTextParagraph().addNewTextRun().setText("Apache");
        textBox.addNewTextParagraph().addNewTextRun().setText("POI");
        assertEquals("Apache\nPOI", textBox.getText());
        assertEquals(2, textBox.getTextParagraphs().size());
    }

}