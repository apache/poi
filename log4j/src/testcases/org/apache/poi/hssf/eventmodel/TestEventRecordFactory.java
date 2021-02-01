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

package org.apache.poi.hssf.eventmodel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordFactory;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.apache.poi.hssf.record.UnknownRecord;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * enclosing_type describe the purpose here
 */
final class TestEventRecordFactory {
    private static final byte[][] CONTINUE_DATA = {
        // an unknown record with 0 length
        {0, -1, 0, 0,},
        // a continuation record with 3 bytes of data
        {0x3C, 0, 3, 0, 1, 2, 3,},
        // one more continuation record with 1 byte of data
        {0x3C, 0, 1, 0, 4}
    };

    /**
     * tests that the records can be processed and properly return values.
     */
    @Test
    void testProcessRecords() {
        final boolean[] wascalled = { false }; // hack to pass boolean by ref into inner class

        ERFListener listener = rec -> {
            wascalled[0] = true;
            assertEquals(rec.getSid(), BOFRecord.sid, "must be BOFRecord got SID=" + rec.getSid());
            return true;
        };
    	EventRecordFactory factory = new EventRecordFactory(listener, new short[] {BOFRecord.sid});

        BOFRecord bof = new BOFRecord();
        bof.setBuild((short)0);
        bof.setBuildYear((short)1999);
        bof.setRequiredVersion(123);
        bof.setType(BOFRecord.TYPE_WORKBOOK);
        bof.setVersion((short)0x06);
        bof.setHistoryBitMask(BOFRecord.HISTORY_MASK);

        EOFRecord eof = EOFRecord.instance;
    	byte[] bytes = new byte[bof.getRecordSize() + eof.getRecordSize()];
        int offset = 0;
        offset = bof.serialize(offset,bytes);
        eof.serialize(offset,bytes);

        factory.processRecords(new ByteArrayInputStream(bytes));
        assertTrue(wascalled[0], "The record listener must be called");
    }


    /**
     * tests that the create record function returns a properly
     * constructed record in the simple case.
     */
    @Test
    void testCreateRecord() {
        BOFRecord bof = new BOFRecord();
        bof.setBuild((short)0);
        bof.setBuildYear((short)1999);
        bof.setRequiredVersion(123);
        bof.setType(BOFRecord.TYPE_WORKBOOK);
        bof.setVersion((short)0x06);
        bof.setHistoryBitMask(BOFRecord.HISTORY_MASK);

        byte[] bytes = bof.serialize();

        Record[] records = RecordFactory.createRecord(TestcaseRecordInputStream.create(bytes));

        assertEquals(1, records.length, "record.length must be 1, was =" + records.length);

        byte[] rec1 = bof.serialize();
        byte[] rec2 = records[0].serialize();
        assertArrayEquals(rec1, rec2);
    }

    /*
     * tests that the create record function returns a properly
     * constructed record in the case of a continued record.
     * TODO - need a real world example to put in a unit test
     */
    // @NotImplemented
    // @Test
    // @Disabled
    // void testCreateContinuedRecord() {
    // }


    /**
     * TEST NAME:  Test Creating ContinueRecords After Unknown Records From An InputStream <P>
     * OBJECTIVE:  Test that the RecordFactory given an InputStream
     *             constructs the expected records.<P>
     * SUCCESS:    Record factory creates the expected records.<P>
     * FAILURE:    The wrong records are created or contain the wrong values <P>
     *
     */
    @Test
     void testContinuedUnknownRecord() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (byte[] b : CONTINUE_DATA) {
            bos.write(b);
        }
        continueHelper(new ByteArrayInputStream(bos.toByteArray()));
    }

    @Test
    @Disabled("same as testContinuedUnknownRecord but with SequenceInputStream which causes the available() bug 59893")
    void bug59893() {
        Iterator<ByteArrayInputStream> iter = Stream.of(CONTINUE_DATA).map(ByteArrayInputStream::new).iterator();
        SequenceInputStream sis = new SequenceInputStream(IteratorUtils.asEnumeration(iter));
        continueHelper(sis);
    }

    private void continueHelper(InputStream data) {
        Iterator<Class<? extends StandardRecord>> expectedType =
                Stream.of(UnknownRecord.class, ContinueRecord.class, ContinueRecord.class).iterator();
        Iterator<byte[]> expectedData = Stream.of(CONTINUE_DATA).iterator();

        ERFListener listener = rec -> {
            assertEquals(expectedType.next(), rec.getClass());
            assertArrayEquals(expectedData.next(), rec.serialize());
            return true;
        };
        EventRecordFactory factory = new EventRecordFactory(listener, new short[] {-256, 0x3C});
        factory.processRecords(data);
        assertFalse(expectedData.hasNext(), "left over input data");
    }
}
