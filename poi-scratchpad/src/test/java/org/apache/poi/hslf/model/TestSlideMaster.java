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

package org.apache.poi.hslf.model;

import static org.apache.poi.hslf.HSLFTestDataSamples.getSlideShow;
import static org.apache.poi.hslf.HSLFTestDataSamples.writeOutAndReadBack;
import static org.apache.poi.sl.usermodel.TextShape.TextPlaceholder.BODY;
import static org.apache.poi.sl.usermodel.TextShape.TextPlaceholder.CENTER_BODY;
import static org.apache.poi.sl.usermodel.TextShape.TextPlaceholder.CENTER_TITLE;
import static org.apache.poi.sl.usermodel.TextShape.TextPlaceholder.TITLE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.model.textproperties.CharFlagsTextProp;
import org.apache.poi.hslf.model.textproperties.TextProp;
import org.apache.poi.hslf.record.Environment;
import org.apache.poi.hslf.usermodel.HSLFFontInfo;
import org.apache.poi.hslf.usermodel.HSLFMasterSheet;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideMaster;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hslf.usermodel.HSLFTextRun;
import org.apache.poi.hslf.usermodel.HSLFTitleMaster;
import org.apache.poi.sl.usermodel.TextShape.TextPlaceholder;
import org.junit.jupiter.api.Test;

/**
 * Tests for SlideMaster
 */
public final class TestSlideMaster {
    private static final POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    /**
     * The reference ppt has two masters.
     * Check we can read their attributes.
     */
    @Test
    void testSlideMaster() throws IOException {
        try (HSLFSlideShow ppt = getSlideShow("slide_master.ppt")) {

            final Environment env = ppt.getDocumentRecord().getEnvironment();

            assertEquals(2, ppt.getSlideMasters().size());

            //character attributes
            assertEquals(40, getMasterVal(ppt, 0, TITLE, "font.size", true));
            assertEquals(48, getMasterVal(ppt, 1, TITLE, "font.size", true));

            int font1 = getMasterVal(ppt, 0, TITLE, "font.index", true);
            int font2 = getMasterVal(ppt, 1, TITLE, "font.index", true);
            HSLFFontInfo fontInfo = env.getFontCollection().getFontInfo(font1);
            assertNotNull(fontInfo);
            assertEquals("Arial", fontInfo.getTypeface());
            fontInfo = env.getFontCollection().getFontInfo(font2);
            assertNotNull(fontInfo);
            assertEquals("Georgia", fontInfo.getTypeface());

            CharFlagsTextProp prop1 = getMasterProp(ppt, 0, TITLE, "char_flags", true);
            assertFalse(prop1.getSubValue(CharFlagsTextProp.BOLD_IDX));
            assertFalse(prop1.getSubValue(CharFlagsTextProp.ITALIC_IDX));
            assertTrue(prop1.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));

            CharFlagsTextProp prop2 = getMasterProp(ppt, 1, TITLE, "char_flags", true);
            assertFalse(prop2.getSubValue(CharFlagsTextProp.BOLD_IDX));
            assertTrue(prop2.getSubValue(CharFlagsTextProp.ITALIC_IDX));
            assertFalse(prop2.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));

            //now paragraph attributes
            assertEquals(0x266B, getMasterVal(ppt, 0, BODY, "bullet.char", false));
            assertEquals(0x2022, getMasterVal(ppt, 1, BODY, "bullet.char", false));

