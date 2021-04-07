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
import java.util.stream.Stream;

import org.apache.poi.hwpf.model.TabDescriptor;
import org.apache.poi.hwpf.usermodel.BorderCode;
import org.apache.poi.hwpf.usermodel.DateAndTime;
import org.apache.poi.hwpf.usermodel.DropCapSpecifier;
import org.apache.poi.hwpf.usermodel.LineSpacingDescriptor;
import org.apache.poi.hwpf.usermodel.ShadingDescriptor;
import org.apache.poi.util.BitField;
import org.apache.poi.util.Internal;

/**
 * Paragraph Properties.
 */
@SuppressWarnings("unused")
@Internal
public abstract class PAPAbstractType {

    protected static final byte BRCL_SINGLE = 0;
    protected static final byte BRCL_THICK = 1;
    protected static final byte BRCL_DOUBLE = 2;
    protected static final byte BRCL_SHADOW = 3;

    protected static final byte BRCP_NONE = 0;
    protected static final byte BRCP_BORDER_ABOVE = 1;
    protected static final byte BRCP_BORDER_BELOW = 2;
    protected static final byte BRCP_BOX_AROUND = 15;
    protected static final byte BRCP_BAR_TO_LEFT_OF_PARAGRAPH = 16;

    protected static final boolean FMINHEIGHT_EXACT = false;
    protected static final boolean FMINHEIGHT_AT_LEAST = true;

    protected static final byte WALIGNFONT_HANGING = 0;
    protected static final byte WALIGNFONT_CENTERED = 1;
    protected static final byte WALIGNFONT_ROMAN = 2;
    protected static final byte WALIGNFONT_VARIABLE = 3;
    protected static final byte WALIGNFONT_AUTO = 4;

    private static final BitField fVertical = new BitField(0x0001);
    private static final BitField fBackward = new BitField(0x0002);
    private static final BitField fRotateFont = new BitField(0x0004);

    protected int field_1_istd;
    protected boolean field_2_fSideBySide;
    protected boolean field_3_fKeep;
    protected boolean field_4_fKeepFollow;
    protected boolean field_5_fPageBreakBefore;
    protected byte field_6_brcl;
    protected byte field_7_brcp;
    protected byte field_8_ilvl;
    protected int field_9_ilfo;
    protected boolean field_10_fNoLnn;
    protected LineSpacingDescriptor field_11_lspd;
    protected int field_12_dyaBefore;
    protected int field_13_dyaAfter;
    protected boolean field_14_fInTable;
    protected boolean field_15_finTableW97;
    protected boolean field_16_fTtp;
    protected int field_17_dxaAbs;
    protected int field_18_dyaAbs;
    protected int field_19_dxaWidth;
    protected boolean field_20_fBrLnAbove;
    protected boolean field_21_fBrLnBelow;
    protected byte field_22_pcVert;
    protected byte field_23_pcHorz;
    protected byte field_24_wr;
    protected boolean field_25_fNoAutoHyph;
    protected int field_26_dyaHeight;
    protected boolean field_27_fMinHeight;
    protected DropCapSpecifier field_28_dcs;
    protected int field_29_dyaFromText;
    protected int field_30_dxaFromText;
    protected boolean field_31_fLocked;
    protected boolean field_32_fWidowControl;
    protected boolean field_33_fKinsoku;
    protected boolean field_34_fWordWrap;
    protected boolean field_35_fOverflowPunct;
    protected boolean field_36_fTopLinePunct;
    protected boolean field_37_fAutoSpaceDE;
    protected boolean field_38_fAutoSpaceDN;
    protected int field_39_wAlignFont;
    protected short field_40_fontAlign;
    protected byte field_41_lvl;
    protected boolean field_42_fBiDi;
    protected boolean field_43_fNumRMIns;
    protected boolean field_44_fCrLf;
    protected boolean field_45_fUsePgsuSettings;
    protected boolean field_46_fAdjustRight;
    protected int field_47_itap;
    protected boolean field_48_fInnerTableCell;
    protected boolean field_49_fOpenTch;
    protected boolean field_50_fTtpEmbedded;
    protected short field_51_dxcRight;
    protected short field_52_dxcLeft;
    protected short field_53_dxcLeft1;
    protected boolean field_54_fDyaBeforeAuto;
    protected boolean field_55_fDyaAfterAuto;
    protected int field_56_dxaRight;
    protected int field_57_dxaLeft;
    protected int field_58_dxaLeft1;
    protected byte field_59_jc;
    protected BorderCode field_60_brcTop;
    protected BorderCode field_61_brcLeft;
    protected BorderCode field_62_brcBottom;
    protected BorderCode field_63_brcRight;
    protected BorderCode field_64_brcBetween;
    protected BorderCode field_65_brcBar;
    protected ShadingDescriptor field_66_shd;
    protected byte[] field_67_anld;
    protected byte[] field_68_phe;
    protected boolean field_69_fPropRMark;
    protected int field_70_ibstPropRMark;
    protected DateAndTime field_71_dttmPropRMark;
    protected int field_72_itbdMac;
    protected int[] field_73_rgdxaTab;
    protected TabDescriptor[] field_74_rgtbd;
    protected byte[] field_75_numrm;
    protected byte[] field_76_ptap;
    protected boolean field_77_fNoAllowOverlap;
    protected long field_78_ipgp;
    protected long field_79_rsid;

    protected PAPAbstractType() {
        field_11_lspd = new LineSpacingDescriptor();
        field_11_lspd = new LineSpacingDescriptor();
        field_28_dcs = new DropCapSpecifier();
        field_32_fWidowControl = true;
        field_41_lvl = 9;
        field_60_brcTop = new BorderCode();
        field_61_brcLeft = new BorderCode();
        field_62_brcBottom = new BorderCode();
        field_63_brcRight = new BorderCode();
        field_64_brcBetween = new BorderCode();
        field_65_brcBar = new BorderCode();
        field_66_shd = new ShadingDescriptor();
        field_67_anld = new byte[0];
        field_68_phe = new byte[0];
        field_71_dttmPropRMark = new DateAndTime();
        field_73_rgdxaTab = new int[0];
        field_74_rgtbd = new TabDescriptor[0];
        field_75_numrm = new byte[0];
        field_76_ptap = new byte[0];
    }

