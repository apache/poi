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

public final class TestEscherChildAnchorRecord {
    @Test
    public void testSerialize() {
        EscherChildAnchorRecord r = createRecord();

        byte[] data = new byte[8 + 16];
        int bytesWritten = r.serialize( 0, data, new NullEscherSerializationListener() );
        assertEquals( data.length, bytesWritten );
        assertEquals( "[01, 00, " +
                "0F, F0, " +
                "10, 00, 00, 00, " +
                "01, 00, 00, 00, " +
                "02, 00, 00, 00, " +
                "03, 00, 00, 00, " +
                "04, 00, 00, 00]", HexDump.toHex( data ) );
    }

    @Test
    public void testFillFields() {
        String hexData = "01 00 " +
                "0F F0 " +
                "10 00 00 00 " +
                "01 00 00 00 " +
                "02 00 00 00 " +
                "03 00 00 00 " +
                "04 00 00 00 ";

        byte[] data = HexRead.readFromString( hexData );
        EscherChildAnchorRecord r = new EscherChildAnchorRecord();
        int bytesWritten = r.fillFields( data, new DefaultEscherRecordFactory() );

        assertEquals( 24, bytesWritten );
        assertEquals( 1, r.getDx1() );
        assertEquals( 2, r.getDy1() );
        assertEquals( 3, r.getDx2() );
        assertEquals( 4, r.getDy2() );
        assertEquals( (short) 0x0001, r.getOptions() );
    }

    @Test
    public void testToString(){
        String nl = System.getProperty( "line.separator" );

        String expected =
            "org.apache.poi.ddf.EscherChildAnchorRecord (ChildAnchor):" + nl +
            "  RecordId: 0xF00F" + nl +
            "  Version: 0x0001" + nl +
            "  Instance: 0x0000" + nl +
            "  Options: 0x0001" + nl +
            "  Record Size: 24" + nl +
            "  X1: 0x00000001" + nl +
            "  Y1: 0x00000002" + nl +
            "  X2: 0x00000003" + nl +
            "  Y2: 0x00000004";
        assertEquals( expected, createRecord().toString() );
    }

    private static EscherChildAnchorRecord createRecord() {
        EscherChildAnchorRecord r = new EscherChildAnchorRecord();
        r.setRecordId( EscherChildAnchorRecord.RECORD_ID );
        r.setOptions( (short) 0x0001 );
        r.setDx1( 1 );
        r.setDy1( 2 );
        r.setDx2( 3 );
        r.setDy2( 4 );
        return r;
    }
}

