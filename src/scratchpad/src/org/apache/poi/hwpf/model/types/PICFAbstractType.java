/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.hwpf.model.types;


import java.util.Arrays;

import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * The PICF structure specifies the type of a picture, as well as the size of the
        picture and information about its border. <p>Class and fields descriptions are quoted
        from Microsoft Office Word 97-2007
        Binary File Format and [MS-DOC] - v20110608 Word (.doc)
        Binary File Format

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
public abstract class PICFAbstractType
{

    protected int field_1_lcb;
    protected int field_2_cbHeader;
    protected short field_3_mm;
    protected short field_4_xExt;
    protected short field_5_yExt;
    protected short field_6_swHMF;
    protected int field_7_grf;
    protected int field_8_padding;
    protected int field_9_mmPM;
    protected int field_10_padding2;
    protected short field_11_dxaGoal;
    protected short field_12_dyaGoal;
    protected int field_13_mx;
    protected int field_14_my;
    protected short field_15_dxaReserved1;
    protected short field_16_dyaReserved1;
    protected short field_17_dxaReserved2;
    protected short field_18_dyaReserved2;
    protected byte field_19_fReserved;
    protected byte field_20_bpp;
    protected byte[] field_21_brcTop80;
    protected byte[] field_22_brcLeft80;
    protected byte[] field_23_brcBottom80;
    protected byte[] field_24_brcRight80;
    protected short field_25_dxaReserved3;
    protected short field_26_dyaReserved3;
    protected short field_27_cProps;

    protected PICFAbstractType()
    {
        this.field_21_brcTop80 = new byte[4];
        this.field_22_brcLeft80 = new byte[4];
        this.field_23_brcBottom80 = new byte[4];
        this.field_24_brcRight80 = new byte[4];
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_lcb                    = LittleEndian.getInt( data, 0x0 + offset );
        field_2_cbHeader               = LittleEndian.getShort( data, 0x4 + offset );
        field_3_mm                     = LittleEndian.getShort( data, 0x6 + offset );
        field_4_xExt                   = LittleEndian.getShort( data, 0x8 + offset );
        field_5_yExt                   = LittleEndian.getShort( data, 0xa + offset );
        field_6_swHMF                  = LittleEndian.getShort( data, 0xc + offset );
        field_7_grf                    = LittleEndian.getInt( data, 0xe + offset );
        field_8_padding                = LittleEndian.getInt( data, 0x12 + offset );
        field_9_mmPM                   = LittleEndian.getShort( data, 0x16 + offset );
        field_10_padding2              = LittleEndian.getInt( data, 0x18 + offset );
        field_11_dxaGoal               = LittleEndian.getShort( data, 0x1c + offset );
        field_12_dyaGoal               = LittleEndian.getShort( data, 0x1e + offset );
        field_13_mx                    = LittleEndian.getShort( data, 0x20 + offset );
        field_14_my                    = LittleEndian.getShort( data, 0x22 + offset );
        field_15_dxaReserved1          = LittleEndian.getShort( data, 0x24 + offset );
        field_16_dyaReserved1          = LittleEndian.getShort( data, 0x26 + offset );
        field_17_dxaReserved2          = LittleEndian.getShort( data, 0x28 + offset );
        field_18_dyaReserved2          = LittleEndian.getShort( data, 0x2a + offset );
        field_19_fReserved             = data[ 0x2c + offset ];
        field_20_bpp                   = data[ 0x2d + offset ];
        field_21_brcTop80              = Arrays.copyOfRange( data, 0x2e + offset, 0x2e + offset + 4 );
        field_22_brcLeft80             = Arrays.copyOfRange( data, 0x32 + offset, 0x32 + offset + 4 );
        field_23_brcBottom80           = Arrays.copyOfRange( data, 0x36 + offset, 0x36 + offset + 4 );
        field_24_brcRight80            = Arrays.copyOfRange( data, 0x3a + offset, 0x3a + offset + 4 );
        field_25_dxaReserved3          = LittleEndian.getShort( data, 0x3e + offset );
        field_26_dyaReserved3          = LittleEndian.getShort( data, 0x40 + offset );
        field_27_cProps                = LittleEndian.getShort( data, 0x42 + offset );
    }

