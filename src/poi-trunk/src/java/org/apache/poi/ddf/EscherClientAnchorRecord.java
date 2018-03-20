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

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * The escher client anchor specifies which rows and cells the shape is bound to as well as
 * the offsets within those cells.  Each cell is 1024 units wide by 256 units long regardless
 * of the actual size of the cell.  The EscherClientAnchorRecord only applies to the top-most
 * shapes.  Shapes contained in groups are bound using the EscherChildAnchorRecords.
 *
 * @see EscherChildAnchorRecord
 */
public class EscherClientAnchorRecord
        extends EscherRecord
{
    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    public static final short RECORD_ID = (short) 0xF010;
    public static final String RECORD_DESCRIPTION = "MsofbtClientAnchor";

    /**
     * bit[0] -  fMove (1 bit): A bit that specifies whether the shape will be kept intact when the cells are moved.
     * bit[1] - fSize (1 bit): A bit that specifies whether the shape will be kept intact when the cells are resized. If fMove is 1, the value MUST be 1.
     * bit[2-4] - reserved, MUST be 0 and MUST be ignored
     * bit[5-15]- Undefined and MUST be ignored.
     *
     * it can take values: 0, 2, 3
     */
    private short field_1_flag;
    private short field_2_col1;
    private short field_3_dx1;
    private short field_4_row1;
    private short field_5_dy1;
    private short field_6_col2;
    private short field_7_dx2;
    private short field_8_row2;
    private short field_9_dy2;
    private byte[] remainingData = new byte[0];
    private boolean shortRecord;

    @Override
    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesRemaining = readHeader( data, offset );
        int pos            = offset + 8;
        int size           = 0;

        // Always find 4 two byte entries. Sometimes find 9
        /*if (bytesRemaining == 4) // Word format only 4 bytes
        {
            // Not sure exactly what the format is quite yet, likely a reference to a PLC
        }
        else */
        if (bytesRemaining != 4) // Word format only 4 bytes
        {
            field_1_flag   =  LittleEndian.getShort( data, pos + size );     size += 2;
            field_2_col1   =  LittleEndian.getShort( data, pos + size );     size += 2;
            field_3_dx1    =  LittleEndian.getShort( data, pos + size );     size += 2;
            field_4_row1   =  LittleEndian.getShort( data, pos + size );     size += 2;
            if(bytesRemaining >= 18) {
                field_5_dy1    =  LittleEndian.getShort( data, pos + size );     size += 2;
                field_6_col2   =  LittleEndian.getShort( data, pos + size );     size += 2;
                field_7_dx2    =  LittleEndian.getShort( data, pos + size );     size += 2;
                field_8_row2   =  LittleEndian.getShort( data, pos + size );     size += 2;
                field_9_dy2    =  LittleEndian.getShort( data, pos + size );     size += 2;
                shortRecord = false;
            } else {
                shortRecord = true;
            }
        }
        bytesRemaining -= size;
        remainingData  = IOUtils.safelyAllocate(bytesRemaining, MAX_RECORD_LENGTH);
        System.arraycopy( data, pos + size, remainingData, 0, bytesRemaining );
        return 8 + size + bytesRemaining;
    }

    @Override
    public int serialize( int offset, byte[] data, EscherSerializationListener listener )
    {
        listener.beforeRecordSerialize( offset, getRecordId(), this );

        if (remainingData == null) {
            remainingData = new byte[0];
        }
        LittleEndian.putShort( data, offset, getOptions() );
        LittleEndian.putShort( data, offset + 2, getRecordId() );
        int remainingBytes = remainingData.length + (shortRecord ? 8 : 18);
        LittleEndian.putInt( data, offset + 4, remainingBytes );
        LittleEndian.putShort( data, offset + 8, field_1_flag );
        LittleEndian.putShort( data, offset + 10, field_2_col1 );
        LittleEndian.putShort( data, offset + 12, field_3_dx1 );
        LittleEndian.putShort( data, offset + 14, field_4_row1 );
        if(!shortRecord) {
            LittleEndian.putShort( data, offset + 16, field_5_dy1 );
            LittleEndian.putShort( data, offset + 18, field_6_col2 );
            LittleEndian.putShort( data, offset + 20, field_7_dx2 );
            LittleEndian.putShort( data, offset + 22, field_8_row2 );
            LittleEndian.putShort( data, offset + 24, field_9_dy2 );
        }
        System.arraycopy( remainingData, 0, data, offset + (shortRecord ? 16 : 26), remainingData.length );
        int pos = offset + 8 + (shortRecord ? 8 : 18) + remainingData.length;

        listener.afterRecordSerialize( pos, getRecordId(), pos - offset, this );
        return pos - offset;
    }

    @Override
    public int getRecordSize()
    {
        return 8 + (shortRecord ? 8 : 18) + (remainingData == null ? 0 : remainingData.length);
    }

    @Override
    public short getRecordId() {
        return RECORD_ID;
    }

    @Override
    public String getRecordName() {
        return "ClientAnchor";
    }

    /**
     * 0 = Move and size with Cells, 2 = Move but don't size with cells, 3 = Don't move or size with cells.
     *
     * @return the move/size flag
     */
    public short getFlag()
    {
        return field_1_flag;
    }

    /**
     * 0 = Move and size with Cells, 2 = Move but don't size with cells, 3 = Don't move or size with cells.
     *
     * @param field_1_flag the move/size flag
     */
    public void setFlag( short field_1_flag )
    {
        this.field_1_flag = field_1_flag;
    }

    /**
     * The column number for the top-left position.  0 based.
     *
     * @return the column number of the top-left corner
     */
    public short getCol1()
    {
        return field_2_col1;
    }

    /**
     * The column number for the top-left position.  0 based.
     *
     * @param field_2_col1 the column number of the top-left corner
     */
    public void setCol1( short field_2_col1 )
    {
        this.field_2_col1 = field_2_col1;
    }

    /**
     * The x offset within the top-left cell.  Range is from 0 to 1023.
     *
     * @return the x offset of the top-left corner
     */
    public short getDx1()
    {
        return field_3_dx1;
    }

    /**
     * The x offset within the top-left cell.  Range is from 0 to 1023.
     *
     * @param field_3_dx1 the x offset of the top-left corner
     */
    public void setDx1( short field_3_dx1 )
    {
        this.field_3_dx1 = field_3_dx1;
    }

    /**
     * The row number for the top-left corner of the shape.
     *
     * @return the row number of the top-left corner
     */
    public short getRow1()
    {
        return field_4_row1;
    }

    /**
     * The row number of the top-left corner of the shape.
     *
     * @param field_4_row1 the row number of the top-left corner
     */
    public void setRow1( short field_4_row1 )
    {
        this.field_4_row1 = field_4_row1;
    }

    /**
     * The y offset within the top-left corner of the current shape.
     *
     * @return the y offset of the top-left corner
     */
    public short getDy1()
    {
        return field_5_dy1;
    }

    /**
     * The y offset within the top-left corner of the current shape.
     *
     * @param field_5_dy1 the y offset of the top-left corner
     */
    public void setDy1( short field_5_dy1 )
    {
        shortRecord = false;
        this.field_5_dy1 = field_5_dy1;
    }

    /**
     * The column of the bottom right corner of this shape.
     *
     * @return the column of the bottom right corner
     */
    public short getCol2()
    {
        return field_6_col2;
    }

    /**
     * The column of the bottom right corner of this shape.
     *
     * @param field_6_col2 the column of the bottom right corner
     */
    public void setCol2( short field_6_col2 )
    {
        shortRecord = false;
        this.field_6_col2 = field_6_col2;
    }

    /**
     * The x offset withing the cell for the bottom-right corner of this shape.
     *
     * @return the x offset of the bottom-right corner
     */
    public short getDx2()
    {
        return field_7_dx2;
    }

    /**
     * The x offset withing the cell for the bottom-right corner of this shape.
     *
     * @param field_7_dx2 the x offset of the bottom-right corner
     */
    public void setDx2( short field_7_dx2 )
    {
        shortRecord = false;
        this.field_7_dx2 = field_7_dx2;
    }

    /**
     * The row number for the bottom-right corner of the current shape.
     *
     * @return the row number for the bottom-right corner
     */
    public short getRow2()
    {
        return field_8_row2;
    }

    /**
     * The row number for the bottom-right corner of the current shape.
     *
     * @param field_8_row2 the row number for the bottom-right corner
     */
    public void setRow2( short field_8_row2 )
    {
        shortRecord = false;
        this.field_8_row2 = field_8_row2;
    }

    /**
     * The y offset withing the cell for the bottom-right corner of this shape.
     *
     * @return the y offset of the bottom-right corner
     */
    public short getDy2()
    {
        return field_9_dy2;
    }

    /**
     * The y offset withing the cell for the bottom-right corner of this shape.
     *
     * @param field_9_dy2 the y offset of the bottom-right corner
     */
    public void setDy2( short field_9_dy2 )
    {
        shortRecord = false;
        this.field_9_dy2 = field_9_dy2;
    }

    /**
     * Any remaining data in the record
     *
     * @return the remaining bytes
     */
    public byte[] getRemainingData()
    {
        return remainingData;
    }

    /**
     * Any remaining data in the record
     *
     * @param remainingData the remaining bytes
     */
    public void setRemainingData( byte[] remainingData ) {
        if (remainingData == null) {
            this.remainingData = new byte[0];
        } else {
            this.remainingData = remainingData.clone();
        }
    }

    @Override
    protected Object[][] getAttributeMap() {
        return new Object[][] {
            { "Flag", field_1_flag },
            { "Col1", field_2_col1 },
            { "DX1", field_3_dx1 },
            { "Row1", field_4_row1 },
            { "DY1", field_5_dy1 },
            { "Col2", field_6_col2 },
            { "DX2", field_7_dx2 },
            { "Row2", field_8_row2 },
            { "DY2", field_9_dy2 },
            { "Extra Data", remainingData }
        };
    }
}
