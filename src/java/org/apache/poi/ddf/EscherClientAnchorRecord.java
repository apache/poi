
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

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

import java.io.ByteArrayOutputStream;

/**
 * The escher client anchor specifies which rows and cells the shape is bound to as well as
 * the offsets within those cells.  Each cell is 1024 units wide by 256 units long regardless
 * of the actual size of the cell.  The EscherClientAnchorRecord only applies to the top-most
 * shapes.  Shapes contained in groups are bound using the EscherChildAnchorRecords.
 *
 * @author Glen Stampoultzis
 * @see EscherChildAnchorRecord
 */
public class EscherClientAnchorRecord
        extends EscherRecord
{
    public static final short RECORD_ID = (short) 0xF010;
    public static final String RECORD_DESCRIPTION = "MsofbtClientAnchor";

    private short field_1_flag;
    private short field_2_col1;
    private short field_3_dx1;
    private short field_4_row1;
    private short field_5_dy1;
    private short field_6_col2;
    private short field_7_dx2;
    private short field_8_row2;
    private short field_9_dy2;
    private byte[] remainingData;
    private boolean shortRecord = false;

    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesRemaining = readHeader( data, offset );
        int pos            = offset + 8;
        int size           = 0;

        // Always find 4 two byte entries. Sometimes find 9
        if (bytesRemaining == 4) // Word format only 4 bytes
        {
            // Not sure exactly what the format is quite yet, likely a reference to a PLC
        }
        else
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
        remainingData  =  new byte[bytesRemaining];
        System.arraycopy( data, pos + size, remainingData, 0, bytesRemaining );
        return 8 + size + bytesRemaining;
    }

    public int serialize( int offset, byte[] data, EscherSerializationListener listener )
    {
        listener.beforeRecordSerialize( offset, getRecordId(), this );

        if (remainingData == null) remainingData = new byte[0];
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

    public int getRecordSize()
    {
        return 8 + (shortRecord ? 8 : 18) + (remainingData == null ? 0 : remainingData.length);
    }

    public short getRecordId() {
        return RECORD_ID;
    }

    public String getRecordName() {
        return "ClientAnchor";
    }

    /**
     * Returns the string representation for this record.
     *
     * @return A string
     */
    public String toString()
    {
        String nl = System.getProperty("line.separator");

        String extraData;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try
        {
            HexDump.dump(this.remainingData, 0, b, 0);
            extraData = b.toString();
        }
        catch ( Exception e )
        {
            extraData = "error\n";
        }
        return getClass().getName() + ":" + nl +
                "  RecordId: 0x" + HexDump.toHex(RECORD_ID) + nl +
                "  Options: 0x" + HexDump.toHex(getOptions()) + nl +
                "  Flag: " + field_1_flag + nl +
                "  Col1: " + field_2_col1 + nl +
                "  DX1: " + field_3_dx1 + nl +
                "  Row1: " + field_4_row1 + nl +
                "  DY1: " + field_5_dy1 + nl +
                "  Col2: " + field_6_col2 + nl +
                "  DX2: " + field_7_dx2 + nl +
                "  Row2: " + field_8_row2 + nl +
                "  DY2: " + field_9_dy2 + nl +
                "  Extra Data:" + nl + extraData;

    }

    /**
     * 0 = Move and size with Cells, 2 = Move but don't size with cells, 3 = Don't move or size with cells.
     */
    public short getFlag()
    {
        return field_1_flag;
    }

    /**
     * 0 = Move and size with Cells, 2 = Move but don't size with cells, 3 = Don't move or size with cells.
     */
    public void setFlag( short field_1_flag )
    {
        this.field_1_flag = field_1_flag;
    }

    /**
     * The column number for the top-left position.  0 based.
     */
    public short getCol1()
    {
        return field_2_col1;
    }

    /**
     * The column number for the top-left position.  0 based.
     */
    public void setCol1( short field_2_col1 )
    {
        this.field_2_col1 = field_2_col1;
    }

    /**
     * The x offset within the top-left cell.  Range is from 0 to 1023.
     */
    public short getDx1()
    {
        return field_3_dx1;
    }

    /**
     * The x offset within the top-left cell.  Range is from 0 to 1023.
     */
    public void setDx1( short field_3_dx1 )
    {
        this.field_3_dx1 = field_3_dx1;
    }

    /**
     * The row number for the top-left corner of the shape.
     */
    public short getRow1()
    {
        return field_4_row1;
    }

    /**
     * The row number for the top-left corner of the shape.
     */
    public void setRow1( short field_4_row1 )
    {
        this.field_4_row1 = field_4_row1;
    }

    /**
     * The y offset within the top-left corner of the current shape.
     */
    public short getDy1()
    {
        return field_5_dy1;
    }

    /**
     * The y offset within the top-left corner of the current shape.
     */
    public void setDy1( short field_5_dy1 )
    {
        shortRecord = false;
        this.field_5_dy1 = field_5_dy1;
    }

    /**
     * The column of the bottom right corner of this shape.
     */
    public short getCol2()
    {
        return field_6_col2;
    }

    /**
     * The column of the bottom right corner of this shape.
     */
    public void setCol2( short field_6_col2 )
    {
        shortRecord = false;
        this.field_6_col2 = field_6_col2;
    }

    /**
     * The x offset withing the cell for the bottom-right corner of this shape.
     */
    public short getDx2()
    {
        return field_7_dx2;
    }

    /**
     * The x offset withing the cell for the bottom-right corner of this shape.
     */
    public void setDx2( short field_7_dx2 )
    {
        shortRecord = false;
        this.field_7_dx2 = field_7_dx2;
    }

    /**
     * The row number for the bottom-right corner of the current shape.
     */
    public short getRow2()
    {
        return field_8_row2;
    }

    /**
     * The row number for the bottom-right corner of the current shape.
     */
    public void setRow2( short field_8_row2 )
    {
        shortRecord = false;
        this.field_8_row2 = field_8_row2;
    }

    /**
     * The y offset withing the cell for the bottom-right corner of this shape.
     */
    public short getDy2()
    {
        return field_9_dy2;
    }

    /**
     * The y offset withing the cell for the bottom-right corner of this shape.
     */
    public void setDy2( short field_9_dy2 )
    {
        shortRecord = false;
        this.field_9_dy2 = field_9_dy2;
    }

    /**
     * Any remaining data in the record
     */
    public byte[] getRemainingData()
    {
        return remainingData;
    }

    /**
     * Any remaining data in the record
     */
    public void setRemainingData( byte[] remainingData )
    {
        this.remainingData = remainingData;
    }
}
