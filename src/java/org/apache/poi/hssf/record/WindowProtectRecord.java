
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
 * Title: Window Protect Record<P>
 * Description:  flags whether workbook windows are protected<P>
 * REFERENCE:  PG 424 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class WindowProtectRecord
    extends Record
{
    public final static short sid = 0x19;
    private short             field_1_protect;

    public WindowProtectRecord()
    {
    }

    /**
     * Constructs a WindowProtect record and sets its fields appropriately.
     *
     * @param id     id must be 0x19 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public WindowProtectRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a WindowProtect record and sets its fields appropriately.
     *
     * @param id     id must be 0x19 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public WindowProtectRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A WINDOWPROTECT RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_protect = LittleEndian.getShort(data, 0 + offset);
    }

    /**
     * set whether this window should be protected or not
     * @param protect or not
     */

    public void setProtect(boolean protect)
    {
        if (protect == true)
        {
            field_1_protect = 1;
        }
        else
        {
            field_1_protect = 0;
        }
    }

    /**
     * is this window protected or not
     *
     * @return protected or not
     */

    public boolean getProtect()
    {
        return (field_1_protect == 1);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[WINDOWPROTECT]\n");
        buffer.append("    .protect         = ").append(getProtect())
            .append("\n");
        buffer.append("[/WINDOWPROTECT]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              (( short ) 0x02));   // 2 bytes (6 total)
        LittleEndian.putShort(data, 4 + offset, field_1_protect);
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
