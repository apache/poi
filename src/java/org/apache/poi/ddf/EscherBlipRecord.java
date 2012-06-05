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

/**
 * @author Glen Stampoultzis
 */
public class EscherBlipRecord extends EscherRecord { // TODO - instantiable superclass
    public static final short  RECORD_ID_START    = (short) 0xF018;
    public static final short  RECORD_ID_END      = (short) 0xF117;
    public static final String RECORD_DESCRIPTION = "msofbtBlip";

    private static final int   HEADER_SIZE               = 8;

    protected              byte[] field_pictureData;

    public EscherBlipRecord() {
    }

    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesAfterHeader = readHeader( data, offset );
        int pos              = offset + HEADER_SIZE;

        field_pictureData = new byte[bytesAfterHeader];
        System.arraycopy(data, pos, field_pictureData, 0, bytesAfterHeader);

        return bytesAfterHeader + 8;
    }

    public int serialize(int offset, byte[] data, EscherSerializationListener listener) {
        listener.beforeRecordSerialize(offset, getRecordId(), this);

        LittleEndian.putShort( data, offset, getOptions() );
        LittleEndian.putShort( data, offset + 2, getRecordId() );

        System.arraycopy( field_pictureData, 0, data, offset + 4, field_pictureData.length );

        listener.afterRecordSerialize(offset + 4 + field_pictureData.length, getRecordId(), field_pictureData.length + 4, this);
        return field_pictureData.length + 4;
    }

    public int getRecordSize() {
        return field_pictureData.length + HEADER_SIZE;
    }

    public String getRecordName() {
        return "Blip";
    }

    public byte[] getPicturedata() {
        return field_pictureData;
    }

    public void setPictureData(byte[] pictureData) {
        field_pictureData = pictureData;
    }

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
