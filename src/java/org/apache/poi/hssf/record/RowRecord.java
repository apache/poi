
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

import org.apache.poi.util.BitField;
import org.apache.poi.util.LittleEndian;

/**
 * Title:        Row Record<P>
 * Description:  stores the row information for the sheet. <P>
 * REFERENCE:  PG 379 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class RowRecord
    extends Record
    implements Comparable
{
    public final static short sid = 0x208;
    private short             field_1_row_number;
    private short             field_2_first_col;
    private short             field_3_last_col;   // plus 1
    private short             field_4_height;
    private short             field_5_optimize;   // hint field for gui, can/should be set to zero

    // for generated sheets.
    private short             field_6_reserved;
    private short             field_7_option_flags;
    private BitField          outlineLevel  = new BitField(0x07);

    // bit 3 reserved
    private BitField          colapsed      = new BitField(0x10);
    private BitField          zeroHeight    = new BitField(0x20);
    private BitField          badFontHeight = new BitField(0x40);
    private BitField          formatted     = new BitField(0x80);
    private short             field_8_xf_index;   // only if isFormatted

    public RowRecord()
    {
    }

    /**
     * Constructs a Row record and sets its fields appropriately.
     *
     * @param id     id must be 0x208 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public RowRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a Row record and sets its fields appropriately.
     *
     * @param id     id must be 0x208 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record data
     */

    public RowRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A valid ROW RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_row_number   = LittleEndian.getShort(data, 0 + offset);
        field_2_first_col    = LittleEndian.getShort(data, 2 + offset);
        field_3_last_col     = LittleEndian.getShort(data, 4 + offset);
        field_4_height       = LittleEndian.getShort(data, 6 + offset);
        field_5_optimize     = LittleEndian.getShort(data, 8 + offset);
        field_6_reserved     = LittleEndian.getShort(data, 10 + offset);
        field_7_option_flags = LittleEndian.getShort(data, 12 + offset);
        field_8_xf_index     = LittleEndian.getShort(data, 14 + offset);
    }

    /**
     * set the logical row number for this row (0 based index)
     * @param row - the row number
     */

    public void setRowNumber(short row)
    {
        field_1_row_number = row;
    }

    /**
     * set the logical col number for the first cell this row (0 based index)
     * @param col - the col number
     */

    public void setFirstCol(short col)
    {
        field_2_first_col = col;
    }

    /**
     * set the logical col number for the last cell this row (0 based index)
     * @param col - the col number
     */

    public void setLastCol(short col)
    {
        field_3_last_col = col;
    }

    /**
     * set the height of the row
     * @param height of the row
     */

    public void setHeight(short height)
    {
        field_4_height = height;
    }

    /**
     * set whether to optimize or not (set to 0)
     * @param optimize (set to 0)
     */

    public void setOptimize(short optimize)
    {
        field_5_optimize = optimize;
    }

    /**
     * sets the option bitmask.  (use the individual bit setters that refer to this
     * method)
     * @param options - the bitmask
     */

    public void setOptionFlags(short options)
    {
        field_7_option_flags = options;
    }

    // option bitfields

    /**
     * set the outline level of this row
     * @param ol - the outline level
     * @see #setOptionFlags(short)
     */

    public void setOutlineLevel(short ol)
    {
        field_7_option_flags =
            outlineLevel.setShortValue(field_7_option_flags, ol);
    }

    /**
     * set whether or not to colapse this row
     * @param c - colapse or not
     * @see #setOptionFlags(short)
     */

    public void setColapsed(boolean c)
    {
        field_7_option_flags = colapsed.setShortBoolean(field_7_option_flags,
                c);
    }

    /**
     * set whether or not to display this row with 0 height
     * @param z  height is zero or not.
     * @see #setOptionFlags(short)
     */

    public void setZeroHeight(boolean z)
    {
        field_7_option_flags =
            zeroHeight.setShortBoolean(field_7_option_flags, z);
    }

    /**
     * set whether the font and row height are not compatible
     * @param  f  true if they aren't compatible (damn not logic)
     * @see #setOptionFlags(short)
     */

    public void setBadFontHeight(boolean f)
    {
        field_7_option_flags =
            badFontHeight.setShortBoolean(field_7_option_flags, f);
    }

    /**
     * set whether the row has been formatted (even if its got all blank cells)
     * @param f  formatted or not
     * @see #setOptionFlags(short)
     */

    public void setFormatted(boolean f)
    {
        field_7_option_flags = formatted.setShortBoolean(field_7_option_flags,
                f);
    }

    // end bitfields

    /**
     * if the row is formatted then this is the index to the extended format record
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     * @param index to the XF record
     */

    public void setXFIndex(short index)
    {
        field_8_xf_index = index;
    }

    /**
     * get the logical row number for this row (0 based index)
     * @return row - the row number
     */

    public short getRowNumber()
    {
        return field_1_row_number;
    }

    /**
     * get the logical col number for the first cell this row (0 based index)
     * @return col - the col number
     */

    public short getFirstCol()
    {
        return field_2_first_col;
    }

    /**
     * get the logical col number for the last cell this row (0 based index)
     * @return col - the col number
     */

    public short getLastCol()
    {
        return field_3_last_col;
    }

    /**
     * get the height of the row
     * @return height of the row
     */

    public short getHeight()
    {
        return field_4_height;
    }

    /**
     * get whether to optimize or not (set to 0)
     * @return optimize (set to 0)
     */

    public short getOptimize()
    {
        return field_5_optimize;
    }

    /**
     * gets the option bitmask.  (use the individual bit setters that refer to this
     * method)
     * @return options - the bitmask
     */

    public short getOptionFlags()
    {
        return field_7_option_flags;
    }

    // option bitfields

    /**
     * get the outline level of this row
     * @return ol - the outline level
     * @see #getOptionFlags()
     */

    public short getOutlineLevel()
    {
        return outlineLevel.getShortValue(field_7_option_flags);
    }

    /**
     * get whether or not to colapse this row
     * @return c - colapse or not
     * @see #getOptionFlags()
     */

    public boolean getColapsed()
    {
        return (colapsed.isSet(field_7_option_flags));
    }

    /**
     * get whether or not to display this row with 0 height
     * @return - z height is zero or not.
     * @see #getOptionFlags()
     */

    public boolean getZeroHeight()
    {
        return zeroHeight.isSet(field_7_option_flags);
    }

    /**
     * get whether the font and row height are not compatible
     * @return - f -true if they aren't compatible (damn not logic)
     * @see #getOptionFlags()
     */

    public boolean getBadFontHeight()
    {
        return badFontHeight.isSet(field_7_option_flags);
    }

    /**
     * get whether the row has been formatted (even if its got all blank cells)
     * @return formatted or not
     * @see #getOptionFlags()
     */

    public boolean getFormatted()
    {
        return formatted.isSet(field_7_option_flags);
    }

    // end bitfields

    /**
     * if the row is formatted then this is the index to the extended format record
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     * @return index to the XF record or bogus value (undefined) if isn't formatted
     */

    public short getXFIndex()
    {
        return field_8_xf_index;
    }

    public boolean isInValueSection()
    {
        return true;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[ROW]\n");
        buffer.append("    .rownumber      = ")
            .append(Integer.toHexString(getRowNumber())).append("\n");
        buffer.append("    .firstcol       = ")
            .append(Integer.toHexString(getFirstCol())).append("\n");
        buffer.append("    .lastcol        = ")
            .append(Integer.toHexString(getLastCol())).append("\n");
        buffer.append("    .height         = ")
            .append(Integer.toHexString(getHeight())).append("\n");
        buffer.append("    .optimize       = ")
            .append(Integer.toHexString(getOptimize())).append("\n");
        buffer.append("    .reserved       = ")
            .append(Integer.toHexString(field_6_reserved)).append("\n");
        buffer.append("    .optionflags    = ")
            .append(Integer.toHexString(getOptionFlags())).append("\n");
        buffer.append("        .outlinelvl = ")
            .append(Integer.toHexString(getOutlineLevel())).append("\n");
        buffer.append("        .colapsed   = ").append(getColapsed())
            .append("\n");
        buffer.append("        .zeroheight = ").append(getZeroHeight())
            .append("\n");
        buffer.append("        .badfontheig= ").append(getBadFontHeight())
            .append("\n");
        buffer.append("        .formatted  = ").append(getFormatted())
            .append("\n");
        buffer.append("    .xfindex        = ")
            .append(Integer.toHexString(getXFIndex())).append("\n");
        buffer.append("[/ROW]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) 16);
        LittleEndian.putShort(data, 4 + offset, getRowNumber());
        LittleEndian.putShort(data, 6 + offset, getFirstCol() == -1 ? (short)0 : getFirstCol());
        LittleEndian.putShort(data, 8 + offset, getLastCol() == -1 ? (short)0 : getLastCol());
        LittleEndian.putShort(data, 10 + offset, getHeight());
        LittleEndian.putShort(data, 12 + offset, getOptimize());
        LittleEndian.putShort(data, 14 + offset, field_6_reserved);
        LittleEndian.putShort(data, 16 + offset, getOptionFlags());

//    LittleEndian.putShort(data,18,getOutlineLevel());
        LittleEndian.putShort(data, 18 + offset, getXFIndex());
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 20;
    }

    public short getSid()
    {
        return this.sid;
    }

    public int compareTo(Object obj)
    {
        RowRecord loc = ( RowRecord ) obj;

        if (this.getRowNumber() == loc.getRowNumber())
        {
            return 0;
        }
        if (this.getRowNumber() < loc.getRowNumber())
        {
            return -1;
        }
        if (this.getRowNumber() > loc.getRowNumber())
        {
            return 1;
        }
        return -1;
    }

    public boolean equals(Object obj)
    {
        if (!(obj instanceof RowRecord))
        {
            return false;
        }
        RowRecord loc = ( RowRecord ) obj;

        if (this.getRowNumber() == loc.getRowNumber())
        {
            return true;
        }
        return false;
    }
}
