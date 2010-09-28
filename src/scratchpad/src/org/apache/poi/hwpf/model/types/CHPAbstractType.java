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
import org.apache.poi.hwpf.usermodel.ShadingDescriptor;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;

/**
 * Character Properties.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author S. Ryan Ackley
 */
public abstract class CHPAbstractType
    implements HDFType
{

    protected  short field_1_chse;
    protected  int field_2_format_flags;
        private static BitField  fBold = BitFieldFactory.getInstance(0x0001);
        private static BitField  fItalic = BitFieldFactory.getInstance(0x0002);
        private static BitField  fRMarkDel = BitFieldFactory.getInstance(0x0004);
        private static BitField  fOutline = BitFieldFactory.getInstance(0x0008);
        private static BitField  fFldVanish = BitFieldFactory.getInstance(0x0010);
        private static BitField  fSmallCaps = BitFieldFactory.getInstance(0x0020);
        private static BitField  fCaps = BitFieldFactory.getInstance(0x0040);
        private static BitField  fVanish = BitFieldFactory.getInstance(0x0080);
        private static BitField  fRMark = BitFieldFactory.getInstance(0x0100);
        private static BitField  fSpec = BitFieldFactory.getInstance(0x0200);
        private static BitField  fStrike = BitFieldFactory.getInstance(0x0400);
        private static BitField  fObj = BitFieldFactory.getInstance(0x0800);
        private static BitField  fShadow = BitFieldFactory.getInstance(0x1000);
        private static BitField  fLowerCase = BitFieldFactory.getInstance(0x2000);
        private static BitField  fData = BitFieldFactory.getInstance(0x4000);
        private static BitField  fOle2 = BitFieldFactory.getInstance(0x8000);
    protected  int field_3_format_flags1;
        private static BitField  fEmboss = BitFieldFactory.getInstance(0x0001);
        private static BitField  fImprint = BitFieldFactory.getInstance(0x0002);
        private static BitField  fDStrike = BitFieldFactory.getInstance(0x0004);
        private static BitField  fUsePgsuSettings = BitFieldFactory.getInstance(0x0008);
    protected  int field_4_ftcAscii;
    protected  int field_5_ftcFE;
    protected  int field_6_ftcOther;
    protected  int field_7_hps;
    protected  int field_8_dxaSpace;
    protected  byte field_9_iss;
    protected  byte field_10_kul;
    protected  byte field_11_ico;
    protected  int field_12_hpsPos;
    protected  int field_13_lidDefault;
    protected  int field_14_lidFE;
    protected  byte field_15_idctHint;
    protected  int field_16_wCharScale;
    protected  int field_17_fcPic;
    protected  int field_18_fcObj;
    protected  int field_19_lTagObj;
    protected  int field_20_ibstRMark;
    protected  int field_21_ibstRMarkDel;
    protected  DateAndTime field_22_dttmRMark;
    protected  DateAndTime field_23_dttmRMarkDel;
    protected  int field_24_istd;
    protected  int field_25_baseIstd;
    protected  int field_26_ftcSym;
    protected  int field_27_xchSym;
    protected  int field_28_idslRMReason;
    protected  int field_29_idslReasonDel;
    protected  byte field_30_ysr;
    protected  byte field_31_chYsr;
    protected  int field_32_hpsKern;
    protected  short field_33_Highlight;
        private static BitField  icoHighlight = BitFieldFactory.getInstance(0x001f);
        private static BitField  fHighlight = BitFieldFactory.getInstance(0x0020);
        private static BitField  kcd = BitFieldFactory.getInstance(0x01c0);
        private static BitField  fNavHighlight = BitFieldFactory.getInstance(0x0200);
        private static BitField  fChsDiff = BitFieldFactory.getInstance(0x0400);
        private static BitField  fMacChs = BitFieldFactory.getInstance(0x0800);
        private static BitField  fFtcAsciSym = BitFieldFactory.getInstance(0x1000);
    protected  short field_34_fPropMark;
    protected  int field_35_ibstPropRMark;
    protected  DateAndTime field_36_dttmPropRMark;
    protected  byte field_37_sfxtText;
    protected  byte field_38_fDispFldRMark;
    protected  int field_39_ibstDispFldRMark;
    protected  DateAndTime field_40_dttmDispFldRMark;
    protected  byte[] field_41_xstDispFldRMark;
    protected  ShadingDescriptor field_42_shd;
    protected  BorderCode field_43_brc;


    public CHPAbstractType()
    {

    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getSize()
    {
        return 4 +  + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 4 + 1 + 1 + 1 + 2 + 2 + 2 + 1 + 2 + 4 + 4 + 4 + 2 + 2 + 4 + 4 + 2 + 2 + 2 + 2 + 2 + 2 + 1 + 1 + 2 + 2 + 2 + 2 + 4 + 1 + 1 + 2 + 4 + 32 + 2 + 4;
    }



    /**
     * Get the chse field for the CHP record.
     */
    public short getChse()
    {
        return field_1_chse;
    }

    /**
     * Set the chse field for the CHP record.
     */
    public void setChse(short field_1_chse)
    {
        this.field_1_chse = field_1_chse;
    }

    /**
     * Get the format_flags field for the CHP record.
     */
    public int getFormat_flags()
    {
        return field_2_format_flags;
    }

    /**
     * Set the format_flags field for the CHP record.
     */
    public void setFormat_flags(int field_2_format_flags)
    {
        this.field_2_format_flags = field_2_format_flags;
    }

    /**
     * Get the format_flags1 field for the CHP record.
     */
    public int getFormat_flags1()
    {
        return field_3_format_flags1;
    }

    /**
     * Set the format_flags1 field for the CHP record.
     */
    public void setFormat_flags1(int field_3_format_flags1)
    {
        this.field_3_format_flags1 = field_3_format_flags1;
    }

    /**
     * Get the ftcAscii field for the CHP record.
     */
    public int getFtcAscii()
    {
        return field_4_ftcAscii;
    }

    /**
     * Set the ftcAscii field for the CHP record.
     */
    public void setFtcAscii(int field_4_ftcAscii)
    {
        this.field_4_ftcAscii = field_4_ftcAscii;
    }

    /**
     * Get the ftcFE field for the CHP record.
     */
    public int getFtcFE()
    {
        return field_5_ftcFE;
    }

    /**
     * Set the ftcFE field for the CHP record.
     */
    public void setFtcFE(int field_5_ftcFE)
    {
        this.field_5_ftcFE = field_5_ftcFE;
    }

    /**
     * Get the ftcOther field for the CHP record.
     */
    public int getFtcOther()
    {
        return field_6_ftcOther;
    }

    /**
     * Set the ftcOther field for the CHP record.
     */
    public void setFtcOther(int field_6_ftcOther)
    {
        this.field_6_ftcOther = field_6_ftcOther;
    }

    /**
     * Get the hps field for the CHP record.
     */
    public int getHps()
    {
        return field_7_hps;
    }

    /**
     * Set the hps field for the CHP record.
     */
    public void setHps(int field_7_hps)
    {
        this.field_7_hps = field_7_hps;
    }

    /**
     * Get the dxaSpace field for the CHP record.
     */
    public int getDxaSpace()
    {
        return field_8_dxaSpace;
    }

    /**
     * Set the dxaSpace field for the CHP record.
     */
    public void setDxaSpace(int field_8_dxaSpace)
    {
        this.field_8_dxaSpace = field_8_dxaSpace;
    }

    /**
     * Get the iss field for the CHP record.
     */
    public byte getIss()
    {
        return field_9_iss;
    }

    /**
     * Set the iss field for the CHP record.
     */
    public void setIss(byte field_9_iss)
    {
        this.field_9_iss = field_9_iss;
    }

    /**
     * Get the kul field for the CHP record.
     */
    public byte getKul()
    {
        return field_10_kul;
    }

    /**
     * Set the kul field for the CHP record.
     */
    public void setKul(byte field_10_kul)
    {
        this.field_10_kul = field_10_kul;
    }

    /**
     * Get the ico field for the CHP record.
     */
    public byte getIco()
    {
        return field_11_ico;
    }

    /**
     * Set the ico field for the CHP record.
     */
    public void setIco(byte field_11_ico)
    {
        this.field_11_ico = field_11_ico;
    }

    /**
     * Get the hpsPos field for the CHP record.
     */
    public int getHpsPos()
    {
        return field_12_hpsPos;
    }

    /**
     * Set the hpsPos field for the CHP record.
     */
    public void setHpsPos(int field_12_hpsPos)
    {
        this.field_12_hpsPos = field_12_hpsPos;
    }

    /**
     * Get the lidDefault field for the CHP record.
     */
    public int getLidDefault()
    {
        return field_13_lidDefault;
    }

    /**
     * Set the lidDefault field for the CHP record.
     */
    public void setLidDefault(int field_13_lidDefault)
    {
        this.field_13_lidDefault = field_13_lidDefault;
    }

    /**
     * Get the lidFE field for the CHP record.
     */
    public int getLidFE()
    {
        return field_14_lidFE;
    }

    /**
     * Set the lidFE field for the CHP record.
     */
    public void setLidFE(int field_14_lidFE)
    {
        this.field_14_lidFE = field_14_lidFE;
    }

    /**
     * Get the idctHint field for the CHP record.
     */
    public byte getIdctHint()
    {
        return field_15_idctHint;
    }

    /**
     * Set the idctHint field for the CHP record.
     */
    public void setIdctHint(byte field_15_idctHint)
    {
        this.field_15_idctHint = field_15_idctHint;
    }

    /**
     * Get the wCharScale field for the CHP record.
     */
    public int getWCharScale()
    {
        return field_16_wCharScale;
    }

    /**
     * Set the wCharScale field for the CHP record.
     */
    public void setWCharScale(int field_16_wCharScale)
    {
        this.field_16_wCharScale = field_16_wCharScale;
    }

    /**
     * Get the fcPic field for the CHP record.
     */
    public int getFcPic()
    {
        return field_17_fcPic;
    }

    /**
     * Set the fcPic field for the CHP record.
     */
    public void setFcPic(int field_17_fcPic)
    {
        this.field_17_fcPic = field_17_fcPic;
    }

    /**
     * Get the fcObj field for the CHP record.
     */
    public int getFcObj()
    {
        return field_18_fcObj;
    }

    /**
     * Set the fcObj field for the CHP record.
     */
    public void setFcObj(int field_18_fcObj)
    {
        this.field_18_fcObj = field_18_fcObj;
    }

    /**
     * Get the lTagObj field for the CHP record.
     */
    public int getLTagObj()
    {
        return field_19_lTagObj;
    }

    /**
     * Set the lTagObj field for the CHP record.
     */
    public void setLTagObj(int field_19_lTagObj)
    {
        this.field_19_lTagObj = field_19_lTagObj;
    }

    /**
     * Get the ibstRMark field for the CHP record.
     */
    public int getIbstRMark()
    {
        return field_20_ibstRMark;
    }

    /**
     * Set the ibstRMark field for the CHP record.
     */
    public void setIbstRMark(int field_20_ibstRMark)
    {
        this.field_20_ibstRMark = field_20_ibstRMark;
    }

    /**
     * Get the ibstRMarkDel field for the CHP record.
     */
    public int getIbstRMarkDel()
    {
        return field_21_ibstRMarkDel;
    }

    /**
     * Set the ibstRMarkDel field for the CHP record.
     */
    public void setIbstRMarkDel(int field_21_ibstRMarkDel)
    {
        this.field_21_ibstRMarkDel = field_21_ibstRMarkDel;
    }

    /**
     * Get the dttmRMark field for the CHP record.
     */
    public DateAndTime getDttmRMark()
    {
        return field_22_dttmRMark;
    }

    /**
     * Set the dttmRMark field for the CHP record.
     */
    public void setDttmRMark(DateAndTime field_22_dttmRMark)
    {
        this.field_22_dttmRMark = field_22_dttmRMark;
    }

    /**
     * Get the dttmRMarkDel field for the CHP record.
     */
    public DateAndTime getDttmRMarkDel()
    {
        return field_23_dttmRMarkDel;
    }

    /**
     * Set the dttmRMarkDel field for the CHP record.
     */
    public void setDttmRMarkDel(DateAndTime field_23_dttmRMarkDel)
    {
        this.field_23_dttmRMarkDel = field_23_dttmRMarkDel;
    }

    /**
     * Get the istd field for the CHP record.
     */
    public int getIstd()
    {
        return field_24_istd;
    }

    /**
     * Set the istd field for the CHP record.
     */
    public void setIstd(int field_24_istd)
    {
        this.field_24_istd = field_24_istd;
    }

    /**
     * Get the baseIstd field for the CHP record.
     */
    public int getBaseIstd()
    {
        return field_25_baseIstd;
    }

    /**
     * Set the baseIstd field for the CHP record.
     */
    public void setBaseIstd(int field_25_baseIstd)
    {
        this.field_25_baseIstd = field_25_baseIstd;
    }

    /**
     * Get the ftcSym field for the CHP record.
     */
    public int getFtcSym()
    {
        return field_26_ftcSym;
    }

    /**
     * Set the ftcSym field for the CHP record.
     */
    public void setFtcSym(int field_26_ftcSym)
    {
        this.field_26_ftcSym = field_26_ftcSym;
    }

    /**
     * Get the xchSym field for the CHP record.
     */
    public int getXchSym()
    {
        return field_27_xchSym;
    }

    /**
     * Set the xchSym field for the CHP record.
     */
    public void setXchSym(int field_27_xchSym)
    {
        this.field_27_xchSym = field_27_xchSym;
    }

    /**
     * Get the idslRMReason field for the CHP record.
     */
    public int getIdslRMReason()
    {
        return field_28_idslRMReason;
    }

    /**
     * Set the idslRMReason field for the CHP record.
     */
    public void setIdslRMReason(int field_28_idslRMReason)
    {
        this.field_28_idslRMReason = field_28_idslRMReason;
    }

    /**
     * Get the idslReasonDel field for the CHP record.
     */
    public int getIdslReasonDel()
    {
        return field_29_idslReasonDel;
    }

    /**
     * Set the idslReasonDel field for the CHP record.
     */
    public void setIdslReasonDel(int field_29_idslReasonDel)
    {
        this.field_29_idslReasonDel = field_29_idslReasonDel;
    }

    /**
     * Get the ysr field for the CHP record.
     */
    public byte getYsr()
    {
        return field_30_ysr;
    }

    /**
     * Set the ysr field for the CHP record.
     */
    public void setYsr(byte field_30_ysr)
    {
        this.field_30_ysr = field_30_ysr;
    }

    /**
     * Get the chYsr field for the CHP record.
     */
    public byte getChYsr()
    {
        return field_31_chYsr;
    }

    /**
     * Set the chYsr field for the CHP record.
     */
    public void setChYsr(byte field_31_chYsr)
    {
        this.field_31_chYsr = field_31_chYsr;
    }

    /**
     * Get the hpsKern field for the CHP record.
     */
    public int getHpsKern()
    {
        return field_32_hpsKern;
    }

    /**
     * Set the hpsKern field for the CHP record.
     */
    public void setHpsKern(int field_32_hpsKern)
    {
        this.field_32_hpsKern = field_32_hpsKern;
    }

    /**
     * Get the Highlight field for the CHP record.
     */
    public short getHighlight()
    {
        return field_33_Highlight;
    }

    /**
     * Set the Highlight field for the CHP record.
     */
    public void setHighlight(short field_33_Highlight)
    {
        this.field_33_Highlight = field_33_Highlight;
    }

    /**
     * Get the fPropMark field for the CHP record.
     */
    public short getFPropMark()
    {
        return field_34_fPropMark;
    }

    /**
     * Set the fPropMark field for the CHP record.
     */
    public void setFPropMark(short field_34_fPropMark)
    {
        this.field_34_fPropMark = field_34_fPropMark;
    }

    /**
     * Get the ibstPropRMark field for the CHP record.
     */
    public int getIbstPropRMark()
    {
        return field_35_ibstPropRMark;
    }

    /**
     * Set the ibstPropRMark field for the CHP record.
     */
    public void setIbstPropRMark(int field_35_ibstPropRMark)
    {
        this.field_35_ibstPropRMark = field_35_ibstPropRMark;
    }

    /**
     * Get the dttmPropRMark field for the CHP record.
     */
    public DateAndTime getDttmPropRMark()
    {
        return field_36_dttmPropRMark;
    }

    /**
     * Set the dttmPropRMark field for the CHP record.
     */
    public void setDttmPropRMark(DateAndTime field_36_dttmPropRMark)
    {
        this.field_36_dttmPropRMark = field_36_dttmPropRMark;
    }

    /**
     * Get the sfxtText field for the CHP record.
     */
    public byte getSfxtText()
    {
        return field_37_sfxtText;
    }

    /**
     * Set the sfxtText field for the CHP record.
     */
    public void setSfxtText(byte field_37_sfxtText)
    {
        this.field_37_sfxtText = field_37_sfxtText;
    }

    /**
     * Get the fDispFldRMark field for the CHP record.
     */
    public byte getFDispFldRMark()
    {
        return field_38_fDispFldRMark;
    }

    /**
     * Set the fDispFldRMark field for the CHP record.
     */
    public void setFDispFldRMark(byte field_38_fDispFldRMark)
    {
        this.field_38_fDispFldRMark = field_38_fDispFldRMark;
    }

    /**
     * Get the ibstDispFldRMark field for the CHP record.
     */
    public int getIbstDispFldRMark()
    {
        return field_39_ibstDispFldRMark;
    }

    /**
     * Set the ibstDispFldRMark field for the CHP record.
     */
    public void setIbstDispFldRMark(int field_39_ibstDispFldRMark)
    {
        this.field_39_ibstDispFldRMark = field_39_ibstDispFldRMark;
    }

    /**
     * Get the dttmDispFldRMark field for the CHP record.
     */
    public DateAndTime getDttmDispFldRMark()
    {
        return field_40_dttmDispFldRMark;
    }

    /**
     * Set the dttmDispFldRMark field for the CHP record.
     */
    public void setDttmDispFldRMark(DateAndTime field_40_dttmDispFldRMark)
    {
        this.field_40_dttmDispFldRMark = field_40_dttmDispFldRMark;
    }

    /**
     * Get the xstDispFldRMark field for the CHP record.
     */
    public byte[] getXstDispFldRMark()
    {
        return field_41_xstDispFldRMark;
    }

    /**
     * Set the xstDispFldRMark field for the CHP record.
     */
    public void setXstDispFldRMark(byte[] field_41_xstDispFldRMark)
    {
        this.field_41_xstDispFldRMark = field_41_xstDispFldRMark;
    }

    /**
     * Get the shd field for the CHP record.
     */
    public ShadingDescriptor getShd()
    {
        return field_42_shd;
    }

    /**
     * Set the shd field for the CHP record.
     */
    public void setShd(ShadingDescriptor field_42_shd)
    {
        this.field_42_shd = field_42_shd;
    }

    /**
     * Get the brc field for the CHP record.
     */
    public BorderCode getBrc()
    {
        return field_43_brc;
    }

    /**
     * Set the brc field for the CHP record.
     */
    public void setBrc(BorderCode field_43_brc)
    {
        this.field_43_brc = field_43_brc;
    }

    /**
     * Sets the fBold field value.
     *
     */
    public void setFBold(boolean value)
    {
        field_2_format_flags = fBold.setBoolean(field_2_format_flags, value);
    }

    /**
     *
     * @return  the fBold field value.
     */
    public boolean isFBold()
    {
        return fBold.isSet(field_2_format_flags);
    }

    /**
     * Sets the fItalic field value.
     *
     */
    public void setFItalic(boolean value)
    {
        field_2_format_flags = fItalic.setBoolean(field_2_format_flags, value);
    }

    /**
     *
     * @return  the fItalic field value.
     */
    public boolean isFItalic()
    {
        return fItalic.isSet(field_2_format_flags);
    }

    /**
     * Sets the fRMarkDel field value.
     *
     */
    public void setFRMarkDel(boolean value)
    {
        field_2_format_flags = fRMarkDel.setBoolean(field_2_format_flags, value);
    }

    /**
     *
     * @return  the fRMarkDel field value.
     */
    public boolean isFRMarkDel()
    {
        return fRMarkDel.isSet(field_2_format_flags);
    }

    /**
     * Sets the fOutline field value.
     *
     */
    public void setFOutline(boolean value)
    {
        field_2_format_flags = fOutline.setBoolean(field_2_format_flags, value);
    }

    /**
     *
     * @return  the fOutline field value.
     */
    public boolean isFOutline()
    {
        return fOutline.isSet(field_2_format_flags);
    }

    /**
     * Sets the fFldVanish field value.
     *
     */
    public void setFFldVanish(boolean value)
    {
        field_2_format_flags = fFldVanish.setBoolean(field_2_format_flags, value);
    }

    /**
     *
     * @return  the fFldVanish field value.
     */
    public boolean isFFldVanish()
    {
        return fFldVanish.isSet(field_2_format_flags);
    }

    /**
     * Sets the fSmallCaps field value.
     *
     */
    public void setFSmallCaps(boolean value)
    {
        field_2_format_flags = fSmallCaps.setBoolean(field_2_format_flags, value);
    }

    /**
     *
     * @return  the fSmallCaps field value.
     */
    public boolean isFSmallCaps()
    {
        return fSmallCaps.isSet(field_2_format_flags);
    }

    /**
     * Sets the fCaps field value.
     *
     */
    public void setFCaps(boolean value)
    {
        field_2_format_flags = fCaps.setBoolean(field_2_format_flags, value);
    }

    /**
     *
     * @return  the fCaps field value.
     */
    public boolean isFCaps()
    {
        return fCaps.isSet(field_2_format_flags);
    }

    /**
     * Sets the fVanish field value.
     *
     */
    public void setFVanish(boolean value)
    {
        field_2_format_flags = fVanish.setBoolean(field_2_format_flags, value);
    }

    /**
     *
     * @return  the fVanish field value.
     */
    public boolean isFVanish()
    {
        return fVanish.isSet(field_2_format_flags);
    }

    /**
     * Sets the fRMark field value.
     *
     */
    public void setFRMark(boolean value)
    {
        field_2_format_flags = fRMark.setBoolean(field_2_format_flags, value);
    }

    /**
     *
     * @return  the fRMark field value.
     */
    public boolean isFRMark()
    {
        return fRMark.isSet(field_2_format_flags);
    }

    /**
     * Sets the fSpec field value.
     *
     */
    public void setFSpec(boolean value)
    {
        field_2_format_flags = fSpec.setBoolean(field_2_format_flags, value);
    }

    /**
     *
     * @return is the fSpec field value set? (Also known as sprmCFSpec)
     */
    public boolean isFSpec()
    {
        return fSpec.isSet(field_2_format_flags);
    }

    /**
     * Sets the fStrike field value.
     *
     */
    public void setFStrike(boolean value)
    {
        field_2_format_flags = fStrike.setBoolean(field_2_format_flags, value);
    }

    /**
     *
     * @return  the fStrike field value.
     */
    public boolean isFStrike()
    {
        return fStrike.isSet(field_2_format_flags);
    }

    /**
     * Sets the fObj field value.
     *
     */
    public void setFObj(boolean value)
    {
        field_2_format_flags = fObj.setBoolean(field_2_format_flags, value);
    }

    /**
     *
     * @return  the fObj field value.
     */
    public boolean isFObj()
    {
        return fObj.isSet(field_2_format_flags);
    }

    /**
     * Sets the fShadow field value.
     *
     */
    public void setFShadow(boolean value)
    {
        field_2_format_flags = fShadow.setBoolean(field_2_format_flags, value);
    }

    /**
     *
     * @return  the fShadow field value.
     */
    public boolean isFShadow()
    {
        return fShadow.isSet(field_2_format_flags);
    }

    /**
     * Sets the fLowerCase field value.
     *
     */
    public void setFLowerCase(boolean value)
    {
        field_2_format_flags = fLowerCase.setBoolean(field_2_format_flags, value);
    }

    /**
     *
     * @return  the fLowerCase field value.
     */
    public boolean isFLowerCase()
    {
        return fLowerCase.isSet(field_2_format_flags);
    }

    /**
     * Sets the fData field value.
     *
     */
    public void setFData(boolean value)
    {
        field_2_format_flags = fData.setBoolean(field_2_format_flags, value);
    }

    /**
     *
     * @return  the fData field value.
     */
    public boolean isFData()
    {
        return fData.isSet(field_2_format_flags);
    }

    /**
     * Sets the fOle2 field value.
     *
     */
    public void setFOle2(boolean value)
    {
        field_2_format_flags = fOle2.setBoolean(field_2_format_flags, value);
    }

    /**
     *
     * @return  the fOle2 field value.
     */
    public boolean isFOle2()
    {
        return fOle2.isSet(field_2_format_flags);
    }

    /**
     * Sets the fEmboss field value.
     *
     */
    public void setFEmboss(boolean value)
    {
        field_3_format_flags1 = fEmboss.setBoolean(field_3_format_flags1, value);
    }

    /**
     *
     * @return  the fEmboss field value.
     */
    public boolean isFEmboss()
    {
        return fEmboss.isSet(field_3_format_flags1);
    }

    /**
     * Sets the fImprint field value.
     *
     */
    public void setFImprint(boolean value)
    {
        field_3_format_flags1 = fImprint.setBoolean(field_3_format_flags1, value);
    }

    /**
     *
     * @return  the fImprint field value.
     */
    public boolean isFImprint()
    {
        return fImprint.isSet(field_3_format_flags1);
    }

    /**
     * Sets the fDStrike field value.
     *
     */
    public void setFDStrike(boolean value)
    {
        field_3_format_flags1 = fDStrike.setBoolean(field_3_format_flags1, value);
    }

    /**
     *
     * @return  the fDStrike field value.
     */
    public boolean isFDStrike()
    {
        return fDStrike.isSet(field_3_format_flags1);
    }

    /**
     * Sets the fUsePgsuSettings field value.
     *
     */
    public void setFUsePgsuSettings(boolean value)
    {
        field_3_format_flags1 = fUsePgsuSettings.setBoolean(field_3_format_flags1, value);
    }

    /**
     *
     * @return  the fUsePgsuSettings field value.
     */
    public boolean isFUsePgsuSettings()
    {
        return fUsePgsuSettings.isSet(field_3_format_flags1);
    }

    /**
     * Sets the icoHighlight field value.
     *
     */
    public void setIcoHighlight(byte value)
    {
        field_33_Highlight = (short)icoHighlight.setValue(field_33_Highlight, value);
    }

    /**
     *
     * @return  the icoHighlight field value.
     */
    public byte getIcoHighlight()
    {
        return ( byte )icoHighlight.getValue(field_33_Highlight);
    }

    /**
     * Sets the fHighlight field value.
     *
     */
    public void setFHighlight(boolean value)
    {
        field_33_Highlight = (short)fHighlight.setBoolean(field_33_Highlight, value);
    }

    /**
     *
     * @return  the fHighlight field value.
     */
    public boolean isFHighlight()
    {
        return fHighlight.isSet(field_33_Highlight);
    }

    /**
     * Sets the kcd field value.
     *
     */
    public void setKcd(byte value)
    {
        field_33_Highlight = (short)kcd.setValue(field_33_Highlight, value);
    }

    /**
     *
     * @return  the kcd field value.
     */
    public byte getKcd()
    {
        return ( byte )kcd.getValue(field_33_Highlight);
    }

    /**
     * Sets the fNavHighlight field value.
     *
     */
    public void setFNavHighlight(boolean value)
    {
        field_33_Highlight = (short)fNavHighlight.setBoolean(field_33_Highlight, value);
    }

    /**
     *
     * @return  the fNavHighlight field value.
     */
    public boolean isFNavHighlight()
    {
        return fNavHighlight.isSet(field_33_Highlight);
    }

    /**
     * Sets the fChsDiff field value.
     *
     */
    public void setFChsDiff(boolean value)
    {
        field_33_Highlight = (short)fChsDiff.setBoolean(field_33_Highlight, value);
    }

    /**
     *
     * @return  the fChsDiff field value.
     */
    public boolean isFChsDiff()
    {
        return fChsDiff.isSet(field_33_Highlight);
    }

    /**
     * Sets the fMacChs field value.
     *
     */
    public void setFMacChs(boolean value)
    {
        field_33_Highlight = (short)fMacChs.setBoolean(field_33_Highlight, value);
    }

    /**
     *
     * @return  the fMacChs field value.
     */
    public boolean isFMacChs()
    {
        return fMacChs.isSet(field_33_Highlight);
    }

    /**
     * Sets the fFtcAsciSym field value.
     *
     */
    public void setFFtcAsciSym(boolean value)
    {
        field_33_Highlight = (short)fFtcAsciSym.setBoolean(field_33_Highlight, value);
    }

    /**
     *
     * @return  the fFtcAsciSym field value.
     */
    public boolean isFFtcAsciSym()
    {
        return fFtcAsciSym.isSet(field_33_Highlight);
    }
}
