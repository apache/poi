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

package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hssf.record.Record;

/**
 * Contains a numeric cell value. <P>
 * REFERENCE:  PG 334 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */
public final class NumberRecord extends Record implements CellValueRecordInterface {
    public static final short sid = 0x203;
    private int             field_1_row;
    private short             field_2_col;
    private short             field_3_xf;
    private double            field_4_value;

    /** Creates new NumberRecord */
    public NumberRecord()
    {
    }

    /**
     * Constructs a Number record and sets its fields appropriately.
     *
     * @param in the RecordInputstream to read the record from
     */

    public NumberRecord(RecordInputStream in)
    {
        super(in);
    }

    /**
     * @param in the RecordInputstream to read the record from
     */

    protected void fillFields(RecordInputStream in)
    {
        //field_1_row   = LittleEndian.getShort(data, 0 + offset);
        field_1_row   = in.readUShort();
        field_2_col   = in.readShort();
        field_3_xf    = in.readShort();
        field_4_value = in.readDouble();
    }

    //public void setRow(short row)
    public void setRow(int row)
    {
        field_1_row = row;
    }

    public void setColumn(short col)
    {
        field_2_col = col;
    }

    /**
     * set the index to the ExtendedFormat
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     * @param xf  index to the XF record
     */

    public void setXFIndex(short xf)
    {
        field_3_xf = xf;
    }

    /**
     * set the value for the cell
     *
     * @param value  double representing the value
     */

    public void setValue(double value)
    {
        field_4_value = value;
    }

    //public short getRow()
    public int getRow()
    {
        return field_1_row;
    }

    public short getColumn()
    {
        return field_2_col;
    }

    /**
     * get the index to the ExtendedFormat
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     * @return index to the XF record
     */

    public short getXFIndex()
    {
        return field_3_xf;
    }

    /**
     * get the value for the cell
     *
     * @return double representing the value
     */

    public double getValue()
    {
        return field_4_value;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[NUMBER]\n");
        buffer.append("    .row            = ")
            .append(Integer.toHexString(getRow())).append("\n");
        buffer.append("    .col            = ")
            .append(Integer.toHexString(getColumn())).append("\n");
        buffer.append("    .xfindex        = ")
            .append(Integer.toHexString(getXFIndex())).append("\n");
        buffer.append("    .value          = ").append(getValue())
            .append("\n");
        buffer.append("[/NUMBER]\n");
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
        LittleEndian.putShort(data, 2 + offset, ( short ) 14);
        //LittleEndian.putShort(data, 4 + offset, getRow());
        LittleEndian.putShort(data, 4 + offset, ( short ) getRow());
        LittleEndian.putShort(data, 6 + offset, getColumn());
        LittleEndian.putShort(data, 8 + offset, getXFIndex());
        LittleEndian.putDouble(data, 10 + offset, getValue());
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 18;
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
            throw new RecordFormatException("NOT A Number RECORD");
        }
    }

    public short getSid()
    {
        return sid;
    }

    public boolean isInValueSection()
    {
        return true;
    }

    public boolean isValue()
    {
        return true;
    }

    public Object clone() {
      NumberRecord rec = new NumberRecord();
      rec.field_1_row = field_1_row;
      rec.field_2_col = field_2_col;
      rec.field_3_xf = field_3_xf;
      rec.field_4_value = field_4_value;
      return rec;
    }
}
