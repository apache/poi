
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        
package org.apache.poi.ddf;

import junit.framework.TestCase;
import org.apache.poi.util.HexRead;
import org.apache.poi.util.HexDump;

import java.io.IOException;

public class TestEscherOptRecord extends TestCase
{

    public void testFillFields() throws Exception
    {
        checkFillFieldsSimple();
        checkFillFieldsComplex();
    }

    private void checkFillFieldsComplex() throws IOException
    {
        String dataStr = "33 00 " +
                "0B F0 " +
                "14 00 00 00 " +
                "BF 00 01 00 00 00 " +
                "01 80 02 00 00 00 " +
                "BF 00 01 00 00 00 " +
                "01 02";

        EscherOptRecord r = new EscherOptRecord();
        r.fillFields( HexRead.readFromString( dataStr ), new DefaultEscherRecordFactory() );
        assertEquals( (short) 0x0033, r.getOptions() );
        assertEquals( (short) 0xF00B, r.getRecordId() );
        assertEquals( 3, r.getEscherProperties().size() );
        EscherBoolProperty prop1 = new EscherBoolProperty( EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 1 );
        EscherComplexProperty prop2 = new EscherComplexProperty( (short) 1, false, new byte[] { 0x01, 0x02 } );
        EscherBoolProperty prop3 = new EscherBoolProperty( EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 1 );
        assertEquals( prop1, r.getEscherProperty( 0 ) );
        assertEquals( prop2, r.getEscherProperty( 1 ) );
        assertEquals( prop3, r.getEscherProperty( 2 ) );

    }

    private void checkFillFieldsSimple()
            throws IOException
    {
        String dataStr = "33 00 " + // options
                        "0B F0 " + // recordid
                        "12 00 00 00 " + // remaining bytes
                        "BF 00 08 00 08 00 " +
                        "81 01 09 00 00 08 " +
                        "C0 01 40 00 00 08";

        EscherOptRecord r = new EscherOptRecord();
        r.fillFields( HexRead.readFromString( dataStr ), new DefaultEscherRecordFactory() );
        assertEquals( (short) 0x0033, r.getOptions() );
        assertEquals( (short) 0xF00B, r.getRecordId() );
        assertEquals( 3, r.getEscherProperties().size() );
        EscherBoolProperty prop1 = new EscherBoolProperty( EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 524296 );
        EscherRGBProperty prop2 = new EscherRGBProperty( EscherProperties.FILL__FILLCOLOR, 0x08000009 );
        EscherRGBProperty prop3 = new EscherRGBProperty( EscherProperties.LINESTYLE__COLOR, 0x08000040 );
        assertEquals( prop1, r.getEscherProperty( 0 ) );
        assertEquals( prop2, r.getEscherProperty( 1 ) );
        assertEquals( prop3, r.getEscherProperty( 2 ) );
    }

    public void testSerialize() throws Exception
    {
        checkSerializeSimple();
        checkSerializeComplex();
    }

    private void checkSerializeComplex()
    {
        //Complex escher record
        EscherOptRecord r = new EscherOptRecord();
        r.setOptions( (short) 0x0033 );
        r.setRecordId( (short) 0xF00B );
        EscherBoolProperty prop1 = new EscherBoolProperty( EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 1 );
        EscherComplexProperty prop2 = new EscherComplexProperty( (short) 1, false, new byte[] { 0x01, 0x02 } );
        EscherBoolProperty prop3 = new EscherBoolProperty( EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 1 );
        r.addEscherProperty( prop1 );
        r.addEscherProperty( prop2 );
        r.addEscherProperty( prop3 );

        byte[] data = new byte[28];
        int bytesWritten = r.serialize(0, data, new NullEscherSerializationListener() );
        assertEquals( 28, bytesWritten );
        String dataStr = "[33, 00, " +
                "0B, F0, " +
                "14, 00, 00, 00, " +
                "BF, 00, 01, 00, 00, 00, " +
                "01, 80, 02, 00, 00, 00, " +
                "BF, 00, 01, 00, 00, 00, " +
                "01, 02, ]";
        assertEquals( dataStr, HexDump.toHex(data) );

    }

