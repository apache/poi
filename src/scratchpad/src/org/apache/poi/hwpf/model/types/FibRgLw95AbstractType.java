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

import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * The FibRgLw95 structure is the third section of the FIB for Word95.
    
 * <p>
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/types/definitions.
 * <p>
 * This class is internal. It content or properties may change without notice 
 * due to changes in our knowledge of internal Microsoft Word binary structures.

 * @author Sergey Vladimirov
    
 */
@Internal
public abstract class FibRgLw95AbstractType
{

    protected int field_1_cbMac;
    @Deprecated
    protected int field_2_reserved1;
    @Deprecated
    protected int field_3_reserved2;
    @Deprecated
    protected int field_4_reserved3;
    @Deprecated
    protected int field_5_reserved4;
    protected int field_6_ccpText;
    protected int field_7_ccpFtn;
    protected int field_8_ccpHdd;
    protected int field_9_ccpMcr;
    protected int field_10_ccpAtn;
    protected int field_11_ccpEdn;
    protected int field_12_ccpTxbx;
    protected int field_13_ccpHdrTxbx;
    @Deprecated
    protected int field_14_reserved5;

    protected FibRgLw95AbstractType()
    {
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_cbMac                  = LittleEndian.getInt( data, 0x0 + offset );
        field_2_reserved1              = LittleEndian.getInt( data, 0x4 + offset );
        field_3_reserved2              = LittleEndian.getInt( data, 0x8 + offset );
        field_4_reserved3              = LittleEndian.getInt( data, 0xc + offset );
        field_5_reserved4              = LittleEndian.getInt( data, 0x10 + offset );
        field_6_ccpText                = LittleEndian.getInt( data, 0x14 + offset );
        field_7_ccpFtn                 = LittleEndian.getInt( data, 0x18 + offset );
        field_8_ccpHdd                 = LittleEndian.getInt( data, 0x1c + offset );
        field_9_ccpMcr                 = LittleEndian.getInt( data, 0x20 + offset );
        field_10_ccpAtn                = LittleEndian.getInt( data, 0x24 + offset );
        field_11_ccpEdn                = LittleEndian.getInt( data, 0x28 + offset );
        field_12_ccpTxbx               = LittleEndian.getInt( data, 0x2c + offset );
        field_13_ccpHdrTxbx            = LittleEndian.getInt( data, 0x30 + offset );
        field_14_reserved5             = LittleEndian.getInt( data, 0x34 + offset );
    }

