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
 * The axis line format record defines the axis type details.<p/>
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class AxisLineFormatRecord extends StandardRecord {
    public final static short      sid                             = 0x1021;
    private  short      field_1_axisType;
    public final static short       AXIS_TYPE_AXIS_LINE            = 0;
    public final static short       AXIS_TYPE_MAJOR_GRID_LINE      = 1;
    public final static short       AXIS_TYPE_MINOR_GRID_LINE      = 2;
    public final static short       AXIS_TYPE_WALLS_OR_FLOOR       = 3;


    public AxisLineFormatRecord()
    {

    }

    public AxisLineFormatRecord(RecordInputStream in)
    {
        field_1_axisType               = in.readShort();
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[AXISLINEFORMAT]\n");
        buffer.append("    .axisType             = ")
            .append("0x").append(HexDump.toHex(  getAxisType ()))
            .append(" (").append( getAxisType() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 

        buffer.append("[/AXISLINEFORMAT]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_axisType);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        AxisLineFormatRecord rec = new AxisLineFormatRecord();
    
        rec.field_1_axisType = field_1_axisType;
        return rec;
    }




    /**
     * Get the axis type field for the AxisLineFormat record.
     *
     * @return  One of 
     *        AXIS_TYPE_AXIS_LINE
     *        AXIS_TYPE_MAJOR_GRID_LINE
     *        AXIS_TYPE_MINOR_GRID_LINE
     *        AXIS_TYPE_WALLS_OR_FLOOR
     */
    public short getAxisType()
    {
        return field_1_axisType;
    }

    /**
     * Set the axis type field for the AxisLineFormat record.
     *
     * @param field_1_axisType
     *        One of 
     *        AXIS_TYPE_AXIS_LINE
     *        AXIS_TYPE_MAJOR_GRID_LINE
     *        AXIS_TYPE_MINOR_GRID_LINE
     *        AXIS_TYPE_WALLS_OR_FLOOR
     */
    public void setAxisType(short field_1_axisType)
    {
        this.field_1_axisType = field_1_axisType;
    }
}
