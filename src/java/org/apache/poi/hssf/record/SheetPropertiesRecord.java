
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
 * Describes a chart sheet properties record.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class SheetPropertiesRecord
    extends Record
{
    public final static short      sid                             = 0x1044;
    private  short      field_1_flags;
    private BitField   chartTypeManuallyFormatted                 = new BitField(0x1);
    private BitField   plotVisibleOnly                            = new BitField(0x2);
    private BitField   doNotSizeWithWindow                        = new BitField(0x4);
    private BitField   defaultPlotDimensions                      = new BitField(0x8);
    private BitField   autoPlotArea                               = new BitField(0x10);
    private  byte       field_2_empty;
    public final static byte        EMPTY_NOT_PLOTTED              = 0;
    public final static byte        EMPTY_ZERO                     = 1;
    public final static byte        EMPTY_INTERPOLATED             = 2;


    public SheetPropertiesRecord()
    {

    }

    /**
     * Constructs a SheetProperties record and sets its fields appropriately.
     *
     * @param id    id must be 0x1044 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public SheetPropertiesRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a SheetProperties record and sets its fields appropriately.
     *
     * @param id    id must be 0x1044 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public SheetPropertiesRecord(short id, short size, byte [] data, int offset)
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
            throw new RecordFormatException("Not a SheetProperties record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_flags                   = LittleEndian.getShort(data, 0x0 + offset);
        field_2_empty                   = data[ 0x2 + offset ];

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[SheetProperties]\n");

        buffer.append("    .flags                = ")
            .append("0x")
            .append(HexDump.toHex((short)getFlags()))
            .append(" (").append(getFlags()).append(" )\n");
        buffer.append("         .chartTypeManuallyFormatted     = ").append(isChartTypeManuallyFormatted()).append('\n');
        buffer.append("         .plotVisibleOnly          = ").append(isPlotVisibleOnly     ()).append('\n');
        buffer.append("         .doNotSizeWithWindow      = ").append(isDoNotSizeWithWindow ()).append('\n');
        buffer.append("         .defaultPlotDimensions     = ").append(isDefaultPlotDimensions()).append('\n');
        buffer.append("         .autoPlotArea             = ").append(isAutoPlotArea        ()).append('\n');

        buffer.append("    .empty                = ")
            .append("0x")
            .append(HexDump.toHex((byte)getEmpty()))
            .append(" (").append(getEmpty()).append(" )\n");

        buffer.append("[/SheetProperties]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset, field_1_flags);
        data[ 6 + offset ] = field_2_empty;

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4 + 2 + 1;
    }

    public short getSid()
    {
        return this.sid;
    }


    /**
     * Get the flags field for the SheetProperties record.
     */
    public short getFlags()
    {
        return field_1_flags;
    }

    /**
     * Set the flags field for the SheetProperties record.
     */
    public void setFlags(short field_1_flags)
    {
        this.field_1_flags = field_1_flags;
    }

    /**
     * Get the empty field for the SheetProperties record.
     *
     * @return  One of 
     *        EMPTY_NOT_PLOTTED
     *        EMPTY_ZERO
     *        EMPTY_INTERPOLATED
     */
    public byte getEmpty()
    {
        return field_2_empty;
    }

    /**
     * Set the empty field for the SheetProperties record.
     *
     * @param field_2_empty
     *        One of 
     *        EMPTY_NOT_PLOTTED
     *        EMPTY_ZERO
     *        EMPTY_INTERPOLATED
     */
    public void setEmpty(byte field_2_empty)
    {
        this.field_2_empty = field_2_empty;
    }

    /**
     * Sets the chart type manually formatted field value.
     * Has the chart type been manually formatted?
     */
    public void setChartTypeManuallyFormatted(boolean value)
    {
        field_1_flags = chartTypeManuallyFormatted.setShortBoolean(field_1_flags, value);
    }

    /**
     * Has the chart type been manually formatted?
     * @return  the chart type manually formatted field value.
     */
    public boolean isChartTypeManuallyFormatted()
    {
        return chartTypeManuallyFormatted.isSet(field_1_flags);
    }

    /**
     * Sets the plot visible only field value.
     * Only show visible cells on the chart.
     */
    public void setPlotVisibleOnly(boolean value)
    {
        field_1_flags = plotVisibleOnly.setShortBoolean(field_1_flags, value);
    }

    /**
     * Only show visible cells on the chart.
     * @return  the plot visible only field value.
     */
    public boolean isPlotVisibleOnly()
    {
        return plotVisibleOnly.isSet(field_1_flags);
    }

    /**
     * Sets the do not size with window field value.
     * Do not size the chart when the window changes size
     */
    public void setDoNotSizeWithWindow(boolean value)
    {
        field_1_flags = doNotSizeWithWindow.setShortBoolean(field_1_flags, value);
    }

    /**
     * Do not size the chart when the window changes size
     * @return  the do not size with window field value.
     */
    public boolean isDoNotSizeWithWindow()
    {
        return doNotSizeWithWindow.isSet(field_1_flags);
    }

    /**
     * Sets the default plot dimensions field value.
     * Indicates that the default area dimensions should be used.
     */
    public void setDefaultPlotDimensions(boolean value)
    {
        field_1_flags = defaultPlotDimensions.setShortBoolean(field_1_flags, value);
    }

    /**
     * Indicates that the default area dimensions should be used.
     * @return  the default plot dimensions field value.
     */
    public boolean isDefaultPlotDimensions()
    {
        return defaultPlotDimensions.isSet(field_1_flags);
    }

    /**
     * Sets the auto plot area field value.
     * ??
     */
    public void setAutoPlotArea(boolean value)
    {
        field_1_flags = autoPlotArea.setShortBoolean(field_1_flags, value);
    }

    /**
     * ??
     * @return  the auto plot area field value.
     */
    public boolean isAutoPlotArea()
    {
        return autoPlotArea.isSet(field_1_flags);
    }


}  // END OF CLASS




