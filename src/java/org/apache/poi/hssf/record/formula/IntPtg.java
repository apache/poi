
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
 * IntPtg.java
 *
 * Created on October 29, 2001, 7:37 PM
 */
package org.apache.poi.hssf.record.formula;

import org.apache.poi.util.LittleEndian;

/**
 * Integer (short intger)
 * Stores a (java) short value in a formula
 * @author  andy
 */

public class IntPtg
    extends Ptg
{
    public final static int  SIZE = 3;
    public final static byte sid  = 0x1e;
    private short            field_1_value;

    private String val;
    private int strlen = 0;
    /** Creates new IntPtg */

    public IntPtg()
    {
    }

    public IntPtg(byte [] data, int offset)
    {
        setValue(LittleEndian.getShort(data, offset + 1));
    }
    
    protected IntPtg(String formula, int offset) {
        val = parseString(formula, offset);
        if (val == null) throw new RuntimeException("WHOOAA there...thats got no int!");
        strlen=val.length();
        field_1_value = Short.parseShort(val);
    }
    

    public void setValue(short value)
    {
        field_1_value = value;
    }

    public short getValue()
    {
        return field_1_value;
    }

    public void writeBytes(byte [] array, int offset)
    {
        array[ offset + 0 ] = sid;
        LittleEndian.putShort(array, offset + 1, getValue());
    }

    public int getSize()
    {
        return SIZE;
    }

    public String toFormulaString()
    {
        return "" + getValue();
    }
    
    private static String parseString(String formula, int pos) {
        String retval = null;
        while (pos < formula.length() && Character.isWhitespace(formula.charAt(pos))) {
            pos++;
        }
        
        if (pos < formula.length()) {
            if (Character.isDigit(formula.charAt(pos)) ) {
                int numpos = pos;
                
                while (numpos < formula.length() && Character.isDigit(formula.charAt(numpos))){
                    numpos++;
                }
                
                if (numpos == formula.length() || formula.charAt(numpos) != '.') {
                    String numberstr = formula.substring(pos,numpos);
                    try {
                        int number = Short.parseShort(numberstr);
                        retval = numberstr;
                    } catch (NumberFormatException e) {
                        retval = null;
                    }
                }
            }
        }
        return retval;
        
    }
    
    public static boolean isNextStringToken(String formula, int pos) {
        return (parseString(formula,pos) != null);
    }
    
    public int getPrecedence() {
        return 5;
    }
    
    public int getStringLength() {
        return strlen;
    }    
}
