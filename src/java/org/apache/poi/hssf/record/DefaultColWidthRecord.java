
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;

/**
 * Title:        Default Column Width Record<P>
 * Description:  Specifies the default width for columns that have no specific
 *               width set.<P>
 * REFERENCE:  PG 302 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */

public class DefaultColWidthRecord
    extends Record
{
    public final static short sid = 0x55;
    private short             field_1_col_width;

    public DefaultColWidthRecord()
    {
    }

    /**
     * Constructs a DefaultColumnWidth record and sets its fields appropriately.
     *
     * @param id     id must be 0x55 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public DefaultColWidthRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a DefaultColumnWidth record and sets its fields appropriately.
     *
     * @param id     id must be 0x55 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public DefaultColWidthRecord(short id, short size, byte [] data,
                                 int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A DefaultColWidth RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_col_width = LittleEndian.getShort(data, 0 + offset);
    }

    /**
     * set the default column width
     * @param height defaultwidth for columns
     */

    public void setColWidth(short height)
    {
        field_1_col_width = height;
    }

    /**
     * get the default column width
     * @return defaultwidth for columns
     */

    public short getColWidth()
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

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) 0x2);
        LittleEndian.putShort(data, 4 + offset, getColWidth());
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 6;
    }

    public short getSid()
    {
        return this.sid;
    }

    public Object clone() {
      DefaultColWidthRecord rec = new DefaultColWidthRecord();
      rec.field_1_col_width = field_1_col_width;
      return rec;
    }
}
