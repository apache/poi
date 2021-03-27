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
import static org.apache.poi.util.GenericRecordUtil.getEnumBitsAsString;

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
 * Describes a line format record.  The line format record controls how a line on a chart appears.
 */
public final class LineFormatRecord extends StandardRecord {
    public static final short sid = 0x1007;

    private static final BitField auto      = BitFieldFactory.getInstance(0x1);
    private static final BitField drawTicks = BitFieldFactory.getInstance(0x4);
    private static final BitField unknown   = BitFieldFactory.getInstance(0x4);

    public static final short LINE_PATTERN_SOLID             = 0;
    public static final short LINE_PATTERN_DASH              = 1;
    public static final short LINE_PATTERN_DOT               = 2;
    public static final short LINE_PATTERN_DASH_DOT          = 3;
    public static final short LINE_PATTERN_DASH_DOT_DOT      = 4;
    public static final short LINE_PATTERN_NONE              = 5;
    public static final short LINE_PATTERN_DARK_GRAY_PATTERN = 6;
    public static final short LINE_PATTERN_MEDIUM_GRAY_PATTERN = 7;
    public static final short LINE_PATTERN_LIGHT_GRAY_PATTERN = 8;

    public static final short WEIGHT_HAIRLINE                = -1;
    public static final short WEIGHT_NARROW                  = 0;
    public static final short WEIGHT_MEDIUM                  = 1;
    public static final short WEIGHT_WIDE                    = 2;


    private int   field_1_lineColor;
    private short field_2_linePattern;
    private short field_3_weight;
    private short field_4_format;
    private short field_5_colourPaletteIndex;


    public LineFormatRecord() {}

    public LineFormatRecord(LineFormatRecord other) {
        super(other);
        field_1_lineColor          = other.field_1_lineColor;
        field_2_linePattern        = other.field_2_linePattern;
        field_3_weight             = other.field_3_weight;
        field_4_format             = other.field_4_format;
        field_5_colourPaletteIndex = other.field_5_colourPaletteIndex;
    }

    public LineFormatRecord(RecordInputStream in) {
        field_1_lineColor          = in.readInt();
        field_2_linePattern        = in.readShort();
        field_3_weight             = in.readShort();
        field_4_format             = in.readShort();
        field_5_colourPaletteIndex = in.readShort();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeInt(field_1_lineColor);
        out.writeShort(field_2_linePattern);
        out.writeShort(field_3_weight);
        out.writeShort(field_4_format);
        out.writeShort(field_5_colourPaletteIndex);
    }

    protected int getDataSize() {
        return 4 + 2 + 2 + 2 + 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public LineFormatRecord copy() {
        return new LineFormatRecord(this);
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

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.LINE_FORMAT;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "lineColor", this::getLineColor,
            "linePattern", getEnumBitsAsString(this::getLinePattern,
                new int[]{LINE_PATTERN_SOLID, LINE_PATTERN_DASH, LINE_PATTERN_DOT, LINE_PATTERN_DASH_DOT,
                        LINE_PATTERN_DASH_DOT_DOT, LINE_PATTERN_NONE, LINE_PATTERN_DARK_GRAY_PATTERN,
                        LINE_PATTERN_MEDIUM_GRAY_PATTERN, LINE_PATTERN_LIGHT_GRAY_PATTERN},
                new String[]{"SOLID","DASH","DOT","DASH_DOT","DASH_DOT_DOT","NONE","DARK_GRAY_PATTERN","MEDIUM_GRAY_PATTERN","LIGHT_GRAY_PATTERN"}),
            "weight", getEnumBitsAsString(this::getWeight,
                new int[]{WEIGHT_HAIRLINE, WEIGHT_NARROW, WEIGHT_MEDIUM, WEIGHT_WIDE},
                new String[]{"HAIRLINE","NARROW","MEDIUM","WIDE"}),
            "format", getBitsAsString(this::getFormat,
                new BitField[]{auto,drawTicks,unknown},new String[]{"AUTO","DRAWTICKS","UNKNOWN"}),
            "colourPaletteIndex", this::getColourPaletteIndex
        );
    }
}
