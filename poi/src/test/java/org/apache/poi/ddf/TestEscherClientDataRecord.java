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

class TestEscherClientDataRecord {
    @Test
    void testSerialize() {
        EscherClientDataRecord r = createRecord();

        byte[] data = new byte[8];
        int bytesWritten = r.serialize( 0, data, new NullEscherSerializationListener() );
        assertEquals( 8, bytesWritten );
        assertEquals( "[02, 00, " +
                "11, F0, " +
                "00, 00, 00, 00]",
                HexDump.toHex( data ) );
    }

    @Test
    void testFillFields() {
        String hexData = "02 00 " +
                "11 F0 " +
                "00 00 00 00 ";
        byte[] data = HexRead.readFromString( hexData );
        EscherClientDataRecord r = new EscherClientDataRecord();
        int bytesWritten = r.fillFields( data, new DefaultEscherRecordFactory() );

        assertEquals( 8, bytesWritten );
        assertEquals( (short)0xF011, r.getRecordId() );
        assertEquals( "[]", HexDump.toHex(r.getRemainingData()) );
    }

    @Test
    void testToString() {
        String expected =
            "{   /* CLIENT_DATA */\n" +
            "\t  \"recordId\": -4079 /* 0xf011 */\n" +
            "\t, \"version\": 2\n" +
            "\t, \"instance\": 0\n" +
            "\t, \"options\": 2\n" +
            "\t, \"recordSize\": 8\n" +
            "\t, \"remainingData\": \"\"\n" +
            "}";
        expected = expected.replace("\n", System.getProperty("line.separator"));
        assertEquals( expected, createRecord().toString() );
    }

    private static EscherClientDataRecord createRecord()
    {
        EscherClientDataRecord r = new EscherClientDataRecord();
        r.setOptions( (short) 0x0002 );
        r.setRecordId( EscherClientDataRecord.RECORD_ID );
        r.setRemainingData( new byte[] {} );
        return r;
    }
}