    public void serialize( byte[] data, int offset )
    {
        LittleEndian.putInt( data, 0x0 + offset, field_1_lcb );
        LittleEndian.putUShort( data, 0x4 + offset, field_2_cbHeader );
        LittleEndian.putShort( data, 0x6 + offset, field_3_mm );
        LittleEndian.putShort( data, 0x8 + offset, field_4_xExt );
        LittleEndian.putShort( data, 0xa + offset, field_5_yExt );
        LittleEndian.putShort( data, 0xc + offset, field_6_swHMF );
        LittleEndian.putInt( data, 0xe + offset, field_7_grf );
        LittleEndian.putInt( data, 0x12 + offset, field_8_padding );
        LittleEndian.putUShort( data, 0x16 + offset, field_9_mmPM );
        LittleEndian.putInt( data, 0x18 + offset, field_10_padding2 );
        LittleEndian.putShort( data, 0x1c + offset, field_11_dxaGoal );
        LittleEndian.putShort( data, 0x1e + offset, field_12_dyaGoal );
        LittleEndian.putUShort( data, 0x20 + offset, field_13_mx );
        LittleEndian.putUShort( data, 0x22 + offset, field_14_my );
        LittleEndian.putShort( data, 0x24 + offset, field_15_dxaReserved1 );
        LittleEndian.putShort( data, 0x26 + offset, field_16_dyaReserved1 );
        LittleEndian.putShort( data, 0x28 + offset, field_17_dxaReserved2 );
        LittleEndian.putShort( data, 0x2a + offset, field_18_dyaReserved2 );
        data[ 0x2c + offset ] = field_19_fReserved;
        data[ 0x2d + offset ] = field_20_bpp;
        System.arraycopy( field_21_brcTop80, 0, data, 0x2e + offset, field_21_brcTop80.length );
        System.arraycopy( field_22_brcLeft80, 0, data, 0x32 + offset, field_22_brcLeft80.length );
        System.arraycopy( field_23_brcBottom80, 0, data, 0x36 + offset, field_23_brcBottom80.length );
        System.arraycopy( field_24_brcRight80, 0, data, 0x3a + offset, field_24_brcRight80.length );
        LittleEndian.putShort( data, 0x3e + offset, field_25_dxaReserved3 );
        LittleEndian.putShort( data, 0x40 + offset, field_26_dyaReserved3 );
        LittleEndian.putShort( data, 0x42 + offset, field_27_cProps );
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
        return 0 + 4 + 2 + 2 + 2 + 2 + 2 + 4 + 4 + 2 + 4 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 1 + 1 + 4 + 4 + 4 + 4 + 2 + 2 + 2;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[PICF]\n");
        builder.append("    .lcb                  = ");
        builder.append(" (").append(getLcb()).append(" )\n");
        builder.append("    .cbHeader             = ");
        builder.append(" (").append(getCbHeader()).append(" )\n");
        builder.append("    .mm                   = ");
        builder.append(" (").append(getMm()).append(" )\n");
        builder.append("    .xExt                 = ");
        builder.append(" (").append(getXExt()).append(" )\n");
        builder.append("    .yExt                 = ");
        builder.append(" (").append(getYExt()).append(" )\n");
        builder.append("    .swHMF                = ");
        builder.append(" (").append(getSwHMF()).append(" )\n");
        builder.append("    .grf                  = ");
        builder.append(" (").append(getGrf()).append(" )\n");
        builder.append("    .padding              = ");
        builder.append(" (").append(getPadding()).append(" )\n");
        builder.append("    .mmPM                 = ");
        builder.append(" (").append(getMmPM()).append(" )\n");
        builder.append("    .padding2             = ");
        builder.append(" (").append(getPadding2()).append(" )\n");
        builder.append("    .dxaGoal              = ");
        builder.append(" (").append(getDxaGoal()).append(" )\n");
        builder.append("    .dyaGoal              = ");
        builder.append(" (").append(getDyaGoal()).append(" )\n");
        builder.append("    .mx                   = ");
        builder.append(" (").append(getMx()).append(" )\n");
        builder.append("    .my                   = ");
        builder.append(" (").append(getMy()).append(" )\n");
        builder.append("    .dxaReserved1         = ");
        builder.append(" (").append(getDxaReserved1()).append(" )\n");
        builder.append("    .dyaReserved1         = ");
        builder.append(" (").append(getDyaReserved1()).append(" )\n");
        builder.append("    .dxaReserved2         = ");
        builder.append(" (").append(getDxaReserved2()).append(" )\n");
        builder.append("    .dyaReserved2         = ");
        builder.append(" (").append(getDyaReserved2()).append(" )\n");
        builder.append("    .fReserved            = ");
        builder.append(" (").append(getFReserved()).append(" )\n");
        builder.append("    .bpp                  = ");
        builder.append(" (").append(getBpp()).append(" )\n");
        builder.append("    .brcTop80             = ");
        builder.append(" (").append(Arrays.toString(getBrcTop80())).append(" )\n");
        builder.append("    .brcLeft80            = ");
        builder.append(" (").append(Arrays.toString(getBrcLeft80())).append(" )\n");
        builder.append("    .brcBottom80          = ");
        builder.append(" (").append(Arrays.toString(getBrcBottom80())).append(" )\n");
        builder.append("    .brcRight80           = ");
        builder.append(" (").append(Arrays.toString(getBrcRight80())).append(" )\n");
        builder.append("    .dxaReserved3         = ");
        builder.append(" (").append(getDxaReserved3()).append(" )\n");
        builder.append("    .dyaReserved3         = ");
        builder.append(" (").append(getDyaReserved3()).append(" )\n");
        builder.append("    .cProps               = ");
        builder.append(" (").append(getCProps()).append(" )\n");

        builder.append("[/PICF]\n");
        return builder.toString();
    }

