
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
 * Character Properties.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author S. Ryan Ackley
 */
public abstract class CHPAbstractType
    implements HDFType
{

    private  int field_1_format_flags;
        private BitField  fBold = new BitField(0x0001);
        private BitField  fItalic = new BitField(0x0002);
        private BitField  fRMarkDel = new BitField(0x0004);
        private BitField  fOutline = new BitField(0x0008);
        private BitField  fFldVanish = new BitField(0x0010);
        private BitField  fSmallCaps = new BitField(0x0020);
        private BitField  fCaps = new BitField(0x0040);
        private BitField  fVanish = new BitField(0x0080);
        private BitField  fRMark = new BitField(0x0100);
        private BitField  fSpec = new BitField(0x0200);
        private BitField  fStrike = new BitField(0x0400);
        private BitField  fObj = new BitField(0x0800);
        private BitField  fShadow = new BitField(0x1000);
        private BitField  fLowerCase = new BitField(0x2000);
        private BitField  fData = new BitField(0x4000);
        private BitField  fOle2 = new BitField(0x8000);
    private  int field_2_format_flags1;
        private BitField  fEmboss = new BitField(0x0001);
        private BitField  fImprint = new BitField(0x0002);
        private BitField  fDStrike = new BitField(0x0004);
        private BitField  fUsePgsuSettings = new BitField(0x0008);
    private  int field_3_ftcAscii;
    private  int field_4_ftcFE;
    private  int field_5_ftcOther;
    private  int field_6_hps;
    private  int field_7_dxaSpace;
    private  int field_8_iss;
    private  int field_9_kul;
    private  int field_10_ico;
    private  int field_11_hpsPos;
    private  int field_12_lidDefault;
    private  int field_13_lidFE;
    private  int field_14_idctHint;
    private  int field_15_wCharScale;
    private  int field_16_FC;
    private  int field_17_ibstRMark;
    private  int field_18_ibstRMarkDel;
    private  int field_19_istd;
    private  int field_20_ftcSym;
    private  int field_21_xchSym;
    private  int field_22_idslRMReason;
    private  int field_23_idslReasonDel;
    private  int field_24_ysr;
    private  int field_25_chYsr;
    private  int field_26_hpsKern;
    private  int field_27_icoHighlight;
    private  int field_28_fHighlight;
    private  int field_29_fPropMark;
    private  int field_30_ibstPropRMark;
    private  int field_31_dttmPropRMark;
    private  int field_32_sfxtText;
    private  int field_33_fDispFldRMark;
    private  int field_34_ibstDispFldRMark;
    private  int field_35_dttmDispFldRMark;
    private  int field_36_shd;
    private  int field_37_brc;


    public CHPAbstractType()
    {

    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getSize()
    {
        return 4 + 2 + 2 + 2 + 2 + 2 + 2 + 4 + 1 + 1 + 1 + 2 + 2 + 2 + 1 + 2 + 4 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 1 + 1 + 2 + 1 + 1 + 2 + 2 + 4 + 1 + 1 + 2 + 4 + 2 + 2;
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
    public int getIss()
    {
        return field_8_iss;
    }

    /**
     * Set the iss field for the CHP record.
     */
    public void setIss(int field_8_iss)
    {
        this.field_8_iss = field_8_iss;
    }

    /**
     * Get the kul field for the CHP record.
     */
    public int getKul()
    {
        return field_9_kul;
    }

    /**
     * Set the kul field for the CHP record.
     */
    public void setKul(int field_9_kul)
    {
        this.field_9_kul = field_9_kul;
    }

    /**
     * Get the ico field for the CHP record.
     */
    public int getIco()
    {
        return field_10_ico;
    }

    /**
     * Set the ico field for the CHP record.
     */
    public void setIco(int field_10_ico)
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
    public int getIdctHint()
    {
        return field_14_idctHint;
    }

    /**
     * Set the idctHint field for the CHP record.
     */
    public void setIdctHint(int field_14_idctHint)
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
     * Get the FC field for the CHP record.
     */
    public int getFC()
    {
        return field_16_FC;
    }

    /**
     * Set the FC field for the CHP record.
     */
    public void setFC(int field_16_FC)
    {
        this.field_16_FC = field_16_FC;
    }

    /**
     * Get the ibstRMark field for the CHP record.
     */
    public int getIbstRMark()
    {
        return field_17_ibstRMark;
    }

    /**
     * Set the ibstRMark field for the CHP record.
     */
    public void setIbstRMark(int field_17_ibstRMark)
    {
        this.field_17_ibstRMark = field_17_ibstRMark;
    }

    /**
     * Get the ibstRMarkDel field for the CHP record.
     */
    public int getIbstRMarkDel()
    {
        return field_18_ibstRMarkDel;
    }

    /**
     * Set the ibstRMarkDel field for the CHP record.
     */
    public void setIbstRMarkDel(int field_18_ibstRMarkDel)
    {
        this.field_18_ibstRMarkDel = field_18_ibstRMarkDel;
    }

    /**
     * Get the istd field for the CHP record.
     */
    public int getIstd()
    {
        return field_19_istd;
    }

    /**
     * Set the istd field for the CHP record.
     */
    public void setIstd(int field_19_istd)
    {
        this.field_19_istd = field_19_istd;
    }

    /**
     * Get the ftcSym field for the CHP record.
     */
    public int getFtcSym()
    {
        return field_20_ftcSym;
    }

    /**
     * Set the ftcSym field for the CHP record.
     */
    public void setFtcSym(int field_20_ftcSym)
    {
        this.field_20_ftcSym = field_20_ftcSym;
    }

    /**
     * Get the xchSym field for the CHP record.
     */
    public int getXchSym()
    {
        return field_21_xchSym;
    }

    /**
     * Set the xchSym field for the CHP record.
     */
    public void setXchSym(int field_21_xchSym)
    {
        this.field_21_xchSym = field_21_xchSym;
    }

    /**
     * Get the idslRMReason field for the CHP record.
     */
    public int getIdslRMReason()
    {
        return field_22_idslRMReason;
    }

    /**
     * Set the idslRMReason field for the CHP record.
     */
    public void setIdslRMReason(int field_22_idslRMReason)
    {
        this.field_22_idslRMReason = field_22_idslRMReason;
    }

    /**
     * Get the idslReasonDel field for the CHP record.
     */
    public int getIdslReasonDel()
    {
        return field_23_idslReasonDel;
    }

    /**
     * Set the idslReasonDel field for the CHP record.
     */
    public void setIdslReasonDel(int field_23_idslReasonDel)
    {
        this.field_23_idslReasonDel = field_23_idslReasonDel;
    }

    /**
     * Get the ysr field for the CHP record.
     */
    public int getYsr()
    {
        return field_24_ysr;
    }

    /**
     * Set the ysr field for the CHP record.
     */
    public void setYsr(int field_24_ysr)
    {
        this.field_24_ysr = field_24_ysr;
    }

    /**
     * Get the chYsr field for the CHP record.
     */
    public int getChYsr()
    {
        return field_25_chYsr;
    }

    /**
     * Set the chYsr field for the CHP record.
     */
    public void setChYsr(int field_25_chYsr)
    {
        this.field_25_chYsr = field_25_chYsr;
    }

    /**
     * Get the hpsKern field for the CHP record.
     */
    public int getHpsKern()
    {
        return field_26_hpsKern;
    }

    /**
     * Set the hpsKern field for the CHP record.
     */
    public void setHpsKern(int field_26_hpsKern)
    {
        this.field_26_hpsKern = field_26_hpsKern;
    }

    /**
     * Get the icoHighlight field for the CHP record.
     */
    public int getIcoHighlight()
    {
        return field_27_icoHighlight;
    }

    /**
     * Set the icoHighlight field for the CHP record.
     */
    public void setIcoHighlight(int field_27_icoHighlight)
    {
        this.field_27_icoHighlight = field_27_icoHighlight;
    }

    /**
     * Get the fHighlight field for the CHP record.
     */
    public int getFHighlight()
    {
        return field_28_fHighlight;
    }

    /**
     * Set the fHighlight field for the CHP record.
     */
    public void setFHighlight(int field_28_fHighlight)
    {
        this.field_28_fHighlight = field_28_fHighlight;
    }

    /**
     * Get the fPropMark field for the CHP record.
     */
    public int getFPropMark()
    {
        return field_29_fPropMark;
    }

    /**
     * Set the fPropMark field for the CHP record.
     */
    public void setFPropMark(int field_29_fPropMark)
    {
        this.field_29_fPropMark = field_29_fPropMark;
    }

    /**
     * Get the ibstPropRMark field for the CHP record.
     */
    public int getIbstPropRMark()
    {
        return field_30_ibstPropRMark;
    }

    /**
     * Set the ibstPropRMark field for the CHP record.
     */
    public void setIbstPropRMark(int field_30_ibstPropRMark)
    {
        this.field_30_ibstPropRMark = field_30_ibstPropRMark;
    }

    /**
     * Get the dttmPropRMark field for the CHP record.
     */
    public int getDttmPropRMark()
    {
        return field_31_dttmPropRMark;
    }

    /**
     * Set the dttmPropRMark field for the CHP record.
     */
    public void setDttmPropRMark(int field_31_dttmPropRMark)
    {
        this.field_31_dttmPropRMark = field_31_dttmPropRMark;
    }

    /**
     * Get the sfxtText field for the CHP record.
     */
    public int getSfxtText()
    {
        return field_32_sfxtText;
    }

    /**
     * Set the sfxtText field for the CHP record.
     */
    public void setSfxtText(int field_32_sfxtText)
    {
        this.field_32_sfxtText = field_32_sfxtText;
    }

    /**
     * Get the fDispFldRMark field for the CHP record.
     */
    public int getFDispFldRMark()
    {
        return field_33_fDispFldRMark;
    }

    /**
     * Set the fDispFldRMark field for the CHP record.
     */
    public void setFDispFldRMark(int field_33_fDispFldRMark)
    {
        this.field_33_fDispFldRMark = field_33_fDispFldRMark;
    }

    /**
     * Get the ibstDispFldRMark field for the CHP record.
     */
    public int getIbstDispFldRMark()
    {
        return field_34_ibstDispFldRMark;
    }

    /**
     * Set the ibstDispFldRMark field for the CHP record.
     */
    public void setIbstDispFldRMark(int field_34_ibstDispFldRMark)
    {
        this.field_34_ibstDispFldRMark = field_34_ibstDispFldRMark;
    }

    /**
     * Get the dttmDispFldRMark field for the CHP record.
     */
    public int getDttmDispFldRMark()
    {
        return field_35_dttmDispFldRMark;
    }

    /**
     * Set the dttmDispFldRMark field for the CHP record.
     */
    public void setDttmDispFldRMark(int field_35_dttmDispFldRMark)
    {
        this.field_35_dttmDispFldRMark = field_35_dttmDispFldRMark;
    }

    /**
     * Get the shd field for the CHP record.
     */
    public int getShd()
    {
        return field_36_shd;
    }

    /**
     * Set the shd field for the CHP record.
     */
    public void setShd(int field_36_shd)
    {
        this.field_36_shd = field_36_shd;
    }

    /**
     * Get the brc field for the CHP record.
     */
    public int getBrc()
    {
        return field_37_brc;
    }

    /**
     * Set the brc field for the CHP record.
     */
    public void setBrc(int field_37_brc)
    {
        this.field_37_brc = field_37_brc;
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


}  // END OF CLASS




