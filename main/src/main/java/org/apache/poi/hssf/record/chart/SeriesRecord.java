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

package org.apache.poi.hssf.record.chart;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.hssf.record.HSSFRecordTypes;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * The series record describes the overall data for a series.
 */
public final class SeriesRecord extends StandardRecord {
    public static final short sid                         = 0x1003;

    public static final short CATEGORY_DATA_TYPE_DATES    = 0;
    public static final short CATEGORY_DATA_TYPE_NUMERIC  = 1;
    public static final short CATEGORY_DATA_TYPE_SEQUENCE = 2;
    public static final short CATEGORY_DATA_TYPE_TEXT     = 3;

    public static final short VALUES_DATA_TYPE_DATES      = 0;
    public static final short VALUES_DATA_TYPE_NUMERIC    = 1;
    public static final short VALUES_DATA_TYPE_SEQUENCE   = 2;
    public static final short VALUES_DATA_TYPE_TEXT       = 3;

    public static final short BUBBLE_SERIES_TYPE_DATES    = 0;
    public static final short BUBBLE_SERIES_TYPE_NUMERIC  = 1;
    public static final short BUBBLE_SERIES_TYPE_SEQUENCE = 2;
    public static final short BUBBLE_SERIES_TYPE_TEXT     = 3;

    private short field_1_categoryDataType;
    private short field_2_valuesDataType;
    private short field_3_numCategories;
    private short field_4_numValues;
    private short field_5_bubbleSeriesType;
    private short field_6_numBubbleValues;


    public SeriesRecord() {}

    public SeriesRecord(SeriesRecord other) {
        super(other);
        field_1_categoryDataType = other.field_1_categoryDataType;
        field_2_valuesDataType   = other.field_2_valuesDataType;
        field_3_numCategories    = other.field_3_numCategories;
        field_4_numValues        = other.field_4_numValues;
        field_5_bubbleSeriesType = other.field_5_bubbleSeriesType;
        field_6_numBubbleValues  = other.field_6_numBubbleValues;
    }

    public SeriesRecord(RecordInputStream in) {
        field_1_categoryDataType = in.readShort();
        field_2_valuesDataType   = in.readShort();
        field_3_numCategories    = in.readShort();
        field_4_numValues        = in.readShort();
        field_5_bubbleSeriesType = in.readShort();
        field_6_numBubbleValues  = in.readShort();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_categoryDataType);
        out.writeShort(field_2_valuesDataType);
        out.writeShort(field_3_numCategories);
        out.writeShort(field_4_numValues);
        out.writeShort(field_5_bubbleSeriesType);
        out.writeShort(field_6_numBubbleValues);
    }

    protected int getDataSize() {
        return 2 + 2 + 2 + 2 + 2 + 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public SeriesRecord copy() {
        return new SeriesRecord(this);
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

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.SERIES;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "categoryDataType", this::getCategoryDataType,
            "valuesDataType", this::getValuesDataType,
            "numCategories", this::getNumCategories,
            "numValues", this::getNumValues,
            "bubbleSeriesType", this::getBubbleSeriesType,
            "numBubbleValues", this::getNumBubbleValues
        );
    }
}
