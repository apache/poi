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

import junit.framework.TestCase;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

/**
 * Tests for HXFWordExtractor
 */
public class TestXWPFWordExtractor extends TestCase {

    /**
     * Get text out of the simple file
     */
    public void testGetSimpleText() {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("sample.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

        String text = extractor.getText();
        assertTrue(text.length() > 0);

        // Check contents
        assertTrue(text.startsWith(
                "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Nunc at risus vel erat tempus posuere. Aenean non ante. Suspendisse vehicula dolor sit amet odio."
        ));
        assertTrue(text.endsWith(
                "Phasellus ultricies mi nec leo. Sed tempus. In sit amet lorem at velit faucibus vestibulum.\n"
        ));

        // Check number of paragraphs
        int ps = 0;
        char[] t = text.toCharArray();
        for (int i = 0; i < t.length; i++) {
            if (t[i] == '\n') {
                ps++;
            }
        }
        assertEquals(3, ps);
    }

    /**
     * Tests getting the text out of a complex file
     */
    public void testGetComplexText() {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("IllustrativeCases.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

        String text = extractor.getText();
        assertTrue(text.length() > 0);

        char euro = '\u20ac';
//		System.err.println("'"+text.substring(text.length() - 40) + "'");

        // Check contents
        assertTrue(text.startsWith(
                "  \n(V) ILLUSTRATIVE CASES\n\n"
        ));
        assertTrue(text.contains(
                "As well as gaining " + euro + "90 from child benefit increases, he will also receive the early childhood supplement of " + euro + "250 per quarter for Vincent for the full four quarters of the year.\n\n\n\n \n\n\n"
        ));
        assertTrue(text.endsWith(
                "11.4%\t\t90\t\t\t\t\t250\t\t1,310\t\n\n"
        ));

        // Check number of paragraphs
        int ps = 0;
        char[] t = text.toCharArray();
        for (int i = 0; i < t.length; i++) {
            if (t[i] == '\n') {
                ps++;
            }
        }
        assertEquals(103, ps);
    }

    public void testGetWithHyperlinks() {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("TestDocument.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

        // Now check contents
        // TODO - fix once correctly handling contents
        extractor.setFetchHyperlinks(false);
        assertEquals(
//				"This is a test document\nThis bit is in bold and italic\n" +
//				"Back to normal\nWe have a hyperlink here, and another.\n",
                "This is a test document\nThis bit is in bold and italic\n" +
                        "Back to normal\nWe have a  here, and .hyperlinkanother\n",
                extractor.getText()
        );

        extractor.setFetchHyperlinks(true);
        assertEquals(
//				"This is a test document\nThis bit is in bold and italic\n" +
//				"Back to normal\nWe have a hyperlink here, and another.\n",
                "This is a test document\nThis bit is in bold and italic\n" +
                        "Back to normal\nWe have a  here, and .hyperlink <http://poi.apache.org/>another\n",
                extractor.getText()
        );
    }

    public void testHeadersFooters() {
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
                        "End of page 1\n\n" +
                        "This is page two. It also has a three column heading, and a three column footer.\n" +
                        "Footer Left\tFooter Middle\tFooter Right\n",
                extractor.getText()
        );

        // Now another file, expect multiple headers
        //  and multiple footers
        doc = XWPFTestDataSamples.openSampleDocument("DiffFirstPageHeadFoot.docx");
        extractor = new XWPFWordExtractor(doc);
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
                        "End of page 1\n\n" +
                        "This is page two. It also has a three column heading, and a three column footer.\n" +
                        "The footer of the first page\n" +
                        "Footer Left\tFooter Middle\tFooter Right\n",
                extractor.getText()
        );
    }

    public void testFootnotes() {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("footnotes.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

        assertTrue(extractor.getText().contains("snoska"));
    }


    public void testTableFootnotes() {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("table_footnotes.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

        assertTrue(extractor.getText().contains("snoska"));
    }

    public void testFormFootnotes() {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("form_footnotes.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

        String text = extractor.getText();
        assertTrue("Unable to find expected word in text\n" + text, text.contains("testdoc"));
        assertTrue("Unable to find expected word in text\n" + text, text.contains("test phrase"));
    }

    public void testEndnotes() {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("endnotes.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

        assertTrue(extractor.getText().contains("XXX"));
    }

    public void testInsertedDeletedText() {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("delins.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

        assertTrue(extractor.getText().contains("pendant worn"));
        assertTrue(extractor.getText().contains("extremely well"));
    }

    public void testParagraphHeader() {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Headers.docx");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

        assertTrue(extractor.getText().contains("Section 1"));
        assertTrue(extractor.getText().contains("Section 2"));
        assertTrue(extractor.getText().contains("Section 3"));
    }

    /**
     * Test that we can open and process .docm
     *  (macro enabled) docx files (bug #45690)
     */
    public void testDOCMFiles() {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("45690.docm");
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

        assertTrue(extractor.getText().contains("2004"));
        assertTrue(extractor.getText().contains("2008"));
        assertTrue(extractor.getText().contains("(120 "));
    }
}
