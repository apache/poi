/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2003 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" and
 *  "Apache POI" must not be used to endorse or promote products
 *  derived from this software without prior written permission. For
 *  written permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  "Apache POI", nor may "Apache" appear in their name, without
 *  prior written permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */


package org.apache.poi.hwpf.model;

import org.apache.poi.util.BitField;
import org.apache.poi.util.LittleEndian;

public class PieceDescriptor
{

  short descriptor;
   private static BitField fNoParaLast = new BitField(0x01);
   private static BitField fPaphNil = new BitField(0x02);
   private static BitField fCopied = new BitField(0x04);
  int fc;
  short prm;
  boolean unicode;


  public PieceDescriptor(byte[] buf, int offset)
  {
    descriptor = LittleEndian.getShort(buf, offset);
    offset += LittleEndian.SHORT_SIZE;
    fc = LittleEndian.getInt(buf, offset);
    offset += LittleEndian.INT_SIZE;
    prm = LittleEndian.getShort(buf, offset);

    // see if this piece uses unicode.
    if ((fc & 0x40000000) == 0)
    {
        unicode = true;
    }
    else
    {
        unicode = false;
        fc &= ~(0x40000000);//gives me FC in doc stream
        fc /= 2;
    }

  }

  public int getFilePosition()
  {
    return fc;
  }

  public void setFilePosition(int pos)
  {
    fc = pos;
  }

  public boolean isUnicode()
  {
    return unicode;
  }

  protected byte[] toByteArray()
  {
    // set up the fc
    int tempFc = fc;
    if (!unicode)
    {
      tempFc *= 2;
      tempFc |= (0x40000000);
    }

    int offset = 0;
    byte[] buf = new byte[8];
    LittleEndian.putShort(buf, offset, descriptor);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putInt(buf, offset, tempFc);
    offset += LittleEndian.INT_SIZE;
    LittleEndian.putShort(buf, offset, prm);

    return buf;

  }

  public static int getSizeInBytes()
  {
    return 8;
  }

  public boolean equals(Object o)
  {
    PieceDescriptor pd = (PieceDescriptor)o;

    return descriptor == pd.descriptor && prm == pd.prm && unicode == pd.unicode;
  }
}
