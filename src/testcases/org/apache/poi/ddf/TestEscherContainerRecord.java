
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

import java.io.File;
import java.io.FileInputStream;

import junit.framework.TestCase;
import org.apache.poi.util.HexRead;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.IOUtils;

public class TestEscherContainerRecord extends TestCase
{
	private String ESCHER_DATA_PATH;
	
	protected void setUp() {
		ESCHER_DATA_PATH = System.getProperty("DDF.testdata.path");
	}

	public void testFillFields() {
        EscherRecordFactory f = new DefaultEscherRecordFactory();
        byte[] data = HexRead.readFromString( "0F 02 11 F1 00 00 00 00" );
        EscherRecord r = f.createRecord( data, 0 );
        r.fillFields( data, 0, f );
        assertTrue( r instanceof EscherContainerRecord );
        assertEquals( (short) 0x020F, r.getOptions() );
        assertEquals( (short) 0xF111, r.getRecordId() );

        data = HexRead.readFromString( "0F 02 11 F1 08 00 00 00" +
                " 02 00 22 F2 00 00 00 00" );
        r = f.createRecord( data, 0 );
        r.fillFields( data, 0, f );
        EscherRecord c = r.getChild( 0 );
        assertFalse( c instanceof EscherContainerRecord );
        assertEquals( (short) 0x0002, c.getOptions() );
        assertEquals( (short) 0xF222, c.getRecordId() );
    }

    public void testSerialize() {
        UnknownEscherRecord r = new UnknownEscherRecord();
        r.setOptions( (short) 0x123F );
        r.setRecordId( (short) 0xF112 );
        byte[] data = new byte[8];
        r.serialize( 0, data, new NullEscherSerializationListener() );

        assertEquals( "[3F, 12, 12, F1, 00, 00, 00, 00]", HexDump.toHex( data ) );

        EscherRecord childRecord = new UnknownEscherRecord();
        childRecord.setOptions( (short) 0x9999 );
        childRecord.setRecordId( (short) 0xFF01 );
        r.addChildRecord( childRecord );
        data = new byte[16];
        r.serialize( 0, data, new NullEscherSerializationListener() );

        assertEquals( "[3F, 12, 12, F1, 08, 00, 00, 00, 99, 99, 01, FF, 00, 00, 00, 00]", HexDump.toHex( data ) );

    }

    public void testToString() {
        EscherContainerRecord r = new EscherContainerRecord();
        r.setRecordId( EscherContainerRecord.SP_CONTAINER );
        r.setOptions( (short) 0x000F );
        String nl = System.getProperty( "line.separator" );
        assertEquals( "org.apache.poi.ddf.EscherContainerRecord (SpContainer):" + nl +
                "  isContainer: true" + nl +
                "  options: 0x000F" + nl +
                "  recordId: 0xF004" + nl +
                "  numchildren: 0" + nl
                , r.toString() );

        EscherOptRecord r2 = new EscherOptRecord();
        r2.setOptions( (short) 0x9876 );
        r2.setRecordId( EscherOptRecord.RECORD_ID );

        String expected;
        r.addChildRecord( r2 );
        expected = "org.apache.poi.ddf.EscherContainerRecord (SpContainer):" + nl +
                   "  isContainer: true" + nl +
                   "  options: 0x000F" + nl +
                   "  recordId: 0xF004" + nl +
                   "  numchildren: 1" + nl +
                   "  children: " + nl +
                   "   Child 0:" + nl +
                   "org.apache.poi.ddf.EscherOptRecord:" + nl +
                   "  isContainer: false" + nl +
                   "  options: 0x0003" + nl +
                   "  recordId: 0xF00B" + nl +
                   "  numchildren: 0" + nl +
                   "  properties:" + nl;
        assertEquals( expected, r.toString() );

        r.addChildRecord( r2 );
        expected = "org.apache.poi.ddf.EscherContainerRecord (SpContainer):" + nl +
                   "  isContainer: true" + nl +
                   "  options: 0x000F" + nl +
                   "  recordId: 0xF004" + nl +
                   "  numchildren: 2" + nl +
                   "  children: " + nl +
                   "   Child 0:" + nl +
                   "org.apache.poi.ddf.EscherOptRecord:" + nl +
                   "  isContainer: false" + nl +
                   "  options: 0x0003" + nl +
                   "  recordId: 0xF00B" + nl +
                   "  numchildren: 0" + nl +
                   "  properties:" + nl +
                   "   Child 1:" + nl +
                   "org.apache.poi.ddf.EscherOptRecord:" + nl +
                   "  isContainer: false" + nl +
                   "  options: 0x0003" + nl +
                   "  recordId: 0xF00B" + nl +
                   "  numchildren: 0" + nl +
                   "  properties:" + nl;
        assertEquals( expected, r.toString() );
    }

    public void testGetRecordSize() {
        EscherContainerRecord r = new EscherContainerRecord();
        r.addChildRecord(new EscherRecord()
        {
            public int fillFields( byte[] data, int offset, EscherRecordFactory recordFactory ) { return 0; }
            public int serialize( int offset, byte[] data, EscherSerializationListener listener ) { return 0; }
            public int getRecordSize() { return 10; }
            public String getRecordName() { return ""; }
        } );

        assertEquals(18, r.getRecordSize());
    }

    /**
     * We were having problems with reading too much data on an UnknownEscherRecord,
     *  but hopefully we now read the correct size.
     */
    public void testBug44857() throws Exception {
    	File f = new File(ESCHER_DATA_PATH, "Container.dat");
    	assertTrue(f.exists());
    	
    	FileInputStream finp = new FileInputStream(f);
    	byte[] data = IOUtils.toByteArray(finp);
    	
    	// This used to fail with an OutOfMemory
    	EscherContainerRecord record = new EscherContainerRecord();
    	record.fillFields(data, 0, new DefaultEscherRecordFactory());
    }
}
