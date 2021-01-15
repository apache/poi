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
package org.apache.poi.stress;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.poi.hwpf.HWPFOldDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.Test;

public class OWPFFileHandler extends POIFSFileHandler {
    @Override
    public void handleFile(InputStream stream, String path) throws Exception {
        try (POIFSFileSystem poifs = new POIFSFileSystem(stream)) {
            HWPFOldDocument doc = new HWPFOldDocument(poifs);
            assertNotNull(doc.getOldFontTable());
            assertNotNull(doc.getCharacterTable());
        }
    }

    // a test-case to test this locally without executing the full TestAllFiles
    @Override
    @Test
    @SuppressWarnings("java:S2699")
    public void test() throws Exception {
        File file = new File("test-data/document/52117.doc");

        try (InputStream stream = new FileInputStream(file)) {
            handleFile(stream, file.getPath());
        }

        handleExtracting(file);

        try (FileInputStream stream = new FileInputStream(file);
             WordExtractor extractor = new WordExtractor(stream)) {
            assertNotNull(extractor.getText());
        }
    }

    @Test
    public void testExtractingOld() {
        File file = new File("test-data/document/52117.doc");
        assertDoesNotThrow(() -> handleExtracting(file));
    }
}
