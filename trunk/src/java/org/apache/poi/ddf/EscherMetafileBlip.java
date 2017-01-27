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
import org.apache.poi.hssf.usermodel.HSSFPictureData;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;
import java.util.zip.DeflaterOutputStream;

public final class EscherMetafileBlip extends EscherBlipRecord {
    private static final POILogger log = POILogFactory.getLogger(EscherMetafileBlip.class);

    public static final short RECORD_ID_EMF = (short) 0xF018 + 2;
    public static final short RECORD_ID_WMF = (short) 0xF018 + 3;
    public static final short RECORD_ID_PICT = (short) 0xF018 + 4;

    private static final int HEADER_SIZE = 8;

    private final byte[] field_1_UID = new byte[16];
    /**
     * The primary UID is only saved to disk if (blip_instance ^ blip_signature == 1)
     */
    private final byte[] field_2_UID = new byte[16];
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
    private byte[] remainingData;

    @Override
    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesAfterHeader = readHeader( data, offset );
        int pos = offset + HEADER_SIZE;
        System.arraycopy( data, pos, field_1_UID, 0, 16 ); pos += 16;

        if((getOptions() ^ getSignature()) == 0x10){
            System.arraycopy( data, pos, field_2_UID, 0, 16 ); pos += 16;
        }

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
        pos += field_5_cbSave;

        // 0 means DEFLATE compression
        // 0xFE means no compression
        if (field_6_fCompression == 0) {
            super.setPictureData(inflatePictureData(raw_pictureData));
        } else {
            super.setPictureData(raw_pictureData);
        }

