
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
import org.apache.poi.util.StringUtil;

/**
 * Title:        Format Record<P>
 * Description:  describes a number format -- those goofy strings like $(#,###)<P>
 *
 * REFERENCE:  PG 317 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class FormatRecord
    extends Record
{
    public final static short sid = 0x41e;
    private short             field_1_index_code;
    private byte              field_2_formatstring_len;
    private short             field_3_zero;   // undocumented 2 bytes of 0
    private String            field_4_formatstring;

    public FormatRecord()
    {
    }

    /**
     * Constructs a Format record and sets its fields appropriately.
     *
     * @param id     id must be 0x41e or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public FormatRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a Format record and sets its fields appropriately.
     *
     * @param id     id must be 0x41e or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public FormatRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A FORMAT RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_index_code       = LittleEndian.getShort(data, 0 + offset);
        field_2_formatstring_len = data[ 2 + offset ];
        field_3_zero             = LittleEndian.getShort(data, 3 + offset);
        field_4_formatstring     = new String(data, 5 + offset,
                                              LittleEndian.ubyteToInt(field_2_formatstring_len));
    }

    /**
     * set the format index code (for built in formats)
     *
     * @param index  the format index code
     * @see org.apache.poi.hssf.model.Workbook
     */

    public void setIndexCode(short index)
    {
        field_1_index_code = index;
    }

    /**
     * set the format string length
     *
     * @param len  the length of the format string
     * @see #setFormatString(String)
     */

    public void setFormatStringLength(byte len)
    {
        field_2_formatstring_len = len;
    }

    /**
     * set the format string
     *
     * @param fs  the format string
     * @see #setFormatStringLength(byte)
     */

    public void setFormatString(String fs)
    {
        field_4_formatstring = fs;
    }

    /**
     * get the format index code (for built in formats)
     *
     * @return the format index code
     * @see org.apache.poi.hssf.model.Workbook
     */

    public short getIndexCode()
    {
        return field_1_index_code;
    }

    /**
     * get the format string length
     *
     * @return the length of the format string
     * @see #getFormatString()
     */

    public byte getFormatStringLength()
    {
        return field_2_formatstring_len;
    }

    /**
     * get the format string
     *
     * @return the format string
     * @see #getFormatStringLength()
     */

    public String getFormatString()
    {
        return field_4_formatstring;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[FORMAT]\n");
        buffer.append("    .indexcode       = ")
            .append(Integer.toHexString(getIndexCode())).append("\n");
        buffer.append("    .formatstringlen = ")
            .append(Integer.toHexString(getFormatStringLength()))
            .append("\n");
        buffer.append("    .zero            = ")
            .append(Integer.toHexString(field_3_zero)).append("\n");
        buffer.append("    .formatstring    = ").append(getFormatString())
            .append("\n");
        buffer.append("[/FORMAT]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              ( short ) (5 + getFormatStringLength()));

        // 9 - 4(len/sid) + format string length
        LittleEndian.putShort(data, 4 + offset, getIndexCode());
        data[ 6 + offset ] = getFormatStringLength();
        LittleEndian.putShort(data, 7 + offset, ( short ) 0);
        StringUtil.putCompressedUnicode(getFormatString(), data, 9 + offset);
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 9 + getFormatStringLength();
    }

    public short getSid()
    {
        return this.sid;
    }
}
