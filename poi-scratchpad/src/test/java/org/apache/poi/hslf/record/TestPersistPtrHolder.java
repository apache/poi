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

package org.apache.poi.hslf.record;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.poi.util.LittleEndian;
import org.junit.jupiter.api.Test;

public final class TestPersistPtrHolder {

    /**
     * Build the byte representation of a PersistPtrIncrementalBlock with a
     * single PersistDirectoryEntry pointing to {@code sheetOffset}. The
     * outer 8-byte record header is included so the buffer can be passed
     * straight to {@link PersistPtrHolder#PersistPtrHolder(byte[], int, int)}.
     */
    private static byte[] buildRecord(long sheetOffset) {
        byte[] buf = new byte[16];
        // Record header (8 bytes): version/instance, type, length
        LittleEndian.putUShort(buf, 0, 0);                  // version/instance (unused here)
        LittleEndian.putUShort(buf, 2, (int) RecordTypes.PersistPtrIncrementalBlock.typeID);
        LittleEndian.putInt(buf, 4, 8);                     // body length = info(4) + offset(4)

        // Body: info field with cntPersist=1, persistId=0, then the 32-bit offset
        LittleEndian.putInt(buf, 8, 0x00100000);            // cntPersistFld bits = 1, persistIdFld bits = 0
        LittleEndian.putUInt(buf, 12, sheetOffset);
        return buf;
    }

    /**
     * A crafted PersistPtrHolder entry whose sheet offset is &gt;
     * {@link Integer#MAX_VALUE} used to be silently narrowed via a plain
     * {@code (int)} cast at parse time. The wrapped negative value was then
     * stored in {@code _slideLocations} and later flowed into
     * {@code Record.buildRecordAtOffset(docstream, offset)} - where a small
     * negative offset can land on valid byte positions of the docstream
     * (since {@code b[offset+2]} can be a valid index when {@code offset} is
     * -1 or -2) and cause the parser to read a record from the wrong
     * location instead of failing on the malformed input. Reject these at
     * parse time via {@link Math#toIntExact(long)}, matching the recent
     * hardening of HDGF v6+ chunk Length and the PointerFactory uint32
     * fields.
     */
    @Test
    void testRejectsOversizedSheetOffset() {
        byte[] data = buildRecord(0x80000001L);
        assertThrows(ArithmeticException.class,
                () -> new PersistPtrHolder(data, 0, data.length));
    }

    /**
     * Equally important: the hardening must not introduce a new lower
     * ceiling. Offsets up to {@link Integer#MAX_VALUE} are still
     * representable as a signed int and must continue to parse - byte
     * arrays in the JVM can be that large in principle, and downstream
     * code already bounds-checks against the actual docstream length.
     */
    @Test
    void testAcceptsMaxIntSheetOffset() {
        byte[] data = buildRecord(Integer.MAX_VALUE);
        PersistPtrHolder ptr = new PersistPtrHolder(data, 0, data.length);
        Integer offset = ptr.getSlideLocationsLookup().get(0);
        assertEquals(Integer.MAX_VALUE, offset.intValue());
    }
}
