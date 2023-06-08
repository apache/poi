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
package org.apache.poi.xslf;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.POIDataSamples;
import org.apache.poi.xslf.usermodel.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class TestXSLFSlideCopy {
    private static final POIDataSamples slTests = POIDataSamples.getSlideShowInstance();

    @Test
    void testCopySlide() throws IOException {
        final String shapeName = "title";
        try (
                InputStream stream = slTests.openResourceAsStream("copy-slide-demo.pptx");
                XMLSlideShow slideShow = new XMLSlideShow(stream);
                UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()
        ) {
            XSLFSlide defaultSlide = getSlideByShapeName(slideShow, shapeName);
            int slideIndex = defaultSlide.getSlideNumber() - 1;
            List<Integer> slideIndexList = new ArrayList<>();
            for (int i = 0; i < 3; i ++) {
                if (i == 0) {
                    // pass
                } else {
                    XSLFSlide newSlide = copySlide(slideShow, slideIndex);
                    slideIndex = newSlide.getSlideNumber() - 1;
                }
                slideIndexList.add(slideIndex);
            }
            for (Integer index : slideIndexList) {
                XSLFSlide slide = slideShow.getSlides().get(index);
                replaceText(slide, shapeName, "this is slide " + slide.getSlideNumber());
            }
            slideShow.write(bos);
            try (XMLSlideShow slideShow1 = new XMLSlideShow(bos.toInputStream())) {
                List<XSLFSlide> slides = slideShow1.getSlides();
                assertEquals(3, slides.size());
                for (XSLFSlide slide : slides) {
                    XSLFShape shape = getShape(slide, shapeName);
                    assertInstanceOf(XSLFTextShape.class, shape);
                    XSLFTextShape textShape = (XSLFTextShape) shape;
                    StringBuilder textBuffer = new StringBuilder();
                    List<XSLFTextParagraph> textParagraphs = textShape.getTextParagraphs();
                    for (XSLFTextParagraph textParagraph : textParagraphs) {
                        List<XSLFTextRun> textRuns = textParagraph.getTextRuns();
                        for (XSLFTextRun textRun : textRuns) {
                            textBuffer.append(textRun.getRawText());
                        }
                    }
                    assertEquals("this is slide " + slide.getSlideNumber(), textBuffer.toString());
                }
            }
        }
    }

    private void replaceText(XSLFSlide slide, String shapeName, String value) {
        XSLFShape shape = getShape(slide, shapeName);
        if (shape == null) {
            return;
        }
        assertInstanceOf(XSLFTextShape.class, shape);
        XSLFTextShape textShape = (XSLFTextShape) shape;
        List<XSLFTextParagraph> textParagraphs = textShape.getTextParagraphs();
        for (XSLFTextParagraph textParagraph : textParagraphs) {
            List<XSLFTextRun> textRuns = textParagraph.getTextRuns();
            for (XSLFTextRun textRun : textRuns) {
                textRun.setText(value);
            }
        }
    }

    private static XSLFSlide copySlide(XMLSlideShow ppt, int index) {
        XSLFSlideLayout defaultSlideLayout = null;
        List<XSLFSlideMaster> slideMasters = ppt.getSlideMasters();
        for (XSLFSlideMaster slideMaster : slideMasters) {
            for (XSLFSlideLayout slideLayout : slideMaster.getSlideLayouts()) {
                if (Objects.equals(SlideLayout.TITLE_AND_CONTENT, slideLayout.getType())) {
                    defaultSlideLayout = slideLayout;
                    break;
                }
            }
        }
        XSLFSlide slide = ppt.getSlides().get(index);
        XSLFSlide newSlide = ppt.createSlide(defaultSlideLayout).importContent(slide);
        ppt.setSlideOrder(newSlide, slide.getSlideNumber());
        return newSlide;
    }

    private static XSLFSlide getSlideByShapeName(XMLSlideShow  ppt, String shapeName) {
        List<XSLFSlide> slides = ppt.getSlides();
        for (XSLFSlide slide : slides) {
            List<XSLFShape> shapes = slide.getShapes();
            for (XSLFShape shape : shapes) {
                if (shape.getShapeName().equals(shapeName)) {
                    return slide;
                }
            }
        }
        throw new InvalidParameterException("shape not exist");
    }

    public XSLFShape getShape(XSLFSlide slide, String shapeName) {
        List<XSLFShape> shapes = slide.getShapes();
        for (XSLFShape shape : shapes) {
            if (shape.getShapeName().equals(shapeName)) {
                return shape;
            }
        }
        throw new InvalidParameterException("shape not exist in slide");
    }
}
