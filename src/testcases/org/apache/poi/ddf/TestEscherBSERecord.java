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

import junit.framework.TestCase;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;

public final class TestEscherBSERecord extends TestCase {
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

    public void testSerialize() {
        EscherBSERecord r = createRecord();

        byte[] data = new byte[8 + 36];
        int bytesWritten = r.serialize( 0, data, new NullEscherSerializationListener() );
        assertEquals( 44, bytesWritten );
        assertEquals( "[01, 00, 00, 00, 24, 00, 00, 00, 05, 05, 01, 02, 03, 04, " +
                "05, 06, 07, 08, 09, 0A, 0B, 0C, 0D, 0E, 0F, 00, 01, 00, 00, 00, " +
                "00, 00, 02, 00, 00, 00, 03, 00, 00, 00, 04, 05, 06, 07]",
                HexDump.toHex(data));

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

    public void testToString() {
        EscherBSERecord record = createRecord();
        String expected = "org.apache.poi.ddf.EscherBSERecord:" + '\n' +
                "  RecordId: 0xF007" + '\n' +
                "  Options: 0x0001" + '\n' +
                "  BlipTypeWin32: 5" + '\n' +
                "  BlipTypeMacOS: 5" + '\n' +
                "  SUID: [01, 02, 03, 04, 05, 06, 07, 08, 09, 0A, 0B, 0C, 0D, 0E, 0F, 00]" + '\n' +
                "  Tag: 1" + '\n' +
                "  Size: 0" + '\n' +
                "  Ref: 2" + '\n' +
                "  Offset: 3" + '\n' +
                "  Usage: 4" + '\n' +
                "  Name: 5" + '\n' +
                "  Unused2: 6" + '\n' +
                "  Unused3: 7" + '\n' +
                "  blipRecord: null" + '\n' +
                "  Extra Data:" + '\n' +
                ": 0";
        String actual = record.toString();
        assertEquals( expected, actual );
    }

}
