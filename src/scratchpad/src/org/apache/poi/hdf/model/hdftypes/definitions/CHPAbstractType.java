
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

    private  short      field_1_formatFlags;
    private BitField   fBold                                      = new BitField(0x0001);
    private BitField   fItalic                                    = new BitField(0x0002);
    private BitField   fRMarkDel                                  = new BitField(0x0004);
    private BitField   fOutline                                   = new BitField(0x0008);
    private BitField   fFldVanish                                 = new BitField(0x0010);
    private BitField   fSmallCaps                                 = new BitField(0x0020);
    private BitField   fCaps                                      = new BitField(0x0040);
    private BitField   fVanish                                    = new BitField(0x0080);
    private BitField   fRMark                                     = new BitField(0x0100);
    private BitField   fSpec                                      = new BitField(0x0200);
    private BitField   fStrike                                    = new BitField(0x0400);
    private BitField   fObj                                       = new BitField(0x0800);
    private BitField   fShadow                                    = new BitField(0x1000);
    private BitField   fLowerCase                                 = new BitField(0x2000);
    private BitField   fData                                      = new BitField(0x4000);
    private BitField   fOle2                                      = new BitField(0x8000);
    private  short      field_2_formatFlags1;
    private BitField   fEmboss                                    = new BitField(0x0001);
    private BitField   fImprint                                   = new BitField(0x0002);
    private BitField   fDStrike                                   = new BitField(0x0004);
    private BitField   fUsePgsuSettings                           = new BitField(0x0008);
    private  short      field_3_ftcAscii;
    private  short      field_4_ftcFE;
    private  short      field_5_ftcOther;
    private  short      field_6_hps;
    private  int        field_7_dxaSpace;
    private  byte       field_8_iss;
    private  byte       field_9_kul;
    private  byte       field_10_ico;
    private  short      field_11_hpsPos;
    private  short      field_12_lidDefault;
    private  short      field_13_lidFE;
    private  byte       field_14_idctHint;
    private  short      field_15_wCharScale;
    private  int        field_16_FC;
    private  short      field_17_ibstRMark;
    private  short      field_18_ibstRMarkDel;
    private  short      field_19_istd;
    private  short      field_20_ftcSym;
    private  short      field_21_xchSym;
    private  short      field_22_idslRMReason;
    private  short      field_23_idslReasonDel;
    private  byte       field_24_ysr;
    private  byte       field_25_chYsr;
    private  short      field_26_hpsKern;
    private  byte       field_27_icoHighlight;
    private  byte       field_28_fHighlight;
    private  short      field_29_fPropMark;
    private  short      field_30_ibstPropRMark;
    private  int        field_31_dttmPropRMark;
    private  byte       field_32_sfxtText;
    private  byte       field_33_fDispFldRMark;
    private  short      field_34_ibstDispFldRMark;
    private  int        field_35_dttmDispFldRMark;
    private  short      field_36_shd;
    private  short      field_37_brc;


    public CHPAbstractType()
    {

    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_formatFlags             = LittleEndian.getShort(data, 0x0 + offset);
        field_2_formatFlags1            = LittleEndian.getShort(data, 0x2 + offset);
        field_3_ftcAscii                = LittleEndian.getShort(data, 0x4 + offset);
        field_4_ftcFE                   = LittleEndian.getShort(data, 0x6 + offset);
        field_5_ftcOther                = LittleEndian.getShort(data, 0x8 + offset);
        field_6_hps                     = LittleEndian.getShort(data, 0xa + offset);
        field_7_dxaSpace                = LittleEndian.getInt(data, 0xc + offset);
        field_8_iss                     = data[ 0x10 + offset ];
        field_9_kul                     = data[ 0x11 + offset ];
        field_10_ico                    = data[ 0x12 + offset ];
        field_11_hpsPos                 = LittleEndian.getShort(data, 0x13 + offset);
        field_12_lidDefault             = LittleEndian.getShort(data, 0x15 + offset);
        field_13_lidFE                  = LittleEndian.getShort(data, 0x17 + offset);
        field_14_idctHint               = data[ 0x19 + offset ];
        field_15_wCharScale             = LittleEndian.getShort(data, 0x1a + offset);
        field_16_FC                     = LittleEndian.getInt(data, 0x1c + offset);
        field_17_ibstRMark              = LittleEndian.getShort(data, 0x20 + offset);
        field_18_ibstRMarkDel           = LittleEndian.getShort(data, 0x22 + offset);
        field_19_istd                   = LittleEndian.getShort(data, 0x24 + offset);
        field_20_ftcSym                 = LittleEndian.getShort(data, 0x26 + offset);
        field_21_xchSym                 = LittleEndian.getShort(data, 0x28 + offset);
        field_22_idslRMReason           = LittleEndian.getShort(data, 0x2a + offset);
        field_23_idslReasonDel          = LittleEndian.getShort(data, 0x2c + offset);
        field_24_ysr                    = data[ 0x2e + offset ];
        field_25_chYsr                  = data[ 0x2f + offset ];
        field_26_hpsKern                = LittleEndian.getShort(data, 0x30 + offset);
        field_27_icoHighlight           = data[ 0x32 + offset ];
        field_28_fHighlight             = data[ 0x33 + offset ];
        field_29_fPropMark              = LittleEndian.getShort(data, 0x34 + offset);
        field_30_ibstPropRMark          = LittleEndian.getShort(data, 0x36 + offset);
        field_31_dttmPropRMark          = LittleEndian.getInt(data, 0x38 + offset);
        field_32_sfxtText               = data[ 0x3c + offset ];
        field_33_fDispFldRMark          = data[ 0x3d + offset ];
        field_34_ibstDispFldRMark       = LittleEndian.getShort(data, 0x3e + offset);
        field_35_dttmDispFldRMark       = LittleEndian.getInt(data, 0x40 + offset);
        field_36_shd                    = LittleEndian.getShort(data, 0x44 + offset);
        field_37_brc                    = LittleEndian.getShort(data, 0x46 + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[CHP]\n");

        buffer.append("    .formatFlags          = ")
            .append("0x")
            .append(HexDump.toHex((short)getFormatFlags()))
            .append(" (").append(getFormatFlags()).append(" )\n");
        buffer.append("         .fBold                    = ").append(isFBold               ()).append('\n');
        buffer.append("         .fItalic                  = ").append(isFItalic             ()).append('\n');
        buffer.append("         .fRMarkDel                = ").append(isFRMarkDel           ()).append('\n');
        buffer.append("         .fOutline                 = ").append(isFOutline            ()).append('\n');
        buffer.append("         .fFldVanish               = ").append(isFFldVanish          ()).append('\n');
        buffer.append("         .fSmallCaps               = ").append(isFSmallCaps          ()).append('\n');
        buffer.append("         .fCaps                    = ").append(isFCaps               ()).append('\n');
        buffer.append("         .fVanish                  = ").append(isFVanish             ()).append('\n');
        buffer.append("         .fRMark                   = ").append(isFRMark              ()).append('\n');
        buffer.append("         .fSpec                    = ").append(isFSpec               ()).append('\n');
        buffer.append("         .fStrike                  = ").append(isFStrike             ()).append('\n');
        buffer.append("         .fObj                     = ").append(isFObj                ()).append('\n');
        buffer.append("         .fShadow                  = ").append(isFShadow             ()).append('\n');
        buffer.append("         .fLowerCase               = ").append(isFLowerCase          ()).append('\n');
        buffer.append("         .fData                    = ").append(isFData               ()).append('\n');
        buffer.append("         .fOle2                    = ").append(isFOle2               ()).append('\n');

        buffer.append("    .formatFlags1         = ")
            .append("0x")
            .append(HexDump.toHex((short)getFormatFlags1()))
            .append(" (").append(getFormatFlags1()).append(" )\n");
        buffer.append("         .fEmboss                  = ").append(isFEmboss             ()).append('\n');
        buffer.append("         .fImprint                 = ").append(isFImprint            ()).append('\n');
        buffer.append("         .fDStrike                 = ").append(isFDStrike            ()).append('\n');
        buffer.append("         .fUsePgsuSettings         = ").append(isFUsePgsuSettings    ()).append('\n');

        buffer.append("    .ftcAscii             = ")
            .append("0x")
            .append(HexDump.toHex((short)getFtcAscii()))
            .append(" (").append(getFtcAscii()).append(" )\n");

        buffer.append("    .ftcFE                = ")
            .append("0x")
            .append(HexDump.toHex((short)getFtcFE()))
            .append(" (").append(getFtcFE()).append(" )\n");

        buffer.append("    .ftcOther             = ")
            .append("0x")
            .append(HexDump.toHex((short)getFtcOther()))
            .append(" (").append(getFtcOther()).append(" )\n");

        buffer.append("    .hps                  = ")
            .append("0x")
            .append(HexDump.toHex((short)getHps()))
            .append(" (").append(getHps()).append(" )\n");

        buffer.append("    .dxaSpace             = ")
            .append("0x")
            .append(HexDump.toHex((int)getDxaSpace()))
            .append(" (").append(getDxaSpace()).append(" )\n");

        buffer.append("    .iss                  = ")
            .append("0x")
            .append(HexDump.toHex((byte)getIss()))
            .append(" (").append(getIss()).append(" )\n");

        buffer.append("    .kul                  = ")
            .append("0x")
            .append(HexDump.toHex((byte)getKul()))
            .append(" (").append(getKul()).append(" )\n");

        buffer.append("    .ico                  = ")
            .append("0x")
            .append(HexDump.toHex((byte)getIco()))
            .append(" (").append(getIco()).append(" )\n");

        buffer.append("    .hpsPos               = ")
            .append("0x")
            .append(HexDump.toHex((short)getHpsPos()))
            .append(" (").append(getHpsPos()).append(" )\n");

        buffer.append("    .lidDefault           = ")
            .append("0x")
            .append(HexDump.toHex((short)getLidDefault()))
            .append(" (").append(getLidDefault()).append(" )\n");

        buffer.append("    .lidFE                = ")
            .append("0x")
            .append(HexDump.toHex((short)getLidFE()))
            .append(" (").append(getLidFE()).append(" )\n");

        buffer.append("    .idctHint             = ")
            .append("0x")
            .append(HexDump.toHex((byte)getIdctHint()))
            .append(" (").append(getIdctHint()).append(" )\n");

        buffer.append("    .wCharScale           = ")
            .append("0x")
            .append(HexDump.toHex((short)getWCharScale()))
            .append(" (").append(getWCharScale()).append(" )\n");

        buffer.append("    .FC                   = ")
            .append("0x")
            .append(HexDump.toHex((int)getFC()))
            .append(" (").append(getFC()).append(" )\n");

        buffer.append("    .ibstRMark            = ")
            .append("0x")
            .append(HexDump.toHex((short)getIbstRMark()))
            .append(" (").append(getIbstRMark()).append(" )\n");

        buffer.append("    .ibstRMarkDel         = ")
            .append("0x")
            .append(HexDump.toHex((short)getIbstRMarkDel()))
            .append(" (").append(getIbstRMarkDel()).append(" )\n");

        buffer.append("    .istd                 = ")
            .append("0x")
            .append(HexDump.toHex((short)getIstd()))
            .append(" (").append(getIstd()).append(" )\n");

        buffer.append("    .ftcSym               = ")
            .append("0x")
            .append(HexDump.toHex((short)getFtcSym()))
            .append(" (").append(getFtcSym()).append(" )\n");

        buffer.append("    .xchSym               = ")
            .append("0x")
            .append(HexDump.toHex((short)getXchSym()))
            .append(" (").append(getXchSym()).append(" )\n");

        buffer.append("    .idslRMReason         = ")
            .append("0x")
            .append(HexDump.toHex((short)getIdslRMReason()))
            .append(" (").append(getIdslRMReason()).append(" )\n");

        buffer.append("    .idslReasonDel        = ")
            .append("0x")
            .append(HexDump.toHex((short)getIdslReasonDel()))
            .append(" (").append(getIdslReasonDel()).append(" )\n");

        buffer.append("    .ysr                  = ")
            .append("0x")
            .append(HexDump.toHex((byte)getYsr()))
            .append(" (").append(getYsr()).append(" )\n");

        buffer.append("    .chYsr                = ")
            .append("0x")
            .append(HexDump.toHex((byte)getChYsr()))
            .append(" (").append(getChYsr()).append(" )\n");

        buffer.append("    .hpsKern              = ")
            .append("0x")
            .append(HexDump.toHex((short)getHpsKern()))
            .append(" (").append(getHpsKern()).append(" )\n");

        buffer.append("    .icoHighlight         = ")
            .append("0x")
            .append(HexDump.toHex((byte)getIcoHighlight()))
            .append(" (").append(getIcoHighlight()).append(" )\n");

        buffer.append("    .fHighlight           = ")
            .append("0x")
            .append(HexDump.toHex((byte)getFHighlight()))
            .append(" (").append(getFHighlight()).append(" )\n");

        buffer.append("    .fPropMark            = ")
            .append("0x")
            .append(HexDump.toHex((short)getFPropMark()))
            .append(" (").append(getFPropMark()).append(" )\n");

        buffer.append("    .ibstPropRMark        = ")
            .append("0x")
            .append(HexDump.toHex((short)getIbstPropRMark()))
            .append(" (").append(getIbstPropRMark()).append(" )\n");

        buffer.append("    .dttmPropRMark        = ")
            .append("0x")
            .append(HexDump.toHex((int)getDttmPropRMark()))
            .append(" (").append(getDttmPropRMark()).append(" )\n");

        buffer.append("    .sfxtText             = ")
            .append("0x")
            .append(HexDump.toHex((byte)getSfxtText()))
            .append(" (").append(getSfxtText()).append(" )\n");

        buffer.append("    .fDispFldRMark        = ")
            .append("0x")
            .append(HexDump.toHex((byte)getFDispFldRMark()))
            .append(" (").append(getFDispFldRMark()).append(" )\n");

        buffer.append("    .ibstDispFldRMark     = ")
            .append("0x")
            .append(HexDump.toHex((short)getIbstDispFldRMark()))
            .append(" (").append(getIbstDispFldRMark()).append(" )\n");

        buffer.append("    .dttmDispFldRMark     = ")
            .append("0x")
            .append(HexDump.toHex((int)getDttmDispFldRMark()))
            .append(" (").append(getDttmDispFldRMark()).append(" )\n");

        buffer.append("    .shd                  = ")
            .append("0x")
            .append(HexDump.toHex((short)getShd()))
            .append(" (").append(getShd()).append(" )\n");

        buffer.append("    .brc                  = ")
            .append("0x")
            .append(HexDump.toHex((short)getBrc()))
            .append(" (").append(getBrc()).append(" )\n");

        buffer.append("[/CHP]\n");
        return buffer.toString();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getSize()
    {
        return 4 + 2 + 2 + 2 + 2 + 2 + 2 + 4 + 1 + 1 + 1 + 2 + 2 + 2 + 1 + 2 + 4 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 1 + 1 + 2 + 1 + 1 + 2 + 2 + 4 + 1 + 1 + 2 + 4 + 2 + 2;
    }



    /**
     * Get the format flags field for the CHP record.
     */
    public short getFormatFlags()
    {
        return field_1_formatFlags;
    }

    /**
     * Set the format flags field for the CHP record.
     */
    public void setFormatFlags(short field_1_formatFlags)
    {
        this.field_1_formatFlags = field_1_formatFlags;
    }

    /**
     * Get the format flags1 field for the CHP record.
     */
    public short getFormatFlags1()
    {
        return field_2_formatFlags1;
    }

    /**
     * Set the format flags1 field for the CHP record.
     */
    public void setFormatFlags1(short field_2_formatFlags1)
    {
        this.field_2_formatFlags1 = field_2_formatFlags1;
    }

    /**
     * Get the ftcAscii field for the CHP record.
     */
    public short getFtcAscii()
    {
        return field_3_ftcAscii;
    }

    /**
     * Set the ftcAscii field for the CHP record.
     */
    public void setFtcAscii(short field_3_ftcAscii)
    {
        this.field_3_ftcAscii = field_3_ftcAscii;
    }

    /**
     * Get the ftcFE field for the CHP record.
     */
    public short getFtcFE()
    {
        return field_4_ftcFE;
    }

    /**
     * Set the ftcFE field for the CHP record.
     */
    public void setFtcFE(short field_4_ftcFE)
    {
        this.field_4_ftcFE = field_4_ftcFE;
    }

    /**
     * Get the ftcOther field for the CHP record.
     */
    public short getFtcOther()
    {
        return field_5_ftcOther;
    }

    /**
     * Set the ftcOther field for the CHP record.
     */
    public void setFtcOther(short field_5_ftcOther)
    {
        this.field_5_ftcOther = field_5_ftcOther;
    }

    /**
     * Get the hps field for the CHP record.
     */
    public short getHps()
    {
        return field_6_hps;
    }

    /**
     * Set the hps field for the CHP record.
     */
    public void setHps(short field_6_hps)
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
    public short getHpsPos()
    {
        return field_11_hpsPos;
    }

    /**
     * Set the hpsPos field for the CHP record.
     */
    public void setHpsPos(short field_11_hpsPos)
    {
        this.field_11_hpsPos = field_11_hpsPos;
    }

    /**
     * Get the lidDefault field for the CHP record.
     */
    public short getLidDefault()
    {
        return field_12_lidDefault;
    }

    /**
     * Set the lidDefault field for the CHP record.
     */
    public void setLidDefault(short field_12_lidDefault)
    {
        this.field_12_lidDefault = field_12_lidDefault;
    }

    /**
     * Get the lidFE field for the CHP record.
     */
    public short getLidFE()
    {
        return field_13_lidFE;
    }

    /**
     * Set the lidFE field for the CHP record.
     */
    public void setLidFE(short field_13_lidFE)
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
    public short getWCharScale()
    {
        return field_15_wCharScale;
    }

    /**
     * Set the wCharScale field for the CHP record.
     */
    public void setWCharScale(short field_15_wCharScale)
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
    public short getIbstRMark()
    {
        return field_17_ibstRMark;
    }

    /**
     * Set the ibstRMark field for the CHP record.
     */
    public void setIbstRMark(short field_17_ibstRMark)
    {
        this.field_17_ibstRMark = field_17_ibstRMark;
    }

    /**
     * Get the ibstRMarkDel field for the CHP record.
     */
    public short getIbstRMarkDel()
    {
        return field_18_ibstRMarkDel;
    }

    /**
     * Set the ibstRMarkDel field for the CHP record.
     */
    public void setIbstRMarkDel(short field_18_ibstRMarkDel)
    {
        this.field_18_ibstRMarkDel = field_18_ibstRMarkDel;
    }

    /**
     * Get the istd field for the CHP record.
     */
    public short getIstd()
    {
        return field_19_istd;
    }

    /**
     * Set the istd field for the CHP record.
     */
    public void setIstd(short field_19_istd)
    {
        this.field_19_istd = field_19_istd;
    }

    /**
     * Get the ftcSym field for the CHP record.
     */
    public short getFtcSym()
    {
        return field_20_ftcSym;
    }

    /**
     * Set the ftcSym field for the CHP record.
     */
    public void setFtcSym(short field_20_ftcSym)
    {
        this.field_20_ftcSym = field_20_ftcSym;
    }

    /**
     * Get the xchSym field for the CHP record.
     */
    public short getXchSym()
    {
        return field_21_xchSym;
    }

    /**
     * Set the xchSym field for the CHP record.
     */
    public void setXchSym(short field_21_xchSym)
    {
        this.field_21_xchSym = field_21_xchSym;
    }

    /**
     * Get the idslRMReason field for the CHP record.
     */
    public short getIdslRMReason()
    {
        return field_22_idslRMReason;
    }

    /**
     * Set the idslRMReason field for the CHP record.
     */
    public void setIdslRMReason(short field_22_idslRMReason)
    {
        this.field_22_idslRMReason = field_22_idslRMReason;
    }

    /**
     * Get the idslReasonDel field for the CHP record.
     */
    public short getIdslReasonDel()
    {
        return field_23_idslReasonDel;
    }

    /**
     * Set the idslReasonDel field for the CHP record.
     */
    public void setIdslReasonDel(short field_23_idslReasonDel)
    {
        this.field_23_idslReasonDel = field_23_idslReasonDel;
    }

    /**
     * Get the ysr field for the CHP record.
     */
    public byte getYsr()
    {
        return field_24_ysr;
    }

    /**
     * Set the ysr field for the CHP record.
     */
    public void setYsr(byte field_24_ysr)
    {
        this.field_24_ysr = field_24_ysr;
    }

    /**
     * Get the chYsr field for the CHP record.
     */
    public byte getChYsr()
    {
        return field_25_chYsr;
    }

    /**
     * Set the chYsr field for the CHP record.
     */
    public void setChYsr(byte field_25_chYsr)
    {
        this.field_25_chYsr = field_25_chYsr;
    }

    /**
     * Get the hpsKern field for the CHP record.
     */
    public short getHpsKern()
    {
        return field_26_hpsKern;
    }

    /**
     * Set the hpsKern field for the CHP record.
     */
    public void setHpsKern(short field_26_hpsKern)
    {
        this.field_26_hpsKern = field_26_hpsKern;
    }

    /**
     * Get the icoHighlight field for the CHP record.
     */
    public byte getIcoHighlight()
    {
        return field_27_icoHighlight;
    }

    /**
     * Set the icoHighlight field for the CHP record.
     */
    public void setIcoHighlight(byte field_27_icoHighlight)
    {
        this.field_27_icoHighlight = field_27_icoHighlight;
    }

    /**
     * Get the fHighlight field for the CHP record.
     */
    public byte getFHighlight()
    {
        return field_28_fHighlight;
    }

    /**
     * Set the fHighlight field for the CHP record.
     */
    public void setFHighlight(byte field_28_fHighlight)
    {
        this.field_28_fHighlight = field_28_fHighlight;
    }

    /**
     * Get the fPropMark field for the CHP record.
     */
    public short getFPropMark()
    {
        return field_29_fPropMark;
    }

    /**
     * Set the fPropMark field for the CHP record.
     */
    public void setFPropMark(short field_29_fPropMark)
    {
        this.field_29_fPropMark = field_29_fPropMark;
    }

    /**
     * Get the ibstPropRMark field for the CHP record.
     */
    public short getIbstPropRMark()
    {
        return field_30_ibstPropRMark;
    }

    /**
     * Set the ibstPropRMark field for the CHP record.
     */
    public void setIbstPropRMark(short field_30_ibstPropRMark)
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
    public byte getSfxtText()
    {
        return field_32_sfxtText;
    }

    /**
     * Set the sfxtText field for the CHP record.
     */
    public void setSfxtText(byte field_32_sfxtText)
    {
        this.field_32_sfxtText = field_32_sfxtText;
    }

    /**
     * Get the fDispFldRMark field for the CHP record.
     */
    public byte getFDispFldRMark()
    {
        return field_33_fDispFldRMark;
    }

    /**
     * Set the fDispFldRMark field for the CHP record.
     */
    public void setFDispFldRMark(byte field_33_fDispFldRMark)
    {
        this.field_33_fDispFldRMark = field_33_fDispFldRMark;
    }

    /**
     * Get the ibstDispFldRMark field for the CHP record.
     */
    public short getIbstDispFldRMark()
    {
        return field_34_ibstDispFldRMark;
    }

    /**
     * Set the ibstDispFldRMark field for the CHP record.
     */
    public void setIbstDispFldRMark(short field_34_ibstDispFldRMark)
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
    public short getShd()
    {
        return field_36_shd;
    }

    /**
     * Set the shd field for the CHP record.
     */
    public void setShd(short field_36_shd)
    {
        this.field_36_shd = field_36_shd;
    }

    /**
     * Get the brc field for the CHP record.
     */
    public short getBrc()
    {
        return field_37_brc;
    }

    /**
     * Set the brc field for the CHP record.
     */
    public void setBrc(short field_37_brc)
    {
        this.field_37_brc = field_37_brc;
    }

    /**
     * Sets the fBold field value.
     * 
     */
    public void setFBold(boolean value)
    {
        field_1_formatFlags = fBold.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * 
     * @return  the fBold field value.
     */
    public boolean isFBold()
    {
        return fBold.isSet(field_1_formatFlags);
    }

    /**
     * Sets the fItalic field value.
     * 
     */
    public void setFItalic(boolean value)
    {
        field_1_formatFlags = fItalic.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * 
     * @return  the fItalic field value.
     */
    public boolean isFItalic()
    {
        return fItalic.isSet(field_1_formatFlags);
    }

    /**
     * Sets the fRMarkDel field value.
     * 
     */
    public void setFRMarkDel(boolean value)
    {
        field_1_formatFlags = fRMarkDel.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * 
     * @return  the fRMarkDel field value.
     */
    public boolean isFRMarkDel()
    {
        return fRMarkDel.isSet(field_1_formatFlags);
    }

    /**
     * Sets the fOutline field value.
     * 
     */
    public void setFOutline(boolean value)
    {
        field_1_formatFlags = fOutline.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * 
     * @return  the fOutline field value.
     */
    public boolean isFOutline()
    {
        return fOutline.isSet(field_1_formatFlags);
    }

    /**
     * Sets the fFldVanish field value.
     * 
     */
    public void setFFldVanish(boolean value)
    {
        field_1_formatFlags = fFldVanish.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * 
     * @return  the fFldVanish field value.
     */
    public boolean isFFldVanish()
    {
        return fFldVanish.isSet(field_1_formatFlags);
    }

    /**
     * Sets the fSmallCaps field value.
     * 
     */
    public void setFSmallCaps(boolean value)
    {
        field_1_formatFlags = fSmallCaps.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * 
     * @return  the fSmallCaps field value.
     */
    public boolean isFSmallCaps()
    {
        return fSmallCaps.isSet(field_1_formatFlags);
    }

    /**
     * Sets the fCaps field value.
     * 
     */
    public void setFCaps(boolean value)
    {
        field_1_formatFlags = fCaps.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * 
     * @return  the fCaps field value.
     */
    public boolean isFCaps()
    {
        return fCaps.isSet(field_1_formatFlags);
    }

    /**
     * Sets the fVanish field value.
     * 
     */
    public void setFVanish(boolean value)
    {
        field_1_formatFlags = fVanish.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * 
     * @return  the fVanish field value.
     */
    public boolean isFVanish()
    {
        return fVanish.isSet(field_1_formatFlags);
    }

    /**
     * Sets the fRMark field value.
     * 
     */
    public void setFRMark(boolean value)
    {
        field_1_formatFlags = fRMark.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * 
     * @return  the fRMark field value.
     */
    public boolean isFRMark()
    {
        return fRMark.isSet(field_1_formatFlags);
    }

    /**
     * Sets the fSpec field value.
     * 
     */
    public void setFSpec(boolean value)
    {
        field_1_formatFlags = fSpec.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * 
     * @return  the fSpec field value.
     */
    public boolean isFSpec()
    {
        return fSpec.isSet(field_1_formatFlags);
    }

    /**
     * Sets the fStrike field value.
     * 
     */
    public void setFStrike(boolean value)
    {
        field_1_formatFlags = fStrike.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * 
     * @return  the fStrike field value.
     */
    public boolean isFStrike()
    {
        return fStrike.isSet(field_1_formatFlags);
    }

    /**
     * Sets the fObj field value.
     * 
     */
    public void setFObj(boolean value)
    {
        field_1_formatFlags = fObj.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * 
     * @return  the fObj field value.
     */
    public boolean isFObj()
    {
        return fObj.isSet(field_1_formatFlags);
    }

    /**
     * Sets the fShadow field value.
     * 
     */
    public void setFShadow(boolean value)
    {
        field_1_formatFlags = fShadow.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * 
     * @return  the fShadow field value.
     */
    public boolean isFShadow()
    {
        return fShadow.isSet(field_1_formatFlags);
    }

    /**
     * Sets the fLowerCase field value.
     * 
     */
    public void setFLowerCase(boolean value)
    {
        field_1_formatFlags = fLowerCase.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * 
     * @return  the fLowerCase field value.
     */
    public boolean isFLowerCase()
    {
        return fLowerCase.isSet(field_1_formatFlags);
    }

    /**
     * Sets the fData field value.
     * 
     */
    public void setFData(boolean value)
    {
        field_1_formatFlags = fData.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * 
     * @return  the fData field value.
     */
    public boolean isFData()
    {
        return fData.isSet(field_1_formatFlags);
    }

    /**
     * Sets the fOle2 field value.
     * 
     */
    public void setFOle2(boolean value)
    {
        field_1_formatFlags = fOle2.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * 
     * @return  the fOle2 field value.
     */
    public boolean isFOle2()
    {
        return fOle2.isSet(field_1_formatFlags);
    }

    /**
     * Sets the fEmboss field value.
     * 
     */
    public void setFEmboss(boolean value)
    {
        field_2_formatFlags1 = fEmboss.setShortBoolean(field_2_formatFlags1, value);
    }

    /**
     * 
     * @return  the fEmboss field value.
     */
    public boolean isFEmboss()
    {
        return fEmboss.isSet(field_2_formatFlags1);
    }

    /**
     * Sets the fImprint field value.
     * 
     */
    public void setFImprint(boolean value)
    {
        field_2_formatFlags1 = fImprint.setShortBoolean(field_2_formatFlags1, value);
    }

    /**
     * 
     * @return  the fImprint field value.
     */
    public boolean isFImprint()
    {
        return fImprint.isSet(field_2_formatFlags1);
    }

    /**
     * Sets the fDStrike field value.
     * 
     */
    public void setFDStrike(boolean value)
    {
        field_2_formatFlags1 = fDStrike.setShortBoolean(field_2_formatFlags1, value);
    }

    /**
     * 
     * @return  the fDStrike field value.
     */
    public boolean isFDStrike()
    {
        return fDStrike.isSet(field_2_formatFlags1);
    }

    /**
     * Sets the fUsePgsuSettings field value.
     * 
     */
    public void setFUsePgsuSettings(boolean value)
    {
        field_2_formatFlags1 = fUsePgsuSettings.setShortBoolean(field_2_formatFlags1, value);
    }

    /**
     * 
     * @return  the fUsePgsuSettings field value.
     */
    public boolean isFUsePgsuSettings()
    {
        return fUsePgsuSettings.isSet(field_2_formatFlags1);
    }


}  // END OF CLASS




