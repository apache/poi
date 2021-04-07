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

final class TestEscherDggRecord {
    @Test
    void testSerialize() {
        EscherDggRecord r = createRecord();

        byte[] data = new byte[32];
        int bytesWritten = r.serialize( 0, data, new NullEscherSerializationListener() );
        assertEquals( 32, bytesWritten );
        assertEquals( "[00, 00, " +
                "06, F0, " +
                "18, 00, 00, 00, " +
                "02, 04, 00, 00, " +
                "02, 00, 00, 00, " +
                "02, 00, 00, 00, " +
                "01, 00, 00, 00, " +
                "01, 00, 00, 00, 02, 00, 00, 00]",
                HexDump.toHex( data ) );
    }

    @Test
    void testFillFields() {
        String hexData = "00 00 " +
                "06 F0 " +
                "18 00 00 00 " +
                "02 04 00 00 " +
                "02 00 00 00 " +
                "02 00 00 00 " +
                "01 00 00 00 " +
                "01 00 00 00 02 00 00 00";
        byte[] data = HexRead.readFromString( hexData );
        EscherDggRecord r = new EscherDggRecord();
        int bytesWritten = r.fillFields( data, new DefaultEscherRecordFactory() );

        assertEquals( 32, bytesWritten );
        assertEquals( 0x402, r.getShapeIdMax() );
        assertEquals( 0x02, r.getNumIdClusters() );
        assertEquals( 0x02, r.getNumShapesSaved() );
        assertEquals( 0x01, r.getDrawingsSaved() );
        assertEquals( 1, r.getFileIdClusters().length );
        assertEquals( 0x01, r.getFileIdClusters()[0].getDrawingGroupId());
        assertEquals( 0x02, r.getFileIdClusters()[0].getNumShapeIdsUsed());
    }

    @Test
    void testToString() {
        String expected =
            "{   /* DGG */\n" +
            "\t  \"recordId\": -4090 /* 0xf006 */\n" +
            "\t, \"version\": 0\n" +
            "\t, \"instance\": 0\n" +
            "\t, \"options\": 0\n" +
            "\t, \"recordSize\": 32 /* 0x00000020 */\n" +
            "\t, \"fileIdClusters\": [\n" +
            "\t{   /* FileIdCluster */\n" +
            "\t\t  \"drawingGroupId\": 1\n" +
            "\t\t, \"numShapeIdUsed\": 2\n" +
            "\t}\t]\n" +
            "\t, \"shapeIdMax\": 1026 /* 0x00000402 */\n" +
            "\t, \"numIdClusters\": 2\n" +
            "\t, \"numShapesSaved\": 2\n" +
            "\t, \"drawingsSaved\": 1\n" +
            "}";
        expected = expected.replace("\n", System.getProperty("line.separator"));
        assertEquals( expected, createRecord().toString() );
    }

    private static EscherDggRecord createRecord()
    {
        EscherDggRecord r = new EscherDggRecord();
        r.setOptions( (short) 0x0000 );
        r.setRecordId( EscherDggRecord.RECORD_ID );
        r.setShapeIdMax( 0x402 );
        r.setNumShapesSaved( 0x02 );
        r.setDrawingsSaved( 0x01 );
        r.setFileIdClusters(new EscherDggRecord.FileIdCluster[] {
            new EscherDggRecord.FileIdCluster( 1, 2 )
        });
        return r;
    }

    @Test
    void testGetRecordSize() {
        EscherDggRecord r = new EscherDggRecord();
        r.setFileIdClusters(new EscherDggRecord.FileIdCluster[] { new EscherDggRecord.FileIdCluster(0,0) } );
        assertEquals(32,r.getRecordSize());
    }
}
