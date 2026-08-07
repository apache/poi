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
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.poi.openxml4j.opc.internal.ZipHelper;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.DefaultTempFileCreationStrategy;
import org.apache.poi.util.TempFile;
import org.apache.poi.util.TempFileCreationStrategy;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;
import org.apache.poi.util.SuppressForbidden;
import org.junit.jupiter.api.parallel.Isolated;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@Isolated // modifies the default locale and we don't want to affect tests running in parallel
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

    @Test
    void testZipInputStreamZipEntrySourceExceptionReleasesResources() throws Exception {
        List<File> createdFiles = new ArrayList<>();
        TempFileCreationStrategy customStrategy = new TempFileCreationStrategy() {
            private final TempFileCreationStrategy delegate = new DefaultTempFileCreationStrategy();
            @Override
            public File createTempFile(String prefix, String suffix) throws IOException {
                File f = delegate.createTempFile(prefix, suffix);
                createdFiles.add(f);
                return f;
            }
            @Override
            public File createTempDirectory(String prefix) throws IOException {
                return delegate.createTempDirectory(prefix);
            }
        };

        TempFile.withStrategy(customStrategy, () -> {
            try {
                int oldThreshold = ZipInputStreamZipEntrySource.getThresholdBytesForTempFiles();
                ZipInputStreamZipEntrySource.setThresholdBytesForTempFiles(0);
                try {
                    File tempZipFile = TempFile.createTempFile("test-leak", ".zip");
                    try {
                        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZipFile)) {
                            ZipArchiveEntry entry1 = new ZipArchiveEntry("first.txt");
                            zos.putArchiveEntry(entry1);
                            zos.write("hello".getBytes(StandardCharsets.UTF_8));
                            zos.closeArchiveEntry();

                            ZipArchiveEntry entry2 = new ZipArchiveEntry("first.txt");
                            zos.putArchiveEntry(entry2);
                            zos.write("world".getBytes(StandardCharsets.UTF_8));
                            zos.closeArchiveEntry();
                        }

                        try (InputStream is = Files.newInputStream(tempZipFile.toPath());
                             ZipArchiveThresholdInputStream zis = ZipHelper.openZipStream(is)) {
                            assertThrows(IOException.class, () -> new ZipInputStreamZipEntrySource(zis));
                        }
                    } finally {
                        tempZipFile.delete();
                    }
                } finally {
                    ZipInputStreamZipEntrySource.setThresholdBytesForTempFiles(oldThreshold);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });

        assertFalse(createdFiles.isEmpty(), "At least one temporary file should have been created for the zip entries");
        for (File f : createdFiles) {
            assertFalse(f.exists(), "Temporary file " + f.getAbsolutePath() + " should have been deleted");
        }
    }

    @Test
    void testValidateMixedSeparatorDuplicateEntryNames() throws Exception {
        File tempFile = TempFile.createTempFile("mixed-duplicate-entries", ".zip");
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempFile)) {
            ZipArchiveEntry entry1 = new ZipArchiveEntry("sub/test.txt");
            zos.putArchiveEntry(entry1);
            zos.write("hello".getBytes(StandardCharsets.UTF_8));
            zos.closeArchiveEntry();

            ZipArchiveEntry entry2 = new ZipArchiveEntry("sub\\test.txt");
            zos.putArchiveEntry(entry2);
            zos.write("world".getBytes(StandardCharsets.UTF_8));
            zos.closeArchiveEntry();
        }

        try {
            // ZipSecureFile should detect the duplicate entries (mixed path separators)
            assertThrows(IOException.class, () -> new ZipSecureFile(tempFile));

            // ZipInputStreamZipEntrySource should also detect the duplicate entries
            try (InputStream is = java.nio.file.Files.newInputStream(tempFile.toPath());
                 ZipArchiveThresholdInputStream zis = org.apache.poi.openxml4j.opc.internal.ZipHelper.openZipStream(is)) {
                assertThrows(IOException.class, () -> new ZipInputStreamZipEntrySource(zis));
            }
        } finally {
            assertTrue(tempFile.delete(), "Temporary file should be successfully deleted");
        }
    }

    @Test
    void testZipInputStreamZipEntrySourceCopyFailureReleasesResources() throws Exception {
        List<File> createdFiles = new ArrayList<>();
        TempFileCreationStrategy customStrategy = new TempFileCreationStrategy() {
            private final TempFileCreationStrategy delegate = new DefaultTempFileCreationStrategy();
            @Override
            public File createTempFile(String prefix, String suffix) throws IOException {
                File f = delegate.createTempFile(prefix, suffix);
                createdFiles.add(f);
                return f;
            }
            @Override
            public File createTempDirectory(String prefix) throws IOException {
                return delegate.createTempDirectory(prefix);
            }
        };

        TempFile.withStrategy(customStrategy, () -> {
            try {
                int oldThreshold = ZipInputStreamZipEntrySource.getThresholdBytesForTempFiles();
                ZipInputStreamZipEntrySource.setThresholdBytesForTempFiles(0);
                try {
                    File tempZipFile = TempFile.createTempFile("test-leak-copy", ".zip");
                    try {
                        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZipFile)) {
                            ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
                            zos.putArchiveEntry(entry);
                            zos.write("some data that will fail during read".getBytes(StandardCharsets.UTF_8));
                            zos.closeArchiveEntry();
                        }

                        try (InputStream is = Files.newInputStream(tempZipFile.toPath());
                             ZipArchiveThresholdInputStream zis = new ZipArchiveThresholdInputStream(
                                     new org.apache.commons.compress.archivers.zip.ZipArchiveInputStream(is)) {
                                 private int readCount = 0;
                                 @Override
                                 public int read(byte[] b, int off, int len) throws IOException {
                                     readCount += len;
                                     if (readCount > 5) {
                                         throw new IOException("Simulated copy failure");
                                     }
                                     return super.read(b, off, len);
                                 }
                             }) {
                            assertThrows(IOException.class, () -> new ZipInputStreamZipEntrySource(zis));
                        }
                    } finally {
                        tempZipFile.delete();
                    }
                } finally {
                    ZipInputStreamZipEntrySource.setThresholdBytesForTempFiles(oldThreshold);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });

        assertFalse(createdFiles.isEmpty(), "At least one temporary file should have been created for the zip entry");
        for (File f : createdFiles) {
            assertFalse(f.exists(), "Temporary file " + f.getAbsolutePath() + " should have been deleted on copy failure");
        }
    }

    @Test
    @SuppressForbidden("test code")
    void testZipFileZipEntrySourceCaseInsensitiveMatchingUnderTurkishLocale() throws Exception {
        Locale defaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));

            File tempFile = TempFile.createTempFile("turkish-test", ".zip");
            try {
                try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempFile)) {
                    ZipArchiveEntry entry = new ZipArchiveEntry("content.xml");
                    zos.putArchiveEntry(entry);
                    zos.write("data".getBytes(StandardCharsets.UTF_8));
                    zos.closeArchiveEntry();
                }

                try (ZipSecureFile zipFile = new ZipSecureFile(tempFile)) {
                    ZipFileZipEntrySource source = new ZipFileZipEntrySource(zipFile);
                    ZipArchiveEntry entry = source.getEntry("Content.xml");
                    assertNotNull(entry, "Should find the entry case-insensitively even under Turkish locale");
                }
            } finally {
                tempFile.delete();
            }
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }
}
