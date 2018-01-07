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

import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.hwpf.model.PAPX;
import org.apache.poi.hwpf.model.StyleSheet;

/**
 * Tests to ensure that our ranges end up with
 *  the right text in them, and the right font/styling
 *  properties applied to them.
 */
public final class TestRangeProperties extends TestCase {
    private static final char page_break = (char)12;

    private static final String u_page_1 =
        "This is a fairly simple word document, over two pages, with headers and footers.\r" +
        "The trick with this one is that it contains some Unicode based strings in it.\r" +
        "Firstly, some currency symbols:\r" +
        "\tGBP - \u00a3\r" +
        "\tEUR - \u20ac\r" +
        "Now, we\u2019ll have some French text, in bold and big:\r" +
        "\tMoli\u00e8re\r" +
        "And some normal French text:\r" +
        "\tL'Avare ou l'\u00c9cole du mensonge\r" +
        "That\u2019s it for page one\r"
    ;
    private static final String u_page_2 =
        "This is page two. Les Pr\u00e9cieuses ridicules. The end.\r"
    ;

    private static final String a_page_1 =
        "I am a test document\r" +
        "This is page 1\r" +
        "I am Calibri (Body) in font size 11\r"
    ;
    private static final String a_page_2 =
        "This is page two\r" +
        "It\u2019s Arial Black in 16 point\r" +
        "It\u2019s also in blue\r"
    ;

    private HWPFDocument u;
    private HWPFDocument a;

    @Override
    protected void setUp() {
        u = HWPFTestDataSamples.openSampleFile("HeaderFooterUnicode.doc");
        a = HWPFTestDataSamples.openSampleFile("SampleDoc.doc");
    }


    public void testAsciiTextParagraphs() {
        Range r = a.getRange();
        assertEquals(
                a_page_1 +
                page_break + "\r" +
                a_page_2,
                r.text()
        );

        assertEquals(1, r.numSections());
        assertEquals(1, a.getSectionTable().getSections().size());
        Section s = r.getSection(0);
        assertEquals(
                a_page_1 +
                page_break + "\r" +
                a_page_2,
                s.text()
        );

        // no PAP reconstruction
        // assertEquals( 7, r.numParagraphs() );
        // with PAP reconstructon
        assertEquals( 8, r.numParagraphs() );

        String[] p1_parts = a_page_1.split("\r");
        String[] p2_parts = a_page_2.split("\r");

        // Check paragraph contents
        assertEquals(
                p1_parts[0] + "\r",
                r.getParagraph(0).text()
        );
        assertEquals(
                p1_parts[1] + "\r",
                r.getParagraph(1).text()
        );
        assertEquals(
                p1_parts[2] + "\r",
                r.getParagraph(2).text()
        );

        // no PAPX reconstruction
        // assertEquals( page_break + "\r", r.getParagraph( 3 ).text() );
        // assertEquals( p2_parts[0] + "\r", r.getParagraph( 4 ).text() );
        // assertEquals( p2_parts[1] + "\r", r.getParagraph( 5 ).text() );
        // assertEquals( p2_parts[2] + "\r", r.getParagraph( 6 ).text() );

        // with PAPX reconstruction
        assertEquals( "" + page_break, r.getParagraph( 3 ).text() );
        assertEquals( "\r", r.getParagraph( 4 ).text() );
        assertEquals( p2_parts[0] + "\r", r.getParagraph( 5 ).text() );
        assertEquals( p2_parts[1] + "\r", r.getParagraph( 6 ).text() );
        assertEquals( p2_parts[2] + "\r", r.getParagraph( 7 ).text() );
    }

    public void testAsciiStyling() {
        Range r = a.getRange();

        Paragraph p1 = r.getParagraph(0);
        Paragraph p7 = r.getParagraph(6);

        assertEquals(1, p1.numCharacterRuns());
        assertEquals(1, p7.numCharacterRuns());

        CharacterRun c1 = p1.getCharacterRun(0);
        CharacterRun c7 = p7.getCharacterRun(0);

        assertEquals("Times New Roman", c1.getFontName()); // No Calibri
        assertEquals("Arial Black", c7.getFontName());
        assertEquals(22, c1.getFontSize());
        assertEquals(32, c7.getFontSize());

        // This document has 15 styles
        assertEquals(15, a.getStyleSheet().numStyles());

        // Ensure none of the paragraphs refer to one that isn't there,
        //  and none of their character runs either
        // Also check all use the default style
        StyleSheet ss = a.getStyleSheet();
        for(int i=0; i<a.getRange().numParagraphs(); i++) {
            Paragraph p = a.getRange().getParagraph(i);
            int styleIndex = p.getStyleIndex();
            assertTrue(styleIndex < 15);
            assertEquals("Normal", ss.getStyleDescription(styleIndex).getName());
        }
        for(int i=0; i<a.getRange().numCharacterRuns(); i++) {
            CharacterRun c = a.getRange().getCharacterRun(i);
            int styleIndex = c.getStyleIndex();
            assertTrue(styleIndex < 15);
            assertEquals("Normal", ss.getStyleDescription(styleIndex).getName());
        }
    }

