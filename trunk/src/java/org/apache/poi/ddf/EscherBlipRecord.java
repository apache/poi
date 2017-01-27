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
import org.apache.poi.util.HexDump;

public class EscherBlipRecord extends EscherRecord {
    public static final short  RECORD_ID_START    = (short) 0xF018;
    public static final short  RECORD_ID_END      = (short) 0xF117;
    public static final String RECORD_DESCRIPTION = "msofbtBlip";

    private static final int   HEADER_SIZE               = 8;

    private byte[] field_pictureData;

    public EscherBlipRecord() {
    }

    @Override
    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesAfterHeader = readHeader( data, offset );
        int pos              = offset + HEADER_SIZE;

        field_pictureData = new byte[bytesAfterHeader];
        System.arraycopy(data, pos, field_pictureData, 0, bytesAfterHeader);

        return bytesAfterHeader + 8;
    }

    @Override
    public int serialize(int offset, byte[] data, EscherSerializationListener listener) {
        listener.beforeRecordSerialize(offset, getRecordId(), this);

        LittleEndian.putShort( data, offset, getOptions() );
        LittleEndian.putShort( data, offset + 2, getRecordId() );

        System.arraycopy( field_pictureData, 0, data, offset + 4, field_pictureData.length );

        listener.afterRecordSerialize(offset + 4 + field_pictureData.length, getRecordId(), field_pictureData.length + 4, this);
        return field_pictureData.length + 4;
    }

    @Override
    public int getRecordSize() {
        return field_pictureData.length + HEADER_SIZE;
    }

    @Override
    public String getRecordName() {
        return "Blip";
    }

    /**
     * Gets the picture data bytes
     *
     * @return the picture data
     */
    public byte[] getPicturedata() {
        return field_pictureData;
    }

    /**
     * Sets the picture data bytes
     *
     * @param pictureData the picture data
     */
    public void setPictureData(byte[] pictureData) {
        setPictureData(pictureData, 0, (pictureData == null ? 0 : pictureData.length));
    }

    /**
     * Sets the picture data bytes
     *
     * @param pictureData the picture data
     * @param offset the offset into the picture data
     * @param length the amount of bytes to be used
     */
    public void setPictureData(byte[] pictureData, int offset, int length) {
        if (pictureData == null || offset < 0 || length < 0 || pictureData.length < offset+length) {
            throw new IllegalArgumentException("picture data can't be null");
        }
        field_pictureData = new byte[length];
        System.arraycopy(pictureData, offset, field_pictureData, 0, length);
    }

    @Override
    public String toString() {
        String extraData = HexDump.toHex(field_pictureData, 32);
        return getClass().getName() + ":" + '\n' +
                "  RecordId: 0x" + HexDump.toHex( getRecordId() ) + '\n' +
                "  Version: 0x" + HexDump.toHex( getVersion() ) + '\n' +
                "  Instance: 0x" + HexDump.toHex( getInstance() ) + '\n' +
                "  Extra Data:" + '\n' + extraData;
    }

    @Override
    public String toXml(String tab) {
        String extraData = HexDump.toHex(field_pictureData, 32);
        StringBuilder builder = new StringBuilder();
        builder.append(tab).append(formatXmlRecordHeader(getClass().getSimpleName(), HexDump.toHex(getRecordId()), HexDump.toHex(getVersion()), HexDump.toHex(getInstance())))
                .append(tab).append("\t").append("<ExtraData>").append(extraData).append("</ExtraData>\n");
        builder.append(tab).append("</").append(getClass().getSimpleName()).append(">\n");
        return builder.toString();
    }
}
