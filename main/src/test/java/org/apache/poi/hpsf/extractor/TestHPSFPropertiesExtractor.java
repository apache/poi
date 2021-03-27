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

package org.apache.poi.hpsf.extractor;

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hpsf.Thumbnail;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.Test;

final class TestHPSFPropertiesExtractor {
    private static final POIDataSamples _samples = POIDataSamples.getHPSFInstance();

    @Test
    void testNormalProperties() throws Exception {
        try (InputStream is = _samples.openResourceAsStream("TestMickey.doc");
             POIFSFileSystem fs = new POIFSFileSystem(is);
             HPSFPropertiesExtractor ext = new HPSFPropertiesExtractor(fs)) {
            // Check each bit in turn
            String summary = ext.getSummaryInformationText();
            String docSummary = ext.getDocumentSummaryInformationText();

            assertContains(summary, "TEMPLATE = Normal");
            assertContains(summary, "SUBJECT = sample subject");
            assertContains(docSummary, "MANAGER = sample manager");
            assertContains(docSummary, "COMPANY = sample company");

            // Now overall
            String text = ext.getText();
            assertContains(text, "TEMPLATE = Normal");
            assertContains(text, "SUBJECT = sample subject");
            assertContains(text, "MANAGER = sample manager");
            assertContains(text, "COMPANY = sample company");
        }
    }

    @Test
    void testNormalUnicodeProperties() throws Exception {

        try (InputStream is = _samples.openResourceAsStream("TestUnicode.xls");
             POIFSFileSystem fs = new POIFSFileSystem(is);
             HPSFPropertiesExtractor ext = new HPSFPropertiesExtractor(fs)) {
            // Check each bit in turn
            String summary = ext.getSummaryInformationText();
            String docSummary = ext.getDocumentSummaryInformationText();

            assertContains(summary, "AUTHOR = marshall");
            assertContains(summary, "TITLE = Titel: \u00c4h");
            assertContains(docSummary, "COMPANY = Schreiner");
            assertContains(docSummary, "SCALE = false");

            // Now overall
            String text = ext.getText();
            assertContains(text, "AUTHOR = marshall");
            assertContains(text, "TITLE = Titel: \u00c4h");
            assertContains(text, "COMPANY = Schreiner");
            assertContains(text, "SCALE = false");
        }
    }

    @Test
    void testCustomProperties() throws Exception {
        try (InputStream is = _samples.openResourceAsStream("TestMickey.doc");
             POIFSFileSystem fs = new POIFSFileSystem(is);
             HPSFPropertiesExtractor ext = new HPSFPropertiesExtractor(fs)) {

            // Custom properties are part of the document info stream
            String dinfText = ext.getDocumentSummaryInformationText();
            assertContains(dinfText, "Client = sample client");
            assertContains(dinfText, "Division = sample division");

            String text = ext.getText();
            assertContains(text, "Client = sample client");
            assertContains(text, "Division = sample division");
        }
    }

    @Test
    void testConstructors() throws IOException {
        final String fsText;
        final String hwText;
        final String eeText;

        try (POIFSFileSystem fs = new POIFSFileSystem(_samples.openResourceAsStream("TestUnicode.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        ExcelExtractor excelExt = new ExcelExtractor(wb)) {

            try (HPSFPropertiesExtractor fsExt = new HPSFPropertiesExtractor(fs)) {
                // Don't close re-used test resources!
                fsExt.setCloseFilesystem(false);
                fsText = fsExt.getText();
            }

            try (HPSFPropertiesExtractor hwExt = new HPSFPropertiesExtractor(wb)) {
                // Don't close re-used test resources!
                hwExt.setCloseFilesystem(false);
                hwText = hwExt.getText();
            }

            try (HPSFPropertiesExtractor eeExt = new HPSFPropertiesExtractor(excelExt)) {
                // Don't close re-used test resources!
                eeExt.setCloseFilesystem(false);
                eeText = eeExt.getText();
            }
        }

        assertEquals(fsText, hwText);
        assertEquals(fsText, eeText);

        assertContains(fsText, "AUTHOR = marshall");
        assertContains(fsText, "TITLE = Titel: \u00c4h");
    }

    @Test
    void test42726() throws IOException {
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("42726.xls");
             HPSFPropertiesExtractor ext = new HPSFPropertiesExtractor(wb)) {
            String txt = ext.getText();
            assertContains(txt, "PID_AUTHOR");
            assertContains(txt, "PID_EDITTIME");
            assertContains(txt, "PID_REVNUMBER");
            assertContains(txt, "PID_THUMBNAIL");
        }
    }

    @Test
    void testThumbnail() throws Exception {
        POIFSFileSystem fs = new POIFSFileSystem(_samples.openResourceAsStream("TestThumbnail.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        Thumbnail thumbnail = new Thumbnail(wb.getSummaryInformation().getThumbnail());
        assertEquals(-1, thumbnail.getClipboardFormatTag());
        assertEquals(3, thumbnail.getClipboardFormat());
        assertNotNull(thumbnail.getThumbnailAsWMF());
        wb.close();
    }

    @Test
    void test52258() throws Exception {
        try (InputStream is = _samples.openResourceAsStream("TestVisioWithCodepage.vsd");
             POIFSFileSystem fs = new POIFSFileSystem(is);
             HPSFPropertiesExtractor ext = new HPSFPropertiesExtractor(fs)) {
            assertNotNull(ext.getDocSummaryInformation());
            assertNotNull(ext.getDocumentSummaryInformationText());
            assertNotNull(ext.getSummaryInformation());
            assertNotNull(ext.getSummaryInformationText());
            assertNotNull(ext.getText());
        }
    }

    @Test
    void test61300Extractor() throws IOException {
        try (POIFSFileSystem poifs = new POIFSFileSystem(
                POIDataSamples.getPOIFSInstance().getFile("61300.bin"))) {
            HPSFPropertiesExtractor ext = new HPSFPropertiesExtractor(poifs);
            assertContains(ext.getText(), "PID_CODEPAGE = 1252");
        }
    }
}
