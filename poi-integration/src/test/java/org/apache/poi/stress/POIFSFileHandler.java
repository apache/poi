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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.POIDocument;
import org.apache.poi.hpsf.extractor.HPSFPropertiesExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.Test;

public class POIFSFileHandler extends AbstractFileHandler {

    @Override
    public void handleFile(InputStream stream, String path) throws Exception {
        try (POIFSFileSystem fs = new POIFSFileSystem(stream)) {
            handlePOIFSFileSystem(fs);
            handleHPSFProperties(fs);
        }
    }

    private void handleHPSFProperties(POIFSFileSystem fs) throws IOException {
        try (HPSFPropertiesExtractor ext = new HPSFPropertiesExtractor(fs)) {
            // can be null
            ext.getDocSummaryInformation();
            ext.getSummaryInformation();

            assertNotNull(ext.getDocumentSummaryInformationText());
            assertNotNull(ext.getSummaryInformationText());
            assertNotNull(ext.getText());
        }
    }

    private void handlePOIFSFileSystem(POIFSFileSystem fs) {
        assertNotNull(fs);
        assertNotNull(fs.getRoot());
    }

    protected void handlePOIDocument(POIDocument doc) throws Exception {
        try (UnsynchronizedByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream()) {
            doc.write(out);

            try (InputStream in = out.toInputStream();
                POIFSFileSystem fs = new POIFSFileSystem(in)) {
                handlePOIFSFileSystem(fs);
            }
        }
    }

    // a test-case to test this locally without executing the full TestAllFiles
    @Test
    void test() throws Exception {
        File file = new File("test-data/poifs/Notes.ole2");

        try (InputStream stream = new FileInputStream(file)) {
            handleFile(stream, file.getPath());
        }

        //handleExtracting(file);
    }
}
