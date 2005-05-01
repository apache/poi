package org.apache.poi.hssf.model;

import junit.framework.TestCase;
import org.apache.poi.ddf.EscherDggRecord;
import org.apache.poi.ddf.EscherDgRecord;

public class TestDrawingManager2 extends TestCase
{
    private DrawingManager2 drawingManager2;
    private EscherDggRecord dgg;

    protected void setUp() throws Exception
    {
        super.setUp();
        dgg = new EscherDggRecord();
        dgg.setFileIdClusters( new EscherDggRecord.FileIdCluster[0] );
        drawingManager2 = new DrawingManager2( dgg );
    }

    public void testCreateDgRecord() throws Exception
    {
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

    public void testAllocateShapeId() throws Exception
    {
        EscherDgRecord dgRecord1 = drawingManager2.createDgRecord();
        EscherDgRecord dgRecord2 = drawingManager2.createDgRecord();

        assertEquals( 1024, drawingManager2.allocateShapeId( (short)1 ) );
        assertEquals( 1024, dgRecord1.getLastMSOSPID() );
        assertEquals( 1025, dgg.getShapeIdMax() );
        assertEquals( 1025, drawingManager2.allocateShapeId( (short)1 ) );
        assertEquals( 1025, dgRecord1.getLastMSOSPID() );
        assertEquals( 1026, dgg.getShapeIdMax() );
        assertEquals( 1026, drawingManager2.allocateShapeId( (short)1 ) );
        assertEquals( 1026, dgRecord1.getLastMSOSPID() );
        assertEquals( 1027, dgg.getShapeIdMax() );
        assertEquals( 2048, drawingManager2.allocateShapeId( (short)2 ) );
        assertEquals( 2048, dgRecord2.getLastMSOSPID() );
        assertEquals( 2049, dgg.getShapeIdMax() );

        for (int i = 0; i < 1021; i++)
        {
            drawingManager2.allocateShapeId( (short)1 );
            assertEquals( 2049, dgg.getShapeIdMax() );
        }
        assertEquals( 3072, drawingManager2.allocateShapeId( (short) 1 ) );
        assertEquals( 3073, dgg.getShapeIdMax() );

        assertEquals( 2, dgg.getDrawingsSaved() );
        assertEquals( 4, dgg.getNumIdClusters() );
        assertEquals( 1026, dgg.getNumShapesSaved() );
    }
}