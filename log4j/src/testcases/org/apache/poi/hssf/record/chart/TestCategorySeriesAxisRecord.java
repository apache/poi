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
 * Tests the serialization and deserialization of the CategorySeriesAxisRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 */
final class TestCategorySeriesAxisRecord {
    byte[] data = new byte[] {
        (byte)0x01,(byte)0x00,    // crossing point
        (byte)0x01,(byte)0x00,    // label frequency
        (byte)0x01,(byte)0x00,    // tick mark frequency
        (byte)0x01,(byte)0x00     // options
    };

    @Test
    void testLoad() {
        CategorySeriesAxisRecord record = new CategorySeriesAxisRecord(TestcaseRecordInputStream.create(0x1020, data));
        assertEquals( 1, record.getCrossingPoint());
        assertEquals( 1, record.getLabelFrequency());
        assertEquals( 1, record.getTickMarkFrequency());
        assertEquals( 1, record.getOptions());
        assertTrue(record.isValueAxisCrossing());
        assertFalse(record.isCrossesFarRight());
        assertFalse(record.isReversed());

        assertEquals( 4 + 8, record.getRecordSize() );
    }

    @SuppressWarnings("squid:S2699")
    @Test
    void testStore() {
        CategorySeriesAxisRecord record = new CategorySeriesAxisRecord();
        record.setCrossingPoint( (short)1 );
        record.setLabelFrequency( (short)1 );
        record.setTickMarkFrequency( (short)1 );
        record.setValueAxisCrossing( true );
        record.setCrossesFarRight( false );
        record.setReversed( false );


        byte [] recordBytes = record.serialize();
        confirmRecordEncoding(CategorySeriesAxisRecord.sid, data, recordBytes);
    }
}
