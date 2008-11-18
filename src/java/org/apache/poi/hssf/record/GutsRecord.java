
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

import org.apache.poi.util.LittleEndianOutput;

/**
 * Title:        Guts Record <P>
 * Description:  Row/column gutter sizes <P>
 * REFERENCE:  PG 320 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */

public final class GutsRecord
    extends StandardRecord
{
    public final static short sid = 0x80;
    private short             field_1_left_row_gutter;   // size of the row gutter to the left of the rows
    private short             field_2_top_col_gutter;    // size of the column gutter above the columns
    private short             field_3_row_level_max;     // maximum outline level for row gutters
    private short             field_4_col_level_max;     // maximum outline level for column gutters

    public GutsRecord()
    {
    }

    public GutsRecord(RecordInputStream in)
    {
        field_1_left_row_gutter = in.readShort();
        field_2_top_col_gutter  = in.readShort();
        field_3_row_level_max   = in.readShort();
        field_4_col_level_max   = in.readShort();
    }

    /**
     * set the size of the gutter that appears at the left of the rows
     *
     * @param gut  gutter size in screen units
     */

    public void setLeftRowGutter(short gut)
    {
        field_1_left_row_gutter = gut;
    }

    /**
     * set the size of the gutter that appears at the above the columns
     *
     * @param gut  gutter size in screen units
     */

    public void setTopColGutter(short gut)
    {
        field_2_top_col_gutter = gut;
    }

    /**
     * set the maximum outline level for the row gutter.
     *
     * @param max  maximum outline level
     */

    public void setRowLevelMax(short max)
    {
        field_3_row_level_max = max;
    }

    /**
     * set the maximum outline level for the col gutter.
     *
     * @param max  maximum outline level
     */

    public void setColLevelMax(short max)
    {
        field_4_col_level_max = max;
    }

    /**
     * get the size of the gutter that appears at the left of the rows
     *
     * @return gutter size in screen units
     */

    public short getLeftRowGutter()
    {
        return field_1_left_row_gutter;
    }

    /**
     * get the size of the gutter that appears at the above the columns
     *
     * @return gutter size in screen units
     */

    public short getTopColGutter()
    {
        return field_2_top_col_gutter;
    }

    /**
     * get the maximum outline level for the row gutter.
     *
     * @return maximum outline level
     */

    public short getRowLevelMax()
    {
        return field_3_row_level_max;
    }

    /**
     * get the maximum outline level for the col gutter.
     *
     * @return maximum outline level
     */

    public short getColLevelMax()
    {
        return field_4_col_level_max;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[GUTS]\n");
        buffer.append("    .leftgutter     = ")
            .append(Integer.toHexString(getLeftRowGutter())).append("\n");
        buffer.append("    .topgutter      = ")
            .append(Integer.toHexString(getTopColGutter())).append("\n");
        buffer.append("    .rowlevelmax    = ")
            .append(Integer.toHexString(getRowLevelMax())).append("\n");
        buffer.append("    .collevelmax    = ")
            .append(Integer.toHexString(getColLevelMax())).append("\n");
        buffer.append("[/GUTS]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getLeftRowGutter());
        out.writeShort(getTopColGutter());
        out.writeShort(getRowLevelMax());
        out.writeShort(getColLevelMax());
    }

    protected int getDataSize() {
        return 8;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
      GutsRecord rec = new GutsRecord();
      rec.field_1_left_row_gutter = field_1_left_row_gutter;
      rec.field_2_top_col_gutter = field_2_top_col_gutter;
      rec.field_3_row_level_max = field_3_row_level_max;
      rec.field_4_col_level_max = field_4_col_level_max;
      return rec;
    }
}
