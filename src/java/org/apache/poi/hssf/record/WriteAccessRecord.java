
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
 * Title:        Write Access Record<P>
 * Description:  Stores the username of that who owns the spreadsheet generator
 *               (on unix the user's login, on Windoze its the name you typed when
 *                you installed the thing)<P>
 * REFERENCE:  PG 424 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class WriteAccessRecord
    extends Record
{
    public final static short sid = 0x5c;
    private String            field_1_username;

    public WriteAccessRecord()
    {
    }

    /**
     * Constructs a WriteAccess record and sets its fields appropriately.
     *
     * @param id     id must be 0x5c or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public WriteAccessRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a WriteAccess record and sets its fields appropriately.
     *
     * @param id     id must be 0x5c or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record data
     */

    public WriteAccessRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A WRITEACCESS RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_username = new String(data, 3 + offset, data.length - 4);
    }

    /**
     * set the username for the user that created the report.  HSSF uses the logged in user.
     * @param username of the user who  is logged in (probably "tomcat" or "apache")
     */

    public void setUsername(String username)
    {
        field_1_username = username;
    }

    /**
     * get the username for the user that created the report.  HSSF uses the logged in user.  On
     * natively created M$ Excel sheet this would be the name you typed in when you installed it
     * in most cases.
     * @return username of the user who  is logged in (probably "tomcat" or "apache")
     */

    public String getUsername()
    {
        return field_1_username;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[WRITEACCESS]\n");
        buffer.append("    .name            = ")
            .append(field_1_username.toString()).append("\n");
        buffer.append("[/WRITEACCESS]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        String       username = getUsername();
        StringBuffer temp     = new StringBuffer(0x70 - (0x3));

        temp.append(username);
        while (temp.length() < 0x70 - 0x3)
        {
            temp.append(
                " ");   // (70 = fixed lenght -3 = the overhead bits of unicode string)
        }
        username = temp.toString();
        UnicodeString str = new UnicodeString();

        str.setString(username);
        str.setOptionFlags(( byte ) 0x0);
        str.setCharCount(( short ) 0x4);
        byte[] stringbytes = str.serialize();

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              ( short ) (stringbytes
                                  .length));   // 112 bytes (115 total)
        System.arraycopy(stringbytes, 0, data, 4 + offset,
                         stringbytes.length);
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 116;
    }

    public short getSid()
    {
        return this.sid;
    }
}
