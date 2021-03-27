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

package org.apache.poi.hwpf.usermodel;

import static org.apache.poi.POITestCase.assertContains;
import static org.apache.poi.hwpf.HWPFTestDataSamples.openOldSampleFile;
import static org.apache.poi.hwpf.HWPFTestDataSamples.openSampleFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.poi.OldFileFormatException;
import org.apache.poi.common.usermodel.fonts.FontCharset;
import org.apache.poi.hwpf.HWPFOldDocument;
import org.apache.poi.hwpf.HWPFTestCase;
import org.apache.poi.hwpf.extractor.Word6Extractor;
import org.apache.poi.hwpf.model.OldFontTable;
import org.junit.jupiter.api.Test;

/**
 * Tests for Word 6 and Word 95 support
 */
public final class TestHWPFOldDocument extends HWPFTestCase {
    /**
     * Test a simple Word 6 document
     */
    @Test
    void testWord6hwpf() {
        // Can't open as HWPFDocument
        assertThrows(OldFileFormatException.class, () -> openSampleFile("Word6.doc"));
    }

    @Test
    void testWord6hwpfOld() throws IOException {
        // Open
        HWPFOldDocument doc = openOldSampleFile("Word6.doc");

        // Check
        assertEquals(1, doc.getRange().numSections());
        assertEquals(1, doc.getRange().numParagraphs());
        assertEquals(1, doc.getRange().numCharacterRuns());

        assertEquals("The quick brown fox jumps over the lazy dog\r",
                doc.getRange().getParagraph(0).text());
        doc.close();
    }

    /**
     * Test a simple Word 2 document
     */
    @Test
    void testWord2hwpf() {
        // Can't open as HWPFDocument
        assertThrows(IllegalArgumentException.class, () -> openSampleFile("word2.doc"));
    }

    @Test
    void testWord2hwpfOld() {
        // Open
        assertThrows(RuntimeException.class, () -> openOldSampleFile("word2.doc"));
    }

    /**
     * Test a simple Word 95 document
     */
    @Test
    void testWord95hwpf() {
        // Can't open as HWPFDocument
        assertThrows(OldFileFormatException.class, () -> openSampleFile("Word95.doc"));
    }

    @Test
    void testWord95hwpfOld() throws IOException {
        // Open
        HWPFOldDocument doc = openOldSampleFile("Word95.doc");

        // Check
        assertEquals(1, doc.getRange().numSections());
        assertEquals(7, doc.getRange().numParagraphs());

        assertEquals("The quick brown fox jumps over the lazy dog\r",
                doc.getRange().getParagraph(0).text());
        assertEquals("\r", doc.getRange().getParagraph(1).text());
        assertEquals("Paragraph 2\r", doc.getRange().getParagraph(2).text());
        assertEquals("\r", doc.getRange().getParagraph(3).text());
        assertEquals(
                "Paragraph 3. Has some RED text and some "
                        + "BLUE BOLD text in it.\r",
                doc.getRange().getParagraph(4).text());
        assertEquals("\r", doc.getRange().getParagraph(5).text());
        assertEquals("Last (4th) paragraph.\r",
                doc.getRange().getParagraph(6).text());

        assertEquals(1, doc.getRange().getParagraph(0).numCharacterRuns());
        assertEquals(1, doc.getRange().getParagraph(1).numCharacterRuns());
        assertEquals(1, doc.getRange().getParagraph(2).numCharacterRuns());
        assertEquals(1, doc.getRange().getParagraph(3).numCharacterRuns());
        // Normal, red, normal, blue+bold, normal
        assertEquals(5, doc.getRange().getParagraph(4).numCharacterRuns());
        assertEquals(1, doc.getRange().getParagraph(5).numCharacterRuns());
        // Normal, superscript for 4th, normal
        assertEquals(3, doc.getRange().getParagraph(6).numCharacterRuns());

        doc.close();
    }

    /**
     * Test a word document that has sections, as well as the usual paragraph
     * stuff.
     */
    @Test
    void testWord6Sections() throws IOException {
        HWPFOldDocument doc = openOldSampleFile("Word6_sections.doc");

        assertEquals(3, doc.getRange().numSections());
        assertEquals(6, doc.getRange().numParagraphs());

        assertEquals("This is a test.\r",
                doc.getRange().getParagraph(0).text());
        assertEquals("\r", doc.getRange().getParagraph(1).text());
        // Section / line?
        assertEquals("\u000c", doc.getRange().getParagraph(2).text());
        assertEquals("This is a new section.\r",
                doc.getRange().getParagraph(3).text());
        // Section / line?
        assertEquals("\u000c", doc.getRange().getParagraph(4).text());
        assertEquals("\r", doc.getRange().getParagraph(5).text());
        doc.close();
    }

