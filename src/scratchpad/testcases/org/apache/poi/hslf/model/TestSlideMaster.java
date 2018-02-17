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

import static org.apache.poi.hslf.record.TextHeaderAtom.BODY_TYPE;
import static org.apache.poi.hslf.record.TextHeaderAtom.CENTER_TITLE_TYPE;
import static org.apache.poi.hslf.record.TextHeaderAtom.CENTRE_BODY_TYPE;
import static org.apache.poi.hslf.record.TextHeaderAtom.TITLE_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.model.textproperties.CharFlagsTextProp;
import org.apache.poi.hslf.model.textproperties.TextProp;
import org.apache.poi.hslf.record.Environment;
import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.hslf.usermodel.HSLFMasterSheet;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideMaster;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hslf.usermodel.HSLFTextRun;
import org.apache.poi.hslf.usermodel.HSLFTitleMaster;
import org.junit.Test;

/**
 * Tests for SlideMaster
 */
public final class TestSlideMaster {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    /**
     * The reference ppt has two masters.
     * Check we can read their attributes.
     */
    @Test
    public void testSlideMaster() throws IOException {
        final HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("slide_master.ppt"));

        final Environment env = ppt.getDocumentRecord().getEnvironment();

        assertEquals(2, ppt.getSlideMasters().size());

        //character attributes
        assertEquals(40, getMasterVal(ppt, 0, TITLE_TYPE, "font.size", true));
        assertEquals(48, getMasterVal(ppt, 1, TITLE_TYPE, "font.size", true));

        int font1 = getMasterVal(ppt, 0, TITLE_TYPE, "font.index", true);
        int font2 = getMasterVal(ppt, 1, TITLE_TYPE, "font.index", true);
        assertEquals("Arial", env.getFontCollection().getFontInfo(font1).getTypeface());
        assertEquals("Georgia", env.getFontCollection().getFontInfo(font2).getTypeface());

