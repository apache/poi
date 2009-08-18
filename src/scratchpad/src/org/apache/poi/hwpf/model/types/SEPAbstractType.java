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

/**
 * Section Properties.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.
 *
 * @author S. Ryan Ackley
 */
public abstract class SEPAbstractType implements HDFType {

    protected  byte field_1_bkc;
    protected  boolean field_2_fTitlePage;
    protected  boolean field_3_fAutoPgn;
    protected  byte field_4_nfcPgn;
    protected  boolean field_5_fUnlocked;
    protected  byte field_6_cnsPgn;
    protected  boolean field_7_fPgnRestart;
    protected  boolean field_8_fEndNote;
    protected  byte field_9_lnc;
    protected  byte field_10_grpfIhdt;
    protected  int field_11_nLnnMod;
    protected  int field_12_dxaLnn;
    protected  int field_13_dxaPgn;
    protected  int field_14_dyaPgn;
    protected  boolean field_15_fLBetween;
    protected  byte field_16_vjc;
    protected  int field_17_dmBinFirst;
    protected  int field_18_dmBinOther;
    protected  int field_19_dmPaperReq;
    protected  BorderCode field_20_brcTop;
    protected  BorderCode field_21_brcLeft;
    protected  BorderCode field_22_brcBottom;
    protected  BorderCode field_23_brcRight;
    protected  boolean field_24_fPropMark;
    protected  int field_25_ibstPropRMark;
    protected  DateAndTime field_26_dttmPropRMark;
    protected  int field_27_dxtCharSpace;
    protected  int field_28_dyaLinePitch;
    protected  int field_29_clm;
    protected  int field_30_unused2;
    protected  byte field_31_dmOrientPage;
    protected  byte field_32_iHeadingPgn;
    protected  int field_33_pgnStart;
    protected  int field_34_lnnMin;
    protected  int field_35_wTextFlow;
    protected  short field_36_unused3;
    protected  int field_37_pgbProp;
    protected  short field_38_unused4;
    protected  int field_39_xaPage;
    protected  int field_40_yaPage;
    protected  int field_41_xaPageNUp;
    protected  int field_42_yaPageNUp;
    protected  int field_43_dxaLeft;
    protected  int field_44_dxaRight;
    protected  int field_45_dyaTop;
    protected  int field_46_dyaBottom;
    protected  int field_47_dzaGutter;
    protected  int field_48_dyaHdrTop;
    protected  int field_49_dyaHdrBottom;
    protected  int field_50_ccolM1;
    protected  boolean field_51_fEvenlySpaced;
    protected  byte field_52_unused5;
    protected  int field_53_dxaColumns;
    protected  int[] field_54_rgdxaColumn;
    protected  int field_55_dxaColumnWidth;
    protected  byte field_56_dmOrientFirst;
    protected  byte field_57_fLayout;
    protected  short field_58_unused6;
    protected  byte[] field_59_olstAnm;