    /**
     * A signed integer that specifies the size, in bytes, of this PICF structure and the subsequent data.
     */
    @Internal
    public int getLcb()
    {
        return field_1_lcb;
    }

    /**
     * A signed integer that specifies the size, in bytes, of this PICF structure and the subsequent data.
     */
    @Internal
    public void setLcb( int field_1_lcb )
    {
        this.field_1_lcb = field_1_lcb;
    }

    /**
     * An unsigned integer that specifies the size, in bytes, of this PICF structure. This value MUST be 0x44.
     */
    @Internal
    public int getCbHeader()
    {
        return field_2_cbHeader;
    }

    /**
     * An unsigned integer that specifies the size, in bytes, of this PICF structure. This value MUST be 0x44.
     */
    @Internal
    public void setCbHeader( int field_2_cbHeader )
    {
        this.field_2_cbHeader = field_2_cbHeader;
    }

    /**
     * A signed integer that specifies the format of the picture data.
     */
    @Internal
    public short getMm()
    {
        return field_3_mm;
    }

    /**
     * A signed integer that specifies the format of the picture data.
     */
    @Internal
    public void setMm( short field_3_mm )
    {
        this.field_3_mm = field_3_mm;
    }

    /**
     * This field is unused and MUST be ignored.
     */
    @Internal
    public short getXExt()
    {
        return field_4_xExt;
    }

    /**
     * This field is unused and MUST be ignored.
     */
    @Internal
    public void setXExt( short field_4_xExt )
    {
        this.field_4_xExt = field_4_xExt;
    }

    /**
     * This field is unused and MUST be ignored.
     */
    @Internal
    public short getYExt()
    {
        return field_5_yExt;
    }

    /**
     * This field is unused and MUST be ignored.
     */
    @Internal
    public void setYExt( short field_5_yExt )
    {
        this.field_5_yExt = field_5_yExt;
    }

    /**
     * This field is unused and MUST be ignored.
     */
    @Internal
    public short getSwHMF()
    {
        return field_6_swHMF;
    }

