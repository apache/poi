
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

import java.util.ArrayList;

/**
 * Title:        DBCell Record (Currently read only.  Not required.)
 * Description:  Used to find rows in blocks...TODO<P>
 * REFERENCE:  PG 299/440 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class DBCellRecord
    extends Record
{
    public final static short sid = 0xd7;
    private int               field_1_row_offset;
    private short[]           field_2_cell_offsets;

    public DBCellRecord()
    {
    }

    /**
     * Constructs a DBCellRecord and sets its fields appropriately
     *
     * @param id     id must be 0xd7 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public DBCellRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a DBCellRecord and sets its fields appropriately
     *
     * @param id     id must be 0xd7 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public DBCellRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A valid DBCell RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_row_offset   = LittleEndian.getShort(data, 0 + offset);
        field_2_cell_offsets = new short[ (size - 4) / 2 ];
        int element = 0;

        for (int k = 4; k < data.length; k += 2)
        {
            field_2_cell_offsets[ element++ ] = LittleEndian.getShort(data,
                    k + offset);
        }
    }

    /**
     * sets offset from the start of this DBCellRecord to the start of the first cell in
     * the next DBCell block.
     *
     * @param rowoffset to the start of the first cell in the next DBCell block
     */

    public void setRowOffset(int offset)
    {
        field_1_row_offset = offset;
    }

    // need short list impl.
    public void addCellOffset(short offset)
    {
        if (field_2_cell_offsets == null)
        {
            field_2_cell_offsets = new short[ 1 ];
        }
        else
        {
            short[] temp = new short[ field_2_cell_offsets.length + 1 ];

            System.arraycopy(field_2_cell_offsets, 0, temp, 0,
                             field_2_cell_offsets.length);
            field_2_cell_offsets = temp;
        }
        field_2_cell_offsets[ field_2_cell_offsets.length - 1 ] = offset;
    }

    /**
     * gets offset from the start of this DBCellRecord to the start of the first cell in
     * the next DBCell block.
     *
     * @return rowoffset to the start of the first cell in the next DBCell block
     */

    public int getRowOffset()
    {
        return field_1_row_offset;
    }

    /**
     * return the cell offset in the array
     *
     * @param index of the cell offset to retrieve
     * @return celloffset from the celloffset array
     */

    public short getCellOffsetAt(int index)
    {
        return field_2_cell_offsets[ index ];
    }

    /**
     * get the number of cell offsets in the celloffset array
     *
     * @return number of cell offsets
     */

    public int getNumCellOffsets()
    {
        return field_2_cell_offsets.length;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[DBCELL]\n");
        buffer.append("    .rowoffset       = ")
            .append(Integer.toHexString(getRowOffset())).append("\n");
        for (int k = 0; k < getNumCellOffsets(); k++)
        {
            buffer.append("    .cell_" + k + "          = ")
                .append(Integer.toHexString(getCellOffsetAt(k))).append("\n");
        }
        buffer.append("[/DBCELL]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        if (field_2_cell_offsets == null)
        {
            field_2_cell_offsets = new short[ 0 ];
        }
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              (( short ) (4 + (getNumCellOffsets() * 2))));
        LittleEndian.putInt(data, 4 + offset, getRowOffset());
        for (int k = 0; k < getNumCellOffsets(); k++)
        {
            LittleEndian.putShort(data, 8 + k + offset, getCellOffsetAt(k));
        }
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 8 + (getNumCellOffsets() * 2);
    }

    public short getSid()
    {
        return this.sid;
    }

    public boolean isInValueSection()
    {
        return true;
    }
}
