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

import static org.apache.poi.hslf.HSLFTestDataSamples.getSlideShow;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.hslf.HSLFTestDataSamples;
import org.apache.poi.hslf.model.textproperties.TextPropCollection;
import org.apache.poi.hslf.record.DateTimeMCAtom;
import org.apache.poi.hslf.record.TextBytesAtom;
import org.apache.poi.hslf.record.TextCharsAtom;
import org.apache.poi.sl.usermodel.BaseTestSlideShow;
import org.apache.poi.sl.usermodel.PlaceholderDetails;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Test;

/**
 * Tests for TextRuns
 */
@SuppressWarnings("UnusedAssignment")
public final class TestTextRun {
    /**
     * Test to ensure that getting the text works correctly
     */
    @Test
    void testGetText() throws IOException {
        try (HSLFSlideShow ppt = getSlideShow("basic_test_ppt_file.ppt")) {
            HSLFSlide slideOne = ppt.getSlides().get(0);
            List<List<HSLFTextParagraph>> textParas = slideOne.getTextParagraphs();

            // Get text works with \n
            String[] exp1 = { "This is a test title", "This is a test subtitle\nThis is on page 1" };
            String[] act1 = textParas.stream().map(HSLFTextParagraph::getText).toArray(String[]::new);
            assertArrayEquals(exp1, act1);

            // Raw text has \r instead
            String[] exp2 = { "This is a test title", "This is a test subtitle\rThis is on page 1" };
            String[] act2 = textParas.stream().map(HSLFTextParagraph::getRawText).toArray(String[]::new);
            assertArrayEquals(exp2, act2);
        }

        // Now check on a rich text run
        try (HSLFSlideShow ppt = getSlideShow("Single_Coloured_Page.ppt")) {
            List<List<HSLFTextParagraph>> textParas = ppt.getSlides().get(0).getTextParagraphs();

            String[] exp1 = { "This is a title, it\u2019s in black", "This is the subtitle, in bold\nThis bit is blue and italic\nThis bit is red (normal)" };
            String[] act1 = textParas.stream().map(HSLFTextParagraph::getText).toArray(String[]::new);
            assertArrayEquals(exp1, act1);

            String[] exp2 = { "This is a title, it\u2019s in black", "This is the subtitle, in bold\rThis bit is blue and italic\rThis bit is red (normal)" };
            String[] act2 = textParas.stream().map(HSLFTextParagraph::getRawText).toArray(String[]::new);
            assertArrayEquals(exp2, act2);
        }
    }

    /**
     * Test to ensure changing non rich text bytes->bytes works correctly
     */
    @Test
    void testSetText() throws IOException {
        try (HSLFSlideShow ppt = getSlideShow("basic_test_ppt_file.ppt")) {
            List<List<HSLFTextParagraph>> textRuns = ppt.getSlides().get(0).getTextParagraphs();
            HSLFTextParagraph run = textRuns.get(0).get(0);
            HSLFTextRun tr = run.getTextRuns().get(0);

            // Check current text
            assertEquals("This is a test title", tr.getRawText());

            // Change
            String changeTo = "New test title";
            tr.setText(changeTo);
            assertEquals(changeTo, tr.getRawText());

            // Ensure trailing \n's are NOT stripped, it is legal to set a text with a trailing '\r'
            tr.setText(changeTo + "\n");
            assertEquals(changeTo + "\r", tr.getRawText());
        }
    }