    public void serialize( byte[] data, int offset )
    {
        LittleEndian.putInt( data, 0x0 + offset, field_1_cbMac );
        LittleEndian.putInt( data, 0x4 + offset, field_2_reserved1 );
        LittleEndian.putInt( data, 0x8 + offset, field_3_reserved2 );
        LittleEndian.putInt( data, 0xc + offset, field_4_reserved3 );
        LittleEndian.putInt( data, 0x10 + offset, field_5_reserved4 );
        LittleEndian.putInt( data, 0x14 + offset, field_6_ccpText );
        LittleEndian.putInt( data, 0x18 + offset, field_7_ccpFtn );
        LittleEndian.putInt( data, 0x1c + offset, field_8_ccpHdd );
        LittleEndian.putInt( data, 0x20 + offset, field_9_ccpMcr );
        LittleEndian.putInt( data, 0x24 + offset, field_10_ccpAtn );
        LittleEndian.putInt( data, 0x28 + offset, field_11_ccpEdn );
        LittleEndian.putInt( data, 0x2c + offset, field_12_ccpTxbx );
        LittleEndian.putInt( data, 0x30 + offset, field_13_ccpHdrTxbx );
        LittleEndian.putInt( data, 0x34 + offset, field_14_reserved5 );
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
        return 0 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4;
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
        FibRgLw95AbstractType other = (FibRgLw95AbstractType) obj;
        if ( field_1_cbMac != other.field_1_cbMac )
            return false;
        if ( field_2_reserved1 != other.field_2_reserved1 )
            return false;
        if ( field_3_reserved2 != other.field_3_reserved2 )
            return false;
        if ( field_4_reserved3 != other.field_4_reserved3 )
            return false;
        if ( field_5_reserved4 != other.field_5_reserved4 )
            return false;
        if ( field_6_ccpText != other.field_6_ccpText )
            return false;
        if ( field_7_ccpFtn != other.field_7_ccpFtn )
            return false;
        if ( field_8_ccpHdd != other.field_8_ccpHdd )
            return false;
        if ( field_9_ccpMcr != other.field_9_ccpMcr )
            return false;
        if ( field_10_ccpAtn != other.field_10_ccpAtn )
            return false;
        if ( field_11_ccpEdn != other.field_11_ccpEdn )
            return false;
        if ( field_12_ccpTxbx != other.field_12_ccpTxbx )
            return false;
        if ( field_13_ccpHdrTxbx != other.field_13_ccpHdrTxbx )
            return false;
        if ( field_14_reserved5 != other.field_14_reserved5 )
            return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + field_1_cbMac;
        result = prime * result + field_2_reserved1;
        result = prime * result + field_3_reserved2;
        result = prime * result + field_4_reserved3;
        result = prime * result + field_5_reserved4;
        result = prime * result + field_6_ccpText;
        result = prime * result + field_7_ccpFtn;
        result = prime * result + field_8_ccpHdd;
        result = prime * result + field_9_ccpMcr;
        result = prime * result + field_10_ccpAtn;
        result = prime * result + field_11_ccpEdn;
        result = prime * result + field_12_ccpTxbx;
        result = prime * result + field_13_ccpHdrTxbx;
        result = prime * result + field_14_reserved5;
        return result;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[FibRgLw95]\n");
        builder.append("    .cbMac                = ");
        builder.append(" (").append(getCbMac()).append(" )\n");
        builder.append("    .reserved1            = ");
        builder.append(" (").append(getReserved1()).append(" )\n");
        builder.append("    .reserved2            = ");
        builder.append(" (").append(getReserved2()).append(" )\n");
        builder.append("    .reserved3            = ");
        builder.append(" (").append(getReserved3()).append(" )\n");
        builder.append("    .reserved4            = ");
        builder.append(" (").append(getReserved4()).append(" )\n");
        builder.append("    .ccpText              = ");
        builder.append(" (").append(getCcpText()).append(" )\n");
        builder.append("    .ccpFtn               = ");
        builder.append(" (").append(getCcpFtn()).append(" )\n");
        builder.append("    .ccpHdd               = ");
        builder.append(" (").append(getCcpHdd()).append(" )\n");
        builder.append("    .ccpMcr               = ");
        builder.append(" (").append(getCcpMcr()).append(" )\n");
        builder.append("    .ccpAtn               = ");
        builder.append(" (").append(getCcpAtn()).append(" )\n");
        builder.append("    .ccpEdn               = ");
        builder.append(" (").append(getCcpEdn()).append(" )\n");
        builder.append("    .ccpTxbx              = ");
        builder.append(" (").append(getCcpTxbx()).append(" )\n");
        builder.append("    .ccpHdrTxbx           = ");
        builder.append(" (").append(getCcpHdrTxbx()).append(" )\n");
        builder.append("    .reserved5            = ");
        builder.append(" (").append(getReserved5()).append(" )\n");

        builder.append("[/FibRgLw95]\n");
        return builder.toString();
    }

    /**
     * Get the cbMac field for the FibRgLw95 record.
     */
    @Internal
    public int getCbMac()
    {
        return field_1_cbMac;
    }

    /**
     * Set the cbMac field for the FibRgLw95 record.
     */
    @Internal
    public void setCbMac( int field_1_cbMac )
    {
        this.field_1_cbMac = field_1_cbMac;
    }

    /**
     * Get the reserved1 field for the FibRgLw95 record.
     */
    @Internal
    public int getReserved1()
    {
        return field_2_reserved1;
    }

    /**
     * Set the reserved1 field for the FibRgLw95 record.
     */
    @Internal
    public void setReserved1( int field_2_reserved1 )
    {
        this.field_2_reserved1 = field_2_reserved1;
    }

    /**
     * Get the reserved2 field for the FibRgLw95 record.
     */
    @Internal
    public int getReserved2()
    {
        return field_3_reserved2;
    }

    /**
     * Set the reserved2 field for the FibRgLw95 record.
     */
    @Internal
    public void setReserved2( int field_3_reserved2 )
    {
        this.field_3_reserved2 = field_3_reserved2;
    }

    /**
     * Get the reserved3 field for the FibRgLw95 record.
     */
    @Internal
    public int getReserved3()
    {
        return field_4_reserved3;
    }

