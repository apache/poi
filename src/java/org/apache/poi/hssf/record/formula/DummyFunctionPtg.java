/*
 * DummyFunctionPtg.java
 *
 * 
 */


package org.apache.poi.hssf.record.formula;

import java.util.List;
/**
 * DUMMY DUMMY DUMMY
 * This class exists only becoz i dont know how to handle functions in formula's properly
 * to be used only for testing my parser. 
 * @author  aviks
 * @version 
 */
public class DummyFunctionPtg extends OperationPtg {
    private String name;
    private int numOperands;
    /** Creates new DummyFunctionPtg */
    public DummyFunctionPtg() {
    }
    
    public DummyFunctionPtg(String pName,int pNumOperands) {
        name=pName;
        numOperands = pNumOperands;
    }
   
    public int getType() {
        return -1;
    }
    
    public int getNumberOfOperands() {
        return numOperands;
    }
    public String getName() {
        return name;
    }
    
    public String toFormulaString() {
        return getName()+getNumberOfOperands();
    }
    
    public String toFormulaString(Ptg[] operands) {
        StringBuffer buf = new StringBuffer();
        buf.append(getName()+"(");
        for (int i=0;i<operands.length;i++) {
            buf.append(operands[i].toFormulaString());
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
    }
    
    public int getSize() {
        return 0;
    }
    
    public void manipulate(List source, List results, int pos) {
    }
  
}
