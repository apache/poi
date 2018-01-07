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

public class TestEscherClientAnchorRecord {
    @Test
    public void testSerialize() {
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
    public void testFillFields() {
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
    public void testToString() {
        String nl = System.getProperty("line.separator");
        String expected =
            "org.apache.poi.ddf.EscherClientAnchorRecord (ClientAnchor):" + nl +
            "  RecordId: 0xF010" + nl +
            "  Version: 0x0001" + nl +
            "  Instance: 0x0000" + nl +
            "  Options: 0x0001" + nl +
            "  Record Size: 28" + nl +
            "  Flag: 0x004D" + nl +
            "  Col1: 0x0037" + nl +
            "  DX1: 0x0021" + nl +
            "  Row1: 0x0058" + nl +
            "  DY1: 0x000B" + nl +
            "  Col2: 0x002C" + nl +
            "  DX2: 0x0016" + nl +
            "  Row2: 0x0063" + nl +
            "  DY2: 0x0042" + nl +
            "  Extra Data: " + nl +
            "     0: FF, DD";
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
