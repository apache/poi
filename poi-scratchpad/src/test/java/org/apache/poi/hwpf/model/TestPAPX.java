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
        final byte[] papx = new byte[8];
        LittleEndian.putShort(papx, 0, (short) 0x0000);  // istd
        LittleEndian.putUShort(papx, 2, 0x6646);         // sprmPHugePapx2
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
     * The two-byte size prefix is an unsigned count. Read as a signed short,
     * 0xFFFC goes negative, slips past the bounds check and the grpprl
     * allocation fails with NegativeArraySizeException.
     */
    @Test
    void ignoresOversizedGrpprlSizePrefix() {
        final byte[] papx = hugePapxStub(0);
        final byte[] datastream = new byte[16];
        LittleEndian.putUShort(datastream, 0, 0xFFFC);

        PAPX node = new PAPX(0, 0, papx, new ParagraphHeight(), datastream);
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
