
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
 * Title:        Password Record<P>
 * Description:  stores the encrypted password for a sheet or workbook (HSSF doesn't support encryption)
 * REFERENCE:  PG 371 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class PasswordRecord
    extends Record
{
    public final static short sid = 0x13;
    private short             field_1_password;   // not sure why this is only 2 bytes, but it is... go figure

    public PasswordRecord()
    {
    }

    /**
     * Constructs a Password record and sets its fields appropriately.
     *
     * @param id     id must be 0x13 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public PasswordRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a Password record and sets its fields appropriately.
     *
     * @param id     id must be 0x13 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the data
     */

    public PasswordRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A PASSWORD RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_password = LittleEndian.getShort(data, 0 + offset);
    }

    /**
     * set the password
     *
     * @param password  representing the password
     */

    public void setPassword(short password)
    {
        field_1_password = password;
    }

    /**
     * get the password
     *
     * @return short  representing the password
     */

    public short getPassword()
    {
        return field_1_password;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[PASSWORD]\n");
        buffer.append("    .password       = ")
            .append(Integer.toHexString(getPassword())).append("\n");
        buffer.append("[/PASSWORD]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              (( short ) 0x02));   // 2 bytes (6 total)
        LittleEndian.putShort(data, 4 + offset, getPassword());
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

    /**
     * Clone this record.
     */
    public Object clone() {
      PasswordRecord clone = new PasswordRecord();
      clone.setPassword(field_1_password);
      return clone;
    }

}
