/* ====================================================================
   Copyright 2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.ddf;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.HexDump;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

/**
 * The BSE record is related closely to the <code>EscherBlipRecord</code> and stores
 * extra information about the blip.
 *
 * @author Glen Stampoultzis
 * @see EscherBlipRecord
 */
public class EscherBSERecord
        extends EscherRecord
{
    public static final short RECORD_ID = (short) 0xF007;
    public static final String RECORD_DESCRIPTION = "MsofbtBSE";

    public static final byte BT_ERROR = 0;
    public static final byte BT_UNKNOWN = 1;
    public static final byte BT_EMF = 2;
    public static final byte BT_WMF = 3;
    public static final byte BT_PICT = 4;
    public static final byte BT_JPEG = 5;
    public static final byte BT_PNG = 6;
    public static final byte BT_DIB = 7;

    private byte field_1_blipTypeWin32;
    private byte field_2_blipTypeMacOS;
    private byte[] field_3_uid;  // 16 bytes
    private short field_4_tag;
    private int field_5_size;
    private int field_6_ref;
    private int field_7_offset;
    private byte field_8_usage;
    private byte field_9_name;
    private byte field_10_unused2;
    private byte field_11_unused3;

    private byte[] remainingData;

    /**
     * This method deserializes the record from a byte array.
     *
     * @param data          The byte array containing the escher record information
     * @param offset        The starting offset into <code>data</code>.
     * @param recordFactory May be null since this is not a container record.
     * @return The number of bytes read from the byte array.
     */
    public int fillFields( byte[] data, int offset,
                              EscherRecordFactory recordFactory
                              )
    {
        int bytesRemaining = readHeader( data, offset );
        int pos = offset + 8;
        field_1_blipTypeWin32 = data[pos];
        field_2_blipTypeMacOS = data[pos + 1];
        System.arraycopy( data, pos + 2, field_3_uid = new byte[16], 0, 16 );
        field_4_tag = LittleEndian.getShort( data, pos + 18 );
        field_5_size = LittleEndian.getInt( data, pos + 20 );
        field_6_ref = LittleEndian.getInt( data, pos + 24 );
        field_7_offset = LittleEndian.getInt( data, pos + 28 );
        field_8_usage = data[pos + 32];
        field_9_name = data[pos + 33];
        field_10_unused2 = data[pos + 34];
        field_11_unused3 = data[pos + 35];
        bytesRemaining -= 36;
        remainingData = new byte[bytesRemaining];
        System.arraycopy( data, pos + 36, remainingData, 0, bytesRemaining );
        return bytesRemaining + 8 + 36;

    }

    /**
     * This method serializes this escher record into a byte array.
     *
     * @param offset   The offset into <code>data</code> to start writing the record data to.
     * @param data     The byte array to serialize to.
     * @param listener A listener to retrieve start and end callbacks.  Use a <code>NullEscherSerailizationListener</code> to ignore these events.
     * @return The number of bytes written.
     * @see NullEscherSerializationListener
     */
    public int serialize( int offset, byte[] data, EscherSerializationListener listener )
    {
        listener.beforeRecordSerialize( offset, getRecordId(), this );

        LittleEndian.putShort( data, offset, getOptions() );
        LittleEndian.putShort( data, offset + 2, getRecordId() );
        int remainingBytes = remainingData.length + 36;
        LittleEndian.putInt( data, offset + 4, remainingBytes );

        data[offset + 8] = field_1_blipTypeWin32;
        data[offset + 9] = field_2_blipTypeMacOS;
        for ( int i = 0; i < 16; i++ )
            data[offset + 10 + i] = field_3_uid[i];
        LittleEndian.putShort( data, offset + 26, field_4_tag );
        LittleEndian.putInt( data, offset + 28, field_5_size );
        LittleEndian.putInt( data, offset + 32, field_6_ref );
        LittleEndian.putInt( data, offset + 36, field_7_offset );
        data[offset + 40] = field_8_usage;
        data[offset + 41] = field_9_name;
        data[offset + 42] = field_10_unused2;
        data[offset + 43] = field_11_unused3;
        System.arraycopy( remainingData, 0, data, offset + 44, remainingData.length );
        int pos = offset + 8 + 36 + remainingData.length;

        listener.afterRecordSerialize(pos, getRecordId(), pos - offset, this);
        return pos - offset;
    }

    /**
     * Returns the number of bytes that are required to serialize this record.
     *
     * @return Number of bytes
     */
    public int getRecordSize()
    {
        return 8 + 1 + 1 + 16 + 2 + 4 + 4 + 4 + 1 + 1 + 1 + 1 + remainingData.length;
    }

    /**
     * The short name for this record
     */
    public String getRecordName()
    {
        return "BSE";
    }

    /**
     * The expected blip type under windows (failure to match this blip type will result in
     * Excel converting to this format).
     */
    public byte getBlipTypeWin32()
    {
        return field_1_blipTypeWin32;
    }

    /**
     * Set the expected win32 blip type
     */
    public void setBlipTypeWin32( byte blipTypeWin32 )
    {
        this.field_1_blipTypeWin32 = blipTypeWin32;
    }

    /**
     * The expected blip type under MacOS (failure to match this blip type will result in
     * Excel converting to this format).
     */
    public byte getBlipTypeMacOS()
    {
        return field_2_blipTypeMacOS;
    }

    /**
     * Set the expected MacOS blip type
     */
    public void setBlipTypeMacOS( byte blipTypeMacOS )
    {
        this.field_2_blipTypeMacOS = blipTypeMacOS;
    }

    /**
     * 16 byte MD4 checksum.
     */
    public byte[] getUid()
    {
        return field_3_uid;
    }

    /**
     * 16 byte MD4 checksum.
     */
    public void setUid( byte[] uid )
    {
        this.field_3_uid = uid;
    }

    /**
     * unused
     */
    public short getTag()
    {
        return field_4_tag;
    }

    /**
     * unused
     */
    public void setTag( short tag )
    {
        this.field_4_tag = tag;
    }

    /**
     * Blip size in stream.
     */
    public int getSize()
    {
        return field_5_size;
    }

    /**
     * Blip size in stream.
     */
    public void setSize( int size )
    {
        this.field_5_size = size;
    }

    /**
     * The reference count of this blip.
     */
    public int getRef()
    {
        return field_6_ref;
    }

    /**
     * The reference count of this blip.
     */
    public void setRef( int ref )
    {
        this.field_6_ref = ref;
    }

    /**
     * File offset in the delay stream.
     */
    public int getOffset()
    {
        return field_7_offset;
    }

    /**
     * File offset in the delay stream.
     */
    public void setOffset( int offset )
    {
        this.field_7_offset = offset;
    }

    /**
     * Defines the way this blip is used.
     */
    public byte getUsage()
    {
        return field_8_usage;
    }

    /**
     * Defines the way this blip is used.
     */
    public void setUsage( byte usage )
    {
        this.field_8_usage = usage;
    }

    /**
     * The length in characters of the blip name.
     */
    public byte getName()
    {
        return field_9_name;
    }

    /**
     * The length in characters of the blip name.
     */
    public void setName( byte name )
    {
        this.field_9_name = name;
    }

    public byte getUnused2()
    {
        return field_10_unused2;
    }

    public void setUnused2( byte unused2 )
    {
        this.field_10_unused2 = unused2;
    }

    public byte getUnused3()
    {
        return field_11_unused3;
    }

    public void setUnused3( byte unused3 )
    {
        this.field_11_unused3 = unused3;
    }

    /**
     * Any remaining data in this record.
     */
    public byte[] getRemainingData()
    {
        return remainingData;
    }

    /**
     * Any remaining data in this record.
     */
    public void setRemainingData( byte[] remainingData )
    {
        this.remainingData = remainingData;
    }

    /**
     * Calculate the string representation of this object
     */
    public String toString()
    {
        String nl = System.getProperty( "line.separator" );

        String extraData;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try
        {
            HexDump.dump( this.remainingData, 0, b, 0 );
            extraData = b.toString();
        }
        catch ( Exception e )
        {
            extraData = e.toString();
        }
        return getClass().getName() + ":" + nl +
                "  RecordId: 0x" + HexDump.toHex( RECORD_ID ) + nl +
                "  Options: 0x" + HexDump.toHex( getOptions() ) + nl +
                "  BlipTypeWin32: " + field_1_blipTypeWin32 + nl +
                "  BlipTypeMacOS: " + field_2_blipTypeMacOS + nl +
                "  SUID: " + HexDump.toHex(field_3_uid) + nl +
                "  Tag: " + field_4_tag + nl +
                "  Size: " + field_5_size + nl +
                "  Ref: " + field_6_ref + nl +
                "  Offset: " + field_7_offset + nl +
                "  Usage: " + field_8_usage + nl +
                "  Name: " + field_9_name + nl +
                "  Unused2: " + field_10_unused2 + nl +
                "  Unused3: " + field_11_unused3 + nl +
                "  Extra Data:" + nl + extraData;


    }

    /**
     * Retrieve the string representation given a blip id.
     */
    public String getBlipType( byte b )
    {
        switch ( b )
        {
            case BT_ERROR:
                return " ERROR";
            case BT_UNKNOWN:
                return " UNKNOWN";
            case BT_EMF:
                return " EMF";
            case BT_WMF:
                return " WMF";
            case BT_PICT:
                return " PICT";
            case BT_JPEG:
                return " JPEG";
            case BT_PNG:
                return " PNG";
            case BT_DIB:
                return " DIB";
            default:
                if ( b < 32 )
                    return " NotKnown";
                else
                    return " Client";
        }
    }


}
