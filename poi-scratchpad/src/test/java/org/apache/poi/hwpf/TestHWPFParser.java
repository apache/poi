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

package org.apache.poi.hwpf;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class TestHWPFParser {
    @Test
    void testDoc() throws Exception {
        try (
            InputStream stream = HWPFTestDataSamples.openSampleFileStream("Lists.doc");
            HWPFDocument doc = HWPFParser.parse(stream)
        ) {
            assertNotNull(doc);
            assertEquals(40, doc.getParagraphTable().getParagraphs().size());
        }
    }

    /**
     * Test reading a real-world .doc file.
     * This test now handles non-standard formatting that WPS/Word can open.
     */
    @Test
    void testDocRead() throws Exception {
        // Enable tolerant mode for corrupt blocks
        System.setProperty("org.apache.poi.poifs.allowCorruptBlocks", "true");
        try {
            try (
                InputStream stream = HWPFTestDataSamples.openSampleFileStream("issue_1041.doc");
                HWPFDocument doc = HWPFParser.parse(stream)
            ) {
                assertNotNull(doc);
                WordExtractor extractor = new WordExtractor(doc);
                String text = extractor.getText();
                
                // Verify actual text content, not just non-null
                assertNotNull(text, "Extracted text should not be null");
                assertFalse(text.isEmpty(), "Extracted text should not be empty");
                assertFalse(text.trim().isEmpty(), "Extracted text should not be blank");
            }
        } finally {
            // Reset to default strict mode
            System.clearProperty("org.apache.poi.poifs.allowCorruptBlocks");
        }
    }

    /**
     * Test that by default (strict mode), reading corrupt files throws an exception.
     */
    @Test
    void testDocReadStrictMode() throws Exception {
        // Ensure strict mode is enabled (default behavior)
        System.clearProperty("org.apache.poi.poifs.allowCorruptBlocks");
        
        // Should throw HWPFReadException (wrapping IndexOutOfBoundsException) in strict mode
        HWPFReadException exception = assertThrows(HWPFReadException.class, () -> {
            try (
                InputStream stream = HWPFTestDataSamples.openSampleFileStream("issue_1041.doc");
                HWPFDocument doc = HWPFParser.parse(stream)
            ) {
                // This should not succeed in strict mode
            }
        });
        
        // Verify the root cause is IndexOutOfBoundsException about corrupt blocks
        Throwable rootCause = getRootCause(exception);
        assertInstanceOf(IndexOutOfBoundsException.class, rootCause, "Expected root cause to be IndexOutOfBoundsException, but was: " + rootCause.getClass().getName());
        assertTrue(rootCause.getMessage().contains("beyond EOF"),
                   "Expected exception message to contain 'beyond EOF', but got: " + rootCause.getMessage());
    }
    
    /**
     * Helper method to get the root cause of an exception chain.
     * Limits traversal depth to prevent infinite loops in case of circular references.
     */
    private static Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        int depth = 0;
        final int MAX_DEPTH = 20; // Reasonable limit for exception chains
        
        while (cause.getCause() != null && cause.getCause() != cause && depth < MAX_DEPTH) {
            cause = cause.getCause();
            depth++;
        }
        return cause;
    }

    @Test
    void testWpsDocByFs()throws Exception{
        // Enable tolerant mode for corrupt blocks
        System.setProperty("org.apache.poi.poifs.allowCorruptBlocks", "true");
        try {
            POIDataSamples instance = POIDataSamples.getDocumentInstance();
            File file = instance.getFile("issue_1041.doc");
            POIFSFileSystem fs = new POIFSFileSystem(file);
            WordExtractor extractor = new WordExtractor(fs);
            String text = extractor.getText();
            
            // Verify actual text content, not just non-null
            assertNotNull(text, "Extracted text should not be null");
            assertFalse(text.isEmpty(), "Extracted text should not be empty");
            assertFalse(text.trim().isEmpty(), "Extracted text should not be blank");
        } finally {
            // Reset to default strict mode
            System.clearProperty("org.apache.poi.poifs.allowCorruptBlocks");
        }
    }

    @Test
    void testOffice97_2003DocRead() throws Exception {
        try (
            InputStream stream = HWPFTestDataSamples.openSampleFileStream("issue_1041_2.doc");
            HWPFDocument doc = HWPFParser.parse(stream)
        ) {
            assertNotNull(doc);
            WordExtractor extractor = new WordExtractor(doc);
            String text = extractor.getText();
            
            // Verify actual text content, not just non-null
            assertNotNull(text, "Extracted text should not be null");
            assertFalse(text.isEmpty(), "Extracted text should not be empty");
            assertFalse(text.trim().isEmpty(), "Extracted text should not be blank");
        }
    }


    @Test
    void testFailOnDocx() throws Exception {
        try (InputStream stream = HWPFTestDataSamples.openSampleFileStream("sample.docx")) {
            HWPFReadException hre = assertThrows(HWPFReadException.class, () -> HWPFParser.parse(stream));
            assertInstanceOf(OfficeXmlFileException.class, hre.getCause());
        }
    }
}
