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

import org.apache.poi.hwpf.model.types.PAPAbstractType;
import org.apache.poi.hwpf.model.StyleDescription;
import org.apache.poi.hwpf.model.StyleSheet;
import org.apache.poi.hwpf.model.PAPX;

import org.apache.poi.hwpf.sprm.SprmBuffer;
import org.apache.poi.hwpf.sprm.ParagraphSprmCompressor;
import org.apache.poi.hwpf.sprm.TableSprmCompressor;

public class Paragraph
  extends Range
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


  private short _istd;
  private ParagraphProperties _props;
  protected SprmBuffer _papx;

  protected Paragraph(int startIdx, int endIdx, Table parent)
  {
    super(startIdx, endIdx, Range.TYPE_PARAGRAPH, parent);
    PAPX papx = (PAPX)_paragraphs.get(_parEnd - 1);
    _props = papx.getParagraphProperties(_doc.getStyleSheet());
    _papx = papx.getSprmBuf();
  }

  public Paragraph(int start, int end, ParagraphProperties pap, SprmBuffer papx, Range parent)
  {
    super(start, end, parent);
    _props = pap;
    _papx = papx;
  }

  public int type()
  {
    return TYPE_PARAGRAPH;
  }

  public boolean isInTable()
  {
    return _props.getFInTable() != 0;
  }

  public boolean isTableRowEnd()
  {
    return _props.getFTtp() != 0 || _props.getFTtpEmbedded() != 0;
  }

  public int getTableLevel()
  {
    return _props.getTableLevel();
  }

  public boolean isEmbeddedCellMark()
  {
    return _props.getEmbeddedCellMark() != 0;
  }

  public int getJustification()
  {
    return _props.getJc();
  }

  public void setJustification(byte jc)
  {
    _props.setJc(jc);
    _papx.addSprm(SPRM_JC, jc);
  }

  public boolean keepOnPage()
  {
    return _props.getFKeep() != 0;
  }

  public void setKeepOnPage(boolean fKeep)
  {
    byte keep = (byte)(fKeep ? 1 : 0);
    _props.setFKeep(keep);
    _papx.addSprm(SPRM_FKEEP, keep);
  }

  public boolean keepWithNext()
  {
    return _props.getFKeepFollow() != 0;
  }

  public void setKeepWithNext(boolean fKeepFollow)
  {
    byte keepFollow = (byte)(fKeepFollow ? 1 : 0);
    _props.setFKeepFollow(keepFollow);
    _papx.addSprm(SPRM_FKEEPFOLLOW, keepFollow);
  }

  public boolean pageBreakBefore()
  {
    return _props.getFPageBreakBefore() != 0;
  }

  public void setPageBreakBefore(boolean fPageBreak)
  {
    byte pageBreak = (byte)(fPageBreak ? 1 : 0);
    _props.setFPageBreakBefore(pageBreak);
    _papx.addSprm(SPRM_FPAGEBREAKBEFORE, pageBreak);
  }

  public boolean isLineNotNumbered()
  {
    return _props.getFNoLnn() != 0;
  }

  public void setLineNotNumbered(boolean fNoLnn)
  {
    byte noLnn = (byte)(fNoLnn ? 1 : 0);
    _props.setFNoLnn(noLnn);
    _papx.addSprm(SPRM_FNOLINENUMB, noLnn);
  }

  public boolean isSideBySide()
  {
    return _props.getFSideBySide() != 0;
  }

  public void setSideBySide(boolean fSideBySide)
  {
    byte sideBySide = (byte)(fSideBySide ? 1 : 0);
    _props.setFSideBySide(sideBySide);
    _papx.addSprm(SPRM_FSIDEBYSIDE, sideBySide);
  }

  public boolean isAutoHyphenated()
  {
    return _props.getFNoAutoHyph() == 0;
  }

  public void setAutoHyphenated(boolean autoHyph)
  {
    byte auto = (byte)(!autoHyph ? 1 : 0);
    _props.setFNoAutoHyph(auto);
    _papx.addSprm(SPRM_FNOAUTOHYPH, auto);
  }

  public boolean isWidowControlled()
  {
    return _props.getFWidowControl() != 0;
  }

  public void setWidowControl(boolean widowControl)
  {
    byte widow = (byte)(widowControl ? 1 : 0);
    _props.setFWidowControl(widow);
    _papx.addSprm(SPRM_FWIDOWCONTROL, widow);
  }

  public int getIndentFromRight()
  {
    return _props.getDxaRight();
  }

  public void setIndentFromRight(int dxaRight)
  {
    _props.setDxaRight(dxaRight);
    _papx.addSprm(SPRM_DXARIGHT, (short)dxaRight);
  }

  public int getIndentFromLeft()
  {
    return _props.getDxaLeft();
  }

  public void setIndentFromLeft(int dxaLeft)
  {
    _props.setDxaLeft(dxaLeft);
    _papx.addSprm(SPRM_DXALEFT, (short)dxaLeft);
  }

  public int getFirstLineIndent()
  {
    return _props.getDxaLeft1();
  }

  public void setFirstLineIndent(int first)
  {
    _props.setDxaLeft1(first);
    _papx.addSprm(SPRM_DXALEFT1, (short)first);
  }

  public LineSpacingDescriptor getLineSpacing()
  {
    return _props.getLspd();
  }

  public void setLineSpacing(LineSpacingDescriptor lspd)
  {
    _props.setLspd(lspd);
    _papx.addSprm(SPRM_DYALINE, lspd.toInt());
  }

  public int getSpacingBefore()
  {
    return _props.getDyaBefore();
  }

  public void setSpacingBefore(int before)
  {
    _props.setDyaBefore(before);
    _papx.addSprm(SPRM_DYABEFORE, (short)before);
  }

  public int getSpacingAfter()
  {
    return _props.getDyaAfter();
  }

  public void setSpacingAfter(int after)
  {
    _props.setDyaAfter(after);
    _papx.addSprm(SPRM_DYAAFTER, (short)after);
  }

  public boolean isKinsoku()
  {
    return _props.getFKinsoku() != 0;
  }

  public void setKinsoku(boolean kinsoku)
  {
    byte kin = (byte)(kinsoku ? 1 : 0);
    _props.setFKinsoku(kin);
    _papx.addSprm(SPRM_FKINSOKU, kin);
  }

  public boolean isWordWrapped()
  {
    return _props.getFWordWrap() != 0;
  }

  public void setWordWrapped(boolean wrap)
  {
    byte wordWrap = (byte)(wrap ? 1 : 0);
    _props.setFWordWrap(wordWrap);
    _papx.addSprm(SPRM_FWORDWRAP, wordWrap);
  }

  public int getFontAlignment()
  {
    return _props.getWAlignFont();
  }

  public void setFontAlignment(int align)
  {
    _props.setWAlignFont(align);
    _papx.addSprm(SPRM_WALIGNFONT, (short)align);
  }

  public boolean isVertical()
  {
    return _props.isFVertical();
  }

  public void setVertical(boolean vertical)
  {
    _props.setFVertical(vertical);
    _papx.addSprm(SPRM_FRAMETEXTFLOW, getFrameTextFlow());
  }

  public boolean isBackward()
  {
    return _props.isFBackward();
  }

  public void setBackward(boolean bward)
  {
    _props.setFBackward(bward);
    _papx.addSprm(SPRM_FRAMETEXTFLOW, getFrameTextFlow());
  }

  public BorderCode getTopBorder()
  {
    return _props.getBrcTop();
  }

  public void setTopBorder(BorderCode top)
  {
    _props.setBrcTop(top);
    _papx.addSprm(SPRM_BRCTOP, top.toInt());
  }

  public BorderCode getLeftBorder()
  {
    return _props.getBrcLeft();
  }

  public void setLeftBorder(BorderCode left)
  {
    _props.setBrcLeft(left);
    _papx.addSprm(SPRM_BRCLEFT, left.toInt());
  }

  public BorderCode getBottomBorder()
  {
    return _props.getBrcBottom();
  }

  public void setBottomBorder(BorderCode bottom)
  {
    _props.setBrcBottom(bottom);
    _papx.addSprm(SPRM_BRCBOTTOM, bottom.toInt());
  }

  public BorderCode getRightBorder()
  {
    return _props.getBrcRight();
  }

  public void setRightBorder(BorderCode right)
  {
    _props.setBrcRight(right);
    _papx.addSprm(SPRM_BRCRIGHT, right.toInt());
  }

  public BorderCode getBarBorder()
  {
    return _props.getBrcBar();
  }

  public void setBarBorder(BorderCode bar)
  {
    _props.setBrcBar(bar);
    _papx.addSprm(SPRM_BRCBAR, bar.toInt());
  }

  public ShadingDescriptor getShading()
  {
    return _props.getShd();
  }

  public void setShading(ShadingDescriptor shd)
  {
    _props.setShd(shd);
    _papx.addSprm(SPRM_SHD, shd.toShort());
  }

  public DropCapSpecifier getDropCap()
  {
    return _props.getDcs();
  }

  public void setDropCap(DropCapSpecifier dcs)
  {
    _props.setDcs(dcs);
    _papx.addSprm(SPRM_DCS, dcs.toShort());
  }

  void setTableRowEnd(TableProperties props)
  {
    setTableRowEnd((byte)1);
    byte[] grpprl = TableSprmCompressor.compressTableProperty(props);
    _papx.append(grpprl);
  }

  private void setTableRowEnd(byte val)
  {
    _props.setFTtp(val);
    _papx.addSprm(SPRM_FTTP, val);
  }

  public Object clone()
    throws CloneNotSupportedException
  {
    Paragraph p = (Paragraph)super.clone();
    p._props = (ParagraphProperties)_props.clone();
    //p._baseStyle = _baseStyle;
    p._papx = new SprmBuffer();
    return p;
  }

  private short getFrameTextFlow()
  {
    short retVal = 0;
    if (_props.isFVertical())
    {
      retVal |= 1;
    }
    if (_props.isFBackward())
    {
      retVal |= 2;
    }
    if (_props.isFRotateFont())
    {
      retVal |= 4;
    }
    return retVal;
  }

}
