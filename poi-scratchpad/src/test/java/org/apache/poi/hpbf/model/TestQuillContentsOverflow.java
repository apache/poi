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

package org.apache.poi.hpbf.model;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.hpbf.HPBFDocument;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.Test;

/**
 * The QuillContents bit descriptors store {@code from} and {@code len} as
 * 32-bit unsigned integers. A crafted .pub with a value &gt;
 * {@code Integer.MAX_VALUE} used to be silently narrowed via a plain
 * {@code (int)} cast, letting a wrapped value reach
 * {@code IOUtils.safelyClone}'s generic
 * {@code "Invalid offset\length specified"} check — the original uint32 was
 * lost in the message. Validate the uint32 values up-front via
 * {@code IOUtils.safelyAllocateCheck} (length) and an explicit
 * {@code RecordFormatException} (offset) so the failure carries the actual
 * offending value, matching the {@code PointerFactory} fix in PR #1076.
 */
public final class TestQuillContentsOverflow {

    /**
     * Builds an in-memory .pub whose Quill/QuillSub/CONTENTS stream contains
     * a single populated bit descriptor at slot 0 with the supplied uint32
     * {@code from}/{@code len} values.
     */
    private static byte[] buildPubWithBit(long fromU32, long lenU32) throws IOException {
        // 512-byte CONTENTS stream: 8-byte "CHNKINK " header + 24 ignored bytes,
        // then 20x 24-byte bit descriptors. Only slot 0 is populated; the
        // 0x18/0x00 sentinel marks it as having data, the rest of the descriptors
        // start with 0x00 and are skipped by QuillContents.
        byte[] qc = new byte[512];
        System.arraycopy("CHNKINK ".getBytes(LocaleUtil.CHARSET_1252), 0, qc, 0, 8);
        int off = 0x20;
        qc[off]     = 0x18;
        qc[off + 1] = 0x00;
        System.arraycopy("TEXT".getBytes(LocaleUtil.CHARSET_1252), 0, qc, off + 2,  4);
        System.arraycopy("TEXT".getBytes(LocaleUtil.CHARSET_1252), 0, qc, off + 12, 4);
        LittleEndian.putUInt(qc, off + 16, fromU32);
        LittleEndian.putUInt(qc, off + 20, lenU32);

        try (POIFSFileSystem fs = new POIFSFileSystem();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            DirectoryEntry quill = fs.getRoot().createDirectory("Quill");
            DirectoryEntry quillSub = quill.createDirectory("QuillSub");
            quillSub.createDocument("CONTENTS", new ByteArrayInputStream(qc));
            // HPBFDocument also requires a top-level Contents stream and an
            // Escher/EscherStm pair to construct the surrounding parts.
            fs.getRoot().createDocument("Contents",
                    new ByteArrayInputStream(new byte[0]));
            DirectoryEntry escher = fs.getRoot().createDirectory("Escher");
            escher.createDocument("EscherStm", new ByteArrayInputStream(new byte[0]));
            escher.createDocument("EscherDelayStm", new ByteArrayInputStream(new byte[0]));
            fs.writeFilesystem(bos);
            return bos.toByteArray();
        }
    }

    @Test
    void testRejectsOversizedLength() throws IOException {
        byte[] pub = buildPubWithBit(0x80L, 0x80000001L); // len wraps to negative as int
        assertThrows(RecordFormatException.class, () -> {
            try (HPBFDocument d = new HPBFDocument(new ByteArrayInputStream(pub))) {
                // QuillContents parsing happens in the constructor; reaching here is failure.
                d.getQuillContents();
            }
        });
    }

    @Test
    void testRejectsOversizedOffset() throws IOException {
        byte[] pub = buildPubWithBit(0xFFFFFFFFL, 0x10L); // from wraps to -1 as int
        RecordFormatException ex = assertThrows(RecordFormatException.class, () -> {
            try (HPBFDocument d = new HPBFDocument(new ByteArrayInputStream(pub))) {
                d.getQuillContents();
            }
        });
        // The pre-cast check preserves the original uint32 (4294967295) in the message,
        // unlike the wrapped int (-1) the legacy safelyClone path would have reported.
        assertTrue(ex.getMessage().contains("4294967295"),
                "expected original uint32 in message, got: " + ex.getMessage());
    }
}
