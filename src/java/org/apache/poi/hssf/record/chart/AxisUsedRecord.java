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
 * The number of axes used on a chart.
 */
public final class AxisUsedRecord extends StandardRecord {
    public static final short sid = 0x1046;

    private short field_1_numAxis;

    public AxisUsedRecord() {}

    public AxisUsedRecord(AxisUsedRecord other) {
        super(other);
        field_1_numAxis = other.field_1_numAxis;
    }

    public AxisUsedRecord(RecordInputStream in) {
        field_1_numAxis = in.readShort();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_numAxis);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    /**
     * Get the num axis field for the AxisUsed record.
     */
    public short getNumAxis()
    {
        return field_1_numAxis;
    }

    /**
     * Set the num axis field for the AxisUsed record.
     */
    public void setNumAxis(short field_1_numAxis)
    {
        this.field_1_numAxis = field_1_numAxis;
    }

    @Override
    public AxisUsedRecord copy() {
        return new AxisUsedRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.AXIS_USED;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "numAxis", this::getNumAxis
        );
    }
}
