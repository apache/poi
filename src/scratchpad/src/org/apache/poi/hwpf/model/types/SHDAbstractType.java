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


import org.apache.poi.hwpf.model.Colorref;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * The SHD is a substructure of the CHP, PAP, and TC for Word 2000. <p>Class
        and
        fields descriptions are quoted from Microsoft Office Word 97-2007 Binary File Format
    
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
public abstract class SHDAbstractType
{

    protected Colorref field_1_cvFore;
    protected Colorref field_2_cvBack;
    protected int field_3_ipat;

    protected SHDAbstractType()
    {
        this.field_1_cvFore = new Colorref();
        this.field_2_cvBack = new Colorref();
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_cvFore                 = new Colorref(data, 0x0 + offset);
        field_2_cvBack                 = new Colorref(data, 0x4 + offset);
        field_3_ipat                   = LittleEndian.getShort(data, 0x8 + offset);
    }

    public void serialize( byte[] data, int offset )
    {
        field_1_cvFore.serialize(data, 0x0 + offset);
        field_2_cvBack.serialize(data, 0x4 + offset);
        LittleEndian.putShort(data, 0x8 + offset, (short)field_3_ipat);
    }

    /**
     * Size of record
     */
    public static int getSize()
    {
        return 0 + 4 + 4 + 2;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[SHD]\n");
        builder.append("    .cvFore               = ");
        builder.append(" (").append(getCvFore()).append(" )\n");
        builder.append("    .cvBack               = ");
        builder.append(" (").append(getCvBack()).append(" )\n");
        builder.append("    .ipat                 = ");
        builder.append(" (").append(getIpat()).append(" )\n");

        builder.append("[/SHD]\n");
        return builder.toString();
    }

    /**
     * 24-bit foreground color.
     */
    @Internal
    public Colorref getCvFore()
    {
        return field_1_cvFore;
    }

    /**
     * 24-bit foreground color.
     */
    @Internal
    public void setCvFore( Colorref field_1_cvFore )
    {
        this.field_1_cvFore = field_1_cvFore;
    }

    /**
     * 24-bit background color.
     */
    @Internal
    public Colorref getCvBack()
    {
        return field_2_cvBack;
    }

    /**
     * 24-bit background color.
     */
    @Internal
    public void setCvBack( Colorref field_2_cvBack )
    {
        this.field_2_cvBack = field_2_cvBack;
    }

    /**
     * Shading pattern.
     */
    @Internal
    public int getIpat()
    {
        return field_3_ipat;
    }

    /**
     * Shading pattern.
     */
    @Internal
    public void setIpat( int field_3_ipat )
    {
        this.field_3_ipat = field_3_ipat;
    }

}  // END OF CLASS
