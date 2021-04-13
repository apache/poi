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

import org.apache.poi.hwpf.model.Colorref;
import org.apache.poi.hwpf.model.Hyphenation;
import org.apache.poi.hwpf.usermodel.BorderCode;
import org.apache.poi.hwpf.usermodel.DateAndTime;
import org.apache.poi.hwpf.usermodel.ShadingDescriptor;
import org.apache.poi.util.BitField;
import org.apache.poi.util.Internal;

/**
 * Character Properties.
 */
@SuppressWarnings("unused")
@Internal
public abstract class CHPAbstractType {

    private static final BitField fBold = new BitField(0x00000001);
    private static final BitField fItalic = new BitField(0x00000002);
    private static final BitField fRMarkDel = new BitField(0x00000004);
    private static final BitField fOutline = new BitField(0x00000008);
    private static final BitField fFldVanish = new BitField(0x00000010);
    private static final BitField fSmallCaps = new BitField(0x00000020);
    private static final BitField fCaps = new BitField(0x00000040);
    private static final BitField fVanish = new BitField(0x00000080);
    private static final BitField fRMark = new BitField(0x00000100);
    private static final BitField fSpec = new BitField(0x00000200);
    private static final BitField fStrike = new BitField(0x00000400);
    private static final BitField fObj = new BitField(0x00000800);
    private static final BitField fShadow = new BitField(0x00001000);
    private static final BitField fLowerCase = new BitField(0x00002000);
    private static final BitField fData = new BitField(0x00004000);
    private static final BitField fOle2 = new BitField(0x00008000);
    private static final BitField fEmboss = new BitField(0x00010000);
    private static final BitField fImprint = new BitField(0x00020000);
    private static final BitField fDStrike = new BitField(0x00040000);
    private static final BitField fUsePgsuSettings = new BitField(0x00080000);
    private static final BitField fBoldBi = new BitField(0x00100000);
    private static final BitField fComplexScripts = new BitField(0x00100000);
    private static final BitField fItalicBi = new BitField(0x00200000);
    private static final BitField fBiDi = new BitField(0x00400000);
    private static final BitField fIcoBi = new BitField(0x00800000);
    private static final BitField fNonGlyph = new BitField(0x01000000);
    private static final BitField fBoldOther = new BitField(0x02000000);
    private static final BitField fItalicOther = new BitField(0x04000000);
    private static final BitField fNoProof = new BitField(0x08000000);
    private static final BitField fWebHidden = new BitField(0x10000000);
    private static final BitField fFitText = new BitField(0x20000000);
    private static final BitField fCalc = new BitField(0x40000000);
    private static final BitField fFmtLineProp = new BitField(0x80000000);

    protected static final byte SFXTTEXT_NO = 0;
    protected static final byte SFXTTEXT_LAS_VEGAS_LIGHTS = 1;
    protected static final byte SFXTTEXT_BACKGROUND_BLINK = 2;
    protected static final byte SFXTTEXT_SPARKLE_TEXT = 3;
    protected static final byte SFXTTEXT_MARCHING_ANTS = 4;
    protected static final byte SFXTTEXT_MARCHING_RED_ANTS = 5;
    protected static final byte SFXTTEXT_SHIMMER = 6;

    protected static final byte KCD_NON = 0;
    protected static final byte KCD_DOT = 1;
    protected static final byte KCD_COMMA = 2;
    protected static final byte KCD_CIRCLE = 3;
    protected static final byte KCD_UNDER_DOT = 4;

    protected static final byte KUL_NONE = 0;
    protected static final byte KUL_SINGLE = 1;
    protected static final byte KUL_BY_WORD = 2;
    protected static final byte KUL_DOUBLE = 3;
    protected static final byte KUL_DOTTED = 4;
    protected static final byte KUL_HIDDEN = 5;
    protected static final byte KUL_THICK = 6;
    protected static final byte KUL_DASH = 7;
    protected static final byte KUL_DOT = 8;
    protected static final byte KUL_DOT_DASH = 9;
    protected static final byte KUL_DOT_DOT_DASH = 10;
    protected static final byte KUL_WAVE = 11;
    protected static final byte KUL_DOTTED_HEAVY = 20;
    protected static final byte KUL_DASHED_HEAVY = 23;
    protected static final byte KUL_DOT_DASH_HEAVY = 25;
    protected static final byte KUL_DOT_DOT_DASH_HEAVY = 26;
    protected static final byte KUL_WAVE_HEAVY = 27;
    protected static final byte KUL_DASH_LONG = 39;
    protected static final byte KUL_WAVE_DOUBLE = 43;
    protected static final byte KUL_DASH_LONG_HEAVY = 55;

    protected static final byte ISS_NONE = 0;
    protected static final byte ISS_SUPERSCRIPTED = 1;
    protected static final byte ISS_SUBSCRIPTED = 2;

    private static final BitField itypFELayout = new BitField(0x00ff);
    private static final BitField fTNY = new BitField(0x0100);
    private static final BitField fWarichu = new BitField(0x0200);
    private static final BitField fKumimoji = new BitField(0x0400);
    private static final BitField fRuby = new BitField(0x0800);
    private static final BitField fLSFitText = new BitField(0x1000);
    private static final BitField spare = new BitField(0xe000);

    private static final BitField iWarichuBracket = new BitField(0x07);
    private static final BitField fWarichuNoOpenBracket = new BitField(0x08);
    private static final BitField fTNYCompress = new BitField(0x10);
    private static final BitField fTNYFetchTxm = new BitField(0x20);
    private static final BitField fCellFitText = new BitField(0x40);
    private static final BitField unused = new BitField(0x80);

    private static final BitField icoHighlight = new BitField(0x001f);
    private static final BitField fHighlight = new BitField(0x0020);

    private static final BitField fChsDiff = new BitField(0x0001);
    private static final BitField fMacChs = new BitField(0x0020);

    protected static final byte LBRCRJ_NONE = 0;
    protected static final byte LBRCRJ_LEFT = 1;
    protected static final byte LBRCRJ_RIGHT = 2;
    protected static final byte LBRCRJ_BOTH = 3;

    protected int field_1_grpfChp;
    protected int field_2_hps;
    protected int field_3_ftcAscii;
    protected int field_4_ftcFE;
    protected int field_5_ftcOther;
    protected int field_6_ftcBi;
    protected int field_7_dxaSpace;
    protected Colorref field_8_cv;
    protected byte field_9_ico;
    protected int field_10_pctCharWidth;
    protected int field_11_lidDefault;
    protected int field_12_lidFE;
    protected byte field_13_kcd;
    protected boolean field_14_fUndetermine;
    protected byte field_15_iss;
    protected boolean field_16_fSpecSymbol;
    protected byte field_17_idct;
    protected byte field_18_idctHint;
    protected byte field_19_kul;
    protected Hyphenation field_20_hresi;
    protected int field_21_hpsKern;
    protected short field_22_hpsPos;
    protected ShadingDescriptor field_23_shd;
    protected BorderCode field_24_brc;
    protected int field_25_ibstRMark;
    protected byte field_26_sfxtText;
    protected boolean field_27_fDblBdr;
    protected boolean field_28_fBorderWS;
    protected short field_29_ufel;
    protected byte field_30_copt;
    protected int field_31_hpsAsci;
    protected int field_32_hpsFE;
    protected int field_33_hpsBi;
    protected int field_34_ftcSym;
    protected int field_35_xchSym;
    protected int field_36_fcPic;
    protected int field_37_fcObj;
    protected int field_38_lTagObj;
    protected int field_39_fcData;
    protected Hyphenation field_40_hresiOld;
    protected int field_41_ibstRMarkDel;
    protected DateAndTime field_42_dttmRMark;
    protected DateAndTime field_43_dttmRMarkDel;
    protected int field_44_istd;
    protected int field_45_idslRMReason;
    protected int field_46_idslReasonDel;
    protected int field_47_cpg;
    protected short field_48_Highlight;
    protected short field_49_CharsetFlags;
    protected short field_50_chse;
    protected boolean field_51_fPropRMark;
    protected int field_52_ibstPropRMark;
    protected DateAndTime field_53_dttmPropRMark;
    protected boolean field_54_fConflictOrig;
    protected boolean field_55_fConflictOtherDel;
    protected int field_56_wConflict;
    protected int field_57_IbstConflict;
    protected DateAndTime field_58_dttmConflict;
    protected boolean field_59_fDispFldRMark;
    protected int field_60_ibstDispFldRMark;
    protected DateAndTime field_61_dttmDispFldRMark;
    protected byte[] field_62_xstDispFldRMark;
    protected int field_63_fcObjp;
    protected byte field_64_lbrCRJ;
    protected boolean field_65_fSpecVanish;
    protected boolean field_66_fHasOldProps;
    protected boolean field_67_fSdtVanish;
    protected int field_68_wCharScale;

    protected CHPAbstractType() {
        field_2_hps = 20;
        field_8_cv = new Colorref();
        field_11_lidDefault = 0x0400;
        field_12_lidFE = 0x0400;
        field_20_hresi = new Hyphenation();
        field_23_shd = new ShadingDescriptor();
        field_24_brc = new BorderCode();
        field_36_fcPic = -1;
        field_40_hresiOld = new Hyphenation();
        field_42_dttmRMark = new DateAndTime();
        field_43_dttmRMarkDel = new DateAndTime();
        field_44_istd = 10;
        field_53_dttmPropRMark = new DateAndTime();
        field_58_dttmConflict = new DateAndTime();
        field_61_dttmDispFldRMark = new DateAndTime();
        field_62_xstDispFldRMark = new byte[32];
        field_68_wCharScale = 100;
    }

