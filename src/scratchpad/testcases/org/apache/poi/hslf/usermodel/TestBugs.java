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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherArrayProperty;
import org.apache.poi.ddf.EscherColorRef;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.hslf.HSLFTestDataSamples;
import org.apache.poi.hslf.exceptions.OldPowerPointFormatException;
import org.apache.poi.hslf.model.HeadersFooters;
import org.apache.poi.hslf.record.Document;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.SlideListWithText;
import org.apache.poi.hslf.record.SlideListWithText.SlideAtomsSet;
import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.sl.usermodel.TextBox;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextRun;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;
import org.apache.poi.util.Units;
import org.junit.Test;

import junit.framework.AssertionFailedError;

/**
 * Testcases for bugs entered in bugzilla
 * the Test name contains the bugzilla bug id
 *
 * @author Yegor Kozlov
 */
public final class TestBugs {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    /**
     * Bug 41384: Array index wrong in record creation
     */
    @Test
    public void bug41384() throws Exception {
        HSLFSlideShowImpl hslf = new HSLFSlideShowImpl(_slTests.openResourceAsStream("41384.ppt"));

        HSLFSlideShow ppt = new HSLFSlideShow(hslf);
        assertTrue("No Exceptions while reading file", true);

        assertEquals(1, ppt.getSlides().size());

        List<HSLFPictureData> pict = ppt.getPictureData();
        assertEquals(2, pict.size());
        assertEquals(PictureType.JPEG, pict.get(0).getType());
        assertEquals(PictureType.JPEG, pict.get(1).getType());
    }

    /**
     * First fix from Bug 42474: NPE in RichTextRun.isBold()
     * when the RichTextRun comes from a Notes model object
     */
    @Test
    public void bug42474_1() throws Exception {
        HSLFSlideShowImpl hslf = new HSLFSlideShowImpl(_slTests.openResourceAsStream("42474-1.ppt"));

        HSLFSlideShow ppt = new HSLFSlideShow(hslf);
        assertTrue("No Exceptions while reading file", true);
        assertEquals(2, ppt.getSlides().size());

        List<HSLFTextParagraph> txrun;
        HSLFNotes notes;

        notes = ppt.getSlides().get(0).getNotes();
        assertNotNull(notes);
        txrun = notes.getTextParagraphs().get(0);
        assertEquals("Notes-1", HSLFTextParagraph.getRawText(txrun));
        assertEquals(false, txrun.get(0).getTextRuns().get(0).isBold());

        //notes for the second slide are in bold
        notes = ppt.getSlides().get(1).getNotes();
        assertNotNull(notes);
        txrun = notes.getTextParagraphs().get(0);
        assertEquals("Notes-2", HSLFTextParagraph.getRawText(txrun));
        assertEquals(true, txrun.get(0).getTextRuns().get(0).isBold());

    }

    /**
     * Second fix from Bug 42474: Incorrect matching of notes to slides
     */
    @Test
    public void bug42474_2() throws Exception {
        HSLFSlideShowImpl hslf = new HSLFSlideShowImpl(_slTests.openResourceAsStream("42474-2.ppt"));

        HSLFSlideShow ppt = new HSLFSlideShow(hslf);

        //map slide number and starting phrase of its notes
        Map<Integer, String> notesMap = new HashMap<Integer, String>();
        notesMap.put(Integer.valueOf(4), "For  decades before calculators");
        notesMap.put(Integer.valueOf(5), "Several commercial applications");
        notesMap.put(Integer.valueOf(6), "There are three variations of LNS that are discussed here");
        notesMap.put(Integer.valueOf(7), "Although multiply and square root are easier");
        notesMap.put(Integer.valueOf(8), "The bus Z is split into Z_H and Z_L");

        for (HSLFSlide slide : ppt.getSlides()) {
            Integer slideNumber = Integer.valueOf(slide.getSlideNumber());
            HSLFNotes notes = slide.getNotes();
            if (notesMap.containsKey(slideNumber)){
                assertNotNull(notes);
                String text = HSLFTextParagraph.getRawText(notes.getTextParagraphs().get(0));
                String startingPhrase = notesMap.get(slideNumber);
                assertTrue("Notes for slide " + slideNumber + " must start with " +
                        startingPhrase , text.startsWith(startingPhrase));
            }
        }
    }

