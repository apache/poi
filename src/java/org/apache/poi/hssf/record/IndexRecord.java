
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

import org.apache.poi.util.IntList;
import org.apache.poi.util.LittleEndian;

/**
 * Title:        Index Record<P>
 * Description:  Occurs right after BOF, tells you where the DBCELL records are for a sheet
 *               Important for locating cells<P>
 * NOT USED IN THIS RELEASE
 * REFERENCE:  PG 323 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */

public class IndexRecord
    extends Record
{
    public final static short sid             = 0x20B;
    public final static int   DBCELL_CAPACITY = 30;
    public int                field_1_zero;            // reserved must be 0
    public int                field_2_first_row;       // first row on the sheet
    public int                field_3_last_row_add1;   // last row
    public int                field_4_zero;            // reserved must be 0
    public IntList            field_5_dbcells;         // array of offsets to DBCELL records

    public IndexRecord()
    {
    }

    /**
     * Constructs an Index record and sets its fields appropriately.
     *
     * @param id     id must be 0x208 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public IndexRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs an Index record and sets its fields appropriately.
     *
     * @param id     id must be 0x208 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of record data
     */

    public IndexRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT An Index RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_5_dbcells       =
            new IntList(DBCELL_CAPACITY);   // initial capacity of 30
        field_1_zero          = LittleEndian.getInt(data, 0 + offset);
        field_2_first_row     = LittleEndian.getInt(data, 4 + offset);
        field_3_last_row_add1 = LittleEndian.getInt(data, 8 + offset);
        field_4_zero          = LittleEndian.getInt(data, 12 + offset);
        for (int k = 16; k < size; k = k + 4)
        {

            // System.out.println("getting " + k);
            field_5_dbcells.add(LittleEndian.getInt(data, k + offset));
        }
    }

    public void setFirstRow(int row)
    {
        field_2_first_row = row;
    }

    public void setLastRowAdd1(int row)
    {
        field_3_last_row_add1 = row;
    }

    public void addDbcell(int cell)
    {
        if (field_5_dbcells == null)
        {
            field_5_dbcells = new IntList();
        }
        field_5_dbcells.add(cell);
    }

    public void setDbcell(int cell, int value)
    {
        field_5_dbcells.set(cell, value);
    }

    public int getFirstRow()
    {
        return field_2_first_row;
    }

    public int getLastRowAdd1()
    {
        return field_3_last_row_add1;
    }

    public int getNumDbcells()
    {
        if (field_5_dbcells == null)
        {
            return 0;
        }
        return field_5_dbcells.size();
    }

    public int getDbcellAt(int cellnum)
    {
        return field_5_dbcells.get(cellnum);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[INDEX]\n");
        buffer.append("    .firstrow       = ")
            .append(Integer.toHexString(getFirstRow())).append("\n");
        buffer.append("    .lastrowadd1    = ")
            .append(Integer.toHexString(getLastRowAdd1())).append("\n");
        for (int k = 0; k < getNumDbcells(); k++)
        {
            buffer.append("    .dbcell_" + k + "       = ")
                .append(Integer.toHexString(getDbcellAt(k))).append("\n");
        }
        buffer.append("[/INDEX]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              ( short ) (16 + (getNumDbcells() * 4)));
        LittleEndian.putInt(data, 4 + offset, 0);
        LittleEndian.putInt(data, 8 + offset, getFirstRow());
        LittleEndian.putInt(data, 12 + offset, getLastRowAdd1());
        LittleEndian.putInt(data, 16 + offset, 0);
        for (int k = 0; k < getNumDbcells(); k++)
        {
            LittleEndian.putInt(data, (k * 4) + 20 + offset, getDbcellAt(k));
        }
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 20 + (getNumDbcells() * 4);
    }

    public short getSid()
    {
        return this.sid;
    }

    public Object clone() {
      IndexRecord rec = new IndexRecord();
      rec.field_1_zero = field_1_zero;
      rec.field_2_first_row = field_2_first_row;
      rec.field_3_last_row_add1 = field_3_last_row_add1;
      rec.field_4_zero = field_4_zero;
      rec.field_5_dbcells = new IntList();
      rec.field_5_dbcells.addAll(field_5_dbcells);
      return rec;
    }
}
