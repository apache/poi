
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
 * Title:        Country Record (aka WIN.INI country)<P>
 * Description:  used for localization.  Currently HSSF always sets this to 1
 * and it seems to work fine even in Germany.  (es geht's auch fuer Deutschland)<P>
 *
 * REFERENCE:  PG 298 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class CountryRecord
    extends Record
{
    public final static short sid = 0x8c;

    // 1 for US
    private short             field_1_default_country;
    private short             field_2_current_country;

    public CountryRecord()
    {
    }

    /**
     * Constructs a CountryRecord and sets its fields appropriately
     *
     * @param id     id must be 0x8c or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public CountryRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a CountryRecord and sets its fields appropriately
     *
     * @param id     id must be 0x8c or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public CountryRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A Country RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_default_country = LittleEndian.getShort(data, 0 + offset);
        field_2_current_country = LittleEndian.getShort(data, 2 + offset);
    }

    /**
     * sets the default country
     *
     * @param country ID to set (1 = US)
     */

    public void setDefaultCountry(short country)
    {
        field_1_default_country = country;
    }

    /**
     * sets the current country
     *
     * @param country ID to set (1 = US)
     */

    public void setCurrentCountry(short country)
    {
        field_2_current_country = country;
    }

    /**
     * gets the default country
     *
     * @return country ID (1 = US)
     */

    public short getDefaultCountry()
    {
        return field_1_default_country;
    }

    /**
     * gets the current country
     *
     * @return country ID (1 = US)
     */

    public short getCurrentCountry()
    {
        return field_2_current_country;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[COUNTRY]\n");
        buffer.append("    .defaultcountry  = ")
            .append(Integer.toHexString(getDefaultCountry())).append("\n");
        buffer.append("    .currentcountry  = ")
            .append(Integer.toHexString(getCurrentCountry())).append("\n");
        buffer.append("[/COUNTRY]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              (( short ) 0x04));   // 4 bytes (8 total)
        LittleEndian.putShort(data, 4 + offset, getDefaultCountry());
        LittleEndian.putShort(data, 6 + offset, getCurrentCountry());
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
}
