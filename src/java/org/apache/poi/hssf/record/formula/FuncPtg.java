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
 * @author aviks
 * @author Jason Height (jheight at chariot dot net dot au)
 * @author Danny Mui (dmui at apache dot org) (Leftover handling)
 */
public class FuncPtg extends AbstractFunctionPtg{
    
    public final static byte sid  = 0x21;
    public final static int  SIZE = 3;
    private int numParams=0;
    
    /**
     * FuncPtgs are defined to be 4 bytes but the actual FuncPtg uses only 2 bytes.
     * If we have leftOvers that are read from the file we should serialize them back out.
     * <p>
     * If the leftovers are removed, a prompt "Warning: Data may have been lost occurs in Excel"
     */
	//protected byte[] leftOvers = null;
    
    private FuncPtg() {
      //Required for clone methods      
    }

    /**Creates new function pointer from a byte array 
     * usually called while reading an excel file. 
     */
    public FuncPtg(byte[] data, int offset) {
        offset++;
        //field_1_num_args = data[ offset + 0 ];
        field_2_fnc_index  = LittleEndian.getShort(data,offset + 0 );
        
      /*  
        if (data.length - offset > 2) { //save left overs if there are any
			leftOvers = new byte[2];
        	System.arraycopy(data, offset+1, leftOvers, 0, leftOvers.length);
        }
        */	
        try {
            numParams = ( (Integer)functionData[field_2_fnc_index][2]).intValue();
        } catch (NullPointerException npe) {
            numParams=0;
        }   
        
    }
    
     public void writeBytes(byte[] array, int offset) {
        array[offset+0]= (byte) (sid + ptgClass);
        //array[offset+1]=field_1_num_args;
        LittleEndian.putShort(array,offset+1,field_2_fnc_index);
        /**if (leftOvers != null) {
        	System.arraycopy(leftOvers, 0, array, offset+2, leftOvers.length);
        }**/
    }
    
     public int getNumberOfOperands() {
        return numParams;
    }

    public Object clone() {
      FuncPtg ptg = new FuncPtg();
      //ptg.field_1_num_args = field_1_num_args;
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
        .append("<FunctionPtg>").append("\n")
        .append("   numArgs(internal)=").append(this.numParams).append("\n")
        .append("      name         =").append(lookupName(field_2_fnc_index)).append("\n")
        .append("   field_2_fnc_index=").append(field_2_fnc_index).append("\n")
        .append("</FunctionPtg>");
        return buffer.toString();
    }
}