    /**
     * Test to ensure that changing non rich text between bytes and
     *  chars works correctly
     */
    @SuppressWarnings("unused")
    @Test
    void testAdvancedSetText() throws IOException {
        try (HSLFSlideShow ppt = getSlideShow("basic_test_ppt_file.ppt")) {
            List<HSLFTextParagraph> paras = ppt.getSlides().get(0).getTextParagraphs().get(0);
            final HSLFTextParagraph para = paras.get(0);

            final TextBytesAtom[] tba = { null };
            final TextCharsAtom[] tca = { null };
            Runnable extract = () -> {
                tba[0] = null;
                tca[0] = null;
                Stream.of(para.getRecords()).forEach(r -> {
                    if (r instanceof TextBytesAtom) tba[0] = (TextBytesAtom) r;
                    else if (r instanceof TextCharsAtom) tca[0] = (TextCharsAtom) r;
                });
            };

            // Bytes -> Bytes
            extract.run();
            assertNull(tca[0]);
            assertNotNull(tba);
            // assertFalse(run._isUnicode);
            assertEquals("This is a test title", para.getTextRuns().get(0).getRawText());

            String changeBytesOnly = "New Test Title";
            HSLFTextParagraph.setText(paras, changeBytesOnly);
            extract.run();
            assertEquals(changeBytesOnly, HSLFTextParagraph.getRawText(paras));
            assertNull(tca[0]);
            assertNotNull(tba);

            // Bytes -> Chars
            String changeByteChar = "This is a test title with a '\u0121' g with a dot";
            HSLFTextParagraph.setText(paras, changeByteChar);
            extract.run();
            assertEquals(changeByteChar, HSLFTextParagraph.getRawText(paras));
            assertNotNull(tca[0]);
            assertNull(tba[0]);

            // Chars -> Chars
            String changeCharChar = "This is a test title with a '\u0147' N with a hat";
            HSLFTextParagraph.setText(paras, changeCharChar);
            extract.run();

            assertEquals(changeCharChar, HSLFTextParagraph.getRawText(paras));
            assertNotNull(tca[0]);
            assertNull(tba[0]);
        }
    }

    /**
     * Tests to ensure that non rich text has the right default rich text run
     *  set up for it
     */
    @Test
    void testGetRichTextNonRich() throws IOException {
        try (HSLFSlideShow ppt = getSlideShow("basic_test_ppt_file.ppt")) {
            List<List<HSLFTextParagraph>> textParass = ppt.getSlides().get(0).getTextParagraphs();

            assertEquals(2, textParass.size());

            List<HSLFTextParagraph> trA = textParass.get(0);
            List<HSLFTextParagraph> trB = textParass.get(1);

            assertEquals(1, trA.size());
            assertEquals(2, trB.size());

            HSLFTextRun rtrA = trA.get(0).getTextRuns().get(0);
            HSLFTextRun rtrB = trB.get(0).getTextRuns().get(0);

            assertEquals(HSLFTextParagraph.getRawText(trA), rtrA.getRawText());
            assertEquals(HSLFTextParagraph.getRawText(trB.subList(0, 1)), rtrB.getRawText());
        }
    }

    /**
     * Tests to ensure that the rich text runs are built up correctly
     */
    @Test
    void testGetRichText() throws IOException {
        try (HSLFSlideShow ppt = getSlideShow("Single_Coloured_Page.ppt")) {
            List<List<HSLFTextParagraph>> textParass = ppt.getSlides().get(0).getTextParagraphs();

            assertEquals(2, textParass.size());

            List<HSLFTextParagraph> trA = textParass.get(0);
            List<HSLFTextParagraph> trB = textParass.get(1);

            assertEquals(1, trA.size());
            assertEquals(3, trB.size());

            HSLFTextRun rtrA = trA.get(0).getTextRuns().get(0);
            HSLFTextRun rtrB = trB.get(0).getTextRuns().get(0);
            HSLFTextRun rtrC = trB.get(1).getTextRuns().get(0);
            HSLFTextRun rtrD = trB.get(2).getTextRuns().get(0);

            assertEquals(HSLFTextParagraph.getRawText(trA), rtrA.getRawText());

            String trBstr = HSLFTextParagraph.getRawText(trB);
            assertEquals(trBstr.substring(0, 30), rtrB.getRawText());
            assertEquals(trBstr.substring(30, 58), rtrC.getRawText());
            assertEquals(trBstr.substring(58, 82), rtrD.getRawText());

            // Same paragraph styles
            assertEquals(trB.get(0).getParagraphStyle(), trB.get(1).getParagraphStyle());
            assertEquals(trB.get(0).getParagraphStyle(), trB.get(2).getParagraphStyle());

            // Different char styles
            assertNotEquals(rtrB.getCharacterStyle(), rtrC.getCharacterStyle());
            assertNotEquals(rtrB.getCharacterStyle(), rtrD.getCharacterStyle());
            assertNotEquals(rtrC.getCharacterStyle(), rtrD.getCharacterStyle());
        }
    }