    /**
     * This field is unused and MUST be ignored.
     */
    @Internal
    public void setSwHMF( short field_6_swHMF )
    {
        this.field_6_swHMF = field_6_swHMF;
    }

    /**
     * This field MUST be ignored.
     */
    @Internal
    public int getGrf()
    {
        return field_7_grf;
    }

    /**
     * This field MUST be ignored.
     */
    @Internal
    public void setGrf( int field_7_grf )
    {
        this.field_7_grf = field_7_grf;
    }

    /**
     * This value MUST be zero and MUST be ignored.
     */
    @Internal
    public int getPadding()
    {
        return field_8_padding;
    }

    /**
     * This value MUST be zero and MUST be ignored.
     */
    @Internal
    public void setPadding( int field_8_padding )
    {
        this.field_8_padding = field_8_padding;
    }

    /**
     * This field MUST be ignored.
     */
    @Internal
    public int getMmPM()
    {
        return field_9_mmPM;
    }

    /**
     * This field MUST be ignored.
     */
    @Internal
    public void setMmPM( int field_9_mmPM )
    {
        this.field_9_mmPM = field_9_mmPM;
    }

    /**
     * This value MUST be zero and MUST be ignored.
     */
    @Internal
    public int getPadding2()
    {
        return field_10_padding2;
    }

    /**
     * This value MUST be zero and MUST be ignored.
     */
    @Internal
    public void setPadding2( int field_10_padding2 )
    {
        this.field_10_padding2 = field_10_padding2;
    }

    /**
     * Get the dxaGoal field for the PICF record.
     */
    @Internal
    public short getDxaGoal()
    {
        return field_11_dxaGoal;
    }

    /**
     * Set the dxaGoal field for the PICF record.
     */
    @Internal
    public void setDxaGoal( short field_11_dxaGoal )
    {
        this.field_11_dxaGoal = field_11_dxaGoal;
    }

    /**
     * Get the dyaGoal field for the PICF record.
     */
    @Internal
    public short getDyaGoal()
    {
        return field_12_dyaGoal;
    }

    /**
     * Set the dyaGoal field for the PICF record.
     */
    @Internal
    public void setDyaGoal( short field_12_dyaGoal )
    {
        this.field_12_dyaGoal = field_12_dyaGoal;
    }

    /**
     * Get the mx field for the PICF record.
     */
    @Internal
    public int getMx()
    {
        return field_13_mx;
    }

    /**
     * Set the mx field for the PICF record.
     */
    @Internal
    public void setMx( int field_13_mx )
    {
        this.field_13_mx = field_13_mx;
    }

    /**
     * Get the my field for the PICF record.
     */
    @Internal
    public int getMy()
    {
        return field_14_my;
    }

    /**
     * Set the my field for the PICF record.
     */
    @Internal
    public void setMy( int field_14_my )
    {
        this.field_14_my = field_14_my;
    }

    /**
     * Get the dxaReserved1 field for the PICF record.
     */
    @Internal
    public short getDxaReserved1()
    {
        return field_15_dxaReserved1;
    }

    /**
     * Set the dxaReserved1 field for the PICF record.
     */
    @Internal
    public void setDxaReserved1( short field_15_dxaReserved1 )
    {
        this.field_15_dxaReserved1 = field_15_dxaReserved1;
    }

    /**
     * Get the dyaReserved1 field for the PICF record.
     */
    @Internal
    public short getDyaReserved1()
    {
        return field_16_dyaReserved1;
    }

    /**
     * Set the dyaReserved1 field for the PICF record.
     */
    @Internal
    public void setDyaReserved1( short field_16_dyaReserved1 )
    {
        this.field_16_dyaReserved1 = field_16_dyaReserved1;
    }

    /**
     * Get the dxaReserved2 field for the PICF record.
     */
    @Internal
    public short getDxaReserved2()
    {
        return field_17_dxaReserved2;
    }

    /**
     * Set the dxaReserved2 field for the PICF record.
     */
    @Internal
    public void setDxaReserved2( short field_17_dxaReserved2 )
    {
        this.field_17_dxaReserved2 = field_17_dxaReserved2;
    }

