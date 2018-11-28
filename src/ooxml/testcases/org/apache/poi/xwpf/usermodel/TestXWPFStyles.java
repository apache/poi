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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.junit.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFonts;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLatentStyles;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLsdException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyles;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STStyleType;

public final class TestXWPFStyles {
    @Test
    public void testGetUsedStyles() throws IOException {
        XWPFDocument sampleDoc = XWPFTestDataSamples.openSampleDocument("Styles.docx");
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

    @Test
    public void testAddStylesToDocument() throws IOException {
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
    public void test52449() throws Exception {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("52449.docx");
        XWPFStyles styles = doc.getStyles();
        assertNotNull(styles);

        XWPFDocument docIn = XWPFTestDataSamples.writeOutAndReadBack(doc);
        styles = docIn.getStyles();
        assertNotNull(styles);
    }


    /**
     * YK: tests below don't make much sense,
     * they exist only to copy xml beans to pi-ooxml-schemas.jar
     */
    @SuppressWarnings("resource")
    @Test
    public void testLanguages() {
        XWPFDocument docOut = new XWPFDocument();
        XWPFStyles styles = docOut.createStyles();
        styles.setEastAsia("Chinese");

        styles.setSpellingLanguage("English");

        CTFonts def = CTFonts.Factory.newInstance();
        styles.setDefaultFonts(def);
    }

    @Test
    public void testType() {
        CTStyle ctStyle = CTStyle.Factory.newInstance();
        XWPFStyle style = new XWPFStyle(ctStyle);

        style.setType(STStyleType.PARAGRAPH);
        assertEquals(STStyleType.PARAGRAPH, style.getType());
    }

    @Test
    public void testLatentStyles() {
        CTLatentStyles latentStyles = CTLatentStyles.Factory.newInstance();
        CTLsdException ex = latentStyles.addNewLsdException();
        ex.setName("ex1");
        XWPFLatentStyles ls = new XWPFLatentStyles(latentStyles);
        assertEquals(true, ls.isLatentStyle("ex1"));
        assertEquals(false, ls.isLatentStyle("notex1"));
    }

    @Test
    public void testSetStyles_Bug57254() throws IOException {
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
    public void testEasyAccessToStyles() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("SampleDoc.docx");
        XWPFStyles styles = doc.getStyles();
        assertNotNull(styles);

        // Has 3 paragraphs on page one, a break, and 3 on page 2
        assertEquals(7, doc.getParagraphs().size());

        // Check the first three have no run styles, just default paragraph style
        for (int i = 0; i < 3; i++) {
            XWPFParagraph p = doc.getParagraphs().get(i);
            assertEquals(null, p.getStyle());
            assertEquals(null, p.getStyleID());
            assertEquals(1, p.getRuns().size());

            XWPFRun r = p.getRuns().get(0);
            assertEquals(null, r.getColor());
            assertEquals(null, r.getFontFamily());
            assertEquals(null, r.getFontName());
            assertEquals(-1, r.getFontSize());
        }

        // On page two, has explicit styles, but on runs not on
        //  the paragraph itself
        for (int i = 4; i < 7; i++) {
            XWPFParagraph p = doc.getParagraphs().get(i);
            assertEquals(null, p.getStyle());
            assertEquals(null, p.getStyleID());
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
        assertEquals(200, styles.getDefaultParagraphStyle().getSpacingAfter());
    }
    
    // Bug 60329: style with missing StyleID throws NPE
    @Test
    public void testMissingStyleId() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("60329.docx");
        XWPFStyles styles = doc.getStyles();
        // Styles exist in the test document in this order, EmptyCellLayoutStyle
        // is missing a StyleId
        try {
            assertNotNull(styles.getStyle("NoList"));
            assertNull(styles.getStyle("EmptyCellLayoutStyle"));
            assertNotNull(styles.getStyle("BalloonText"));
        } catch (NullPointerException e) {
            fail(e.toString());
        }

        doc.close();
    }
    
    @Test
    public void testGetStyleByName() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("SampleDoc.docx");
        XWPFStyles styles = doc.getStyles();
        assertNotNull(styles);

        String styleName = "Normal Table";
        XWPFStyle style = styles.getStyleWithName(styleName);
        assertNotNull("Expected to find style \"" + styleName + "\"", style);
        assertEquals(styleName, style.getName());
    }
}