    /**
     * Tests to ensure that setting the text where the text isn't rich,
     *  ensuring that everything stays with the same default styling
     */
    @Test
    void testSetTextWhereNotRich() throws IOException {
        try (HSLFSlideShow ppt = getSlideShow("basic_test_ppt_file.ppt")) {
            List<HSLFTextParagraph> trB = ppt.getSlides().get(0).getTextParagraphs().get(0);
            assertEquals(1, trB.size());

            HSLFTextRun rtrB = trB.get(0).getTextRuns().get(0);
            assertEquals(HSLFTextParagraph.getText(trB), rtrB.getRawText());

            // Change text via normal
            HSLFTextParagraph.setText(trB, "Test Foo Test");
            rtrB = trB.get(0).getTextRuns().get(0);
            assertEquals("Test Foo Test", HSLFTextParagraph.getRawText(trB));
            assertEquals("Test Foo Test", rtrB.getRawText());
        }
    }

    /**
     * Tests to ensure that setting the text where the text is rich
     *  sets everything to the same styling
     */
    @Test
    void testSetTextWhereRich() throws IOException {
        try (HSLFSlideShow ppt = getSlideShow("Single_Coloured_Page.ppt")) {
            List<HSLFTextParagraph> trB = ppt.getSlides().get(0).getTextParagraphs().get(1);
            assertEquals(3, trB.size());

            HSLFTextRun rtrB = trB.get(0).getTextRuns().get(0);
            HSLFTextRun rtrC = trB.get(1).getTextRuns().get(0);
            HSLFTextRun rtrD = trB.get(2).getTextRuns().get(0);
            TextPropCollection tpBP = rtrB.getTextParagraph().getParagraphStyle();
            TextPropCollection tpBC = rtrB.getCharacterStyle();
            TextPropCollection tpCP = rtrC.getTextParagraph().getParagraphStyle();
            TextPropCollection tpCC = rtrC.getCharacterStyle();
            TextPropCollection tpDP = rtrD.getTextParagraph().getParagraphStyle();
            TextPropCollection tpDC = rtrD.getCharacterStyle();

//      assertEquals(trB.getRawText().substring(0, 30), rtrB.getRawText());
            assertNotNull(tpBP);
            assertNotNull(tpBC);
            assertNotNull(tpCP);
            assertNotNull(tpCC);
            assertNotNull(tpDP);
            assertNotNull(tpDC);
            assertEquals(tpBP, tpCP);
            assertEquals(tpBP, tpDP);
            assertEquals(tpCP, tpDP);
            assertNotEquals(tpBC, tpCC);
            assertNotEquals(tpBC, tpDC);
            assertNotEquals(tpCC, tpDC);

            // Change text via normal
            HSLFTextParagraph.setText(trB, "Test Foo Test");

            // Ensure now have first style
            assertEquals(1, trB.get(0).getTextRuns().size());
            rtrB = trB.get(0).getTextRuns().get(0);
            assertEquals("Test Foo Test", HSLFTextParagraph.getRawText(trB));
            assertEquals("Test Foo Test", rtrB.getRawText());
            assertNotNull(rtrB.getCharacterStyle());
            assertNotNull(rtrB.getTextParagraph().getParagraphStyle());
            assertEquals(tpBP, rtrB.getTextParagraph().getParagraphStyle());
            assertEquals(tpBC, rtrB.getCharacterStyle());
        }
    }

    /**
     * Test to ensure the right stuff happens if we change the text
     *  in a rich text run, that doesn't happen to actually be rich
     */
    @Test
    void testChangeTextInRichTextRunNonRich() throws IOException {
        try (HSLFSlideShow ppt = getSlideShow("basic_test_ppt_file.ppt")) {
            List<List<HSLFTextParagraph>> textRuns = ppt.getSlides().get(0).getTextParagraphs();
            List<HSLFTextParagraph> trB = textRuns.get(1);
            assertEquals(1, trB.get(0).getTextRuns().size());

            HSLFTextRun rtrB = trB.get(0).getTextRuns().get(0);
            assertEquals(HSLFTextParagraph.getRawText(trB.subList(0, 1)), rtrB.getRawText());
            assertNotNull(rtrB.getCharacterStyle());
            assertNotNull(rtrB.getTextParagraph().getParagraphStyle());

            // Change text via rich
            rtrB.setText("Test Test Test");
            assertEquals("Test Test Test", HSLFTextParagraph.getRawText(trB.subList(0, 1)));
            assertEquals("Test Test Test", rtrB.getRawText());

            // Will now have dummy props
            assertNotNull(rtrB.getCharacterStyle());
            assertNotNull(rtrB.getTextParagraph().getParagraphStyle());
        }
    }

