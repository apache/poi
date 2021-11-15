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

package org.apache.poi.hssf.usermodel;

import static org.apache.poi.hssf.HSSFTestDataSamples.openSampleWorkbook;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link HSSFHeader} / {@link HSSFFooter}
 */
final class TestHSSFHeaderFooter {

    /**
     * Tests that get header retrieves the proper values.
     */
    @Test
    void testRetrieveCorrectHeader() throws IOException {

        try (HSSFWorkbook wb = openSampleWorkbook("EmbeddedChartHeaderTest.xls")) {
            HSSFSheet s = wb.getSheetAt(0);
            HSSFHeader head = s.getHeader();

            assertEquals("Top Left", head.getLeft());
            assertEquals("Top Center", head.getCenter());
            assertEquals("Top Right", head.getRight());
        }
    }

    @Test
    void testSpecialChars() {
        assertEquals("&U", HSSFHeader.startUnderline());
        assertEquals("&U", HSSFHeader.endUnderline());
        assertEquals("&P", HSSFHeader.page());

        assertEquals("&22", HSSFFooter.fontSize((short)22));
        assertEquals("&\"Arial,bold\"", HSSFFooter.font("Arial", "bold"));
    }

    @Test
    void testStripFields() throws IOException {
        String simple = "I am a test header";
        String withPage = "I am a&P test header";
        String withLots = "I&A am&N a&P test&T header&U";
        String withFont = "I&22 am a&\"Arial,bold\" test header";
        String withOtherAnds = "I am a&P test header&&";
        String withOtherAnds2 = "I am a&P test header&a&b";

        assertEquals(simple, HSSFHeader.stripFields(simple));
        assertEquals(simple, HSSFHeader.stripFields(withPage));
        assertEquals(simple, HSSFHeader.stripFields(withLots));
        assertEquals(simple, HSSFHeader.stripFields(withFont));
        assertEquals(simple + "&", HSSFHeader.stripFields(withOtherAnds));
        assertEquals(simple + "&a&b", HSSFHeader.stripFields(withOtherAnds2));

        // Now test the default strip flag
        try (HSSFWorkbook wb = openSampleWorkbook("EmbeddedChartHeaderTest.xls")) {
            HSSFSheet s = wb.getSheetAt(0);
            HSSFHeader head = s.getHeader();

            assertEquals("Top Left", head.getLeft());
            assertEquals("Top Center", head.getCenter());
            assertEquals("Top Right", head.getRight());

            head.setLeft("Top &P&F&D Left");
            assertEquals("Top &P&F&D Left", head.getLeft());

            assertEquals("Top  Left", HeaderFooter.stripFields(head.getLeft()));

            // Now even more complex
            head.setCenter("HEADER TEXT &P&N&D&T&Z&F&F&A&G&X END");
            assertEquals("HEADER TEXT  END", HeaderFooter.stripFields(head.getCenter()));
        }
    }

    /**
     * Tests that get header retrieves the proper values.
     */
    @Test
    void testRetrieveCorrectFooter() throws IOException {
        try (HSSFWorkbook wb = openSampleWorkbook("EmbeddedChartHeaderTest.xls")) {
            HSSFSheet s = wb.getSheetAt(0);
            HSSFFooter foot = s.getFooter();

            assertEquals("Bottom Left", foot.getLeft());
            assertEquals("Bottom Center", foot.getCenter());
            assertEquals("Bottom Right", foot.getRight());
        }
    }

    /**
     * Testcase for Bug 17039 HSSFHeader  does not support DBCS
     */
    @Test
    void testHeaderHas16bitCharacter() throws IOException {
        try (HSSFWorkbook b = new HSSFWorkbook()) {
            HSSFSheet s = b.createSheet("Test");
            HSSFHeader h = s.getHeader();
            h.setLeft("\u0391");
            h.setCenter("\u0392");
            h.setRight("\u0393");

            try (HSSFWorkbook b2 = HSSFTestDataSamples.writeOutAndReadBack(b)) {
                HSSFHeader h2 = b2.getSheet("Test").getHeader();

                assertEquals("\u0391", h2.getLeft());
                assertEquals("\u0392", h2.getCenter());
                assertEquals("\u0393", h2.getRight());
            }
        }
    }

    /**
     * Testcase for Bug 17039 HSSFFooter does not support DBCS
     */
    @Test
     void testFooterHas16bitCharacter() throws IOException {
        try (HSSFWorkbook b = new HSSFWorkbook()) {
            HSSFSheet s = b.createSheet("Test");
            HSSFFooter f = s.getFooter();
            f.setLeft("\u0391");
            f.setCenter("\u0392");
            f.setRight("\u0393");

            try (HSSFWorkbook b2 = HSSFTestDataSamples.writeOutAndReadBack(b)) {
                HSSFFooter f2 = b2.getSheet("Test").getFooter();

                assertEquals("\u0391", f2.getLeft());
                assertEquals("\u0392", f2.getCenter());
                assertEquals("\u0393", f2.getRight());
            }
        }
    }

    @Test
    void testReadDBCSHeaderFooter() throws IOException {
        try (HSSFWorkbook wb = openSampleWorkbook("DBCSHeader.xls")) {
            HSSFSheet s = wb.getSheetAt(0);
            HSSFHeader h = s.getHeader();
            assertEquals("\u090f\u0915", h.getLeft(), "Header Left");
            assertEquals("\u0939\u094b\u0917\u093e", h.getCenter(), "Header Center");
            assertEquals("\u091c\u093e", h.getRight(), "Header Right");

            HSSFFooter f = s.getFooter();
            assertEquals("\u091c\u093e", f.getLeft(), "Footer Left");
            assertEquals("\u091c\u093e", f.getCenter(), "Footer Center");
            assertEquals("\u091c\u093e", f.getRight(), "Footer Right");
        }
    }

    /**
     * Excel tolerates files with missing HEADER/FOOTER records.  POI should do the same.
     */
    @Test
    void testMissingHeaderFooterRecord_bug47244() throws IOException {
        // noHeaderFooter47244.xls was created by a slightly modified POI
        // which omitted the HEADER/FOOTER records
        try (HSSFWorkbook wb = openSampleWorkbook("noHeaderFooter47244.xls")) {
            HSSFSheet sheet = wb.getSheetAt(0);
            // bug 47244a - NullPointerException
            HSSFFooter footer = sheet.getFooter();
            assertEquals("", footer.getRawText());
            HSSFHeader header = sheet.getHeader();
            assertEquals("", header.getRawText());

            // make sure header / footer is properly linked to underlying data
            HSSFHeader header2 = sheet.getHeader();
            header.setCenter("foo");
            assertEquals("foo", header2.getCenter());

            HSSFFooter footer2 = sheet.getFooter();
            footer.setCenter("bar");
            assertEquals("bar", footer2.getCenter());
        }
    }

    @Test
    void testHeaderWithAmpersand() throws IOException {
        try (HSSFWorkbook wb = openSampleWorkbook("AmpersandHeader.xls")) {
            HSSFSheet s = wb.getSheetAt(0);
            HSSFHeader h = s.getHeader();
            String header = h.getCenter();
            assertEquals("one && two &&&&", header);

            // In Excel headers fields start with '&'
            // For '&' to appear as text it needs to be escaped as '&&'
            assertEquals("one & two &&", HSSFHeader.stripFields(header));
        }
    }
}
