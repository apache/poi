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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void testInvalidEntrySize() throws IOException {
        final long fakeLen = 123;
        final String fakeValue = "fakeValue";
        ZipArchiveEntry entry = new ZipArchiveEntry("hack123") {
            @Override
            public long getSize() {
                return fakeLen;
            }
        };
        UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
        final byte[] data = fakeValue.getBytes(StandardCharsets.UTF_8);
        baos.write(data);
        // the data in the stream is less than the fakeLen in the entry
        // this is a regression test for the existing internal behaviour - if we choose to change the
        // behaviour in the future, this test can be updated
        assertThrows(EOFException.class, () -> new ZipArchiveFakeEntry(entry, baos.toInputStream()));
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
            assertEquals(data.length, IOUtils.copy(zipArchiveFakeEntry.getInputStream(), baos2));
            assertEquals(fakeValue, baos2.toString(StandardCharsets.UTF_8.name()));
            assertEquals(data.length, zipArchiveFakeEntry.getSize());
            assertEquals(unencryptedTempFileExpected, zipArchiveFakeEntry.isUnencryptedTempFileBacked());
            assertEquals(encryptedTempFileExpected, zipArchiveFakeEntry.isEncryptedTempFileBacked());
        }
    }

}
