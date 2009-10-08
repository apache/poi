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

package org.apache.poi.hdf.extractor.data;

import java.util.*;

import org.apache.poi.hdf.extractor.*;


/**
 * Comment me
 *
 * @author Ryan Ackley
 */

public final class ListTables
{

  LFO[] _pllfo;
  Hashtable _lists = new Hashtable();

  public ListTables(byte[] plcflst, byte[] plflfo)
  {
    initLST(plcflst);
    initLFO(plflfo);
  }
  public LVL getLevel(int list, int level)
  {

    LFO override = _pllfo[list - 1];

    for(int x = 0; x < override._clfolvl; x++)
    {
      if(override._levels[x]._ilvl == level)
      {
        LFOLVL lfolvl = override._levels[x];
        if(lfolvl._fFormatting)
        {
          LST lst = (LST)_lists.get(Integer.valueOf(override._lsid));
          LVL lvl = lfolvl._override;
          lvl._istd = Utils.convertBytesToShort(lst._rgistd, level * 2);
          return lvl;
        }
        else if(lfolvl._fStartAt)
        {
          LST lst = (LST)_lists.get(Integer.valueOf(override._lsid));
          LVL lvl = lst._levels[level];
          LVL newLvl = (LVL)lvl.clone();
          newLvl._istd = Utils.convertBytesToShort(lst._rgistd, level * 2);
          newLvl._iStartAt = lfolvl._iStartAt;
          return newLvl;
        }
      }
    }

    LST lst = (LST)_lists.get(Integer.valueOf(override._lsid));
    LVL lvl = lst._levels[level];
    lvl._istd = Utils.convertBytesToShort(lst._rgistd, level * 2);
    return lvl;


  }
  private void initLST(byte[] plcflst)
  {
    short length = Utils.convertBytesToShort(plcflst, 0);
    int nextLevelOffset = 0;
    //LST[] lstArray = new LST[length];
    for(int x = 0; x < length; x++)
    {
      LST lst = new LST();
      lst._lsid = Utils.convertBytesToInt(plcflst, 2 + (x * 28));
      lst._tplc = Utils.convertBytesToInt(plcflst, 2 + 4 + (x * 28));
      System.arraycopy(plcflst, 2 + 8 + (x * 28), lst._rgistd, 0, 18);
      byte code = plcflst[2 + 26 + (x * 28)];
      lst._fSimpleList = StyleSheet.getFlag(code & 0x01);
      //lstArray[x] = lst;
      _lists.put(Integer.valueOf(lst._lsid), lst);

      if(lst._fSimpleList)
      {
        lst._levels = new LVL[1];
      }
      else
      {
        lst._levels = new LVL[9];
      }

      for(int y = 0; y < lst._levels.length; y++)
      {
        int offset = 2 + (length * 28) + nextLevelOffset;
        lst._levels[y] = new LVL();
        nextLevelOffset += createLVL(plcflst, offset, lst._levels[y]);
      }
    }


  }
  private void initLFO(byte[] plflfo)
  {
    int lfoSize = Utils.convertBytesToInt(plflfo, 0);
    _pllfo = new LFO[lfoSize];
    for(int x = 0; x < lfoSize; x++)
    {
      LFO nextLFO = new LFO();
      nextLFO._lsid = Utils.convertBytesToInt(plflfo, 4 + (x * 16));
      nextLFO._clfolvl = plflfo[4 + 12 + (x * 16)];
      nextLFO._levels = new LFOLVL[nextLFO._clfolvl];
      _pllfo[x] = nextLFO;
    }

    int lfolvlOffset = (lfoSize * 16) + 4;
    int lvlOffset = 0;
    int lfolvlNum = 0;
    for(int x = 0; x < lfoSize; x++)
    {
      for(int y = 0; y < _pllfo[x]._clfolvl; y++)
      {
        int offset = lfolvlOffset + (lfolvlNum * 8) + lvlOffset;
        LFOLVL lfolvl = new LFOLVL();
        lfolvl._iStartAt = Utils.convertBytesToInt(plflfo, offset);
        lfolvl._ilvl = Utils.convertBytesToInt(plflfo, offset + 4);
        lfolvl._fStartAt = StyleSheet.getFlag(lfolvl._ilvl & 0x10);
        lfolvl._fFormatting = StyleSheet.getFlag(lfolvl._ilvl & 0x20);
        lfolvl._ilvl = (lfolvl._ilvl & (byte)0x0f);
        lfolvlNum++;

        if(lfolvl._fFormatting)
        {
          offset = lfolvlOffset + (lfolvlNum * 12) + lvlOffset;
          lfolvl._override = new LVL();
          lvlOffset += createLVL(plflfo, offset, lfolvl._override);
        }
        _pllfo[x]._levels[y] = lfolvl;
      }
    }
  }
  private int createLVL(byte[] data, int offset, LVL lvl)
  {

    lvl._iStartAt = Utils.convertBytesToInt(data, offset);
    lvl._nfc = data[offset + 4];
    int code = Utils.convertBytesToInt(data, offset + 5);
    lvl._jc = (byte)(code & 0x03);
    lvl._fLegal = StyleSheet.getFlag(code & 0x04);
    lvl._fNoRestart = StyleSheet.getFlag(code & 0x08);
    lvl._fPrev = StyleSheet.getFlag(code & 0x10);
    lvl._fPrevSpace = StyleSheet.getFlag(code & 0x20);
    lvl._fWord6 = StyleSheet.getFlag(code & 0x40);
    System.arraycopy(data, offset + 6, lvl._rgbxchNums, 0, 9);
    lvl._ixchFollow = data[offset + 15];
    int chpxSize = data[offset + 24];
    int papxSize = data[offset + 25];
    lvl._chpx = new byte[chpxSize];
    lvl._papx = new byte[papxSize];
    System.arraycopy(data, offset + 28, lvl._papx, 0, papxSize);
    System.arraycopy(data, offset + 28 + papxSize, lvl._chpx, 0, chpxSize);
    offset += 28 + papxSize + chpxSize;//modify offset
    int xstSize = Utils.convertBytesToShort(data, offset);
    lvl._xst = new char[xstSize];

    offset += 2;
    for(int x = 0; x < xstSize; x++)
    {
      lvl._xst[x] = (char)Utils.convertBytesToShort(data, offset + (x * 2));
    }
    return 28 + papxSize + chpxSize + 2 + (xstSize * 2);
  }
}
