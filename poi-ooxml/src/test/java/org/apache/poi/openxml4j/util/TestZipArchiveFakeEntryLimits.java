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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Isolated
class TestZipArchiveFakeEntryLimits {
    @Test
    void oversizedEntryIsRejectedInTempFileMode() throws IOException {
        int origThreshold = ZipInputStreamZipEntrySource.getThresholdBytesForTempFiles();
        int origMax = ZipArchiveFakeEntry.getMaxEntrySize();
        try {
            // Force temp-file spooling path
            ZipInputStreamZipEntrySource.setThresholdBytesForTempFiles(0);
            ZipArchiveFakeEntry.setMaxEntrySize(100);

            // Create ZIP with unknown size (-1) and payload > maxEntrySize
            byte[] zipBytes = createZipWithUnknownSize("test.txt", 200);
            try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
                 ZipArchiveThresholdInputStream ztis = new ZipArchiveThresholdInputStream(zis)) {
                
                // Should throw RecordFormatException during ZipInputStreamZipEntrySource construction
                assertThrows(RecordFormatException.class, () -> new ZipInputStreamZipEntrySource(ztis));
            }
        } finally {
            // Restore static values
            ZipInputStreamZipEntrySource.setThresholdBytesForTempFiles(origThreshold);
            ZipArchiveFakeEntry.setMaxEntrySize(origMax);
        }
    }

    private static byte[] createZipWithUnknownSize(String name, int size) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos)) {
            ZipArchiveEntry entry = new ZipArchiveEntry(name);
            zos.putArchiveEntry(entry);
            zos.write(new byte[size]);
            zos.closeArchiveEntry();
            zos.finish();
        }
        return bos.toByteArray();
    }
}
