
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
 * Title:        Dimensions Record<P>
 * Description:  provides the minumum and maximum bounds
 *               of a sheet.<P>
 * REFERENCE:  PG 303 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class DimensionsRecord
    extends Record
{
    public final static short sid = 0x200;
    private int               field_1_first_row;
    private int               field_2_last_row;   // plus 1
    private short             field_3_first_col;
    private short             field_4_last_col;
    private short             field_5_zero;       // must be 0 (reserved)

    public DimensionsRecord()
    {
    }

    /**
     * Constructs a Dimensions record and sets its fields appropriately.
     *
     * @param id     id must be 0x200 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public DimensionsRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a Dimensions record and sets its fields appropriately.
     *
     * @param id     id must be 0x200 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public DimensionsRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A valid DIMENSIONS RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_first_row = LittleEndian.getInt(data, 0 + offset);
        field_2_last_row  = LittleEndian.getInt(data, 4 + offset);
        field_3_first_col = LittleEndian.getShort(data, 8 + offset);
        field_4_last_col  = LittleEndian.getShort(data, 10 + offset);
        field_5_zero      = LittleEndian.getShort(data, 12 + offset);
    }

    /**
     * set the first row number for the sheet
     * @param row - first row on the sheet
     */

    public void setFirstRow(int row)
    {
        field_1_first_row = row;
    }

    /**
     * set the last row number for the sheet
     * @param row - last row on the sheet
     */

    public void setLastRow(int row)
    {
        field_2_last_row = row;
    }

    /**
     * set the first column number for the sheet
     * @param col  first column on the sheet
     */

    public void setFirstCol(short col)
    {
        field_3_first_col = col;
    }

    /**
     * set the last col number for the sheet
     * @param col  last column on the sheet
     */

    public void setLastCol(short col)
    {
        field_4_last_col = col;
    }

    /**
     * get the first row number for the sheet
     * @return row - first row on the sheet
     */

    public int getFirstRow()
    {
        return field_1_first_row;
    }

    /**
     * get the last row number for the sheet
     * @return row - last row on the sheet
     */

    public int getLastRow()
    {
        return field_2_last_row;
    }

    /**
     * get the first column number for the sheet
     * @return column - first column on the sheet
     */

    public short getFirstCol()
    {
        return field_3_first_col;
    }

    /**
     * get the last col number for the sheet
     * @return column - last column on the sheet
     */

    public short getLastCol()
    {
        return field_4_last_col;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[DIMENSIONS]\n");
        buffer.append("    .firstrow       = ")
            .append(Integer.toHexString(getFirstRow())).append("\n");
        buffer.append("    .lastrow        = ")
            .append(Integer.toHexString(getLastRow())).append("\n");
        buffer.append("    .firstcol       = ")
            .append(Integer.toHexString(getFirstCol())).append("\n");
        buffer.append("    .lastcol        = ")
            .append(Integer.toHexString(getLastCol())).append("\n");
        buffer.append("    .zero           = ")
            .append(Integer.toHexString(field_5_zero)).append("\n");
        buffer.append("[/DIMENSIONS]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) 14);
        LittleEndian.putInt(data, 4 + offset, getFirstRow());
        LittleEndian.putInt(data, 8 + offset, getLastRow());
        LittleEndian.putShort(data, 12 + offset, getFirstCol());
        LittleEndian.putShort(data, 14 + offset, getLastCol());
        LittleEndian.putShort(data, 16 + offset, ( short ) 0);
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 18;
    }

    public short getSid()
    {
        return this.sid;
    }
}
