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
import org.apache.poi.util.HexRead;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.Arrays;

/**
 * Tests the serialization and deserialization of the TestEmbeddedObjectRefSubRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *
 * @author Yegor Kozlov
 */
public class TestEmbeddedObjectRefSubRecord extends TestCase {

    String data1 = "[20, 00, 05, 00, FC, 10, 76, 01, 02, 24, 14, DF, 00, 03, 10, 00, 00, 46, 6F, 72, 6D, 73, 2E, 43, 68, 65, 63, 6B, 42, 6F, 78, 2E, 31, 00, 00, 00, 00, 00, 70, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, ]";

    public void testStore() throws IOException {

        byte[] src = HexRead.readFromString(data1);
        src = TestcaseRecordInputStream.mergeDataAndSid(EmbeddedObjectRefSubRecord.sid, (short)src.length, src);

        RecordInputStream in = new RecordInputStream(new ByteArrayInputStream(src));
        in.nextRecord();

        EmbeddedObjectRefSubRecord record1 = new EmbeddedObjectRefSubRecord(in);

        byte[] ser = record1.serialize();

        RecordInputStream in2 = new RecordInputStream(new ByteArrayInputStream(ser));
        in2.nextRecord();
        EmbeddedObjectRefSubRecord record2 = new EmbeddedObjectRefSubRecord(in2);

        assertTrue(Arrays.equals(src, ser));
        assertEquals(record1.field_1_stream_id_offset, record2.field_1_stream_id_offset);
        assertTrue(Arrays.equals(record1.field_2_unknown, record2.field_2_unknown));
        assertEquals(record1.field_3_unicode_len, record2.field_3_unicode_len);
        assertEquals(record1.field_4_unicode_flag, record2.field_4_unicode_flag);
        assertEquals(record1.field_5_ole_classname, record2.field_5_ole_classname);
        assertEquals(record1.field_6_stream_id, record2.field_6_stream_id);
        assertTrue(Arrays.equals(record1.remainingBytes, record2.remainingBytes));
    }

    public void testCreate() throws IOException {


        EmbeddedObjectRefSubRecord record1 = new EmbeddedObjectRefSubRecord();

        byte[] ser = record1.serialize();
        RecordInputStream in2 = new RecordInputStream(new ByteArrayInputStream(ser));
        in2.nextRecord();
        EmbeddedObjectRefSubRecord record2 = new EmbeddedObjectRefSubRecord(in2);

        assertEquals(record1.field_1_stream_id_offset, record2.field_1_stream_id_offset);
        assertTrue(Arrays.equals(record1.field_2_unknown, record2.field_2_unknown));
        assertEquals(record1.field_3_unicode_len, record2.field_3_unicode_len);
        assertEquals(record1.field_4_unicode_flag, record2.field_4_unicode_flag);
        assertEquals(record1.field_5_ole_classname, record2.field_5_ole_classname);
        assertEquals(record1.field_6_stream_id, record2.field_6_stream_id);
        assertTrue(Arrays.equals(record1.remainingBytes, record2.remainingBytes));

    }
}
