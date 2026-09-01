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
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated // changes static values, so other tests should not run at the same time
public class TestZipArchiveFakeEntry {
    @Test
    void testSize() throws IOException {
        runTestSize(false, false);
    }

    @Test
    void testSizeTempFileBacked() throws IOException {
        ZipInputStreamZipEntrySource.setThresholdBytesForTempFiles(0);
        try {
            runTestSize(true, false);
        } finally {
            ZipInputStreamZipEntrySource.setThresholdBytesForTempFiles(-1);
        }
    }

    @Test
    void testSizeEncryptedTempFileBacked() throws IOException {
        ZipInputStreamZipEntrySource.setThresholdBytesForTempFiles(0);
        ZipInputStreamZipEntrySource.setEncryptTempFiles(true);
        try {
            runTestSize(false, true);
        } finally {
            ZipInputStreamZipEntrySource.setThresholdBytesForTempFiles(-1);
            ZipInputStreamZipEntrySource.setEncryptTempFiles(false);
        }
    }

    @Test
    void testKnownEntrySize() throws IOException {
        // an entry whose declared size matches the data reads with a single exactly-sized buffer
        final String fakeValue = "fakeValue";
        final byte[] data = fakeValue.getBytes(StandardCharsets.UTF_8);
        ZipArchiveEntry entry = new ZipArchiveEntry("hack123") {
            @Override
            public long getSize() {
                return data.length;
            }
        };
        UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
        baos.write(data);
        try (ZipArchiveFakeEntry zipArchiveFakeEntry = new ZipArchiveFakeEntry(entry, baos.toInputStream())) {
            assertEquals(data.length, zipArchiveFakeEntry.getSize());
            UnsynchronizedByteArrayOutputStream baos2 = UnsynchronizedByteArrayOutputStream.builder().get();
            try (InputStream stream = zipArchiveFakeEntry.getInputStream()) {
                assertEquals(data.length, IOUtils.copy(stream, baos2));
            }
            assertEquals(fakeValue, baos2.toString(StandardCharsets.UTF_8.name()));
        }
    }

    @Test
    void testEntrySizeLargerThanData() {
        // A malicious zip local file header can declare a huge uncompressed size while the entry
        // actually contains only a few bytes. The declared size must not be trusted to size the
        // read buffer (the initial allocation is capped well below getMaxEntrySize()), and an
        // entry holding fewer bytes than declared is rejected.
        final long fakeLen = 99_000_000;
        ZipArchiveEntry entry = new ZipArchiveEntry("hack123") {
            @Override
            public long getSize() {
                return fakeLen;
            }
        };
        UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
        baos.write("fakeValue".getBytes(StandardCharsets.UTF_8), 0, 9);
        assertThrows(EOFException.class, () -> new ZipArchiveFakeEntry(entry, baos.toInputStream()));
    }

    @Test
    void testEntrySizeSmallerThanData() {
        // an entry holding more bytes than its declared size is rejected rather than read past
        // the declared size
        ZipArchiveEntry entry = new ZipArchiveEntry("hack123") {
            @Override
            public long getSize() {
                return 4;
            }
        };
        UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
        baos.write("fakeValue".getBytes(StandardCharsets.UTF_8), 0, 9);
        IOException ex = assertThrows(IOException.class, () -> new ZipArchiveFakeEntry(entry, baos.toInputStream()));
        assertTrue(ex.getMessage().contains("more data than its declared size"), ex.getMessage());
    }

    @Test
    void testEntryDataLargerThanMaxEntrySize() throws IOException {
        // Reading is bounded by getMaxEntrySize(): a stream longer than the limit is rejected
        // rather than buffered in full.
        final int previous = ZipArchiveFakeEntry.getMaxEntrySize();
        ZipArchiveFakeEntry.setMaxEntrySize(4);
        try {
            ZipArchiveEntry entry = new ZipArchiveEntry("hack123");
            UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
            baos.write("fakeValue".getBytes(StandardCharsets.UTF_8));
            assertThrows(RecordFormatException.class, () -> new ZipArchiveFakeEntry(entry, baos.toInputStream()));
        } finally {
            ZipArchiveFakeEntry.setMaxEntrySize(previous);
        }
    }

