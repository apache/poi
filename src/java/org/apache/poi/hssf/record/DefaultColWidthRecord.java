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
 * Title:        Default Column Width Record (0x0055) <P>
 * Description:  Specifies the default width for columns that have no specific
 *               width set.<P>
 * REFERENCE:  PG 302 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */
public final class DefaultColWidthRecord extends StandardRecord implements Cloneable {
    public final static short sid = 0x0055;
    private int             field_1_col_width;

    /**
     *  The default column width is 8 characters
     */
    public final static int DEFAULT_COLUMN_WIDTH = 0x0008;

    public DefaultColWidthRecord()
    {
        field_1_col_width = DEFAULT_COLUMN_WIDTH;
    }

    public DefaultColWidthRecord(RecordInputStream in)
    {
        field_1_col_width = in.readUShort();
    }

    /**
     * set the default column width
     * @param width defaultwidth for columns
     */

    public void setColWidth(int width)
    {
        field_1_col_width = width;
    }

    /**
     * get the default column width
     * @return defaultwidth for columns
     */

    public int getColWidth()
    {
        return field_1_col_width;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[DEFAULTCOLWIDTH]\n");
        buffer.append("    .colwidth      = ")
            .append(Integer.toHexString(getColWidth())).append("\n");
        buffer.append("[/DEFAULTCOLWIDTH]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getColWidth());
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public DefaultColWidthRecord clone() {
      DefaultColWidthRecord rec = new DefaultColWidthRecord();
      rec.field_1_col_width = field_1_col_width;
      return rec;
    }
}
