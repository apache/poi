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

package org.apache.poi.ddf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;
import org.junit.Test;

public final class TestUnknownEscherRecord {
    @Test
    public void testFillFields() {
        String testData =
                "0F 02 " + // options
                "11 F1 " + // record id
                "00 00 00 00";      // remaining bytes

        UnknownEscherRecord r = new UnknownEscherRecord();
        EscherRecordFactory factory = new DefaultEscherRecordFactory();
        r.fillFields( HexRead.readFromString( testData ), factory );

        assertEquals( 0x020F, r.getOptions() );
        assertEquals( (short) 0xF111, r.getRecordId() );
        assertTrue( r.isContainerRecord() );
        assertEquals( 8, r.getRecordSize() );
        assertEquals( 0, r.getChildRecords().size() );
        assertEquals( 0, r.getData().length );

        testData =
                "00 02 " + // options
                "11 F1 " + // record id
                "04 00 00 00 " + // remaining bytes
                "01 02 03 04";

        r = new UnknownEscherRecord();
        r.fillFields( HexRead.readFromString( testData ), factory );

        assertEquals( 0x0200, r.getOptions() );
        assertEquals( (short) 0xF111, r.getRecordId() );
        assertEquals( 12, r.getRecordSize() );
        assertFalse( r.isContainerRecord() );
        assertEquals( 0, r.getChildRecords().size() );
        assertEquals( 4, r.getData().length );
        assertEquals( 1, r.getData()[0] );
        assertEquals( 2, r.getData()[1] );
        assertEquals( 3, r.getData()[2] );
        assertEquals( 4, r.getData()[3] );

        testData =
                "0F 02 " + // options
                "11 F1 " + // record id
                "08 00 00 00 " + // remaining bytes
                "00 02 " + // options
                "FF FF " + // record id
                "00 00 00 00";      // remaining bytes

        r = new UnknownEscherRecord();
        r.fillFields( HexRead.readFromString( testData ), factory );

        assertEquals( 0x020F, r.getOptions() );
        assertEquals( (short) 0xF111, r.getRecordId() );
        assertEquals( 8, r.getRecordSize() );
        assertTrue( r.isContainerRecord() );
        assertEquals( 1, r.getChildRecords().size() );
        assertEquals( (short) 0xFFFF, r.getChild( 0 ).getRecordId() );
        
        //Add by Zhang Zhang test error situation when remaining bytes > avalible bytes
        testData =
            "00 02 " + // options
            "11 F1 " + // record id
            "05 00 00 00 " + // remaining bytes
            "01 02 03 04";

	    r = new UnknownEscherRecord();
	    r.fillFields( HexRead.readFromString( testData ), factory );
	
	    assertEquals( 0x0200, r.getOptions() );
	    assertEquals( (short) 0xF111, r.getRecordId() );
	    assertEquals( 12, r.getRecordSize() );
	    assertFalse( r.isContainerRecord() );
	    assertEquals( 0, r.getChildRecords().size() );
	    assertEquals( 4, r.getData().length );
	    assertEquals( 1, r.getData()[0] );
	    assertEquals( 2, r.getData()[1] );
	    assertEquals( 3, r.getData()[2] );
	    assertEquals( 4, r.getData()[3] );
	    
        testData =
            "0F 02 " + // options
            "11 F1 " + // record id
            "09 00 00 00 " + // remaining bytes
            "00 02 " + // options
            "FF FF " + // record id
            "00 00 00 00";      // remaining bytes

	    r = new UnknownEscherRecord();
	    r.fillFields( HexRead.readFromString( testData ), factory );
	
	    assertEquals( 0x020F, r.getOptions() );
	    assertEquals( (short) 0xF111, r.getRecordId() );
	    assertEquals( 8, r.getRecordSize() );
	    assertTrue( r.isContainerRecord() );
	    assertEquals( 1, r.getChildRecords().size() );
	    assertEquals( (short) 0xFFFF, r.getChild( 0 ).getRecordId() );
    }

    @Test
    public void testSerialize() {
        UnknownEscherRecord r = new UnknownEscherRecord();
        r.setOptions( (short) 0x1234 );
        r.setRecordId( (short) 0xF112 );
        byte[] data = new byte[8];
        r.serialize( 0, data, new NullEscherSerializationListener() );

        assertEquals( "[34, 12, 12, F1, 00, 00, 00, 00]", HexDump.toHex( data ) );

        EscherRecord childRecord = new UnknownEscherRecord();
        childRecord.setOptions( (short) 0x9999 );
        childRecord.setRecordId( (short) 0xFF01 );
        r.addChildRecord( childRecord );
        r.setOptions( (short) 0x123F );
        data = new byte[16];
        r.serialize( 0, data, new NullEscherSerializationListener() );

        assertEquals( "[3F, 12, 12, F1, 08, 00, 00, 00, 99, 99, 01, FF, 00, 00, 00, 00]", HexDump.toHex( data ) );
    }

    @Test
    public void testToString() {
        UnknownEscherRecord r = new UnknownEscherRecord();
        r.setOptions( (short) 0x1234 );
        r.setRecordId( (short) 0xF112 );
        byte[] data = new byte[8];
        r.serialize( 0, data, new NullEscherSerializationListener() );
        String nl = System.getProperty("line.separator");
        String expected =
            "org.apache.poi.ddf.UnknownEscherRecord (Unknown 0xF112):" + nl +
            "  RecordId: 0xF112" + nl +
            "  Version: 0x0004" + nl +
            "  Instance: 0x0123" + nl +
            "  Options: 0x1234" + nl +
            "  Record Size: 8" + nl +
            "  isContainer: false" + nl +
            "  children: 0x00000000" + nl +
            "  Extra Data: " + nl +
            "     : 0";
        
        
        assertEquals(expected, r.toString() );
    }
}
