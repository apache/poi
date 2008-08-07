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

package org.apache.poi.hssf.record;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;

/**
 * Class ChartFormatRecord
 *
 *
 * @author Glen Stampoultzis (glens at apache.org)
 * @version %I%, %G%
 */
public final class ChartFormatRecord extends Record {
    public static final short sid = 0x1014;

    private static final BitField varyDisplayPattern = BitFieldFactory.getInstance(0x01);

    // ignored?
    private int               field1_x_position;   // lower left
    private int               field2_y_position;   // lower left
    private int               field3_width;
    private int               field4_height;
    private short             field5_grbit;

    public ChartFormatRecord()
    {
    }

    /**
     * Constructs a ChartFormatRecord record and sets its fields appropriately.
     * @param in the RecordInputstream to read the record from
     */

    public ChartFormatRecord(RecordInputStream in)
    {
        super(in);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A CHARTFORMAT RECORD");
        }
    }

    protected void fillFields(RecordInputStream in)
    {
        field1_x_position = in.readInt();
        field2_y_position = in.readInt();
        field3_width      = in.readInt();
        field4_height     = in.readInt();
        field5_grbit      = in.readShort();
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[CHARTFORMAT]\n");
        buffer.append("    .xPosition       = ").append(getXPosition())
            .append("\n");
        buffer.append("    .yPosition       = ").append(getYPosition())
            .append("\n");
        buffer.append("    .width           = ").append(getWidth())
            .append("\n");
        buffer.append("    .height          = ").append(getHeight())
            .append("\n");
        buffer.append("    .grBit           = ")
            .append(Integer.toHexString(field5_grbit)).append("\n");
        buffer.append("[/CHARTFORMAT]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              (( short ) 22));   // 22 byte length
        LittleEndian.putInt(data, 4 + offset, getXPosition());
        LittleEndian.putInt(data, 8 + offset, getYPosition());
        LittleEndian.putInt(data, 12 + offset, getWidth());
        LittleEndian.putInt(data, 16 + offset, getHeight());
        LittleEndian.putShort(data, 20 + offset, field5_grbit);
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 22;
    }

    public short getSid()
    {
        return sid;
    }

    public int getXPosition()
    {
        return field1_x_position;
    }

    public void setXPosition(int xPosition)
    {
        this.field1_x_position = xPosition;
    }

    public int getYPosition()
    {
        return field2_y_position;
    }

    public void setYPosition(int yPosition)
    {
        this.field2_y_position = yPosition;
    }

    public int getWidth()
    {
        return field3_width;
    }

    public void setWidth(int width)
    {
        this.field3_width = width;
    }

    public int getHeight()
    {
        return field4_height;
    }

    public void setHeight(int height)
    {
        this.field4_height = height;
    }

    public boolean getVaryDisplayPattern()
    {
        return varyDisplayPattern.isSet(field5_grbit);
    }

    public void setVaryDisplayPattern(boolean value)
    {
        field5_grbit = varyDisplayPattern.setShortBoolean(field5_grbit,
                value);
    }
}
