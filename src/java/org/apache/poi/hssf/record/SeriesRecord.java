
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

import org.apache.poi.util.LittleEndian;

/**
 * The series record defines the (graphing) series within a chart.
 * This record is matched with a corresponding EndRecord.
 *
 * @author Glen Stampoultzis (gstamp at iprimus dot com dot au)
 */

public class SeriesRecord
    extends Record
{
    public static final short sid                = 0x1003;
    public static final short AXIS_TYPE_DATE     = 0;
    public static final short AXIS_TYPE_NUMERIC  = 1;
    public static final short AXIS_TYPE_SEQUENCE = 3;
    public static final short AXIS_TYPE_TEXT     = 4;
    private short             field_1_xAxisType;
    private short             field_2_yAxisType;
    private short             field_3_countOfXValues;
    private short             field_4_countOfYValues;
    private short             field_5_bubbleType;            // type of data in "bubble size series"
    private short             field_6_countOfBubbleSeries;   // count of bubble series values

    public SeriesRecord()
    {
    }

    /**
     * Constructs a SeriesRecord record and sets its fields appropriately.
     *
     * @param short id must be 0x1003 or an exception will be throw upon validation
     * @param short size the size of the data area of the record
     * @param byte[] data of the record (should not contain sid/len)
     */

    public SeriesRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a SeriesRecord record and sets its fields appropriately.
     *
     * @param short id must be 0x1003 or an exception will be throw upon validation
     * @param short size the size of the data area of the record
     * @param byte[] data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public SeriesRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A SERIES RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_xAxisType           = LittleEndian.getShort(data, 0 + offset);
        field_2_yAxisType           = LittleEndian.getShort(data, 2 + offset);
        field_3_countOfXValues      = LittleEndian.getShort(data, 4 + offset);
        field_4_countOfYValues      = LittleEndian.getShort(data, 6 + offset);
        field_5_bubbleType          = LittleEndian.getShort(data, 8 + offset);
        field_6_countOfBubbleSeries = LittleEndian.getShort(data,
                10 + offset);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[SERIES]\n");
        buffer.append("    .xAxisType       = ")
            .append(Integer.toHexString(getXAxisType())).append("\n");
        buffer.append("    .yAxisType       = ")
            .append(Integer.toHexString(getYAxisType())).append("\n");
        buffer.append("    .countOfXValues  = ").append(getCountOfXValues())
            .append("\n");
        buffer.append("    .countOfYValues  = ").append(getCountOfYValues())
            .append("\n");
        buffer.append("[/SERIES]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              (( short ) 12));   // 12 byte length
        LittleEndian.putShort(data, 4 + offset, getXAxisType());
        LittleEndian.putShort(data, 6 + offset, getYAxisType());
        LittleEndian.putShort(data, 8 + offset, getCountOfXValues());
        LittleEndian.putShort(data, 10 + offset, getCountOfYValues());
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 12;
    }

    public short getSid()
    {
        return this.sid;
    }

    /**
     * @return one of AXIS_TYPE_XXX
     */

    public short getXAxisType()
    {
        return field_1_xAxisType;
    }

    /**
     * @param xAxisType one of AXIS_TYPE_XXX
     */

    public void setXAxisType(short xAxisType)
    {
        this.field_1_xAxisType = xAxisType;
    }

    /**
     * @return one of AXIS_TYPE_XXX
     */

    public short getYAxisType()
    {
        return field_2_yAxisType;
    }

    /**
     * @param xAxisType one of AXIS_TYPE_XXX
     */

    public void setYAxisType(short yAxisType)
    {
        this.field_2_yAxisType = yAxisType;
    }

    /**
     * @return number of x values in the series.
     */

    public short getCountOfXValues()
    {
        return field_3_countOfXValues;
    }

    /**
     * Sets the number of x values in the series.
     */

    public void setCountOfXValues(short countOfXValues)
    {
        this.field_3_countOfXValues = countOfXValues;
    }

    /**
     * @return number of y values in the series.
     */

    public short getCountOfYValues()
    {
        return field_4_countOfYValues;
    }

    /**
     * @param countOfYValues    sets the number of y values for the series.
     */

    public void setCountOfYValues(short countOfYValues)
    {
        this.field_4_countOfYValues = countOfYValues;
    }
}
