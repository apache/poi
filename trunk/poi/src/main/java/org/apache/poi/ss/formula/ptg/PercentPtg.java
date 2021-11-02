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

/**
 * Percent PTG.
 */
public final class PercentPtg extends ValueOperatorPtg {
    public static final int  SIZE = 1;
    public static final byte sid  = 0x14;

    private static final String PERCENT = "%";

    public static final PercentPtg instance = new PercentPtg();

    private PercentPtg() {
        // enforce singleton
    }

    @Override
    public byte getSid() {
        return sid;
    }

    public int getNumberOfOperands() {
        return 1;
    }

    public String toFormulaString(String[] operands) {
        return operands[0] + PERCENT;
    }

    @Override
    public PercentPtg copy() {
        return instance;
    }
}
