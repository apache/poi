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

final class TestEscherSpgrRecord {
    @Test
    void testSerialize() {
        EscherSpgrRecord r = createRecord();

        byte[] data = new byte[24];
        int bytesWritten = r.serialize( 0, data, new NullEscherSerializationListener() );
        assertEquals( 24, bytesWritten );
        assertEquals( "[10, 00, " +
                "09, F0, " +
                "10, 00, 00, 00, " +
                "01, 00, 00, 00, " +     // x
                "02, 00, 00, 00, " +     // y
                "03, 00, 00, 00, " +     // width
                "04, 00, 00, 00]",     // height
                HexDump.toHex( data ) );
    }

    @Test
    void testFillFields() {
        String hexData = "10 00 " +
                "09 F0 " +
                "10 00 00 00 " +
                "01 00 00 00 " +
                "02 00 00 00 " +
                "03 00 00 00 " +
                "04 00 00 00 ";
        byte[] data = HexRead.readFromString( hexData );
        EscherSpgrRecord r = new EscherSpgrRecord();
        int bytesWritten = r.fillFields( data, new DefaultEscherRecordFactory() );

        assertEquals( 24, bytesWritten );
        assertEquals( 1, r.getRectX1() );
        assertEquals( 2, r.getRectY1() );
        assertEquals( 3, r.getRectX2() );
        assertEquals( 4, r.getRectY2() );
    }

    @Test
    void testToString() {
        String expected =
            "{   /* SPGR */\n" +
            "\t  \"recordId\": -4087 /* 0xf009 */\n" +
            "\t, \"version\": 0\n" +
            "\t, \"instance\": 1\n" +
            "\t, \"options\": 16 /* 0x0010 */\n" +
            "\t, \"recordSize\": 24 /* 0x00000018 */\n" +
            "\t, \"rectX1\": 1\n" +
            "\t, \"rectY1\": 2\n" +
            "\t, \"rectX2\": 3\n" +
            "\t, \"rectY2\": 4\n" +
            "}";
        expected = expected.replace("\n", System.getProperty("line.separator"));
        assertEquals( expected, createRecord().toString() );
    }

    private static EscherSpgrRecord createRecord()
    {
        EscherSpgrRecord r = new EscherSpgrRecord();
        r.setOptions( (short) 0x0010 );
        r.setRecordId( EscherSpgrRecord.RECORD_ID );
        r.setRectX1(1);
        r.setRectY1(2);
        r.setRectX2(3);
        r.setRectY2(4);
        return r;
    }
}
