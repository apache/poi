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
import org.apache.poi.util.BitField;

import java.util.Arrays;

public class ListFormatOverrideLevel
{
  private static final int BASE_SIZE = 8;

  int _iStartAt;
  byte _info;
   private static BitField _ilvl = new BitField(0xf);
   private static BitField _fStartAt = new BitField(0x10);
   private static BitField _fFormatting = new BitField(0x20);
  byte[] _reserved = new byte[3];
  ListLevel _lvl;

  public ListFormatOverrideLevel(byte[] buf, int offset)
  {
    _iStartAt = LittleEndian.getInt(buf, offset);
    offset += LittleEndian.INT_SIZE;
    _info = buf[offset++];
    System.arraycopy(buf, offset, _reserved, 0, _reserved.length);
    offset += _reserved.length;

    if (_fFormatting.getValue(_info) > 0)
    {
      _lvl = new ListLevel(buf, offset);
    }
  }

  public ListLevel getLevel()
  {
    return _lvl;
  }

  public int getLevelNum()
  {
    return _ilvl.getValue(_info);
  }

  public boolean isFormatting()
  {
    return _fFormatting.getValue(_info) != 0;
  }

  public boolean isStartAt()
  {
    return _fStartAt.getValue(_info) != 0;
  }

  public int getSizeInBytes()
  {
    return (_lvl == null ? BASE_SIZE : BASE_SIZE + _lvl.getSizeInBytes());
  }

  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    ListFormatOverrideLevel lfolvl = (ListFormatOverrideLevel)obj;
    return lfolvl._iStartAt == _iStartAt && lfolvl._info == _info &&
      lfolvl._lvl.equals(_lvl) && Arrays.equals(lfolvl._reserved, _reserved);
  }

  public byte[] toByteArray()
  {
    byte[] buf = new byte[getSizeInBytes()];

    int offset = 0;
    LittleEndian.putInt(buf, _iStartAt);
    offset += LittleEndian.INT_SIZE;
    buf[offset++] = _info;
    System.arraycopy(_reserved, 0, buf, offset, 3);
    offset += 3;

    if (_lvl != null)
    {
      byte[] levelBuf = _lvl.toByteArray();
      System.arraycopy(levelBuf, 0, buf, offset, levelBuf.length);
    }

    return buf;
  }
}
