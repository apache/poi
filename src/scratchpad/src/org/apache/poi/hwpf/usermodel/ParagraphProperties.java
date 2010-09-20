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

import org.apache.poi.hwpf.model.types.PAPAbstractType;

public final class ParagraphProperties extends PAPAbstractType implements Cloneable {

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
    this.field_58_lvl = (byte)9;
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
    pp.field_24_phe = field_24_phe.clone();
    pp.field_46_brcTop = (BorderCode)field_46_brcTop.clone();
    pp.field_47_brcLeft = (BorderCode)field_47_brcLeft.clone();
    pp.field_48_brcBottom = (BorderCode)field_48_brcBottom.clone();
    pp.field_49_brcRight = (BorderCode)field_49_brcRight.clone();
    pp.field_50_brcBetween = (BorderCode)field_50_brcBetween.clone();
    pp.field_51_brcBar = (BorderCode)field_51_brcBar.clone();
    pp.field_60_anld = field_60_anld.clone();
    return pp;
  }

}
