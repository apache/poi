
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
package org.apache.poi.hslf.usermodel;

import junit.framework.TestCase;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.model.*;

import java.io.*;
import java.util.HashSet;
import java.util.HashMap;

/**
 * Testcases for bugs entered in bugzilla
 * the Test name contains the bugzilla bug id
 *
 * @author Yegor Kozlov
 */
public class TestBugs extends TestCase {
    protected String cwd = System.getProperty("HSLF.testdata.path");

    /**
     * Bug 41384: Array index wrong in record creation
     */
    public void test41384() throws Exception {
        FileInputStream is = new FileInputStream(new File(cwd, "41384.ppt"));
        HSLFSlideShow hslf = new HSLFSlideShow(is);
        is.close();

        SlideShow ppt = new SlideShow(hslf);
        assertTrue("No Exceptions while reading file", true);

        assertEquals(1, ppt.getSlides().length);

        PictureData[] pict = ppt.getPictureData();
        assertEquals(2, pict.length);
        assertEquals(Picture.JPEG, pict[0].getType());
        assertEquals(Picture.JPEG, pict[1].getType());
    }

    /**
     * First fix from Bug 42474: NPE in RichTextRun.isBold()
     * when the RichTextRun comes from a Notes model object
     */
    public void test42474_1() throws Exception {
        FileInputStream is = new FileInputStream(new File(cwd, "42474-1.ppt"));
        HSLFSlideShow hslf = new HSLFSlideShow(is);
        is.close();

        SlideShow ppt = new SlideShow(hslf);
        assertTrue("No Exceptions while reading file", true);
        assertEquals(2, ppt.getSlides().length);

        TextRun txrun;
        Notes notes;

        notes = ppt.getSlides()[0].getNotesSheet();
        assertNotNull(notes);
        txrun = notes.getTextRuns()[0];
        assertEquals("Notes-1", txrun.getRawText());
        assertEquals(false, txrun.getRichTextRuns()[0].isBold());

        //notes for the second slide are in bold
        notes = ppt.getSlides()[1].getNotesSheet();
        assertNotNull(notes);
        txrun = notes.getTextRuns()[0];
        assertEquals("Notes-2", txrun.getRawText());
        assertEquals(true, txrun.getRichTextRuns()[0].isBold());

    }

    /**
     * Second fix from Bug 42474: Incorrect matching of notes to slides
     */
    public void test42474_2() throws Exception {
        FileInputStream is = new FileInputStream(new File(cwd, "42474-2.ppt"));
        HSLFSlideShow hslf = new HSLFSlideShow(is);
        is.close();

        SlideShow ppt = new SlideShow(hslf);

        //map slide number and starting phrase of its notes
        HashMap notesMap = new HashMap();
        notesMap.put(new Integer(4), "For  decades before calculators");
        notesMap.put(new Integer(5), "Several commercial applications");
        notesMap.put(new Integer(6), "There are three variations of LNS that are discussed here");
        notesMap.put(new Integer(7), "Although multiply and square root are easier");
        notesMap.put(new Integer(8), "The bus Z is split into Z_H and Z_L");

        Slide[] slide = ppt.getSlides();
        for (int i = 0; i < slide.length; i++) {
            Integer slideNumber = new Integer(slide[i].getSlideNumber());
            Notes notes = slide[i].getNotesSheet();
            if (notesMap.containsKey(slideNumber)){
                assertNotNull(notes);
                String text = notes.getTextRuns()[0].getRawText();
                String startingPhrase = (String)notesMap.get(slideNumber);
                assertTrue("Notes for slide " + slideNumber + " must start with " +
                        startingPhrase , text.startsWith(startingPhrase));
            }
        }
    }

    /**
     * Bug 42485: All TextBoxes inside ShapeGroups have null TextRuns
     */
    public void test42485 () throws Exception {
        FileInputStream is = new FileInputStream(new File(cwd, "42485.ppt"));
        HSLFSlideShow hslf = new HSLFSlideShow(is);
        is.close();

        SlideShow ppt = new SlideShow(hslf);
        Shape[] shape = ppt.getSlides()[0].getShapes();
        for (int i = 0; i < shape.length; i++) {
            if(shape[i] instanceof ShapeGroup){
                ShapeGroup  group = (ShapeGroup)shape[i];
                Shape[] sh = group.getShapes();
                for (int j = 0; j < sh.length; j++) {
                    if( sh[j] instanceof TextBox){
                        TextBox txt = (TextBox)sh[j];
                        assertNotNull(txt.getTextRun());
                    }
                }
            }
        }
    }

    /**
     * Bug 42484: NullPointerException from ShapeGroup.getAnchor()
     */
    public void test42484 () throws Exception {
        FileInputStream is = new FileInputStream(new File(cwd, "42485.ppt")); //test file is the same as for bug 42485
        HSLFSlideShow hslf = new HSLFSlideShow(is);
        is.close();

        SlideShow ppt = new SlideShow(hslf);
        Shape[] shape = ppt.getSlides()[0].getShapes();
        for (int i = 0; i < shape.length; i++) {
            if(shape[i] instanceof ShapeGroup){
                ShapeGroup  group = (ShapeGroup)shape[i];
                assertNotNull(group.getAnchor());
                Shape[] sh = group.getShapes();
                for (int j = 0; j < sh.length; j++) {
                    assertNotNull(sh[j].getAnchor());
                }
            }
        }
        assertTrue("No Exceptions while reading file", true);
    }

    /**
     * Bug 41381: Exception from Slide.getMasterSheet() on a seemingly valid PPT file
     */
    public void test41381() throws Exception {
        FileInputStream is = new FileInputStream(new File(cwd, "alterman_security.ppt"));
        HSLFSlideShow hslf = new HSLFSlideShow(is);
        is.close();

        SlideShow ppt = new SlideShow(hslf);
        assertTrue("No Exceptions while reading file", true);

        assertEquals(1, ppt.getSlidesMasters().length);
        assertEquals(1, ppt.getTitleMasters().length);
        Slide[] slide = ppt.getSlides();
        for (int i = 0; i < slide.length; i++) {
            MasterSheet master = slide[i].getMasterSheet();
            if (i == 0) assertTrue(master instanceof TitleMaster); //the first slide follows TitleMaster
            else assertTrue(master instanceof SlideMaster);
        }
    }

}
