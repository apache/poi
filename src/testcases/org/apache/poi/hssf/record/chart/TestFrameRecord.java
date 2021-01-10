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
 * Tests the serialization and deserialization of the FrameRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 */
final class TestFrameRecord {
    byte[] data = new byte[] {
        (byte)0x00,(byte)0x00,      // border type
        (byte)0x02,(byte)0x00       // options
    };

    @Test
    void testLoad() {
        FrameRecord record = new FrameRecord(TestcaseRecordInputStream.create(0x1032, data));
        assertEquals( FrameRecord.BORDER_TYPE_REGULAR, record.getBorderType());
        assertEquals( 2, record.getOptions());
        assertFalse(record.isAutoSize());
        assertTrue(record.isAutoPosition());

        assertEquals( 8, record.getRecordSize() );
    }

    @SuppressWarnings("squid:S2699")
    @Test
    void testStore() {
        FrameRecord record = new FrameRecord();
        record.setBorderType( FrameRecord.BORDER_TYPE_REGULAR );
        record.setOptions( (short)2 );
        record.setAutoSize( false );
        record.setAutoPosition( true );

        byte [] recordBytes = record.serialize();
        confirmRecordEncoding(FrameRecord.sid, data, recordBytes);
    }
}