    /**
     * Get the dyaReserved2 field for the PICF record.
     */
    @Internal
    public short getDyaReserved2()
    {
        return field_18_dyaReserved2;
    }

    /**
     * Set the dyaReserved2 field for the PICF record.
     */
    @Internal
    public void setDyaReserved2( short field_18_dyaReserved2 )
    {
        this.field_18_dyaReserved2 = field_18_dyaReserved2;
    }

    /**
     * Get the fReserved field for the PICF record.
     */
    @Internal
    public byte getFReserved()
    {
        return field_19_fReserved;
    }

    /**
     * Set the fReserved field for the PICF record.
     */
    @Internal
    public void setFReserved( byte field_19_fReserved )
    {
        this.field_19_fReserved = field_19_fReserved;
    }

    /**
     * Get the bpp field for the PICF record.
     */
    @Internal
    public byte getBpp()
    {
        return field_20_bpp;
    }

    /**
     * Set the bpp field for the PICF record.
     */
    @Internal
    public void setBpp( byte field_20_bpp )
    {
        this.field_20_bpp = field_20_bpp;
    }

    /**
     * Get the brcTop80 field for the PICF record.
     */
    @Internal
    public byte[] getBrcTop80()
    {
        return field_21_brcTop80;
    }

    /**
     * Set the brcTop80 field for the PICF record.
     */
    @Internal
    public void setBrcTop80( byte[] field_21_brcTop80 )
    {
        this.field_21_brcTop80 = field_21_brcTop80;
    }

    /**
     * Get the brcLeft80 field for the PICF record.
     */
    @Internal
    public byte[] getBrcLeft80()
    {
        return field_22_brcLeft80;
    }

    /**
     * Set the brcLeft80 field for the PICF record.
     */
    @Internal
    public void setBrcLeft80( byte[] field_22_brcLeft80 )
    {
        this.field_22_brcLeft80 = field_22_brcLeft80;
    }

    /**
     * Get the brcBottom80 field for the PICF record.
     */
    @Internal
    public byte[] getBrcBottom80()
    {
        return field_23_brcBottom80;
    }

    /**
     * Set the brcBottom80 field for the PICF record.
     */
    @Internal
    public void setBrcBottom80( byte[] field_23_brcBottom80 )
    {
        this.field_23_brcBottom80 = field_23_brcBottom80;
    }

    /**
     * Get the brcRight80 field for the PICF record.
     */
    @Internal
    public byte[] getBrcRight80()
    {
        return field_24_brcRight80;
    }

    /**
     * Set the brcRight80 field for the PICF record.
     */
    @Internal
    public void setBrcRight80( byte[] field_24_brcRight80 )
    {
        this.field_24_brcRight80 = field_24_brcRight80;
    }

    /**
     * Get the dxaReserved3 field for the PICF record.
     */
    @Internal
    public short getDxaReserved3()
    {
        return field_25_dxaReserved3;
    }

    /**
     * Set the dxaReserved3 field for the PICF record.
     */
    @Internal
    public void setDxaReserved3( short field_25_dxaReserved3 )
    {
        this.field_25_dxaReserved3 = field_25_dxaReserved3;
    }

    /**
     * Get the dyaReserved3 field for the PICF record.
     */
    @Internal
    public short getDyaReserved3()
    {
        return field_26_dyaReserved3;
    }

    /**
     * Set the dyaReserved3 field for the PICF record.
     */
    @Internal
    public void setDyaReserved3( short field_26_dyaReserved3 )
    {
        this.field_26_dyaReserved3 = field_26_dyaReserved3;
    }

    /**
     * This value MUST be 0 and MUST be ignored.
     */
    @Internal
    public short getCProps()
    {
        return field_27_cProps;
    }

    /**
     * This value MUST be 0 and MUST be ignored.
     */
    @Internal
    public void setCProps( short field_27_cProps )
    {
        this.field_27_cProps = field_27_cProps;
    }

}  // END OF CLASS
