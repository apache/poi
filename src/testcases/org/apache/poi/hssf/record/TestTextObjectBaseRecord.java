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
 * Tests the serialization and deserialization of the TextObjectBaseRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestTextObjectBaseRecord extends TestCase {
    byte[] data = new byte[] {
	    0x44, 0x02, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00,
	    0x00, 0x00, 0x02, 0x00, 0x02, 0x00, 0x00, 0x00,
        0x00, 0x00,
    };

    public void testLoad() {
        TextObjectBaseRecord record = new TextObjectBaseRecord(new TestcaseRecordInputStream((short)0x1B6, (short)data.length, data));


//        assertEquals( (short), record.getOptions());
        assertEquals( false, record.isReserved1() );
        assertEquals( TextObjectBaseRecord.HORIZONTAL_TEXT_ALIGNMENT_CENTERED, record.getHorizontalTextAlignment() );
        assertEquals( TextObjectBaseRecord.VERTICAL_TEXT_ALIGNMENT_JUSTIFY, record.getVerticalTextAlignment() );
        assertEquals( 0, record.getReserved2() );
        assertEquals( true, record.isTextLocked() );
        assertEquals( 0, record.getReserved3() );
        assertEquals( TextObjectBaseRecord.TEXT_ORIENTATION_ROT_RIGHT, record.getTextOrientation());
        assertEquals( 0, record.getReserved4());
        assertEquals( 0, record.getReserved5());
        assertEquals( 0, record.getReserved6());
        assertEquals( 2, record.getTextLength());
        assertEquals( 2, record.getFormattingRunLength());
        assertEquals( 0, record.getReserved7());


        assertEquals( 22, record.getRecordSize() );
    }

    public void testStore()
    {
        TextObjectBaseRecord record = new TextObjectBaseRecord();



//        record.setOptions( (short) 0x0000);
        record.setReserved1( false );
        record.setHorizontalTextAlignment( TextObjectBaseRecord.HORIZONTAL_TEXT_ALIGNMENT_CENTERED );
        record.setVerticalTextAlignment( TextObjectBaseRecord.VERTICAL_TEXT_ALIGNMENT_JUSTIFY );
        record.setReserved2( (short)0 );
        record.setTextLocked( true );
        record.setReserved3( (short)0 );
        record.setTextOrientation( TextObjectBaseRecord.TEXT_ORIENTATION_ROT_RIGHT );
        record.setReserved4( (short)0 );
        record.setReserved5( (short)0 );
        record.setReserved6( (short)0 );
        record.setTextLength( (short)2 );
        record.setFormattingRunLength( (short)2 );
        record.setReserved7( 0 );

        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}
