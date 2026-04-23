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

package org.apache.poi.extractor;

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MainExtractorFactory} password support.
 */
class TestMainExtractorFactory {

    private static final POIDataSamples SS_SAMPLES = POIDataSamples.getSpreadSheetInstance();

    private static final String PASSWORD_XLS = "password.xls";
    private static final String PASSWORD = "password";

    @Test
    void testCreateFromFileWithPassword() throws IOException {
        File file = SS_SAMPLES.getFile(PASSWORD_XLS);
        MainExtractorFactory factory = new MainExtractorFactory();
        try (POITextExtractor extractor = factory.create(file, PASSWORD)) {
            assertNotNull(extractor);
            assertContains(extractor.getText(), "ZIP");
        }
    }

    @Test
    void testCreateFromInputStreamWithPassword() throws IOException {
        MainExtractorFactory factory = new MainExtractorFactory();
        try (InputStream is = SS_SAMPLES.openResourceAsStream(PASSWORD_XLS);
             POITextExtractor extractor = factory.create(is, PASSWORD)) {
            assertNotNull(extractor);
            assertContains(extractor.getText(), "ZIP");
        }
    }

    @Test
    void testCreateFromDirectoryWithPassword() throws IOException {
        File file = SS_SAMPLES.getFile(PASSWORD_XLS);
        MainExtractorFactory factory = new MainExtractorFactory();
        try (POIFSFileSystem fs = new POIFSFileSystem(file, true);
             POITextExtractor extractor = factory.create(fs.getRoot(), PASSWORD)) {
            assertNotNull(extractor);
            assertContains(extractor.getText(), "ZIP");
        }
    }

    @Test
    void testCreateWithNullPasswordUsesNoPassword() throws IOException {
        // A non-encrypted file should be readable with null password
        File file = SS_SAMPLES.getFile("Simple.xls");
        MainExtractorFactory factory = new MainExtractorFactory();
        try (POITextExtractor extractor = factory.create(file, null)) {
            assertNotNull(extractor);
            assertContains(extractor.getText(), "Sheet1");
        }
    }

    @Test
    void testBiff8EncryptionKeyCurrentUserPassword() {
        // Verify the ThreadLocal get/set/clear behaviour
        assertNull(Biff8EncryptionKey.getCurrentUserPassword());

        Biff8EncryptionKey.setCurrentUserPassword("test");
        assertNotNull(Biff8EncryptionKey.getCurrentUserPassword());
        assertContains(Biff8EncryptionKey.getCurrentUserPassword(), "test");

        // Clearing with null
        Biff8EncryptionKey.setCurrentUserPassword(null);
        assertNull(Biff8EncryptionKey.getCurrentUserPassword());
    }

    @Test
    void testCreateEventBasedExtractorWithPassword() throws IOException {
        ExtractorFactory.setThreadPrefersEventExtractors(true);
        try {
            File file = SS_SAMPLES.getFile(PASSWORD_XLS);
            MainExtractorFactory factory = new MainExtractorFactory();
            try (POITextExtractor extractor = factory.create(file, PASSWORD)) {
                assertNotNull(extractor);
                assertContains(extractor.getText(), "ZIP");
            }
        } finally {
            ExtractorFactory.removeThreadPrefersEventExtractorsSetting();
        }
    }

    @Test
    void testCreateEventBasedExtractorFromInputStreamWithPassword() throws IOException {
        ExtractorFactory.setThreadPrefersEventExtractors(true);
        try {
            MainExtractorFactory factory = new MainExtractorFactory();
            try (InputStream is = SS_SAMPLES.openResourceAsStream(PASSWORD_XLS);
                 POITextExtractor extractor = factory.create(is, PASSWORD)) {
                assertNotNull(extractor);
                assertContains(extractor.getText(), "ZIP");
            }
        } finally {
            ExtractorFactory.removeThreadPrefersEventExtractorsSetting();
        }
    }
}
