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

package org.apache.poi.hwpf.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.apache.poi.util.LittleEndian;
import org.junit.jupiter.api.Test;

public final class TestPAPX {

    private static byte[] hugePapxStub(int hugeGrpprlOffset) {
        return hugePapxStub(0x6646, hugeGrpprlOffset);  // sprmPHugePapx2
    }

    private static byte[] hugePapxStub(int sprm, int hugeGrpprlOffset) {
        final byte[] papx = new byte[8];
        LittleEndian.putShort(papx, 0, (short) 0x0000);  // istd
        LittleEndian.putUShort(papx, 2, sprm);
        LittleEndian.putInt(papx, 4, hugeGrpprlOffset);
        return papx;
    }

    /**
     * A valid sprmPHugePapx2 grpprl is loaded from the data stream: the
     * two-byte size prefix is followed by that many grpprl bytes, prefixed
     * with the istd from the stub.
     */
    @Test
    void readsValidHugeGrpprl() {
        final byte[] papx = hugePapxStub(1);
        final byte[] datastream = new byte[9];
        LittleEndian.putUShort(datastream, 1, 4);
        datastream[3] = 0x11;
        datastream[4] = 0x22;
        datastream[5] = 0x33;
        datastream[6] = 0x44;

        PAPX node = new PAPX(0, 0, papx, new ParagraphHeight(), datastream);
        assertArrayEquals(new byte[] { 0, 0, 0x11, 0x22, 0x33, 0x44 }, node.getGrpprl());
    }

    /**
     * The huge grpprl offset is a full 32-bit operand taken from the
     * document stream; a negative value must be skipped, not used to index
     * the data stream.
     */
    @Test
    void ignoresNegativeHugeGrpprlOffset() {
        final byte[] papx = hugePapxStub(-1);
        final byte[] datastream = new byte[16];

        PAPX node = new PAPX(0, 0, papx, new ParagraphHeight(), datastream);
        assertArrayEquals(papx, node.getGrpprl());
    }

    /**
     * The size prefix is PrcData.cbGrpprl, a signed 16-bit value. A negative
     * size must be skipped rather than used to size the grpprl allocation.
     */
    @Test
    void ignoresNegativeGrpprlSizePrefix() {
        final byte[] papx = hugePapxStub(0);
        final byte[] datastream = new byte[16];
        LittleEndian.putUShort(datastream, 0, 0xFFFC);

        PAPX node = new PAPX(0, 0, papx, new ParagraphHeight(), datastream);
        assertArrayEquals(papx, node.getGrpprl());
    }

    /**
     * A negative size prefix is skipped even when the data stream is large
     * enough to hold that many bytes if the prefix were read as unsigned.
     */
    @Test
    void ignoresNegativeGrpprlSizePrefixInLargeDataStream() {
        final byte[] papx = hugePapxStub(0);
        final byte[] datastream = new byte[0x10002];
        LittleEndian.putUShort(datastream, 0, 0x8000);

        PAPX node = new PAPX(0, 0, papx, new ParagraphHeight(), datastream);
        assertArrayEquals(papx, node.getGrpprl());
    }

    /**
     * An offset of Integer.MAX_VALUE overflowed the old offset + 1 < length
     * check and was then used to index the data stream.
     */
    @Test
    void ignoresMaxIntHugeGrpprlOffset() {
        final byte[] papx = hugePapxStub(Integer.MAX_VALUE);
        final byte[] datastream = new byte[16];

        PAPX node = new PAPX(0, 0, papx, new ParagraphHeight(), datastream);
        assertArrayEquals(papx, node.getGrpprl());
    }

    /**
     * An offset at or past the data stream end leaves no room for the
     * two-byte size prefix.
     */
    @Test
    void ignoresHugeGrpprlOffsetWithoutRoomForSizePrefix() {
        final byte[] datastream = new byte[16];
        for (int offset : new int[] { datastream.length - 1, datastream.length, datastream.length + 1 }) {
            final byte[] papx = hugePapxStub(offset);
            PAPX node = new PAPX(0, 0, papx, new ParagraphHeight(), datastream);
            assertArrayEquals(papx, node.getGrpprl(), "offset " + offset);
        }
    }

    /**
     * A size prefix in the last two bytes of the data stream with a size of
     * zero is valid and yields a grpprl holding only the istd.
     */
    @Test
    void readsEmptyHugeGrpprlAtEndOfDataStream() {
        final byte[] datastream = new byte[8];
        final byte[] papx = hugePapxStub(datastream.length - 2);
        LittleEndian.putShort(papx, 0, (short) 0x0102);  // istd

        PAPX node = new PAPX(0, 0, papx, new ParagraphHeight(), datastream);
        assertArrayEquals(new byte[] { 0x02, 0x01 }, node.getGrpprl());
    }

    /**
     * A grpprl that runs exactly to the last byte of the data stream is
     * accepted.
     */
    @Test
    void readsHugeGrpprlEndingAtLastByte() {
        final byte[] papx = hugePapxStub(2);
        final byte[] datastream = new byte[7];
        LittleEndian.putUShort(datastream, 2, 3);
        datastream[4] = 0x11;
        datastream[5] = 0x22;
        datastream[6] = 0x33;

        PAPX node = new PAPX(0, 0, papx, new ParagraphHeight(), datastream);
        assertArrayEquals(new byte[] { 0, 0, 0x11, 0x22, 0x33 }, node.getGrpprl());
    }

    /**
     * The older sprmPHugePapx (0x6645) is resolved the same way as
     * sprmPHugePapx2.
     */
    @Test
    void readsHugeGrpprlForSprmPHugePapx() {
        final byte[] papx = hugePapxStub(0x6645, 0);
        final byte[] datastream = new byte[4];
        LittleEndian.putUShort(datastream, 0, 2);
        datastream[2] = 0x11;
        datastream[3] = 0x22;

        PAPX node = new PAPX(0, 0, papx, new ParagraphHeight(), datastream);
        assertArrayEquals(new byte[] { 0, 0, 0x11, 0x22 }, node.getGrpprl());
    }

    /**
     * Without a data stream the stub is kept as the grpprl.
     */
    @Test
    void keepsStubWithoutDataStream() {
        final byte[] papx = hugePapxStub(0);

        PAPX node = new PAPX(0, 0, papx, new ParagraphHeight(), null);
        assertArrayEquals(papx, node.getGrpprl());
    }

    /**
     * The grpprl bytes start two bytes after the size prefix, so a size of
     * datastream.length - 1 passed the old offset + size < length check but
     * made the copy read one byte past the end of the data stream.
     */
    @Test
    void ignoresGrpprlSizeExceedingDataStream() {
        final byte[] papx = hugePapxStub(0);
        final byte[] datastream = new byte[12];
        LittleEndian.putUShort(datastream, 0, datastream.length - 1);

        PAPX node = new PAPX(0, 0, papx, new ParagraphHeight(), datastream);
        assertArrayEquals(papx, node.getGrpprl());
    }
}