    /**
     * Bug 42485: All TextBoxes inside ShapeGroups have null TextRuns
     */
    @Test
    public void bug42485 () throws Exception {
        HSLFSlideShowImpl hslf = new HSLFSlideShowImpl(_slTests.openResourceAsStream("42485.ppt"));

        HSLFSlideShow ppt = new HSLFSlideShow(hslf);
        for (HSLFShape shape : ppt.getSlides().get(0).getShapes()) {
            if(shape instanceof HSLFGroupShape){
                HSLFGroupShape group = (HSLFGroupShape)shape;
                for (HSLFShape sh : group.getShapes()) {
                    if(sh instanceof HSLFTextBox){
                        HSLFTextBox txt = (HSLFTextBox)sh;
                        assertNotNull(txt.getTextParagraphs());
                    }
                }
            }
        }
    }

    /**
     * Bug 42484: NullPointerException from ShapeGroup.getAnchor()
     */
    @Test
    public void bug42484 () throws Exception {
        HSLFSlideShowImpl hslf = new HSLFSlideShowImpl(_slTests.openResourceAsStream("42485.ppt"));

        HSLFSlideShow ppt = new HSLFSlideShow(hslf);
        for (HSLFShape shape : ppt.getSlides().get(0).getShapes()) {
            if(shape instanceof HSLFGroupShape){
                HSLFGroupShape  group = (HSLFGroupShape)shape;
                assertNotNull(group.getAnchor());
                for (HSLFShape sh : group.getShapes()) {
                    assertNotNull(sh.getAnchor());
                }
            }
        }
        assertTrue("No Exceptions while reading file", true);
    }

    /**
     * Bug 41381: Exception from Slide.getMasterSheet() on a seemingly valid PPT file
     */
    @Test
    public void bug41381() throws Exception {
        HSLFSlideShowImpl hslf = new HSLFSlideShowImpl(_slTests.openResourceAsStream("alterman_security.ppt"));

        HSLFSlideShow ppt = new HSLFSlideShow(hslf);
        assertTrue("No Exceptions while reading file", true);

        assertEquals(1, ppt.getSlideMasters().size());
        assertEquals(1, ppt.getTitleMasters().size());
        boolean isFirst = true;
        for (HSLFSlide slide : ppt.getSlides()) {
            HSLFMasterSheet master = slide.getMasterSheet();
            // the first slide follows TitleMaster
            assertTrue(isFirst ? master instanceof HSLFTitleMaster : master instanceof HSLFSlideMaster);
            isFirst = false;
        }
    }

    /**
     * Bug 42486:  Failure parsing a seemingly valid PPT
     */
    @SuppressWarnings("unused")
    @Test
    public void bug42486 () throws Exception {
        HSLFSlideShowImpl hslf = new HSLFSlideShowImpl(_slTests.openResourceAsStream("42486.ppt"));

        HSLFSlideShow ppt = new HSLFSlideShow(hslf);
        for (HSLFSlide slide : ppt.getSlides()) {
            List<HSLFShape> shape = slide.getShapes();
        }
        assertTrue("No Exceptions while reading file", true);

    }

    /**
     * Bug 42524:  NPE in Shape.getShapeType()
     */
    @Test
    public void bug42524 () throws Exception {
        HSLFSlideShowImpl hslf = new HSLFSlideShowImpl(_slTests.openResourceAsStream("42486.ppt"));

        HSLFSlideShow ppt = new HSLFSlideShow(hslf);
        //walk down the tree and see if there were no errors while reading
        for (HSLFSlide slide : ppt.getSlides()) {
            for (HSLFShape shape : slide.getShapes()) {
                assertNotNull(shape.getShapeName());
                if (shape instanceof HSLFGroupShape){
                    HSLFGroupShape group = (HSLFGroupShape)shape;
                    for (HSLFShape comps : group.getShapes()) {
                        assertNotNull(comps.getShapeName());
                   }
                }
            }

        }
        assertTrue("No Exceptions while reading file", true);

    }