    @Test
    void testUnknownSizeBelowThresholdStaysInMemory() throws IOException {
        // an entry that does not declare its size (getSize() == -1) is buffered in memory
        // as long as it stays below the temp-file threshold - no temp file is created
        ZipInputStreamZipEntrySource.setThresholdBytesForTempFiles(1000);
        try {
            ZipArchiveEntry entry = new ZipArchiveEntry("hack123");
            UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
            final byte[] data = "fakeValue".getBytes(StandardCharsets.UTF_8);
            baos.write(data);
            try (ZipArchiveFakeEntry zipArchiveFakeEntry = new ZipArchiveFakeEntry(entry, baos.toInputStream())) {
                assertFalse(zipArchiveFakeEntry.isUnencryptedTempFileBacked());
                assertFalse(zipArchiveFakeEntry.isEncryptedTempFileBacked());
                assertEquals(data.length, zipArchiveFakeEntry.getSize());
                UnsynchronizedByteArrayOutputStream baos2 = UnsynchronizedByteArrayOutputStream.builder().get();
                try (InputStream stream = zipArchiveFakeEntry.getInputStream()) {
                    IOUtils.copy(stream, baos2);
                }
                assertEquals("fakeValue", baos2.toString(StandardCharsets.UTF_8.name()));
            }
        } finally {
            ZipInputStreamZipEntrySource.setThresholdBytesForTempFiles(-1);
        }
    }

    @Test
    void testUnknownSizeAboveThresholdSpillsToTempFile() throws IOException {
        // an entry that does not declare its size and turns out to be larger than the
        // temp-file threshold spills to a temp file with no data lost
        ZipInputStreamZipEntrySource.setThresholdBytesForTempFiles(4);
        try {
            runTestSpill(true, false);
        } finally {
            ZipInputStreamZipEntrySource.setThresholdBytesForTempFiles(-1);
        }
    }

    @Test
    void testUnknownSizeAboveThresholdSpillsToEncryptedTempFile() throws IOException {
        ZipInputStreamZipEntrySource.setThresholdBytesForTempFiles(4);
        ZipInputStreamZipEntrySource.setEncryptTempFiles(true);
        try {
            runTestSpill(false, true);
        } finally {
            ZipInputStreamZipEntrySource.setThresholdBytesForTempFiles(-1);
            ZipInputStreamZipEntrySource.setEncryptTempFiles(false);
        }
    }

    private static void runTestSpill(boolean unencryptedTempFileExpected,
                                     boolean encryptedTempFileExpected) throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry("hack123");
        UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
        final byte[] data = "fakeValue".getBytes(StandardCharsets.UTF_8);
        baos.write(data);
        try (ZipArchiveFakeEntry zipArchiveFakeEntry = new ZipArchiveFakeEntry(entry, baos.toInputStream())) {
            assertEquals(unencryptedTempFileExpected, zipArchiveFakeEntry.isUnencryptedTempFileBacked());
            assertEquals(encryptedTempFileExpected, zipArchiveFakeEntry.isEncryptedTempFileBacked());
            assertEquals(data.length, zipArchiveFakeEntry.getSize());
            UnsynchronizedByteArrayOutputStream baos2 = UnsynchronizedByteArrayOutputStream.builder().get();
            try (InputStream stream = zipArchiveFakeEntry.getInputStream()) {
                assertEquals(data.length, IOUtils.copy(stream, baos2));
            }
            assertEquals("fakeValue", baos2.toString(StandardCharsets.UTF_8.name()));
        }
    }

    static void runTestSize(boolean unencryptedTempFileExpected,
                            boolean encryptedTempFileExpected) throws IOException {
        final String fakeEntryName = "hack123";
        final String fakeValue = "fakeValue";
        ZipArchiveEntry entry = new ZipArchiveEntry(fakeEntryName);
        UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
        final byte[] data = fakeValue.getBytes(StandardCharsets.UTF_8);
        baos.write(data);
        try (ZipArchiveFakeEntry zipArchiveFakeEntry = new ZipArchiveFakeEntry(entry, baos.toInputStream())) {
            assertEquals(fakeEntryName, zipArchiveFakeEntry.getName());
            UnsynchronizedByteArrayOutputStream baos2 = UnsynchronizedByteArrayOutputStream.builder().get();
            try (InputStream stream = zipArchiveFakeEntry.getInputStream()) {
                assertEquals(data.length, IOUtils.copy(stream, baos2));
            }
            assertEquals(fakeValue, baos2.toString(StandardCharsets.UTF_8.name()));
            assertEquals(data.length, zipArchiveFakeEntry.getSize());
            assertEquals(unencryptedTempFileExpected, zipArchiveFakeEntry.isUnencryptedTempFileBacked());
            assertEquals(encryptedTempFileExpected, zipArchiveFakeEntry.isEncryptedTempFileBacked());
        }
    }

}
