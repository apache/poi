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

import org.apache.poi.common.Duplicatable;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * Mapping class for BRC80 structure (Border Code for Word 97)
 */
public final class BorderCode implements Duplicatable {

    public static final int SIZE = 4;

    private static final BitField _dptLineWidth = BitFieldFactory.getInstance(0x00ff);
    private static final BitField _brcType = BitFieldFactory.getInstance(0xff00);

    private static final BitField _ico = BitFieldFactory.getInstance(0x00ff);
    private static final BitField _dptSpace = BitFieldFactory.getInstance(0x1f00);
    private static final BitField _fShadow = BitFieldFactory.getInstance(0x2000);
    private static final BitField _fFrame = BitFieldFactory.getInstance(0x4000);

    private short _info;
    private short _info2;


  public BorderCode() {}

  public BorderCode(BorderCode other) {
    _info = other._info;
    _info2 = other._info2;
  }

  public BorderCode(byte[] buf, int offset)
  {
    _info = LittleEndian.getShort(buf, offset);
    _info2 = LittleEndian.getShort(buf, offset + LittleEndianConsts.SHORT_SIZE);
  }

  public void serialize(byte[] buf, int offset)
  {
    LittleEndian.putShort(buf, offset, _info);
    LittleEndian.putShort(buf, offset + LittleEndianConsts.SHORT_SIZE, _info2);
  }

  public int toInt()
  {
    byte[] buf = new byte[4];
    serialize(buf, 0);
    return LittleEndian.getInt(buf);
  }

  public boolean isEmpty()
  {
    return _info == 0 && _info2 == 0 || _info == -1;
  }

  @Override
  public boolean equals(Object o)
  {
    if (!(o instanceof BorderCode)) return false;
    BorderCode brc = (BorderCode)o;
    return _info == brc._info && _info2 == brc._info2;
  }

  @Override
  public int hashCode() {
      assert false : "hashCode not designed";
      return 42; // any arbitrary constant will do
  }

  @Override
  public BorderCode copy() {
    return new BorderCode(this);
  }

  /**
   * Width of a single line in 1/8 pt, max of 32 pt.
   */
  public int getLineWidth() {
    return _dptLineWidth.getShortValue(_info);
  }

  /**
   * @param lineWidth the width of the line to set
   */
  public void setLineWidth(int lineWidth) {
    _info = _dptLineWidth.setShortValue(_info, (short)lineWidth);
  }

  /**
   * Border type code:
   * <ul>
   * <li>0  none</li>
   * <li>1  single</li>
   * <li>2  thick</li>
   * <li>3  double</li>
   * <li>5  hairline</li>
   * <li>6  dot</li>
   * <li>7  dash large gap</li>
   * <li>8  dot dash</li>
   * <li>9  dot dot dash</li>
   * <li>10  triple</li>
   * <li>11  thin-thick small gap</li>
   * <li>12  thick-thin small gap</li>
   * <li>13  thin-thick-thin small gap</li>
   * <li>14  thin-thick medium gap</li>
   * <li>15  thick-thin medium gap</li>
   * <li>16  thin-thick-thin medium gap</li>
   * <li>17  thin-thick large gap</li>
   * <li>18  thick-thin large gap</li>
   * <li>19  thin-thick-thin large gap</li>
   * <li>20  wave</li>
   * <li>21  double wave</li>
   * <li>22  dash small gap</li>
   * <li>23  dash dot stroked</li>
   * <li>24  emboss 3D</li>
   * <li>25  engrave 3D</li>
   * <li>codes 64 - 230 represent border art types and are used only for page borders</li>
   * </ul>
   */
  public int getBorderType() {
    return _brcType.getShortValue(_info);
  }

  public void setBorderType(int borderType) {
      _info = _brcType.setShortValue(_info, (short)borderType);
  }

  /**
   * Color:
   * <ul>
   * <li>0  Auto</li>
   * <li>1  Black</li>
   * <li>2  Blue</li>
   * <li>3  Cyan</li>
   * <li>4  Green</li>
   * <li>5  Magenta</li>
   * <li>6  Red</li>
   * <li>7  Yellow</li>
   * <li>8  White</li>
   * <li>9  DkBlue</li>
   * <li>10  DkCyan</li>
   * <li>11  DkGreen</li>
   * <li>12  DkMagenta</li>
   * <li>13  DkRed</li>
   * <li>14  DkYellow</li>
   * <li>15  DkGray</li>
   * <li>16  LtGray</li>
   * </ul>
   */
  public short getColor() {
    return _ico.getShortValue(_info2);
  }

  public void setColor(short color) {
      _info2 = _ico.setShortValue(_info2, color);
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
      _info2 = (short)_dptSpace.setValue(_info2, space);
  }

  /**
   * When true, border is drawn with shadow
   * Must be false when BRC is a substructure of the TC.
   */
  public boolean isShadow() {
    return _fShadow.getValue(_info2) != 0;
  }

  public void setShadow(boolean shadow) {
      _info2 = (short)_fShadow.setValue(_info2, shadow ? 1 : 0);
  }

  /**
   * Don't reverse the border.
   */
  public boolean isFrame() {
    return _fFrame.getValue(_info2) != 0;
  }

  public void setFrame(boolean frame) {
      _info2 = (short)_fFrame.setValue(_info2, frame ? 1 : 0);
  }

    @Override
    public String toString() {
        return isEmpty() ? "[BRC] EMPTY" :
            "[BRC]\n" +
            "        .dptLineWidth         =  (" + getLineWidth() + " )\n" +
            "        .brcType              =  (" + getBorderType() + " )\n" +
            "        .ico                  =  (" + getColor() + " )\n" +
            "        .dptSpace             =  (" + getSpace() + " )\n" +
            "        .fShadow              =  (" + isShadow() + " )\n" +
            "        .fFrame               =  (" + isFrame() + " )\n";
    }

}
