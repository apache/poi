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

package org.apache.poi.hdgf.pointers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.Test;

/**
 * Tests for the pointer factory, and the pointers themselves
 */
public final class TestPointerFactory {
    // Type: 14    Addr: 011eb2ac  Offset: 1dd4    Len:  14d   Format: 52  From: 24
    private static byte[] vp5_a = {
        0x14, 0, 0x52, 0, (byte)0xac, (byte)0xb2, 0x1e, 1, (byte)0xd4, 0x1d, 0, 0,
        0x4d, 1, 0, 0
    };

    // Type: 16   Addr: 0143aff4  Offset: 80   Len: 54   Format: 46   From: 8a94
    private static byte[] vp6_a = {
        22, 0, 0, 0, -12, -81, 67, 1, -128, 0, 0, 0, 84, 0, 0, 0, 70, 0
    };
    // Type: 17   Addr: 014fd84c  Offset: d4   Len: 20   Format: 54   From: 8a94
    private static byte[] vp6_b = {
        23, 0, 0, 0, 76, -40, 79, 1, -44, 0, 0, 0, 32, 0, 0, 0, 84, 0
    };
    // Type: 17   Addr: 014fd8bc  Offset: f8   Len: 20   Format: 54   From: 8a94
    private static byte[] vp6_c = {
        23, 0, 0, 0, -68, -40, 79, 1, -8, 0, 0, 0, 32, 0, 0, 0, 84, 0
    };
    // Type: ff   Addr: 014fffac  Offset: 0    Len:  0   Format: 60   From: 8a94
    private static byte[] vp6_d = {
        -1, 0, 0, 0, -84, -1, 79, 1, 0, 0, 0, 0, 0, 0, 0, 0, 96, 0
    };

    @Test
    void testCreateV4() {
        PointerFactory pf = new PointerFactory(4);
        assertThrows(IllegalArgumentException.class, () -> pf.createPointer(new byte[]{}, 0));
    }

    @Test
    void testCreateV5() {
        PointerFactory pf = new PointerFactory(5);

        Pointer a = pf.createPointer(vp5_a, 0);
        assertEquals(0x14, a.getType());
        assertEquals(0x011eb2ac, a.getAddress());
        assertEquals(0x1dd4, a.getOffset());
        assertEquals(0x14d, a.getLength());
        assertEquals(0x52, a.getFormat());

        // TODO Are these right?
        assertTrue(a.destinationCompressed());
        assertFalse(a.destinationHasStrings());
        assertFalse(a.destinationHasChunks());
        assertTrue(a.destinationHasPointers());

        assertEquals(16, a.getSizeInBytes());
    }

    @Test
    void testCreateV6() {
        PointerFactory pf = new PointerFactory(6);

        Pointer a = pf.createPointer(vp6_a, 0);
        assertEquals(0x16, a.getType());
        assertEquals(0x0143aff4, a.getAddress());
        assertEquals(0x80, a.getOffset());
        assertEquals(0x54, a.getLength());
        assertEquals(0x46, a.getFormat());

        assertTrue(a.destinationCompressed());
        assertTrue(a.destinationHasStrings());
        assertFalse(a.destinationHasChunks());
        assertFalse(a.destinationHasPointers());

        assertEquals(18, a.getSizeInBytes());


        Pointer b = pf.createPointer(vp6_b, 0);
        assertEquals(0x17, b.getType());
        assertEquals(0x014fd84c, b.getAddress());
        assertEquals(0xd4, b.getOffset());
        assertEquals(0x20, b.getLength());
        assertEquals(0x54, b.getFormat());

        assertFalse(b.destinationCompressed());
        assertFalse(b.destinationHasStrings());
        assertFalse(b.destinationHasChunks());
        assertTrue(b.destinationHasPointers());

        Pointer c = pf.createPointer(vp6_c, 0);
        assertEquals(0x17, c.getType());
        assertEquals(0x014fd8bc, c.getAddress());
        assertEquals(0xf8, c.getOffset());
        assertEquals(0x20, c.getLength());
        assertEquals(0x54, c.getFormat());

        assertFalse(c.destinationCompressed());
        assertFalse(c.destinationHasStrings());
        assertFalse(c.destinationHasChunks());
        assertTrue(c.destinationHasPointers());

        // Type: ff   Addr: 014fffac  Offset: 0    Len:  0   Format: 60   From: 8a94
        Pointer d = pf.createPointer(vp6_d, 0);
        assertEquals(0xff, d.getType());
        assertEquals(0x014fffac, d.getAddress());
        assertEquals(0x00, d.getOffset());
        assertEquals(0x00, d.getLength());
        assertEquals(0x60, d.getFormat());

        assertFalse(d.destinationCompressed());
        assertFalse(d.destinationHasStrings());
        assertFalse(d.destinationHasChunks());
        assertFalse(d.destinationHasPointers());
    }

