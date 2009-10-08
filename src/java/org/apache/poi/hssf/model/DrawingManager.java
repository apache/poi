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

import org.apache.poi.ddf.EscherDggRecord;
import org.apache.poi.ddf.EscherDgRecord;

import java.util.Map;
import java.util.HashMap;

/**
 * Provides utilities to manage drawing groups.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class DrawingManager
{
    EscherDggRecord dgg;
    Map dgMap = new HashMap(); // key = Short(drawingId), value=EscherDgRecord

    public DrawingManager( EscherDggRecord dgg )
    {
        this.dgg = dgg;
    }

    public EscherDgRecord createDgRecord()
    {
        EscherDgRecord dg = new EscherDgRecord();
        dg.setRecordId( EscherDgRecord.RECORD_ID );
        short dgId = findNewDrawingGroupId();
        dg.setOptions( (short) ( dgId << 4 ) );
        dg.setNumShapes( 0 );
        dg.setLastMSOSPID( -1 );
        dgg.addCluster( dgId, 0 );
        dgg.setDrawingsSaved( dgg.getDrawingsSaved() + 1 );
        dgMap.put( Short.valueOf( dgId ), dg );
        return dg;
    }

    /**
     * Allocates new shape id for the new drawing group id.
     *
     * @return a new shape id.
     */
    public int allocateShapeId(short drawingGroupId)
    {
        // Get the last shape id for this drawing group.
        EscherDgRecord dg = (EscherDgRecord) dgMap.get(Short.valueOf(drawingGroupId));
        int lastShapeId = dg.getLastMSOSPID();


        // Have we run out of shapes for this cluster?
        int newShapeId = 0;
        if (lastShapeId % 1024 == 1023)
        {
            // Yes:
                // Find the starting shape id of the next free cluster
            newShapeId = findFreeSPIDBlock();
                // Create a new cluster in the dgg record.
            dgg.addCluster(drawingGroupId, 1);
        }
        else
        {
            // No:
                // Find the cluster for this drawing group with free space.
            for (int i = 0; i < dgg.getFileIdClusters().length; i++)
            {
                EscherDggRecord.FileIdCluster c = dgg.getFileIdClusters()[i];
                if (c.getDrawingGroupId() == drawingGroupId)
                {
                    if (c.getNumShapeIdsUsed() != 1024)
                    {
                        // Increment the number of shapes used for this cluster.
                        c.incrementShapeId();
                    }
                }
                // If the last shape id = -1 then we know to find a free block;
                if (dg.getLastMSOSPID() == -1)
                {
                    newShapeId = findFreeSPIDBlock();
                }
                else
                {
                    // The new shape id to be the last shapeid of this cluster + 1
                    newShapeId = dg.getLastMSOSPID() + 1;
                }
            }
        }
        // Increment the total number of shapes used in the dgg.
        dgg.setNumShapesSaved(dgg.getNumShapesSaved() + 1);
        // Is the new shape id >= max shape id for dgg?
        if (newShapeId >= dgg.getShapeIdMax())
        {
            // Yes:
                // Set the max shape id = new shape id + 1
            dgg.setShapeIdMax(newShapeId + 1);
        }
        // Set last shape id for this drawing group.
        dg.setLastMSOSPID(newShapeId);
        // Increased the number of shapes used for this drawing group.
        dg.incrementShapeCount();


        return newShapeId;
    }

    ////////////  Non-public methods /////////////
    short findNewDrawingGroupId()
    {
        short dgId = 1;
        while ( drawingGroupExists( dgId ) )
            dgId++;
        return dgId;
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
