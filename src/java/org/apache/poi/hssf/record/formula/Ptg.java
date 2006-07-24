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
import java.util.Stack;

import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.RecordInputStream;

/**
 *
 * @author  andy
 * @author avik
 * @author Jason Height (jheight at chariot dot net dot au)
 */

public abstract class Ptg
{

        
    /* convert infix order ptg list to rpn order ptg list
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

    public static Stack createParsedExpressionTokens(short size,  RecordInputStream in )
    {
        Stack stack = new Stack();
        int pos = 0;
        List arrayPtgs = null;
        while ( pos < size )
        {
            Ptg ptg = Ptg.createPtg( in );
            if (ptg instanceof ArrayPtg) {
            	if (arrayPtgs == null)
            		arrayPtgs = new ArrayList(5);
            	arrayPtgs.add(ptg);
            	pos += 8;
            } else pos += ptg.getSize();
            stack.push( ptg );
        }
        if (arrayPtgs != null) {
        	for (int i=0;i<arrayPtgs.size();i++) {
        		ArrayPtg p = (ArrayPtg)arrayPtgs.get(i);
        		p.readTokenValues(in);
        	}
        }
        return stack;
    }
    
    private static Ptg createPtg(RecordInputStream in)
    {
        byte id     = in.readByte();
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
                retval = new AddPtg(in);
                break;

            case SubtractPtg.sid :
                retval = new SubtractPtg(in);
                break;

            case BoolPtg.sid:
               retval = new BoolPtg(in);
               break;

            case IntPtg.sid :
                retval = new IntPtg(in);
                break;

            case DividePtg.sid :
                retval = new DividePtg(in);
                break;

            case MultiplyPtg.sid :
                retval = new MultiplyPtg(in);
                break;

            case PowerPtg.sid :
                retval = new PowerPtg(in);
                break;
 
            case EqualPtg.sid:
                retval = new EqualPtg(in);
                break;
                
            case GreaterThanPtg.sid:
                retval = new GreaterThanPtg(in);
                break;
                
            case LessThanPtg.sid:
                retval = new LessThanPtg(in);
                break;

			   case LessEqualPtg.sid:
			       retval = new LessEqualPtg(in);
			       break;
			                
			   case GreaterEqualPtg.sid:
			       retval = new GreaterEqualPtg(in);
			       break;
			       
			   case NotEqualPtg.sid:
          		 retval = new NotEqualPtg(in);
         		 break;
			       
            case ConcatPtg.sid :
                retval = new ConcatPtg(in);
                break;
                
            case ArrayPtg.sid:
            	retval = new ArrayPtg(in);
            	break;
            case ArrayPtgV.sid:
            	retval = new ArrayPtgV(in);
            	break;            	
            case ArrayPtgA.sid:            	
            	retval = new ArrayPtgA(in);
            	break;

            case AreaPtg.sid :
                retval = new AreaPtg(in);
                break;
            case valueArea:
                retval = new AreaPtg(in);
                break;
            case arrayArea:
                retval = new AreaPtg(in);
                break;
            case MemErrPtg.sid :        // 0x27       These 3 values 
            case MemErrPtg.sid+0x20 :   // 0x47       documented in 
            case MemErrPtg.sid+0x40 :   // 0x67       openOffice.org doc.
                retval = new MemErrPtg(in);
                break;

            case AttrPtg.sid :
                retval = new AttrPtg(in);
                break;
                
            case ReferencePtg.sid :
                retval = new ReferencePtg(in);
                break;   
            case valueRef :
                retval = new ReferencePtg(in);
                break;   
            case arrayRef :
                retval = new ReferencePtg(in);
                break;   
            case RefErrorPtg.sid:
                retval = new RefErrorPtg(in);
                break;   

            case ParenthesisPtg.sid :
                retval = new ParenthesisPtg(in);
                break;

            case MemFuncPtg.sid :
                retval = new MemFuncPtg(in);
                break;

            case UnionPtg.sid :
                retval = new UnionPtg(in);
                break;

            case FuncPtg.sid :
                retval = new FuncPtg(in);
                break;
                
            case valueFunc :
                retval = new FuncPtg(in);
                break;
            case arrayFunc :
                retval = new FuncPtg(in);
                break;

            case FuncVarPtg.sid :
                retval = new FuncVarPtg(in);
                break;
                
            case valueFuncVar :
                retval = new FuncVarPtg(in);
                break;
            case arrayFuncVar :
                retval = new FuncVarPtg(in);
                break;
                
            case NumberPtg.sid :
               retval = new NumberPtg(in);
               break;

            case StringPtg.sid :
               retval = new StringPtg(in);
               break;

            case NamePtg.sid :            // 0x23     These 3 values
            case NamePtg.sid+0x20 :       // 0x43     documented in
            case NamePtg.sid+0x40 :       // 0x63     openOffice.org doc.

                retval = new NamePtg(in);
                break;
                
            case NameXPtg.sid :            // 0x39
            case NameXPtg.sid+0x20 :       // 0x45
            case NameXPtg.sid+0x40 :       // 0x79

                retval = new NameXPtg(in);
                break;

            case ExpPtg.sid :
                retval = new ExpPtg(in);
                break;

            case Area3DPtg.sid :          // 0x3b     These 3 values 
             case Area3DPtg.sid+0x20 :     // 0x5b     documented in 
             case Area3DPtg.sid+0x40 :     // 0x7b     openOffice.org doc.

                retval = new Area3DPtg(in);
                break;

            case Ref3DPtg.sid:            // 0x3a     These 3 values 
             case Ref3DPtg.sid+0x20:       // 0x5a     documented in 
             case Ref3DPtg.sid+0x40:       // 0x7a     openOffice.org doc.

                retval = new Ref3DPtg(in);
                break;
                
            case DeletedArea3DPtg.sid :      // 0x3d     
            case DeletedArea3DPtg.sid+0x20 : // 0x5d     
            case DeletedArea3DPtg.sid+0x40 : // 0x7d     

                retval = new DeletedArea3DPtg(in);
                break;

            case DeletedRef3DPtg.sid:      // 0x3c      
            case DeletedRef3DPtg.sid+0x20: // 0x5c     
            case DeletedRef3DPtg.sid+0x40: // 0x7c     

                retval = new DeletedRef3DPtg(in);
                break;
                
            case MissingArgPtg.sid:
                retval = new MissingArgPtg(in);
                break;
            case UnaryPlusPtg.sid:
                retval=new UnaryPlusPtg(in);
                break;
            case UnaryMinusPtg.sid:
                retval=new UnaryMinusPtg(in);
                break;

            default :

                 //retval = new UnknownPtg();
                 throw new java.lang.UnsupportedOperationException(" Unknown Ptg in Formula: 0x"+
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
    
    public static int serializePtgStack(Stack expression, byte[] array, int offset) {
    	int pos = 0;
    	int size = 0;
    	if (expression != null)
    		size = expression.size();

    	List arrayPtgs = null;
    	
    	for (int k = 0; k < size; k++) {
    		Ptg ptg = ( Ptg ) expression.get(k);
    		
    		ptg.writeBytes(array, pos + offset);
    		if (ptg instanceof ArrayPtg) {
    		  if (arrayPtgs == null)
    			  arrayPtgs = new ArrayList(5);
    		  arrayPtgs.add(ptg);
    		  pos += 8;
    		} else pos += ptg.getSize();
    	}
    	if (arrayPtgs != null) {
    		for (int i=0;i<arrayPtgs.size();i++) {
    			ArrayPtg p = (ArrayPtg)arrayPtgs.get(i);
    			pos += p.writeTokenValueBytes(array, pos + offset);
    		}
    	}
    	return pos;
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
