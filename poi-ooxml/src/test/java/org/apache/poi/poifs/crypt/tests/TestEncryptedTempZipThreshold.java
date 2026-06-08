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

package org.apache.poi.poifs.crypt.tests;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.poifs.crypt.temp.AesZipFileZipEntrySource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * Regression test proving that encrypted-temp ZIP creation enforces
 * the {@link ZipSecureFile#MIN_INFLATE_RATIO} check.
 *
 * This test constructs a small compressed ZIP entry which expands to a
 * much larger uncompressed payload (very low inflate ratio). The
 * encrypted-temp creation must trigger the min-inflate-ratio check and
 * throw an IOException. The test is deterministic and uses a small
 * in-memory ZIP payload.
 */
@Isolated // changes global ZipSecureFile limits
public class TestEncryptedTempZipThreshold {

    @Test
    void minInflateRatioEnforced() throws IOException {
        final double oldMinInflateRatio = ZipSecureFile.getMinInflateRatio();
        final long oldGraceEntrySize = ZipSecureFile.getGraceEntrySize();
        try {
            // make threshold very strict so small compressed -> large expanded fails
            ZipSecureFile.setMinInflateRatio(0.5d);
            ZipSecureFile.setGraceEntrySize(0);

            // create an in-memory zip with one entry that compresses extremely well
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
                ZipArchiveEntry ze = new ZipArchiveEntry("large.txt");
                zos.putArchiveEntry(ze);

                // write a highly compressible payload (repeated 'A')
                byte[] payload = new byte[200_000];
                for (int i = 0; i < payload.length; i++) payload[i] = 'A';
                // use high compression level to make compressed size tiny
                zos.setLevel(Deflater.BEST_COMPRESSION);
                zos.write(payload);
                zos.closeArchiveEntry();
                zos.finish();
            }

            byte[] zipBytes = baos.toByteArray();
            try (InputStream in = new ByteArrayInputStream(zipBytes)) {
                // createZipEntrySource will attempt to materialize entries and should fail
                assertThrows(IOException.class, () -> AesZipFileZipEntrySource.createZipEntrySource(in));
            }
        } finally {
            ZipSecureFile.setMinInflateRatio(oldMinInflateRatio);
            ZipSecureFile.setGraceEntrySize(oldGraceEntrySize);
        }
    }
}