    /**
     * Set the reserved3 field for the FibRgLw95 record.
     */
    @Internal
    public void setReserved3( int field_4_reserved3 )
    {
        this.field_4_reserved3 = field_4_reserved3;
    }

    /**
     * Get the reserved4 field for the FibRgLw95 record.
     */
    @Internal
    public int getReserved4()
    {
        return field_5_reserved4;
    }

    /**
     * Set the reserved4 field for the FibRgLw95 record.
     */
    @Internal
    public void setReserved4( int field_5_reserved4 )
    {
        this.field_5_reserved4 = field_5_reserved4;
    }

    /**
     * Get the ccpText field for the FibRgLw95 record.
     */
    @Internal
    public int getCcpText()
    {
        return field_6_ccpText;
    }

    /**
     * Set the ccpText field for the FibRgLw95 record.
     */
    @Internal
    public void setCcpText( int field_6_ccpText )
    {
        this.field_6_ccpText = field_6_ccpText;
    }

    /**
     * Get the ccpFtn field for the FibRgLw95 record.
     */
    @Internal
    public int getCcpFtn()
    {
        return field_7_ccpFtn;
    }

    /**
     * Set the ccpFtn field for the FibRgLw95 record.
     */
    @Internal
    public void setCcpFtn( int field_7_ccpFtn )
    {
        this.field_7_ccpFtn = field_7_ccpFtn;
    }

    /**
     * Get the ccpHdd field for the FibRgLw95 record.
     */
    @Internal
    public int getCcpHdd()
    {
        return field_8_ccpHdd;
    }

    /**
     * Set the ccpHdd field for the FibRgLw95 record.
     */
    @Internal
    public void setCcpHdd( int field_8_ccpHdd )
    {
        this.field_8_ccpHdd = field_8_ccpHdd;
    }

    /**
     * Get the ccpMcr field for the FibRgLw95 record.
     */
    @Internal
    public int getCcpMcr()
    {
        return field_9_ccpMcr;
    }

    /**
     * Set the ccpMcr field for the FibRgLw95 record.
     */
    @Internal
    public void setCcpMcr( int field_9_ccpMcr )
    {
        this.field_9_ccpMcr = field_9_ccpMcr;
    }

    /**
     * Get the ccpAtn field for the FibRgLw95 record.
     */
    @Internal
    public int getCcpAtn()
    {
        return field_10_ccpAtn;
    }

    /**
     * Set the ccpAtn field for the FibRgLw95 record.
     */
    @Internal
    public void setCcpAtn( int field_10_ccpAtn )
    {
        this.field_10_ccpAtn = field_10_ccpAtn;
    }

    /**
     * Get the ccpEdn field for the FibRgLw95 record.
     */
    @Internal
    public int getCcpEdn()
    {
        return field_11_ccpEdn;
    }

    /**
     * Set the ccpEdn field for the FibRgLw95 record.
     */
    @Internal
    public void setCcpEdn( int field_11_ccpEdn )
    {
        this.field_11_ccpEdn = field_11_ccpEdn;
    }

    /**
     * Get the ccpTxbx field for the FibRgLw95 record.
     */
    @Internal
    public int getCcpTxbx()
    {
        return field_12_ccpTxbx;
    }

    /**
     * Set the ccpTxbx field for the FibRgLw95 record.
     */
    @Internal
    public void setCcpTxbx( int field_12_ccpTxbx )
    {
        this.field_12_ccpTxbx = field_12_ccpTxbx;
    }

    /**
     * Get the ccpHdrTxbx field for the FibRgLw95 record.
     */
    @Internal
    public int getCcpHdrTxbx()
    {
        return field_13_ccpHdrTxbx;
    }

    /**
     * Set the ccpHdrTxbx field for the FibRgLw95 record.
     */
    @Internal
    public void setCcpHdrTxbx( int field_13_ccpHdrTxbx )
    {
        this.field_13_ccpHdrTxbx = field_13_ccpHdrTxbx;
    }

    /**
     * Get the reserved5 field for the FibRgLw95 record.
     */
    @Internal
    public int getReserved5()
    {
        return field_14_reserved5;
    }

    /**
     * Set the reserved5 field for the FibRgLw95 record.
     */
    @Internal
    public void setReserved5( int field_14_reserved5 )
    {
        this.field_14_reserved5 = field_14_reserved5;
    }

}  // END OF CLASS
