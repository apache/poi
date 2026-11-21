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

import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;

import java.io.IOException;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.apache.poi.xslf.XSLFTestDataSamples.openSampleDocument;
import static org.apache.poi.xslf.XSLFTestDataSamples.writeOutAndReadBack;

class TestXSLFGraphicFrame {

    /**
     * Removing the frame of a chart, an embedded object or a diagram also removes the parts
     * only it used, so the chart data, embedded documents, etc. aren't written out anymore.
     */
    @Test
    void testRemoveGraphicFrameRemovesParts() throws IOException {
        try (XMLSlideShow ppt = openSampleDocument("pie-chart.pptx")) {
            XSLFSlide slide = ppt.getSlides().get(0);
            assertTrue(slide.removeShape(findFrame(slide)));
            assertTrue(ppt.getCharts().isEmpty());
            try (XMLSlideShow ppt2 = writeOutAndReadBack(ppt)) {
                assertEquals(0, ppt2.getPackage().getPartsByName(Pattern.compile("/ppt/(charts|embeddings)/.*")).size());
                assertTrue(ppt2.getCharts().isEmpty());
            }
        }

        try (XMLSlideShow ppt = openSampleDocument("45545_Comment.pptx")) {
            XSLFSlide slide = ppt.getSlides().get(0);
            assertTrue(slide.removeShape(findFrame(slide)));
            try (XMLSlideShow ppt2 = writeOutAndReadBack(ppt)) {
                assertEquals(0, ppt2.getPackage().getPartsByName(Pattern.compile("/ppt/embeddings/oleObject1.bin")).size());
                // the objects of the other slides are kept
                assertEquals(2, ppt2.getPackage().getPartsByName(Pattern.compile("/ppt/embeddings/.*")).size());
            }
        }

        try (XMLSlideShow ppt = openSampleDocument("smartart-simple.pptx")) {
            XSLFSlide slide = ppt.getSlides().get(0);
            assertTrue(slide.removeShape(findFrame(slide)));
            try (XMLSlideShow ppt2 = writeOutAndReadBack(ppt)) {
                assertEquals(0, ppt2.getPackage().getPartsByName(Pattern.compile("/ppt/(diagrams|media)/.*")).size());
                assertEquals(1, ppt2.getSlides().get(0).getRelations().size());
            }
        }
    }

    /**
     * A related part, which another shape of the sheet still uses, is kept
     */
    @Test
    void testRemoveGraphicFrameKeepsSharedParts() throws IOException {
        try (XMLSlideShow ppt1 = openSampleDocument("pie-chart.pptx")) {
            XSLFSlide slide1 = ppt1.getSlides().get(0);
            CTGraphicalObjectFrame frameXml = (CTGraphicalObjectFrame) findFrame(slide1).getXmlObject();
            slide1.getSpTree().addNewGraphicFrame().set(frameXml.copy());

            try (XMLSlideShow ppt = writeOutAndReadBack(ppt1)) {
                XSLFSlide slide = ppt.getSlides().get(0);
                assertTrue(slide.removeShape(findFrame(slide)));
                assertEquals(1, ppt.getCharts().size());
                assertNotNull(findFrame(slide).getChart());

                assertTrue(slide.removeShape(findFrame(slide)));
                assertTrue(ppt.getCharts().isEmpty());
                try (XMLSlideShow ppt2 = writeOutAndReadBack(ppt)) {
                    assertEquals(0, ppt2.getPackage().getPartsByName(Pattern.compile("/ppt/(charts|embeddings)/.*")).size());
                }
            }
        }
    }

    private static XSLFGraphicFrame findFrame(XSLFSlide slide) {
        return slide.getShapes().stream()
            .filter(XSLFGraphicFrame.class::isInstance)
            .map(XSLFGraphicFrame.class::cast)
            .findFirst().orElseThrow(AssertionError::new);
    }

    @Test
    void testHasDiagram() throws IOException {
        try (XMLSlideShow ppt = openSampleDocument("SmartArt.pptx")) {
            XSLFSlide slide = ppt.getSlides().get(0);
            XSLFGraphicFrame gf = (XSLFGraphicFrame) slide.getShapes().get(0);

            assertTrue(gf.hasDiagram());
        }
    }
}