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

package org.apache.poi.hssf.record;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests the serialization and deserialization of the NoteRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 */
final class TestNoteStructureSubRecord {
    private final byte[] data = new byte[] {
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0x80, 0x00, 0x00, 0x00,
        0x00, 0x00, (byte)0xBF, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0x81, 0x01,
        (byte)0xCC, (byte)0xEC
    };

    @Test
    void testRead() {
        NoteStructureSubRecord record = new NoteStructureSubRecord(TestcaseRecordInputStream.create(NoteStructureSubRecord.sid, data), data.length);

        assertEquals(NoteStructureSubRecord.sid, record.getSid());
        assertEquals(data.length, record.getDataSize());
    }

    @Test
    void testWrite() {
        NoteStructureSubRecord record = new NoteStructureSubRecord();
        assertEquals(NoteStructureSubRecord.sid, record.getSid());
        assertEquals(data.length, record.getDataSize());

        byte [] ser = record.serialize();
        assertEquals(ser.length - 4, data.length);
    }

    @Test
    void testClone() {
        NoteStructureSubRecord record = new NoteStructureSubRecord();
        byte[] src = record.serialize();

        NoteStructureSubRecord cloned = record.copy();
        byte[] cln = cloned.serialize();

        assertEquals(record.getDataSize(), cloned.getDataSize());
        assertArrayEquals(src, cln);
    }
}
