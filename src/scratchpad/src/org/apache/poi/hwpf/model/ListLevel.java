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

package org.apache.poi.hwpf.model;

import org.apache.poi.util.BitField;
import org.apache.poi.util.LittleEndian;

import org.apache.poi.hwpf.usermodel.CharacterProperties;
import org.apache.poi.hwpf.usermodel.ParagraphProperties;

import org.apache.poi.hwpf.sprm.ParagraphSprmCompressor;
import org.apache.poi.hwpf.sprm.CharacterSprmCompressor;

import java.util.Arrays;

public class ListLevel
{
  private int _iStartAt;
  private byte _nfc;
  private byte _info;
    private static BitField _jc;
    private static BitField _fLegal;
    private static BitField _fNoRestart;
    private static BitField _fPrev;
    private static BitField _fPrevSpace;
    private static BitField _fWord6;
  private byte[] _rgbxchNums;
  private byte _ixchFollow;
  private int _dxaSpace;
  private int _dxaIndent;
  private int _cbGrpprlChpx;
  private int _cbGrpprlPapx;
  private short _reserved;
  private byte[] _grpprlPapx;
  private byte[] _grpprlChpx;
  private char[] _numberText;

  public ListLevel(int startAt, int numberFormatCode, int alignment,
                   byte[] numberProperties, byte[] entryProperties,
                   String numberText)
  {
    _iStartAt = startAt;
    _nfc = (byte)numberFormatCode;
    _jc.setValue(_info, alignment);
    _grpprlChpx = numberProperties;
    _grpprlPapx = entryProperties;
    _numberText = numberText.toCharArray();
  }

  public ListLevel(int level, boolean numbered)
  {
    _iStartAt = 1;
    _grpprlPapx = new byte[0];
    _grpprlChpx = new byte[0];
    _numberText = new char[0];
    _rgbxchNums = new byte[9];

    if (numbered)
    {
      _rgbxchNums[0] = 1;
      _numberText = new char[]{(char)level, '.'};
    }
    else
    {
      _numberText = new char[]{'\u2022'};
    }
  }

  public ListLevel(byte[] buf, int offset)
  {
    _iStartAt = LittleEndian.getInt(buf, offset);
    offset += LittleEndian.INT_SIZE;
    _nfc = buf[offset++];
    _info = buf[offset++];

    _rgbxchNums = new byte[9];
    for (int x = 0; x < 9; x++)
    {
      _rgbxchNums[x] = buf[offset++];
    }
    _ixchFollow = buf[offset++];
    _dxaSpace = LittleEndian.getInt(buf, offset);
    offset += LittleEndian.INT_SIZE;
    _dxaIndent = LittleEndian.getInt(buf, offset);
    offset += LittleEndian.INT_SIZE;
    _cbGrpprlChpx = LittleEndian.getUnsignedByte(buf, offset++);
    _cbGrpprlPapx = LittleEndian.getUnsignedByte(buf, offset++);
    _reserved = LittleEndian.getShort(buf, offset);
    offset += LittleEndian.SHORT_SIZE;

    _grpprlPapx = new byte[_cbGrpprlPapx];
    _grpprlChpx = new byte[_cbGrpprlChpx];
    System.arraycopy(buf, offset, _grpprlChpx, 0, _cbGrpprlChpx);
    offset += _cbGrpprlChpx;
    System.arraycopy(buf, offset, _grpprlPapx, 0, _cbGrpprlPapx);
    offset += _cbGrpprlPapx;

    int numberTextLength = LittleEndian.getShort(buf, offset);
    _numberText = new char[numberTextLength];
    offset += LittleEndian.SHORT_SIZE;
    for (int x = 0; x < numberTextLength; x++)
    {
      _numberText[x] = (char)LittleEndian.getShort(buf, offset);
      offset += LittleEndian.SHORT_SIZE;
    }

  }

  public int getStartAt()
  {
    return _iStartAt;
  }

  public int getNumberFormat()
  {
    return _nfc;
  }

  public int getAlignment()
  {
    return _jc.getValue(_info);
  }

  public String getNumberText()
  {
    return new String(_numberText);
  }

  public void setStartAt(int startAt)
  {
    _iStartAt = startAt;
  }

  public void setNumberFormat(int numberFormatCode)
  {
    _nfc = (byte)numberFormatCode;
  }

  public void setAlignment(int alignment)
  {
    _jc.setValue(_info, alignment);
  }

  public void setNumberProperties(byte[] grpprl)
  {
    _grpprlChpx = grpprl;

  }

  public void setLevelProperties(byte[] grpprl)
  {
    _grpprlPapx = grpprl;
  }


  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    ListLevel lvl = (ListLevel)obj;
    return _cbGrpprlChpx == lvl._cbGrpprlChpx && lvl._cbGrpprlPapx == _cbGrpprlPapx &&
      lvl._dxaIndent == _dxaIndent && lvl._dxaSpace == _dxaSpace &&
      Arrays.equals(lvl._grpprlChpx, _grpprlChpx) &&
      Arrays.equals(lvl._grpprlPapx, _grpprlPapx) &&
      lvl._info == _info && lvl._iStartAt == _iStartAt &&
      lvl._ixchFollow == _ixchFollow && lvl._nfc == _nfc &&
      Arrays.equals(lvl._numberText, _numberText) &&
      Arrays.equals(lvl._rgbxchNums, _rgbxchNums) &&
      lvl._reserved == _reserved;


  }
  public byte[] toByteArray()
  {
    byte[] buf = new byte[getSizeInBytes()];
    int offset = 0;
    LittleEndian.putInt(buf, offset, _iStartAt);
    offset += LittleEndian.INT_SIZE;
    buf[offset++] = _nfc;
    buf[offset++] = _info;
    System.arraycopy(_rgbxchNums, 0, buf, offset, _rgbxchNums.length);
    offset += _rgbxchNums.length;
    buf[offset++] = _ixchFollow;
    LittleEndian.putInt(buf, offset, _dxaSpace);
    offset += LittleEndian.INT_SIZE;
    LittleEndian.putInt(buf, offset, _dxaIndent);
    offset += LittleEndian.INT_SIZE;

    buf[offset++] = (byte)_cbGrpprlChpx;
    buf[offset++] = (byte)_cbGrpprlPapx;
    LittleEndian.putShort(buf, offset, _reserved);
    offset += LittleEndian.SHORT_SIZE;

    System.arraycopy(_grpprlChpx, 0, buf, offset, _cbGrpprlChpx);
    offset += _cbGrpprlChpx;
    System.arraycopy(_grpprlPapx, 0, buf, offset, _cbGrpprlPapx);
    offset += _cbGrpprlPapx;

    LittleEndian.putShort(buf, offset, (short)_numberText.length);
    offset += LittleEndian.SHORT_SIZE;
    for (int x = 0; x < _numberText.length; x++)
    {
      LittleEndian.putShort(buf, offset, (short)_numberText[x]);
      offset += LittleEndian.SHORT_SIZE;
    }
    return buf;
  }
  public int getSizeInBytes()
  {
    return 28 + _cbGrpprlChpx + _cbGrpprlPapx + (_numberText.length * LittleEndian.SHORT_SIZE) + 2;
  }

}
