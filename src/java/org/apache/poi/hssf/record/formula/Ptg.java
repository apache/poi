
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/*
 * Ptg.java
 *
 * Created on October 28, 2001, 6:30 PM
 */
package org.apache.poi.hssf.record.formula;

import java.util.List;
import java.util.ArrayList;

import org.apache.poi.hssf.util.SheetReferences;

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
    public abstract String toFormulaString(SheetReferences refs);
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
