
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
import org.apache.poi.util.RecordFormatException;

/**
 * The spgr record defines information about a shape group.  Groups in escher
 * are simply another form of shape that you can't physically see.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class EscherSpgrRecord
    extends EscherRecord
{
    public static final short RECORD_ID = (short) 0xF009;
    public static final String RECORD_DESCRIPTION = "MsofbtSpgr";

    private int field_1_rectX1;
    private int field_2_rectY1;
    private int field_3_rectX2;
    private int field_4_rectY2;

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
        field_1_rectX1 =  LittleEndian.getInt( data, pos + size );size+=4;
        field_2_rectY1 =  LittleEndian.getInt( data, pos + size );size+=4;
        field_3_rectX2 =  LittleEndian.getInt( data, pos + size );size+=4;
        field_4_rectY2 =  LittleEndian.getInt( data, pos + size );size+=4;
        bytesRemaining -= size;
        if (bytesRemaining != 0) throw new RecordFormatException("Expected no remaining bytes but got " + bytesRemaining);
//        remainingData  =  new byte[bytesRemaining];
//        System.arraycopy( data, pos + size, remainingData, 0, bytesRemaining );
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
        LittleEndian.putShort( data, offset, getOptions() );
        LittleEndian.putShort( data, offset + 2, getRecordId() );
        int remainingBytes = 16;
        LittleEndian.putInt( data, offset + 4, remainingBytes );
        LittleEndian.putInt( data, offset + 8, field_1_rectX1 );
        LittleEndian.putInt( data, offset + 12, field_2_rectY1 );
        LittleEndian.putInt( data, offset + 16, field_3_rectX2 );
        LittleEndian.putInt( data, offset + 20, field_4_rectY2 );
//        System.arraycopy( remainingData, 0, data, offset + 26, remainingData.length );
//        int pos = offset + 8 + 18 + remainingData.length;
        listener.afterRecordSerialize( offset + getRecordSize(), getRecordId(), offset + getRecordSize(), this );
        return 8 + 16;
    }

    /**
     * Returns the number of bytes that are required to serialize this record.
     *
     * @return Number of bytes
     */
    public int getRecordSize()
    {
        return 8 + 16;
    }

    /**
     * The 16 bit identifier of this shape group record.
     */
    public short getRecordId()
    {
        return RECORD_ID;
    }

    /**
     * The short name for this record
     */
    public String getRecordName()
    {
        return "Spgr";
    }

    /**
     * @return  the string representation of this record.
     */
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
        return getClass().getName() + ":" + nl +
                "  RecordId: 0x" + HexDump.toHex(RECORD_ID) + nl +
                "  Options: 0x" + HexDump.toHex(getOptions()) + nl +
                "  RectX: " + field_1_rectX1 + nl +
                "  RectY: " + field_2_rectY1 + nl +
                "  RectWidth: " + field_3_rectX2 + nl +
                "  RectHeight: " + field_4_rectY2 + nl;

    }

    /**
     * The starting top-left coordinate of child records.
     */
    public int getRectX1()
    {
        return field_1_rectX1;
    }

    /**
     * The starting top-left coordinate of child records.
     */
    public void setRectX1( int x1 )
    {
        this.field_1_rectX1 = x1;
    }

    /**
     * The starting top-left coordinate of child records.
     */
    public int getRectY1()
    {
        return field_2_rectY1;
    }

    /**
     * The starting top-left coordinate of child records.
     */
    public void setRectY1( int y1 )
    {
        this.field_2_rectY1 = y1;
    }

    /**
     * The starting bottom-right coordinate of child records.
     */
    public int getRectX2()
    {
        return field_3_rectX2;
    }

    /**
     * The starting bottom-right coordinate of child records.
     */
    public void setRectX2( int x2 )
    {
        this.field_3_rectX2 = x2;
    }

    /**
     * The starting bottom-right coordinate of child records.
     */
    public int getRectY2()
    {
        return field_4_rectY2;
    }

    /**
     * The starting bottom-right coordinate of child records.
     */
    public void setRectY2( int field_4_rectY2 )
    {
        this.field_4_rectY2 = field_4_rectY2;
    }
}
