
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
 * Title:        VCenter record<P>
 * Description:  tells whether to center the sheet between vertical margins<P>
 * REFERENCE:  PG 420 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */

public class VCenterRecord
    extends Record
{
    public final static short sid = 0x84;
    private short             field_1_vcenter;

    public VCenterRecord()
    {
    }

    /**
     * Constructs a VCENTER record and sets its fields appropriately.
     *
     * @param id     id must be 0x84 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public VCenterRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a VCENTER record and sets its fields appropriately.
     *
     * @param id     id must be 0x84 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public VCenterRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A VCenter RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_vcenter = LittleEndian.getShort(data, 0 + offset);
    }

    /**
     * set whether to center vertically or not
     * @param hc  vcenter or not
     */

    public void setVCenter(boolean hc)
    {
        if (hc == true)
        {
            field_1_vcenter = 1;
        }
        else
        {
            field_1_vcenter = 0;
        }
    }

    /**
     * get whether to center vertically or not
     * @return vcenter or not
     */

    public boolean getVCenter()
    {
        return (field_1_vcenter == 1);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[VCENTER]\n");
        buffer.append("    .vcenter        = ").append(getVCenter())
            .append("\n");
        buffer.append("[/VCENTER]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) 0x2);
        LittleEndian.putShort(data, 4 + offset, ( short ) field_1_vcenter);
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
      VCenterRecord rec = new VCenterRecord();
      rec.field_1_vcenter = field_1_vcenter;
      return rec;
    }
}
