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
 * The units record describes units.
 */
public final class UnitsRecord extends StandardRecord {
    public static final short sid = 0x1001;

    private short field_1_units;

    public UnitsRecord() {}

    public UnitsRecord(UnitsRecord other) {
        super(other);
        field_1_units = other.field_1_units;
    }

    public UnitsRecord(RecordInputStream in) {
        field_1_units = in.readShort();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_units);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public UnitsRecord copy() {
        return new UnitsRecord(this);
    }

    /**
     * Get the units field for the Units record.
     */
    public short getUnits()
    {
        return field_1_units;
    }

    /**
     * Set the units field for the Units record.
     */
    public void setUnits(short field_1_units)
    {
        this.field_1_units = field_1_units;
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.UNITS;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("units", this::getUnits);
    }
}
