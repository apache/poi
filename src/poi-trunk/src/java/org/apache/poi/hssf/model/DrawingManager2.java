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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ddf.EscherDgRecord;
import org.apache.poi.ddf.EscherDggRecord;
import org.apache.poi.util.Removal;


/**
 * Provides utilities to manage drawing groups.
 */
public class DrawingManager2 {
    private final EscherDggRecord dgg;
    private final List<EscherDgRecord> drawingGroups = new ArrayList<>();


    public DrawingManager2( EscherDggRecord dgg ) {
        this.dgg = dgg;
    }
    
    /**
     * Clears the cached list of drawing groups
     */
    public void clearDrawingGroups() {
    	drawingGroups.clear(); 
    }

    /**
     * Creates a new drawing group 
     *
     * @return a new drawing group
     */
    public EscherDgRecord createDgRecord() {
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
     * Allocates new shape id for the drawing group id.
     * 
     * @param drawingGroupId the drawing group id
     * 
     * @return a new shape id
     * 
     * @deprecated in POI 3.17-beta2, use allocateShapeId(EscherDgRecord) 
     */
    @Deprecated
    @Removal(version="4.0")
    public int allocateShapeId(short drawingGroupId) {
        for (EscherDgRecord dg : drawingGroups) {
            if (dg.getDrawingGroupId() == drawingGroupId) {
                return allocateShapeId(dg);
            }
        }
        throw new IllegalStateException("Drawing group id "+drawingGroupId+" doesn't exist.");
    }

    /**
     * Allocates new shape id for the drawing group
     *
     * @param drawingGroupId the drawing group id
     * @param dg the EscherDgRecord which receives the new shape
     *
     * @return a new shape id.
     * 
     * @deprecated in POI 3.17-beta2, use allocateShapeId(EscherDgRecord) 
     */
    @Deprecated
    @Removal(version="4.0")
    public int allocateShapeId(short drawingGroupId, EscherDgRecord dg) {
        return allocateShapeId(dg);
    }

    /**
     * Allocates new shape id for the drawing group
     *
     * @param dg the EscherDgRecord which receives the new shape
     *
     * @return a new shape id.
     */
    public int allocateShapeId(EscherDgRecord dg) {
        return dgg.allocateShapeId(dg, true);
    }
    
    /**
     * Finds the next available (1 based) drawing group id
     * 
     * @return the next available drawing group id
     */
    public short findNewDrawingGroupId() {
        return dgg.findNewDrawingGroupId();
    }

    /**
     * Returns the drawing group container record
     *
     * @return the drawing group container record
     */
    public EscherDggRecord getDgg() {
        return dgg;
    }

    /**
     * Increment the drawing counter
     */
    public void incrementDrawingsSaved(){
        dgg.setDrawingsSaved(dgg.getDrawingsSaved()+1);
    }
}