    /**
     * Tests the raw definitions of the paragraphs of
     *  a unicode document
     */
    public void testUnicodeParagraphDefinitions() {
        Range r = u.getRange();
        String[] p1_parts = u_page_1.split("\r");
        String[] p2_parts = u_page_2.split("\r");

        assertEquals(
                u_page_1 + page_break + "\r" + u_page_2,
                r.text()
        );
        assertEquals(
                408, r.text().length()
        );


        assertEquals(1, r.numSections());
        assertEquals(1, u.getSectionTable().getSections().size());
        Section s = r.getSection(0);
        assertEquals(
                u_page_1 +
                page_break + "\r" +
                u_page_2,
                s.text()
        );
        assertEquals(0, s.getStartOffset());
        assertEquals(408, s.getEndOffset());


        List<PAPX> pDefs = r._paragraphs;
        // no PAPX reconstruction
        // assertEquals(36, pDefs.size());
        // with PAPX reconstruction
        assertEquals( 36, pDefs.size() );

        // Check that the last paragraph ends where it should do
        assertEquals(531, u.getOverallRange().text().length());
        PAPX pLast = pDefs.get(34);
        // assertEquals(530, pLast.getEnd());

        // Only care about the first few really though
        PAPX p0 = pDefs.get(0);
        PAPX p1 = pDefs.get(1);
        PAPX p2 = pDefs.get(2);
        PAPX p3 = pDefs.get(3);
        PAPX p4 = pDefs.get(4);

        // 5 paragraphs should get us to the end of our text
        assertTrue(p0.getStart() < 408);
        assertTrue(p0.getEnd() < 408);
        assertTrue(p1.getStart() < 408);
        assertTrue(p1.getEnd() < 408);
        assertTrue(p2.getStart() < 408);
        assertTrue(p2.getEnd() < 408);
        assertTrue(p3.getStart() < 408);
        assertTrue(p3.getEnd() < 408);
        assertTrue(p4.getStart() < 408);
        assertTrue(p4.getEnd() < 408);

        // Paragraphs should match with lines
        assertEquals(
                0,
                p0.getStart()
        );
        assertEquals(
                p1_parts[0].length() + 1,
                p0.getEnd()
        );

        assertEquals(
                p1_parts[0].length() + 1,
                p1.getStart()
        );
        assertEquals(
                p1_parts[0].length() + 1 +
                p1_parts[1].length() + 1,
                p1.getEnd()
        );

        assertEquals(
                p1_parts[0].length() + 1 +
                p1_parts[1].length() + 1,
                p2.getStart()
        );
        assertEquals(
                p1_parts[0].length() + 1 +
                p1_parts[1].length() + 1 +
                p1_parts[2].length() + 1,
                p2.getEnd()
        );
	}

