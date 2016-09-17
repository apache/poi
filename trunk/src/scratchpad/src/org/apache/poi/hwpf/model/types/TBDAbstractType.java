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

/**
 * The TBD is a substructure of the PAP. <p>Class and fields descriptions are quoted from
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
public abstract class TBDAbstractType
{

    protected byte field_1_value;
    /**/private static BitField jc = new BitField(0x07);
    /**/private static BitField tlc = new BitField(0x38);
    /**/private static BitField reserved = new BitField(0xc0);

    protected TBDAbstractType()
    {
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_value                  = data[ 0x0 + offset ];
    }

    public void serialize( byte[] data, int offset )
    {
        data[ 0x0 + offset] = field_1_value;
    }

    /**
     * Size of record
     */
    public static int getSize()
    {
        return 0 + 1;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[TBD]\n");
        builder.append("    .value                = ");
        builder.append(" (").append(getValue()).append(" )\n");
        builder.append("         .jc                       = ").append(getJc()).append('\n');
        builder.append("         .tlc                      = ").append(getTlc()).append('\n');
        builder.append("         .reserved                 = ").append(getReserved()).append('\n');

        builder.append("[/TBD]\n");
        return builder.toString();
    }

    /**
     * Get the value field for the TBD record.
     */
    @Internal
    public byte getValue()
    {
        return field_1_value;
    }

    /**
     * Set the value field for the TBD record.
     */
    @Internal
    public void setValue( byte field_1_value )
    {
        this.field_1_value = field_1_value;
    }

    /**
     * Sets the jc field value.
     * Justification code
     */
    @Internal
    public void setJc( byte value )
    {
        field_1_value = (byte)jc.setValue(field_1_value, value);
    }

    /**
     * Justification code
     * @return  the jc field value.
     */
    @Internal
    public byte getJc()
    {
        return ( byte )jc.getValue(field_1_value);
    }

    /**
     * Sets the tlc field value.
     * Tab leader code
     */
    @Internal
    public void setTlc( byte value )
    {
        field_1_value = (byte)tlc.setValue(field_1_value, value);
    }

    /**
     * Tab leader code
     * @return  the tlc field value.
     */
    @Internal
    public byte getTlc()
    {
        return ( byte )tlc.getValue(field_1_value);
    }

    /**
     * Sets the reserved field value.
     * 
     */
    @Internal
    public void setReserved( byte value )
    {
        field_1_value = (byte)reserved.setValue(field_1_value, value);
    }

    /**
     * 
     * @return  the reserved field value.
     */
    @Internal
    public byte getReserved()
    {
        return ( byte )reserved.getValue(field_1_value);
    }

}  // END OF CLASS
