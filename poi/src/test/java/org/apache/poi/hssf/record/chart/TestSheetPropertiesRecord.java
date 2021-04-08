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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SheetPropertiesRecord}
 * Test data taken directly from a real Excel file.
 */
final class TestSheetPropertiesRecord {
    private static final byte[] data = {
        (byte)0x0A,(byte)0x00,
        (byte)0x00,
        (byte)0x00,      // not sure where that last byte comes from
    };

    @Test
    void testLoad() {
        RecordInputStream in = TestcaseRecordInputStream.create(0x1044, data);
        SheetPropertiesRecord record = new SheetPropertiesRecord(in);
        assertNotEquals(1, in.remaining(), "Identified bug 44693c");
        assertEquals(0, in.remaining());
        assertEquals( 10, record.getFlags());
        assertFalse(record.isChartTypeManuallyFormatted());
        assertTrue(record.isPlotVisibleOnly());
        assertFalse(record.isDoNotSizeWithWindow());
        assertTrue(record.isDefaultPlotDimensions());
        assertFalse(record.isAutoPlotArea());
        assertEquals( 0, record.getEmpty());

        assertEquals( 8, record.getRecordSize() );
    }

    @SuppressWarnings("squid:S2699")
    @Test
    void testStore() {
        SheetPropertiesRecord record = new SheetPropertiesRecord();
        record.setChartTypeManuallyFormatted( false );
        record.setPlotVisibleOnly( true );
        record.setDoNotSizeWithWindow( false );
        record.setDefaultPlotDimensions( true );
        record.setAutoPlotArea( false );
        record.setEmpty( (byte)0 );


        byte [] recordBytes = record.serialize();
        confirmRecordEncoding(SheetPropertiesRecord.sid, data, recordBytes);
    }
}
