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
 * The series chart group index record stores the index to the CHARTFORMAT record (0 based).
 */
public final class SeriesChartGroupIndexRecord extends StandardRecord {
    public final static short      sid                             = 0x1045;
    private  short      field_1_chartGroupIndex;


    public SeriesChartGroupIndexRecord()
    {

    }

    public SeriesChartGroupIndexRecord(RecordInputStream in)
    {
        field_1_chartGroupIndex        = in.readShort();
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[SERTOCRT]\n");
        buffer.append("    .chartGroupIndex      = ")
            .append("0x").append(HexDump.toHex(  getChartGroupIndex ()))
            .append(" (").append( getChartGroupIndex() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 

        buffer.append("[/SERTOCRT]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_chartGroupIndex);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        SeriesChartGroupIndexRecord rec = new SeriesChartGroupIndexRecord();
    
        rec.field_1_chartGroupIndex = field_1_chartGroupIndex;
        return rec;
    }




    /**
     * Get the chart group index field for the SeriesChartGroupIndex record.
     */
    public short getChartGroupIndex()
    {
        return field_1_chartGroupIndex;
    }

    /**
     * Set the chart group index field for the SeriesChartGroupIndex record.
     */
    public void setChartGroupIndex(short field_1_chartGroupIndex)
    {
        this.field_1_chartGroupIndex = field_1_chartGroupIndex;
    }
}