    protected PAPAbstractType(PAPAbstractType other) {
        field_1_istd = other.field_1_istd;
        field_2_fSideBySide = other.field_2_fSideBySide;
        field_3_fKeep = other.field_3_fKeep;
        field_4_fKeepFollow = other.field_4_fKeepFollow;
        field_5_fPageBreakBefore = other.field_5_fPageBreakBefore;
        field_6_brcl = other.field_6_brcl;
        field_7_brcp = other.field_7_brcp;
        field_8_ilvl = other.field_8_ilvl;
        field_9_ilfo = other.field_9_ilfo;
        field_10_fNoLnn = other.field_10_fNoLnn;
        field_11_lspd = (other.field_11_lspd == null) ? null : other.field_11_lspd.copy();
        field_12_dyaBefore = other.field_12_dyaBefore;
        field_13_dyaAfter = other.field_13_dyaAfter;
        field_14_fInTable = other.field_14_fInTable;
        field_15_finTableW97 = other.field_15_finTableW97;
        field_16_fTtp = other.field_16_fTtp;
        field_17_dxaAbs = other.field_17_dxaAbs;
        field_18_dyaAbs = other.field_18_dyaAbs;
        field_19_dxaWidth = other.field_19_dxaWidth;
        field_20_fBrLnAbove = other.field_20_fBrLnAbove;
        field_21_fBrLnBelow = other.field_21_fBrLnBelow;
        field_22_pcVert = other.field_22_pcVert;
        field_23_pcHorz = other.field_23_pcHorz;
        field_24_wr = other.field_24_wr;
        field_25_fNoAutoHyph = other.field_25_fNoAutoHyph;
        field_26_dyaHeight = other.field_26_dyaHeight;
        field_27_fMinHeight = other.field_27_fMinHeight;
        field_28_dcs = (other.field_28_dcs == null) ? null : other.field_28_dcs.copy();
        field_29_dyaFromText = other.field_29_dyaFromText;
        field_30_dxaFromText = other.field_30_dxaFromText;
        field_31_fLocked = other.field_31_fLocked;
        field_32_fWidowControl = other.field_32_fWidowControl;
        field_33_fKinsoku = other.field_33_fKinsoku;
        field_34_fWordWrap = other.field_34_fWordWrap;
        field_35_fOverflowPunct = other.field_35_fOverflowPunct;
        field_36_fTopLinePunct = other.field_36_fTopLinePunct;
        field_37_fAutoSpaceDE = other.field_37_fAutoSpaceDE;
        field_38_fAutoSpaceDN = other.field_38_fAutoSpaceDN;
        field_39_wAlignFont = other.field_39_wAlignFont;
        field_40_fontAlign = other.field_40_fontAlign;
        field_41_lvl = other.field_41_lvl;
        field_42_fBiDi = other.field_42_fBiDi;
        field_43_fNumRMIns = other.field_43_fNumRMIns;
        field_44_fCrLf = other.field_44_fCrLf;
        field_45_fUsePgsuSettings = other.field_45_fUsePgsuSettings;
        field_46_fAdjustRight = other.field_46_fAdjustRight;
        field_47_itap = other.field_47_itap;
        field_48_fInnerTableCell = other.field_48_fInnerTableCell;
        field_49_fOpenTch = other.field_49_fOpenTch;
        field_50_fTtpEmbedded = other.field_50_fTtpEmbedded;
        field_51_dxcRight = other.field_51_dxcRight;
        field_52_dxcLeft = other.field_52_dxcLeft;
        field_53_dxcLeft1 = other.field_53_dxcLeft1;
        field_54_fDyaBeforeAuto = other.field_54_fDyaBeforeAuto;
        field_55_fDyaAfterAuto = other.field_55_fDyaAfterAuto;
        field_56_dxaRight = other.field_56_dxaRight;
        field_57_dxaLeft = other.field_57_dxaLeft;
        field_58_dxaLeft1 = other.field_58_dxaLeft1;
        field_59_jc = other.field_59_jc;
        field_60_brcTop = (other.field_60_brcTop == null) ? null : other.field_60_brcTop.copy();
        field_61_brcLeft = (other.field_61_brcLeft == null) ? null : other.field_61_brcLeft.copy();
        field_62_brcBottom = (other.field_62_brcBottom == null) ? null : other.field_62_brcBottom.copy();
        field_63_brcRight = (other.field_63_brcRight == null) ? null : other.field_63_brcRight.copy();
        field_64_brcBetween = (other.field_64_brcBetween == null) ? null : other.field_64_brcBetween.copy();
        field_65_brcBar = (other.field_65_brcBar == null) ? null : other.field_65_brcBar.copy();
        field_66_shd = (other.field_66_shd == null) ? null : other.field_66_shd.copy();
        field_67_anld = (other.field_67_anld == null) ? null : other.field_67_anld.clone();
        field_68_phe = (other.field_68_phe == null) ? null : other.field_68_phe.clone();
        field_69_fPropRMark = other.field_69_fPropRMark;
        field_70_ibstPropRMark = other.field_70_ibstPropRMark;
        field_71_dttmPropRMark = (other.field_71_dttmPropRMark == null) ? null : other.field_71_dttmPropRMark.copy();
        field_72_itbdMac = other.field_72_itbdMac;
        field_73_rgdxaTab = (other.field_73_rgdxaTab == null) ? null : other.field_73_rgdxaTab.clone();
        field_74_rgtbd = (other.field_74_rgtbd == null) ? null
            : Stream.of(other.field_74_rgtbd).map(TabDescriptor::copy).toArray(TabDescriptor[]::new);
        field_75_numrm = (other.field_75_numrm == null) ? null : other.field_75_numrm.clone();
        field_76_ptap = (other.field_76_ptap == null) ? null : other.field_76_ptap.clone();
        field_77_fNoAllowOverlap = other.field_77_fNoAllowOverlap;
        field_78_ipgp = other.field_78_ipgp;
        field_79_rsid = other.field_79_rsid;

    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[PAP]\n");
        builder.append("    .istd                 = ");
        builder.append(" (").append(getIstd()).append(" )\n");
        builder.append("    .fSideBySide          = ");
        builder.append(" (").append(getFSideBySide()).append(" )\n");
        builder.append("    .fKeep                = ");
        builder.append(" (").append(getFKeep()).append(" )\n");
        builder.append("    .fKeepFollow          = ");
        builder.append(" (").append(getFKeepFollow()).append(" )\n");
        builder.append("    .fPageBreakBefore     = ");
        builder.append(" (").append(getFPageBreakBefore()).append(" )\n");
        builder.append("    .brcl                 = ");
        builder.append(" (").append(getBrcl()).append(" )\n");
        builder.append("    .brcp                 = ");
        builder.append(" (").append(getBrcp()).append(" )\n");
        builder.append("    .ilvl                 = ");
        builder.append(" (").append(getIlvl()).append(" )\n");
        builder.append("    .ilfo                 = ");
        builder.append(" (").append(getIlfo()).append(" )\n");
        builder.append("    .fNoLnn               = ");
        builder.append(" (").append(getFNoLnn()).append(" )\n");
        builder.append("    .lspd                 = ");
        builder.append(" (").append(getLspd()).append(" )\n");
        builder.append("    .dyaBefore            = ");
        builder.append(" (").append(getDyaBefore()).append(" )\n");
        builder.append("    .dyaAfter             = ");
        builder.append(" (").append(getDyaAfter()).append(" )\n");
        builder.append("    .fInTable             = ");
        builder.append(" (").append(getFInTable()).append(" )\n");
        builder.append("    .finTableW97          = ");
        builder.append(" (").append(getFinTableW97()).append(" )\n");
        builder.append("    .fTtp                 = ");
        builder.append(" (").append(getFTtp()).append(" )\n");
        builder.append("    .dxaAbs               = ");
        builder.append(" (").append(getDxaAbs()).append(" )\n");
        builder.append("    .dyaAbs               = ");
        builder.append(" (").append(getDyaAbs()).append(" )\n");
        builder.append("    .dxaWidth             = ");
        builder.append(" (").append(getDxaWidth()).append(" )\n");
        builder.append("    .fBrLnAbove           = ");
        builder.append(" (").append(getFBrLnAbove()).append(" )\n");
        builder.append("    .fBrLnBelow           = ");
        builder.append(" (").append(getFBrLnBelow()).append(" )\n");
        builder.append("    .pcVert               = ");
        builder.append(" (").append(getPcVert()).append(" )\n");
        builder.append("    .pcHorz               = ");
        builder.append(" (").append(getPcHorz()).append(" )\n");
        builder.append("    .wr                   = ");
        builder.append(" (").append(getWr()).append(" )\n");
        builder.append("    .fNoAutoHyph          = ");
        builder.append(" (").append(getFNoAutoHyph()).append(" )\n");
        builder.append("    .dyaHeight            = ");
        builder.append(" (").append(getDyaHeight()).append(" )\n");
        builder.append("    .fMinHeight           = ");
        builder.append(" (").append(getFMinHeight()).append(" )\n");
        builder.append("    .dcs                  = ");
        builder.append(" (").append(getDcs()).append(" )\n");
        builder.append("    .dyaFromText          = ");
        builder.append(" (").append(getDyaFromText()).append(" )\n");
        builder.append("    .dxaFromText          = ");
        builder.append(" (").append(getDxaFromText()).append(" )\n");
        builder.append("    .fLocked              = ");
        builder.append(" (").append(getFLocked()).append(" )\n");
        builder.append("    .fWidowControl        = ");
        builder.append(" (").append(getFWidowControl()).append(" )\n");
        builder.append("    .fKinsoku             = ");
        builder.append(" (").append(getFKinsoku()).append(" )\n");
        builder.append("    .fWordWrap            = ");
        builder.append(" (").append(getFWordWrap()).append(" )\n");
        builder.append("    .fOverflowPunct       = ");
        builder.append(" (").append(getFOverflowPunct()).append(" )\n");
        builder.append("    .fTopLinePunct        = ");
        builder.append(" (").append(getFTopLinePunct()).append(" )\n");
        builder.append("    .fAutoSpaceDE         = ");
        builder.append(" (").append(getFAutoSpaceDE()).append(" )\n");
        builder.append("    .fAutoSpaceDN         = ");
        builder.append(" (").append(getFAutoSpaceDN()).append(" )\n");
        builder.append("    .wAlignFont           = ");
        builder.append(" (").append(getWAlignFont()).append(" )\n");
        builder.append("    .fontAlign            = ");
        builder.append(" (").append(getFontAlign()).append(" )\n");
        builder.append("         .fVertical                = ").append(isFVertical()).append('\n');
        builder.append("         .fBackward                = ").append(isFBackward()).append('\n');
        builder.append("         .fRotateFont              = ").append(isFRotateFont()).append('\n');
        builder.append("    .lvl                  = ");
        builder.append(" (").append(getLvl()).append(" )\n");
        builder.append("    .fBiDi                = ");
        builder.append(" (").append(getFBiDi()).append(" )\n");
        builder.append("    .fNumRMIns            = ");
        builder.append(" (").append(getFNumRMIns()).append(" )\n");
        builder.append("    .fCrLf                = ");
        builder.append(" (").append(getFCrLf()).append(" )\n");
        builder.append("    .fUsePgsuSettings     = ");
        builder.append(" (").append(getFUsePgsuSettings()).append(" )\n");
        builder.append("    .fAdjustRight         = ");
        builder.append(" (").append(getFAdjustRight()).append(" )\n");
        builder.append("    .itap                 = ");
        builder.append(" (").append(getItap()).append(" )\n");
        builder.append("    .fInnerTableCell      = ");
        builder.append(" (").append(getFInnerTableCell()).append(" )\n");
        builder.append("    .fOpenTch             = ");
        builder.append(" (").append(getFOpenTch()).append(" )\n");
        builder.append("    .fTtpEmbedded         = ");
        builder.append(" (").append(getFTtpEmbedded()).append(" )\n");
        builder.append("    .dxcRight             = ");
        builder.append(" (").append(getDxcRight()).append(" )\n");
        builder.append("    .dxcLeft              = ");
        builder.append(" (").append(getDxcLeft()).append(" )\n");
        builder.append("    .dxcLeft1             = ");
        builder.append(" (").append(getDxcLeft1()).append(" )\n");
        builder.append("    .fDyaBeforeAuto       = ");
        builder.append(" (").append(getFDyaBeforeAuto()).append(" )\n");
        builder.append("    .fDyaAfterAuto        = ");
        builder.append(" (").append(getFDyaAfterAuto()).append(" )\n");
        builder.append("    .dxaRight             = ");
        builder.append(" (").append(getDxaRight()).append(" )\n");
        builder.append("    .dxaLeft              = ");
        builder.append(" (").append(getDxaLeft()).append(" )\n");
        builder.append("    .dxaLeft1             = ");
        builder.append(" (").append(getDxaLeft1()).append(" )\n");
        builder.append("    .jc                   = ");
        builder.append(" (").append(getJc()).append(" )\n");
        builder.append("    .brcTop               = ");
        builder.append(" (").append(getBrcTop()).append(" )\n");
        builder.append("    .brcLeft              = ");
        builder.append(" (").append(getBrcLeft()).append(" )\n");
        builder.append("    .brcBottom            = ");
        builder.append(" (").append(getBrcBottom()).append(" )\n");
        builder.append("    .brcRight             = ");
        builder.append(" (").append(getBrcRight()).append(" )\n");
        builder.append("    .brcBetween           = ");
        builder.append(" (").append(getBrcBetween()).append(" )\n");
        builder.append("    .brcBar               = ");
        builder.append(" (").append(getBrcBar()).append(" )\n");
        builder.append("    .shd                  = ");
        builder.append(" (").append(getShd()).append(" )\n");
        builder.append("    .anld                 = ");
        builder.append(" (").append(Arrays.toString(getAnld())).append(" )\n");
        builder.append("    .phe                  = ");
        builder.append(" (").append(Arrays.toString(getPhe())).append(" )\n");
        builder.append("    .fPropRMark           = ");
        builder.append(" (").append(getFPropRMark()).append(" )\n");
        builder.append("    .ibstPropRMark        = ");
        builder.append(" (").append(getIbstPropRMark()).append(" )\n");
        builder.append("    .dttmPropRMark        = ");
        builder.append(" (").append(getDttmPropRMark()).append(" )\n");
        builder.append("    .itbdMac              = ");
        builder.append(" (").append(getItbdMac()).append(" )\n");
        builder.append("    .rgdxaTab             = ");
        builder.append(" (").append(Arrays.toString(getRgdxaTab())).append(" )\n");
        builder.append("    .rgtbd                = ");
        builder.append(" (").append(Arrays.toString(getRgtbd())).append(" )\n");
        builder.append("    .numrm                = ");
        builder.append(" (").append(Arrays.toString(getNumrm())).append(" )\n");
        builder.append("    .ptap                 = ");
        builder.append(" (").append(Arrays.toString(getPtap())).append(" )\n");
        builder.append("    .fNoAllowOverlap      = ");
        builder.append(" (").append(getFNoAllowOverlap()).append(" )\n");
        builder.append("    .ipgp                 = ");
        builder.append(" (").append(getIpgp()).append(" )\n");
        builder.append("    .rsid                 = ");
        builder.append(" (").append(getRsid()).append(" )\n");

        builder.append("[/PAP]\n");
        return builder.toString();
    }

