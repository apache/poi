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
 * The axis line format record defines the axis type details.
 */
public final class AxisLineFormatRecord extends StandardRecord {
    public static final short sid                       = 0x1021;
    public static final short AXIS_TYPE_AXIS_LINE       = 0;
    public static final short AXIS_TYPE_MAJOR_GRID_LINE = 1;
    public static final short AXIS_TYPE_MINOR_GRID_LINE = 2;
    public static final short AXIS_TYPE_WALLS_OR_FLOOR  = 3;

    private short field_1_axisType;

    public AxisLineFormatRecord() {}

    public AxisLineFormatRecord(AxisLineFormatRecord other) {
        super(other);
        field_1_axisType = other.field_1_axisType;
    }

    public AxisLineFormatRecord(RecordInputStream in) {
        field_1_axisType = in.readShort();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_axisType);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    /**
     * Get the axis type field for the AxisLineFormat record.
     *
     * @return  One of
     *        AXIS_TYPE_AXIS_LINE
     *        AXIS_TYPE_MAJOR_GRID_LINE
     *        AXIS_TYPE_MINOR_GRID_LINE
     *        AXIS_TYPE_WALLS_OR_FLOOR
     */
    public short getAxisType()
    {
        return field_1_axisType;
    }

    /**
     * Set the axis type field for the AxisLineFormat record.
     *
     * @param field_1_axisType
     *        One of
     *        AXIS_TYPE_AXIS_LINE
     *        AXIS_TYPE_MAJOR_GRID_LINE
     *        AXIS_TYPE_MINOR_GRID_LINE
     *        AXIS_TYPE_WALLS_OR_FLOOR
     */
    public void setAxisType(short field_1_axisType)
    {
        this.field_1_axisType = field_1_axisType;
    }

    @Override
    public AxisLineFormatRecord copy() {
        return new AxisLineFormatRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.AXIS_LINE_FORMAT;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "axisType", this::getAxisType
        );
    }
}
