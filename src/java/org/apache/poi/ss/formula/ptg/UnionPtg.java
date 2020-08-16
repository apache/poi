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

import org.apache.poi.util.LittleEndianOutput;


/**
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class UnionPtg extends OperationPtg {
    public final static byte sid  = 0x10;

    public static final UnionPtg instance = new UnionPtg();

    private UnionPtg() {
    	// enforce singleton
    }

    public final boolean isBaseToken() {
        return true;
    }

    @Override
    public byte getSid() {
        return sid;
    }

    public int getSize() {
        return 1;
    }

    public void write(LittleEndianOutput out) {
        out.writeByte(sid + getPtgClass());
    }

    public String toFormulaString()
    {
        return ",";
    }


    /** implementation of method from OperationsPtg*/
    public String toFormulaString(String[] operands) {
        return operands[0] + "," + operands[1];
     }

    public int getNumberOfOperands()
    {
        return 2;
    }

    @Override
    public UnionPtg copy() {
        return instance;
    }
}
