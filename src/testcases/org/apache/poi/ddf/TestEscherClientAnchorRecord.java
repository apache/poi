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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;
import org.junit.jupiter.api.Test;

class TestEscherClientAnchorRecord {
    @Test
    void testSerialize() {
        EscherClientAnchorRecord r = createRecord();

        byte[] data = new byte[8 + 18 + 2];
        int bytesWritten = r.serialize( 0, data, new NullEscherSerializationListener() );
        assertEquals( 28, bytesWritten );
        assertEquals( "[01, 00, " +
                "10, F0, " +
                "14, 00, 00, 00, " +
                "4D, 00, 37, 00, 21, 00, 58, 00, " +
                "0B, 00, 2C, 00, 16, 00, 63, 00, " +
                "42, 00, " +
                "FF, DD]", HexDump.toHex( data ) );
    }

    @Test
    void testFillFields() {
        String hexData = "01 00 " +
                "10 F0 " +
                "14 00 00 00 " +
                "4D 00 37 00 21 00 58 00 " +
                "0B 00 2C 00 16 00 63 00 " +
                "42 00 " +
                "FF DD";
        byte[] data = HexRead.readFromString( hexData );
        EscherClientAnchorRecord r = new EscherClientAnchorRecord();
        int bytesWritten = r.fillFields( data, new DefaultEscherRecordFactory() );

        assertEquals( 28, bytesWritten );
        assertEquals( (short) 55, r.getCol1() );
        assertEquals( (short) 44, r.getCol2() );
        assertEquals( (short) 33, r.getDx1() );
        assertEquals( (short) 22, r.getDx2() );
        assertEquals( (short) 11, r.getDy1() );
        assertEquals( (short) 66, r.getDy2() );
        assertEquals( (short) 77, r.getFlag() );
        assertEquals( (short) 88, r.getRow1() );
        assertEquals( (short) 99, r.getRow2() );
        assertEquals( (short) 0x0001, r.getOptions() );
        assertEquals( (byte) 0xFF, r.getRemainingData()[0] );
        assertEquals( (byte) 0xDD, r.getRemainingData()[1] );
    }

    @Test
    void testToString() {
        String expected =
            "{   /* CLIENT_ANCHOR */\n" +
            "\t  \"recordId\": -4080 /* 0xf010 */\n" +
            "\t, \"version\": 1\n" +
            "\t, \"instance\": 0\n" +
            "\t, \"options\": 1\n" +
            "\t, \"recordSize\": 28 /* 0x0000001c */\n" +
            "\t, \"flag\": 77 /* 0x004d */\n" +
            "\t, \"col1\": 55 /* 0x0037 */\n" +
            "\t, \"dx1\": 33 /* 0x0021 */\n" +
            "\t, \"row1\": 88 /* 0x0058 */\n" +
            "\t, \"dy1\": 11 /* 0x000b */\n" +
            "\t, \"col2\": 44 /* 0x002c */\n" +
            "\t, \"dx2\": 22 /* 0x0016 */\n" +
            "\t, \"row2\": 99 /* 0x0063 */\n" +
            "\t, \"dy2\": 66 /* 0x0042 */\n" +
            "\t, \"remainingData\": \"/90=\"\n" +
            "}";
        expected = expected.replace("\n", System.getProperty("line.separator"));
        assertEquals( expected, createRecord().toString() );
    }

    private EscherClientAnchorRecord createRecord()
    {
        EscherClientAnchorRecord r = new EscherClientAnchorRecord();
        r.setCol1( (short) 55 );
        r.setCol2( (short) 44 );
        r.setDx1( (short) 33 );
        r.setDx2( (short) 22 );
        r.setDy1( (short) 11 );
        r.setDy2( (short) 66 );
        r.setFlag( (short) 77 );
        r.setRow1( (short) 88 );
        r.setRow2( (short) 99 );
        r.setOptions( (short) 0x0001 );
        r.setRemainingData( new byte[]{(byte) 0xFF, (byte) 0xDD} );
        return r;
    }

}
