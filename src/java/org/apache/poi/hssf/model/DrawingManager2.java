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

import org.apache.poi.ddf.EscherDgRecord;
import org.apache.poi.ddf.EscherDggRecord;

import java.util.List;
import java.util.ArrayList;


/**
 * Provides utilities to manage drawing groups.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class DrawingManager2
{
    EscherDggRecord dgg;
    List drawingGroups = new ArrayList( );


    public DrawingManager2( EscherDggRecord dgg )
    {
        this.dgg = dgg;
    }
    
    /**
     * Clears the cached list of drawing groups
     */
    public void clearDrawingGroups() {
    	drawingGroups.clear(); 
    }

    public EscherDgRecord createDgRecord()
    {
        EscherDgRecord dg = new EscherDgRecord();
        dg.setRecordId( EscherDgRecord.RECORD_ID );
        short dgId = findNewDrawingGroupId();
        dg.setOptions( (short) ( dgId << 4 ) );
        dg.setNumShapes( 0 );
        dg.setLastMSOSPID( -1 );
        drawingGroups.add(dg);
        dgg.addCluster( dgId, 0 );
        dgg.setDrawingsSaved( dgg.getDrawingsSaved() + 1 );
        return dg;
    }

    /**
     * Allocates new shape id for the new drawing group id.
     *
     * @return a new shape id.
     */
    public int allocateShapeId(short drawingGroupId)
    {
        EscherDgRecord dg = getDrawingGroup(drawingGroupId);
        return allocateShapeId(drawingGroupId, dg);
    }

    /**
     * Allocates new shape id for the new drawing group id.
     *
     * @return a new shape id.
     */
    public int allocateShapeId(short drawingGroupId, EscherDgRecord dg)
    {
        dgg.setNumShapesSaved( dgg.getNumShapesSaved() + 1 );

        // Add to existing cluster if space available
        for (int i = 0; i < dgg.getFileIdClusters().length; i++)
        {
            EscherDggRecord.FileIdCluster c = dgg.getFileIdClusters()[i];
            if (c.getDrawingGroupId() == drawingGroupId && c.getNumShapeIdsUsed() != 1024)
            {
                int result = c.getNumShapeIdsUsed() + (1024 * (i+1));
                c.incrementShapeId();
                dg.setNumShapes( dg.getNumShapes() + 1 );
                dg.setLastMSOSPID( result );
                if (result >= dgg.getShapeIdMax())
                    dgg.setShapeIdMax( result + 1 );
                return result;
            }
        }

        // Create new cluster
        dgg.addCluster( drawingGroupId, 0 );
        dgg.getFileIdClusters()[dgg.getFileIdClusters().length-1].incrementShapeId();
        dg.setNumShapes( dg.getNumShapes() + 1 );
        int result = (1024 * dgg.getFileIdClusters().length);
        dg.setLastMSOSPID( result );
        if (result >= dgg.getShapeIdMax())
            dgg.setShapeIdMax( result + 1 );
        return result;
    }
    ////////////  Non-public methods /////////////
    
    /**
     * Finds the next available (1 based) drawing group id
     */
    short findNewDrawingGroupId()
    {
        short dgId = 1; 
        while ( drawingGroupExists( dgId ) )
            dgId++;
        return dgId;
    }

    EscherDgRecord getDrawingGroup(int drawingGroupId)
    {
        return (EscherDgRecord) drawingGroups.get(drawingGroupId-1);
    }

    boolean drawingGroupExists( short dgId )
    {
        for ( int i = 0; i < dgg.getFileIdClusters().length; i++ )
        {
            if ( dgg.getFileIdClusters()[i].getDrawingGroupId() == dgId )
                return true;
        }
        return false;
    }

    int findFreeSPIDBlock()
    {
        int max = dgg.getShapeIdMax();
        int next = ( ( max / 1024 ) + 1 ) * 1024;
        return next;
    }

    public EscherDggRecord getDgg()
    {
        return dgg;
    }

}
