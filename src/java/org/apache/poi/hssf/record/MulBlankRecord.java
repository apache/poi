
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
 * MulBlankRecord.java
 *
 * Created on December 10, 2001, 12:49 PM
 */
package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;

/**
 * Title:        Mulitple Blank cell record <P>
 * Description:  Represents a  set of columns in a row with no value but with styling.
 *               In this release we have read-only support for this record type.
 *               The RecordFactory converts this to a set of BlankRecord objects.<P>
 * REFERENCE:  PG 329 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 * @version 2.0-pre
 * @see org.apache.poi.hssf.record.RecordFactory
 * @see org.apache.poi.hssf.record.BlankRecord
 */

public class MulBlankRecord
    extends Record
{
    public final static short sid = 0xbe;
    //private short             field_1_row;
    private int             field_1_row;
    private short             field_2_first_col;
    private short[]           field_3_xfs;
    private short             field_4_last_col;

    /** Creates new MulBlankRecord */

    public MulBlankRecord()
    {
    }

    /**
     * Constructs a MulBlank record and sets its fields appropriately.
     *
     * @param id     id must be 0xbe or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public MulBlankRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a MulBlank record and sets its fields appropriately.
     *
     * @param id     id must be 0xbe or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public MulBlankRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    /**
     * get the row number of the cells this represents
     *
     * @return row number
     */

    //public short getRow()
    public int getRow()
    {
        return field_1_row;
    }

    /**
     * starting column (first cell this holds in the row)
     * @return first column number
     */

    public short getFirstColumn()
    {
        return field_2_first_col;
    }

    /**
     * ending column (last cell this holds in the row)
     * @return first column number
     */

    public short getLastColumn()
    {
        return field_4_last_col;
    }

    /**
     * get the number of columns this contains (last-first +1)
     * @return number of columns (last - first +1)
     */

    public int getNumColumns()
    {
        return field_4_last_col - field_2_first_col + 1;
    }

    /**
     * returns the xf index for column (coffset = column - field_2_first_col)
     * @param coffset  the column (coffset = column - field_2_first_col)
     * @return the XF index for the column
     */

    public short getXFAt(int coffset)
    {
        return field_3_xfs[ coffset ];
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
        //field_1_row       = LittleEndian.getShort(data, 0 + offset);
        field_1_row       = LittleEndian.getUShort(data, 0 + offset);
        field_2_first_col = LittleEndian.getShort(data, 2 + offset);
        field_3_xfs       = parseXFs(data, 4, offset, size);
        field_4_last_col  = LittleEndian.getShort(data,
                                                  (field_3_xfs.length * 2)
                                                  + 4 + offset);
    }

    private short [] parseXFs(byte [] data, int offset, int recoffset,
                              short size)
    {
        short[] retval = new short[ ((size - offset) - 2) / 2 ];
        int     idx    = 0;

        for (; offset < size - 2; )
        {
            short xf = 0;

            xf            = LittleEndian.getShort(data, offset + recoffset);
            offset        += 2;
            retval[ idx ] = xf;
            idx++;
        }
        return retval;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[MULBLANK]\n");
        buffer.append("row  = ")
            .append(Integer.toHexString(getRow())).append("\n");
        buffer.append("firstcol  = ")
            .append(Integer.toHexString(getFirstColumn())).append("\n");
        buffer.append(" lastcol  = ")
            .append(Integer.toHexString(getLastColumn())).append("\n");
        for (int k = 0; k < getNumColumns(); k++)
        {
            buffer.append("xf").append(k).append("        = ")
                .append(Integer.toHexString(getXFAt(k))).append("\n");
        }
        buffer.append("[/MULBLANK]\n");
        return buffer.toString();
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
            throw new RecordFormatException("Not a MulBlankRecord!");
        }
    }

    public short getSid()
    {
        return this.sid;
    }

    public int serialize(int offset, byte [] data)
    {
        throw new RecordFormatException(
            "Sorry, you can't serialize a MulBlank in this release");
    }
}
