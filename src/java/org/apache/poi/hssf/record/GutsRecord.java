
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
 * Title:        Guts Record <P>
 * Description:  Row/column gutter sizes <P>
 * REFERENCE:  PG 320 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class GutsRecord
    extends Record
{
    public final static short sid = 0x80;
    private short             field_1_left_row_gutter;   // size of the row gutter to the left of the rows
    private short             field_2_top_col_gutter;    // size of the column gutter above the columns
    private short             field_3_row_level_max;     // maximum outline level for row gutters
    private short             field_4_col_level_max;     // maximum outline level for column gutters

    public GutsRecord()
    {
    }

    /**
     * Constructs a Guts record and sets its fields appropriately.
     *
     * @param id     id must be 0x80 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public GutsRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a Guts record and sets its fields appropriately.
     *
     * @param id     id must be 0x80 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public GutsRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A Guts RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_left_row_gutter = LittleEndian.getShort(data, 0 + offset);
        field_2_top_col_gutter  = LittleEndian.getShort(data, 2 + offset);
        field_3_row_level_max   = LittleEndian.getShort(data, 4 + offset);
        field_4_col_level_max   = LittleEndian.getShort(data, 6 + offset);
    }

    /**
     * set the size of the gutter that appears at the left of the rows
     *
     * @param gut  gutter size in screen units
     */

    public void setLeftRowGutter(short gut)
    {
        field_1_left_row_gutter = gut;
    }

    /**
     * set the size of the gutter that appears at the above the columns
     *
     * @param gut  gutter size in screen units
     */

    public void setTopColGutter(short gut)
    {
        field_2_top_col_gutter = gut;
    }

    /**
     * set the maximum outline level for the row gutter.
     *
     * @param max  maximum outline level
     */

    public void setRowLevelMax(short max)
    {
        field_3_row_level_max = max;
    }

    /**
     * set the maximum outline level for the col gutter.
     *
     * @param max  maximum outline level
     */

    public void setColLevelMax(short max)
    {
        field_4_col_level_max = max;
    }

    /**
     * get the size of the gutter that appears at the left of the rows
     *
     * @return gutter size in screen units
     */

    public short getLeftRowGutter()
    {
        return field_1_left_row_gutter;
    }

    /**
     * get the size of the gutter that appears at the above the columns
     *
     * @return gutter size in screen units
     */

    public short getTopColGutter()
    {
        return field_2_top_col_gutter;
    }

    /**
     * get the maximum outline level for the row gutter.
     *
     * @return maximum outline level
     */

    public short getRowLevelMax()
    {
        return field_3_row_level_max;
    }

    /**
     * get the maximum outline level for the col gutter.
     *
     * @return maximum outline level
     */

    public short getColLevelMax()
    {
        return field_4_col_level_max;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[GUTS]\n");
        buffer.append("    .leftgutter     = ")
            .append(Integer.toHexString(getLeftRowGutter())).append("\n");
        buffer.append("    .topgutter      = ")
            .append(Integer.toHexString(getTopColGutter())).append("\n");
        buffer.append("    .rowlevelmax    = ")
            .append(Integer.toHexString(getRowLevelMax())).append("\n");
        buffer.append("    .collevelmax    = ")
            .append(Integer.toHexString(getColLevelMax())).append("\n");
        buffer.append("[/GUTS]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) 0x8);
        LittleEndian.putShort(data, 4 + offset, getLeftRowGutter());
        LittleEndian.putShort(data, 6 + offset, getTopColGutter());
        LittleEndian.putShort(data, 8 + offset, getRowLevelMax());
        LittleEndian.putShort(data, 10 + offset, getColLevelMax());
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 12;
    }

    public short getSid()
    {
        return this.sid;
    }
}
