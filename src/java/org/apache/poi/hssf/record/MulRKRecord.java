
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

/*
 * MulRKRecord.java
 *
 * Created on November 9, 2001, 4:53 PM
 */
package org.apache.poi.hssf.record;

import java.util.ArrayList;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hssf.util.RKUtil;

/**
 * Used to store multiple RK numbers on a row.  1 MulRk = Multiple Cell values.
 * HSSF just converts this into multiple NUMBER records.  READ-ONLY SUPPORT!<P>
 * REFERENCE:  PG 330 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class MulRKRecord
    extends Record
{
    public final static short sid = 0xbd;
    private short             field_1_row;
    private short             field_2_first_col;
    private ArrayList         field_3_rks;
    private short             field_4_last_col;

    /** Creates new MulRKRecord */

    public MulRKRecord()
    {
    }

    /**
     * Constructs a MulRK record and sets its fields appropriately.
     *
     * @param id     id must be 0xbd or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public MulRKRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a MulRK record and sets its fields appropriately.
     *
     * @param id     id must be 0xbd or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of data
     */

    public MulRKRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    public short getRow()
    {
        return field_1_row;
    }

    /**
     * starting column (first cell this holds in the row)
     * @return first column number
     */

    public short getFirstColumn()
    {
        return field_2_first_col;
    }

    /**
     * ending column (last cell this holds in the row)
     * @return first column number
     */

    public short getLastColumn()
    {
        return field_4_last_col;
    }

    /**
     * get the number of columns this contains (last-first +1)
     * @return number of columns (last - first +1)
     */

    public int getNumColumns()
    {
        return field_4_last_col - field_2_first_col + 1;
    }

    /**
     * returns the xf index for column (coffset = column - field_2_first_col)
     * @return the XF index for the column
     */

    public short getXFAt(int coffset)
    {
        return (( RkRec ) field_3_rks.get(coffset)).xf;
    }

    /**
     * returns the rk number for column (coffset = column - field_2_first_col)
     * @return the value (decoded into a double)
     */

    public double getRKNumberAt(int coffset)
    {
        return RKUtil.decodeNumber((( RkRec ) field_3_rks.get(coffset)).rk);
    }

    /**
     * called by the constructor, should set class level fields.  Should throw
     * runtime exception for bad/icomplete data.
     *
     * @param data raw data
     * @param size size of data
     */

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_row       = LittleEndian.getShort(data, 0 + offset);
        field_2_first_col = LittleEndian.getShort(data, 2 + offset);
        field_3_rks       = parseRKs(data, 4, offset, size);
        field_4_last_col  = LittleEndian.getShort(data,
                                                  (field_3_rks.size() * 6)
                                                  + 4 + offset);
    }

    private ArrayList parseRKs(byte [] data, int offset, int recoffset,
                               short size)
    {
        ArrayList retval = new ArrayList();

        for (; offset < size - 2; )
        {
            RkRec rec = new RkRec();

            rec.xf = LittleEndian.getShort(data, offset + recoffset);
            offset += 2;
            rec.rk = LittleEndian.getInt(data, offset + recoffset);
            offset += 4;
            retval.add(rec);
        }
        return retval;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[MULRK]\n");
        buffer.append("firstcol  = ")
            .append(Integer.toHexString(getFirstColumn())).append("\n");
        buffer.append(" lastcol  = ")
            .append(Integer.toHexString(getLastColumn())).append("\n");
        for (int k = 0; k < getNumColumns(); k++)
        {
            buffer.append("xf").append(k).append("        = ")
                .append(Integer.toHexString(getXFAt(k))).append("\n");
            buffer.append("rk").append(k).append("        = ")
                .append(getRKNumberAt(k)).append("\n");
        }
        buffer.append("[/MULRK]\n");
        return buffer.toString();
    }

    /**
     * called by constructor, should throw runtime exception in the event of a
     * record passed with a differing ID.
     *
     * @param id alleged id for this record
     */

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("Not a MulRKRecord!");
        }
    }

    public short getSid()
    {
        return this.sid;
    }

    public int serialize(int offset, byte [] data)
    {
        throw new RecordFormatException(
            "Sorry, you can't serialize a MulRK in this release");
    }
}

class RkRec
{
    public short xf;
    public int   rk;
}
