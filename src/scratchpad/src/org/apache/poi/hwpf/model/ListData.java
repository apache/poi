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

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;

import java.util.Arrays;

public final class ListData
{
  private int _lsid;
  private int _tplc;
  private short[] _rgistd;
  private byte _info;
    private static BitField _fSimpleList = BitFieldFactory.getInstance(0x1);
    private static BitField _fRestartHdn = BitFieldFactory.getInstance(0x2);
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
    _lsid = (int)(Math.random() * System.currentTimeMillis());
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