    /**
     * Index to style descriptor.
     */
    @Internal
    public int getIstd()
    {
        return field_1_istd;
    }

    /**
     * Index to style descriptor.
     */
    @Internal
    public void setIstd( int field_1_istd )
    {
        this.field_1_istd = field_1_istd;
    }

    /**
     * Get the fSideBySide field for the PAP record.
     */
    @Internal
    public boolean getFSideBySide()
    {
        return field_2_fSideBySide;
    }

    /**
     * Set the fSideBySide field for the PAP record.
     */
    @Internal
    public void setFSideBySide( boolean field_2_fSideBySide )
    {
        this.field_2_fSideBySide = field_2_fSideBySide;
    }

    /**
     * Get the fKeep field for the PAP record.
     */
    @Internal
    public boolean getFKeep()
    {
        return field_3_fKeep;
    }

    /**
     * Set the fKeep field for the PAP record.
     */
    @Internal
    public void setFKeep( boolean field_3_fKeep )
    {
        this.field_3_fKeep = field_3_fKeep;
    }

    /**
     * Get the fKeepFollow field for the PAP record.
     */
    @Internal
    public boolean getFKeepFollow()
    {
        return field_4_fKeepFollow;
    }

    /**
     * Set the fKeepFollow field for the PAP record.
     */
    @Internal
    public void setFKeepFollow( boolean field_4_fKeepFollow )
    {
        this.field_4_fKeepFollow = field_4_fKeepFollow;
    }

