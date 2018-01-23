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

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.poi.util.StringUtil;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import static org.apache.poi.POITestCase.assertContains;
import static org.apache.poi.POITestCase.assertEndsWith;
import static org.apache.poi.POITestCase.assertStartsWith;

/**
 * Tests for HXFWordExtractor
 */
public class TestXWPFWordExtractor extends TestCase {

    /**
     * Get text out of the simple file
     *
     * @throws IOException
     */
    public void testGetSimpleText() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("sample.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

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

        extractor.close();
    }

    /**
     * Tests getting the text out of a complex file
     *
     * @throws IOException
     */
    public void testGetComplexText() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("IllustrativeCases.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

        String text = extractor.getText();
        assertTrue(text.length() > 0);

        char euro = '\u20ac';
//		System.err.println("'"+text.substring(text.length() - 40) + "'");

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

        extractor.close();
    }

    public void testGetWithHyperlinks() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("TestDocument.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

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

        extractor.close();
    }

    public void testHeadersFooters() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("ThreeColHeadFoot.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

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

        // Now another file, expect multiple headers
        //  and multiple footers
        doc = XWPFTestDataSamples.openSampleDocument("DiffFirstPageHeadFoot.docx");
        extractor.close();

        extractor = new XWPFWordExtractor(doc);
        extractor.close();

        extractor =
                new XWPFWordExtractor(doc);
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

        extractor.close();
    }

    public void testFootnotes() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("footnotes.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
        String text = extractor.getText();
        assertContains(text,"snoska");
        assertContains(text,"Eto ochen prostoy[footnoteRef:1] text so snoskoy");

        extractor.close();
    }


    public void testTableFootnotes() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("table_footnotes.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

        assertContains(extractor.getText(),"snoska");

        extractor.close();
    }

    public void testFormFootnotes() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("form_footnotes.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

        String text = extractor.getText();
        assertContains(text,"testdoc");
        assertContains(text,"test phrase");

        extractor.close();
    }

    public void testEndnotes() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("endnotes.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
        String text = extractor.getText();
        assertContains(text,"XXX");
        assertContains(text,"tilaka [endnoteRef:2]or 'tika'");

        extractor.close();
    }

    public void testInsertedDeletedText() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("delins.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

        assertContains(extractor.getText(),"pendant worn");
        assertContains(extractor.getText(),"extremely well");

        extractor.close();
    }

    public void testParagraphHeader() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Headers.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

        assertContains(extractor.getText(),"Section 1");
        assertContains(extractor.getText(),"Section 2");
        assertContains(extractor.getText(),"Section 3");

