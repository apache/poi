
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */


package org.apache.poi.hdf.model.hdftypes.definitions;



import org.apache.poi.util.BitField;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;
import org.apache.poi.util.HexDump;
import org.apache.poi.hdf.model.hdftypes.HDFType;

/**
 * Paragraph Properties.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author S. Ryan Ackley
 */
public abstract class PAPAbstractType
    implements HDFType
{

    private  int field_1_istd;
    private  byte field_2_jc;
    private  byte field_3_fKeep;
    private  byte field_4_fKeepFollow;
    private  byte field_5_fPageBreakBefore;
    private  byte field_6_fBrLnAbove;
    private  byte field_7_fBrLnBelow;
    private  byte field_8_pcVert;
    private  byte field_9_pcHorz;
    private  byte field_10_brcp;
    private  byte field_11_brcl;
    private  byte field_12_ilvl;
    private  byte field_13_fNoLnn;
    private  int field_14_ilfo;
    private  byte field_15_fSideBiSide;
    private  byte field_16_fNoAutoHyph;
    private  byte field_17_fWidowControl;
    private  int field_18_dxaRight;
    private  int field_19_dxaLeft;
    private  int field_20_dxaLeft1;
    private  short[] field_21_lspd;
    private  int field_22_dyaBefore;
    private  byte[] field_23_phe;
    private  byte field_24_fCrLf;
    private  byte field_25_fUsePgsuSettings;
    private  byte field_26_fAdjustRight;
    private  byte field_27_fKinsoku;
    private  byte field_28_fWordWrap;
    private  byte field_29_fOverflowPunct;
    private  byte field_30_fTopLinePunct;
    private  byte field_31_fAutoSpaceDE;
    private  byte field_32_fAtuoSpaceDN;
    private  int field_33_wAlignFont;
    private  byte field_34_fVertical;
    private  byte field_35_fBackward;
    private  byte field_36_fRotateFont;
    private  byte field_37_fInTable;
    private  byte field_38_fTtp;
    private  byte field_39_wr;
    private  byte field_40_fLocked;
    private  byte[] field_41_ptap;
    private  int field_42_dxaAbs;
    private  int field_43_dyaAbs;
    private  int field_44_dxaWidth;
    private  short[] field_45_brcTop;
    private  short[] field_46_brcLeft;
    private  short[] field_47_brcBottom;
    private  short[] field_48_brcRight;
    private  short[] field_49_brcBetween;
    private  short[] field_50_brcBar;
    private  int field_51_dxaFromText;
    private  int field_52_dyaFromText;
    private  int field_53_dyaHeight;
    private  byte field_54_fMinHeight;
    private  byte[] field_55_shd;
    private  byte[] field_56_dcs;
    private  byte field_57_lvl;
    private  byte field_58_fNumRMIns;
    private  byte[] field_59_anld;
    private  int field_60_fPropRMark;
    private  int field_61_ibstPropRMark;
    private  byte[] field_62_dttmPropRMark;
    private  byte[] field_63_numrm;
    private  int field_64_itbdMac;
    private  byte[] field_65_rgdxaTab;
    private  byte[] field_66_rgtbd;


    public PAPAbstractType()
    {

    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getSize()
    {
        return 4 + 2 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 2 + 1 + 1 + 1 + 4 + 4 + 4 + 4 + 4 + 12 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 2 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 2 + 1 + 2 + 2 + 1 + 1 + 84 + 1 + 2 + 4 + 128 + 2 + 128 + 128;
    }



    /**
     * Get the istd field for the PAP record.
     */
    public int getIstd()
    {
        return field_1_istd;
    }

    /**
     * Set the istd field for the PAP record.
     */
    public void setIstd(int field_1_istd)
    {
        this.field_1_istd = field_1_istd;
    }

    /**
     * Get the jc field for the PAP record.
     */
    public byte getJc()
    {
        return field_2_jc;
    }

    /**
     * Set the jc field for the PAP record.
     */
    public void setJc(byte field_2_jc)
    {
        this.field_2_jc = field_2_jc;
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
     * Get the fBrLnAbove field for the PAP record.
     */
    public byte getFBrLnAbove()
    {
        return field_6_fBrLnAbove;
    }

    /**
     * Set the fBrLnAbove field for the PAP record.
     */
    public void setFBrLnAbove(byte field_6_fBrLnAbove)
    {
        this.field_6_fBrLnAbove = field_6_fBrLnAbove;
    }

    /**
     * Get the fBrLnBelow field for the PAP record.
     */
    public byte getFBrLnBelow()
    {
        return field_7_fBrLnBelow;
    }

    /**
     * Set the fBrLnBelow field for the PAP record.
     */
    public void setFBrLnBelow(byte field_7_fBrLnBelow)
    {
        this.field_7_fBrLnBelow = field_7_fBrLnBelow;
    }

    /**
     * Get the pcVert field for the PAP record.
     */
    public byte getPcVert()
    {
        return field_8_pcVert;
    }

    /**
     * Set the pcVert field for the PAP record.
     */
    public void setPcVert(byte field_8_pcVert)
    {
        this.field_8_pcVert = field_8_pcVert;
    }

    /**
     * Get the pcHorz field for the PAP record.
     */
    public byte getPcHorz()
    {
        return field_9_pcHorz;
    }

    /**
     * Set the pcHorz field for the PAP record.
     */
    public void setPcHorz(byte field_9_pcHorz)
    {
        this.field_9_pcHorz = field_9_pcHorz;
    }

    /**
     * Get the brcp field for the PAP record.
     */
    public byte getBrcp()
    {
        return field_10_brcp;
    }

    /**
     * Set the brcp field for the PAP record.
     */
    public void setBrcp(byte field_10_brcp)
    {
        this.field_10_brcp = field_10_brcp;
    }

    /**
     * Get the brcl field for the PAP record.
     */
    public byte getBrcl()
    {
        return field_11_brcl;
    }

    /**
     * Set the brcl field for the PAP record.
     */
    public void setBrcl(byte field_11_brcl)
    {
        this.field_11_brcl = field_11_brcl;
    }

    /**
     * Get the ilvl field for the PAP record.
     */
    public byte getIlvl()
    {
        return field_12_ilvl;
    }

    /**
     * Set the ilvl field for the PAP record.
     */
    public void setIlvl(byte field_12_ilvl)
    {
        this.field_12_ilvl = field_12_ilvl;
    }

    /**
     * Get the fNoLnn field for the PAP record.
     */
    public byte getFNoLnn()
    {
        return field_13_fNoLnn;
    }

    /**
     * Set the fNoLnn field for the PAP record.
     */
    public void setFNoLnn(byte field_13_fNoLnn)
    {
        this.field_13_fNoLnn = field_13_fNoLnn;
    }

    /**
     * Get the ilfo field for the PAP record.
     */
    public int getIlfo()
    {
        return field_14_ilfo;
    }

    /**
     * Set the ilfo field for the PAP record.
     */
    public void setIlfo(int field_14_ilfo)
    {
        this.field_14_ilfo = field_14_ilfo;
    }

    /**
     * Get the fSideBiSide field for the PAP record.
     */
    public byte getFSideBiSide()
    {
        return field_15_fSideBiSide;
    }

    /**
     * Set the fSideBiSide field for the PAP record.
     */
    public void setFSideBiSide(byte field_15_fSideBiSide)
    {
        this.field_15_fSideBiSide = field_15_fSideBiSide;
    }

    /**
     * Get the fNoAutoHyph field for the PAP record.
     */
    public byte getFNoAutoHyph()
    {
        return field_16_fNoAutoHyph;
    }

    /**
     * Set the fNoAutoHyph field for the PAP record.
     */
    public void setFNoAutoHyph(byte field_16_fNoAutoHyph)
    {
        this.field_16_fNoAutoHyph = field_16_fNoAutoHyph;
    }

    /**
     * Get the fWidowControl field for the PAP record.
     */
    public byte getFWidowControl()
    {
        return field_17_fWidowControl;
    }

    /**
     * Set the fWidowControl field for the PAP record.
     */
    public void setFWidowControl(byte field_17_fWidowControl)
    {
        this.field_17_fWidowControl = field_17_fWidowControl;
    }

    /**
     * Get the dxaRight field for the PAP record.
     */
    public int getDxaRight()
    {
        return field_18_dxaRight;
    }

    /**
     * Set the dxaRight field for the PAP record.
     */
    public void setDxaRight(int field_18_dxaRight)
    {
        this.field_18_dxaRight = field_18_dxaRight;
    }

    /**
     * Get the dxaLeft field for the PAP record.
     */
    public int getDxaLeft()
    {
        return field_19_dxaLeft;
    }

    /**
     * Set the dxaLeft field for the PAP record.
     */
    public void setDxaLeft(int field_19_dxaLeft)
    {
        this.field_19_dxaLeft = field_19_dxaLeft;
    }

    /**
     * Get the dxaLeft1 field for the PAP record.
     */
    public int getDxaLeft1()
    {
        return field_20_dxaLeft1;
    }

    /**
     * Set the dxaLeft1 field for the PAP record.
     */
    public void setDxaLeft1(int field_20_dxaLeft1)
    {
        this.field_20_dxaLeft1 = field_20_dxaLeft1;
    }

    /**
     * Get the lspd field for the PAP record.
     */
    public short[] getLspd()
    {
        return field_21_lspd;
    }

    /**
     * Set the lspd field for the PAP record.
     */
    public void setLspd(short[] field_21_lspd)
    {
        this.field_21_lspd = field_21_lspd;
    }

    /**
     * Get the dyaBefore field for the PAP record.
     */
    public int getDyaBefore()
    {
        return field_22_dyaBefore;
    }

    /**
     * Set the dyaBefore field for the PAP record.
     */
    public void setDyaBefore(int field_22_dyaBefore)
    {
        this.field_22_dyaBefore = field_22_dyaBefore;
    }

    /**
     * Get the phe field for the PAP record.
     */
    public byte[] getPhe()
    {
        return field_23_phe;
    }

    /**
     * Set the phe field for the PAP record.
     */
    public void setPhe(byte[] field_23_phe)
    {
        this.field_23_phe = field_23_phe;
    }

    /**
     * Get the fCrLf field for the PAP record.
     */
    public byte getFCrLf()
    {
        return field_24_fCrLf;
    }

    /**
     * Set the fCrLf field for the PAP record.
     */
    public void setFCrLf(byte field_24_fCrLf)
    {
        this.field_24_fCrLf = field_24_fCrLf;
    }

    /**
     * Get the fUsePgsuSettings field for the PAP record.
     */
    public byte getFUsePgsuSettings()
    {
        return field_25_fUsePgsuSettings;
    }

    /**
     * Set the fUsePgsuSettings field for the PAP record.
     */
    public void setFUsePgsuSettings(byte field_25_fUsePgsuSettings)
    {
        this.field_25_fUsePgsuSettings = field_25_fUsePgsuSettings;
    }

    /**
     * Get the fAdjustRight field for the PAP record.
     */
    public byte getFAdjustRight()
    {
        return field_26_fAdjustRight;
    }

    /**
     * Set the fAdjustRight field for the PAP record.
     */
    public void setFAdjustRight(byte field_26_fAdjustRight)
    {
        this.field_26_fAdjustRight = field_26_fAdjustRight;
    }

    /**
     * Get the fKinsoku field for the PAP record.
     */
    public byte getFKinsoku()
    {
        return field_27_fKinsoku;
    }

    /**
     * Set the fKinsoku field for the PAP record.
     */
    public void setFKinsoku(byte field_27_fKinsoku)
    {
        this.field_27_fKinsoku = field_27_fKinsoku;
    }

    /**
     * Get the fWordWrap field for the PAP record.
     */
    public byte getFWordWrap()
    {
        return field_28_fWordWrap;
    }

    /**
     * Set the fWordWrap field for the PAP record.
     */
    public void setFWordWrap(byte field_28_fWordWrap)
    {
        this.field_28_fWordWrap = field_28_fWordWrap;
    }

    /**
     * Get the fOverflowPunct field for the PAP record.
     */
    public byte getFOverflowPunct()
    {
        return field_29_fOverflowPunct;
    }

    /**
     * Set the fOverflowPunct field for the PAP record.
     */
    public void setFOverflowPunct(byte field_29_fOverflowPunct)
    {
        this.field_29_fOverflowPunct = field_29_fOverflowPunct;
    }

    /**
     * Get the fTopLinePunct field for the PAP record.
     */
    public byte getFTopLinePunct()
    {
        return field_30_fTopLinePunct;
    }

    /**
     * Set the fTopLinePunct field for the PAP record.
     */
    public void setFTopLinePunct(byte field_30_fTopLinePunct)
    {
        this.field_30_fTopLinePunct = field_30_fTopLinePunct;
    }

    /**
     * Get the fAutoSpaceDE field for the PAP record.
     */
    public byte getFAutoSpaceDE()
    {
        return field_31_fAutoSpaceDE;
    }

    /**
     * Set the fAutoSpaceDE field for the PAP record.
     */
    public void setFAutoSpaceDE(byte field_31_fAutoSpaceDE)
    {
        this.field_31_fAutoSpaceDE = field_31_fAutoSpaceDE;
    }

    /**
     * Get the fAtuoSpaceDN field for the PAP record.
     */
    public byte getFAtuoSpaceDN()
    {
        return field_32_fAtuoSpaceDN;
    }

    /**
     * Set the fAtuoSpaceDN field for the PAP record.
     */
    public void setFAtuoSpaceDN(byte field_32_fAtuoSpaceDN)
    {
        this.field_32_fAtuoSpaceDN = field_32_fAtuoSpaceDN;
    }

    /**
     * Get the wAlignFont field for the PAP record.
     */
    public int getWAlignFont()
    {
        return field_33_wAlignFont;
    }

    /**
     * Set the wAlignFont field for the PAP record.
     */
    public void setWAlignFont(int field_33_wAlignFont)
    {
        this.field_33_wAlignFont = field_33_wAlignFont;
    }

    /**
     * Get the fVertical field for the PAP record.
     */
    public byte getFVertical()
    {
        return field_34_fVertical;
    }

    /**
     * Set the fVertical field for the PAP record.
     */
    public void setFVertical(byte field_34_fVertical)
    {
        this.field_34_fVertical = field_34_fVertical;
    }

    /**
     * Get the fBackward field for the PAP record.
     */
    public byte getFBackward()
    {
        return field_35_fBackward;
    }

    /**
     * Set the fBackward field for the PAP record.
     */
    public void setFBackward(byte field_35_fBackward)
    {
        this.field_35_fBackward = field_35_fBackward;
    }

    /**
     * Get the fRotateFont field for the PAP record.
     */
    public byte getFRotateFont()
    {
        return field_36_fRotateFont;
    }

    /**
     * Set the fRotateFont field for the PAP record.
     */
    public void setFRotateFont(byte field_36_fRotateFont)
    {
        this.field_36_fRotateFont = field_36_fRotateFont;
    }

    /**
     * Get the fInTable field for the PAP record.
     */
    public byte getFInTable()
    {
        return field_37_fInTable;
    }

    /**
     * Set the fInTable field for the PAP record.
     */
    public void setFInTable(byte field_37_fInTable)
    {
        this.field_37_fInTable = field_37_fInTable;
    }

    /**
     * Get the fTtp field for the PAP record.
     */
    public byte getFTtp()
    {
        return field_38_fTtp;
    }

    /**
     * Set the fTtp field for the PAP record.
     */
    public void setFTtp(byte field_38_fTtp)
    {
        this.field_38_fTtp = field_38_fTtp;
    }

    /**
     * Get the wr field for the PAP record.
     */
    public byte getWr()
    {
        return field_39_wr;
    }

    /**
     * Set the wr field for the PAP record.
     */
    public void setWr(byte field_39_wr)
    {
        this.field_39_wr = field_39_wr;
    }

    /**
     * Get the fLocked field for the PAP record.
     */
    public byte getFLocked()
    {
        return field_40_fLocked;
    }

    /**
     * Set the fLocked field for the PAP record.
     */
    public void setFLocked(byte field_40_fLocked)
    {
        this.field_40_fLocked = field_40_fLocked;
    }

    /**
     * Get the ptap field for the PAP record.
     */
    public byte[] getPtap()
    {
        return field_41_ptap;
    }

    /**
     * Set the ptap field for the PAP record.
     */
    public void setPtap(byte[] field_41_ptap)
    {
        this.field_41_ptap = field_41_ptap;
    }

    /**
     * Get the dxaAbs field for the PAP record.
     */
    public int getDxaAbs()
    {
        return field_42_dxaAbs;
    }

    /**
     * Set the dxaAbs field for the PAP record.
     */
    public void setDxaAbs(int field_42_dxaAbs)
    {
        this.field_42_dxaAbs = field_42_dxaAbs;
    }

    /**
     * Get the dyaAbs field for the PAP record.
     */
    public int getDyaAbs()
    {
        return field_43_dyaAbs;
    }

    /**
     * Set the dyaAbs field for the PAP record.
     */
    public void setDyaAbs(int field_43_dyaAbs)
    {
        this.field_43_dyaAbs = field_43_dyaAbs;
    }

    /**
     * Get the dxaWidth field for the PAP record.
     */
    public int getDxaWidth()
    {
        return field_44_dxaWidth;
    }

    /**
     * Set the dxaWidth field for the PAP record.
     */
    public void setDxaWidth(int field_44_dxaWidth)
    {
        this.field_44_dxaWidth = field_44_dxaWidth;
    }

    /**
     * Get the brcTop field for the PAP record.
     */
    public short[] getBrcTop()
    {
        return field_45_brcTop;
    }

    /**
     * Set the brcTop field for the PAP record.
     */
    public void setBrcTop(short[] field_45_brcTop)
    {
        this.field_45_brcTop = field_45_brcTop;
    }

    /**
     * Get the brcLeft field for the PAP record.
     */
    public short[] getBrcLeft()
    {
        return field_46_brcLeft;
    }

    /**
     * Set the brcLeft field for the PAP record.
     */
    public void setBrcLeft(short[] field_46_brcLeft)
    {
        this.field_46_brcLeft = field_46_brcLeft;
    }

    /**
     * Get the brcBottom field for the PAP record.
     */
    public short[] getBrcBottom()
    {
        return field_47_brcBottom;
    }

    /**
     * Set the brcBottom field for the PAP record.
     */
    public void setBrcBottom(short[] field_47_brcBottom)
    {
        this.field_47_brcBottom = field_47_brcBottom;
    }

    /**
     * Get the brcRight field for the PAP record.
     */
    public short[] getBrcRight()
    {
        return field_48_brcRight;
    }

    /**
     * Set the brcRight field for the PAP record.
     */
    public void setBrcRight(short[] field_48_brcRight)
    {
        this.field_48_brcRight = field_48_brcRight;
    }

    /**
     * Get the brcBetween field for the PAP record.
     */
    public short[] getBrcBetween()
    {
        return field_49_brcBetween;
    }

    /**
     * Set the brcBetween field for the PAP record.
     */
    public void setBrcBetween(short[] field_49_brcBetween)
    {
        this.field_49_brcBetween = field_49_brcBetween;
    }

    /**
     * Get the brcBar field for the PAP record.
     */
    public short[] getBrcBar()
    {
        return field_50_brcBar;
    }

    /**
     * Set the brcBar field for the PAP record.
     */
    public void setBrcBar(short[] field_50_brcBar)
    {
        this.field_50_brcBar = field_50_brcBar;
    }

    /**
     * Get the dxaFromText field for the PAP record.
     */
    public int getDxaFromText()
    {
        return field_51_dxaFromText;
    }

    /**
     * Set the dxaFromText field for the PAP record.
     */
    public void setDxaFromText(int field_51_dxaFromText)
    {
        this.field_51_dxaFromText = field_51_dxaFromText;
    }

    /**
     * Get the dyaFromText field for the PAP record.
     */
    public int getDyaFromText()
    {
        return field_52_dyaFromText;
    }

    /**
     * Set the dyaFromText field for the PAP record.
     */
    public void setDyaFromText(int field_52_dyaFromText)
    {
        this.field_52_dyaFromText = field_52_dyaFromText;
    }

    /**
     * Get the dyaHeight field for the PAP record.
     */
    public int getDyaHeight()
    {
        return field_53_dyaHeight;
    }

    /**
     * Set the dyaHeight field for the PAP record.
     */
    public void setDyaHeight(int field_53_dyaHeight)
    {
        this.field_53_dyaHeight = field_53_dyaHeight;
    }

    /**
     * Get the fMinHeight field for the PAP record.
     */
    public byte getFMinHeight()
    {
        return field_54_fMinHeight;
    }

    /**
     * Set the fMinHeight field for the PAP record.
     */
    public void setFMinHeight(byte field_54_fMinHeight)
    {
        this.field_54_fMinHeight = field_54_fMinHeight;
    }

    /**
     * Get the shd field for the PAP record.
     */
    public byte[] getShd()
    {
        return field_55_shd;
    }

    /**
     * Set the shd field for the PAP record.
     */
    public void setShd(byte[] field_55_shd)
    {
        this.field_55_shd = field_55_shd;
    }

    /**
     * Get the dcs field for the PAP record.
     */
    public byte[] getDcs()
    {
        return field_56_dcs;
    }

    /**
     * Set the dcs field for the PAP record.
     */
    public void setDcs(byte[] field_56_dcs)
    {
        this.field_56_dcs = field_56_dcs;
    }

    /**
     * Get the lvl field for the PAP record.
     */
    public byte getLvl()
    {
        return field_57_lvl;
    }

    /**
     * Set the lvl field for the PAP record.
     */
    public void setLvl(byte field_57_lvl)
    {
        this.field_57_lvl = field_57_lvl;
    }

    /**
     * Get the fNumRMIns field for the PAP record.
     */
    public byte getFNumRMIns()
    {
        return field_58_fNumRMIns;
    }

    /**
     * Set the fNumRMIns field for the PAP record.
     */
    public void setFNumRMIns(byte field_58_fNumRMIns)
    {
        this.field_58_fNumRMIns = field_58_fNumRMIns;
    }

    /**
     * Get the anld field for the PAP record.
     */
    public byte[] getAnld()
    {
        return field_59_anld;
    }

    /**
     * Set the anld field for the PAP record.
     */
    public void setAnld(byte[] field_59_anld)
    {
        this.field_59_anld = field_59_anld;
    }

    /**
     * Get the fPropRMark field for the PAP record.
     */
    public int getFPropRMark()
    {
        return field_60_fPropRMark;
    }

    /**
     * Set the fPropRMark field for the PAP record.
     */
    public void setFPropRMark(int field_60_fPropRMark)
    {
        this.field_60_fPropRMark = field_60_fPropRMark;
    }

    /**
     * Get the ibstPropRMark field for the PAP record.
     */
    public int getIbstPropRMark()
    {
        return field_61_ibstPropRMark;
    }

    /**
     * Set the ibstPropRMark field for the PAP record.
     */
    public void setIbstPropRMark(int field_61_ibstPropRMark)
    {
        this.field_61_ibstPropRMark = field_61_ibstPropRMark;
    }

    /**
     * Get the dttmPropRMark field for the PAP record.
     */
    public byte[] getDttmPropRMark()
    {
        return field_62_dttmPropRMark;
    }

    /**
     * Set the dttmPropRMark field for the PAP record.
     */
    public void setDttmPropRMark(byte[] field_62_dttmPropRMark)
    {
        this.field_62_dttmPropRMark = field_62_dttmPropRMark;
    }

    /**
     * Get the numrm field for the PAP record.
     */
    public byte[] getNumrm()
    {
        return field_63_numrm;
    }

    /**
     * Set the numrm field for the PAP record.
     */
    public void setNumrm(byte[] field_63_numrm)
    {
        this.field_63_numrm = field_63_numrm;
    }

    /**
     * Get the itbdMac field for the PAP record.
     */
    public int getItbdMac()
    {
        return field_64_itbdMac;
    }

    /**
     * Set the itbdMac field for the PAP record.
     */
    public void setItbdMac(int field_64_itbdMac)
    {
        this.field_64_itbdMac = field_64_itbdMac;
    }

    /**
     * Get the rgdxaTab field for the PAP record.
     */
    public byte[] getRgdxaTab()
    {
        return field_65_rgdxaTab;
    }

    /**
     * Set the rgdxaTab field for the PAP record.
     */
    public void setRgdxaTab(byte[] field_65_rgdxaTab)
    {
        this.field_65_rgdxaTab = field_65_rgdxaTab;
    }

    /**
     * Get the rgtbd field for the PAP record.
     */
    public byte[] getRgtbd()
    {
        return field_66_rgtbd;
    }

    /**
     * Set the rgtbd field for the PAP record.
     */
    public void setRgtbd(byte[] field_66_rgtbd)
    {
        this.field_66_rgtbd = field_66_rgtbd;
    }


}  // END OF CLASS




