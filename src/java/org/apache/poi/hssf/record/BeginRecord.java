
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
 * The begin record defines the start of a block of records for a (grpahing
 * data object. This record is matched with a corresponding EndRecord.
 *
 * @see EndRecord
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */

public class BeginRecord
    extends Record
{
    public static final short sid = 0x1033;

    public BeginRecord()
    {
    }

    /**
     * Constructs a BeginRecord record and sets its fields appropriately.
     *
     * @param id     id must be 0x1033 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public BeginRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a BeginRecord record and sets its fields appropriately.
     *
     * @param id     id must be 0x1033 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public BeginRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A BEGIN RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[BEGIN]\n");
        buffer.append("[/BEGIN]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              (( short ) 0));   // no record info
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
