
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
 * Title: MMS Record<P>
 * Description: defines how many add menu and del menu options are stored
 *                    in the file. Should always be set to 0 for HSSF workbooks<P>
 * REFERENCE:  PG 328 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class MMSRecord
    extends Record
{
    public final static short sid = 0xC1;
    private byte              field_1_addMenuCount;   // = 0;
    private byte              field_2_delMenuCount;   // = 0;

    public MMSRecord()
    {
    }

    /**
     * Constructs a MMS record and sets its fields appropriately.
     *
     * @param id     id must be 0xc1 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public MMSRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a MMS record and sets its fields appropriately.
     *
     * @param id     id must be 0xc1 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the data
     */

    public MMSRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A MMS RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_addMenuCount = data[ 0 + offset ];
        field_2_delMenuCount = data[ 1 + offset ];
    }

    /**
     * set number of add menu options (set to 0)
     * @param am  number of add menu options
     */

    public void setAddMenuCount(byte am)
    {
        field_1_addMenuCount = am;
    }

    /**
     * set number of del menu options (set to 0)
     * @param dm  number of del menu options
     */

    public void setDelMenuCount(byte dm)
    {
        field_2_delMenuCount = dm;
    }

    /**
     * get number of add menu options (should be 0)
     * @return number of add menu options
     */

    public byte getAddMenuCount()
    {
        return field_1_addMenuCount;
    }

    /**
     * get number of add del options (should be 0)
     * @return number of add menu options
     */

    public byte getDelMenuCount()
    {
        return field_2_delMenuCount;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[MMS]\n");
        buffer.append("    .addMenu        = ")
            .append(Integer.toHexString(getAddMenuCount())).append("\n");
        buffer.append("    .delMenu        = ")
            .append(Integer.toHexString(getDelMenuCount())).append("\n");
        buffer.append("[/MMS]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              (( short ) 0x02));   // 2 bytes (6 total)
        data[ 4 + offset ] = getAddMenuCount();
        data[ 5 + offset ] = getDelMenuCount();
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
