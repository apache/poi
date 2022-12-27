/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.xslf.usermodel;

import static org.apache.poi.sl.usermodel.BaseTestSlideShow.getColor;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.sl.draw.DrawTextParagraph;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextLineBreak;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;

class TestXSLFTextRun {

    @Test
    void testRunProperties() throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();
        XSLFTextShape sh = slide.createAutoShape();

        XSLFTextRun r = sh.addNewTextParagraph().addNewTextRun();
        assertEquals("en-US", r.getRPr(true).getLang());

        assertEquals(0., r.getCharacterSpacing(), 0);
        r.setCharacterSpacing(3);
        assertEquals(3., r.getCharacterSpacing(), 0);
        r.setCharacterSpacing(-3);
        assertEquals(-3., r.getCharacterSpacing(), 0);
        r.setCharacterSpacing(0);
        assertEquals(0., r.getCharacterSpacing(), 0);
        assertFalse(r.getRPr(true).isSetSpc());

        assertEquals(Color.black, getColor(r.getFontColor()));
        r.setFontColor(Color.red);
        assertEquals(Color.red, getColor(r.getFontColor()));

        assertEquals("Calibri", r.getFontFamily());
        r.setFontFamily("Arial");
        assertEquals("Arial", r.getFontFamily());

        assertEquals(18.0, r.getFontSize(), 0);
        r.setFontSize(13.0);
        assertEquals(13.0, r.getFontSize(), 0);

        assertFalse(r.isSuperscript());
        r.setSuperscript(true);
        assertTrue(r.isSuperscript());
        r.setSuperscript(false);
        assertFalse(r.isSuperscript());

        assertFalse(r.isSubscript());
        r.setSubscript(true);
        assertTrue(r.isSubscript());
        r.setSubscript(false);
        assertFalse(r.isSubscript());

        ppt.close();
    }

    @Test
    void testUnicodeSurrogates() throws Exception {
        final String unicodeSurrogates = "\uD835\uDF4A\uD835\uDF4B\uD835\uDF4C\uD835\uDF4D\uD835\uDF4E"
                + "\uD835\uDF4F\uD835\uDF50\uD835\uDF51\uD835\uDF52\uD835\uDF53\uD835\uDF54\uD835"
                + "\uDF55\uD835\uDF56\uD835\uDF57\uD835\uDF58\uD835\uDF59\uD835\uDF5A\uD835\uDF5B"
                + "\uD835\uDF5C\uD835\uDF5D\uD835\uDF5E\uD835\uDF5F\uD835\uDF60\uD835\uDF61\uD835"
                + "\uDF62\uD835\uDF63\uD835\uDF64\uD835\uDF65\uD835\uDF66\uD835\uDF67\uD835\uDF68"
                + "\uD835\uDF69\uD835\uDF6A\uD835\uDF6B\uD835\uDF6C\uD835\uDF6D\uD835\uDF6E\uD835"
                + "\uDF6F\uD835\uDF70\uD835\uDF71\uD835\uDF72\uD835\uDF73\uD835\uDF74\uD835\uDF75"
                + "\uD835\uDF76\uD835\uDF77\uD835\uDF78\uD835\uDF79\uD835\uDF7A";

        try (XMLSlideShow ppt = new XMLSlideShow()) {
            XSLFSlide slide = ppt.createSlide();
            XSLFTextShape sh = slide.createAutoShape();
            XSLFTextParagraph p = sh.addNewTextParagraph();
            XSLFTextRun r = p.addNewTextRun();
            r.setText(unicodeSurrogates);

            assertEquals(unicodeSurrogates, new DrawTextParagraph(p).getRenderableText(r));
        }
    }

    @Test
    void testCopyNullFontSize() throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            XSLFSlide slide = ppt.createSlide();
            XSLFTextShape sh = slide.createAutoShape();

            XSLFTextRun r = sh.addNewTextParagraph().addNewTextRun();

            XSLFTextRun s = new XSLFTextRun(CTTextLineBreak.Factory.newInstance(),
                new XSLFTextParagraph(CTTextParagraph.Factory.newInstance(),
                    new XSLFTextBox(CTShape.Factory.newInstance(), slide)));

            assertDoesNotThrow(() -> r.copy(s));
        }
    }

    @Test
    void testDefaultRunProperties() throws IOException {
        // bug #63290
        POIDataSamples pds = POIDataSamples.getSlideShowInstance();
        try (InputStream is = pds.openResourceAsStream("bug63290.pptx");
             XMLSlideShow ppt = new XMLSlideShow(is)) {
            XSLFSlide slide = ppt.getSlides().get(0);
            for (XSLFShape shape : slide.getShapes()) {
                if (shape instanceof  XSLFTextShape) {
                    XSLFTextShape text = (XSLFTextShape) shape;
                    XSLFTextParagraph paragraph = text.getTextParagraphs().get(0);
                    XSLFTextRun defaultRun = paragraph.getTextRuns().get(0);
                    assertEquals("DefaultRunProperties", defaultRun.getRawText().trim());
                    XSLFTextRun explicitRun = paragraph.getTextRuns().get(1);
                    assertEquals("ExplicitRunProperties", explicitRun.getRawText().trim());
                    assertEquals(defaultRun.getFontSize(), explicitRun.getFontSize());
                    assertNotEquals(defaultRun.getFontColor(), explicitRun.getFontColor());
                }
            }
        }
    }

}
