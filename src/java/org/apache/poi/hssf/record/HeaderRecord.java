
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
 * Title:        Header Record<P>
 * Description:  Specifies a header for a sheet<P>
 * REFERENCE:  PG 321 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Shawn Laubach (laubach@acm.org) Modified 3/14/02
 * @version 2.0-pre
 */

public class HeaderRecord
    extends Record
{
    public final static short sid = 0x14;
    private byte              field_1_header_len;
    private String            field_2_header;

    public HeaderRecord()
    {
    }

    /**
     * Constructs an Header record and sets its fields appropriately.
     *
     * @param id     id must be 0x14 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public HeaderRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs an Header record and sets its fields appropriately.
     *
     * @param id     id must be 0x14 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public HeaderRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A HEADERRECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        if (size > 0)
        {
            field_1_header_len = data[ 0 + offset ];
            field_2_header     = new String(data, 3 + offset, // [Shawn] Changed 1 to 3 for offset of string
                                            LittleEndian.ubyteToInt(field_1_header_len));
        }
    }

    /**
     * set the length of the header string
     *
     * @param len  length of the header string
     * @see #setHeader(String)
     */

    public void setHeaderLength(byte len)
    {
        field_1_header_len = len;
    }

    /**
     * set the header string
     *
     * @param header string to display
     * @see #setHeaderLength(byte)
     */

    public void setHeader(String header)
    {
        field_2_header = header;
    }

    /**
     * get the length of the header string
     *
     * @return length of the header string
     * @see #getHeader()
     */

    public short getHeaderLength()
    {
        return (short)(0xFF & field_1_header_len); // [Shawn] Fixed needing unsigned byte
    }

    /**
     * get the header string
     *
     * @return header string to display
     * @see #getHeaderLength()
     */

    public String getHeader()
    {
        return field_2_header;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[HEADER]\n");
        buffer.append("    .length         = ").append(getHeaderLength())
            .append("\n");
        buffer.append("    .header         = ").append(getHeader())
            .append("\n");
        buffer.append("[/HEADER]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        int len = 4;

        if (getHeaderLength() != 0)
        {
            len+=3; // [Shawn] Fixed for two null bytes in the length
        }
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              ( short ) ((len - 4) + getHeaderLength()));

        if (getHeaderLength() > 0)
        {
            data[ 4 + offset ] = (byte)getHeaderLength();
            StringUtil.putCompressedUnicode(getHeader(), data, 7 + offset); // [Shawn] Place the string in the correct offset
        }
        return getRecordSize();
    }

    public int getRecordSize()
    {
        int retval = 4;

        if (getHeaderLength() != 0)
        {
            retval+=3; // [Shawn] Fixed for two null bytes in the length
        }
        retval += getHeaderLength();
        return retval;
    }

    public short getSid()
    {
        return this.sid;
    }
}
