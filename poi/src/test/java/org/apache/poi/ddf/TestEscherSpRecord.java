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

class TestEscherSpRecord {
    @Test
    void testSerialize() {
        EscherSpRecord r = createRecord();

        byte[] data = new byte[16];
        int bytesWritten = r.serialize( 0, data, new NullEscherSerializationListener() );
        assertEquals( 16, bytesWritten );
        assertEquals( "[02, 00, " +
                "0A, F0, " +
                "08, 00, 00, 00, " +
                "00, 04, 00, 00, " +
                "05, 00, 00, 00]",
                HexDump.toHex( data ) );
    }

    @Test
    void testFillFields() {
        String hexData = "02 00 " +
                "0A F0 " +
                "08 00 00 00 " +
                "00 04 00 00 " +
                "05 00 00 00 ";
        byte[] data = HexRead.readFromString( hexData );
        EscherSpRecord r = new EscherSpRecord();
        int bytesWritten = r.fillFields( data, new DefaultEscherRecordFactory() );

        assertEquals( 16, bytesWritten );
        assertEquals( 0x0400, r.getShapeId() );
        assertEquals( 0x05, r.getFlags() );
    }

    @Test
    void testToString() {
        String expected =
            "{   /* SP */\n" +
            "\t  \"recordId\": -4086 /* 0xf00a */\n" +
            "\t, \"version\": 2\n" +
            "\t, \"instance\": 0\n" +
            "\t, \"options\": 2\n" +
            "\t, \"recordSize\": 16 /* 0x00000010 */\n" +
            "\t, \"shapeType\": 0\n" +
            "\t, \"shapeId\": 1024 /* 0x00000400 */\n" +
            "\t, \"flags\": 5 /* GROUP | PATRIARCH */ \n" +
            "}";
        expected = expected.replace("\n", System.getProperty("line.separator"));
        assertEquals( expected, createRecord().toString() );
    }

    private static EscherSpRecord createRecord()
    {
        EscherSpRecord r = new EscherSpRecord();
        r.setOptions( (short) 0x0002 );
        r.setRecordId( EscherSpRecord.RECORD_ID );
        r.setShapeId(0x0400);
        r.setFlags(0x05);
        return r;
    }

}
