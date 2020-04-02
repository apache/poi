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

package org.apache.poi.hwpf.model;

import java.util.Arrays;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * FFN - Font Family Name. FFN is a data structure that stores the names of the Main
 * Font and that of Alternate font as an array of characters. It has also a header
 * that stores info about the whole structure and the fonts
 *
 * @author Praveen Mathew
 */
@Internal
public final class Ffn
{

  //arbitrarily selected; may need to increase
  private static final int MAX_RECORD_LENGTH = 100_000;

  private int _cbFfnM1;//total length of FFN - 1.
  private byte _info;
    private static BitField _prq = BitFieldFactory.getInstance(0x0003);// pitch request
    private static BitField _fTrueType = BitFieldFactory.getInstance(0x0004);// when 1, font is a TrueType font
    private static BitField _ff = BitFieldFactory.getInstance(0x0070);
  private short _wWeight;// base weight of font
  private byte _chs;// character set identifier
  private byte _ixchSzAlt;  // index into ffn.szFfn to the name of
                                  // the alternate font
  private byte [] _panose = new byte[10];//????
  private byte [] _fontSig = new byte[24];//????

  // zero terminated string that records name of font, cuurently not
  // supporting Extended chars
  private char [] _xszFfn;

  // extra facilitator members
  private int _xszFfnLength;

  public Ffn(byte[] buf, int offset)
  {
    int offsetTmp = offset;

    _cbFfnM1 = LittleEndian.getUByte(buf,offset);
    offset += LittleEndianConsts.BYTE_SIZE;
    _info = buf[offset];
    offset += LittleEndianConsts.BYTE_SIZE;
    _wWeight = LittleEndian.getShort(buf, offset);
    offset += LittleEndianConsts.SHORT_SIZE;
    _chs = buf[offset];
    offset += LittleEndianConsts.BYTE_SIZE;
    _ixchSzAlt = buf[offset];
    offset += LittleEndianConsts.BYTE_SIZE;

    // read panose and fs so we can write them back out.
    System.arraycopy(buf, offset, _panose, 0, _panose.length);
    offset += _panose.length;
    System.arraycopy(buf, offset, _fontSig, 0, _fontSig.length);
    offset += _fontSig.length;

    offsetTmp = offset - offsetTmp;
    _xszFfnLength = (this.getSize() - offsetTmp)/2;
    _xszFfn = new char[_xszFfnLength];

    for(int i = 0; i < _xszFfnLength; i++)
    {
      _xszFfn[i] = (char)LittleEndian.getShort(buf, offset);
      offset += LittleEndianConsts.SHORT_SIZE;
    }


  }

  public int get_cbFfnM1()
  {
    return  _cbFfnM1;
  }

  public short getWeight()
  {
	  return  _wWeight;
  }

  public byte getChs()
  {
	  return  _chs;
  }

  public byte [] getPanose()
  {
	  return  _panose;
  }

  public byte [] getFontSig()
  {
	  return  _fontSig;
  }

  public int getSize()
  {
    return (_cbFfnM1 + 1);
  }

  public String getMainFontName()
  {
    int index = 0;
    for (;index < _xszFfnLength; index++)
    {
      if (_xszFfn[index] == '\0')
      {
        break;
      }
    }
    return new String(_xszFfn, 0, index);
  }

  public String getAltFontName()
  {
    int index = _ixchSzAlt;
    for (;index < _xszFfnLength; index++)
    {
      if (_xszFfn[index] == '\0')
      {
        break;
      }
    }
    return new String(_xszFfn, _ixchSzAlt, index);

  }

  public void set_cbFfnM1(int _cbFfnM1)
  {
    this._cbFfnM1 = _cbFfnM1;
  }

  // changed protected to public
  public byte[] toByteArray()
  {
    int offset = 0;
    byte[] buf = IOUtils.safelyAllocate(this.getSize(), MAX_RECORD_LENGTH);

    buf[offset] = (byte)_cbFfnM1;
    offset += LittleEndianConsts.BYTE_SIZE;
    buf[offset] = _info;
    offset += LittleEndianConsts.BYTE_SIZE;
    LittleEndian.putShort(buf, offset, _wWeight);
    offset += LittleEndianConsts.SHORT_SIZE;
    buf[offset] = _chs;
    offset += LittleEndianConsts.BYTE_SIZE;
    buf[offset] = _ixchSzAlt;
    offset += LittleEndianConsts.BYTE_SIZE;

    System.arraycopy(_panose,0,buf, offset,_panose.length);
    offset += _panose.length;
    System.arraycopy(_fontSig,0,buf, offset, _fontSig.length);
    offset += _fontSig.length;

    for(int i = 0; i < _xszFfn.length; i++)
    {
      LittleEndian.putShort(buf, offset, (short)_xszFfn[i]);
      offset += LittleEndianConsts.SHORT_SIZE;
    }

    return buf;

  }

  @Override
  public boolean equals(Object other) {
      if (!(other instanceof Ffn)) return false;
      Ffn o = (Ffn)other;

      return (
             o._cbFfnM1 == this._cbFfnM1
          && o._info == this._info
          && o._wWeight == _wWeight
          && o._chs == _chs
          && o._ixchSzAlt == _ixchSzAlt
          && Arrays.equals(o._panose,_panose)
          && Arrays.equals(o._fontSig,_fontSig)
          && Arrays.equals(o._xszFfn,_xszFfn)
      );
  }


    @Override
    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }
}


