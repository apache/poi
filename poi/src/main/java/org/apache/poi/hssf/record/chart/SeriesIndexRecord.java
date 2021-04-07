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
 * links a series to its position in the series list.
 */
public final class SeriesIndexRecord extends StandardRecord {
    public static final short sid = 0x1065;
    private short field_1_index;

    public SeriesIndexRecord() {}

    public SeriesIndexRecord(SeriesIndexRecord other) {
        super(other);
        field_1_index = other.field_1_index;
    }

    public SeriesIndexRecord(RecordInputStream in) {
        field_1_index = in.readShort();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_index);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public SeriesIndexRecord copy() {
        return new SeriesIndexRecord(this);
    }

    /**
     * Get the index field for the SeriesIndex record.
     */
    public short getIndex()
    {
        return field_1_index;
    }

    /**
     * Set the index field for the SeriesIndex record.
     */
    public void setIndex(short field_1_index)
    {
        this.field_1_index = field_1_index;
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.SERIES_INDEX;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("index", this::getIndex);
    }
}
