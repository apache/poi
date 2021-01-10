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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.poi.xslf.XSLFTestDataSamples;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextUnderlineType;

class TestXSLFShape {

    @Test
    void testReadTextShapes() throws IOException {
        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("shapes.pptx");
        List<XSLFSlide> slides = ppt.getSlides();

        XSLFSlide slide1 = slides.get(0);
        List<XSLFShape> shapes1 = slide1.getShapes();
        assertEquals(7, shapes1.size());
        assertEquals("TextBox 3", shapes1.get(0).getShapeName());
        XSLFAutoShape sh0 = (XSLFAutoShape) shapes1.get(0);
        assertEquals("Learning PPTX", sh0.getText());
        List<XSLFTextParagraph> paragraphs0 = sh0.getTextParagraphs();
        assertEquals(1, paragraphs0.size());
        XSLFTextParagraph p0 = paragraphs0.get(0);
        assertEquals("Learning PPTX", p0.getText());
        assertEquals(1, p0.getTextRuns().size());
        XSLFTextRun r0 = p0.getTextRuns().get(0);
        assertEquals("Learning PPTX", r0.getRawText());

        XSLFSlide slide2 = slides.get(1);
        List<XSLFShape> shapes2 = slide2.getShapes();
        assertTrue(shapes2.get(0) instanceof XSLFAutoShape);
        assertEquals("PPTX Title", ((XSLFAutoShape) shapes2.get(0)).getText());
        XSLFAutoShape sh1 = (XSLFAutoShape) shapes2.get(0);
        List<XSLFTextParagraph> paragraphs1 = sh1.getTextParagraphs();
        assertEquals(1, paragraphs1.size());
        XSLFTextParagraph p1 = paragraphs1.get(0);
        assertEquals("PPTX Title", p1.getText());
        List<XSLFTextRun> r2 = paragraphs1.get(0).getTextRuns();
        assertEquals(2, r2.size());
        assertEquals("PPTX ", r2.get(0).getRawText());
        assertEquals("Title", r2.get(1).getRawText());
        // Title is underlined
        assertEquals(STTextUnderlineType.SNG, r2.get(1).getRPr(false).getU());


        assertTrue(shapes2.get(1) instanceof XSLFAutoShape);
        assertEquals("Subtitle\nAnd second line", ((XSLFAutoShape) shapes2.get(1)).getText());
        XSLFAutoShape sh2 = (XSLFAutoShape) shapes2.get(1);
        List<XSLFTextParagraph> paragraphs2 = sh2.getTextParagraphs();
        assertEquals(2, paragraphs2.size());
        assertEquals("Subtitle", paragraphs2.get(0).getText());
        assertEquals("And second line", paragraphs2.get(1).getText());

        assertEquals(1, paragraphs2.get(0).getTextRuns().size());
        assertEquals(1, paragraphs2.get(1).getTextRuns().size());

        assertEquals("Subtitle", paragraphs2.get(0).getTextRuns().get(0).getRawText());
        assertTrue(paragraphs2.get(0).getTextRuns().get(0).getRPr(false).getB());
        assertEquals("And second line", paragraphs2.get(1).getTextRuns().get(0).getRawText());

        ppt.close();
    }

    @Test
    void testProblemFile() throws IOException {
        try (XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("ececapstonespring2012.pptx")) {
            List<XSLFSlide> slides = ppt.getSlides();
            assertEquals(24, slides.size());
        }
    }

    @Test
    void testCreateShapes() throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();
        assertTrue(slide.getShapes().isEmpty());

        XSLFTextBox textBox = slide.createTextBox();

        assertEquals(1, slide.getShapes().size());
        assertSame(textBox, slide.getShapes().get(0));

        assertEquals("", textBox.getText());
        // FIXME: is this correct? Should it be starting out with 0 or 1 text paragraphs?
        assertEquals(1, textBox.getTextParagraphs().size());
        textBox.addNewTextParagraph().addNewTextRun().setText("Apache");
        textBox.addNewTextParagraph().addNewTextRun().setText("POI");
        assertEquals("Apache\nPOI", textBox.getText());
        assertEquals(3, textBox.getTextParagraphs().size());

        ppt.close();
    }

}