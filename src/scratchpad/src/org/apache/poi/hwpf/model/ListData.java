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

public class ListData
{
  private int _lsid;
  private int _tplc;
  private short[] _rgistd;
  private byte _info;
    private static BitField _fSimpleList = new BitField(0x1);
    private static BitField _fRestartHdn = new BitField(0x2);
  private byte _reserved;
  ListLevel[] _levels;

  public ListData(int listID, boolean numbered)
  {
    _lsid = listID;
    _rgistd = new short[9];

    for (int x = 0; x < 9; x++)
    {
      _rgistd[x] = StyleSheet.NIL_STYLE;
    }

    _levels = new ListLevel[9];

    for (int x = 0; x < _levels.length; x++)
    {
      _levels[x] = new ListLevel(x, numbered);
    }
  }

  ListData(byte[] buf, int offset)
  {
    _lsid = LittleEndian.getInt(buf, offset);
    offset += LittleEndian.INT_SIZE;
    _tplc = LittleEndian.getInt(buf, offset);
    offset += LittleEndian.INT_SIZE;
    _rgistd = new short[9];
    for (int x = 0; x < 9; x++)
    {
      _rgistd[x] = LittleEndian.getShort(buf, offset);
      offset += LittleEndian.SHORT_SIZE;
    }
    _info = buf[offset++];
    _reserved = buf[offset];
    if (_fSimpleList.getValue(_info) > 0)
    {
      _levels = new ListLevel[1];
    }
    else
    {
      _levels = new ListLevel[9];
    }

  }

  public int getLsid()
  {
    return _lsid;
  }

  public int numLevels()
  {
    return _levels.length;
  }

  public void setLevel(int index, ListLevel level)
  {
    _levels[index] = level;
  }

  public ListLevel[] getLevels()
  {
    return _levels;
  }

  /**
   * Gets the level associated to a particular List at a particular index.
   *
   * @param index 1-based index
   * @return a list level
   */
  public ListLevel getLevel(int index)
  {
    return _levels[index - 1];
  }

  public int getLevelStyle(int index)
  {
    return _rgistd[index];
  }

  public void setLevelStyle(int index, int styleIndex)
  {
    _rgistd[index] = (short)styleIndex;
  }

  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    ListData lst = (ListData)obj;
    return lst._info == _info && Arrays.equals(lst._levels, _levels) &&
      lst._lsid == _lsid && lst._reserved == _reserved && lst._tplc == _tplc &&
      Arrays.equals(lst._rgistd, _rgistd);
  }

  int resetListID()
  {
    _lsid = (int)(Math.random() * (double)System.currentTimeMillis());
    return _lsid;
  }

  public byte[] toByteArray()
  {
    byte[] buf = new byte[28];
    int offset = 0;
    LittleEndian.putInt(buf, _lsid);
    offset += LittleEndian.INT_SIZE;
    LittleEndian.putInt(buf, offset, _tplc);
    offset += LittleEndian.INT_SIZE;
    for (int x = 0; x < 9; x++)
    {
      LittleEndian.putShort(buf, offset, _rgistd[x]);
      offset += LittleEndian.SHORT_SIZE;
    }
    buf[offset++] = _info;
    buf[offset] = _reserved;
    return buf;
  }
}
