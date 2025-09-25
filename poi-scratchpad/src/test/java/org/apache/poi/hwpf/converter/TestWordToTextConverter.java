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
package org.apache.poi.hwpf.converter;

import static org.apache.poi.hwpf.HWPFTestDataSamples.openSampleFile;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.OldWordFileFormatException;
import org.apache.poi.logging.PoiLogManager;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TestWordToTextConverter {
    private static final Logger LOG = PoiLogManager.getLogger(TestWordToTextConverter.class);

    private static final List<String> failingFiles = Arrays.asList(
        // Excel file
        "TestRobert_Flaherty.doc",
        // Corrupt files
        "clusterfuzz-testcase-minimized-POIHWPFFuzzer-5418937293340672.doc",
        "TestHPSFWritingFunctionality.doc",
        "clusterfuzz-testcase-minimized-POIHWPFFuzzer-4947285593948160.doc",
        "clusterfuzz-testcase-minimized-POIHWPFFuzzer-5440721166139392.doc",
        "clusterfuzz-testcase-minimized-POIHWPFFuzzer-5050208641482752.doc",
        "clusterfuzz-testcase-minimized-POIHWPFFuzzer-6610789829836800.doc"
    );

    /**
     * [FAILING] Bug 47731 - Word Extractor considers text copied from some
     * website as an embedded object
     */
    @Test
    void testBug47731() throws Exception {
        try (HWPFDocument doc = openSampleFile( "Bug47731.doc" )) {
            String foundText = WordToTextConverter.getText(doc);

            assertTrue(foundText.contains("Soak the rice in water for three to four hours"));
        }
    }

    @Test
    void testBug52311() throws Exception {
        try (HWPFDocument doc = openSampleFile( "Bug52311.doc" )) {
            String result = WordToTextConverter.getText(doc);

            assertTrue(result.contains("2.1\tHeader 2.1"));
            assertTrue(result.contains("2.2\tHeader 2.2"));
            assertTrue(result.contains("2.3\tHeader 2.3"));
            assertTrue(result.contains("2.3.1\tHeader 2.3.1"));
            assertTrue(result.contains("2.99\tHeader 2.99"));
            assertTrue(result.contains("2.99.1\tHeader 2.99.1"));
            assertTrue(result.contains("2.100\tHeader 2.100"));
            assertTrue(result.contains("2.101\tHeader 2.101"));
        }
    }

    @Test
    void testBug53380_3() throws Exception {
        try (HWPFDocument doc = openSampleFile( "Bug53380_3.doc" )) {
            assertNotNull(WordToTextConverter.getText(doc));
        }
    }

    @ParameterizedTest
    @MethodSource("files")
    void testAllFiles(File file) throws Exception {
        LOG.info("Testing {}", file);
        try (FileInputStream stream = new FileInputStream(file)) {
            InputStream is = FileMagic.prepareToCheckMagic(stream);
            FileMagic fm = FileMagic.valueOf(is);

            if (fm != FileMagic.OLE2) {
                LOG.info("Skip non-doc file {}", file);

                return;
            }

            try (HWPFDocument doc = new HWPFDocument(is)) {
                String foundText = WordToTextConverter.getText(doc);
                assertNotNull(foundText);
            } catch (OldWordFileFormatException | EncryptedDocumentException | RecordFormatException e) {
                // ignored here
            }
        }
    }

    public static Stream<Arguments> files() {
        return Stream.concat(
            Arrays.stream(getFiles(POIDataSamples.getDocumentInstance().getFile(""))),
            Arrays.stream(getFiles(POIDataSamples.getHPSFInstance().getFile("")))
        ).map(Arguments::of);
    }

    private static File[] getFiles(File directory) {
        FilenameFilter ff = (dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".doc") && !failingFiles.contains(name);
        File[] docs = directory.listFiles(ff);
        assertNotNull(docs);
        return docs;
    }
}
