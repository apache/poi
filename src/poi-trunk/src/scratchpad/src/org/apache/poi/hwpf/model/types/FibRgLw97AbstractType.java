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
 * The FibRgLw97 structure is the third section of the FIB. This contains an array of
        4-byte values. <p>Class and fields descriptions are quoted from Microsoft Office Word
        97-2007 Binary File Format and [MS-DOC] - v20110608 Word (.doc) Binary File Format
    
 * <p>
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/types/definitions.
 * <p>
 * This class is internal. It content or properties may change without notice 
 * due to changes in our knowledge of internal Microsoft Word binary structures.

 * @author Sergey Vladimirov; according to Microsoft Office Word 97-2007 Binary File Format
        Specification [*.doc] and [MS-DOC] - v20110608 Word (.doc) Binary File Format
    
 */
@Internal
public abstract class FibRgLw97AbstractType
{

    protected int field_1_cbMac;
    @Deprecated
    protected int field_2_reserved1;
    @Deprecated
    protected int field_3_reserved2;
    protected int field_4_ccpText;
    protected int field_5_ccpFtn;
    protected int field_6_ccpHdd;
    @Deprecated
    protected int field_7_reserved3;
    protected int field_8_ccpAtn;
    protected int field_9_ccpEdn;
    protected int field_10_ccpTxbx;
    protected int field_11_ccpHdrTxbx;
    @Deprecated
    protected int field_12_reserved4;
    @Deprecated
    protected int field_13_reserved5;
    @Deprecated
    protected int field_14_reserved6;
    @Deprecated
    protected int field_15_reserved7;
    @Deprecated
    protected int field_16_reserved8;
    @Deprecated
    protected int field_17_reserved9;
    @Deprecated
    protected int field_18_reserved10;
    @Deprecated
    protected int field_19_reserved11;
    @Deprecated
    protected int field_20_reserved12;
    @Deprecated
    protected int field_21_reserved13;
    @Deprecated
    protected int field_22_reserved14;

    protected FibRgLw97AbstractType()
    {
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_cbMac                  = LittleEndian.getInt( data, 0x0 + offset );
        field_2_reserved1              = LittleEndian.getInt( data, 0x4 + offset );
        field_3_reserved2              = LittleEndian.getInt( data, 0x8 + offset );
        field_4_ccpText                = LittleEndian.getInt( data, 0xc + offset );
        field_5_ccpFtn                 = LittleEndian.getInt( data, 0x10 + offset );
        field_6_ccpHdd                 = LittleEndian.getInt( data, 0x14 + offset );
        field_7_reserved3              = LittleEndian.getInt( data, 0x18 + offset );
        field_8_ccpAtn                 = LittleEndian.getInt( data, 0x1c + offset );
        field_9_ccpEdn                 = LittleEndian.getInt( data, 0x20 + offset );
        field_10_ccpTxbx               = LittleEndian.getInt( data, 0x24 + offset );
        field_11_ccpHdrTxbx            = LittleEndian.getInt( data, 0x28 + offset );
        field_12_reserved4             = LittleEndian.getInt( data, 0x2c + offset );
        field_13_reserved5             = LittleEndian.getInt( data, 0x30 + offset );
        field_14_reserved6             = LittleEndian.getInt( data, 0x34 + offset );
        field_15_reserved7             = LittleEndian.getInt( data, 0x38 + offset );
        field_16_reserved8             = LittleEndian.getInt( data, 0x3c + offset );
        field_17_reserved9             = LittleEndian.getInt( data, 0x40 + offset );
        field_18_reserved10            = LittleEndian.getInt( data, 0x44 + offset );
        field_19_reserved11            = LittleEndian.getInt( data, 0x48 + offset );
        field_20_reserved12            = LittleEndian.getInt( data, 0x4c + offset );
        field_21_reserved13            = LittleEndian.getInt( data, 0x50 + offset );
        field_22_reserved14            = LittleEndian.getInt( data, 0x54 + offset );
    }

