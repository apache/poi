
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

import org.apache.poi.util.LittleEndian;

/**
 * The escher child achor record is used to specify the position of a shape under an
 * existing group.  The first level of shape records use a EscherClientAnchor record instead.
 *
 * @see EscherChildAnchorRecord
 */
public class EscherChildAnchorRecord
        extends EscherRecord
{
    public static final short RECORD_ID = (short) 0xF00F;
    public static final String RECORD_DESCRIPTION = "MsofbtChildAnchor";

    private int field_1_dx1;
    private int field_2_dy1;
    private int field_3_dx2;
    private int field_4_dy2;

    @Override
    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesRemaining = readHeader( data, offset );
        int pos            = offset + 8;
        int size           = 0;
        switch (bytesRemaining) {
        case 16: // RectStruct
            field_1_dx1 =  LittleEndian.getInt( data, pos + size );size+=4;
            field_2_dy1 =  LittleEndian.getInt( data, pos + size );size+=4;
            field_3_dx2 =  LittleEndian.getInt( data, pos + size );size+=4;
            field_4_dy2 =  LittleEndian.getInt( data, pos + size );size+=4;
            break;
        case 8: // SmallRectStruct
            field_1_dx1 =  LittleEndian.getShort( data, pos + size );size+=2;
            field_2_dy1 =  LittleEndian.getShort( data, pos + size );size+=2;
            field_3_dx2 =  LittleEndian.getShort( data, pos + size );size+=2;
            field_4_dy2 =  LittleEndian.getShort( data, pos + size );size+=2;
            break;
        default:
            throw new RuntimeException("Invalid EscherChildAnchorRecord - neither 8 nor 16 bytes.");
        }
            
        return 8 + size;
    }

    @Override
    public int serialize(int offset, byte[] data, EscherSerializationListener listener) {
        listener.beforeRecordSerialize( offset, getRecordId(), this );
        int pos = offset;
        LittleEndian.putShort( data, pos, getOptions() );          pos += 2;
        LittleEndian.putShort( data, pos, getRecordId() );         pos += 2;
        LittleEndian.putInt( data, pos, getRecordSize()-8 );       pos += 4;
        LittleEndian.putInt( data, pos, field_1_dx1 );             pos += 4;
        LittleEndian.putInt( data, pos, field_2_dy1 );             pos += 4;
        LittleEndian.putInt( data, pos, field_3_dx2 );             pos += 4;
        LittleEndian.putInt( data, pos, field_4_dy2 );             pos += 4;

        listener.afterRecordSerialize( pos, getRecordId(), pos - offset, this );
        return pos - offset;
    }

    @Override
    public int getRecordSize()
    {
        return 8 + 4 * 4;
    }

    @Override
    public short getRecordId() {
        return RECORD_ID;
    }

    @Override
    public String getRecordName() {
        return "ChildAnchor";
    }


    /**
     * Retrieves offset within the parent coordinate space for the top left point.
     * 
     * @return the x offset of the top left point
     */
    public int getDx1()
    {
        return field_1_dx1;
    }

    /**
     * Sets offset within the parent coordinate space for the top left point.
     * 
     * @param field_1_dx1 the x offset of the top left point
     */
    public void setDx1( int field_1_dx1 )
    {
        this.field_1_dx1 = field_1_dx1;
    }

    /**
     * Gets offset within the parent coordinate space for the top left point.
     * 
     * @return the y offset of the top left point
     */
    public int getDy1()
    {
        return field_2_dy1;
    }

    /**
     * Sets offset within the parent coordinate space for the top left point.
     * 
     * @param field_2_dy1 the y offset of the top left point 
     */
    public void setDy1( int field_2_dy1 )
    {
        this.field_2_dy1 = field_2_dy1;
    }

    /**
     * Retrieves offset within the parent coordinate space for the bottom right point.
     * 
     * @return the x offset of the bottom right point
     */
    public int getDx2()
    {
        return field_3_dx2;
    }

    /**
     * Sets offset within the parent coordinate space for the bottom right point.
     * 
     * @param field_3_dx2 the x offset of the bottom right point
     */
    public void setDx2( int field_3_dx2 )
    {
        this.field_3_dx2 = field_3_dx2;
    }

    /**
     * Gets the offset within the parent coordinate space for the bottom right point.
     * 
     * @return the y offset of the bottom right point
     */
    public int getDy2()
    {
        return field_4_dy2;
    }

    /**
     * Sets the offset within the parent coordinate space for the bottom right point.
     * 
     * @param field_4_dy2 the y offset of the bottom right point
     */
    public void setDy2( int field_4_dy2 )
    {
        this.field_4_dy2 = field_4_dy2;
    }

    @Override
    protected Object[][] getAttributeMap() {
        return new Object[][] {
            { "X1", field_1_dx1 },
            { "Y1", field_2_dy1 },
            { "X2", field_3_dx2 },
            { "Y2", field_4_dy2 }
        };
    }
}
