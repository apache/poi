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

import org.apache.poi.hwpf.sprm.SprmBuffer;

public class ParagraphProperties
  extends PAPAbstractType
    implements Cloneable
{

  public ParagraphProperties()
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
    this.field_66_rgdxaTab = new int[0];
    this.field_67_rgtbd = new byte[0];
    this.field_63_dttmPropRMark = new DateAndTime();

  }

  public int getJustification()
  {
    return super.getJc();
  }

  public void setJustification(byte jc)
  {
    super.setJc(jc);
  }

  public boolean keepOnPage()
  {
    return super.getFKeep() != 0;
  }

  public void setKeepOnPage(boolean fKeep)
  {
    super.setFKeep((byte)(fKeep ? 1 : 0));
  }

  public boolean keepWithNext()
  {
    return super.getFKeepFollow() != 0;
  }

  public void setKeepWithNext(boolean fKeepFollow)
  {
    super.setFKeepFollow((byte)(fKeepFollow ? 1 : 0));
  }

  public boolean pageBreakBefore()
  {
    return super.getFPageBreakBefore() != 0;
  }

  public void setPageBreakBefore(boolean fPageBreak)
  {
    super.setFPageBreakBefore((byte)(fPageBreak ? 1 : 0));
  }

  public boolean isLineNotNumbered()
  {
    return super.getFNoLnn() != 0;
  }

  public void setLineNotNumbered(boolean fNoLnn)
  {
    super.setFNoLnn((byte)(fNoLnn ? 1 : 0));
  }

  public boolean isSideBySide()
  {
    return super.getFSideBySide() != 0;
  }

  public void setSideBySide(boolean fSideBySide)
  {
    super.setFSideBySide((byte)(fSideBySide ? 1 : 0));
  }

  public boolean isAutoHyphenated()
  {
    return super.getFNoAutoHyph() == 0;
  }

  public void setAutoHyphenated(boolean auto)
  {
    super.setFNoAutoHyph((byte)(!auto ? 1 : 0));
  }

  public boolean isWidowControlled()
  {
    return super.getFWidowControl() != 0;
  }

  public void setWidowControl(boolean widowControl)
  {
    super.setFWidowControl((byte)(widowControl ? 1 : 0));
  }

  public int getIndentFromRight()
  {
    return super.getDxaRight();
  }

  public void setIndentFromRight(int dxaRight)
  {
    super.setDxaRight(dxaRight);
  }

  public int getIndentFromLeft()
  {
    return super.getDxaLeft();
  }

  public void setIndentFromLeft(int dxaLeft)
  {
    super.setDxaLeft(dxaLeft);
  }

  public int getFirstLineIndent()
  {
    return super.getDxaLeft1();
  }

  public void setFirstLineIndent(int first)
  {
    super.setDxaLeft1(first);
  }

  public LineSpacingDescriptor getLineSpacing()
  {
    return super.getLspd();
  }

  public void setLineSpacing(LineSpacingDescriptor lspd)
  {
    super.setLspd(lspd);
  }

  public int getSpacingBefore()
  {
    return super.getDyaBefore();
  }

  public void setSpacingBefore(int before)
  {
    super.setDyaBefore(before);
  }

  public int getSpacingAfter()
  {
    return super.getDyaAfter();
  }

  public void setSpacingAfter(int after)
  {
    super.setDyaAfter(after);
  }

  public boolean isKinsoku()
  {
    return super.getFKinsoku() != 0;
  }

  public void setKinsoku(boolean kinsoku)
  {
    super.setFKinsoku((byte)(kinsoku ? 1 : 0));
  }

  public boolean isWordWrapped()
  {
    return super.getFWordWrap() != 0;
  }

  public void setWordWrapped(boolean wrap)
  {
    super.setFWordWrap((byte)(wrap ? 1 : 0));
  }

  public int getFontAlignment()
  {
    return super.getWAlignFont();
  }

  public void setFontAlignment(int align)
  {
    super.setWAlignFont(align);
  }

  public boolean isVertical()
  {
    return super.isFVertical();
  }

  public void setVertical(boolean vertical)
  {
    super.setFVertical(vertical);
  }

  public boolean isBackward()
  {
    return super.isFBackward();
  }

  public void setBackward(boolean bward)
  {
    super.setFBackward(bward);
  }

  public BorderCode getTopBorder()
  {
    return super.getBrcTop();
  }

  public void setTopBorder(BorderCode top)
  {
    super.setBrcTop(top);
  }

  public BorderCode getLeftBorder()
  {
    return super.getBrcLeft();
  }

  public void setLeftBorder(BorderCode left)
  {
    super.setBrcLeft(left);
  }

  public BorderCode getBottomBorder()
  {
    return super.getBrcBottom();
  }

  public void setBottomBorder(BorderCode bottom)
  {
    super.setBrcBottom(bottom);
  }

  public BorderCode getRightBorder()
  {
    return super.getBrcRight();
  }

  public void setRightBorder(BorderCode right)
  {
    super.setBrcRight(right);
  }

  public BorderCode getBarBorder()
  {
    return super.getBrcBar();
  }

  public void setBarBorder(BorderCode bar)
  {
    super.setBrcBar(bar);
  }

  public ShadingDescriptor getShading()
  {
    return super.getShd();
  }

  public void setShading(ShadingDescriptor shd)
  {
    super.setShd(shd);
  }

  public DropCapSpecifier getDropCap()
  {
    return super.getDcs();
  }

  public void setDropCap(DropCapSpecifier dcs)
  {
    super.setDcs(dcs);
  }

  public Object clone()
    throws CloneNotSupportedException
  {
    ParagraphProperties pp = (ParagraphProperties)super.clone();
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
