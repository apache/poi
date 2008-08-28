
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
 * BoolErrRecord.java
 *
 * Created on January 19, 2002, 9:30 AM
 */
package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;

/**
 * Creates new BoolErrRecord. <P>
 * REFERENCE:  PG ??? Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Michael P. Harhen
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */
public final class BoolErrRecord extends Record implements CellValueRecordInterface {
    public final static short sid = 0x205;
    private int               field_1_row;
    private short             field_2_column;
    private short             field_3_xf_index;
    private byte              field_4_bBoolErr;
    private byte              field_5_fError;

    /** Creates new BoolErrRecord */

    public BoolErrRecord()
    {
    }

    /**
     * Constructs a BoolErr record and sets its fields appropriately.
     *
     * @param in the RecordInputstream to read the record from
     */

    public BoolErrRecord(RecordInputStream in)
    {
        super(in);
    }

    /**
     * @param in the RecordInputstream to read the record from
     */

    protected void fillFields(RecordInputStream in)
    {
        //field_1_row      = LittleEndian.getShort(data, 0 + offset);
        field_1_row      = in.readUShort();
        field_2_column   = in.readShort();
        field_3_xf_index = in.readShort();
        field_4_bBoolErr = in.readByte();
        field_5_fError   = in.readByte();
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
     * set the index to the ExtendedFormat
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     * @param xf    index to the XF record
     */

    public void setXFIndex(short xf)
    {
        field_3_xf_index = xf;
    }

    /**
     * set the boolean value for the cell
     *
     * @param value   representing the boolean value
     */

    public void setValue(boolean value)
    {
        field_4_bBoolErr = value ? ( byte ) 1
                                 : ( byte ) 0;
        field_5_fError   = ( byte ) 0;
    }

    /**
     * set the error value for the cell
     *
     * @param value     error representing the error value
     *                  this value can only be 0,7,15,23,29,36 or 42
     *                  see bugzilla bug 16560 for an explanation
     */

    public void setValue(byte value)
    {
        if ( (value==0)||(value==7)||(value==15)||(value==23)||(value==29)||(value==36)||(value==42)) {
            field_4_bBoolErr = value;
            field_5_fError   = ( byte ) 1;
        } else {
            throw new RuntimeException("Error Value can only be 0,7,15,23,29,36 or 42. It cannot be "+value);
        }
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
     * get the index to the ExtendedFormat
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     * @return index to the XF record
     */

    public short getXFIndex()
    {
        return field_3_xf_index;
    }

    /**
     * get the value for the cell
     *
     * @return boolean representing the boolean value
     */

    public boolean getBooleanValue()
    {
        return (field_4_bBoolErr != 0);
    }

    /**
     * get the error value for the cell
     *
     * @return byte representing the error value
     */

    public byte getErrorValue()
    {
        return field_4_bBoolErr;
    }

    /**
     * Indicates whether the call holds a boolean value
     *
     * @return boolean true if the cell holds a boolean value
     */

    public boolean isBoolean()
    {
        return (field_5_fError == ( byte ) 0);
    }

    /**
     * manually indicate this is an error rather than a boolean
     */
    public void setError(boolean val) {
        field_5_fError = (byte) (val == false ? 0 : 1);
    }

    /**
     * Indicates whether the call holds an error value
     *
     * @return boolean true if the cell holds an error value
     */

    public boolean isError()
    {
        return (field_5_fError != ( byte ) 0);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[BOOLERR]\n");
        buffer.append("    .row            = ")
            .append(Integer.toHexString(getRow())).append("\n");
        buffer.append("    .col            = ")
            .append(Integer.toHexString(getColumn())).append("\n");
        buffer.append("    .xfindex        = ")
            .append(Integer.toHexString(getXFIndex())).append("\n");
        if (isBoolean())
        {
            buffer.append("    .booleanValue   = ").append(getBooleanValue())
                .append("\n");
        }
        else
        {
            buffer.append("    .errorValue     = ").append(getErrorValue())
                .append("\n");
        }
        buffer.append("[/BOOLERR]\n");
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
        LittleEndian.putShort(data, 2 + offset, ( short ) 8);
        //LittleEndian.putShort(data, 4 + offset, getRow());
        LittleEndian.putShort(data, 4 + offset, ( short ) getRow());
        LittleEndian.putShort(data, 6 + offset, getColumn());
        LittleEndian.putShort(data, 8 + offset, getXFIndex());
        data[ 10 + offset ] = field_4_bBoolErr;
        data[ 11 + offset ] = field_5_fError;
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 12;
    }

    /**
     * called by constructor, should throw runtime exception in the event of a
     * record passed with a differing ID.
     *
     * @param id alleged id for this record
     */

    protected void validateSid(short id)
    {
        if (id != BoolErrRecord.sid)
        {
            throw new RecordFormatException("Not a valid BoolErrRecord");
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
      BoolErrRecord rec = new BoolErrRecord();
      rec.field_1_row = field_1_row;
      rec.field_2_column = field_2_column;
      rec.field_3_xf_index = field_3_xf_index;
      rec.field_4_bBoolErr = field_4_bBoolErr;
      rec.field_5_fError = field_5_fError;
      return rec;
    }
}
