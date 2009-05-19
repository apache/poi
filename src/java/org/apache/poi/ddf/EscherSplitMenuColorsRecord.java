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

/**
 * A list of the most recently used colours for the drawings contained in
 * this document.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class EscherSplitMenuColorsRecord
    extends EscherRecord
{
    public static final short RECORD_ID = (short) 0xF11E;
    public static final String RECORD_DESCRIPTION = "MsofbtSplitMenuColors";

    private int field_1_color1;
    private int field_2_color2;
    private int field_3_color3;
    private int field_4_color4;

    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesRemaining = readHeader( data, offset );
        int pos            = offset + 8;
        int size           = 0;
        field_1_color1 =  LittleEndian.getInt( data, pos + size );size+=4;
        field_2_color2 =  LittleEndian.getInt( data, pos + size );size+=4;
        field_3_color3 =  LittleEndian.getInt( data, pos + size );size+=4;
        field_4_color4 =  LittleEndian.getInt( data, pos + size );size+=4;
        bytesRemaining -= size;
        if (bytesRemaining != 0)
            throw new RecordFormatException("Expecting no remaining data but got " + bytesRemaining + " byte(s).");
        return 8 + size + bytesRemaining;
    }

    public int serialize( int offset, byte[] data, EscherSerializationListener listener )
    {
//        int field_2_numIdClusters = field_5_fileIdClusters.length + 1;
        listener.beforeRecordSerialize( offset, getRecordId(), this );

        int pos = offset;
        LittleEndian.putShort( data, pos, getOptions() );     pos += 2;
        LittleEndian.putShort( data, pos, getRecordId() );    pos += 2;
        int remainingBytes =  getRecordSize() - 8;

        LittleEndian.putInt( data, pos, remainingBytes );          pos += 4;
        LittleEndian.putInt( data, pos, field_1_color1 );          pos += 4;
        LittleEndian.putInt( data, pos, field_2_color2 );          pos += 4;
        LittleEndian.putInt( data, pos, field_3_color3 );          pos += 4;
        LittleEndian.putInt( data, pos, field_4_color4 );          pos += 4;
        listener.afterRecordSerialize( pos, getRecordId(), pos - offset, this );
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 8 + 4 * 4;
    }

    public short getRecordId() {
        return RECORD_ID;
    }

    public String getRecordName() {
        return "SplitMenuColors";
    }

    /**
     * @return  a string representation of this record.
     */
    public String toString() {
        return getClass().getName() + ":" + '\n' +
                "  RecordId: 0x" + HexDump.toHex(RECORD_ID) + '\n' +
                "  Options: 0x" + HexDump.toHex(getOptions()) + '\n' +
                "  Color1: 0x" + HexDump.toHex(field_1_color1) + '\n' +
                "  Color2: 0x" + HexDump.toHex(field_2_color2) + '\n' +
                "  Color3: 0x" + HexDump.toHex(field_3_color3) + '\n' +
                "  Color4: 0x" + HexDump.toHex(field_4_color4) + '\n' +
                "";
    }

    public int getColor1()
    {
        return field_1_color1;
    }

    public void setColor1( int field_1_color1 )
    {
        this.field_1_color1 = field_1_color1;
    }

    public int getColor2()
    {
        return field_2_color2;
    }

    public void setColor2( int field_2_color2 )
    {
        this.field_2_color2 = field_2_color2;
    }

    public int getColor3()
    {
        return field_3_color3;
    }

    public void setColor3( int field_3_color3 )
    {
        this.field_3_color3 = field_3_color3;
    }

    public int getColor4()
    {
        return field_4_color4;
    }

    public void setColor4( int field_4_color4 )
    {
        this.field_4_color4 = field_4_color4;
    }

}
