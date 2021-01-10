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

package org.apache.poi.xwpf.extractor;

import static org.apache.poi.POITestCase.assertContains;
import static org.apache.poi.POITestCase.assertEndsWith;
import static org.apache.poi.POITestCase.assertNotContained;
import static org.apache.poi.POITestCase.assertStartsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.util.StringUtil;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

/**
 * Tests for HXFWordExtractor
 */
class TestXWPFWordExtractor {

    /**
     * Get text out of the simple file
     */
    @Test
    void testGetSimpleText() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("sample.docx");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {

            String text = extractor.getText();
            assertTrue(text.length() > 0);

            // Check contents
            assertStartsWith(text,
                    "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Nunc at risus vel erat tempus posuere. Aenean non ante. Suspendisse vehicula dolor sit amet odio."
            );
            assertEndsWith(text,
                    "Phasellus ultricies mi nec leo. Sed tempus. In sit amet lorem at velit faucibus vestibulum.\n"
            );

            // Check number of paragraphs by counting number of newlines
            int numberOfParagraphs = StringUtil.countMatches(text, '\n');
            assertEquals(3, numberOfParagraphs);
        }
    }

    /**
     * Tests getting the text out of a complex file
     */
    @Test
    void testGetComplexText() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("IllustrativeCases.docx");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {

            String text = extractor.getText();
            assertTrue(text.length() > 0);

            char euro = '\u20ac';

            // Check contents
            assertStartsWith(text,
                    "  \n(V) ILLUSTRATIVE CASES\n\n"
            );
            assertContains(text,
                    "As well as gaining " + euro + "90 from child benefit increases, he will also receive the early childhood supplement of " + euro + "250 per quarter for Vincent for the full four quarters of the year.\n\n\n\n"// \n\n\n"
            );
            assertEndsWith(text,
                    "11.4%\t\t90\t\t\t\t\t250\t\t1,310\t\n\n \n\n\n"
            );

            // Check number of paragraphs by counting number of newlines
            int numberOfParagraphs = StringUtil.countMatches(text, '\n');
            assertEquals(134, numberOfParagraphs);
        }
    }

    @Test
    void testGetWithHyperlinks() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("TestDocument.docx");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {

            // Now check contents
            extractor.setFetchHyperlinks(false);
            assertEquals(
                    "This is a test document.\nThis bit is in bold and italic\n" +
                            "Back to normal\n" +
                            "This contains BOLD, ITALIC and BOTH, as well as RED and YELLOW text.\n" +
                            "We have a hyperlink here, and another.\n",
                    extractor.getText()
            );

            // One hyperlink is a real one, one is just to the top of page
            extractor.setFetchHyperlinks(true);
            assertEquals(
                    "This is a test document.\nThis bit is in bold and italic\n" +
                            "Back to normal\n" +
                            "This contains BOLD, ITALIC and BOTH, as well as RED and YELLOW text.\n" +
                            "We have a hyperlink <http://poi.apache.org/> here, and another.\n",
                    extractor.getText()
            );
        }
    }

    @Test
    void testHeadersFooters() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("ThreeColHeadFoot.docx");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {

            assertEquals(
                "First header column!\tMid header\tRight header!\n" +
                "This is a sample word document. It has two pages. It has a three column heading, and a three column footer\n" +
                "\n" +
                "HEADING TEXT\n" +
                "\n" +
                "More on page one\n" +
                "\n\n" +
                "End of page 1\n\n\n" +
                "This is page two. It also has a three column heading, and a three column footer.\n" +
                "Footer Left\tFooter Middle\tFooter Right\n",
                extractor.getText()
            );
        }

        // Now another file, expect multiple headers
        //  and multiple footers
        try (XWPFDocument doc2 = XWPFTestDataSamples.openSampleDocument("DiffFirstPageHeadFoot.docx")) {

            new XWPFWordExtractor(doc2).close();

            try (XWPFWordExtractor extractor = new XWPFWordExtractor(doc2)) {
                extractor.getText();

                assertEquals(
                    "I am the header on the first page, and I" + '\u2019' + "m nice and simple\n" +
                    "First header column!\tMid header\tRight header!\n" +
                    "This is a sample word document. It has two pages. It has a simple header and footer, which is different to all the other pages.\n" +
                    "\n" +
                    "HEADING TEXT\n" +
                    "\n" +
                    "More on page one\n" +
                    "\n\n" +
                    "End of page 1\n\n\n" +
                    "This is page two. It also has a three column heading, and a three column footer.\n" +
                    "The footer of the first page\n" +
                    "Footer Left\tFooter Middle\tFooter Right\n",
                    extractor.getText()
                );

            }
        }
    }

    @Test
    void testFootnotes() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("footnotes.docx");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            String text = extractor.getText();
            assertContains(text, "snoska");
            assertContains(text, "Eto ochen prostoy[footnoteRef:1] text so snoskoy");
        }
    }


    @Test
    void testTableFootnotes() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("table_footnotes.docx");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {

            assertContains(extractor.getText(), "snoska");
        }
    }

    @Test
    void testFormFootnotes() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("form_footnotes.docx");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {

            String text = extractor.getText();
            assertContains(text, "testdoc");
            assertContains(text, "test phrase");
        }
    }

    @Test
    void testEndnotes() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("endnotes.docx");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            String text = extractor.getText();
            assertContains(text, "XXX");
            assertContains(text, "tilaka [endnoteRef:2]or 'tika'");
        }
    }

    @Test
    void testInsertedDeletedText() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("delins.docx");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {

            assertContains(extractor.getText(), "pendant worn");
            assertContains(extractor.getText(), "extremely well");
        }
    }

    @Test
    void testParagraphHeader() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Headers.docx");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {

            assertContains(extractor.getText(), "Section 1");
            assertContains(extractor.getText(), "Section 2");
            assertContains(extractor.getText(), "Section 3");
        }
    }

    /**
     * Test that we can open and process .docm
     * (macro enabled) docx files (bug #45690)
     */
    @Test
    void testDOCMFiles() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("45690.docm");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {

            assertContains(extractor.getText(), "2004");
            assertContains(extractor.getText(), "2008");
            assertContains(extractor.getText(), "(120 ");
        }
    }

    /**
     * Test that we handle things like tabs and
     * carriage returns properly in the text that
     * we're extracting (bug #49189)
     */
    @Test
    void testDocTabs() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("WithTabs.docx");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {

            // Check bits
            assertContains(extractor.getText(), "a");
            assertContains(extractor.getText(), "\t");
            assertContains(extractor.getText(), "b");

            // Now check the first paragraph in total
            assertContains(extractor.getText(), "a\tb\n");
        }
    }

    /**
     * The output should not contain field codes, e.g. those specified in the
     * w:instrText tag (spec sec. 17.16.23)
     */
    @Test
    void testNoFieldCodes() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("FieldCodes.docx");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            String text = extractor.getText();
            assertTrue(text.length() > 0);
            assertFalse(text.contains("AUTHOR"));
            assertFalse(text.contains("CREATEDATE"));
        }
    }

    /**
     * The output should contain the values of simple fields, those specified
     * with the fldSimple element (spec sec. 17.16.19)
     */
    @Test
    void testFldSimpleContent() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("FldSimple.docx");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            String text = extractor.getText();
            assertTrue(text.length() > 0);
            assertContains(text, "FldSimple.docx");
        }
    }

    /**
     * Test for parsing document with drawings to prevent
     * NoClassDefFoundError for CTAnchor in XWPFRun
     */
    @Test
    void testDrawings() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("drawing.docx");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            String text = extractor.getText();
            assertTrue(text.length() > 0);
        }
    }

    /**
     * Test for basic extraction of SDT content
     */
    @Test
    void testSimpleControlContent() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Bug54849.docx");
             XWPFWordExtractor ex = new XWPFWordExtractor(doc)) {
            String[] targs = new String[]{
                "header_rich_text",
                "rich_text",
                "rich_text_pre_table\nrich_text_cell1\t\t\t\n\t\t\t\n\t\t\t\n\nrich_text_post_table",
                "plain_text_no_newlines",
                "plain_text_with_newlines1\nplain_text_with_newlines2\n",
                "watermelon\n",
                "dirt\n",
                "4/16/2013\n",
                "rich_text_in_cell",
                "abc",
                "rich_text_in_paragraph_in_cell",
                "footer_rich_text",
                "footnote_sdt",
                "endnote_sdt"
            };
            String s = ex.getText().toLowerCase(Locale.ROOT);
            int hits = 0;

            for (String targ : targs) {
                boolean hit = false;
                if (s.contains(targ)) {
                    hit = true;
                    hits++;
                }
                assertTrue(hit, "controlled content loading-" + targ);
            }
            assertEquals(targs.length, hits, "controlled content loading hit count");
        }

        try (XWPFDocument doc2 = XWPFTestDataSamples.openSampleDocument("Bug54771a.docx");
             XWPFWordExtractor ex = new XWPFWordExtractor(doc2)) {
            String s = ex.getText().toLowerCase(Locale.ROOT);

            String[] targs = {
                "bb",
                "test subtitle\n",
                "test user\n",
            };

            //At one point in development there were three copies of the text.
            //This ensures that there is only one copy.
            for (String targ : targs) {
                Matcher m = Pattern.compile(targ).matcher(s);
                int hit = 0;
                while (m.find()) {
                    hit++;
                }
                assertEquals(1, hit, "controlled content loading-" + targ);
            }
            //"test\n" appears twice: once as the "title" and once in the text.
            //This also happens when you save this document as text from MSWord.
            Matcher m = Pattern.compile("test\n").matcher(s);
            int hit = 0;
            while (m.find()) {
                hit++;
            }
            assertEquals(2, hit, "test<N>");
        }
    }

    /**
     * No Header or Footer in document
     */
    @Test
    void testBug55733() throws Exception {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("55733.docx");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {

            // Check it gives text without error
            extractor.getText();
        }
    }

    @Test
    void testCheckboxes() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("checkboxes.docx");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {

            assertEquals("This is a small test for checkboxes \nunchecked: |_| \n" +
                                 "Or checked: |X|\n\n\n\n\n" +
                                 "Test a checkbox within a textbox: |_| -> |X|\n\n\n" +
                                 "In Table:\n|_|\t|X|\n\n\n" +
                                 "In Sequence:\n|X||_||X|\n", extractor.getText());
        }
    }

    @Test
    void testMultipleBodyBug() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("MultipleBodyBug.docx");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            assertEquals("START BODY 1 The quick, brown fox jumps over a lazy dog. END BODY 1.\n"
                                 + "START BODY 2 The quick, brown fox jumps over a lazy dog. END BODY 2.\n"
                                 + "START BODY 3 The quick, brown fox jumps over a lazy dog. END BODY 3.\n",
                         extractor.getText());
        }
    }

    @Test
    void testPhonetic() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("61470.docx")) {
            try (XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                //expect: baseText (phoneticText)
                assertEquals("\u6771\u4EAC (\u3068\u3046\u304D\u3087\u3046)", extractor.getText().trim());
            }
            try (XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                extractor.setConcatenatePhoneticRuns(false);
                assertEquals("\u6771\u4EAC", extractor.getText().trim());
            }
        }
    }

    @Test
    void testCTPictureBase() throws IOException {
        //This forces ctpicturebase to be included in the poi-ooxml-lite jar
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("61991.docx");
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            String txt = extractor.getText();
            assertContains(txt, "Sequencing data");
        }
    }

    @Test
    void testGlossary() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("60316.dotx")) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            String txt = extractor.getText();
            assertContains(txt, "Getting the perfect");
            //this content appears only in the glossary document
            //once we add processing for this, we can change this to contains
            assertNotContained(txt, "table rows");
        }
    }

    @Test
    void testPartsInTemplate() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("60316b.dotx")) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            String txt = extractor.getText();
            assertContains(txt, "header 2");
            assertContains(txt, "footer 1");
        }
    }

    @Test
    void bug55966() throws IOException  {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("55966.docx")) {
            String expected = "Content control within a paragraph is here text content from within a paragraph second control with a new\n" +
                    "line\n" +
                    "\n" +
                    "Content control that is the entire paragraph\n";

            XWPFWordExtractor extractedDoc = new XWPFWordExtractor(doc);

            String actual = extractedDoc.getText();

            extractedDoc.close();
            assertEquals(expected, actual);
        }
    }
}