    private void checkSerializeSimple()
    {
        EscherOptRecord r = new EscherOptRecord();
        r.setOptions( (short) 0x0033 );
        r.setRecordId( (short) 0xF00B );
        EscherBoolProperty prop1 = new EscherBoolProperty( EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 1 );
        EscherRGBProperty prop2 = new EscherRGBProperty( EscherProperties.FILL__FILLCOLOR, 0x08000009 );
        EscherRGBProperty prop3 = new EscherRGBProperty( EscherProperties.LINESTYLE__COLOR, 0x08000040 );
        r.addEscherProperty( prop1 );
        r.addEscherProperty( prop2 );
        r.addEscherProperty( prop3 );

        byte[] data = new byte[26];
        int bytesWritten = r.serialize(0, data, new NullEscherSerializationListener() );
        String dataStr = "[33, 00, " +
                "0B, F0, " +
                "12, 00, 00, 00, " +
                "BF, 00, 01, 00, 00, 00, " +
                "81, 01, 09, 00, 00, 08, " +
                "C0, 01, 40, 00, 00, 08, ]";
        assertEquals( dataStr, HexDump.toHex(data) );
        assertEquals( 26, bytesWritten );
    }

    public void testToString() throws Exception
    {
        String nl = System.getProperty("line.separator");
        EscherOptRecord r = new EscherOptRecord();
        r.setOptions((short)0x000F);
        r.setRecordId(EscherOptRecord.RECORD_ID);
        EscherProperty prop1 = new EscherBoolProperty((short)1, 1);
        r.addEscherProperty(prop1);
        String expected = "org.apache.poi.ddf.EscherOptRecord:" + nl +
                "  isContainer: true" + nl +
                "  options: 0x0013" + nl +
                "  recordId: 0x" + HexDump.toHex(EscherOptRecord.RECORD_ID) + nl +
                "  numchildren: 0" + nl +
                "  properties:" + nl +
                "    propNum: 1, RAW: 0x0001, propName: unknown, complex: false, blipId: false, value: 1 (0x00000001)" + nl;
        assertEquals( expected, r.toString());
    }

