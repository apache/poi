
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
 * @version 2.0-pre
 */

public class BoolErrRecord
    extends Record
    implements CellValueRecordInterface, Comparable
{
    public final static short sid = 0x205;
    private short             field_1_row;
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
     * @param id     id must be 0x205 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public BoolErrRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a BoolErr record and sets its fields appropriately.
     *
     * @param id     id must be 0x205 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record
     */

    public BoolErrRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    /**
     * called by the constructor, should set class level fields.  Should throw
     * runtime exception for bad/icomplete data.
     *
     * @param data raw data
     * @param size size of data
     */

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_row      = LittleEndian.getShort(data, 0 + offset);
        field_2_column   = LittleEndian.getShort(data, 2 + offset);
        field_3_xf_index = LittleEndian.getShort(data, 4 + offset);
        field_4_bBoolErr = data[ 6 + offset ];
        field_5_fError   = data[ 7 + offset ];
    }

    public void setRow(short row)
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
     */

    public void setValue(byte value)
    {
        field_4_bBoolErr = value;
        field_5_fError   = ( byte ) 1;
    }

    public short getRow()
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
        LittleEndian.putShort(data, 4 + offset, getRow());
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
        if (id != this.sid)
        {
            throw new RecordFormatException("Not a valid BoolErrRecord");
        }
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
}
