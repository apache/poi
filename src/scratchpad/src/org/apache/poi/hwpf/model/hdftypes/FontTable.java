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

import org.apache.poi.util.BitField;
import org.apache.poi.util.LittleEndian;


public class FontTable
{
  private short exntdChar; // strings are extended character if = 0xFFFF
  private short stringCount; // how many strings are included in the string table
  private short extraDataSz; // size in bytes of the extra data

  private int lcbSttbfffn; // count of bytes in sttbfffn
  private boolean isExtndChar;


  // FFN structure containing strings of font names
  private Ffn     [] fontNames = null;

  public FontTable(byte[] buf, int offset, int lcbSttbfffn)
  {
    this.lcbSttbfffn = lcbSttbfffn;

    exntdChar = LittleEndian.getShort(buf, offset);
    offset += LittleEndian.SHORT_SIZE;
    stringCount = LittleEndian.getShort(buf, offset);
    offset += LittleEndian.SHORT_SIZE;
    extraDataSz = LittleEndian.getShort(buf, offset);
    offset += LittleEndian.SHORT_SIZE;

    if ((exntdChar & 0xFFFF) == 0xFFFF)
    {
      isExtndChar = true;
    }
    else
    {
      isExtndChar = false;
    }

    fontNames = new Ffn[stringCount]; //Ffn corresponds to a Pascal style String in STTBF.

    for(int i = 0;i<stringCount; i++)
    {
      // some mistake in the fields we have chosen
      if(offset >= this.getSize())
      {
        System.out.println("Total size of Sttbfn mismatched with calculated size");
        break;
      }

      fontNames[i] = new Ffn(buf,offset);
      offset += fontNames[i].getSize();
    }
  }

  public boolean isExtndChar()
  {
    return  isExtndChar;
  }

  public short getStringCount()
  {
    return  stringCount;
  }

  public int getSize()
  {
    return lcbSttbfffn;
  }

  public char [] getMainFont(int chpFtc )
  {
    if(chpFtc >= stringCount)
    {
      System.out.println("Mismatch in chpFtc with stringCount");
      return null;
    }

    return fontNames[chpFtc].getMainFontName();
  }

  public char [] getAltFont(int chpFtc )
  {
    if(chpFtc >= stringCount)
    {
      System.out.println("Mismatch in chpFtc with stringCount");
      return null;
    }

    return fontNames[chpFtc].getAltFontName();
  }

  public void setStringCount(short stringCount)
  {
    this.stringCount = stringCount;
  }


}