    /**
     * Tests to ensure changing the text within rich text runs works
     *  correctly
     */
    @Test
    void testChangeTextInRichTextRun() throws IOException {
        try (HSLFSlideShow ppt = getSlideShow("Single_Coloured_Page.ppt")) {
            HSLFSlide slideOne = ppt.getSlides().get(0);
            List<List<HSLFTextParagraph>> textParass = slideOne.getTextParagraphs();
            List<HSLFTextParagraph> trB = textParass.get(1);
            assertEquals(3, trB.size());

            // We start with 3 text runs, each with their own set of styles,
            // but all sharing the same paragraph styles
            HSLFTextRun rtrB = trB.get(0).getTextRuns().get(0);
            HSLFTextRun rtrC = trB.get(1).getTextRuns().get(0);
            HSLFTextRun rtrD = trB.get(2).getTextRuns().get(0);
            TextPropCollection tpBP = rtrB.getTextParagraph().getParagraphStyle();
            TextPropCollection tpBC = rtrB.getCharacterStyle();
            TextPropCollection tpCP = rtrC.getTextParagraph().getParagraphStyle();
            TextPropCollection tpCC = rtrC.getCharacterStyle();
            TextPropCollection tpDP = rtrD.getTextParagraph().getParagraphStyle();
            TextPropCollection tpDC = rtrD.getCharacterStyle();

            // Check text and stylings
            assertEquals(HSLFTextParagraph.getRawText(trB).substring(0, 30), rtrB.getRawText());
            assertNotNull(tpBP);
            assertNotNull(tpBC);
            assertNotNull(tpCP);
            assertNotNull(tpCC);
            assertNotNull(tpDP);
            assertNotNull(tpDC);
            assertEquals(tpBP, tpCP);
            assertEquals(tpBP, tpDP);
            assertEquals(tpCP, tpDP);
            assertNotEquals(tpBC, tpCC);
            assertNotEquals(tpBC, tpDC);
            assertNotEquals(tpCC, tpDC);

            // Check text in the rich runs
            assertEquals("This is the subtitle, in bold\r", rtrB.getRawText());
            assertEquals("This bit is blue and italic\r", rtrC.getRawText());
            assertEquals("This bit is red (normal)", rtrD.getRawText());

            String newBText = "New Subtitle, will still be bold\n";
            String newCText = "New blue and italic text\n";
            String newDText = "Funky new normal red text";
            rtrB.setText(newBText);
            rtrC.setText(newCText);
            rtrD.setText(newDText);
            HSLFTextParagraph.storeText(trB);

            assertEquals(newBText.replace('\n', '\r'), rtrB.getRawText());
            assertEquals(newCText.replace('\n', '\r'), rtrC.getRawText());
            assertEquals(newDText.replace('\n', '\r'), rtrD.getRawText());

            assertEquals(newBText.replace('\n', '\r') + newCText.replace('\n', '\r') + newDText.replace('\n', '\r'), HSLFTextParagraph.getRawText(trB));

            // The styles should have been updated for the new sizes
            assertEquals(newBText.length(), tpBC.getCharactersCovered());
            assertEquals(newCText.length(), tpCC.getCharactersCovered());
            assertEquals(newDText.length() + 1, tpDC.getCharactersCovered()); // Last one is always one larger

            // Paragraph style should be sum of text length
            assertEquals(
                newBText.length() + newCText.length() + newDText.length() + 1,
                tpBP.getCharactersCovered() + tpCP.getCharactersCovered() + tpDP.getCharactersCovered()
            );

            // Check stylings still as expected
            TextPropCollection ntpBC = rtrB.getCharacterStyle();
            TextPropCollection ntpCC = rtrC.getCharacterStyle();
            TextPropCollection ntpDC = rtrD.getCharacterStyle();
            assertEquals(tpBC.getTextPropList(), ntpBC.getTextPropList());
            assertEquals(tpCC.getTextPropList(), ntpCC.getTextPropList());
            assertEquals(tpDC.getTextPropList(), ntpDC.getTextPropList());
        }
    }


