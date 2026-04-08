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

import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        try (
            InputStream stream = HWPFTestDataSamples.openSampleFileStream("issue_1041.doc");
            HWPFDocument doc = HWPFParser.parse(stream)
        ) {
            WordExtractor extractor = new WordExtractor(doc);
            String text = extractor.getText();
            assertNotNull(doc);
            assertNotNull(text);
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
