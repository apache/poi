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

package org.apache.poi.xssf.extractor;

import static org.apache.poi.POITestCase.assertContains;
import static org.apache.poi.POITestCase.assertStartsWith;
import static org.apache.poi.POITestCase.assertEndsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Tests for {@link XSSFBEventBasedExcelExtractor}
 */
class TestXSSFBEventBasedExcelExtractor {
    protected XSSFEventBasedExcelExtractor getExtractor(String sampleName) throws Exception {
        return new XSSFBEventBasedExcelExtractor(XSSFTestDataSamples.
                openSamplePackage(sampleName));
    }

    /**
     * Get text out of the simple file
     */
    @Test
    void testGetSimpleText() throws Exception {
        // a very simple file
        try (XSSFEventBasedExcelExtractor extractor = getExtractor("sample.xlsb")) {
            extractor.setIncludeCellComments(true);
            extractor.getText();

            String text = extractor.getText();
            assertTrue(text.length() > 0);

            // Check sheet names
            assertStartsWith(text, "Sheet1");
            assertEndsWith(text, "Sheet3\n");

            // Now without, will have text
            extractor.setIncludeSheetNames(false);
            text = extractor.getText();
            String CHUNK1 =
                    "Lorem\t111\n" +
                            "ipsum\t222\n" +
                            "dolor\t333\n" +
                            "sit\t444\n" +
                            "amet\t555\n" +
                            "consectetuer\t666\n" +
                            "adipiscing\t777\n" +
                            "elit\t888\n" +
                            "Nunc\t999\n";
            String CHUNK2 =
                    "The quick brown fox jumps over the lazy dog\n" +
                            "hello, xssf	hello, xssf\n" +
                            "hello, xssf	hello, xssf\n" +
                            "hello, xssf	hello, xssf\n" +
                            "hello, xssf	hello, xssf\n";
            assertEquals(
                    CHUNK1 +
                            "at\t4995\n" +
                            CHUNK2
                    , text);
        }
    }

    /**
     * Test text extraction from text box using getShapes()
     */
    @Test
    void testShapes() throws Exception {
        try (XSSFEventBasedExcelExtractor ooxmlExtractor = getExtractor("WithTextBox.xlsb")) {
            String text = ooxmlExtractor.getText();
            assertContains(text, "Line 1");
            assertContains(text, "Line 2");
            assertContains(text, "Line 3");
        }
    }

    @Test
    void testBeta() throws Exception {
        try (XSSFEventBasedExcelExtractor extractor = getExtractor("Simple.xlsb")) {
            extractor.setIncludeCellComments(true);
            String text = extractor.getText();
            assertContains(text,
                    "This is an example spreadsheet created with Microsoft Excel 2007 Beta 2.");
        }
    }

    @Test
    void test62815() throws Exception {
        //test file based on http://oss.sheetjs.com/test_files/RkNumber.xlsb
        try (XSSFEventBasedExcelExtractor extractor = getExtractor("62815.xlsb")) {
            extractor.setIncludeCellComments(true);
            String[] rows = extractor.getText().split("[\r\n]+");
            assertEquals(283, rows.length);
            try (BufferedReader reader = Files.newBufferedReader(XSSFTestDataSamples.getSampleFile("62815.xlsb.txt").toPath(),
                    StandardCharsets.UTF_8)) {
                String line = reader.readLine();
                for (String row : rows) {
                    assertEquals(line, row);
                    line = reader.readLine();
                    while (line != null && line.startsWith("#")) {
                        line = reader.readLine();
                    }
                }
            }
        }
    }
}
