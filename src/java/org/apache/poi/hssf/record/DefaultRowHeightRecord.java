
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
 * Title:        Default Row Height Record
 * Description:  Row height for rows with undefined or not explicitly defined
 *               heights.
 * REFERENCE:  PG 301 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */

public class DefaultRowHeightRecord
    extends Record
{
    public final static short sid = 0x225;
    private short             field_1_option_flags;
    private short             field_2_row_height;

    public DefaultRowHeightRecord()
    {
    }

    /**
     * Constructs a DefaultRowHeight record and sets its fields appropriately.
     *
     * @param id     id must be 0x225 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public DefaultRowHeightRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a DefaultRowHeight record and sets its fields appropriately.
     *
     * @param id     id must be 0x225 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the records data
     */

    public DefaultRowHeightRecord(short id, short size, byte [] data,
                                  int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A DefaultRowHeight RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_option_flags = LittleEndian.getShort(data, 0 + offset);
        field_2_row_height   = LittleEndian.getShort(data, 2 + offset);
    }

    /**
     * set the (currently unimportant to HSSF) option flags
     * @param flags the bitmask to set
     */

    public void setOptionFlags(short flags)
    {
        field_1_option_flags = flags;
    }

    /**
     * set the default row height
     * @param height    for undefined rows/rows w/undefined height
     */

    public void setRowHeight(short height)
    {
        field_2_row_height = height;
    }

    /**
     * get the (currently unimportant to HSSF) option flags
     * @return flags - the current bitmask
     */

    public short getOptionFlags()
    {
        return field_1_option_flags;
    }

    /**
     * get the default row height
     * @return rowheight for undefined rows/rows w/undefined height
     */

    public short getRowHeight()
    {
        return field_2_row_height;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[DEFAULTROWHEIGHT]\n");
        buffer.append("    .optionflags    = ")
            .append(Integer.toHexString(getOptionFlags())).append("\n");
        buffer.append("    .rowheight      = ")
            .append(Integer.toHexString(getRowHeight())).append("\n");
        buffer.append("[/DEFAULTROWHEIGHT]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) 0x4);
        LittleEndian.putShort(data, 4 + offset, getOptionFlags());
        LittleEndian.putShort(data, 6 + offset, getRowHeight());
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 8;
    }

    public short getSid()
    {
        return this.sid;
    }

    public Object clone() {
      DefaultRowHeightRecord rec = new DefaultRowHeightRecord();
      rec.field_1_option_flags = field_1_option_flags;
      rec.field_2_row_height = field_2_row_height;
      return rec;
    }
}
