package org.apache.poi.hssf.record.formula;
import org.apache.poi.util.LittleEndian;
public class FuncVarPtg extends AbstractFunctionPtg{
    
    public final static short sid  = 0x22;
    
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
    protected FuncVarPtg(String pName, byte pNumOperands) {
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
    
    
}
