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
 * Base part of the File information Block (FibBase). Holds the core part of the FIB,
        from the first 32 bytes. <p>Class and fields descriptions are quoted from Microsoft
        Office Word 97-2007 Binary File Format and [MS-DOC] - v20110608 Word (.doc) Binary File
        Format
    
 * <p>
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/types/definitions.
 * <p>
 * This class is internal. It content or properties may change without notice 
 * due to changes in our knowledge of internal Microsoft Word binary structures.

 * @author Andrew C. Oliver; Sergey Vladimirov; according to Microsoft Office Word 97-2007 Binary
        File Format Specification [*.doc] and [MS-DOC] - v20110608 Word (.doc) Binary File Format
    
 */
@Internal
public abstract class FibBaseAbstractType
{

    protected int field_1_wIdent;
    protected int field_2_nFib;
    protected int field_3_unused;
    protected int field_4_lid;
    protected int field_5_pnNext;
    protected short field_6_flags1;
    /**/private static final BitField fDot = new BitField(0x0001);
    /**/private static final BitField fGlsy = new BitField(0x0002);
    /**/private static final BitField fComplex = new BitField(0x0004);
    /**/private static final BitField fHasPic = new BitField(0x0008);
    /**/private static final BitField cQuickSaves = new BitField(0x00F0);
    /**/private static final BitField fEncrypted = new BitField(0x0100);
    /**/private static final BitField fWhichTblStm = new BitField(0x0200);
    /**/private static final BitField fReadOnlyRecommended = new BitField(0x0400);
    /**/private static final BitField fWriteReservation = new BitField(0x0800);
    /**/private static final BitField fExtChar = new BitField(0x1000);
    /**/private static final BitField fLoadOverride = new BitField(0x2000);
    /**/private static final BitField fFarEast = new BitField(0x4000);
    /**/private static final BitField fObfuscated = new BitField(0x8000);
    protected int field_7_nFibBack;
    protected int field_8_lKey;
    @Deprecated
    protected byte field_9_envr;
    protected byte field_10_flags2;
    /**/private static final BitField fMac = new BitField(0x01);
    /**/private static final BitField fEmptySpecial = new BitField(0x02);
    /**/private static final BitField fLoadOverridePage = new BitField(0x04);
    /**/private static final BitField reserved1 = new BitField(0x08);
    /**/private static final BitField reserved2 = new BitField(0x10);
    /**/private static final BitField fSpare0 = new BitField(0xFE);
    @Deprecated
    protected short field_11_Chs;
    @Deprecated
    protected short field_12_chsTables;
    @Deprecated
    protected int field_13_fcMin;
    @Deprecated
    protected int field_14_fcMac;

    protected FibBaseAbstractType()
    {
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_wIdent                 = LittleEndian.getShort( data, 0x0 + offset );
        field_2_nFib                   = LittleEndian.getShort( data, 0x2 + offset );
        field_3_unused                 = LittleEndian.getShort( data, 0x4 + offset );
        field_4_lid                    = LittleEndian.getShort( data, 0x6 + offset );
        field_5_pnNext                 = LittleEndian.getShort( data, 0x8 + offset );
        field_6_flags1                 = LittleEndian.getShort( data, 0xa + offset );
        field_7_nFibBack               = LittleEndian.getShort( data, 0xc + offset );
        field_8_lKey                   = LittleEndian.getInt( data, 0xe + offset );
        field_9_envr                   = data[ 0x12 + offset ];
        field_10_flags2                = data[ 0x13 + offset ];
        field_11_Chs                   = LittleEndian.getShort( data, 0x14 + offset );
        field_12_chsTables             = LittleEndian.getShort( data, 0x16 + offset );
        field_13_fcMin                 = LittleEndian.getInt( data, 0x18 + offset );
        field_14_fcMac                 = LittleEndian.getInt( data, 0x1c + offset );
    }

