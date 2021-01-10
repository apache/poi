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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.poi.poifs.storage.RawDataUtil;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;
import org.junit.jupiter.api.Test;

final class TestEscherOptRecord {

    @Test
    void testFillFields() {
        checkFillFieldsSimple();
        checkFillFieldsComplex();
    }

    private void checkFillFieldsComplex() {
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
        EscherBoolProperty prop1 = new EscherBoolProperty( EscherPropertyTypes.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 1 );
        EscherComplexProperty prop2 = new EscherComplexProperty( EscherPropertyTypes.TEXT__SIZE_TEXT_TO_FIT_SHAPE, false, 2);
        prop2.setComplexData(new byte[] { 0x01, 0x02 });
        EscherBoolProperty prop3 = new EscherBoolProperty( EscherPropertyTypes.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 1 );
        assertEquals( prop1, r.getEscherProperty( 0 ) );
        assertEquals( prop2, r.getEscherProperty( 1 ) );
        assertEquals( prop3, r.getEscherProperty( 2 ) );

    }

    private void checkFillFieldsSimple() {
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
        EscherBoolProperty prop1 = new EscherBoolProperty( EscherPropertyTypes.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 524296 );
        EscherRGBProperty prop2 = new EscherRGBProperty( EscherPropertyTypes.FILL__FILLCOLOR, 0x08000009 );
        EscherRGBProperty prop3 = new EscherRGBProperty( EscherPropertyTypes.LINESTYLE__COLOR, 0x08000040 );
        assertEquals( prop1, r.getEscherProperty( 0 ) );
        assertEquals( prop2, r.getEscherProperty( 1 ) );
        assertEquals( prop3, r.getEscherProperty( 2 ) );
    }

    @Test
    void testSerialize() {
        checkSerializeSimple();
        checkSerializeComplex();
    }

    private void checkSerializeComplex()
    {
        //Complex escher record
        EscherOptRecord r = new EscherOptRecord();
        r.setOptions( (short) 0x0033 );
        r.setRecordId( (short) 0xF00B );
        EscherBoolProperty prop1 = new EscherBoolProperty( EscherPropertyTypes.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 1 );
        EscherComplexProperty prop2 = new EscherComplexProperty( EscherPropertyTypes.TEXT__SIZE_TEXT_TO_FIT_SHAPE, false, 2);
        prop2.setComplexData(new byte[] { 0x01, 0x02 });
        EscherBoolProperty prop3 = new EscherBoolProperty( EscherPropertyTypes.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 1 );
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
                "BF, 80, 02, 00, 00, 00, " +
                "BF, 00, 01, 00, 00, 00, " +
                "01, 02]";
        assertEquals( dataStr, HexDump.toHex(data) );

    }

    private void checkSerializeSimple()
    {
        EscherOptRecord r = new EscherOptRecord();
        r.setOptions( (short) 0x0033 );
        r.setRecordId( (short) 0xF00B );
        EscherBoolProperty prop1 = new EscherBoolProperty( EscherPropertyTypes.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 1 );
        EscherRGBProperty prop2 = new EscherRGBProperty( EscherPropertyTypes.FILL__FILLCOLOR, 0x08000009 );
        EscherRGBProperty prop3 = new EscherRGBProperty( EscherPropertyTypes.LINESTYLE__COLOR, 0x08000040 );
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
                "C0, 01, 40, 00, 00, 08]";
        assertEquals( dataStr, HexDump.toHex(data) );
        assertEquals( 26, bytesWritten );
    }

