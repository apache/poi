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
package org.apache.poi.hwpf.model.types;


import org.apache.poi.util.BitField;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * The SHD80 is a substructure of the CHP and PAP, and TC for Word 97. <p>Class
        and fields descriptions are quoted from
        Microsoft Office Word 97-2007 Binary File Format
    
 * <p>
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/types/definitions.
 * <p>
 * This class is internal. It content or properties may change without notice 
 * due to changes in our knowledge of internal Microsoft Word binary structures.

 * @author Sergey Vladimirov; according to Microsoft Office Word 97-2007 Binary File Format
        Specification [*.doc]
    
 */
@Internal
public abstract class SHD80AbstractType
{

    protected short field_1_value;
    /**/private static BitField icoFore = new BitField(0x001F);
    /**/private static BitField icoBack = new BitField(0x03E0);
    /**/private static BitField ipat = new BitField(0xFC00);

    protected SHD80AbstractType()
    {
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_value                  = LittleEndian.getShort(data, 0x0 + offset);
    }

    public void serialize( byte[] data, int offset )
    {
        LittleEndian.putShort(data, 0x0 + offset, (short)field_1_value);
    }

    /**
     * Size of record
     */
    public static int getSize()
    {
        return 0 + 2;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[SHD80]\n");
        builder.append("    .value                = ");
        builder.append(" (").append(getValue()).append(" )\n");
        builder.append("         .icoFore                  = ").append(getIcoFore()).append('\n');
        builder.append("         .icoBack                  = ").append(getIcoBack()).append('\n');
        builder.append("         .ipat                     = ").append(getIpat()).append('\n');

        builder.append("[/SHD80]\n");
        return builder.toString();
    }

    /**
     * Get the value field for the SHD80 record.
     */
    @Internal
    public short getValue()
    {
        return field_1_value;
    }

    /**
     * Set the value field for the SHD80 record.
     */
    @Internal
    public void setValue( short field_1_value )
    {
        this.field_1_value = field_1_value;
    }

    /**
     * Sets the icoFore field value.
     * Foreground color
     */
    @Internal
    public void setIcoFore( byte value )
    {
        field_1_value = (short)icoFore.setValue(field_1_value, value);
    }

    /**
     * Foreground color
     * @return  the icoFore field value.
     */
    @Internal
    public byte getIcoFore()
    {
        return ( byte )icoFore.getValue(field_1_value);
    }

    /**
     * Sets the icoBack field value.
     * Background color
     */
    @Internal
    public void setIcoBack( byte value )
    {
        field_1_value = (short)icoBack.setValue(field_1_value, value);
    }

    /**
     * Background color
     * @return  the icoBack field value.
     */
    @Internal
    public byte getIcoBack()
    {
        return ( byte )icoBack.getValue(field_1_value);
    }

    /**
     * Sets the ipat field value.
     * Shading pattern
     */
    @Internal
    public void setIpat( byte value )
    {
        field_1_value = (short)ipat.setValue(field_1_value, value);
    }

    /**
     * Shading pattern
     * @return  the ipat field value.
     */
    @Internal
    public byte getIpat()
    {
        return ( byte )ipat.getValue(field_1_value);
    }

}  // END OF CLASS
