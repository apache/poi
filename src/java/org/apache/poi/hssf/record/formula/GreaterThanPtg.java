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
 * GreaterThanPtg.java
 *
 * Created on January 23, 2003, 9:47 AM
 */
package org.apache.poi.hssf.record.formula;

import java.util.List;

import org.apache.poi.hssf.model.Workbook;

/**
 * Greater than operator PTG ">"
 * @author  Cameron Riley (criley at ekmail.com)
 */
public class GreaterThanPtg
    extends OperationPtg
{
    public final static int  SIZE = 1;
    public final static byte sid  = 0x0D;    
    private final static String GREATERTHAN = ">";

    /** 
     * Constructor. Creates new GreaterThanPtg 
     */
    public GreaterThanPtg()
    {
        //deliberately empty
    }

    /**
     * Constructor. Create a new GreaterThanPtg.
     * @param data the byte array to have the PTG added to
     * @param offset the offset to the PTG to.
     */
    public GreaterThanPtg(byte [] data, int offset)
    {
        //deliberately empty
    }
    
    /**
     * Write the sid to an array
     * @param array the array of bytes to write the sid to
     * @param offset the offset to add the sid to
     */
    public void writeBytes(byte [] array, int offset)
    {
        array[ offset + 0 ] = sid;
    }

    /**
     * Get the size of the sid
     * @return int the size of the sid in terms of byte additions to an array
     */
    public int getSize()
    {
        return SIZE;
    }

    /**
     * Get the type of PTG for Greater Than
     * @return int the identifier for the type
     */
    public int getType()
    {
        return TYPE_BINARY;
    }

    /**
     * Get the number of operands for the Less than operator
     * @return int the number of operands
     */
    public int getNumberOfOperands()
    {
        return 2;
    }
    
    /** 
     * Implementation of method from Ptg 
     * @param refs the Sheet References
     */
    public String toFormulaString(Workbook book)
    {
        return this.GREATERTHAN;
    }
      
    /** 
     * Implementation of method from OperationsPtg
     * @param operands a String array of operands
     * @return String the Formula as a String
     */  
    public String toFormulaString(String[] operands) 
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append(operands[ 0 ]);
        buffer.append(this.GREATERTHAN);
        buffer.append(operands[ 1 ]);
        return buffer.toString();
    }
    
    /**
     * Get the default operands class value
     * @return byte the Ptg Class Value as a byte from the Ptg Parent object
     */
    public byte getDefaultOperandClass() 
    {
        return Ptg.CLASS_VALUE;
    }
           
    /**
     * Implementation of clone method from Object
     * @return Object a clone of this class as an Object
     */ 
    public Object clone() 
    {
        return new GreaterThanPtg();
    }
}
