
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

/**
 *
 * @author  andy
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

    
    
    /*
    private static List ptgsToList(Class [] ptgs)
    {
        List         result = new ArrayList();
        Constructor constructor;

        for (int i = 0; i < ptgs.length; i++)
        {
            Class ptg = null;
 
            ptg = ptgs[ i ];
            try
            {
                
                constructor = ptg.getConstructor(new Class[]
                {
                    byte [].class, int.class
                });
            }
            catch (Exception illegalArgumentException)
            {
                throw new RuntimeException(
                    "Now that didn't work nicely at all (couldn't do that there list of ptgs)");
            }
            result.add(constructor);
        }
        return result;
    }*/
    

    public static Ptg createPtg(byte [] data, int offset)
    {
        byte id     = data[ offset + 0 ];
        Ptg  retval = null;
        
        final int valueRef = ReferencePtg.sid + 0x20;  //note this only matters for READ
        final int arrayRef = ReferencePtg.sid + 0x40; // excel doesn't really care which one you 
                                                      // write.  
        
        final int valueFunc = FunctionPtg.sid + 0x20;  //note this only matters for READ
        final int arrayFunc = FunctionPtg.sid + 0x40; // excel doesn't really care which one you 
                                                      // write.  

        
        switch (id)
        {

            case AddPtg.sid :
                retval = new AddPtg(data, offset);
                break;

            case SubtractPtg.sid :
                retval = new SubtractPtg(data, offset);
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
                
            case ConcatPtg.sid :
                retval = new ConcatPtg(data, offset);
                break;
                

            case AreaPtg.sid :
                retval = new AreaPtg(data, offset);
                break;

            case MemErrPtg.sid :
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

            case FunctionPtg.sid :
                retval = new FunctionPtg(data, offset);
                break;
                
            case valueFunc :
                retval = new FunctionPtg(data, offset);
                break;
                
            case arrayFunc :
                retval = new FunctionPtg(data, offset);
                break;
                
                
             case NumberPtg.sid :
                retval = new NumberPtg(data, offset);
             break;


                

            case NamePtg.sid :
                retval = new NamePtg(data, offset);
                break;

            case ExpPtg.sid :
                retval = new ExpPtg(data, offset);
                break;

            case Area3DPtg.sid :
                retval = new Area3DPtg(data, offset);
                break;

            case Ref3DPtg.sid:
                retval = new Ref3DPtg(data, offset);
                break;

            default :

                // retval = new UnknownPtg();
                throw new RuntimeException("Unknown PTG = "
                                           + Integer.toHexString(( int ) id)
                                           + " (" + ( int ) id + ")");
        }
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
    public abstract String toFormulaString();
    /**
     * dump a debug representation (hexdump) to a strnig
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
    
    
}
