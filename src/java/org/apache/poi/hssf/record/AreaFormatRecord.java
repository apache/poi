
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
import org.apache.poi.util.StringUtil;
import org.apache.poi.util.HexDump;

/**
 * The area format record is used to define the colours and patterns for an area.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class AreaFormatRecord
    extends Record
{
    public final static short      sid                             = 0x100a;
    private  int        field_1_foregroundColor;
    private  int        field_2_backgroundColor;
    private  short      field_3_pattern;
    private  short      field_4_formatFlags;
    private BitField   automatic                                  = new BitField(0x1);
    private BitField   invert                                     = new BitField(0x2);
    private  short      field_5_forecolorIndex;
    private  short      field_6_backcolorIndex;


    public AreaFormatRecord()
    {

    }

    /**
     * Constructs a AreaFormat record and sets its fields appropriately.
     *
     * @param id    id must be 0x100a or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public AreaFormatRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a AreaFormat record and sets its fields appropriately.
     *
     * @param id    id must be 0x100a or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public AreaFormatRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    /**
     * Checks the sid matches the expected side for this record
     *
     * @param id   the expected sid.
     */
    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("Not a AreaFormat record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_foregroundColor         = LittleEndian.getInt(data, 0x0 + offset);
        field_2_backgroundColor         = LittleEndian.getInt(data, 0x4 + offset);
        field_3_pattern                 = LittleEndian.getShort(data, 0x8 + offset);
        field_4_formatFlags             = LittleEndian.getShort(data, 0xa + offset);
        field_5_forecolorIndex          = LittleEndian.getShort(data, 0xc + offset);
        field_6_backcolorIndex          = LittleEndian.getShort(data, 0xe + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[AreaFormat]\n");

        buffer.append("    .foregroundColor      = ")
            .append("0x")
            .append(HexDump.toHex((int)getForegroundColor()))
            .append(" (").append(getForegroundColor()).append(" )\n");

        buffer.append("    .backgroundColor      = ")
            .append("0x")
            .append(HexDump.toHex((int)getBackgroundColor()))
            .append(" (").append(getBackgroundColor()).append(" )\n");

        buffer.append("    .pattern              = ")
            .append("0x")
            .append(HexDump.toHex((short)getPattern()))
            .append(" (").append(getPattern()).append(" )\n");

        buffer.append("    .formatFlags          = ")
            .append("0x")
            .append(HexDump.toHex((short)getFormatFlags()))
            .append(" (").append(getFormatFlags()).append(" )\n");
        buffer.append("         .automatic                = ").append(isAutomatic           ()).append('\n');
        buffer.append("         .invert                   = ").append(isInvert              ()).append('\n');

        buffer.append("    .forecolorIndex       = ")
            .append("0x")
            .append(HexDump.toHex((short)getForecolorIndex()))
            .append(" (").append(getForecolorIndex()).append(" )\n");

        buffer.append("    .backcolorIndex       = ")
            .append("0x")
            .append(HexDump.toHex((short)getBackcolorIndex()))
            .append(" (").append(getBackcolorIndex()).append(" )\n");

        buffer.append("[/AreaFormat]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putInt(data, 4 + offset, field_1_foregroundColor);
        LittleEndian.putInt(data, 8 + offset, field_2_backgroundColor);
        LittleEndian.putShort(data, 12 + offset, field_3_pattern);
        LittleEndian.putShort(data, 14 + offset, field_4_formatFlags);
        LittleEndian.putShort(data, 16 + offset, field_5_forecolorIndex);
        LittleEndian.putShort(data, 18 + offset, field_6_backcolorIndex);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4 + 4 + 4 + 2 + 2 + 2 + 2;
    }

    public short getSid()
    {
        return this.sid;
    }


    /**
     * Get the foreground color field for the AreaFormat record.
     */
    public int getForegroundColor()
    {
        return field_1_foregroundColor;
    }

    /**
     * Set the foreground color field for the AreaFormat record.
     */
    public void setForegroundColor(int field_1_foregroundColor)
    {
        this.field_1_foregroundColor = field_1_foregroundColor;
    }

    /**
     * Get the background color field for the AreaFormat record.
     */
    public int getBackgroundColor()
    {
        return field_2_backgroundColor;
    }

    /**
     * Set the background color field for the AreaFormat record.
     */
    public void setBackgroundColor(int field_2_backgroundColor)
    {
        this.field_2_backgroundColor = field_2_backgroundColor;
    }

    /**
     * Get the pattern field for the AreaFormat record.
     */
    public short getPattern()
    {
        return field_3_pattern;
    }

    /**
     * Set the pattern field for the AreaFormat record.
     */
    public void setPattern(short field_3_pattern)
    {
        this.field_3_pattern = field_3_pattern;
    }

    /**
     * Get the format flags field for the AreaFormat record.
     */
    public short getFormatFlags()
    {
        return field_4_formatFlags;
    }

    /**
     * Set the format flags field for the AreaFormat record.
     */
    public void setFormatFlags(short field_4_formatFlags)
    {
        this.field_4_formatFlags = field_4_formatFlags;
    }

    /**
     * Get the forecolor index field for the AreaFormat record.
     */
    public short getForecolorIndex()
    {
        return field_5_forecolorIndex;
    }

    /**
     * Set the forecolor index field for the AreaFormat record.
     */
    public void setForecolorIndex(short field_5_forecolorIndex)
    {
        this.field_5_forecolorIndex = field_5_forecolorIndex;
    }

    /**
     * Get the backcolor index field for the AreaFormat record.
     */
    public short getBackcolorIndex()
    {
        return field_6_backcolorIndex;
    }

    /**
     * Set the backcolor index field for the AreaFormat record.
     */
    public void setBackcolorIndex(short field_6_backcolorIndex)
    {
        this.field_6_backcolorIndex = field_6_backcolorIndex;
    }

    /**
     * Sets the automatic field value.
     * automatic formatting
     */
    public void setAutomatic(boolean value)
    {
        field_4_formatFlags = automatic.setShortBoolean(field_4_formatFlags, value);
    }

    /**
     * automatic formatting
     * @return  the automatic field value.
     */
    public boolean isAutomatic()
    {
        return automatic.isSet(field_4_formatFlags);
    }

    /**
     * Sets the invert field value.
     * swap foreground and background colours when data is negative
     */
    public void setInvert(boolean value)
    {
        field_4_formatFlags = invert.setShortBoolean(field_4_formatFlags, value);
    }

    /**
     * swap foreground and background colours when data is negative
     * @return  the invert field value.
     */
    public boolean isInvert()
    {
        return invert.isSet(field_4_formatFlags);
    }


}  // END OF CLASS




