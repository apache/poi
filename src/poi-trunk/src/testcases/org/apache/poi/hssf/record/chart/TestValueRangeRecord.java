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


import org.apache.poi.hssf.record.TestcaseRecordInputStream;

import junit.framework.TestCase;

/**
 * Tests the serialization and deserialization of the ValueRangeRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestValueRangeRecord extends TestCase {
    byte[] data = new byte[] {
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,     // min axis value
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,     // max axis value
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,     // major increment
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,     // minor increment
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,     // cross over
        (byte)0x1F,(byte)0x01                                    // options
    };

    public void testLoad() {

        ValueRangeRecord record = new ValueRangeRecord(TestcaseRecordInputStream.create(0x101f, data));
        assertEquals( 0.0, record.getMinimumAxisValue(), 0.001);
        assertEquals( 0.0, record.getMaximumAxisValue(), 0.001);
        assertEquals( 0.0, record.getMajorIncrement(), 0.001);
        assertEquals( 0.0, record.getMinorIncrement(), 0.001);
        assertEquals( 0.0, record.getCategoryAxisCross(), 0.001);
        assertEquals( 0x011f, record.getOptions());
        assertEquals( true, record.isAutomaticMinimum() );
        assertEquals( true, record.isAutomaticMaximum() );
        assertEquals( true, record.isAutomaticMajor() );
        assertEquals( true, record.isAutomaticMinor() );
        assertEquals( true, record.isAutomaticCategoryCrossing() );
        assertEquals( false, record.isLogarithmicScale() );
        assertEquals( false, record.isValuesInReverse() );
        assertEquals( false, record.isCrossCategoryAxisAtMaximum() );
        assertEquals( true, record.isReserved() );

        assertEquals( 42+4, record.getRecordSize() );
    }

    public void testStore()
    {
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
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}
