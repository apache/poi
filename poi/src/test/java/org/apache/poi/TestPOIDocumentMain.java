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

package org.apache.poi;

import static org.apache.poi.hssf.HSSFTestDataSamples.openSampleWorkbook;
import static org.apache.poi.hssf.HSSFTestDataSamples.writeOutAndReadBack;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFPropertiesOnlyDocument;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.Test;

/**
 * Tests that POIDocument correctly loads and saves the common
 *  (hspf) Document Properties.
 *
 * This is part 1 of 2 of the tests - it only does the POIDocuments
 *  which are part of the Main (not scratchpad)
 */
final class TestPOIDocumentMain {
    @Test
    void readProperties() throws IOException {
        try (POIDocument xls = openSampleWorkbook("DateFormats.xls")) {
            readPropertiesHelper(xls);
        }
    }

    private void readPropertiesHelper(POIDocument docWB) {
        // We should have both sets
        assertNotNull(docWB.getDocumentSummaryInformation());
        assertNotNull(docWB.getSummaryInformation());

        // Check they are as expected for the test doc
        assertEquals("Administrator", docWB.getSummaryInformation().getAuthor());
        assertEquals(0, docWB.getDocumentSummaryInformation().getByteCount());
    }

    @Test
    void readProperties2() throws IOException {
        try (POIDocument xls = openSampleWorkbook("StringFormulas.xls")) {
            // Check again on the word one
            assertNotNull(xls.getDocumentSummaryInformation());
            assertNotNull(xls.getSummaryInformation());

            assertEquals("Avik Sengupta", xls.getSummaryInformation().getAuthor());
            assertNull(xls.getSummaryInformation().getKeywords());
            assertEquals(0, xls.getDocumentSummaryInformation().getByteCount());
        }
    }

    @Test
    void writeProperties() throws IOException {
        // Just check we can write them back out into a filesystem
        try (POIDocument xls = openSampleWorkbook("DateFormats.xls");
             POIFSFileSystem outFS = new POIFSFileSystem()) {
            xls.readProperties();
            xls.writeProperties(outFS);

            // Should now hold them
            assertNotNull(outFS.createDocumentInputStream(SummaryInformation.DEFAULT_STREAM_NAME));
            assertNotNull(outFS.createDocumentInputStream(DocumentSummaryInformation.DEFAULT_STREAM_NAME));
        }
    }

    @Test
    void WriteReadProperties() throws IOException {
        UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();

        // Write them out
        try (POIDocument xls = openSampleWorkbook("DateFormats.xls");
             POIFSFileSystem outFS = new POIFSFileSystem()) {
            xls.readProperties();
            xls.writeProperties(outFS);
            outFS.writeFilesystem(baos);
        }

        // Create a new version
        try (POIFSFileSystem inFS = new POIFSFileSystem(baos.toInputStream());
             POIDocument doc3 = new HPSFPropertiesOnlyDocument(inFS)) {

            // Check they're still there
            doc3.readProperties();

            // Delegate test
            readPropertiesHelper(doc3);
        }
    }

    @Test
    void createNewProperties() throws IOException {
        try (HSSFWorkbook xls1 = new HSSFWorkbook()) {
            // New document won't have them
            assertNull(xls1.getSummaryInformation());
            assertNull(xls1.getDocumentSummaryInformation());

            // Add them in
            xls1.createInformationProperties();
            assertNotNull(xls1.getSummaryInformation());
            assertNotNull(xls1.getDocumentSummaryInformation());

            try (HSSFWorkbook xls2 = writeOutAndReadBack(xls1)) {
                assertNotNull(xls2.getSummaryInformation());
                assertNotNull(xls2.getDocumentSummaryInformation());
            }
        }

    }

    @Test
    void createNewPropertiesOnExistingFile() throws IOException {
        try (HSSFWorkbook xls1 = new HSSFWorkbook()) {
            // New document won't have them
            assertNull(xls1.getSummaryInformation());
            assertNull(xls1.getDocumentSummaryInformation());

            try (HSSFWorkbook xls2 = writeOutAndReadBack(xls1)) {

                assertNull(xls2.getSummaryInformation());
                assertNull(xls2.getDocumentSummaryInformation());

                // Create, and change
                xls2.createInformationProperties();
                xls2.getSummaryInformation().setAuthor("POI Testing");
                xls2.getDocumentSummaryInformation().setCompany("ASF");

                try (HSSFWorkbook xls3 = writeOutAndReadBack(xls2)) {
                    // Check
                    assertNotNull(xls3.getSummaryInformation());
                    assertNotNull(xls3.getDocumentSummaryInformation());
                    assertEquals("POI Testing", xls3.getSummaryInformation().getAuthor());
                    assertEquals("ASF", xls3.getDocumentSummaryInformation().getCompany());

                    // Asking to re-create will make no difference now
                    xls3.createInformationProperties();
                    assertNotNull(xls3.getSummaryInformation());
                    assertNotNull(xls3.getDocumentSummaryInformation());
                    assertEquals("POI Testing", xls3.getSummaryInformation().getAuthor());
                    assertEquals("ASF", xls3.getDocumentSummaryInformation().getCompany());
                }
            }
        }
    }
}
