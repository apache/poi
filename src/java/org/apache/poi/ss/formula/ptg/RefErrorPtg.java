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

import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * RefError - handles deleted cell reference
 */
public final class RefErrorPtg extends OperandPtg {

    private final static int SIZE = 5;
    public final static byte sid  = 0x2A;
    private int              field_1_reserved;

    public RefErrorPtg() {
        field_1_reserved = 0;
    }

    public RefErrorPtg(RefErrorPtg other) {
        super(other);
        field_1_reserved = other.field_1_reserved;
    }

    public RefErrorPtg(LittleEndianInput in)  {
        field_1_reserved = in.readInt();
    }

    public void write(LittleEndianOutput out) {
        out.writeByte(sid + getPtgClass());
        out.writeInt(field_1_reserved);
    }

    @Override
    public byte getSid() {
        return sid;
    }

    public int getSize() {
        return SIZE;
    }

    public String toFormulaString() {
        return FormulaError.REF.getString();
    }

    public byte getDefaultOperandClass() {
        return Ptg.CLASS_REF;
    }

    @Override
    public RefErrorPtg copy() {
        return new RefErrorPtg(this);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("reserved", () -> field_1_reserved);
    }
}