    /**
     * Get the fPageBreakBefore field for the PAP record.
     */
    @Internal
    public boolean getFPageBreakBefore()
    {
        return field_5_fPageBreakBefore;
    }

    /**
     * Set the fPageBreakBefore field for the PAP record.
     */
    @Internal
    public void setFPageBreakBefore( boolean field_5_fPageBreakBefore )
    {
        this.field_5_fPageBreakBefore = field_5_fPageBreakBefore;
    }

    /**
     * Border line style.
     *
     * @return One of
     * <li>{@link #BRCL_SINGLE}
     * <li>{@link #BRCL_THICK}
     * <li>{@link #BRCL_DOUBLE}
     * <li>{@link #BRCL_SHADOW}
     */
    @Internal
    public byte getBrcl()
    {
        return field_6_brcl;
    }

    /**
     * Border line style.
     *
     * @param field_6_brcl
     *        One of
     * <li>{@link #BRCL_SINGLE}
     * <li>{@link #BRCL_THICK}
     * <li>{@link #BRCL_DOUBLE}
     * <li>{@link #BRCL_SHADOW}
     */
    @Internal
    public void setBrcl( byte field_6_brcl )
    {
        this.field_6_brcl = field_6_brcl;
    }

    /**
     * Rectangle border codes.
     *
     * @return One of
     * <li>{@link #BRCP_NONE}
     * <li>{@link #BRCP_BORDER_ABOVE}
     * <li>{@link #BRCP_BORDER_BELOW}
     * <li>{@link #BRCP_BOX_AROUND}
     * <li>{@link #BRCP_BAR_TO_LEFT_OF_PARAGRAPH}
     */
    @Internal
    public byte getBrcp()
    {
        return field_7_brcp;
    }

    /**
     * Rectangle border codes.
     *
     * @param field_7_brcp
     *        One of
     * <li>{@link #BRCP_NONE}
     * <li>{@link #BRCP_BORDER_ABOVE}
     * <li>{@link #BRCP_BORDER_BELOW}
     * <li>{@link #BRCP_BOX_AROUND}
     * <li>{@link #BRCP_BAR_TO_LEFT_OF_PARAGRAPH}
     */
    @Internal
    public void setBrcp( byte field_7_brcp )
    {
        this.field_7_brcp = field_7_brcp;
    }

    /**
     * List level if non-zero.
     */
    @Internal
    public byte getIlvl()
    {
        return field_8_ilvl;
    }

    /**
     * List level if non-zero.
     */
    @Internal
    public void setIlvl( byte field_8_ilvl )
    {
        this.field_8_ilvl = field_8_ilvl;
    }

    /**
     * "A 16-bit signed integer value that is used to determine which list
     * contains the paragraph. This value MUST be one of the following:
     *
     * 0x0000 -- This paragraph is not in a list, and any list formatting on the
     * paragraph is removed.
     *
     * 0x0001 - 0x07FE -- The value is a 1-based index into PlfLfo.rgLfo. The
     * LFO at this index defines the list that this paragraph is in.
     *
     * 0xF801 -- This paragraph is not in a list.
     *
     * 0xF802 - 0xFFFF -- The value is the negation of a 1-based index into
     * PlfLfo.rgLfo. The LFO at this index defines the list that this paragraph
     * is in. The logical left indentation (see sprmPDxaLeft) and the logical
     * left first line indentation (see sprmPDxaLeft1) of the paragraph MUST be
     * preserved despite any list formatting.
     *
     * By default, a paragraph is not in a list."
     *
     * Quote from [MS-DOC] -- v20110315, page 125
     */
    @Internal
    public int getIlfo()
    {
        return field_9_ilfo;
    }

    /**
     * "A 16-bit signed integer value that is used to determine which list
     * contains the paragraph. This value MUST be one of the following:
     *
     * 0x0000 -- This paragraph is not in a list, and any list formatting on the
     * paragraph is removed.
     *
     * 0x0001 - 0x07FE -- The value is a 1-based index into PlfLfo.rgLfo. The
     * LFO at this index defines the list that this paragraph is in.
     *
     * 0xF801 -- This paragraph is not in a list.
     *
     * 0xF802 - 0xFFFF -- The value is the negation of a 1-based index into
     * PlfLfo.rgLfo. The LFO at this index defines the list that this paragraph
     * is in. The logical left indentation (see sprmPDxaLeft) and the logical
     * left first line indentation (see sprmPDxaLeft1) of the paragraph MUST be
     * preserved despite any list formatting. By default, a paragraph is not in
     * a list."
     *
     * Quote from [MS-DOC] -- v20110315, page 125
     */
    @Internal
    public void setIlfo( int field_9_ilfo )
    {
        this.field_9_ilfo = field_9_ilfo;
    }

    /**
     * No line numbering.
     */
    @Internal
    public boolean getFNoLnn()
    {
        return field_10_fNoLnn;
    }

    /**
     * No line numbering.
     */
    @Internal
    public void setFNoLnn( boolean field_10_fNoLnn )
    {
        this.field_10_fNoLnn = field_10_fNoLnn;
    }

    /**
     * Line spacing descriptor.
     */
    @Internal
    public LineSpacingDescriptor getLspd()
    {
        return field_11_lspd;
    }

