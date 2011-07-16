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

import org.apache.poi.hwpf.model.Hyphenation;
import org.apache.poi.hwpf.usermodel.BorderCode;
import org.apache.poi.hwpf.usermodel.DateAndTime;
import org.apache.poi.hwpf.usermodel.ShadingDescriptor;
import org.apache.poi.util.BitField;

/**
 * Character Properties.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author S. Ryan Ackley
 */
public abstract class CHPAbstractType
{

    protected int field_1_grpfChp;
        private static BitField  fBold = new BitField(0x00000001);
        private static BitField  fItalic = new BitField(0x00000002);
        private static BitField  fRMarkDel = new BitField(0x00000004);
        private static BitField  fOutline = new BitField(0x00000008);
        private static BitField  fFldVanish = new BitField(0x00000010);
        private static BitField  fSmallCaps = new BitField(0x00000020);
        private static BitField  fCaps = new BitField(0x00000040);
        private static BitField  fVanish = new BitField(0x00000080);
        private static BitField  fRMark = new BitField(0x00000100);
        private static BitField  fSpec = new BitField(0x00000200);
        private static BitField  fStrike = new BitField(0x00000400);
        private static BitField  fObj = new BitField(0x00000800);
        private static BitField  fShadow = new BitField(0x00001000);
        private static BitField  fLowerCase = new BitField(0x00002000);
        private static BitField  fData = new BitField(0x00004000);
        private static BitField  fOle2 = new BitField(0x00008000);
        private static BitField  fEmboss = new BitField(0x00010000);
        private static BitField  fImprint = new BitField(0x00020000);
        private static BitField  fDStrike = new BitField(0x00040000);
        private static BitField  fUsePgsuSettings = new BitField(0x00080000);
        private static BitField  fBoldBi = new BitField(0x00100000);
        private static BitField  fComplexScripts = new BitField(0x00200000);
        private static BitField  fItalicBi = new BitField(0x00400000);
        private static BitField  fBiDi = new BitField(0x00800000);
    protected int field_2_hps;
    protected int field_3_ftcAscii;
    protected int field_4_ftcFE;
    protected int field_5_ftcOther;
    protected int field_6_ftcBi;
    protected int field_7_dxaSpace;
    protected byte field_8_ico;
    protected int field_9_pctCharWidth;
    protected int field_10_lidDefault;
    protected int field_11_lidFE;
    protected byte field_12_kcd;
    /**/public final static byte KCD_NON = 0;
    /**/public final static byte KCD_DOT = 1;
    /**/public final static byte KCD_COMMA = 2;
    /**/public final static byte KCD_CIRCLE = 3;
    /**/public final static byte KCD_UNDER_DOT = 4;
    protected boolean field_13_fUndetermine;
    protected byte field_14_iss;
    /**/public final static byte ISS_NONE = 0;
    /**/public final static byte ISS_SUPERSCRIPTED = 1;
    /**/public final static byte ISS_SUBSCRIPTED = 2;
    protected boolean field_15_fSpecSymbol;
    protected byte field_16_idct;
    protected byte field_17_idctHint;
    protected byte field_18_kul;
    /**/public final static byte KUL_NONE = 0;
    /**/public final static byte KUL_SINGLE = 1;
    /**/public final static byte KUL_BY_WORD = 2;
    /**/public final static byte KUL_DOUBLE = 3;
    /**/public final static byte KUL_DOTTED = 4;
    /**/public final static byte KUL_HIDDEN = 5;
    /**/public final static byte KUL_THICK = 6;
    /**/public final static byte KUL_DASH = 7;
    /**/public final static byte KUL_DOT = 8;
    /**/public final static byte KUL_DOT_DASH = 9;
    /**/public final static byte KUL_DOT_DOT_DASH = 10;
    /**/public final static byte KUL_WAVE = 11;
    /**/public final static byte KUL_DOTTED_HEAVY = 20;
    /**/public final static byte KUL_DASHED_HEAVY = 23;
    /**/public final static byte KUL_DOT_DASH_HEAVY = 25;
    /**/public final static byte KUL_DOT_DOT_DASH_HEAVY = 26;
    /**/public final static byte KUL_WAVE_HEAVY = 27;
    /**/public final static byte KUL_DASH_LONG = 39;
    /**/public final static byte KUL_WAVE_DOUBLE = 43;
    /**/public final static byte KUL_DASH_LONG_HEAVY = 55;
    protected Hyphenation field_19_hresi;
    protected int field_20_hpsKern;
    protected short field_21_hpsPos;
    protected ShadingDescriptor field_22_shd;
    protected BorderCode field_23_brc;
    protected int field_24_ibstRMark;
    protected byte field_25_sfxtText;
    /**/public final static byte SFXTTEXT_NO = 0;
    /**/public final static byte SFXTTEXT_LAS_VEGAS_LIGHTS = 1;
    /**/public final static byte SFXTTEXT_BACKGROUND_BLINK = 2;
    /**/public final static byte SFXTTEXT_SPARKLE_TEXT = 3;
    /**/public final static byte SFXTTEXT_MARCHING_ANTS = 4;
    /**/public final static byte SFXTTEXT_MARCHING_RED_ANTS = 5;
    /**/public final static byte SFXTTEXT_SHIMMER = 6;
    protected boolean field_26_fDblBdr;
    protected boolean field_27_fBorderWS;
    protected short field_28_ufel;
        private static BitField  itypFELayout = new BitField(0x00ff);
        private static BitField  fTNY = new BitField(0x0100);
        private static BitField  fWarichu = new BitField(0x0200);
        private static BitField  fKumimoji = new BitField(0x0400);
        private static BitField  fRuby = new BitField(0x0800);
        private static BitField  fLSFitText = new BitField(0x1000);
        private static BitField  spare = new BitField(0xe000);
    protected byte field_29_copt;
        private static BitField  iWarichuBracket = new BitField(0x07);
        private static BitField  fWarichuNoOpenBracket = new BitField(0x08);
        private static BitField  fTNYCompress = new BitField(0x10);
        private static BitField  fTNYFetchTxm = new BitField(0x20);
        private static BitField  fCellFitText = new BitField(0x40);
        private static BitField  unused = new BitField(0x80);
    protected int field_30_hpsAsci;
    protected int field_31_hpsFE;
    protected int field_32_hpsBi;
    protected int field_33_ftcSym;
    protected int field_34_xchSym;
    protected int field_35_fcPic;
    protected int field_36_fcObj;
    protected int field_37_lTagObj;
    protected int field_38_fcData;
    protected Hyphenation field_39_hresiOld;
    protected int field_40_ibstRMarkDel;
    protected DateAndTime field_41_dttmRMark;
    protected DateAndTime field_42_dttmRMarkDel;
    protected int field_43_istd;
    protected int field_44_idslRMReason;
    protected int field_45_idslReasonDel;
    protected int field_46_cpg;
    protected short field_47_Highlight;
        private static BitField  icoHighlight = new BitField(0x001f);
        private static BitField  fHighlight = new BitField(0x0020);
    protected short field_48_CharsetFlags;
        private static BitField  fChsDiff = new BitField(0x0001);
        private static BitField  fMacChs = new BitField(0x0020);
    protected short field_49_chse;
    protected boolean field_50_fPropRMark;
    protected int field_51_ibstPropRMark;
    protected DateAndTime field_52_dttmPropRMark;
    protected boolean field_53_fConflictOrig;
    protected boolean field_54_fConflictOtherDel;
    protected int field_55_wConflict;
    protected int field_56_IbstConflict;
    protected DateAndTime field_57_dttmConflict;
    protected boolean field_58_fDispFldRMark;
    protected int field_59_ibstDispFldRMark;
    protected DateAndTime field_60_dttmDispFldRMark;
    protected byte[] field_61_xstDispFldRMark;
    protected int field_62_fcObjp;
    protected byte field_63_lbrCRJ;
    /**/public final static byte LBRCRJ_NONE = 0;
    /**/public final static byte LBRCRJ_LEFT = 1;
    /**/public final static byte LBRCRJ_RIGHT = 2;
    /**/public final static byte LBRCRJ_BOTH = 3;
    protected boolean field_64_fSpecVanish;
    protected boolean field_65_fHasOldProps;
    protected boolean field_66_fSdtVanish;
    protected int field_67_wCharScale;

