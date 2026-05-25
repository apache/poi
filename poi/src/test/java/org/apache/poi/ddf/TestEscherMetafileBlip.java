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

package org.apache.poi.ddf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link EscherMetafileBlip}, including the inflation size guard
 * against zip-bomb style payloads.
 */
class TestEscherMetafileBlip {

    private int savedMaxRecordLength;

    @BeforeEach
    void saveLimit() {
        savedMaxRecordLength = EscherMetafileBlip.getMaxRecordLength();
    }

    @AfterEach
    void restoreLimit() {
        EscherMetafileBlip.setMaxRecordLength(savedMaxRecordLength);
    }

    /**
     * Normal case: inflated data is within the configured limit.
     * fillFields() must succeed and return the correct uncompressed bytes.
     */
    @Test
    void testInflatePictureDataWithinLimit() throws IOException {
        byte[] plain = new byte[100];
        Arrays.fill(plain, (byte) 'X');
        byte[] compressed = deflate(plain);

        // limit is well above 100 bytes
        EscherMetafileBlip.setMaxRecordLength(1000);

        byte[] record = buildWmfRecord(compressed, plain.length);
        EscherMetafileBlip blip = new EscherMetafileBlip();
        assertDoesNotThrow(() -> blip.fillFields(record, 0, new DefaultEscherRecordFactory()));
        assertArrayEquals(plain, blip.getPicturedata());
    }

    /**
     * Zip-bomb guard: inflated data exceeds MAX_RECORD_LENGTH.
     * fillFields() must throw {@link RecordFormatException} instead of OOM.
     */
    @Test
    void testInflatePictureDataExceedsLimitThrows() throws IOException {
        // plain data is 1000 bytes; set the limit below that
        byte[] plain = new byte[1000];
        Arrays.fill(plain, (byte) 'A');
        byte[] compressed = deflate(plain);

        EscherMetafileBlip.setMaxRecordLength(500);

        byte[] record = buildWmfRecord(compressed, plain.length);
        EscherMetafileBlip blip = new EscherMetafileBlip();
        RecordFormatException ex = assertThrows(RecordFormatException.class,
                () -> blip.fillFields(record, 0, new DefaultEscherRecordFactory()));
        assertTrue(ex.getMessage().contains("MAX_RECORD_LENGTH"),
                "Exception message should mention MAX_RECORD_LENGTH");
    }

    // ---- helpers --------------------------------------------------------

    /** DEFLATE-compress {@code data} using standard java.util.zip deflate. */
    private static byte[] deflate(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DeflaterOutputStream dos = new DeflaterOutputStream(bos)) {
            dos.write(data);
        }
        return bos.toByteArray();
    }

    /**
     * Build a minimal WMF EscherMetafileBlip record byte array from the given
     * already-compressed picture data.
     *
     * Record layout (after the 8-byte Escher header):
     *   16 bytes  UID1
     *    4 bytes  cb (uncompressed size)
     *   16 bytes  rcBounds (x1, y1, x2, y2 – all zero)
     *    8 bytes  ptSize  (w, h – all zero)
     *    4 bytes  cbSave  (compressed size)
     *    1 byte   fCompression = 0 (DEFLATE)
     *    1 byte   fFilter      = 0xFE
     *    n bytes  compressed picture data
     *
     * Options is set to MSOBI_WMF (0x2160) so that options ^ signature == 0
     * (not 0x10), meaning no secondary UID is written.
     */
    private static byte[] buildWmfRecord(byte[] compressedData, int uncompressedSize) {
        // options = 0x2160 (MSOBI_WMF); recordId = 0xF01B (BLIP_WMF)
        final short options  = 0x2160;
        final short recordId = (short) EscherRecordTypes.BLIP_WMF.typeID;

        // bytes after the 8-byte header
        int bodyLen = 16 + 4 + 16 + 8 + 4 + 1 + 1 + compressedData.length;
        byte[] record = new byte[8 + bodyLen];
        int pos = 0;

        // header
        LittleEndian.putShort(record, pos, options);       pos += 2;
        LittleEndian.putShort(record, pos, recordId);      pos += 2;
        LittleEndian.putInt(record, pos, bodyLen);         pos += 4;

        // UID1 (16 zero bytes)
        pos += 16;

        // cb (uncompressed size)
        LittleEndian.putInt(record, pos, uncompressedSize); pos += 4;

        // rcBounds (16 zero bytes)
        pos += 16;

        // ptSize (8 zero bytes)
        pos += 8;

        // cbSave (compressed size)
        LittleEndian.putInt(record, pos, compressedData.length); pos += 4;

        // fCompression = 0 (DEFLATE)
        record[pos++] = 0;

        // fFilter = 0xFE
        record[pos++] = (byte) 0xFE;

        // compressed picture data
        System.arraycopy(compressedData, 0, record, pos, compressedData.length);

        return record;
    }
}
