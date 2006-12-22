
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

/**
 * <p>For iterating through our fields.</p>
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class FieldIterator
{
    protected int offset;

    public FieldIterator()
    {
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
            result = "LittleEndian.getShort(data, pos + 0x" + Integer.toHexString(offset) + " + offset)";
        else if (javaType.equals("short[]"))
            result = "LittleEndian.getShortArray(data, pos + 0x" + Integer.toHexString(offset) + " + offset)";
        else if (javaType.equals("int"))
            result = "LittleEndian.getInt(data, pos + 0x" + Integer.toHexString(offset) + " + offset)";
        else if (javaType.equals("byte"))
            result = "data[ pos + 0x" + Integer.toHexString(offset) + " + offset ]";
        else if (javaType.equals("double"))
            result = "LittleEndian.getDouble(data, pos + 0x" + Integer.toHexString(offset) + " + offset)";
        else if (javaType.equals("String") && !type.equals("hbstring"))
            result = "StringUtil.getFromUnicode(data, pos + 0x" + Integer.toHexString(offset) + " + offset,("+ size + "-1)/2)";
        else if (javaType.equals("String") && type.equals("hbstring"))
            result = "StringUtil.getFromUnicodeHigh(data, pos + 0x" + Integer.toHexString(offset) + " + offset, ("+ size+"/2))";

        try
        {
            offset += Integer.parseInt(size);
        }
        catch (NumberFormatException ignore)
        {
        }
        return result;
    }

    public String fillDecoder2(int position, String name, String size, String type)
    {
        if (type.startsWith("custom:"))
        {
            StringBuffer result = new StringBuffer();
            result.append( RecordUtil.getFieldName( position, name, 0 ) );
            result.append( " = new " );
            String javaType = type.substring( 7 );
            result.append(javaType);
            result.append( "();\n");
            result.append( "        pos += " );
            result.append(RecordUtil.getFieldName(position, name, 0))
                    .append(".fillField(data,size,pos + offset + ")
                    .append(offset)
                    .append(")");
            return result.toString();
        }
        else
        {
            return RecordUtil.getFieldName(position, name, 30) +
                    " = " + fillDecoder(size, type);
        }
    }


    //position(),@name,@size,@type
    public String serialiseEncoder( int fieldNumber, String fieldName, String size, String type)
    {
        String javaType = RecordUtil.getType(size, type, 0);
        String javaFieldName = RecordUtil.getFieldName(fieldNumber,fieldName,0);

        String result = "";
        if (type.startsWith("custom:"))
            result = "pos += " + javaFieldName + ".serializeField( pos + " + (offset+4) + " + offset, data );";
        else if (javaType.equals("short"))
            result = "LittleEndian.putShort(data, " + (offset+4) + " + offset + pos, " + javaFieldName + ");";
        else if (javaType.equals("short[]"))
            result = "LittleEndian.putShortArray(data, " + (offset+4) + " + offset + pos, " + javaFieldName + ");";
        else if (javaType.equals("int"))
            result = "LittleEndian.putInt(data, " + (offset+4) + " + offset + pos, " + javaFieldName + ");";
        else if (javaType.equals("byte"))
            result = "data[ " + (offset+4) + " + offset + pos ] = " + javaFieldName + ";";
        else if (javaType.equals("double"))
            result = "LittleEndian.putDouble(data, " + (offset+4) + " + offset + pos, " + javaFieldName + ");";
        else if (javaType.equals("String") && !type.equals("hbstring"))
            result = "StringUtil.putUncompressedUnicode("+ javaFieldName +", data, offset + pos + 4);";
        else if (javaType.equals("String") && type.equals("hbstring"))
            result = "StringUtil.putUncompressedUnicodeHigh("+ javaFieldName +", data, "+(offset+4)+" + offset + pos);";


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
        String result = " + ";
        if (type.startsWith("custom:"))
        {
            String javaFieldName = RecordUtil.getFieldName(fieldNumber, fieldName, 0);
            return result + javaFieldName + ".getSize()";
        }
        else if ("var".equals(size))
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
