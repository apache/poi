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

import static org.apache.poi.util.GenericRecordUtil.getBitsAsString;

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
 * The series label record defines the type of label associated with the data format record.
 */
public final class SeriesLabelsRecord extends StandardRecord {
    public static final short sid = 0x100c;

    private static final BitField showActual        = BitFieldFactory.getInstance(0x01);
    private static final BitField showPercent       = BitFieldFactory.getInstance(0x02);
    private static final BitField labelAsPercentage = BitFieldFactory.getInstance(0x04);
    private static final BitField smoothedLine      = BitFieldFactory.getInstance(0x08);
    private static final BitField showLabel         = BitFieldFactory.getInstance(0x10);
    private static final BitField showBubbleSizes   = BitFieldFactory.getInstance(0x20);

    private short field_1_formatFlags;

    public SeriesLabelsRecord() {}

    public SeriesLabelsRecord(SeriesLabelsRecord other) {
        super(other);
        field_1_formatFlags = other.field_1_formatFlags;
    }

    public SeriesLabelsRecord(RecordInputStream in) {
        field_1_formatFlags = in.readShort();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_formatFlags);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public SeriesLabelsRecord copy() {
        return new SeriesLabelsRecord(this);
    }

    /**
     * Get the format flags field for the SeriesLabels record.
     */
    public short getFormatFlags()
    {
        return field_1_formatFlags;
    }

    /**
     * Set the format flags field for the SeriesLabels record.
     */
    public void setFormatFlags(short field_1_formatFlags)
    {
        this.field_1_formatFlags = field_1_formatFlags;
    }

    /**
     * Sets the show actual field value.
     * show actual value of the data point
     */
    public void setShowActual(boolean value)
    {
        field_1_formatFlags = showActual.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * show actual value of the data point
     * @return  the show actual field value.
     */
    public boolean isShowActual()
    {
        return showActual.isSet(field_1_formatFlags);
    }

    /**
     * Sets the show percent field value.
     * show value as percentage of total (pie charts only)
     */
    public void setShowPercent(boolean value)
    {
        field_1_formatFlags = showPercent.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * show value as percentage of total (pie charts only)
     * @return  the show percent field value.
     */
    public boolean isShowPercent()
    {
        return showPercent.isSet(field_1_formatFlags);
    }

    /**
     * Sets the label as percentage field value.
     * show category label/value as percentage (pie charts only)
     */
    public void setLabelAsPercentage(boolean value)
    {
        field_1_formatFlags = labelAsPercentage.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * show category label/value as percentage (pie charts only)
     * @return  the label as percentage field value.
     */
    public boolean isLabelAsPercentage()
    {
        return labelAsPercentage.isSet(field_1_formatFlags);
    }

    /**
     * Sets the smoothed line field value.
     * show smooth line
     */
    public void setSmoothedLine(boolean value)
    {
        field_1_formatFlags = smoothedLine.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * show smooth line
     * @return  the smoothed line field value.
     */
    public boolean isSmoothedLine()
    {
        return smoothedLine.isSet(field_1_formatFlags);
    }

    /**
     * Sets the show label field value.
     * display category label
     */
    public void setShowLabel(boolean value)
    {
        field_1_formatFlags = showLabel.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * display category label
     * @return  the show label field value.
     */
    public boolean isShowLabel()
    {
        return showLabel.isSet(field_1_formatFlags);
    }

    /**
     * Sets the show bubble sizes field value.
     * ??
     */
    public void setShowBubbleSizes(boolean value)
    {
        field_1_formatFlags = showBubbleSizes.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * ??
     * @return  the show bubble sizes field value.
     */
    public boolean isShowBubbleSizes()
    {
        return showBubbleSizes.isSet(field_1_formatFlags);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.SERIES_LABELS;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "formatFlags", getBitsAsString(this::getFormatFlags,
                new BitField[]{showActual,showPercent,labelAsPercentage,smoothedLine,showLabel,showBubbleSizes},
                new String[]{"SHOW_ACTUAL","SHOW_PERCENT","LABEL_AS_PERCENTAGE","SMOOTHED_LINE","SHOW_LABEL","SHOW_BUBBLE_SIZES"}));
    }
}
