
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
 * Title:        Precision Record<P>
 * Description:  defines whether to store with full precision or what's displayed by the gui
 *               (meaning have really screwed up and skewed figures or only think you do!)<P>
 * REFERENCE:  PG 372 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class PrecisionRecord
    extends Record
{
    public final static short sid = 0xE;
    public short              field_1_precision;

    public PrecisionRecord()
    {
    }

    /**
     * Constructs a Precision record and sets its fields appropriately.
     *
     * @param id     id must be 0xe or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public PrecisionRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a Precision record and sets its fields appropriately.
     *
     * @param id     id must be 0xe or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record
     */

    public PrecisionRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A PRECISION RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_precision = LittleEndian.getShort(data, 0 + offset);
    }

    /**
     * set whether to use full precision or just skew all you figures all to hell.
     *
     * @param fullprecision - or not
     */

    public void setFullPrecision(boolean fullprecision)
    {
        if (fullprecision == true)
        {
            field_1_precision = 1;
        }
        else
        {
            field_1_precision = 0;
        }
    }

    /**
     * get whether to use full precision or just skew all you figures all to hell.
     *
     * @return fullprecision - or not
     */

    public boolean getFullPrecision()
    {
        return (field_1_precision == 1);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[PRECISION]\n");
        buffer.append("    .precision       = ").append(getFullPrecision())
            .append("\n");
        buffer.append("[/PRECISION]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              (( short ) 0x02));   // 2 bytes (6 total)
        LittleEndian.putShort(data, 4 + offset, field_1_precision);
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
