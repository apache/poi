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



import org.apache.poi.hdf.model.hdftypes.HDFType;
import org.apache.poi.hwpf.usermodel.BorderCode;
import org.apache.poi.hwpf.usermodel.DateAndTime;
import org.apache.poi.hwpf.usermodel.DropCapSpecifier;
import org.apache.poi.hwpf.usermodel.LineSpacingDescriptor;
import org.apache.poi.hwpf.usermodel.ShadingDescriptor;
import org.apache.poi.util.BitField;

/**
 * Paragraph Properties.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author S. Ryan Ackley
 */
public abstract class PAPAbstractType
    implements HDFType
{

    protected  int field_1_istd;
    protected  byte field_2_fSideBySide;
    protected  byte field_3_fKeep;
    protected  byte field_4_fKeepFollow;
    protected  byte field_5_fPageBreakBefore;
    protected  byte field_6_brcl;
    protected  byte field_7_brcp;
    protected  byte field_8_ilvl;
    protected  int field_9_ilfo;
    protected  byte field_10_fNoLnn;
    protected  LineSpacingDescriptor field_11_lspd;
    protected  int field_12_dyaBefore;
    protected  int field_13_dyaAfter;
    protected  byte field_14_fInTable;
    protected  byte field_15_finTableW97;
    protected  byte field_16_fTtp;
    protected  int field_17_dxaAbs;
    protected  int field_18_dyaAbs;
    protected  int field_19_dxaWidth;
    protected  byte field_20_fBrLnAbove;
    protected  byte field_21_fBrLnBelow;
    protected  byte field_22_pcVert;
    protected  byte field_23_pcHorz;
    protected  byte field_24_wr;
    protected  byte field_25_fNoAutoHyph;
    protected  int field_26_dyaHeight;
    protected  byte field_27_fMinHeight;
    protected  DropCapSpecifier field_28_dcs;
    protected  int field_29_dyaFromText;
    protected  int field_30_dxaFromText;
    protected  byte field_31_fLocked;
    protected  byte field_32_fWidowControl;
    protected  byte field_33_fKinsoku;
    protected  byte field_34_fWordWrap;
    protected  byte field_35_fOverflowPunct;
    protected  byte field_36_fTopLinePunct;
    protected  byte field_37_fAutoSpaceDE;
    protected  byte field_38_fAutoSpaceDN;
    protected  int field_39_wAlignFont;
    protected  short field_40_fontAlign;
        private static BitField  fVertical = new BitField(0x0001);
        private static BitField  fBackward = new BitField(0x0002);
        private static BitField  fRotateFont = new BitField(0x0004);
    protected  byte field_41_fVertical;
    protected  byte field_42_fBackward;
    protected  byte field_43_fRotateFont;
    protected  byte field_44_lvl;
    protected  byte field_45_fBiDi;
    protected  byte field_46_fNumRMIns;
    protected  byte field_47_fCrLf;
    protected  byte field_48_fUsePgsuSettings;
    protected  byte field_49_fAdjustRight;
    protected  int field_50_itap;
    protected  byte field_51_fInnerTableCell;
    protected  byte field_52_fOpenTch;
    protected  byte field_53_fTtpEmbedded;
    protected  short field_54_dxcRight;
    protected  short field_55_dxcLeft;
    protected  short field_56_dxcLeft1;
    protected  byte field_57_fDyaBeforeAuto;
    protected  byte field_58_fDyaAfterAuto;
    protected  int field_59_dxaRight;
    protected  int field_60_dxaLeft;
    protected  int field_61_dxaLeft1;
    protected  byte field_62_jc;
    protected  byte field_63_fNoAllowOverlap;
    protected  BorderCode field_64_brcTop;
    protected  BorderCode field_65_brcLeft;
    protected  BorderCode field_66_brcBottom;
    protected  BorderCode field_67_brcRight;
    protected  BorderCode field_68_brcBetween;
    protected  BorderCode field_69_brcBar;
    protected  ShadingDescriptor field_70_shd;
    protected  byte[] field_71_anld;
    protected  byte[] field_72_phe;
    protected  int field_73_fPropRMark;
    protected  int field_74_ibstPropRMark;
    protected  DateAndTime field_75_dttmPropRMark;
    protected  int field_76_itbdMac;
    protected  int[] field_77_rgdxaTab;
    protected  byte[] field_78_rgtbd;
    protected  byte[] field_79_numrm;
    protected  byte[] field_80_ptap;


    public PAPAbstractType()
    {

    }


    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[PAP]\n");

        buffer.append("    .istd                 = ");
        buffer.append(" (").append(getIstd()).append(" )\n");

        buffer.append("    .fSideBySide          = ");
        buffer.append(" (").append(getFSideBySide()).append(" )\n");

        buffer.append("    .fKeep                = ");
        buffer.append(" (").append(getFKeep()).append(" )\n");

        buffer.append("    .fKeepFollow          = ");
        buffer.append(" (").append(getFKeepFollow()).append(" )\n");

        buffer.append("    .fPageBreakBefore     = ");
        buffer.append(" (").append(getFPageBreakBefore()).append(" )\n");

        buffer.append("    .brcl                 = ");
        buffer.append(" (").append(getBrcl()).append(" )\n");

        buffer.append("    .brcp                 = ");
        buffer.append(" (").append(getBrcp()).append(" )\n");

        buffer.append("    .ilvl                 = ");
        buffer.append(" (").append(getIlvl()).append(" )\n");

        buffer.append("    .ilfo                 = ");
        buffer.append(" (").append(getIlfo()).append(" )\n");

        buffer.append("    .fNoLnn               = ");
        buffer.append(" (").append(getFNoLnn()).append(" )\n");

        buffer.append("    .lspd                 = ");
        buffer.append(" (").append(getLspd()).append(" )\n");

        buffer.append("    .dyaBefore            = ");
        buffer.append(" (").append(getDyaBefore()).append(" )\n");

        buffer.append("    .dyaAfter             = ");
        buffer.append(" (").append(getDyaAfter()).append(" )\n");

        buffer.append("    .fInTable             = ");
        buffer.append(" (").append(getFInTable()).append(" )\n");

        buffer.append("    .finTableW97          = ");
        buffer.append(" (").append(getFinTableW97()).append(" )\n");

        buffer.append("    .fTtp                 = ");
        buffer.append(" (").append(getFTtp()).append(" )\n");

        buffer.append("    .dxaAbs               = ");
        buffer.append(" (").append(getDxaAbs()).append(" )\n");

        buffer.append("    .dyaAbs               = ");
        buffer.append(" (").append(getDyaAbs()).append(" )\n");

        buffer.append("    .dxaWidth             = ");
        buffer.append(" (").append(getDxaWidth()).append(" )\n");

        buffer.append("    .fBrLnAbove           = ");
        buffer.append(" (").append(getFBrLnAbove()).append(" )\n");

        buffer.append("    .fBrLnBelow           = ");
        buffer.append(" (").append(getFBrLnBelow()).append(" )\n");

        buffer.append("    .pcVert               = ");
        buffer.append(" (").append(getPcVert()).append(" )\n");

        buffer.append("    .pcHorz               = ");
        buffer.append(" (").append(getPcHorz()).append(" )\n");

        buffer.append("    .wr                   = ");
        buffer.append(" (").append(getWr()).append(" )\n");

        buffer.append("    .fNoAutoHyph          = ");
        buffer.append(" (").append(getFNoAutoHyph()).append(" )\n");

        buffer.append("    .dyaHeight            = ");
        buffer.append(" (").append(getDyaHeight()).append(" )\n");

        buffer.append("    .fMinHeight           = ");
        buffer.append(" (").append(getFMinHeight()).append(" )\n");

        buffer.append("    .dcs                  = ");
        buffer.append(" (").append(getDcs()).append(" )\n");

        buffer.append("    .dyaFromText          = ");
        buffer.append(" (").append(getDyaFromText()).append(" )\n");

        buffer.append("    .dxaFromText          = ");
        buffer.append(" (").append(getDxaFromText()).append(" )\n");

        buffer.append("    .fLocked              = ");
        buffer.append(" (").append(getFLocked()).append(" )\n");

        buffer.append("    .fWidowControl        = ");
        buffer.append(" (").append(getFWidowControl()).append(" )\n");

        buffer.append("    .fKinsoku             = ");
        buffer.append(" (").append(getFKinsoku()).append(" )\n");

        buffer.append("    .fWordWrap            = ");
        buffer.append(" (").append(getFWordWrap()).append(" )\n");

        buffer.append("    .fOverflowPunct       = ");
        buffer.append(" (").append(getFOverflowPunct()).append(" )\n");

        buffer.append("    .fTopLinePunct        = ");
        buffer.append(" (").append(getFTopLinePunct()).append(" )\n");

        buffer.append("    .fAutoSpaceDE         = ");
        buffer.append(" (").append(getFAutoSpaceDE()).append(" )\n");

        buffer.append("    .fAutoSpaceDN         = ");
        buffer.append(" (").append(getFAutoSpaceDN()).append(" )\n");

        buffer.append("    .wAlignFont           = ");
        buffer.append(" (").append(getWAlignFont()).append(" )\n");

        buffer.append("    .fontAlign            = ");
        buffer.append(" (").append(getFontAlign()).append(" )\n");
        buffer.append("         .fVertical                = ").append(isFVertical()).append('\n');
        buffer.append("         .fBackward                = ").append(isFBackward()).append('\n');
        buffer.append("         .fRotateFont              = ").append(isFRotateFont()).append('\n');

        buffer.append("    .fVertical            = ");
        buffer.append(" (").append(getFVertical()).append(" )\n");

        buffer.append("    .fBackward            = ");
        buffer.append(" (").append(getFBackward()).append(" )\n");

        buffer.append("    .fRotateFont          = ");
        buffer.append(" (").append(getFRotateFont()).append(" )\n");

        buffer.append("    .lvl                  = ");
        buffer.append(" (").append(getLvl()).append(" )\n");

        buffer.append("    .fBiDi                = ");
        buffer.append(" (").append(getFBiDi()).append(" )\n");

        buffer.append("    .fNumRMIns            = ");
        buffer.append(" (").append(getFNumRMIns()).append(" )\n");

        buffer.append("    .fCrLf                = ");
        buffer.append(" (").append(getFCrLf()).append(" )\n");

        buffer.append("    .fUsePgsuSettings     = ");
        buffer.append(" (").append(getFUsePgsuSettings()).append(" )\n");

        buffer.append("    .fAdjustRight         = ");
        buffer.append(" (").append(getFAdjustRight()).append(" )\n");

        buffer.append("    .itap                 = ");
        buffer.append(" (").append(getItap()).append(" )\n");

        buffer.append("    .fInnerTableCell      = ");
        buffer.append(" (").append(getFInnerTableCell()).append(" )\n");

        buffer.append("    .fOpenTch             = ");
        buffer.append(" (").append(getFOpenTch()).append(" )\n");

        buffer.append("    .fTtpEmbedded         = ");
        buffer.append(" (").append(getFTtpEmbedded()).append(" )\n");

        buffer.append("    .dxcRight             = ");
        buffer.append(" (").append(getDxcRight()).append(" )\n");

        buffer.append("    .dxcLeft              = ");
        buffer.append(" (").append(getDxcLeft()).append(" )\n");

        buffer.append("    .dxcLeft1             = ");
        buffer.append(" (").append(getDxcLeft1()).append(" )\n");

        buffer.append("    .fDyaBeforeAuto       = ");
        buffer.append(" (").append(getFDyaBeforeAuto()).append(" )\n");

        buffer.append("    .fDyaAfterAuto        = ");
        buffer.append(" (").append(getFDyaAfterAuto()).append(" )\n");

        buffer.append("    .dxaRight             = ");
        buffer.append(" (").append(getDxaRight()).append(" )\n");

        buffer.append("    .dxaLeft              = ");
        buffer.append(" (").append(getDxaLeft()).append(" )\n");

        buffer.append("    .dxaLeft1             = ");
        buffer.append(" (").append(getDxaLeft1()).append(" )\n");

        buffer.append("    .jc                   = ");
        buffer.append(" (").append(getJc()).append(" )\n");

        buffer.append("    .fNoAllowOverlap      = ");
        buffer.append(" (").append(getFNoAllowOverlap()).append(" )\n");

        buffer.append("    .brcTop               = ");
        buffer.append(" (").append(getBrcTop()).append(" )\n");

        buffer.append("    .brcLeft              = ");
        buffer.append(" (").append(getBrcLeft()).append(" )\n");

        buffer.append("    .brcBottom            = ");
        buffer.append(" (").append(getBrcBottom()).append(" )\n");

        buffer.append("    .brcRight             = ");
        buffer.append(" (").append(getBrcRight()).append(" )\n");

        buffer.append("    .brcBetween           = ");
        buffer.append(" (").append(getBrcBetween()).append(" )\n");

        buffer.append("    .brcBar               = ");
        buffer.append(" (").append(getBrcBar()).append(" )\n");

        buffer.append("    .shd                  = ");
        buffer.append(" (").append(getShd()).append(" )\n");

        buffer.append("    .anld                 = ");
        buffer.append(" (").append(getAnld()).append(" )\n");

        buffer.append("    .phe                  = ");
        buffer.append(" (").append(getPhe()).append(" )\n");

        buffer.append("    .fPropRMark           = ");
        buffer.append(" (").append(getFPropRMark()).append(" )\n");

        buffer.append("    .ibstPropRMark        = ");
        buffer.append(" (").append(getIbstPropRMark()).append(" )\n");

        buffer.append("    .dttmPropRMark        = ");
        buffer.append(" (").append(getDttmPropRMark()).append(" )\n");

        buffer.append("    .itbdMac              = ");
        buffer.append(" (").append(getItbdMac()).append(" )\n");

        buffer.append("    .rgdxaTab             = ");
        buffer.append(" (").append(getRgdxaTab()).append(" )\n");

        buffer.append("    .rgtbd                = ");
        buffer.append(" (").append(getRgtbd()).append(" )\n");

        buffer.append("    .numrm                = ");
        buffer.append(" (").append(getNumrm()).append(" )\n");

        buffer.append("    .ptap                 = ");
        buffer.append(" (").append(getPtap()).append(" )\n");

        buffer.append("[/PAP]\n");
        return buffer.toString();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getSize()
    {
        return 4 +  + 2 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 2 + 1 + 4 + 4 + 4 + 1 + 1 + 1 + 4 + 4 + 4 + 1 + 1 + 1 + 1 + 1 + 1 + 2 + 1 + 2 + 4 + 4 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 2 + 2 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 4 + 1 + 1 + 1 + 2 + 2 + 2 + 1 + 1 + 4 + 4 + 4 + 1 + 1 + 4 + 4 + 4 + 4 + 4 + 4 + 2 + 84 + 12 + 1 + 2 + 4 + 2 + 128 + 128 + 128 + 4;
    }



    /**
     * Index to style descriptor.
     */
    public int getIstd()
    {
        return field_1_istd;
    }

    /**
     * Index to style descriptor.
     */
    public void setIstd(int field_1_istd)
    {
        this.field_1_istd = field_1_istd;
    }

    /**
     * Get the fSideBySide field for the PAP record.
     */
    public byte getFSideBySide()
    {
        return field_2_fSideBySide;
    }

    /**
     * Set the fSideBySide field for the PAP record.
     */
    public void setFSideBySide(byte field_2_fSideBySide)
    {
        this.field_2_fSideBySide = field_2_fSideBySide;
    }

    /**
     * Get the fKeep field for the PAP record.
     */
    public byte getFKeep()
    {
        return field_3_fKeep;
    }

    /**
     * Set the fKeep field for the PAP record.
     */
    public void setFKeep(byte field_3_fKeep)
    {
        this.field_3_fKeep = field_3_fKeep;
    }

    /**
     * Get the fKeepFollow field for the PAP record.
     */
    public byte getFKeepFollow()
    {
        return field_4_fKeepFollow;
    }

    /**
     * Set the fKeepFollow field for the PAP record.
     */
    public void setFKeepFollow(byte field_4_fKeepFollow)
    {
        this.field_4_fKeepFollow = field_4_fKeepFollow;
    }

    /**
     * Get the fPageBreakBefore field for the PAP record.
     */
    public byte getFPageBreakBefore()
    {
        return field_5_fPageBreakBefore;
    }

    /**
     * Set the fPageBreakBefore field for the PAP record.
     */
    public void setFPageBreakBefore(byte field_5_fPageBreakBefore)
    {
        this.field_5_fPageBreakBefore = field_5_fPageBreakBefore;
    }

    /**
     * Border line style.
     */
    public byte getBrcl()
    {
        return field_6_brcl;
    }

    /**
     * Border line style.
     */
    public void setBrcl(byte field_6_brcl)
    {
        this.field_6_brcl = field_6_brcl;
    }

    /**
     * Rectangle border codes.
     */
    public byte getBrcp()
    {
        return field_7_brcp;
    }

    /**
     * Rectangle border codes.
     */
    public void setBrcp(byte field_7_brcp)
    {
        this.field_7_brcp = field_7_brcp;
    }

    /**
     * List level if non-zero.
     */
    public byte getIlvl()
    {
        return field_8_ilvl;
    }

    /**
     * List level if non-zero.
     */
    public void setIlvl(byte field_8_ilvl)
    {
        this.field_8_ilvl = field_8_ilvl;
    }

    /**
     * 1-based index into the pllfo (lists structure), if non-zero.
     */
    public int getIlfo()
    {
        return field_9_ilfo;
    }

    /**
     * 1-based index into the pllfo (lists structure), if non-zero.
     */
    public void setIlfo(int field_9_ilfo)
    {
        this.field_9_ilfo = field_9_ilfo;
    }

    /**
     * No line numbering.
     */
    public byte getFNoLnn()
    {
        return field_10_fNoLnn;
    }

    /**
     * No line numbering.
     */
    public void setFNoLnn(byte field_10_fNoLnn)
    {
        this.field_10_fNoLnn = field_10_fNoLnn;
    }

    /**
     * Line spacing descriptor.
     */
    public LineSpacingDescriptor getLspd()
    {
        return field_11_lspd;
    }

    /**
     * Line spacing descriptor.
     */
    public void setLspd(LineSpacingDescriptor field_11_lspd)
    {
        this.field_11_lspd = field_11_lspd;
    }

    /**
     * Space before paragraph.
     */
    public int getDyaBefore()
    {
        return field_12_dyaBefore;
    }

    /**
     * Space before paragraph.
     */
    public void setDyaBefore(int field_12_dyaBefore)
    {
        this.field_12_dyaBefore = field_12_dyaBefore;
    }

    /**
     * Space after paragraph.
     */
    public int getDyaAfter()
    {
        return field_13_dyaAfter;
    }

    /**
     * Space after paragraph.
     */
    public void setDyaAfter(int field_13_dyaAfter)
    {
        this.field_13_dyaAfter = field_13_dyaAfter;
    }

    /**
     * Paragraph is in table flag.
     */
    public byte getFInTable()
    {
        return field_14_fInTable;
    }

    /**
     * Paragraph is in table flag.
     */
    public void setFInTable(byte field_14_fInTable)
    {
        this.field_14_fInTable = field_14_fInTable;
    }

    /**
     * Archaic paragraph is in table flag.
     */
    public byte getFinTableW97()
    {
        return field_15_finTableW97;
    }

    /**
     * Archaic paragraph is in table flag.
     */
    public void setFinTableW97(byte field_15_finTableW97)
    {
        this.field_15_finTableW97 = field_15_finTableW97;
    }

    /**
     * Table trailer paragraph (last in table row).
     */
    public byte getFTtp()
    {
        return field_16_fTtp;
    }

    /**
     * Table trailer paragraph (last in table row).
     */
    public void setFTtp(byte field_16_fTtp)
    {
        this.field_16_fTtp = field_16_fTtp;
    }

    /**
     * Get the dxaAbs field for the PAP record.
     */
    public int getDxaAbs()
    {
        return field_17_dxaAbs;
    }

    /**
     * Set the dxaAbs field for the PAP record.
     */
    public void setDxaAbs(int field_17_dxaAbs)
    {
        this.field_17_dxaAbs = field_17_dxaAbs;
    }

    /**
     * Get the dyaAbs field for the PAP record.
     */
    public int getDyaAbs()
    {
        return field_18_dyaAbs;
    }

    /**
     * Set the dyaAbs field for the PAP record.
     */
    public void setDyaAbs(int field_18_dyaAbs)
    {
        this.field_18_dyaAbs = field_18_dyaAbs;
    }

    /**
     * Get the dxaWidth field for the PAP record.
     */
    public int getDxaWidth()
    {
        return field_19_dxaWidth;
    }

    /**
     * Set the dxaWidth field for the PAP record.
     */
    public void setDxaWidth(int field_19_dxaWidth)
    {
        this.field_19_dxaWidth = field_19_dxaWidth;
    }

    /**
     * Get the fBrLnAbove field for the PAP record.
     */
    public byte getFBrLnAbove()
    {
        return field_20_fBrLnAbove;
    }

    /**
     * Set the fBrLnAbove field for the PAP record.
     */
    public void setFBrLnAbove(byte field_20_fBrLnAbove)
    {
        this.field_20_fBrLnAbove = field_20_fBrLnAbove;
    }

    /**
     * Get the fBrLnBelow field for the PAP record.
     */
    public byte getFBrLnBelow()
    {
        return field_21_fBrLnBelow;
    }

    /**
     * Set the fBrLnBelow field for the PAP record.
     */
    public void setFBrLnBelow(byte field_21_fBrLnBelow)
    {
        this.field_21_fBrLnBelow = field_21_fBrLnBelow;
    }

    /**
     * Get the pcVert field for the PAP record.
     */
    public byte getPcVert()
    {
        return field_22_pcVert;
    }

    /**
     * Set the pcVert field for the PAP record.
     */
    public void setPcVert(byte field_22_pcVert)
    {
        this.field_22_pcVert = field_22_pcVert;
    }

    /**
     * Get the pcHorz field for the PAP record.
     */
    public byte getPcHorz()
    {
        return field_23_pcHorz;
    }

    /**
     * Set the pcHorz field for the PAP record.
     */
    public void setPcHorz(byte field_23_pcHorz)
    {
        this.field_23_pcHorz = field_23_pcHorz;
    }

    /**
     * Get the wr field for the PAP record.
     */
    public byte getWr()
    {
        return field_24_wr;
    }

    /**
     * Set the wr field for the PAP record.
     */
    public void setWr(byte field_24_wr)
    {
        this.field_24_wr = field_24_wr;
    }

    /**
     * Get the fNoAutoHyph field for the PAP record.
     */
    public byte getFNoAutoHyph()
    {
        return field_25_fNoAutoHyph;
    }

    /**
     * Set the fNoAutoHyph field for the PAP record.
     */
    public void setFNoAutoHyph(byte field_25_fNoAutoHyph)
    {
        this.field_25_fNoAutoHyph = field_25_fNoAutoHyph;
    }

    /**
     * Get the dyaHeight field for the PAP record.
     */
    public int getDyaHeight()
    {
        return field_26_dyaHeight;
    }

    /**
     * Set the dyaHeight field for the PAP record.
     */
    public void setDyaHeight(int field_26_dyaHeight)
    {
        this.field_26_dyaHeight = field_26_dyaHeight;
    }

    /**
     * Get the fMinHeight field for the PAP record.
     */
    public byte getFMinHeight()
    {
        return field_27_fMinHeight;
    }

    /**
     * Set the fMinHeight field for the PAP record.
     */
    public void setFMinHeight(byte field_27_fMinHeight)
    {
        this.field_27_fMinHeight = field_27_fMinHeight;
    }

    /**
     * Get the dcs field for the PAP record.
     */
    public DropCapSpecifier getDcs()
    {
        return field_28_dcs;
    }

    /**
     * Set the dcs field for the PAP record.
     */
    public void setDcs(DropCapSpecifier field_28_dcs)
    {
        this.field_28_dcs = field_28_dcs;
    }

    /**
     * Vertical distance between text and absolutely positioned object.
     */
    public int getDyaFromText()
    {
        return field_29_dyaFromText;
    }

    /**
     * Vertical distance between text and absolutely positioned object.
     */
    public void setDyaFromText(int field_29_dyaFromText)
    {
        this.field_29_dyaFromText = field_29_dyaFromText;
    }

    /**
     * Horizontal distance between text and absolutely positioned object.
     */
    public int getDxaFromText()
    {
        return field_30_dxaFromText;
    }

    /**
     * Horizontal distance between text and absolutely positioned object.
     */
    public void setDxaFromText(int field_30_dxaFromText)
    {
        this.field_30_dxaFromText = field_30_dxaFromText;
    }

    /**
     * Get the fLocked field for the PAP record.
     */
    public byte getFLocked()
    {
        return field_31_fLocked;
    }

    /**
     * Set the fLocked field for the PAP record.
     */
    public void setFLocked(byte field_31_fLocked)
    {
        this.field_31_fLocked = field_31_fLocked;
    }

    /**
     * Get the fWidowControl field for the PAP record.
     */
    public byte getFWidowControl()
    {
        return field_32_fWidowControl;
    }

    /**
     * Set the fWidowControl field for the PAP record.
     */
    public void setFWidowControl(byte field_32_fWidowControl)
    {
        this.field_32_fWidowControl = field_32_fWidowControl;
    }

    /**
     * Get the fKinsoku field for the PAP record.
     */
    public byte getFKinsoku()
    {
        return field_33_fKinsoku;
    }

    /**
     * Set the fKinsoku field for the PAP record.
     */
    public void setFKinsoku(byte field_33_fKinsoku)
    {
        this.field_33_fKinsoku = field_33_fKinsoku;
    }

    /**
     * Get the fWordWrap field for the PAP record.
     */
    public byte getFWordWrap()
    {
        return field_34_fWordWrap;
    }

    /**
     * Set the fWordWrap field for the PAP record.
     */
    public void setFWordWrap(byte field_34_fWordWrap)
    {
        this.field_34_fWordWrap = field_34_fWordWrap;
    }

    /**
     * Get the fOverflowPunct field for the PAP record.
     */
    public byte getFOverflowPunct()
    {
        return field_35_fOverflowPunct;
    }

    /**
     * Set the fOverflowPunct field for the PAP record.
     */
    public void setFOverflowPunct(byte field_35_fOverflowPunct)
    {
        this.field_35_fOverflowPunct = field_35_fOverflowPunct;
    }

    /**
     * Get the fTopLinePunct field for the PAP record.
     */
    public byte getFTopLinePunct()
    {
        return field_36_fTopLinePunct;
    }

    /**
     * Set the fTopLinePunct field for the PAP record.
     */
    public void setFTopLinePunct(byte field_36_fTopLinePunct)
    {
        this.field_36_fTopLinePunct = field_36_fTopLinePunct;
    }

    /**
     * Get the fAutoSpaceDE field for the PAP record.
     */
    public byte getFAutoSpaceDE()
    {
        return field_37_fAutoSpaceDE;
    }

    /**
     * Set the fAutoSpaceDE field for the PAP record.
     */
    public void setFAutoSpaceDE(byte field_37_fAutoSpaceDE)
    {
        this.field_37_fAutoSpaceDE = field_37_fAutoSpaceDE;
    }

    /**
     * Get the fAutoSpaceDN field for the PAP record.
     */
    public byte getFAutoSpaceDN()
    {
        return field_38_fAutoSpaceDN;
    }

    /**
     * Set the fAutoSpaceDN field for the PAP record.
     */
    public void setFAutoSpaceDN(byte field_38_fAutoSpaceDN)
    {
        this.field_38_fAutoSpaceDN = field_38_fAutoSpaceDN;
    }

    /**
     * Get the wAlignFont field for the PAP record.
     */
    public int getWAlignFont()
    {
        return field_39_wAlignFont;
    }

    /**
     * Set the wAlignFont field for the PAP record.
     */
    public void setWAlignFont(int field_39_wAlignFont)
    {
        this.field_39_wAlignFont = field_39_wAlignFont;
    }

    /**
     * Get the fontAlign field for the PAP record.
     */
    public short getFontAlign()
    {
        return field_40_fontAlign;
    }

    /**
     * Set the fontAlign field for the PAP record.
     */
    public void setFontAlign(short field_40_fontAlign)
    {
        this.field_40_fontAlign = field_40_fontAlign;
    }

    /**
     * Get the fVertical field for the PAP record.
     */
    public byte getFVertical()
    {
        return field_41_fVertical;
    }

    /**
     * Set the fVertical field for the PAP record.
     */
    public void setFVertical(byte field_41_fVertical)
    {
        this.field_41_fVertical = field_41_fVertical;
    }

    /**
     * Get the fBackward field for the PAP record.
     */
    public byte getFBackward()
    {
        return field_42_fBackward;
    }

    /**
     * Set the fBackward field for the PAP record.
     */
    public void setFBackward(byte field_42_fBackward)
    {
        this.field_42_fBackward = field_42_fBackward;
    }

    /**
     * Get the fRotateFont field for the PAP record.
     */
    public byte getFRotateFont()
    {
        return field_43_fRotateFont;
    }

    /**
     * Set the fRotateFont field for the PAP record.
     */
    public void setFRotateFont(byte field_43_fRotateFont)
    {
        this.field_43_fRotateFont = field_43_fRotateFont;
    }

    /**
     * Get the lvl field for the PAP record.
     */
    public byte getLvl()
    {
        return field_44_lvl;
    }

    /**
     * Set the lvl field for the PAP record.
     */
    public void setLvl(byte field_44_lvl)
    {
        this.field_44_lvl = field_44_lvl;
    }

    /**
     * Get the fBiDi field for the PAP record.
     */
    public byte getFBiDi()
    {
        return field_45_fBiDi;
    }

    /**
     * Set the fBiDi field for the PAP record.
     */
    public void setFBiDi(byte field_45_fBiDi)
    {
        this.field_45_fBiDi = field_45_fBiDi;
    }

    /**
     * Get the fNumRMIns field for the PAP record.
     */
    public byte getFNumRMIns()
    {
        return field_46_fNumRMIns;
    }

    /**
     * Set the fNumRMIns field for the PAP record.
     */
    public void setFNumRMIns(byte field_46_fNumRMIns)
    {
        this.field_46_fNumRMIns = field_46_fNumRMIns;
    }

    /**
     * Get the fCrLf field for the PAP record.
     */
    public byte getFCrLf()
    {
        return field_47_fCrLf;
    }

    /**
     * Set the fCrLf field for the PAP record.
     */
    public void setFCrLf(byte field_47_fCrLf)
    {
        this.field_47_fCrLf = field_47_fCrLf;
    }

    /**
     * Get the fUsePgsuSettings field for the PAP record.
     */
    public byte getFUsePgsuSettings()
    {
        return field_48_fUsePgsuSettings;
    }

    /**
     * Set the fUsePgsuSettings field for the PAP record.
     */
    public void setFUsePgsuSettings(byte field_48_fUsePgsuSettings)
    {
        this.field_48_fUsePgsuSettings = field_48_fUsePgsuSettings;
    }

    /**
     * Get the fAdjustRight field for the PAP record.
     */
    public byte getFAdjustRight()
    {
        return field_49_fAdjustRight;
    }

    /**
     * Set the fAdjustRight field for the PAP record.
     */
    public void setFAdjustRight(byte field_49_fAdjustRight)
    {
        this.field_49_fAdjustRight = field_49_fAdjustRight;
    }

    /**
     * Table nesting level.
     */
    public int getItap()
    {
        return field_50_itap;
    }

    /**
     * Table nesting level.
     */
    public void setItap(int field_50_itap)
    {
        this.field_50_itap = field_50_itap;
    }

    /**
     * When 1, the end of paragraph mark is really an end of cell mark for a nested table cell.
     */
    public byte getFInnerTableCell()
    {
        return field_51_fInnerTableCell;
    }

    /**
     * When 1, the end of paragraph mark is really an end of cell mark for a nested table cell.
     */
    public void setFInnerTableCell(byte field_51_fInnerTableCell)
    {
        this.field_51_fInnerTableCell = field_51_fInnerTableCell;
    }

    /**
     * Ensure the Table Cell char doesn't show up as zero height.
     */
    public byte getFOpenTch()
    {
        return field_52_fOpenTch;
    }

    /**
     * Ensure the Table Cell char doesn't show up as zero height.
     */
    public void setFOpenTch(byte field_52_fOpenTch)
    {
        this.field_52_fOpenTch = field_52_fOpenTch;
    }

    /**
     * Word 97 compatibility indicates this end of paragraph mark is really an end of row marker for a nested table.
     */
    public byte getFTtpEmbedded()
    {
        return field_53_fTtpEmbedded;
    }

    /**
     * Word 97 compatibility indicates this end of paragraph mark is really an end of row marker for a nested table.
     */
    public void setFTtpEmbedded(byte field_53_fTtpEmbedded)
    {
        this.field_53_fTtpEmbedded = field_53_fTtpEmbedded;
    }

    /**
     * Right indent in character units.
     */
    public short getDxcRight()
    {
        return field_54_dxcRight;
    }

    /**
     * Right indent in character units.
     */
    public void setDxcRight(short field_54_dxcRight)
    {
        this.field_54_dxcRight = field_54_dxcRight;
    }

    /**
     * Left indent in character units.
     */
    public short getDxcLeft()
    {
        return field_55_dxcLeft;
    }

    /**
     * Left indent in character units.
     */
    public void setDxcLeft(short field_55_dxcLeft)
    {
        this.field_55_dxcLeft = field_55_dxcLeft;
    }

    /**
     * First line indent in character units.
     */
    public short getDxcLeft1()
    {
        return field_56_dxcLeft1;
    }

    /**
     * First line indent in character units.
     */
    public void setDxcLeft1(short field_56_dxcLeft1)
    {
        this.field_56_dxcLeft1 = field_56_dxcLeft1;
    }

    /**
     * Vertical spacing before is automatic.
     */
    public byte getFDyaBeforeAuto()
    {
        return field_57_fDyaBeforeAuto;
    }

    /**
     * Vertical spacing before is automatic.
     */
    public void setFDyaBeforeAuto(byte field_57_fDyaBeforeAuto)
    {
        this.field_57_fDyaBeforeAuto = field_57_fDyaBeforeAuto;
    }

    /**
     * Vertical spacing after is automatic.
     */
    public byte getFDyaAfterAuto()
    {
        return field_58_fDyaAfterAuto;
    }

    /**
     * Vertical spacing after is automatic.
     */
    public void setFDyaAfterAuto(byte field_58_fDyaAfterAuto)
    {
        this.field_58_fDyaAfterAuto = field_58_fDyaAfterAuto;
    }

    /**
     * Get the dxaRight field for the PAP record.
     */
    public int getDxaRight()
    {
        return field_59_dxaRight;
    }

    /**
     * Set the dxaRight field for the PAP record.
     */
    public void setDxaRight(int field_59_dxaRight)
    {
        this.field_59_dxaRight = field_59_dxaRight;
    }

    /**
     * Get the dxaLeft field for the PAP record.
     */
    public int getDxaLeft()
    {
        return field_60_dxaLeft;
    }

    /**
     * Set the dxaLeft field for the PAP record.
     */
    public void setDxaLeft(int field_60_dxaLeft)
    {
        this.field_60_dxaLeft = field_60_dxaLeft;
    }

    /**
     * Get the dxaLeft1 field for the PAP record.
     */
    public int getDxaLeft1()
    {
        return field_61_dxaLeft1;
    }

    /**
     * Set the dxaLeft1 field for the PAP record.
     */
    public void setDxaLeft1(int field_61_dxaLeft1)
    {
        this.field_61_dxaLeft1 = field_61_dxaLeft1;
    }

    /**
     * Get the jc field for the PAP record.
     */
    public byte getJc()
    {
        return field_62_jc;
    }

    /**
     * Set the jc field for the PAP record.
     */
    public void setJc(byte field_62_jc)
    {
        this.field_62_jc = field_62_jc;
    }

    /**
     * Get the fNoAllowOverlap field for the PAP record.
     */
    public byte getFNoAllowOverlap()
    {
        return field_63_fNoAllowOverlap;
    }

    /**
     * Set the fNoAllowOverlap field for the PAP record.
     */
    public void setFNoAllowOverlap(byte field_63_fNoAllowOverlap)
    {
        this.field_63_fNoAllowOverlap = field_63_fNoAllowOverlap;
    }

    /**
     * Get the brcTop field for the PAP record.
     */
    public BorderCode getBrcTop()
    {
        return field_64_brcTop;
    }

    /**
     * Set the brcTop field for the PAP record.
     */
    public void setBrcTop(BorderCode field_64_brcTop)
    {
        this.field_64_brcTop = field_64_brcTop;
    }

    /**
     * Get the brcLeft field for the PAP record.
     */
    public BorderCode getBrcLeft()
    {
        return field_65_brcLeft;
    }

    /**
     * Set the brcLeft field for the PAP record.
     */
    public void setBrcLeft(BorderCode field_65_brcLeft)
    {
        this.field_65_brcLeft = field_65_brcLeft;
    }

    /**
     * Get the brcBottom field for the PAP record.
     */
    public BorderCode getBrcBottom()
    {
        return field_66_brcBottom;
    }

    /**
     * Set the brcBottom field for the PAP record.
     */
    public void setBrcBottom(BorderCode field_66_brcBottom)
    {
        this.field_66_brcBottom = field_66_brcBottom;
    }

    /**
     * Get the brcRight field for the PAP record.
     */
    public BorderCode getBrcRight()
    {
        return field_67_brcRight;
    }

    /**
     * Set the brcRight field for the PAP record.
     */
    public void setBrcRight(BorderCode field_67_brcRight)
    {
        this.field_67_brcRight = field_67_brcRight;
    }

    /**
     * Get the brcBetween field for the PAP record.
     */
    public BorderCode getBrcBetween()
    {
        return field_68_brcBetween;
    }

    /**
     * Set the brcBetween field for the PAP record.
     */
    public void setBrcBetween(BorderCode field_68_brcBetween)
    {
        this.field_68_brcBetween = field_68_brcBetween;
    }

    /**
     * Get the brcBar field for the PAP record.
     */
    public BorderCode getBrcBar()
    {
        return field_69_brcBar;
    }

    /**
     * Set the brcBar field for the PAP record.
     */
    public void setBrcBar(BorderCode field_69_brcBar)
    {
        this.field_69_brcBar = field_69_brcBar;
    }

    /**
     * Get the shd field for the PAP record.
     */
    public ShadingDescriptor getShd()
    {
        return field_70_shd;
    }

    /**
     * Set the shd field for the PAP record.
     */
    public void setShd(ShadingDescriptor field_70_shd)
    {
        this.field_70_shd = field_70_shd;
    }

    /**
     * Get the anld field for the PAP record.
     */
    public byte[] getAnld()
    {
        return field_71_anld;
    }

    /**
     * Set the anld field for the PAP record.
     */
    public void setAnld(byte[] field_71_anld)
    {
        this.field_71_anld = field_71_anld;
    }

    /**
     * Get the phe field for the PAP record.
     */
    public byte[] getPhe()
    {
        return field_72_phe;
    }

    /**
     * Set the phe field for the PAP record.
     */
    public void setPhe(byte[] field_72_phe)
    {
        this.field_72_phe = field_72_phe;
    }

    /**
     * Get the fPropRMark field for the PAP record.
     */
    public int getFPropRMark()
    {
        return field_73_fPropRMark;
    }

    /**
     * Set the fPropRMark field for the PAP record.
     */
    public void setFPropRMark(int field_73_fPropRMark)
    {
        this.field_73_fPropRMark = field_73_fPropRMark;
    }

    /**
     * Get the ibstPropRMark field for the PAP record.
     */
    public int getIbstPropRMark()
    {
        return field_74_ibstPropRMark;
    }

    /**
     * Set the ibstPropRMark field for the PAP record.
     */
    public void setIbstPropRMark(int field_74_ibstPropRMark)
    {
        this.field_74_ibstPropRMark = field_74_ibstPropRMark;
    }

    /**
     * Get the dttmPropRMark field for the PAP record.
     */
    public DateAndTime getDttmPropRMark()
    {
        return field_75_dttmPropRMark;
    }

    /**
     * Set the dttmPropRMark field for the PAP record.
     */
    public void setDttmPropRMark(DateAndTime field_75_dttmPropRMark)
    {
        this.field_75_dttmPropRMark = field_75_dttmPropRMark;
    }

    /**
     * Get the itbdMac field for the PAP record.
     */
    public int getItbdMac()
    {
        return field_76_itbdMac;
    }

    /**
     * Set the itbdMac field for the PAP record.
     */
    public void setItbdMac(int field_76_itbdMac)
    {
        this.field_76_itbdMac = field_76_itbdMac;
    }

    /**
     * Get the rgdxaTab field for the PAP record.
     */
    public int[] getRgdxaTab()
    {
        return field_77_rgdxaTab;
    }

    /**
     * Set the rgdxaTab field for the PAP record.
     */
    public void setRgdxaTab(int[] field_77_rgdxaTab)
    {
        this.field_77_rgdxaTab = field_77_rgdxaTab;
    }

    /**
     * Get the rgtbd field for the PAP record.
     */
    public byte[] getRgtbd()
    {
        return field_78_rgtbd;
    }

    /**
     * Set the rgtbd field for the PAP record.
     */
    public void setRgtbd(byte[] field_78_rgtbd)
    {
        this.field_78_rgtbd = field_78_rgtbd;
    }

    /**
     * Get the numrm field for the PAP record.
     */
    public byte[] getNumrm()
    {
        return field_79_numrm;
    }

    /**
     * Set the numrm field for the PAP record.
     */
    public void setNumrm(byte[] field_79_numrm)
    {
        this.field_79_numrm = field_79_numrm;
    }

    /**
     * Get the ptap field for the PAP record.
     */
    public byte[] getPtap()
    {
        return field_80_ptap;
    }

    /**
     * Set the ptap field for the PAP record.
     */
    public void setPtap(byte[] field_80_ptap)
    {
        this.field_80_ptap = field_80_ptap;
    }

    /**
     * Sets the fVertical field value.
     * 
     */
    public void setFVertical(boolean value)
    {
        field_40_fontAlign = (short)fVertical.setBoolean(field_40_fontAlign, value);

        
    }

    /**
     * 
     * @return  the fVertical field value.
     */
    public boolean isFVertical()
    {
        return fVertical.isSet(field_40_fontAlign);
        
    }

    /**
     * Sets the fBackward field value.
     * 
     */
    public void setFBackward(boolean value)
    {
        field_40_fontAlign = (short)fBackward.setBoolean(field_40_fontAlign, value);

        
    }

    /**
     * 
     * @return  the fBackward field value.
     */
    public boolean isFBackward()
    {
        return fBackward.isSet(field_40_fontAlign);
        
    }

    /**
     * Sets the fRotateFont field value.
     * 
     */
    public void setFRotateFont(boolean value)
    {
        field_40_fontAlign = (short)fRotateFont.setBoolean(field_40_fontAlign, value);

        
    }

    /**
     * 
     * @return  the fRotateFont field value.
     */
    public boolean isFRotateFont()
    {
        return fRotateFont.isSet(field_40_fontAlign);
        
    }


}  // END OF CLASS