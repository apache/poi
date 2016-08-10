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

package org.apache.poi.hssf.record;

import junit.framework.TestCase;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.util.HexDump;

public final class TestDrawingGroupRecord extends TestCase {
    private static final int MAX_RECORD_SIZE = 8228;
    private static final int MAX_DATA_SIZE = MAX_RECORD_SIZE - 4;

    public void testGetRecordSize() {
        DrawingGroupRecord r = new DrawingGroupRecord();
        assertEquals(4, r.getRecordSize());

        EscherSpRecord sp = new EscherSpRecord();
        sp.setRecordId(EscherSpRecord.RECORD_ID);
        sp.setOptions((short) 0x1111);
        sp.setFlags(-1);
        sp.setShapeId(-1);
        EscherContainerRecord dggContainer = new EscherContainerRecord();
        dggContainer.setOptions((short) 0x000F);
        dggContainer.setRecordId((short) 0xF000);
        dggContainer.addChildRecord(sp);

        r.addEscherRecord(dggContainer);
        assertEquals(28, r.getRecordSize());

        byte[] data = new byte[28];
        int size = r.serialize(0, data);
        assertEquals("[EB, 00, 18, 00, 0F, 00, 00, F0, 10, 00, 00, 00, 11, 11, 0A, F0, 08, 00, 00, 00, FF, FF, FF, FF, FF, FF, FF, FF]", HexDump.toHex(data));
        assertEquals(28, size);

        assertEquals(24, dggContainer.getRecordSize());


        r = new DrawingGroupRecord( );
        r.setRawData( new byte[MAX_DATA_SIZE] );
        assertEquals( MAX_RECORD_SIZE, r.getRecordSize() );
        r.setRawData( new byte[MAX_DATA_SIZE+1] );
        assertEquals( MAX_RECORD_SIZE + 5, r.getRecordSize() );
        r.setRawData( new byte[MAX_DATA_SIZE*2] );
        assertEquals( MAX_RECORD_SIZE * 2, r.getRecordSize() );
        r.setRawData( new byte[MAX_DATA_SIZE*2 + 1] );
        assertEquals( MAX_RECORD_SIZE * 2 + 5, r.getRecordSize() );
    }

    public void testSerialize() {
        // Check under max record size
        DrawingGroupRecord r = new DrawingGroupRecord();
        byte[] rawData = new byte[100];
        rawData[0] = 100;
        rawData[99] = (byte) 200;
        r.setRawData( rawData );
        byte[] buffer = new byte[r.getRecordSize()];
        int size = r.serialize( 0, buffer );
        assertEquals( 104, size );
        assertEquals("[EB, 00, 64, 00, 64, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, C8]", HexDump.toHex(buffer));

        // check at max record size
        rawData = new byte[MAX_DATA_SIZE];
        r.setRawData( rawData );
        buffer = new byte[r.getRecordSize()];
        size = r.serialize( 0, buffer );
        assertEquals( MAX_RECORD_SIZE, size );

        // check over max record size
        rawData = new byte[MAX_DATA_SIZE+1];
        rawData[rawData.length-1] = (byte) 255;
        r.setRawData( rawData );
        buffer = new byte[r.getRecordSize()];
        size = r.serialize( 0, buffer );
        assertEquals( MAX_RECORD_SIZE + 5, size );
        assertEquals( "[EB, 00, 20, 20]", HexDump.toHex(cut(buffer, 0, 4) ));
        assertEquals( "[00, EB, 00, 01, 00, FF]", HexDump.toHex(cut(buffer, MAX_RECORD_SIZE - 1, MAX_RECORD_SIZE + 5) ));

        // check continue record
        rawData = new byte[MAX_DATA_SIZE * 2 + 1];
        rawData[rawData.length-1] = (byte) 255;
        r.setRawData( rawData );
        buffer = new byte[r.getRecordSize()];
        size = r.serialize( 0, buffer );
        assertEquals( MAX_RECORD_SIZE * 2 + 5, size );
        assertEquals( MAX_RECORD_SIZE * 2 + 5, r.getRecordSize() );
        assertEquals( "[EB, 00, 20, 20]", HexDump.toHex(cut(buffer, 0, 4) ));
        assertEquals( "[EB, 00, 20, 20]", HexDump.toHex(cut(buffer, MAX_RECORD_SIZE, MAX_RECORD_SIZE + 4) ));
        assertEquals( "[3C, 00, 01, 00, FF]", HexDump.toHex(cut(buffer, MAX_RECORD_SIZE * 2, MAX_RECORD_SIZE * 2 + 5) ));

        // check continue record
        rawData = new byte[664532];
        r.setRawData( rawData );
        buffer = new byte[r.getRecordSize()];
        size = r.serialize( 0, buffer );
        assertEquals( 664856, size );
        assertEquals( 664856, r.getRecordSize() );
    }

    private static byte[] cut( byte[] data, int fromInclusive, int toExclusive )
    {
        int length = toExclusive - fromInclusive;
        byte[] result = new byte[length];
        System.arraycopy( data, fromInclusive, result, 0, length);
        return result;
    }

    public void testGrossSizeFromDataSize() {
        for (int i = 0; i < MAX_RECORD_SIZE * 4; i += 11)
        {
            //System.out.print( "data size = " + i + ", gross size = " + DrawingGroupRecord.grossSizeFromDataSize( i ) );
            //System.out.println( "  Diff: " + (DrawingGroupRecord.grossSizeFromDataSize( i ) - i) );
        }

        assertEquals( 4, DrawingGroupRecord.grossSizeFromDataSize( 0 ) );
        assertEquals( 5, DrawingGroupRecord.grossSizeFromDataSize( 1 ) );
        assertEquals( MAX_RECORD_SIZE, DrawingGroupRecord.grossSizeFromDataSize( MAX_DATA_SIZE ) );
        assertEquals( MAX_RECORD_SIZE + 5, DrawingGroupRecord.grossSizeFromDataSize( MAX_DATA_SIZE + 1 ) );
        assertEquals( MAX_RECORD_SIZE * 2, DrawingGroupRecord.grossSizeFromDataSize( MAX_DATA_SIZE * 2 ) );
        assertEquals( MAX_RECORD_SIZE * 2 + 5, DrawingGroupRecord.grossSizeFromDataSize( MAX_DATA_SIZE * 2 + 1 ) );
    }
}
