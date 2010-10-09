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

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;

/**
 * Mapping class for BRC80 structure (Border Code for Word 97)
 *
 * <p>Comments are copied out from the binary format specification.
 */
public final class BorderCode implements Cloneable {
  
  public static final int SIZE = 4;
  
  private short _info;
    private static final BitField _dptLineWidth = BitFieldFactory.getInstance(0x00ff);
    private static final BitField _brcType = BitFieldFactory.getInstance(0xff00);
    
  private short _info2;
    private static final BitField _ico = BitFieldFactory.getInstance(0x00ff);
    private static final BitField _dptSpace = BitFieldFactory.getInstance(0x1f00);
    private static final BitField _fShadow = BitFieldFactory.getInstance(0x2000);
    private static final BitField _fFrame = BitFieldFactory.getInstance(0x4000);
    
  public BorderCode()
  {
  }

  public BorderCode(byte[] buf, int offset)
  {
    _info = LittleEndian.getShort(buf, offset);
    _info2 = LittleEndian.getShort(buf, offset + LittleEndian.SHORT_SIZE);
  }

  public void serialize(byte[] buf, int offset)
  {
    LittleEndian.putShort(buf, offset, _info);
    LittleEndian.putShort(buf, offset + LittleEndian.SHORT_SIZE, _info2);
  }

  public int toInt()
  {
    byte[] buf = new byte[4];
    serialize(buf, 0);
    return LittleEndian.getInt(buf);
  }

  public boolean isEmpty()
  {
    return _info == 0 && _info2 == 0;
  }

  public boolean equals(Object o)
  {
    BorderCode brc = (BorderCode)o;
    return _info == brc._info && _info2 == brc._info2;
  }

  public Object clone()
    throws CloneNotSupportedException
  {
    return super.clone();
  }
  
  /**
   * Width of a single line in 1/8 pt, max of 32 pt.
   */
  public int getLineWidth() {
    return _dptLineWidth.getShortValue(_info);
  }
  
  public void setLineWidth(int lineWidth) {
    _dptLineWidth.setValue(_info, lineWidth);
  }

  /**
   * Border type code:
   * <li>0  none
   * <li>1  single
   * <li>2  thick
   * <li>3  double
   * <li>5  hairline
   * <li>6  dot
   * <li>7  dash large gap
   * <li>8  dot dash
   * <li>9  dot dot dash
   * <li>10  triple
   * <li>11  thin-thick small gap
   * <li>12  thick-thin small gap
   * <li>13  thin-thick-thin small gap
   * <li>14  thin-thick medium gap
   * <li>15  thick-thin medium gap
   * <li>16  thin-thick-thin medium gap
   * <li>17  thin-thick large gap
   * <li>18  thick-thin large gap
   * <li>19  thin-thick-thin large gap
   * <li>20  wave
   * <li>21  double wave
   * <li>22  dash small gap
   * <li>23  dash dot stroked
   * <li>24  emboss 3D
   * <li>25  engrave 3D
   * <li>codes 64 - 230 represent border art types and are used only for page borders
   */
  public int getBorderType() {
    return _brcType.getShortValue(_info);
  }
  
  public void setBorderType(int borderType) {
    _brcType.setValue(_info, borderType);
  }
  
  /**
   * Color:
   * <li>0  Auto
   * <li>1  Black
   * <li>2  Blue
   * <li>3  Cyan
   * <li>4  Green
   * <li>5  Magenta
   * <li>6  Red
   * <li>7  Yellow
   * <li>8  White
   * <li>9  DkBlue
   * <li>10  DkCyan
   * <li>11  DkGreen
   * <li>12  DkMagenta
   * <li>13  DkRed
   * <li>14  DkYellow
   * <li>15  DkGray
   * <li>16  LtGray
   */
  public short getColor() {
    return _ico.getShortValue(_info2);
  }
  
  public void setColor(short color) {
    _ico.setValue(_info2, color);
  }
  
  /**
   * Width of space to maintain between border and text within border.
   * 
   * <p>Must be 0 when BRC is a substructure of TC.
   * 
   * <p>Stored in points.
   */
  public int getSpace() {
    return _dptSpace.getShortValue(_info2);
  }
  
  public void setSpace(int space) {
    _dptSpace.setValue(_info2, space);
  }
  
  /**
   * When true, border is drawn with shadow
   * Must be false when BRC is a substructure of the TC.
   */
  public boolean isShadow() {
    return _fShadow.getValue(_info2) != 0;
  }
  
  public void setShadow(boolean shadow) {
    _fShadow.setValue(_info2, shadow ? 1 : 0);
  }
  
  /**
   * Don't reverse the border.
   */
  public boolean isFrame() {
    return _fFrame.getValue(_info2) != 0;
  }
  
  public void setFrame(boolean frame) {
    _fFrame.setValue(_info2, frame ? 1 : 0);
  }

}