    @Test
    void testToString() {
        EscherOptRecord r = new EscherOptRecord();
        // don't try to shoot in foot, please -- vlsergey
        // r.setOptions((short)0x000F);
        r.setRecordId(EscherOptRecord.RECORD_ID);
        EscherProperty prop1 = new EscherBoolProperty(EscherPropertyTypes.GEOMETRY__FILLOK, 1);
        r.addEscherProperty(prop1);
        String expected =
            "{   /* OPT */\n" +
            "\t  \"recordId\": -4085 /* 0xf00b */\n" +
            "\t, \"version\": 3\n" +
            "\t, \"instance\": 1\n" +
            "\t, \"options\": 19 /* 0x0013 */\n" +
            "\t, \"recordSize\": 14 /* 0x0000000e */\n" +
            "\t, \"isContainer\": false\n" +
            "\t, \"properties\": [\n" +
            "\t{   /* GEOMETRY__FILLOK */\n" +
            "\t\t  \"id\": 383 /* 0x017f */\n" +
            "\t\t, \"name\": \"geometry.fillok\"\n" +
            "\t\t, \"propertyNumber\": 383 /* 0x017f */\n" +
            "\t\t, \"propertySize\": 6\n" +
            "\t\t, \"flags\": 383 /*  */ \n" +
            "\t\t, \"value\": 1\n" +
            "\t}\t]\n" +
            "}";
        expected = expected.replace("\n", System.getProperty("line.separator"));
        assertEquals( expected, r.toString());
    }

