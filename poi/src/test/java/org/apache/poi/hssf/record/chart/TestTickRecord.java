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

package org.apache.poi.hssf.record.chart;


import static org.apache.poi.hssf.record.TestcaseRecordInputStream.confirmRecordEncoding;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.junit.jupiter.api.Test;

/**
 * Tests the serialization and deserialization of the TickRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 */
final class TestTickRecord {
    private static final byte[] data = {
        (byte)0x02, (byte)0x00, (byte)0x03, (byte)0x01,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x23, (byte)0x00,
        (byte)0x4D, (byte)0x00, (byte)0x00, (byte)0x00
    };

    @Test
    void testLoad() {
        TickRecord record = new TickRecord(TestcaseRecordInputStream.create(0x101e, data));
        assertEquals( (byte)2, record.getMajorTickType());
        assertEquals( (byte)0, record.getMinorTickType());
        assertEquals( (byte)3, record.getLabelPosition());
        assertEquals( (short)1, record.getBackground());
        assertEquals( 0, record.getLabelColorRgb());
        assertEquals( (short)0, record.getZero1());
        assertEquals( (short)0, record.getZero2());
        assertEquals( (short)35, record.getOptions());
        assertTrue(record.isAutoTextColor());
        assertTrue(record.isAutoTextBackground());
        assertEquals( (short)0x0, record.getRotation() );
        assertTrue(record.isAutorotate());
        assertEquals( (short)77, record.getTickColor());
        assertEquals( (short)0x0, record.getZero3());


        assertEquals( 34, record.getRecordSize() );
    }

    @SuppressWarnings("squid:S2699")
    @Test
    void testStore() {
        TickRecord record = new TickRecord();
        record.setMajorTickType( (byte)2 );
        record.setMinorTickType( (byte)0 );
        record.setLabelPosition( (byte)3 );
        record.setBackground( (byte)1 );
        record.setLabelColorRgb( 0 );
        record.setZero1( (short)0 );
        record.setZero2( (short)0 );
        record.setOptions( (short)35 );
        record.setAutoTextColor( true );
        record.setAutoTextBackground( true );
        record.setRotation( (short)0 );
        record.setAutorotate( true );
        record.setTickColor( (short)77 );
        record.setZero3( (short)0 );


        byte [] recordBytes = record.serialize();
        confirmRecordEncoding(TickRecord.sid, data, recordBytes);
    }
}
