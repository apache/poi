
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
 * Title:        Use Natural Language Formulas Flag<P>
 * Description:  Tells the GUI if this was written by something that can use
 *               "natural language" formulas. HSSF can't.<P>
 * REFERENCE:  PG 420 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class UseSelFSRecord
    extends Record
{
    public final static short sid   = 0x160;
    public final static short TRUE  = 1;
    public final static short FALSE = 0;
    private short             field_1_flag;

    public UseSelFSRecord()
    {
    }

    /**
     * Constructs a UseSelFS record and sets its fields appropriately.
     *
     * @param id     id must be 0x160 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public UseSelFSRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a UseSelFS record and sets its fields appropriately.
     *
     * @param id     id must be 0x160 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of record
     */

    public UseSelFSRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A UseSelFS RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_flag = LittleEndian.getShort(data, 0 + offset);
    }

    /**
     * turn the flag on or off
     *
     * @param flag  whether to use natural language formulas or not
     * @see #TRUE
     * @see #FALSE
     */

    public void setFlag(short flag)
    {
        field_1_flag = flag;
    }

    /**
     * returns whether we use natural language formulas or not
     *
     * @return whether to use natural language formulas or not
     * @see #TRUE
     * @see #FALSE
     */

    public short getFlag()
    {
        return field_1_flag;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[USESELFS]\n");
        buffer.append("    .flag            = ")
            .append(Integer.toHexString(getFlag())).append("\n");
        buffer.append("[/USESELFS]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              (( short ) 0x02));   // 2 bytes (6 total)
        LittleEndian.putShort(data, 4 + offset, getFlag());
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
