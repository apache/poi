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

import java.util.Arrays;

public class ListFormatOverride
{
  int _lsid;
  int _reserved1;
  int _reserved2;
  byte _clfolvl;
  byte[] _reserved3 = new byte[3];
  ListFormatOverrideLevel[] _levelOverrides;

  public ListFormatOverride(int lsid)
  {
    _lsid = lsid;
    _levelOverrides = new ListFormatOverrideLevel[0];
  }

  public ListFormatOverride(byte[] buf, int offset)
  {
    _lsid = LittleEndian.getInt(buf, offset);
    offset += LittleEndian.INT_SIZE;
    _reserved1 = LittleEndian.getInt(buf, offset);
    offset += LittleEndian.INT_SIZE;
    _reserved2 = LittleEndian.getInt(buf, offset);
    offset += LittleEndian.INT_SIZE;
    _clfolvl = buf[offset++];
    System.arraycopy(buf, offset, _reserved3, 0, _reserved3.length);
    _levelOverrides = new ListFormatOverrideLevel[_clfolvl];
  }

  public int numOverrides()
  {
    return _clfolvl;
  }

  public int getLsid()
  {
    return _lsid;
  }

  void setLsid(int lsid)
  {
    _lsid = lsid;
  }

  public ListFormatOverrideLevel[] getLevelOverrides()
  {
    return _levelOverrides;
  }

  public void setOverride(int index, ListFormatOverrideLevel lfolvl)
  {
    _levelOverrides[index] = lfolvl;
  }

  public ListFormatOverrideLevel getOverrideLevel(int level)
  {

    ListFormatOverrideLevel retLevel = null;

    for(int x = 0; x < _levelOverrides.length; x++)
    {
      if (_levelOverrides[x].getLevelNum() == level)
      {
        retLevel = _levelOverrides[x];
      }
    }
    return retLevel;
  }

  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    ListFormatOverride lfo = (ListFormatOverride)obj;
    return lfo._clfolvl == _clfolvl && lfo._lsid == _lsid &&
      lfo._reserved1 == _reserved1 && lfo._reserved2 == _reserved2 &&
      Arrays.equals(lfo._reserved3, _reserved3) &&
      Arrays.equals(lfo._levelOverrides, _levelOverrides);
  }

  public byte[] toByteArray()
  {
    byte[] buf = new byte[16];
    int offset = 0;
    LittleEndian.putInt(buf, offset, _lsid);
    offset += LittleEndian.INT_SIZE;
    LittleEndian.putInt(buf, offset, _reserved1);
    offset += LittleEndian.INT_SIZE;
    LittleEndian.putInt(buf, offset, _reserved2);
    offset += LittleEndian.INT_SIZE;
    buf[offset++] = _clfolvl;
    System.arraycopy(_reserved3, 0, buf, offset, 3);

    return buf;
  }
}
