
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
        

/*
 * BlankRecord.java
 *
 * Created on December 10, 2001, 12:07 PM
 */
package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;

/**
 * Title:        Blank cell record <P>
 * Description:  Represents a column in a row with no value but with styling.<P>
 * REFERENCE:  PG 287 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */

public class BlankRecord
    extends Record
    implements CellValueRecordInterface, Comparable
{
    public final static short sid = 0x201;
    //private short             field_1_row;
    private int             field_1_row;
    private short             field_2_col;
    private short             field_3_xf;

    /** Creates a new instance of BlankRecord */

    public BlankRecord()
    {
    }

    /**
     * Constructs a BlankRecord and sets its fields appropriately
     *
     * @param id     id must be 0x201 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public BlankRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a BlankRecord and sets its fields appropriately
     *
     * @param id     id must be 0x201 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public BlankRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        //field_1_row = LittleEndian.getShort(data, 0 + offset);
        field_1_row = LittleEndian.getUShort(data, 0 + offset);
        field_2_col = LittleEndian.getShort(data, 2 + offset);
        field_3_xf  = LittleEndian.getShort(data, 4 + offset);
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
            throw new RecordFormatException("NOT A BLANKRECORD!");
        }
    }

    /**
     * set the row this cell occurs on
     * @param row the row this cell occurs within
     */

    //public void setRow(short row)
    public void setRow(int row)
    {
        field_1_row = row;
    }

    /**
     * get the row this cell occurs on
     *
     * @return the row
     */

    //public short getRow()
    public int getRow()
    {
        return field_1_row;
    }

    /**
     * get the column this cell defines within the row
     *
     * @return the column
     */

    public short getColumn()
    {
        return field_2_col;
    }

    /**
     * set the index of the extended format record to style this cell with
     *
     * @param xf - the 0-based index of the extended format
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     */

    public void setXFIndex(short xf)
    {
        field_3_xf = xf;
    }

    /**
     * get the index of the extended format record to style this cell with
     *
     * @return extended format index
     */

    public short getXFIndex()
    {
        return field_3_xf;
    }

    /**
     * set the column this cell defines within the row
     *
     * @param col the column this cell defines
     */

    public void setColumn(short col)
    {
        field_2_col = col;
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

    /**
     * return the non static version of the id for this record.
     */

    public short getSid()
    {
        return BlankRecord.sid;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[BLANK]\n");
        buffer.append("row       = ").append(Integer.toHexString(getRow()))
            .append("\n");
        buffer.append("col       = ").append(Integer.toHexString(getColumn()))
            .append("\n");
        buffer.append("xf        = ")
            .append(Integer.toHexString(getXFIndex())).append("\n");
        buffer.append("[/BLANK]\n");
        return buffer.toString();
    }

    /**
     * called by the class that is responsible for writing this sucker.
     * Subclasses should implement this so that their data is passed back in a
     * byte array.
     *
     * @return byte array containing instance data
     */

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) 6);
        //LittleEndian.putShort(data, 4 + offset, getRow());
        LittleEndian.putShort(data, 4 + offset, ( short ) getRow());
        LittleEndian.putShort(data, 6 + offset, getColumn());
        LittleEndian.putShort(data, 8 + offset, getXFIndex());
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 10;
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
      BlankRecord rec = new BlankRecord();
      rec.field_1_row = field_1_row;
      rec.field_2_col = field_2_col;
      rec.field_3_xf = field_3_xf;
      return rec;
    }
}
