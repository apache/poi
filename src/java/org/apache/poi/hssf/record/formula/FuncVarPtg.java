/* ====================================================================
   Copyright 2003-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */


package org.apache.poi.hssf.record.formula;
import org.apache.poi.util.LittleEndian;

/**
 *
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public class FuncVarPtg extends AbstractFunctionPtg{
    
    public final static byte sid  = 0x22;
    private final static int  SIZE = 4;  
    
    private FuncVarPtg() {
      //Required for clone methods
    }

 /**Creates new function pointer from a byte array 
     * usually called while reading an excel file. 
     */
    public FuncVarPtg(byte[] data, int offset) {
        offset++;
        field_1_num_args = data[ offset + 0 ];
        field_2_fnc_index  = LittleEndian.getShort(data,offset + 1 );
    }
    
    /**
     * Create a function ptg from a string tokenised by the parser
     */
    public FuncVarPtg(String pName, byte pNumOperands) {
        field_1_num_args = pNumOperands;
        field_2_fnc_index = lookupIndex(pName);
        try{
            returnClass = ( (Byte) functionData[field_2_fnc_index][0]).byteValue();
            paramClass = (byte[]) functionData[field_2_fnc_index][1];
        } catch (NullPointerException npe ) {
            returnClass = Ptg.CLASS_VALUE;
            paramClass = new byte[] {Ptg.CLASS_VALUE};
        }
    }
    
     public void writeBytes(byte[] array, int offset) {
        array[offset+0]=(byte) (sid + ptgClass);
        array[offset+1]=field_1_num_args;
        LittleEndian.putShort(array,offset+2,field_2_fnc_index);
    }
    
     public int getNumberOfOperands() {
        return field_1_num_args;
    }
    
    public Object clone() {
      FuncVarPtg ptg = new FuncVarPtg();
      ptg.field_1_num_args = field_1_num_args;
      ptg.field_2_fnc_index = field_2_fnc_index;
      ptg.setClass(ptgClass);
      return ptg;
    }
    
    public int getSize() {
        return SIZE;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer
        .append("<FunctionVarPtg>").append("\n")
        .append("   field_1_num_args=").append(field_1_num_args).append("\n")
        .append("      name         =").append(lookupName(field_2_fnc_index)).append("\n")
        .append("   field_2_fnc_index=").append(field_2_fnc_index).append("\n")
        .append("</FunctionPtg>");
        return buffer.toString();
    }

    
}
