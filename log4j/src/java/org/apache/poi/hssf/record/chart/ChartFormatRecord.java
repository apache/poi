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
 * Class ChartFormatRecord (0x1014)<p>
 *
 * (As with all chart related records, documentation is lacking.
 * See {@link ChartRecord} for more details)
 */
public final class ChartFormatRecord extends StandardRecord {
    public static final short sid = 0x1014;

    private static final BitField varyDisplayPattern = BitFieldFactory.getInstance(0x01);

    // ignored?
    private int field1_x_position; // lower left
    private int field2_y_position; // lower left
    private int field3_width;
    private int field4_height;
    private int field5_grbit;
    private int field6_unknown;

    public ChartFormatRecord() {}

    public ChartFormatRecord(ChartFormatRecord other) {
        super(other);
        field1_x_position = other.field1_x_position;
        field2_y_position = other.field2_y_position;
        field3_width = other.field3_width;
        field4_height = other.field4_height;
        field5_grbit = other.field5_grbit;
        field6_unknown = other.field6_unknown;
    }


    public ChartFormatRecord(RecordInputStream in) {
        field1_x_position = in.readInt();
        field2_y_position = in.readInt();
        field3_width = in.readInt();
        field4_height = in.readInt();
        field5_grbit = in.readUShort();
        field6_unknown = in.readUShort();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeInt(getXPosition());
        out.writeInt(getYPosition());
        out.writeInt(getWidth());
        out.writeInt(getHeight());
        out.writeShort(field5_grbit);
        out.writeShort(field6_unknown);
    }

    protected int getDataSize() {
        return 20; // 4 ints and 2 shorts
    }

    public short getSid() {
        return sid;
    }

    public int getXPosition() {
        return field1_x_position;
    }

    public void setXPosition(int xPosition) {
        field1_x_position = xPosition;
    }

    public int getYPosition() {
        return field2_y_position;
    }

    public void setYPosition(int yPosition) {
        field2_y_position = yPosition;
    }

    public int getWidth() {
        return field3_width;
    }

    public void setWidth(int width) {
        field3_width = width;
    }

    public int getHeight() {
        return field4_height;
    }

    public void setHeight(int height) {
        field4_height = height;
    }

    public boolean getVaryDisplayPattern() {
        return varyDisplayPattern.isSet(field5_grbit);
    }

    public void setVaryDisplayPattern(boolean value) {
        field5_grbit = varyDisplayPattern.setBoolean(field5_grbit, value);
    }

    @Override
    public ChartFormatRecord copy() {
        return new ChartFormatRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.CHART_FORMAT;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "x", this::getXPosition,
            "y", this::getYPosition,
            "width", this::getWidth,
            "height", this::getHeight,
            "grbit", () -> field5_grbit,
            "varyDisplayPattern", this::getVaryDisplayPattern,
            "unknown", () -> field6_unknown
        );
    }
}
