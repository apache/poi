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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.junit.jupiter.api.Test;

/**
 * Tests the serialization and deserialization of the ValueRangeRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 */
final class TestValueRangeRecord {
    byte[] data = new byte[] {
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,     // min axis value
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,     // max axis value
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,     // major increment
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,     // minor increment
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,     // cross over
        (byte)0x1F,(byte)0x01                                    // options
    };

    @Test
    void testLoad() {

        ValueRangeRecord record = new ValueRangeRecord(TestcaseRecordInputStream.create(0x101f, data));
        assertEquals( 0.0, record.getMinimumAxisValue(), 0.001);
        assertEquals( 0.0, record.getMaximumAxisValue(), 0.001);
        assertEquals( 0.0, record.getMajorIncrement(), 0.001);
        assertEquals( 0.0, record.getMinorIncrement(), 0.001);
        assertEquals( 0.0, record.getCategoryAxisCross(), 0.001);
        assertEquals( 0x011f, record.getOptions());
        assertTrue(record.isAutomaticMinimum());
        assertTrue(record.isAutomaticMaximum());
        assertTrue(record.isAutomaticMajor());
        assertTrue(record.isAutomaticMinor());
        assertTrue(record.isAutomaticCategoryCrossing());
        assertFalse(record.isLogarithmicScale());
        assertFalse(record.isValuesInReverse());
        assertFalse(record.isCrossCategoryAxisAtMaximum());
        assertTrue(record.isReserved());

        assertEquals( 42+4, record.getRecordSize() );
    }

    @SuppressWarnings("squid:S2699")
    @Test
    void testStore() {
        ValueRangeRecord record = new ValueRangeRecord();
        record.setMinimumAxisValue( 0 );
        record.setMaximumAxisValue( 0 );
        record.setMajorIncrement( 0 );
        record.setMinorIncrement( 0 );
        record.setCategoryAxisCross( 0 );
        record.setAutomaticMinimum( true );
        record.setAutomaticMaximum( true );
        record.setAutomaticMajor( true );
        record.setAutomaticMinor( true );
        record.setAutomaticCategoryCrossing( true );
        record.setLogarithmicScale( false );
        record.setValuesInReverse( false );
        record.setCrossCategoryAxisAtMaximum( false );
        record.setReserved( true );

        byte [] recordBytes = record.serialize();
        confirmRecordEncoding(ValueRangeRecord.sid, data, recordBytes);
    }
}
