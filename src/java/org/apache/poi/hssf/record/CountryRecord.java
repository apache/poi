
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

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
