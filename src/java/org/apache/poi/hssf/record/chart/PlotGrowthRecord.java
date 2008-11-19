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
 * The plot growth record specifies the scaling factors used when a font is scaled.<p/>
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class PlotGrowthRecord extends StandardRecord {
    public final static short      sid                             = 0x1064;
    private  int        field_1_horizontalScale;
    private  int        field_2_verticalScale;


    public PlotGrowthRecord()
    {

    }

    public PlotGrowthRecord(RecordInputStream in)
    {
        field_1_horizontalScale        = in.readInt();
        field_2_verticalScale          = in.readInt();

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[PLOTGROWTH]\n");
        buffer.append("    .horizontalScale      = ")
            .append("0x").append(HexDump.toHex(  getHorizontalScale ()))
            .append(" (").append( getHorizontalScale() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .verticalScale        = ")
            .append("0x").append(HexDump.toHex(  getVerticalScale ()))
            .append(" (").append( getVerticalScale() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 

        buffer.append("[/PLOTGROWTH]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeInt(field_1_horizontalScale);
        out.writeInt(field_2_verticalScale);
    }

    protected int getDataSize() {
        return 4 + 4;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        PlotGrowthRecord rec = new PlotGrowthRecord();
    
        rec.field_1_horizontalScale = field_1_horizontalScale;
        rec.field_2_verticalScale = field_2_verticalScale;
        return rec;
    }




    /**
     * Get the horizontalScale field for the PlotGrowth record.
     */
    public int getHorizontalScale()
    {
        return field_1_horizontalScale;
    }

    /**
     * Set the horizontalScale field for the PlotGrowth record.
     */
    public void setHorizontalScale(int field_1_horizontalScale)
    {
        this.field_1_horizontalScale = field_1_horizontalScale;
    }

    /**
     * Get the verticalScale field for the PlotGrowth record.
     */
    public int getVerticalScale()
    {
        return field_2_verticalScale;
    }

    /**
     * Set the verticalScale field for the PlotGrowth record.
     */
    public void setVerticalScale(int field_2_verticalScale)
    {
        this.field_2_verticalScale = field_2_verticalScale;
    }
}