    public void serialize( byte[] data, int offset )
    {
        LittleEndian.putUShort( data, 0x0 + offset, field_1_wIdent );
        LittleEndian.putUShort( data, 0x2 + offset, field_2_nFib );
        LittleEndian.putUShort( data, 0x4 + offset, field_3_unused );
        LittleEndian.putUShort( data, 0x6 + offset, field_4_lid );
        LittleEndian.putUShort( data, 0x8 + offset, field_5_pnNext );
        LittleEndian.putShort( data, 0xa + offset, field_6_flags1 );
        LittleEndian.putUShort( data, 0xc + offset, field_7_nFibBack );
        LittleEndian.putInt( data, 0xe + offset, field_8_lKey );
        data[ 0x12 + offset ] = field_9_envr;
        data[ 0x13 + offset ] = field_10_flags2;
        LittleEndian.putShort( data, 0x14 + offset, field_11_Chs );
        LittleEndian.putShort( data, 0x16 + offset, field_12_chsTables );
        LittleEndian.putInt( data, 0x18 + offset, field_13_fcMin );
        LittleEndian.putInt( data, 0x1c + offset, field_14_fcMac );
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
        return 0 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 4 + 1 + 1 + 2 + 2 + 4 + 4;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[FibBase]\n");
        builder.append("    .wIdent               = ");
        builder.append(" (").append(getWIdent()).append(" )\n");
        builder.append("    .nFib                 = ");
        builder.append(" (").append(getNFib()).append(" )\n");
        builder.append("    .unused               = ");
        builder.append(" (").append(getUnused()).append(" )\n");
        builder.append("    .lid                  = ");
        builder.append(" (").append(getLid()).append(" )\n");
        builder.append("    .pnNext               = ");
        builder.append(" (").append(getPnNext()).append(" )\n");
        builder.append("    .flags1               = ");
        builder.append(" (").append(getFlags1()).append(" )\n");
        builder.append("         .fDot                     = ").append(isFDot()).append('\n');
        builder.append("         .fGlsy                    = ").append(isFGlsy()).append('\n');
        builder.append("         .fComplex                 = ").append(isFComplex()).append('\n');
        builder.append("         .fHasPic                  = ").append(isFHasPic()).append('\n');
        builder.append("         .cQuickSaves              = ").append(getCQuickSaves()).append('\n');
        builder.append("         .fEncrypted               = ").append(isFEncrypted()).append('\n');
        builder.append("         .fWhichTblStm             = ").append(isFWhichTblStm()).append('\n');
        builder.append("         .fReadOnlyRecommended     = ").append(isFReadOnlyRecommended()).append('\n');
        builder.append("         .fWriteReservation        = ").append(isFWriteReservation()).append('\n');
        builder.append("         .fExtChar                 = ").append(isFExtChar()).append('\n');
        builder.append("         .fLoadOverride            = ").append(isFLoadOverride()).append('\n');
        builder.append("         .fFarEast                 = ").append(isFFarEast()).append('\n');
        builder.append("         .fObfuscated              = ").append(isFObfuscated()).append('\n');
        builder.append("    .nFibBack             = ");
        builder.append(" (").append(getNFibBack()).append(" )\n");
        builder.append("    .lKey                 = ");
        builder.append(" (").append(getLKey()).append(" )\n");
        builder.append("    .envr                 = ");
        builder.append(" (").append(getEnvr()).append(" )\n");
        builder.append("    .flags2               = ");
        builder.append(" (").append(getFlags2()).append(" )\n");
        builder.append("         .fMac                     = ").append(isFMac()).append('\n');
        builder.append("         .fEmptySpecial            = ").append(isFEmptySpecial()).append('\n');
        builder.append("         .fLoadOverridePage        = ").append(isFLoadOverridePage()).append('\n');
        builder.append("         .reserved1                = ").append(isReserved1()).append('\n');
        builder.append("         .reserved2                = ").append(isReserved2()).append('\n');
        builder.append("         .fSpare0                  = ").append(getFSpare0()).append('\n');
        builder.append("    .Chs                  = ");
        builder.append(" (").append(getChs()).append(" )\n");
        builder.append("    .chsTables            = ");
        builder.append(" (").append(getChsTables()).append(" )\n");
        builder.append("    .fcMin                = ");
        builder.append(" (").append(getFcMin()).append(" )\n");
        builder.append("    .fcMac                = ");
        builder.append(" (").append(getFcMac()).append(" )\n");

        builder.append("[/FibBase]\n");
        return builder.toString();
    }

