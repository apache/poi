
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
 * Title:        Refresh All Record <P>
 * Description:  Flag whether to refresh all external data when loading a sheet.
 *               (which hssf doesn't support anyhow so who really cares?)<P>
 * REFERENCE:  PG 376 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class RefreshAllRecord
    extends Record
{
    public final static short sid = 0x1B7;
    private short             field_1_refreshall;

    public RefreshAllRecord()
    {
    }

    /**
     * Constructs a RefreshAll record and sets its fields appropriately.
     *
     * @param id     id must be 0x187 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public RefreshAllRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a RefreshAll record and sets its fields appropriately.
     *
     * @param id     id must be 0x187 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record data
     */

    public RefreshAllRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A REFRESHALL RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_refreshall = LittleEndian.getShort(data, 0 + offset);
    }

    /**
     * set whether to refresh all external data when loading a sheet
     * @param refreshall or not
     */

    public void setRefreshAll(boolean refreshall)
    {
        if (refreshall)
        {
            field_1_refreshall = 1;
        }
        else
        {
            field_1_refreshall = 0;
        }
    }

    /**
     * get whether to refresh all external data when loading a sheet
     * @return refreshall or not
     */

    public boolean getRefreshAll()
    {
        return (field_1_refreshall == 1);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[REFRESHALL]\n");
        buffer.append("    .refreshall      = ").append(getRefreshAll())
            .append("\n");
        buffer.append("[/REFRESHALL]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              (( short ) 0x02));   // 2 bytes (6 total)
        LittleEndian.putShort(data, 4 + offset, field_1_refreshall);
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
