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
 * The BSE record is related closely to the {@code EscherBlipRecord} and stores
 * extra information about the blip.  A blip record is actually stored inside
 * the BSE record even though the BSE record isn't actually a container record.
 *
 * @see EscherBlipRecord
 */
public final class EscherBSERecord extends EscherRecord {

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

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
    private final byte[] field_3_uid = new byte[16];
    private short field_4_tag;
    private int field_5_size;
    private int field_6_ref;
    private int field_7_offset;
    private byte field_8_usage;
    private byte field_9_name;
    private byte field_10_unused2;
    private byte field_11_unused3;
    private EscherBlipRecord field_12_blipRecord;

    private byte[] _remainingData = new byte[0];

    public EscherBSERecord() {
        setRecordId(RECORD_ID);
    }
    
    @Override
    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesRemaining = readHeader( data, offset );
        int pos = offset + 8;
        field_1_blipTypeWin32 = data[pos];
        field_2_blipTypeMacOS = data[pos + 1];
        System.arraycopy( data, pos + 2, field_3_uid, 0, 16 );
        field_4_tag = LittleEndian.getShort( data, pos + 18 );
        field_5_size = LittleEndian.getInt( data, pos + 20 );
        field_6_ref = LittleEndian.getInt( data, pos + 24 );
        field_7_offset = LittleEndian.getInt( data, pos + 28 );
        field_8_usage = data[pos + 32];
        field_9_name = data[pos + 33];
        field_10_unused2 = data[pos + 34];
        field_11_unused3 = data[pos + 35];
        bytesRemaining -= 36;

        int bytesRead = 0;
        if (bytesRemaining > 0) {
            // Some older escher formats skip this last record
            field_12_blipRecord = (EscherBlipRecord) recordFactory.createRecord( data, pos + 36 );
            bytesRead = field_12_blipRecord.fillFields( data, pos + 36, recordFactory );
        }
        pos += 36 + bytesRead;
        bytesRemaining -= bytesRead;

