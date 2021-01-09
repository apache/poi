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

import static org.apache.poi.POITestCase.assertContains;
import static org.apache.poi.POITestCase.assertStartsWith;
import static org.apache.poi.hslf.HSLFTestDataSamples.writeOutAndReadBack;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.poi.POIDataSamples;
import org.apache.poi.common.usermodel.fonts.FontGroup;
import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherArrayProperty;
import org.apache.poi.ddf.EscherColorRef;
import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.hslf.HSLFTestDataSamples;
import org.apache.poi.hslf.exceptions.OldPowerPointFormatException;
import org.apache.poi.hslf.model.HeadersFooters;
import org.apache.poi.hslf.record.DocInfoListContainer;
import org.apache.poi.hslf.record.Document;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.hslf.record.SlideListWithText;
import org.apache.poi.hslf.record.SlideListWithText.SlideAtomsSet;
import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.hslf.record.VBAInfoAtom;
import org.apache.poi.hslf.record.VBAInfoContainer;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.macros.VBAMacroReader;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.sl.usermodel.ColorStyle;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.sl.usermodel.TextBox;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextParagraph.TextAlign;
import org.apache.poi.sl.usermodel.TextRun;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;
import org.apache.poi.util.Units;
import org.junit.jupiter.api.Test;

/**
 * Testcases for bugs entered in bugzilla
 * the Test name contains the bugzilla bug id
 */
public final class TestBugs {
    /**
     * Bug 41384: Array index wrong in record creation
     */
    @Test
    void bug41384() throws IOException {
        try (HSLFSlideShow ppt = open("41384.ppt")) {
            assertEquals(1, ppt.getSlides().size());

            List<HSLFPictureData> pict = ppt.getPictureData();
            assertEquals(2, pict.size());
            assertEquals(PictureType.JPEG, pict.get(0).getType());
            assertEquals(PictureType.JPEG, pict.get(1).getType());
        }
    }

    /**
     * First fix from Bug 42474: NPE in RichTextRun.isBold()
     * when the RichTextRun comes from a Notes model object
     */
    @Test
    void bug42474_1() throws IOException {
        try (HSLFSlideShow ppt = open("42474-1.ppt")) {
            assertEquals(2, ppt.getSlides().size());

            List<HSLFTextParagraph> txrun;
            HSLFNotes notes;

            notes = ppt.getSlides().get(0).getNotes();
            assertNotNull(notes);
            txrun = notes.getTextParagraphs().get(0);
            assertEquals("Notes-1", HSLFTextParagraph.getRawText(txrun));
            assertFalse(txrun.get(0).getTextRuns().get(0).isBold());

            //notes for the second slide are in bold
            notes = ppt.getSlides().get(1).getNotes();
            assertNotNull(notes);
            txrun = notes.getTextParagraphs().get(0);
            assertEquals("Notes-2", HSLFTextParagraph.getRawText(txrun));
            assertTrue(txrun.get(0).getTextRuns().get(0).isBold());
        }
    }

    /**
     * Second fix from Bug 42474: Incorrect matching of notes to slides
     */
    @Test
    void bug42474_2() throws IOException {
        try (HSLFSlideShow ppt = open("42474-2.ppt")) {

            //map slide number and starting phrase of its notes
            Map<Integer, String> notesMap = new HashMap<>();
            notesMap.put(4, "For  decades before calculators");
            notesMap.put(5, "Several commercial applications");
            notesMap.put(6, "There are three variations of LNS that are discussed here");
            notesMap.put(7, "Although multiply and square root are easier");
            notesMap.put(8, "The bus Z is split into Z_H and Z_L");

            for (HSLFSlide slide : ppt.getSlides()) {
                Integer slideNumber = slide.getSlideNumber();
                HSLFNotes notes = slide.getNotes();
                if (notesMap.containsKey(slideNumber)) {
                    assertNotNull(notes);
                    String text = HSLFTextParagraph.getRawText(notes.getTextParagraphs().get(0));
                    String startingPhrase = notesMap.get(slideNumber);
                    assertStartsWith("Notes for slide " + slideNumber + " must start with starting phrase",
                                     text, startingPhrase);
                }
            }
        }
    }

