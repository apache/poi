
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
 * Title:        Hide Object Record<P>
 * Description:  flag defines whether to hide placeholders and object<P>
 * REFERENCE:  PG 321 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class HideObjRecord
    extends Record
{
    public final static short sid               = 0x8d;
    public final static short HIDE_ALL          = 2;
    public final static short SHOW_PLACEHOLDERS = 1;
    public final static short SHOW_ALL          = 0;
    private short             field_1_hide_obj;

    public HideObjRecord()
    {
    }

    /**
     * Constructs an HideObj record and sets its fields appropriately.
     *
     * @param id     id must be 0x8d or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public HideObjRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs an HideObj record and sets its fields appropriately.
     *
     * @param id     id must be 0x8d or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public HideObjRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A HIDEOBJ RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_hide_obj = LittleEndian.getShort(data, 0 + offset);
    }

    /**
     * set hide object options
     *
     * @param hide options
     * @see #HIDE_ALL
     * @see #SHOW_PLACEHOLDERS
     * @see #SHOW_ALL
     */

    public void setHideObj(short hide)
    {
        field_1_hide_obj = hide;
    }

    /**
     * get hide object options
     *
     * @return hide options
     * @see #HIDE_ALL
     * @see #SHOW_PLACEHOLDERS
     * @see #SHOW_ALL
     */

    public short getHideObj()
    {
        return field_1_hide_obj;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[HIDEOBJ]\n");
        buffer.append("    .hideobj         = ")
            .append(Integer.toHexString(getHideObj())).append("\n");
        buffer.append("[/HIDEOBJ]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              (( short ) 0x02));   // 2 bytes (6 total)
        LittleEndian.putShort(data, 4 + offset, getHideObj());
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
}
