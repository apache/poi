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

/**
 * defines a Ptg that is an operation instead of an operand
 * @author  andy
 */
public abstract class OperationPtg extends Ptg {
    public static final int TYPE_UNARY    = 0;
    public static final int TYPE_BINARY   = 1;
    public static final int TYPE_FUNCTION = 2;

    protected OperationPtg() {}

    /**
     *  returns a string representation of the operations
     *  the length of the input array should equal the number returned by
     *  @see #getNumberOfOperands
     *
     */
    public abstract String toFormulaString(String[] operands);

    /**
     * The number of operands expected by the operations
     */
    public abstract int getNumberOfOperands();

    public byte getDefaultOperandClass() {
        return Ptg.CLASS_VALUE;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return null;
    }
}
