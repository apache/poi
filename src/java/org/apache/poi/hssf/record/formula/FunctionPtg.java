package org.apache.poi.hssf.record.formula;

import java.util.List;
import java.util.ArrayList;
import org.apache.poi.util.LittleEndian;
/**
 * This class provides functions with variable arguments.  
 * @author  Avik Sengupta
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 
 */
public class FunctionPtg extends OperationPtg {
    public final static short sid  = 0x22;
    private final static int  SIZE = 4;    
    
    private byte field_1_num_args;
    private short field_2_fnc_index;
    
    //private String name;
    //private int numOperands;
    /** Creates new DummyFunctionPtg */
    public FunctionPtg() {
    }
    
    public FunctionPtg(byte[] data, int offset) {
        offset++;
        field_1_num_args = data[ offset + 0 ];
        field_2_fnc_index  = LittleEndian.getShort(data,offset + 1 );
        
    }
    
    
    public FunctionPtg(String pName, byte pNumOperands) {
        field_1_num_args = pNumOperands;
        field_2_fnc_index = lookupIndex(pName);
        
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer
        .append("<FunctionPtg>").append("\n")
        .append("   field_1_num_args=").append(field_1_num_args).append("\n")
        .append("      name         =").append(lookupName(field_2_fnc_index)).append("\n")
        .append("   field_2_fnc_index=").append(field_2_fnc_index).append("\n")
        .append("</FunctionPtg>");
        return buffer.toString();
    }
   
    public int getType() {
        return -1;
    }   
    
    public int getNumberOfOperands() {
        return field_1_num_args;
    }
    
    public short getFunctionIndex() {
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
        array[offset+0]=sid;
        array[offset+1]=field_1_num_args;
        LittleEndian.putShort(array,offset+2,field_2_fnc_index);
    }
    
    public int getSize() {
        return SIZE;
    }
    
    private String lookupName(short index) {
        String retval = null;
        switch (index) {
            case 5: 
                retval="AVERAGE";
            break;
        }
        return retval; //for now always return "SUM"
    }
    
    private short lookupIndex(String name) {
        short retval=0;
        if (name.equals("AVERAGE")) {
            retval=(short)5;
        }
        return retval; //for now just return SUM everytime...
    }
    
    /**
     * Produces the function table hashmap
     */
    private static List produceHash() {
        List list = new ArrayList(349);
        list.add(0,"COUNT");
        list.add(2,"ISNA");
        list.add(3,"ISERROR");
        list.add(4,"SUM");
        list.add(5,"AVERAGE");
        list.add(6,"MIN");
        list.add(7,"MAX");
        list.add(8,"ROW");
        list.add(9,"COLUMN");
        list.add(10,"NA");
        list.add(11,"NPV");
        list.add(12,"STDEV");
        list.add(13,"DOLLAR");
        list.add(14,"FIXED");
        list.add(15,"SIN");
        list.add(16,"COS");
        list.add(17,"TAN");
        list.add(18,"ATAN");
        list.add(19,"PI");
        list.add(20,"SQRT");
        list.add(21,"EXP");
        list.add(22,"LN");
        list.add(23,"LOG10");
        list.add(24,"ABS");
        list.add(25,"INT");
        list.add(26,"SIGN");
        list.add(27,"ROUND");
        list.add(28,"LOOKUP");
        list.add(29,"INDEX");
        list.add(30,"REPT");
        list.add(31,"MID");
        list.add(32,"LEN");
        list.add(33,"VALUE");
        list.add(34,"TRUE");
        list.add(35,"FALSE");
        list.add(36,"AND");
        list.add(37,"OR");
        list.add(38,"NOT");
        list.add(39,"MOD");
        list.add(40,"DCOUNT");
        list.add(41,"DSUM");
        list.add(42,"DAVERAGE");
        list.add(43,"DMIN");
        list.add(44,"DMAX");
        list.add(45,"DSTDEV");
        list.add(46,"VAR");
        list.add(47,"DVAR");
        list.add(48,"TEXT");
        list.add(49,"LINEST");
        list.add(50,"TREND");
        list.add(51,"LOGEST");
        list.add(52,"GROWTH");
        list.add(53,"GOTO");
        list.add(54,"HALT");
        list.add(56,"PV");
        list.add(57,"FV");
        list.add(58,"NPER");
        list.add(59,"PMT");
        list.add(60,"RATE");
        list.add(61,"MIRR");
        list.add(62,"IRR");
        list.add(63,"RAND");
        list.add(64,"MATCH");
        list.add(65,"DATE");
        list.add(66,"TIME");
        list.add(67,"DAY");
        list.add(68,"MONTH");
        list.add(69,"YEAR");
        list.add(70,"WEEKDAY");
        list.add(71,"HOUR");
        list.add(72,"MINUTE");
        list.add(73,"SECOND");
        list.add(74,"NOW");
        list.add(75,"AREAS");
        list.add(76,"ROWS");
        list.add(77,"COLUMNS");
        list.add(78,"OFFSET");
        list.add(79,"ABSREF");
        list.add(80,"RELREF");
        list.add(81,"ARGUMENT");
        return list;
    }
}
