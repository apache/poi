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


package org.apache.poi.generator;

/**
 * For iterating through our fields.  Todo: Change this to javascript in the style sheet.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class FieldIterator
{
    protected int offset;

    public FieldIterator()
    {
    }

    public void init(org.apache.xalan.extensions.XSLProcessorContext context,
                     org.apache.xalan.templates.ElemExtensionCall extElem)
    {
        offset = 0;
    }

    /** 
     * This utility function returns a fill method entry for a given field
     *
     * @param size - how big of an "int" or the name of the size field for a string
     * @param type - int or string
     */    
    public String fillDecoder(String size, String type)
    {
        String javaType = RecordUtil.getType(size, type, 0);

        String result = "";
        if (javaType.equals("short"))
            result = "LittleEndian.getShort(data, 0x" + Integer.toHexString(offset) + " + offset)";
        else if (javaType.equals("short[]"))
            result = "LittleEndian.getShortArray(data, 0x" + Integer.toHexString(offset) + " + offset)";
        else if (javaType.equals("int"))
            result = "LittleEndian.getInt(data, 0x" + Integer.toHexString(offset) + " + offset)";
        else if (javaType.equals("byte"))
            result = "data[ 0x" + Integer.toHexString(offset) + " + offset ]";
        else if (javaType.equals("double"))
            result = "LittleEndian.getDouble(data, 0x" + Integer.toHexString(offset) + " + offset)";
        else if (javaType.equals("String") && !type.equals("hbstring"))
            result = "StringUtil.getFromUnicode(data, 0x" + Integer.toHexString(offset) + " + offset,("+ size + "-1)/2)";
        else if (javaType.equals("String") && type.equals("hbstring"))
            result = "StringUtil.getFromUnicodeHigh(data, 0x" + Integer.toHexString(offset) + " + offset, ("+ size+"/2))";

        try
        {
            offset += Integer.parseInt(size);
        }
        catch (NumberFormatException ignore)
        {
        }
        return result;
    }
    

    //position(),@name,@size,@type
    public String serialiseEncoder( int fieldNumber, String fieldName, String size, String type)
    {
        String javaType = RecordUtil.getType(size, type, 0);
        String javaFieldName = RecordUtil.getFieldName(fieldNumber,fieldName,0);

        String result = "";
        if (javaType.equals("short"))
            result = "LittleEndian.putShort(data, " + (offset+4) + " + offset, " + javaFieldName + ");";
        else if (javaType.equals("short[]"))
            result = "LittleEndian.putShortArray(data, " + (offset+4) + " + offset, " + javaFieldName + ");";
        else if (javaType.equals("int"))
            result = "LittleEndian.putInt(data, " + (offset+4) + " + offset, " + javaFieldName + ");";
        else if (javaType.equals("byte"))
            result = "data[ " + (offset+4) + " + offset ] = " + javaFieldName + ";";
        else if (javaType.equals("double"))
            result = "LittleEndian.putDouble(data, " + (offset+4) + " + offset, " + javaFieldName + ");";
        else if (javaType.equals("String") && !type.equals("hbstring"))
            result = "StringUtil.putUncompressedUnicode("+ javaFieldName +", data, offset+4);";
        else if (javaType.equals("String") && type.equals("hbstring"))
            result = "StringUtil.putUncompressedUnicodeHigh("+ javaFieldName +", data, "+(offset+4)+" + offset);";
        

        try
        {
            offset += Integer.parseInt(size);
        }
        catch (NumberFormatException ignore)
        {
        }
        return result;

    }

    public String calcSize( int fieldNumber, String fieldName, String size, String type)
    {
        String result = fieldNumber == 1 ? "" : " + ";
        if ("var".equals(size))
        {
            String javaFieldName = RecordUtil.getFieldName(fieldNumber,fieldName,0);
            return result + " ("+javaFieldName + ".length() *2)";
        }
        else if ("varword".equals(size))
        {
            String javaFieldName = RecordUtil.getFieldName(fieldNumber,fieldName,0);
            return result + javaFieldName + ".length * 2 + 2";
        } else 
        {
            return result + size;
        }
    }
    

}