    /**
     * An unsigned integer that specifies that this is a Word Binary File. This value MUST be 0xA5EC.
     */
    @Internal
    public int getWIdent()
    {
        return field_1_wIdent;
    }

    /**
     * An unsigned integer that specifies that this is a Word Binary File. This value MUST be 0xA5EC.
     */
    @Internal
    public void setWIdent( int field_1_wIdent )
    {
        this.field_1_wIdent = field_1_wIdent;
    }

    /**
     * An unsigned integer that specifies the version number of the file format used. Superseded by FibRgCswNew.nFibNew if it is present. This value SHOULD be 0x00C1.
     */
    @Internal
    public int getNFib()
    {
        return field_2_nFib;
    }

    /**
     * An unsigned integer that specifies the version number of the file format used. Superseded by FibRgCswNew.nFibNew if it is present. This value SHOULD be 0x00C1.
     */
    @Internal
    public void setNFib( int field_2_nFib )
    {
        this.field_2_nFib = field_2_nFib;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public int getUnused()
    {
        return field_3_unused;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setUnused( int field_3_unused )
    {
        this.field_3_unused = field_3_unused;
    }

    /**
     * A LID that specifies the install language of the application that is producing the document. If nFib is 0x00D9 or greater, then any East Asian install lid or any install lid with a base language of Spanish, German or French MUST be recorded as lidAmerican. If the nFib is 0x0101 or greater, then any install lid with a base language of Vietnamese, Thai, or Hindi MUST be recorded as lidAmerican..
     */
    @Internal
    public int getLid()
    {
        return field_4_lid;
    }

    /**
     * A LID that specifies the install language of the application that is producing the document. If nFib is 0x00D9 or greater, then any East Asian install lid or any install lid with a base language of Spanish, German or French MUST be recorded as lidAmerican. If the nFib is 0x0101 or greater, then any install lid with a base language of Vietnamese, Thai, or Hindi MUST be recorded as lidAmerican..
     */
    @Internal
    public void setLid( int field_4_lid )
    {
        this.field_4_lid = field_4_lid;
    }

    /**
     * An unsigned integer that specifies the offset in the WordDocument stream of the FIB for the document which contains all the AutoText items.
     */
    @Internal
    public int getPnNext()
    {
        return field_5_pnNext;
    }

    /**
     * An unsigned integer that specifies the offset in the WordDocument stream of the FIB for the document which contains all the AutoText items.
     */
    @Internal
    public void setPnNext( int field_5_pnNext )
    {
        this.field_5_pnNext = field_5_pnNext;
    }

    /**
     * Get the flags1 field for the FibBase record.
     */
    @Internal
    public short getFlags1()
    {
        return field_6_flags1;
    }

    /**
     * Set the flags1 field for the FibBase record.
     */
    @Internal
    public void setFlags1( short field_6_flags1 )
    {
        this.field_6_flags1 = field_6_flags1;
    }

    /**
     * This value SHOULD be 0x00BF. This value MUST be 0x00BF or 0x00C1.
     */
    @Internal
    public int getNFibBack()
    {
        return field_7_nFibBack;
    }

    /**
     * This value SHOULD be 0x00BF. This value MUST be 0x00BF or 0x00C1.
     */
    @Internal
    public void setNFibBack( int field_7_nFibBack )
    {
        this.field_7_nFibBack = field_7_nFibBack;
    }

    /**
     * If fEncryption is 1 and fObfuscation is 1, this value specifies the XOR obfuscation password verifier. If fEncryption is 1 and fObfuscation is 0, this value specifies the size of the EncryptionHeader that is stored at the beginning of the Table stream as described in Encryption and Obfuscation. Otherwise, this value MUST be 0.
     */
    @Internal
    public int getLKey()
    {
        return field_8_lKey;
    }

    /**
     * If fEncryption is 1 and fObfuscation is 1, this value specifies the XOR obfuscation password verifier. If fEncryption is 1 and fObfuscation is 0, this value specifies the size of the EncryptionHeader that is stored at the beginning of the Table stream as described in Encryption and Obfuscation. Otherwise, this value MUST be 0.
     */
    @Internal
    public void setLKey( int field_8_lKey )
    {
        this.field_8_lKey = field_8_lKey;
    }

    /**
     * This value MUST be 0, and MUST be ignored.
     */
    @Internal
    public byte getEnvr()
    {
        return field_9_envr;
    }

    /**
     * This value MUST be 0, and MUST be ignored.
     */
    @Internal
    public void setEnvr( byte field_9_envr )
    {
        this.field_9_envr = field_9_envr;
    }

    /**
     * Get the flags2 field for the FibBase record.
     */
    @Internal
    public byte getFlags2()
    {
        return field_10_flags2;
    }

    /**
     * Set the flags2 field for the FibBase record.
     */
    @Internal
    public void setFlags2( byte field_10_flags2 )
    {
        this.field_10_flags2 = field_10_flags2;
    }

    /**
     * This value MUST be 0 and MUST be ignored.
     */
    @Internal
    public short getChs()
    {
        return field_11_Chs;
    }

    /**
     * This value MUST be 0 and MUST be ignored.
     */
    @Internal
    public void setChs( short field_11_Chs )
    {
        this.field_11_Chs = field_11_Chs;
    }

    /**
     * This value MUST be 0 and MUST be ignored.
     */
    @Internal
    public short getChsTables()
    {
        return field_12_chsTables;
    }

    /**
     * This value MUST be 0 and MUST be ignored.
     */
    @Internal
    public void setChsTables( short field_12_chsTables )
    {
        this.field_12_chsTables = field_12_chsTables;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public int getFcMin()
    {
        return field_13_fcMin;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setFcMin( int field_13_fcMin )
    {
        this.field_13_fcMin = field_13_fcMin;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public int getFcMac()
    {
        return field_14_fcMac;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setFcMac( int field_14_fcMac )
    {
        this.field_14_fcMac = field_14_fcMac;
    }

    /**
     * Sets the fDot field value.
     * Specifies whether this is a document template
     */
    @Internal
    public void setFDot( boolean value )
    {
        field_6_flags1 = (short)fDot.setBoolean(field_6_flags1, value);
    }

    /**
     * Specifies whether this is a document template
     * @return  the fDot field value.
     */
    @Internal
    public boolean isFDot()
    {
        return fDot.isSet(field_6_flags1);
    }

    /**
     * Sets the fGlsy field value.
     * Specifies whether this is a document that contains only AutoText items
     */
    @Internal
    public void setFGlsy( boolean value )
    {
        field_6_flags1 = (short)fGlsy.setBoolean(field_6_flags1, value);
    }

    /**
     * Specifies whether this is a document that contains only AutoText items
     * @return  the fGlsy field value.
     */
    @Internal
    public boolean isFGlsy()
    {
        return fGlsy.isSet(field_6_flags1);
    }

    /**
     * Sets the fComplex field value.
     * Specifies that the last save operation that was performed on this document was an incremental save operation
     */
    @Internal
    public void setFComplex( boolean value )
    {
        field_6_flags1 = (short)fComplex.setBoolean(field_6_flags1, value);
    }

    /**
     * Specifies that the last save operation that was performed on this document was an incremental save operation
     * @return  the fComplex field value.
     */
    @Internal
    public boolean isFComplex()
    {
        return fComplex.isSet(field_6_flags1);
    }

    /**
     * Sets the fHasPic field value.
     * When set to 0, there SHOULD be no pictures in the document
     */
    @Internal
    public void setFHasPic( boolean value )
    {
        field_6_flags1 = (short)fHasPic.setBoolean(field_6_flags1, value);
    }

    /**
     * When set to 0, there SHOULD be no pictures in the document
     * @return  the fHasPic field value.
     */
    @Internal
    public boolean isFHasPic()
    {
        return fHasPic.isSet(field_6_flags1);
    }

    /**
     * Sets the cQuickSaves field value.
     * An unsigned integer. If nFib is less than 0x00D9, then cQuickSaves specifies the number of consecutive times this document was incrementally saved. If nFib is 0x00D9 or greater, then cQuickSaves MUST be 0xF
     */
    @Internal
    public void setCQuickSaves( byte value )
    {
        field_6_flags1 = (short)cQuickSaves.setValue(field_6_flags1, value);
    }

    /**
     * An unsigned integer. If nFib is less than 0x00D9, then cQuickSaves specifies the number of consecutive times this document was incrementally saved. If nFib is 0x00D9 or greater, then cQuickSaves MUST be 0xF
     * @return  the cQuickSaves field value.
     */
    @Internal
    public byte getCQuickSaves()
    {
        return ( byte )cQuickSaves.getValue(field_6_flags1);
    }

    /**
     * Sets the fEncrypted field value.
     * Specifies whether the document is encrypted or obfuscated as specified in Encryption and Obfuscation
     */
    @Internal
    public void setFEncrypted( boolean value )
    {
        field_6_flags1 = (short)fEncrypted.setBoolean(field_6_flags1, value);
    }

    /**
     * Specifies whether the document is encrypted or obfuscated as specified in Encryption and Obfuscation
     * @return  the fEncrypted field value.
     */
    @Internal
    public boolean isFEncrypted()
    {
        return fEncrypted.isSet(field_6_flags1);
    }

    /**
     * Sets the fWhichTblStm field value.
     * Specifies the Table stream to which the FIB refers. When this value is set to 1, use 1Table; when this value is set to 0, use 0Table.
     */
    @Internal
    public void setFWhichTblStm( boolean value )
    {
        field_6_flags1 = (short)fWhichTblStm.setBoolean(field_6_flags1, value);
    }

    /**
     * Specifies the Table stream to which the FIB refers. When this value is set to 1, use 1Table; when this value is set to 0, use 0Table.
     * @return  the fWhichTblStm field value.
     */
    @Internal
    public boolean isFWhichTblStm()
    {
        return fWhichTblStm.isSet(field_6_flags1);
    }

    /**
     * Sets the fReadOnlyRecommended field value.
     * Specifies whether the document author recommended that the document be opened in read-only mode
     */
    @Internal
    public void setFReadOnlyRecommended( boolean value )
    {
        field_6_flags1 = (short)fReadOnlyRecommended.setBoolean(field_6_flags1, value);
    }

    /**
     * Specifies whether the document author recommended that the document be opened in read-only mode
     * @return  the fReadOnlyRecommended field value.
     */
    @Internal
    public boolean isFReadOnlyRecommended()
    {
        return fReadOnlyRecommended.isSet(field_6_flags1);
    }

    /**
     * Sets the fWriteReservation field value.
     * Specifies whether the document has a write-reservation password
     */
    @Internal
    public void setFWriteReservation( boolean value )
    {
        field_6_flags1 = (short)fWriteReservation.setBoolean(field_6_flags1, value);
    }

    /**
     * Specifies whether the document has a write-reservation password
     * @return  the fWriteReservation field value.
     */
    @Internal
    public boolean isFWriteReservation()
    {
        return fWriteReservation.isSet(field_6_flags1);
    }

    /**
     * Sets the fExtChar field value.
     * This value MUST be 1
     */
    @Internal
    public void setFExtChar( boolean value )
    {
        field_6_flags1 = (short)fExtChar.setBoolean(field_6_flags1, value);
    }

    /**
     * This value MUST be 1
     * @return  the fExtChar field value.
     */
    @Internal
    public boolean isFExtChar()
    {
        return fExtChar.isSet(field_6_flags1);
    }

    /**
     * Sets the fLoadOverride field value.
     * Specifies whether to override the language information and font that are specified in the paragraph style at istd 0 (the normal style) with the defaults that are appropriate for the installation language of the application
     */
    @Internal
    public void setFLoadOverride( boolean value )
    {
        field_6_flags1 = (short)fLoadOverride.setBoolean(field_6_flags1, value);
    }

    /**
     * Specifies whether to override the language information and font that are specified in the paragraph style at istd 0 (the normal style) with the defaults that are appropriate for the installation language of the application
     * @return  the fLoadOverride field value.
     */
    @Internal
    public boolean isFLoadOverride()
    {
        return fLoadOverride.isSet(field_6_flags1);
    }

    /**
     * Sets the fFarEast field value.
     * Specifies whether the installation language of the application that created the document was an East Asian language
     */
    @Internal
    public void setFFarEast( boolean value )
    {
        field_6_flags1 = (short)fFarEast.setBoolean(field_6_flags1, value);
    }

    /**
     * Specifies whether the installation language of the application that created the document was an East Asian language
     * @return  the fFarEast field value.
     */
    @Internal
    public boolean isFFarEast()
    {
        return fFarEast.isSet(field_6_flags1);
    }

    /**
     * Sets the fObfuscated field value.
     * If fEncrypted is 1, this bit specifies whether the document is obfuscated by using XOR obfuscation; otherwise, this bit MUST be ignored
     */
    @Internal
    public void setFObfuscated( boolean value )
    {
        field_6_flags1 = (short)fObfuscated.setBoolean(field_6_flags1, value);
    }

    /**
     * If fEncrypted is 1, this bit specifies whether the document is obfuscated by using XOR obfuscation; otherwise, this bit MUST be ignored
     * @return  the fObfuscated field value.
     */
    @Internal
    public boolean isFObfuscated()
    {
        return fObfuscated.isSet(field_6_flags1);
    }

    /**
     * Sets the fMac field value.
     * This value MUST be 0, and MUST be ignored
     */
    @Internal
    public void setFMac( boolean value )
    {
        field_10_flags2 = (byte)fMac.setBoolean(field_10_flags2, value);
    }

    /**
     * This value MUST be 0, and MUST be ignored
     * @return  the fMac field value.
     * @deprecated This field should not be used according to specification
     */
    @Internal
    @Deprecated
    public boolean isFMac()
    {
        return fMac.isSet(field_10_flags2);
    }

    /**
     * Sets the fEmptySpecial field value.
     * This value SHOULD be 0 and SHOULD be ignored
     */
    @Internal
    public void setFEmptySpecial( boolean value )
    {
        field_10_flags2 = (byte)fEmptySpecial.setBoolean(field_10_flags2, value);
    }

    /**
     * This value SHOULD be 0 and SHOULD be ignored
     * @return  the fEmptySpecial field value.
     * @deprecated This field should not be used according to specification
     */
    @Internal
    @Deprecated
    public boolean isFEmptySpecial()
    {
        return fEmptySpecial.isSet(field_10_flags2);
    }

    /**
     * Sets the fLoadOverridePage field value.
     * Specifies whether to override the section properties for page size, orientation, and margins with the defaults that are appropriate for the installation language of the application
     */
    @Internal
    public void setFLoadOverridePage( boolean value )
    {
        field_10_flags2 = (byte)fLoadOverridePage.setBoolean(field_10_flags2, value);
    }

    /**
     * Specifies whether to override the section properties for page size, orientation, and margins with the defaults that are appropriate for the installation language of the application
     * @return  the fLoadOverridePage field value.
     */
    @Internal
    public boolean isFLoadOverridePage()
    {
        return fLoadOverridePage.isSet(field_10_flags2);
    }

    /**
     * Sets the reserved1 field value.
     * This value is undefined and MUST be ignored
     */
    @Internal
    public void setReserved1( boolean value )
    {
        field_10_flags2 = (byte)reserved1.setBoolean(field_10_flags2, value);
    }

    /**
     * This value is undefined and MUST be ignored
     * @return  the reserved1 field value.
     * @deprecated This field should not be used according to specification
     */
    @Internal
    @Deprecated
    public boolean isReserved1()
    {
        return reserved1.isSet(field_10_flags2);
    }

    /**
     * Sets the reserved2 field value.
     * This value is undefined and MUST be ignored
     */
    @Internal
    public void setReserved2( boolean value )
    {
        field_10_flags2 = (byte)reserved2.setBoolean(field_10_flags2, value);
    }

    /**
     * This value is undefined and MUST be ignored
     * @return  the reserved2 field value.
     * @deprecated This field should not be used according to specification
     */
    @Internal
    @Deprecated
    public boolean isReserved2()
    {
        return reserved2.isSet(field_10_flags2);
    }

    /**
     * Sets the fSpare0 field value.
     * This value is undefined and MUST be ignored
     */
    @Internal
    public void setFSpare0( byte value )
    {
        field_10_flags2 = (byte)fSpare0.setValue(field_10_flags2, value);
    }

    /**
     * This value is undefined and MUST be ignored
     * @return  the fSpare0 field value.
     * @deprecated This field should not be used according to specification
     */
    @Internal
    @Deprecated
    public byte getFSpare0()
    {
        return ( byte )fSpare0.getValue(field_10_flags2);
    }

}  // END OF CLASS
