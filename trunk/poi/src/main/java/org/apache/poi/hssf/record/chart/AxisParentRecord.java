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
 * The axis size and location
 */
public final class AxisParentRecord extends StandardRecord {
    public static final short sid                 = 0x1041;
    public static final short AXIS_TYPE_MAIN      = 0;
    public static final short AXIS_TYPE_SECONDARY = 1;

    private short field_1_axisType;
    private int field_2_x;
    private int field_3_y;
    private int field_4_width;
    private int field_5_height;


    public AxisParentRecord() {}

    public AxisParentRecord(AxisParentRecord other) {
        super(other);
        field_1_axisType = other.field_1_axisType;
        field_2_x        = other.field_2_x;
        field_3_y        = other.field_3_y;
        field_4_width    = other.field_4_width;
        field_5_height   = other.field_5_height;
    }


    public AxisParentRecord(RecordInputStream in) {
        field_1_axisType = in.readShort();
        field_2_x        = in.readInt();
        field_3_y        = in.readInt();
        field_4_width    = in.readInt();
        field_5_height   = in.readInt();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_axisType);
        out.writeInt(field_2_x);
        out.writeInt(field_3_y);
        out.writeInt(field_4_width);
        out.writeInt(field_5_height);
    }

    protected int getDataSize() {
        return 2 + 4 + 4 + 4 + 4;
    }

    public short getSid()
    {
        return sid;
    }

    /**
     * Get the axis type field for the AxisParent record.
     *
     * @return  One of
     *        AXIS_TYPE_MAIN
     *        AXIS_TYPE_SECONDARY
     */
    public short getAxisType()
    {
        return field_1_axisType;
    }

    /**
     * Set the axis type field for the AxisParent record.
     *
     * @param field_1_axisType
     *        One of
     *        AXIS_TYPE_MAIN
     *        AXIS_TYPE_SECONDARY
     */
    public void setAxisType(short field_1_axisType)
    {
        this.field_1_axisType = field_1_axisType;
    }

    /**
     * Get the x field for the AxisParent record.
     */
    public int getX()
    {
        return field_2_x;
    }

    /**
     * Set the x field for the AxisParent record.
     */
    public void setX(int field_2_x)
    {
        this.field_2_x = field_2_x;
    }

    /**
     * Get the y field for the AxisParent record.
     */
    public int getY()
    {
        return field_3_y;
    }

    /**
     * Set the y field for the AxisParent record.
     */
    public void setY(int field_3_y)
    {
        this.field_3_y = field_3_y;
    }

    /**
     * Get the width field for the AxisParent record.
     */
    public int getWidth()
    {
        return field_4_width;
    }

    /**
     * Set the width field for the AxisParent record.
     */
    public void setWidth(int field_4_width)
    {
        this.field_4_width = field_4_width;
    }

    /**
     * Get the height field for the AxisParent record.
     */
    public int getHeight()
    {
        return field_5_height;
    }

    /**
     * Set the height field for the AxisParent record.
     */
    public void setHeight(int field_5_height)
    {
        this.field_5_height = field_5_height;
    }

    @Override
    public AxisParentRecord copy() {
        return new AxisParentRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.AXIS_PARENT;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "axisType", this::getAxisType,
            "x", this::getX,
            "y", this::getY,
            "width", this::getWidth,
            "height", this::getHeight
        );
    }
}
