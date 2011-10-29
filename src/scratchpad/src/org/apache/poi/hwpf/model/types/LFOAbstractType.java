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

import org.apache.poi.hwpf.model.Grfhic;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * List Format Override (LFO). <p>Class and fields descriptions are quoted from
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
public abstract class LFOAbstractType
{

    protected int field_1_lsid;
    protected int field_2_reserved1;
    protected int field_3_reserved2;
    protected byte field_4_clfolvl;
    protected byte field_5_ibstFltAutoNum;
    protected Grfhic field_6_grfhic;
    protected byte field_7_reserved3;

    protected LFOAbstractType()
    {
        this.field_6_grfhic = new Grfhic();
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_lsid                   = LittleEndian.getInt( data, 0x0 + offset );
        field_2_reserved1              = LittleEndian.getInt( data, 0x4 + offset );
        field_3_reserved2              = LittleEndian.getInt( data, 0x8 + offset );
        field_4_clfolvl                = data[ 0xc + offset ];
        field_5_ibstFltAutoNum         = data[ 0xd + offset ];
        field_6_grfhic                 = new Grfhic( data, 0xe + offset );
        field_7_reserved3              = data[ 0xf + offset ];
    }

    public void serialize( byte[] data, int offset )
    {
        LittleEndian.putInt( data, 0x0 + offset, field_1_lsid );
        LittleEndian.putInt( data, 0x4 + offset, field_2_reserved1 );
        LittleEndian.putInt( data, 0x8 + offset, field_3_reserved2 );
        data[ 0xc + offset ] = field_4_clfolvl;
        data[ 0xd + offset ] = field_5_ibstFltAutoNum;
        field_6_grfhic.serialize( data, 0xe + offset );
        data[ 0xf + offset ] = field_7_reserved3;
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
        return 0 + 4 + 4 + 4 + 1 + 1 + 1 + 1;
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
        LFOAbstractType other = (LFOAbstractType) obj;
        if ( field_1_lsid != other.field_1_lsid )
            return false;
        if ( field_2_reserved1 != other.field_2_reserved1 )
            return false;
        if ( field_3_reserved2 != other.field_3_reserved2 )
            return false;
        if ( field_4_clfolvl != other.field_4_clfolvl )
            return false;
        if ( field_5_ibstFltAutoNum != other.field_5_ibstFltAutoNum )
            return false;
        if ( field_6_grfhic == null )
        {
            if ( other.field_6_grfhic != null )
                return false;
        }
        else if ( !field_6_grfhic.equals( other.field_6_grfhic ) )
            return false;
        if ( field_7_reserved3 != other.field_7_reserved3 )
            return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + field_1_lsid;
        result = prime * result + field_2_reserved1;
        result = prime * result + field_3_reserved2;
        result = prime * result + field_4_clfolvl;
        result = prime * result + field_5_ibstFltAutoNum;
        result = prime * result + field_6_grfhic.hashCode();
        result = prime * result + field_7_reserved3;
        return result;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[LFO]\n");
        builder.append("    .lsid                 = ");
        builder.append(" (").append(getLsid()).append(" )\n");
        builder.append("    .reserved1            = ");
        builder.append(" (").append(getReserved1()).append(" )\n");
        builder.append("    .reserved2            = ");
        builder.append(" (").append(getReserved2()).append(" )\n");
        builder.append("    .clfolvl              = ");
        builder.append(" (").append(getClfolvl()).append(" )\n");
        builder.append("    .ibstFltAutoNum       = ");
        builder.append(" (").append(getIbstFltAutoNum()).append(" )\n");
        builder.append("    .grfhic               = ");
        builder.append(" (").append(getGrfhic()).append(" )\n");
        builder.append("    .reserved3            = ");
        builder.append(" (").append(getReserved3()).append(" )\n");

        builder.append("[/LFO]\n");
        return builder.toString();
    }

    /**
     * List ID of corresponding LSTF (see LSTF).
     */
    @Internal
    public int getLsid()
    {
        return field_1_lsid;
    }

    /**
     * List ID of corresponding LSTF (see LSTF).
     */
    @Internal
    public void setLsid( int field_1_lsid )
    {
        this.field_1_lsid = field_1_lsid;
    }

    /**
     * Reserved.
     */
    @Internal
    public int getReserved1()
    {
        return field_2_reserved1;
    }

    /**
     * Reserved.
     */
    @Internal
    public void setReserved1( int field_2_reserved1 )
    {
        this.field_2_reserved1 = field_2_reserved1;
    }

    /**
     * Reserved.
     */
    @Internal
    public int getReserved2()
    {
        return field_3_reserved2;
    }

    /**
     * Reserved.
     */
    @Internal
    public void setReserved2( int field_3_reserved2 )
    {
        this.field_3_reserved2 = field_3_reserved2;
    }

    /**
     * Count of levels whose format is overridden (see LFOLVL).
     */
    @Internal
    public byte getClfolvl()
    {
        return field_4_clfolvl;
    }

    /**
     * Count of levels whose format is overridden (see LFOLVL).
     */
    @Internal
    public void setClfolvl( byte field_4_clfolvl )
    {
        this.field_4_clfolvl = field_4_clfolvl;
    }

    /**
     * Used for AUTONUM field emulation.
     */
    @Internal
    public byte getIbstFltAutoNum()
    {
        return field_5_ibstFltAutoNum;
    }

    /**
     * Used for AUTONUM field emulation.
     */
    @Internal
    public void setIbstFltAutoNum( byte field_5_ibstFltAutoNum )
    {
        this.field_5_ibstFltAutoNum = field_5_ibstFltAutoNum;
    }

    /**
     * HTML compatibility flags.
     */
    @Internal
    public Grfhic getGrfhic()
    {
        return field_6_grfhic;
    }

    /**
     * HTML compatibility flags.
     */
    @Internal
    public void setGrfhic( Grfhic field_6_grfhic )
    {
        this.field_6_grfhic = field_6_grfhic;
    }

    /**
     * Reserved.
     */
    @Internal
    public byte getReserved3()
    {
        return field_7_reserved3;
    }

    /**
     * Reserved.
     */
    @Internal
    public void setReserved3( byte field_7_reserved3 )
    {
        this.field_7_reserved3 = field_7_reserved3;
    }

}  // END OF CLASS
