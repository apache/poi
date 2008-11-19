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
 * Tests the serialization and deserialization of the SeriesIndexRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *

 * @author Andrew C. Oliver (acoliver at apache.org)
 */
public final class TestSeriesIndexRecord extends TestCase {
    byte[] data = new byte[] {
	(byte)0x03,(byte)0x00
    };

    public void testLoad() {
        SeriesIndexRecord record = new SeriesIndexRecord(TestcaseRecordInputStream.create(0x1065, data));
        
        assertEquals( (short)3, record.getIndex());
        assertEquals( 6, record.getRecordSize() );
    }

    public void testStore()
    {
        SeriesIndexRecord record = new SeriesIndexRecord();



        record.setIndex( (short)3 );


        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}
