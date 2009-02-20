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
import org.apache.poi.ddf.EscherClientDataRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherDggRecord;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.hssf.model.DrawingManager2;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the EscherAggregate class.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestEscherAggregate extends TestCase {
    /**
     * Tests that the create aggregate method correctly rejoins escher records together.
     */
    public void testCreateAggregate() {
        String msoDrawingRecord1 =
                "0F 00 02 F0 20 01 00 00 10 00 08 F0 08 00 00 00 \n" +
                "03 00 00 00 02 04 00 00 0F 00 03 F0 08 01 00 00 \n" +
                "0F 00 04 F0 28 00 00 00 01 00 09 F0 10 00 00 00 \n" +
                "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 \n" +
                "02 00 0A F0 08 00 00 00 00 04 00 00 05 00 00 00 \n" +
                "0F 00 04 F0 64 00 00 00 42 01 0A F0 08 00 00 00 \n" +
                "01 04 00 00 00 0A 00 00 73 00 0B F0 2A 00 00 00 \n" +
                "BF 00 08 00 08 00 44 01 04 00 00 00 7F 01 00 00 \n" +
                "01 00 BF 01 00 00 11 00 C0 01 40 00 00 08 FF 01 \n" +
                "10 00 10 00 BF 03 00 00 08 00 00 00 10 F0 12 00 \n" +
                "00 00 00 00 01 00 54 00 05 00 45 00 01 00 88 03 \n" +
                "05 00 94 00 00 00 11 F0 00 00 00 00";

        String msoDrawingRecord2 =
                "0F 00 04 F0 64 00 00 00 42 01 0A F0 08 00 00 00 " +
                "02 04 00 00 80 0A 00 00 73 00 0B F0 2A 00 00 00 " +
                "BF 00 08 00 08 00 44 01 04 00 00 00 7F 01 00 00 " +
                "01 00 BF 01 00 00 11 00 C0 01 40 00 00 08 FF 01 " +
                "10 00 10 00 BF 03 00 00 08 00 00 00 10 F0 12 00 " +
                "00 00 00 00 01 00 8D 03 05 00 E4 00 03 00 4D 03 " +
                "0B 00 0C 00 00 00 11 F0 00 00 00 00";

        DrawingRecord d1 = new DrawingRecord();
        d1.setData( HexRead.readFromString( msoDrawingRecord1 ) );

        ObjRecord r1 = new ObjRecord();

        DrawingRecord d2 = new DrawingRecord();
        d2.setData( HexRead.readFromString( msoDrawingRecord2 ) );

        ObjRecord r2 = new ObjRecord();

        List<Record> records = new ArrayList<Record>();
        records.add( d1 );
        records.add( r1 );
        records.add( d2 );
        records.add( r2 );

        DrawingManager2 drawingManager = new DrawingManager2(new EscherDggRecord() );
        EscherAggregate aggregate = EscherAggregate.createAggregate( records, 0, drawingManager );

        assertEquals( 1, aggregate.getEscherRecords().size() );
        assertEquals( (short) 0xF002, aggregate.getEscherRecord( 0 ).getRecordId() );
        assertEquals( 2, aggregate.getEscherRecord( 0 ).getChildRecords().size() );

//        System.out.println( "aggregate = " + aggregate );
    }

    public void testSerialize() {

        EscherContainerRecord container1 = new EscherContainerRecord();
        EscherContainerRecord spContainer1 = new EscherContainerRecord();
        EscherContainerRecord spContainer2 = new EscherContainerRecord();
        EscherContainerRecord spContainer3 = new EscherContainerRecord();
        EscherSpRecord sp1 = new EscherSpRecord();
        EscherSpRecord sp2 = new EscherSpRecord();
        EscherSpRecord sp3 = new EscherSpRecord();
        EscherClientDataRecord d2 = new EscherClientDataRecord();
        EscherClientDataRecord d3 = new EscherClientDataRecord();

        container1.setOptions( (short) 0x000F );
        spContainer1.setOptions( (short) 0x000F );
        spContainer1.setRecordId( EscherContainerRecord.SP_CONTAINER );
        spContainer2.setOptions( (short) 0x000F );
        spContainer2.setRecordId( EscherContainerRecord.SP_CONTAINER );
        spContainer3.setOptions( (short) 0x000F );
        spContainer3.setRecordId( EscherContainerRecord.SP_CONTAINER );
        d2.setRecordId( EscherClientDataRecord.RECORD_ID );
        d2.setRemainingData( new byte[0] );
        d3.setRecordId( EscherClientDataRecord.RECORD_ID );
        d3.setRemainingData( new byte[0] );
        container1.addChildRecord( spContainer1 );
        container1.addChildRecord( spContainer2 );
        container1.addChildRecord( spContainer3 );
        spContainer1.addChildRecord( sp1 );
        spContainer2.addChildRecord( sp2 );
        spContainer3.addChildRecord( sp3 );
        spContainer2.addChildRecord( d2 );
        spContainer3.addChildRecord( d3 );

        EscherAggregate aggregate = new EscherAggregate(null);
        aggregate.addEscherRecord( container1 );
        aggregate.associateShapeToObjRecord( d2, new ObjRecord() );
        aggregate.associateShapeToObjRecord( d3, new ObjRecord() );

        byte[] data = new byte[112];
        int bytesWritten = aggregate.serialize( 0, data );
        assertEquals( 112, bytesWritten );
        assertEquals( "[EC, 00, 40, 00, 0F, 00, 00, 00, 58, 00, 00, 00, 0F, 00, 04, F0, 10, 00, 00, 00, 00, 00, 0A, F0, 08, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 0F, 00, 04, F0, 18, 00, 00, 00, 00, 00, 0A, F0, 08, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 11, F0, 00, 00, 00, 00, 5D, 00, 00, 00, EC, 00, 20, 00, 0F, 00, 04, F0, 18, 00, 00, 00, 00, 00, 0A, F0, 08, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 11, F0, 00, 00, 00, 00, 5D, 00, 00, 00]",
                HexDump.toHex( data ) );
    }
}
