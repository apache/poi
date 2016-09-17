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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.model.textproperties.CharFlagsTextProp;
import org.apache.poi.hslf.record.Environment;
import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.hslf.usermodel.*;
import org.junit.Test;

/**
 * Tests for SlideMaster
 *
 * @author Yegor Kozlov
 */
public final class TestSlideMaster {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    /**
     * The reference ppt has two masters.
     * Check we can read their attributes.
     */
    @Test
    public void testSlideMaster() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("slide_master.ppt"));

        Environment env = ppt.getDocumentRecord().getEnvironment();

        List<HSLFSlideMaster> master = ppt.getSlideMasters();
        assertEquals(2, master.size());

        //character attributes
        assertEquals(40, master.get(0).getStyleAttribute(TextHeaderAtom.TITLE_TYPE, 0, "font.size", true).getValue());
        assertEquals(48, master.get(1).getStyleAttribute(TextHeaderAtom.TITLE_TYPE, 0, "font.size", true).getValue());

        int font1 = master.get(0).getStyleAttribute(TextHeaderAtom.TITLE_TYPE, 0, "font.index", true).getValue();
        int font2 = master.get(1).getStyleAttribute(TextHeaderAtom.TITLE_TYPE, 0, "font.index", true).getValue();
        assertEquals("Arial", env.getFontCollection().getFontWithId(font1));
        assertEquals("Georgia", env.getFontCollection().getFontWithId(font2));

        CharFlagsTextProp prop1 = (CharFlagsTextProp)master.get(0).getStyleAttribute(TextHeaderAtom.TITLE_TYPE, 0, "char_flags", true);
        assertEquals(false, prop1.getSubValue(CharFlagsTextProp.BOLD_IDX));
        assertEquals(false, prop1.getSubValue(CharFlagsTextProp.ITALIC_IDX));
        assertEquals(true, prop1.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));

        CharFlagsTextProp prop2 = (CharFlagsTextProp)master.get(1).getStyleAttribute(TextHeaderAtom.TITLE_TYPE, 0, "char_flags", true);
        assertEquals(false, prop2.getSubValue(CharFlagsTextProp.BOLD_IDX));
        assertEquals(true, prop2.getSubValue(CharFlagsTextProp.ITALIC_IDX));
        assertEquals(false, prop2.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));

        //now paragraph attributes
        assertEquals(0x266B, master.get(0).getStyleAttribute(TextHeaderAtom.BODY_TYPE, 0, "bullet.char", false).getValue());
        assertEquals(0x2022, master.get(1).getStyleAttribute(TextHeaderAtom.BODY_TYPE, 0, "bullet.char", false).getValue());

        int b1 = master.get(0).getStyleAttribute(TextHeaderAtom.BODY_TYPE, 0, "bullet.font", false).getValue();
        int b2 = master.get(1).getStyleAttribute(TextHeaderAtom.BODY_TYPE, 0, "bullet.font", false).getValue();
        assertEquals("Arial", env.getFontCollection().getFontWithId(b1));
        assertEquals("Georgia", env.getFontCollection().getFontWithId(b2));
    }

    /**
     * Test we can read default text attributes for a title master sheet
     */
    @Test
    public void testTitleMasterTextAttributes() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("slide_master.ppt"));
        List<HSLFTitleMaster> master = ppt.getTitleMasters();
        assertEquals(1, master.size());

        assertEquals(32, master.get(0).getStyleAttribute(TextHeaderAtom.CENTER_TITLE_TYPE, 0, "font.size", true).getValue());
        CharFlagsTextProp prop1 = (CharFlagsTextProp)master.get(0).getStyleAttribute(TextHeaderAtom.CENTER_TITLE_TYPE, 0, "char_flags", true);
        assertEquals(true, prop1.getSubValue(CharFlagsTextProp.BOLD_IDX));
        assertEquals(false, prop1.getSubValue(CharFlagsTextProp.ITALIC_IDX));
        assertEquals(true, prop1.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));

        assertEquals(20, master.get(0).getStyleAttribute(TextHeaderAtom.CENTRE_BODY_TYPE, 0, "font.size", true).getValue());
        CharFlagsTextProp prop2 = (CharFlagsTextProp)master.get(0).getStyleAttribute(TextHeaderAtom.CENTRE_BODY_TYPE, 0, "char_flags", true);
        assertEquals(true, prop2.getSubValue(CharFlagsTextProp.BOLD_IDX));
        assertEquals(false, prop2.getSubValue(CharFlagsTextProp.ITALIC_IDX));
        assertEquals(false, prop2.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));
    }

    /**
     * Slide 3 has title layout and follows the TitleMaster. Verify that.
     */
    @Test
    public void testTitleMaster() throws Exception {
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

    }

    /**
     * Check we can dynamically assign a slide master to a slide.
     */
    @Test
    public void testChangeSlideMaster() throws Exception {
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
    }

    /**
     * Varify we can read attrubutes for different identtation levels.
     * (typical for the "bullted body" placeholder)
     */
    @Test
    public void testIndentation() throws Exception {
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

    }

}
