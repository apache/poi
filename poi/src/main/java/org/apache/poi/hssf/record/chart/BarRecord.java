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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.hssf.record.HSSFRecordTypes;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndianOutput;

/**
 * The bar record is used to define a bar chart.
 */
public final class BarRecord extends StandardRecord {
    public static final short sid = 0x1017;

    private static final BitField horizontal          = BitFieldFactory.getInstance(0x1);
    private static final BitField stacked             = BitFieldFactory.getInstance(0x2);
    private static final BitField displayAsPercentage = BitFieldFactory.getInstance(0x4);
    private static final BitField shadow              = BitFieldFactory.getInstance(0x8);

    private short field_1_barSpace;
    private short field_2_categorySpace;
    private short field_3_formatFlags;


    public BarRecord() {}

    public BarRecord(BarRecord other) {
        super(other);
        field_1_barSpace      = other.field_1_barSpace;
        field_2_categorySpace = other.field_2_categorySpace;
        field_3_formatFlags   = other.field_3_formatFlags;
    }

    public BarRecord(RecordInputStream in) {
        field_1_barSpace      = in.readShort();
        field_2_categorySpace = in.readShort();
        field_3_formatFlags   = in.readShort();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_barSpace);
        out.writeShort(field_2_categorySpace);
        out.writeShort(field_3_formatFlags);
    }

    protected int getDataSize() {
        return 2 + 2 + 2;
    }

    public short getSid()
    {
        return sid;
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

    @Override
    public BarRecord copy() {
        return new BarRecord(this);
    }


    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.BAR;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        final Map<String,Supplier<?>> m = new LinkedHashMap<>();
        m.put("barSpace", this::getBarSpace);
        m.put("categorySpace", this::getCategorySpace);
        m.put("formatFlags", this::getFormatFlags);
        m.put("horizontal", this::isHorizontal);
        m.put("stacked", this::isStacked);
        m.put("displayAsPercentage", this::isDisplayAsPercentage);
        m.put("shadow", this::isShadow);
        return Collections.unmodifiableMap(m);
    }
}
