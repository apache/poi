
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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.BitField;
/**
 * Comment me
 *
 * @author Ryan Ackley
 */

public class StyleDescription implements HDFType
{

  private static int PARAGRAPH_STYLE = 1;
  private static int CHARACTER_STYLE = 2;

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

  byte[] _papx;
  byte[] _chpx;
  String _name;
//  ParagraphProperties _pap;
//  CharacterProperties _chp;

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
      int add = 0;
      int numUPX = _numUPX.getValue(_infoShort3);
      for(int x = 0; x < numUPX; x++)
      {
          int upxSize = LittleEndian.getShort(std, grupxStart + add);
          if(_styleTypeCode.getValue(_infoShort2) == PARAGRAPH_STYLE)
          {
              if(x == 0)
              {
                  _istd = LittleEndian.getShort(std, grupxStart + add + 2);
                  int grrprlSize = upxSize - 2;
                  _papx = new byte[upxSize];
                  System.arraycopy(std, grupxStart + add + 4, _papx, 0, grrprlSize);
              }
              else if(x == 1)
              {
                  _chpx = new byte[upxSize];
                  System.arraycopy(std, grupxStart + add + 2, _chpx, 0, upxSize);
              }
          }
          else if(_styleTypeCode.getValue(_infoShort2) == CHARACTER_STYLE && x == 0)
          {
              _chpx = new byte[upxSize];
              System.arraycopy(std, grupxStart + add + 2, _chpx, 0, upxSize);
          }

          // the upx will always start on a word boundary.
          if(upxSize % 2 == 1)
          {
              ++upxSize;
          }
          add += 2 + upxSize;
      }


  }
  public int getBaseStyle()
  {
      return _baseStyle.getValue(_infoShort2);
  }
  public byte[] getCHPX()
  {
      return _chpx;
  }
  public byte[] getPAPX()
  {
      return _papx;
  }
//  public ParagraphProperties getPAP()
//  {
//      return _pap;
//  }
//  public CharacterProperties getCHP()
//  {
//      return _chp;
//  }
//  public void setPAP(ParagraphProperties pap)
//  {
//      _pap = pap;
//  }
//  public void setCHP(CharacterProperties chp)
//  {
//      _chp = chp;
//  }
  public byte[] toByteArray()
  {
    // size equals _baseLength bytes for known variables plus 2 bytes for name
    // length plus name length * 2 plus 2 bytes for null plus upx's preceded by
    // length
    int size = _baseLength + 2 + ((_name.length() + 1) * 2);

    //only worry about papx and chpx for upxs
    if(_styleTypeCode.getValue(_infoShort2) == PARAGRAPH_STYLE)
    {
      size += _papx.length + 4 + (_papx.length % 2);
      if (_chpx != null)
      {
        size += _chpx.length + 2;
      }
    }
    else if (_styleTypeCode.getValue(_infoShort2) == CHARACTER_STYLE)
    {
      size += _chpx.length + 2;
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
    offset += LittleEndian.SHORT_SIZE;

    char[] letters = _name.toCharArray();
    LittleEndian.putShort(buf, offset, (short)letters.length);
    offset += LittleEndian.SHORT_SIZE;
    for (int x = 0; x < letters.length; x++)
    {
      LittleEndian.putShort(buf, offset, (short)letters[x]);
      offset += LittleEndian.SHORT_SIZE;
    }
    // get past the null delimiter for the name.
    offset += LittleEndian.SHORT_SIZE;

    //only worry about papx and chpx for upxs
    if(_styleTypeCode.getValue(_infoShort2) == PARAGRAPH_STYLE)
    {
      LittleEndian.putShort(buf, offset, (short)_papx.length);
      offset += LittleEndian.SHORT_SIZE;
      LittleEndian.putShort(buf, offset, (short)_istd);
      offset += LittleEndian.SHORT_SIZE;
      System.arraycopy(_papx, 0, buf, offset, _papx.length);
      offset += _papx.length + (_papx.length % 2);

      if (_chpx != null)
      {
        LittleEndian.putShort(buf, offset, (short) _chpx.length);
        offset += LittleEndian.SHORT_SIZE;
        System.arraycopy(_chpx, 0, buf, offset, _chpx.length);
        offset += _chpx.length;
      }
    }
    else if (_styleTypeCode.getValue(_infoShort2) == CHARACTER_STYLE)
    {
      LittleEndian.putShort(buf, offset, (short)_chpx.length);
      offset += LittleEndian.SHORT_SIZE;
      System.arraycopy(_chpx, 0, buf, offset, _chpx.length);
      offset += _chpx.length;
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
      if (_chpx != null && _chpx.length == sd._chpx.length)
      {
        for (int x = 0; x < _chpx.length; x++)
        {
          if (_chpx[x] != sd._chpx[x])
          {
            return false;
          }
        }
        return true;
      }
      if (_papx != null && _papx.length == sd._papx.length)
      {
        for (int x = 0; x < _papx.length; x++)
        {
          if (_papx[x] != sd._papx[x])
          {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }
}