    public void serialize( byte[] data, int offset )
    {
        LittleEndian.putInt( data, 0x0 + offset, field_1_cbMac );
        LittleEndian.putInt( data, 0x4 + offset, field_2_reserved1 );
        LittleEndian.putInt( data, 0x8 + offset, field_3_reserved2 );
        LittleEndian.putInt( data, 0xc + offset, field_4_ccpText );
        LittleEndian.putInt( data, 0x10 + offset, field_5_ccpFtn );
        LittleEndian.putInt( data, 0x14 + offset, field_6_ccpHdd );
        LittleEndian.putInt( data, 0x18 + offset, field_7_reserved3 );
        LittleEndian.putInt( data, 0x1c + offset, field_8_ccpAtn );
        LittleEndian.putInt( data, 0x20 + offset, field_9_ccpEdn );
        LittleEndian.putInt( data, 0x24 + offset, field_10_ccpTxbx );
        LittleEndian.putInt( data, 0x28 + offset, field_11_ccpHdrTxbx );
        LittleEndian.putInt( data, 0x2c + offset, field_12_reserved4 );
        LittleEndian.putInt( data, 0x30 + offset, field_13_reserved5 );
        LittleEndian.putInt( data, 0x34 + offset, field_14_reserved6 );
        LittleEndian.putInt( data, 0x38 + offset, field_15_reserved7 );
        LittleEndian.putInt( data, 0x3c + offset, field_16_reserved8 );
        LittleEndian.putInt( data, 0x40 + offset, field_17_reserved9 );
        LittleEndian.putInt( data, 0x44 + offset, field_18_reserved10 );
        LittleEndian.putInt( data, 0x48 + offset, field_19_reserved11 );
        LittleEndian.putInt( data, 0x4c + offset, field_20_reserved12 );
        LittleEndian.putInt( data, 0x50 + offset, field_21_reserved13 );
        LittleEndian.putInt( data, 0x54 + offset, field_22_reserved14 );
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
        return 0 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4;
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
        FibRgLw97AbstractType other = (FibRgLw97AbstractType) obj;
        if ( field_1_cbMac != other.field_1_cbMac )
            return false;
        if ( field_2_reserved1 != other.field_2_reserved1 )
            return false;
        if ( field_3_reserved2 != other.field_3_reserved2 )
            return false;
        if ( field_4_ccpText != other.field_4_ccpText )
            return false;
        if ( field_5_ccpFtn != other.field_5_ccpFtn )
            return false;
        if ( field_6_ccpHdd != other.field_6_ccpHdd )
            return false;
        if ( field_7_reserved3 != other.field_7_reserved3 )
            return false;
        if ( field_8_ccpAtn != other.field_8_ccpAtn )
            return false;
        if ( field_9_ccpEdn != other.field_9_ccpEdn )
            return false;
        if ( field_10_ccpTxbx != other.field_10_ccpTxbx )
            return false;
        if ( field_11_ccpHdrTxbx != other.field_11_ccpHdrTxbx )
            return false;
        if ( field_12_reserved4 != other.field_12_reserved4 )
            return false;
        if ( field_13_reserved5 != other.field_13_reserved5 )
            return false;
        if ( field_14_reserved6 != other.field_14_reserved6 )
            return false;
        if ( field_15_reserved7 != other.field_15_reserved7 )
            return false;
        if ( field_16_reserved8 != other.field_16_reserved8 )
            return false;
        if ( field_17_reserved9 != other.field_17_reserved9 )
            return false;
        if ( field_18_reserved10 != other.field_18_reserved10 )
            return false;
        if ( field_19_reserved11 != other.field_19_reserved11 )
            return false;
        if ( field_20_reserved12 != other.field_20_reserved12 )
            return false;
        if ( field_21_reserved13 != other.field_21_reserved13 )
            return false;
        if ( field_22_reserved14 != other.field_22_reserved14 )
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
        result = prime * result + field_4_ccpText;
        result = prime * result + field_5_ccpFtn;
        result = prime * result + field_6_ccpHdd;
        result = prime * result + field_7_reserved3;
        result = prime * result + field_8_ccpAtn;
        result = prime * result + field_9_ccpEdn;
        result = prime * result + field_10_ccpTxbx;
        result = prime * result + field_11_ccpHdrTxbx;
        result = prime * result + field_12_reserved4;
        result = prime * result + field_13_reserved5;
        result = prime * result + field_14_reserved6;
        result = prime * result + field_15_reserved7;
        result = prime * result + field_16_reserved8;
        result = prime * result + field_17_reserved9;
        result = prime * result + field_18_reserved10;
        result = prime * result + field_19_reserved11;
        result = prime * result + field_20_reserved12;
        result = prime * result + field_21_reserved13;
        result = prime * result + field_22_reserved14;
        return result;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[FibRgLw97]\n");
        builder.append("    .cbMac                = ");
        builder.append(" (").append(getCbMac()).append(" )\n");
        builder.append("    .reserved1            = ");
        builder.append(" (").append(getReserved1()).append(" )\n");
        builder.append("    .reserved2            = ");
        builder.append(" (").append(getReserved2()).append(" )\n");
        builder.append("    .ccpText              = ");
        builder.append(" (").append(getCcpText()).append(" )\n");
        builder.append("    .ccpFtn               = ");
        builder.append(" (").append(getCcpFtn()).append(" )\n");
        builder.append("    .ccpHdd               = ");
        builder.append(" (").append(getCcpHdd()).append(" )\n");
        builder.append("    .reserved3            = ");
        builder.append(" (").append(getReserved3()).append(" )\n");
        builder.append("    .ccpAtn               = ");
        builder.append(" (").append(getCcpAtn()).append(" )\n");
        builder.append("    .ccpEdn               = ");
        builder.append(" (").append(getCcpEdn()).append(" )\n");
        builder.append("    .ccpTxbx              = ");
        builder.append(" (").append(getCcpTxbx()).append(" )\n");
        builder.append("    .ccpHdrTxbx           = ");
        builder.append(" (").append(getCcpHdrTxbx()).append(" )\n");
        builder.append("    .reserved4            = ");
        builder.append(" (").append(getReserved4()).append(" )\n");
        builder.append("    .reserved5            = ");
        builder.append(" (").append(getReserved5()).append(" )\n");
        builder.append("    .reserved6            = ");
        builder.append(" (").append(getReserved6()).append(" )\n");
        builder.append("    .reserved7            = ");
        builder.append(" (").append(getReserved7()).append(" )\n");
        builder.append("    .reserved8            = ");
        builder.append(" (").append(getReserved8()).append(" )\n");
        builder.append("    .reserved9            = ");
        builder.append(" (").append(getReserved9()).append(" )\n");
        builder.append("    .reserved10           = ");
        builder.append(" (").append(getReserved10()).append(" )\n");
        builder.append("    .reserved11           = ");
        builder.append(" (").append(getReserved11()).append(" )\n");
        builder.append("    .reserved12           = ");
        builder.append(" (").append(getReserved12()).append(" )\n");
        builder.append("    .reserved13           = ");
        builder.append(" (").append(getReserved13()).append(" )\n");
        builder.append("    .reserved14           = ");
        builder.append(" (").append(getReserved14()).append(" )\n");

        builder.append("[/FibRgLw97]\n");
        return builder.toString();
    }

