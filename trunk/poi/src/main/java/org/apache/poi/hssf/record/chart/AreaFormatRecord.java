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
 * The area format record is used to define the colours and patterns for an area.
 */
public final class AreaFormatRecord extends StandardRecord {
    public static final short sid = 0x100A;

    private static final BitField automatic = BitFieldFactory.getInstance(0x1);
    private static final BitField invert    = BitFieldFactory.getInstance(0x2);

    private  int        field_1_foregroundColor;
    private  int        field_2_backgroundColor;
    private  short      field_3_pattern;
    private  short      field_4_formatFlags;
    private  short      field_5_forecolorIndex;
    private  short      field_6_backcolorIndex;


    public AreaFormatRecord() {}

    public AreaFormatRecord(RecordInputStream in) {
        field_1_foregroundColor        = in.readInt();
        field_2_backgroundColor        = in.readInt();
        field_3_pattern                = in.readShort();
        field_4_formatFlags            = in.readShort();
        field_5_forecolorIndex         = in.readShort();
        field_6_backcolorIndex         = in.readShort();
    }

    public AreaFormatRecord(AreaFormatRecord other) {
        super(other);
        field_1_foregroundColor        = other.field_1_foregroundColor;
        field_2_backgroundColor        = other.field_2_backgroundColor;
        field_3_pattern                = other.field_3_pattern;
        field_4_formatFlags            = other.field_4_formatFlags;
        field_5_forecolorIndex         = other.field_5_forecolorIndex;
        field_6_backcolorIndex         = other.field_6_backcolorIndex;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeInt(field_1_foregroundColor);
        out.writeInt(field_2_backgroundColor);
        out.writeShort(field_3_pattern);
        out.writeShort(field_4_formatFlags);
        out.writeShort(field_5_forecolorIndex);
        out.writeShort(field_6_backcolorIndex);
    }

    protected int getDataSize() {
        return 4 + 4 + 2 + 2 + 2 + 2;
    }

    public short getSid()
    {
        return sid;
    }

    /**
     * Get the foreground color field for the AreaFormat record.
     */
    public int getForegroundColor()
    {
        return field_1_foregroundColor;
    }

    /**
     * Set the foreground color field for the AreaFormat record.
     */
    public void setForegroundColor(int field_1_foregroundColor)
    {
        this.field_1_foregroundColor = field_1_foregroundColor;
    }

    /**
     * Get the background color field for the AreaFormat record.
     */
    public int getBackgroundColor()
    {
        return field_2_backgroundColor;
    }

    /**
     * Set the background color field for the AreaFormat record.
     */
    public void setBackgroundColor(int field_2_backgroundColor)
    {
        this.field_2_backgroundColor = field_2_backgroundColor;
    }

    /**
     * Get the pattern field for the AreaFormat record.
     */
    public short getPattern()
    {
        return field_3_pattern;
    }

    /**
     * Set the pattern field for the AreaFormat record.
     */
    public void setPattern(short field_3_pattern)
    {
        this.field_3_pattern = field_3_pattern;
    }

    /**
     * Get the format flags field for the AreaFormat record.
     */
    public short getFormatFlags()
    {
        return field_4_formatFlags;
    }

    /**
     * Set the format flags field for the AreaFormat record.
     */
    public void setFormatFlags(short field_4_formatFlags)
    {
        this.field_4_formatFlags = field_4_formatFlags;
    }

    /**
     * Get the forecolor index field for the AreaFormat record.
     */
    public short getForecolorIndex()
    {
        return field_5_forecolorIndex;
    }

    /**
     * Set the forecolor index field for the AreaFormat record.
     */
    public void setForecolorIndex(short field_5_forecolorIndex)
    {
        this.field_5_forecolorIndex = field_5_forecolorIndex;
    }

    /**
     * Get the backcolor index field for the AreaFormat record.
     */
    public short getBackcolorIndex()
    {
        return field_6_backcolorIndex;
    }

    /**
     * Set the backcolor index field for the AreaFormat record.
     */
    public void setBackcolorIndex(short field_6_backcolorIndex)
    {
        this.field_6_backcolorIndex = field_6_backcolorIndex;
    }

    /**
     * Sets the automatic field value.
     * automatic formatting
     */
    public void setAutomatic(boolean value)
    {
        field_4_formatFlags = automatic.setShortBoolean(field_4_formatFlags, value);
    }

    /**
     * automatic formatting
     * @return  the automatic field value.
     */
    public boolean isAutomatic()
    {
        return automatic.isSet(field_4_formatFlags);
    }

    /**
     * Sets the invert field value.
     * swap foreground and background colours when data is negative
     */
    public void setInvert(boolean value)
    {
        field_4_formatFlags = invert.setShortBoolean(field_4_formatFlags, value);
    }

    /**
     * swap foreground and background colours when data is negative
     * @return  the invert field value.
     */
    public boolean isInvert()
    {
        return invert.isSet(field_4_formatFlags);
    }

    @Override
    public AreaFormatRecord copy() {
        return new AreaFormatRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.AREA_FORMAT;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        final Map<String,Supplier<?>> m = new LinkedHashMap<>();
        m.put("foregroundColor", this::getForegroundColor);
        m.put("backgroundColor", this::getBackgroundColor);
        m.put("pattern", this::getPattern);
        m.put("inverted", this::isInvert);
        m.put("automatic", this::isAutomatic);
        m.put("formatFlags", this::getFormatFlags);
        m.put("forecolorIndex", this::getForecolorIndex);
        m.put("backcolorIndex", this::getBackcolorIndex);
        return Collections.unmodifiableMap(m);
    }
}
