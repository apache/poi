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

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.hwpf.usermodel.Picture;
import org.junit.Test;

public class HWPFFileHandler extends POIFSFileHandler {
    @Override
    public void handleFile(InputStream stream, String path) throws Exception {
        HWPFDocument doc = new HWPFDocument(stream);
        assertNotNull(doc.getBookmarks());
        assertNotNull(doc.getCharacterTable());
        assertNotNull(doc.getEndnotes());

        PicturesTable picturesTable = doc.getPicturesTable();
        List<Picture> pictures = picturesTable.getAllPictures();
        assertNotNull(pictures);

        handlePOIDocument(doc);
    }

    // a test-case to test this locally without executing the full TestAllFiles
    @Override
    @Test
    public void test() throws Exception {
        File file = new File("test-data/document/52117.doc");

        InputStream stream = new FileInputStream(file);
        try {
            handleFile(stream, file.getPath());
        } finally {
            stream.close();
        }
        
        handleExtracting(file);
        
        stream = new FileInputStream(file);
        try {
            try (WordExtractor extractor = new WordExtractor(stream)) {
                assertNotNull(extractor.getText());
            }
        } finally {
            stream.close();
        }
    }

    @Test
    public void testExtractingOld() throws Exception {
        File file = new File("test-data/document/52117.doc");
        handleExtracting(file);
    }
}
