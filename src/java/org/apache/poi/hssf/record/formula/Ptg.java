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

import java.util.List;
import java.util.ArrayList;

import org.apache.poi.hssf.model.Workbook;

/**
 *
 * @author  andy
 * @author avik
 * @author Jason Height (jheight at chariot dot net dot au)
 */

public abstract class Ptg
{

        
    /** convert infix order ptg list to rpn order ptg list
     * @return List ptgs in RPN order
     * @param infixPtgs List of ptgs in infix order
     */
    
    /* DO NOT REMOVE
     *we keep this method in case we wish to change the way we parse
     *It needs a getPrecedence in OperationsPtg
    
    public static List ptgsToRpn(List infixPtgs) {
        java.util.Stack operands = new java.util.Stack();
        java.util.List retval = new java.util.Stack();
        
        java.util.ListIterator i = infixPtgs.listIterator();
        Object p;
        OperationPtg o ;
        boolean weHaveABracket = false;
        while (i.hasNext()) {
            p=i.next();
            if (p instanceof OperationPtg) {
                if (p instanceof ParenthesisPtg) {
                    if (!weHaveABracket) {
                        operands.push(p);
                        weHaveABracket = true;
                    } else {
                        o = (OperationPtg) operands.pop();
                        while (!(o instanceof ParenthesisPtg)) { 
                            retval.add(o);
                        }
                        weHaveABracket = false;
                    }
                } else {
                    
                    while  (!operands.isEmpty() && ((OperationPtg) operands.peek()).getPrecedence() >= ((OperationPtg) p).getPrecedence() ) { //TODO handle ^ since it is right associative
                        retval.add(operands.pop());
                    }
                    operands.push(p);
                }
            } else {
                retval.add(p);
            }
        }
        while (!operands.isEmpty()) {
            if (operands.peek() instanceof ParenthesisPtg ){
                //throw some error
            } else {
                retval.add(operands.pop());
            }   
        }
        return retval;
    }
    */
    
