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
import java.util.Arrays;

/**
 * FFN - Font Family Name. FFN is a data structure that stores the names of the Main
 * Font and that of Alternate font as an array of characters. It has also a header
 * that stores info about the whole structure and the fonts
 *
 * @author Praveen Mathew
 */
public class Ffn
{
  private int _cbFfnM1;//total length of FFN - 1.
  private byte _info;
    private static BitField _prq = new BitField(0x0003);// pitch request
    private static BitField _fTrueType = new BitField(0x0004);// when 1, font is a TrueType font
    private static BitField _ff = new BitField(0x0070);
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

    _cbFfnM1 = LittleEndian.getUnsignedByte(buf,offset);
    offset += LittleEndian.BYTE_SIZE;
    _info = buf[offset];
    offset += LittleEndian.BYTE_SIZE;
    _wWeight = LittleEndian.getShort(buf, offset);
    offset += LittleEndian.SHORT_SIZE;
    _chs = buf[offset];
    offset += LittleEndian.BYTE_SIZE;
    _ixchSzAlt = buf[offset];
    offset += LittleEndian.BYTE_SIZE;

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
      offset += LittleEndian.SHORT_SIZE;
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
    byte[] buf = new byte[this.getSize()];

    buf[offset] = (byte)_cbFfnM1;
    offset += LittleEndian.BYTE_SIZE;
    buf[offset] = _info;
    offset += LittleEndian.BYTE_SIZE;
    LittleEndian.putShort(buf, offset, _wWeight);
    offset += LittleEndian.SHORT_SIZE;
    buf[offset] = _chs;
    offset += LittleEndian.BYTE_SIZE;
    buf[offset] = _ixchSzAlt;
    offset += LittleEndian.BYTE_SIZE;

    System.arraycopy(_panose,0,buf, offset,_panose.length);
    offset += _panose.length;
    System.arraycopy(_fontSig,0,buf, offset, _fontSig.length);
    offset += _fontSig.length;

    for(int i = 0; i < _xszFfn.length; i++)
    {
      LittleEndian.putShort(buf, offset, (short)_xszFfn[i]);
      offset += LittleEndian.SHORT_SIZE;
    }

    return buf;

  }

    public boolean equals(Object o)
    {
    boolean retVal = true;

    if (((Ffn)o).get_cbFfnM1() == _cbFfnM1)
    {
      if(((Ffn)o)._info == _info)
      {
      if(((Ffn)o)._wWeight == _wWeight)
      {
        if(((Ffn)o)._chs == _chs)
        {
        if(((Ffn)o)._ixchSzAlt == _ixchSzAlt)
        {
          if(Arrays.equals(((Ffn)o)._panose,_panose))
          {
          if(Arrays.equals(((Ffn)o)._fontSig,_fontSig))
          {
                  if(!(Arrays.equals(((Ffn)o)._xszFfn,_xszFfn)))
                    retVal = false;
          }
          else
            retVal = false;
          }
          else
          retVal = false;
        }
        else
          retVal = false;
        }
        else
        retVal = false;
      }
      else
        retVal = false;
      }
      else
      retVal = false;
    }
    else
      retVal = false;

    return retVal;
  }


}


