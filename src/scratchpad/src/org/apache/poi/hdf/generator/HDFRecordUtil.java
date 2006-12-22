
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