    /**
     * Test case for Bug 41015.
     *
     * In some cases RichTextRun.getText() threw StringIndexOutOfBoundsException because
     * of the wrong list of potential paragraph properties defined in StyleTextPropAtom.
     *
     */
    @Test
    void testBug41015() throws IOException {
        try (HSLFSlideShow ppt = getSlideShow("bug-41015.ppt")) {
            HSLFSlide sl = ppt.getSlides().get(0);
            List<List<HSLFTextParagraph>> textParass = sl.getTextParagraphs();
            assertEquals(2, textParass.size());

            List<HSLFTextParagraph> textParas = textParass.get(0);
            List<HSLFTextRun> rt = textParass.get(0).get(0).getTextRuns();
            assertEquals(1, rt.size());
            assertEquals(0, textParass.get(0).get(0).getIndentLevel());
            assertEquals("sdfsdfsdf", rt.get(0).getRawText());

            textParas = textParass.get(1);
            String[] texts = {"Sdfsdfsdf\r", "Dfgdfg\r", "Dfgdfgdfg\r", "Sdfsdfs\r", "Sdfsdf\r"};
            int[] indents = {0, 0, 0, 1, 1};
            int i = 0;
            for (HSLFTextParagraph p : textParas) {
                assertEquals(texts[i], p.getTextRuns().get(0).getRawText());
                assertEquals(indents[i], p.getIndentLevel());
                i++;
            }
        }
    }