    /**
     * Line spacing descriptor.
     */
    @Internal
    public void setLspd( LineSpacingDescriptor field_11_lspd )
    {
        this.field_11_lspd = field_11_lspd;
    }

    /**
     * Space before paragraph.
     */
    @Internal
    public int getDyaBefore()
    {
        return field_12_dyaBefore;
    }

    /**
     * Space before paragraph.
     */
    @Internal
    public void setDyaBefore( int field_12_dyaBefore )
    {
        this.field_12_dyaBefore = field_12_dyaBefore;
    }

    /**
     * Space after paragraph.
     */
    @Internal
    public int getDyaAfter()
    {
        return field_13_dyaAfter;
    }

    /**
     * Space after paragraph.
     */
    @Internal
    public void setDyaAfter( int field_13_dyaAfter )
    {
        this.field_13_dyaAfter = field_13_dyaAfter;
    }

    /**
     * Paragraph is in table flag.
     */
    @Internal
    public boolean getFInTable()
    {
        return field_14_fInTable;
    }

    /**
     * Paragraph is in table flag.
     */
    @Internal
    public void setFInTable( boolean field_14_fInTable )
    {
        this.field_14_fInTable = field_14_fInTable;
    }

    /**
     * Archaic paragraph is in table flag.
     */
    @Internal
    public boolean getFinTableW97()
    {
        return field_15_finTableW97;
    }

    /**
     * Archaic paragraph is in table flag.
     */
    @Internal
    public void setFinTableW97( boolean field_15_finTableW97 )
    {
        this.field_15_finTableW97 = field_15_finTableW97;
    }

    /**
     * Table trailer paragraph (last in table row).
     */
    @Internal
    public boolean getFTtp()
    {
        return field_16_fTtp;
    }

    /**
     * Table trailer paragraph (last in table row).
     */
    @Internal
    public void setFTtp( boolean field_16_fTtp )
    {
        this.field_16_fTtp = field_16_fTtp;
    }

    /**
     * Get the dxaAbs field for the PAP record.
     */
    @Internal
    public int getDxaAbs()
    {
        return field_17_dxaAbs;
    }

    /**
     * Set the dxaAbs field for the PAP record.
     */
    @Internal
    public void setDxaAbs( int field_17_dxaAbs )
    {
        this.field_17_dxaAbs = field_17_dxaAbs;
    }

    /**
     * Get the dyaAbs field for the PAP record.
     */
    @Internal
    public int getDyaAbs()
    {
        return field_18_dyaAbs;
    }

    /**
     * Set the dyaAbs field for the PAP record.
     */
    @Internal
    public void setDyaAbs( int field_18_dyaAbs )
    {
        this.field_18_dyaAbs = field_18_dyaAbs;
    }

    /**
     * Get the dxaWidth field for the PAP record.
     */
    @Internal
    public int getDxaWidth()
    {
        return field_19_dxaWidth;
    }

    /**
     * Set the dxaWidth field for the PAP record.
     */
    @Internal
    public void setDxaWidth( int field_19_dxaWidth )
    {
        this.field_19_dxaWidth = field_19_dxaWidth;
    }

    /**
     * Get the fBrLnAbove field for the PAP record.
     */
    @Internal
    public boolean getFBrLnAbove()
    {
        return field_20_fBrLnAbove;
    }

    /**
     * Set the fBrLnAbove field for the PAP record.
     */
    @Internal
    public void setFBrLnAbove( boolean field_20_fBrLnAbove )
    {
        this.field_20_fBrLnAbove = field_20_fBrLnAbove;
    }

    /**
     * Get the fBrLnBelow field for the PAP record.
     */
    @Internal
    public boolean getFBrLnBelow()
    {
        return field_21_fBrLnBelow;
    }

    /**
     * Set the fBrLnBelow field for the PAP record.
     */
    @Internal
    public void setFBrLnBelow( boolean field_21_fBrLnBelow )
    {
        this.field_21_fBrLnBelow = field_21_fBrLnBelow;
    }

    /**
     * Get the pcVert field for the PAP record.
     */
    @Internal
    public byte getPcVert()
    {
        return field_22_pcVert;
    }

    /**
     * Set the pcVert field for the PAP record.
     */
    @Internal
    public void setPcVert( byte field_22_pcVert )
    {
        this.field_22_pcVert = field_22_pcVert;
    }

    /**
     * Get the pcHorz field for the PAP record.
     */
    @Internal
    public byte getPcHorz()
    {
        return field_23_pcHorz;
    }

    /**
     * Set the pcHorz field for the PAP record.
     */
    @Internal
    public void setPcHorz( byte field_23_pcHorz )
    {
        this.field_23_pcHorz = field_23_pcHorz;
    }

    /**
     * Get the wr field for the PAP record.
     */
    @Internal
    public byte getWr()
    {
        return field_24_wr;
    }

    /**
     * Set the wr field for the PAP record.
     */
    @Internal
    public void setWr( byte field_24_wr )
    {
        this.field_24_wr = field_24_wr;
    }

    /**
     * Get the fNoAutoHyph field for the PAP record.
     */
    @Internal
    public boolean getFNoAutoHyph()
    {
        return field_25_fNoAutoHyph;
    }

    /**
     * Set the fNoAutoHyph field for the PAP record.
     */
    @Internal
    public void setFNoAutoHyph( boolean field_25_fNoAutoHyph )
    {
        this.field_25_fNoAutoHyph = field_25_fNoAutoHyph;
    }

    /**
     * Get the dyaHeight field for the PAP record.
     */
    @Internal
    public int getDyaHeight()
    {
        return field_26_dyaHeight;
    }

    /**
     * Set the dyaHeight field for the PAP record.
     */
    @Internal
    public void setDyaHeight( int field_26_dyaHeight )
    {
        this.field_26_dyaHeight = field_26_dyaHeight;
    }

    /**
     * Minimum height is exact or auto.
     *
     * @return One of
     * <li>{@link #FMINHEIGHT_EXACT}
     * <li>{@link #FMINHEIGHT_AT_LEAST}
     */
    @Internal
    public boolean getFMinHeight()
    {
        return field_27_fMinHeight;
    }

    /**
     * Minimum height is exact or auto.
     *
     * @param field_27_fMinHeight
     *        One of
     * <li>{@link #FMINHEIGHT_EXACT}
     * <li>{@link #FMINHEIGHT_AT_LEAST}
     */
    @Internal
    public void setFMinHeight( boolean field_27_fMinHeight )
    {
        this.field_27_fMinHeight = field_27_fMinHeight;
    }

    /**
     * Get the dcs field for the PAP record.
     */
    @Internal
    public DropCapSpecifier getDcs()
    {
        return field_28_dcs;
    }

    /**
     * Set the dcs field for the PAP record.
     */
    @Internal
    public void setDcs( DropCapSpecifier field_28_dcs )
    {
        this.field_28_dcs = field_28_dcs;
    }

    /**
     * Vertical distance between text and absolutely positioned object.
     */
    @Internal
    public int getDyaFromText()
    {
        return field_29_dyaFromText;
    }

    /**
     * Vertical distance between text and absolutely positioned object.
     */
    @Internal
    public void setDyaFromText( int field_29_dyaFromText )
    {
        this.field_29_dyaFromText = field_29_dyaFromText;
    }

