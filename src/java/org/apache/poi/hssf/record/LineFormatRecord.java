
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
 * Describes a line format record.  The line format record controls how a line on a chart appears.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class LineFormatRecord
    extends Record
{
    public final static short      sid                             = 0x1007;
    private  int        field_1_lineColor;
    private  short      field_2_linePattern;
    public final static short       LINE_PATTERN_SOLID             = 0;
    public final static short       LINE_PATTERN_DASH              = 1;
    public final static short       LINE_PATTERN_DOT               = 2;
    public final static short       LINE_PATTERN_DASH_DOT          = 3;
    public final static short       LINE_PATTERN_DASH_DOT_DOT      = 4;
    public final static short       LINE_PATTERN_NONE              = 5;
    public final static short       LINE_PATTERN_DARK_GRAY_PATTERN = 6;
    public final static short       LINE_PATTERN_MEDIUM_GRAY_PATTERN = 7;
    public final static short       LINE_PATTERN_LIGHT_GRAY_PATTERN = 8;
    private  short      field_3_weight;
    public final static short       WEIGHT_HAIRLINE                = -1;
    public final static short       WEIGHT_NARROW                  = 0;
    public final static short       WEIGHT_MEDIUM                  = 1;
    public final static short       WEIGHT_WIDE                    = 2;
    private  short      field_4_format;
    private BitField   auto                                       = new BitField(0x1);
    private BitField   drawTicks                                  = new BitField(0x4);
    private BitField   unknown                                    = new BitField(0x4);
    private  short      field_5_colourPaletteIndex;


    public LineFormatRecord()
    {

    }

    /**
     * Constructs a LineFormat record and sets its fields appropriately.
     *
     * @param id    id must be 0x1007 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public LineFormatRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a LineFormat record and sets its fields appropriately.
     *
     * @param id    id must be 0x1007 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public LineFormatRecord(short id, short size, byte [] data, int offset)
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
            throw new RecordFormatException("Not a LineFormat record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_lineColor               = LittleEndian.getInt(data, 0x0 + offset);
        field_2_linePattern             = LittleEndian.getShort(data, 0x4 + offset);
        field_3_weight                  = LittleEndian.getShort(data, 0x6 + offset);
        field_4_format                  = LittleEndian.getShort(data, 0x8 + offset);
        field_5_colourPaletteIndex      = LittleEndian.getShort(data, 0xa + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[LineFormat]\n");

        buffer.append("    .lineColor            = ")
            .append("0x")
            .append(HexDump.toHex((int)getLineColor()))
            .append(" (").append(getLineColor()).append(" )\n");

        buffer.append("    .linePattern          = ")
            .append("0x")
            .append(HexDump.toHex((short)getLinePattern()))
            .append(" (").append(getLinePattern()).append(" )\n");

        buffer.append("    .weight               = ")
            .append("0x")
            .append(HexDump.toHex((short)getWeight()))
            .append(" (").append(getWeight()).append(" )\n");

        buffer.append("    .format               = ")
            .append("0x")
            .append(HexDump.toHex((short)getFormat()))
            .append(" (").append(getFormat()).append(" )\n");
        buffer.append("         .auto                     = ").append(isAuto                ()).append('\n');
        buffer.append("         .drawTicks                = ").append(isDrawTicks           ()).append('\n');
        buffer.append("         .unknown                  = ").append(isUnknown             ()).append('\n');

        buffer.append("    .colourPaletteIndex   = ")
            .append("0x")
            .append(HexDump.toHex((short)getColourPaletteIndex()))
            .append(" (").append(getColourPaletteIndex()).append(" )\n");

        buffer.append("[/LineFormat]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putInt(data, 4 + offset, field_1_lineColor);
        LittleEndian.putShort(data, 8 + offset, field_2_linePattern);
        LittleEndian.putShort(data, 10 + offset, field_3_weight);
        LittleEndian.putShort(data, 12 + offset, field_4_format);
        LittleEndian.putShort(data, 14 + offset, field_5_colourPaletteIndex);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4 + 4 + 2 + 2 + 2 + 2;
    }

    public short getSid()
    {
        return this.sid;
    }


    /**
     * Get the line color field for the LineFormat record.
     */
    public int getLineColor()
    {
        return field_1_lineColor;
    }

    /**
     * Set the line color field for the LineFormat record.
     */
    public void setLineColor(int field_1_lineColor)
    {
        this.field_1_lineColor = field_1_lineColor;
    }

    /**
     * Get the line pattern field for the LineFormat record.
     *
     * @return  One of 
     *        LINE_PATTERN_SOLID
     *        LINE_PATTERN_DASH
     *        LINE_PATTERN_DOT
     *        LINE_PATTERN_DASH_DOT
     *        LINE_PATTERN_DASH_DOT_DOT
     *        LINE_PATTERN_NONE
     *        LINE_PATTERN_DARK_GRAY_PATTERN
     *        LINE_PATTERN_MEDIUM_GRAY_PATTERN
     *        LINE_PATTERN_LIGHT_GRAY_PATTERN
     */
    public short getLinePattern()
    {
        return field_2_linePattern;
    }

    /**
     * Set the line pattern field for the LineFormat record.
     *
     * @param field_2_linePattern
     *        One of 
     *        LINE_PATTERN_SOLID
     *        LINE_PATTERN_DASH
     *        LINE_PATTERN_DOT
     *        LINE_PATTERN_DASH_DOT
     *        LINE_PATTERN_DASH_DOT_DOT
     *        LINE_PATTERN_NONE
     *        LINE_PATTERN_DARK_GRAY_PATTERN
     *        LINE_PATTERN_MEDIUM_GRAY_PATTERN
     *        LINE_PATTERN_LIGHT_GRAY_PATTERN
     */
    public void setLinePattern(short field_2_linePattern)
    {
        this.field_2_linePattern = field_2_linePattern;
    }

    /**
     * Get the weight field for the LineFormat record.
     *
     * @return  One of 
     *        WEIGHT_HAIRLINE
     *        WEIGHT_NARROW
     *        WEIGHT_MEDIUM
     *        WEIGHT_WIDE
     */
    public short getWeight()
    {
        return field_3_weight;
    }

    /**
     * Set the weight field for the LineFormat record.
     *
     * @param field_3_weight
     *        One of 
     *        WEIGHT_HAIRLINE
     *        WEIGHT_NARROW
     *        WEIGHT_MEDIUM
     *        WEIGHT_WIDE
     */
    public void setWeight(short field_3_weight)
    {
        this.field_3_weight = field_3_weight;
    }

    /**
     * Get the format field for the LineFormat record.
     */
    public short getFormat()
    {
        return field_4_format;
    }

    /**
     * Set the format field for the LineFormat record.
     */
    public void setFormat(short field_4_format)
    {
        this.field_4_format = field_4_format;
    }

    /**
     * Get the colour palette index field for the LineFormat record.
     */
    public short getColourPaletteIndex()
    {
        return field_5_colourPaletteIndex;
    }

    /**
     * Set the colour palette index field for the LineFormat record.
     */
    public void setColourPaletteIndex(short field_5_colourPaletteIndex)
    {
        this.field_5_colourPaletteIndex = field_5_colourPaletteIndex;
    }

    /**
     * Sets the auto field value.
     * automatic format
     */
    public void setAuto(boolean value)
    {
        field_4_format = auto.setShortBoolean(field_4_format, value);
    }

    /**
     * automatic format
     * @return  the auto field value.
     */
    public boolean isAuto()
    {
        return auto.isSet(field_4_format);
    }

    /**
     * Sets the draw ticks field value.
     * draw tick marks
     */
    public void setDrawTicks(boolean value)
    {
        field_4_format = drawTicks.setShortBoolean(field_4_format, value);
    }

    /**
     * draw tick marks
     * @return  the draw ticks field value.
     */
    public boolean isDrawTicks()
    {
        return drawTicks.isSet(field_4_format);
    }

    /**
     * Sets the unknown field value.
     * book marks this as reserved = 0 but it seems to do something
     */
    public void setUnknown(boolean value)
    {
        field_4_format = unknown.setShortBoolean(field_4_format, value);
    }

    /**
     * book marks this as reserved = 0 but it seems to do something
     * @return  the unknown field value.
     */
    public boolean isUnknown()
    {
        return unknown.isSet(field_4_format);
    }


}  // END OF CLASS




