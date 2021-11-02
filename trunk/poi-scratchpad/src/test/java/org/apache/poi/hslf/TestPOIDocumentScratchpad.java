
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



package org.apache.poi.hslf;


import static org.apache.poi.POIDataSamples.writeOutAndReadBack;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.apache.poi.POIDocument;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFPropertiesOnlyDocument;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.Test;

/**
 * Tests that POIDocument correctly loads and saves the common
 *  (hspf) Document Properties
 *
 * This is part 2 of 2 of the tests - it only does the POIDocuments
 *  which are part of the scratchpad (not main)
 */
public final class TestPOIDocumentScratchpad {
    @Test
    void testReadProperties() throws IOException {
        try (POIDocument doc = HSLFTestDataSamples.getSlideShow("basic_test_ppt_file.ppt")) {
            testReadPropertiesHelper(doc);
        }
    }

    private void testReadPropertiesHelper(POIDocument docPH) {
        // We should have both sets
        assertNotNull(docPH.getDocumentSummaryInformation());
        assertNotNull(docPH.getSummaryInformation());

        // Check they are as expected for the test doc
        assertEquals("Hogwarts", docPH.getSummaryInformation().getAuthor());
        assertEquals(10598, docPH.getDocumentSummaryInformation().getByteCount());
    }

    @Test
    void testReadProperties2() throws IOException {
        try (POIDocument doc2 = HWPFTestDataSamples.openSampleFile("test2.doc")) {
            // Check again on the word one
            assertNotNull(doc2.getDocumentSummaryInformation());
            assertNotNull(doc2.getSummaryInformation());

            assertEquals("Hogwarts", doc2.getSummaryInformation().getAuthor());
            assertEquals("", doc2.getSummaryInformation().getKeywords());
            assertEquals(0, doc2.getDocumentSummaryInformation().getByteCount());
        }
    }

    @Test
    void testWriteProperties() throws IOException {
        // Just check we can write them back out into a filesystem
        try (POIDocument doc = HSLFTestDataSamples.getSlideShow("basic_test_ppt_file.ppt");
             POIFSFileSystem outFS = new POIFSFileSystem()) {
            doc.writeProperties(outFS);

            // Should now hold them
            assertNotNull(outFS.createDocumentInputStream(SummaryInformation.DEFAULT_STREAM_NAME));
            assertNotNull(outFS.createDocumentInputStream(DocumentSummaryInformation.DEFAULT_STREAM_NAME));
        }
    }

    @Test
    void testWriteReadProperties() throws IOException {
        // Write them out
        try (POIDocument doc = HSLFTestDataSamples.getSlideShow("basic_test_ppt_file.ppt");
            POIFSFileSystem outFS = new POIFSFileSystem()) {
            doc.writeProperties(outFS);

            // Check they're still there
            try (POIFSFileSystem inFS = writeOutAndReadBack(outFS);
                 POIDocument ppt = new HPSFPropertiesOnlyDocument(inFS)) {
                ppt.readProperties();

                // Delegate test
                testReadPropertiesHelper(ppt);
            }
        }
    }
}
