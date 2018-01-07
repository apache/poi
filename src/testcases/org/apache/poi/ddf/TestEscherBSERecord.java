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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.poi.poifs.storage.RawDataUtil;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;
import org.junit.Test;

public final class TestEscherBSERecord {
    @Test
    public void testFillFields() {
        String data = "01 00 00 00 24 00 00 00 05 05 01 02 03 04 " +
                " 05 06 07 08 09 0A 0B 0C 0D 0E 0F 00 01 00 00 00 " +
                " 00 00 02 00 00 00 03 00 00 00 04 05 06 07";
        EscherBSERecord r = new EscherBSERecord();
        int bytesWritten = r.fillFields( HexRead.readFromString( data ), 0, new DefaultEscherRecordFactory() );
        assertEquals( 44, bytesWritten );
        assertEquals( (short) 0x0001, r.getOptions() );
        assertEquals( EscherBSERecord.BT_JPEG, r.getBlipTypeWin32() );
        assertEquals( EscherBSERecord.BT_JPEG, r.getBlipTypeMacOS() );
        assertEquals( "[01, 02, 03, 04, 05, 06, 07, 08, 09, 0A, 0B, 0C, 0D, 0E, 0F, 00]", HexDump.toHex( r.getUid() ) );
        assertEquals( (short) 1, r.getTag() );
        assertEquals( 2, r.getRef() );
        assertEquals( 3, r.getOffset() );
        assertEquals( (byte) 4, r.getUsage() );
        assertEquals( (byte) 5, r.getName() );
        assertEquals( (byte) 6, r.getUnused2() );
        assertEquals( (byte) 7, r.getUnused3() );
        assertEquals( 0, r.getRemainingData().length );
    }

    @Test
    public void testSerialize() throws IOException {
        EscherBSERecord r = createRecord();
        String exp64 = "H4sIAAAAAAAAAGNkYP+gwsDAwMrKyMTMwsrGzsHJxc3Dy8fPwMgAAkxAzAzEICkAgs9OoSwAAAA=";
        byte[] expected = RawDataUtil.decompress(exp64);
        
        byte[] data = new byte[8 + 36];
        int bytesWritten = r.serialize( 0, data, new NullEscherSerializationListener() );
        assertEquals(data.length, bytesWritten);
        assertArrayEquals(expected, data);
    }

    private EscherBSERecord createRecord() {
        EscherBSERecord r = new EscherBSERecord();
        r.setOptions( (short) 0x0001 );
        r.setBlipTypeWin32( EscherBSERecord.BT_JPEG );
        r.setBlipTypeMacOS( EscherBSERecord.BT_JPEG );
        r.setUid( HexRead.readFromString( "01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 00" ) );
        r.setTag( (short) 1 );
        r.setRef( 2 );
        r.setOffset( 3 );
        r.setUsage( (byte) 4 );
        r.setName( (byte) 5 );
        r.setUnused2( (byte) 6 );
        r.setUnused3( (byte) 7 );
        r.setRemainingData( new byte[0] );
        return r;

    }

    @Test
    public void testToString() {
        String nl = System.getProperty("line.separator");
        EscherBSERecord record = createRecord();
        String expected =
            "org.apache.poi.ddf.EscherBSERecord (BSE):" + nl +
            "  RecordId: 0xF007" + nl +
            "  Version: 0x0001" + nl +
            "  Instance: 0x0000" + nl +
            "  Options: 0x0001" + nl +
            "  Record Size: 44" + nl +
            "  BlipTypeWin32: 0x05" + nl +
            "  BlipTypeMacOS: 0x05" + nl +
            "  SUID: " + nl +
            "     00: 01, 02, 03, 04, 05, 06, 07, 08, 09, 0A, 0B, 0C, 0D, 0E, 0F, 00" + nl +
            "  Tag: 0x0001" + nl +
            "  Size: 0x00000000" + nl +
            "  Ref: 0x00000002" + nl +
            "  Offset: 0x00000003" + nl +
            "  Usage: 0x04" + nl +
            "  Name: 0x05" + nl +
            "  Unused2: 0x06" + nl +
            "  Unused3: 0x07" + nl +
            "  Extra Data: " + nl +
            "     : 0";                
        String actual = record.toString();
        assertEquals( expected, actual );
    }

}
