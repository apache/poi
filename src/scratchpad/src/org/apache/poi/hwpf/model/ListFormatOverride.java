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

import org.apache.poi.util.LittleEndian;

import java.util.Arrays;

public final class ListFormatOverride
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
