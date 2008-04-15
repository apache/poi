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

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

import org.apache.poi.ss.usermodel.Workbook;
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
    
    public static Ptg createPtg(RecordInputStream in)
    {
        byte id     = in.readByte();
        Ptg  retval = null;

        switch (id)
        {
             case ExpPtg.sid :                  // 0x01
                 retval = new ExpPtg(in);
                 break;
 
             case AddPtg.sid :                  // 0x03
                 retval = new AddPtg(in);
                 break;
       	  
             case SubtractPtg.sid :             // 0x04
                 retval = new SubtractPtg(in);
                 break;
      	  
             case MultiplyPtg.sid :             // 0x05
                 retval = new MultiplyPtg(in);
                 break;
        	  
             case DividePtg.sid :               // 0x06
        	      retval = new DividePtg(in);
        	      break;
        	  
             case PowerPtg.sid :                // 0x07
                 retval = new PowerPtg(in);
                 break;
       	  
             case ConcatPtg.sid :               // 0x08
                 retval = new ConcatPtg(in);
        	                  break;
 
             case LessThanPtg.sid:              // 0x09
                 retval = new LessThanPtg(in);
        	                  break;
 
              case LessEqualPtg.sid :            // 0x0a
                 retval = new LessEqualPtg(in);
        	                  break;
 
             case EqualPtg.sid :                // 0x0b
                 retval = new EqualPtg(in);
        	                  break;
        	  
             case GreaterEqualPtg.sid :         // 0x0c
                 retval = new GreaterEqualPtg(in);
        	                  break;
        	  
             case GreaterThanPtg.sid :          // 0x0d
                 retval = new GreaterThanPtg(in);
        	                  break;
 
             case NotEqualPtg.sid :             // 0x0e
                 retval = new NotEqualPtg(in);
        	                  break;
 
             case IntersectionPtg.sid :         // 0x0f
                 retval = new IntersectionPtg(in);
        	                  break;
              case UnionPtg.sid :                // 0x10
                 retval = new UnionPtg(in);
        	                  break;
        	  
             case RangePtg.sid :                // 0x11
                 retval = new RangePtg(in);
        	                  break;
        	  
             case UnaryPlusPtg.sid :            // 0x12
                 retval = new UnaryPlusPtg(in);
        	                  break;
        	  
             case UnaryMinusPtg.sid :           // 0x13
                 retval = new UnaryMinusPtg(in);
        	                  break;
        	  
             case PercentPtg.sid :              // 0x14
                 retval = new PercentPtg(in);
        	                  break;
        	  
             case ParenthesisPtg.sid :          // 0x15
                 retval = new ParenthesisPtg(in);
        	                  break;
 
             case MissingArgPtg.sid :           // 0x16
                 retval = new MissingArgPtg(in);
        	                  break;
 
             case StringPtg.sid :               // 0x17
                retval = new StringPtg(in);
                break;
 
             case AttrPtg.sid :                 // 0x19
             case 0x1a :
                 retval = new AttrPtg(in);
        	                  break;
        	  
             case ErrPtg.sid :                  // 0x1c
                 retval = new ErrPtg(in);
        	                  break;
 
             case BoolPtg.sid :                 // 0x1d
                retval = new BoolPtg(in);
                break;
 
             case IntPtg.sid :                  // 0x1e
                 retval = new IntPtg(in);
        	                  break;
 
             case NumberPtg.sid :               // 0x1f
        	      retval = new NumberPtg(in);
        	      break;
        	  
             case ArrayPtg.sid :                // 0x20
             	retval = new ArrayPtg(in);
             	break;
             case ArrayPtgV.sid :               // 0x40
             	retval = new ArrayPtgV(in);
             	break;
             case ArrayPtgA.sid :               // 0x60
             	retval = new ArrayPtgA(in);
             	break;
        	  
             case FuncPtg.sid :                 // 0x21
             case FuncPtg.sid + 0x20 :          // 0x41
             case FuncPtg.sid + 0x40 :          // 0x61
                 retval = new FuncPtg(in);
                 break;
        	  
             case FuncVarPtg.sid :              // 0x22
             case FuncVarPtg.sid + 0x20 :       // 0x42
             case FuncVarPtg.sid + 0x40 :       // 0x62
                 retval = new FuncVarPtg(in);
        	                  break;
        	  
             case ReferencePtg.sid :            // 0x24  
                 retval = new ReferencePtg(in);
        	                  break;
             case RefAPtg.sid :                 // 0x64
                 retval = new RefAPtg(in);
                 break;   
             case RefVPtg.sid :                 // 0x44
                 retval = new RefVPtg(in);
                 break;   
             case RefNAPtg.sid :                // 0x6C
                 retval = new RefNAPtg(in);
                 break;
             case RefNPtg.sid :                 // 0x2C
                 retval = new RefNPtg(in);
                 break;
             case RefNVPtg.sid :                // 0x4C
                 retval = new RefNVPtg(in);
                 break;           	                  
        	  
             case AreaPtg.sid :                 // 0x25          
                 retval = new AreaPtg(in);
        	                  break;
             case AreaVPtg.sid:                 // 0x45
                 retval = new AreaVPtg(in);
                 break;
             case AreaAPtg.sid:                 // 0x65
                 retval = new AreaAPtg(in);
                 break;
             case AreaNAPtg.sid :               // 0x6D
                 retval = new AreaNAPtg(in);
                  break;
             case AreaNPtg.sid :                // 0x2D
                 retval = new AreaNPtg(in);
                 break;
             case AreaNVPtg.sid :               // 0x4D
                retval = new AreaNVPtg(in);
                break;
        	  
             case MemAreaPtg.sid :              // 0x26
             case MemAreaPtg.sid + 0x40 :       // 0x46
             case MemAreaPtg.sid + 0x20 :       // 0x66
                 retval = new MemAreaPtg(in);
                 break;
        	  
             case MemErrPtg.sid :               // 0x27
             case MemErrPtg.sid + 0x20 :        // 0x47
             case MemErrPtg.sid + 0x40 :        // 0x67
                 retval = new MemErrPtg(in);
        	                  break;
        	  
             case MemFuncPtg.sid :              // 0x29
                 retval = new MemFuncPtg(in);
                 break;
        	  
             case RefErrorPtg.sid :             // 0x2a
             case RefErrorPtg.sid + 0x20 :      // 0x4a
             case RefErrorPtg.sid + 0x40 :      // 0x6a
                 retval = new RefErrorPtg(in);
        	                  break;
        	  
             case AreaErrPtg.sid :              // 0x2b
             case AreaErrPtg.sid + 0x20 :       // 0x4b
             case AreaErrPtg.sid + 0x40 :       // 0x6b
                 retval = new AreaErrPtg(in);
        	                  break;
        	  
             case NamePtg.sid :                 // 0x23
             case NamePtg.sid + 0x20 :          // 0x43
             case NamePtg.sid + 0x40 :          // 0x63
                 retval = new NamePtg(in);
                 break;
        	  
             case NameXPtg.sid :                // 0x39
             case NameXPtg.sid + 0x20 :         // 0x45
             case NameXPtg.sid + 0x40 :         // 0x79
                 retval = new NameXPtg(in);
        	                  break;
 
             case Area3DPtg.sid :               // 0x3b
             case Area3DPtg.sid + 0x20 :        // 0x5b
             case Area3DPtg.sid + 0x40 :        // 0x7b
                 retval = new Area3DPtg(in);
        	                  break;
 
             case Ref3DPtg.sid :                // 0x3a
             case Ref3DPtg.sid + 0x20:          // 0x5a
             case Ref3DPtg.sid + 0x40:          // 0x7a
                 retval = new Ref3DPtg(in);
        	                  break;
 
             case DeletedRef3DPtg.sid:          // 0x3c
             case DeletedRef3DPtg.sid + 0x20:   // 0x5c
             case DeletedRef3DPtg.sid + 0x40:   // 0x7c
                 retval = new DeletedRef3DPtg(in);
        	                  break;
        	  
             case DeletedArea3DPtg.sid :        // 0x3d
             case DeletedArea3DPtg.sid + 0x20 : // 0x5d
             case DeletedArea3DPtg.sid + 0x40 : // 0x7d
                 retval = new DeletedArea3DPtg(in);
                 break;
                 
             case 0x00:
            	 retval = new UnknownPtg();
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
        } else {
            retval.setClass(CLASS_REF);
        }

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
