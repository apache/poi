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

package org.apache.poi.hssf.record.chart;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianOutput;

/**
 * The axis size and location<p>
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class AxisParentRecord extends StandardRecord implements Cloneable {
    public final static short      sid                             = 0x1041;
    private  short      field_1_axisType;
    public final static short       AXIS_TYPE_MAIN                 = 0;
    public final static short       AXIS_TYPE_SECONDARY            = 1;
    private  int        field_2_x;
    private  int        field_3_y;
    private  int        field_4_width;
    private  int        field_5_height;


    public AxisParentRecord()
    {

    }

    public AxisParentRecord(RecordInputStream in)
    {
        field_1_axisType               = in.readShort();
        field_2_x                      = in.readInt();
        field_3_y                      = in.readInt();
        field_4_width                  = in.readInt();
        field_5_height                 = in.readInt();
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[AXISPARENT]\n");
        buffer.append("    .axisType             = ")
            .append("0x").append(HexDump.toHex(  getAxisType ()))
            .append(" (").append( getAxisType() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .x                    = ")
            .append("0x").append(HexDump.toHex(  getX ()))
            .append(" (").append( getX() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .y                    = ")
            .append("0x").append(HexDump.toHex(  getY ()))
            .append(" (").append( getY() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .width                = ")
            .append("0x").append(HexDump.toHex(  getWidth ()))
            .append(" (").append( getWidth() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .height               = ")
            .append("0x").append(HexDump.toHex(  getHeight ()))
            .append(" (").append( getHeight() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 

        buffer.append("[/AXISPARENT]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_axisType);
        out.writeInt(field_2_x);
        out.writeInt(field_3_y);
        out.writeInt(field_4_width);
        out.writeInt(field_5_height);
    }

    protected int getDataSize() {
        return 2 + 4 + 4 + 4 + 4;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public AxisParentRecord clone() {
        AxisParentRecord rec = new AxisParentRecord();
    
        rec.field_1_axisType = field_1_axisType;
        rec.field_2_x = field_2_x;
        rec.field_3_y = field_3_y;
        rec.field_4_width = field_4_width;
        rec.field_5_height = field_5_height;
        return rec;
    }




    /**
     * Get the axis type field for the AxisParent record.
     *
     * @return  One of 
     *        AXIS_TYPE_MAIN
     *        AXIS_TYPE_SECONDARY
     */
    public short getAxisType()
    {
        return field_1_axisType;
    }

    /**
     * Set the axis type field for the AxisParent record.
     *
     * @param field_1_axisType
     *        One of 
     *        AXIS_TYPE_MAIN
     *        AXIS_TYPE_SECONDARY
     */
    public void setAxisType(short field_1_axisType)
    {
        this.field_1_axisType = field_1_axisType;
    }

    /**
     * Get the x field for the AxisParent record.
     */
    public int getX()
    {
        return field_2_x;
    }

    /**
     * Set the x field for the AxisParent record.
     */
    public void setX(int field_2_x)
    {
        this.field_2_x = field_2_x;
    }

    /**
     * Get the y field for the AxisParent record.
     */
    public int getY()
    {
        return field_3_y;
    }

    /**
     * Set the y field for the AxisParent record.
     */
    public void setY(int field_3_y)
    {
        this.field_3_y = field_3_y;
    }

    /**
     * Get the width field for the AxisParent record.
     */
    public int getWidth()
    {
        return field_4_width;
    }

    /**
     * Set the width field for the AxisParent record.
     */
    public void setWidth(int field_4_width)
    {
        this.field_4_width = field_4_width;
    }

    /**
     * Get the height field for the AxisParent record.
     */
    public int getHeight()
    {
        return field_5_height;
    }

    /**
     * Set the height field for the AxisParent record.
     */
    public void setHeight(int field_5_height)
    {
        this.field_5_height = field_5_height;
    }
}
