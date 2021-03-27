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

package org.apache.poi.hssf.record;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/** Controls the accuracy of the calculations */
public final class DeltaRecord extends StandardRecord {
    public static final short sid = 0x0010;
    public static final double DEFAULT_VALUE = 0.0010;

    // a double is an IEEE 8-byte float...damn IEEE and their goofy standards an
    // ambiguous numeric identifiers
    private double field_1_max_change;

    public DeltaRecord(double maxChange) {
        field_1_max_change = maxChange;
    }

    public DeltaRecord(DeltaRecord other) {
        super(other);
        field_1_max_change = other.field_1_max_change;
    }

    public DeltaRecord(RecordInputStream in) {
        field_1_max_change = in.readDouble();
    }

    /**
     * get the maximum change
     * @return maxChange - maximum rounding error
     */
    public double getMaxChange() {
        return field_1_max_change;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeDouble(getMaxChange());
    }

    protected int getDataSize() {
        return 8;
    }

    public short getSid() {
        return sid;
    }

    @Override
    public DeltaRecord copy() {
        // immutable
        return this;
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.DELTA;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("maxChange", this::getMaxChange);
    }
}
