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
  private int field_1_cbFfnM1;//total length of FFN - 1.
  private byte field_2;
    private  static BitField _prq = new BitField(0x0003);// pitch request
    private  static BitField _fTrueType = new BitField(0x0004);// when 1, font is a TrueType font
    private  static BitField _ff = new BitField(0x0070);
  private short field_3_wWeight;// base weight of font
  private byte field_4_chs;// character set identifier
  private byte field_5_ixchSzAlt;  // index into ffn.szFfn to the name of
                                  // the alternate font
  private byte [] field_6_panose = new byte[10];//????
  private byte [] field_7_fontSig = new byte[24];//????

  // zero terminated string that records name of font, cuurently not
  // supporting Extended chars
  private char [] field_8_xszFfn;

  // extra facilitator members
  private int xszFfnLength;

  public Ffn(byte[] buf, int offset)
  {
	  int offsetTmp = offset;

	  field_1_cbFfnM1 = LittleEndian.getUnsignedByte(buf,offset);
    offset += LittleEndian.BYTE_SIZE;
	  field_2 = buf[offset];
    offset += LittleEndian.BYTE_SIZE;
	  field_3_wWeight = LittleEndian.getShort(buf, offset);
    offset += LittleEndian.SHORT_SIZE;
	  field_4_chs = buf[offset];
    offset += LittleEndian.BYTE_SIZE;
	  field_5_ixchSzAlt = buf[offset];
    offset += LittleEndian.BYTE_SIZE;

    // read panose and fs so we can write them back out.
    System.arraycopy(buf, offset, field_6_panose, 0, field_6_panose.length);
    offset += field_6_panose.length;
    System.arraycopy(buf, offset, field_7_fontSig, 0, field_7_fontSig.length);
    offset += field_7_fontSig.length;

	  offsetTmp = offset - offsetTmp;
    xszFfnLength = this.getSize() - offsetTmp;
	  field_8_xszFfn = new char[xszFfnLength];

    for(int i = 0; i < xszFfnLength; i++)
    {
	    field_8_xszFfn[i] = (char)LittleEndian.getUnsignedByte(buf, offset);
      offset += LittleEndian.BYTE_SIZE;
    }


  }

  public int getField_1_cbFfnM1()
  {
    return  field_1_cbFfnM1;
  }

  public byte getField_2()
  {
	  return  field_2;
  }

  public short getField_3_wWeight()
  {
	  return  field_3_wWeight;
  }

  public byte getField_4_chs()
  {
	  return  field_4_chs;
  }

  public byte getField_5_ixchSzAlt()
  {
	  return  field_5_ixchSzAlt;
  }

  public byte [] getField_6_panose()
  {
	  return  field_6_panose;
  }

  public byte [] getField_7_fontSig()
  {
	  return  field_7_fontSig;
  }

  public char [] getField_8_xszFfn()
  {
	  return  field_8_xszFfn;
  }

  public int getSize()
  {
    return (field_1_cbFfnM1 + 1);
  }

  public char [] getMainFontName()
  {
    char [] temp = new char[field_5_ixchSzAlt];
    System.arraycopy(field_8_xszFfn,0,temp,0,temp.length);
    return temp;
  }

  public char [] getAltFontName()
  {
    char [] temp = new char[xszFfnLength - field_5_ixchSzAlt];
    System.arraycopy(field_8_xszFfn, field_5_ixchSzAlt, temp, 0, temp.length);
    return temp;
  }

  public void setField_1_cbFfnM1(int field_1_cbFfnM1)
  {
    this.field_1_cbFfnM1 = field_1_cbFfnM1;
  }

  // changed protected to public
  public byte[] toByteArray()
  {
    int offset = 0;
    byte[] buf = new byte[this.getSize()];

    buf[offset] = (byte)field_1_cbFfnM1;
    offset += LittleEndian.BYTE_SIZE;
    buf[offset] = field_2;
    offset += LittleEndian.BYTE_SIZE;
    LittleEndian.putShort(buf, offset, field_3_wWeight);
    offset += LittleEndian.SHORT_SIZE;
    buf[offset] = field_4_chs;
    offset += LittleEndian.BYTE_SIZE;
    buf[offset] = field_5_ixchSzAlt;
    offset += LittleEndian.BYTE_SIZE;

    System.arraycopy(field_6_panose,0,buf, offset,field_6_panose.length);
    offset += field_6_panose.length;
    System.arraycopy(field_7_fontSig,0,buf, offset, field_7_fontSig.length);
    offset += field_7_fontSig.length;

    for(int i = 0; i < field_8_xszFfn.length; i++)
    {
      buf[offset] = (byte)field_8_xszFfn[i];
        offset += LittleEndian.BYTE_SIZE;
    }

      return buf;

    }

    public boolean equals(Object o)
    {
    boolean retVal = true;

    if (((Ffn)o).getField_1_cbFfnM1() == field_1_cbFfnM1)
    {
      if(((Ffn)o).getField_2() == field_2)
      {
      if(((Ffn)o).getField_3_wWeight() == field_3_wWeight)
      {
        if(((Ffn)o).getField_4_chs() == field_4_chs)
        {
        if(((Ffn)o).getField_5_ixchSzAlt() == field_5_ixchSzAlt)
        {
          if(Arrays.equals(((Ffn)o).getField_6_panose(),field_6_panose))
          {
          if(Arrays.equals(((Ffn)o).getField_7_fontSig(),field_7_fontSig))
          {
                  if(!(Arrays.equals(((Ffn)o).getField_8_xszFfn(),field_8_xszFfn)))
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


