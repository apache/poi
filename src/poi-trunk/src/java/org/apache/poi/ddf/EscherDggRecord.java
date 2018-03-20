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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.RecordFormatException;

/**
 * This record defines the drawing groups used for a particular sheet.
 */
public final class EscherDggRecord extends EscherRecord {
    public static final short RECORD_ID = (short) 0xF006;
    public static final String RECORD_DESCRIPTION = "MsofbtDgg";
    
    private int field_1_shapeIdMax;
    // for some reason the number of clusters is actually the real number + 1
    // private int field_2_numIdClusters;
    private int field_3_numShapesSaved;
    private int field_4_drawingsSaved;
    private final List<FileIdCluster> field_5_fileIdClusters = new ArrayList<>();
    private int maxDgId;

    public static class FileIdCluster {
        private int field_1_drawingGroupId;
        private int field_2_numShapeIdsUsed;

        public FileIdCluster( int drawingGroupId, int numShapeIdsUsed ) {
            this.field_1_drawingGroupId = drawingGroupId;
            this.field_2_numShapeIdsUsed = numShapeIdsUsed;
        }

        public int getDrawingGroupId() {
            return field_1_drawingGroupId;
        }

        public int getNumShapeIdsUsed() {
            return field_2_numShapeIdsUsed;
        }

        private void incrementUsedShapeId() {
            field_2_numShapeIdsUsed++;
        }
    }

    @Override
    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesRemaining = readHeader( data, offset );
        int pos            = offset + 8;
        int size           = 0;
        field_1_shapeIdMax     =  LittleEndian.getInt( data, pos + size );size+=4;
        // field_2_numIdClusters = LittleEndian.getInt( data, pos + size );
        size+=4; 
        field_3_numShapesSaved =  LittleEndian.getInt( data, pos + size );size+=4;
        field_4_drawingsSaved  =  LittleEndian.getInt( data, pos + size );size+=4;
        
        field_5_fileIdClusters.clear();
        // Can't rely on field_2_numIdClusters
        int numIdClusters = (bytesRemaining-size) / 8;
        
