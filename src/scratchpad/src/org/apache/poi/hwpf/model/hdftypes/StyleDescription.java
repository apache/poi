
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


package org.apache.poi.hwpf.model.hdftypes;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.BitField;
/**
 * Comment me
 *
 * @author Ryan Ackley
 */

public class StyleDescription implements HDFType
{

  private final static int PARAGRAPH_STYLE = 1;
  private final static int CHARACTER_STYLE = 2;

  private int _istd;
  private int _baseLength;
  private short _infoShort;
    private static BitField _sti = new BitField(0xfff);
    private static BitField _fScratch = new BitField(0x1000);
    private static BitField _fInvalHeight = new BitField(0x2000);
    private static BitField _fHasUpe = new BitField(0x4000);
    private static BitField _fMassCopy = new BitField(0x8000);
  private short _infoShort2;
    private static BitField _styleTypeCode = new BitField(0xf);
    private static BitField _baseStyle = new BitField(0xfff0);
  private short _infoShort3;
    private static BitField _numUPX = new BitField(0xf);
    private static BitField _nextStyle = new BitField(0xfff0);
  private short _bchUpe;
  private short _infoShort4;
    private static BitField _fAutoRedef = new BitField(0x1);
    private static BitField _fHidden = new BitField(0x2);

  UPX[] _upxs;
  String _name;
  Paragraph _pap;
  CharacterRun _chp;

  public StyleDescription()
  {
//      _pap = new ParagraphProperties();
//      _chp = new CharacterProperties();
  }
  public StyleDescription(byte[] std, int baseLength, int offset, boolean word9)
  {
     _baseLength = baseLength;
     int nameStart = offset + baseLength;
      _infoShort = LittleEndian.getShort(std, offset);
      offset += LittleEndian.SHORT_SIZE;
      _infoShort2 = LittleEndian.getShort(std, offset);
      offset += LittleEndian.SHORT_SIZE;
      _infoShort3 = LittleEndian.getShort(std, offset);
      offset += LittleEndian.SHORT_SIZE;
      _bchUpe = LittleEndian.getShort(std, offset);
      offset += LittleEndian.SHORT_SIZE;
      _infoShort4 = LittleEndian.getShort(std, offset);
      offset += LittleEndian.SHORT_SIZE;

      //first byte(s) of variable length section of std is the length of the
      //style name and aliases string
      int nameLength = 0;
      int multiplier = 1;
      if(word9)
      {
          nameLength = LittleEndian.getShort(std, nameStart);
          multiplier = 2;
          nameStart += LittleEndian.SHORT_SIZE;
      }
      else
      {
          nameLength = std[nameStart];
      }

      try
      {
        _name = new String(std, nameStart, nameLength * multiplier, "UTF-16LE");
      }
      catch (UnsupportedEncodingException ignore)
      {
        // ignore
      }

      //length then null terminator.
      int grupxStart = ((nameLength + 1) * multiplier) + nameStart;

      // the spec only refers to two possible upxs but it mentions
      // that more may be added in the future
      int varOffset = grupxStart;
      int numUPX = _numUPX.getValue(_infoShort3);
      _upxs = new UPX[numUPX];
      for(int x = 0; x < numUPX; x++)
      {
          int upxSize = LittleEndian.getShort(std, varOffset);
          varOffset += LittleEndian.SHORT_SIZE;

          byte[] upx = new byte[upxSize];
          System.arraycopy(std, varOffset, upx, 0, upxSize);
          _upxs[x] = new UPX(upx);
          varOffset += upxSize;


          // the upx will always start on a word boundary.
          if(upxSize % 2 == 1)
          {
              ++varOffset;
          }

      }


  }
  public int getBaseStyle()
  {
    return _baseStyle.getValue(_infoShort2);
  }
  public byte[] getCHPX()
  {
    switch (_styleTypeCode.getValue(_infoShort2))
    {
      case PARAGRAPH_STYLE:
        if (_upxs.length > 1)
        {
          return _upxs[1].getUPX();
        }
        return null;
      case CHARACTER_STYLE:
        return _upxs[0].getUPX();
      default:
        return null;
    }

  }
  public byte[] getPAPX()
  {
    switch (_styleTypeCode.getValue(_infoShort2))
    {
      case PARAGRAPH_STYLE:
        return _upxs[0].getUPX();
      default:
        return null;
    }
  }
  public Paragraph getPAP()
  {
      return _pap;
  }
  public CharacterRun getCHP()
  {
      return _chp;
  }
  void setPAP(Paragraph pap)
  {
      _pap = pap;
  }
  void setCHP(CharacterRun chp)
  {
      _chp = chp;
  }

  public byte[] toByteArray()
  {
    // size equals _baseLength bytes for known variables plus 2 bytes for name
    // length plus name length * 2 plus 2 bytes for null plus upx's preceded by
    // length
    int size = _baseLength + 2 + ((_name.length() + 1) * 2);

    // determine the size needed for the upxs. They always fall on word
    // boundaries.
    size += _upxs[0].size() + 2;
    for (int x = 1; x < _upxs.length; x++)
    {
      size += _upxs[x-1].size() % 2;
      size += _upxs[x].size() + 2;
    }


    byte[] buf = new byte[size];

    int offset = 0;
    LittleEndian.putShort(buf, offset, _infoShort);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putShort(buf, offset, _infoShort2);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putShort(buf, offset, _infoShort3);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putShort(buf, offset, _bchUpe);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putShort(buf, offset, _infoShort4);
    offset = _baseLength;

    char[] letters = _name.toCharArray();
    LittleEndian.putShort(buf, _baseLength, (short)letters.length);
    offset += LittleEndian.SHORT_SIZE;
    for (int x = 0; x < letters.length; x++)
    {
      LittleEndian.putShort(buf, offset, (short)letters[x]);
      offset += LittleEndian.SHORT_SIZE;
    }
    // get past the null delimiter for the name.
    offset += LittleEndian.SHORT_SIZE;

    for (int x = 0; x < _upxs.length; x++)
    {
      short upxSize = (short)_upxs[x].size();
      LittleEndian.putShort(buf, offset, upxSize);
      offset += LittleEndian.SHORT_SIZE;
      System.arraycopy(_upxs[x].getUPX(), 0, buf, offset, upxSize);
      offset += upxSize + (upxSize % 2);
    }

    return buf;
  }

  public boolean equals(Object o)
  {
    StyleDescription sd = (StyleDescription)o;
    if (sd._infoShort == _infoShort && sd._infoShort2 == _infoShort2 &&
        sd._infoShort3 == _infoShort3 && sd._bchUpe == _bchUpe &&
        sd._infoShort4 == _infoShort4 &&
        _name.equals(sd._name))
    {

      if (!Arrays.equals(_upxs, sd._upxs))
      {
        return false;
      }
      return true;
    }
    return false;
  }
}