    /**
     * Horizontal distance between text and absolutely positioned object.
     */
    @Internal
    public int getDxaFromText()
    {
        return field_30_dxaFromText;
    }

    /**
     * Horizontal distance between text and absolutely positioned object.
     */
    @Internal
    public void setDxaFromText( int field_30_dxaFromText )
    {
        this.field_30_dxaFromText = field_30_dxaFromText;
    }

    /**
     * Anchor of an absolutely positioned frame is locked.
     */
    @Internal
    public boolean getFLocked()
    {
        return field_31_fLocked;
    }

    /**
     * Anchor of an absolutely positioned frame is locked.
     */
    @Internal
    public void setFLocked( boolean field_31_fLocked )
    {
        this.field_31_fLocked = field_31_fLocked;
    }

    /**
     * 1, Word will prevent widowed lines in this paragraph from being placed at the beginning of a page.
     */
    @Internal
    public boolean getFWidowControl()
    {
        return field_32_fWidowControl;
    }

    /**
     * 1, Word will prevent widowed lines in this paragraph from being placed at the beginning of a page.
     */
    @Internal
    public void setFWidowControl( boolean field_32_fWidowControl )
    {
        this.field_32_fWidowControl = field_32_fWidowControl;
    }

    /**
     * apply Kinsoku rules when performing line wrapping.
     */
    @Internal
    public boolean getFKinsoku()
    {
        return field_33_fKinsoku;
    }

    /**
     * apply Kinsoku rules when performing line wrapping.
     */
    @Internal
    public void setFKinsoku( boolean field_33_fKinsoku )
    {
        this.field_33_fKinsoku = field_33_fKinsoku;
    }

    /**
     * perform word wrap.
     */
    @Internal
    public boolean getFWordWrap()
    {
        return field_34_fWordWrap;
    }

    /**
     * perform word wrap.
     */
    @Internal
    public void setFWordWrap( boolean field_34_fWordWrap )
    {
        this.field_34_fWordWrap = field_34_fWordWrap;
    }

    /**
     * apply overflow punctuation rules when performing line wrapping.
     */
    @Internal
    public boolean getFOverflowPunct()
    {
        return field_35_fOverflowPunct;
    }

    /**
     * apply overflow punctuation rules when performing line wrapping.
     */
    @Internal
    public void setFOverflowPunct( boolean field_35_fOverflowPunct )
    {
        this.field_35_fOverflowPunct = field_35_fOverflowPunct;
    }

    /**
     * perform top line punctuation processing.
     */
    @Internal
    public boolean getFTopLinePunct()
    {
        return field_36_fTopLinePunct;
    }

    /**
     * perform top line punctuation processing.
     */
    @Internal
    public void setFTopLinePunct( boolean field_36_fTopLinePunct )
    {
        this.field_36_fTopLinePunct = field_36_fTopLinePunct;
    }

    /**
     * auto space East Asian and alphabetic characters.
     */
    @Internal
    public boolean getFAutoSpaceDE()
    {
        return field_37_fAutoSpaceDE;
    }

    /**
     * auto space East Asian and alphabetic characters.
     */
    @Internal
    public void setFAutoSpaceDE( boolean field_37_fAutoSpaceDE )
    {
        this.field_37_fAutoSpaceDE = field_37_fAutoSpaceDE;
    }

    /**
     * auto space East Asian and numeric characters.
     */
    @Internal
    public boolean getFAutoSpaceDN()
    {
        return field_38_fAutoSpaceDN;
    }

    /**
     * auto space East Asian and numeric characters.
     */
    @Internal
    public void setFAutoSpaceDN( boolean field_38_fAutoSpaceDN )
    {
        this.field_38_fAutoSpaceDN = field_38_fAutoSpaceDN;
    }

    /**
     * Get the wAlignFont field for the PAP record.
     *
     * @return One of
     * <li>{@link #WALIGNFONT_HANGING}
     * <li>{@link #WALIGNFONT_CENTERED}
     * <li>{@link #WALIGNFONT_ROMAN}
     * <li>{@link #WALIGNFONT_VARIABLE}
     * <li>{@link #WALIGNFONT_AUTO}
     */
    @Internal
    public int getWAlignFont()
    {
        return field_39_wAlignFont;
    }

