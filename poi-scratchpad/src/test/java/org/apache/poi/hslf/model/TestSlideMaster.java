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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.model.textproperties.CharFlagsTextProp;
import org.apache.poi.hslf.record.Environment;
import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.POIDataSamples;

/**
 * Tests for SlideMaster
 *
 * @author Yegor Kozlov
 */
public final class TestSlideMaster extends TestCase{
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    /**
     * The reference ppt has two masters.
     * Check we can read their attributes.
     */
    public void testSlideMaster() throws Exception {
        SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("slide_master.ppt"));

        Environment env = ppt.getDocumentRecord().getEnvironment();

        SlideMaster[] master = ppt.getSlidesMasters();
        assertEquals(2, master.length);

        //character attributes
        assertEquals(40, master[0].getStyleAttribute(TextHeaderAtom.TITLE_TYPE, 0, "font.size", true).getValue());
        assertEquals(48, master[1].getStyleAttribute(TextHeaderAtom.TITLE_TYPE, 0, "font.size", true).getValue());

        int font1 = master[0].getStyleAttribute(TextHeaderAtom.TITLE_TYPE, 0, "font.index", true).getValue();
        int font2 = master[1].getStyleAttribute(TextHeaderAtom.TITLE_TYPE, 0, "font.index", true).getValue();
        assertEquals("Arial", env.getFontCollection().getFontWithId(font1));
        assertEquals("Georgia", env.getFontCollection().getFontWithId(font2));

