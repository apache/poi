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
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 *
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class FuncVarPtg extends AbstractFunctionPtg{

    public final static byte sid  = 0x22;
    private final static int  SIZE = 4;

    /**Creates new function pointer from a byte array
     * usually called while reading an excel file.
     */
    public FuncVarPtg(LittleEndianInput in)  {
        field_1_num_args = in.readByte();
        field_2_fnc_index  = in.readShort();
        FunctionMetadata fm = FunctionMetadataRegistry.getFunctionByIndex(field_2_fnc_index);
        if(fm == null) {
            // Happens only as a result of a call to FormulaParser.parse(), with a non-built-in function name
            returnClass = Ptg.CLASS_VALUE;
            paramClass = new byte[] {Ptg.CLASS_VALUE};
        } else {
            returnClass = fm.getReturnClassCode();
            paramClass = fm.getParameterClassCodes();
        }
    }

    /**
     * Create a function ptg from a string tokenised by the parser
     */
    public FuncVarPtg(String pName, byte pNumOperands) {
        field_1_num_args = pNumOperands;
        field_2_fnc_index = lookupIndex(pName);
        FunctionMetadata fm = FunctionMetadataRegistry.getFunctionByIndex(field_2_fnc_index);
        if(fm == null) {
            // Happens only as a result of a call to FormulaParser.parse(), with a non-built-in function name
            returnClass = Ptg.CLASS_VALUE;
            paramClass = new byte[] {Ptg.CLASS_VALUE};
        } else {
            returnClass = fm.getReturnClassCode();
            paramClass = fm.getParameterClassCodes();
        }
    }

    public void write(LittleEndianOutput out) {
        out.writeByte(sid + getPtgClass());
        out.writeByte(field_1_num_args);
        out.writeShort(field_2_fnc_index);
    }

     public int getNumberOfOperands() {
        return field_1_num_args;
    }

    public int getSize() {
        return SIZE;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append(getClass().getName()).append(" [");
        sb.append(lookupName(field_2_fnc_index));
        sb.append(" nArgs=").append(field_1_num_args);
        sb.append("]");
        return sb.toString();
    }
}
