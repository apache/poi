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

package org.apache.poi.hemf.record.emfplus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;

import org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectType;
import org.apache.poi.hemf.record.emfplus.HemfPlusPath.EmfPlusPath;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.Test;

class TestHemfPlusPath {

    /**
     * A run-length-encoded EmfPlusPath expands the per-point type table from a
     * sequence of (runCount, type) pairs. The fill must start at the running
     * offset; a crafted path with the RLE_COMPRESSED flag set used to write at
     * the array end, overrunning the pointTypes buffer.
     */
    @Test
    void rleCompressedPathFillsWithinBounds() throws Exception {
        final int pointCount = 2;
        byte[] data = new byte[64];
        int pos = 0;
        // EmfPlusGraphicsVersion: metafile signature 0xDBC01, graphics version 1
        LittleEndian.putInt(data, pos, 0xDBC01001); pos += 4;
        LittleEndian.putInt(data, pos, pointCount); pos += 4;
        // pointFlags: RLE_COMPRESSED (0x1000), neither relative nor compressed
        LittleEndian.putShort(data, pos, (short) 0x1000); pos += 2;
        pos += 2; // skipped
        // pointCount absolute EmfPlusPointF entries (two floats each)
        pos += pointCount * 8;
        // single RLE run covering both points: runCount=2, type byte
        data[pos++] = 0x02;
        data[pos++] = 0x00;

        EmfPlusPath path = new EmfPlusPath();
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
            assertDoesNotThrow(() -> path.init(leis, data.length, EmfPlusObjectType.PATH, 0));
        }
    }

    /**
     * PointCount is an untrusted 32-bit field that is used directly as the length of the
     * {@code pathPoints} and {@code pointTypes} arrays. Without validation a crafted path
     * makes the parser run {@code new Point2D[pointCount]} / {@code new byte[pointCount]} on
     * an arbitrary attacker value - a high-bit value yields a NegativeArraySizeException and a
     * large positive value (e.g. 0x40000000 ~ 1 billion points in a 12-byte record) an
     * OutOfMemoryError. Both must be rejected as a RecordFormatException before allocation,
     * consistent with the sibling EmfPlusDrawDriverString record.
     */
    @Test
    void pathPointCountIsValidatedBeforeAllocation() {
        // high-bit count -> would be new Point2D[negative] -> NegativeArraySizeException (checked first,
        // so unpatched code fails here without attempting any large allocation)
        assertThrows(RecordFormatException.class, () -> initPath(0x80000000));
        // ~1 billion points claimed in a 12-byte record -> would be a multi-GB allocation -> OutOfMemoryError
        assertThrows(RecordFormatException.class, () -> initPath(0x40000000));
    }

    private static void initPath(int pointCount) throws Exception {
        byte[] data = new byte[12];
        int pos = 0;
        // EmfPlusGraphicsVersion: metafile signature 0xDBC01, graphics version 1
        LittleEndian.putInt(data, pos, 0xDBC01001); pos += 4;
        LittleEndian.putInt(data, pos, pointCount); pos += 4;
        // pointFlags + skipped reserved short (the allocation happens right after these)
        LittleEndian.putShort(data, pos, (short) 0); pos += 2;
        LittleEndian.putShort(data, pos, (short) 0);

        EmfPlusPath path = new EmfPlusPath();
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
            path.init(leis, data.length, EmfPlusObjectType.PATH, 0);
        }
    }
}
