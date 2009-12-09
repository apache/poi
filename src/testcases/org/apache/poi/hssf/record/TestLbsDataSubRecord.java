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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.HexRead;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.LittleEndianOutputStream;

/**
 * Tests the serialization and deserialization of the LbsDataSubRecord class works correctly.
 *
 * @author Yegor Kozlov
 */
public final class TestLbsDataSubRecord extends TestCase {

    /**
     * test read-write round trip
     * test data was taken from 47701.xls
     */
    public void test_47701(){
        byte[] data = HexRead.readFromString(
                        "15, 00, 12, 00, 12, 00, 02, 00, 11, 20, " +
                        "00, 00, 00, 00, 80, 3D, 03, 05, 00, 00, " +
                        "00, 00, 0C, 00, 14, 00, 00, 00, 00, 00, " +
                        "00, 00, 00, 00, 00, 00, 01, 00, 0A, 00, " +
                        "00, 00, 10, 00, 01, 00, 13, 00, EE, 1F, " +
                        "10, 00, 09, 00, 40, 9F, 74, 01, 25, 09, " +
                        "00, 0C, 00, 07, 00, 07, 00, 07, 04, 00, " +
                        "00, 00, 08, 00, 00, 00");
        RecordInputStream in = TestcaseRecordInputStream.create(ObjRecord.sid, data);
        // check read OK
        ObjRecord record = new ObjRecord(in);
        assertEquals(3, record.getSubRecords().size());
        SubRecord sr = record.getSubRecords().get(2);
        assertTrue(sr instanceof LbsDataSubRecord);
        LbsDataSubRecord lbs = (LbsDataSubRecord)sr;
        assertEquals(4, lbs.getNumberOfItems());

        assertTrue(lbs.getFormula() instanceof AreaPtg);
        AreaPtg ptg = (AreaPtg)lbs.getFormula();
        CellRangeAddress range = new CellRangeAddress(
                ptg.getFirstRow(), ptg.getLastRow(), ptg.getFirstColumn(), ptg.getLastColumn());
        assertEquals("H10:H13", range.formatAsString());

        // check that it re-serializes to the same data
        byte[] ser = record.serialize();
        TestcaseRecordInputStream.confirmRecordEncoding(ObjRecord.sid, data, ser);
    }

    /**
     * test data was taken from the file attached to Bugzilla 45778
     */
    public void test_45778(){
        byte[] data = HexRead.readFromString(
                                "15, 00, 12, 00, 14, 00, 01, 00, 01, 00, " +
                                "01, 21, 00, 00, 3C, 13, F4, 03, 00, 00, " +
                                "00, 00, 0C, 00, 14, 00, 00, 00, 00, 00, " +
                                "00, 00, 00, 00, 00, 00, 01, 00, 08, 00, 00, " +
                                "00, 10, 00, 00, 00, " +
                                 "13, 00, EE, 1F, " +
                                 "00, 00, " +
                                 "08, 00, " +  //number of items
                                 "08, 00, " + //selected item
                                 "01, 03, " + //flags
                                 "00, 00, " + //objId
                                 //LbsDropData
                                 "0A, 00, " + //flags
                                 "14, 00, " + //the number of lines to be displayed in the dropdown
                                 "6C, 00, " + //the smallest width in pixels allowed for the dropdown window
                                 "00, 00, " +  //num chars
                                 "00, 00");
        RecordInputStream in = TestcaseRecordInputStream.create(ObjRecord.sid, data);
        // check read OK
        ObjRecord record = new ObjRecord(in);

        SubRecord sr = record.getSubRecords().get(2);
        assertTrue(sr instanceof LbsDataSubRecord);
        LbsDataSubRecord lbs = (LbsDataSubRecord)sr;
        assertEquals(8, lbs.getNumberOfItems());
        assertNull(lbs.getFormula());

        // check that it re-serializes to the same data
        byte[] ser = record.serialize();
        TestcaseRecordInputStream.confirmRecordEncoding(ObjRecord.sid, data, ser);

    }

    /**
     * Test data produced by OpenOffice 3.1 by opening  and saving 47701.xls
     * There are 5 padding bytes that are removed by POI
     */
    public void test_remove_padding(){
        byte[] data = HexRead.readFromString(
                        "5D, 00, 4C, 00, " +
                        "15, 00, 12, 00, 12, 00, 01, 00, 11, 00, " +
                        "00, 00, 00, 00, 00, 00, 00, 00, 00, 00, " +
                        "00, 00, 0C, 00, 14, 00, 00, 00, 00, 00, " +
                        "00, 00, 00, 00, 00, 00, 01, 00, 09, 00, " +
                        "00, 00, 0F, 00, 01, 00, " +
                        "13, 00, 1B, 00, " +
                        "10, 00, " + //next 16 bytes is a ptg aray
                        "09, 00, 00, 00, 00, 00, 25, 09, 00, 0C, 00, 07, 00, 07, 00, 00, " +
                        "01, 00, " + //num lines
                        "00, 00, " + //selected
                         "08, 00, " +
                         "00, 00, " +
                         "00, 00, 00, 00, 00"); //padding bytes

        RecordInputStream in = TestcaseRecordInputStream.create(data);
        // check read OK
        ObjRecord record = new ObjRecord(in);
        // check that it re-serializes to the same data
        byte[] ser = record.serialize();

        assertEquals(data.length-5, ser.length);
        for(int i=0; i < ser.length; i++) assertEquals(data[i],ser[i]);

        //check we can read the trimmed record
        RecordInputStream in2 = TestcaseRecordInputStream.create(ser);
        ObjRecord record2 = new ObjRecord(in2);
        byte[] ser2 = record2.serialize();
        assertTrue(Arrays.equals(ser, ser2));
    }

    public void test_LbsDropData(){
        byte[] data = HexRead.readFromString(
                                 //LbsDropData
                                 "0A, 00, " + //flags
                                 "14, 00, " + //the number of lines to be displayed in the dropdown
                                 "6C, 00, " + //the smallest width in pixels allowed for the dropdown window
                                 "00, 00, " +  //num chars
                                 "00, " +      //compression flag
                                 "00");        //padding byte

        LittleEndianInputStream in = new LittleEndianInputStream(new ByteArrayInputStream(data));

        LbsDataSubRecord.LbsDropData lbs = new LbsDataSubRecord.LbsDropData(in);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        lbs.serialize(new LittleEndianOutputStream(baos));

        assertTrue(Arrays.equals(data, baos.toByteArray()));
    }
}
