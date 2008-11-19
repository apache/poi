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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import java.util.Arrays;

import org.apache.poi.util.HexRead;

/**
 * Tests the serialization and deserialization of the NoteRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *
 * @author Yegor Kozlov
 */
public final class TestNoteRecord extends TestCase {
    private byte[] testData = HexRead.readFromString(
            "06 00 01 00 02 00 02 04 " +
            "1A 00 00 " +
            "41 70 61 63 68 65 20 53 6F 66 74 77 61 72 65 20 46 6F 75 6E 64 61 74 69 6F 6E " +
            "00" // padding byte
            );

    public void testRead() {

        NoteRecord record = new NoteRecord(TestcaseRecordInputStream.create(NoteRecord.sid, testData));

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

        byte[] ser = record.serialize();
        TestcaseRecordInputStream.confirmRecordEncoding(NoteRecord.sid, testData, ser);
    }

    public void testClone() {
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
    
    public void testUnicodeAuthor() {
        // This sample data was created by setting the 'user name' field in the 'Personalize' 
        // section of Excel's options to \u30A2\u30D1\u30C3\u30C1\u65CF, and then 
        // creating a cell comment.
        byte[] data = HexRead.readFromString("01 00 01 00 00 00 03 00 " +
                "05 00 01 " + // len=5, 16bit
                "A2 30 D1 30 C3 30 C1 30 CF 65 " + // character data 
                "00 " // padding byte
                );
        RecordInputStream in = TestcaseRecordInputStream.create(NoteRecord.sid, data);
        NoteRecord nr = new NoteRecord(in);
        if ("\u00A2\u0030\u00D1\u0030\u00C3".equals(nr.getAuthor())) {
            throw new AssertionFailedError("Identified bug in reading note with unicode author");
        }
        assertEquals("\u30A2\u30D1\u30C3\u30C1\u65CF", nr.getAuthor());
        
        byte[] ser = nr.serialize();
        TestcaseRecordInputStream.confirmRecordEncoding(NoteRecord.sid, data, ser);
    }
}