        for (int i = 0; i < numIdClusters; i++) {
            int drawingGroupId = LittleEndian.getInt( data, pos + size );
            int numShapeIdsUsed = LittleEndian.getInt( data, pos + size + 4 );
            FileIdCluster fic = new FileIdCluster(drawingGroupId, numShapeIdsUsed);
            field_5_fileIdClusters.add(fic);
            maxDgId = Math.max(maxDgId, drawingGroupId);
            size += 8;
        }
        bytesRemaining -= size;
        if (bytesRemaining != 0) {
            throw new RecordFormatException("Expecting no remaining data but got " + bytesRemaining + " byte(s).");
        }
        return 8 + size;
    }

    @Override
    public int serialize(int offset, byte[] data, EscherSerializationListener listener) {
        listener.beforeRecordSerialize( offset, getRecordId(), this );

        int pos = offset;
        LittleEndian.putShort( data, pos, getOptions() );     pos += 2;
        LittleEndian.putShort( data, pos, getRecordId() );    pos += 2;
        int remainingBytes = getRecordSize() - 8;
        LittleEndian.putInt( data, pos, remainingBytes );              pos += 4;

        LittleEndian.putInt( data, pos, field_1_shapeIdMax );          pos += 4;
        LittleEndian.putInt( data, pos, getNumIdClusters() );          pos += 4;
        LittleEndian.putInt( data, pos, field_3_numShapesSaved );      pos += 4;
        LittleEndian.putInt( data, pos, field_4_drawingsSaved );       pos += 4;
        
        for (FileIdCluster fic : field_5_fileIdClusters) {
            LittleEndian.putInt( data, pos, fic.getDrawingGroupId() );   pos += 4;
            LittleEndian.putInt( data, pos, fic.getNumShapeIdsUsed() );  pos += 4;
        }

        listener.afterRecordSerialize( pos, getRecordId(), getRecordSize(), this );
        return getRecordSize();
    }

    @Override
    public int getRecordSize() {
        return 8 + 16 + (8 * field_5_fileIdClusters.size());
    }

    @Override
    public short getRecordId() {
        return RECORD_ID;
    }

    @Override
    public String getRecordName() {
        return "Dgg";
    }

    /**
     * Gets the next available shape id
     *
     * @return the next available shape id
     */
    public int getShapeIdMax() {
        return field_1_shapeIdMax;
    }

    /**
     * The maximum is actually the next available shape id.
     * 
     * @param shapeIdMax the next available shape id
     */
    public void setShapeIdMax(int shapeIdMax) {
        this.field_1_shapeIdMax = shapeIdMax;
    }

    /**
     * Number of id clusters + 1
     * 
     * @return the number of id clusters + 1
     */
    public int getNumIdClusters() {
        return (field_5_fileIdClusters.isEmpty() ? 0 : field_5_fileIdClusters.size() + 1);
    }

    /**
     * Gets the number of shapes saved
     *
     * @return the number of shapes saved
     */
    public int getNumShapesSaved() {
        return field_3_numShapesSaved;
    }

    /**
     * Sets the number of shapes saved
     * 
     * @param numShapesSaved the number of shapes saved
     */
    public void setNumShapesSaved(int numShapesSaved) {
        this.field_3_numShapesSaved = numShapesSaved;
    }

    /**
     * Gets the number of drawings saved
     *
     * @return the number of drawings saved
     */
    public int getDrawingsSaved() {
        return field_4_drawingsSaved;
    }

    /**
     * Sets the number of drawings saved
     *
     * @param drawingsSaved the number of drawings saved
     */
    public void setDrawingsSaved(int drawingsSaved) {
        this.field_4_drawingsSaved = drawingsSaved;
    }

    /**
     * Gets the maximum drawing group ID
     * 
     * @return The maximum drawing group ID
     */
    public int getMaxDrawingGroupId() {
        return maxDgId;
    }

    /**
     * @return the file id clusters
     */
    public FileIdCluster[] getFileIdClusters() {
        return field_5_fileIdClusters.toArray(new FileIdCluster[field_5_fileIdClusters.size()]);
    }

    /**
     * Sets the file id clusters
     *
     * @param fileIdClusters the file id clusters
     */
    public void setFileIdClusters(FileIdCluster[] fileIdClusters) {
        field_5_fileIdClusters.clear();
        if (fileIdClusters != null) {
            field_5_fileIdClusters.addAll(Arrays.asList(fileIdClusters));
        }
    }

    
    /**
     * Add a new cluster
     *
     * @param dgId  id of the drawing group (stored in the record options)
     * @param numShapedUsed initial value of the numShapedUsed field
     * 
     * @return the new {@link FileIdCluster}
     */
    public FileIdCluster addCluster(int dgId, int numShapedUsed) {
        return addCluster(dgId, numShapedUsed, true);
    }

    /**
     * Add a new cluster
     *
     * @param dgId  id of the drawing group (stored in the record options)
     * @param numShapedUsed initial value of the numShapedUsed field
     * @param sort if true then sort clusters by drawing group id.(
     *  In Excel the clusters are sorted but in PPT they are not)
     * 
     * @return the new {@link FileIdCluster}
     */
    public FileIdCluster addCluster( int dgId, int numShapedUsed, boolean sort ) {
        FileIdCluster ficNew = new FileIdCluster(dgId, numShapedUsed);
        field_5_fileIdClusters.add(ficNew);
        maxDgId = Math.min(maxDgId, dgId);
        
        if (sort) {
            sortCluster();
        }
        
        return ficNew;
    }

    private void sortCluster() {
        field_5_fileIdClusters.sort(new Comparator<FileIdCluster>() {
            @Override
            public int compare(FileIdCluster f1, FileIdCluster f2) {
                int dgDif = f1.getDrawingGroupId() - f2.getDrawingGroupId();
                int cntDif = f2.getNumShapeIdsUsed() - f1.getNumShapeIdsUsed();
                return (dgDif != 0) ? dgDif : cntDif;
            }
        });
    }
    
    /**
     * Finds the next available (1 based) drawing group id
     * 
     * @return the next available drawing group id
     */
    public short findNewDrawingGroupId() {
        BitSet bs = new BitSet();
        bs.set(0);
        for (FileIdCluster fic : field_5_fileIdClusters) {
            bs.set(fic.getDrawingGroupId());
        }
        return (short)bs.nextClearBit(0);
    }
    
    /**
     * Allocates new shape id for the drawing group
     *
     * @param dg the EscherDgRecord which receives the new shape
     * @param sort if true then sort clusters by drawing group id.(
     *  In Excel the clusters are sorted but in PPT they are not)
     *
     * @return a new shape id.
     */
    public int allocateShapeId(EscherDgRecord dg, boolean sort) {
        final short drawingGroupId = dg.getDrawingGroupId();
        field_3_numShapesSaved++;
        
        // check for an existing cluster, which has space available
        // see 2.2.46 OfficeArtIDCL (cspidCur) for the 1024 limitation
        // multiple clusters can belong to the same drawing group
        FileIdCluster ficAdd = null;
        int index = 1;
        for (FileIdCluster fic : field_5_fileIdClusters) {
            if (fic.getDrawingGroupId() == drawingGroupId
                && fic.getNumShapeIdsUsed() < 1024) {
                ficAdd = fic;
                break;
            }
            index++;
        }

        if (ficAdd == null) {
            ficAdd = addCluster( drawingGroupId, 0, sort );
            maxDgId = Math.max(maxDgId, drawingGroupId);
        }
        
        int shapeId = index*1024 + ficAdd.getNumShapeIdsUsed();
        ficAdd.incrementUsedShapeId();
        
        dg.setNumShapes( dg.getNumShapes() + 1 );
        dg.setLastMSOSPID( shapeId );
        field_1_shapeIdMax = Math.max(field_1_shapeIdMax, shapeId + 1);
        
        return shapeId;
    }    
    
    
    @Override
    protected Object[][] getAttributeMap() {
        List<Object> fldIds = new ArrayList<>();
        fldIds.add("FileId Clusters");
        fldIds.add(field_5_fileIdClusters.size());
        for (FileIdCluster fic : field_5_fileIdClusters) {
            fldIds.add("Group"+fic.field_1_drawingGroupId);
            fldIds.add(fic.field_2_numShapeIdsUsed);
        }
        
        return new Object[][] {
            { "ShapeIdMax", field_1_shapeIdMax },
            { "NumIdClusters", getNumIdClusters() },
            { "NumShapesSaved", field_3_numShapesSaved },
            { "DrawingsSaved", field_4_drawingsSaved },
            fldIds.toArray()
        };
    }
}