    public static Ptg createPtg(byte [] data, int offset)
    {
        byte id     = data[ offset + 0 ];
        Ptg  retval = null;

        final byte valueRef = ReferencePtg.sid + 0x20;
        final byte arrayRef = ReferencePtg.sid + 0x40;
        final byte valueFunc = FuncPtg.sid + 0x20;
        final byte arrayFunc = FuncPtg.sid + 0x40;
        final byte valueFuncVar = FuncVarPtg.sid +0x20;
        final byte arrayFuncVar = FuncVarPtg.sid+0x40;
        final byte valueArea = AreaPtg.sid + 0x20;
        final byte arrayArea = AreaPtg.sid + 0x40;

        switch (id)
        {
            case AddPtg.sid :
                retval = new AddPtg(data, offset);
                break;

            case SubtractPtg.sid :
                retval = new SubtractPtg(data, offset);
                break;

            case BoolPtg.sid:
               retval = new BoolPtg(data, offset);
               break;

            case IntPtg.sid :
                retval = new IntPtg(data, offset);
                break;

            case DividePtg.sid :
                retval = new DividePtg(data, offset);
                break;

            case MultiplyPtg.sid :
                retval = new MultiplyPtg(data, offset);
                break;

            case PowerPtg.sid :
                retval = new PowerPtg(data, offset);
                break;
 
            case EqualPtg.sid:
                retval = new EqualPtg(data, offset);
                break;
                
            case GreaterThanPtg.sid:
                retval = new GreaterThanPtg(data, offset);
                break;
                
            case LessThanPtg.sid:
                retval = new LessThanPtg(data, offset);
                break;

			   case LessEqualPtg.sid:
			       retval = new LessEqualPtg(data, offset);
			       break;
			                
			   case GreaterEqualPtg.sid:
			       retval = new GreaterEqualPtg(data, offset);
			       break;
			       
			   case NotEqualPtg.sid:
          		 retval = new NotEqualPtg(data, offset);
         		 break;
			       
            case ConcatPtg.sid :
                retval = new ConcatPtg(data, offset);
                break;

            case AreaPtg.sid :
                retval = new AreaPtg(data, offset);
                break;
            case valueArea:
                retval = new AreaPtg(data, offset);
                break;
            case arrayArea:
                retval = new AreaPtg(data, offset);
                break;
            case MemErrPtg.sid :        // 0x27       These 3 values 
            case MemErrPtg.sid+0x20 :   // 0x47       documented in 
            case MemErrPtg.sid+0x40 :   // 0x67       openOffice.org doc.
                retval = new MemErrPtg(data, offset);
                break;

            case AttrPtg.sid :
                retval = new AttrPtg(data, offset);
                break;
                
            case ReferencePtg.sid :
                retval = new ReferencePtg(data, offset);
                break;   
            case valueRef :
                retval = new ReferencePtg(data, offset);
                break;   
            case arrayRef :
                retval = new ReferencePtg(data, offset);
                break;   

            case ParenthesisPtg.sid :
                retval = new ParenthesisPtg(data, offset);
                break;

            case MemFuncPtg.sid :
                retval = new MemFuncPtg(data, offset);
                break;

            case UnionPtg.sid :
                retval = new UnionPtg(data, offset);
                break;

            case FuncPtg.sid :
                retval = new FuncPtg(data, offset);
                break;
                
            case valueFunc :
                retval = new FuncPtg(data, offset);
                break;
            case arrayFunc :
                retval = new FuncPtg(data, offset);
                break;

            case FuncVarPtg.sid :
                retval = new FuncVarPtg(data, offset);
                break;
                
            case valueFuncVar :
                retval = new FuncVarPtg(data, offset);
                break;
            case arrayFuncVar :
                retval = new FuncVarPtg(data, offset);
                break;
                
            case NumberPtg.sid :
               retval = new NumberPtg(data, offset);
               break;

            case StringPtg.sid :
               retval = new StringPtg(data, offset);
               break;

            case NamePtg.sid :            // 0x23     These 3 values
            case NamePtg.sid+0x20 :       // 0x43     documented in
            case NamePtg.sid+0x40 :       // 0x63     openOffice.org doc.

                retval = new NamePtg(data, offset);
                break;
                
            case NameXPtg.sid :            // 0x39
            case NameXPtg.sid+0x20 :       // 0x45
            case NameXPtg.sid+0x40 :       // 0x79

                retval = new NameXPtg(data, offset);
                break;

            case ExpPtg.sid :
                retval = new ExpPtg(data, offset);
                break;

            case Area3DPtg.sid :          // 0x3b     These 3 values 
             case Area3DPtg.sid+0x20 :     // 0x5b     documented in 
             case Area3DPtg.sid+0x40 :     // 0x7b     openOffice.org doc.

                retval = new Area3DPtg(data, offset);
                break;

            case Ref3DPtg.sid:            // 0x3a     These 3 values 
             case Ref3DPtg.sid+0x20:       // 0x5a     documented in 
             case Ref3DPtg.sid+0x40:       // 0x7a     openOffice.org doc.

                retval = new Ref3DPtg(data, offset);
                break;
                
            case MissingArgPtg.sid:
                retval = new MissingArgPtg(data,offset);
                break;
            case UnaryPlusPtg.sid:
                retval=new UnaryPlusPtg(data,offset);
                break;
            case UnaryMinusPtg.sid:
                retval=new UnaryMinusPtg(data,offset);
                break;

            default :

                 //retval = new UnknownPtg();
                 throw new java.lang.UnsupportedOperationException(
                        Integer.toHexString(( int ) id) + " (" + ( int ) id + ")");
        }
        
        if (id > 0x60) {
            retval.setClass(CLASS_ARRAY);
        } else if (id > 0x40) {
            retval.setClass(CLASS_VALUE);
        } else 
            retval.setClass(CLASS_REF);
       return retval;
        
    }

    public abstract int getSize();

    public final byte [] getBytes()
    {
        int    size  = getSize();
        byte[] bytes = new byte[ size ];

        writeBytes(bytes, 0);
        return bytes;
    }
    /** write this Ptg to a byte array*/
    public abstract void writeBytes(byte [] array, int offset);
    
    /**
     * return a string representation of this token alone
     */
    public abstract String toFormulaString(Workbook book);
    /**
     * dump a debug representation (hexdump) to a string
     */
    public String toDebugString() {
        byte[] ba = new byte[getSize()];
        String retval=null;
        writeBytes(ba,0);        
        try {
            retval = org.apache.poi.util.HexDump.dump(ba,0,0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retval;
    }
    
    /** Overridden toString method to ensure object hash is not printed.
     * This helps get rid of gratuitous diffs when comparing two dumps
     * Subclasses may output more relevant information by overriding this method
     **/
    public String toString(){
        return this.getClass().toString();
    }
    
    public static final byte CLASS_REF = 0x00;
    public static final byte CLASS_VALUE = 0x20;
    public static final byte CLASS_ARRAY = 0x40;
    
    protected byte ptgClass = CLASS_REF; //base ptg
    
    public void setClass(byte thePtgClass) {
        ptgClass = thePtgClass;
    }
    
    /** returns the class (REF/VALUE/ARRAY) for this Ptg */
    public byte getPtgClass() {
        return ptgClass;
    }
    
    public abstract byte getDefaultOperandClass();

    public abstract Object clone();

    
    
}
