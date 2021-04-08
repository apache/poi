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
 * The data format record is used to index into a series.
 */
public final class DataFormatRecord extends StandardRecord {
    public static final short sid = 0x1006;

    private static final BitField useExcel4Colors = BitFieldFactory.getInstance(0x1);

    private short field_1_pointNumber;
    private short field_2_seriesIndex;
    private short field_3_seriesNumber;
    private short field_4_formatFlags;

    public DataFormatRecord() {}

    public DataFormatRecord(DataFormatRecord other) {
        super(other);
        field_1_pointNumber = other.field_1_pointNumber;
        field_2_seriesIndex = other.field_2_seriesIndex;
        field_3_seriesNumber = other.field_3_seriesNumber;
        field_4_formatFlags = other.field_4_formatFlags;
    }

    public DataFormatRecord(RecordInputStream in) {
        field_1_pointNumber = in.readShort();
        field_2_seriesIndex = in.readShort();
        field_3_seriesNumber = in.readShort();
        field_4_formatFlags = in.readShort();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_pointNumber);
        out.writeShort(field_2_seriesIndex);
        out.writeShort(field_3_seriesNumber);
        out.writeShort(field_4_formatFlags);
    }

    protected int getDataSize() {
        return 2 + 2 + 2 + 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public DataFormatRecord copy() {
        return new DataFormatRecord(this);
    }

    /**
     * Get the point number field for the DataFormat record.
     */
    public short getPointNumber()
    {
        return field_1_pointNumber;
    }

    /**
     * Set the point number field for the DataFormat record.
     */
    public void setPointNumber(short field_1_pointNumber)
    {
        this.field_1_pointNumber = field_1_pointNumber;
    }

    /**
     * Get the series index field for the DataFormat record.
     */
    public short getSeriesIndex()
    {
        return field_2_seriesIndex;
    }

    /**
     * Set the series index field for the DataFormat record.
     */
    public void setSeriesIndex(short field_2_seriesIndex)
    {
        this.field_2_seriesIndex = field_2_seriesIndex;
    }

    /**
     * Get the series number field for the DataFormat record.
     */
    public short getSeriesNumber()
    {
        return field_3_seriesNumber;
    }

    /**
     * Set the series number field for the DataFormat record.
     */
    public void setSeriesNumber(short field_3_seriesNumber)
    {
        this.field_3_seriesNumber = field_3_seriesNumber;
    }

    /**
     * Get the format flags field for the DataFormat record.
     */
    public short getFormatFlags()
    {
        return field_4_formatFlags;
    }

    /**
     * Set the format flags field for the DataFormat record.
     */
    public void setFormatFlags(short field_4_formatFlags)
    {
        this.field_4_formatFlags = field_4_formatFlags;
    }

    /**
     * Sets the use excel 4 colors field value.
     * set true to use excel 4 colors.
     */
    public void setUseExcel4Colors(boolean value)
    {
        field_4_formatFlags = useExcel4Colors.setShortBoolean(field_4_formatFlags, value);
    }

    /**
     * set true to use excel 4 colors.
     * @return  the use excel 4 colors field value.
     */
    public boolean isUseExcel4Colors()
    {
        return useExcel4Colors.isSet(field_4_formatFlags);
    }


    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.DATA_FORMAT;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "pointNumber", this::getPointNumber,
            "seriesIndex", this::getSeriesIndex,
            "seriesNumber", this::getSeriesNumber,
            "formatFlags", this::getFormatFlags,
            "useExcel4Colors", this::isUseExcel4Colors
        );
    }
}
