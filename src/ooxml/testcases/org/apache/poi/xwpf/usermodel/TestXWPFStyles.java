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

package org.apache.poi.xwpf.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFonts;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLatentStyles;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLsdException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyles;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STStyleType;

public final class TestXWPFStyles {
    @Test
    void testGetUsedStyles() throws IOException {
        try (XWPFDocument sampleDoc = XWPFTestDataSamples.openSampleDocument("Styles.docx")) {
            List<XWPFStyle> testUsedStyleList = new ArrayList<>();
            XWPFStyles styles = sampleDoc.getStyles();
            XWPFStyle style = styles.getStyle("berschrift1");
            testUsedStyleList.add(style);
            testUsedStyleList.add(styles.getStyle("Standard"));
            testUsedStyleList.add(styles.getStyle("berschrift1Zchn"));
            testUsedStyleList.add(styles.getStyle("Absatz-Standardschriftart"));
            style.hasSameName(style);

            List<XWPFStyle> usedStyleList = styles.getUsedStyleList(style);
            assertEquals(usedStyleList, testUsedStyleList);
        }
    }

    @Test
    void testAddStylesToDocument() throws IOException {
        XWPFDocument docOut = new XWPFDocument();
        XWPFStyles styles = docOut.createStyles();

        String strStyleId = "headline1";
        CTStyle ctStyle = CTStyle.Factory.newInstance();

        ctStyle.setStyleId(strStyleId);
        XWPFStyle s = new XWPFStyle(ctStyle);
        styles.addStyle(s);

        assertTrue(styles.styleExist(strStyleId));

        XWPFDocument docIn = XWPFTestDataSamples.writeOutAndReadBack(docOut);

        styles = docIn.getStyles();
        assertTrue(styles.styleExist(strStyleId));
    }

    /**
     * Bug #52449 - We should be able to write a file containing
     * both regular and glossary styles without error
     */
    @Test
    void test52449() throws Exception {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("52449.docx")) {
            XWPFStyles styles = doc.getStyles();
            assertNotNull(styles);

            XWPFDocument docIn = XWPFTestDataSamples.writeOutAndReadBack(doc);
            styles = docIn.getStyles();
            assertNotNull(styles);
        }
    }


    /**
     * YK: tests below don't make much sense,
     * they exist only to copy xml beans to pi-ooxml-lite.jar
     */
    @SuppressWarnings("resource")
    @Test
    void testLanguages() {
        XWPFDocument docOut = new XWPFDocument();
        XWPFStyles styles = docOut.createStyles();
        styles.setEastAsia("Chinese");

        styles.setSpellingLanguage("English");

        CTFonts def = CTFonts.Factory.newInstance();
        styles.setDefaultFonts(def);
    }

    @Test
    void testType() {
        CTStyle ctStyle = CTStyle.Factory.newInstance();
        XWPFStyle style = new XWPFStyle(ctStyle);

        style.setType(STStyleType.PARAGRAPH);
        assertEquals(STStyleType.PARAGRAPH, style.getType());
    }

    @Test
    void testLatentStyles() {
        CTLatentStyles latentStyles = CTLatentStyles.Factory.newInstance();
        CTLsdException ex = latentStyles.addNewLsdException();
        ex.setName("ex1");
        XWPFLatentStyles ls = new XWPFLatentStyles(latentStyles);
        assertTrue(ls.isLatentStyle("ex1"));
        assertFalse(ls.isLatentStyle("notex1"));
    }

    @Test
    void testSetStyles_Bug57254() throws IOException {
        XWPFDocument docOut = new XWPFDocument();
        XWPFStyles styles = docOut.createStyles();

        CTStyles ctStyles = CTStyles.Factory.newInstance();
        String strStyleId = "headline1";
        CTStyle ctStyle = ctStyles.addNewStyle();

        ctStyle.setStyleId(strStyleId);
        styles.setStyles(ctStyles);

        assertTrue(styles.styleExist(strStyleId));

        XWPFDocument docIn = XWPFTestDataSamples.writeOutAndReadBack(docOut);

        styles = docIn.getStyles();
        assertTrue(styles.styleExist(strStyleId));
    }

    @Test
    void testEasyAccessToStyles() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("SampleDoc.docx")) {
            XWPFStyles styles = doc.getStyles();
            assertNotNull(styles);

            // Has 3 paragraphs on page one, a break, and 3 on page 2
            assertEquals(7, doc.getParagraphs().size());

            // Check the first three have no run styles, just default paragraph style
            for (int i = 0; i < 3; i++) {
                XWPFParagraph p = doc.getParagraphs().get(i);
                assertNull(p.getStyle());
                assertNull(p.getStyleID());
                assertEquals(1, p.getRuns().size());

                XWPFRun r = p.getRuns().get(0);
                assertNull(r.getColor());
                assertNull(r.getFontFamily());
                assertNull(r.getFontName());
                assertEquals(-1, r.getFontSize());
            }

            // On page two, has explicit styles, but on runs not on
            //  the paragraph itself
            for (int i = 4; i < 7; i++) {
                XWPFParagraph p = doc.getParagraphs().get(i);
                assertNull(p.getStyle());
                assertNull(p.getStyleID());
                assertEquals(1, p.getRuns().size());

                XWPFRun r = p.getRuns().get(0);
                assertEquals("Arial Black", r.getFontFamily());
                assertEquals("Arial Black", r.getFontName());
                assertEquals(16, r.getFontSize());
                assertEquals("548DD4", r.getColor());
            }

            // Check the document styles
            // Should have a style defined for each type
            assertEquals(4, styles.getNumberOfStyles());
            assertNotNull(styles.getStyle("Normal"));
            assertNotNull(styles.getStyle("DefaultParagraphFont"));
            assertNotNull(styles.getStyle("TableNormal"));
            assertNotNull(styles.getStyle("NoList"));

            // We can't do much yet with latent styles
            assertEquals(137, styles.getLatentStyles().getNumberOfStyles());

            // Check the default styles
            assertNotNull(styles.getDefaultRunStyle());
            assertNotNull(styles.getDefaultParagraphStyle());

            assertEquals(11, styles.getDefaultRunStyle().getFontSize());
            assertEquals(11.0, styles.getDefaultRunStyle().getFontSizeAsDouble(), 0.01);
            assertEquals(200, styles.getDefaultParagraphStyle().getSpacingAfter());
        }
    }

    // Bug 60329: style with missing StyleID throws NPE
    @Test
    void testMissingStyleId() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("60329.docx")) {
            XWPFStyles styles = doc.getStyles();
            // Styles exist in the test document in this order, EmptyCellLayoutStyle
            // is missing a StyleId
            assertNotNull(styles.getStyle("NoList"));
            assertNull(styles.getStyle("EmptyCellLayoutStyle"));
            assertNotNull(styles.getStyle("BalloonText"));

            // Bug 64600: styleExist throws NPE
            assertTrue(styles.styleExist("NoList"));
            assertFalse(styles.styleExist("EmptyCellLayoutStyle"));
            assertTrue(styles.styleExist("BalloonText"));
        }
    }

    @Test
    void testGetStyleByName() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("SampleDoc.docx")) {
            XWPFStyles styles = doc.getStyles();
            assertNotNull(styles);

            String styleName = "Normal Table";
            XWPFStyle style = styles.getStyleWithName(styleName);
            assertNotNull(style, "Expected to find style \"" + styleName + "\"");
            assertEquals(styleName, style.getName());
        }
    }

}
