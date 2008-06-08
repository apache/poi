
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

import org.apache.poi.hssf.record.RecordFormatException;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

import java.util.*;

/**
 * This record defines the drawing groups used for a particular sheet.
 */
public class EscherDggRecord
    extends EscherRecord
{
    public static final short RECORD_ID = (short) 0xF006;
    public static final String RECORD_DESCRIPTION = "MsofbtDgg";

    private int field_1_shapeIdMax;
//    private int field_2_numIdClusters;      // for some reason the number of clusters is actually the real number + 1
    private int field_3_numShapesSaved;
    private int field_4_drawingsSaved;
    private FileIdCluster[] field_5_fileIdClusters;
    private int maxDgId;

    public static class FileIdCluster
    {
        public FileIdCluster( int drawingGroupId, int numShapeIdsUsed )
        {
            this.field_1_drawingGroupId = drawingGroupId;
            this.field_2_numShapeIdsUsed = numShapeIdsUsed;
        }

        private int field_1_drawingGroupId;
        private int field_2_numShapeIdsUsed;

        public int getDrawingGroupId()
        {
            return field_1_drawingGroupId;
        }

        public int getNumShapeIdsUsed()
        {
            return field_2_numShapeIdsUsed;
        }

        public void incrementShapeId( )
        {
            this.field_2_numShapeIdsUsed++;
        }
    }

    /**
     * This method deserializes the record from a byte array.
     *
     * @param data          The byte array containing the escher record information
     * @param offset        The starting offset into <code>data</code>.
     * @param recordFactory May be null since this is not a container record.
     * @return The number of bytes read from the byte array.
     */
    public int fillFields( byte[] data, int offset, EscherRecordFactory recordFactory )
    {
        int bytesRemaining = readHeader( data, offset );
        int pos            = offset + 8;
        int size           = 0;
        field_1_shapeIdMax     =  LittleEndian.getInt( data, pos + size );size+=4;
        int field_2_numIdClusters  =  LittleEndian.getInt( data, pos + size );size+=4;
        field_3_numShapesSaved =  LittleEndian.getInt( data, pos + size );size+=4;
        field_4_drawingsSaved  =  LittleEndian.getInt( data, pos + size );size+=4;
        field_5_fileIdClusters = new FileIdCluster[(bytesRemaining-size) / 8];  // Can't rely on field_2_numIdClusters
        for (int i = 0; i < field_5_fileIdClusters.length; i++)
        {
            field_5_fileIdClusters[i] = new FileIdCluster(LittleEndian.getInt( data, pos + size ), LittleEndian.getInt( data, pos + size + 4 ));
            maxDgId = Math.max(maxDgId, field_5_fileIdClusters[i].getDrawingGroupId());
            size += 8;
        }
        bytesRemaining         -= size;
        if (bytesRemaining != 0)
            throw new RecordFormatException("Expecting no remaining data but got " + bytesRemaining + " byte(s).");
        return 8 + size + bytesRemaining;
    }

    /**
     * This method serializes this escher record into a byte array.
     *
     * @param offset   The offset into <code>data</code> to start writing the record data to.
     * @param data     The byte array to serialize to.
     * @param listener A listener to retrieve start and end callbacks.  Use a <code>NullEscherSerailizationListener</code> to ignore these events.
     * @return The number of bytes written.
     *
     * @see NullEscherSerializationListener
     */
    public int serialize( int offset, byte[] data, EscherSerializationListener listener )
    {
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
        for ( int i = 0; i < field_5_fileIdClusters.length; i++ )
        {
            LittleEndian.putInt( data, pos, field_5_fileIdClusters[i].field_1_drawingGroupId );   pos += 4;
            LittleEndian.putInt( data, pos, field_5_fileIdClusters[i].field_2_numShapeIdsUsed );  pos += 4;
        }

        listener.afterRecordSerialize( pos, getRecordId(), getRecordSize(), this );
        return getRecordSize();
    }

    /**
     * Returns the number of bytes that are required to serialize this record.
     *
     * @return Number of bytes
     */
    public int getRecordSize()
    {
        return 8 + 16 + (8 * field_5_fileIdClusters.length);
    }

    public short getRecordId()
    {
        return RECORD_ID;
    }

    /**
     * The short name for this record
     */
    public String getRecordName()
    {
        return "Dgg";
    }

    public String toString()
    {
        String nl = System.getProperty("line.separator");

//        String extraData;
//        ByteArrayOutputStream b = new ByteArrayOutputStream();
//        try
//        {
//            HexDump.dump(this.remainingData, 0, b, 0);
//            extraData = b.toString();
//        }
//        catch ( Exception e )
//        {
//            extraData = "error";
//        }
        StringBuffer field_5_string = new StringBuffer();
        for ( int i = 0; i < field_5_fileIdClusters.length; i++ )
        {
            field_5_string.append("  DrawingGroupId").append(i+1).append(": ");
            field_5_string.append(field_5_fileIdClusters[i].field_1_drawingGroupId);
            field_5_string.append(nl);
            field_5_string.append("  NumShapeIdsUsed").append(i+1).append(": ");
            field_5_string.append(field_5_fileIdClusters[i].field_2_numShapeIdsUsed);
            field_5_string.append(nl);
        }
        return getClass().getName() + ":" + nl +
                "  RecordId: 0x" + HexDump.toHex(RECORD_ID) + nl +
                "  Options: 0x" + HexDump.toHex(getOptions()) + nl +
                "  ShapeIdMax: " + field_1_shapeIdMax + nl +
                "  NumIdClusters: " + getNumIdClusters() + nl +
                "  NumShapesSaved: " + field_3_numShapesSaved + nl +
                "  DrawingsSaved: " + field_4_drawingsSaved + nl +
                "" + field_5_string.toString();

    }

    public int getShapeIdMax()
    {
        return field_1_shapeIdMax;
    }

    /**
     * The maximum is actually the next available. shape id.
     */
    public void setShapeIdMax( int field_1_shapeIdMax )
    {
        this.field_1_shapeIdMax = field_1_shapeIdMax;
    }

    /**
     * Number of id clusters + 1
     */ 
    public int getNumIdClusters()
    {
        return field_5_fileIdClusters.length + 1;
    }

    public int getNumShapesSaved()
    {
        return field_3_numShapesSaved;
    }

    public void setNumShapesSaved( int field_3_numShapesSaved )
    {
        this.field_3_numShapesSaved = field_3_numShapesSaved;
    }

    public int getDrawingsSaved()
    {
        return field_4_drawingsSaved;
    }

    public void setDrawingsSaved( int field_4_drawingsSaved )
    {
        this.field_4_drawingsSaved = field_4_drawingsSaved;
    }

    /**
     * @return The maximum drawing group ID
     */
    public int getMaxDrawingGroupId(){
        return maxDgId;
    }

    public void setMaxDrawingGroupId(int id){
        maxDgId = id;
    }

     public FileIdCluster[] getFileIdClusters()
    {
        return field_5_fileIdClusters;
    }

    public void setFileIdClusters( FileIdCluster[] field_5_fileIdClusters )
    {
        this.field_5_fileIdClusters = field_5_fileIdClusters;
    }

    public void addCluster( int dgId, int numShapedUsed )
    {
        addCluster(dgId, numShapedUsed, true);
    }

    /**
     * Add a new cluster
     *
     * @param dgId  id of the drawing group (stored in the record options)
     * @param numShapedUsed initial value of the numShapedUsed field
     * @param sort if true then sort clusters by drawing group id.(
     *  In Excel the clusters are sorted but in PPT they are not)
     */
    public void addCluster( int dgId, int numShapedUsed, boolean sort )
    {
        List clusters = new ArrayList(Arrays.asList(field_5_fileIdClusters));
        clusters.add(new FileIdCluster(dgId, numShapedUsed));
        if(sort) Collections.sort(clusters, new Comparator()
        {
            public int compare( Object o1, Object o2 )
            {
                FileIdCluster f1 = (FileIdCluster) o1;
                FileIdCluster f2 = (FileIdCluster) o2;
                if (f1.getDrawingGroupId() == f2.getDrawingGroupId())
                    return 0;
                if (f1.getDrawingGroupId() < f2.getDrawingGroupId())
                    return -1;
                else
                    return +1;
            }
        } );
        maxDgId = Math.min(maxDgId, dgId);
        field_5_fileIdClusters = (FileIdCluster[]) clusters.toArray( new FileIdCluster[clusters.size()] );
    }
}