    /**
     * Bug 42485: All TextBoxes inside ShapeGroups have null TextRuns
     */
    @Test
    void bug42485 () throws IOException {
        try (HSLFSlideShow ppt = open("42485.ppt")) {
            for (HSLFShape shape : ppt.getSlides().get(0).getShapes()) {
                if (shape instanceof HSLFGroupShape) {
                    HSLFGroupShape group = (HSLFGroupShape) shape;
                    for (HSLFShape sh : group.getShapes()) {
                        if (sh instanceof HSLFTextBox) {
                            HSLFTextBox txt = (HSLFTextBox) sh;
                            assertNotNull(txt.getTextParagraphs());
                        }
                    }
                }
            }
        }
    }

    /**
     * Bug 42484: NullPointerException from ShapeGroup.getAnchor()
     */
    @Test
    void bug42484 () throws IOException {
        try (HSLFSlideShow ppt = open("42485.ppt")) {
            for (HSLFShape shape : ppt.getSlides().get(0).getShapes()) {
                if (shape instanceof HSLFGroupShape) {
                    HSLFGroupShape group = (HSLFGroupShape) shape;
                    assertNotNull(group.getAnchor());
                    for (HSLFShape sh : group.getShapes()) {
                        assertNotNull(sh.getAnchor());
                    }
                }
            }
        }
    }

