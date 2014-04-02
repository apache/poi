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

import junit.framework.TestCase;
import org.apache.poi.ddf.EscherDggRecord;
import org.apache.poi.ddf.EscherDgRecord;

public final class TestDrawingManager extends TestCase {
    public void testFindFreeSPIDBlock() {
        EscherDggRecord dgg = new EscherDggRecord();
        DrawingManager dm = new DrawingManager( dgg );
        dgg.setShapeIdMax( 1024 );
        assertEquals( 2048, dm.findFreeSPIDBlock() );
        dgg.setShapeIdMax( 1025 );
        assertEquals( 2048, dm.findFreeSPIDBlock() );
        dgg.setShapeIdMax( 2047 );
        assertEquals( 2048, dm.findFreeSPIDBlock() );
    }

    public void testFindNewDrawingGroupId() {
        EscherDggRecord dgg = new EscherDggRecord();
        dgg.setDrawingsSaved( 1 );
        dgg.setFileIdClusters( new EscherDggRecord.FileIdCluster[]{
            new EscherDggRecord.FileIdCluster( 2, 10 )} );
        DrawingManager dm = new DrawingManager( dgg );
        assertEquals( 1, dm.findNewDrawingGroupId() );
        dgg.setFileIdClusters( new EscherDggRecord.FileIdCluster[]{
            new EscherDggRecord.FileIdCluster( 1, 10 ),
            new EscherDggRecord.FileIdCluster( 2, 10 )} );
        assertEquals( 3, dm.findNewDrawingGroupId() );
    }

    public void testDrawingGroupExists() {
        EscherDggRecord dgg = new EscherDggRecord();
        dgg.setDrawingsSaved( 1 );
        dgg.setFileIdClusters( new EscherDggRecord.FileIdCluster[]{
            new EscherDggRecord.FileIdCluster( 2, 10 )} );
        DrawingManager dm = new DrawingManager( dgg );
        assertFalse( dm.drawingGroupExists( (short) 1 ) );
        assertTrue( dm.drawingGroupExists( (short) 2 ) );
        assertFalse( dm.drawingGroupExists( (short) 3 ) );
    }

    public void testCreateDgRecord() {
        EscherDggRecord dgg = new EscherDggRecord();
        dgg.setDrawingsSaved( 0 );
        dgg.setFileIdClusters( new EscherDggRecord.FileIdCluster[]{} );
        DrawingManager dm = new DrawingManager( dgg );

        EscherDgRecord dgRecord = dm.createDgRecord();
        assertEquals( -1, dgRecord.getLastMSOSPID() );
        assertEquals( 0, dgRecord.getNumShapes() );
        assertEquals( 1, dm.getDgg().getDrawingsSaved() );
        assertEquals( 1, dm.getDgg().getFileIdClusters().length );
        assertEquals( 1, dm.getDgg().getFileIdClusters()[0].getDrawingGroupId() );
        assertEquals( 0, dm.getDgg().getFileIdClusters()[0].getNumShapeIdsUsed() );
    }

    public void testAllocateShapeId() {
        EscherDggRecord dgg = new EscherDggRecord();
        dgg.setDrawingsSaved( 0 );
        dgg.setFileIdClusters( new EscherDggRecord.FileIdCluster[]{} );
        DrawingManager dm = new DrawingManager( dgg );

        EscherDgRecord dg = dm.createDgRecord();
        int shapeId = dm.allocateShapeId( dg.getDrawingGroupId() );
        assertEquals( 1024, shapeId );
        assertEquals( 1025, dgg.getShapeIdMax() );
        assertEquals( 1, dgg.getDrawingsSaved() );
        assertEquals( 1, dgg.getFileIdClusters()[0].getDrawingGroupId() );
        assertEquals( 1, dgg.getFileIdClusters()[0].getNumShapeIdsUsed() );
        assertEquals( 1024, dg.getLastMSOSPID() );
        assertEquals( 1, dg.getNumShapes() );
    }
}