    /**
     * Test creation of TextRun objects.
     */
    @Test
    void testAddTextRun() throws IOException {
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFSlide slide = ppt.createSlide();

            assertEquals(0, slide.getTextParagraphs().size());

            HSLFTextBox shape1 = new HSLFTextBox();
            List<HSLFTextParagraph> run1 = shape1.getTextParagraphs();
            shape1.setText("Text 1");
            slide.addShape(shape1);

            //The array of Slide's text runs must be updated when new text shapes are added.
            List<List<HSLFTextParagraph>> runs = slide.getTextParagraphs();
            assertNotNull(runs);
            assertSame(run1, runs.get(0));

            HSLFTextBox shape2 = new HSLFTextBox();
            List<HSLFTextParagraph> run2 = shape2.getTextParagraphs();
            shape2.setText("Text 2");
            slide.addShape(shape2);

            assertEquals(2, runs.size());

            assertSame(run1, runs.get(0));
            assertSame(run2, runs.get(1));

            // as getShapes()
            List<HSLFShape> sh = slide.getShapes();
            assertEquals(2, sh.size());
            assertInstanceOf(HSLFTextBox.class, sh.get(0));
            HSLFTextBox box1 = (HSLFTextBox) sh.get(0);
            assertSame(run1, box1.getTextParagraphs());
            HSLFTextBox box2 = (HSLFTextBox) sh.get(1);
            assertSame(run2, box2.getTextParagraphs());

            // test Table - a complex group of shapes containing text objects
            HSLFSlide slide2 = ppt.createSlide();
            assertTrue(slide2.getTextParagraphs().isEmpty());
            HSLFTable table = new HSLFTable(2, 2);
            slide2.addShape(table);
            runs = slide2.getTextParagraphs();
            assertNotNull(runs);
            assertEquals(4, runs.size());
        }
    }

    @Test
    void test48916() throws IOException {
        try (HSLFSlideShow ppt1 = getSlideShow("SampleShow.ppt")) {
            List<HSLFSlide> slides = ppt1.getSlides();
            for (HSLFSlide slide : slides) {
                for (HSLFShape sh : slide.getShapes()) {
                    if (!(sh instanceof HSLFTextShape)) continue;
                    HSLFTextShape tx = (HSLFTextShape) sh;
                    List<HSLFTextParagraph> paras = tx.getTextParagraphs();
                    //verify that records cached in  TextRun and EscherTextboxWrapper are the same
                    org.apache.poi.hslf.record.Record[] runChildren = paras.get(0).getRecords();
                    org.apache.poi.hslf.record.Record[] txboxChildren = tx.getEscherTextboxWrapper().getChildRecords();
                    assertEquals(runChildren.length, txboxChildren.length);
                    for (int i = 0; i < txboxChildren.length; i++) {
                        assertSame(txboxChildren[i], runChildren[i]);
                    }
                    // caused NPE prior to fix of Bugzilla #48916
                    for (HSLFTextParagraph p : paras) {
                        for (HSLFTextRun rt : p.getTextRuns()) {
                            rt.setBold(true);
                            rt.setFontColor(Color.RED);
                        }
                    }
                    // tx.storeText();
                }
            }

            try (HSLFSlideShow ppt2 = HSLFTestDataSamples.writeOutAndReadBack(ppt1)) {
                List<HSLFTextRun> runs = ppt2.getSlides().stream()
                    .flatMap(s -> s.getShapes().stream())
                    .filter(s -> s instanceof HSLFTextShape)
                    .map(s -> ((HSLFTextShape) s).getTextParagraphs().get(0).getTextRuns().get(0))
                    .collect(Collectors.toList());

                assertFalse(runs.isEmpty());
                assertTrue(runs.stream().allMatch(HSLFTextRun::isBold));
                assertTrue(runs.stream().map(HSLFTextRun::getFontColor)
                    .map(BaseTestSlideShow::getColor).allMatch(Color.RED::equals));
            }
        }
    }

    @Test
    void test52244() throws IOException {
        try (HSLFSlideShow ppt = getSlideShow("52244.ppt")) {
            HSLFSlide slide = ppt.getSlides().get(0);

            List<HSLFTextRun> runs = slide.getTextParagraphs().stream().map(tp -> tp.get(0).getTextRuns().get(0)).collect(Collectors.toList());
            assertTrue(runs.stream().map(HSLFTextRun::getFontFamily).allMatch("Arial"::equals));

            int[] exp = {36, 24, 12, 32, 12, 12};
            //noinspection ConstantConditions
            int[] act = runs.stream().map(HSLFTextRun::getFontSize).mapToInt(Double::intValue).toArray();
            assertArrayEquals(exp, act);
        }
    }


    @Test
    void testAppendEmpty() throws IOException {
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFSlide s = ppt.createSlide();
            HSLFTextBox title = s.addTitle();
            title.setText("");
            title.appendText("\n", true);
            title.appendText("para", true);
            assertEquals("\npara", title.getText());
        }
    }

    @Test
    void datetimeFormats() throws IOException {
        LocalDateTime ldt = LocalDateTime.of(2012, 3, 4, 23, 45, 26);
        final Map<Locale, String[]> formats = new HashMap<>();
        formats.put(Locale.GERMANY, new String[]{
            "04.03.2012",
            "Sonntag, 4. M\u00e4rz 2012",
            "04/03/12",
            "4. M\u00e4rz 2012",
            "12-03-04",
            "M\u00e4rz 12",
            "M\u00e4r-12",
            "04.03.12  23:45",
            "04.03.12  23:45:26",
            "23:45",
            "23:45:26",
            "11:45",
            "11:45:26"
        });
        formats.put(Locale.US, new String[]{
            "03/04/2012",
            "Sunday, March 4, 2012",
            "4 March 2012",
            "March 04, 2012",
            "4-Mar-12",
            "March 12",
            "Mar-12",
            "3/4/12  11:45 PM",
            "3/4/12  11:45:26 PM",
            "23:45",
            "23:45:26",
            "11:45 PM",
            "11:45:26 PM"
        });


        try (HSLFSlideShow ppt = getSlideShow("datetime.ppt")) {
            List<HSLFTextShape> shapes = ppt.getSlides().get(0).getShapes()
                .stream().map(HSLFTextShape.class::cast).collect(Collectors.toList());

            int[] expFormatId = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
            int[] actFormatId = shapes.stream().flatMap(tp -> Stream.of(tp.getTextParagraphs().get(0).getRecords()))
                .filter(r -> r instanceof DateTimeMCAtom)
                .mapToInt(r -> ((DateTimeMCAtom)r).getIndex()).toArray();
            assertArrayEquals(expFormatId, actFormatId);

            List<HSLFShapePlaceholderDetails> phs =
                    shapes.stream().map(HSLFSimpleShape::getPlaceholderDetails).collect(Collectors.toList());

            for (Map.Entry<Locale,String[]> me : formats.entrySet()) {
                LocaleUtil.setUserLocale(me.getKey());

                // refresh internal members
                phs.forEach(PlaceholderDetails::getPlaceholder);

                String[] actDate = phs.stream().map(PlaceholderDetails::getDateFormat).map(ldt::format).
                        // JDK 23 removes the COMPAT/JRE locale provider and this changes blanks to some non-breaking space
                        map(s -> s.replace(" ", " ")).
                        toArray(String[]::new);
                String[] values = me.getValue();

                assertArrayEquals(values, actDate,
                        "While handling local " + me.getKey());
            }
        } finally {
            LocaleUtil.resetUserLocale();
        }
    }
}