            int b1 = getMasterVal(ppt, 0, BODY, "bullet.font", false);
            int b2 = getMasterVal(ppt, 1, BODY, "bullet.font", false);
            fontInfo = env.getFontCollection().getFontInfo(b1);
            assertNotNull(fontInfo);
            assertEquals("Arial", fontInfo.getTypeface());
            fontInfo = env.getFontCollection().getFontInfo(b2);
            assertNotNull(fontInfo);
            assertEquals("Georgia", fontInfo.getTypeface());
        }
    }

    private static <T extends TextProp> T getMasterProp(HSLFSlideShow ppt, int masterIdx, TextPlaceholder txtype, String propName, boolean isCharacter) {
        return Objects.requireNonNull(ppt.getSlideMasters().get(masterIdx)
            .getPropCollection(txtype.nativeId, 0, propName, isCharacter))
            .findByName(propName);
    }

    private static int getMasterVal(HSLFSlideShow ppt, int masterIdx, TextPlaceholder txtype, String propName, boolean isCharacter) {
        return getMasterProp(ppt, masterIdx, txtype, propName, isCharacter).getValue();
    }


    /**
     * Test we can read default text attributes for a title master sheet
     */
    @Test
    void testTitleMasterTextAttributes() throws IOException {
        try (HSLFSlideShow ppt = getSlideShow("slide_master.ppt")) {
            assertEquals(1, ppt.getTitleMasters().size());

            assertEquals(40, getMasterVal(ppt, 0, CENTER_TITLE, "font.size", true));
            CharFlagsTextProp prop1 = getMasterProp(ppt, 0, CENTER_TITLE, "char_flags", true);
            assertFalse(prop1.getSubValue(CharFlagsTextProp.BOLD_IDX));
            assertFalse(prop1.getSubValue(CharFlagsTextProp.ITALIC_IDX));
            assertTrue(prop1.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));

            assertEquals(32, getMasterVal(ppt, 0, CENTER_BODY, "font.size", true));
            CharFlagsTextProp prop2 = getMasterProp(ppt, 0, CENTER_BODY, "char_flags", true);
            assertFalse(prop2.getSubValue(CharFlagsTextProp.BOLD_IDX));
            assertFalse(prop2.getSubValue(CharFlagsTextProp.ITALIC_IDX));
            assertFalse(prop2.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));
        }
    }

    /**
     * Slide 3 has title layout and follows the TitleMaster. Verify that.
     */
    @Test
    void testTitleMaster() throws IOException {
        try (HSLFSlideShow ppt = getSlideShow("slide_master.ppt")) {
            HSLFSlide slide = ppt.getSlides().get(2);
            HSLFMasterSheet masterSheet = slide.getMasterSheet();
            assertTrue(masterSheet instanceof HSLFTitleMaster);

            for (List<HSLFTextParagraph> txt : slide.getTextParagraphs()) {
                HSLFTextRun rt = txt.get(0).getTextRuns().get(0);
                assertNotNull(rt.getFontSize());
                TextPlaceholder tp = TextPlaceholder.fromNativeId(txt.get(0).getRunType());
                assertNotNull(tp);
                switch (tp) {
                    case CENTER_TITLE:
                        assertEquals("Arial", rt.getFontFamily());
                        assertEquals(32, rt.getFontSize(), 0);
                        assertTrue(rt.isBold());
                        assertTrue(rt.isUnderlined());
                        break;
                    case CENTER_BODY:
                        assertEquals("Courier New", rt.getFontFamily());
                        assertEquals(20, rt.getFontSize(), 0);
                        assertTrue(rt.isBold());
                        assertFalse(rt.isUnderlined());
                        break;
                }
            }
        }
    }

    /**
     * If a style attribute is not set ensure it is read from the master
     */
    @Test
    void testMasterAttributes() throws Exception {
        try (HSLFSlideShow ppt = getSlideShow("slide_master.ppt")) {
            List<HSLFSlide> slide = ppt.getSlides();
            assertEquals(3, slide.size());
            for (List<HSLFTextParagraph> tparas : slide.get(0).getTextParagraphs()) {
                HSLFTextParagraph tpara = tparas.get(0);
                HSLFTextRun rt = tpara.getTextRuns().get(0);
                assertNotNull(rt.getFontSize());
                if (tpara.getRunType() == TITLE.nativeId) {
                    assertEquals(40, rt.getFontSize(), 0);
                    assertTrue(rt.isUnderlined());
                    assertEquals("Arial", rt.getFontFamily());
                } else if (tpara.getRunType() == BODY.nativeId) {
                    assertEquals(0, tpara.getIndentLevel());
                    assertEquals(32, rt.getFontSize(), 0);
                    assertEquals("Arial", rt.getFontFamily());

                    tpara = tparas.get(1);
                    rt = tpara.getTextRuns().get(0);
                    assertEquals(1, tpara.getIndentLevel());
                    assertNotNull(rt.getFontSize());
                    assertEquals(28, rt.getFontSize(), 0);
                    assertEquals("Arial", rt.getFontFamily());
                }
            }

            for (List<HSLFTextParagraph> tparas : slide.get(1).getTextParagraphs()) {
                HSLFTextParagraph tpara = tparas.get(0);
                HSLFTextRun rt = tpara.getTextRuns().get(0);
                assertNotNull(rt.getFontSize());
                if (tpara.getRunType() == TITLE.nativeId) {
                    assertEquals(48, rt.getFontSize(), 0);
                    assertTrue(rt.isItalic());
                    assertEquals("Georgia", rt.getFontFamily());
                } else if (tpara.getRunType() == BODY.nativeId) {
                    assertEquals(0, tpara.getIndentLevel());
                    assertEquals(32, rt.getFontSize(), 0);
                    assertEquals("Courier New", rt.getFontFamily());
                }
            }
        }
    }

    /**
     * Check we can dynamically assign a slide master to a slide.
     */
    @Test
    void testChangeSlideMaster() throws IOException {
        try (HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("slide_master.ppt"))) {
            int[] masterIds = IntStream.concat(
                ppt.getSlideMasters().stream().mapToInt(HSLFSlideMaster::_getSheetNumber),
                ppt.getTitleMasters().stream().mapToInt(HSLFTitleMaster::_getSheetNumber)
            ).toArray();
            //each slide uses its own master
            int[] slideMasters = ppt.getSlides().stream().mapToInt(s -> {
                HSLFMasterSheet m = s.getMasterSheet();
                assertNotNull(m);
                return m._getSheetNumber();
            }).toArray();
            assertArrayEquals(masterIds, slideMasters);

            //all slides use the first master slide
            HSLFSlideMaster master0 = ppt.getSlideMasters().get(0);
            int sheetNo = master0._getSheetNumber();
            ppt.getSlides().forEach(s -> s.setMasterSheet(master0));

            try (HSLFSlideShow ppt2 = writeOutAndReadBack(ppt)) {
                for (HSLFSlide s : ppt2.getSlides()) {
                    HSLFMasterSheet ms = s.getMasterSheet();
                    assertNotNull(ms);
                    assertEquals(sheetNo, ms._getSheetNumber());
                }
            }
        }
    }

    /**
     * Varify we can read attributes for different indentation levels.
     * (typical for the "bulleted body" placeholder)
     */
    @Test
    void testIndentation() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("slide_master.ppt"));
        HSLFSlide slide = ppt.getSlides().get(0);

        for (List<HSLFTextParagraph> tparas : slide.getTextParagraphs()) {
            HSLFTextParagraph tpara = tparas.get(0);
            if (tpara.getRunType() == TITLE.nativeId){
                HSLFTextRun rt = tpara.getTextRuns().get(0);
                assertNotNull(rt.getFontSize());
                assertEquals(40, rt.getFontSize(), 0);
                assertTrue(rt.isUnderlined());
                assertEquals("Arial", rt.getFontFamily());
            } else if (tpara.getRunType() == BODY.nativeId){
                int[] indents = {32, 28, 24};
                for (HSLFTextRun rt : tpara.getTextRuns()) {
                    int indent = tpara.getIndentLevel();
                    assertNotNull(rt.getFontSize());
                    assertEquals(indents[indent], rt.getFontSize(), 0);
                }
            }
        }
        ppt.close();
    }
}