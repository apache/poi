
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
 * The bar record is used to define a bar chart.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class BarRecord
    extends Record
{
    public final static short      sid                             = 0x1017;
    private  short      field_1_barSpace;
    private  short      field_2_categorySpace;
    private  short      field_3_formatFlags;
    private BitField   horizontal                                 = new BitField(0x1);
    private BitField   stacked                                    = new BitField(0x2);
    private BitField   displayAsPercentage                        = new BitField(0x4);
    private BitField   shadow                                     = new BitField(0x8);


    public BarRecord()
    {
        field_2_categorySpace = 50;

    }

    /**
     * Constructs a Bar record and sets its fields appropriately.
     *
     * @param id    id must be 0x1017 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public BarRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a Bar record and sets its fields appropriately.
     *
     * @param id    id must be 0x1017 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public BarRecord(short id, short size, byte [] data, int offset)
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
            throw new RecordFormatException("Not a Bar record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_barSpace                = LittleEndian.getShort(data, 0x0 + offset);
        field_2_categorySpace           = LittleEndian.getShort(data, 0x2 + offset);
        field_3_formatFlags             = LittleEndian.getShort(data, 0x4 + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[Bar]\n");

        buffer.append("    .barSpace             = ")
            .append("0x")
            .append(HexDump.toHex((short)getBarSpace()))
            .append(" (").append(getBarSpace()).append(" )\n");

        buffer.append("    .categorySpace        = ")
            .append("0x")
            .append(HexDump.toHex((short)getCategorySpace()))
            .append(" (").append(getCategorySpace()).append(" )\n");

        buffer.append("    .formatFlags          = ")
            .append("0x")
            .append(HexDump.toHex((short)getFormatFlags()))
            .append(" (").append(getFormatFlags()).append(" )\n");
        buffer.append("         .horizontal               = ").append(isHorizontal          ()).append('\n');
        buffer.append("         .stacked                  = ").append(isStacked             ()).append('\n');
        buffer.append("         .displayAsPercentage      = ").append(isDisplayAsPercentage ()).append('\n');
        buffer.append("         .shadow                   = ").append(isShadow              ()).append('\n');

        buffer.append("[/Bar]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset, field_1_barSpace);
        LittleEndian.putShort(data, 6 + offset, field_2_categorySpace);
        LittleEndian.putShort(data, 8 + offset, field_3_formatFlags);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4 + 2 + 2 + 2;
    }

    public short getSid()
    {
        return this.sid;
    }


    /**
     * Get the bar space field for the Bar record.
     */
    public short getBarSpace()
    {
        return field_1_barSpace;
    }

    /**
     * Set the bar space field for the Bar record.
     */
    public void setBarSpace(short field_1_barSpace)
    {
        this.field_1_barSpace = field_1_barSpace;
    }

    /**
     * Get the category space field for the Bar record.
     */
    public short getCategorySpace()
    {
        return field_2_categorySpace;
    }

    /**
     * Set the category space field for the Bar record.
     */
    public void setCategorySpace(short field_2_categorySpace)
    {
        this.field_2_categorySpace = field_2_categorySpace;
    }

    /**
     * Get the format flags field for the Bar record.
     */
    public short getFormatFlags()
    {
        return field_3_formatFlags;
    }

    /**
     * Set the format flags field for the Bar record.
     */
    public void setFormatFlags(short field_3_formatFlags)
    {
        this.field_3_formatFlags = field_3_formatFlags;
    }

    /**
     * Sets the horizontal field value.
     * true to display horizontal bar charts, false for vertical
     */
    public void setHorizontal(boolean value)
    {
        field_3_formatFlags = horizontal.setShortBoolean(field_3_formatFlags, value);
    }

    /**
     * true to display horizontal bar charts, false for vertical
     * @return  the horizontal field value.
     */
    public boolean isHorizontal()
    {
        return horizontal.isSet(field_3_formatFlags);
    }

    /**
     * Sets the stacked field value.
     * stack displayed values
     */
    public void setStacked(boolean value)
    {
        field_3_formatFlags = stacked.setShortBoolean(field_3_formatFlags, value);
    }

    /**
     * stack displayed values
     * @return  the stacked field value.
     */
    public boolean isStacked()
    {
        return stacked.isSet(field_3_formatFlags);
    }

    /**
     * Sets the display as percentage field value.
     * display chart values as a percentage
     */
    public void setDisplayAsPercentage(boolean value)
    {
        field_3_formatFlags = displayAsPercentage.setShortBoolean(field_3_formatFlags, value);
    }

    /**
     * display chart values as a percentage
     * @return  the display as percentage field value.
     */
    public boolean isDisplayAsPercentage()
    {
        return displayAsPercentage.isSet(field_3_formatFlags);
    }

    /**
     * Sets the shadow field value.
     * display a shadow for the chart
     */
    public void setShadow(boolean value)
    {
        field_3_formatFlags = shadow.setShortBoolean(field_3_formatFlags, value);
    }

    /**
     * display a shadow for the chart
     * @return  the shadow field value.
     */
    public boolean isShadow()
    {
        return shadow.isSet(field_3_formatFlags);
    }


}  // END OF CLASS




