
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
 * The series record describes the overall data for a series.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class SeriesRecord
    extends Record
{
    public final static short      sid                             = 0x1003;
    private  short      field_1_categoryDataType;
    public final static short       CATEGORY_DATA_TYPE_DATES       = 0;
    public final static short       CATEGORY_DATA_TYPE_NUMERIC     = 1;
    public final static short       CATEGORY_DATA_TYPE_SEQUENCE    = 2;
    public final static short       CATEGORY_DATA_TYPE_TEXT        = 3;
    private  short      field_2_valuesDataType;
    public final static short       VALUES_DATA_TYPE_DATES         = 0;
    public final static short       VALUES_DATA_TYPE_NUMERIC       = 1;
    public final static short       VALUES_DATA_TYPE_SEQUENCE      = 2;
    public final static short       VALUES_DATA_TYPE_TEXT          = 3;
    private  short      field_3_numCategories;
    private  short      field_4_numValues;
    private  short      field_5_bubbleSeriesType;
    public final static short       BUBBLE_SERIES_TYPE_DATES       = 0;
    public final static short       BUBBLE_SERIES_TYPE_NUMERIC     = 1;
    public final static short       BUBBLE_SERIES_TYPE_SEQUENCE    = 2;
    public final static short       BUBBLE_SERIES_TYPE_TEXT        = 3;
    private  short      field_6_numBubbleValues;


    public SeriesRecord()
    {

    }

    /**
     * Constructs a Series record and sets its fields appropriately.
     *
     * @param id    id must be 0x1003 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public SeriesRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a Series record and sets its fields appropriately.
     *
     * @param id    id must be 0x1003 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public SeriesRecord(short id, short size, byte [] data, int offset)
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
            throw new RecordFormatException("Not a Series record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_categoryDataType        = LittleEndian.getShort(data, 0x0 + offset);
        field_2_valuesDataType          = LittleEndian.getShort(data, 0x2 + offset);
        field_3_numCategories           = LittleEndian.getShort(data, 0x4 + offset);
        field_4_numValues               = LittleEndian.getShort(data, 0x6 + offset);
        field_5_bubbleSeriesType        = LittleEndian.getShort(data, 0x8 + offset);
        field_6_numBubbleValues         = LittleEndian.getShort(data, 0xa + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[Series]\n");

        buffer.append("    .categoryDataType     = ")
            .append("0x")
            .append(HexDump.toHex((short)getCategoryDataType()))
            .append(" (").append(getCategoryDataType()).append(" )\n");

        buffer.append("    .valuesDataType       = ")
            .append("0x")
            .append(HexDump.toHex((short)getValuesDataType()))
            .append(" (").append(getValuesDataType()).append(" )\n");

        buffer.append("    .numCategories        = ")
            .append("0x")
            .append(HexDump.toHex((short)getNumCategories()))
            .append(" (").append(getNumCategories()).append(" )\n");

        buffer.append("    .numValues            = ")
            .append("0x")
            .append(HexDump.toHex((short)getNumValues()))
            .append(" (").append(getNumValues()).append(" )\n");

        buffer.append("    .bubbleSeriesType     = ")
            .append("0x")
            .append(HexDump.toHex((short)getBubbleSeriesType()))
            .append(" (").append(getBubbleSeriesType()).append(" )\n");

        buffer.append("    .numBubbleValues      = ")
            .append("0x")
            .append(HexDump.toHex((short)getNumBubbleValues()))
            .append(" (").append(getNumBubbleValues()).append(" )\n");

        buffer.append("[/Series]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset, field_1_categoryDataType);
        LittleEndian.putShort(data, 6 + offset, field_2_valuesDataType);
        LittleEndian.putShort(data, 8 + offset, field_3_numCategories);
        LittleEndian.putShort(data, 10 + offset, field_4_numValues);
        LittleEndian.putShort(data, 12 + offset, field_5_bubbleSeriesType);
        LittleEndian.putShort(data, 14 + offset, field_6_numBubbleValues);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4 + 2 + 2 + 2 + 2 + 2 + 2;
    }

    public short getSid()
    {
        return this.sid;
    }


    /**
     * Get the category data type field for the Series record.
     *
     * @return  One of 
     *        CATEGORY_DATA_TYPE_DATES
     *        CATEGORY_DATA_TYPE_NUMERIC
     *        CATEGORY_DATA_TYPE_SEQUENCE
     *        CATEGORY_DATA_TYPE_TEXT
     */
    public short getCategoryDataType()
    {
        return field_1_categoryDataType;
    }

    /**
     * Set the category data type field for the Series record.
     *
     * @param field_1_categoryDataType
     *        One of 
     *        CATEGORY_DATA_TYPE_DATES
     *        CATEGORY_DATA_TYPE_NUMERIC
     *        CATEGORY_DATA_TYPE_SEQUENCE
     *        CATEGORY_DATA_TYPE_TEXT
     */
    public void setCategoryDataType(short field_1_categoryDataType)
    {
        this.field_1_categoryDataType = field_1_categoryDataType;
    }

    /**
     * Get the values data type field for the Series record.
     *
     * @return  One of 
     *        VALUES_DATA_TYPE_DATES
     *        VALUES_DATA_TYPE_NUMERIC
     *        VALUES_DATA_TYPE_SEQUENCE
     *        VALUES_DATA_TYPE_TEXT
     */
    public short getValuesDataType()
    {
        return field_2_valuesDataType;
    }

    /**
     * Set the values data type field for the Series record.
     *
     * @param field_2_valuesDataType
     *        One of 
     *        VALUES_DATA_TYPE_DATES
     *        VALUES_DATA_TYPE_NUMERIC
     *        VALUES_DATA_TYPE_SEQUENCE
     *        VALUES_DATA_TYPE_TEXT
     */
    public void setValuesDataType(short field_2_valuesDataType)
    {
        this.field_2_valuesDataType = field_2_valuesDataType;
    }

    /**
     * Get the num categories field for the Series record.
     */
    public short getNumCategories()
    {
        return field_3_numCategories;
    }

    /**
     * Set the num categories field for the Series record.
     */
    public void setNumCategories(short field_3_numCategories)
    {
        this.field_3_numCategories = field_3_numCategories;
    }

    /**
     * Get the num values field for the Series record.
     */
    public short getNumValues()
    {
        return field_4_numValues;
    }

    /**
     * Set the num values field for the Series record.
     */
    public void setNumValues(short field_4_numValues)
    {
        this.field_4_numValues = field_4_numValues;
    }

    /**
     * Get the bubble series type field for the Series record.
     *
     * @return  One of 
     *        BUBBLE_SERIES_TYPE_DATES
     *        BUBBLE_SERIES_TYPE_NUMERIC
     *        BUBBLE_SERIES_TYPE_SEQUENCE
     *        BUBBLE_SERIES_TYPE_TEXT
     */
    public short getBubbleSeriesType()
    {
        return field_5_bubbleSeriesType;
    }

    /**
     * Set the bubble series type field for the Series record.
     *
     * @param field_5_bubbleSeriesType
     *        One of 
     *        BUBBLE_SERIES_TYPE_DATES
     *        BUBBLE_SERIES_TYPE_NUMERIC
     *        BUBBLE_SERIES_TYPE_SEQUENCE
     *        BUBBLE_SERIES_TYPE_TEXT
     */
    public void setBubbleSeriesType(short field_5_bubbleSeriesType)
    {
        this.field_5_bubbleSeriesType = field_5_bubbleSeriesType;
    }

    /**
     * Get the num bubble values field for the Series record.
     */
    public short getNumBubbleValues()
    {
        return field_6_numBubbleValues;
    }

    /**
     * Set the num bubble values field for the Series record.
     */
    public void setNumBubbleValues(short field_6_numBubbleValues)
    {
        this.field_6_numBubbleValues = field_6_numBubbleValues;
    }


}  // END OF CLASS




