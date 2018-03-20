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

package org.apache.poi.hwpf.usermodel;

import org.apache.poi.hwpf.model.Colorref;
import org.apache.poi.hwpf.model.types.CHPAbstractType;

/**
 * @author Ryan Ackley
 */
public final class CharacterProperties
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
    /*
     * Microsoft Office Word 97-2007 Binary File Format (.doc) Specification;
     * Page 60 of 210
     */
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
    /**
     * change chp.cv
     */
    public final static short SPRM_CCV = 0x6870;

    public CharacterProperties()
    {
        setFUsePgsuSettings( true );
        setXstDispFldRMark( new byte[36] );
    }

  public boolean isMarkedDeleted()
  {
    return isFRMarkDel();
  }

  public void markDeleted(boolean mark)
  {
    super.setFRMarkDel(mark);
  }

  public boolean isBold()
  {
    return isFBold();
  }

  public void setBold(boolean bold)
  {
    super.setFBold(bold);
  }

  public boolean isItalic()
  {
    return isFItalic();
  }

  public void setItalic(boolean italic)
  {
    super.setFItalic(italic);
  }

  public boolean isOutlined()
  {
    return isFOutline();
  }

  public void setOutline(boolean outlined)
  {
    super.setFOutline(outlined);
  }

  public boolean isFldVanished()
  {
    return isFFldVanish();
  }

  public void setFldVanish(boolean fldVanish)
  {
    super.setFFldVanish(fldVanish);
  }

  public boolean isSmallCaps()
  {
    return isFSmallCaps();
  }

  public void setSmallCaps(boolean smallCaps)
  {
    super.setFSmallCaps(smallCaps);
  }

  public boolean isCapitalized()
  {
    return isFCaps();
  }

  public void setCapitalized(boolean caps)
  {
    super.setFCaps(caps);
  }

  public boolean isVanished()
  {
    return isFVanish();
  }

  public void setVanished(boolean vanish)
  {
    super.setFVanish(vanish);

  }
  public boolean isMarkedInserted()
  {
    return isFRMark();
  }

  public void markInserted(boolean mark)
  {
    super.setFRMark(mark);
  }

  public boolean isStrikeThrough()
  {
    return isFStrike();
  }

  public void strikeThrough(boolean strike)
  {
    super.setFStrike(strike);
  }

  public boolean isShadowed()
  {
    return isFShadow();
  }

  public void setShadow(boolean shadow)
  {
    super.setFShadow(shadow);

  }

  public boolean isEmbossed()
  {
    return isFEmboss();
  }

  public void setEmbossed(boolean emboss)
  {
    super.setFEmboss(emboss);
  }

  public boolean isImprinted()
  {
    return isFImprint();
  }

  public void setImprinted(boolean imprint)
  {
    super.setFImprint(imprint);
  }

  public boolean isDoubleStrikeThrough()
  {
    return isFDStrike();
  }

  public void setDoubleStrikeThrough(boolean dstrike)
  {
    super.setFDStrike(dstrike);
  }

  public int getFontSize()
  {
    return getHps();
  }

  public void setFontSize(int halfPoints)
  {
    super.setHps(halfPoints);
  }

  public int getCharacterSpacing()
  {
    return getDxaSpace();
  }

  public void setCharacterSpacing(int twips)
  {
    super.setDxaSpace(twips);
  }

  public short getSubSuperScriptIndex()
  {
    return getIss();
  }

  public void setSubSuperScriptIndex(short iss)
  {
    super.setDxaSpace(iss);
  }

  public int getUnderlineCode()
  {
    return super.getKul();
  }

  public void setUnderlineCode(int kul)
  {
    super.setKul((byte)kul);
  }

  public int getColor()
  {
    return super.getIco();
  }

  public void setColor(int color)
  {
    super.setIco((byte)color);
  }

  public int getVerticalOffset()
  {
    return super.getHpsPos();
  }

  public void setVerticalOffset(int hpsPos)
  {
    super.setHpsPos((short) hpsPos);
  }

  public int getKerning()
  {
    return super.getHpsKern();
  }

  public void setKerning(int kern)
  {
    super.setHpsKern(kern);
  }

  public boolean isHighlighted()
  {
    return super.isFHighlight();
  }

  public void setHighlighted(byte color)
  {
    super.setIcoHighlight(color);
  }

    /**
     * Get the ico24 field for the CHP record.
     */
    public int getIco24()
    {
        if ( !getCv().isEmpty() )
            return getCv().getValue();

        // convert word 97 colour numbers to 0xBBRRGGRR value
        switch ( getIco() )
        {
        case 0: // auto
            return -1;
        case 1: // black
            return 0x00000000;
        case 2: // blue
            return 0x00FF0000;
        case 3: // cyan
            return 0x00FFFF00;
        case 4: // green
            return 0x0000FF00;
        case 5: // magenta
            return 0x00FF00FF;
        case 6: // red
            return 0x000000FF;
        case 7: // yellow
            return 0x0000FFFF;
        case 8: // white
            return 0x00FFFFFF;
        case 9: // dark blue
            return 0x00800000;
        case 10: // dark cyan
            return 0x00808000;
        case 11: // dark green
            return 0x00008000;
        case 12: // dark magenta
            return 0x00800080;
        case 13: // dark red
            return 0x00000080;
        case 14: // dark yellow
            return 0x00008080;
        case 15: // dark grey
            return 0x00808080;
        case 16: // light grey
            return 0x00C0C0C0;
        }

        return -1;
    }

    /**
     * Set the ico24 field for the CHP record.
     */
    public void setIco24( int colour24 )
    {
        setCv( new Colorref( colour24 & 0xFFFFFF ) );
    }

    public CharacterProperties clone()
    {
        try
        {
            CharacterProperties cp = (CharacterProperties) super.clone();

            cp.setCv( getCv().clone() );
            cp.setDttmRMark( (DateAndTime) getDttmRMark().clone() );
            cp.setDttmRMarkDel( (DateAndTime) getDttmRMarkDel().clone() );
            cp.setDttmPropRMark( (DateAndTime) getDttmPropRMark().clone() );
            cp.setDttmDispFldRMark( (DateAndTime) getDttmDispFldRMark().clone() );
            cp.setXstDispFldRMark( getXstDispFldRMark().clone() );
            cp.setShd( getShd().clone() );
            cp.setBrc( (BorderCode) getBrc().clone() );

            return cp;
        }
        catch ( CloneNotSupportedException exc )
        {
            throw new UnsupportedOperationException(
                    "Impossible CloneNotSupportedException occured", exc );
        }
    }
}
