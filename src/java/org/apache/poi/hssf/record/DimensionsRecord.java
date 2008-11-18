
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
 * Title:        Dimensions Record<P>
 * Description:  provides the minumum and maximum bounds
 *               of a sheet.<P>
 * REFERENCE:  PG 303 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */

public final class DimensionsRecord
    extends StandardRecord
{
    public final static short sid = 0x200;
    private int               field_1_first_row;
    private int               field_2_last_row;   // plus 1
    private short             field_3_first_col;
    private short             field_4_last_col;
    private short             field_5_zero;       // must be 0 (reserved)

    public DimensionsRecord()
    {
    }

    public DimensionsRecord(RecordInputStream in)
    {
        field_1_first_row = in.readInt();
        field_2_last_row  = in.readInt();
        field_3_first_col = in.readShort();
        field_4_last_col  = in.readShort();
        field_5_zero      = in.readShort();
    }

    /**
     * set the first row number for the sheet
     * @param row - first row on the sheet
     */

    public void setFirstRow(int row)
    {
        field_1_first_row = row;
    }

    /**
     * set the last row number for the sheet
     * @param row - last row on the sheet
     */

    public void setLastRow(int row)
    {
        field_2_last_row = row;
    }

    /**
     * set the first column number for the sheet
     * @param col  first column on the sheet
     */

    public void setFirstCol(short col)
    {
        field_3_first_col = col;
    }

    /**
     * set the last col number for the sheet
     * @param col  last column on the sheet
     */

    public void setLastCol(short col)
    {
        field_4_last_col = col;
    }

    /**
     * get the first row number for the sheet
     * @return row - first row on the sheet
     */

    public int getFirstRow()
    {
        return field_1_first_row;
    }

    /**
     * get the last row number for the sheet
     * @return row - last row on the sheet
     */

    public int getLastRow()
    {
        return field_2_last_row;
    }

    /**
     * get the first column number for the sheet
     * @return column - first column on the sheet
     */

    public short getFirstCol()
    {
        return field_3_first_col;
    }

    /**
     * get the last col number for the sheet
     * @return column - last column on the sheet
     */

    public short getLastCol()
    {
        return field_4_last_col;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[DIMENSIONS]\n");
        buffer.append("    .firstrow       = ")
            .append(Integer.toHexString(getFirstRow())).append("\n");
        buffer.append("    .lastrow        = ")
            .append(Integer.toHexString(getLastRow())).append("\n");
        buffer.append("    .firstcol       = ")
            .append(Integer.toHexString(getFirstCol())).append("\n");
        buffer.append("    .lastcol        = ")
            .append(Integer.toHexString(getLastCol())).append("\n");
        buffer.append("    .zero           = ")
            .append(Integer.toHexString(field_5_zero)).append("\n");
        buffer.append("[/DIMENSIONS]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeInt(getFirstRow());
        out.writeInt(getLastRow());
        out.writeShort(getFirstCol());
        out.writeShort(getLastCol());
        out.writeShort(( short ) 0);
    }

    protected int getDataSize() {
        return 14;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
      DimensionsRecord rec = new DimensionsRecord();
      rec.field_1_first_row = field_1_first_row;
      rec.field_2_last_row = field_2_last_row;
      rec.field_3_first_col = field_3_first_col;
      rec.field_4_last_col = field_4_last_col;
      rec.field_5_zero = field_5_zero;
      return rec;
    }
}