    public SEPAbstractType()
    {

    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getSize()
    {
        return 4 +  + 1 + 0 + 0 + 1 + 0 + 1 + 0 + 0 + 1 + 1 + 2 + 4 + 2 + 2 + 0 + 1 + 2 + 2 + 2 + 4 + 4 + 4 + 4 + 0 + 2 + 4 + 4 + 4 + 2 + 2 + 1 + 1 + 2 + 2 + 2 + 2 + 2 + 2 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 2 + 0 + 1 + 4 + 356 + 4 + 1 + 1 + 2 + 212;
    }



    /**
     * Get the bkc field for the SEP record.
     */
    public byte getBkc()
    {
        return field_1_bkc;
    }

    /**
     * Set the bkc field for the SEP record.
     */
    public void setBkc(byte field_1_bkc)
    {
        this.field_1_bkc = field_1_bkc;
    }

    /**
     * Get the fTitlePage field for the SEP record.
     */
    public boolean getFTitlePage()
    {
        return field_2_fTitlePage;
    }

    /**
     * Set the fTitlePage field for the SEP record.
     */
    public void setFTitlePage(boolean field_2_fTitlePage)
    {
        this.field_2_fTitlePage = field_2_fTitlePage;
    }

    /**
     * Get the fAutoPgn field for the SEP record.
     */
    public boolean getFAutoPgn()
    {
        return field_3_fAutoPgn;
    }

    /**
     * Set the fAutoPgn field for the SEP record.
     */
    public void setFAutoPgn(boolean field_3_fAutoPgn)
    {
        this.field_3_fAutoPgn = field_3_fAutoPgn;
    }

    /**
     * Get the nfcPgn field for the SEP record.
     */
    public byte getNfcPgn()
    {
        return field_4_nfcPgn;
    }

    /**
     * Set the nfcPgn field for the SEP record.
     */
    public void setNfcPgn(byte field_4_nfcPgn)
    {
        this.field_4_nfcPgn = field_4_nfcPgn;
    }

    /**
     * Get the fUnlocked field for the SEP record.
     */
    public boolean getFUnlocked()
    {
        return field_5_fUnlocked;
    }

    /**
     * Set the fUnlocked field for the SEP record.
     */
    public void setFUnlocked(boolean field_5_fUnlocked)
    {
        this.field_5_fUnlocked = field_5_fUnlocked;
    }

    /**
     * Get the cnsPgn field for the SEP record.
     */
    public byte getCnsPgn()
    {
        return field_6_cnsPgn;
    }

    /**
     * Set the cnsPgn field for the SEP record.
     */
    public void setCnsPgn(byte field_6_cnsPgn)
    {
        this.field_6_cnsPgn = field_6_cnsPgn;
    }

    /**
     * Get the fPgnRestart field for the SEP record.
     */
    public boolean getFPgnRestart()
    {
        return field_7_fPgnRestart;
    }

    /**
     * Set the fPgnRestart field for the SEP record.
     */
    public void setFPgnRestart(boolean field_7_fPgnRestart)
    {
        this.field_7_fPgnRestart = field_7_fPgnRestart;
    }

    /**
     * Get the fEndNote field for the SEP record.
     */
    public boolean getFEndNote()
    {
        return field_8_fEndNote;
    }

    /**
     * Set the fEndNote field for the SEP record.
     */
    public void setFEndNote(boolean field_8_fEndNote)
    {
        this.field_8_fEndNote = field_8_fEndNote;
    }

    /**
     * Get the lnc field for the SEP record.
     */
    public byte getLnc()
    {
        return field_9_lnc;
    }

    /**
     * Set the lnc field for the SEP record.
     */
    public void setLnc(byte field_9_lnc)
    {
        this.field_9_lnc = field_9_lnc;
    }

    /**
     * Get the grpfIhdt field for the SEP record.
     */
    public byte getGrpfIhdt()
    {
        return field_10_grpfIhdt;
    }

    /**
     * Set the grpfIhdt field for the SEP record.
     */
    public void setGrpfIhdt(byte field_10_grpfIhdt)
    {
        this.field_10_grpfIhdt = field_10_grpfIhdt;
    }

    /**
     * Get the nLnnMod field for the SEP record.
     */
    public int getNLnnMod()
    {
        return field_11_nLnnMod;
    }

    /**
     * Set the nLnnMod field for the SEP record.
     */
    public void setNLnnMod(int field_11_nLnnMod)
    {
        this.field_11_nLnnMod = field_11_nLnnMod;
    }

    /**
     * Get the dxaLnn field for the SEP record.
     */
    public int getDxaLnn()
    {
        return field_12_dxaLnn;
    }

    /**
     * Set the dxaLnn field for the SEP record.
     */
    public void setDxaLnn(int field_12_dxaLnn)
    {
        this.field_12_dxaLnn = field_12_dxaLnn;
    }

    /**
     * Get the dxaPgn field for the SEP record.
     */
    public int getDxaPgn()
    {
        return field_13_dxaPgn;
    }

    /**
     * Set the dxaPgn field for the SEP record.
     */
    public void setDxaPgn(int field_13_dxaPgn)
    {
        this.field_13_dxaPgn = field_13_dxaPgn;
    }

    /**
     * Get the dyaPgn field for the SEP record.
     */
    public int getDyaPgn()
    {
        return field_14_dyaPgn;
    }

    /**
     * Set the dyaPgn field for the SEP record.
     */
    public void setDyaPgn(int field_14_dyaPgn)
    {
        this.field_14_dyaPgn = field_14_dyaPgn;
    }

    /**
     * Get the fLBetween field for the SEP record.
     */
    public boolean getFLBetween()
    {
        return field_15_fLBetween;
    }

    /**
     * Set the fLBetween field for the SEP record.
     */
    public void setFLBetween(boolean field_15_fLBetween)
    {
        this.field_15_fLBetween = field_15_fLBetween;
    }

    /**
     * Get the vjc field for the SEP record.
     */
    public byte getVjc()
    {
        return field_16_vjc;
    }

    /**
     * Set the vjc field for the SEP record.
     */
    public void setVjc(byte field_16_vjc)
    {
        this.field_16_vjc = field_16_vjc;
    }

    /**
     * Get the dmBinFirst field for the SEP record.
     */
    public int getDmBinFirst()
    {
        return field_17_dmBinFirst;
    }

    /**
     * Set the dmBinFirst field for the SEP record.
     */
    public void setDmBinFirst(int field_17_dmBinFirst)
    {
        this.field_17_dmBinFirst = field_17_dmBinFirst;
    }

    /**
     * Get the dmBinOther field for the SEP record.
     */
    public int getDmBinOther()
    {
        return field_18_dmBinOther;
    }

    /**
     * Set the dmBinOther field for the SEP record.
     */
    public void setDmBinOther(int field_18_dmBinOther)
    {
        this.field_18_dmBinOther = field_18_dmBinOther;
    }

    /**
     * Get the dmPaperReq field for the SEP record.
     */
    public int getDmPaperReq()
    {
        return field_19_dmPaperReq;
    }

    /**
     * Set the dmPaperReq field for the SEP record.
     */
    public void setDmPaperReq(int field_19_dmPaperReq)
    {
        this.field_19_dmPaperReq = field_19_dmPaperReq;
    }

    /**
     * Get the brcTop field for the SEP record.
     */
    public BorderCode getBrcTop()
    {
        return field_20_brcTop;
    }

    /**
     * Set the brcTop field for the SEP record.
     */
    public void setBrcTop(BorderCode field_20_brcTop)
    {
        this.field_20_brcTop = field_20_brcTop;
    }

    /**
     * Get the brcLeft field for the SEP record.
     */
    public BorderCode getBrcLeft()
    {
        return field_21_brcLeft;
    }

    /**
     * Set the brcLeft field for the SEP record.
     */
    public void setBrcLeft(BorderCode field_21_brcLeft)
    {
        this.field_21_brcLeft = field_21_brcLeft;
    }

    /**
     * Get the brcBottom field for the SEP record.
     */
    public BorderCode getBrcBottom()
    {
        return field_22_brcBottom;
    }

    /**
     * Set the brcBottom field for the SEP record.
     */
    public void setBrcBottom(BorderCode field_22_brcBottom)
    {
        this.field_22_brcBottom = field_22_brcBottom;
    }

    /**
     * Get the brcRight field for the SEP record.
     */
    public BorderCode getBrcRight()
    {
        return field_23_brcRight;
    }

    /**
     * Set the brcRight field for the SEP record.
     */
    public void setBrcRight(BorderCode field_23_brcRight)
    {
        this.field_23_brcRight = field_23_brcRight;
    }

    /**
     * Get the fPropMark field for the SEP record.
     */
    public boolean getFPropMark()
    {
        return field_24_fPropMark;
    }

    /**
     * Set the fPropMark field for the SEP record.
     */
    public void setFPropMark(boolean field_24_fPropMark)
    {
        this.field_24_fPropMark = field_24_fPropMark;
    }

    /**
     * Get the ibstPropRMark field for the SEP record.
     */
    public int getIbstPropRMark()
    {
        return field_25_ibstPropRMark;
    }

    /**
     * Set the ibstPropRMark field for the SEP record.
     */
    public void setIbstPropRMark(int field_25_ibstPropRMark)
    {
        this.field_25_ibstPropRMark = field_25_ibstPropRMark;
    }

    /**
     * Get the dttmPropRMark field for the SEP record.
     */
    public DateAndTime getDttmPropRMark()
    {
        return field_26_dttmPropRMark;
    }

    /**
     * Set the dttmPropRMark field for the SEP record.
     */
    public void setDttmPropRMark(DateAndTime field_26_dttmPropRMark)
    {
        this.field_26_dttmPropRMark = field_26_dttmPropRMark;
    }

    /**
     * Get the dxtCharSpace field for the SEP record.
     */
    public int getDxtCharSpace()
    {
        return field_27_dxtCharSpace;
    }

    /**
     * Set the dxtCharSpace field for the SEP record.
     */
    public void setDxtCharSpace(int field_27_dxtCharSpace)
    {
        this.field_27_dxtCharSpace = field_27_dxtCharSpace;
    }

    /**
     * Get the dyaLinePitch field for the SEP record.
     */
    public int getDyaLinePitch()
    {
        return field_28_dyaLinePitch;
    }

    /**
     * Set the dyaLinePitch field for the SEP record.
     */
    public void setDyaLinePitch(int field_28_dyaLinePitch)
    {
        this.field_28_dyaLinePitch = field_28_dyaLinePitch;
    }

    /**
     * Get the clm field for the SEP record.
     */
    public int getClm()
    {
        return field_29_clm;
    }

    /**
     * Set the clm field for the SEP record.
     */
    public void setClm(int field_29_clm)
    {
        this.field_29_clm = field_29_clm;
    }

    /**
     * Get the unused2 field for the SEP record.
     */
    public int getUnused2()
    {
        return field_30_unused2;
    }

    /**
     * Set the unused2 field for the SEP record.
     */
    public void setUnused2(int field_30_unused2)
    {
        this.field_30_unused2 = field_30_unused2;
    }

    /**
     * Get the dmOrientPage field for the SEP record.
     */
    public byte getDmOrientPage()
    {
        return field_31_dmOrientPage;
    }

    /**
     * Set the dmOrientPage field for the SEP record.
     */
    public void setDmOrientPage(byte field_31_dmOrientPage)
    {
        this.field_31_dmOrientPage = field_31_dmOrientPage;
    }

    /**
     * Get the iHeadingPgn field for the SEP record.
     */
    public byte getIHeadingPgn()
    {
        return field_32_iHeadingPgn;
    }

    /**
     * Set the iHeadingPgn field for the SEP record.
     */
    public void setIHeadingPgn(byte field_32_iHeadingPgn)
    {
        this.field_32_iHeadingPgn = field_32_iHeadingPgn;
    }

    /**
     * Get the pgnStart field for the SEP record.
     */
    public int getPgnStart()
    {
        return field_33_pgnStart;
    }

    /**
     * Set the pgnStart field for the SEP record.
     */
    public void setPgnStart(int field_33_pgnStart)
    {
        this.field_33_pgnStart = field_33_pgnStart;
    }

    /**
     * Get the lnnMin field for the SEP record.
     */
    public int getLnnMin()
    {
        return field_34_lnnMin;
    }

    /**
     * Set the lnnMin field for the SEP record.
     */
    public void setLnnMin(int field_34_lnnMin)
    {
        this.field_34_lnnMin = field_34_lnnMin;
    }

    /**
     * Get the wTextFlow field for the SEP record.
     */
    public int getWTextFlow()
    {
        return field_35_wTextFlow;
    }

    /**
     * Set the wTextFlow field for the SEP record.
     */
    public void setWTextFlow(int field_35_wTextFlow)
    {
        this.field_35_wTextFlow = field_35_wTextFlow;
    }

    /**
     * Get the unused3 field for the SEP record.
     */
    public short getUnused3()
    {
        return field_36_unused3;
    }

    /**
     * Set the unused3 field for the SEP record.
     */
    public void setUnused3(short field_36_unused3)
    {
        this.field_36_unused3 = field_36_unused3;
    }

    /**
     * Get the pgbProp field for the SEP record.
     */
    public int getPgbProp()
    {
        return field_37_pgbProp;
    }

    /**
     * Set the pgbProp field for the SEP record.
     */
    public void setPgbProp(int field_37_pgbProp)
    {
        this.field_37_pgbProp = field_37_pgbProp;
    }

    /**
     * Get the unused4 field for the SEP record.
     */
    public short getUnused4()
    {
        return field_38_unused4;
    }

    /**
     * Set the unused4 field for the SEP record.
     */
    public void setUnused4(short field_38_unused4)
    {
        this.field_38_unused4 = field_38_unused4;
    }

    /**
     * Get the xaPage field for the SEP record.
     */
    public int getXaPage()
    {
        return field_39_xaPage;
    }

    /**
     * Set the xaPage field for the SEP record.
     */
    public void setXaPage(int field_39_xaPage)
    {
        this.field_39_xaPage = field_39_xaPage;
    }

    /**
     * Get the yaPage field for the SEP record.
     */
    public int getYaPage()
    {
        return field_40_yaPage;
    }

    /**
     * Set the yaPage field for the SEP record.
     */
    public void setYaPage(int field_40_yaPage)
    {
        this.field_40_yaPage = field_40_yaPage;
    }

    /**
     * Get the xaPageNUp field for the SEP record.
     */
    public int getXaPageNUp()
    {
        return field_41_xaPageNUp;
    }

    /**
     * Set the xaPageNUp field for the SEP record.
     */
    public void setXaPageNUp(int field_41_xaPageNUp)
    {
        this.field_41_xaPageNUp = field_41_xaPageNUp;
    }

    /**
     * Get the yaPageNUp field for the SEP record.
     */
    public int getYaPageNUp()
    {
        return field_42_yaPageNUp;
    }

    /**
     * Set the yaPageNUp field for the SEP record.
     */
    public void setYaPageNUp(int field_42_yaPageNUp)
    {
        this.field_42_yaPageNUp = field_42_yaPageNUp;
    }

    /**
     * Get the dxaLeft field for the SEP record.
     */
    public int getDxaLeft()
    {
        return field_43_dxaLeft;
    }

    /**
     * Set the dxaLeft field for the SEP record.
     */
    public void setDxaLeft(int field_43_dxaLeft)
    {
        this.field_43_dxaLeft = field_43_dxaLeft;
    }

    /**
     * Get the dxaRight field for the SEP record.
     */
    public int getDxaRight()
    {
        return field_44_dxaRight;
    }

    /**
     * Set the dxaRight field for the SEP record.
     */
    public void setDxaRight(int field_44_dxaRight)
    {
        this.field_44_dxaRight = field_44_dxaRight;
    }

    /**
     * Get the dyaTop field for the SEP record.
     */
    public int getDyaTop()
    {
        return field_45_dyaTop;
    }

    /**
     * Set the dyaTop field for the SEP record.
     */
    public void setDyaTop(int field_45_dyaTop)
    {
        this.field_45_dyaTop = field_45_dyaTop;
    }

    /**
     * Get the dyaBottom field for the SEP record.
     */
    public int getDyaBottom()
    {
        return field_46_dyaBottom;
    }

    /**
     * Set the dyaBottom field for the SEP record.
     */
    public void setDyaBottom(int field_46_dyaBottom)
    {
        this.field_46_dyaBottom = field_46_dyaBottom;
    }

    /**
     * Get the dzaGutter field for the SEP record.
     */
    public int getDzaGutter()
    {
        return field_47_dzaGutter;
    }

    /**
     * Set the dzaGutter field for the SEP record.
     */
    public void setDzaGutter(int field_47_dzaGutter)
    {
        this.field_47_dzaGutter = field_47_dzaGutter;
    }

    /**
     * Get the dyaHdrTop field for the SEP record.
     */
    public int getDyaHdrTop()
    {
        return field_48_dyaHdrTop;
    }

    /**
     * Set the dyaHdrTop field for the SEP record.
     */
    public void setDyaHdrTop(int field_48_dyaHdrTop)
    {
        this.field_48_dyaHdrTop = field_48_dyaHdrTop;
    }

    /**
     * Get the dyaHdrBottom field for the SEP record.
     */
    public int getDyaHdrBottom()
    {
        return field_49_dyaHdrBottom;
    }

    /**
     * Set the dyaHdrBottom field for the SEP record.
     */
    public void setDyaHdrBottom(int field_49_dyaHdrBottom)
    {
        this.field_49_dyaHdrBottom = field_49_dyaHdrBottom;
    }

    /**
     * Get the ccolM1 field for the SEP record.
     */
    public int getCcolM1()
    {
        return field_50_ccolM1;
    }

    /**
     * Set the ccolM1 field for the SEP record.
     */
    public void setCcolM1(int field_50_ccolM1)
    {
        this.field_50_ccolM1 = field_50_ccolM1;
    }

    /**
     * Get the fEvenlySpaced field for the SEP record.
     */
    public boolean getFEvenlySpaced()
    {
        return field_51_fEvenlySpaced;
    }

    /**
     * Set the fEvenlySpaced field for the SEP record.
     */
    public void setFEvenlySpaced(boolean field_51_fEvenlySpaced)
    {
        this.field_51_fEvenlySpaced = field_51_fEvenlySpaced;
    }

    /**
     * Get the unused5 field for the SEP record.
     */
    public byte getUnused5()
    {
        return field_52_unused5;
    }

    /**
     * Set the unused5 field for the SEP record.
     */
    public void setUnused5(byte field_52_unused5)
    {
        this.field_52_unused5 = field_52_unused5;
    }

    /**
     * Get the dxaColumns field for the SEP record.
     */
    public int getDxaColumns()
    {
        return field_53_dxaColumns;
    }

    /**
     * Set the dxaColumns field for the SEP record.
     */
    public void setDxaColumns(int field_53_dxaColumns)
    {
        this.field_53_dxaColumns = field_53_dxaColumns;
    }

    /**
     * Get the rgdxaColumn field for the SEP record.
     */
    public int[] getRgdxaColumn()
    {
        return field_54_rgdxaColumn;
    }

    /**
     * Set the rgdxaColumn field for the SEP record.
     */
    public void setRgdxaColumn(int[] field_54_rgdxaColumn)
    {
        this.field_54_rgdxaColumn = field_54_rgdxaColumn;
    }

    /**
     * Get the dxaColumnWidth field for the SEP record.
     */
    public int getDxaColumnWidth()
    {
        return field_55_dxaColumnWidth;
    }

    /**
     * Set the dxaColumnWidth field for the SEP record.
     */
    public void setDxaColumnWidth(int field_55_dxaColumnWidth)
    {
        this.field_55_dxaColumnWidth = field_55_dxaColumnWidth;
    }

    /**
     * Get the dmOrientFirst field for the SEP record.
     */
    public byte getDmOrientFirst()
    {
        return field_56_dmOrientFirst;
    }

    /**
     * Set the dmOrientFirst field for the SEP record.
     */
    public void setDmOrientFirst(byte field_56_dmOrientFirst)
    {
        this.field_56_dmOrientFirst = field_56_dmOrientFirst;
    }

    /**
     * Get the fLayout field for the SEP record.
     */
    public byte getFLayout()
    {
        return field_57_fLayout;
    }

    /**
     * Set the fLayout field for the SEP record.
     */
    public void setFLayout(byte field_57_fLayout)
    {
        this.field_57_fLayout = field_57_fLayout;
    }

    /**
     * Get the unused6 field for the SEP record.
     */
    public short getUnused6()
    {
        return field_58_unused6;
    }

    /**
     * Set the unused6 field for the SEP record.
     */
    public void setUnused6(short field_58_unused6)
    {
        this.field_58_unused6 = field_58_unused6;
    }

    /**
     * Get the olstAnm field for the SEP record.
     */
    public byte[] getOlstAnm()
    {
        return field_59_olstAnm;
    }

    /**
     * Set the olstAnm field for the SEP record.
     */
    public void setOlstAnm(byte[] field_59_olstAnm)
    {
        this.field_59_olstAnm = field_59_olstAnm;
    }


}  // END OF CLASS




