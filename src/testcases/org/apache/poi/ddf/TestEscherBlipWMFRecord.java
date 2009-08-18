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

package org.apache.poi.ddf;

import junit.framework.TestCase;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;

public final class TestEscherBlipWMFRecord extends TestCase {
    private String dataStr;
    private byte[] data;

    protected void setUp() {
        dataStr = "2C 15 18 F0 34 00 00 00 01 01 01 01 01 01 01 01 " +
                        "01 01 01 01 01 01 01 01 06 00 00 00 03 00 00 00 " +
                        "01 00 00 00 04 00 00 00 02 00 00 00 0A 00 00 00 " +
                        "0B 00 00 00 05 00 00 00 08 07 01 02";
        data = HexRead.readFromString(dataStr);
    }

    public void testSerialize() {
        EscherBlipWMFRecord r = new EscherBlipWMFRecord();
        r.setBoundaryLeft(1);
        r.setBoundaryHeight(2);
        r.setBoundaryTop(3);
        r.setBoundaryWidth(4);
        r.setCacheOfSavedSize(5);
        r.setCacheOfSize(6);
        r.setFilter((byte)7);
        r.setCompressionFlag((byte)8);
        r.setSecondaryUID(new byte[] { (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01,
                                       (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01,
                                       (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01,
                                       (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01,  });
        r.setWidth(10);
        r.setHeight(11);
        r.setRecordId(EscherBlipWMFRecord.RECORD_ID_START);
        r.setOptions((short)5420);
        r.setData(new byte[] { (byte)0x01, (byte)0x02 } );

        byte[] buf = new byte[r.getRecordSize()];
        r.serialize(0, buf, new NullEscherSerializationListener() );

        assertEquals("[2C, 15, 18, F0, 26, 00, 00, 00, " +
                "01, 01, 01, 01, 01, 01, 01, 01, 01, 01, 01, 01, 01, 01, 01, 01, " +
                "06, 00, 00, 00, " +    // field_2_cacheOfSize
                "03, 00, 00, 00, " +    // field_3_boundaryTop
                "01, 00, 00, 00, " +    // field_4_boundaryLeft
                "04, 00, 00, 00, " +    // field_5_boundaryWidth
                "02, 00, 00, 00, " +    // field_6_boundaryHeight
                "0A, 00, 00, 00, " +    // field_7_x
                "0B, 00, 00, 00, " +    // field_8_y
                "05, 00, 00, 00, " +    // field_9_cacheOfSavedSize
                "08, " +                // field_10_compressionFlag
                "07, " +                // field_11_filter
                "01, 02]",            // field_12_data
                HexDump.toHex(buf));
        assertEquals(60, r.getRecordSize() );

    }

    public void testFillFields() {
        EscherBlipWMFRecord r = new EscherBlipWMFRecord();
        r.fillFields( data, 0, new DefaultEscherRecordFactory());

        assertEquals( EscherBlipWMFRecord.RECORD_ID_START, r.getRecordId() );
        assertEquals( 1, r.getBoundaryLeft() );
        assertEquals( 2, r.getBoundaryHeight() );
        assertEquals( 3, r.getBoundaryTop() );
        assertEquals( 4, r.getBoundaryWidth() );
        assertEquals( 5, r.getCacheOfSavedSize() );
        assertEquals( 6, r.getCacheOfSize() );
        assertEquals( 7, r.getFilter() );
        assertEquals( 8, r.getCompressionFlag() );
        assertEquals( "[01, 01, 01, 01, 01, 01, 01, 01, 01, 01, 01, 01, 01, 01, 01, 01]", HexDump.toHex(r.getSecondaryUID() ) );
        assertEquals( 10, r.getWidth() );
        assertEquals( 11, r.getHeight() );
        assertEquals( (short)5420, r.getOptions() );
        assertEquals( "[01, 02]", HexDump.toHex( r.getData() ) );
    }

    public void testToString() {
        EscherBlipWMFRecord r = new EscherBlipWMFRecord();
        r.fillFields( data, 0, new DefaultEscherRecordFactory() );

        String nl = System.getProperty("line.separator");

        assertEquals( "org.apache.poi.ddf.EscherBlipWMFRecord:" + nl +
                "  RecordId: 0xF018" + nl +
                "  Options: 0x152C" + nl +
                "  Secondary UID: [01, 01, 01, 01, 01, 01, 01, 01, 01, 01, 01, 01, 01, 01, 01, 01]" + nl +
                "  CacheOfSize: 6" + nl +
                "  BoundaryTop: 3" + nl +
                "  BoundaryLeft: 1" + nl +
                "  BoundaryWidth: 4" + nl +
                "  BoundaryHeight: 2" + nl +
                "  X: 10" + nl +
                "  Y: 11" + nl +
                "  CacheOfSavedSize: 5" + nl +
                "  CompressionFlag: 8" + nl +
                "  Filter: 7" + nl +
                "  Data:" + nl +
                "00000000 01 02                                           .." + nl
                , r.toString() );
    }
}