        _remainingData = IOUtils.safelyAllocate(bytesRemaining, MAX_RECORD_LENGTH);
        System.arraycopy( data, pos, _remainingData, 0, bytesRemaining );
        return bytesRemaining + 8 + 36 + (field_12_blipRecord == null ? 0 : field_12_blipRecord.getRecordSize()) ;

    }

    @Override
    public int serialize(int offset, byte[] data, EscherSerializationListener listener) {
        listener.beforeRecordSerialize( offset, getRecordId(), this );

        if (_remainingData == null) {
            _remainingData = new byte[0];
        }

        LittleEndian.putShort( data, offset, getOptions() );
        LittleEndian.putShort( data, offset + 2, getRecordId() );
        int blipSize = field_12_blipRecord == null ? 0 : field_12_blipRecord.getRecordSize();
        int remainingBytes = _remainingData.length + 36 + blipSize;
        LittleEndian.putInt( data, offset + 4, remainingBytes );

        data[offset + 8] = field_1_blipTypeWin32;
        data[offset + 9] = field_2_blipTypeMacOS;
        System.arraycopy(field_3_uid, 0, data, offset + 10, 16);
        LittleEndian.putShort( data, offset + 26, field_4_tag );
        LittleEndian.putInt( data, offset + 28, field_5_size );
        LittleEndian.putInt( data, offset + 32, field_6_ref );
        LittleEndian.putInt( data, offset + 36, field_7_offset );
        data[offset + 40] = field_8_usage;
        data[offset + 41] = field_9_name;
        data[offset + 42] = field_10_unused2;
        data[offset + 43] = field_11_unused3;
        int bytesWritten = 0;
        if (field_12_blipRecord != null) {
            bytesWritten = field_12_blipRecord.serialize( offset + 44, data, new NullEscherSerializationListener() );
        }
        System.arraycopy( _remainingData, 0, data, offset + 44 + bytesWritten, _remainingData.length );
        int pos = offset + 8 + 36 + _remainingData.length + bytesWritten;

        listener.afterRecordSerialize(pos, getRecordId(), pos - offset, this);
        return pos - offset;
    }

    @Override
    public int getRecordSize() {
        int field_12_size = 0;
        if(field_12_blipRecord != null) {
            field_12_size = field_12_blipRecord.getRecordSize();
        }
        int remaining_size = 0;
        if(_remainingData != null) {
            remaining_size = _remainingData.length;
        }
        return 8 + 1 + 1 + 16 + 2 + 4 + 4 + 4 + 1 + 1 +
            1 + 1 + field_12_size + remaining_size;
    }

    @Override
    public String getRecordName() {
        return "BSE";
    }

    /**
     * The expected blip type under windows (failure to match this blip type will result in
     * Excel converting to this format).
     * 
     * @return win32 blip type
     */
    public byte getBlipTypeWin32() {
        return field_1_blipTypeWin32;
    }

    /**
     * Set the expected win32 blip type
     * 
     * @param blipTypeWin32 win32 blip type
     */
    public void setBlipTypeWin32(byte blipTypeWin32) {
        field_1_blipTypeWin32 = blipTypeWin32;
    }

    /**
     * The expected blip type under MacOS (failure to match this blip type will result in
     * Excel converting to this format).
     * 
     * @return MacOS blip type
     */
    public byte getBlipTypeMacOS() {
        return field_2_blipTypeMacOS;
    }

    /**
     * Set the expected MacOS blip type
     * 
     * @param blipTypeMacOS MacOS blip type
     */
    public void setBlipTypeMacOS(byte blipTypeMacOS) {
        field_2_blipTypeMacOS = blipTypeMacOS;
    }

    /**
     * 16 byte MD4 checksum.
     * 
     * @return 16 byte MD4 checksum
     */
    public byte[] getUid() {
        return field_3_uid;
    }

    /**
     * 16 byte MD4 checksum.
     * 
     * @param uid 16 byte MD4 checksum
     */
    public void setUid(byte[] uid) {
        if (uid == null || uid.length != 16) {
            throw new IllegalArgumentException("uid must be byte[16]");
        }
        System.arraycopy(uid, 0, field_3_uid, 0, field_3_uid.length);
    }

    /**
     * unused
     * 
     * @return an unknown tag
     */
    public short getTag() {
        return field_4_tag;
    }

    /**
     * unused
     * 
     * @param tag unknown tag
     */
    public void setTag(short tag) {
        field_4_tag = tag;
    }

    /**
     * Blip size in stream.
     * 
     * @return the blip size
     */
    public int getSize() {
        return field_5_size;
    }

    /**
     * Blip size in stream.
     * 
     * @param size blip size
     */
    public void setSize(int size) {
        field_5_size = size;
    }

    /**
     * The reference count of this blip.
     * 
     * @return the reference count
     */
    public int getRef() {
        return field_6_ref;
    }

    /**
     * The reference count of this blip.
     * 
     * @param ref the reference count
     */
    public void setRef(int ref) {
        field_6_ref = ref;
    }

    /**
     * File offset in the delay stream.
     * 
     * @return the file offset
     */
    public int getOffset() {
        return field_7_offset;
    }

    /**
     * File offset in the delay stream.
     * 
     * @param offset the file offset
     */
    public void setOffset(int offset) {
        field_7_offset = offset;
    }

    /**
     * Defines the way this blip is used.
     * 
     * @return the blip usage
     */
    public byte getUsage() {
        return field_8_usage;
    }

    /**
     * Defines the way this blip is used.
     * 
     * @param usage the blip usae
     */
    public void setUsage(byte usage) {
        field_8_usage = usage;
    }

    /**
     * The length in characters of the blip name.
     * 
     * @return the blip name length
     */
    public byte getName() {
        return field_9_name;
    }

    /**
     * The length in characters of the blip name.
     * 
     * @param name the blip name length
     */
    public void setName(byte name) {
        field_9_name = name;
    }

    public byte getUnused2() {
        return field_10_unused2;
    }

    public void setUnused2(byte unused2) {
        field_10_unused2 = unused2;
    }

    public byte getUnused3() {
        return field_11_unused3;
    }

    public void setUnused3(byte unused3) {
        field_11_unused3 = unused3;
    }

    public EscherBlipRecord getBlipRecord() {
        return field_12_blipRecord;
    }

    public void setBlipRecord(EscherBlipRecord blipRecord) {
        field_12_blipRecord = blipRecord;
    }

    /**
     * Any remaining data in this record.
     * 
     * @return the remaining bytes
     */
    public byte[] getRemainingData() {
        return _remainingData;
    }

    /**
     * Any remaining data in this record.
     * 
     * @param remainingData the remaining bytes
     */
    public void setRemainingData(byte[] remainingData) {
        _remainingData = (remainingData == null) ? new byte[0] : remainingData.clone();
    }

    /**
     * Retrieve the string representation given a blip id.
     * 
     * @param b the blip type byte-encoded
     * 
     * @return the blip type as string
     */
    public static String getBlipType(byte b) {
        switch (b) {
            case BT_ERROR:   return " ERROR";
            case BT_UNKNOWN: return " UNKNOWN";
            case BT_EMF:     return " EMF";
            case BT_WMF:     return " WMF";
            case BT_PICT:    return " PICT";
            case BT_JPEG:    return " JPEG";
            case BT_PNG:     return " PNG";
            case BT_DIB:     return " DIB";
        }
        if ( b < 32 ) {
            return " NotKnown";
        }
        return " Client";
    }

    @Override
    protected Object[][] getAttributeMap() {
        return new Object[][] {
            { "BlipTypeWin32", field_1_blipTypeWin32 },
            { "BlipTypeMacOS", field_2_blipTypeMacOS },
            { "SUID", field_3_uid },
            { "Tag", field_4_tag },
            { "Size", field_5_size },
            { "Ref", field_6_ref },
            { "Offset", field_7_offset },
            { "Usage", field_8_usage },
            { "Name", field_9_name },
            { "Unused2", field_10_unused2 },
            { "Unused3", field_11_unused3 },
            { "Blip Record", field_12_blipRecord },
            { "Extra Data", _remainingData }
        };
    }
}
