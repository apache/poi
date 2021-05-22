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

package org.apache.poi.ss.formula.ptg;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Integer (unsigned short integer) Stores an unsigned short value (java int) in a formula
 */
public final class IntPtg extends ScalarConstantPtg {
    // 16 bit unsigned integer
    private static final int MIN_VALUE = 0x0000;
    private static final int MAX_VALUE = 0xFFFF;

    /**
     * Excel represents integers 0..65535 with the tInt token.
     *
     * @return {@code true} if the specified value is within the range of values
     * {@code IntPtg} can represent.
     */
    public static boolean isInRange(int i) {
        return i >= MIN_VALUE && i <= MAX_VALUE;
    }

    public static final int SIZE = 3;
    public static final byte sid = 0x1e;
    private final int field_1_value;

    public IntPtg(LittleEndianInput in)  {
        this(in.readUShort());
    }

    public IntPtg(int value) {
        if (!isInRange(value)) {
            throw new IllegalArgumentException("value is out of range: " + value);
        }
        field_1_value = value;
    }

    public int getValue() {
        return field_1_value;
    }

    @Override
    public void write(LittleEndianOutput out) {
        out.writeByte(sid + getPtgClass());
        out.writeShort(getValue());
    }

    @Override
    public byte getSid() {
        return sid;
    }

    @Override
    public int getSize() {
        return SIZE;
    }

    @Override
    public String toFormulaString() {
        return String.valueOf(getValue());
    }

    @Override
    public IntPtg copy() {
        return this;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("value", this::getValue);
    }
}
