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
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * This record refers to a category or series axis and is used to specify label/tickmark frequency.
 */
public final class CategorySeriesAxisRecord extends StandardRecord {
    public static final short sid = 0x1020;

    private static final BitField valueAxisCrossing = BitFieldFactory.getInstance(0x1);
    private static final BitField crossesFarRight   = BitFieldFactory.getInstance(0x2);
    private static final BitField reversed          = BitFieldFactory.getInstance(0x4);

    private short field_1_crossingPoint;
    private short field_2_labelFrequency;
    private short field_3_tickMarkFrequency;
    private short field_4_options;


    public CategorySeriesAxisRecord() {}

    public CategorySeriesAxisRecord(CategorySeriesAxisRecord other) {
        super(other);
        field_1_crossingPoint     = other.field_1_crossingPoint;
        field_2_labelFrequency    = other.field_2_labelFrequency;
        field_3_tickMarkFrequency = other.field_3_tickMarkFrequency;
        field_4_options           = other.field_4_options;
    }

    public CategorySeriesAxisRecord(RecordInputStream in) {
        field_1_crossingPoint     = in.readShort();
        field_2_labelFrequency    = in.readShort();
        field_3_tickMarkFrequency = in.readShort();
        field_4_options           = in.readShort();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_crossingPoint);
        out.writeShort(field_2_labelFrequency);
        out.writeShort(field_3_tickMarkFrequency);
        out.writeShort(field_4_options);
    }

    protected int getDataSize() {
        return 2 + 2 + 2 + 2;
    }

    public short getSid()
    {
        return sid;
    }

    /**
     * Get the crossing point field for the CategorySeriesAxis record.
     */
    public short getCrossingPoint()
    {
        return field_1_crossingPoint;
    }

    /**
     * Set the crossing point field for the CategorySeriesAxis record.
     */
    public void setCrossingPoint(short field_1_crossingPoint)
    {
        this.field_1_crossingPoint = field_1_crossingPoint;
    }

    /**
     * Get the label frequency field for the CategorySeriesAxis record.
     */
    public short getLabelFrequency()
    {
        return field_2_labelFrequency;
    }

    /**
     * Set the label frequency field for the CategorySeriesAxis record.
     */
    public void setLabelFrequency(short field_2_labelFrequency)
    {
        this.field_2_labelFrequency = field_2_labelFrequency;
    }

    /**
     * Get the tick mark frequency field for the CategorySeriesAxis record.
     */
    public short getTickMarkFrequency()
    {
        return field_3_tickMarkFrequency;
    }

    /**
     * Set the tick mark frequency field for the CategorySeriesAxis record.
     */
    public void setTickMarkFrequency(short field_3_tickMarkFrequency)
    {
        this.field_3_tickMarkFrequency = field_3_tickMarkFrequency;
    }

    /**
     * Get the options field for the CategorySeriesAxis record.
     */
    public short getOptions()
    {
        return field_4_options;
    }

    /**
     * Set the options field for the CategorySeriesAxis record.
     */
    public void setOptions(short field_4_options)
    {
        this.field_4_options = field_4_options;
    }

    /**
     * Sets the value axis crossing field value.
     * set true to indicate axis crosses between categories and false to cross axis midway
     */
    public void setValueAxisCrossing(boolean value)
    {
        field_4_options = valueAxisCrossing.setShortBoolean(field_4_options, value);
    }

    /**
     * set true to indicate axis crosses between categories and false to cross axis midway
     * @return  the value axis crossing field value.
     */
    public boolean isValueAxisCrossing()
    {
        return valueAxisCrossing.isSet(field_4_options);
    }

    /**
     * Sets the crosses far right field value.
     * axis crosses at the far right
     */
    public void setCrossesFarRight(boolean value)
    {
        field_4_options = crossesFarRight.setShortBoolean(field_4_options, value);
    }

    /**
     * axis crosses at the far right
     * @return  the crosses far right field value.
     */
    public boolean isCrossesFarRight()
    {
        return crossesFarRight.isSet(field_4_options);
    }

    /**
     * Sets the reversed field value.
     * categories are displayed in reverse order
     */
    public void setReversed(boolean value)
    {
        field_4_options = reversed.setShortBoolean(field_4_options, value);
    }

    /**
     * categories are displayed in reverse order
     * @return  the reversed field value.
     */
    public boolean isReversed()
    {
        return reversed.isSet(field_4_options);
    }

    @Override
    public CategorySeriesAxisRecord copy() {
        return new CategorySeriesAxisRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.CATEGORY_SERIES_AXIS;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "crossingPoint", this::getCrossingPoint,
            "labelFrequency", this::getLabelFrequency,
            "tickMarkFrequency", this::getTickMarkFrequency,
            "options", this::getOptions,
            "valueAxisCrossing", this::isValueAxisCrossing,
            "crossesFarRight", this::isCrossesFarRight,
            "reversed", this::isReversed
        );
    }
}
