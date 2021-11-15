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

import static org.apache.logging.log4j.util.Unbox.box;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFPictureData;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.Removal;

public final class EscherMetafileBlip extends EscherBlipRecord {
    private static final Logger LOGGER = LogManager.getLogger(EscherMetafileBlip.class);
    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 100_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    /** @deprecated use EscherRecordTypes.BLIP_EMF.typeID */
    @Deprecated
    @Removal(version = "5.3")
    public static final short RECORD_ID_EMF = EscherRecordTypes.BLIP_EMF.typeID;
    /** @deprecated use EscherRecordTypes.BLIP_WMF.typeID */
    @Deprecated
    @Removal(version = "5.3")
    public static final short RECORD_ID_WMF = EscherRecordTypes.BLIP_WMF.typeID;
    /** @deprecated use EscherRecordTypes.BLIP_PICT.typeID */
    @Deprecated
    @Removal(version = "5.3")
    public static final short RECORD_ID_PICT = EscherRecordTypes.BLIP_PICT.typeID;

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

    /**
     * @param length the max record length allowed for EscherMetafileBlip
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for EscherMetafileBlip
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    public EscherMetafileBlip() {}

    public EscherMetafileBlip(EscherMetafileBlip other) {
        super(other);
        System.arraycopy(other.field_1_UID, 0, field_1_UID, 0, field_1_UID.length);
        System.arraycopy(other.field_2_UID, 0, field_2_UID, 0, field_2_UID.length);
        field_2_cb = other.field_2_cb;
        field_3_rcBounds_x1 = other.field_3_rcBounds_x1;
        field_3_rcBounds_y1 = other.field_3_rcBounds_y1;
        field_3_rcBounds_x2 = other.field_3_rcBounds_x2;
        field_3_rcBounds_y2 = other.field_3_rcBounds_y2;
        field_4_ptSize_h = other.field_4_ptSize_h;
        field_4_ptSize_w = other.field_4_ptSize_w;
        field_5_cbSave = other.field_5_cbSave;
        field_6_fCompression = other.field_6_fCompression;
        field_7_fFilter = other.field_7_fFilter;
        raw_pictureData = (other.raw_pictureData == null) ? null : other.raw_pictureData.clone();
        remainingData = (other.remainingData == null) ? null : other.remainingData.clone();
    }

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

        raw_pictureData = IOUtils.safelyClone(data, pos, field_5_cbSave, MAX_RECORD_LENGTH);
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
            remainingData = IOUtils.safelyClone(data, pos, remaining, MAX_RECORD_LENGTH);
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

        System.arraycopy( raw_pictureData, 0, data, pos, raw_pictureData.length );
        pos += raw_pictureData.length;
        if(remainingData != null) {
            System.arraycopy( remainingData, 0, data, pos, remainingData.length );
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
        try (InflaterInputStream in = new InflaterInputStream(new UnsynchronizedByteArrayInputStream(data));
             UnsynchronizedByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream()) {
            IOUtils.copy(in, out);
            return out.toByteArray();
        } catch (IOException e) {
            LOGGER.atWarn().withThrowable(e).log("Possibly corrupt compression or non-compressed data");
            return data;
        }
    }

    @Override
    public int getRecordSize() {
        int size = 8 + 50 + raw_pictureData.length;
        if(remainingData != null) {
            size += remainingData.length;
        }
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
     * Gets the filter byte - this is usually 0xFE
     *
     * @return the filter byte
     */
    public byte getFilter() {
        return field_7_fFilter;
    }

    /**
     * Sets the filter byte - this is usually 0xFE
     *
     * @param filter the filter byte
     */
    public void setFilter(byte filter) {
        field_7_fFilter = filter;
    }



    /**
     * Returns any remaining bytes
     *
     * @return any remaining bytes
     */
    public byte[] getRemainingData() {
        return remainingData;
    }

    /**
     * Return the blip signature
     *
     * @return the blip signature
     */
    public short getSignature() {
        switch (EscherRecordTypes.forTypeID(getRecordId())) {
            case BLIP_EMF:  return HSSFPictureData.MSOBI_EMF;
            case BLIP_WMF:  return HSSFPictureData.MSOBI_WMF;
            case BLIP_PICT: return HSSFPictureData.MSOBI_PICT;
        }
        LOGGER.atWarn().log("Unknown metafile: {}", box(getRecordId()));
        return 0;
    }

    @Override
    public void setPictureData(byte[] pictureData) {
        super.setPictureData(pictureData);
        setUncompressedSize(pictureData.length);

        // info of chicago project:
        // "... LZ compression algorithm in the format used by GNU Zip deflate/inflate with a 32k window ..."
        // not sure what to do, when lookup tables exceed 32k ...

        try (UnsynchronizedByteArrayOutputStream bos = new UnsynchronizedByteArrayOutputStream()) {
            try (DeflaterOutputStream dos = new DeflaterOutputStream(bos)) {
                dos.write(pictureData);
            }
            raw_pictureData = bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Can't compress metafile picture data", e);
        }

        setCompressedSize(raw_pictureData.length);
        setCompressed(true);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        final Map<String, Supplier<?>> m = new LinkedHashMap<>(super.getGenericProperties());
        m.put("uid", this::getUID);
        m.put("uncompressedSize", this::getUncompressedSize);
        m.put("bounds", this::getBounds);
        m.put("sizeInEMU", this::getSizeEMU);
        m.put("compressedSize", this::getCompressedSize);
        m.put("isCompressed", this::isCompressed);
        m.put("filter", this::getFilter);
        return Collections.unmodifiableMap(m);
    }

    @Override
    public EscherMetafileBlip copy() {
        return new EscherMetafileBlip(this);
    }
}