    /**
     * Bug 42520:  NPE in Picture.getPictureData()
     */
    @SuppressWarnings("unused")
    @Test
    public void bug42520 () throws Exception {
        HSLFSlideShowImpl hslf = new HSLFSlideShowImpl(_slTests.openResourceAsStream("42520.ppt"));

        HSLFSlideShow ppt = new HSLFSlideShow(hslf);

        //test case from the bug report
        HSLFGroupShape shapeGroup = (HSLFGroupShape)ppt.getSlides().get(11).getShapes().get(10);
        HSLFPictureShape picture = (HSLFPictureShape)shapeGroup.getShapes().get(0);
        picture.getPictureData();

        //walk down the tree and see if there were no errors while reading
        for (HSLFSlide slide : ppt.getSlides()) {
            for (HSLFShape shape : slide.getShapes()) {
              if (shape instanceof HSLFGroupShape){
                    HSLFGroupShape group = (HSLFGroupShape)shape;
                    for (HSLFShape comp : group.getShapes()) {
                        if (comp instanceof HSLFPictureShape){
                            HSLFPictureData pict = ((HSLFPictureShape)comp).getPictureData();
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
    @Test
    public void bug38256 () throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("38256.ppt"));

        assertTrue("No Exceptions while reading file", true);

        List<HSLFSlide> slide = ppt.getSlides();
        assertEquals(1, slide.size());
        List<List<HSLFTextParagraph>> paras = slide.get(0).getTextParagraphs();
        assertEquals(4, paras.size());

        Set<String> txt = new HashSet<String>();
        txt.add("\u201CHAPPY BIRTHDAY SCOTT\u201D");
        txt.add("Have a HAPPY DAY");
        txt.add("PS Nobody is allowed to hassle Scott TODAY\u2026");
        txt.add("Drinks will be in the Boardroom at 5pm today to celebrate Scott\u2019s B\u2019Day\u2026  See you all there!");

        for (List<HSLFTextParagraph> para : paras) {
            String text = HSLFTextParagraph.getRawText(para);
            assertTrue(text, txt.contains(text));
        }

    }

    /**
     * Bug 38256:  RuntimeException: Couldn't instantiate the class for type with id 0.
     * ( also fixed followup: getTextRuns() returns no text )
     */
    @Test
    public void bug43781() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("43781.ppt"));

        assertTrue("No Exceptions while reading file", true);

        // Check the first slide
        HSLFSlide slide = ppt.getSlides().get(0);
        List<List<HSLFTextParagraph>> slTr = slide.getTextParagraphs();
        
        // Has 3 text paragraphs, two from slide text (empty title / filled body), one from drawing
        assertEquals(3, slTr.size());
        assertFalse(slTr.get(0).get(0).isDrawingBased());
        assertFalse(slTr.get(1).get(0).isDrawingBased());
        assertTrue(slTr.get(2).get(0).isDrawingBased());
        assertEquals("", HSLFTextParagraph.getRawText(slTr.get(0)));
        assertEquals("First run", HSLFTextParagraph.getRawText(slTr.get(1)));
        assertEquals("Second run", HSLFTextParagraph.getRawText(slTr.get(2)));

        // Check the shape based text runs
        List<HSLFTextParagraph> lst = new ArrayList<HSLFTextParagraph>();
        for (HSLFShape shape : slide.getShapes()) {
            if (shape instanceof HSLFTextShape){
                List<HSLFTextParagraph> textRun = ((HSLFTextShape)shape).getTextParagraphs();
                lst.addAll(textRun);
            }

        }
        
        // There are two shapes in the ppt
        assertEquals(2, lst.size());
        assertEquals("First runSecond run", HSLFTextParagraph.getRawText(lst));
    }

    /**
     * Bug 44296: HSLF Not Extracting Slide Background Image
     */
    @Test
    public void bug44296  () throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("44296.ppt"));

        HSLFSlide slide = ppt.getSlides().get(0);

        HSLFBackground b = slide.getBackground();
        HSLFFill f = b.getFill();
        assertEquals(HSLFFill.FILL_PICTURE, f.getFillType());

        HSLFPictureData pict = f.getPictureData();
        assertNotNull(pict);
        assertEquals(PictureType.JPEG, pict.getType());
    }

