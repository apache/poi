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

import org.apache.poi.xslf.XSLFTestDataSamples;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.presentationml.x2006.main.CTBackgroundProperties;
import org.openxmlformats.schemas.presentationml.x2006.main.impl.CTBackgroundImpl;

import java.awt.*;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TestXSLFBackground {
    @Test
    void testNoFillBackground() throws IOException {
        XMLSlideShow pptx = new XMLSlideShow();
        XSLFSlide slide = pptx.createSlide();

        slide.getBackground().setFillColor(null);

        CTBackgroundImpl bg = (CTBackgroundImpl) slide.getBackground().getXmlObject();
        CTBackgroundProperties bgPr = bg.getBgPr();

        assertFalse(bgPr.isSetBlipFill());
        assertFalse(bgPr.isSetGradFill());
        assertFalse(bgPr.isSetGrpFill());
        assertFalse(bgPr.isSetPattFill());
        assertFalse(bgPr.isSetSolidFill());
        assertTrue(bgPr.isSetNoFill());

        pptx.close();
    }

    @Test
    void testSolidFillBackground() throws IOException {
        XMLSlideShow pptx = new XMLSlideShow();
        XSLFSlide slide = pptx.createSlide();

        Color color = Color.RED;

        slide.getBackground().setFillColor(color);

        CTBackgroundImpl bg = (CTBackgroundImpl) slide.getBackground().getXmlObject();
        CTBackgroundProperties bgPr = bg.getBgPr();

        assertFalse(bgPr.isSetBlipFill());
        assertFalse(bgPr.isSetGradFill());
        assertFalse(bgPr.isSetGrpFill());
        assertFalse(bgPr.isSetPattFill());
        assertTrue(bgPr.isSetSolidFill());
        assertFalse(bgPr.isSetNoFill());

        assertEquals(slide.getBackground().getFillColor(), color);

        pptx.close();
    }

    @Test
    void testBlipFillBackground() throws IOException {
        XMLSlideShow pptx = XSLFTestDataSamples.openSampleDocument("pptx2svg.pptx");
        XSLFSlide slide = pptx.getSlides().get(0);

        Color color = Color.WHITE;

        CTBackgroundImpl bg = (CTBackgroundImpl) slide.getBackground().getXmlObject();
        CTBackgroundProperties bgPr = bg.getBgPr();

        assertTrue(bgPr.isSetBlipFill());
        assertFalse(bgPr.isSetGradFill());
        assertFalse(bgPr.isSetGrpFill());
        assertFalse(bgPr.isSetPattFill());
        assertFalse(bgPr.isSetSolidFill());
        assertFalse(bgPr.isSetNoFill());

        slide.getBackground().setFillColor(color);

        assertFalse(bgPr.isSetBlipFill());
        assertFalse(bgPr.isSetGradFill());
        assertFalse(bgPr.isSetGrpFill());
        assertFalse(bgPr.isSetPattFill());
        assertTrue(bgPr.isSetSolidFill());
        assertFalse(bgPr.isSetNoFill());

        assertEquals(slide.getBackground().getFillColor(), color);

        slide.getBackground().setFillColor(null);

        assertFalse(bgPr.isSetBlipFill());
        assertFalse(bgPr.isSetGradFill());
        assertFalse(bgPr.isSetGrpFill());
        assertFalse(bgPr.isSetPattFill());
        assertFalse(bgPr.isSetSolidFill());
        assertTrue(bgPr.isSetNoFill());

        assertNull(slide.getBackground().getFillColor());

        pptx.close();
    }

    @Test
    void testGradFillBackground() throws IOException {
        XMLSlideShow pptx = XSLFTestDataSamples.openSampleDocument("themes.pptx");
        XSLFSlide slide = pptx.getSlides().get(9);

        Color color = Color.GREEN;

        CTBackgroundImpl bg = (CTBackgroundImpl) slide.getBackground().getXmlObject();
        CTBackgroundProperties bgPr = bg.getBgPr();

        assertFalse(bgPr.isSetBlipFill());
        assertTrue(bgPr.isSetGradFill());
        assertFalse(bgPr.isSetGrpFill());
        assertFalse(bgPr.isSetPattFill());
        assertFalse(bgPr.isSetSolidFill());
        assertFalse(bgPr.isSetNoFill());

        slide.getBackground().setFillColor(color);

        assertFalse(bgPr.isSetBlipFill());
        assertFalse(bgPr.isSetGradFill());
        assertFalse(bgPr.isSetGrpFill());
        assertFalse(bgPr.isSetPattFill());
        assertTrue(bgPr.isSetSolidFill());
        assertFalse(bgPr.isSetNoFill());

        assertEquals(slide.getBackground().getFillColor(), color);

        slide.getBackground().setFillColor(null);

        assertFalse(bgPr.isSetBlipFill());
        assertFalse(bgPr.isSetGradFill());
        assertFalse(bgPr.isSetGrpFill());
        assertFalse(bgPr.isSetPattFill());
        assertFalse(bgPr.isSetSolidFill());
        assertTrue(bgPr.isSetNoFill());

        assertNull(slide.getBackground().getFillColor());

        pptx.close();
    }
}

