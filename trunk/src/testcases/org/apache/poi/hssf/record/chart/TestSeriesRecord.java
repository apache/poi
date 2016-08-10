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
 * Tests the serialization and deserialization of the SeriesRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestSeriesRecord extends TestCase {
    byte[] data = new byte[] {
        (byte)0x01,(byte)0x00,      // category data type
        (byte)0x01,(byte)0x00,      // values data type
        (byte)0x1B,(byte)0x00,      // num categories
        (byte)0x1B,(byte)0x00,      // num values
        (byte)0x01,(byte)0x00,      // bubble series type
        (byte)0x00,(byte)0x00       // num bubble values
    };

    public void testLoad() {

        SeriesRecord record = new SeriesRecord(TestcaseRecordInputStream.create(0x1003, data));
        assertEquals( SeriesRecord.CATEGORY_DATA_TYPE_NUMERIC, record.getCategoryDataType());
        assertEquals( SeriesRecord.VALUES_DATA_TYPE_NUMERIC, record.getValuesDataType());
        assertEquals( 27, record.getNumCategories());
        assertEquals( 27, record.getNumValues());
        assertEquals( SeriesRecord.BUBBLE_SERIES_TYPE_NUMERIC, record.getBubbleSeriesType());
        assertEquals( 0, record.getNumBubbleValues());


        assertEquals( 16, record.getRecordSize() );
    }

    public void testStore()
    {
        SeriesRecord record = new SeriesRecord();
        record.setCategoryDataType( SeriesRecord.CATEGORY_DATA_TYPE_NUMERIC );
        record.setValuesDataType( SeriesRecord.VALUES_DATA_TYPE_NUMERIC );
        record.setNumCategories( (short)27 );
        record.setNumValues( (short)27 );
        record.setBubbleSeriesType( SeriesRecord.BUBBLE_SERIES_TYPE_NUMERIC );
        record.setNumBubbleValues( (short)0 );

        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}
