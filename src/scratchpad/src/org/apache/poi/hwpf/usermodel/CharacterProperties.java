/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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

package org.apache.poi.hwpf.usermodel;

import org.apache.poi.hwpf.model.hdftypes.definitions.CHPAbstractType;
import org.apache.poi.hwpf.model.hdftypes.StyleDescription;

import org.apache.poi.hwpf.sprm.SprmBuffer;

public class CharacterProperties
  extends CHPAbstractType implements Cloneable
{
  public final static short SPRM_FRMARKDEL = (short)0x0800;
  public final static short SPRM_FRMARK = 0x0801;
  public final static short SPRM_FFLDVANISH = 0x0802;
  public final static short SPRM_PICLOCATION = 0x6A03;
  public final static short SPRM_IBSTRMARK = 0x4804;
  public final static short SPRM_DTTMRMARK = 0x6805;
  public final static short SPRM_FDATA = 0x0806;
  public final static short SPRM_SYMBOL = 0x6A09;
  public final static short SPRM_FOLE2 = 0x080A;
  public final static short SPRM_HIGHLIGHT = 0x2A0C;
  public final static short SPRM_OBJLOCATION = 0x680E;
  public final static short SPRM_ISTD = 0x4A30;
  public final static short SPRM_FBOLD = 0x0835;
  public final static short SPRM_FITALIC = 0x0836;
  public final static short SPRM_FSTRIKE = 0x0837;
  public final static short SPRM_FOUTLINE = 0x0838;
  public final static short SPRM_FSHADOW = 0x0839;
  public final static short SPRM_FSMALLCAPS = 0x083A;
  public final static short SPRM_FCAPS = 0x083B;
  public final static short SPRM_FVANISH = 0x083C;
  public final static short SPRM_KUL = 0x2A3E;
  public final static short SPRM_DXASPACE = (short)0x8840;
  public final static short SPRM_LID = 0x4A41;
  public final static short SPRM_ICO = 0x2A42;
  public final static short SPRM_HPS = 0x4A43;
  public final static short SPRM_HPSPOS = 0x4845;
  public final static short SPRM_ISS = 0x2A48;
  public final static short SPRM_HPSKERN = 0x484B;
  public final static short SPRM_YSRI = 0x484E;
  public final static short SPRM_RGFTCASCII = 0x4A4F;
  public final static short SPRM_RGFTCFAREAST = 0x4A50;
  public final static short SPRM_RGFTCNOTFAREAST = 0x4A51;
  public final static short SPRM_CHARSCALE = 0x4852;
  public final static short SPRM_FDSTRIKE = 0x2A53;
  public final static short SPRM_FIMPRINT = 0x0854;
  public final static short SPRM_FSPEC = 0x0855;
  public final static short SPRM_FOBJ = 0x0856;
  public final static short SPRM_PROPRMARK = (short)0xCA57;
  public final static short SPRM_FEMBOSS = 0x0858;
  public final static short SPRM_SFXTEXT = 0x2859;
  public final static short SPRM_DISPFLDRMARK = (short)0xCA62;
  public final static short SPRM_IBSTRMARKDEL = 0x4863;
  public final static short SPRM_DTTMRMARKDEL = 0x6864;
  public final static short SPRM_BRC = 0x6865;
  public final static short SPRM_SHD = 0x4866;
  public final static short SPRM_IDSIRMARKDEL = 0x4867;
  public final static short SPRM_CPG = 0x486B;
  public final static short SPRM_NONFELID = 0x486D;
  public final static short SPRM_FELID = 0x486E;
  public final static short SPRM_IDCTHINT = 0x286F;


  StyleDescription _baseStyle;
  SprmBuffer _chpx;

  public CharacterProperties()
  {
    field_17_fcPic = -1;
    field_22_dttmRMark = new DateAndTime();
    field_23_dttmRMarkDel = new DateAndTime();
    field_36_dttmPropRMark = new DateAndTime();
    field_40_dttmDispFldRMark = new DateAndTime();
    field_41_xstDispFldRMark = new byte[36];
    field_42_shd = new ShadingDescriptor();
    field_43_brc = new BorderCode();
    field_7_hps = 20;
    field_24_istd = 10;
    field_16_wCharScale = 100;
    field_13_lidDefault = 0x0400;
    field_14_lidFE = 0x0400;
  }

  public boolean isMarkedDeleted()
  {
    return isFRMarkDel();
  }

  public void markDeleted(boolean mark)
  {
    if (mark != isFRMarkDel() && mark != _baseStyle.getCHP().isFRMarkDel())
    {
      byte newVal = (byte)(mark ? 1 : 0);
      _chpx.addSprm(SPRM_FRMARKDEL, newVal);
      super.setFRMarkDel(mark);
    }
  }

  public boolean isBold()
  {
    return isFBold();
  }

  public void setBold(boolean bold)
  {
    if (bold != isFBold() && bold != _baseStyle.getCHP().isFBold())
    {
      byte newVal = (byte)(bold ? 1 : 0);
      _chpx.addSprm(SPRM_FBOLD, newVal);
      super.setFBold(bold);
    }
  }

  public boolean isItalic()
  {
    return isFItalic();
  }

  public void setItalic(boolean italic)
  {
    if (italic != isFItalic() && italic != _baseStyle.getCHP().isFItalic())
    {
      byte newVal = (byte)(italic ? 1 : 0);
      _chpx.addSprm(SPRM_FITALIC, newVal);
      super.setFItalic(italic);
    }
  }

  public boolean isOutlined()
  {
    return isFOutline();
  }

  public void setOutline(boolean outlined)
  {
    if (outlined != isFOutline() && outlined != _baseStyle.getCHP().isFOutline())
    {
      byte newVal = (byte)(outlined ? 1 : 0);
      _chpx.addSprm(SPRM_FOUTLINE, newVal);
      super.setFOutline(outlined);
    }

  }

  public boolean isFldVanished()
  {
    return isFFldVanish();
  }

  public void setFldVanish(boolean fldVanish)
  {
    if (fldVanish != isFFldVanish() && fldVanish != _baseStyle.getCHP().isFFldVanish())
    {
      byte newVal = (byte)(fldVanish ? 1 : 0);
      _chpx.addSprm(SPRM_FFLDVANISH, newVal);
      super.setFFldVanish(fldVanish);
    }

  }
  public boolean isSmallCaps()
  {
    return isFSmallCaps();
  }

  public void setSmallCaps(boolean smallCaps)
  {
    if (smallCaps != isFSmallCaps() && smallCaps != _baseStyle.getCHP().isFSmallCaps())
    {
      byte newVal = (byte)(smallCaps ? 1 : 0);
      _chpx.addSprm(SPRM_FSMALLCAPS, newVal);
      super.setFSmallCaps(smallCaps);
    }
  }
  public boolean isCapitalized()
  {
    return isFCaps();
  }

  public void setCapitalized(boolean caps)
  {
    if (caps != isFCaps() && caps != _baseStyle.getCHP().isFCaps())
    {
      byte newVal = (byte)(caps ? 1 : 0);
      _chpx.addSprm(SPRM_FCAPS, newVal);
      super.setFCaps(caps);
    }
  }

  public boolean isVanished()
  {
    return isFVanish();
  }

  public void setVanished(boolean vanish)
  {
    if (vanish != isFVanish() && vanish != _baseStyle.getCHP().isFVanish())
    {
      byte newVal = (byte)(vanish ? 1 : 0);
      _chpx.addSprm(SPRM_FVANISH, newVal);
      super.setFVanish(vanish);
    }

  }
  public boolean isMarkedInserted()
  {
    return isFRMark();
  }

  public void markInserted(boolean mark)
  {
    if (mark != isFRMark() && mark != _baseStyle.getCHP().isFRMark())
    {
      byte newVal = (byte)(mark ? 1 : 0);
      _chpx.addSprm(SPRM_FRMARK, newVal);
      super.setFRMark(mark);
    }
  }

  public boolean isStrikeThrough()
  {
    return isFStrike();
  }

  public void strikeThrough(boolean strike)
  {
    if (strike != isFStrike() && strike != _baseStyle.getCHP().isFStrike())
    {
      byte newVal = (byte)(strike ? 1 : 0);
      _chpx.addSprm(SPRM_FSTRIKE, newVal);
      super.setFStrike(strike);
    }

  }
  public boolean isShadowed()
  {
    return isFShadow();
  }

  public void setShadow(boolean shadow)
  {
    if (shadow != isFShadow() && shadow != _baseStyle.getCHP().isFShadow())
    {
      byte newVal = (byte)(shadow ? 1 : 0);
      _chpx.addSprm(SPRM_FSHADOW, newVal);
      super.setFShadow(shadow);
    }

  }

  public boolean isEmbossed()
  {
    return isFEmboss();
  }

  public void setEmbossed(boolean emboss)
  {
    if (emboss != isFEmboss() && emboss != _baseStyle.getCHP().isFEmboss())
    {
      byte newVal = (byte)(emboss ? 1 : 0);
      _chpx.addSprm(SPRM_FEMBOSS, newVal);
      super.setFEmboss(emboss);
    }

  }


  public Object clone()
    throws CloneNotSupportedException
  {
    CharacterProperties cp = (CharacterProperties)super.clone();
    cp.field_22_dttmRMark = (DateAndTime)field_22_dttmRMark.clone();
    cp.field_23_dttmRMarkDel = (DateAndTime)field_23_dttmRMarkDel.clone();
    cp.field_36_dttmPropRMark = (DateAndTime)field_36_dttmPropRMark.clone();
    cp.field_40_dttmDispFldRMark = (DateAndTime)field_40_dttmDispFldRMark.clone();
    cp.field_41_xstDispFldRMark = (byte[])field_41_xstDispFldRMark.clone();
    cp.field_42_shd = (ShadingDescriptor)field_42_shd.clone();

    return cp;
  }


}
