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


import java.util.Arrays;

import org.apache.poi.hwpf.model.Grfhic;
import org.apache.poi.util.BitField;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * The LVLF structure contains formatting properties for an individual level in a list.
 */
@Internal
public abstract class LVLFAbstractType
{

    protected int field_1_iStartAt;
    protected byte field_2_nfc;
    protected byte field_3_info;
    /**/private static final BitField jc = new BitField(0x03);
    /**/private static final BitField fLegal = new BitField(0x04);
    /**/private static final BitField fNoRestart = new BitField(0x08);
    /**/private static final BitField fIndentSav = new BitField(0x10);
    /**/private static final BitField fConverted = new BitField(0x20);
    /**/private static final BitField unused1 = new BitField(0x40);
    /**/private static final BitField fTentative = new BitField(0x80);
    protected byte[] field_4_rgbxchNums;
    protected byte field_5_ixchFollow;
    protected int field_6_dxaIndentSav;
    protected int field_7_unused2;
    protected short field_8_cbGrpprlChpx;
    protected short field_9_cbGrpprlPapx;
    protected short field_10_ilvlRestartLim;
    protected Grfhic field_11_grfhic;

    protected LVLFAbstractType()
    {
        this.field_4_rgbxchNums = new byte[9];
        this.field_11_grfhic = new Grfhic();
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_iStartAt               = LittleEndian.getInt( data, 0x0 + offset );
        field_2_nfc                    = data[ 0x4 + offset ];
        field_3_info                   = data[ 0x5 + offset ];
        field_4_rgbxchNums             = Arrays.copyOfRange( data, 0x6 + offset, 0x6 + offset + 9 );
        field_5_ixchFollow             = data[ 0xf + offset ];
        field_6_dxaIndentSav           = LittleEndian.getInt( data, 0x10 + offset );
        field_7_unused2                = LittleEndian.getInt( data, 0x14 + offset );
        field_8_cbGrpprlChpx           = LittleEndian.getUByte( data, 0x18 + offset );
        field_9_cbGrpprlPapx           = LittleEndian.getUByte( data, 0x19 + offset );
        field_10_ilvlRestartLim        = LittleEndian.getUByte( data, 0x1a + offset );
        field_11_grfhic                = new Grfhic( data, 0x1b + offset );
    }

    public void serialize( byte[] data, int offset )
    {
        LittleEndian.putInt( data, 0x0 + offset, field_1_iStartAt );
        data[ 0x4 + offset ] = field_2_nfc;
        data[ 0x5 + offset ] = field_3_info;
        System.arraycopy( field_4_rgbxchNums, 0, data, 0x6 + offset, field_4_rgbxchNums.length );
        data[ 0xf + offset ] = field_5_ixchFollow;
        LittleEndian.putInt( data, 0x10 + offset, field_6_dxaIndentSav );
        LittleEndian.putInt( data, 0x14 + offset, field_7_unused2 );
        LittleEndian.putUByte( data, 0x18 + offset, field_8_cbGrpprlChpx );
        LittleEndian.putUByte( data, 0x19 + offset, field_9_cbGrpprlPapx );
        LittleEndian.putUByte( data, 0x1a + offset, field_10_ilvlRestartLim );
        field_11_grfhic.serialize( data, 0x1b + offset );
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
        return 0 + 4 + 1 + 1 + 9 + 1 + 4 + 4 + 1 + 1 + 1 + 1;
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
        LVLFAbstractType other = (LVLFAbstractType) obj;
        if ( field_1_iStartAt != other.field_1_iStartAt )
            return false;
        if ( field_2_nfc != other.field_2_nfc )
            return false;
        if ( field_3_info != other.field_3_info )
            return false;
        if ( !Arrays.equals( field_4_rgbxchNums, other.field_4_rgbxchNums ) )
            return false;
        if ( field_5_ixchFollow != other.field_5_ixchFollow )
            return false;
        if ( field_6_dxaIndentSav != other.field_6_dxaIndentSav )
            return false;
        if ( field_7_unused2 != other.field_7_unused2 )
            return false;
        if ( field_8_cbGrpprlChpx != other.field_8_cbGrpprlChpx )
            return false;
        if ( field_9_cbGrpprlPapx != other.field_9_cbGrpprlPapx )
            return false;
        if ( field_10_ilvlRestartLim != other.field_10_ilvlRestartLim )
            return false;
        if ( field_11_grfhic == null )
        {
            if ( other.field_11_grfhic != null )
                return false;
        }
        else if ( !field_11_grfhic.equals( other.field_11_grfhic ) )
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{field_1_iStartAt, field_2_nfc, field_3_info, field_4_rgbxchNums,
            field_5_ixchFollow, field_6_dxaIndentSav, field_7_unused2, field_8_cbGrpprlChpx, field_9_cbGrpprlPapx,
            field_10_ilvlRestartLim, field_11_grfhic});
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[LVLF]\n");
        builder.append("    .iStartAt             = ");
        builder.append(" (").append(getIStartAt()).append(" )\n");
        builder.append("    .nfc                  = ");
        builder.append(" (").append(getNfc()).append(" )\n");
        builder.append("    .info                 = ");
        builder.append(" (").append(getInfo()).append(" )\n");
        builder.append("         .jc                       = ").append(getJc()).append('\n');
        builder.append("         .fLegal                   = ").append(isFLegal()).append('\n');
        builder.append("         .fNoRestart               = ").append(isFNoRestart()).append('\n');
        builder.append("         .fIndentSav               = ").append(isFIndentSav()).append('\n');
        builder.append("         .fConverted               = ").append(isFConverted()).append('\n');
        builder.append("         .unused1                  = ").append(isUnused1()).append('\n');
        builder.append("         .fTentative               = ").append(isFTentative()).append('\n');
        builder.append("    .rgbxchNums           = ");
        builder.append(" (").append(Arrays.toString(getRgbxchNums())).append(" )\n");
        builder.append("    .ixchFollow           = ");
        builder.append(" (").append(getIxchFollow()).append(" )\n");
        builder.append("    .dxaIndentSav         = ");
        builder.append(" (").append(getDxaIndentSav()).append(" )\n");
        builder.append("    .unused2              = ");
        builder.append(" (").append(getUnused2()).append(" )\n");
        builder.append("    .cbGrpprlChpx         = ");
        builder.append(" (").append(getCbGrpprlChpx()).append(" )\n");
        builder.append("    .cbGrpprlPapx         = ");
        builder.append(" (").append(getCbGrpprlPapx()).append(" )\n");
        builder.append("    .ilvlRestartLim       = ");
        builder.append(" (").append(getIlvlRestartLim()).append(" )\n");
        builder.append("    .grfhic               = ");
        builder.append(" (").append(getGrfhic()).append(" )\n");

