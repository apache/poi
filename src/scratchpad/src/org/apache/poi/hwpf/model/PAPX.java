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


import org.apache.poi.util.LittleEndian;

import org.apache.poi.hwpf.usermodel.ParagraphProperties;
import org.apache.poi.hwpf.sprm.ParagraphSprmUncompressor;
import org.apache.poi.hwpf.sprm.SprmBuffer;
import org.apache.poi.hwpf.sprm.SprmOperation;

/**
 * Comment me
 *
 * @author Ryan Ackley
 */

public class PAPX extends CachedPropertyNode
{

  private ParagraphHeight _phe;
  private int _hugeGrpprlOffset = -1;

  public PAPX(int fcStart, int fcEnd, byte[] papx, ParagraphHeight phe, byte[] dataStream)
  {
    super(fcStart, fcEnd, new SprmBuffer(papx));
    _phe = phe;
    SprmBuffer buf = findHuge(new SprmBuffer(papx), dataStream);
    if(buf != null)
      _buf = buf;
  }

  public PAPX(int fcStart, int fcEnd, SprmBuffer buf, byte[] dataStream)
  {
    super(fcStart, fcEnd, buf);
    _phe = new ParagraphHeight();
    buf = findHuge(buf, dataStream);
    if(buf != null)
      _buf = buf;
  }

  private SprmBuffer findHuge(SprmBuffer buf, byte[] datastream)
  {
    byte[] grpprl = buf.toByteArray();
    if(grpprl.length==8 && datastream!=null) // then check for sprmPHugePapx
    {
      SprmOperation sprm = new SprmOperation(grpprl, 2);
      if ((sprm.getOperation()==0x45 || sprm.getOperation()==0x46)
          && sprm.getSizeCode() == 3)
      {
        int hugeGrpprlOffset = sprm.getOperand();
        if(hugeGrpprlOffset+1 < datastream.length)
        {
          int grpprlSize = LittleEndian.getShort(datastream, hugeGrpprlOffset);
          if( hugeGrpprlOffset+grpprlSize < datastream.length)
          {
            byte[] hugeGrpprl = new byte[grpprlSize + 2];
            // copy original istd into huge Grpprl
            hugeGrpprl[0] = grpprl[0]; hugeGrpprl[1] = grpprl[1];
            // copy Grpprl from dataStream
            System.arraycopy(datastream, hugeGrpprlOffset + 2, hugeGrpprl, 2,
                             grpprlSize);
            // save a pointer to where we got the huge Grpprl from
            _hugeGrpprlOffset = hugeGrpprlOffset;
            return new SprmBuffer(hugeGrpprl);
          }
        }
      }
    }
    return null;
  }


  public ParagraphHeight getParagraphHeight()
  {
    return _phe;
  }

  public byte[] getGrpprl()
  {
    return ((SprmBuffer)_buf).toByteArray();
  }

  public int getHugeGrpprlOffset()
  {
    return _hugeGrpprlOffset;
  }

  public short getIstd()
  {
    byte[] buf = getGrpprl();
    if (buf.length == 0)
    {
      return 0;
    }
    else
    {
      return LittleEndian.getShort(buf);
    }
  }

  public ParagraphProperties getParagraphProperties(StyleSheet ss)
  {

    ParagraphProperties props = (ParagraphProperties)super.getCacheContents();
    if (props == null)
    {
      short istd = getIstd();
      ParagraphProperties baseStyle = ss.getParagraphStyle(istd);
      props = ParagraphSprmUncompressor.uncompressPAP(baseStyle, getGrpprl(), 2);
      super.fillCache(props);
    }
    return props;

  }

  public boolean equals(Object o)
  {
    if (super.equals(o))
    {
      return _phe.equals(((PAPX)o)._phe);
    }
    return false;
  }
}
