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


import junit.framework.TestCase;

/**
 * Tests the serialization and deserialization of the SeriesTextRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *

 * @author Andrew C. Oliver (acoliver at apache.org)
 */
public final class TestSeriesTextRecord extends TestCase {
    byte[] data = new byte[] {
	(byte)0x00,(byte)0x00,(byte)0x0C,(byte)0x01,(byte)0x56,(byte)0x00,(byte)0x61,(byte)0x00,(byte)0x6C,(byte)0x00,(byte)0x75,(byte)0x00,(byte)0x65,(byte)0x00,(byte)0x20,(byte)0x00,(byte)0x4E,(byte)0x00,(byte)0x75,(byte)0x00,(byte)0x6D,(byte)0x00,(byte)0x62,(byte)0x00,(byte)0x65,(byte)0x00,(byte)0x72,(byte)0x00
    };

    public void testLoad() {
        SeriesTextRecord record = new SeriesTextRecord(new TestcaseRecordInputStream((short)0x100d, (short)data.length, data));

        assertEquals( (short)0, record.getId());
        assertEquals( (byte)0x0C, record.getTextLength());
        assertEquals( (byte)0x01, record.getUndocumented());
        assertEquals( "Value Number", record.getText());

        assertEquals( 32, record.getRecordSize() );
    }

    public void testStore()
    {
        SeriesTextRecord record = new SeriesTextRecord();

        record.setId( (short)0 );
        record.setTextLength( (byte)0x0C );
        record.setUndocumented( (byte)0x01 );
        record.setText( "Value Number" );


        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}
