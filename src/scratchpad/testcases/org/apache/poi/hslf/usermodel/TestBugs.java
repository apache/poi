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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.exceptions.OldPowerPointFormatException;
import org.apache.poi.hslf.model.Background;
import org.apache.poi.hslf.model.Fill;
import org.apache.poi.hslf.model.MasterSheet;
import org.apache.poi.hslf.model.Notes;
import org.apache.poi.hslf.model.Picture;
import org.apache.poi.hslf.model.Shape;
import org.apache.poi.hslf.model.ShapeGroup;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.SlideMaster;
import org.apache.poi.hslf.model.TextBox;
import org.apache.poi.hslf.model.TextRun;
import org.apache.poi.hslf.model.TextShape;
import org.apache.poi.hslf.model.TitleMaster;
import org.apache.poi.POIDataSamples;

/**
 * Testcases for bugs entered in bugzilla
 * the Test name contains the bugzilla bug id
 *
 * @author Yegor Kozlov
 */
public final class TestBugs extends TestCase {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    /**
     * Bug 41384: Array index wrong in record creation
     */
    public void test41384() throws Exception {
        HSLFSlideShow hslf = new HSLFSlideShow(_slTests.openResourceAsStream("41384.ppt"));

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
        HSLFSlideShow hslf = new HSLFSlideShow(_slTests.openResourceAsStream("42474-1.ppt"));

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
        HSLFSlideShow hslf = new HSLFSlideShow(_slTests.openResourceAsStream("42474-2.ppt"));

        SlideShow ppt = new SlideShow(hslf);

        //map slide number and starting phrase of its notes
        Map<Integer, String> notesMap = new HashMap<Integer, String>();
        notesMap.put(Integer.valueOf(4), "For  decades before calculators");
        notesMap.put(Integer.valueOf(5), "Several commercial applications");
        notesMap.put(Integer.valueOf(6), "There are three variations of LNS that are discussed here");
        notesMap.put(Integer.valueOf(7), "Although multiply and square root are easier");
        notesMap.put(Integer.valueOf(8), "The bus Z is split into Z_H and Z_L");

        Slide[] slide = ppt.getSlides();
        for (int i = 0; i < slide.length; i++) {
            Integer slideNumber = Integer.valueOf(slide[i].getSlideNumber());
            Notes notes = slide[i].getNotesSheet();
            if (notesMap.containsKey(slideNumber)){
                assertNotNull(notes);
                String text = notes.getTextRuns()[0].getRawText();
                String startingPhrase = notesMap.get(slideNumber);
                assertTrue("Notes for slide " + slideNumber + " must start with " +
                        startingPhrase , text.startsWith(startingPhrase));
            }
        }
    }

    /**
     * Bug 42485: All TextBoxes inside ShapeGroups have null TextRuns
     */
    public void test42485 () throws Exception {
        HSLFSlideShow hslf = new HSLFSlideShow(_slTests.openResourceAsStream("42485.ppt"));

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
        HSLFSlideShow hslf = new HSLFSlideShow(_slTests.openResourceAsStream("42485.ppt"));

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
        HSLFSlideShow hslf = new HSLFSlideShow(_slTests.openResourceAsStream("alterman_security.ppt"));

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

    /**
     * Bug 42486:  Failure parsing a seemingly valid PPT
     */
    public void test42486 () throws Exception {
        HSLFSlideShow hslf = new HSLFSlideShow(_slTests.openResourceAsStream("42486.ppt"));

        SlideShow ppt = new SlideShow(hslf);
        Slide[] slide = ppt.getSlides();
        for (int i = 0; i < slide.length; i++) {
            Shape[] shape = slide[i].getShapes();
        }
        assertTrue("No Exceptions while reading file", true);

    }

    /**
     * Bug 42524:  NPE in Shape.getShapeType()
     */
    public void test42524 () throws Exception {
        HSLFSlideShow hslf = new HSLFSlideShow(_slTests.openResourceAsStream("42486.ppt"));

        SlideShow ppt = new SlideShow(hslf);
        //walk down the tree and see if there were no errors while reading
        Slide[] slide = ppt.getSlides();
        for (int i = 0; i < slide.length; i++) {
            Shape[] shape = slide[i].getShapes();
            for (int j = 0; j < shape.length; j++) {
                assertNotNull(shape[j].getShapeName());
                if (shape[j] instanceof ShapeGroup){
                    ShapeGroup group = (ShapeGroup)shape[j];
                    Shape[] comps = group.getShapes();
                    for (int k = 0; k < comps.length; k++) {
                        assertNotNull(comps[k].getShapeName());
                   }
                }
            }

        }
        assertTrue("No Exceptions while reading file", true);

    }

    /**
     * Bug 42520:  NPE in Picture.getPictureData()
     */
    public void test42520 () throws Exception {
        HSLFSlideShow hslf = new HSLFSlideShow(_slTests.openResourceAsStream("42520.ppt"));

        SlideShow ppt = new SlideShow(hslf);

        //test case from the bug report
        ShapeGroup shapeGroup = (ShapeGroup)ppt.getSlides()[11].getShapes()[10];
        Picture picture = (Picture)shapeGroup.getShapes()[0];
        picture.getPictureData();

        //walk down the tree and see if there were no errors while reading
        Slide[] slide = ppt.getSlides();
        for (int i = 0; i < slide.length; i++) {
            Shape[] shape = slide[i].getShapes();
            for (int j = 0; j < shape.length; j++) {
              if (shape[j] instanceof ShapeGroup){
                    ShapeGroup group = (ShapeGroup)shape[j];
                    Shape[] comps = group.getShapes();
                    for (int k = 0; k < comps.length; k++) {
                        Shape comp = comps[k];
                        if (comp instanceof Picture){
                            PictureData pict = ((Picture)comp).getPictureData();
                        }
                    }
                }
            }

        }
        assertTrue("No Exceptions while reading file", true);

    }

    /**
     * Bug 38256:  RuntimeException: Couldn't instantiate the class for type with id 0.
     * ( also fixed followup: getTextRuns() returns no text )
     */
    public void test38256 () throws Exception {
        SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("38256.ppt"));

        assertTrue("No Exceptions while reading file", true);

        Slide[] slide = ppt.getSlides();
        assertEquals(1, slide.length);
        TextRun[] runs = slide[0].getTextRuns();
        assertEquals(4, runs.length);

        Set<String> txt = new HashSet<String>();
        txt.add("\u201CHAPPY BIRTHDAY SCOTT\u201D");
        txt.add("Have a HAPPY DAY");
        txt.add("PS Nobody is allowed to hassle Scott TODAY\u2026");
        txt.add("Drinks will be in the Boardroom at 5pm today to celebrate Scott\u2019s B\u2019Day\u2026  See you all there!");

        for (int i = 0; i < runs.length; i++) {
            String text = runs[i].getRawText();
            assertTrue(text, txt.contains(text));
        }

    }