        CharFlagsTextProp prop1 = getMasterProp(ppt, 0, TITLE_TYPE, "char_flags", true);
        assertEquals(false, prop1.getSubValue(CharFlagsTextProp.BOLD_IDX));
        assertEquals(false, prop1.getSubValue(CharFlagsTextProp.ITALIC_IDX));
        assertEquals(true, prop1.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));

        CharFlagsTextProp prop2 = getMasterProp(ppt, 1, TITLE_TYPE, "char_flags", true);
        assertEquals(false, prop2.getSubValue(CharFlagsTextProp.BOLD_IDX));
        assertEquals(true, prop2.getSubValue(CharFlagsTextProp.ITALIC_IDX));
        assertEquals(false, prop2.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));

        //now paragraph attributes
        assertEquals(0x266B, getMasterVal(ppt, 0, BODY_TYPE, "bullet.char", false));
        assertEquals(0x2022, getMasterVal(ppt, 1, BODY_TYPE, "bullet.char", false));

        int b1 = getMasterVal(ppt, 0, BODY_TYPE, "bullet.font", false);
        int b2 = getMasterVal(ppt, 1, BODY_TYPE, "bullet.font", false);
        assertEquals("Arial", env.getFontCollection().getFontInfo(b1).getTypeface());
        assertEquals("Georgia", env.getFontCollection().getFontInfo(b2).getTypeface());

        ppt.close();
    }

    @SuppressWarnings("unchecked")
    private static <T extends TextProp> T getMasterProp(HSLFSlideShow ppt, int masterIdx, int txtype, String propName, boolean isCharacter) {
        return (T)ppt.getSlideMasters().get(masterIdx).getPropCollection(txtype, 0, propName, isCharacter).findByName(propName);
    }

    private static int getMasterVal(HSLFSlideShow ppt, int masterIdx, int txtype, String propName, boolean isCharacter) {
        return getMasterProp(ppt, masterIdx, txtype, propName, isCharacter).getValue();
    }

    
    /**
     * Test we can read default text attributes for a title master sheet
     */
    @Test
    public void testTitleMasterTextAttributes() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("slide_master.ppt"));
        assertEquals(1, ppt.getTitleMasters().size());

        assertEquals(40, getMasterVal(ppt, 0, CENTER_TITLE_TYPE, "font.size", true));
        CharFlagsTextProp prop1 = getMasterProp(ppt, 0, CENTER_TITLE_TYPE, "char_flags", true);
        assertEquals(false, prop1.getSubValue(CharFlagsTextProp.BOLD_IDX));
        assertEquals(false, prop1.getSubValue(CharFlagsTextProp.ITALIC_IDX));
        assertEquals(true, prop1.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));

        assertEquals(32, getMasterVal(ppt, 0, CENTRE_BODY_TYPE, "font.size", true));
        CharFlagsTextProp prop2 = getMasterProp(ppt, 0, CENTRE_BODY_TYPE, "char_flags", true);
        assertEquals(false, prop2.getSubValue(CharFlagsTextProp.BOLD_IDX));
        assertEquals(false, prop2.getSubValue(CharFlagsTextProp.ITALIC_IDX));
        assertEquals(false, prop2.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));

        ppt.close();
    }

    /**
     * Slide 3 has title layout and follows the TitleMaster. Verify that.
     */
    @Test
    public void testTitleMaster() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("slide_master.ppt"));
        HSLFSlide slide = ppt.getSlides().get(2);
        HSLFMasterSheet masterSheet = slide.getMasterSheet();
        assertTrue(masterSheet instanceof HSLFTitleMaster);

        for (List<HSLFTextParagraph> txt : slide.getTextParagraphs()) {
            HSLFTextRun rt = txt.get(0).getTextRuns().get(0);
            switch(txt.get(0).getRunType()){
                case TextHeaderAtom.CENTER_TITLE_TYPE:
                    assertEquals("Arial", rt.getFontFamily());
                    assertEquals(32, rt.getFontSize(), 0);
                    assertEquals(true, rt.isBold());
                    assertEquals(true, rt.isUnderlined());
                    break;
                case TextHeaderAtom.CENTRE_BODY_TYPE:
                    assertEquals("Courier New", rt.getFontFamily());
                    assertEquals(20, rt.getFontSize(), 0);
                    assertEquals(true, rt.isBold());
                    assertEquals(false, rt.isUnderlined());
                    break;
            }

        }
        ppt.close();
    }

    /**
     * If a style attribute is not set ensure it is read from the master
     */
    @Test
    public void testMasterAttributes() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("slide_master.ppt"));
        List<HSLFSlide> slide = ppt.getSlides();
        assertEquals(3, slide.size());
        for (List<HSLFTextParagraph> tparas : slide.get(0).getTextParagraphs()) {
            HSLFTextParagraph tpara = tparas.get(0);
            if (tpara.getRunType() == TextHeaderAtom.TITLE_TYPE){
                HSLFTextRun rt = tpara.getTextRuns().get(0);
                assertEquals(40, rt.getFontSize(), 0);
                assertEquals(true, rt.isUnderlined());
                assertEquals("Arial", rt.getFontFamily());
            } else if (tpara.getRunType() == TextHeaderAtom.BODY_TYPE){
                HSLFTextRun rt = tpara.getTextRuns().get(0);
                assertEquals(0, tpara.getIndentLevel());
                assertEquals(32, rt.getFontSize(), 0);
                assertEquals("Arial", rt.getFontFamily());

                tpara = tparas.get(1);
                rt = tpara.getTextRuns().get(0);
                assertEquals(1, tpara.getIndentLevel());
                assertEquals(28, rt.getFontSize(), 0);
                assertEquals("Arial", rt.getFontFamily());

            }
        }

        for (List<HSLFTextParagraph> tparas : slide.get(1).getTextParagraphs()) {
            HSLFTextParagraph tpara = tparas.get(0);
            if (tpara.getRunType() == TextHeaderAtom.TITLE_TYPE){
                HSLFTextRun rt = tpara.getTextRuns().get(0);
                assertEquals(48, rt.getFontSize(), 0);
                assertEquals(true, rt.isItalic());
                assertEquals("Georgia", rt.getFontFamily());
            } else if (tpara.getRunType() == TextHeaderAtom.BODY_TYPE){
                HSLFTextRun rt;
                rt = tpara.getTextRuns().get(0);
                assertEquals(0, tpara.getIndentLevel());
                assertEquals(32, rt.getFontSize(), 0);
                assertEquals("Courier New", rt.getFontFamily());
            }
        }

        ppt.close();
    }

    /**
     * Check we can dynamically assign a slide master to a slide.
     */
    @Test
    public void testChangeSlideMaster() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("slide_master.ppt"));
        List<HSLFSlideMaster> master = ppt.getSlideMasters();
        List<HSLFSlide> slide = ppt.getSlides();
        int sheetNo;

        //each slide uses its own master
        assertEquals(slide.get(0).getMasterSheet()._getSheetNumber(), master.get(0)._getSheetNumber());
        assertEquals(slide.get(1).getMasterSheet()._getSheetNumber(), master.get(1)._getSheetNumber());

        //all slides use the first master slide
        sheetNo = master.get(0)._getSheetNumber();
        for (HSLFSlide s : slide) {
            s.setMasterSheet(master.get(0));
        }

        ByteArrayOutputStream out;

        out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new HSLFSlideShowImpl(new ByteArrayInputStream(out.toByteArray())));
        master = ppt.getSlideMasters();
        slide = ppt.getSlides();
        for (HSLFSlide s : slide) {
            assertEquals(sheetNo, s.getMasterSheet()._getSheetNumber());
        }

        ppt.close();
    }

    /**
     * Varify we can read attrubutes for different identtation levels.
     * (typical for the "bullted body" placeholder)
     */
    @Test
    public void testIndentation() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("slide_master.ppt"));
        HSLFSlide slide = ppt.getSlides().get(0);

        for (List<HSLFTextParagraph> tparas : slide.getTextParagraphs()) {
            HSLFTextParagraph tpara = tparas.get(0);
            if (tpara.getRunType() == TextHeaderAtom.TITLE_TYPE){
                HSLFTextRun rt = tpara.getTextRuns().get(0);
                assertEquals(40, rt.getFontSize(), 0);
                assertEquals(true, rt.isUnderlined());
                assertEquals("Arial", rt.getFontFamily());
            } else if (tpara.getRunType() == TextHeaderAtom.BODY_TYPE){
                int indents[] = { 32, 28, 24 };
                for (HSLFTextRun rt : tpara.getTextRuns()) {
                    int indent = tpara.getIndentLevel();
                    assertEquals(indents[indent], rt.getFontSize(), 0);
                }
            }
        }
        ppt.close();
    }
}