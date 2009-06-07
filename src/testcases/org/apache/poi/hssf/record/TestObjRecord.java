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

import java.util.Arrays;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.util.HexRead;

/**
 * Tests the serialization and deserialization of the ObjRecord class works correctly.
 * Test data taken directly from a real Excel file.
 *
 * @author Yegor Kozlov
 */
public final class TestObjRecord extends TestCase {
    /**
     * OBJ record data containing two sub-records.
     * The data taken directly from a real Excel file.
     *
     * [OBJ]
     *     [ftCmo]
     *     [ftEnd]
     */
    private static final byte[] recdata = HexRead.readFromString(""
            + "15 00 12 00 06 00 01 00 11 60 "
            + "F4 02 41 01 14 10 1F 02 00 00 "
            +"00 00 00 00 00 00"
            // TODO - this data seems to require two extra bytes padding. not sure where original file is.
            // it's not bug 38607 attachment 17639
    );

    /**
     * Hex dump from
     * offset 0x2072 in att 22076 of bug 45133
     */
    private static final byte[] recdataNeedingPadding = HexRead.readFromString(""
            + "5D 00 20 00"
            + "15 00 12 00 00 00 01 00 11 60 00 00 00 00 38 6F CC 03 00 00 00 00 06 00 02 00 00 00 00 00 00 00"
    );

    public void testLoad() {
        ObjRecord record = new ObjRecord(TestcaseRecordInputStream.create(ObjRecord.sid, recdata));

        assertEquals(26, record.getRecordSize() - 4);

        List<SubRecord> subrecords = record.getSubRecords();
        assertEquals(2, subrecords.size() );
        assertTrue(subrecords.get(0) instanceof CommonObjectDataSubRecord);
        assertTrue(subrecords.get(1) instanceof EndSubRecord );

    }

    public void testStore() {
        ObjRecord record = new ObjRecord(TestcaseRecordInputStream.create(ObjRecord.sid, recdata));

        byte [] recordBytes = record.serialize();
        assertEquals(26, recordBytes.length - 4);
        byte[] subData = new byte[recdata.length];
        System.arraycopy(recordBytes, 4, subData, 0, subData.length);
        assertTrue(Arrays.equals(recdata, subData));
    }

    public void testConstruct() {
        ObjRecord record = new ObjRecord();
        CommonObjectDataSubRecord ftCmo = new CommonObjectDataSubRecord();
        ftCmo.setObjectType( CommonObjectDataSubRecord.OBJECT_TYPE_COMMENT);
        ftCmo.setObjectId( 1024 );
        ftCmo.setLocked( true );
        ftCmo.setPrintable( true );
        ftCmo.setAutofill( true );
        ftCmo.setAutoline( true );
        record.addSubRecord(ftCmo);
        EndSubRecord ftEnd = new EndSubRecord();
        record.addSubRecord(ftEnd);

        //serialize and read again
        byte [] recordBytes = record.serialize();
        //cut off the record header
        byte [] bytes = new byte[recordBytes.length-4];
        System.arraycopy(recordBytes, 4, bytes, 0, bytes.length);

        record = new ObjRecord(TestcaseRecordInputStream.create(ObjRecord.sid, bytes));
        List<SubRecord> subrecords = record.getSubRecords();
        assertEquals( 2, subrecords.size() );
        assertTrue( subrecords.get(0) instanceof CommonObjectDataSubRecord);
        assertTrue( subrecords.get(1) instanceof EndSubRecord );
    }
    
    public void testReadWriteWithPadding_bug45133() {
        ObjRecord record = new ObjRecord(TestcaseRecordInputStream.create(recdataNeedingPadding));
        
        if (record.getRecordSize() == 34) {
            throw new AssertionFailedError("Identified bug 45133");
        }

        assertEquals(36, record.getRecordSize());

        List<SubRecord> subrecords = record.getSubRecords();
        assertEquals(3, subrecords.size() );
        assertEquals(CommonObjectDataSubRecord.class, subrecords.get(0).getClass());
        assertEquals(GroupMarkerSubRecord.class, subrecords.get(1).getClass());
        assertEquals(EndSubRecord.class, subrecords.get(2).getClass());
    }
    
    /**
     * Check that ObjRecord tolerates and preserves padding to a 4-byte boundary
     * (normally padding is to a 2-byte boundary).
     */
    public void test4BytePadding() {
        // actual data from file saved by Excel 2007
        byte[] data = HexRead.readFromString(""
                + "15 00 12 00  1E 00 01 00  11 60 B4 6D  3C 01 C4 06 "
                + "49 06 00 00  00 00 00 00  00 00 00 00");
        // this data seems to have 2 extra bytes of padding more than usual
        // the total may have been padded to the nearest quad-byte length
        RecordInputStream in = TestcaseRecordInputStream.create(ObjRecord.sid, data);
        // check read OK
        ObjRecord record = new ObjRecord(in);
        // check that it re-serializes to the same data
        byte[] ser = record.serialize();
        TestcaseRecordInputStream.confirmRecordEncoding(ObjRecord.sid, data, ser);
    }
}
