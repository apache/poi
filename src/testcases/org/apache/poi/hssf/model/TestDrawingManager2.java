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

package org.apache.poi.hssf.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.ddf.EscherDgRecord;
import org.apache.poi.ddf.EscherDggRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class TestDrawingManager2 {
    private DrawingManager2 drawingManager2;
    private EscherDggRecord dgg;

    @BeforeEach
    void setUp() {
        dgg = new EscherDggRecord();
        dgg.setFileIdClusters( new EscherDggRecord.FileIdCluster[0] );
        drawingManager2 = new DrawingManager2( dgg );
    }

    @Test
    void testCreateDgRecord() {
        EscherDgRecord dgRecord1 = drawingManager2.createDgRecord();
        assertEquals( 1, dgRecord1.getDrawingGroupId() );
        assertEquals( -1, dgRecord1.getLastMSOSPID() );

        EscherDgRecord dgRecord2 = drawingManager2.createDgRecord();
        assertEquals( 2, dgRecord2.getDrawingGroupId() );
        assertEquals( -1, dgRecord2.getLastMSOSPID() );

        assertEquals( 2, dgg.getDrawingsSaved( ) );
        assertEquals( 2, dgg.getFileIdClusters().length );
        assertEquals( 3, dgg.getNumIdClusters() );
        assertEquals( 0, dgg.getNumShapesSaved() );
    }

    @Test
    void testCreateDgRecordOld() {
        // converted from TestDrawingManager(1)
        EscherDggRecord dgg = new EscherDggRecord();
        dgg.setDrawingsSaved( 0 );
        dgg.setFileIdClusters( new EscherDggRecord.FileIdCluster[]{} );
        DrawingManager2 dm = new DrawingManager2( dgg );

        EscherDgRecord dgRecord = dm.createDgRecord();
        assertEquals( -1, dgRecord.getLastMSOSPID() );
        assertEquals( 0, dgRecord.getNumShapes() );
        assertEquals( 1, dm.getDgg().getDrawingsSaved() );
        assertEquals( 1, dm.getDgg().getFileIdClusters().length );
        assertEquals( 1, dm.getDgg().getFileIdClusters()[0].getDrawingGroupId() );
        assertEquals( 0, dm.getDgg().getFileIdClusters()[0].getNumShapeIdsUsed() );
    }

    @Test
    void testAllocateShapeId() {
        EscherDgRecord dgRecord1 = drawingManager2.createDgRecord();
        assertEquals( 1, dgg.getDrawingsSaved() );
        EscherDgRecord dgRecord2 = drawingManager2.createDgRecord();
        assertEquals( 2, dgg.getDrawingsSaved() );

        assertEquals( 1024, drawingManager2.allocateShapeId( dgRecord1 ) );
        assertEquals( 1024, dgRecord1.getLastMSOSPID() );
        assertEquals( 1025, dgg.getShapeIdMax() );
        assertEquals( 1, dgg.getFileIdClusters()[0].getDrawingGroupId() );
        assertEquals( 1, dgg.getFileIdClusters()[0].getNumShapeIdsUsed() );
        assertEquals( 1, dgRecord1.getNumShapes() );
        assertEquals( 1025, drawingManager2.allocateShapeId( dgRecord1 ) );
        assertEquals( 1025, dgRecord1.getLastMSOSPID() );
        assertEquals( 1026, dgg.getShapeIdMax() );
        assertEquals( 1026, drawingManager2.allocateShapeId( dgRecord1 ) );
        assertEquals( 1026, dgRecord1.getLastMSOSPID() );
        assertEquals( 1027, dgg.getShapeIdMax() );
        assertEquals( 2048, drawingManager2.allocateShapeId( dgRecord2 ) );
        assertEquals( 2048, dgRecord2.getLastMSOSPID() );
        assertEquals( 2049, dgg.getShapeIdMax() );

        for (int i = 0; i < 1021; i++)
        {
            drawingManager2.allocateShapeId( dgRecord1 );
            assertEquals( 2049, dgg.getShapeIdMax() );
        }
        assertEquals( 3072, drawingManager2.allocateShapeId( dgRecord1 ) );
        assertEquals( 3073, dgg.getShapeIdMax() );

        assertEquals( 2, dgg.getDrawingsSaved() );
        assertEquals( 4, dgg.getNumIdClusters() );
        assertEquals( 1026, dgg.getNumShapesSaved() );
    }

    @Test
    void testFindNewDrawingGroupId() {
        // converted from TestDrawingManager(1)
        EscherDggRecord dgg = new EscherDggRecord();
        dgg.setDrawingsSaved( 1 );
        dgg.setFileIdClusters( new EscherDggRecord.FileIdCluster[]{
            new EscherDggRecord.FileIdCluster( 2, 10 )} );
        DrawingManager2 dm = new DrawingManager2( dgg );
        assertEquals( 1, dm.findNewDrawingGroupId() );
        dgg.setFileIdClusters( new EscherDggRecord.FileIdCluster[]{
            new EscherDggRecord.FileIdCluster( 1, 10 ),
            new EscherDggRecord.FileIdCluster( 2, 10 )} );
        assertEquals( 3, dm.findNewDrawingGroupId() );
    }
}
