
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

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
public final class BlankRecord extends Record implements CellValueRecordInterface {
    public final static short sid = 0x201;
    private int             field_1_row;
    private short             field_2_col;
    private short             field_3_xf;

    /** Creates a new instance of BlankRecord */

    public BlankRecord()
    {
    }

    /**
     * Constructs a BlankRecord and sets its fields appropriately
     * @param in the RecordInputstream to read the record from
     */

    public BlankRecord(RecordInputStream in)
    {
        super(in);
    }

    protected void fillFields(RecordInputStream in)
    {
        //field_1_row = LittleEndian.getShort(data, 0 + offset);
        field_1_row = in.readUShort();
        field_2_col = in.readShort();
        field_3_xf  = in.readShort();
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
        return sid;
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

    public Object clone() {
      BlankRecord rec = new BlankRecord();
      rec.field_1_row = field_1_row;
      rec.field_2_col = field_2_col;
      rec.field_3_xf = field_3_xf;
      return rec;
    }
}