    /**
     * Specifies the count of bytes of those written to the WordDocument stream of the file that have any meaning. All bytes in the WordDocument stream at offset cbMac and greater MUST be ignored..
     */
    @Internal
    public int getCbMac()
    {
        return field_1_cbMac;
    }

    /**
     * Specifies the count of bytes of those written to the WordDocument stream of the file that have any meaning. All bytes in the WordDocument stream at offset cbMac and greater MUST be ignored..
     */
    @Internal
    public void setCbMac( int field_1_cbMac )
    {
        this.field_1_cbMac = field_1_cbMac;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public int getReserved1()
    {
        return field_2_reserved1;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved1( int field_2_reserved1 )
    {
        this.field_2_reserved1 = field_2_reserved1;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public int getReserved2()
    {
        return field_3_reserved2;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved2( int field_3_reserved2 )
    {
        this.field_3_reserved2 = field_3_reserved2;
    }

    /**
     * A signed integer that specifies the count of CPs in the main document. This value MUST be zero, 1, or greater.
     */
    @Internal
    public int getCcpText()
    {
        return field_4_ccpText;
    }

    /**
     * A signed integer that specifies the count of CPs in the main document. This value MUST be zero, 1, or greater.
     */
    @Internal
    public void setCcpText( int field_4_ccpText )
    {
        this.field_4_ccpText = field_4_ccpText;
    }

    /**
     * A signed integer that specifies the count of CPs in the footnote subdocument. This value MUST be zero, 1, or greater.
     */
    @Internal
    public int getCcpFtn()
    {
        return field_5_ccpFtn;
    }

    /**
     * A signed integer that specifies the count of CPs in the footnote subdocument. This value MUST be zero, 1, or greater.
     */
    @Internal
    public void setCcpFtn( int field_5_ccpFtn )
    {
        this.field_5_ccpFtn = field_5_ccpFtn;
    }

    /**
     * A signed integer that specifies the count of CPs in the header subdocument. This value MUST be zero, 1, or greater.
     */
    @Internal
    public int getCcpHdd()
    {
        return field_6_ccpHdd;
    }

    /**
     * A signed integer that specifies the count of CPs in the header subdocument. This value MUST be zero, 1, or greater.
     */
    @Internal
    public void setCcpHdd( int field_6_ccpHdd )
    {
        this.field_6_ccpHdd = field_6_ccpHdd;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public int getReserved3()
    {
        return field_7_reserved3;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved3( int field_7_reserved3 )
    {
        this.field_7_reserved3 = field_7_reserved3;
    }

    /**
     * A signed integer that specifies the count of CPs in the comment subdocument. This value MUST be zero, 1, or greater.
     */
    @Internal
    public int getCcpAtn()
    {
        return field_8_ccpAtn;
    }

    /**
     * A signed integer that specifies the count of CPs in the comment subdocument. This value MUST be zero, 1, or greater.
     */
    @Internal
    public void setCcpAtn( int field_8_ccpAtn )
    {
        this.field_8_ccpAtn = field_8_ccpAtn;
    }

    /**
     * A signed integer that specifies the count of CPs in the endnote subdocument. This value MUST be zero, 1, or greater.
     */
    @Internal
    public int getCcpEdn()
    {
        return field_9_ccpEdn;
    }

    /**
     * A signed integer that specifies the count of CPs in the endnote subdocument. This value MUST be zero, 1, or greater.
     */
    @Internal
    public void setCcpEdn( int field_9_ccpEdn )
    {
        this.field_9_ccpEdn = field_9_ccpEdn;
    }

    /**
     * A signed integer that specifies the count of CPs in the textbox subdocument of the main document. This value MUST be zero, 1, or greater.
     */
    @Internal
    public int getCcpTxbx()
    {
        return field_10_ccpTxbx;
    }

    /**
     * A signed integer that specifies the count of CPs in the textbox subdocument of the main document. This value MUST be zero, 1, or greater.
     */
    @Internal
    public void setCcpTxbx( int field_10_ccpTxbx )
    {
        this.field_10_ccpTxbx = field_10_ccpTxbx;
    }

    /**
     * A signed integer that specifies the count of CPs in the textbox subdocument of the header. This value MUST be zero, 1, or greater.
     */
    @Internal
    public int getCcpHdrTxbx()
    {
        return field_11_ccpHdrTxbx;
    }

    /**
     * A signed integer that specifies the count of CPs in the textbox subdocument of the header. This value MUST be zero, 1, or greater.
     */
    @Internal
    public void setCcpHdrTxbx( int field_11_ccpHdrTxbx )
    {
        this.field_11_ccpHdrTxbx = field_11_ccpHdrTxbx;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public int getReserved4()
    {
        return field_12_reserved4;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved4( int field_12_reserved4 )
    {
        this.field_12_reserved4 = field_12_reserved4;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public int getReserved5()
    {
        return field_13_reserved5;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved5( int field_13_reserved5 )
    {
        this.field_13_reserved5 = field_13_reserved5;
    }

    /**
     * This value MUST be equal or less than the number of data elements in PlcBteChpx, as specified by FibRgFcLcb97.fcPlcfBteChpx and FibRgFcLcb97.lcbPlcfBteChpx. This value MUST be ignored.
     */
    @Internal
    public int getReserved6()
    {
        return field_14_reserved6;
    }

    /**
     * This value MUST be equal or less than the number of data elements in PlcBteChpx, as specified by FibRgFcLcb97.fcPlcfBteChpx and FibRgFcLcb97.lcbPlcfBteChpx. This value MUST be ignored.
     */
    @Internal
    public void setReserved6( int field_14_reserved6 )
    {
        this.field_14_reserved6 = field_14_reserved6;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public int getReserved7()
    {
        return field_15_reserved7;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved7( int field_15_reserved7 )
    {
        this.field_15_reserved7 = field_15_reserved7;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public int getReserved8()
    {
        return field_16_reserved8;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved8( int field_16_reserved8 )
    {
        this.field_16_reserved8 = field_16_reserved8;
    }

    /**
     * This value MUST be less than or equal to the number of data elements in PlcBtePapx, as specified by FibRgFcLcb97.fcPlcfBtePapx and FibRgFcLcb97.lcbPlcfBtePapx. This value MUST be ignored.
     */
    @Internal
    public int getReserved9()
    {
        return field_17_reserved9;
    }

    /**
     * This value MUST be less than or equal to the number of data elements in PlcBtePapx, as specified by FibRgFcLcb97.fcPlcfBtePapx and FibRgFcLcb97.lcbPlcfBtePapx. This value MUST be ignored.
     */
    @Internal
    public void setReserved9( int field_17_reserved9 )
    {
        this.field_17_reserved9 = field_17_reserved9;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public int getReserved10()
    {
        return field_18_reserved10;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved10( int field_18_reserved10 )
    {
        this.field_18_reserved10 = field_18_reserved10;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public int getReserved11()
    {
        return field_19_reserved11;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved11( int field_19_reserved11 )
    {
        this.field_19_reserved11 = field_19_reserved11;
    }

    /**
     * This value SHOULD be zero, and MUST be ignored.
     */
    @Internal
    public int getReserved12()
    {
        return field_20_reserved12;
    }

    /**
     * This value SHOULD be zero, and MUST be ignored.
     */
    @Internal
    public void setReserved12( int field_20_reserved12 )
    {
        this.field_20_reserved12 = field_20_reserved12;
    }

    /**
     * This value MUST be zero and MUST be ignored.
     */
    @Internal
    public int getReserved13()
    {
        return field_21_reserved13;
    }

    /**
     * This value MUST be zero and MUST be ignored.
     */
    @Internal
    public void setReserved13( int field_21_reserved13 )
    {
        this.field_21_reserved13 = field_21_reserved13;
    }

    /**
     * This value MUST be zero and MUST be ignored.
     */
    @Internal
    public int getReserved14()
    {
        return field_22_reserved14;
    }

    /**
     * This value MUST be zero and MUST be ignored.
     */
    @Internal
    public void setReserved14( int field_22_reserved14 )
    {
        this.field_22_reserved14 = field_22_reserved14;
    }

}  // END OF CLASS