    protected CHPAbstractType(CHPAbstractType other) {
        field_1_grpfChp = other.field_1_grpfChp;
        field_2_hps = other.field_2_hps;
        field_3_ftcAscii = other.field_3_ftcAscii;
        field_4_ftcFE = other.field_4_ftcFE;
        field_5_ftcOther = other.field_5_ftcOther;
        field_6_ftcBi = other.field_6_ftcBi;
        field_7_dxaSpace = other.field_7_dxaSpace;
        field_8_cv = (other.field_8_cv == null) ? null : other.field_8_cv.copy();
        field_9_ico = other.field_9_ico;
        field_10_pctCharWidth = other.field_10_pctCharWidth;
        field_11_lidDefault = other.field_11_lidDefault;
        field_12_lidFE = other.field_12_lidFE;
        field_13_kcd = other.field_13_kcd;
        field_14_fUndetermine = other.field_14_fUndetermine;
        field_15_iss = other.field_15_iss;
        field_16_fSpecSymbol = other.field_16_fSpecSymbol;
        field_17_idct = other.field_17_idct;
        field_18_idctHint = other.field_18_idctHint;
        field_19_kul = other.field_19_kul;
        field_20_hresi = (other.field_20_hresi == null) ? null : other.field_20_hresi.copy();
        field_21_hpsKern = other.field_21_hpsKern;
        field_22_hpsPos = other.field_22_hpsPos;
        field_23_shd = (other.field_23_shd == null) ? null : other.field_23_shd.copy();
        field_24_brc = (other.field_24_brc == null) ? null : other.field_24_brc.copy();
        field_25_ibstRMark = other.field_25_ibstRMark;
        field_26_sfxtText = other.field_26_sfxtText;
        field_27_fDblBdr = other.field_27_fDblBdr;
        field_28_fBorderWS = other.field_28_fBorderWS;
        field_29_ufel = other.field_29_ufel;
        field_30_copt = other.field_30_copt;
        field_31_hpsAsci = other.field_31_hpsAsci;
        field_32_hpsFE = other.field_32_hpsFE;
        field_33_hpsBi = other.field_33_hpsBi;
        field_34_ftcSym = other.field_34_ftcSym;
        field_35_xchSym = other.field_35_xchSym;
        field_36_fcPic = other.field_36_fcPic;
        field_37_fcObj = other.field_37_fcObj;
        field_38_lTagObj = other.field_38_lTagObj;
        field_39_fcData = other.field_39_fcData;
        field_40_hresiOld = (other.field_40_hresiOld == null) ? null : other.field_40_hresiOld.copy();
        field_41_ibstRMarkDel = other.field_41_ibstRMarkDel;
        field_42_dttmRMark = (other.field_42_dttmRMark == null) ? null : other.field_42_dttmRMark.copy();
        field_43_dttmRMarkDel = (other.field_43_dttmRMarkDel == null) ? null : other.field_43_dttmRMarkDel.copy();
        field_44_istd = other.field_44_istd;
        field_45_idslRMReason = other.field_45_idslRMReason;
        field_46_idslReasonDel = other.field_46_idslReasonDel;
        field_47_cpg = other.field_47_cpg;
        field_48_Highlight = other.field_48_Highlight;
        field_49_CharsetFlags = other.field_49_CharsetFlags;
        field_50_chse = other.field_50_chse;
        field_51_fPropRMark = other.field_51_fPropRMark;
        field_52_ibstPropRMark = other.field_52_ibstPropRMark;
        field_53_dttmPropRMark = (other.field_53_dttmPropRMark == null) ? null : other.field_53_dttmPropRMark.copy();
        field_54_fConflictOrig = other.field_54_fConflictOrig;
        field_55_fConflictOtherDel = other.field_55_fConflictOtherDel;
        field_56_wConflict = other.field_56_wConflict;
        field_57_IbstConflict = other.field_57_IbstConflict;
        field_58_dttmConflict = (other.field_58_dttmConflict == null) ? null : other.field_58_dttmConflict.copy();
        field_59_fDispFldRMark = other.field_59_fDispFldRMark;
        field_60_ibstDispFldRMark = other.field_60_ibstDispFldRMark;
        field_61_dttmDispFldRMark = (other.field_61_dttmDispFldRMark == null) ? null : other.field_61_dttmDispFldRMark.copy();
        field_62_xstDispFldRMark = (other.field_62_xstDispFldRMark == null) ? null : other.field_62_xstDispFldRMark.clone();
        field_63_fcObjp = other.field_63_fcObjp;
        field_64_lbrCRJ = other.field_64_lbrCRJ;
        field_65_fSpecVanish = other.field_65_fSpecVanish;
        field_66_fHasOldProps = other.field_66_fHasOldProps;
        field_67_fSdtVanish = other.field_67_fSdtVanish;
        field_68_wCharScale = other.field_68_wCharScale;
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
        CHPAbstractType other = (CHPAbstractType) obj;
        if ( field_1_grpfChp != other.field_1_grpfChp )
            return false;
        if ( field_2_hps != other.field_2_hps )
            return false;
        if ( field_3_ftcAscii != other.field_3_ftcAscii )
            return false;
        if ( field_4_ftcFE != other.field_4_ftcFE )
            return false;
        if ( field_5_ftcOther != other.field_5_ftcOther )
            return false;
        if ( field_6_ftcBi != other.field_6_ftcBi )
            return false;
        if ( field_7_dxaSpace != other.field_7_dxaSpace )
            return false;
        if ( field_8_cv == null )
        {
            if ( other.field_8_cv != null )
                return false;
        }
        else if ( !field_8_cv.equals( other.field_8_cv ) )
            return false;
        if ( field_9_ico != other.field_9_ico )
            return false;
        if ( field_10_pctCharWidth != other.field_10_pctCharWidth )
            return false;
        if ( field_11_lidDefault != other.field_11_lidDefault )
            return false;
        if ( field_12_lidFE != other.field_12_lidFE )
            return false;
        if ( field_13_kcd != other.field_13_kcd )
            return false;
        if ( field_14_fUndetermine != other.field_14_fUndetermine )
            return false;
        if ( field_15_iss != other.field_15_iss )
            return false;
        if ( field_16_fSpecSymbol != other.field_16_fSpecSymbol )
            return false;
        if ( field_17_idct != other.field_17_idct )
            return false;
        if ( field_18_idctHint != other.field_18_idctHint )
            return false;
        if ( field_19_kul != other.field_19_kul )
            return false;
        if ( field_20_hresi == null )
        {
            if ( other.field_20_hresi != null )
                return false;
        }
        else if ( !field_20_hresi.equals( other.field_20_hresi ) )
            return false;
        if ( field_21_hpsKern != other.field_21_hpsKern )
            return false;
        if ( field_22_hpsPos != other.field_22_hpsPos )
            return false;
        if ( field_23_shd == null )
        {
            if ( other.field_23_shd != null )
                return false;
        }
        else if ( !field_23_shd.equals( other.field_23_shd ) )
            return false;
        if ( field_24_brc == null )
        {
            if ( other.field_24_brc != null )
                return false;
        }
        else if ( !field_24_brc.equals( other.field_24_brc ) )
            return false;
        if ( field_25_ibstRMark != other.field_25_ibstRMark )
            return false;
        if ( field_26_sfxtText != other.field_26_sfxtText )
            return false;
        if ( field_27_fDblBdr != other.field_27_fDblBdr )
            return false;
        if ( field_28_fBorderWS != other.field_28_fBorderWS )
            return false;
        if ( field_29_ufel != other.field_29_ufel )
            return false;
        if ( field_30_copt != other.field_30_copt )
            return false;
        if ( field_31_hpsAsci != other.field_31_hpsAsci )
            return false;
        if ( field_32_hpsFE != other.field_32_hpsFE )
            return false;
        if ( field_33_hpsBi != other.field_33_hpsBi )
            return false;
        if ( field_34_ftcSym != other.field_34_ftcSym )
            return false;
        if ( field_35_xchSym != other.field_35_xchSym )
            return false;
        if ( field_36_fcPic != other.field_36_fcPic )
            return false;
        if ( field_37_fcObj != other.field_37_fcObj )
            return false;
        if ( field_38_lTagObj != other.field_38_lTagObj )
            return false;
        if ( field_39_fcData != other.field_39_fcData )
            return false;
        if ( field_40_hresiOld == null )
        {
            if ( other.field_40_hresiOld != null )
                return false;
        }
        else if ( !field_40_hresiOld.equals( other.field_40_hresiOld ) )
            return false;
        if ( field_41_ibstRMarkDel != other.field_41_ibstRMarkDel )
            return false;
        if ( field_42_dttmRMark == null )
        {
            if ( other.field_42_dttmRMark != null )
                return false;
        }
        else if ( !field_42_dttmRMark.equals( other.field_42_dttmRMark ) )
            return false;
        if ( field_43_dttmRMarkDel == null )
        {
            if ( other.field_43_dttmRMarkDel != null )
                return false;
        }
        else if ( !field_43_dttmRMarkDel.equals( other.field_43_dttmRMarkDel ) )
            return false;
        if ( field_44_istd != other.field_44_istd )
            return false;
        if ( field_45_idslRMReason != other.field_45_idslRMReason )
            return false;
        if ( field_46_idslReasonDel != other.field_46_idslReasonDel )
            return false;
        if ( field_47_cpg != other.field_47_cpg )
            return false;
        if ( field_48_Highlight != other.field_48_Highlight )
            return false;
        if ( field_49_CharsetFlags != other.field_49_CharsetFlags )
            return false;
        if ( field_50_chse != other.field_50_chse )
            return false;
        if ( field_51_fPropRMark != other.field_51_fPropRMark )
            return false;
        if ( field_52_ibstPropRMark != other.field_52_ibstPropRMark )
            return false;
        if ( field_53_dttmPropRMark == null )
        {
            if ( other.field_53_dttmPropRMark != null )
                return false;
        }
        else if ( !field_53_dttmPropRMark.equals( other.field_53_dttmPropRMark ) )
            return false;
        if ( field_54_fConflictOrig != other.field_54_fConflictOrig )
            return false;
        if ( field_55_fConflictOtherDel != other.field_55_fConflictOtherDel )
            return false;
        if ( field_56_wConflict != other.field_56_wConflict )
            return false;
        if ( field_57_IbstConflict != other.field_57_IbstConflict )
            return false;
        if ( field_58_dttmConflict == null )
        {
            if ( other.field_58_dttmConflict != null )
                return false;
        }
        else if ( !field_58_dttmConflict.equals( other.field_58_dttmConflict ) )
            return false;
        if ( field_59_fDispFldRMark != other.field_59_fDispFldRMark )
            return false;
        if ( field_60_ibstDispFldRMark != other.field_60_ibstDispFldRMark )
            return false;
        if ( field_61_dttmDispFldRMark == null )
        {
            if ( other.field_61_dttmDispFldRMark != null )
                return false;
        }
        else if ( !field_61_dttmDispFldRMark.equals( other.field_61_dttmDispFldRMark ) )
            return false;
        if ( !Arrays.equals( field_62_xstDispFldRMark, other.field_62_xstDispFldRMark ) )
            return false;
        if ( field_63_fcObjp != other.field_63_fcObjp )
            return false;
        if ( field_64_lbrCRJ != other.field_64_lbrCRJ )
            return false;
        if ( field_65_fSpecVanish != other.field_65_fSpecVanish )
            return false;
        if ( field_66_fHasOldProps != other.field_66_fHasOldProps )
            return false;
        if ( field_67_fSdtVanish != other.field_67_fSdtVanish )
            return false;
        if ( field_68_wCharScale != other.field_68_wCharScale )
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{field_1_grpfChp, field_2_hps, field_3_ftcAscii, field_4_ftcFE, field_5_ftcOther,
           field_6_ftcBi, field_7_dxaSpace, field_8_cv, field_9_ico, field_10_pctCharWidth, field_11_lidDefault,
           field_12_lidFE, field_13_kcd, field_14_fUndetermine, field_15_iss, field_16_fSpecSymbol, field_17_idct,
           field_18_idctHint, field_19_kul, field_20_hresi, field_21_hpsKern, field_22_hpsPos, field_23_shd,
           field_24_brc, field_25_ibstRMark, field_26_sfxtText, field_27_fDblBdr, field_28_fBorderWS, field_29_ufel,
           field_30_copt, field_31_hpsAsci, field_32_hpsFE, field_33_hpsBi, field_34_ftcSym, field_35_xchSym,
           field_36_fcPic, field_37_fcObj, field_38_lTagObj, field_39_fcData, field_40_hresiOld, field_41_ibstRMarkDel,
           field_42_dttmRMark, field_43_dttmRMarkDel, field_44_istd, field_45_idslRMReason, field_46_idslReasonDel,
           field_47_cpg, field_48_Highlight, field_49_CharsetFlags, field_50_chse, field_51_fPropRMark,
           field_52_ibstPropRMark, field_53_dttmPropRMark, field_54_fConflictOrig, field_55_fConflictOtherDel,
           field_56_wConflict, field_57_IbstConflict, field_58_dttmConflict, field_59_fDispFldRMark,
           field_60_ibstDispFldRMark, field_61_dttmDispFldRMark, field_62_xstDispFldRMark, field_63_fcObjp,
           field_64_lbrCRJ, field_65_fSpecVanish, field_66_fHasOldProps, field_67_fSdtVanish, field_68_wCharScale});
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[CHP]\n");
        builder.append("    .grpfChp              = ");
        builder.append(" (").append(getGrpfChp()).append(" )\n");
        builder.append("         .fBold                    = ").append(isFBold()).append('\n');
        builder.append("         .fItalic                  = ").append(isFItalic()).append('\n');
        builder.append("         .fRMarkDel                = ").append(isFRMarkDel()).append('\n');
        builder.append("         .fOutline                 = ").append(isFOutline()).append('\n');
        builder.append("         .fFldVanish               = ").append(isFFldVanish()).append('\n');
        builder.append("         .fSmallCaps               = ").append(isFSmallCaps()).append('\n');
        builder.append("         .fCaps                    = ").append(isFCaps()).append('\n');
        builder.append("         .fVanish                  = ").append(isFVanish()).append('\n');
        builder.append("         .fRMark                   = ").append(isFRMark()).append('\n');
        builder.append("         .fSpec                    = ").append(isFSpec()).append('\n');
        builder.append("         .fStrike                  = ").append(isFStrike()).append('\n');
        builder.append("         .fObj                     = ").append(isFObj()).append('\n');
        builder.append("         .fShadow                  = ").append(isFShadow()).append('\n');
        builder.append("         .fLowerCase               = ").append(isFLowerCase()).append('\n');
        builder.append("         .fData                    = ").append(isFData()).append('\n');
        builder.append("         .fOle2                    = ").append(isFOle2()).append('\n');
        builder.append("         .fEmboss                  = ").append(isFEmboss()).append('\n');
        builder.append("         .fImprint                 = ").append(isFImprint()).append('\n');
        builder.append("         .fDStrike                 = ").append(isFDStrike()).append('\n');
        builder.append("         .fUsePgsuSettings         = ").append(isFUsePgsuSettings()).append('\n');
        builder.append("         .fBoldBi                  = ").append(isFBoldBi()).append('\n');
        builder.append("         .fComplexScripts          = ").append(isFComplexScripts()).append('\n');
        builder.append("         .fItalicBi                = ").append(isFItalicBi()).append('\n');
        builder.append("         .fBiDi                    = ").append(isFBiDi()).append('\n');
        builder.append("         .fIcoBi                   = ").append(isFIcoBi()).append('\n');
        builder.append("         .fNonGlyph                = ").append(isFNonGlyph()).append('\n');
        builder.append("         .fBoldOther               = ").append(isFBoldOther()).append('\n');
        builder.append("         .fItalicOther             = ").append(isFItalicOther()).append('\n');
        builder.append("         .fNoProof                 = ").append(isFNoProof()).append('\n');
        builder.append("         .fWebHidden               = ").append(isFWebHidden()).append('\n');
        builder.append("         .fFitText                 = ").append(isFFitText()).append('\n');
        builder.append("         .fCalc                    = ").append(isFCalc()).append('\n');
        builder.append("         .fFmtLineProp             = ").append(isFFmtLineProp()).append('\n');
        builder.append("    .hps                  = ");
        builder.append(" (").append(getHps()).append(" )\n");
        builder.append("    .ftcAscii             = ");
        builder.append(" (").append(getFtcAscii()).append(" )\n");
        builder.append("    .ftcFE                = ");
        builder.append(" (").append(getFtcFE()).append(" )\n");
        builder.append("    .ftcOther             = ");
        builder.append(" (").append(getFtcOther()).append(" )\n");
        builder.append("    .ftcBi                = ");
        builder.append(" (").append(getFtcBi()).append(" )\n");
        builder.append("    .dxaSpace             = ");
        builder.append(" (").append(getDxaSpace()).append(" )\n");
        builder.append("    .cv                   = ");
        builder.append(" (").append(getCv()).append(" )\n");
        builder.append("    .ico                  = ");
        builder.append(" (").append(getIco()).append(" )\n");
        builder.append("    .pctCharWidth         = ");
        builder.append(" (").append(getPctCharWidth()).append(" )\n");
        builder.append("    .lidDefault           = ");
        builder.append(" (").append(getLidDefault()).append(" )\n");
        builder.append("    .lidFE                = ");
        builder.append(" (").append(getLidFE()).append(" )\n");
        builder.append("    .kcd                  = ");
        builder.append(" (").append(getKcd()).append(" )\n");
        builder.append("    .fUndetermine         = ");
        builder.append(" (").append(getFUndetermine()).append(" )\n");
        builder.append("    .iss                  = ");
        builder.append(" (").append(getIss()).append(" )\n");
        builder.append("    .fSpecSymbol          = ");
        builder.append(" (").append(getFSpecSymbol()).append(" )\n");
        builder.append("    .idct                 = ");
        builder.append(" (").append(getIdct()).append(" )\n");
        builder.append("    .idctHint             = ");
        builder.append(" (").append(getIdctHint()).append(" )\n");
        builder.append("    .kul                  = ");
        builder.append(" (").append(getKul()).append(" )\n");
        builder.append("    .hresi                = ");
        builder.append(" (").append(getHresi()).append(" )\n");
        builder.append("    .hpsKern              = ");
        builder.append(" (").append(getHpsKern()).append(" )\n");
        builder.append("    .hpsPos               = ");
        builder.append(" (").append(getHpsPos()).append(" )\n");
        builder.append("    .shd                  = ");
        builder.append(" (").append(getShd()).append(" )\n");
        builder.append("    .brc                  = ");
        builder.append(" (").append(getBrc()).append(" )\n");
        builder.append("    .ibstRMark            = ");
        builder.append(" (").append(getIbstRMark()).append(" )\n");
        builder.append("    .sfxtText             = ");
        builder.append(" (").append(getSfxtText()).append(" )\n");
        builder.append("    .fDblBdr              = ");
        builder.append(" (").append(getFDblBdr()).append(" )\n");
        builder.append("    .fBorderWS            = ");
        builder.append(" (").append(getFBorderWS()).append(" )\n");
        builder.append("    .ufel                 = ");
        builder.append(" (").append(getUfel()).append(" )\n");
        builder.append("         .itypFELayout             = ").append(getItypFELayout()).append('\n');
        builder.append("         .fTNY                     = ").append(isFTNY()).append('\n');
        builder.append("         .fWarichu                 = ").append(isFWarichu()).append('\n');
        builder.append("         .fKumimoji                = ").append(isFKumimoji()).append('\n');
        builder.append("         .fRuby                    = ").append(isFRuby()).append('\n');
        builder.append("         .fLSFitText               = ").append(isFLSFitText()).append('\n');
        builder.append("         .spare                    = ").append(getSpare()).append('\n');
        builder.append("    .copt                 = ");
        builder.append(" (").append(getCopt()).append(" )\n");
        builder.append("         .iWarichuBracket          = ").append(getIWarichuBracket()).append('\n');
        builder.append("         .fWarichuNoOpenBracket     = ").append(isFWarichuNoOpenBracket()).append('\n');
        builder.append("         .fTNYCompress             = ").append(isFTNYCompress()).append('\n');
        builder.append("         .fTNYFetchTxm             = ").append(isFTNYFetchTxm()).append('\n');
        builder.append("         .fCellFitText             = ").append(isFCellFitText()).append('\n');
        builder.append("         .unused                   = ").append(isUnused()).append('\n');
        builder.append("    .hpsAsci              = ");
        builder.append(" (").append(getHpsAsci()).append(" )\n");
        builder.append("    .hpsFE                = ");
        builder.append(" (").append(getHpsFE()).append(" )\n");
        builder.append("    .hpsBi                = ");
        builder.append(" (").append(getHpsBi()).append(" )\n");
        builder.append("    .ftcSym               = ");
        builder.append(" (").append(getFtcSym()).append(" )\n");
        builder.append("    .xchSym               = ");
        builder.append(" (").append(getXchSym()).append(" )\n");
        builder.append("    .fcPic                = ");
        builder.append(" (").append(getFcPic()).append(" )\n");
        builder.append("    .fcObj                = ");
        builder.append(" (").append(getFcObj()).append(" )\n");
        builder.append("    .lTagObj              = ");
        builder.append(" (").append(getLTagObj()).append(" )\n");
        builder.append("    .fcData               = ");
        builder.append(" (").append(getFcData()).append(" )\n");
        builder.append("    .hresiOld             = ");
        builder.append(" (").append(getHresiOld()).append(" )\n");
        builder.append("    .ibstRMarkDel         = ");
        builder.append(" (").append(getIbstRMarkDel()).append(" )\n");
        builder.append("    .dttmRMark            = ");
        builder.append(" (").append(getDttmRMark()).append(" )\n");
        builder.append("    .dttmRMarkDel         = ");
        builder.append(" (").append(getDttmRMarkDel()).append(" )\n");
        builder.append("    .istd                 = ");
        builder.append(" (").append(getIstd()).append(" )\n");
        builder.append("    .idslRMReason         = ");
        builder.append(" (").append(getIdslRMReason()).append(" )\n");
        builder.append("    .idslReasonDel        = ");
        builder.append(" (").append(getIdslReasonDel()).append(" )\n");
        builder.append("    .cpg                  = ");
        builder.append(" (").append(getCpg()).append(" )\n");
        builder.append("    .Highlight            = ");
        builder.append(" (").append(getHighlight()).append(" )\n");
        builder.append("         .icoHighlight             = ").append(getIcoHighlight()).append('\n');
        builder.append("         .fHighlight               = ").append(isFHighlight()).append('\n');
        builder.append("    .CharsetFlags         = ");
        builder.append(" (").append(getCharsetFlags()).append(" )\n");
        builder.append("         .fChsDiff                 = ").append(isFChsDiff()).append('\n');
        builder.append("         .fMacChs                  = ").append(isFMacChs()).append('\n');
        builder.append("    .chse                 = ");
        builder.append(" (").append(getChse()).append(" )\n");
        builder.append("    .fPropRMark           = ");
        builder.append(" (").append(getFPropRMark()).append(" )\n");
        builder.append("    .ibstPropRMark        = ");
        builder.append(" (").append(getIbstPropRMark()).append(" )\n");
        builder.append("    .dttmPropRMark        = ");
        builder.append(" (").append(getDttmPropRMark()).append(" )\n");
        builder.append("    .fConflictOrig        = ");
        builder.append(" (").append(getFConflictOrig()).append(" )\n");
        builder.append("    .fConflictOtherDel    = ");
        builder.append(" (").append(getFConflictOtherDel()).append(" )\n");
        builder.append("    .wConflict            = ");
        builder.append(" (").append(getWConflict()).append(" )\n");
        builder.append("    .IbstConflict         = ");
        builder.append(" (").append(getIbstConflict()).append(" )\n");
        builder.append("    .dttmConflict         = ");
        builder.append(" (").append(getDttmConflict()).append(" )\n");
        builder.append("    .fDispFldRMark        = ");
        builder.append(" (").append(getFDispFldRMark()).append(" )\n");
        builder.append("    .ibstDispFldRMark     = ");
        builder.append(" (").append(getIbstDispFldRMark()).append(" )\n");
        builder.append("    .dttmDispFldRMark     = ");
        builder.append(" (").append(getDttmDispFldRMark()).append(" )\n");
        builder.append("    .xstDispFldRMark      = ");
        builder.append(" (").append(Arrays.toString(getXstDispFldRMark())).append(" )\n");
        builder.append("    .fcObjp               = ");
        builder.append(" (").append(getFcObjp()).append(" )\n");
        builder.append("    .lbrCRJ               = ");
        builder.append(" (").append(getLbrCRJ()).append(" )\n");
        builder.append("    .fSpecVanish          = ");
        builder.append(" (").append(getFSpecVanish()).append(" )\n");
        builder.append("    .fHasOldProps         = ");
        builder.append(" (").append(getFHasOldProps()).append(" )\n");
        builder.append("    .fSdtVanish           = ");
        builder.append(" (").append(getFSdtVanish()).append(" )\n");
        builder.append("    .wCharScale           = ");
        builder.append(" (").append(getWCharScale()).append(" )\n");

