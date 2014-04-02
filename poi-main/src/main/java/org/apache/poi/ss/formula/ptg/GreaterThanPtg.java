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
 * @author  Cameron Riley (criley at ekmail.com)
 */
public final class GreaterThanPtg extends ValueOperatorPtg {
    public final static byte sid  = 0x0D;    
    private final static String GREATERTHAN = ">";

    public static final ValueOperatorPtg instance = new GreaterThanPtg();

    private GreaterThanPtg() {
    	// enforce singleton
    }
    
    protected byte getSid() {
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
    public String toFormulaString(String[] operands) 
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append(operands[ 0 ]);
        buffer.append(GREATERTHAN);
        buffer.append(operands[ 1 ]);
        return buffer.toString();
    }
}
