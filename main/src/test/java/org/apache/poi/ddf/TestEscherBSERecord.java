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
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;
import org.junit.jupiter.api.Test;

final class TestEscherBSERecord {
    @Test
    void testFillFields() {
        String data = "01 00 00 00 24 00 00 00 05 05 01 02 03 04 " +
                " 05 06 07 08 09 0A 0B 0C 0D 0E 0F 00 01 00 00 00 " +
                " 00 00 02 00 00 00 03 00 00 00 04 05 06 07";
        EscherBSERecord r = new EscherBSERecord();
        int bytesWritten = r.fillFields( HexRead.readFromString( data ), 0, new DefaultEscherRecordFactory() );
        assertEquals( 44, bytesWritten );
        assertEquals( (short) 0x0001, r.getOptions() );
        assertEquals( PictureData.PictureType.JPEG.nativeId, r.getBlipTypeWin32() );
        assertEquals( PictureData.PictureType.JPEG.nativeId, r.getBlipTypeMacOS() );
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
    void testSerialize() throws IOException {
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
        r.setBlipTypeWin32( (byte)PictureData.PictureType.JPEG.nativeId );
        r.setBlipTypeMacOS( (byte)PictureData.PictureType.JPEG.nativeId );
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
    void testToString() {
        EscherBSERecord record = createRecord();
        String expected =
            "{   /* BSE */\n" +
            "\t  \"recordId\": -4089 /* 0xf007 */\n" +
            "\t, \"version\": 1\n" +
            "\t, \"instance\": 0\n" +
            "\t, \"options\": 1\n" +
            "\t, \"recordSize\": 44 /* 0x0000002c */\n" +
            "\t, \"blipTypeWin32\": 5\n" +
            "\t, \"pictureTypeWin32\": \"JPEG\"\n" +
            "\t, \"blipTypeMacOS\": 5\n" +
            "\t, \"pictureTypeMacOS\": \"JPEG\"\n" +
            "\t, \"suid\": \"AQIDBAUGBwgJCgsMDQ4PAA==\"\n" +
            "\t, \"tag\": 1\n" +
            "\t, \"size\": 0\n" +
            "\t, \"ref\": 2\n" +
            "\t, \"offset\": 3\n" +
            "\t, \"usage\": 4\n" +
            "\t, \"name\": 5\n" +
            "\t, \"unused2\": 6\n" +
            "\t, \"unused3\": 7\n" +
            "\t, \"blipRecord\": null\n" +
            "\t, \"remainingData\": \"\"\n" +
            "}";
        String actual = record.toString().replace("\r", "");
        assertEquals( expected, actual );
    }
}