    /**
     * Test serialization of a particularly complex example
     * This test is currently broken!
     */
    @Test
    void testComplexSerialise() {
    	byte[] data = {
    		0x53, 0x01, 0x0B, 0xF0-256, 0x9C-256, 0x01, 0x00, 0x00,
    		// Simple data follows
    		0x42, 0x01,	0x49, 0x00, 0x00, 0x00,          // SP @ 8
    		0x43, 0x01, 0x85-256, 0x00, 0x00, 0x00,      // SP @ 14
    		0x44, 0x01, 0x04, 0x00, 0x00, 0x00,          // SP @ 20
    		0x45, 0xC1-256, 0x88-256, 0x00, 0x00, 0x00,  // SP @ 26
    		0x46, 0xC1-256, 0x90-256, 0x00, 0x00, 0x00,  // SP @ 32
    		0x7F, 0x01, 0x01, 0x00, 0x01, 0x00,
    		0x80-256, 0x01, 0x00, 0x00, 0x00, 0x00,
    		0x81-256, 0x01, 0x02, 0x00, 0x00, 0x08,
    		0xBF-256, 0x01,	0x10, 0x00, 0x10, 0x00,
    		0xC0-256, 0x01, 0x01, 0x00, 0x00, 0x08,      // SP 10
    		0xC1-256, 0x01, 0x00, 0x00, 0x01, 0x00,
    		0xC4-256, 0x01, 0x00, 0x00, 0x00, 0x00,
    		0xCB-256, 0x01, 0x38, 0x63, 0x00, 0x00,
    		0xCD-256, 0x01, 0x00, 0x00,	0x00, 0x00,
    		0xCE-256, 0x01, 0x00, 0x00, 0x00, 0x00,      // SP 15
    		0xD0-256, 0x01, 0x00, 0x00, 0x00, 0x00,
    		0xD1-256, 0x01, 0x00, 0x00, 0x00, 0x00,
    		0xD7-256, 0x01, 0x00, 0x00, 0x00, 0x00,
    		0xFF-256, 0x01, 0x18, 0x00, 0x18, 0x00,
    		0x01, 0x02, 0x02, 0x00, 0x00, 0x08,
    		0x3F, 0x02, 0x00, 0x00,	0x02, 0x00,          // SP 21

    		// Complex data follows

    		// Complex data for Array #325
    		// Array header
    		0x22, 0x00, 0x22, 0x00, 0xF0-256, 0xFF-256,
    		// Array data
    		0x18, 0x00, 0x28, 0x00, 0x04, 0x00, 0x34,
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
    		0x3C, 0x00, 0x00, 0x00, 0x30, 0x00, 0x04, 0x00, 0x24, 0x00,
    		// Complex data for Array #326
    		// Array header
    		0x45, 0x00, 0x48, 0x00, 0x02, 0x00,
    		// Array data
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
        int filled = r.fillFields( data, new DefaultEscherRecordFactory() );

        // Check it's the right length
        assertEquals(data.length, filled);
        assertEquals(data.length, r.getRecordSize());

        // Serialize it
        byte[] dest = new byte[data.length];
        int written = r.serialize(0, dest);

        // Check it serialised it back to the same data
        assertEquals(data.length, written);
        assertArrayEquals(data, dest);
    }

    /**
     * Test read/write against an OPT record from a real ppt file.
     * In PowerPoint it is legal to have array properties with empty complex part.
     * In Glen's original implementation complex part is always 6 bytes which resulted
     * in +6 extra bytes when writing back out. As the result the ppt becomes unreadable.
     *
     * Make sure we write back the original empty complex part.
     *
     * See Bug 41946 for details.
     */
    @Test
    void test41946() throws IOException {
        String data64 =
            "H4sIAAAAAAAAAB3SuW5TQRjF8TPfOOZCHMeARAluEKIzSEgUSCQsLaLgDYCehgIJCe8L+xIgQB6"+
            "AEvEAOI6zOwlhX54BpBRIiGqY+Vvy7x6d+3k8nmufje/ISzVVrjrVNftWapCb5JbSqyMX7ZJ72I"+
            "/vSRXcH6k0kW6Wi1hNquZyUlaP2amRmqxJbjHTnmbNQbLLfA9v4x28i/fwPj7Ah/gIH+MTnMGn+"+
            "Ayfs/4s+QW+xFc45+KPnuq7gg5q3sUqG7DDBRdC0JB9LjK5xG6XWW2FZhXXcB1H7sRhaSMto02a"+
            "LXzPp745iwaXV1FKUc7iJTMbjUbyqSnnLH37mJ28LOVxF5MZ7ubuHvI4FmgmyEWctPSQSuS9eDr"+
            "qVSXXmK/bWMwNmzsmNelbtvMvrza5Y3/jAl320zcXn+88/QAX7Ep0SF7EJVzGFVzFNVy3yvV4Mr"+
            "a9b782rPL7V9i0qUs9bZmq8WSiIWzHyRvhgx2P8x+tfEH6ZBeH0mdW+GKlI9JXuzYTz9DenArhO"+
            "/0P+p/0wQ7okHI+Hfe0f33U6YxPM2d9upzzN985nae55dM/tknTommTO+T/V9IPpAgDAAA=";

        EscherOptRecord r = new EscherOptRecord();
        byte[] data = RawDataUtil.decompress(data64);
        r.fillFields( data, 0, new DefaultEscherRecordFactory() );
        assertEquals( (short) 0xF00B, r.getRecordId() );

        byte[] data1 = r.serialize();
        EscherOptRecord opt2 = new EscherOptRecord();
        opt2.fillFields( data1, new DefaultEscherRecordFactory() );

        byte[] data2 = opt2.serialize();
        assertArrayEquals(data1, data2);
    }

    /**
     * Test that EscherOptRecord can properly read/write array properties
     * with empty complex part.
     */
    @Test
    void testEmptyArrayProperty() {
        EscherOptRecord r = new EscherOptRecord();
        EscherArrayProperty p = new EscherArrayProperty(EscherPropertyTypes.FILL__SHADECOLORS, false, 0);
        assertEquals(0, p.getNumberOfElementsInArray());
        r.addEscherProperty(p);

        byte[] data1 = r.serialize();
        EscherOptRecord opt2 = new EscherOptRecord();
        opt2.fillFields( data1, new DefaultEscherRecordFactory() );
        p = (EscherArrayProperty)opt2.getEscherProperties().get(0);
        assertEquals(0, p.getNumberOfElementsInArray());

        byte[] data2 = opt2.serialize();
        assertArrayEquals(data1, data2);
    }
}