    /**
     * Set the wAlignFont field for the PAP record.
     *
     * @param field_39_wAlignFont
     *        One of
     * <li>{@link #WALIGNFONT_HANGING}
     * <li>{@link #WALIGNFONT_CENTERED}
     * <li>{@link #WALIGNFONT_ROMAN}
     * <li>{@link #WALIGNFONT_VARIABLE}
     * <li>{@link #WALIGNFONT_AUTO}
     */
    @Internal
    public void setWAlignFont( int field_39_wAlignFont )
    {
        this.field_39_wAlignFont = field_39_wAlignFont;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public short getFontAlign()
    {
        return field_40_fontAlign;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public void setFontAlign( short field_40_fontAlign )
    {
        this.field_40_fontAlign = field_40_fontAlign;
    }

    /**
     * Outline level.
     */
    @Internal
    public byte getLvl()
    {
        return field_41_lvl;
    }

    /**
     * Outline level.
     */
    @Internal
    public void setLvl( byte field_41_lvl )
    {
        this.field_41_lvl = field_41_lvl;
    }

    /**
     * Get the fBiDi field for the PAP record.
     */
    @Internal
    public boolean getFBiDi()
    {
        return field_42_fBiDi;
    }

    /**
     * Set the fBiDi field for the PAP record.
     */
    @Internal
    public void setFBiDi( boolean field_42_fBiDi )
    {
        this.field_42_fBiDi = field_42_fBiDi;
    }

    /**
     * Get the fNumRMIns field for the PAP record.
     */
    @Internal
    public boolean getFNumRMIns()
    {
        return field_43_fNumRMIns;
    }

    /**
     * Set the fNumRMIns field for the PAP record.
     */
    @Internal
    public void setFNumRMIns( boolean field_43_fNumRMIns )
    {
        this.field_43_fNumRMIns = field_43_fNumRMIns;
    }

    /**
     * Get the fCrLf field for the PAP record.
     */
    @Internal
    public boolean getFCrLf()
    {
        return field_44_fCrLf;
    }

    /**
     * Set the fCrLf field for the PAP record.
     */
    @Internal
    public void setFCrLf( boolean field_44_fCrLf )
    {
        this.field_44_fCrLf = field_44_fCrLf;
    }

    /**
     * Get the fUsePgsuSettings field for the PAP record.
     */
    @Internal
    public boolean getFUsePgsuSettings()
    {
        return field_45_fUsePgsuSettings;
    }

    /**
     * Set the fUsePgsuSettings field for the PAP record.
     */
    @Internal
    public void setFUsePgsuSettings( boolean field_45_fUsePgsuSettings )
    {
        this.field_45_fUsePgsuSettings = field_45_fUsePgsuSettings;
    }

    /**
     * Get the fAdjustRight field for the PAP record.
     */
    @Internal
    public boolean getFAdjustRight()
    {
        return field_46_fAdjustRight;
    }

    /**
     * Set the fAdjustRight field for the PAP record.
     */
    @Internal
    public void setFAdjustRight( boolean field_46_fAdjustRight )
    {
        this.field_46_fAdjustRight = field_46_fAdjustRight;
    }

    /**
     * Table nesting level.
     */
    @Internal
    public int getItap()
    {
        return field_47_itap;
    }

    /**
     * Table nesting level.
     */
    @Internal
    public void setItap( int field_47_itap )
    {
        this.field_47_itap = field_47_itap;
    }

    /**
     * When 1, the end of paragraph mark is really an end of cell mark for a nested table cell.
     */
    @Internal
    public boolean getFInnerTableCell()
    {
        return field_48_fInnerTableCell;
    }

    /**
     * When 1, the end of paragraph mark is really an end of cell mark for a nested table cell.
     */
    @Internal
    public void setFInnerTableCell( boolean field_48_fInnerTableCell )
    {
        this.field_48_fInnerTableCell = field_48_fInnerTableCell;
    }

    /**
     * Ensure the Table Cell char doesn't show up as zero height.
     */
    @Internal
    public boolean getFOpenTch()
    {
        return field_49_fOpenTch;
    }

    /**
     * Ensure the Table Cell char doesn't show up as zero height.
     */
    @Internal
    public void setFOpenTch( boolean field_49_fOpenTch )
    {
        this.field_49_fOpenTch = field_49_fOpenTch;
    }

    /**
     * Word 97 compatibility indicates this end of paragraph mark is really an end of row marker for a nested table.
     */
    @Internal
    public boolean getFTtpEmbedded()
    {
        return field_50_fTtpEmbedded;
    }

    /**
     * Word 97 compatibility indicates this end of paragraph mark is really an end of row marker for a nested table.
     */
    @Internal
    public void setFTtpEmbedded( boolean field_50_fTtpEmbedded )
    {
        this.field_50_fTtpEmbedded = field_50_fTtpEmbedded;
    }

    /**
     * Right indent in character units.
     */
    @Internal
    public short getDxcRight()
    {
        return field_51_dxcRight;
    }

    /**
     * Right indent in character units.
     */
    @Internal
    public void setDxcRight( short field_51_dxcRight )
    {
        this.field_51_dxcRight = field_51_dxcRight;
    }

    /**
     * Left indent in character units.
     */
    @Internal
    public short getDxcLeft()
    {
        return field_52_dxcLeft;
    }

    /**
     * Left indent in character units.
     */
    @Internal
    public void setDxcLeft( short field_52_dxcLeft )
    {
        this.field_52_dxcLeft = field_52_dxcLeft;
    }

    /**
     * First line indent in character units.
     */
    @Internal
    public short getDxcLeft1()
    {
        return field_53_dxcLeft1;
    }

    /**
     * First line indent in character units.
     */
    @Internal
    public void setDxcLeft1( short field_53_dxcLeft1 )
    {
        this.field_53_dxcLeft1 = field_53_dxcLeft1;
    }

    /**
     * Vertical spacing before is automatic.
     */
    @Internal
    public boolean getFDyaBeforeAuto()
    {
        return field_54_fDyaBeforeAuto;
    }

    /**
     * Vertical spacing before is automatic.
     */
    @Internal
    public void setFDyaBeforeAuto( boolean field_54_fDyaBeforeAuto )
    {
        this.field_54_fDyaBeforeAuto = field_54_fDyaBeforeAuto;
    }

    /**
     * Vertical spacing after is automatic.
     */
    @Internal
    public boolean getFDyaAfterAuto()
    {
        return field_55_fDyaAfterAuto;
    }

    /**
     * Vertical spacing after is automatic.
     */
    @Internal
    public void setFDyaAfterAuto( boolean field_55_fDyaAfterAuto )
    {
        this.field_55_fDyaAfterAuto = field_55_fDyaAfterAuto;
    }

    /**
     * Get the dxaRight field for the PAP record.
     */
    @Internal
    public int getDxaRight()
    {
        return field_56_dxaRight;
    }

    /**
     * Set the dxaRight field for the PAP record.
     */
    @Internal
    public void setDxaRight( int field_56_dxaRight )
    {
        this.field_56_dxaRight = field_56_dxaRight;
    }

    /**
     * Get the dxaLeft field for the PAP record.
     */
    @Internal
    public int getDxaLeft()
    {
        return field_57_dxaLeft;
    }

    /**
     * Set the dxaLeft field for the PAP record.
     */
    @Internal
    public void setDxaLeft( int field_57_dxaLeft )
    {
        this.field_57_dxaLeft = field_57_dxaLeft;
    }

    /**
     * Get the dxaLeft1 field for the PAP record.
     */
    @Internal
    public int getDxaLeft1()
    {
        return field_58_dxaLeft1;
    }

    /**
     * Set the dxaLeft1 field for the PAP record.
     */
    @Internal
    public void setDxaLeft1( int field_58_dxaLeft1 )
    {
        this.field_58_dxaLeft1 = field_58_dxaLeft1;
    }

    /**
     * Get the jc field for the PAP record.
     */
    @Internal
    public byte getJc()
    {
        return field_59_jc;
    }

    /**
     * Set the jc field for the PAP record.
     */
    @Internal
    public void setJc( byte field_59_jc )
    {
        this.field_59_jc = field_59_jc;
    }

    /**
     * Get the brcTop field for the PAP record.
     */
    @Internal
    public BorderCode getBrcTop()
    {
        return field_60_brcTop;
    }

    /**
     * Set the brcTop field for the PAP record.
     */
    @Internal
    public void setBrcTop( BorderCode field_60_brcTop )
    {
        this.field_60_brcTop = field_60_brcTop;
    }

    /**
     * Get the brcLeft field for the PAP record.
     */
    @Internal
    public BorderCode getBrcLeft()
    {
        return field_61_brcLeft;
    }

    /**
     * Set the brcLeft field for the PAP record.
     */
    @Internal
    public void setBrcLeft( BorderCode field_61_brcLeft )
    {
        this.field_61_brcLeft = field_61_brcLeft;
    }

    /**
     * Get the brcBottom field for the PAP record.
     */
    @Internal
    public BorderCode getBrcBottom()
    {
        return field_62_brcBottom;
    }

    /**
     * Set the brcBottom field for the PAP record.
     */
    @Internal
    public void setBrcBottom( BorderCode field_62_brcBottom )
    {
        this.field_62_brcBottom = field_62_brcBottom;
    }

    /**
     * Get the brcRight field for the PAP record.
     */
    @Internal
    public BorderCode getBrcRight()
    {
        return field_63_brcRight;
    }

    /**
     * Set the brcRight field for the PAP record.
     */
    @Internal
    public void setBrcRight( BorderCode field_63_brcRight )
    {
        this.field_63_brcRight = field_63_brcRight;
    }

    /**
     * Get the brcBetween field for the PAP record.
     */
    @Internal
    public BorderCode getBrcBetween()
    {
        return field_64_brcBetween;
    }

    /**
     * Set the brcBetween field for the PAP record.
     */
    @Internal
    public void setBrcBetween( BorderCode field_64_brcBetween )
    {
        this.field_64_brcBetween = field_64_brcBetween;
    }

    /**
     * Get the brcBar field for the PAP record.
     */
    @Internal
    public BorderCode getBrcBar()
    {
        return field_65_brcBar;
    }

    /**
     * Set the brcBar field for the PAP record.
     */
    @Internal
    public void setBrcBar( BorderCode field_65_brcBar )
    {
        this.field_65_brcBar = field_65_brcBar;
    }

    /**
     * Get the shd field for the PAP record.
     */
    @Internal
    public ShadingDescriptor getShd()
    {
        return field_66_shd;
    }

    /**
     * Set the shd field for the PAP record.
     */
    @Internal
    public void setShd( ShadingDescriptor field_66_shd )
    {
        this.field_66_shd = field_66_shd;
    }

    /**
     * Get the anld field for the PAP record.
     */
    @Internal
    public byte[] getAnld()
    {
        return field_67_anld;
    }

    /**
     * Set the anld field for the PAP record.
     */
    @Internal
    public void setAnld( byte[] field_67_anld )
    {
        this.field_67_anld = field_67_anld;
    }

    /**
     * Get the phe field for the PAP record.
     */
    @Internal
    public byte[] getPhe()
    {
        return field_68_phe;
    }

    /**
     * Set the phe field for the PAP record.
     */
    @Internal
    public void setPhe( byte[] field_68_phe )
    {
        this.field_68_phe = field_68_phe;
    }

    /**
     * Get the fPropRMark field for the PAP record.
     */
    @Internal
    public boolean getFPropRMark()
    {
        return field_69_fPropRMark;
    }

    /**
     * Set the fPropRMark field for the PAP record.
     */
    @Internal
    public void setFPropRMark( boolean field_69_fPropRMark )
    {
        this.field_69_fPropRMark = field_69_fPropRMark;
    }

    /**
     * Get the ibstPropRMark field for the PAP record.
     */
    @Internal
    public int getIbstPropRMark()
    {
        return field_70_ibstPropRMark;
    }

    /**
     * Set the ibstPropRMark field for the PAP record.
     */
    @Internal
    public void setIbstPropRMark( int field_70_ibstPropRMark )
    {
        this.field_70_ibstPropRMark = field_70_ibstPropRMark;
    }

    /**
     * Get the dttmPropRMark field for the PAP record.
     */
    @Internal
    public DateAndTime getDttmPropRMark()
    {
        return field_71_dttmPropRMark;
    }

    /**
     * Set the dttmPropRMark field for the PAP record.
     */
    @Internal
    public void setDttmPropRMark( DateAndTime field_71_dttmPropRMark )
    {
        this.field_71_dttmPropRMark = field_71_dttmPropRMark;
    }

    /**
     * Number of tabs stops defined for paragraph. Must be >= 0 and <= 64..
     */
    @Internal
    public int getItbdMac()
    {
        return field_72_itbdMac;
    }

    /**
     * Number of tabs stops defined for paragraph. Must be >= 0 and <= 64..
     */
    @Internal
    public void setItbdMac( int field_72_itbdMac )
    {
        this.field_72_itbdMac = field_72_itbdMac;
    }

    /**
     * Array of positions of itbdMac tab stops. itbdMax==64.
     */
    @Internal
    public int[] getRgdxaTab()
    {
        return field_73_rgdxaTab;
    }

    /**
     * Array of positions of itbdMac tab stops. itbdMax==64.
     */
    @Internal
    public void setRgdxaTab( int[] field_73_rgdxaTab )
    {
        this.field_73_rgdxaTab = field_73_rgdxaTab;
    }

    /**
     * Array of itbdMac tab descriptors.
     */
    @Internal
    public TabDescriptor[] getRgtbd()
    {
        return field_74_rgtbd;
    }

    /**
     * Array of itbdMac tab descriptors.
     */
    @Internal
    public void setRgtbd( TabDescriptor[] field_74_rgtbd )
    {
        this.field_74_rgtbd = field_74_rgtbd;
    }

    /**
     * Get the numrm field for the PAP record.
     */
    @Internal
    public byte[] getNumrm()
    {
        return field_75_numrm;
    }

    /**
     * Set the numrm field for the PAP record.
     */
    @Internal
    public void setNumrm( byte[] field_75_numrm )
    {
        this.field_75_numrm = field_75_numrm;
    }

    /**
     * Get the ptap field for the PAP record.
     */
    @Internal
    public byte[] getPtap()
    {
        return field_76_ptap;
    }

    /**
     * Set the ptap field for the PAP record.
     */
    @Internal
    public void setPtap( byte[] field_76_ptap )
    {
        this.field_76_ptap = field_76_ptap;
    }

    /**
     * When 1, absolutely positioned paragraph cannot overlap with another paragraph.
     */
    @Internal
    public boolean getFNoAllowOverlap()
    {
        return field_77_fNoAllowOverlap;
    }

    /**
     * When 1, absolutely positioned paragraph cannot overlap with another paragraph.
     */
    @Internal
    public void setFNoAllowOverlap( boolean field_77_fNoAllowOverlap )
    {
        this.field_77_fNoAllowOverlap = field_77_fNoAllowOverlap;
    }

    /**
     * HTML DIV ID for this paragraph.
     */
    @Internal
    public long getIpgp()
    {
        return field_78_ipgp;
    }

    /**
     * HTML DIV ID for this paragraph.
     */
    @Internal
    public void setIpgp( long field_78_ipgp )
    {
        this.field_78_ipgp = field_78_ipgp;
    }

    /**
     * Save ID for last time this PAP was revised.
     */
    @Internal
    public long getRsid()
    {
        return field_79_rsid;
    }

    /**
     * Save ID for last time this PAP was revised.
     */
    @Internal
    public void setRsid( long field_79_rsid )
    {
        this.field_79_rsid = field_79_rsid;
    }

    /**
     * Sets the fVertical field value.
     *
     */
    @Internal
    public void setFVertical( boolean value )
    {
        field_40_fontAlign = (short)fVertical.setBoolean(field_40_fontAlign, value);
    }

    /**
     *
     * @return  the fVertical field value.
     */
    @Internal
    public boolean isFVertical()
    {
        return fVertical.isSet(field_40_fontAlign);
    }

    /**
     * Sets the fBackward field value.
     *
     */
    @Internal
    public void setFBackward( boolean value )
    {
        field_40_fontAlign = (short)fBackward.setBoolean(field_40_fontAlign, value);
    }

    /**
     *
     * @return  the fBackward field value.
     */
    @Internal
    public boolean isFBackward()
    {
        return fBackward.isSet(field_40_fontAlign);
    }

    /**
     * Sets the fRotateFont field value.
     *
     */
    @Internal
    public void setFRotateFont( boolean value )
    {
        field_40_fontAlign = (short)fRotateFont.setBoolean(field_40_fontAlign, value);
    }

    /**
     *
     * @return  the fRotateFont field value.
     */
    @Internal
    public boolean isFRotateFont()
    {
        return fRotateFont.isSet(field_40_fontAlign);
    }

}  // END OF CLASS
