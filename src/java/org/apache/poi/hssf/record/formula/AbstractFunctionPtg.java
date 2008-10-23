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

package org.apache.poi.hssf.record.formula;

import org.apache.poi.hssf.record.formula.function.FunctionMetadata;
import org.apache.poi.hssf.record.formula.function.FunctionMetadataRegistry;


/**
 * This class provides the base functionality for Excel sheet functions
 * There are two kinds of function Ptgs - tFunc and tFuncVar
 * Therefore, this class will have ONLY two subclasses
 * @author  Avik Sengupta
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
public abstract class AbstractFunctionPtg extends OperationPtg {

    /**
     * The name of the IF function (i.e. "IF").  Extracted as a constant for clarity.
     */
    public static final String FUNCTION_NAME_IF = "IF";
    /** All external functions have function index 255 */
    private static final short FUNCTION_INDEX_EXTERNAL = 255;

    protected byte returnClass;
    protected byte[] paramClass;

    protected byte field_1_num_args;
    protected short field_2_fnc_index;

    public final boolean isBaseToken() {
    	return false;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append(getClass().getName()).append(" [");
        sb.append(field_2_fnc_index).append(" ").append(field_1_num_args);
        sb.append("]");
        return sb.toString();
    }

    public short getFunctionIndex() {
        return field_2_fnc_index;
    }

    public String getName() {
        return lookupName(field_2_fnc_index);
    }
    /**
     * external functions get some special processing
     * @return <code>true</code> if this is an external function
     */
    public boolean isExternalFunction() {
        return field_2_fnc_index == FUNCTION_INDEX_EXTERNAL;
    }

    public String toFormulaString() {
        return getName();
    }

    public String toFormulaString(String[] operands) {
        StringBuffer buf = new StringBuffer();

        if(isExternalFunction()) {
            buf.append(operands[0]); // first operand is actually the function name
            appendArgs(buf, 1, operands);
        } else {
            buf.append(getName());
            appendArgs(buf, 0, operands);
        }
        return buf.toString();
    }

    private static void appendArgs(StringBuffer buf, int firstArgIx, String[] operands) {
        buf.append('(');
        for (int i=firstArgIx;i<operands.length;i++) {
            if (i>firstArgIx) {
                buf.append(',');
            }
            buf.append(operands[i]);
        }
        buf.append(")");
    }

    public abstract int getSize();


    /**
     * Used to detect whether a function name found in a formula is one of the standard excel functions
     * <p>
     * The name matching is case insensitive.
     * @return <code>true</code> if the name specifies a standard worksheet function,
     *  <code>false</code> if the name should be assumed to be an external function.
     */
    public static final boolean isBuiltInFunctionName(String name) {
        short ix = FunctionMetadataRegistry.lookupIndexByName(name.toUpperCase());
        return ix >= 0;
    }

    protected String lookupName(short index) {
        if(index == FunctionMetadataRegistry.FUNCTION_INDEX_EXTERNAL) {
            return "#external#";
        }
        FunctionMetadata fm = FunctionMetadataRegistry.getFunctionByIndex(index);
        if(fm == null) {
            throw new RuntimeException("bad function index (" + index + ")");
        }
        return fm.getName();
    }

    /**
     * Resolves internal function names into function indexes.
     * <p>
     * The name matching is case insensitive.
     * @return the standard worksheet function index if found, otherwise <tt>FUNCTION_INDEX_EXTERNAL</tt>
     */
    protected static short lookupIndex(String name) {
        short ix = FunctionMetadataRegistry.lookupIndexByName(name.toUpperCase());
        if (ix < 0) {
            return FUNCTION_INDEX_EXTERNAL;
        }
        return ix;
    }

    public byte getDefaultOperandClass() {
        return returnClass;
    }

    public byte getParameterClass(int index) {
        if (index >= paramClass.length) {
            // For var-arg (and other?) functions, the metadata does not list all the parameter
            // operand classes.  In these cases, all extra parameters are assumed to have the 
            // same operand class as the last one specified.
            return paramClass[paramClass.length - 1];
        }
        return paramClass[index];
    }
}