    protected CHPAbstractType()
    {
        this.field_2_hps = 20;
        this.field_10_lidDefault = 0x0400;
        this.field_11_lidFE = 0x0400;
        this.field_19_hresi = new Hyphenation();
        this.field_22_shd = new ShadingDescriptor();
        this.field_23_brc = new BorderCode();
        this.field_35_fcPic = -1;
        this.field_39_hresiOld = new Hyphenation();
        this.field_41_dttmRMark = new DateAndTime();
        this.field_42_dttmRMarkDel = new DateAndTime();
        this.field_43_istd = 10;
        this.field_52_dttmPropRMark = new DateAndTime();
        this.field_57_dttmConflict = new DateAndTime();
        this.field_60_dttmDispFldRMark = new DateAndTime();
        this.field_61_xstDispFldRMark = new byte[0];
        this.field_67_wCharScale = 100;
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
        builder.append(" (").append(getXstDispFldRMark()).append(" )\n");
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
    public int getGrpfChp()
    {
        return field_1_grpfChp;
    }

    /**
     * Collection of the 32 flags.
     */
    public void setGrpfChp( int field_1_grpfChp )
    {
        this.field_1_grpfChp = field_1_grpfChp;
    }

    /**
     * Font size in half points.
     */
    public int getHps()
    {
        return field_2_hps;
    }

    /**
     * Font size in half points.
     */
    public void setHps( int field_2_hps )
    {
        this.field_2_hps = field_2_hps;
    }

    /**
     * Font for ASCII text.
     */
    public int getFtcAscii()
    {
        return field_3_ftcAscii;
    }

    /**
     * Font for ASCII text.
     */
    public void setFtcAscii( int field_3_ftcAscii )
    {
        this.field_3_ftcAscii = field_3_ftcAscii;
    }

    /**
     * Font for East Asian text.
     */
    public int getFtcFE()
    {
        return field_4_ftcFE;
    }

    /**
     * Font for East Asian text.
     */
    public void setFtcFE( int field_4_ftcFE )
    {
        this.field_4_ftcFE = field_4_ftcFE;
    }

    /**
     * Font for non-East Asian text.
     */
    public int getFtcOther()
    {
        return field_5_ftcOther;
    }

    /**
     * Font for non-East Asian text.
     */
    public void setFtcOther( int field_5_ftcOther )
    {
        this.field_5_ftcOther = field_5_ftcOther;
    }

    /**
     * Font for Complex Scripts text.
     */
    public int getFtcBi()
    {
        return field_6_ftcBi;
    }

    /**
     * Font for Complex Scripts text.
     */
    public void setFtcBi( int field_6_ftcBi )
    {
        this.field_6_ftcBi = field_6_ftcBi;
    }

    /**
     * Space following each character in the run expressed in twip units..
     */
    public int getDxaSpace()
    {
        return field_7_dxaSpace;
    }

    /**
     * Space following each character in the run expressed in twip units..
     */
    public void setDxaSpace( int field_7_dxaSpace )
    {
        this.field_7_dxaSpace = field_7_dxaSpace;
    }

    /**
     * Color of text for Word 97.
     */
    public byte getIco()
    {
        return field_8_ico;
    }

    /**
     * Color of text for Word 97.
     */
    public void setIco( byte field_8_ico )
    {
        this.field_8_ico = field_8_ico;
    }

    /**
     * Character scale.
     */
    public int getPctCharWidth()
    {
        return field_9_pctCharWidth;
    }

    /**
     * Character scale.
     */
    public void setPctCharWidth( int field_9_pctCharWidth )
    {
        this.field_9_pctCharWidth = field_9_pctCharWidth;
    }

    /**
     * Get the lidDefault field for the CHP record.
     */
    public int getLidDefault()
    {
        return field_10_lidDefault;
    }

    /**
     * Set the lidDefault field for the CHP record.
     */
    public void setLidDefault( int field_10_lidDefault )
    {
        this.field_10_lidDefault = field_10_lidDefault;
    }

    /**
     * Get the lidFE field for the CHP record.
     */
    public int getLidFE()
    {
        return field_11_lidFE;
    }

    /**
     * Set the lidFE field for the CHP record.
     */
    public void setLidFE( int field_11_lidFE )
    {
        this.field_11_lidFE = field_11_lidFE;
    }

    /**
     * Emphasis mark.
     *
     * @return One of 
     * <li>{@link #KCD_NON}
     * <li>{@link #KCD_DOT}
     * <li>{@link #KCD_COMMA}
     * <li>{@link #KCD_CIRCLE}
     * <li>{@link #KCD_UNDER_DOT}
     */
    public byte getKcd()
    {
        return field_12_kcd;
    }

    /**
     * Emphasis mark.
     *
     * @param field_12_kcd
     *        One of 
     * <li>{@link #KCD_NON}
     * <li>{@link #KCD_DOT}
     * <li>{@link #KCD_COMMA}
     * <li>{@link #KCD_CIRCLE}
     * <li>{@link #KCD_UNDER_DOT}
     */
    public void setKcd( byte field_12_kcd )
    {
        this.field_12_kcd = field_12_kcd;
    }

    /**
     * Character is undetermined.
     */
    public boolean getFUndetermine()
    {
        return field_13_fUndetermine;
    }

    /**
     * Character is undetermined.
     */
    public void setFUndetermine( boolean field_13_fUndetermine )
    {
        this.field_13_fUndetermine = field_13_fUndetermine;
    }

    /**
     * Superscript/subscript indices.
     *
     * @return One of 
     * <li>{@link #ISS_NONE}
     * <li>{@link #ISS_SUPERSCRIPTED}
     * <li>{@link #ISS_SUBSCRIPTED}
     */
    public byte getIss()
    {
        return field_14_iss;
    }

    /**
     * Superscript/subscript indices.
     *
     * @param field_14_iss
     *        One of 
     * <li>{@link #ISS_NONE}
     * <li>{@link #ISS_SUPERSCRIPTED}
     * <li>{@link #ISS_SUBSCRIPTED}
     */
    public void setIss( byte field_14_iss )
    {
        this.field_14_iss = field_14_iss;
    }

    /**
     * Used by Word internally.
     */
    public boolean getFSpecSymbol()
    {
        return field_15_fSpecSymbol;
    }

    /**
     * Used by Word internally.
     */
    public void setFSpecSymbol( boolean field_15_fSpecSymbol )
    {
        this.field_15_fSpecSymbol = field_15_fSpecSymbol;
    }

    /**
     * Not stored in file.
     */
    public byte getIdct()
    {
        return field_16_idct;
    }

    /**
     * Not stored in file.
     */
    public void setIdct( byte field_16_idct )
    {
        this.field_16_idct = field_16_idct;
    }

    /**
     * Identifier of Character type.
     */
    public byte getIdctHint()
    {
        return field_17_idctHint;
    }

    /**
     * Identifier of Character type.
     */
    public void setIdctHint( byte field_17_idctHint )
    {
        this.field_17_idctHint = field_17_idctHint;
    }

    /**
     * Underline code.
     *
     * @return One of 
     * <li>{@link #KUL_NONE}
     * <li>{@link #KUL_SINGLE}
     * <li>{@link #KUL_BY_WORD}
     * <li>{@link #KUL_DOUBLE}
     * <li>{@link #KUL_DOTTED}
     * <li>{@link #KUL_HIDDEN}
     * <li>{@link #KUL_THICK}
     * <li>{@link #KUL_DASH}
     * <li>{@link #KUL_DOT}
     * <li>{@link #KUL_DOT_DASH}
     * <li>{@link #KUL_DOT_DOT_DASH}
     * <li>{@link #KUL_WAVE}
     * <li>{@link #KUL_DOTTED_HEAVY}
     * <li>{@link #KUL_DASHED_HEAVY}
     * <li>{@link #KUL_DOT_DASH_HEAVY}
     * <li>{@link #KUL_DOT_DOT_DASH_HEAVY}
     * <li>{@link #KUL_WAVE_HEAVY}
     * <li>{@link #KUL_DASH_LONG}
     * <li>{@link #KUL_WAVE_DOUBLE}
     * <li>{@link #KUL_DASH_LONG_HEAVY}
     */
    public byte getKul()
    {
        return field_18_kul;
    }

    /**
     * Underline code.
     *
     * @param field_18_kul
     *        One of 
     * <li>{@link #KUL_NONE}
     * <li>{@link #KUL_SINGLE}
     * <li>{@link #KUL_BY_WORD}
     * <li>{@link #KUL_DOUBLE}
     * <li>{@link #KUL_DOTTED}
     * <li>{@link #KUL_HIDDEN}
     * <li>{@link #KUL_THICK}
     * <li>{@link #KUL_DASH}
     * <li>{@link #KUL_DOT}
     * <li>{@link #KUL_DOT_DASH}
     * <li>{@link #KUL_DOT_DOT_DASH}
     * <li>{@link #KUL_WAVE}
     * <li>{@link #KUL_DOTTED_HEAVY}
     * <li>{@link #KUL_DASHED_HEAVY}
     * <li>{@link #KUL_DOT_DASH_HEAVY}
     * <li>{@link #KUL_DOT_DOT_DASH_HEAVY}
     * <li>{@link #KUL_WAVE_HEAVY}
     * <li>{@link #KUL_DASH_LONG}
     * <li>{@link #KUL_WAVE_DOUBLE}
     * <li>{@link #KUL_DASH_LONG_HEAVY}
     */
    public void setKul( byte field_18_kul )
    {
        this.field_18_kul = field_18_kul;
    }

    /**
     * Get the hresi field for the CHP record.
     */
    public Hyphenation getHresi()
    {
        return field_19_hresi;
    }

    /**
     * Set the hresi field for the CHP record.
     */
    public void setHresi( Hyphenation field_19_hresi )
    {
        this.field_19_hresi = field_19_hresi;
    }

    /**
     * Kerning distance for characters in run recorded in half points.
     */
    public int getHpsKern()
    {
        return field_20_hpsKern;
    }

    /**
     * Kerning distance for characters in run recorded in half points.
     */
    public void setHpsKern( int field_20_hpsKern )
    {
        this.field_20_hpsKern = field_20_hpsKern;
    }

    /**
     * Reserved (actually used as vertical offset(?) value).
     */
    public short getHpsPos()
    {
        return field_21_hpsPos;
    }

    /**
     * Reserved (actually used as vertical offset(?) value).
     */
    public void setHpsPos( short field_21_hpsPos )
    {
        this.field_21_hpsPos = field_21_hpsPos;
    }

    /**
     * Shading.
     */
    public ShadingDescriptor getShd()
    {
        return field_22_shd;
    }

    /**
     * Shading.
     */
    public void setShd( ShadingDescriptor field_22_shd )
    {
        this.field_22_shd = field_22_shd;
    }

    /**
     * Border.
     */
    public BorderCode getBrc()
    {
        return field_23_brc;
    }

    /**
     * Border.
     */
    public void setBrc( BorderCode field_23_brc )
    {
        this.field_23_brc = field_23_brc;
    }

    /**
     * Index to author IDs stored in hsttbfRMark. Used when text in run was newly typed when revision marking was enabled.
     */
    public int getIbstRMark()
    {
        return field_24_ibstRMark;
    }

    /**
     * Index to author IDs stored in hsttbfRMark. Used when text in run was newly typed when revision marking was enabled.
     */
    public void setIbstRMark( int field_24_ibstRMark )
    {
        this.field_24_ibstRMark = field_24_ibstRMark;
    }

    /**
     * Text animation.
     *
     * @return One of 
     * <li>{@link #SFXTTEXT_NO}
     * <li>{@link #SFXTTEXT_LAS_VEGAS_LIGHTS}
     * <li>{@link #SFXTTEXT_BACKGROUND_BLINK}
     * <li>{@link #SFXTTEXT_SPARKLE_TEXT}
     * <li>{@link #SFXTTEXT_MARCHING_ANTS}
     * <li>{@link #SFXTTEXT_MARCHING_RED_ANTS}
     * <li>{@link #SFXTTEXT_SHIMMER}
     */
    public byte getSfxtText()
    {
        return field_25_sfxtText;
    }

    /**
     * Text animation.
     *
     * @param field_25_sfxtText
     *        One of 
     * <li>{@link #SFXTTEXT_NO}
     * <li>{@link #SFXTTEXT_LAS_VEGAS_LIGHTS}
     * <li>{@link #SFXTTEXT_BACKGROUND_BLINK}
     * <li>{@link #SFXTTEXT_SPARKLE_TEXT}
     * <li>{@link #SFXTTEXT_MARCHING_ANTS}
     * <li>{@link #SFXTTEXT_MARCHING_RED_ANTS}
     * <li>{@link #SFXTTEXT_SHIMMER}
     */
    public void setSfxtText( byte field_25_sfxtText )
    {
        this.field_25_sfxtText = field_25_sfxtText;
    }

    /**
     * Used internally by Word.
     */
    public boolean getFDblBdr()
    {
        return field_26_fDblBdr;
    }

    /**
     * Used internally by Word.
     */
    public void setFDblBdr( boolean field_26_fDblBdr )
    {
        this.field_26_fDblBdr = field_26_fDblBdr;
    }

    /**
     * Used internally by Word.
     */
    public boolean getFBorderWS()
    {
        return field_27_fBorderWS;
    }

    /**
     * Used internally by Word.
     */
    public void setFBorderWS( boolean field_27_fBorderWS )
    {
        this.field_27_fBorderWS = field_27_fBorderWS;
    }

    /**
     * Collection properties represented by itypFELayout and copt (East Asian layout properties).
     */
    public short getUfel()
    {
        return field_28_ufel;
    }

    /**
     * Collection properties represented by itypFELayout and copt (East Asian layout properties).
     */
    public void setUfel( short field_28_ufel )
    {
        this.field_28_ufel = field_28_ufel;
    }

    /**
     * Collection of the 5 flags.
     */
    public byte getCopt()
    {
        return field_29_copt;
    }

    /**
     * Collection of the 5 flags.
     */
    public void setCopt( byte field_29_copt )
    {
        this.field_29_copt = field_29_copt;
    }

    /**
     * Font size for ASCII font.
     */
    public int getHpsAsci()
    {
        return field_30_hpsAsci;
    }

    /**
     * Font size for ASCII font.
     */
    public void setHpsAsci( int field_30_hpsAsci )
    {
        this.field_30_hpsAsci = field_30_hpsAsci;
    }

    /**
     * Font size for East Asian text.
     */
    public int getHpsFE()
    {
        return field_31_hpsFE;
    }

    /**
     * Font size for East Asian text.
     */
    public void setHpsFE( int field_31_hpsFE )
    {
        this.field_31_hpsFE = field_31_hpsFE;
    }

    /**
     * Font size for Complex Scripts text.
     */
    public int getHpsBi()
    {
        return field_32_hpsBi;
    }

    /**
     * Font size for Complex Scripts text.
     */
    public void setHpsBi( int field_32_hpsBi )
    {
        this.field_32_hpsBi = field_32_hpsBi;
    }

    /**
     * an index into the rgffn structure. When chp.fSpec is 1 and the character recorded for the run in the document stream is chSymbol (0x28), chp.ftcSym identifies the font code of the symbol font that will be used to display the symbol character recorded in chp.xchSym..
     */
    public int getFtcSym()
    {
        return field_33_ftcSym;
    }

    /**
     * an index into the rgffn structure. When chp.fSpec is 1 and the character recorded for the run in the document stream is chSymbol (0x28), chp.ftcSym identifies the font code of the symbol font that will be used to display the symbol character recorded in chp.xchSym..
     */
    public void setFtcSym( int field_33_ftcSym )
    {
        this.field_33_ftcSym = field_33_ftcSym;
    }

    /**
     * When chp.fSpec==1 and the character recorded for the run in the document stream is chSymbol (0x28), the character stored chp.xchSym will be displayed using the font specified in chp.ftcSym..
     */
    public int getXchSym()
    {
        return field_34_xchSym;
    }

    /**
     * When chp.fSpec==1 and the character recorded for the run in the document stream is chSymbol (0x28), the character stored chp.xchSym will be displayed using the font specified in chp.ftcSym..
     */
    public void setXchSym( int field_34_xchSym )
    {
        this.field_34_xchSym = field_34_xchSym;
    }

    /**
     * Offset in data stream pointing to beginning of a picture when character is a picture character (character is 0x01 and chp.fSpec is 1)..
     */
    public int getFcPic()
    {
        return field_35_fcPic;
    }

    /**
     * Offset in data stream pointing to beginning of a picture when character is a picture character (character is 0x01 and chp.fSpec is 1)..
     */
    public void setFcPic( int field_35_fcPic )
    {
        this.field_35_fcPic = field_35_fcPic;
    }

    /**
     * Offset in data stream pointing to beginning of a picture when character is an OLE1 object character (character is 0x20 and chp.fSpec is 1, chp.fOle2 is 0)..
     */
    public int getFcObj()
    {
        return field_36_fcObj;
    }

    /**
     * Offset in data stream pointing to beginning of a picture when character is an OLE1 object character (character is 0x20 and chp.fSpec is 1, chp.fOle2 is 0)..
     */
    public void setFcObj( int field_36_fcObj )
    {
        this.field_36_fcObj = field_36_fcObj;
    }

    /**
     * An object ID for an OLE object, only set if chp.fSpec and chp.fOle2 are both true, and chp.fObj..
     */
    public int getLTagObj()
    {
        return field_37_lTagObj;
    }

    /**
     * An object ID for an OLE object, only set if chp.fSpec and chp.fOle2 are both true, and chp.fObj..
     */
    public void setLTagObj( int field_37_lTagObj )
    {
        this.field_37_lTagObj = field_37_lTagObj;
    }

    /**
     * Points to location of picture data, only if chp.fSpec is true..
     */
    public int getFcData()
    {
        return field_38_fcData;
    }

    /**
     * Points to location of picture data, only if chp.fSpec is true..
     */
    public void setFcData( int field_38_fcData )
    {
        this.field_38_fcData = field_38_fcData;
    }

    /**
     * Get the hresiOld field for the CHP record.
     */
    public Hyphenation getHresiOld()
    {
        return field_39_hresiOld;
    }

    /**
     * Set the hresiOld field for the CHP record.
     */
    public void setHresiOld( Hyphenation field_39_hresiOld )
    {
        this.field_39_hresiOld = field_39_hresiOld;
    }

    /**
     * Index to author IDs stored in hsttbfRMark. Used when text in run was deleted when revision marking was enabled..
     */
    public int getIbstRMarkDel()
    {
        return field_40_ibstRMarkDel;
    }

    /**
     * Index to author IDs stored in hsttbfRMark. Used when text in run was deleted when revision marking was enabled..
     */
    public void setIbstRMarkDel( int field_40_ibstRMarkDel )
    {
        this.field_40_ibstRMarkDel = field_40_ibstRMarkDel;
    }

    /**
     * Date/time at which this run of text was entered/modified by the author (Only recorded when revision marking is on.).
     */
    public DateAndTime getDttmRMark()
    {
        return field_41_dttmRMark;
    }

    /**
     * Date/time at which this run of text was entered/modified by the author (Only recorded when revision marking is on.).
     */
    public void setDttmRMark( DateAndTime field_41_dttmRMark )
    {
        this.field_41_dttmRMark = field_41_dttmRMark;
    }

    /**
     * Date/time at which this run of text was deleted by the author (Only recorded when revision marking is on.).
     */
    public DateAndTime getDttmRMarkDel()
    {
        return field_42_dttmRMarkDel;
    }

    /**
     * Date/time at which this run of text was deleted by the author (Only recorded when revision marking is on.).
     */
    public void setDttmRMarkDel( DateAndTime field_42_dttmRMarkDel )
    {
        this.field_42_dttmRMarkDel = field_42_dttmRMarkDel;
    }

    /**
     * Index to character style descriptor in the stylesheet that tags this run of text. When istd is istdNormalChar (10 decimal), characters in run are not affected by a character style. If chp.istd contains any other value, chpx of the specified character style are applied to CHP for this run before any other exceptional properties are applied..
     */
    public int getIstd()
    {
        return field_43_istd;
    }

    /**
     * Index to character style descriptor in the stylesheet that tags this run of text. When istd is istdNormalChar (10 decimal), characters in run are not affected by a character style. If chp.istd contains any other value, chpx of the specified character style are applied to CHP for this run before any other exceptional properties are applied..
     */
    public void setIstd( int field_43_istd )
    {
        this.field_43_istd = field_43_istd;
    }

    /**
     * An index to strings displayed as reasons for actions taken by Word‘s AutoFormat code.
     */
    public int getIdslRMReason()
    {
        return field_44_idslRMReason;
    }

    /**
     * An index to strings displayed as reasons for actions taken by Word‘s AutoFormat code.
     */
    public void setIdslRMReason( int field_44_idslRMReason )
    {
        this.field_44_idslRMReason = field_44_idslRMReason;
    }

    /**
     * An index to strings displayed as reasons for actions taken by Word‘s AutoFormat code.
     */
    public int getIdslReasonDel()
    {
        return field_45_idslReasonDel;
    }

    /**
     * An index to strings displayed as reasons for actions taken by Word‘s AutoFormat code.
     */
    public void setIdslReasonDel( int field_45_idslReasonDel )
    {
        this.field_45_idslReasonDel = field_45_idslReasonDel;
    }

    /**
     * Code page of run in pre-Unicode files.
     */
    public int getCpg()
    {
        return field_46_cpg;
    }

    /**
     * Code page of run in pre-Unicode files.
     */
    public void setCpg( int field_46_cpg )
    {
        this.field_46_cpg = field_46_cpg;
    }

    /**
     * Get the Highlight field for the CHP record.
     */
    public short getHighlight()
    {
        return field_47_Highlight;
    }

    /**
     * Set the Highlight field for the CHP record.
     */
    public void setHighlight( short field_47_Highlight )
    {
        this.field_47_Highlight = field_47_Highlight;
    }

    /**
     * Get the CharsetFlags field for the CHP record.
     */
    public short getCharsetFlags()
    {
        return field_48_CharsetFlags;
    }

    /**
     * Set the CharsetFlags field for the CHP record.
     */
    public void setCharsetFlags( short field_48_CharsetFlags )
    {
        this.field_48_CharsetFlags = field_48_CharsetFlags;
    }

    /**
     * Get the chse field for the CHP record.
     */
    public short getChse()
    {
        return field_49_chse;
    }

    /**
     * Set the chse field for the CHP record.
     */
    public void setChse( short field_49_chse )
    {
        this.field_49_chse = field_49_chse;
    }

    /**
     * properties have been changed with revision marking on.
     */
    public boolean getFPropRMark()
    {
        return field_50_fPropRMark;
    }

    /**
     * properties have been changed with revision marking on.
     */
    public void setFPropRMark( boolean field_50_fPropRMark )
    {
        this.field_50_fPropRMark = field_50_fPropRMark;
    }

    /**
     * Index to author IDs stored in hsttbfRMark. Used when properties have been changed when revision marking was enabled..
     */
    public int getIbstPropRMark()
    {
        return field_51_ibstPropRMark;
    }

    /**
     * Index to author IDs stored in hsttbfRMark. Used when properties have been changed when revision marking was enabled..
     */
    public void setIbstPropRMark( int field_51_ibstPropRMark )
    {
        this.field_51_ibstPropRMark = field_51_ibstPropRMark;
    }

    /**
     * Date/time at which properties of this were changed for this run of text by the author. (Only recorded when revision marking is on.).
     */
    public DateAndTime getDttmPropRMark()
    {
        return field_52_dttmPropRMark;
    }

    /**
     * Date/time at which properties of this were changed for this run of text by the author. (Only recorded when revision marking is on.).
     */
    public void setDttmPropRMark( DateAndTime field_52_dttmPropRMark )
    {
        this.field_52_dttmPropRMark = field_52_dttmPropRMark;
    }

    /**
     * When chp.wConflict!=0, this is TRUE when text is part of the original version of text. When FALSE, text is alternative introduced by reconciliation operation..
     */
    public boolean getFConflictOrig()
    {
        return field_53_fConflictOrig;
    }

    /**
     * When chp.wConflict!=0, this is TRUE when text is part of the original version of text. When FALSE, text is alternative introduced by reconciliation operation..
     */
    public void setFConflictOrig( boolean field_53_fConflictOrig )
    {
        this.field_53_fConflictOrig = field_53_fConflictOrig;
    }

    /**
     * When fConflictOtherDel==fTrue, the other side of a reconciliation conflict causes this text to be deleted.
     */
    public boolean getFConflictOtherDel()
    {
        return field_54_fConflictOtherDel;
    }

    /**
     * When fConflictOtherDel==fTrue, the other side of a reconciliation conflict causes this text to be deleted.
     */
    public void setFConflictOtherDel( boolean field_54_fConflictOtherDel )
    {
        this.field_54_fConflictOtherDel = field_54_fConflictOtherDel;
    }

    /**
     * When != 0, index number that identifies all text participating in a particular conflict incident.
     */
    public int getWConflict()
    {
        return field_55_wConflict;
    }

    /**
     * When != 0, index number that identifies all text participating in a particular conflict incident.
     */
    public void setWConflict( int field_55_wConflict )
    {
        this.field_55_wConflict = field_55_wConflict;
    }

    /**
     * Who made this change for this side of the conflict..
     */
    public int getIbstConflict()
    {
        return field_56_IbstConflict;
    }

    /**
     * Who made this change for this side of the conflict..
     */
    public void setIbstConflict( int field_56_IbstConflict )
    {
        this.field_56_IbstConflict = field_56_IbstConflict;
    }

    /**
     * When the change was made.
     */
    public DateAndTime getDttmConflict()
    {
        return field_57_dttmConflict;
    }

    /**
     * When the change was made.
     */
    public void setDttmConflict( DateAndTime field_57_dttmConflict )
    {
        this.field_57_dttmConflict = field_57_dttmConflict;
    }

    /**
     * the number for a ListNum field is being tracked in xstDispFldRMark. If that number is different from the current value, the number has changed. Only valid for ListNum fields..
     */
    public boolean getFDispFldRMark()
    {
        return field_58_fDispFldRMark;
    }

    /**
     * the number for a ListNum field is being tracked in xstDispFldRMark. If that number is different from the current value, the number has changed. Only valid for ListNum fields..
     */
    public void setFDispFldRMark( boolean field_58_fDispFldRMark )
    {
        this.field_58_fDispFldRMark = field_58_fDispFldRMark;
    }

    /**
     * Index to author IDs stored in hsttbfRMark. Used when ListNum field numbering has been changed when revision marking was enabled..
     */
    public int getIbstDispFldRMark()
    {
        return field_59_ibstDispFldRMark;
    }

    /**
     * Index to author IDs stored in hsttbfRMark. Used when ListNum field numbering has been changed when revision marking was enabled..
     */
    public void setIbstDispFldRMark( int field_59_ibstDispFldRMark )
    {
        this.field_59_ibstDispFldRMark = field_59_ibstDispFldRMark;
    }

    /**
     * The date for the ListNum field number change.
     */
    public DateAndTime getDttmDispFldRMark()
    {
        return field_60_dttmDispFldRMark;
    }

    /**
     * The date for the ListNum field number change.
     */
    public void setDttmDispFldRMark( DateAndTime field_60_dttmDispFldRMark )
    {
        this.field_60_dttmDispFldRMark = field_60_dttmDispFldRMark;
    }

    /**
     * The string value of the ListNum field when revision mark tracking began.
     */
    public byte[] getXstDispFldRMark()
    {
        return field_61_xstDispFldRMark;
    }

    /**
     * The string value of the ListNum field when revision mark tracking began.
     */
    public void setXstDispFldRMark( byte[] field_61_xstDispFldRMark )
    {
        this.field_61_xstDispFldRMark = field_61_xstDispFldRMark;
    }

    /**
     * Offset in the data stream indicating the location of OLE object data.
     */
    public int getFcObjp()
    {
        return field_62_fcObjp;
    }

    /**
     * Offset in the data stream indicating the location of OLE object data.
     */
    public void setFcObjp( int field_62_fcObjp )
    {
        this.field_62_fcObjp = field_62_fcObjp;
    }

    /**
     * Line BReak code for xchCRJ.
     *
     * @return One of 
     * <li>{@link #LBRCRJ_NONE}
     * <li>{@link #LBRCRJ_LEFT}
     * <li>{@link #LBRCRJ_RIGHT}
     * <li>{@link #LBRCRJ_BOTH}
     */
    public byte getLbrCRJ()
    {
        return field_63_lbrCRJ;
    }

    /**
     * Line BReak code for xchCRJ.
     *
     * @param field_63_lbrCRJ
     *        One of 
     * <li>{@link #LBRCRJ_NONE}
     * <li>{@link #LBRCRJ_LEFT}
     * <li>{@link #LBRCRJ_RIGHT}
     * <li>{@link #LBRCRJ_BOTH}
     */
    public void setLbrCRJ( byte field_63_lbrCRJ )
    {
        this.field_63_lbrCRJ = field_63_lbrCRJ;
    }

    /**
     * Special hidden for leading emphasis (always hidden).
     */
    public boolean getFSpecVanish()
    {
        return field_64_fSpecVanish;
    }

    /**
     * Special hidden for leading emphasis (always hidden).
     */
    public void setFSpecVanish( boolean field_64_fSpecVanish )
    {
        this.field_64_fSpecVanish = field_64_fSpecVanish;
    }

    /**
     * Used for character property revision marking. The chp at the time fHasOldProps is set to 1, the is the old chp..
     */
    public boolean getFHasOldProps()
    {
        return field_65_fHasOldProps;
    }

    /**
     * Used for character property revision marking. The chp at the time fHasOldProps is set to 1, the is the old chp..
     */
    public void setFHasOldProps( boolean field_65_fHasOldProps )
    {
        this.field_65_fHasOldProps = field_65_fHasOldProps;
    }

    /**
     * Mark the character as hidden..
     */
    public boolean getFSdtVanish()
    {
        return field_66_fSdtVanish;
    }

    /**
     * Mark the character as hidden..
     */
    public void setFSdtVanish( boolean field_66_fSdtVanish )
    {
        this.field_66_fSdtVanish = field_66_fSdtVanish;
    }

    /**
     * Get the wCharScale field for the CHP record.
     */
    public int getWCharScale()
    {
        return field_67_wCharScale;
    }

    /**
     * Set the wCharScale field for the CHP record.
     */
    public void setWCharScale( int field_67_wCharScale )
    {
        this.field_67_wCharScale = field_67_wCharScale;
    }

    /**
     * Sets the fBold field value.
     * Text is bold
     */
    public void setFBold( boolean value )
    {
        field_1_grpfChp = (int)fBold.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * Text is bold
     * @return  the fBold field value.
     */
    public boolean isFBold()
    {
        return fBold.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fItalic field value.
     * Italic
     */
    public void setFItalic( boolean value )
    {
        field_1_grpfChp = (int)fItalic.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * Italic
     * @return  the fItalic field value.
     */
    public boolean isFItalic()
    {
        return fItalic.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fRMarkDel field value.
     * has been deleted and will be displayed with strikethrough when revision marked text is to be displayed
     */
    public void setFRMarkDel( boolean value )
    {
        field_1_grpfChp = (int)fRMarkDel.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * has been deleted and will be displayed with strikethrough when revision marked text is to be displayed
     * @return  the fRMarkDel field value.
     */
    public boolean isFRMarkDel()
    {
        return fRMarkDel.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fOutline field value.
     * Outlined
     */
    public void setFOutline( boolean value )
    {
        field_1_grpfChp = (int)fOutline.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * Outlined
     * @return  the fOutline field value.
     */
    public boolean isFOutline()
    {
        return fOutline.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fFldVanish field value.
     * Used internally by Word
     */
    public void setFFldVanish( boolean value )
    {
        field_1_grpfChp = (int)fFldVanish.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * Used internally by Word
     * @return  the fFldVanish field value.
     */
    public boolean isFFldVanish()
    {
        return fFldVanish.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fSmallCaps field value.
     * Displayed with small caps
     */
    public void setFSmallCaps( boolean value )
    {
        field_1_grpfChp = (int)fSmallCaps.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * Displayed with small caps
     * @return  the fSmallCaps field value.
     */
    public boolean isFSmallCaps()
    {
        return fSmallCaps.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fCaps field value.
     * Displayed with caps
     */
    public void setFCaps( boolean value )
    {
        field_1_grpfChp = (int)fCaps.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * Displayed with caps
     * @return  the fCaps field value.
     */
    public boolean isFCaps()
    {
        return fCaps.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fVanish field value.
     * text has ―hidden‖ format, and is not displayed unless fPagHidden is set in the DOP
     */
    public void setFVanish( boolean value )
    {
        field_1_grpfChp = (int)fVanish.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * text has ―hidden‖ format, and is not displayed unless fPagHidden is set in the DOP
     * @return  the fVanish field value.
     */
    public boolean isFVanish()
    {
        return fVanish.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fRMark field value.
     * text is newly typed since the last time revision marks have been accepted and will be displayed with an underline when revision marked text is to be displayed
     */
    public void setFRMark( boolean value )
    {
        field_1_grpfChp = (int)fRMark.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * text is newly typed since the last time revision marks have been accepted and will be displayed with an underline when revision marked text is to be displayed
     * @return  the fRMark field value.
     */
    public boolean isFRMark()
    {
        return fRMark.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fSpec field value.
     * Character is a Word special character
     */
    public void setFSpec( boolean value )
    {
        field_1_grpfChp = (int)fSpec.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * Character is a Word special character
     * @return  the fSpec field value.
     */
    public boolean isFSpec()
    {
        return fSpec.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fStrike field value.
     * Displayed with strikethrough
     */
    public void setFStrike( boolean value )
    {
        field_1_grpfChp = (int)fStrike.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * Displayed with strikethrough
     * @return  the fStrike field value.
     */
    public boolean isFStrike()
    {
        return fStrike.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fObj field value.
     * Embedded objec
     */
    public void setFObj( boolean value )
    {
        field_1_grpfChp = (int)fObj.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * Embedded objec
     * @return  the fObj field value.
     */
    public boolean isFObj()
    {
        return fObj.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fShadow field value.
     * Character is drawn with a shadow
     */
    public void setFShadow( boolean value )
    {
        field_1_grpfChp = (int)fShadow.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * Character is drawn with a shadow
     * @return  the fShadow field value.
     */
    public boolean isFShadow()
    {
        return fShadow.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fLowerCase field value.
     * Character is displayed in lower case. This field may be set to 1 only when chp.fSmallCaps is 1.
     */
    public void setFLowerCase( boolean value )
    {
        field_1_grpfChp = (int)fLowerCase.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * Character is displayed in lower case. This field may be set to 1 only when chp.fSmallCaps is 1.
     * @return  the fLowerCase field value.
     */
    public boolean isFLowerCase()
    {
        return fLowerCase.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fData field value.
     * chp.fcPic points to an FFDATA, the data structure binary data used by Word to describe a form field. The bit chp.fData may only be 1 when chp.fSpec is also 1 and the special character in the document stream that has this property is a chPicture (0x01)
     */
    public void setFData( boolean value )
    {
        field_1_grpfChp = (int)fData.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * chp.fcPic points to an FFDATA, the data structure binary data used by Word to describe a form field. The bit chp.fData may only be 1 when chp.fSpec is also 1 and the special character in the document stream that has this property is a chPicture (0x01)
     * @return  the fData field value.
     */
    public boolean isFData()
    {
        return fData.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fOle2 field value.
     * chp.lTagObj specifies a particular object in the object stream that specifies the particular OLE object in the stream that should be displayed when the chPicture fSpec character that is tagged with the fOle2 is encountered. The bit chp.fOle2 may only be 1 when chp.fSpec is also 1 and the special character in the document stream that has this property is a chPicture (0x01).
     */
    public void setFOle2( boolean value )
    {
        field_1_grpfChp = (int)fOle2.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * chp.lTagObj specifies a particular object in the object stream that specifies the particular OLE object in the stream that should be displayed when the chPicture fSpec character that is tagged with the fOle2 is encountered. The bit chp.fOle2 may only be 1 when chp.fSpec is also 1 and the special character in the document stream that has this property is a chPicture (0x01).
     * @return  the fOle2 field value.
     */
    public boolean isFOle2()
    {
        return fOle2.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fEmboss field value.
     * Text is embossed
     */
    public void setFEmboss( boolean value )
    {
        field_1_grpfChp = (int)fEmboss.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * Text is embossed
     * @return  the fEmboss field value.
     */
    public boolean isFEmboss()
    {
        return fEmboss.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fImprint field value.
     * Text is engraved
     */
    public void setFImprint( boolean value )
    {
        field_1_grpfChp = (int)fImprint.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * Text is engraved
     * @return  the fImprint field value.
     */
    public boolean isFImprint()
    {
        return fImprint.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fDStrike field value.
     * Displayed with double strikethrough
     */
    public void setFDStrike( boolean value )
    {
        field_1_grpfChp = (int)fDStrike.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * Displayed with double strikethrough
     * @return  the fDStrike field value.
     */
    public boolean isFDStrike()
    {
        return fDStrike.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fUsePgsuSettings field value.
     * Used internally by Word
     */
    public void setFUsePgsuSettings( boolean value )
    {
        field_1_grpfChp = (int)fUsePgsuSettings.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * Used internally by Word
     * @return  the fUsePgsuSettings field value.
     */
    public boolean isFUsePgsuSettings()
    {
        return fUsePgsuSettings.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fBoldBi field value.
     * Complex Scripts text is bold
     */
    public void setFBoldBi( boolean value )
    {
        field_1_grpfChp = (int)fBoldBi.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * Complex Scripts text is bold
     * @return  the fBoldBi field value.
     */
    public boolean isFBoldBi()
    {
        return fBoldBi.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fComplexScripts field value.
     * Complex Scripts text that requires special processing to display and process
     */
    public void setFComplexScripts( boolean value )
    {
        field_1_grpfChp = (int)fComplexScripts.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * Complex Scripts text that requires special processing to display and process
     * @return  the fComplexScripts field value.
     */
    public boolean isFComplexScripts()
    {
        return fComplexScripts.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fItalicBi field value.
     * Complex Scripts text is italics
     */
    public void setFItalicBi( boolean value )
    {
        field_1_grpfChp = (int)fItalicBi.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * Complex Scripts text is italics
     * @return  the fItalicBi field value.
     */
    public boolean isFItalicBi()
    {
        return fItalicBi.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the fBiDi field value.
     * Complex Scripts right-to-left text that requires special processing to display and process (character reordering; contextual shaping; display of combining characters and diacritics; specialized justification rules; cursor positioning)
     */
    public void setFBiDi( boolean value )
    {
        field_1_grpfChp = (int)fBiDi.setBoolean(field_1_grpfChp, value);

        
    }

    /**
     * Complex Scripts right-to-left text that requires special processing to display and process (character reordering; contextual shaping; display of combining characters and diacritics; specialized justification rules; cursor positioning)
     * @return  the fBiDi field value.
     */
    public boolean isFBiDi()
    {
        return fBiDi.isSet(field_1_grpfChp);
        
    }

    /**
     * Sets the itypFELayout field value.
     * 
     */
    public void setItypFELayout( short value )
    {
        field_28_ufel = (short)itypFELayout.setValue(field_28_ufel, value);

        
    }

    /**
     * 
     * @return  the itypFELayout field value.
     */
    public short getItypFELayout()
    {
        return ( short )itypFELayout.getValue(field_28_ufel);
        
    }

    /**
     * Sets the fTNY field value.
     * Tatenakayoko: Horizontal–in-vertical (range of text in a direction perpendicular to the text flow) is used
     */
    public void setFTNY( boolean value )
    {
        field_28_ufel = (short)fTNY.setBoolean(field_28_ufel, value);

        
    }

    /**
     * Tatenakayoko: Horizontal–in-vertical (range of text in a direction perpendicular to the text flow) is used
     * @return  the fTNY field value.
     */
    public boolean isFTNY()
    {
        return fTNY.isSet(field_28_ufel);
        
    }

    /**
     * Sets the fWarichu field value.
     * Two lines in one (text in the group is displayed as two half-height lines within a line)
     */
    public void setFWarichu( boolean value )
    {
        field_28_ufel = (short)fWarichu.setBoolean(field_28_ufel, value);

        
    }

    /**
     * Two lines in one (text in the group is displayed as two half-height lines within a line)
     * @return  the fWarichu field value.
     */
    public boolean isFWarichu()
    {
        return fWarichu.isSet(field_28_ufel);
        
    }

    /**
     * Sets the fKumimoji field value.
     * combine characters
     */
    public void setFKumimoji( boolean value )
    {
        field_28_ufel = (short)fKumimoji.setBoolean(field_28_ufel, value);

        
    }

    /**
     * combine characters
     * @return  the fKumimoji field value.
     */
    public boolean isFKumimoji()
    {
        return fKumimoji.isSet(field_28_ufel);
        
    }

    /**
     * Sets the fRuby field value.
     * Phonetic guide
     */
    public void setFRuby( boolean value )
    {
        field_28_ufel = (short)fRuby.setBoolean(field_28_ufel, value);

        
    }

    /**
     * Phonetic guide
     * @return  the fRuby field value.
     */
    public boolean isFRuby()
    {
        return fRuby.isSet(field_28_ufel);
        
    }

    /**
     * Sets the fLSFitText field value.
     * fit text
     */
    public void setFLSFitText( boolean value )
    {
        field_28_ufel = (short)fLSFitText.setBoolean(field_28_ufel, value);

        
    }

    /**
     * fit text
     * @return  the fLSFitText field value.
     */
    public boolean isFLSFitText()
    {
        return fLSFitText.isSet(field_28_ufel);
        
    }

    /**
     * Sets the spare field value.
     * Unused
     */
    public void setSpare( byte value )
    {
        field_28_ufel = (short)spare.setValue(field_28_ufel, value);

        
    }

    /**
     * Unused
     * @return  the spare field value.
     */
    public byte getSpare()
    {
        return ( byte )spare.getValue(field_28_ufel);
        
    }

    /**
     * Sets the iWarichuBracket field value.
     * Bracket character for two-lines-in-one
     */
    public void setIWarichuBracket( byte value )
    {
        field_29_copt = (byte)iWarichuBracket.setValue(field_29_copt, value);

        
    }

    /**
     * Bracket character for two-lines-in-one
     * @return  the iWarichuBracket field value.
     */
    public byte getIWarichuBracket()
    {
        return ( byte )iWarichuBracket.getValue(field_29_copt);
        
    }

    /**
     * Sets the fWarichuNoOpenBracket field value.
     * Two-lines-in-one uses no open
     */
    public void setFWarichuNoOpenBracket( boolean value )
    {
        field_29_copt = (byte)fWarichuNoOpenBracket.setBoolean(field_29_copt, value);

        
    }

    /**
     * Two-lines-in-one uses no open
     * @return  the fWarichuNoOpenBracket field value.
     */
    public boolean isFWarichuNoOpenBracket()
    {
        return fWarichuNoOpenBracket.isSet(field_29_copt);
        
    }

    /**
     * Sets the fTNYCompress field value.
     * fit text in line
     */
    public void setFTNYCompress( boolean value )
    {
        field_29_copt = (byte)fTNYCompress.setBoolean(field_29_copt, value);

        
    }

    /**
     * fit text in line
     * @return  the fTNYCompress field value.
     */
    public boolean isFTNYCompress()
    {
        return fTNYCompress.isSet(field_29_copt);
        
    }

    /**
     * Sets the fTNYFetchTxm field value.
     * fetch text metrics
     */
    public void setFTNYFetchTxm( boolean value )
    {
        field_29_copt = (byte)fTNYFetchTxm.setBoolean(field_29_copt, value);

        
    }

    /**
     * fetch text metrics
     * @return  the fTNYFetchTxm field value.
     */
    public boolean isFTNYFetchTxm()
    {
        return fTNYFetchTxm.isSet(field_29_copt);
        
    }

    /**
     * Sets the fCellFitText field value.
     * Fit text in cell
     */
    public void setFCellFitText( boolean value )
    {
        field_29_copt = (byte)fCellFitText.setBoolean(field_29_copt, value);

        
    }

    /**
     * Fit text in cell
     * @return  the fCellFitText field value.
     */
    public boolean isFCellFitText()
    {
        return fCellFitText.isSet(field_29_copt);
        
    }

    /**
     * Sets the unused field value.
     * Not used
     */
    public void setUnused( boolean value )
    {
        field_29_copt = (byte)unused.setBoolean(field_29_copt, value);

        
    }

    /**
     * Not used
     * @return  the unused field value.
     */
    public boolean isUnused()
    {
        return unused.isSet(field_29_copt);
        
    }

    /**
     * Sets the icoHighlight field value.
     * Highlight color (see chp.ico)
     */
    public void setIcoHighlight( byte value )
    {
        field_47_Highlight = (short)icoHighlight.setValue(field_47_Highlight, value);

        
    }

    /**
     * Highlight color (see chp.ico)
     * @return  the icoHighlight field value.
     */
    public byte getIcoHighlight()
    {
        return ( byte )icoHighlight.getValue(field_47_Highlight);
        
    }

    /**
     * Sets the fHighlight field value.
     * When 1, characters are highlighted with color specified by chp.icoHighlight
     */
    public void setFHighlight( boolean value )
    {
        field_47_Highlight = (short)fHighlight.setBoolean(field_47_Highlight, value);

        
    }

    /**
     * When 1, characters are highlighted with color specified by chp.icoHighlight
     * @return  the fHighlight field value.
     */
    public boolean isFHighlight()
    {
        return fHighlight.isSet(field_47_Highlight);
        
    }

    /**
     * Sets the fChsDiff field value.
     * Pre-Unicode files, char's char set different from FIB char set
     */
    public void setFChsDiff( boolean value )
    {
        field_48_CharsetFlags = (short)fChsDiff.setBoolean(field_48_CharsetFlags, value);

        
    }

    /**
     * Pre-Unicode files, char's char set different from FIB char set
     * @return  the fChsDiff field value.
     */
    public boolean isFChsDiff()
    {
        return fChsDiff.isSet(field_48_CharsetFlags);
        
    }

    /**
     * Sets the fMacChs field value.
     * fTrue if char's are Macintosh char set
     */
    public void setFMacChs( boolean value )
    {
        field_48_CharsetFlags = (short)fMacChs.setBoolean(field_48_CharsetFlags, value);

        
    }

    /**
     * fTrue if char's are Macintosh char set
     * @return  the fMacChs field value.
     */
    public boolean isFMacChs()
    {
        return fMacChs.isSet(field_48_CharsetFlags);
        
    }

}  // END OF CLASS
