
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

import junit.framework.*;

import java.util.Arrays;
import java.util.List;

/**
 * Tests the serialization and deserialization of the ObjRecord class works correctly.
 * Test data taken directly from a real Excel file.
 *
 * @author Yegor Kozlov
 */
public class TestObjRecord extends TestCase {
    /**
     * OBJ record data containing two sub-records.
     * The data taken directly from a real Excel file.
     *
     * [OBJ]
     *     [ftCmo]
     *     [ftEnd]
     */
    public static byte[] recdata = {
        0x15, 0x00, 0x12, 0x00, 0x06, 0x00, 0x01, 0x00, 0x11, 0x60,
        (byte)0xF4, 0x02, 0x41, 0x01, 0x14, 0x10, 0x1F, 0x02, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };


    public void testLoad() throws Exception {
        ObjRecord record = new ObjRecord(new TestcaseRecordInputStream(ObjRecord.sid, (short)recdata.length, recdata));

        assertEquals( recdata.length, record.getRecordSize() - 4);

        List subrecords = record.getSubRecords();
        assertEquals( 2, subrecords.size() );
        assertTrue( subrecords.get(0) instanceof CommonObjectDataSubRecord);
        assertTrue( subrecords.get(1) instanceof EndSubRecord );

    }

    public void testStore() {
        ObjRecord record = new ObjRecord(new TestcaseRecordInputStream(ObjRecord.sid, (short)recdata.length, recdata));

        byte [] recordBytes = record.serialize();
        assertEquals(recdata.length, recordBytes.length - 4);
        byte[] subData = new byte[recordBytes.length - 4];
        System.arraycopy(recordBytes, 4, subData, 0, subData.length);
        assertTrue(Arrays.equals(recdata, subData));
    }

    public void testConstruct() {
        ObjRecord record = new ObjRecord();
        CommonObjectDataSubRecord ftCmo = new CommonObjectDataSubRecord();
        ftCmo.setObjectType( CommonObjectDataSubRecord.OBJECT_TYPE_COMMENT);
        ftCmo.setObjectId( (short) 1024 );
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

        record = new ObjRecord(new TestcaseRecordInputStream(ObjRecord.sid, (short)bytes.length, bytes));
        List subrecords = record.getSubRecords();
        assertEquals( 2, subrecords.size() );
        assertTrue( subrecords.get(0) instanceof CommonObjectDataSubRecord);
        assertTrue( subrecords.get(1) instanceof EndSubRecord );
    }
}
