
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
        field_1_row_offset   = LittleEndian.getUShort(data, 0 + offset);
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
     * @param offset    offset to the start of the first cell in the next DBCell block
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