        CharFlagsTextProp prop1 = (CharFlagsTextProp)master[0].getStyleAttribute(TextHeaderAtom.TITLE_TYPE, 0, "char_flags", true);
        assertEquals(false, prop1.getSubValue(CharFlagsTextProp.BOLD_IDX));
        assertEquals(false, prop1.getSubValue(CharFlagsTextProp.ITALIC_IDX));
        assertEquals(true, prop1.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));

        CharFlagsTextProp prop2 = (CharFlagsTextProp)master[1].getStyleAttribute(TextHeaderAtom.TITLE_TYPE, 0, "char_flags", true);
        assertEquals(false, prop2.getSubValue(CharFlagsTextProp.BOLD_IDX));
        assertEquals(true, prop2.getSubValue(CharFlagsTextProp.ITALIC_IDX));
        assertEquals(false, prop2.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));

        //now paragraph attributes
        assertEquals(0x266B, master[0].getStyleAttribute(TextHeaderAtom.BODY_TYPE, 0, "bullet.char", false).getValue());
        assertEquals(0x2022, master[1].getStyleAttribute(TextHeaderAtom.BODY_TYPE, 0, "bullet.char", false).getValue());

        int b1 = master[0].getStyleAttribute(TextHeaderAtom.BODY_TYPE, 0, "bullet.font", false).getValue();
        int b2 = master[1].getStyleAttribute(TextHeaderAtom.BODY_TYPE, 0, "bullet.font", false).getValue();
        assertEquals("Arial", env.getFontCollection().getFontWithId(b1));
        assertEquals("Georgia", env.getFontCollection().getFontWithId(b2));
    }

    /**
     * Test we can read default text attributes for a title master sheet
     */
    public void testTitleMasterTextAttributes() throws Exception {
        SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("slide_master.ppt"));
        TitleMaster[] master = ppt.getTitleMasters();
        assertEquals(1, master.length);

        assertEquals(32, master[0].getStyleAttribute(TextHeaderAtom.CENTER_TITLE_TYPE, 0, "font.size", true).getValue());
        CharFlagsTextProp prop1 = (CharFlagsTextProp)master[0].getStyleAttribute(TextHeaderAtom.CENTER_TITLE_TYPE, 0, "char_flags", true);
        assertEquals(true, prop1.getSubValue(CharFlagsTextProp.BOLD_IDX));
        assertEquals(false, prop1.getSubValue(CharFlagsTextProp.ITALIC_IDX));
        assertEquals(true, prop1.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));

        assertEquals(20, master[0].getStyleAttribute(TextHeaderAtom.CENTRE_BODY_TYPE, 0, "font.size", true).getValue());
        CharFlagsTextProp prop2 = (CharFlagsTextProp)master[0].getStyleAttribute(TextHeaderAtom.CENTRE_BODY_TYPE, 0, "char_flags", true);
        assertEquals(true, prop2.getSubValue(CharFlagsTextProp.BOLD_IDX));
        assertEquals(false, prop2.getSubValue(CharFlagsTextProp.ITALIC_IDX));
        assertEquals(false, prop2.getSubValue(CharFlagsTextProp.UNDERLINE_IDX));
    }

    /**
     * Slide 3 has title layout and follows the TitleMaster. Verify that.
     */
    public void testTitleMaster() throws Exception {
        SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("slide_master.ppt"));
        Slide slide = ppt.getSlides()[2];
        MasterSheet masterSheet = slide.getMasterSheet();
        assertTrue(masterSheet instanceof TitleMaster);

        TextRun[] txt = slide.getTextRuns();
        for (int i = 0; i < txt.length; i++) {
            RichTextRun rt = txt[i].getRichTextRuns()[0];
            switch(txt[i].getRunType()){
                case TextHeaderAtom.CENTER_TITLE_TYPE:
                    assertEquals("Arial", rt.getFontName());
                    assertEquals(32, rt.getFontSize());
                    assertEquals(true, rt.isBold());
                    assertEquals(true, rt.isUnderlined());
                    break;
                case TextHeaderAtom.CENTRE_BODY_TYPE:
                    assertEquals("Courier New", rt.getFontName());
                    assertEquals(20, rt.getFontSize());
                    assertEquals(true, rt.isBold());
                    assertEquals(false, rt.isUnderlined());
                    break;
            }

        }
    }
    /**
     * If a style attribute is not set ensure it is read from the master
     */
    public void testMasterAttributes() throws Exception {
        SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("slide_master.ppt"));
        Slide[] slide = ppt.getSlides();
        assertEquals(3, slide.length);
        TextRun[] trun;

        trun = slide[0].getTextRuns();
        for (int i = 0; i < trun.length; i++) {
            if (trun[i].getRunType() == TextHeaderAtom.TITLE_TYPE){
                RichTextRun rt = trun[i].getRichTextRuns()[0];
                assertEquals(40, rt.getFontSize());
                assertEquals(true, rt.isUnderlined());
                assertEquals("Arial", rt.getFontName());
            } else if (trun[i].getRunType() == TextHeaderAtom.BODY_TYPE){
                RichTextRun rt;
                rt = trun[i].getRichTextRuns()[0];
                assertEquals(0, rt.getIndentLevel());
                assertEquals(32, rt.getFontSize());
                assertEquals("Arial", rt.getFontName());

                rt = trun[i].getRichTextRuns()[1];
                assertEquals(1, rt.getIndentLevel());
                assertEquals(28, rt.getFontSize());
                assertEquals("Arial", rt.getFontName());

            }
        }

        trun = slide[1].getTextRuns();
        for (int i = 0; i < trun.length; i++) {
            if (trun[i].getRunType() == TextHeaderAtom.TITLE_TYPE){
                RichTextRun rt = trun[i].getRichTextRuns()[0];
                assertEquals(48, rt.getFontSize());
                assertEquals(true, rt.isItalic());
                assertEquals("Georgia", rt.getFontName());
            } else if (trun[i].getRunType() == TextHeaderAtom.BODY_TYPE){
                RichTextRun rt;
                rt = trun[i].getRichTextRuns()[0];
                assertEquals(0, rt.getIndentLevel());
                assertEquals(32, rt.getFontSize());
                assertEquals("Courier New", rt.getFontName());
            }
        }

    }

    /**
     * Check we can dynamically assign a slide master to a slide.
     */
    public void testChangeSlideMaster() throws Exception {
        SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("slide_master.ppt"));
        SlideMaster[] master = ppt.getSlidesMasters();
        Slide[] slide = ppt.getSlides();
        int sheetNo;

        //each slide uses its own master
        assertEquals(slide[0].getMasterSheet()._getSheetNumber(), master[0]._getSheetNumber());
        assertEquals(slide[1].getMasterSheet()._getSheetNumber(), master[1]._getSheetNumber());

        //all slides use the first master slide
        sheetNo = master[0]._getSheetNumber();
        for (int i = 0; i < slide.length; i++) {
            slide[i].setMasterSheet(master[0]);
        }

        ByteArrayOutputStream out;

        out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new SlideShow(new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray())));
        master = ppt.getSlidesMasters();
        slide = ppt.getSlides();
        for (int i = 0; i < slide.length; i++) {
            assertEquals(sheetNo, slide[i].getMasterSheet()._getSheetNumber());
        }
    }

    /**
     * Varify we can read attrubutes for different identtation levels.
     * (typical for the "bullted body" placeholder)
     */
    public void testIndentation() throws Exception {
        SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("slide_master.ppt"));
        Slide slide = ppt.getSlides()[0];
        TextRun[] trun;

        trun = slide.getTextRuns();
        for (int i = 0; i < trun.length; i++) {
            if (trun[i].getRunType() == TextHeaderAtom.TITLE_TYPE){
                RichTextRun rt = trun[i].getRichTextRuns()[0];
                assertEquals(40, rt.getFontSize());
                assertEquals(true, rt.isUnderlined());
                assertEquals("Arial", rt.getFontName());
            } else if (trun[i].getRunType() == TextHeaderAtom.BODY_TYPE){
                RichTextRun[] rt = trun[i].getRichTextRuns();
                for (int j = 0; j < rt.length; j++) {
                    int indent = rt[j].getIndentLevel();
                    switch (indent){
                        case 0:
                            assertEquals(32, rt[j].getFontSize());
                            break;
                        case 1:
                            assertEquals(28, rt[j].getFontSize());
                            break;
                        case 2:
                            assertEquals(24, rt[j].getFontSize());
                            break;
                    }
                }
            }
        }

    }

}
