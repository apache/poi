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

import org.apache.poi.util.LittleEndianOutput;

public class UnknownPtg extends Ptg {
    private final short size = 1;
    private final int _sid;

    public UnknownPtg(int sid) {
        _sid = sid;
    }

    @Override
    public boolean isBaseToken() {
        return true;
    }
    @Override
    public void write(LittleEndianOutput out) {
        out.writeByte(_sid);
    }

    @Override
    public byte getSid() {
        return (byte)_sid;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String toFormulaString() {
        return "UNKNOWN";
    }
    @Override
    public byte getDefaultOperandClass() {
        return Ptg.CLASS_VALUE;
    }

    @Override
    public UnknownPtg copy() {
        return this;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return null;
    }
}
