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

/**
 * The BSE record is related closely to the <code>EscherBlipRecord</code> and stores
 * extra information about the blip.  A blip record is actually stored inside
 * the BSE record even though the BSE record isn't actually a container record.
 *
 * @author Glen Stampoultzis
 * @see EscherBlipRecord
 */
public final class EscherBSERecord extends EscherRecord {
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
    private EscherBlipRecord field_12_blipRecord;

    private byte[] _remainingData;

    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
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

        int bytesRead = 0;
        if (bytesRemaining > 0) {
            // Some older escher formats skip this last record
            field_12_blipRecord = (EscherBlipRecord) recordFactory.createRecord( data, pos + 36 );
            bytesRead = field_12_blipRecord.fillFields( data, pos + 36, recordFactory );
        }
        pos += 36 + bytesRead;
        bytesRemaining -= bytesRead;

        _remainingData = new byte[bytesRemaining];
        System.arraycopy( data, pos, _remainingData, 0, bytesRemaining );
        return bytesRemaining + 8 + 36 + (field_12_blipRecord == null ? 0 : field_12_blipRecord.getRecordSize()) ;

    }

    public int serialize(int offset, byte[] data, EscherSerializationListener listener) {
        listener.beforeRecordSerialize( offset, getRecordId(), this );

        if (_remainingData == null)
            _remainingData = new byte[0];

        LittleEndian.putShort( data, offset, getOptions() );
        LittleEndian.putShort( data, offset + 2, getRecordId() );
        if (_remainingData == null) _remainingData = new byte[0];
        int blipSize = field_12_blipRecord == null ? 0 : field_12_blipRecord.getRecordSize();
        int remainingBytes = _remainingData.length + 36 + blipSize;
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
        int bytesWritten = 0;
        if (field_12_blipRecord != null)
        {
            bytesWritten = field_12_blipRecord.serialize( offset + 44, data, new NullEscherSerializationListener() );
        }
        if (_remainingData == null)
            _remainingData = new byte[0];
        System.arraycopy( _remainingData, 0, data, offset + 44 + bytesWritten, _remainingData.length );
        int pos = offset + 8 + 36 + _remainingData.length + bytesWritten;

        listener.afterRecordSerialize(pos, getRecordId(), pos - offset, this);
        return pos - offset;
    }

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

    public String getRecordName() {
        return "BSE";
    }

    /**
     * The expected blip type under windows (failure to match this blip type will result in
     * Excel converting to this format).
     */
    public byte getBlipTypeWin32() {
        return field_1_blipTypeWin32;
    }

    /**
     * Set the expected win32 blip type
     */
    public void setBlipTypeWin32(byte blipTypeWin32) {
        field_1_blipTypeWin32 = blipTypeWin32;
    }

    /**
     * The expected blip type under MacOS (failure to match this blip type will result in
     * Excel converting to this format).
     */
    public byte getBlipTypeMacOS() {
        return field_2_blipTypeMacOS;
    }

    /**
     * Set the expected MacOS blip type
     */
    public void setBlipTypeMacOS(byte blipTypeMacOS) {
        field_2_blipTypeMacOS = blipTypeMacOS;
    }

    /**
     * 16 byte MD4 checksum.
     */
    public byte[] getUid() {
        return field_3_uid;
    }

    /**
     * 16 byte MD4 checksum.
     */
    public void setUid(byte[] uid) {
        field_3_uid = uid;
    }

    /**
     * unused
     */
    public short getTag() {
        return field_4_tag;
    }

    /**
     * unused
     */
    public void setTag(short tag) {
        field_4_tag = tag;
    }

    /**
     * Blip size in stream.
     */
    public int getSize() {
        return field_5_size;
    }

    /**
     * Blip size in stream.
     */
    public void setSize(int size) {
        field_5_size = size;
    }

    /**
     * The reference count of this blip.
     */
    public int getRef() {
        return field_6_ref;
    }

    /**
     * The reference count of this blip.
     */
    public void setRef(int ref) {
        field_6_ref = ref;
    }

    /**
     * File offset in the delay stream.
     */
    public int getOffset() {
        return field_7_offset;
    }

    /**
     * File offset in the delay stream.
     */
    public void setOffset(int offset) {
        field_7_offset = offset;
    }

    /**
     * Defines the way this blip is used.
     */
    public byte getUsage() {
        return field_8_usage;
    }

    /**
     * Defines the way this blip is used.
     */
    public void setUsage(byte usage) {
        field_8_usage = usage;
    }

    /**
     * The length in characters of the blip name.
     */
    public byte getName() {
        return field_9_name;
    }

    /**
     * The length in characters of the blip name.
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
     */
    public byte[] getRemainingData() {
        return _remainingData;
    }

    /**
     * Any remaining data in this record.
     */
    public void setRemainingData(byte[] remainingData) {
        _remainingData = remainingData;
    }

    public String toString() {
        String extraData = _remainingData == null ? null : HexDump.toHex(_remainingData, 32);
        return getClass().getName() + ":" + '\n' +
                "  RecordId: 0x" + HexDump.toHex( RECORD_ID ) + '\n' +
                "  Options: 0x" + HexDump.toHex( getOptions() ) + '\n' +
                "  BlipTypeWin32: " + field_1_blipTypeWin32 + '\n' +
                "  BlipTypeMacOS: " + field_2_blipTypeMacOS + '\n' +
                "  SUID: " + (field_3_uid == null ? "" : HexDump.toHex(field_3_uid)) + '\n' +
                "  Tag: " + field_4_tag + '\n' +
                "  Size: " + field_5_size + '\n' +
                "  Ref: " + field_6_ref + '\n' +
                "  Offset: " + field_7_offset + '\n' +
                "  Usage: " + field_8_usage + '\n' +
                "  Name: " + field_9_name + '\n' +
                "  Unused2: " + field_10_unused2 + '\n' +
                "  Unused3: " + field_11_unused3 + '\n' +
                "  blipRecord: " + field_12_blipRecord + '\n' +
                "  Extra Data:" + '\n' + extraData;
    }

    /**
     * Retrieve the string representation given a blip id.
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
}
