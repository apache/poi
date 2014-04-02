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
 * Tests the serialization and deserialization of the FontBasisRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *

 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestFontBasisRecord extends TestCase {
    byte[] data = new byte[] {
        (byte)0x28,(byte)0x1A,   // x basis
        (byte)0x9C,(byte)0x0F,   // y basis
        (byte)0xC8,(byte)0x00,   // height basis
        (byte)0x00,(byte)0x00,   // scale
        (byte)0x05,(byte)0x00    // index to font table
    };

    public void testLoad() {

        FontBasisRecord record = new FontBasisRecord(TestcaseRecordInputStream.create(0x1060, data));
        assertEquals( 0x1a28, record.getXBasis());
        assertEquals( 0x0f9c, record.getYBasis());
        assertEquals( 0xc8, record.getHeightBasis());
        assertEquals( 0x00, record.getScale());
        assertEquals( 0x05, record.getIndexToFontTable());

        assertEquals( 14, record.getRecordSize() );
    }

    public void testStore()
    {
        FontBasisRecord record = new FontBasisRecord();
        record.setXBasis( (short)0x1a28 );
        record.setYBasis( (short)0x0f9c );
        record.setHeightBasis( (short)0xc8 );
        record.setScale( (short)0x00 );
        record.setIndexToFontTable( (short)0x05 );

        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}
