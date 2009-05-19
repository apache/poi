
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;
import java.util.zip.DeflaterOutputStream;

/**
 * The blip record is used to hold details about large binary objects that occur in escher such
 * as JPEG, GIF, PICT and WMF files.  The contents of the stream is usually compressed.  Inflate
 * can be used to decompress the data.
 *
 * @author Glen Stampoultzis
 * @see java.util.zip.Inflater
 */
public class EscherBlipWMFRecord
        extends EscherBlipRecord
{
//    public static final short  RECORD_ID_START    = (short) 0xF018;
//    public static final short  RECORD_ID_END      = (short) 0xF117;
    public static final String RECORD_DESCRIPTION = "msofbtBlip";
    private static final int   HEADER_SIZE               = 8;

    private              byte[] field_1_secondaryUID;
    private              int    field_2_cacheOfSize;
    private              int    field_3_boundaryTop;
    private              int    field_4_boundaryLeft;
    private              int    field_5_boundaryWidth;
    private              int    field_6_boundaryHeight;
    private              int    field_7_width;
    private              int    field_8_height;
    private              int    field_9_cacheOfSavedSize;
    private              byte   field_10_compressionFlag;
    private              byte   field_11_filter;
    private              byte[] field_12_data;

    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesAfterHeader = readHeader( data, offset );
        int pos              = offset + HEADER_SIZE;

        int size = 0;
        field_1_secondaryUID = new byte[16];
        System.arraycopy( data, pos + size, field_1_secondaryUID, 0, 16 ); size += 16;
        field_2_cacheOfSize      =  LittleEndian.getInt( data, pos + size );size+=4;
        field_3_boundaryTop      =  LittleEndian.getInt( data, pos + size );size+=4;
        field_4_boundaryLeft     =  LittleEndian.getInt( data, pos + size );size+=4;
        field_5_boundaryWidth    =  LittleEndian.getInt( data, pos + size );size+=4;
        field_6_boundaryHeight   =  LittleEndian.getInt( data, pos + size );size+=4;
        field_7_width            =  LittleEndian.getInt( data, pos + size );size+=4;
        field_8_height           =  LittleEndian.getInt( data, pos + size );size+=4;
        field_9_cacheOfSavedSize =  LittleEndian.getInt( data, pos + size );size+=4;
        field_10_compressionFlag = data[pos + size]; size++;
        field_11_filter          = data[pos + size]; size++;

        int bytesRemaining = bytesAfterHeader - size;
        field_12_data = new byte[bytesRemaining];
        System.arraycopy(data, pos + size, field_12_data, 0, bytesRemaining);
        size += bytesRemaining;

        return HEADER_SIZE + size;
    }

    public int serialize( int offset, byte[] data, EscherSerializationListener listener )
    {
        listener.beforeRecordSerialize(offset, getRecordId(), this);

        LittleEndian.putShort( data, offset, getOptions() );
        LittleEndian.putShort( data, offset + 2, getRecordId() );
        int remainingBytes = field_12_data.length + 36;
        LittleEndian.putInt( data, offset + 4, remainingBytes );

        int pos = offset + HEADER_SIZE;
        System.arraycopy(field_1_secondaryUID, 0, data, pos, 16 ); pos += 16;
        LittleEndian.putInt( data, pos, field_2_cacheOfSize); pos += 4;
        LittleEndian.putInt( data, pos, field_3_boundaryTop); pos += 4;
        LittleEndian.putInt( data, pos, field_4_boundaryLeft); pos += 4;
        LittleEndian.putInt( data, pos, field_5_boundaryWidth); pos += 4;
        LittleEndian.putInt( data, pos, field_6_boundaryHeight); pos += 4;
        LittleEndian.putInt( data, pos, field_7_width); pos += 4;
        LittleEndian.putInt( data, pos, field_8_height); pos += 4;
        LittleEndian.putInt( data, pos, field_9_cacheOfSavedSize); pos += 4;
        data[pos++] = field_10_compressionFlag;
        data[pos++] = field_11_filter;
        System.arraycopy(field_12_data, 0, data, pos, field_12_data.length); pos += field_12_data.length;

        listener.afterRecordSerialize(pos, getRecordId(), pos - offset, this);
        return pos - offset;
    }

    public int getRecordSize()
    {
        return 58 + field_12_data.length;
    }

    public String getRecordName() {
        return "Blip";
    }

    /**
     * Retrieve the secondary UID
     */
    public byte[] getSecondaryUID()
    {
        return field_1_secondaryUID;
    }

    /**
     * Set the secondary UID
     */
    public void setSecondaryUID( byte[] field_1_secondaryUID )
    {
        this.field_1_secondaryUID = field_1_secondaryUID;
    }

    /**
     * Retrieve the cache of the metafile size
     */
    public int getCacheOfSize()
    {
        return field_2_cacheOfSize;
    }

    /**
     * Set the cache of the metafile size
     */
    public void setCacheOfSize( int field_2_cacheOfSize )
    {
        this.field_2_cacheOfSize = field_2_cacheOfSize;
    }

    /**
     * Retrieve the top boundary of the metafile drawing commands
     */
    public int getBoundaryTop()
    {
        return field_3_boundaryTop;
    }

    /**
     * Set the top boundary of the metafile drawing commands
     */
    public void setBoundaryTop( int field_3_boundaryTop )
    {
        this.field_3_boundaryTop = field_3_boundaryTop;
    }

    /**
     * Retrieve the left boundary of the metafile drawing commands
     */
    public int getBoundaryLeft()
    {
        return field_4_boundaryLeft;
    }

    /**
     * Set the left boundary of the metafile drawing commands
     */
    public void setBoundaryLeft( int field_4_boundaryLeft )
    {
        this.field_4_boundaryLeft = field_4_boundaryLeft;
    }

    /**
     * Retrieve the boundary width of the metafile drawing commands
     */
    public int getBoundaryWidth()
    {
        return field_5_boundaryWidth;
    }

    /**
     * Set the boundary width of the metafile drawing commands
     */
    public void setBoundaryWidth( int field_5_boundaryWidth )
    {
        this.field_5_boundaryWidth = field_5_boundaryWidth;
    }

    /**
     * Retrieve the boundary height of the metafile drawing commands
     */
    public int getBoundaryHeight()
    {
        return field_6_boundaryHeight;
    }

    /**
     * Set the boundary height of the metafile drawing commands
     */
    public void setBoundaryHeight( int field_6_boundaryHeight )
    {
        this.field_6_boundaryHeight = field_6_boundaryHeight;
    }

    /**
     * Retrieve the width of the metafile in EMU's (English Metric Units).
     */
    public int getWidth()
    {
        return field_7_width;
    }

    /**
     * Set the width of the metafile in EMU's (English Metric Units).
     */
    public void setWidth( int width )
    {
        this.field_7_width = width;
    }

    /**
     * Retrieve the height of the metafile in EMU's (English Metric Units).
     */
    public int getHeight()
    {
        return field_8_height;
    }

    /**
     * Set the height of the metafile in EMU's (English Metric Units).
     */
    public void setHeight( int height )
    {
        this.field_8_height = height;
    }

    /**
     * Retrieve the cache of the saved size
     */
    public int getCacheOfSavedSize()
    {
        return field_9_cacheOfSavedSize;
    }

    /**
     * Set the cache of the saved size
     */
    public void setCacheOfSavedSize( int field_9_cacheOfSavedSize )
    {
        this.field_9_cacheOfSavedSize = field_9_cacheOfSavedSize;
    }

    /**
     * Is the contents of the blip compressed?
     */
    public byte getCompressionFlag()
    {
        return field_10_compressionFlag;
    }

    /**
     * Set whether the contents of the blip is compressed
     */
    public void setCompressionFlag( byte field_10_compressionFlag )
    {
        this.field_10_compressionFlag = field_10_compressionFlag;
    }

    /**
     * Filter should always be 0
     */
    public byte getFilter()
    {
        return field_11_filter;
    }

    /**
     * Filter should always be 0
     */
    public void setFilter( byte field_11_filter )
    {
        this.field_11_filter = field_11_filter;
    }

    /**
     * The BLIP data
     */
    public byte[] getData()
    {
        return field_12_data;
    }

    /**
     * The BLIP data
     */
    public void setData( byte[] field_12_data )
    {
        this.field_12_data = field_12_data;
    }

    /**
     * The string representation of this record.
     *
     * @return A string
     */
    public String toString()
    {
        String nl = System.getProperty( "line.separator" );

        String extraData;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try
        {
            HexDump.dump( this.field_12_data, 0, b, 0 );
            extraData = b.toString();
        }
        catch ( Exception e )
        {
            extraData = e.toString();
        }
        return getClass().getName() + ":" + nl +
                "  RecordId: 0x" + HexDump.toHex( getRecordId() ) + nl +
                "  Options: 0x" + HexDump.toHex( getOptions() ) + nl +
                "  Secondary UID: " + HexDump.toHex( field_1_secondaryUID ) + nl +
                "  CacheOfSize: " + field_2_cacheOfSize + nl +
                "  BoundaryTop: " + field_3_boundaryTop + nl +
                "  BoundaryLeft: " + field_4_boundaryLeft + nl +
                "  BoundaryWidth: " + field_5_boundaryWidth + nl +
                "  BoundaryHeight: " + field_6_boundaryHeight + nl +
                "  X: " + field_7_width + nl +
                "  Y: " + field_8_height + nl +
                "  CacheOfSavedSize: " + field_9_cacheOfSavedSize + nl +
                "  CompressionFlag: " + field_10_compressionFlag + nl +
                "  Filter: " + field_11_filter + nl +
                "  Data:" + nl + extraData;
    }

    /**
     * Compress the contents of the provided array
     *
     * @param data An uncompressed byte array
     * @see DeflaterOutputStream#write(int b)
     */
    public static byte[] compress( byte[] data )
    {
        ByteArrayOutputStream out                  = new ByteArrayOutputStream();
        DeflaterOutputStream  deflaterOutputStream = new DeflaterOutputStream( out );
        try
        {
            for ( int i = 0; i < data.length; i++ )
                deflaterOutputStream.write( data[i] );
        }
        catch ( IOException e )
        {
            throw new RecordFormatException( e.toString() );
        }

        return out.toByteArray();
    }

    /**
     * Decompresses a byte array.
     *
     * @param data   The compressed byte array
     * @param pos    The starting position into the byte array
     * @param length The number of compressed bytes to decompress
     * @return An uncompressed byte array
     * @see InflaterInputStream#read
     */
    public static byte[] decompress( byte[] data, int pos, int length )
    {
        byte[] compressedData = new byte[length];
        System.arraycopy( data, pos + 50, compressedData, 0, length );
        InputStream           compressedInputStream = new ByteArrayInputStream( compressedData );
        InflaterInputStream   inflaterInputStream   = new InflaterInputStream( compressedInputStream );
        ByteArrayOutputStream out                   = new ByteArrayOutputStream();
        int                   c;
        try
        {
            while ( ( c = inflaterInputStream.read() ) != -1 )
                out.write( c );
        }
        catch ( IOException e )
        {
            throw new RecordFormatException( e.toString() );
        }
        return out.toByteArray();
    }

}
