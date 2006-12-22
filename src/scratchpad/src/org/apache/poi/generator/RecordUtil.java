
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
        

package org.apache.poi.generator;

import java.util.StringTokenizer;

/**
 * Helper functions for the record transformations. 
 *
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
public class RecordUtil
{
    private static final String CR = "\n";

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

    public static String clone(String name, String type, int pos) {
        StringBuffer fieldName = new StringBuffer();
        toIdentifier(name, fieldName);

        String javaFieldName = getFieldName(pos, name, 0);

        if (type.startsWith("custom:"))
        {
            String javaType = type.substring(7);
            return "rec." + javaFieldName + " = ((" + javaType + ")" + javaFieldName + ".clone());";
        }
        else
        {
            return "rec." + javaFieldName + " = " + javaFieldName;
        }
    }

    public static String initializeText(String size, String type)
    {
        // Removed because of wierdo initialization sequence in constructors.
//        if (type.startsWith("custom:"))
//        {
//            String javaType = type.substring( 7 );
//            return " = new " + javaType + "()";
//        }
//        else
//        {
//            return "";
//        }
        return "";
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
            return pad(new StringBuffer("String"), padTo).toString();
        else if (type.equals("hbstring"))
            return pad(new StringBuffer("String"), padTo).toString();
        else if (type.startsWith("custom:"))
        {
            int pos = type.lastIndexOf('.');
            return pad(new StringBuffer(type.substring(pos+1)), padTo)
                    .toString();
        }

        return "short";   // if we don't know, default to short
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
            result = pad(new StringBuffer("String"), padTo);
        else if (type.equals("hbstring"))
            result = pad(new StringBuffer("HighByteString"), padTo);
        
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
    
    /**
     * @return a byte array formatted string from a HexDump formatted string
     *  for example (byte)0x00,(byte)0x01 instead of 00 01
     */
    public static String getByteArrayString(String data) {
        StringTokenizer tokenizer = new StringTokenizer(data);
        StringBuffer retval = new StringBuffer();
        
        while (tokenizer.hasMoreTokens()) {
            retval.append("(byte)0x").append(tokenizer.nextToken());
            if (tokenizer.hasMoreTokens()) {
                retval.append(",");
            }
        }
        return retval.toString();
    }

    public static String getToString(String fieldName, String type, String size) {
        StringBuffer result = new StringBuffer();
        result.append("        buffer.append(\"    .");
        result.append(getFieldName(fieldName, 20));
        result.append(" = \")" + CR);
        if (type.equals("string") == false
                && type.equals("hbstring") == false
                && type.equals("float") == false
//                && type.equals("varword") == false
                && size.equals("varword") == false
                && type.startsWith("custom:") == false)
        {
            result.append("            .append(\"0x\")");
            result.append(".append(HexDump.toHex( ");
//            result.append(getType(size, type, 0));
            result.append(" get");
            result.append(getFieldName1stCap(fieldName, 0));
            result.append(" ()))" + CR);
        }
        result.append("            .append(\" (\").append( get");
        result.append(getFieldName1stCap(fieldName,0));
        result.append("() ).append(\" )\");");
        return result.toString();
    }

    public static String getRecordId(String recordName, String excelName)
    {
        if (excelName == null || excelName.equals(""))
            return recordName;
        else
            return excelName;
    }
}
