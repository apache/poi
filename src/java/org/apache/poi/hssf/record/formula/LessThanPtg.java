/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * LessThanPtg.java
 *
 * Created on January 23, 2003, 9:47 AM
 */
package org.apache.poi.hssf.record.formula;

//JDK
import java.util.List;

//POI
import org.apache.poi.hssf.model.Workbook;

/**
 * Less than operator PTG "<". The SID is taken from the 
 * Openoffice.orgs Documentation of the Excel File Format,
 * Table 3.5.7
 * @author Cameron Riley (criley at ekmail.com)
 */
public class LessThanPtg
    extends OperationPtg
{
    /** the size of the Ptg  */
    public final static int SIZE = 1;

    /** the sid for the less than operator as hex */
    public final static byte sid  = 0x09;    

    /** identifier for LESS THAN char */
    private final static String LESSTHAN = "<";

    /** 
     * Constructor. Creates new LessThanPtg 
     */
    public LessThanPtg()
    {
        //deliberately empty
    }

    /**
     * Constructor. Create a new LessThanPtg.
     * @param data the byte array to have the PTG added to
     * @param offset the offset to the PTG to.
     */
    public LessThanPtg(byte [] data, int offset)
    {
        //deliberately empty
    }
    
    /**
     * Write the sid to an array
     * @param array the array of bytes to write the sid to
     * @param offset the offset to add the sid to
     */
    public void writeBytes(byte[] array, int offset)
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
     * Get the type of PTG for Less Than
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
     */
    public String toFormulaString(Workbook book)
    {
        return LessThanPtg.LESSTHAN;
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
        buffer.append(this.LESSTHAN);
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
        return new LessThanPtg();
    }

}
