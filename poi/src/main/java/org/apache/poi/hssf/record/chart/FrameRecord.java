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
 * The frame record indicates whether there is a border around the displayed text of a chart.
 */
public final class FrameRecord extends StandardRecord {
    public static final short sid  = 0x1032;
    public static final short       BORDER_TYPE_REGULAR            = 0;
    public static final short       BORDER_TYPE_SHADOW             = 1;

    private static final BitField autoSize     = BitFieldFactory.getInstance(0x1);
    private static final BitField autoPosition = BitFieldFactory.getInstance(0x2);

    private short field_1_borderType;
    private short field_2_options;


    public FrameRecord() {}

    public FrameRecord(FrameRecord other) {
        super(other);
        field_1_borderType = other.field_1_borderType;
        field_2_options = other.field_2_options;
    }

    public FrameRecord(RecordInputStream in) {
        field_1_borderType = in.readShort();
        field_2_options = in.readShort();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_borderType);
        out.writeShort(field_2_options);
    }

    protected int getDataSize() {
        return 2 + 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public FrameRecord copy() {
        return new FrameRecord(this);
    }

    /**
     * Get the border type field for the Frame record.
     *
     * @return  One of
     *        BORDER_TYPE_REGULAR
     *        BORDER_TYPE_SHADOW
     */
    public short getBorderType()
    {
        return field_1_borderType;
    }

    /**
     * Set the border type field for the Frame record.
     *
     * @param field_1_borderType
     *        One of
     *        BORDER_TYPE_REGULAR
     *        BORDER_TYPE_SHADOW
     */
    public void setBorderType(short field_1_borderType)
    {
        this.field_1_borderType = field_1_borderType;
    }

    /**
     * Get the options field for the Frame record.
     */
    public short getOptions()
    {
        return field_2_options;
    }

    /**
     * Set the options field for the Frame record.
     */
    public void setOptions(short field_2_options)
    {
        this.field_2_options = field_2_options;
    }

    /**
     * Sets the auto size field value.
     * excel calculates the size automatically if true
     */
    public void setAutoSize(boolean value)
    {
        field_2_options = autoSize.setShortBoolean(field_2_options, value);
    }

    /**
     * excel calculates the size automatically if true
     * @return  the auto size field value.
     */
    public boolean isAutoSize()
    {
        return autoSize.isSet(field_2_options);
    }

    /**
     * Sets the auto position field value.
     * excel calculates the position automatically
     */
    public void setAutoPosition(boolean value)
    {
        field_2_options = autoPosition.setShortBoolean(field_2_options, value);
    }

    /**
     * excel calculates the position automatically
     * @return  the auto position field value.
     */
    public boolean isAutoPosition()
    {
        return autoPosition.isSet(field_2_options);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.FRAME;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "borderType", this::getBorderType,
            "options", this::getOptions,
            "autoSize", this::isAutoSize,
            "autoPosition", this::isAutoPosition
        );
    }
}