    /**
     * Bug 44770: java.lang.RuntimeException: Couldn't instantiate the class for type with id 1036 on class class org.apache.poi.hslf.record.PPDrawing
     */
    @Test
    public void bug44770() throws Exception {
        try {
             new HSLFSlideShow(_slTests.openResourceAsStream("44770.ppt"));
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
    @Test
    public void bug41071() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("41071.ppt"));

        HSLFSlide slide = ppt.getSlides().get(0);
        List<HSLFShape> sh = slide.getShapes();
        assertEquals(1, sh.size());
        assertTrue(sh.get(0) instanceof HSLFTextShape);
        HSLFTextShape tx = (HSLFTextShape)sh.get(0);
        assertEquals("Fundera, planera och involvera.", HSLFTextParagraph.getRawText(tx.getTextParagraphs()));

        List<List<HSLFTextParagraph>> run = slide.getTextParagraphs();
        assertEquals(3, run.size());
        assertEquals("Fundera, planera och involvera.", HSLFTextParagraph.getRawText(run.get(2)));
    }

    /**
     * PowerPoint 95 files should throw a more helpful exception
     * @throws Exception
     */
    @Test(expected=OldPowerPointFormatException.class)
    public void bug41711() throws Exception {
    	// New file is fine
        new HSLFSlideShow(_slTests.openResourceAsStream("SampleShow.ppt"));

        // PowerPoint 95 gives an old format exception
    	new HSLFSlideShow(_slTests.openResourceAsStream("PPT95.ppt"));
    }
    
