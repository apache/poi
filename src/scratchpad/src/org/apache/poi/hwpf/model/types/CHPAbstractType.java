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

/**
 * Character Properties.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author S. Ryan Ackley
 */
public abstract class CHPAbstractType
    implements HDFType
{

    protected  int field_1_format_flags;
        private static BitField  fBold = new BitField(0x0001);
        private static BitField  fItalic = new BitField(0x0002);
        private static BitField  fRMarkDel = new BitField(0x0004);
        private static BitField  fOutline = new BitField(0x0008);
        private static BitField  fFldVanish = new BitField(0x0010);
        private static BitField  fSmallCaps = new BitField(0x0020);
        private static BitField  fCaps = new BitField(0x0040);
        private static BitField  fVanish = new BitField(0x0080);
        private static BitField  fRMark = new BitField(0x0100);
        private static BitField  fSpec = new BitField(0x0200);
        private static BitField  fStrike = new BitField(0x0400);
        private static BitField  fObj = new BitField(0x0800);
        private static BitField  fShadow = new BitField(0x1000);
        private static BitField  fLowerCase = new BitField(0x2000);
        private static BitField  fData = new BitField(0x4000);
        private static BitField  fOle2 = new BitField(0x8000);
    protected  int field_2_format_flags1;
        private static BitField  fEmboss = new BitField(0x0001);
        private static BitField  fImprint = new BitField(0x0002);
        private static BitField  fDStrike = new BitField(0x0004);
        private static BitField  fUsePgsuSettings = new BitField(0x0008);
    protected  int field_3_ftcAscii;
    protected  int field_4_ftcFE;
    protected  int field_5_ftcOther;
    protected  int field_6_hps;
    protected  int field_7_dxaSpace;
    protected  byte field_8_iss;
    protected  byte field_9_kul;
    protected  byte field_10_ico;
    protected  int field_11_hpsPos;
    protected  int field_12_lidDefault;
    protected  int field_13_lidFE;
    protected  byte field_14_idctHint;
    protected  int field_15_wCharScale;
    protected  int field_16_fcPic;
    protected  int field_17_fcObj;
    protected  int field_18_lTagObj;
    protected  int field_19_ibstRMark;
    protected  int field_20_ibstRMarkDel;
    protected  DateAndTime field_21_dttmRMark;
    protected  DateAndTime field_22_dttmRMarkDel;
    protected  int field_23_istd;
    protected  int field_24_baseIstd;
    protected  int field_25_ftcSym;
    protected  int field_26_xchSym;
    protected  int field_27_idslRMReason;
    protected  int field_28_idslReasonDel;
    protected  byte field_29_ysr;
    protected  byte field_30_chYsr;
    protected  int field_31_hpsKern;
    protected  short field_32_Highlight;
        private static BitField  icoHighlight = new BitField(0x001f);
        private static BitField  fHighlight = new BitField(0x0020);
        private static BitField  fNavHighlight = new BitField(0x0040);
    protected  short field_33_InternalFlags;
        private static BitField  iatrUndetType = new BitField(0x000f);
        private static BitField  fUlGap = new BitField(0x0010);
        private static BitField  fScriptAnchor = new BitField(0x0800);
        private static BitField  fFixedObj = new BitField(0x1000);
        private static BitField  spare2 = new BitField(0x2000);
    protected  short field_34_EncodingFlags;
        private static BitField  fChsDiff = new BitField(0x0001);
        private static BitField  fMacChs = new BitField(0x0002);
        private static BitField  fFtcAsciSym = new BitField(0x0004);
        private static BitField  fFtcReq = new BitField(0x0008);
        private static BitField  fLangApplied = new BitField(0x0010);
        private static BitField  fSpareLangApplied = new BitField(0x0020);
        private static BitField  fForcedCvAuto = new BitField(0x0040);
    protected  short field_35_chse;
    protected  short field_36_fPropMark;
    protected  int field_37_ibstPropRMark;
    protected  DateAndTime field_38_dttmPropRMark;
    protected  byte field_39_sfxtText;
    protected  byte field_40_fDispFldRMark;
    protected  int field_41_ibstDispFldRMark;
    protected  DateAndTime field_42_dttmDispFldRMark;
    protected  byte[] field_43_xstDispFldRMark;
    protected  ShadingDescriptor field_44_shd;
    protected  BorderCode field_45_brc;


    public CHPAbstractType()
    {

    }


    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[CHP]\n");

        buffer.append("    .format_flags         = ");
        buffer.append(" (").append(getFormat_flags()).append(" )\n");
        buffer.append("         .fBold                    = ").append(isFBold()).append('\n');
        buffer.append("         .fItalic                  = ").append(isFItalic()).append('\n');
        buffer.append("         .fRMarkDel                = ").append(isFRMarkDel()).append('\n');
        buffer.append("         .fOutline                 = ").append(isFOutline()).append('\n');
        buffer.append("         .fFldVanish               = ").append(isFFldVanish()).append('\n');
        buffer.append("         .fSmallCaps               = ").append(isFSmallCaps()).append('\n');
        buffer.append("         .fCaps                    = ").append(isFCaps()).append('\n');
        buffer.append("         .fVanish                  = ").append(isFVanish()).append('\n');
        buffer.append("         .fRMark                   = ").append(isFRMark()).append('\n');
        buffer.append("         .fSpec                    = ").append(isFSpec()).append('\n');
        buffer.append("         .fStrike                  = ").append(isFStrike()).append('\n');
        buffer.append("         .fObj                     = ").append(isFObj()).append('\n');
        buffer.append("         .fShadow                  = ").append(isFShadow()).append('\n');
        buffer.append("         .fLowerCase               = ").append(isFLowerCase()).append('\n');
        buffer.append("         .fData                    = ").append(isFData()).append('\n');
        buffer.append("         .fOle2                    = ").append(isFOle2()).append('\n');

        buffer.append("    .format_flags1        = ");
        buffer.append(" (").append(getFormat_flags1()).append(" )\n");
        buffer.append("         .fEmboss                  = ").append(isFEmboss()).append('\n');
        buffer.append("         .fImprint                 = ").append(isFImprint()).append('\n');
        buffer.append("         .fDStrike                 = ").append(isFDStrike()).append('\n');
        buffer.append("         .fUsePgsuSettings         = ").append(isFUsePgsuSettings()).append('\n');

        buffer.append("    .ftcAscii             = ");
        buffer.append(" (").append(getFtcAscii()).append(" )\n");

        buffer.append("    .ftcFE                = ");
        buffer.append(" (").append(getFtcFE()).append(" )\n");

        buffer.append("    .ftcOther             = ");
        buffer.append(" (").append(getFtcOther()).append(" )\n");

        buffer.append("    .hps                  = ");
        buffer.append(" (").append(getHps()).append(" )\n");

        buffer.append("    .dxaSpace             = ");
        buffer.append(" (").append(getDxaSpace()).append(" )\n");

        buffer.append("    .iss                  = ");
        buffer.append(" (").append(getIss()).append(" )\n");

        buffer.append("    .kul                  = ");
        buffer.append(" (").append(getKul()).append(" )\n");

        buffer.append("    .ico                  = ");
        buffer.append(" (").append(getIco()).append(" )\n");

        buffer.append("    .hpsPos               = ");
        buffer.append(" (").append(getHpsPos()).append(" )\n");

        buffer.append("    .lidDefault           = ");
        buffer.append(" (").append(getLidDefault()).append(" )\n");

        buffer.append("    .lidFE                = ");
        buffer.append(" (").append(getLidFE()).append(" )\n");

        buffer.append("    .idctHint             = ");
        buffer.append(" (").append(getIdctHint()).append(" )\n");

        buffer.append("    .wCharScale           = ");
        buffer.append(" (").append(getWCharScale()).append(" )\n");

        buffer.append("    .fcPic                = ");
        buffer.append(" (").append(getFcPic()).append(" )\n");

        buffer.append("    .fcObj                = ");
        buffer.append(" (").append(getFcObj()).append(" )\n");

        buffer.append("    .lTagObj              = ");
        buffer.append(" (").append(getLTagObj()).append(" )\n");

        buffer.append("    .ibstRMark            = ");
        buffer.append(" (").append(getIbstRMark()).append(" )\n");

        buffer.append("    .ibstRMarkDel         = ");
        buffer.append(" (").append(getIbstRMarkDel()).append(" )\n");

        buffer.append("    .dttmRMark            = ");
        buffer.append(" (").append(getDttmRMark()).append(" )\n");

        buffer.append("    .dttmRMarkDel         = ");
        buffer.append(" (").append(getDttmRMarkDel()).append(" )\n");

        buffer.append("    .istd                 = ");
        buffer.append(" (").append(getIstd()).append(" )\n");

        buffer.append("    .baseIstd             = ");
        buffer.append(" (").append(getBaseIstd()).append(" )\n");

        buffer.append("    .ftcSym               = ");
        buffer.append(" (").append(getFtcSym()).append(" )\n");

        buffer.append("    .xchSym               = ");
        buffer.append(" (").append(getXchSym()).append(" )\n");

        buffer.append("    .idslRMReason         = ");
        buffer.append(" (").append(getIdslRMReason()).append(" )\n");

        buffer.append("    .idslReasonDel        = ");
        buffer.append(" (").append(getIdslReasonDel()).append(" )\n");

        buffer.append("    .ysr                  = ");
        buffer.append(" (").append(getYsr()).append(" )\n");

        buffer.append("    .chYsr                = ");
        buffer.append(" (").append(getChYsr()).append(" )\n");

        buffer.append("    .hpsKern              = ");
        buffer.append(" (").append(getHpsKern()).append(" )\n");

        buffer.append("    .Highlight            = ");
        buffer.append(" (").append(getHighlight()).append(" )\n");
        buffer.append("         .icoHighlight             = ").append(getIcoHighlight()).append('\n');
        buffer.append("         .fHighlight               = ").append(isFHighlight()).append('\n');
        buffer.append("         .fNavHighlight            = ").append(isFNavHighlight()).append('\n');

        buffer.append("    .InternalFlags        = ");
        buffer.append(" (").append(getInternalFlags()).append(" )\n");
        buffer.append("         .iatrUndetType            = ").append(getIatrUndetType()).append('\n');
        buffer.append("         .fUlGap                   = ").append(isFUlGap()).append('\n');
        buffer.append("         .fScriptAnchor            = ").append(isFScriptAnchor()).append('\n');
        buffer.append("         .fFixedObj                = ").append(isFFixedObj()).append('\n');
        buffer.append("         .spare2                   = ").append(isSpare2()).append('\n');

        buffer.append("    .EncodingFlags        = ");
        buffer.append(" (").append(getEncodingFlags()).append(" )\n");
        buffer.append("         .fChsDiff                 = ").append(isFChsDiff()).append('\n');
        buffer.append("         .fMacChs                  = ").append(isFMacChs()).append('\n');
        buffer.append("         .fFtcAsciSym              = ").append(isFFtcAsciSym()).append('\n');
        buffer.append("         .fFtcReq                  = ").append(isFFtcReq()).append('\n');
        buffer.append("         .fLangApplied             = ").append(isFLangApplied()).append('\n');
        buffer.append("         .fSpareLangApplied        = ").append(isFSpareLangApplied()).append('\n');
        buffer.append("         .fForcedCvAuto            = ").append(isFForcedCvAuto()).append('\n');

        buffer.append("    .chse                 = ");
        buffer.append(" (").append(getChse()).append(" )\n");

        buffer.append("    .fPropMark            = ");
        buffer.append(" (").append(getFPropMark()).append(" )\n");

        buffer.append("    .ibstPropRMark        = ");
        buffer.append(" (").append(getIbstPropRMark()).append(" )\n");

        buffer.append("    .dttmPropRMark        = ");
        buffer.append(" (").append(getDttmPropRMark()).append(" )\n");

        buffer.append("    .sfxtText             = ");
        buffer.append(" (").append(getSfxtText()).append(" )\n");

        buffer.append("    .fDispFldRMark        = ");
        buffer.append(" (").append(getFDispFldRMark()).append(" )\n");

        buffer.append("    .ibstDispFldRMark     = ");
        buffer.append(" (").append(getIbstDispFldRMark()).append(" )\n");

        buffer.append("    .dttmDispFldRMark     = ");
        buffer.append(" (").append(getDttmDispFldRMark()).append(" )\n");

        buffer.append("    .xstDispFldRMark      = ");
        buffer.append(" (").append(getXstDispFldRMark()).append(" )\n");

        buffer.append("    .shd                  = ");
        buffer.append(" (").append(getShd()).append(" )\n");

        buffer.append("    .brc                  = ");
        buffer.append(" (").append(getBrc()).append(" )\n");

        buffer.append("[/CHP]\n");
        return buffer.toString();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getSize()
    {
        return 4 +  + 2 + 2 + 2 + 2 + 2 + 2 + 4 + 1 + 1 + 1 + 2 + 2 + 2 + 1 + 2 + 4 + 4 + 4 + 2 + 2 + 4 + 4 + 2 + 2 + 2 + 2 + 2 + 2 + 1 + 1 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 4 + 1 + 1 + 2 + 4 + 32 + 2 + 4;
    }



    /**
     * Get the format_flags field for the CHP record.
     */
    public int getFormat_flags()
    {
        return field_1_format_flags;
    }

    /**
     * Set the format_flags field for the CHP record.
     */
    public void setFormat_flags(int field_1_format_flags)
    {
        this.field_1_format_flags = field_1_format_flags;
    }

    /**
     * Get the format_flags1 field for the CHP record.
     */
    public int getFormat_flags1()
    {
        return field_2_format_flags1;
    }

    /**
     * Set the format_flags1 field for the CHP record.
     */
    public void setFormat_flags1(int field_2_format_flags1)
    {
        this.field_2_format_flags1 = field_2_format_flags1;
    }

    /**
     * Get the ftcAscii field for the CHP record.
     */
    public int getFtcAscii()
    {
        return field_3_ftcAscii;
    }

    /**
     * Set the ftcAscii field for the CHP record.
     */
    public void setFtcAscii(int field_3_ftcAscii)
    {
        this.field_3_ftcAscii = field_3_ftcAscii;
    }

    /**
     * Get the ftcFE field for the CHP record.
     */
    public int getFtcFE()
    {
        return field_4_ftcFE;
    }

    /**
     * Set the ftcFE field for the CHP record.
     */
    public void setFtcFE(int field_4_ftcFE)
    {
        this.field_4_ftcFE = field_4_ftcFE;
    }

    /**
     * Get the ftcOther field for the CHP record.
     */
    public int getFtcOther()
    {
        return field_5_ftcOther;
    }

    /**
     * Set the ftcOther field for the CHP record.
     */
    public void setFtcOther(int field_5_ftcOther)
    {
        this.field_5_ftcOther = field_5_ftcOther;
    }

    /**
     * Get the hps field for the CHP record.
     */
    public int getHps()
    {
        return field_6_hps;
    }

    /**
     * Set the hps field for the CHP record.
     */
    public void setHps(int field_6_hps)
    {
        this.field_6_hps = field_6_hps;
    }

    /**
     * Get the dxaSpace field for the CHP record.
     */
    public int getDxaSpace()
    {
        return field_7_dxaSpace;
    }

    /**
     * Set the dxaSpace field for the CHP record.
     */
    public void setDxaSpace(int field_7_dxaSpace)
    {
        this.field_7_dxaSpace = field_7_dxaSpace;
    }

    /**
     * Get the iss field for the CHP record.
     */
    public byte getIss()
    {
        return field_8_iss;
    }

    /**
     * Set the iss field for the CHP record.
     */
    public void setIss(byte field_8_iss)
    {
        this.field_8_iss = field_8_iss;
    }

    /**
     * Get the kul field for the CHP record.
     */
    public byte getKul()
    {
        return field_9_kul;
    }

    /**
     * Set the kul field for the CHP record.
     */
    public void setKul(byte field_9_kul)
    {
        this.field_9_kul = field_9_kul;
    }

    /**
     * Get the ico field for the CHP record.
     */
    public byte getIco()
    {
        return field_10_ico;
    }

    /**
     * Set the ico field for the CHP record.
     */
    public void setIco(byte field_10_ico)
    {
        this.field_10_ico = field_10_ico;
    }

    /**
     * Get the hpsPos field for the CHP record.
     */
    public int getHpsPos()
    {
        return field_11_hpsPos;
    }

    /**
     * Set the hpsPos field for the CHP record.
     */
    public void setHpsPos(int field_11_hpsPos)
    {
        this.field_11_hpsPos = field_11_hpsPos;
    }

    /**
     * Get the lidDefault field for the CHP record.
     */
    public int getLidDefault()
    {
        return field_12_lidDefault;
    }

    /**
     * Set the lidDefault field for the CHP record.
     */
    public void setLidDefault(int field_12_lidDefault)
    {
        this.field_12_lidDefault = field_12_lidDefault;
    }

    /**
     * Get the lidFE field for the CHP record.
     */
    public int getLidFE()
    {
        return field_13_lidFE;
    }

    /**
     * Set the lidFE field for the CHP record.
     */
    public void setLidFE(int field_13_lidFE)
    {
        this.field_13_lidFE = field_13_lidFE;
    }

    /**
     * Get the idctHint field for the CHP record.
     */
    public byte getIdctHint()
    {
        return field_14_idctHint;
    }

    /**
     * Set the idctHint field for the CHP record.
     */
    public void setIdctHint(byte field_14_idctHint)
    {
        this.field_14_idctHint = field_14_idctHint;
    }

    /**
     * Get the wCharScale field for the CHP record.
     */
    public int getWCharScale()
    {
        return field_15_wCharScale;
    }

    /**
     * Set the wCharScale field for the CHP record.
     */
    public void setWCharScale(int field_15_wCharScale)
    {
        this.field_15_wCharScale = field_15_wCharScale;
    }

    /**
     * Get the fcPic field for the CHP record.
     */
    public int getFcPic()
    {
        return field_16_fcPic;
    }

    /**
     * Set the fcPic field for the CHP record.
     */
    public void setFcPic(int field_16_fcPic)
    {
        this.field_16_fcPic = field_16_fcPic;
    }

    /**
     * Get the fcObj field for the CHP record.
     */
    public int getFcObj()
    {
        return field_17_fcObj;
    }

    /**
     * Set the fcObj field for the CHP record.
     */
    public void setFcObj(int field_17_fcObj)
    {
        this.field_17_fcObj = field_17_fcObj;
    }

    /**
     * Get the lTagObj field for the CHP record.
     */
    public int getLTagObj()
    {
        return field_18_lTagObj;
    }

    /**
     * Set the lTagObj field for the CHP record.
     */
    public void setLTagObj(int field_18_lTagObj)
    {
        this.field_18_lTagObj = field_18_lTagObj;
    }

    /**
     * Get the ibstRMark field for the CHP record.
     */
    public int getIbstRMark()
    {
        return field_19_ibstRMark;
    }

    /**
     * Set the ibstRMark field for the CHP record.
     */
    public void setIbstRMark(int field_19_ibstRMark)
    {
        this.field_19_ibstRMark = field_19_ibstRMark;
    }

    /**
     * Get the ibstRMarkDel field for the CHP record.
     */
    public int getIbstRMarkDel()
    {
        return field_20_ibstRMarkDel;
    }

    /**
     * Set the ibstRMarkDel field for the CHP record.
     */
    public void setIbstRMarkDel(int field_20_ibstRMarkDel)
    {
        this.field_20_ibstRMarkDel = field_20_ibstRMarkDel;
    }

    /**
     * Get the dttmRMark field for the CHP record.
     */
    public DateAndTime getDttmRMark()
    {
        return field_21_dttmRMark;
    }

    /**
     * Set the dttmRMark field for the CHP record.
     */
    public void setDttmRMark(DateAndTime field_21_dttmRMark)
    {
        this.field_21_dttmRMark = field_21_dttmRMark;
    }

    /**
     * Get the dttmRMarkDel field for the CHP record.
     */
    public DateAndTime getDttmRMarkDel()
    {
        return field_22_dttmRMarkDel;
    }

    /**
     * Set the dttmRMarkDel field for the CHP record.
     */
    public void setDttmRMarkDel(DateAndTime field_22_dttmRMarkDel)
    {
        this.field_22_dttmRMarkDel = field_22_dttmRMarkDel;
    }

    /**
     * Get the istd field for the CHP record.
     */
    public int getIstd()
    {
        return field_23_istd;
    }

    /**
     * Set the istd field for the CHP record.
     */
    public void setIstd(int field_23_istd)
    {
        this.field_23_istd = field_23_istd;
    }

    /**
     * Get the baseIstd field for the CHP record.
     */
    public int getBaseIstd()
    {
        return field_24_baseIstd;
    }

    /**
     * Set the baseIstd field for the CHP record.
     */
    public void setBaseIstd(int field_24_baseIstd)
    {
        this.field_24_baseIstd = field_24_baseIstd;
    }

    /**
     * Get the ftcSym field for the CHP record.
     */
    public int getFtcSym()
    {
        return field_25_ftcSym;
    }

    /**
     * Set the ftcSym field for the CHP record.
     */
    public void setFtcSym(int field_25_ftcSym)
    {
        this.field_25_ftcSym = field_25_ftcSym;
    }

    /**
     * Get the xchSym field for the CHP record.
     */
    public int getXchSym()
    {
        return field_26_xchSym;
    }

    /**
     * Set the xchSym field for the CHP record.
     */
    public void setXchSym(int field_26_xchSym)
    {
        this.field_26_xchSym = field_26_xchSym;
    }

    /**
     * Get the idslRMReason field for the CHP record.
     */
    public int getIdslRMReason()
    {
        return field_27_idslRMReason;
    }

    /**
     * Set the idslRMReason field for the CHP record.
     */
    public void setIdslRMReason(int field_27_idslRMReason)
    {
        this.field_27_idslRMReason = field_27_idslRMReason;
    }

    /**
     * Get the idslReasonDel field for the CHP record.
     */
    public int getIdslReasonDel()
    {
        return field_28_idslReasonDel;
    }

    /**
     * Set the idslReasonDel field for the CHP record.
     */
    public void setIdslReasonDel(int field_28_idslReasonDel)
    {
        this.field_28_idslReasonDel = field_28_idslReasonDel;
    }

    /**
     * Get the ysr field for the CHP record.
     */
    public byte getYsr()
    {
        return field_29_ysr;
    }

    /**
     * Set the ysr field for the CHP record.
     */
    public void setYsr(byte field_29_ysr)
    {
        this.field_29_ysr = field_29_ysr;
    }

    /**
     * Get the chYsr field for the CHP record.
     */
    public byte getChYsr()
    {
        return field_30_chYsr;
    }

    /**
     * Set the chYsr field for the CHP record.
     */
    public void setChYsr(byte field_30_chYsr)
    {
        this.field_30_chYsr = field_30_chYsr;
    }

    /**
     * Get the hpsKern field for the CHP record.
     */
    public int getHpsKern()
    {
        return field_31_hpsKern;
    }

    /**
     * Set the hpsKern field for the CHP record.
     */
    public void setHpsKern(int field_31_hpsKern)
    {
        this.field_31_hpsKern = field_31_hpsKern;
    }

    /**
     * Get the Highlight field for the CHP record.
     */
    public short getHighlight()
    {
        return field_32_Highlight;
    }

    /**
     * Set the Highlight field for the CHP record.
     */
    public void setHighlight(short field_32_Highlight)
    {
        this.field_32_Highlight = field_32_Highlight;
    }

    /**
     * Get the InternalFlags field for the CHP record.
     */
    public short getInternalFlags()
    {
        return field_33_InternalFlags;
    }

    /**
     * Set the InternalFlags field for the CHP record.
     */
    public void setInternalFlags(short field_33_InternalFlags)
    {
        this.field_33_InternalFlags = field_33_InternalFlags;
    }

    /**
     * Get the EncodingFlags field for the CHP record.
     */
    public short getEncodingFlags()
    {
        return field_34_EncodingFlags;
    }

    /**
     * Set the EncodingFlags field for the CHP record.
     */
    public void setEncodingFlags(short field_34_EncodingFlags)
    {
        this.field_34_EncodingFlags = field_34_EncodingFlags;
    }

    /**
     * used to record a character set id for text that was pasted into the Word document that used a character set different than Word‘s default character set.
     */
    public short getChse()
    {
        return field_35_chse;
    }

    /**
     * used to record a character set id for text that was pasted into the Word document that used a character set different than Word‘s default character set.
     */
    public void setChse(short field_35_chse)
    {
        this.field_35_chse = field_35_chse;
    }

    /**
     * Get the fPropMark field for the CHP record.
     */
    public short getFPropMark()
    {
        return field_36_fPropMark;
    }

    /**
     * Set the fPropMark field for the CHP record.
     */
    public void setFPropMark(short field_36_fPropMark)
    {
        this.field_36_fPropMark = field_36_fPropMark;
    }

    /**
     * Get the ibstPropRMark field for the CHP record.
     */
    public int getIbstPropRMark()
    {
        return field_37_ibstPropRMark;
    }

    /**
     * Set the ibstPropRMark field for the CHP record.
     */
    public void setIbstPropRMark(int field_37_ibstPropRMark)
    {
        this.field_37_ibstPropRMark = field_37_ibstPropRMark;
    }

    /**
     * Get the dttmPropRMark field for the CHP record.
     */
    public DateAndTime getDttmPropRMark()
    {
        return field_38_dttmPropRMark;
    }

    /**
     * Set the dttmPropRMark field for the CHP record.
     */
    public void setDttmPropRMark(DateAndTime field_38_dttmPropRMark)
    {
        this.field_38_dttmPropRMark = field_38_dttmPropRMark;
    }

    /**
     * Get the sfxtText field for the CHP record.
     */
    public byte getSfxtText()
    {
        return field_39_sfxtText;
    }

    /**
     * Set the sfxtText field for the CHP record.
     */
    public void setSfxtText(byte field_39_sfxtText)
    {
        this.field_39_sfxtText = field_39_sfxtText;
    }

    /**
     * Get the fDispFldRMark field for the CHP record.
     */
    public byte getFDispFldRMark()
    {
        return field_40_fDispFldRMark;
    }

    /**
     * Set the fDispFldRMark field for the CHP record.
     */
    public void setFDispFldRMark(byte field_40_fDispFldRMark)
    {
        this.field_40_fDispFldRMark = field_40_fDispFldRMark;
    }

    /**
     * Get the ibstDispFldRMark field for the CHP record.
     */
    public int getIbstDispFldRMark()
    {
        return field_41_ibstDispFldRMark;
    }

    /**
     * Set the ibstDispFldRMark field for the CHP record.
     */
    public void setIbstDispFldRMark(int field_41_ibstDispFldRMark)
    {
        this.field_41_ibstDispFldRMark = field_41_ibstDispFldRMark;
    }

    /**
     * Get the dttmDispFldRMark field for the CHP record.
     */
    public DateAndTime getDttmDispFldRMark()
    {
        return field_42_dttmDispFldRMark;
    }

    /**
     * Set the dttmDispFldRMark field for the CHP record.
     */
    public void setDttmDispFldRMark(DateAndTime field_42_dttmDispFldRMark)
    {
        this.field_42_dttmDispFldRMark = field_42_dttmDispFldRMark;
    }

    /**
     * Get the xstDispFldRMark field for the CHP record.
     */
    public byte[] getXstDispFldRMark()
    {
        return field_43_xstDispFldRMark;
    }

    /**
     * Set the xstDispFldRMark field for the CHP record.
     */
    public void setXstDispFldRMark(byte[] field_43_xstDispFldRMark)
    {
        this.field_43_xstDispFldRMark = field_43_xstDispFldRMark;
    }

    /**
     * Get the shd field for the CHP record.
     */
    public ShadingDescriptor getShd()
    {
        return field_44_shd;
    }

    /**
     * Set the shd field for the CHP record.
     */
    public void setShd(ShadingDescriptor field_44_shd)
    {
        this.field_44_shd = field_44_shd;
    }

    /**
     * Get the brc field for the CHP record.
     */
    public BorderCode getBrc()
    {
        return field_45_brc;
    }

    /**
     * Set the brc field for the CHP record.
     */
    public void setBrc(BorderCode field_45_brc)
    {
        this.field_45_brc = field_45_brc;
    }

    /**
     * Sets the fBold field value.
     * 
     */
    public void setFBold(boolean value)
    {
        field_1_format_flags = (int)fBold.setBoolean(field_1_format_flags, value);

        
    }

    /**
     * 
     * @return  the fBold field value.
     */
    public boolean isFBold()
    {
        return fBold.isSet(field_1_format_flags);
        
    }

    /**
     * Sets the fItalic field value.
     * 
     */
    public void setFItalic(boolean value)
    {
        field_1_format_flags = (int)fItalic.setBoolean(field_1_format_flags, value);

        
    }

    /**
     * 
     * @return  the fItalic field value.
     */
    public boolean isFItalic()
    {
        return fItalic.isSet(field_1_format_flags);
        
    }

    /**
     * Sets the fRMarkDel field value.
     * 
     */
    public void setFRMarkDel(boolean value)
    {
        field_1_format_flags = (int)fRMarkDel.setBoolean(field_1_format_flags, value);

        
    }

    /**
     * 
     * @return  the fRMarkDel field value.
     */
    public boolean isFRMarkDel()
    {
        return fRMarkDel.isSet(field_1_format_flags);
        
    }

    /**
     * Sets the fOutline field value.
     * 
     */
    public void setFOutline(boolean value)
    {
        field_1_format_flags = (int)fOutline.setBoolean(field_1_format_flags, value);

        
    }

    /**
     * 
     * @return  the fOutline field value.
     */
    public boolean isFOutline()
    {
        return fOutline.isSet(field_1_format_flags);
        
    }

    /**
     * Sets the fFldVanish field value.
     * 
     */
    public void setFFldVanish(boolean value)
    {
        field_1_format_flags = (int)fFldVanish.setBoolean(field_1_format_flags, value);

        
    }

    /**
     * 
     * @return  the fFldVanish field value.
     */
    public boolean isFFldVanish()
    {
        return fFldVanish.isSet(field_1_format_flags);
        
    }

    /**
     * Sets the fSmallCaps field value.
     * 
     */
    public void setFSmallCaps(boolean value)
    {
        field_1_format_flags = (int)fSmallCaps.setBoolean(field_1_format_flags, value);

        
    }

    /**
     * 
     * @return  the fSmallCaps field value.
     */
    public boolean isFSmallCaps()
    {
        return fSmallCaps.isSet(field_1_format_flags);
        
    }

    /**
     * Sets the fCaps field value.
     * 
     */
    public void setFCaps(boolean value)
    {
        field_1_format_flags = (int)fCaps.setBoolean(field_1_format_flags, value);

        
    }

    /**
     * 
     * @return  the fCaps field value.
     */
    public boolean isFCaps()
    {
        return fCaps.isSet(field_1_format_flags);
        
    }

    /**
     * Sets the fVanish field value.
     * 
     */
    public void setFVanish(boolean value)
    {
        field_1_format_flags = (int)fVanish.setBoolean(field_1_format_flags, value);

        
    }

    /**
     * 
     * @return  the fVanish field value.
     */
    public boolean isFVanish()
    {
        return fVanish.isSet(field_1_format_flags);
        
    }

    /**
     * Sets the fRMark field value.
     * 
     */
    public void setFRMark(boolean value)
    {
        field_1_format_flags = (int)fRMark.setBoolean(field_1_format_flags, value);

        
    }

    /**
     * 
     * @return  the fRMark field value.
     */
    public boolean isFRMark()
    {
        return fRMark.isSet(field_1_format_flags);
        
    }

    /**
     * Sets the fSpec field value.
     * 
     */
    public void setFSpec(boolean value)
    {
        field_1_format_flags = (int)fSpec.setBoolean(field_1_format_flags, value);

        
    }

    /**
     * 
     * @return  the fSpec field value.
     */
    public boolean isFSpec()
    {
        return fSpec.isSet(field_1_format_flags);
        
    }

    /**
     * Sets the fStrike field value.
     * 
     */
    public void setFStrike(boolean value)
    {
        field_1_format_flags = (int)fStrike.setBoolean(field_1_format_flags, value);

        
    }

    /**
     * 
     * @return  the fStrike field value.
     */
    public boolean isFStrike()
    {
        return fStrike.isSet(field_1_format_flags);
        
    }

    /**
     * Sets the fObj field value.
     * 
     */
    public void setFObj(boolean value)
    {
        field_1_format_flags = (int)fObj.setBoolean(field_1_format_flags, value);

        
    }

    /**
     * 
     * @return  the fObj field value.
     */
    public boolean isFObj()
    {
        return fObj.isSet(field_1_format_flags);
        
    }

    /**
     * Sets the fShadow field value.
     * 
     */
    public void setFShadow(boolean value)
    {
        field_1_format_flags = (int)fShadow.setBoolean(field_1_format_flags, value);

        
    }

    /**
     * 
     * @return  the fShadow field value.
     */
    public boolean isFShadow()
    {
        return fShadow.isSet(field_1_format_flags);
        
    }

    /**
     * Sets the fLowerCase field value.
     * 
     */
    public void setFLowerCase(boolean value)
    {
        field_1_format_flags = (int)fLowerCase.setBoolean(field_1_format_flags, value);

        
    }

    /**
     * 
     * @return  the fLowerCase field value.
     */
    public boolean isFLowerCase()
    {
        return fLowerCase.isSet(field_1_format_flags);
        
    }

    /**
     * Sets the fData field value.
     * 
     */
    public void setFData(boolean value)
    {
        field_1_format_flags = (int)fData.setBoolean(field_1_format_flags, value);

        
    }

    /**
     * 
     * @return  the fData field value.
     */
    public boolean isFData()
    {
        return fData.isSet(field_1_format_flags);
        
    }

    /**
     * Sets the fOle2 field value.
     * 
     */
    public void setFOle2(boolean value)
    {
        field_1_format_flags = (int)fOle2.setBoolean(field_1_format_flags, value);

        
    }

    /**
     * 
     * @return  the fOle2 field value.
     */
    public boolean isFOle2()
    {
        return fOle2.isSet(field_1_format_flags);
        
    }

    /**
     * Sets the fEmboss field value.
     * 
     */
    public void setFEmboss(boolean value)
    {
        field_2_format_flags1 = (int)fEmboss.setBoolean(field_2_format_flags1, value);

        
    }

    /**
     * 
     * @return  the fEmboss field value.
     */
    public boolean isFEmboss()
    {
        return fEmboss.isSet(field_2_format_flags1);
        
    }

    /**
     * Sets the fImprint field value.
     * 
     */
    public void setFImprint(boolean value)
    {
        field_2_format_flags1 = (int)fImprint.setBoolean(field_2_format_flags1, value);

        
    }

    /**
     * 
     * @return  the fImprint field value.
     */
    public boolean isFImprint()
    {
        return fImprint.isSet(field_2_format_flags1);
        
    }

    /**
     * Sets the fDStrike field value.
     * 
     */
    public void setFDStrike(boolean value)
    {
        field_2_format_flags1 = (int)fDStrike.setBoolean(field_2_format_flags1, value);

        
    }

    /**
     * 
     * @return  the fDStrike field value.
     */
    public boolean isFDStrike()
    {
        return fDStrike.isSet(field_2_format_flags1);
        
    }

    /**
     * Sets the fUsePgsuSettings field value.
     * 
     */
    public void setFUsePgsuSettings(boolean value)
    {
        field_2_format_flags1 = (int)fUsePgsuSettings.setBoolean(field_2_format_flags1, value);

        
    }

    /**
     * 
     * @return  the fUsePgsuSettings field value.
     */
    public boolean isFUsePgsuSettings()
    {
        return fUsePgsuSettings.isSet(field_2_format_flags1);
        
    }

    /**
     * Sets the icoHighlight field value.
     * Highlight color (see chp.ico)
     */
    public void setIcoHighlight(byte value)
    {
        field_32_Highlight = (short)icoHighlight.setValue(field_32_Highlight, value);

        
    }

    /**
     * Highlight color (see chp.ico)
     * @return  the icoHighlight field value.
     */
    public byte getIcoHighlight()
    {
        return ( byte )icoHighlight.getValue(field_32_Highlight);
        
    }

    /**
     * Sets the fHighlight field value.
     * When 1, characters are highlighted with color specified by chp.icoHighlight
     */
    public void setFHighlight(boolean value)
    {
        field_32_Highlight = (short)fHighlight.setBoolean(field_32_Highlight, value);

        
    }

    /**
     * When 1, characters are highlighted with color specified by chp.icoHighlight
     * @return  the fHighlight field value.
     */
    public boolean isFHighlight()
    {
        return fHighlight.isSet(field_32_Highlight);
        
    }

    /**
     * Sets the fNavHighlight field value.
     * Used internally by Word
     */
    public void setFNavHighlight(boolean value)
    {
        field_32_Highlight = (short)fNavHighlight.setBoolean(field_32_Highlight, value);

        
    }

    /**
     * Used internally by Word
     * @return  the fNavHighlight field value.
     */
    public boolean isFNavHighlight()
    {
        return fNavHighlight.isSet(field_32_Highlight);
        
    }

    /**
     * Sets the iatrUndetType field value.
     * Used internally by Word
     */
    public void setIatrUndetType(byte value)
    {
        field_33_InternalFlags = (short)iatrUndetType.setValue(field_33_InternalFlags, value);

        
    }

    /**
     * Used internally by Word
     * @return  the iatrUndetType field value.
     */
    public byte getIatrUndetType()
    {
        return ( byte )iatrUndetType.getValue(field_33_InternalFlags);
        
    }

    /**
     * Sets the fUlGap field value.
     * Used internally by Word 8
     */
    public void setFUlGap(boolean value)
    {
        field_33_InternalFlags = (short)fUlGap.setBoolean(field_33_InternalFlags, value);

        
    }

    /**
     * Used internally by Word 8
     * @return  the fUlGap field value.
     */
    public boolean isFUlGap()
    {
        return fUlGap.isSet(field_33_InternalFlags);
        
    }

    /**
     * Sets the fScriptAnchor field value.
     * Used internally by Word
     */
    public void setFScriptAnchor(boolean value)
    {
        field_33_InternalFlags = (short)fScriptAnchor.setBoolean(field_33_InternalFlags, value);

        
    }

    /**
     * Used internally by Word
     * @return  the fScriptAnchor field value.
     */
    public boolean isFScriptAnchor()
    {
        return fScriptAnchor.isSet(field_33_InternalFlags);
        
    }

    /**
     * Sets the fFixedObj field value.
     * Used internally by Word
     */
    public void setFFixedObj(boolean value)
    {
        field_33_InternalFlags = (short)fFixedObj.setBoolean(field_33_InternalFlags, value);

        
    }

    /**
     * Used internally by Word
     * @return  the fFixedObj field value.
     */
    public boolean isFFixedObj()
    {
        return fFixedObj.isSet(field_33_InternalFlags);
        
    }

    /**
     * Sets the spare2 field value.
     * Not used
     */
    public void setSpare2(boolean value)
    {
        field_33_InternalFlags = (short)spare2.setBoolean(field_33_InternalFlags, value);

        
    }

    /**
     * Not used
     * @return  the spare2 field value.
     */
    public boolean isSpare2()
    {
        return spare2.isSet(field_33_InternalFlags);
        
    }

    /**
     * Sets the fChsDiff field value.
     * Pre-Unicode files, char's char set different from FIB char set
     */
    public void setFChsDiff(boolean value)
    {
        field_34_EncodingFlags = (short)fChsDiff.setBoolean(field_34_EncodingFlags, value);

        
    }

    /**
     * Pre-Unicode files, char's char set different from FIB char set
     * @return  the fChsDiff field value.
     */
    public boolean isFChsDiff()
    {
        return fChsDiff.isSet(field_34_EncodingFlags);
        
    }

    /**
     * Sets the fMacChs field value.
     * fTrue if char's are Macintosh char set
     */
    public void setFMacChs(boolean value)
    {
        field_34_EncodingFlags = (short)fMacChs.setBoolean(field_34_EncodingFlags, value);

        
    }

    /**
     * fTrue if char's are Macintosh char set
     * @return  the fMacChs field value.
     */
    public boolean isFMacChs()
    {
        return fMacChs.isSet(field_34_EncodingFlags);
        
    }

    /**
     * Sets the fFtcAsciSym field value.
     * Used internally by Word
     */
    public void setFFtcAsciSym(boolean value)
    {
        field_34_EncodingFlags = (short)fFtcAsciSym.setBoolean(field_34_EncodingFlags, value);

        
    }

    /**
     * Used internally by Word
     * @return  the fFtcAsciSym field value.
     */
    public boolean isFFtcAsciSym()
    {
        return fFtcAsciSym.isSet(field_34_EncodingFlags);
        
    }

    /**
     * Sets the fFtcReq field value.
     * Used internally by Word
     */
    public void setFFtcReq(boolean value)
    {
        field_34_EncodingFlags = (short)fFtcReq.setBoolean(field_34_EncodingFlags, value);

        
    }

    /**
     * Used internally by Word
     * @return  the fFtcReq field value.
     */
    public boolean isFFtcReq()
    {
        return fFtcReq.isSet(field_34_EncodingFlags);
        
    }

    /**
     * Sets the fLangApplied field value.
     * Used internally by Word
     */
    public void setFLangApplied(boolean value)
    {
        field_34_EncodingFlags = (short)fLangApplied.setBoolean(field_34_EncodingFlags, value);

        
    }

    /**
     * Used internally by Word
     * @return  the fLangApplied field value.
     */
    public boolean isFLangApplied()
    {
        return fLangApplied.isSet(field_34_EncodingFlags);
        
    }

    /**
     * Sets the fSpareLangApplied field value.
     * Used internally by Word
     */
    public void setFSpareLangApplied(boolean value)
    {
        field_34_EncodingFlags = (short)fSpareLangApplied.setBoolean(field_34_EncodingFlags, value);

        
    }

    /**
     * Used internally by Word
     * @return  the fSpareLangApplied field value.
     */
    public boolean isFSpareLangApplied()
    {
        return fSpareLangApplied.isSet(field_34_EncodingFlags);
        
    }

    /**
     * Sets the fForcedCvAuto field value.
     * Used internally by Word
     */
    public void setFForcedCvAuto(boolean value)
    {
        field_34_EncodingFlags = (short)fForcedCvAuto.setBoolean(field_34_EncodingFlags, value);

        
    }

    /**
     * Used internally by Word
     * @return  the fForcedCvAuto field value.
     */
    public boolean isFForcedCvAuto()
    {
        return fForcedCvAuto.isSet(field_34_EncodingFlags);
        
    }



}