        int remaining = bytesAfterHeader - pos + offset + HEADER_SIZE;
        if(remaining > 0) {
            remainingData = new byte[remaining];
            System.arraycopy( data, pos, remainingData, 0, remaining );
        }
        return bytesAfterHeader + HEADER_SIZE;
    }

    @Override
    public int serialize(int offset, byte[] data, EscherSerializationListener listener) {
        listener.beforeRecordSerialize(offset, getRecordId(), this);

        int pos = offset;
        LittleEndian.putShort( data, pos, getOptions() ); pos += 2;
        LittleEndian.putShort( data, pos, getRecordId() ); pos += 2;
        LittleEndian.putInt( data, pos, getRecordSize() - HEADER_SIZE ); pos += 4;

        System.arraycopy( field_1_UID, 0, data, pos, field_1_UID.length ); pos += field_1_UID.length;
        if ((getOptions() ^ getSignature()) == 0x10) {
            System.arraycopy( field_2_UID, 0, data, pos, field_2_UID.length ); pos += field_2_UID.length;
        }
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

        System.arraycopy( raw_pictureData, 0, data, pos, raw_pictureData.length ); pos += raw_pictureData.length;
        if(remainingData != null) {
            System.arraycopy( remainingData, 0, data, pos, remainingData.length ); pos += remainingData.length;
        }

        listener.afterRecordSerialize(offset + getRecordSize(), getRecordId(), getRecordSize(), this);
        return getRecordSize();
    }

    /**
     * Decompresses the provided data, returning the inflated result.
     *
     * @param data the deflated picture data.
     * @return the inflated picture data.
     */
    private static byte[] inflatePictureData(byte[] data) {
        try {
            InflaterInputStream in = new InflaterInputStream(
                new ByteArrayInputStream( data ) );
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int readBytes;
            while ((readBytes = in.read(buf)) > 0) {
                out.write(buf, 0, readBytes);
            }
            return out.toByteArray();
        } catch (IOException e) {
            log.log(POILogger.WARN, "Possibly corrupt compression or non-compressed data", e);
            return data;
        }
    }

    @Override
    public int getRecordSize() {
        int size = 8 + 50 + raw_pictureData.length;
        if(remainingData != null) size += remainingData.length;
        if((getOptions() ^ getSignature()) == 0x10){
            size += field_2_UID.length;
        }
        return size;
    }

    /**
     * Gets the first MD4, that specifies the unique identifier of the
     * uncompressed blip data
     *
     * @return the first MD4
     */
    public byte[] getUID() {
        return field_1_UID;
    }

    /**
     * Sets the first MD4, that specifies the unique identifier of the
     * uncompressed blip data
     *
     * @param uid the first MD4
     */
    public void setUID(byte[] uid) {
        if (uid == null || uid.length != 16) {
            throw new IllegalArgumentException("uid must be byte[16]");
        }
        System.arraycopy(uid, 0, field_1_UID, 0, field_1_UID.length);
    }

    /**
     * Gets the second MD4, that specifies the unique identifier of the
     * uncompressed blip data
     *
     * @return the second MD4
     */
    public byte[] getPrimaryUID() {
        return field_2_UID;
    }

    /**
     * Sets the second MD4, that specifies the unique identifier of the
     * uncompressed blip data
     *
     * @param primaryUID the second MD4
     */
    public void setPrimaryUID(byte[] primaryUID) {
        if (primaryUID == null || primaryUID.length != 16) {
            throw new IllegalArgumentException("primaryUID must be byte[16]");
        }
        System.arraycopy(primaryUID, 0, field_2_UID, 0, field_2_UID.length);
    }

    /**
     * Gets the uncompressed size (in bytes)
     *
     * @return the uncompressed size
     */
    public int getUncompressedSize() {
        return field_2_cb;
    }

    /**
     * Sets the uncompressed size (in bytes)
     *
     * @param uncompressedSize the uncompressed size
     */
    public void setUncompressedSize(int uncompressedSize) {
        field_2_cb = uncompressedSize;
    }

    /**
     * Get the clipping region of the metafile
     *
     * @return the clipping region
     */
    public Rectangle getBounds() {
        return new Rectangle(field_3_rcBounds_x1,
                             field_3_rcBounds_y1,
                             field_3_rcBounds_x2 - field_3_rcBounds_x1,
                             field_3_rcBounds_y2 - field_3_rcBounds_y1);
    }

    /**
     * Sets the clipping region
     *
     * @param bounds the clipping region
     */
    public void setBounds(Rectangle bounds) {
        field_3_rcBounds_x1 = bounds.x;
        field_3_rcBounds_y1 = bounds.y;
        field_3_rcBounds_x2 = bounds.x + bounds.width;
        field_3_rcBounds_y2 = bounds.y + bounds.height;
    }

    /**
     * Gets the dimensions of the metafile 
     *
     * @return the dimensions of the metafile
     */
    public Dimension getSizeEMU() {
        return new Dimension(field_4_ptSize_w, field_4_ptSize_h);
    }

    /**
     * Gets the dimensions of the metafile
     *
     * @param sizeEMU the dimensions of the metafile
     */
    public void setSizeEMU(Dimension sizeEMU) {
        field_4_ptSize_w = sizeEMU.width;
        field_4_ptSize_h = sizeEMU.height;
    }

    /**
     * Gets the compressed size of the metafile (in bytes)
     * 
     * @return the compressed size
     */
    public int getCompressedSize() {
        return field_5_cbSave;
    }

    /**
     * Sets the compressed size of the metafile (in bytes)
     *
     * @param compressedSize the compressed size
     */
    public void setCompressedSize(int compressedSize) {
        field_5_cbSave = compressedSize;
    }

    /**
     * Gets the compression of the metafile
     *
     * @return true, if the metafile is compressed
     */
    public boolean isCompressed() {
        return (field_6_fCompression == 0);
    }

    /**
     * Sets the compression of the metafile
     *
     * @param compressed the compression state, true if it's compressed
     */
    public void setCompressed(boolean compressed) {
        field_6_fCompression = compressed ? 0 : (byte)0xFE;
    }

    /**
     * Returns any remaining bytes
     *
     * @return any remaining bytes
     */
    public byte[] getRemainingData() {
        return remainingData;
    }

    // filtering is always 254 according to available docs, so no point giving it a setter method.

    @Override
    public String toString() {
        String extraData = "";//HexDump.toHex(field_pictureData, 32);
        return getClass().getName() + ":" + '\n' +
                "  RecordId: 0x" + HexDump.toHex( getRecordId() ) + '\n' +
                "  Version: 0x" + HexDump.toHex( getVersion() ) + '\n' +
                "  Instance: 0x" + HexDump.toHex( getInstance() ) + '\n' +
                "  UID: 0x" + HexDump.toHex( field_1_UID ) + '\n' +
                (field_2_UID == null ? "" : ("  UID2: 0x" + HexDump.toHex( field_2_UID ) + '\n')) +
                "  Uncompressed Size: " + HexDump.toHex( field_2_cb ) + '\n' +
                "  Bounds: " + getBounds() + '\n' +
                "  Size in EMU: " + getSizeEMU() + '\n' +
                "  Compressed Size: " + HexDump.toHex( field_5_cbSave ) + '\n' +
                "  Compression: " + HexDump.toHex( field_6_fCompression ) + '\n' +
                "  Filter: " + HexDump.toHex( field_7_fFilter ) + '\n' +
                "  Extra Data:" + '\n' + extraData +
                (remainingData == null ? null : ("\n" +
                 " Remaining Data: " + HexDump.toHex(remainingData, 32)));
    }

    @Override
    public String toXml(String tab) {
        String extraData = "";
        StringBuilder builder = new StringBuilder();
        builder.append(tab).append(formatXmlRecordHeader(getClass().getSimpleName(), HexDump.toHex(getRecordId()), HexDump.toHex(getVersion()), HexDump.toHex(getInstance())))
                .append(tab).append("\t").append("<UID>0x").append(HexDump.toHex( field_1_UID ) + '\n' +
                                (field_2_UID == null ? "" : ("  UID2: 0x" + HexDump.toHex( field_2_UID ) + '\n'))).append("</UID>\n")
                .append(tab).append("\t").append("<UncompressedSize>0x").append(HexDump.toHex( field_2_cb )).append("</UncompressedSize>\n")
                .append(tab).append("\t").append("<Bounds>").append(getBounds()).append("</Bounds>\n")
                .append(tab).append("\t").append("<SizeInEMU>").append(getSizeEMU()).append("</SizeInEMU>\n")
                .append(tab).append("\t").append("<CompressedSize>0x").append(HexDump.toHex( field_5_cbSave )).append("</CompressedSize>\n")
                .append(tab).append("\t").append("<Compression>0x").append(HexDump.toHex( field_6_fCompression )).append("</Compression>\n")
                .append(tab).append("\t").append("<Filter>0x").append(HexDump.toHex( field_7_fFilter )).append("</Filter>\n")
                .append(tab).append("\t").append("<ExtraData>").append(extraData).append("</ExtraData>\n")
                .append(tab).append("\t").append("<RemainingData>0x").append(HexDump.toHex(remainingData, 32)).append("</RemainingData>\n");
        builder.append(tab).append("</").append(getClass().getSimpleName()).append(">\n");
        return builder.toString();
    }

    /**
     * Return the blip signature
     *
     * @return the blip signature
     */
    public short getSignature() {
        switch (getRecordId()) {
            case RECORD_ID_EMF:  return HSSFPictureData.MSOBI_EMF;
            case RECORD_ID_WMF:  return HSSFPictureData.MSOBI_WMF;
            case RECORD_ID_PICT: return HSSFPictureData.MSOBI_PICT;
        }
        if (log.check(POILogger.WARN)) {
            log.log(POILogger.WARN, "Unknown metafile: " + getRecordId());
        }
        return 0;
    }

    @Override
    public void setPictureData(byte[] pictureData) {
    	super.setPictureData(pictureData);
        setUncompressedSize(pictureData.length);

        // info of chicago project:
        // "... LZ compression algorithm in the format used by GNU Zip deflate/inflate with a 32k window ..."
        // not sure what to do, when lookup tables exceed 32k ...

        try {
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        DeflaterOutputStream dos = new DeflaterOutputStream(bos);
	        dos.write(pictureData);
	        dos.close();
	        raw_pictureData = bos.toByteArray();
        } catch (IOException e) {
        	throw new RuntimeException("Can't compress metafile picture data", e);
        }
        
        setCompressedSize(raw_pictureData.length);
        setCompressed(true);
    }

    /**
     * Sets the filter byte - usually this is 0xFE
     *
     * @param filter the filter byte
     */
    public void setFilter(byte filter) {
    	field_7_fFilter = filter;
    }
}
