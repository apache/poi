
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
 * Title: Interface End Record<P>
 * Description: Shows where the Interface Records end (MMS)
 *  (has no fields)<P>
 * REFERENCE:  PG 324 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class InterfaceEndRecord
    extends Record
{
    public final static short sid = 0xe2;

    public InterfaceEndRecord()
    {
    }

    /**
     * Constructs an InterfaceEnd record and sets its fields appropriately.
     *
     * @param id     id must be 0xe2 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public InterfaceEndRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs an InterfaceEnd record and sets its fields appropriately.
     *
     * @param id     id must be 0xe2 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the data
     */

    public InterfaceEndRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A INTERFACEEND RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[INTERFACEEND]\n");
        buffer.append("[/INTERFACEEND]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              (( short ) 0x00));   // 0 bytes (4 total)
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 4;
    }

    public short getSid()
    {
        return this.sid;
    }
}