    /**
     * A v6+ Pointer reads its Offset and Length fields as 32-bit unsigned
     * integers, then narrows them to int and hands the pair to
     * {@code Stream.createStream} -&gt; {@code StreamStore} /
     * {@code CompressedStreamStore} -&gt; {@code IOUtils.safelyClone}. A
     * crafted file with Length &gt; Integer.MAX_VALUE used to be silently
     * narrowed via a plain {@code (int)} cast, letting a wrapped value flow
     * into the downstream bounds check. Validate the uint32 values up-front
     * via {@code IOUtils.safelyAllocateCheck} (length) and an explicit
     * {@code RecordFormatException} (offset) so the failure carries the
     * actual offending value, not a bare "integer overflow".
     */
    @Test
    void testCreateV6RejectsOversizedLength() {
        PointerFactory pf = new PointerFactory(11);

        byte[] ptr = new byte[18];
        LittleEndian.putInt  (ptr, 0,  0x16);              // type
        LittleEndian.putUInt (ptr, 4,  0x0143aff4L);       // address
        LittleEndian.putUInt (ptr, 8,  0x80L);             // offset (valid)
        LittleEndian.putUInt (ptr, 12, 0x80000001L);       // length: would wrap to negative int
        LittleEndian.putShort(ptr, 16, (short)0x46);       // format

        assertThrows(RecordFormatException.class, () -> pf.createPointer(ptr, 0));
    }

    @Test
    void testCreateV6RejectsOversizedOffset() {
        PointerFactory pf = new PointerFactory(11);

        byte[] ptr = new byte[18];
        LittleEndian.putInt  (ptr, 0,  0x16);
        LittleEndian.putUInt (ptr, 4,  0x0143aff4L);
        LittleEndian.putUInt (ptr, 8,  0xFFFFFFFFL);       // offset: would wrap to -1
        LittleEndian.putUInt (ptr, 12, 0x54L);
        LittleEndian.putShort(ptr, 16, (short)0x46);

        assertThrows(RecordFormatException.class, () -> pf.createPointer(ptr, 0));
    }

    @Test
    void testCreateV5RejectsOversizedLength() {
        PointerFactory pf = new PointerFactory(5);

        byte[] ptr = new byte[16];
        LittleEndian.putShort(ptr, 0,  (short)0x14);
        LittleEndian.putShort(ptr, 2,  (short)0x52);
        LittleEndian.putUInt (ptr, 4,  0x011eb2acL);
        LittleEndian.putUInt (ptr, 8,  0x1dd4L);
        LittleEndian.putUInt (ptr, 12, 0x80000001L);       // length: would wrap to negative

        assertThrows(RecordFormatException.class, () -> pf.createPointer(ptr, 0));
    }

    @Test
    void testCreateV5RejectsOversizedOffset() {
        PointerFactory pf = new PointerFactory(5);

        byte[] ptr = new byte[16];
        LittleEndian.putShort(ptr, 0,  (short)0x14);
        LittleEndian.putShort(ptr, 2,  (short)0x52);
        LittleEndian.putUInt (ptr, 4,  0x011eb2acL);
        LittleEndian.putUInt (ptr, 8,  0xFFFFFFFFL);       // offset: would wrap to -1
        LittleEndian.putUInt (ptr, 12, 0x14dL);

        assertThrows(RecordFormatException.class, () -> pf.createPointer(ptr, 0));
    }

    /**
     * Values up to {@code Integer.MAX_VALUE} (still nonsensically large but
     * representable) must continue to parse — the hardening is only meant to
     * catch the silent-narrowing case, not to introduce a new lower ceiling.
     * Downstream {@code IOUtils.safelyClone} handles real bounding.
     */
    @Test
    void testCreateV6AcceptsMaxIntOffsetAndLength() {
        PointerFactory pf = new PointerFactory(11);

        byte[] ptr = new byte[18];
        LittleEndian.putInt  (ptr, 0,  0x16);
        LittleEndian.putUInt (ptr, 4,  0x0143aff4L);
        LittleEndian.putUInt (ptr, 8,  Integer.MAX_VALUE & 0xFFFFFFFFL);
        LittleEndian.putUInt (ptr, 12, Integer.MAX_VALUE & 0xFFFFFFFFL);
        LittleEndian.putShort(ptr, 16, (short)0x46);

        Pointer p = pf.createPointer(ptr, 0);
        assertEquals(Integer.MAX_VALUE, p.getOffset());
        assertEquals(Integer.MAX_VALUE, p.getLength());
    }

    @Test
    void testCreateV6FromMid() {
        PointerFactory pf = new PointerFactory(11);

        // Create a from part way down the byte stream
        byte[] bytes = new byte[28];
        System.arraycopy(vp6_b, 0, bytes, 0, 10);
        System.arraycopy(vp6_a, 0, bytes, 10, 18);

        Pointer a = pf.createPointer(bytes, 10);
        assertEquals(0x16, a.getType());
        assertEquals(0x0143aff4, a.getAddress());
        assertEquals(0x80, a.getOffset());
        assertEquals(0x54, a.getLength());
        assertEquals(0x46, a.getFormat());

        assertTrue(a.destinationCompressed());
        assertTrue(a.destinationHasStrings());
        assertFalse(a.destinationHasChunks());
        assertFalse(a.destinationHasPointers());
    }
}
