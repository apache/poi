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


import junit.framework.TestCase;

import java.util.Arrays;

/**
 * Tests the serialization and deserialization of the NoteRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *
 * @author Yegor Kozlov
 */
public final class TestNoteRecord extends TestCase {
    private byte[] data = new byte[] {
        0x06, 0x00, 0x01, 0x00, 0x02, 0x00, 0x02, 0x04, 0x1A, 0x00,
        0x00, 0x41, 0x70, 0x61, 0x63, 0x68, 0x65, 0x20, 0x53, 0x6F,
        0x66, 0x74, 0x77, 0x61, 0x72, 0x65, 0x20, 0x46, 0x6F, 0x75,
        0x6E, 0x64, 0x61, 0x74, 0x69, 0x6F, 0x6E, 0x00
    };

    public void testRead() {

        NoteRecord record = new NoteRecord(TestcaseRecordInputStream.create(NoteRecord.sid, data));

        assertEquals(NoteRecord.sid, record.getSid());
        assertEquals(6, record.getRow());
        assertEquals(1, record.getColumn());
        assertEquals(NoteRecord.NOTE_VISIBLE, record.getFlags());
        assertEquals(1026, record.getShapeId());
        assertEquals("Apache Software Foundation", record.getAuthor());

    }

    public void testWrite() {
        NoteRecord record = new NoteRecord();
        assertEquals(NoteRecord.sid, record.getSid());
        
        record.setRow((short)6);
        record.setColumn((short)1);
        record.setFlags(NoteRecord.NOTE_VISIBLE);
        record.setShapeId((short)1026);
        record.setAuthor("Apache Software Foundation");

        byte [] ser = record.serialize();
        assertEquals(ser.length - 4, data.length);

        byte[] recdata = new byte[ser.length - 4];
        System.arraycopy(ser, 4, recdata, 0, recdata.length);
        assertTrue(Arrays.equals(data, recdata));
    }

    public void testClone()
    {
        NoteRecord record = new NoteRecord();

        record.setRow((short)1);
        record.setColumn((short)2);
        record.setFlags(NoteRecord.NOTE_VISIBLE);
        record.setShapeId((short)1026);
        record.setAuthor("Apache Software Foundation");

        NoteRecord cloned = (NoteRecord)record.clone();
        assertEquals(record.getRow(), cloned.getRow());
        assertEquals(record.getColumn(), cloned.getColumn());
        assertEquals(record.getFlags(), cloned.getFlags());
        assertEquals(record.getShapeId(), cloned.getShapeId());
        assertEquals(record.getAuthor(), cloned.getAuthor());

        //finally check that the serialized data is the same
        byte[] src = record.serialize();
        byte[] cln = cloned.serialize();
        assertTrue(Arrays.equals(src, cln));
    }
}