    /**
     * Bug 38256:  RuntimeException: Couldn't instantiate the class for type with id 0.
     * ( also fixed followup: getTextRuns() returns no text )
     */
    public void test43781 () throws Exception {
        SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("43781.ppt"));

        assertTrue("No Exceptions while reading file", true);

        Slide slide = ppt.getSlides()[0];
        TextRun[] tr1 = slide.getTextRuns();

        List<TextRun> lst = new ArrayList<TextRun>();
        Shape[] shape = slide.getShapes();
        for (int i = 0; i < shape.length; i++) {
            if( shape[i] instanceof TextShape){
                TextRun textRun = ((TextShape)shape[i]).getTextRun();
                if(textRun != null) {
                    lst.add(textRun);
                }
            }

        }
        TextRun[] tr2 = new TextRun[lst.size()];
        lst.toArray(tr2);

        assertEquals(tr1.length, tr2.length);
        for (int i = 0; i < tr1.length; i++) {
            assertEquals(tr1[i].getText(), tr2[i].getText());
        }
    }

    /**
     * Bug 44296: HSLF Not Extracting Slide Background Image
     */
    public void test44296  () throws Exception {
        SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("44296.ppt"));

        Slide slide = ppt.getSlides()[0];

        Background b = slide.getBackground();
        Fill f = b.getFill();
        assertEquals(Fill.FILL_PICTURE, f.getFillType());

        PictureData pict = f.getPictureData();
        assertNotNull(pict);
        assertEquals(Picture.JPEG, pict.getType());
    }

    /**
     * Bug 44770: java.lang.RuntimeException: Couldn't instantiate the class for type with id 1036 on class class org.apache.poi.hslf.record.PPDrawing
     */
    public void test44770() throws Exception {
        try {
             new SlideShow(_slTests.openResourceAsStream("44770.ppt"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Couldn't instantiate the class for type with id 1036 on class class org.apache.poi.hslf.record.PPDrawing")) {
                throw new AssertionFailedError("Identified bug 44770");
            }
            throw e;
        }
    }

    /**
     * Bug 41071: Will not extract text from Powerpoint TextBoxes
     */
    public void test41071() throws Exception {
        SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("41071.ppt"));

        Slide slide = ppt.getSlides()[0];
        Shape[] sh = slide.getShapes();
        assertEquals(1, sh.length);
        assertTrue(sh[0] instanceof TextShape);
        TextShape tx = (TextShape)sh[0];
        assertEquals("Fundera, planera och involvera.", tx.getTextRun().getText());

        TextRun[] run = slide.getTextRuns();
        assertEquals(1, run.length);
        assertEquals("Fundera, planera och involvera.", run[0].getText());
    }

    /**
     * PowerPoint 95 files should throw a more helpful exception
     * @throws Exception
     */
    public void test41711() throws Exception {
    	// New file is fine
        new SlideShow(_slTests.openResourceAsStream("SampleShow.ppt"));

        // PowerPoint 95 gives an old format exception
        try {
        	new SlideShow(_slTests.openResourceAsStream("PPT95.ppt"));
        	fail("OldPowerPointFormatException should've been thrown");
        } catch(OldPowerPointFormatException e) {
        	// Good
        }
    }
}
