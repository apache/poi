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

package org.apache.poi.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.hssf.record.common.FormatRun;
import org.junit.jupiter.api.Test;

class TestLittleEndianInputStream {

    @Test
    void formatRun() throws IOException {
        FormatRun fr = new FormatRun((short)4, (short)0x15c);
        assertEquals(4, fr.getCharacterPos());
        assertEquals(0x15c, fr.getFontIndex());

        UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
        LittleEndianOutputStream out = new LittleEndianOutputStream(baos);

        fr.serialize(out);

        byte[] b = baos.toByteArray();
        assertEquals(4, b.length);
        assertEquals(4, b[0]);
        assertEquals(0, b[1]);
        assertEquals(0x5c, b[2]);
        assertEquals(0x01, b[3]);

        LittleEndianInputStream inp = new LittleEndianInputStream(new ByteArrayInputStream(b));
        fr = new FormatRun(inp);
        assertEquals(4, fr.getCharacterPos());
        assertEquals(0x15c, fr.getFontIndex());

        assertEquals(4, inp.getReadIndex());

        byte[] arr = new byte[1024];
        assertEquals(-1, inp.read(arr, 0, 1024));
        assertEquals(4, inp.getReadIndex());
    }

    @Test
    void empty() throws IOException {
        byte[] b = new byte[0];
        LittleEndianInputStream inp = new LittleEndianInputStream(new ByteArrayInputStream(b));
        assertEquals(0, inp.getReadIndex());

        byte[] arr = new byte[1024];
        assertEquals(-1, inp.read(arr, 0, 1024));
        assertEquals(0, inp.getReadIndex());
    }

    @Test
    void readIndexCountsSingleByteReadsAndSkips() throws IOException {
        byte[] b = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        try (LittleEndianInputStream inp = new LittleEndianInputStream(new ByteArrayInputStream(b))) {
            assertEquals(1, inp.read());
            assertEquals(1, inp.getReadIndex());
            assertEquals(2, inp.skip(2));
            assertEquals(3, inp.getReadIndex());
            assertEquals(4, inp.readUByte());
            assertEquals(4, inp.getReadIndex());
            inp.skipFully(3);
            assertEquals(7, inp.getReadIndex());
            assertEquals(8, inp.readByte());
            assertEquals(8, inp.getReadIndex());
        }
    }

    @Test
    void skipFully() throws IOException {
        byte[] b = new byte[5000];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) i;
        }
        try (LittleEndianInputStream inp = new LittleEndianInputStream(new ByteArrayInputStream(b))) {
            inp.skipFully(0);
            assertThrows(IllegalArgumentException.class, () -> inp.skipFully(-1));
            // larger than the internal skip buffer
            inp.skipFully(4500L);
            assertEquals(4500, inp.getReadIndex());
            assertEquals(b[4500] & 0xFF, inp.readUByte());
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> inp.skipFully(1000));
            assertEquals("Unexpected end-of-file", ex.getMessage());
        }
    }

    /**
     * The primitive reads must not fail when the inner stream returns fewer bytes than requested
     */
    @Test
    void primitivesWithShortReads() throws IOException {
        UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
        try (LittleEndianOutputStream out = new LittleEndianOutputStream(baos)) {
            out.writeLong(0x0102030405060708L);
            out.writeInt(-2);
            out.writeShort(0xFFFE);
            out.writeByte(0xFD);
        }
        InputStream oneByteAtATime = new FilterInputStream(baos.toInputStream()) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return super.read(b, off, Math.min(len, 1));
            }
        };
        try (LittleEndianInputStream inp = new LittleEndianInputStream(oneByteAtATime)) {
            assertEquals(0x0102030405060708L, inp.readLong());
            assertEquals(-2, inp.readInt());
            assertEquals(0xFFFE, inp.readUShort());
            assertEquals(0xFD, inp.readUByte());
            assertEquals(15, inp.getReadIndex());
            assertThrows(IllegalStateException.class, inp::readShort);
        }
    }
}
