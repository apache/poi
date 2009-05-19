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
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

/**
 * @author Daniel Noll
 */
public final class EscherPictBlip extends EscherBlipRecord {
    private static final POILogger log = POILogFactory.getLogger(EscherPictBlip.class);

    public static final short RECORD_ID_EMF = (short) 0xF018 + 2;
    public static final short RECORD_ID_WMF = (short) 0xF018 + 3;
    public static final short RECORD_ID_PICT = (short) 0xF018 + 4;

    private static final int HEADER_SIZE = 8;

    private byte[] field_1_UID;
    private int field_2_cb;
    private int field_3_rcBounds_x1;
    private int field_3_rcBounds_y1;
    private int field_3_rcBounds_x2;
    private int field_3_rcBounds_y2;
    private int field_4_ptSize_w;
    private int field_4_ptSize_h;
    private int field_5_cbSave;
    private byte field_6_fCompression;
    private byte field_7_fFilter;

    private byte[] raw_pictureData;

    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesAfterHeader = readHeader(data, offset);
        int pos = offset + HEADER_SIZE;

        field_1_UID = new byte[16];
        System.arraycopy( data, pos, field_1_UID, 0, 16 ); pos += 16;
        field_2_cb = LittleEndian.getInt( data, pos ); pos += 4;
        field_3_rcBounds_x1 = LittleEndian.getInt( data, pos ); pos += 4;
        field_3_rcBounds_y1 = LittleEndian.getInt( data, pos ); pos += 4;
        field_3_rcBounds_x2 = LittleEndian.getInt( data, pos ); pos += 4;
        field_3_rcBounds_y2 = LittleEndian.getInt( data, pos ); pos += 4;
        field_4_ptSize_w = LittleEndian.getInt( data, pos ); pos += 4;
        field_4_ptSize_h = LittleEndian.getInt( data, pos ); pos += 4;
        field_5_cbSave = LittleEndian.getInt( data, pos ); pos += 4;
        field_6_fCompression = data[pos]; pos++;
        field_7_fFilter = data[pos]; pos++;

        raw_pictureData = new byte[field_5_cbSave];
        System.arraycopy( data, pos, raw_pictureData, 0, field_5_cbSave );

        // 0 means DEFLATE compression
        // 0xFE means no compression
        if (field_6_fCompression == 0)
        {
            field_pictureData = inflatePictureData(raw_pictureData);
        }
        else
        {
            field_pictureData = raw_pictureData;
        }

        return bytesAfterHeader + HEADER_SIZE;
    }

    public int serialize(int offset, byte[] data, EscherSerializationListener listener) {
        listener.beforeRecordSerialize(offset, getRecordId(), this);

        int pos = offset;
        LittleEndian.putShort( data, pos, getOptions() ); pos += 2;
        LittleEndian.putShort( data, pos, getRecordId() ); pos += 2;
        LittleEndian.putInt( data, getRecordSize() - HEADER_SIZE ); pos += 4;

        System.arraycopy( field_1_UID, 0, data, pos, 16 ); pos += 16;
        LittleEndian.putInt( data, pos, field_2_cb ); pos += 4;
        LittleEndian.putInt( data, pos, field_3_rcBounds_x1 ); pos += 4;
        LittleEndian.putInt( data, pos, field_3_rcBounds_y1 ); pos += 4;
        LittleEndian.putInt( data, pos, field_3_rcBounds_x2 ); pos += 4;
        LittleEndian.putInt( data, pos, field_3_rcBounds_y2 ); pos += 4;
        LittleEndian.putInt( data, pos, field_4_ptSize_w ); pos += 4;
        LittleEndian.putInt( data, pos, field_4_ptSize_h ); pos += 4;
        LittleEndian.putInt( data, pos, field_5_cbSave ); pos += 4;
        data[pos] = field_6_fCompression; pos++;
        data[pos] = field_7_fFilter; pos++;

        System.arraycopy( raw_pictureData, 0, data, pos, raw_pictureData.length );

        listener.afterRecordSerialize(offset + getRecordSize(), getRecordId(), getRecordSize(), this);
        return HEADER_SIZE + 16 + 1 + raw_pictureData.length;
    }

    /**
     * Decompresses the provided data, returning the inflated result.
     *
     * @param data the deflated picture data.
     * @return the inflated picture data.
     */
    private static byte[] inflatePictureData(byte[] data) {
        try {
            InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(data));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int readBytes;
            while ((readBytes = in.read(buf)) > 0) {
                out.write(buf, 0, readBytes);
            }
            return out.toByteArray();
        } catch (IOException e) {
            log.log(POILogger.INFO, "Possibly corrupt compression or non-compressed data", e);
            return data;
        }
    }

    public int getRecordSize() {
        return 8 + 50 + raw_pictureData.length;
    }

    public byte[] getUID() {
        return field_1_UID;
    }

    public void setUID(byte[] uid) {
        this.field_1_UID = uid;
    }

    public int getUncompressedSize() {
        return field_2_cb;
    }

    public void setUncompressedSize(int uncompressedSize) {
        field_2_cb = uncompressedSize;
    }

    public Rectangle getBounds() {
        return new Rectangle(field_3_rcBounds_x1,
                             field_3_rcBounds_y1,
                             field_3_rcBounds_x2 - field_3_rcBounds_x1,
                             field_3_rcBounds_y2 - field_3_rcBounds_y1);
    }

    public void setBounds(Rectangle bounds) {
        field_3_rcBounds_x1 = bounds.x;
        field_3_rcBounds_y1 = bounds.y;
        field_3_rcBounds_x2 = bounds.x + bounds.width;
        field_3_rcBounds_y2 = bounds.y + bounds.height;
    }

    public Dimension getSizeEMU() {
        return new Dimension(field_4_ptSize_w, field_4_ptSize_h);
    }

    public void setSizeEMU(Dimension sizeEMU) {
        field_4_ptSize_w = sizeEMU.width;
        field_4_ptSize_h = sizeEMU.height;
    }

    public int getCompressedSize() {
        return field_5_cbSave;
    }

    public void setCompressedSize(int compressedSize) {
        field_5_cbSave = compressedSize;
    }

    public boolean isCompressed() {
        return (field_6_fCompression == 0);
    }

    public void setCompressed(boolean compressed) {
        field_6_fCompression = compressed ? 0 : (byte)0xFE;
    }

    // filtering is always 254 according to available docs, so no point giving it a setter method.

    public String toString() {
        String extraData = HexDump.toHex(field_pictureData, 32);
        return getClass().getName() + ":" + '\n' +
                "  RecordId: 0x" + HexDump.toHex( getRecordId() ) + '\n' +
                "  Options: 0x" + HexDump.toHex( getOptions() ) + '\n' +
                "  UID: 0x" + HexDump.toHex( field_1_UID ) + '\n' +
                "  Uncompressed Size: " + HexDump.toHex( field_2_cb ) + '\n' +
                "  Bounds: " + getBounds() + '\n' +
                "  Size in EMU: " + getSizeEMU() + '\n' +
                "  Compressed Size: " + HexDump.toHex( field_5_cbSave ) + '\n' +
                "  Compression: " + HexDump.toHex( field_6_fCompression ) + '\n' +
                "  Filter: " + HexDump.toHex( field_7_fFilter ) + '\n' +
                "  Extra Data:" + '\n' + extraData;
    }
}
