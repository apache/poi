
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.hdf.generator;

import org.apache.poi.generator.FieldIterator;

/**
 * This class overrides FieldIterator to handle HDF specific types
 */
public class HDFFieldIterator extends FieldIterator
{

    public HDFFieldIterator()
    {
    }
    public String fillDecoder(String size, String type)
    {

        String result = "";

        if (type.equals("short[]"))
            result = "LittleEndian.getSimpleShortArray(data, 0x" + Integer.toHexString(offset) + " + offset, size)";
        else if (type.equals("byte[]"))
            result = "LittleEndian.getByteArray(data, 0x" + Integer.toHexString(offset) + " + offset, size)";
        if (size.equals("2"))
            result = "LittleEndian.getShort(data, 0x" + Integer.toHexString(offset) + " + offset)";
        else if (size.equals("4"))
            result = "LittleEndian.getInt(data, 0x" + Integer.toHexString(offset) + " + offset)";
        else if (size.equals("1"))
            result = "data[ 0x" + Integer.toHexString(offset) + " + offset ]";
        else if (type.equals("double"))
            result = "LittleEndian.getDouble(data, 0x" + Integer.toHexString(offset) + " + offset)";

        try
        {
            offset += Integer.parseInt(size);
        }
        catch (NumberFormatException ignore)
        {
        }
        return result;
    }
}
