/*
 * DummyFunctionPtg.java
 *
 * 
 */


package org.apache.poi.hssf.record.formula;

import java.util.List;
/**
 * This class provides functions with variable arguments.  
 * @author  aviks
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 
 */
public class FunctionPtg extends OperationPtg {
    public final static short sid  = 0x22;
    private final static int  SIZE = 2;    
    
    private byte field_1_num_args;
    private byte field_2_fnc_index;
    
    //private String name;
    //private int numOperands;
    /** Creates new DummyFunctionPtg */
    public FunctionPtg() {
    }
    
    public FunctionPtg(String pName, byte pNumOperands) {
        field_1_num_args = pNumOperands;
        field_2_fnc_index = lookupIndex(pName);
        
    }
   
    public int getType() {
        return -1;
    }   
    
    public int getNumberOfOperands() {
        return field_1_num_args;
    }
    
    public int getFunctionIndex() {
        return field_2_fnc_index;
    }
    
    public String getName() {
        return lookupName(field_2_fnc_index);
    }
    
    public String toFormulaString() {
        return getName()+getNumberOfOperands();
    }
    
    public String toFormulaString(Ptg[] operands) {
        StringBuffer buf = new StringBuffer();
        buf.append(getName()+"(");
        for (int i=0;i<operands.length;i++) {
            buf.append(operands[i].toFormulaString()).append(',');
        }
        buf.append(")");
        return buf.toString();
    }
    
     public String toFormulaString(String[] operands) {
        StringBuffer buf = new StringBuffer();
        buf.append(getName()+"(");
        if (operands.length >0) {
            for (int i=0;i<operands.length;i++) {
                buf.append(operands[i]);
                buf.append(',');
            }
            buf.deleteCharAt(buf.length()-1);
        }
        buf.append(")");
        return buf.toString();
    }
    
    
    public void writeBytes(byte[] array, int offset) {
        array[offset]=field_1_num_args;
        array[offset]=field_2_fnc_index;
    }
    
    public int getSize() {
        return SIZE;
    }
    
    private String lookupName(byte index) {
        return "SUM"; //for now always return "SUM"
    }
    
    private byte lookupIndex(String name) {
        return 4; //for now just return SUM everytime...
    }
    
  
}
