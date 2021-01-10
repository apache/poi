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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFPropertiesOnlyDocument;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that POIDocument correctly loads and saves the common
 *  (hspf) Document Properties.
 *
 * This is part 1 of 2 of the tests - it only does the POIDocuments
 *  which are part of the Main (not scratchpad)
 */
final class TestPOIDocumentMain {
    // The POI Documents to work on
    private POIDocument doc;
    private POIDocument doc2;

    /**
     * Set things up, two spreadsheets for our testing
     */
    @BeforeEach
    void setUp() {
        doc = HSSFTestDataSamples.openSampleWorkbook("DateFormats.xls");
        doc2 = HSSFTestDataSamples.openSampleWorkbook("StringFormulas.xls");
    }

    @Test
    void readProperties() {
        readPropertiesHelper(doc);
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
    void readProperties2() {
        // Check again on the word one
        assertNotNull(doc2.getDocumentSummaryInformation());
        assertNotNull(doc2.getSummaryInformation());

        assertEquals("Avik Sengupta", doc2.getSummaryInformation().getAuthor());
        assertNull(doc2.getSummaryInformation().getKeywords());
        assertEquals(0, doc2.getDocumentSummaryInformation().getByteCount());
    }

    @Test
    void writeProperties() throws IOException {
        // Just check we can write them back out into a filesystem
        POIFSFileSystem outFS = new POIFSFileSystem();
        doc.readProperties();
        doc.writeProperties(outFS);

        // Should now hold them
        assertNotNull(
                outFS.createDocumentInputStream(SummaryInformation.DEFAULT_STREAM_NAME)
        );
        assertNotNull(
                outFS.createDocumentInputStream(DocumentSummaryInformation.DEFAULT_STREAM_NAME)
        );
    }

    @Test
    void WriteReadProperties() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Write them out
        POIFSFileSystem outFS = new POIFSFileSystem();
        doc.readProperties();
        doc.writeProperties(outFS);
        outFS.writeFilesystem(baos);

        // Create a new version
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        POIFSFileSystem inFS = new POIFSFileSystem(bais);

        // Check they're still there
        POIDocument doc3 = new HPSFPropertiesOnlyDocument(inFS);
        doc3.readProperties();

        // Delegate test
        readPropertiesHelper(doc3);
        doc3.close();
    }

    @Test
    void createNewProperties() throws IOException {
        POIDocument doc = new HSSFWorkbook();

        // New document won't have them
        assertNull(doc.getSummaryInformation());
        assertNull(doc.getDocumentSummaryInformation());

        // Add them in
        doc.createInformationProperties();
        assertNotNull(doc.getSummaryInformation());
        assertNotNull(doc.getDocumentSummaryInformation());

        // Write out and back in again, no change
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.write(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        doc.close();

        doc = new HSSFWorkbook(bais);

        assertNotNull(doc.getSummaryInformation());
        assertNotNull(doc.getDocumentSummaryInformation());

        doc.close();
    }

    @Test
    void createNewPropertiesOnExistingFile() throws IOException {
        POIDocument doc = new HSSFWorkbook();

        // New document won't have them
        assertNull(doc.getSummaryInformation());
        assertNull(doc.getDocumentSummaryInformation());

        // Write out and back in again, no change
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.write(baos);

        doc.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        doc = new HSSFWorkbook(bais);

        assertNull(doc.getSummaryInformation());
        assertNull(doc.getDocumentSummaryInformation());

        // Create, and change
        doc.createInformationProperties();
        doc.getSummaryInformation().setAuthor("POI Testing");
        doc.getDocumentSummaryInformation().setCompany("ASF");

        // Save and re-load
        baos = new ByteArrayOutputStream();
        doc.write(baos);

        doc.close();

        bais = new ByteArrayInputStream(baos.toByteArray());
        doc = new HSSFWorkbook(bais);

        // Check
        assertNotNull(doc.getSummaryInformation());
        assertNotNull(doc.getDocumentSummaryInformation());
        assertEquals("POI Testing", doc.getSummaryInformation().getAuthor());
        assertEquals("ASF", doc.getDocumentSummaryInformation().getCompany());

        // Asking to re-create will make no difference now
        doc.createInformationProperties();
        assertNotNull(doc.getSummaryInformation());
        assertNotNull(doc.getDocumentSummaryInformation());
        assertEquals("POI Testing", doc.getSummaryInformation().getAuthor());
        assertEquals("ASF", doc.getDocumentSummaryInformation().getCompany());

        doc.close();
    }
}