        builder.append("[/CHP]\n");
        return builder.toString();
    }

    /**
     * Collection of the 32 flags.
     */
    @Internal
    public int getGrpfChp()
    {
        return field_1_grpfChp;
    }

    /**
     * Collection of the 32 flags.
     */
    @Internal
    public void setGrpfChp( int field_1_grpfChp )
    {
        this.field_1_grpfChp = field_1_grpfChp;
    }

    /**
     * Font size in half points.
     */
    @Internal
    public int getHps()
    {
        return field_2_hps;
    }

    /**
     * Font size in half points.
     */
    @Internal
    public void setHps( int field_2_hps )
    {
        this.field_2_hps = field_2_hps;
    }

    /**
     * Font for ASCII text.
     */
    @Internal
    public int getFtcAscii()
    {
        return field_3_ftcAscii;
    }

    /**
     * Font for ASCII text.
     */
    @Internal
    public void setFtcAscii( int field_3_ftcAscii )
    {
        this.field_3_ftcAscii = field_3_ftcAscii;
    }

    /**
     * Font for East Asian text.
     */
    @Internal
    public int getFtcFE()
    {
        return field_4_ftcFE;
    }

    /**
     * Font for East Asian text.
     */
    @Internal
    public void setFtcFE( int field_4_ftcFE )
    {
        this.field_4_ftcFE = field_4_ftcFE;
    }

    /**
     * Font for non-East Asian text.
     */
    @Internal
    public int getFtcOther()
    {
        return field_5_ftcOther;
    }

    /**
     * Font for non-East Asian text.
     */
    @Internal
    public void setFtcOther( int field_5_ftcOther )
    {
        this.field_5_ftcOther = field_5_ftcOther;
    }

    /**
     * Font for Complex Scripts text.
     */
    @Internal
    public int getFtcBi()
    {
        return field_6_ftcBi;
    }

    /**
     * Font for Complex Scripts text.
     */
    @Internal
    public void setFtcBi( int field_6_ftcBi )
    {
        this.field_6_ftcBi = field_6_ftcBi;
    }

    /**
     * Space following each character in the run expressed in twip units..
     */
    @Internal
    public int getDxaSpace()
    {
        return field_7_dxaSpace;
    }

    /**
     * Space following each character in the run expressed in twip units..
     */
    @Internal
    public void setDxaSpace( int field_7_dxaSpace )
    {
        this.field_7_dxaSpace = field_7_dxaSpace;
    }

    /**
     * 24-bit color.
     */
    @Internal
    public Colorref getCv()
    {
        return field_8_cv;
    }

    /**
     * 24-bit color.
     */
    @Internal
    public void setCv( Colorref field_8_cv )
    {
        this.field_8_cv = field_8_cv;
    }

    /**
     * Color of text for Word 97.
     */
    @Internal
    public byte getIco()
    {
        return field_9_ico;
    }

    /**
     * Color of text for Word 97.
     */
    @Internal
    public void setIco( byte field_9_ico )
    {
        this.field_9_ico = field_9_ico;
    }

    /**
     * Character scale.
     */
    @Internal
    public int getPctCharWidth()
    {
        return field_10_pctCharWidth;
    }

    /**
     * Character scale.
     */
    @Internal
    public void setPctCharWidth( int field_10_pctCharWidth )
    {
        this.field_10_pctCharWidth = field_10_pctCharWidth;
    }

    /**
     * Get the lidDefault field for the CHP record.
     */
    @Internal
    public int getLidDefault()
    {
        return field_11_lidDefault;
    }

    /**
     * Set the lidDefault field for the CHP record.
     */
    @Internal
    public void setLidDefault( int field_11_lidDefault )
    {
        this.field_11_lidDefault = field_11_lidDefault;
    }

    /**
     * Get the lidFE field for the CHP record.
     */
    @Internal
    public int getLidFE()
    {
        return field_12_lidFE;
    }

    /**
     * Set the lidFE field for the CHP record.
     */
    @Internal
    public void setLidFE( int field_12_lidFE )
    {
        this.field_12_lidFE = field_12_lidFE;
    }

    /**
     * Emphasis mark.
     *
     * @return One of {@link #KCD_NON},{@link #KCD_DOT},{@link #KCD_COMMA},{@link #KCD_CIRCLE},{@link #KCD_UNDER_DOT}
     */
    @Internal
    public byte getKcd()
    {
        return field_13_kcd;
    }

    /**
     * Emphasis mark.
     *
     * @param field_13_kcd One of {@link #KCD_NON}, {@link #KCD_DOT}, {@link #KCD_COMMA}, {@link #KCD_CIRCLE}, {@link #KCD_UNDER_DOT}
     */
    @Internal
    public void setKcd( byte field_13_kcd )
    {
        this.field_13_kcd = field_13_kcd;
    }

    /**
     * Character is undetermined.
     */
    @Internal
    public boolean getFUndetermine()
    {
        return field_14_fUndetermine;
    }

    /**
     * Character is undetermined.
     */
    @Internal
    public void setFUndetermine( boolean field_14_fUndetermine )
    {
        this.field_14_fUndetermine = field_14_fUndetermine;
    }

    /**
     * Superscript/subscript indices.
     *
     * @return One of {@link #ISS_NONE},{@link #ISS_SUPERSCRIPTED},{@link #ISS_SUBSCRIPTED}
     */
    @Internal
    public byte getIss()
    {
        return field_15_iss;
    }

    /**
     * Superscript/subscript indices.
     *
     * @param field_15_iss One of {@link #ISS_NONE},{@link #ISS_SUPERSCRIPTED},{@link #ISS_SUBSCRIPTED}
     */
    @Internal
    public void setIss( byte field_15_iss )
    {
        this.field_15_iss = field_15_iss;
    }

    /**
     * Used by Word internally.
     */
    @Internal
    public boolean getFSpecSymbol()
    {
        return field_16_fSpecSymbol;
    }

    /**
     * Used by Word internally.
     */
    @Internal
    public void setFSpecSymbol( boolean field_16_fSpecSymbol )
    {
        this.field_16_fSpecSymbol = field_16_fSpecSymbol;
    }

    /**
     * Not stored in file.
     */
    @Internal
    public byte getIdct()
    {
        return field_17_idct;
    }

    /**
     * Not stored in file.
     */
    @Internal
    public void setIdct( byte field_17_idct )
    {
        this.field_17_idct = field_17_idct;
    }

    /**
     * Identifier of Character type.
     */
    @Internal
    public byte getIdctHint()
    {
        return field_18_idctHint;
    }

    /**
     * Identifier of Character type.
     */
    @Internal
    public void setIdctHint( byte field_18_idctHint )
    {
        this.field_18_idctHint = field_18_idctHint;
    }

    /**
     * Underline code.
     *
     * @return One of {@link #KUL_NONE},{@link #KUL_SINGLE},{@link #KUL_BY_WORD},{@link #KUL_DOUBLE},
     *      {@link #KUL_DOTTED},{@link #KUL_HIDDEN},{@link #KUL_THICK},{@link #KUL_DASH},{@link #KUL_DOT},
     *      {@link #KUL_DOT_DASH},{@link #KUL_DOT_DOT_DASH},{@link #KUL_WAVE},{@link #KUL_DOTTED_HEAVY},
     *      {@link #KUL_DASHED_HEAVY},{@link #KUL_DOT_DASH_HEAVY},{@link #KUL_DOT_DOT_DASH_HEAVY},
     *      {@link #KUL_WAVE_HEAVY},{@link #KUL_DASH_LONG},{@link #KUL_WAVE_DOUBLE},{@link #KUL_DASH_LONG_HEAVY}
     */
    @Internal
    public byte getKul()
    {
        return field_19_kul;
    }

    /**
     * Underline code.
     *
     * @param field_19_kul One of,{@link #KUL_NONE},{@link #KUL_SINGLE},{@link #KUL_BY_WORD},{@link #KUL_DOUBLE},
     *      {@link #KUL_DOTTED},{@link #KUL_HIDDEN},{@link #KUL_THICK},{@link #KUL_DASH},{@link #KUL_DOT},
     *      {@link #KUL_DOT_DASH},{@link #KUL_DOT_DOT_DASH},{@link #KUL_WAVE},{@link #KUL_DOTTED_HEAVY},
     *      {@link #KUL_DASHED_HEAVY},{@link #KUL_DOT_DASH_HEAVY},{@link #KUL_DOT_DOT_DASH_HEAVY},
     *      {@link #KUL_WAVE_HEAVY},{@link #KUL_DASH_LONG},{@link #KUL_WAVE_DOUBLE},{@link #KUL_DASH_LONG_HEAVY}
     */
    @Internal
    public void setKul( byte field_19_kul )
    {
        this.field_19_kul = field_19_kul;
    }

    /**
     * Get the hresi field for the CHP record.
     */
    @Internal
    public Hyphenation getHresi()
    {
        return field_20_hresi;
    }

    /**
     * Set the hresi field for the CHP record.
     */
    @Internal
    public void setHresi( Hyphenation field_20_hresi )
    {
        this.field_20_hresi = field_20_hresi;
    }

    /**
     * Kerning distance for characters in run recorded in half points.
     */
    @Internal
    public int getHpsKern()
    {
        return field_21_hpsKern;
    }

    /**
     * Kerning distance for characters in run recorded in half points.
     */
    @Internal
    public void setHpsKern( int field_21_hpsKern )
    {
        this.field_21_hpsKern = field_21_hpsKern;
    }

    /**
     * Reserved (actually used as vertical offset(?) value).
     */
    @Internal
    public short getHpsPos()
    {
        return field_22_hpsPos;
    }

    /**
     * Reserved (actually used as vertical offset(?) value).
     */
    @Internal
    public void setHpsPos( short field_22_hpsPos )
    {
        this.field_22_hpsPos = field_22_hpsPos;
    }

    /**
     * Shading.
     */
    @Internal
    public ShadingDescriptor getShd()
    {
        return field_23_shd;
    }

    /**
     * Shading.
     */
    @Internal
    public void setShd( ShadingDescriptor field_23_shd )
    {
        this.field_23_shd = field_23_shd;
    }

    /**
     * Border.
     */
    @Internal
    public BorderCode getBrc()
    {
        return field_24_brc;
    }

    /**
     * Border.
     */
    @Internal
    public void setBrc( BorderCode field_24_brc )
    {
        this.field_24_brc = field_24_brc;
    }

    /**
     * Index to author IDs stored in hsttbfRMark. Used when text in run was newly typed when revision marking was enabled.
     */
    @Internal
    public int getIbstRMark()
    {
        return field_25_ibstRMark;
    }

    /**
     * Index to author IDs stored in hsttbfRMark. Used when text in run was newly typed when revision marking was enabled.
     */
    @Internal
    public void setIbstRMark( int field_25_ibstRMark )
    {
        this.field_25_ibstRMark = field_25_ibstRMark;
    }

    /**
     * Text animation.
     *
     * @return One of {@link #SFXTTEXT_NO},{@link #SFXTTEXT_LAS_VEGAS_LIGHTS},{@link #SFXTTEXT_BACKGROUND_BLINK},
     *      {@link #SFXTTEXT_SPARKLE_TEXT},{@link #SFXTTEXT_MARCHING_ANTS},{@link #SFXTTEXT_MARCHING_RED_ANTS},
     *      {@link #SFXTTEXT_SHIMMER}
     */
    @Internal
    public byte getSfxtText()
    {
        return field_26_sfxtText;
    }

    /**
     * Text animation.
     *
     * @param field_26_sfxtText One of {@link #SFXTTEXT_NO},{@link #SFXTTEXT_LAS_VEGAS_LIGHTS},{@link #SFXTTEXT_BACKGROUND_BLINK},{@link #SFXTTEXT_SPARKLE_TEXT},{@link #SFXTTEXT_MARCHING_ANTS},{@link #SFXTTEXT_MARCHING_RED_ANTS},{@link #SFXTTEXT_SHIMMER}
     */
    @Internal
    public void setSfxtText( byte field_26_sfxtText )
    {
        this.field_26_sfxtText = field_26_sfxtText;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public boolean getFDblBdr()
    {
        return field_27_fDblBdr;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public void setFDblBdr( boolean field_27_fDblBdr )
    {
        this.field_27_fDblBdr = field_27_fDblBdr;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public boolean getFBorderWS()
    {
        return field_28_fBorderWS;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public void setFBorderWS( boolean field_28_fBorderWS )
    {
        this.field_28_fBorderWS = field_28_fBorderWS;
    }

    /**
     * Collection properties represented by itypFELayout and copt (East Asian layout properties).
     */
    @Internal
    public short getUfel()
    {
        return field_29_ufel;
    }

    /**
     * Collection properties represented by itypFELayout and copt (East Asian layout properties).
     */
    @Internal
    public void setUfel( short field_29_ufel )
    {
        this.field_29_ufel = field_29_ufel;
    }

    /**
     * Collection of the 5 flags.
     */
    @Internal
    public byte getCopt()
    {
        return field_30_copt;
    }

    /**
     * Collection of the 5 flags.
     */
    @Internal
    public void setCopt( byte field_30_copt )
    {
        this.field_30_copt = field_30_copt;
    }

    /**
     * Font size for ASCII font.
     */
    @Internal
    public int getHpsAsci()
    {
        return field_31_hpsAsci;
    }

    /**
     * Font size for ASCII font.
     */
    @Internal
    public void setHpsAsci( int field_31_hpsAsci )
    {
        this.field_31_hpsAsci = field_31_hpsAsci;
    }

    /**
     * Font size for East Asian text.
     */
    @Internal
    public int getHpsFE()
    {
        return field_32_hpsFE;
    }

    /**
     * Font size for East Asian text.
     */
    @Internal
    public void setHpsFE( int field_32_hpsFE )
    {
        this.field_32_hpsFE = field_32_hpsFE;
    }

    /**
     * Font size for Complex Scripts text.
     */
    @Internal
    public int getHpsBi()
    {
        return field_33_hpsBi;
    }

    /**
     * Font size for Complex Scripts text.
     */
    @Internal
    public void setHpsBi( int field_33_hpsBi )
    {
        this.field_33_hpsBi = field_33_hpsBi;
    }

    /**
     * an index into the rgffn structure. When chp.fSpec is 1 and the character recorded for the run in the document stream is chSymbol (0x28), chp.ftcSym identifies the font code of the symbol font that will be used to display the symbol character recorded in chp.xchSym..
     */
    @Internal
    public int getFtcSym()
    {
        return field_34_ftcSym;
    }

    /**
     * an index into the rgffn structure. When chp.fSpec is 1 and the character recorded for the run in the document stream is chSymbol (0x28), chp.ftcSym identifies the font code of the symbol font that will be used to display the symbol character recorded in chp.xchSym..
     */
    @Internal
    public void setFtcSym( int field_34_ftcSym )
    {
        this.field_34_ftcSym = field_34_ftcSym;
    }

    /**
     * When chp.fSpec==1 and the character recorded for the run in the document stream is chSymbol (0x28), the character stored chp.xchSym will be displayed using the font specified in chp.ftcSym..
     */
    @Internal
    public int getXchSym()
    {
        return field_35_xchSym;
    }

    /**
     * When chp.fSpec==1 and the character recorded for the run in the document stream is chSymbol (0x28), the character stored chp.xchSym will be displayed using the font specified in chp.ftcSym..
     */
    @Internal
    public void setXchSym( int field_35_xchSym )
    {
        this.field_35_xchSym = field_35_xchSym;
    }

    /**
     * Offset in data stream pointing to beginning of a picture when character is a picture character (character is 0x01 and chp.fSpec is 1)..
     */
    @Internal
    public int getFcPic()
    {
        return field_36_fcPic;
    }

    /**
     * Offset in data stream pointing to beginning of a picture when character is a picture character (character is 0x01 and chp.fSpec is 1)..
     */
    @Internal
    public void setFcPic( int field_36_fcPic )
    {
        this.field_36_fcPic = field_36_fcPic;
    }

    /**
     * Offset in data stream pointing to beginning of a picture when character is an OLE1 object character (character is 0x20 and chp.fSpec is 1, chp.fOle2 is 0)..
     */
    @Internal
    public int getFcObj()
    {
        return field_37_fcObj;
    }

    /**
     * Offset in data stream pointing to beginning of a picture when character is an OLE1 object character (character is 0x20 and chp.fSpec is 1, chp.fOle2 is 0)..
     */
    @Internal
    public void setFcObj( int field_37_fcObj )
    {
        this.field_37_fcObj = field_37_fcObj;
    }

    /**
     * An object ID for an OLE object, only set if chp.fSpec and chp.fOle2 are both true, and chp.fObj..
     */
    @Internal
    public int getLTagObj()
    {
        return field_38_lTagObj;
    }

    /**
     * An object ID for an OLE object, only set if chp.fSpec and chp.fOle2 are both true, and chp.fObj..
     */
    @Internal
    public void setLTagObj( int field_38_lTagObj )
    {
        this.field_38_lTagObj = field_38_lTagObj;
    }

    /**
     * Points to location of picture data, only if chp.fSpec is true..
     */
    @Internal
    public int getFcData()
    {
        return field_39_fcData;
    }

    /**
     * Points to location of picture data, only if chp.fSpec is true..
     */
    @Internal
    public void setFcData( int field_39_fcData )
    {
        this.field_39_fcData = field_39_fcData;
    }

    /**
     * Get the hresiOld field for the CHP record.
     */
    @Internal
    public Hyphenation getHresiOld()
    {
        return field_40_hresiOld;
    }

    /**
     * Set the hresiOld field for the CHP record.
     */
    @Internal
    public void setHresiOld( Hyphenation field_40_hresiOld )
    {
        this.field_40_hresiOld = field_40_hresiOld;
    }

    /**
     * Index to author IDs stored in hsttbfRMark. Used when text in run was deleted when revision marking was enabled..
     */
    @Internal
    public int getIbstRMarkDel()
    {
        return field_41_ibstRMarkDel;
    }

    /**
     * Index to author IDs stored in hsttbfRMark. Used when text in run was deleted when revision marking was enabled..
     */
    @Internal
    public void setIbstRMarkDel( int field_41_ibstRMarkDel )
    {
        this.field_41_ibstRMarkDel = field_41_ibstRMarkDel;
    }

    /**
     * Date/time at which this run of text was entered/modified by the author (Only recorded when revision marking is on.).
     */
    @Internal
    public DateAndTime getDttmRMark()
    {
        return field_42_dttmRMark;
    }

    /**
     * Date/time at which this run of text was entered/modified by the author (Only recorded when revision marking is on.).
     */
    @Internal
    public void setDttmRMark( DateAndTime field_42_dttmRMark )
    {
        this.field_42_dttmRMark = field_42_dttmRMark;
    }

    /**
     * Date/time at which this run of text was deleted by the author (Only recorded when revision marking is on.).
     */
    @Internal
    public DateAndTime getDttmRMarkDel()
    {
        return field_43_dttmRMarkDel;
    }

    /**
     * Date/time at which this run of text was deleted by the author (Only recorded when revision marking is on.).
     */
    @Internal
    public void setDttmRMarkDel( DateAndTime field_43_dttmRMarkDel )
    {
        this.field_43_dttmRMarkDel = field_43_dttmRMarkDel;
    }

    /**
     * Index to character style descriptor in the stylesheet that tags this run of text. When istd is istdNormalChar (10 decimal), characters in run are not affected by a character style. If chp.istd contains any other value, chpx of the specified character style are applied to CHP for this run before any other exceptional properties are applied..
     */
    @Internal
    public int getIstd()
    {
        return field_44_istd;
    }

    /**
     * Index to character style descriptor in the stylesheet that tags this run of text. When istd is istdNormalChar (10 decimal), characters in run are not affected by a character style. If chp.istd contains any other value, chpx of the specified character style are applied to CHP for this run before any other exceptional properties are applied..
     */
    @Internal
    public void setIstd( int field_44_istd )
    {
        this.field_44_istd = field_44_istd;
    }

    /**
     * An index to strings displayed as reasons for actions taken by Word's AutoFormat code.
     */
    @Internal
    public int getIdslRMReason()
    {
        return field_45_idslRMReason;
    }

    /**
     * An index to strings displayed as reasons for actions taken by Word's AutoFormat code.
     */
    @Internal
    public void setIdslRMReason( int field_45_idslRMReason )
    {
        this.field_45_idslRMReason = field_45_idslRMReason;
    }

    /**
     * An index to strings displayed as reasons for actions taken by Word's AutoFormat code.
     */
    @Internal
    public int getIdslReasonDel()
    {
        return field_46_idslReasonDel;
    }

    /**
     * An index to strings displayed as reasons for actions taken by Word's AutoFormat code.
     */
    @Internal
    public void setIdslReasonDel( int field_46_idslReasonDel )
    {
        this.field_46_idslReasonDel = field_46_idslReasonDel;
    }

    /**
     * Code page of run in pre-Unicode files.
     */
    @Internal
    public int getCpg()
    {
        return field_47_cpg;
    }

    /**
     * Code page of run in pre-Unicode files.
     */
    @Internal
    public void setCpg( int field_47_cpg )
    {
        this.field_47_cpg = field_47_cpg;
    }

    /**
     * Get the Highlight field for the CHP record.
     */
    @Internal
    public short getHighlight()
    {
        return field_48_Highlight;
    }

    /**
     * Set the Highlight field for the CHP record.
     */
    @Internal
    public void setHighlight( short field_48_Highlight )
    {
        this.field_48_Highlight = field_48_Highlight;
    }

    /**
     * Get the CharsetFlags field for the CHP record.
     */
    @Internal
    public short getCharsetFlags()
    {
        return field_49_CharsetFlags;
    }

    /**
     * Set the CharsetFlags field for the CHP record.
     */
    @Internal
    public void setCharsetFlags( short field_49_CharsetFlags )
    {
        this.field_49_CharsetFlags = field_49_CharsetFlags;
    }

    /**
     * Get the chse field for the CHP record.
     */
    @Internal
    public short getChse()
    {
        return field_50_chse;
    }

    /**
     * Set the chse field for the CHP record.
     */
    @Internal
    public void setChse( short field_50_chse )
    {
        this.field_50_chse = field_50_chse;
    }

    /**
     * properties have been changed with revision marking on.
     */
    @Internal
    public boolean getFPropRMark()
    {
        return field_51_fPropRMark;
    }

    /**
     * properties have been changed with revision marking on.
     */
    @Internal
    public void setFPropRMark( boolean field_51_fPropRMark )
    {
        this.field_51_fPropRMark = field_51_fPropRMark;
    }

    /**
     * Index to author IDs stored in hsttbfRMark. Used when properties have been changed when revision marking was enabled..
     */
    @Internal
    public int getIbstPropRMark()
    {
        return field_52_ibstPropRMark;
    }

    /**
     * Index to author IDs stored in hsttbfRMark. Used when properties have been changed when revision marking was enabled..
     */
    @Internal
    public void setIbstPropRMark( int field_52_ibstPropRMark )
    {
        this.field_52_ibstPropRMark = field_52_ibstPropRMark;
    }

    /**
     * Date/time at which properties of this were changed for this run of text by the author. (Only recorded when revision marking is on.).
     */
    @Internal
    public DateAndTime getDttmPropRMark()
    {
        return field_53_dttmPropRMark;
    }

    /**
     * Date/time at which properties of this were changed for this run of text by the author. (Only recorded when revision marking is on.).
     */
    @Internal
    public void setDttmPropRMark( DateAndTime field_53_dttmPropRMark )
    {
        this.field_53_dttmPropRMark = field_53_dttmPropRMark;
    }

    /**
     * When chp.wConflict!=0, this is TRUE when text is part of the original version of text. When FALSE, text is alternative introduced by reconciliation operation..
     */
    @Internal
    public boolean getFConflictOrig()
    {
        return field_54_fConflictOrig;
    }

    /**
     * When chp.wConflict!=0, this is TRUE when text is part of the original version of text. When FALSE, text is alternative introduced by reconciliation operation..
     */
    @Internal
    public void setFConflictOrig( boolean field_54_fConflictOrig )
    {
        this.field_54_fConflictOrig = field_54_fConflictOrig;
    }

    /**
     * When fConflictOtherDel==fTrue, the other side of a reconciliation conflict causes this text to be deleted.
     */
    @Internal
    public boolean getFConflictOtherDel()
    {
        return field_55_fConflictOtherDel;
    }

    /**
     * When fConflictOtherDel==fTrue, the other side of a reconciliation conflict causes this text to be deleted.
     */
    @Internal
    public void setFConflictOtherDel( boolean field_55_fConflictOtherDel )
    {
        this.field_55_fConflictOtherDel = field_55_fConflictOtherDel;
    }

    /**
     * When != 0, index number that identifies all text participating in a particular conflict incident.
     */
    @Internal
    public int getWConflict()
    {
        return field_56_wConflict;
    }

    /**
     * When != 0, index number that identifies all text participating in a particular conflict incident.
     */
    @Internal
    public void setWConflict( int field_56_wConflict )
    {
        this.field_56_wConflict = field_56_wConflict;
    }

    /**
     * Who made this change for this side of the conflict..
     */
    @Internal
    public int getIbstConflict()
    {
        return field_57_IbstConflict;
    }

    /**
     * Who made this change for this side of the conflict..
     */
    @Internal
    public void setIbstConflict( int field_57_IbstConflict )
    {
        this.field_57_IbstConflict = field_57_IbstConflict;
    }

    /**
     * When the change was made.
     */
    @Internal
    public DateAndTime getDttmConflict()
    {
        return field_58_dttmConflict;
    }

    /**
     * When the change was made.
     */
    @Internal
    public void setDttmConflict( DateAndTime field_58_dttmConflict )
    {
        this.field_58_dttmConflict = field_58_dttmConflict;
    }

    /**
     * the number for a ListNum field is being tracked in xstDispFldRMark. If that number is different from the current value, the number has changed. Only valid for ListNum fields..
     */
    @Internal
    public boolean getFDispFldRMark()
    {
        return field_59_fDispFldRMark;
    }

    /**
     * the number for a ListNum field is being tracked in xstDispFldRMark. If that number is different from the current value, the number has changed. Only valid for ListNum fields..
     */
    @Internal
    public void setFDispFldRMark( boolean field_59_fDispFldRMark )
    {
        this.field_59_fDispFldRMark = field_59_fDispFldRMark;
    }

    /**
     * Index to author IDs stored in hsttbfRMark. Used when ListNum field numbering has been changed when revision marking was enabled..
     */
    @Internal
    public int getIbstDispFldRMark()
    {
        return field_60_ibstDispFldRMark;
    }

    /**
     * Index to author IDs stored in hsttbfRMark. Used when ListNum field numbering has been changed when revision marking was enabled..
     */
    @Internal
    public void setIbstDispFldRMark( int field_60_ibstDispFldRMark )
    {
        this.field_60_ibstDispFldRMark = field_60_ibstDispFldRMark;
    }

    /**
     * The date for the ListNum field number change.
     */
    @Internal
    public DateAndTime getDttmDispFldRMark()
    {
        return field_61_dttmDispFldRMark;
    }

    /**
     * The date for the ListNum field number change.
     */
    @Internal
    public void setDttmDispFldRMark( DateAndTime field_61_dttmDispFldRMark )
    {
        this.field_61_dttmDispFldRMark = field_61_dttmDispFldRMark;
    }

    /**
     * The string value of the ListNum field when revision mark tracking began.
     */
    @Internal
    public byte[] getXstDispFldRMark()
    {
        return field_62_xstDispFldRMark;
    }

    /**
     * The string value of the ListNum field when revision mark tracking began.
     */
    @Internal
    public void setXstDispFldRMark( byte[] field_62_xstDispFldRMark )
    {
        this.field_62_xstDispFldRMark = field_62_xstDispFldRMark;
    }

    /**
     * Offset in the data stream indicating the location of OLE object data.
     */
    @Internal
    public int getFcObjp()
    {
        return field_63_fcObjp;
    }

    /**
     * Offset in the data stream indicating the location of OLE object data.
     */
    @Internal
    public void setFcObjp( int field_63_fcObjp )
    {
        this.field_63_fcObjp = field_63_fcObjp;
    }

    /**
     * Line BReak code for xchCRJ.
     *
     * @return One of {@link #LBRCRJ_NONE},{@link #LBRCRJ_LEFT},{@link #LBRCRJ_RIGHT},{@link #LBRCRJ_BOTH}
     */
    @Internal
    public byte getLbrCRJ()
    {
        return field_64_lbrCRJ;
    }

    /**
     * Line BReak code for xchCRJ.
     *
     * @param field_64_lbrCRJ One of {@link #LBRCRJ_NONE},{@link #LBRCRJ_LEFT},{@link #LBRCRJ_RIGHT},{@link #LBRCRJ_BOTH}
     */
    @Internal
    public void setLbrCRJ( byte field_64_lbrCRJ )
    {
        this.field_64_lbrCRJ = field_64_lbrCRJ;
    }

    /**
     * Special hidden for leading emphasis (always hidden).
     */
    @Internal
    public boolean getFSpecVanish()
    {
        return field_65_fSpecVanish;
    }

    /**
     * Special hidden for leading emphasis (always hidden).
     */
    @Internal
    public void setFSpecVanish( boolean field_65_fSpecVanish )
    {
        this.field_65_fSpecVanish = field_65_fSpecVanish;
    }

    /**
     * Used for character property revision marking. The chp at the time fHasOldProps is set to 1, the is the old chp..
     */
    @Internal
    public boolean getFHasOldProps()
    {
        return field_66_fHasOldProps;
    }

    /**
     * Used for character property revision marking. The chp at the time fHasOldProps is set to 1, the is the old chp..
     */
    @Internal
    public void setFHasOldProps( boolean field_66_fHasOldProps )
    {
        this.field_66_fHasOldProps = field_66_fHasOldProps;
    }

    /**
     * Mark the character as hidden..
     */
    @Internal
    public boolean getFSdtVanish()
    {
        return field_67_fSdtVanish;
    }

    /**
     * Mark the character as hidden..
     */
    @Internal
    public void setFSdtVanish( boolean field_67_fSdtVanish )
    {
        this.field_67_fSdtVanish = field_67_fSdtVanish;
    }

    /**
     * Get the wCharScale field for the CHP record.
     */
    @Internal
    public int getWCharScale()
    {
        return field_68_wCharScale;
    }

    /**
     * Set the wCharScale field for the CHP record.
     */
    @Internal
    public void setWCharScale( int field_68_wCharScale )
    {
        this.field_68_wCharScale = field_68_wCharScale;
    }

    /**
     * Sets the fBold field value.
     * Text is bold
     */
    @Internal
    public void setFBold( boolean value )
    {
        field_1_grpfChp = fBold.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Text is bold
     * @return  the fBold field value.
     */
    @Internal
    public boolean isFBold()
    {
        return fBold.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fItalic field value.
     * Italic
     */
    @Internal
    public void setFItalic( boolean value )
    {
        field_1_grpfChp = fItalic.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Italic
     * @return  the fItalic field value.
     */
    @Internal
    public boolean isFItalic()
    {
        return fItalic.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fRMarkDel field value.
     * has been deleted and will be displayed with strikethrough when revision marked text is to be displayed
     */
    @Internal
    public void setFRMarkDel( boolean value )
    {
        field_1_grpfChp = fRMarkDel.setBoolean(field_1_grpfChp, value);
    }

    /**
     * has been deleted and will be displayed with strikethrough when revision marked text is to be displayed
     * @return  the fRMarkDel field value.
     */
    @Internal
    public boolean isFRMarkDel()
    {
        return fRMarkDel.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fOutline field value.
     * Outlined
     */
    @Internal
    public void setFOutline( boolean value )
    {
        field_1_grpfChp = fOutline.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Outlined
     * @return  the fOutline field value.
     */
    @Internal
    public boolean isFOutline()
    {
        return fOutline.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fFldVanish field value.
     * Used internally by Word
     */
    @Internal
    public void setFFldVanish( boolean value )
    {
        field_1_grpfChp = fFldVanish.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Used internally by Word
     * @return  the fFldVanish field value.
     */
    @Internal
    public boolean isFFldVanish()
    {
        return fFldVanish.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fSmallCaps field value.
     * Displayed with small caps
     */
    @Internal
    public void setFSmallCaps( boolean value )
    {
        field_1_grpfChp = fSmallCaps.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Displayed with small caps
     * @return  the fSmallCaps field value.
     */
    @Internal
    public boolean isFSmallCaps()
    {
        return fSmallCaps.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fCaps field value.
     * Displayed with caps
     */
    @Internal
    public void setFCaps( boolean value )
    {
        field_1_grpfChp = fCaps.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Displayed with caps
     * @return  the fCaps field value.
     */
    @Internal
    public boolean isFCaps()
    {
        return fCaps.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fVanish field value.
     * text has hidden format, and is not displayed unless fPagHidden is set in the DOP
     */
    @Internal
    public void setFVanish( boolean value )
    {
        field_1_grpfChp = fVanish.setBoolean(field_1_grpfChp, value);
    }

    /**
     * text has hidden format, and is not displayed unless fPagHidden is set in the DOP
     * @return  the fVanish field value.
     */
    @Internal
    public boolean isFVanish()
    {
        return fVanish.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fRMark field value.
     * text is newly typed since the last time revision marks have been accepted and will be displayed with an underline when revision marked text is to be displayed
     */
    @Internal
    public void setFRMark( boolean value )
    {
        field_1_grpfChp = fRMark.setBoolean(field_1_grpfChp, value);
    }

    /**
     * text is newly typed since the last time revision marks have been accepted and will be displayed with an underline when revision marked text is to be displayed
     * @return  the fRMark field value.
     */
    @Internal
    public boolean isFRMark()
    {
        return fRMark.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fSpec field value.
     * Character is a Word special character
     */
    @Internal
    public void setFSpec( boolean value )
    {
        field_1_grpfChp = fSpec.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Character is a Word special character
     * @return  the fSpec field value.
     */
    @Internal
    public boolean isFSpec()
    {
        return fSpec.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fStrike field value.
     * Displayed with strikethrough
     */
    @Internal
    public void setFStrike( boolean value )
    {
        field_1_grpfChp = fStrike.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Displayed with strikethrough
     * @return  the fStrike field value.
     */
    @Internal
    public boolean isFStrike()
    {
        return fStrike.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fObj field value.
     * Embedded objec
     */
    @Internal
    public void setFObj( boolean value )
    {
        field_1_grpfChp = fObj.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Embedded objec
     * @return  the fObj field value.
     */
    @Internal
    public boolean isFObj()
    {
        return fObj.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fShadow field value.
     * Character is drawn with a shadow
     */
    @Internal
    public void setFShadow( boolean value )
    {
        field_1_grpfChp = fShadow.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Character is drawn with a shadow
     * @return  the fShadow field value.
     */
    @Internal
    public boolean isFShadow()
    {
        return fShadow.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fLowerCase field value.
     * Character is displayed in lower case. This field may be set to 1 only when chp.fSmallCaps is 1.
     */
    @Internal
    public void setFLowerCase( boolean value )
    {
        field_1_grpfChp = fLowerCase.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Character is displayed in lower case. This field may be set to 1 only when chp.fSmallCaps is 1.
     * @return  the fLowerCase field value.
     */
    @Internal
    public boolean isFLowerCase()
    {
        return fLowerCase.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fData field value.
     * chp.fcPic points to an FFDATA, the data structure binary data used by Word to describe a form field. The bit chp.fData may only be 1 when chp.fSpec is also 1 and the special character in the document stream that has this property is a chPicture (0x01)
     */
    @Internal
    public void setFData( boolean value )
    {
        field_1_grpfChp = fData.setBoolean(field_1_grpfChp, value);
    }

    /**
     * chp.fcPic points to an FFDATA, the data structure binary data used by Word to describe a form field. The bit chp.fData may only be 1 when chp.fSpec is also 1 and the special character in the document stream that has this property is a chPicture (0x01)
     * @return  the fData field value.
     */
    @Internal
    public boolean isFData()
    {
        return fData.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fOle2 field value.
     * chp.lTagObj specifies a particular object in the object stream that specifies the particular OLE object in the stream that should be displayed when the chPicture fSpec character that is tagged with the fOle2 is encountered. The bit chp.fOle2 may only be 1 when chp.fSpec is also 1 and the special character in the document stream that has this property is a chPicture (0x01).
     */
    @Internal
    public void setFOle2( boolean value )
    {
        field_1_grpfChp = fOle2.setBoolean(field_1_grpfChp, value);
    }

    /**
     * chp.lTagObj specifies a particular object in the object stream that specifies the particular OLE object in the stream that should be displayed when the chPicture fSpec character that is tagged with the fOle2 is encountered. The bit chp.fOle2 may only be 1 when chp.fSpec is also 1 and the special character in the document stream that has this property is a chPicture (0x01).
     * @return  the fOle2 field value.
     */
    @Internal
    public boolean isFOle2()
    {
        return fOle2.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fEmboss field value.
     * Text is embossed
     */
    @Internal
    public void setFEmboss( boolean value )
    {
        field_1_grpfChp = fEmboss.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Text is embossed
     * @return  the fEmboss field value.
     */
    @Internal
    public boolean isFEmboss()
    {
        return fEmboss.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fImprint field value.
     * Text is engraved
     */
    @Internal
    public void setFImprint( boolean value )
    {
        field_1_grpfChp = fImprint.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Text is engraved
     * @return  the fImprint field value.
     */
    @Internal
    public boolean isFImprint()
    {
        return fImprint.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fDStrike field value.
     * Displayed with double strikethrough
     */
    @Internal
    public void setFDStrike( boolean value )
    {
        field_1_grpfChp = fDStrike.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Displayed with double strikethrough
     * @return  the fDStrike field value.
     */
    @Internal
    public boolean isFDStrike()
    {
        return fDStrike.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fUsePgsuSettings field value.
     * Used internally by Word
     */
    @Internal
    public void setFUsePgsuSettings( boolean value )
    {
        field_1_grpfChp = fUsePgsuSettings.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Used internally by Word
     * @return  the fUsePgsuSettings field value.
     */
    @Internal
    public boolean isFUsePgsuSettings()
    {
        return fUsePgsuSettings.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fBoldBi field value.
     * Complex Scripts text is bold
     */
    @Internal
    public void setFBoldBi( boolean value )
    {
        field_1_grpfChp = fBoldBi.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Complex Scripts text is bold
     * @return  the fBoldBi field value.
     */
    @Internal
    public boolean isFBoldBi()
    {
        return fBoldBi.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fComplexScripts field value.
     * Complex Scripts text that requires special processing to display and process
     */
    @Internal
    public void setFComplexScripts( boolean value )
    {
        field_1_grpfChp = fComplexScripts.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Complex Scripts text that requires special processing to display and process
     * @return  the fComplexScripts field value.
     */
    @Internal
    public boolean isFComplexScripts()
    {
        return fComplexScripts.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fItalicBi field value.
     * Complex Scripts text is italics
     */
    @Internal
    public void setFItalicBi( boolean value )
    {
        field_1_grpfChp = fItalicBi.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Complex Scripts text is italics
     * @return  the fItalicBi field value.
     */
    @Internal
    public boolean isFItalicBi()
    {
        return fItalicBi.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fBiDi field value.
     * Complex Scripts right-to-left text that requires special processing to display and process (character reordering; contextual shaping; display of combining characters and diacritics; specialized justification rules; cursor positioning)
     */
    @Internal
    public void setFBiDi( boolean value )
    {
        field_1_grpfChp = fBiDi.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Complex Scripts right-to-left text that requires special processing to display and process (character reordering; contextual shaping; display of combining characters and diacritics; specialized justification rules; cursor positioning)
     * @return  the fBiDi field value.
     */
    @Internal
    public boolean isFBiDi()
    {
        return fBiDi.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fIcoBi field value.
     * Used internally by Word
     */
    @Internal
    public void setFIcoBi( boolean value )
    {
        field_1_grpfChp = fIcoBi.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Used internally by Word
     * @return  the fIcoBi field value.
     */
    @Internal
    public boolean isFIcoBi()
    {
        return fIcoBi.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fNonGlyph field value.
     * Used internally by Word
     */
    @Internal
    public void setFNonGlyph( boolean value )
    {
        field_1_grpfChp = fNonGlyph.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Used internally by Word
     * @return  the fNonGlyph field value.
     */
    @Internal
    public boolean isFNonGlyph()
    {
        return fNonGlyph.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fBoldOther field value.
     * Used internally by Word 97 and earlier versions
     */
    @Internal
    public void setFBoldOther( boolean value )
    {
        field_1_grpfChp = fBoldOther.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Used internally by Word 97 and earlier versions
     * @return  the fBoldOther field value.
     */
    @Internal
    public boolean isFBoldOther()
    {
        return fBoldOther.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fItalicOther field value.
     * Used internally by Word 97 and earlier versions
     */
    @Internal
    public void setFItalicOther( boolean value )
    {
        field_1_grpfChp = fItalicOther.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Used internally by Word 97 and earlier versions
     * @return  the fItalicOther field value.
     */
    @Internal
    public boolean isFItalicOther()
    {
        return fItalicOther.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fNoProof field value.
     * When set to 1, do not check spelling or grammar
     */
    @Internal
    public void setFNoProof( boolean value )
    {
        field_1_grpfChp = fNoProof.setBoolean(field_1_grpfChp, value);
    }

    /**
     * When set to 1, do not check spelling or grammar
     * @return  the fNoProof field value.
     */
    @Internal
    public boolean isFNoProof()
    {
        return fNoProof.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fWebHidden field value.
     * Text should be hidden in Web View when set to 1
     */
    @Internal
    public void setFWebHidden( boolean value )
    {
        field_1_grpfChp = fWebHidden.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Text should be hidden in Web View when set to 1
     * @return  the fWebHidden field value.
     */
    @Internal
    public boolean isFWebHidden()
    {
        return fWebHidden.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fFitText field value.
     * Fit text when set to 1
     */
    @Internal
    public void setFFitText( boolean value )
    {
        field_1_grpfChp = fFitText.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Fit text when set to 1
     * @return  the fFitText field value.
     */
    @Internal
    public boolean isFFitText()
    {
        return fFitText.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fCalc field value.
     * Used internally by Word
     */
    @Internal
    public void setFCalc( boolean value )
    {
        field_1_grpfChp = fCalc.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Used internally by Word
     * @return  the fCalc field value.
     */
    @Internal
    public boolean isFCalc()
    {
        return fCalc.isSet(field_1_grpfChp);
    }

    /**
     * Sets the fFmtLineProp field value.
     * Used internally by Word
     */
    @Internal
    public void setFFmtLineProp( boolean value )
    {
        field_1_grpfChp = fFmtLineProp.setBoolean(field_1_grpfChp, value);
    }

    /**
     * Used internally by Word
     * @return  the fFmtLineProp field value.
     */
    @Internal
    public boolean isFFmtLineProp()
    {
        return fFmtLineProp.isSet(field_1_grpfChp);
    }

    /**
     * Sets the itypFELayout field value.
     *
     */
    @Internal
    public void setItypFELayout( short value )
    {
        field_29_ufel = (short)itypFELayout.setValue(field_29_ufel, value);
    }

    /**
     *
     * @return  the itypFELayout field value.
     */
    @Internal
    public short getItypFELayout()
    {
        return ( short )itypFELayout.getValue(field_29_ufel);
    }

    /**
     * Sets the fTNY field value.
     * Tatenakayoko: Horizontal-in-vertical (range of text in a direction perpendicular to the text flow) is used
     */
    @Internal
    public void setFTNY( boolean value )
    {
        field_29_ufel = (short)fTNY.setBoolean(field_29_ufel, value);
    }

    /**
     * Tatenakayoko: Horizontal-in-vertical (range of text in a direction perpendicular to the text flow) is used
     * @return  the fTNY field value.
     */
    @Internal
    public boolean isFTNY()
    {
        return fTNY.isSet(field_29_ufel);
    }

    /**
     * Sets the fWarichu field value.
     * Two lines in one (text in the group is displayed as two half-height lines within a line)
     */
    @Internal
    public void setFWarichu( boolean value )
    {
        field_29_ufel = (short)fWarichu.setBoolean(field_29_ufel, value);
    }

    /**
     * Two lines in one (text in the group is displayed as two half-height lines within a line)
     * @return  the fWarichu field value.
     */
    @Internal
    public boolean isFWarichu()
    {
        return fWarichu.isSet(field_29_ufel);
    }

    /**
     * Sets the fKumimoji field value.
     * combine characters
     */
    @Internal
    public void setFKumimoji( boolean value )
    {
        field_29_ufel = (short)fKumimoji.setBoolean(field_29_ufel, value);
    }

    /**
     * combine characters
     * @return  the fKumimoji field value.
     */
    @Internal
    public boolean isFKumimoji()
    {
        return fKumimoji.isSet(field_29_ufel);
    }

    /**
     * Sets the fRuby field value.
     * Phonetic guide
     */
    @Internal
    public void setFRuby( boolean value )
    {
        field_29_ufel = (short)fRuby.setBoolean(field_29_ufel, value);
    }

    /**
     * Phonetic guide
     * @return  the fRuby field value.
     */
    @Internal
    public boolean isFRuby()
    {
        return fRuby.isSet(field_29_ufel);
    }

    /**
     * Sets the fLSFitText field value.
     * fit text
     */
    @Internal
    public void setFLSFitText( boolean value )
    {
        field_29_ufel = (short)fLSFitText.setBoolean(field_29_ufel, value);
    }

    /**
     * fit text
     * @return  the fLSFitText field value.
     */
    @Internal
    public boolean isFLSFitText()
    {
        return fLSFitText.isSet(field_29_ufel);
    }

    /**
     * Sets the spare field value.
     * Unused
     */
    @Internal
    public void setSpare( byte value )
    {
        field_29_ufel = (short)spare.setValue(field_29_ufel, value);
    }

    /**
     * Unused
     * @return  the spare field value.
     */
    @Internal
    public byte getSpare()
    {
        return ( byte )spare.getValue(field_29_ufel);
    }

    /**
     * Sets the iWarichuBracket field value.
     * Bracket character for two-lines-in-one
     */
    @Internal
    public void setIWarichuBracket( byte value )
    {
        field_30_copt = (byte)iWarichuBracket.setValue(field_30_copt, value);
    }

    /**
     * Bracket character for two-lines-in-one
     * @return  the iWarichuBracket field value.
     */
    @Internal
    public byte getIWarichuBracket()
    {
        return ( byte )iWarichuBracket.getValue(field_30_copt);
    }

    /**
     * Sets the fWarichuNoOpenBracket field value.
     * Two-lines-in-one uses no open
     */
    @Internal
    public void setFWarichuNoOpenBracket( boolean value )
    {
        field_30_copt = (byte)fWarichuNoOpenBracket.setBoolean(field_30_copt, value);
    }

    /**
     * Two-lines-in-one uses no open
     * @return  the fWarichuNoOpenBracket field value.
     */
    @Internal
    public boolean isFWarichuNoOpenBracket()
    {
        return fWarichuNoOpenBracket.isSet(field_30_copt);
    }

    /**
     * Sets the fTNYCompress field value.
     * fit text in line
     */
    @Internal
    public void setFTNYCompress( boolean value )
    {
        field_30_copt = (byte)fTNYCompress.setBoolean(field_30_copt, value);
    }

    /**
     * fit text in line
     * @return  the fTNYCompress field value.
     */
    @Internal
    public boolean isFTNYCompress()
    {
        return fTNYCompress.isSet(field_30_copt);
    }

    /**
     * Sets the fTNYFetchTxm field value.
     * fetch text metrics
     */
    @Internal
    public void setFTNYFetchTxm( boolean value )
    {
        field_30_copt = (byte)fTNYFetchTxm.setBoolean(field_30_copt, value);
    }

    /**
     * fetch text metrics
     * @return  the fTNYFetchTxm field value.
     */
    @Internal
    public boolean isFTNYFetchTxm()
    {
        return fTNYFetchTxm.isSet(field_30_copt);
    }

    /**
     * Sets the fCellFitText field value.
     * Fit text in cell
     */
    @Internal
    public void setFCellFitText( boolean value )
    {
        field_30_copt = (byte)fCellFitText.setBoolean(field_30_copt, value);
    }

    /**
     * Fit text in cell
     * @return  the fCellFitText field value.
     */
    @Internal
    public boolean isFCellFitText()
    {
        return fCellFitText.isSet(field_30_copt);
    }

    /**
     * Sets the unused field value.
     * Not used
     */
    @Internal
    public void setUnused( boolean value )
    {
        field_30_copt = (byte)unused.setBoolean(field_30_copt, value);
    }

    /**
     * Not used
     * @return  the unused field value.
     */
    @Internal
    public boolean isUnused()
    {
        return unused.isSet(field_30_copt);
    }

    /**
     * Sets the icoHighlight field value.
     * Highlight color (see chp.ico)
     */
    @Internal
    public void setIcoHighlight( byte value )
    {
        field_48_Highlight = (short)icoHighlight.setValue(field_48_Highlight, value);
    }

    /**
     * Highlight color (see chp.ico)
     * @return  the icoHighlight field value.
     */
    @Internal
    public byte getIcoHighlight()
    {
        return ( byte )icoHighlight.getValue(field_48_Highlight);
    }

    /**
     * Sets the fHighlight field value.
     * When 1, characters are highlighted with color specified by chp.icoHighlight
     */
    @Internal
    public void setFHighlight( boolean value )
    {
        field_48_Highlight = (short)fHighlight.setBoolean(field_48_Highlight, value);
    }

    /**
     * When 1, characters are highlighted with color specified by chp.icoHighlight
     * @return  the fHighlight field value.
     */
    @Internal
    public boolean isFHighlight()
    {
        return fHighlight.isSet(field_48_Highlight);
    }

    /**
     * Sets the fChsDiff field value.
     * Pre-Unicode files, char's char set different from FIB char set
     */
    @Internal
    public void setFChsDiff( boolean value )
    {
        field_49_CharsetFlags = (short)fChsDiff.setBoolean(field_49_CharsetFlags, value);
    }

    /**
     * Pre-Unicode files, char's char set different from FIB char set
     * @return  the fChsDiff field value.
     */
    @Internal
    public boolean isFChsDiff()
    {
        return fChsDiff.isSet(field_49_CharsetFlags);
    }

    /**
     * Sets the fMacChs field value.
     * fTrue if char's are Macintosh char set
     */
    @Internal
    public void setFMacChs( boolean value )
    {
        field_49_CharsetFlags = (short)fMacChs.setBoolean(field_49_CharsetFlags, value);
    }

    /**
     * fTrue if char's are Macintosh char set
     * @return  the fMacChs field value.
     */
    @Internal
    public boolean isFMacChs()
    {
        return fMacChs.isSet(field_49_CharsetFlags);
    }

}  // END OF CLASS
