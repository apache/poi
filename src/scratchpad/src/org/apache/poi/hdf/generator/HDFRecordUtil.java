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

package org.apache.poi.hdf.generator;

import org.apache.poi.generator.RecordUtil;

/**
 * This class overrides RecordUtil to handle HDF specific types
 */
public class HDFRecordUtil extends RecordUtil
{

    public HDFRecordUtil()
    {
    }

    public static String getType(String size, String type, int padTo)
    {

        return type;
    }

    public static String getType1stCap(String size, String type, int padTo)
    {
        StringBuffer result = new StringBuffer();
        result.append(type);
        result = pad(result, padTo);
        result.setCharAt(0, Character.toUpperCase(result.charAt(0)));

        return result.toString();
    }

    public static String getBitFieldFunction(String name, String bitMask, String parentType, String withType)
    {
        String type = getBitFieldType(name, bitMask, parentType);

        String retVal = new String();
        if(withType.equals("true"))
        {
            retVal = type + " ";
        }
        if(type.equals("boolean"))
        {
            retVal += "is" + getFieldName1stCap(name, 0);
        }
        else
        {
            retVal +="get" + getFieldName1stCap(name, 0);
        }
        return retVal;

    }

    public static String getBitFieldGet(String name, String bitMask, String parentType, String parentField)
    {
        String type = getBitFieldType(name, bitMask, parentType);

        String retVal = null;

        if(type.equals("boolean"))
            retVal = name + ".isSet(" + parentField + ");";
        else
            retVal = "( " + type + " )" + name + ".getValue(" + parentField + ");";

        return retVal;

    }
    public static String getBitFieldSet(String name, String bitMask, String parentType, String parentField)
    {
        String type = getBitFieldType(name, bitMask, parentType);

        String retVal = null;

        if(type.equals("boolean"))
            retVal = "(" + parentType + ")" + getFieldName(name, 0) + ".setBoolean(" + parentField + ", value)";
        else
            retVal = "(" + parentType + ")" + getFieldName(name, 0) + ".setValue(" + parentField + ", value)";
        return retVal;
    }

    public static String getBitFieldType(String name, String bitMask, String parentType)
    {
        byte parentSize = 0;
        byte numBits = 0;
        int mask = (int)Long.parseLong(bitMask.substring(2), 16);

        if (parentType.equals("byte"))
            parentSize = 8;
        else if (parentType.equals("short"))
            parentSize = 16;
        else if (parentType.equals("int"))
            parentSize = 32;

        for (int x = 0; x < parentSize; x++)
        {
            int temp = mask;
            numBits += (temp >> x) & 0x1;
        }

        if(numBits == 1)
        {
            return "boolean";
        }
        else if (numBits < 8)
        {
            return "byte";
        }
        else if (numBits < 16)
        {
            return "short";
        }
        else
        {
            return "int";
        }
    }


}