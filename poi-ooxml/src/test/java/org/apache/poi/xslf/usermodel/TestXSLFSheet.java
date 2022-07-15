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

import static org.apache.poi.xslf.XSLFTestDataSamples.openSampleDocument;
import static org.apache.poi.xslf.XSLFTestDataSamples.writeOutAndReadBack;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;

import org.apache.poi.xslf.XSLFTestDataSamples;
import org.junit.jupiter.api.Test;

/**
 * test common properties for sheets (slides, masters, layouts, etc.)
 */
class TestXSLFSheet {

    @Test
    void testCreateShapes() throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();
        assertTrue(slide.getShapes().isEmpty());

        XSLFSimpleShape shape1 = slide.createAutoShape();
        assertEquals(1, slide.getShapes().size());
        assertSame(shape1, slide.getShapes().get(0));

        XSLFTextBox shape2 = slide.createTextBox();
        assertEquals(2, slide.getShapes().size());
        assertSame(shape1, slide.getShapes().get(0));
        assertSame(shape2, slide.getShapes().get(1));

        XSLFConnectorShape shape3 = slide.createConnector();
        assertEquals(3, slide.getShapes().size());
        assertSame(shape1, slide.getShapes().get(0));
        assertSame(shape2, slide.getShapes().get(1));
        assertSame(shape3, slide.getShapes().get(2));

        XSLFGroupShape shape4 = slide.createGroup();
        assertEquals(4, slide.getShapes().size());
        assertSame(shape1, slide.getShapes().get(0));
        assertSame(shape2, slide.getShapes().get(1));
        assertSame(shape3, slide.getShapes().get(2));
        assertSame(shape4, slide.getShapes().get(3));

        XMLSlideShow ppt2 = XSLFTestDataSamples.writeOutAndReadBack(ppt);
        slide = ppt2.getSlides().get(0);
        List<XSLFShape> shapes = slide.getShapes();
        assertEquals(4, shapes.size());

        assertTrue(shapes.get(0) instanceof XSLFAutoShape);
        assertTrue(shapes.get(1) instanceof XSLFTextBox);
        assertTrue(shapes.get(2) instanceof XSLFConnectorShape);
        assertTrue(shapes.get(3) instanceof XSLFGroupShape);

        ppt.close();
        ppt2.close();
    }

    @Test
    void testImportContent() throws Exception {
        try (XMLSlideShow ppt = openSampleDocument("chart-slide-bg.pptx")) {
            XSLFSlide sourceSlide = ppt.getSlides().get(0);
            XSLFSlide targetSlide = ppt.createSlide();
            targetSlide.importContent(sourceSlide);
            XSLFShape shape = targetSlide.getShapes().get(0);
            assertNotNull(((XSLFGraphicFrame) shape).getChart(), "chart found?");
            assertEquals(2, targetSlide.getSlideNumber());
            try (XMLSlideShow ppt2 = writeOutAndReadBack(ppt)) {
                XSLFSlide slide1 = ppt2.getSlides().get(1);
                assertEquals(2, slide1.getSlideNumber());
                XSLFShape shape1 = targetSlide.getShapes().get(0);
                assertNotNull(((XSLFGraphicFrame) shape1).getChart(), "chart found in slide1?");
            }
        }

        // test importing charts with blip fills
        try (
            XMLSlideShow textureSlideShow = openSampleDocument("chart-texture-bg.pptx");
            XMLSlideShow pictureSlideShow = openSampleDocument("chart-picture-bg.pptx");
        ) {
            XMLSlideShow[] sourceSlideShows = new XMLSlideShow[] { textureSlideShow, pictureSlideShow };
            XMLSlideShow targetSlideShow = textureSlideShow;
            for (XMLSlideShow sourceSlideShow : sourceSlideShows) {
                boolean sameSlideShow = sourceSlideShow == targetSlideShow;
                String assertMessage = "importing charts " + (sameSlideShow ? "within the same slide show" : "from another slideshow") + ": ";
                XSLFSlide sourceSlide = sourceSlideShow.getSlides().get(0);
                XSLFSlide slide = targetSlideShow.createSlide();
                slide.importContent(sourceSlide);

                XSLFShape shape = slide.getShapes().get(0);
                assertNotNull(shape, assertMessage + "the shape is not copied");
                assertInstanceOf(XSLFGraphicFrame.class, shape, assertMessage + "the shape is not XSLFGraphicFrame");

                XSLFChart chart = ((XSLFGraphicFrame) shape).getChart();
                assertNotNull(chart, assertMessage + "the shape doesn't have the chart");

                String blipId1 = chart.getCTChartSpace().getSpPr().getBlipFill().getBlip().getEmbed();
                assertNotNull(slide.getRelationById(blipId1), assertMessage + "the shape chart doesn't have the blip fill");
            }
        }
    }

}