    /**
     * Bug 41381: Exception from Slide.getMasterSheet() on a seemingly valid PPT file
     */
    @Test
    void bug41381() throws IOException {
        try (HSLFSlideShow ppt = open("alterman_security.ppt")) {
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
    }

    /**
     * Bug 42486:  Failure parsing a seemingly valid PPT
     */
    @SuppressWarnings("unused")
    @Test
    void bug42486 () throws IOException {
        try (HSLFSlideShow ppt = open("42486.ppt")) {
            for (HSLFSlide slide : ppt.getSlides()) {
                List<HSLFShape> shape = slide.getShapes();
            }
        }
    }

    /**
     * Bug 42524:  NPE in Shape.getShapeType()
     */
    @Test
    void bug42524 () throws IOException {
        try (HSLFSlideShow ppt = open("42486.ppt")) {
            //walk down the tree and see if there were no errors while reading
            for (HSLFSlide slide : ppt.getSlides()) {
                for (HSLFShape shape : slide.getShapes()) {
                    assertNotNull(shape.getShapeName());
                    if (shape instanceof HSLFGroupShape) {
                        HSLFGroupShape group = (HSLFGroupShape) shape;
                        for (HSLFShape comps : group.getShapes()) {
                            assertNotNull(comps.getShapeName());
                        }
                    }
                }
            }
        }
    }

    /**
     * Bug 42520:  NPE in Picture.getPictureData()
     */
    @SuppressWarnings("unused")
    @Test
    void bug42520 () throws IOException {
        try (HSLFSlideShow ppt = open("42520.ppt")) {

            //test case from the bug report
            HSLFGroupShape shapeGroup = (HSLFGroupShape) ppt.getSlides().get(11).getShapes().get(10);
            HSLFPictureShape picture = (HSLFPictureShape) shapeGroup.getShapes().get(0);
            picture.getPictureData();

            //walk down the tree and see if there were no errors while reading
            for (HSLFSlide slide : ppt.getSlides()) {
                for (HSLFShape shape : slide.getShapes()) {
                    if (shape instanceof HSLFGroupShape) {
                        HSLFGroupShape group = (HSLFGroupShape) shape;
                        for (HSLFShape comp : group.getShapes()) {
                            if (comp instanceof HSLFPictureShape) {
                                HSLFPictureData pict = ((HSLFPictureShape) comp).getPictureData();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Bug 38256:  RuntimeException: Couldn't instantiate the class for type with id 0.
     * ( also fixed followup: getTextRuns() returns no text )
     */
    @Test
    void bug38256 () throws IOException {
        try (HSLFSlideShow ppt = open("38256.ppt")) {
            List<HSLFSlide> slide = ppt.getSlides();
            assertEquals(1, slide.size());
            List<List<HSLFTextParagraph>> paras = slide.get(0).getTextParagraphs();
            assertEquals(4, paras.size());

            Set<String> expected = new HashSet<>();
            expected.add("\u201CHAPPY BIRTHDAY SCOTT\u201D");
            expected.add("Have a HAPPY DAY");
            expected.add("PS Nobody is allowed to hassle Scott TODAY\u2026");
            expected.add("Drinks will be in the Boardroom at 5pm today to celebrate Scott\u2019s B\u2019Day\u2026  See you all there!");

            for (List<HSLFTextParagraph> para : paras) {
                String text = HSLFTextParagraph.getRawText(para);
                assertTrue(expected.contains(text), text);
            }
        }
    }

    /**
     * Bug 38256:  RuntimeException: Couldn't instantiate the class for type with id 0.
     * ( also fixed followup: getTextRuns() returns no text )
     */
    @Test
    void bug43781() throws IOException {
        try (HSLFSlideShow ppt = open("43781.ppt")) {
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
            List<HSLFTextParagraph> lst = new ArrayList<>();
            for (HSLFShape shape : slide.getShapes()) {
                if (shape instanceof HSLFTextShape) {
                    List<HSLFTextParagraph> textRun = ((HSLFTextShape) shape).getTextParagraphs();
                    lst.addAll(textRun);
                }

            }

            // There are two shapes in the ppt
            assertEquals(2, lst.size());
            assertEquals("First runSecond run", HSLFTextParagraph.getRawText(lst));
        }
    }

    /**
     * Bug 44296: HSLF Not Extracting Slide Background Image
     */
    @Test
    void bug44296  () throws IOException {
        try (HSLFSlideShow ppt = open("44296.ppt")) {
            HSLFSlide slide = ppt.getSlides().get(0);

            HSLFBackground b = slide.getBackground();
            assertNotNull(b);
            HSLFFill f = b.getFill();
            assertEquals(HSLFFill.FILL_PICTURE, f.getFillType());

            HSLFPictureData pict = f.getPictureData();
            assertNotNull(pict);
            assertEquals(PictureType.JPEG, pict.getType());
        }
    }

    /**
     * Bug 44770: java.lang.RuntimeException: Couldn't instantiate the class for
     * type with id 1036 on class class org.apache.poi.hslf.record.PPDrawing
     */
    @Test
    void bug44770() throws IOException {
        try (HSLFSlideShow ppt = open("44770.ppt")) {
            assertNotNull(ppt.getSlides().get(0));
        }
    }

    /**
     * Bug 41071: Will not extract text from Powerpoint TextBoxes
     */
    @Test
    void bug41071() throws IOException {
        try (HSLFSlideShow ppt = open("41071.ppt")) {
            HSLFSlide slide = ppt.getSlides().get(0);
            List<HSLFShape> sh = slide.getShapes();
            assertEquals(1, sh.size());
            assertTrue(sh.get(0) instanceof HSLFTextShape);
            HSLFTextShape tx = (HSLFTextShape) sh.get(0);
            assertEquals("Fundera, planera och involvera.", HSLFTextParagraph.getRawText(tx.getTextParagraphs()));

            List<List<HSLFTextParagraph>> run = slide.getTextParagraphs();
            assertEquals(3, run.size());
            assertEquals("Fundera, planera och involvera.", HSLFTextParagraph.getRawText(run.get(2)));
        }
    }

    /**
     * PowerPoint 95 files should throw a more helpful exception
     */
    @Test
    void bug41711() throws IOException {
    	// New file is fine
        open("SampleShow.ppt").close();

        // PowerPoint 95 gives an old format exception
        assertThrows(OldPowerPointFormatException.class, () -> open("PPT95.ppt").close());
    }

    /**
     * Changing text from Ascii to Unicode
     */
    @Test
    void bug49648() throws IOException {
        try (HSLFSlideShow ppt = open("49648.ppt")) {
            for (HSLFSlide slide : ppt.getSlides()) {
                for (List<HSLFTextParagraph> run : slide.getTextParagraphs()) {
                    String text = HSLFTextParagraph.getRawText(run);
                    text = text.replace("{txtTot}", "With \u0123\u1234\u5678 unicode");
                    HSLFTextParagraph.setText(run, text);
                }
            }
        }
    }

    /**
     * Bug 41246: AIOOB with illegal note references
     */
    @Test
    void bug41246a() throws IOException {
        try (HSLFSlideShow ppt = open("41246-1.ppt")) {
            try (HSLFSlideShow ppt2 = writeOutAndReadBack(ppt)) {
                assertNotNull(ppt2.getSlides().get(0));
            }
        }
    }

    @Test
    void bug41246b() throws IOException {
        try (HSLFSlideShow ppt = open("41246-2.ppt")) {
            try (HSLFSlideShow ppt2 = writeOutAndReadBack(ppt)) {
                assertNotNull(ppt2.getSlides().get(0));
            }
        }
    }

    /**
     * Bug 45776: Fix corrupt file problem using TextRun.setText
     */
    @Test
    void bug45776() throws IOException {
        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ROOT);
        try (HSLFSlideShow ppt = open("45776.ppt")) {

            // get slides
            for (HSLFSlide slide : ppt.getSlides()) {
                for (HSLFShape shape : slide.getShapes()) {
                    if (!(shape instanceof HSLFTextBox)) {
                        continue;
                    }
                    HSLFTextBox tb = (HSLFTextBox) shape;
                    // work with TextBox
                    String str = tb.getText();

                    if (!str.contains("$$DATE$$")) {
                        continue;
                    }
                    str = str.replace("$$DATE$$", df.format(new Date()));
                    tb.setText(str);

                    List<HSLFTextParagraph> tr = tb.getTextParagraphs();
                    assertEquals(str.length() + 1, tr.get(0).getParagraphStyle().getCharactersCovered());
                    assertEquals(str.length() + 1, tr.get(0).getTextRuns().get(0).getCharacterStyle().getCharactersCovered());
                }
            }

        }
    }

    @Test
    void bug55732() throws IOException {
        try (HSLFSlideShow ppt = open("bug55732.ppt")) {
            /* Iterate over slides and extract text */
            for (HSLFSlide slide : ppt.getSlides()) {
                HeadersFooters hf = slide.getHeadersFooters();
                hf.isHeaderVisible(); // exception happens here
            }
        }
    }

    @Test
    void bug56260() throws IOException {
        try (HSLFSlideShow ppt = open("56260.ppt")) {
            List<HSLFSlide> _slides = ppt.getSlides();
            assertEquals(13, _slides.size());

            // Check the number of TextHeaderAtoms on Slide 1
            Document dr = ppt.getDocumentRecord();
            SlideListWithText slidesSLWT = dr.getSlideSlideListWithText();
            assertNotNull(slidesSLWT);
            SlideAtomsSet s1 = slidesSLWT.getSlideAtomsSets()[0];

            int tha = 0;
            for (org.apache.poi.hslf.record.Record r : s1.getSlideRecords()) {
                if (r instanceof TextHeaderAtom) {
                    tha++;
                }
            }
            assertEquals(2, tha);

            // Check to see that we have a pair next to each other
            assertEquals(TextHeaderAtom.class, s1.getSlideRecords()[0].getClass());
            assertEquals(TextHeaderAtom.class, s1.getSlideRecords()[1].getClass());


            // Check the number of text runs based on the slide (not textbox)
            // Will have skipped the empty one
            int str = 0;
            for (List<HSLFTextParagraph> tr : _slides.get(0).getTextParagraphs()) {
                if (!tr.get(0).isDrawingBased()) {
                    str++;
                }
            }
            assertEquals(2, str);
        }
    }

    @Test
    void bug37625() throws IOException {
        try (HSLFSlideShow ppt1 = open("37625.ppt");
             HSLFSlideShow ppt2 = writeOutAndReadBack(ppt1)) {
            assertEquals(29, ppt1.getSlides().size());
            assertNotNull(ppt2);
            assertEquals(29, ppt2.getSlides().size());
        }
    }

    @Test
    void bug57272() throws IOException {
        try (HSLFSlideShow ppt1 = open("57272_corrupted_usereditatom.ppt");
             HSLFSlideShow ppt2 = writeOutAndReadBack(ppt1)) {
            assertEquals(6, ppt1.getSlides().size());
            assertNotNull(ppt2);
            assertEquals(6, ppt2.getSlides().size());
        }
    }

    @Test
    void bug49541() throws IOException {
        try (HSLFSlideShow ppt = open("49541_symbol_map.ppt")) {
            HSLFSlide slide = ppt.getSlides().get(0);
            HSLFGroupShape sg = (HSLFGroupShape) slide.getShapes().get(0);
            HSLFTextBox tb = (HSLFTextBox) sg.getShapes().get(0);
            String text = StringUtil.mapMsCodepointString(tb.getText());
            assertEquals("\u226575 years", text);
        }
    }

    @Test
    void bug47261() throws IOException {
        try (HSLFSlideShow ppt = open("bug47261.ppt")) {
            ppt.removeSlide(0);
            ppt.createSlide();
            writeOutAndReadBack(ppt).close();
        }
    }

    @Test
    void bug56240() throws IOException {
        try (HSLFSlideShow ppt = open("bug56240.ppt")) {
            int slideCnt = ppt.getSlides().size();
            assertEquals(105, slideCnt);
            writeOutAndReadBack(ppt).close();
        }
    }

    @Test
    void bug46441() throws IOException {
        try (HSLFSlideShow ppt = open("bug46441.ppt")) {
            HSLFAutoShape as = (HSLFAutoShape) ppt.getSlides().get(0).getShapes().get(0);
            AbstractEscherOptRecord opt = as.getEscherOptRecord();
            EscherArrayProperty ep = HSLFShape.getEscherProperty(opt, EscherPropertyTypes.FILL__SHADECOLORS);
            double[][] exp = {
                // r, g, b, position
                {94, 158, 255, 0},
                {133, 194, 255, 0.399994},
                {196, 214, 235, 0.699997},
                {255, 235, 250, 1}
            };

            int i = 0;
            for (byte[] data : ep) {
                EscherColorRef ecr = new EscherColorRef(data, 0, 4);
                int[] rgb = ecr.getRGB();
                double pos = Units.fixedPointToDouble(LittleEndian.getInt(data, 4));
                assertEquals((int) exp[i][0], rgb[0]);
                assertEquals((int) exp[i][1], rgb[1]);
                assertEquals((int) exp[i][2], rgb[2]);
                assertEquals(exp[i][3], pos, 0.01);
                i++;
            }
        }
    }

    @Test
    void bug58516() throws IOException {
        open("bug58516.ppt").close();
    }

    @Test
    void bug45124() throws IOException {
        try (HSLFSlideShow ppt = open("bug45124.ppt")) {
            Slide<?, ?> slide1 = ppt.getSlides().get(1);

            TextBox<?, ?> res = slide1.createTextBox();
            res.setAnchor(new java.awt.Rectangle(60, 150, 700, 100));
            res.setText("I am italic-false, bold-true inserted text");

            TextParagraph<?, ?, ?> tp = res.getTextParagraphs().get(0);
            TextRun rt = tp.getTextRuns().get(0);
            rt.setItalic(false);
            assertTrue(rt.isBold());

            tp.setBulletStyle(Color.red, 'A');

            try (SlideShow<?, ?> ppt2 = writeOutAndReadBack(ppt)) {
                res = (TextBox<?, ?>) ppt2.getSlides().get(1).getShapes().get(1);
                tp = res.getTextParagraphs().get(0);
                rt = tp.getTextRuns().get(0);

                assertFalse(rt.isItalic());
                assertTrue(rt.isBold());
                PaintStyle ps = tp.getBulletStyle().getBulletFontColor();
                assertTrue(ps instanceof SolidPaint);
                Color actColor = DrawPaint.applyColorTransform(((SolidPaint) ps).getSolidColor());
                assertEquals(Color.red, actColor);
                assertEquals("A", tp.getBulletStyle().getBulletCharacter());
            }
        }
    }

    @Test
    void bug45088() throws IOException {
        String template = "[SYSDATE]";
        String textExp = "REPLACED_DATE_WITH_A_LONG_ONE";

        try (HSLFSlideShow ppt1 = open("bug45088.ppt")) {
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

            try (HSLFSlideShow ppt2 = writeOutAndReadBack(ppt1)) {
                HSLFTextBox tb = (HSLFTextBox) ppt2.getSlides().get(0).getShapes().get(1);
                String textAct = tb.getTextParagraphs().get(0).getTextRuns().get(0).getRawText().trim();
                assertEquals(textExp, textAct);
            }
        }
    }

    @Test
    void bug45908() throws IOException {
        try (HSLFSlideShow ppt1 = open("bug45908.ppt")) {

            HSLFSlide slide = ppt1.getSlides().get(0);
            HSLFAutoShape styleShape = (HSLFAutoShape) slide.getShapes().get(1);
            HSLFTextParagraph tp0 = styleShape.getTextParagraphs().get(0);
            HSLFTextRun tr0 = tp0.getTextRuns().get(0);


            int rows = 5;
            int cols = 2;
            HSLFTable table = slide.createTable(rows, cols);
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {

                    HSLFTableCell cell = table.getCell(i, j);
                    assertNotNull(cell);
                    cell.setText("Test");

                    HSLFTextParagraph tp = cell.getTextParagraphs().get(0);
                    tp.setBulletStyle('%', tp0.getBulletColor(), tp0.getBulletFont(), tp0.getBulletSize());
                    tp.setIndent(tp0.getIndent());
                    tp.setTextAlign(tp0.getTextAlign());
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

            try (HSLFSlideShow ppt2 = writeOutAndReadBack(ppt1)) {

                HSLFTable tab = (HSLFTable) ppt2.getSlides().get(0).getShapes().get(2);
                HSLFTableCell c2 = tab.getCell(0, 0);
                assertNotNull(c2);
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
                assertNotNull(tr1.getFontColor());
                assertEquals(Color.black, DrawPaint.applyColorTransform(tr1.getFontColor().getSolidColor()));
                assertEquals(tr0.getFontFamily(), tr1.getFontFamily());
                assertEquals(tr0.getFontSize(), tr1.getFontSize());
                assertEquals(tr0.isItalic(), tr1.isItalic());
                assertEquals(tr0.isShadowed(), tr1.isShadowed());
                assertEquals(tr0.isStrikethrough(), tr1.isStrikethrough());
                assertEquals(tr0.isUnderlined(), tr1.isUnderlined());
            }
        }
    }

    @Test
    void bug47904() throws IOException {
        try (HSLFSlideShow ppt1 = new HSLFSlideShow()) {
            HSLFSlideMaster sm = ppt1.getSlideMasters().get(0);
            HSLFAutoShape as = (HSLFAutoShape) sm.getPlaceholder(Placeholder.TITLE);
            HSLFTextParagraph tp = as.getTextParagraphs().get(0);
            HSLFTextRun tr = tp.getTextRuns().get(0);
            tr.setFontFamily("Tahoma");
            tr.setShadowed(true);
            tr.setFontSize(44.);
            tr.setFontColor(Color.red);
            tp.setTextAlign(TextAlign.RIGHT);
            HSLFTextBox tb = ppt1.createSlide().addTitle();
            tb.setText("foobaa");

            try (HSLFSlideShow ppt2 = writeOutAndReadBack(ppt1)) {
                HSLFTextShape ts = (HSLFTextShape) ppt2.getSlides().get(0).getShapes().get(0);
                tp = ts.getTextParagraphs().get(0);
                tr = tp.getTextRuns().get(0);
                assertNotNull(tr);
                assertNotNull(tr.getFontSize());
                assertEquals(44., tr.getFontSize(), 0);
                assertEquals("Tahoma", tr.getFontFamily());
                assertNotNull(tr.getFontColor());
                Color colorAct = DrawPaint.applyColorTransform(tr.getFontColor().getSolidColor());
                assertEquals(Color.red, colorAct);
                assertEquals(TextAlign.RIGHT, tp.getTextAlign());
                assertEquals("foobaa", tr.getRawText());
            }
        }
    }

    @Test
    void bug58718() throws IOException {
        String[] files = {"bug58718_008524.ppt", "bug58718_008558.ppt", "bug58718_349008.ppt", "bug58718_008495.ppt",};
        for (String f : files) {
            File sample = HSLFTestDataSamples.getSampleFile(f);
            try (SlideShowExtractor<?,?> ex = new SlideShowExtractor<>(SlideShowFactory.create(sample))) {
                 assertNotNull(ex.getText());
             }
        }
    }

    @Test
    void bug58733() throws IOException {
        File sample = HSLFTestDataSamples.getSampleFile("bug58733_671884.ppt");
        try (SlideShowExtractor<?,?> ex = new SlideShowExtractor<>(SlideShowFactory.create(sample))) {
            assertNotNull(ex.getText());
        }
    }

    @Test
    void bug58159() throws IOException {
        try (HSLFSlideShow ppt = open("bug58159_headers-and-footers.ppt")) {
            HeadersFooters hf = ppt.getSlideHeadersFooters();
            assertNull(hf.getHeaderText());
            assertEquals("Slide footer", hf.getFooterText());
            hf = ppt.getNotesHeadersFooters();
            assertEquals("Notes header", hf.getHeaderText());
            assertEquals("Notes footer", hf.getFooterText());
            HSLFSlide sl = ppt.getSlides().get(0);
            hf = sl.getHeadersFooters();
            assertNull(hf.getHeaderText());
            assertEquals("Slide footer", hf.getFooterText());
            for (HSLFShape shape : sl.getShapes()) {
                if (shape instanceof HSLFTextShape) {
                    HSLFTextShape ts = (HSLFTextShape) shape;
                    Placeholder ph = ts.getPlaceholder();
                    if (Placeholder.FOOTER == ph) {
                        assertEquals("Slide footer", ts.getText());
                    }
                }
            }
        }
    }

    @Test
    void bug55030() throws IOException {
        try (HSLFSlideShow ppt = open("bug55030.ppt")) {
            String expFamily = "\u96b6\u4e66";

            HSLFSlide sl = ppt.getSlides().get(0);
            for (List<HSLFTextParagraph> paraList : sl.getTextParagraphs()) {
                for (HSLFTextParagraph htp : paraList) {
                    for (HSLFTextRun htr : htp) {
                        String actFamily = htr.getFontFamily(FontGroup.EAST_ASIAN);
                        assertEquals(expFamily, actFamily);
                    }
                }
            }
        }
    }

    @Test
    void bug59056() throws IOException {
        try (HSLFSlideShow ppt = open("54541_cropped_bitmap.ppt")) {
            for (HSLFShape shape : ppt.getSlides().get(0).getShapes()) {
                BufferedImage img = new BufferedImage(500, 300, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = img.createGraphics();
                Rectangle2D box = new Rectangle2D.Double(50, 50, 300, 100);
                graphics.setPaint(Color.red);
                graphics.fill(box);
                box = new Rectangle2D.Double(box.getX() + 1, box.getY() + 1, box.getWidth() - 2, box.getHeight() - 2);
                DrawFactory.getInstance(graphics).drawShape(graphics, shape, box);
                graphics.dispose();
            }
        }

    }

    private static HSLFSlideShow open(String fileName) throws IOException {
        File sample = HSLFTestDataSamples.getSampleFile(fileName);
        // Note: don't change the code here, it is required for Eclipse to compile the code
        SlideShow<?,?> slideShowOrig = SlideShowFactory.create(sample, null, false);
        return (HSLFSlideShow)slideShowOrig;
    }

    @Test
    void bug55983() throws IOException {
        try (HSLFSlideShow ppt1 = new HSLFSlideShow()) {
            HSLFSlide sl = ppt1.createSlide();
            assertNotNull(sl.getBackground());
            HSLFFill fill = sl.getBackground().getFill();
            assertNotNull(fill);
            fill.setForegroundColor(Color.blue);
            HSLFFreeformShape fs = sl.createFreeform();
            Ellipse2D.Double el = new Ellipse2D.Double(0, 0, 300, 200);
            fs.setAnchor(new Rectangle2D.Double(100, 100, 300, 200));
            fs.setPath(new Path2D.Double(el));
            Color cExp = new Color(50, 100, 150, 200);
            fs.setFillColor(cExp);

            try (HSLFSlideShow ppt2 = writeOutAndReadBack(ppt1)) {
                sl = ppt2.getSlides().get(0);
                fs = (HSLFFreeformShape) sl.getShapes().get(0);
                Color cAct = fs.getFillColor();
                assertEquals(cExp.getRed(), cAct.getRed());
                assertEquals(cExp.getGreen(), cAct.getGreen());
                assertEquals(cExp.getBlue(), cAct.getBlue());
                assertEquals(cExp.getAlpha(), cAct.getAlpha(), 1);

                PaintStyle ps = fs.getFillStyle().getPaint();
                assertTrue(ps instanceof SolidPaint);
                ColorStyle cs = ((SolidPaint) ps).getSolidColor();
                cAct = cs.getColor();
                assertEquals(cExp.getRed(), cAct.getRed());
                assertEquals(cExp.getGreen(), cAct.getGreen());
                assertEquals(cExp.getBlue(), cAct.getBlue());
                assertEquals(255, cAct.getAlpha());
                assertEquals(cExp.getAlpha() * 100000. / 255., cs.getAlpha(), 1);
            }
        }
    }

    @Test
    void bug59302() throws IOException {
        //add extraction from PPT
        Map<String, String> macros = getMacrosFromHSLF("59302.ppt");
        assertNotNull(macros, "couldn't find macros");
        assertNotNull(macros.get("Module2"), "couldn't find second module");
        assertContains(macros.get("Module2"), "newMacro in Module2");

        assertNotNull(macros.get("Module1"), "couldn't find first module");
        assertContains(macros.get("Module1"), "Italicize");

        macros = getMacrosFromHSLF("SimpleMacro.ppt");
        assertNotNull(macros);
        assertNotNull(macros.get("Module1"));
        assertContains(macros.get("Module1"), "This is a macro slideshow");
    }

    //It isn't pretty, but it works...
    private Map<String, String> getMacrosFromHSLF(String fileName) throws IOException {
        try (InputStream is = new FileInputStream(POIDataSamples.getSlideShowInstance().getFile(fileName));
             POIFSFileSystem poifs = new POIFSFileSystem(is);
             HSLFSlideShow ppt = new HSLFSlideShow(poifs)) {
            //TODO: should we run the VBAMacroReader on this poifs?
            //TBD: We know that ppt typically don't store macros in the regular place,
            //but _can_ they?

            //get macro persist id
            DocInfoListContainer list = (DocInfoListContainer)ppt.getDocumentRecord().findFirstOfType(RecordTypes.List.typeID);
            VBAInfoContainer vbaInfo = (VBAInfoContainer)list.findFirstOfType(RecordTypes.VBAInfo.typeID);
            VBAInfoAtom vbaAtom = (VBAInfoAtom)vbaInfo.findFirstOfType(RecordTypes.VBAInfoAtom.typeID);
            long persistId = vbaAtom.getPersistIdRef();
            for (HSLFObjectData objData : ppt.getEmbeddedObjects()) {
                if (objData.getExOleObjStg().getPersistId() == persistId) {
                    try (VBAMacroReader mr = new VBAMacroReader(objData.getInputStream())) {
                        return mr.readMacros();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Bug 60294: Add "unknown" ShapeType for 4095
     */
    @Test
    void bug60294() throws IOException {
        try (HSLFSlideShow ppt = open("60294.ppt")) {
            List<HSLFShape> shList = ppt.getSlides().get(0).getShapes();
            assertEquals(ShapeType.NOT_PRIMITIVE, shList.get(2).getShapeType());
        }
    }
}
