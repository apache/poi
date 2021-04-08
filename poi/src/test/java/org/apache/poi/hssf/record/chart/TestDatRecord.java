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
 * Tests the serialization and deserialization of the DatRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 */
final class TestDatRecord {
    byte[] data = new byte[] {
        (byte)0x0D,(byte)0x00   // options
    };

    @Test
    void testLoad() {
        DatRecord record = new DatRecord(TestcaseRecordInputStream.create(0x1063, data));
        assertEquals( 0xD, record.getOptions());
        assertTrue(record.isHorizontalBorder());
        assertFalse(record.isVerticalBorder());
        assertTrue(record.isBorder());
        assertTrue(record.isShowSeriesKey());

        assertEquals( 6, record.getRecordSize() );
    }

    @SuppressWarnings("squid:S2699")
    @Test
    void testStore() {
        DatRecord record = new DatRecord();
        record.setHorizontalBorder( true );
        record.setVerticalBorder( false );
        record.setBorder( true );
        record.setShowSeriesKey( true );

        byte [] recordBytes = record.serialize();
        confirmRecordEncoding(DatRecord.sid, data, recordBytes);
    }
}
