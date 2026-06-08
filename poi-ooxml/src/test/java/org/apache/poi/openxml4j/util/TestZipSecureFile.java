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
package org.apache.poi.openxml4j.util;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class TestZipSecureFile {
    @Test
    void testThresholdInputStream() throws Exception {
        // This fails in Java 10 because our reflection injection of the ThresholdInputStream causes a
        // ClassCastException in ZipFile now
        // The relevant change in the JDK is http://hg.openjdk.java.net/jdk/jdk10/rev/85ea7e83af30#l5.66

        try (ZipFile thresholdInputStream =
                 ZipFile.builder().setFile(XSSFTestDataSamples.getSampleFile("template.xlsx")).get()) {
            try (ZipSecureFile secureFile = new ZipSecureFile(XSSFTestDataSamples.getSampleFile("template.xlsx"))) {
                Enumeration<? extends ZipArchiveEntry> entries = thresholdInputStream.getEntries();
                while (entries.hasMoreElements()) {
                    ZipArchiveEntry entry = entries.nextElement();

                    try (InputStream inputStream = secureFile.getInputStream(entry)) {
                        assertTrue(IOUtils.toByteArray(inputStream).length > 0);
                    }
                }
            }
        }
    }

    @Test
    void testSettingMaxEntrySizeAsNegative() {
        assertThrows(IllegalArgumentException.class, () -> ZipSecureFile.setMaxEntrySize(-1));
    }

    @Test
    void testSettingMaxEntrySizeAs8Gb() {
        long approx8Gb = ZipSecureFile.MAX_ENTRY_SIZE * 2;
        try {
            ZipSecureFile.setMaxEntrySize(approx8Gb);
            assertEquals(approx8Gb, ZipSecureFile.getMaxEntrySize());
        } finally {
            ZipSecureFile.setMaxEntrySize(ZipSecureFile.MAX_ENTRY_SIZE);
        }
    }

    @Test
    void testSettingMaxTextSizeAsNegative() {
        assertThrows(IllegalArgumentException.class, () -> ZipSecureFile.setMaxTextSize(-1));
    }

    @Test
    void testSettingMaxTextSizeAs8GChars() {
        long approx8G = ZipSecureFile.MAX_ENTRY_SIZE * 2;
        try {
            ZipSecureFile.setMaxTextSize(approx8G);
            assertEquals(approx8G, ZipSecureFile.getMaxTextSize());
        } finally {
            ZipSecureFile.setMaxTextSize(ZipSecureFile.DEFAULT_MAX_TEXT_SIZE);
        }
    }

    @Test
    void testSettingGraceEntrySize() {
        long approx8G = ZipSecureFile.MAX_ENTRY_SIZE * 2;
        try {
            ZipSecureFile.setGraceEntrySize(approx8G);
            assertEquals(approx8G, ZipSecureFile.getGraceEntrySize());
        } finally {
            ZipSecureFile.setGraceEntrySize(ZipSecureFile.DEFAULT_GRACE_ENTRY_SIZE);
        }
    }

    @Test
    void testSettingMaxFileCount() {
        try {
            ZipSecureFile.setMaxFileCount(123456789);
            assertEquals(123456789, ZipSecureFile.getMaxFileCount());
        } finally {
            ZipSecureFile.setMaxFileCount(ZipSecureFile.DEFAULT_MAX_FILE_COUNT);
        }
    }

    @Test
    void testConstructorExceptionReleasesFileHandle() throws Exception {
        File tempFile = TempFile.createTempFile("duplicate-entries", ".zip");
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempFile)) {
            ZipArchiveEntry entry1 = new ZipArchiveEntry("test.txt");
            zos.putArchiveEntry(entry1);
            zos.write("hello".getBytes(StandardCharsets.UTF_8));
            zos.closeArchiveEntry();

            ZipArchiveEntry entry2 = new ZipArchiveEntry("test.txt");
            zos.putArchiveEntry(entry2);
            zos.write("world".getBytes(StandardCharsets.UTF_8));
            zos.closeArchiveEntry();
        }

        // The constructor should throw an IOException (specifically InvalidZipException)
        assertThrows(IOException.class, () -> new ZipSecureFile(tempFile));

        // If the file descriptor was correctly closed, we should be able to delete the file
        assertTrue(tempFile.delete(), "Temporary file should be successfully deleted after constructor failure");
    }
}
