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

import org.apache.poi.hwpf.model.hdftypes.definitions.PAPAbstractType;
import org.apache.poi.hwpf.model.hdftypes.StyleDescription;

import org.apache.poi.hwpf.sprm.SprmBuffer;

public class Paragraph
  extends PAPAbstractType
    implements Cloneable
{
  public final static short SPRM_JC = 0x2403;
  public final static short SPRM_FSIDEBYSIDE = 0x2404;
  public final static short SPRM_FKEEP = 0x2405;
  public final static short SPRM_FKEEPFOLLOW = 0x2406;
  public final static short SPRM_FPAGEBREAKBEFORE = 0x2407;
  public final static short SPRM_BRCL = 0x2408;
  public final static short SPRM_BRCP = 0x2409;
  public final static short SPRM_ILVL = 0x260A;
  public final static short SPRM_ILFO = 0x460B;
  public final static short SPRM_FNOLINENUMB = 0x240C;
  public final static short SPRM_CHGTABSPAPX = (short)0xC60D;
  public final static short SPRM_DXARIGHT = (short)0x840E;
  public final static short SPRM_DXALEFT = (short)0x840F;
  public final static short SPRM_DXALEFT1 = (short)0x8411;
  public final static short SPRM_DYALINE = 0x6412;
  public final static short SPRM_DYABEFORE = (short)0xA413;
  public final static short SPRM_DYAAFTER = (short)0xA414;
  public final static short SPRM_CHGTABS = (short)0xC615;
  public final static short SPRM_FINTABLE = 0x2416;
  public final static short SPRM_FTTP = 0x2417;
  public final static short SPRM_DXAABS = (short)0x8418;
  public final static short SPRM_DYAABS = (short)0x8419;
  public final static short SPRM_DXAWIDTH = (short)0x841A;
  public final static short SPRM_PC = 0x261B;
  public final static short SPRM_WR = 0x2423;
  public final static short SPRM_BRCTOP = 0x6424;
  public final static short SPRM_BRCLEFT = 0x6425;
  public final static short SPRM_BRCBOTTOM = 0x6426;
  public final static short SPRM_BRCRIGHT = 0x6427;
  public final static short SPRM_BRCBAR = 0x6629;
  public final static short SPRM_FNOAUTOHYPH = 0x242A;
  public final static short SPRM_WHEIGHTABS = 0x442B;
  public final static short SPRM_DCS = 0x442C;
  public final static short SPRM_SHD = 0x442D;
  public final static short SPRM_DYAFROMTEXT = (short)0x842E;
  public final static short SPRM_DXAFROMTEXT = (short)0x842F;
  public final static short SPRM_FLOCKED = 0x2430;
  public final static short SPRM_FWIDOWCONTROL = 0x2431;
  public final static short SPRM_RULER = (short)0xC632;
  public final static short SPRM_FKINSOKU = 0x2433;
  public final static short SPRM_FWORDWRAP = 0x2434;
  public final static short SPRM_FOVERFLOWPUNCT = 0x2435;
  public final static short SPRM_FTOPLINEPUNCT = 0x2436;
  public final static short SPRM_AUTOSPACEDE = 0x2437;
  public final static short SPRM_AUTOSPACEDN = 0x2438;
  public final static short SPRM_WALIGNFONT = 0x4439;
  public final static short SPRM_FRAMETEXTFLOW = 0x443A;
  public final static short SPRM_ANLD = (short)0xC63E;
  public final static short SPRM_PROPRMARK = (short)0xC63F;
  public final static short SPRM_OUTLVL = 0x2640;
  public final static short SPRM_FBIDI = 0x2441;
  public final static short SPRM_FNUMRMLNS = 0x2443;
  public final static short SPRM_CRLF = 0x2444;
  public final static short SPRM_NUMRM = (short)0xC645;
  public final static short SPRM_USEPGSUSETTINGS = 0x2447;
  public final static short SPRM_FADJUSTRIGHT = 0x2448;


  private StyleDescription _baseStyle;
  private SprmBuffer _papx;

  public Paragraph()
  {
    field_21_lspd = new LineSpacingDescriptor();
    field_24_phe = new byte[12];
    field_46_brcTop = new BorderCode();
    field_47_brcLeft = new BorderCode();
    field_48_brcBottom = new BorderCode();
    field_49_brcRight = new BorderCode();
    field_50_brcBetween = new BorderCode();
    field_51_brcBar = new BorderCode();
    field_60_anld = new byte[84];
    this.field_17_fWidowControl = 1;
    this.field_21_lspd.setMultiLinespace((short)1);
    this.field_21_lspd.setDyaLine((short)240);
    this.field_12_ilvl = (byte)9;

  }

  public Object clone()
    throws CloneNotSupportedException
  {
    Paragraph pp = (Paragraph)super.clone();
    pp.field_21_lspd = (LineSpacingDescriptor)field_21_lspd.clone();
    pp.field_24_phe = (byte[])field_24_phe.clone();
    pp.field_46_brcTop = (BorderCode)field_46_brcTop.clone();
    pp.field_47_brcLeft = (BorderCode)field_47_brcLeft.clone();
    pp.field_48_brcBottom = (BorderCode)field_48_brcBottom.clone();
    pp.field_49_brcRight = (BorderCode)field_49_brcRight.clone();
    pp.field_50_brcBetween = (BorderCode)field_50_brcBetween.clone();
    pp.field_51_brcBar = (BorderCode)field_51_brcBar.clone();
    pp.field_60_anld = (byte[])field_60_anld.clone();
    return pp;
  }

}