    /**
     * Another word document with sections, this time with a few more section
     * properties set on it
     */
    @Test
    void testWord6Sections2() throws IOException {
        HWPFOldDocument doc = openOldSampleFile("Word6_sections2.doc");

        assertEquals(1, doc.getRange().numSections());
        assertEquals(57, doc.getRange().numParagraphs());

        assertEquals("\r", doc.getRange().getParagraph(0).text());
        assertEquals("STATEMENT  OF  INSOLVENCY  PRACTICE  10  (SCOTLAND)\r",
                doc.getRange().getParagraph(1).text());
        doc.close();
    }

    @Test
    void testDefaultCodePageEncoding() throws IOException {
        HWPFOldDocument doc = openOldSampleFile("Bug60942.doc");
        Word6Extractor ex = new Word6Extractor(doc);
        String txt = ex.getText();
        assertContains(txt, "BERTHOD");
        assertContains(txt, "APPLICOLOR");
        assertContains(txt, "les meilleurs");
        assertContains(txt, "GUY LECOLE");
        ex.close();
        doc.close();
    }


    @Test
    void testCodePageBug50955() throws IOException {
        //windows 1251
        HWPFOldDocument doc = openOldSampleFile("Bug50955.doc");
        Word6Extractor ex = new Word6Extractor(doc);

        StringBuilder sb = new StringBuilder();
        for (String p : ex.getParagraphText()) {
            sb.append(p);
        }
        assertContains(sb.toString(), "\u043F\u0440\u0438\u0432\u0435\u0442");//Greetings!
        ex.close();
        doc.close();
    }

    @Test
    void testCodePageBug60936() throws IOException {
        //windows 1250 -- this test file was generated with OpenOffice
        //see https://bz.apache.org/ooo/show_bug.cgi?id=12445 for the inspiration


        HWPFOldDocument doc = openOldSampleFile("Bug60936.doc");
        Word6Extractor ex = new Word6Extractor(doc);
        StringBuilder sb = new StringBuilder();
        for (String p : ex.getParagraphText()) {
            sb.append(p);
        }
        assertContains(sb.toString(), "4 sk\u00f3re a p\u0159ed 7 lety");//Greetings!
        ex.close();
        doc.close();
    }

    @Test
    void testOldFontTableEncoding() throws IOException {
        HWPFOldDocument doc = openOldSampleFile("Bug51944.doc");
        OldFontTable oldFontTable = doc.getOldFontTable();
        assertEquals(5, oldFontTable.getFontNames().length);
        assertEquals("\u7D30\u660E\u9AD4", oldFontTable.getFontNames()[0].getMainFontName());
        assertEquals(FontCharset.CHINESEBIG5.getCharset(), Charset.forName("Big5"));
        assertEquals("Times New Roman", oldFontTable.getFontNames()[1].getMainFontName());
        doc.close();

    }

    @Test
    void testOldFontTableAltName() {
        HWPFOldDocument doc  = openOldSampleFile("Bug60942b.doc");
        OldFontTable oldFontTable = doc.getOldFontTable();
        assertEquals(5, oldFontTable.getFontNames().length);
        assertEquals("Roboto", oldFontTable.getFontNames()[3].getMainFontName());
        assertEquals("arial", oldFontTable.getFontNames()[3].getAltFontName());
        assertEquals("Roboto", oldFontTable.getFontNames()[4].getMainFontName());
        assertEquals("arial", oldFontTable.getFontNames()[4].getAltFontName());
    }


    @Test
    void test51944() throws IOException {
        HWPFOldDocument doc = openOldSampleFile("Bug51944.doc");
        Word6Extractor ex = new Word6Extractor(doc);
        StringBuilder sb = new StringBuilder();
        for (String p : ex.getParagraphText()) {
            sb.append(p.replaceAll("[\r\n]+", "\n"));
        }
        String txt = sb.toString();
        assertContains(txt, "Post and Fax");
        assertContains(txt, "also maintain");//this is at a critical juncture
        assertContains(txt, "which are available for");//this too

        /*
            The bytes for the following test:
            170 : 78 : x
            171 : 0 :
            172 : d : <r>
            173 : 35 : 5
            174 : 39 : 9
            175 : 0 :
            176 : 2d : -
            177 : 0 :
            178 : 35 : 5
            179 : 0 :
            180 : 35 : 5

            Note that we are skipping over the value "5" at offset 173.
            This is an apparently invalid sequence in MS's encoding scheme

            When I open the document in MSWord, I also see "\r9-55"
        */
        assertContains(txt, "\n9-55 xxxxx block5");
        //TODO: figure out why these two aren't passing
        //assertContains(txt, "\u2019\u0078 block2");//make sure smart quote is extracted correctly
        //assertContains(txt, "We are able to");//not sure if we can get this easily?
        ex.close();
        doc.close();
    }

}