    /**
     * Changing text from Ascii to Unicode
     */
    @Test
    public void bug49648() throws Exception {
       HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("49648.ppt"));
       for(HSLFSlide slide : ppt.getSlides()) {
          for(List<HSLFTextParagraph> run : slide.getTextParagraphs()) {
             String text = HSLFTextParagraph.getRawText(run);
             text.replace("{txtTot}", "With \u0123\u1234\u5678 unicode");
             HSLFTextParagraph.setText(run, text);
          }
       }
    }

    /**
     * Bug 41246: AIOOB with illegal note references
     */
    @Test
    public void bug41246a() throws Exception {
        InputStream fis = _slTests.openResourceAsStream("41246-1.ppt");
        HSLFSlideShowImpl hslf = new HSLFSlideShowImpl(fis);
        fis.close();

        HSLFSlideShow ppt = new HSLFSlideShow(hslf);
        assertTrue("No Exceptions while reading file", true);

        ppt = HSLFTestDataSamples.writeOutAndReadBack(ppt);
        assertTrue("No Exceptions while rewriting file", true);
    }

    @Test
    public void bug41246b() throws Exception {
        InputStream fis = _slTests.openResourceAsStream("41246-2.ppt");
        HSLFSlideShowImpl hslf = new HSLFSlideShowImpl(fis);
        fis.close();

        HSLFSlideShow ppt = new HSLFSlideShow(hslf);
        assertTrue("No Exceptions while reading file", true);

        ppt = HSLFTestDataSamples.writeOutAndReadBack(ppt);
        assertTrue("No Exceptions while rewriting file", true);
    }

    /**
     * Bug 45776: Fix corrupt file problem using TextRun.setText
     */
    @Test
    public void bug45776() throws Exception {
        InputStream is = _slTests.openResourceAsStream("45776.ppt");
        HSLFSlideShow ppt = new HSLFSlideShow(new HSLFSlideShowImpl(is));
        is.close();

        // get slides
        for (HSLFSlide slide : ppt.getSlides()) {
            for (HSLFShape shape : slide.getShapes()) {
                if (!(shape instanceof HSLFTextBox)) continue;
                HSLFTextBox tb = (HSLFTextBox) shape;
                // work with TextBox
                String str = tb.getText();

                if (!str.contains("$$DATE$$")) continue;
                str = str.replace("$$DATE$$", new Date().toString());
                tb.setText(str);
                
                List<HSLFTextParagraph> tr = tb.getTextParagraphs();
                assertEquals(str.length()+1,tr.get(0).getParagraphStyle().getCharactersCovered());
                assertEquals(str.length()+1,tr.get(0).getTextRuns().get(0).getCharacterStyle().getCharactersCovered());
            }
        }
    }

    @Test
    public void bug55732() throws Exception {
        File file = _slTests.getFile("bug55732.ppt");
        
        HSLFSlideShowImpl ss = new HSLFSlideShowImpl(file.getAbsolutePath());
        HSLFSlideShow _show = new HSLFSlideShow(ss);
        List<HSLFSlide> _slides = _show.getSlides();

        /* Iterate over slides and extract text */
        for( HSLFSlide slide : _slides ) {
            HeadersFooters hf = slide.getHeadersFooters();
            /*boolean visible =*/ hf.isHeaderVisible(); // exception happens here
        }
        assertTrue("No Exceptions while reading headers", true);
    }
    
    @Test
    public void bug56260() throws Exception {
        File file = _slTests.getFile("56260.ppt");
        
        HSLFSlideShowImpl ss = new HSLFSlideShowImpl(file.getAbsolutePath());
        HSLFSlideShow _show = new HSLFSlideShow(ss);
        List<HSLFSlide> _slides = _show.getSlides();
        assertEquals(13, _slides.size());
        
        // Check the number of TextHeaderAtoms on Slide 1
        Document dr = _show.getDocumentRecord();
        SlideListWithText slidesSLWT = dr.getSlideSlideListWithText();
        SlideAtomsSet s1 = slidesSLWT.getSlideAtomsSets()[0];

        int tha = 0;
        for (Record r : s1.getSlideRecords()) {
            if (r instanceof TextHeaderAtom) tha++;
        }
        assertEquals(2, tha);
        
        // Check to see that we have a pair next to each other
        assertEquals(TextHeaderAtom.class, s1.getSlideRecords()[0].getClass());
        assertEquals(TextHeaderAtom.class, s1.getSlideRecords()[1].getClass());
        
        
        // Check the number of text runs based on the slide (not textbox)
        // Will have skipped the empty one
        int str = 0;
        for (List<HSLFTextParagraph> tr : _slides.get(0).getTextParagraphs()) {
            if (! tr.get(0).isDrawingBased()) str++;
        }
        assertEquals(2, str);
    }
    
    @Test
    public void bug37625() throws IOException {
        InputStream inputStream = new FileInputStream(_slTests.getFile("37625.ppt"));
        try {
            HSLFSlideShow slideShow = new HSLFSlideShow(inputStream);
            assertEquals(29, slideShow.getSlides().size());
            
            HSLFSlideShow slideBack = HSLFTestDataSamples.writeOutAndReadBack(slideShow);
            assertNotNull(slideBack);
            assertEquals(29, slideBack.getSlides().size());
        } finally {
            inputStream.close();
        }
    }
    
    @Test
    public void bug57272() throws Exception {
        InputStream inputStream = new FileInputStream(_slTests.getFile("57272_corrupted_usereditatom.ppt"));
        try {
            HSLFSlideShow slideShow = new HSLFSlideShow(inputStream);
            assertEquals(6, slideShow.getSlides().size());

            HSLFSlideShow slideBack = HSLFTestDataSamples.writeOutAndReadBack(slideShow);
            assertNotNull(slideBack);
            assertEquals(6, slideBack.getSlides().size());
        } finally {
            inputStream.close();
        }
    }

    @Test
    public void bug49541() throws Exception {
        InputStream inputStream = new FileInputStream(_slTests.getFile("49541_symbol_map.ppt"));
        try {
            HSLFSlideShow slideShow = new HSLFSlideShow(inputStream);
            HSLFSlide slide = slideShow.getSlides().get(0);
            HSLFGroupShape sg = (HSLFGroupShape)slide.getShapes().get(0);
            HSLFTextBox tb = (HSLFTextBox)sg.getShapes().get(0);
            String text = StringUtil.mapMsCodepointString(tb.getText());
            assertEquals("\u226575 years", text);
        } finally {
            inputStream.close();
        }
    }
    
    @Test
    public void bug47261() throws Exception {
        InputStream inputStream = new FileInputStream(_slTests.getFile("bug47261.ppt"));
        try {
            HSLFSlideShow slideShow = new HSLFSlideShow(inputStream);
            slideShow.removeSlide(0);
            slideShow.createSlide();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            slideShow.write(bos);
        } finally {
            inputStream.close();
        }
    }
    
    @Test
    public void bug56240() throws Exception {
        InputStream inputStream = new FileInputStream(_slTests.getFile("bug56240.ppt"));
        try {
            HSLFSlideShow slideShow = new HSLFSlideShow(inputStream);
            int slideCnt = slideShow.getSlides().size();
            assertEquals(105, slideCnt);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            slideShow.write(bos);
            bos.close();
        } finally {
            inputStream.close();
        }
    }
    
    @Test
    public void bug46441() throws Exception {
        InputStream inputStream = new FileInputStream(_slTests.getFile("bug46441.ppt"));
        try {
            HSLFSlideShow slideShow = new HSLFSlideShow(inputStream);
            HSLFAutoShape as = (HSLFAutoShape)slideShow.getSlides().get(0).getShapes().get(0);
            AbstractEscherOptRecord opt = as.getEscherOptRecord();
            EscherArrayProperty ep = HSLFShape.getEscherProperty(opt, EscherProperties.FILL__SHADECOLORS);
            double exp[][] = {
                // r, g, b, position
                { 94, 158, 255, 0 },
                { 133, 194, 255, 0.399994 },
                { 196, 214, 235, 0.699997 },
                { 255, 235, 250, 1 }                    
            };
            
            int i = 0;
            for (byte data[] : ep) {
                EscherColorRef ecr = new EscherColorRef(data, 0, 4);
                int rgb[] = ecr.getRGB();
                double pos = Units.fixedPointToDouble(LittleEndian.getInt(data, 4));
                assertEquals((int)exp[i][0], rgb[0]);
                assertEquals((int)exp[i][1], rgb[1]);
                assertEquals((int)exp[i][2], rgb[2]);
                assertEquals(exp[i][3], pos, 0.01);
                i++;
            }
        } finally {
            inputStream.close();
        }
    }
    
    @Test
    public void bug58516() throws IOException {
        SlideShowFactory.create(_slTests.getFile("bug58516.ppt")).close();
    }

    @Test
    public void bug45124() throws IOException {
        SlideShow<?,?> ppt = SlideShowFactory.create(_slTests.getFile("bug45124.ppt"));
        Slide<?,?> slide1 = ppt.getSlides().get(1);

        TextBox<?,?> res = slide1.createTextBox();
        res.setAnchor(new java.awt.Rectangle(60, 150, 700, 100));
        res.setText("I am italic-false, bold-true inserted text");
        

        TextParagraph<?,?,?> tp = res.getTextParagraphs().get(0);
        TextRun rt = tp.getTextRuns().get(0);
        rt.setItalic(false);
        assertTrue(rt.isBold());
        
        tp.setBulletStyle(Color.red, 'A');

        SlideShow<?,?> ppt2 = HSLFTestDataSamples.writeOutAndReadBack((HSLFSlideShow)ppt);
        ppt.close();
        
        res = (TextBox<?,?>)ppt2.getSlides().get(1).getShapes().get(1);
        tp = res.getTextParagraphs().get(0);
        rt = tp.getTextRuns().get(0);
        
        assertFalse(rt.isItalic());
        assertTrue(rt.isBold());
        PaintStyle ps = tp.getBulletStyle().getBulletFontColor();
        assertTrue(ps instanceof SolidPaint);
        Color actColor = DrawPaint.applyColorTransform(((SolidPaint)ps).getSolidColor());
        assertEquals(Color.red, actColor);
        assertEquals("A", tp.getBulletStyle().getBulletCharacter());
        
        ppt2.close();
    }
    
    @Test
    public void bug45088() throws IOException {
        String template = "[SYSDATE]";
        String textExp = "REPLACED_DATE_WITH_A_LONG_ONE";
        
        HSLFSlideShow ppt1 = (HSLFSlideShow)SlideShowFactory.create(_slTests.getFile("bug45088.ppt"));
        for (HSLFSlide slide : ppt1.getSlides()) {
            for (List<HSLFTextParagraph> paraList : slide.getTextParagraphs()) {
                for (HSLFTextParagraph para : paraList) {
                    for (HSLFTextRun run : para.getTextRuns()) {
                        String text = run.getRawText();
                        if (text != null && text.contains(template)) {
                            String replacedText = text.replace(template, textExp);
                            run.setText(replacedText);
                            para.setDirty();
                        }
                    }
                }
            }
        }

        HSLFSlideShow ppt2 = HSLFTestDataSamples.writeOutAndReadBack(ppt1);
        ppt1.close();
        
        HSLFTextBox tb = (HSLFTextBox)ppt2.getSlides().get(0).getShapes().get(1);
        String textAct = tb.getTextParagraphs().get(0).getTextRuns().get(0).getRawText().trim();
        assertEquals(textExp, textAct);
        ppt2.close();
    }
    
    @Test
    public void bug45908() throws IOException {
        HSLFSlideShow ppt1 = (HSLFSlideShow)SlideShowFactory.create(_slTests.getFile("bug45908.ppt"));

        HSLFSlide slide = ppt1.getSlides().get(0);
        HSLFAutoShape styleShape = (HSLFAutoShape)slide.getShapes().get(1);
        HSLFTextParagraph tp0 = styleShape.getTextParagraphs().get(0);
        HSLFTextRun tr0 = tp0.getTextRuns().get(0);


        int rows = 5;
        int cols = 2;
        HSLFTable table = slide.createTable(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                HSLFTableCell cell = table.getCell(i, j);
                cell.setText("Test");

                HSLFTextParagraph tp = cell.getTextParagraphs().get(0);
                tp.setBulletStyle('%', tp0.getBulletColor(), tp0.getBulletFont(), tp0.getBulletSize());
                tp.setIndent(tp0.getIndent());
                tp.setAlignment(tp0.getTextAlign());
                tp.setIndentLevel(tp0.getIndentLevel());
                tp.setSpaceAfter(tp0.getSpaceAfter());
                tp.setSpaceBefore(tp0.getSpaceBefore());
                tp.setBulletStyle();
                
                HSLFTextRun tr = tp.getTextRuns().get(0);
                tr.setBold(tr0.isBold());
                // rt.setEmbossed();
                tr.setFontColor(Color.BLACK);
                tr.setFontFamily(tr0.getFontFamily());
                tr.setFontSize(tr0.getFontSize());
                tr.setItalic(tr0.isItalic());
                tr.setShadowed(tr0.isShadowed());
                tr.setStrikethrough(tr0.isStrikethrough());
                tr.setUnderlined(tr0.isUnderlined());
            }
        }

        table.moveTo(100, 100);

        HSLFSlideShow ppt2 = HSLFTestDataSamples.writeOutAndReadBack(ppt1);
        ppt1.close();

        HSLFTable tab = (HSLFTable)ppt2.getSlides().get(0).getShapes().get(2);
        HSLFTableCell c2 = tab.getCell(0, 0);
        HSLFTextParagraph tp1 = c2.getTextParagraphs().get(0);
        HSLFTextRun tr1 = tp1.getTextRuns().get(0);
        assertFalse(tp1.isBullet());
        assertEquals(tp0.getBulletColor(), tp1.getBulletColor());
        assertEquals(tp0.getBulletFont(), tp1.getBulletFont());
        assertEquals(tp0.getBulletSize(), tp1.getBulletSize());
        assertEquals(tp0.getIndent(), tp1.getIndent());
        assertEquals(tp0.getTextAlign(), tp1.getTextAlign());
        assertEquals(tp0.getIndentLevel(), tp1.getIndentLevel());
        assertEquals(tp0.getSpaceAfter(), tp1.getSpaceAfter());
        assertEquals(tp0.getSpaceBefore(), tp1.getSpaceBefore());
        assertEquals(tr0.isBold(), tr1.isBold());
        assertEquals(Color.black, DrawPaint.applyColorTransform(tr1.getFontColor().getSolidColor()));
        assertEquals(tr0.getFontFamily(), tr1.getFontFamily());
        assertEquals(tr0.getFontSize(), tr1.getFontSize());
        assertEquals(tr0.isItalic(), tr1.isItalic());
        assertEquals(tr0.isShadowed(), tr1.isShadowed());
        assertEquals(tr0.isStrikethrough(), tr1.isStrikethrough());
        assertEquals(tr0.isUnderlined(), tr1.isUnderlined());
        
        ppt2.close();
    }
}
