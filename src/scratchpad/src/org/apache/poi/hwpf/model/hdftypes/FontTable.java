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

import java.io.IOException;
import org.apache.poi.hwpf.model.io.HWPFFileSystem;
import org.apache.poi.hwpf.model.io.HWPFOutputStream;
import org.apache.poi.util.LittleEndian;

/**
 * FontTable or in MS terminology sttbfffn is a common data structure written in all
 * Word files. The sttbfffn is an sttbf where each string is an FFN structure instead
 * of pascal-style strings. An sttbf is a string Table stored in file. Thus sttbffn
 * is like an Sttbf with an array of FFN structures that stores the font name strings
 *
 * @author Praveen Mathew
 */
public class FontTable
{
  private short _stringCount;// how many strings are included in the string table
  private short _extraDataSz;// size in bytes of the extra data

  // added extra facilitator members
  private int lcbSttbfffn;// count of bytes in sttbfffn
  private int fcSttbfffn;// table stream offset for sttbfffn

  // FFN structure containing strings of font names
  private Ffn[] _fontNames = null;


  public FontTable(byte[] buf, int offset, int lcbSttbfffn)
  {
    this.lcbSttbfffn = lcbSttbfffn;
    this.fcSttbfffn = offset;

    _stringCount = LittleEndian.getShort(buf, offset);
    offset += LittleEndian.SHORT_SIZE;
    _extraDataSz = LittleEndian.getShort(buf, offset);
    offset += LittleEndian.SHORT_SIZE;

    _fontNames = new Ffn[_stringCount]; //Ffn corresponds to a Pascal style String in STTBF.

    for(int i = 0;i<_stringCount; i++)
    {
      _fontNames[i] = new Ffn(buf,offset);
      offset += _fontNames[i].getSize();
    }
  }

  public short getStringCount()
  {
    return  _stringCount;
  }

  public short getExtraDataSz()
  {
  	return _extraDataSz;
  }

  public Ffn[] getFontNames()
  {
  	return _fontNames;
  }

  public int getSize()
  {
    return lcbSttbfffn;
  }

  public char [] getMainFont(int chpFtc )
  {
    if(chpFtc >= _stringCount)
    {
      System.out.println("Mismatch in chpFtc with stringCount");
      return null;
    }

    return _fontNames[chpFtc].getMainFontName();
  }

  public char [] getAltFont(int chpFtc )
  {
    if(chpFtc >= _stringCount)
    {
      System.out.println("Mismatch in chpFtc with stringCount");
      return null;
    }

    return _fontNames[chpFtc].getAltFontName();
  }

  public void setStringCount(short stringCount)
  {
    this._stringCount = stringCount;
  }

  public void writeTo(HWPFFileSystem sys)
	  throws IOException
  {
	  HWPFOutputStream tableStream = sys.getStream("1Table");

	  byte[] buf = new byte[LittleEndian.SHORT_SIZE];
	  LittleEndian.putShort(buf, _stringCount);
	  tableStream.write(buf);
	  LittleEndian.putShort(buf, _extraDataSz);
	  tableStream.write(buf);

	  for(int i = 0; i < _fontNames.length; i++)
	  {
		tableStream.write(_fontNames[i].toByteArray());
	  }

  }

  public boolean equals(Object o)
  {
  	boolean retVal = true;

    if(((FontTable)o).getStringCount() == _stringCount)
    {
      if(((FontTable)o).getExtraDataSz() == _extraDataSz)
      {
        Ffn[] fontNamesNew = ((FontTable)o).getFontNames();
        for(int i = 0;i<_stringCount; i++)
        {
          if(!(_fontNames[i].equals(fontNamesNew[i])))
            retVal = false;
        }
      }
      else
        retVal = false;
    }
    else
	    retVal = false;


	  return retVal;
  }



}