    /**
     * Test serialisation of a particually complex example 
     * This test is currently broken!
     */
    public void BROKENtestComplexSerialise() throws Exception {
    	byte[] data = new byte[] {
    		0x53, 0x01, 0x0B, 0xF0-256, 0x9C-256, 0x01, 0x00, 0x00, 0x42, 0x01,
    		0x49, 0x00, 0x00, 0x00, 0x43, 0x01, 0x85-256, 0x00, 0x00, 
    		0x00, 0x44, 0x01, 0x04, 0x00, 0x00, 0x00,
    		0x45, 0xC1-256, 0x88-256, 0x00, 0x00, 0x00, 0x46, 0xC1-256, 
    		0x90-256, 0x00, 0x00, 0x00, 0x7F, 0x01, 0x01, 0x00,
    		0x01, 0x00, 0x80-256, 0x01, 0x00, 0x00, 0x00, 0x00, 0x81-256, 
    		0x01, 0x02, 0x00, 0x00, 0x08, 0xBF-256, 0x01,
    		0x10, 0x00, 0x10, 0x00, 0xC0-256, 0x01, 0x01, 0x00, 0x00, 0x08, 
    		0xC1-256, 0x01, 0x00, 0x00, 0x01, 0x00,
    		0xC4-256, 0x01, 0x00, 0x00, 0x00, 0x00, 0xCB-256, 0x01, 0x38, 
    		0x63, 0x00, 0x00, 0xCD-256, 0x01, 0x00, 0x00,
    		0x00, 0x00, 0xCE-256, 0x01, 0x00, 0x00, 0x00, 0x00, 0xD0-256, 
    		0x01, 0x00, 0x00, 0x00, 0x00, 0xD1-256, 0x01,
    		0x00, 0x00, 0x00, 0x00, 0xD7-256, 0x01, 0x00, 0x00, 0x00, 0x00, 
    		0xFF-256, 0x01, 0x18, 0x00, 0x18, 0x00,
    		0x01, 0x02, 0x02, 0x00, 0x00, 0x08, 0x3F, 0x02, 0x00, 0x00, 
    		0x02, 0x00, 0x22, 0x00, 0x22, 0x00,
    		0xF0-256, 0xFF-256, 0x18, 0x00, 0x28, 0x00, 0x04, 0x00, 0x34, 
    		0x00, 0x04, 0x00, 0x28, 0x00, 0x04, 0x00,
    		0x1C, 0x00, 0x04, 0x00, 0x10, 0x00, 0x04, 0x00, 0x04, 0x00, 0x10, 
    		0x00, 0x00, 0x00, 0x1C, 0x00,
    		0x04, 0x00, 0x28, 0x00, 0x10, 0x00, 0x34, 0x00, 0x18, 0x00, 0x3C, 
    		0x00, 0x24, 0x00, 0x44, 0x00,
    		0x30, 0x00, 0x48, 0x00, 0x3C, 0x00, 0x44, 0x00, 0x48, 0x00, 0x3C, 
    		0x00, 0x54, 0x00, 0x38, 0x00,
    		0x60, 0x00, 0x2C, 0x00, 0x70, 0x00, 0x20, 0x00, 0x78, 0x00, 
    		0x14, 0x00, 0x80-256, 0x00, 0x08, 0x00,
    		0x84-256, 0x00, 0x04, 0x00, 0x78, 0x00, 0x04, 0x00, 0x6C, 0x00, 
    		0x04, 0x00, 0x60, 0x00, 0x04, 0x00,
    		0x54, 0x00, 0x08, 0x00, 0x48, 0x00, 0x0C, 0x00, 0x3C, 0x00, 0x0C, 
    		0x00, 0x30, 0x00, 0x08, 0x00,
    		0x3C, 0x00, 0x08, 0x00, 0x48, 0x00, 0x08, 0x00, 0x54, 0x00, 0x00, 
    		0x00, 0x48, 0x00, 0x00, 0x00,
    		0x3C, 0x00, 0x00, 0x00, 0x30, 0x00, 0x04, 0x00, 0x24, 0x00, 0x45, 
    		0x00, 0x48, 0x00, 0x02, 0x00,
    		0x00, 0x40, 0x00, 0xB0-256, 0x01, 0x00, 0x00, 0xB0-256, 0x01, 0x00, 
    		0x00, 0xB0-256, 0x01, 0x00, 0x00, 0xB0-256,
    		0x01, 0x00, 0x00, 0xB0-256, 0x01, 0x00, 0x00, 0xB0-256, 0x01, 0x00, 
    		0x00, 0xB0-256, 0x01, 0x00, 0x00, 0xB0-256,
    		0x01, 0x00, 0x00, 0xB0-256, 0x01, 0x00, 0x00, 0xB0-256, 0x01, 0x00, 
    		0x00, 0xB0-256, 0x01, 0x00, 0x00, 0xB0-256,
    		0x01, 0x00, 0x00, 0xB0-256, 0x01, 0x00, 0x00, 0xB0-256, 0x01, 0x00, 
    		0x00, 0xB0-256, 0x01, 0x00, 0x00, 0xB0-256,
    		0x01, 0x00, 0x00, 0xB0-256, 0x01, 0x00, 0x00, 0xB0-256, 0x01, 0x00, 
    		0x00, 0xB0-256, 0x01, 0x00, 0x00, 0xB0-256,
    		0x01, 0x00, 0x00, 0xB0-256, 0x01, 0x00, 0x00, 0xB0-256, 0x01, 0x00, 
    		0x00, 0xB0-256, 0x01, 0x00, 0x00, 0xB0-256,
    		0x01, 0x00, 0x00, 0xB0-256, 0x01, 0x00, 0x00, 0xB0-256, 0x01, 0x00, 
    		0x00, 0xB0-256, 0x01, 0x00, 0x00, 0xB0-256,
    		0x01, 0x00, 0x00, 0xB0-256, 0x01, 0x00, 0x00, 0xB0-256, 0x01, 0x00, 
    		0x00, 0xB0-256, 0x01, 0x00, 0x00, 0xB0-256,
    		0x01, 0x00, 0x00, 0xB0-256, 0x01, 0x00, 0x00, 0xB0-256, 0x00, 0x80-256
    	};
    	
    	// Create the record
        EscherOptRecord r = new EscherOptRecord();
        r.fillFields( data, new DefaultEscherRecordFactory() );
        
        // Check it's the right length
        assertEquals(data.length, r.getRecordSize());
        
        // Serialise it
        byte[] dest = new byte[data.length];
        int written = r.serialize(0, dest);
        
        // Check it serialised it back to the same data
        assertEquals(data.length, written);
        for(int i=0; i<data.length; i++) {
        	System.err.println(i);
        	assertEquals(data[i], dest[i]);
        }
    }
}