        builder.append("[/LVLF]\n");
        return builder.toString();
    }

    /**
     * A signed integer that specifies the beginning number for the number sequence belonging to this level. This value MUST be less than or equal to 0x7FFF and MUST be greater than or equal to zero. If this level does not have a number sequence (see nfc), this MUST be ignored.
     */
    @Internal
    public int getIStartAt()
    {
        return field_1_iStartAt;
    }

    /**
     * A signed integer that specifies the beginning number for the number sequence belonging to this level. This value MUST be less than or equal to 0x7FFF and MUST be greater than or equal to zero. If this level does not have a number sequence (see nfc), this MUST be ignored.
     */
    @Internal
    public void setIStartAt( int field_1_iStartAt )
    {
        this.field_1_iStartAt = field_1_iStartAt;
    }

    /**
     * An MSONFC, as specified in [MS-OSHARED] section 2.2.1.3, that specifies the format of the level numbers that replace the placeholders for this level in the xst fields of the LVLs in this list. This value MUST not be equal to 0x08, 0x09, 0x0F, or 0x13. If this is equal to 0xFF or 0x17, this level does not have a number sequence and therefore has no number formatting. If this is equal to 0x17, the level uses bullets.
     */
    @Internal
    public byte getNfc()
    {
        return field_2_nfc;
    }

    /**
     * An MSONFC, as specified in [MS-OSHARED] section 2.2.1.3, that specifies the format of the level numbers that replace the placeholders for this level in the xst fields of the LVLs in this list. This value MUST not be equal to 0x08, 0x09, 0x0F, or 0x13. If this is equal to 0xFF or 0x17, this level does not have a number sequence and therefore has no number formatting. If this is equal to 0x17, the level uses bullets.
     */
    @Internal
    public void setNfc( byte field_2_nfc )
    {
        this.field_2_nfc = field_2_nfc;
    }

    /**
     * Get the info field for the LVLF record.
     */
    @Internal
    public byte getInfo()
    {
        return field_3_info;
    }

    /**
     * Set the info field for the LVLF record.
     */
    @Internal
    public void setInfo( byte field_3_info )
    {
        this.field_3_info = field_3_info;
    }

    /**
     * An array of 8-bit integers. Each integer specifies a one-based character offset to a level placeholder in the xst.rgtchar of the LVL that contains this LVLF. This array is zero-terminated, unless it is full. The count of elements in this array, before to the first terminating zero, MUST be less than or equal to the one-based level of the list to which this LVL corresponds. The integers in this array, before the first terminating zero, MUST be in ascending order, and MUST be unique.
     */
    @Internal
    public byte[] getRgbxchNums()
    {
        return field_4_rgbxchNums;
    }

    /**
     * An array of 8-bit integers. Each integer specifies a one-based character offset to a level placeholder in the xst.rgtchar of the LVL that contains this LVLF. This array is zero-terminated, unless it is full. The count of elements in this array, before to the first terminating zero, MUST be less than or equal to the one-based level of the list to which this LVL corresponds. The integers in this array, before the first terminating zero, MUST be in ascending order, and MUST be unique.
     */
    @Internal
    public void setRgbxchNums( byte[] field_4_rgbxchNums )
    {
        this.field_4_rgbxchNums = field_4_rgbxchNums;
    }

    /**
     * An unsigned integer that specifies the character that follows the number text.
     */
    @Internal
    public byte getIxchFollow()
    {
        return field_5_ixchFollow;
    }

    /**
     * An unsigned integer that specifies the character that follows the number text.
     */
    @Internal
    public void setIxchFollow( byte field_5_ixchFollow )
    {
        this.field_5_ixchFollow = field_5_ixchFollow;
    }

    /**
     * If fIndentSav is nonzero, this is a signed integer that specifies the size, in twips, of the indent that needs to be removed when the numbering is removed. This MUST be less than or equal to 0x00007BC0 or greater than or equal to 0xFFFF8440. If fIndentSav is zero, this MUST be ignored.
     */
    @Internal
    public int getDxaIndentSav()
    {
        return field_6_dxaIndentSav;
    }

    /**
     * If fIndentSav is nonzero, this is a signed integer that specifies the size, in twips, of the indent that needs to be removed when the numbering is removed. This MUST be less than or equal to 0x00007BC0 or greater than or equal to 0xFFFF8440. If fIndentSav is zero, this MUST be ignored.
     */
    @Internal
    public void setDxaIndentSav( int field_6_dxaIndentSav )
    {
        this.field_6_dxaIndentSav = field_6_dxaIndentSav;
    }

    /**
     * This field MUST be ignored.
     */
    @Internal
    public int getUnused2()
    {
        return field_7_unused2;
    }

    /**
     * This field MUST be ignored.
     */
    @Internal
    public void setUnused2( int field_7_unused2 )
    {
        this.field_7_unused2 = field_7_unused2;
    }

    /**
     * An unsigned integer that specifies the size, in bytes, of the grpprlChpx in the LVL that contains this LVLF.
     */
    @Internal
    public short getCbGrpprlChpx()
    {
        return field_8_cbGrpprlChpx;
    }

    /**
     * An unsigned integer that specifies the size, in bytes, of the grpprlChpx in the LVL that contains this LVLF.
     */
    @Internal
    public void setCbGrpprlChpx( short field_8_cbGrpprlChpx )
    {
        this.field_8_cbGrpprlChpx = field_8_cbGrpprlChpx;
    }

    /**
     * An unsigned integer that specifies the size, in bytes, of the grpprlPapx in the LVL that contains this LVLF.
     */
    @Internal
    public short getCbGrpprlPapx()
    {
        return field_9_cbGrpprlPapx;
    }

    /**
     * An unsigned integer that specifies the size, in bytes, of the grpprlPapx in the LVL that contains this LVLF.
     */
    @Internal
    public void setCbGrpprlPapx( short field_9_cbGrpprlPapx )
    {
        this.field_9_cbGrpprlPapx = field_9_cbGrpprlPapx;
    }

    /**
     * An unsigned integer that specifies the first (most-significant) zero-based level after which the number sequence of this level does not restart. The number sequence of this level does restart after any level that is more significant than the specified level. This MUST be less than or equal to the zero-based level of the list to which this LVLF corresponds. If fNoRestart is zero, this MUST be ignored. If this level does not have a number sequence (see nfc), this MUST be ignored.
     */
    @Internal
    public short getIlvlRestartLim()
    {
        return field_10_ilvlRestartLim;
    }

    /**
     * An unsigned integer that specifies the first (most-significant) zero-based level after which the number sequence of this level does not restart. The number sequence of this level does restart after any level that is more significant than the specified level. This MUST be less than or equal to the zero-based level of the list to which this LVLF corresponds. If fNoRestart is zero, this MUST be ignored. If this level does not have a number sequence (see nfc), this MUST be ignored.
     */
    @Internal
    public void setIlvlRestartLim( short field_10_ilvlRestartLim )
    {
        this.field_10_ilvlRestartLim = field_10_ilvlRestartLim;
    }

    /**
     * A grfhic that specifies the HTML incompatibilities of the level..
     */
    @Internal
    public Grfhic getGrfhic()
    {
        return field_11_grfhic;
    }

    /**
     * A grfhic that specifies the HTML incompatibilities of the level..
     */
    @Internal
    public void setGrfhic( Grfhic field_11_grfhic )
    {
        this.field_11_grfhic = field_11_grfhic;
    }

    /**
     * Sets the jc field value.
     * An unsigned integer that specifies the justification of this level
     */
    @Internal
    public void setJc( byte value )
    {
        field_3_info = (byte)jc.setValue(field_3_info, value);
    }

    /**
     * An unsigned integer that specifies the justification of this level
     * @return  the jc field value.
     */
    @Internal
    public byte getJc()
    {
        return ( byte )jc.getValue(field_3_info);
    }

    /**
     * Sets the fLegal field value.
     * A bit that specifies whether this level overrides the nfc of all inherited level numbers. If the original nfc of a level number is msonfcArabicLZ, it is preserved. Otherwise, the nfc of the level number is overridden by msonfcArabic.
     */
    @Internal
    public void setFLegal( boolean value )
    {
        field_3_info = (byte)fLegal.setBoolean(field_3_info, value);
    }

    /**
     * A bit that specifies whether this level overrides the nfc of all inherited level numbers. If the original nfc of a level number is msonfcArabicLZ, it is preserved. Otherwise, the nfc of the level number is overridden by msonfcArabic.
     * @return  the fLegal field value.
     */
    @Internal
    public boolean isFLegal()
    {
        return fLegal.isSet(field_3_info);
    }

    /**
     * Sets the fNoRestart field value.
     * A bit that specifies whether the number sequence of the level does not restart after a level is encountered that is more significant than the level to which this LVLF corresponds
     */
    @Internal
    public void setFNoRestart( boolean value )
    {
        field_3_info = (byte)fNoRestart.setBoolean(field_3_info, value);
    }

    /**
     * A bit that specifies whether the number sequence of the level does not restart after a level is encountered that is more significant than the level to which this LVLF corresponds
     * @return  the fNoRestart field value.
     */
    @Internal
    public boolean isFNoRestart()
    {
        return fNoRestart.isSet(field_3_info);
    }

    /**
     * Sets the fIndentSav field value.
     * A bit that specifies whether the level indented the text it was applied to and that the indent needs to be removed when numbering is removed. The indent to be removed is stored in dxaIndentSav
     */
    @Internal
    public void setFIndentSav( boolean value )
    {
        field_3_info = (byte)fIndentSav.setBoolean(field_3_info, value);
    }

    /**
     * A bit that specifies whether the level indented the text it was applied to and that the indent needs to be removed when numbering is removed. The indent to be removed is stored in dxaIndentSav
     * @return  the fIndentSav field value.
     */
    @Internal
    public boolean isFIndentSav()
    {
        return fIndentSav.isSet(field_3_info);
    }

    /**
     * Sets the fConverted field value.
     * A bit that specifies whether the nfc of this LVLF structure was previously a temporary value used for bidirectional compatibility that was converted into a standard MSONFC
     */
    @Internal
    public void setFConverted( boolean value )
    {
        field_3_info = (byte)fConverted.setBoolean(field_3_info, value);
    }

    /**
     * A bit that specifies whether the nfc of this LVLF structure was previously a temporary value used for bidirectional compatibility that was converted into a standard MSONFC
     * @return  the fConverted field value.
     */
    @Internal
    public boolean isFConverted()
    {
        return fConverted.isSet(field_3_info);
    }

    /**
     * Sets the unused1 field value.
     * This bit MUST be ignored
     */
    @Internal
    public void setUnused1( boolean value )
    {
        field_3_info = (byte)unused1.setBoolean(field_3_info, value);
    }

    /**
     * This bit MUST be ignored
     * @return  the unused1 field value.
     * @deprecated This field should not be used according to specification
     */
    @Internal
    @Deprecated
    public boolean isUnused1()
    {
        return unused1.isSet(field_3_info);
    }

    /**
     * Sets the fTentative field value.
     * A bit that specifies whether the format of the level is tentative
     */
    @Internal
    public void setFTentative( boolean value )
    {
        field_3_info = (byte)fTentative.setBoolean(field_3_info, value);
    }

    /**
     * A bit that specifies whether the format of the level is tentative
     * @return  the fTentative field value.
     */
    @Internal
    public boolean isFTentative()
    {
        return fTentative.isSet(field_3_info);
    }

}  // END OF CLASS
