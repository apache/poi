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
 * Helper functions for the record transformations. TODO: Change this to
 * javascript in the style sheet.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class RecordUtil
{
    public static String getFieldName(int position, String name, int padTo)
    {
        StringBuffer fieldName = new StringBuffer("field_" + position + "_");
        toIdentifier(name, fieldName);
        pad(fieldName, padTo);

        return fieldName.toString();
    }

    protected static StringBuffer pad(StringBuffer fieldName, int padTo)
    {
        for (int i = fieldName.length(); i < padTo; i++)
            fieldName.append(' ');
        return fieldName;
    }

    public static String getFieldName(String name, int padTo)
    {
        StringBuffer fieldName = new StringBuffer();
        toIdentifier(name, fieldName);
        pad(fieldName, padTo);

        return fieldName.toString();
    }

    public static String getFieldName1stCap(String name, int padTo)
    {
        StringBuffer fieldName = new StringBuffer();
        toIdentifier(name, fieldName);
        fieldName.setCharAt(0, Character.toUpperCase(fieldName.charAt(0)));
        pad(fieldName, padTo);

        return fieldName.toString();
    }

    private static void toIdentifier(String name, StringBuffer fieldName)
    {
        for (int i = 0; i < name.length(); i++)
        {
            if (name.charAt(i) == ' ')
                fieldName.append(Character.toUpperCase(name.charAt(++i)));
            else
                fieldName.append(name.charAt(i));
        }
    }

    private static void toConstIdentifier(String name, StringBuffer fieldName)
    {
        for (int i = 0; i < name.length(); i++)
        {
            if (name.charAt(i) == ' ')
                fieldName.append('_');
            else
                fieldName.append(Character.toUpperCase(name.charAt(i)));
        }
    }

    public static String getType(String size, String type, int padTo)
    {

        boolean wholeNumber = type.equals("bits") || type.equals("int");
        if (wholeNumber && "1".equals(size))
            return pad(new StringBuffer("byte"), padTo).toString();
        else if (wholeNumber && "2".equals(size))
            return pad(new StringBuffer("short"), padTo).toString();
        else if (type.equals("int") && "varword".equals(size))
            return pad(new StringBuffer("short[]"), padTo).toString();
        else if (wholeNumber && "4".equals(size))
            return pad(new StringBuffer("int"), padTo).toString();
        else if (type.equals("float") && "8".equals(size))
            return pad(new StringBuffer("double"), padTo).toString();
        else if (type.equals("string"))
            return pad(new StringBuffer("ExcelString"), padTo).toString();

        return "";
    }

    public static String getType1stCap(String size, String type, int padTo)
    {
        StringBuffer result;
        boolean numeric = type.equals("bits") || type.equals("int");
        if (numeric && "1".equals(size))
            result = pad(new StringBuffer("byte"), padTo);
        else if (type.equals("int") && "varword".equals(size))
            result = pad(new StringBuffer("short[]"), padTo);
        else if (numeric && "2".equals(size))
            result = pad(new StringBuffer("short"), padTo);
        else if (type.equals("string"))
            result = pad(new StringBuffer("ExcelString"), padTo);
        else
            return "";

        result.setCharAt(0, Character.toUpperCase(result.charAt(0)));

        return result.toString();
    }

    public static String getMask(int bit)
    {
	//if (bit > 1) bit--;
        int mask = (int)Math.pow(2, bit);

        return "0x" + Integer.toHexString(mask);
    }

    public static String getConstName(String parentName, String constName, int padTo)
    {
        StringBuffer fieldName = new StringBuffer();
        toConstIdentifier(parentName, fieldName);
        fieldName.append('_');
        toConstIdentifier(constName, fieldName);
        pad(fieldName, padTo);
        return fieldName.toString();
    }

}
