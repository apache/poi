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


import java.util.Objects;

import org.apache.poi.hwpf.model.Colorref;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * The Shd structure specifies the colors and pattern that are used for background shading.
 */
@Internal
public abstract class SHDAbstractType {

    protected Colorref field_1_cvFore;
    protected Colorref field_2_cvBack;
    protected int field_3_ipat;

    protected SHDAbstractType() {
        field_1_cvFore = new Colorref();
        field_2_cvBack = new Colorref();
    }

    protected SHDAbstractType(SHDAbstractType other) {
        field_1_cvFore = (other.field_1_cvFore == null) ? null : other.field_1_cvFore.copy();
        field_2_cvBack = (other.field_2_cvBack == null) ? null : other.field_2_cvBack.copy();
        field_3_ipat = other.field_3_ipat;
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_cvFore                 = new Colorref( data, 0x0 + offset );
        field_2_cvBack                 = new Colorref( data, 0x4 + offset );
        field_3_ipat                   = LittleEndian.getShort( data, 0x8 + offset );
    }

    public void serialize( byte[] data, int offset )
    {
        field_1_cvFore.serialize( data, 0x0 + offset );
        field_2_cvBack.serialize( data, 0x4 + offset );
        LittleEndian.putUShort( data, 0x8 + offset, field_3_ipat );
    }

    public byte[] serialize()
    {
        final byte[] result = new byte[ getSize() ];
        serialize( result, 0 );
        return result;
    }

    /**
     * Size of record
     */
    public static int getSize()
    {
        return 0 + 4 + 4 + 2;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        SHDAbstractType other = (SHDAbstractType) obj;
        if ( field_1_cvFore != other.field_1_cvFore )
            return false;
        if ( field_2_cvBack != other.field_2_cvBack )
            return false;
        if ( field_3_ipat != other.field_3_ipat )
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field_1_cvFore,field_2_cvBack,field_3_ipat);
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
     * A COLORREF that specifies the foreground color of ipat.
     */
    @Internal
    public Colorref getCvFore()
    {
        return field_1_cvFore;
    }

    /**
     * A COLORREF that specifies the foreground color of ipat.
     */
    @Internal
    public void setCvFore( Colorref field_1_cvFore )
    {
        this.field_1_cvFore = field_1_cvFore;
    }

    /**
     * A COLORREF that specifies the background color of ipat.
     */
    @Internal
    public Colorref getCvBack()
    {
        return field_2_cvBack;
    }

    /**
     * A COLORREF that specifies the background color of ipat.
     */
    @Internal
    public void setCvBack( Colorref field_2_cvBack )
    {
        this.field_2_cvBack = field_2_cvBack;
    }

    /**
     * An Ipat that specifies the pattern used for shading.
     */
    @Internal
    public int getIpat()
    {
        return field_3_ipat;
    }

    /**
     * An Ipat that specifies the pattern used for shading.
     */
    @Internal
    public void setIpat( int field_3_ipat )
    {
        this.field_3_ipat = field_3_ipat;
    }

}
