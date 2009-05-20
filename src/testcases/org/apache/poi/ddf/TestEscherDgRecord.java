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

public final class TestEscherDgRecord extends TestCase {
    public void testSerialize() {
        EscherDgRecord r = createRecord();

        byte[] data = new byte[16];
        int bytesWritten = r.serialize( 0, data, new NullEscherSerializationListener() );
        assertEquals( 16, bytesWritten );
        assertEquals( "[10, 00, " +
                "08, F0, " +
                "08, 00, 00, 00, " +
                "02, 00, 00, 00, " +     // num shapes in drawing
                "01, 04, 00, 00]",     // The last MSOSPID given to an SP in this DG
                HexDump.toHex( data ) );
    }

    public void testFillFields() {
        String hexData = "10 00 " +
                "08 F0 " +
                "08 00 00 00 " +
                "02 00 00 00 " +
                "01 04 00 00 ";
        byte[] data = HexRead.readFromString( hexData );
        EscherDgRecord r = new EscherDgRecord();
        int bytesWritten = r.fillFields( data, new DefaultEscherRecordFactory() );

        assertEquals( 16, bytesWritten );
        assertEquals( 2, r.getNumShapes() );
        assertEquals( 1025, r.getLastMSOSPID() );
    }

    public void testToString() {
        String expected = "org.apache.poi.ddf.EscherDgRecord:" + '\n' +
                "  RecordId: 0xF008" + '\n' +
                "  Options: 0x0010" + '\n' +
                "  NumShapes: 2" + '\n' +
                "  LastMSOSPID: 1025" + '\n';
        assertEquals( expected, createRecord().toString() );
    }

    private static EscherDgRecord createRecord()
    {
        EscherDgRecord r = new EscherDgRecord();
        r.setOptions( (short) 0x0010 );
        r.setRecordId( EscherDgRecord.RECORD_ID );
        r.setNumShapes(2);
        r.setLastMSOSPID(1025);
        return r;
    }
}
