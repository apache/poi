
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
 * Title:        Label SST Record<P>
 * Description:  Refers to a string in the shared string table and is a column
 *               value.  <P>
 * REFERENCE:  PG 325 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */

public class LabelSSTRecord
    extends Record
    implements CellValueRecordInterface, Comparable
{
    public final static short sid = 0xfd;
    //private short             field_1_row;
    private int             field_1_row;
    private short             field_2_column;
    private short             field_3_xf_index;
    private int               field_4_sst_index;

    public LabelSSTRecord()
    {
    }

    /**
     * Constructs an LabelSST record and sets its fields appropriately.
     *
     * @param id     id must be 0xfd or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public LabelSSTRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs an LabelSST record and sets its fields appropriately.
     *
     * @param id     id must be 0xfd or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public LabelSSTRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A valid LabelSST RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        //field_1_row       = LittleEndian.getShort(data, 0 + offset);
        field_1_row       = LittleEndian.getUShort(data, 0 + offset);
        field_2_column    = LittleEndian.getShort(data, 2 + offset);
        field_3_xf_index  = LittleEndian.getShort(data, 4 + offset);
        field_4_sst_index = LittleEndian.getInt(data, 6 + offset);
    }

    //public void setRow(short row)
    public void setRow(int row)
    {
        field_1_row = row;
    }

    public void setColumn(short col)
    {
        field_2_column = col;
    }

    /**
     * set the index to the extended format record
     *
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     * @param index - the index to the XF record
     */

    public void setXFIndex(short index)
    {
        field_3_xf_index = index;
    }

    /**
     * set the index to the string in the SSTRecord
     *
     * @param index - of string in the SST Table
     * @see org.apache.poi.hssf.record.SSTRecord
     */

    public void setSSTIndex(int index)
    {
        field_4_sst_index = index;
    }

    //public short getRow()
    public int getRow()
    {
        return field_1_row;
    }

    public short getColumn()
    {
        return field_2_column;
    }

    /**
     * get the index to the extended format record
     *
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     * @return the index to the XF record
     */

    public short getXFIndex()
    {
        return field_3_xf_index;
    }

    /**
     * get the index to the string in the SSTRecord
     *
     * @return index of string in the SST Table
     * @see org.apache.poi.hssf.record.SSTRecord
     */

    public int getSSTIndex()
    {
        return field_4_sst_index;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[LABELSST]\n");
        buffer.append("    .row            = ")
            .append(Integer.toHexString(getRow())).append("\n");
        buffer.append("    .column         = ")
            .append(Integer.toHexString(getColumn())).append("\n");
        buffer.append("    .xfindex        = ")
            .append(Integer.toHexString(getXFIndex())).append("\n");
        buffer.append("    .sstindex       = ")
            .append(Integer.toHexString(getSSTIndex())).append("\n");
        buffer.append("[/LABELSST]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) 10);
        //LittleEndian.putShort(data, 4 + offset, getRow());
        LittleEndian.putShort(data, 4 + offset, ( short )getRow());
        LittleEndian.putShort(data, 6 + offset, getColumn());
        LittleEndian.putShort(data, 8 + offset, getXFIndex());
        LittleEndian.putInt(data, 10 + offset, getSSTIndex());
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 14;
    }

    public short getSid()
    {
        return this.sid;
    }

    public boolean isBefore(CellValueRecordInterface i)
    {
        if (this.getRow() > i.getRow())
        {
            return false;
        }
        if ((this.getRow() == i.getRow())
                && (this.getColumn() > i.getColumn()))
        {
            return false;
        }
        if ((this.getRow() == i.getRow())
                && (this.getColumn() == i.getColumn()))
        {
            return false;
        }
        return true;
    }

    public boolean isAfter(CellValueRecordInterface i)
    {
        if (this.getRow() < i.getRow())
        {
            return false;
        }
        if ((this.getRow() == i.getRow())
                && (this.getColumn() < i.getColumn()))
        {
            return false;
        }
        if ((this.getRow() == i.getRow())
                && (this.getColumn() == i.getColumn()))
        {
            return false;
        }
        return true;
    }

    public boolean isEqual(CellValueRecordInterface i)
    {
        return ((this.getRow() == i.getRow())
                && (this.getColumn() == i.getColumn()));
    }

    public boolean isInValueSection()
    {
        return true;
    }

    public boolean isValue()
    {
        return true;
    }

    public int compareTo(Object obj)
    {
        CellValueRecordInterface loc = ( CellValueRecordInterface ) obj;

        if ((this.getRow() == loc.getRow())
                && (this.getColumn() == loc.getColumn()))
        {
            return 0;
        }
        if (this.getRow() < loc.getRow())
        {
            return -1;
        }
        if (this.getRow() > loc.getRow())
        {
            return 1;
        }
        if (this.getColumn() < loc.getColumn())
        {
            return -1;
        }
        if (this.getColumn() > loc.getColumn())
        {
            return 1;
        }
        return -1;
    }

    public boolean equals(Object obj)
    {
        if (!(obj instanceof CellValueRecordInterface))
        {
            return false;
        }
        CellValueRecordInterface loc = ( CellValueRecordInterface ) obj;

        if ((this.getRow() == loc.getRow())
                && (this.getColumn() == loc.getColumn()))
        {
            return true;
        }
        return false;
    }

    public Object clone() {
      LabelSSTRecord rec = new LabelSSTRecord();
      rec.field_1_row = field_1_row;
      rec.field_2_column = field_2_column;
      rec.field_3_xf_index = field_3_xf_index;
      rec.field_4_sst_index = field_4_sst_index;
      return rec;
    }
}
