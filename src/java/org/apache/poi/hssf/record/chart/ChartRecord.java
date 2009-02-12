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
import org.apache.poi.util.LittleEndianOutput;

/**
 * CHART (0x1002) <p/>
 * 
 * The chart record is used to define the location and size of a chart.<p/>
 * 
 * Chart related records don't seem to be covered in either the 
 * <A HREF="http://sc.openoffice.org/excelfileformat.pdf">OOO</A> 
 *  or the  
 * <A HREF="http://download.microsoft.com/download/0/B/E/0BE8BDD7-E5E8-422A-ABFD-4342ED7AD886/Excel97-2007BinaryFileFormat(xls)Specification.pdf">MS</A> 
 *  documentation. 
 * 
 * The book "Microsoft Excel 97 Developer's Kit" ISBN: (1-57231-498-2) seems to have an entire
 * chapter (10) devoted to Chart records.  One  
 * <A HREF="http://ooxmlisdefectivebydesign.blogspot.com/2008/03/bad-surprise-in-microsoft-office-binary.html">blog</A>
 *  suggests that some documentation for these records is available in "MSDN Library, Feb 1998",
 * but no later.
 *  
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class ChartRecord extends StandardRecord {
    public final static short sid = 0x1002;
    private int field_1_x;
    private int field_2_y;
    private int field_3_width;
    private int field_4_height;


    public ChartRecord() {
        // fields uninitialised
    }

    public ChartRecord(RecordInputStream in) {
        field_1_x = in.readInt();
        field_2_y = in.readInt();
        field_3_width = in.readInt();
        field_4_height = in.readInt();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("[CHART]\n");
        sb.append("    .x     = ").append(getX()).append('\n');
        sb.append("    .y     = ").append(getY()).append('\n');
        sb.append("    .width = ").append(getWidth()).append('\n');
        sb.append("    .height= ").append(getHeight()).append('\n');
        sb.append("[/CHART]\n");
        return sb.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeInt(field_1_x);
        out.writeInt(field_2_y);
        out.writeInt(field_3_width);
        out.writeInt(field_4_height);
    }

    protected int getDataSize() {
        return 4 + 4 + 4 + 4;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        ChartRecord rec = new ChartRecord();
    
        rec.field_1_x = field_1_x;
        rec.field_2_y = field_2_y;
        rec.field_3_width = field_3_width;
        rec.field_4_height = field_4_height;
        return rec;
    }




    /**
     * Get the x field for the Chart record.
     */
    public int getX() {
        return field_1_x;
    }

    /**
     * Set the x field for the Chart record.
     */
    public void setX(int x) {
        field_1_x = x;
    }

    /**
     * Get the y field for the Chart record.
     */
    public int getY() {
        return field_2_y;
    }

    /**
     * Set the y field for the Chart record.
     */
    public void setY(int y) {
        field_2_y = y;
    }

    /**
     * Get the width field for the Chart record.
     */
    public int getWidth() {
        return field_3_width;
    }

    /**
     * Set the width field for the Chart record.
     */
    public void setWidth(int width) {
        field_3_width = width;
    }

    /**
     * Get the height field for the Chart record.
     */
    public int getHeight() {
        return field_4_height;
    }

    /**
     * Set the height field for the Chart record.
     */
    public void setHeight(int height) {
        field_4_height = height;
    }
}
