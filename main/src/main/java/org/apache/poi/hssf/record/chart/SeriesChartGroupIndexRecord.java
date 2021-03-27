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
 * The series chart group index record stores the index to the CHARTFORMAT record (0 based).
 */
public final class SeriesChartGroupIndexRecord extends StandardRecord {
    public static final short sid = 0x1045;

    private short field_1_chartGroupIndex;

    public SeriesChartGroupIndexRecord() {}

    public SeriesChartGroupIndexRecord(SeriesChartGroupIndexRecord other) {
        super(other);
        field_1_chartGroupIndex = other.field_1_chartGroupIndex;
    }

    public SeriesChartGroupIndexRecord(RecordInputStream in) {
        field_1_chartGroupIndex = in.readShort();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_chartGroupIndex);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public SeriesChartGroupIndexRecord copy() {
        return new SeriesChartGroupIndexRecord(this);
    }

    /**
     * Get the chart group index field for the SeriesChartGroupIndex record.
     */
    public short getChartGroupIndex()
    {
        return field_1_chartGroupIndex;
    }

    /**
     * Set the chart group index field for the SeriesChartGroupIndex record.
     */
    public void setChartGroupIndex(short field_1_chartGroupIndex)
    {
        this.field_1_chartGroupIndex = field_1_chartGroupIndex;
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.SERIES_CHART_GROUP_INDEX;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("chartGroupIndex", this::getChartGroupIndex);
    }
}
