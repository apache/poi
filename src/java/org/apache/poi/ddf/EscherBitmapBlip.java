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

import org.apache.poi.util.LittleEndian;

public class EscherBitmapBlip extends EscherBlipRecord {
    public static final short RECORD_ID_JPEG = (short) 0xF018 + 5;
    public static final short RECORD_ID_PNG = (short) 0xF018 + 6;
    public static final short RECORD_ID_DIB = (short) 0xF018 + 7;

    private static final int HEADER_SIZE = 8;

    private final byte[] field_1_UID = new byte[16];
    private byte field_2_marker = (byte) 0xFF;

    @Override
    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesAfterHeader = readHeader( data, offset );
        int pos = offset + HEADER_SIZE;

        System.arraycopy( data, pos, field_1_UID, 0, 16 ); pos += 16;
        field_2_marker = data[pos]; pos++;

        setPictureData(data, pos, bytesAfterHeader - 17);

        return bytesAfterHeader + HEADER_SIZE;
    }

    @Override
    public int serialize( int offset, byte[] data, EscherSerializationListener listener ) {
        listener.beforeRecordSerialize(offset, getRecordId(), this);

        LittleEndian.putShort( data, offset, getOptions() );
        LittleEndian.putShort( data, offset + 2, getRecordId() );
        LittleEndian.putInt( data, offset + 4, getRecordSize() - HEADER_SIZE );
        int pos = offset + HEADER_SIZE;

        System.arraycopy( field_1_UID, 0, data, pos, 16 );
        data[pos + 16] = field_2_marker;
        byte[] pd = getPicturedata();
        System.arraycopy( pd, 0, data, pos + 17, pd.length );

        listener.afterRecordSerialize(offset + getRecordSize(), getRecordId(), getRecordSize(), this);
        return HEADER_SIZE + 16 + 1 + pd.length;
    }

    @Override
    public int getRecordSize() {
        return 8 + 16 + 1 + getPicturedata().length;
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
     * @param field_1_UID the first MD4
     */
    public void setUID( byte[] field_1_UID ) {
        if (field_1_UID == null || field_1_UID.length != 16) {
            throw new IllegalArgumentException("field_1_UID must be byte[16]");
        }
        System.arraycopy(field_1_UID, 0, this.field_1_UID , 0, 16);
    }

    /**
     * Gets an unsigned integer that specifies an application-defined internal
     * resource tag. This value MUST be 0xFF for external files.
     *
     * @return the marker
     */
    public byte getMarker() {
        return field_2_marker;
    }

    /**
     * Sets an unsigned integer that specifies an application-defined internal
     * resource tag. This value MUST be 0xFF for external files.
     *
     * @param field_2_marker the marker
     */
    public void setMarker( byte field_2_marker ) {
        this.field_2_marker = field_2_marker;
    }

    @Override
    protected Object[][] getAttributeMap() {
        return new Object[][] {
            { "Marker", field_2_marker },
            { "Extra Data", getPicturedata() }
        };
    }
}