        extractor.close();
    }

    /**
     * Test that we can open and process .docm
     * (macro enabled) docx files (bug #45690)
     *
     * @throws IOException
     */
    public void testDOCMFiles() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("45690.docm");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

        assertContains(extractor.getText(),"2004");
        assertContains(extractor.getText(),"2008");
        assertContains(extractor.getText(),"(120 ");

        extractor.close();
    }

    /**
     * Test that we handle things like tabs and
     * carriage returns properly in the text that
     * we're extracting (bug #49189)
     *
     * @throws IOException
     */
    public void testDocTabs() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("WithTabs.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

        // Check bits
        assertContains(extractor.getText(),"a");
        assertContains(extractor.getText(),"\t");
        assertContains(extractor.getText(),"b");

        // Now check the first paragraph in total
        assertContains(extractor.getText(),"a\tb\n");

        extractor.close();
    }

    /**
     * The output should not contain field codes, e.g. those specified in the
     * w:instrText tag (spec sec. 17.16.23)
     *
     * @throws IOException
     */
    public void testNoFieldCodes() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("FieldCodes.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
        String text = extractor.getText();
        assertTrue(text.length() > 0);
        assertFalse(text.contains("AUTHOR"));
        assertFalse(text.contains("CREATEDATE"));

        extractor.close();
    }

    /**
     * The output should contain the values of simple fields, those specified
     * with the fldSimple element (spec sec. 17.16.19)
     *
     * @throws IOException
     */
    public void testFldSimpleContent() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("FldSimple.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
        String text = extractor.getText();
        assertTrue(text.length() > 0);
        assertContains(text,"FldSimple.docx");

        extractor.close();
    }

    /**
     * Test for parsing document with drawings to prevent
     * NoClassDefFoundError for CTAnchor in XWPFRun
     */
    public void testDrawings() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("drawing.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
        String text = extractor.getText();
        assertTrue(text.length() > 0);

        extractor.close();
    }

    /**
     * Test for basic extraction of SDT content
     *
     * @throws IOException
     */
    public void testSimpleControlContent() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Bug54849.docx");
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
        XWPFWordExtractor ex = new XWPFWordExtractor(doc);
        String s = ex.getText().toLowerCase(Locale.ROOT);
        int hits = 0;

        for (String targ : targs) {
            boolean hit = false;
            if (s.contains(targ)) {
                hit = true;
                hits++;
            }
            assertEquals("controlled content loading-" + targ, true, hit);
        }
        assertEquals("controlled content loading hit count", targs.length, hits);
        ex.close();


        doc = XWPFTestDataSamples.openSampleDocument("Bug54771a.docx");
        targs = new String[]{
                "bb",
                "test subtitle\n",
                "test user\n",
        };
        ex = new XWPFWordExtractor(doc);
        s = ex.getText().toLowerCase(Locale.ROOT);

        //At one point in development there were three copies of the text.
        //This ensures that there is only one copy.
        for (String targ : targs) {
            Matcher m = Pattern.compile(targ).matcher(s);
            int hit = 0;
            while (m.find()) {
                hit++;
            }
            assertEquals("controlled content loading-" + targ, 1, hit);
        }
        //"test\n" appears twice: once as the "title" and once in the text.
        //This also happens when you save this document as text from MSWord.
        Matcher m = Pattern.compile("test\n").matcher(s);
        int hit = 0;
        while (m.find()) {
            hit++;
        }
        assertEquals("test<N>", 2, hit);
        ex.close();
    }

    /**
     * No Header or Footer in document
     */
    public void testBug55733() throws Exception {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("55733.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

        // Check it gives text without error
        extractor.getText();
        extractor.close();
    }

    public void testCheckboxes() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("checkboxes.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

        assertEquals("This is a small test for checkboxes \nunchecked: |_| \n" +
                "Or checked: |X|\n\n\n\n\n" +
                "Test a checkbox within a textbox: |_| -> |X|\n\n\n" +
                "In Table:\n|_|\t|X|\n\n\n" +
                "In Sequence:\n|X||_||X|\n", extractor.getText());
        extractor.close();
    }
    
    public void testMultipleBodyBug() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("MultipleBodyBug.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
        assertEquals("START BODY 1 The quick, brown fox jumps over a lazy dog. END BODY 1.\n"
                        + "START BODY 2 The quick, brown fox jumps over a lazy dog. END BODY 2.\n"
                        + "START BODY 3 The quick, brown fox jumps over a lazy dog. END BODY 3.\n",
                extractor.getText());
        extractor.close();
    }

    public void testPhonetic() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("61470.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
        //expect: baseText (phoneticText)
        assertEquals("\u6771\u4EAC (\u3068\u3046\u304D\u3087\u3046)", extractor.getText().trim());
        extractor.close();
        extractor = new XWPFWordExtractor(doc);
        extractor.setConcatenatePhoneticRuns(false);
        assertEquals("\u6771\u4EAC", extractor.getText().trim());
    }

    public void testCTPictureBase() throws IOException {
        //This forces ctpicturebase to be included in the poi-ooxml-schemas jar
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("61991.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
        String txt = extractor.getText();
        assertContains(txt, "Sequencing data");
        extractor.close();
    }
}
