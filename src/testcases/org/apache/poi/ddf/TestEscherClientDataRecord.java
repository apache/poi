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

import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;
import org.junit.Test;

public class TestEscherClientDataRecord {
    @Test
    public void testSerialize() {
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
    public void testFillFields() {
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
    public void testToString() {
        String nl = System.getProperty("line.separator");

        String expected =
            "org.apache.poi.ddf.EscherClientDataRecord (ClientData):" + nl +
            "  RecordId: 0xF011" + nl +
            "  Version: 0x0002" + nl +
            "  Instance: 0x0000" + nl +
            "  Options: 0x0002" + nl +
            "  Record Size: 8" + nl +
            "  Extra Data: " + nl +
            "     : 0";
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
