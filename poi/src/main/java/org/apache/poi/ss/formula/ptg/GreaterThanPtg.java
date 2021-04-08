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
 * Greater than operator PTG ">"
 */
public final class GreaterThanPtg extends ValueOperatorPtg {
    public static final byte sid  = 0x0D;
    private static final String GREATERTHAN = ">";

    public static final GreaterThanPtg instance = new GreaterThanPtg();

    private GreaterThanPtg() {
    	// enforce singleton
    }

    @Override
    public byte getSid() {
        return sid;
    }

    /**
     * Get the number of operands for the Less than operator
     * @return int the number of operands
     */
    public int getNumberOfOperands() {
        return 2;
    }

    /**
     * Implementation of method from OperationsPtg
     * @param operands a String array of operands
     * @return String the Formula as a String
     */
    public String toFormulaString(String[] operands) {
        return operands[0] + GREATERTHAN + operands[1];
    }

    @Override
    public GreaterThanPtg copy() {
        return instance;
    }
}
