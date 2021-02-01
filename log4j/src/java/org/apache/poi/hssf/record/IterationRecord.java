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

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Tells whether to iterate over formula calculations or not.
 * If a formula is dependent upon another formula's result.
 * (odd feature for something that can only have 32 elements in a formula!)
 */
public final class IterationRecord extends StandardRecord {
    public static final short sid = 0x0011;

    private static final BitField iterationOn = BitFieldFactory.getInstance(0x0001);

    private int _flags;

    public IterationRecord(IterationRecord other) {
        super(other);
        _flags = other._flags;
    }

    public IterationRecord(boolean iterateOn) {
        _flags = iterationOn.setBoolean(0, iterateOn);
    }

    public IterationRecord(RecordInputStream in) {
        _flags = in.readShort();
    }

    /**
     * set whether or not to iterate for calculations
     * @param iterate or not
     */
    public void setIteration(boolean iterate) {
        _flags = iterationOn.setBoolean(_flags, iterate);
    }

    /**
     * get whether or not to iterate for calculations
     *
     * @return whether iterative calculations are turned off or on
     */
    public boolean getIteration() {
        return iterationOn.isSet(_flags);
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(_flags);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid() {
        return sid;
    }

    @Override
    public IterationRecord copy() {
        return new IterationRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.ITERATION;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "flags", () -> _flags,
            "iteration", this::getIteration
        );
    }
}