    /**
     * Tests the paragraph text of a unicode document
     */
    public void testUnicodeTextParagraphs() {
        Range r = u.getRange();
        assertEquals(
                u_page_1 +
                page_break + "\r" +
                u_page_2,
                r.text()
        );

        // without PAPX reconstruction
        // assertEquals( 12, r.numParagraphs() );
        // with PAPX reconstruction
        assertEquals( 13, r.numParagraphs() );
        String[] p1_parts = u_page_1.split("\r");
        String[] p2_parts = u_page_2.split("\r");

        // Check text all matches up properly
        assertEquals(p1_parts[0] + "\r", r.getParagraph(0).text());
        assertEquals(p1_parts[1] + "\r", r.getParagraph(1).text());
        assertEquals(p1_parts[2] + "\r", r.getParagraph(2).text());
        assertEquals(p1_parts[3] + "\r", r.getParagraph(3).text());
        assertEquals(p1_parts[4] + "\r", r.getParagraph(4).text());
        assertEquals(p1_parts[5] + "\r", r.getParagraph(5).text());
        assertEquals(p1_parts[6] + "\r", r.getParagraph(6).text());
        assertEquals(p1_parts[7] + "\r", r.getParagraph(7).text());
        assertEquals(p1_parts[8] + "\r", r.getParagraph(8).text());
        assertEquals(p1_parts[9] + "\r", r.getParagraph(9).text());
        // without PAPX reconstruction
        // assertEquals(page_break + "\r", r.getParagraph(10).text());
        // assertEquals(p2_parts[0] + "\r", r.getParagraph(11).text());
        // with PAPX reconstruction
        assertEquals( page_break + "", r.getParagraph( 10 ).text() );
        assertEquals( "\r", r.getParagraph( 11 ).text() );
        assertEquals( p2_parts[0] + "\r", r.getParagraph( 12 ).text() );
    }
    public void testUnicodeStyling() {
        Range r = u.getRange();
        String[] p1_parts = u_page_1.split("\r");

        Paragraph p1 = r.getParagraph(0);
        Paragraph p7 = r.getParagraph(6);
        StyleSheet ss = r._doc.getStyleSheet();

        // Line ending in its own run each time!
        assertEquals(2, p1.numCharacterRuns());
        assertEquals(2, p7.numCharacterRuns());

        CharacterRun c1a = p1.getCharacterRun(0);
        CharacterRun c1b = p1.getCharacterRun(1);
        CharacterRun c7a = p7.getCharacterRun(0);
        CharacterRun c7b = p7.getCharacterRun(1);

        assertEquals("Times New Roman", c1a.getFontName()); // No Calibri
        assertEquals(22, c1a.getFontSize());

        assertEquals("Times New Roman", c1b.getFontName()); // No Calibri
        assertEquals(22, c1b.getFontSize());

        assertEquals("Times New Roman", c7a.getFontName());
        assertEquals(48, c7a.getFontSize());

        assertEquals("Times New Roman", c7b.getFontName());
        assertEquals(48, c7b.getFontSize());

        // All use the default base style
        assertEquals("Normal", ss.getStyleDescription(c1a.getStyleIndex()).getName());
        assertEquals("Normal", ss.getStyleDescription(c1b.getStyleIndex()).getName());
        assertEquals("Heading 1", ss.getStyleDescription(c7a.getStyleIndex()).getName());
        assertEquals("Heading 1", ss.getStyleDescription(c7b.getStyleIndex()).getName());

        // Now check where they crop up
        assertEquals(
                0,
                c1a.getStartOffset()
        );
        assertEquals(
                p1_parts[0].length(),
                c1a.getEndOffset()
        );

        assertEquals(
                p1_parts[0].length(),
                c1b.getStartOffset()
        );
        assertEquals(
                p1_parts[0].length()+1,
                c1b.getEndOffset()
        );

        assertEquals(
                p1_parts[0].length() + 1 +
                p1_parts[1].length() + 1 +
                p1_parts[2].length() + 1 +
                p1_parts[3].length() + 1 +
                p1_parts[4].length() + 1 +
                p1_parts[5].length() + 1,
                c7a.getStartOffset()
        );
        assertEquals(
                p1_parts[0].length() + 1 +
                p1_parts[1].length() + 1 +
                p1_parts[2].length() + 1 +
                p1_parts[3].length() + 1 +
                p1_parts[4].length() + 1 +
                p1_parts[5].length() + 1 +
                1,
                c7a.getEndOffset()
        );

        assertEquals(
                p1_parts[0].length() + 1 +
                p1_parts[1].length() + 1 +
                p1_parts[2].length() + 1 +
                p1_parts[3].length() + 1 +
                p1_parts[4].length() + 1 +
                p1_parts[5].length() + 1 +
                1,
                c7b.getStartOffset()
        );
        assertEquals(
                p1_parts[0].length() + 1 +
                p1_parts[1].length() + 1 +
                p1_parts[2].length() + 1 +
                p1_parts[3].length() + 1 +
                p1_parts[4].length() + 1 +
                p1_parts[5].length() + 1 +
                p1_parts[6].length() + 1,
                c7b.getEndOffset()
        );

        // This document also has 22 styles
        assertEquals(22, ss.numStyles());

        // Ensure none of the paragraphs refer to one that isn't there,
        //  and none of their character runs either
        for(int i=0; i<r.numParagraphs(); i++) {
            Paragraph p = r.getParagraph(i);
            int styleIndex = p.getStyleIndex();
            assertTrue(styleIndex < 22);
            assertNotNull(ss.getStyleDescription(styleIndex).getName());
        }
        for(int i=0; i<r.numCharacterRuns(); i++) {
            CharacterRun c = r.getCharacterRun(i);
            int styleIndex = c.getStyleIndex();
            assertTrue(styleIndex < 22);
            assertNotNull(ss.getStyleDescription(styleIndex).getName());
        }
    }
}
