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

package org.apache.poi.hssf.record.formula;

import org.apache.poi.util.LittleEndian;

import org.apache.poi.hssf.util.SheetReferences;

/**
 * Number
 * Stores a String value in a formula value stored in the format <length 2 bytes>char[]
 * @author  Werner Froidevaux
 * @author Jason Height (jheight at chariot dot net dot au)
 */

public class StringPtg
    extends Ptg
{
    public final static int  SIZE = 9;
    public final static byte sid  = 0x17;
    private String            field_1_value;

    private StringPtg() {
      //Required for clone methods
    }

    /** Create a StringPtg from a byte array read from disk */
    public StringPtg(byte [] data, int offset)
    {
        setValue(new String(data, offset+3, data[offset+1] + 256*data[offset+2]));
    }

    /** Create a StringPtg from a string representation of  the number
     *  Number format is not checked, it is expected to be validated in the parser
     *   that calls this method.
     *  @param value : String representation of a floating point number
     */
    public StringPtg(String value) {
        setValue(value);
    }


    public void setValue(String value)
    {
        field_1_value = value;
    }


    public String getValue()
    {
        return field_1_value;
    }

    public void writeBytes(byte [] array, int offset)
    {
        array[ offset + 0 ] = sid;
        array[ offset + 1 ] = (byte)(getValue().length() % 256);
        array[ offset + 2 ] = (byte)(getValue().length() / 256);
        System.arraycopy(getValue().getBytes(), 0, array, offset + 3, getValue().length());
    }

    public int getSize()
    {
        return field_1_value.length() + 3;
    }

    public String toFormulaString(SheetReferences refs)
    {
        return "\""+getValue()+"\"";
    }
    public byte getDefaultOperandClass() {
       return Ptg.CLASS_VALUE;
   }

   public Object clone() {
     StringPtg ptg = new StringPtg();
     ptg.field_1_value = field_1_value;
     return ptg;
   }

}

