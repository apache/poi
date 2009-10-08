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
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

import org.apache.poi.hwpf.model.io.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Ryan Ackley
 */
public final class ListTables
{
  private static final int LIST_DATA_SIZE = 28;
  private static final int LIST_FORMAT_OVERRIDE_SIZE = 16;
  private static POILogger log = POILogFactory.getLogger(ListTables.class);

  HashMap _listMap = new HashMap();
  ArrayList _overrideList = new ArrayList();

  public ListTables()
  {

  }

  public ListTables(byte[] tableStream, int lstOffset, int lfoOffset)
  {
    // get the list data
    int length = LittleEndian.getShort(tableStream, lstOffset);
    lstOffset += LittleEndian.SHORT_SIZE;
    int levelOffset = lstOffset + (length * LIST_DATA_SIZE);

    for (int x = 0; x < length; x++)
    {
      ListData lst = new ListData(tableStream, lstOffset);
      _listMap.put(Integer.valueOf(lst.getLsid()), lst);
      lstOffset += LIST_DATA_SIZE;

      int num = lst.numLevels();
      for (int y = 0; y < num; y++)
      {
        ListLevel lvl = new ListLevel(tableStream, levelOffset);
        lst.setLevel(y, lvl);
        levelOffset += lvl.getSizeInBytes();
      }
    }

    // now get the list format overrides. The size is an int unlike the LST size
    length = LittleEndian.getInt(tableStream, lfoOffset);
    lfoOffset += LittleEndian.INT_SIZE;
    int lfolvlOffset = lfoOffset + (LIST_FORMAT_OVERRIDE_SIZE * length);
    for (int x = 0; x < length; x++)
    {
      ListFormatOverride lfo = new ListFormatOverride(tableStream, lfoOffset);
      lfoOffset += LIST_FORMAT_OVERRIDE_SIZE;
      int num = lfo.numOverrides();
      for (int y = 0; y < num; y++)
      {
        while(tableStream[lfolvlOffset] == -1)
        {
          lfolvlOffset++;
        }
        ListFormatOverrideLevel lfolvl = new ListFormatOverrideLevel(tableStream, lfolvlOffset);
        lfo.setOverride(y, lfolvl);
        lfolvlOffset += lfolvl.getSizeInBytes();
      }
      _overrideList.add(lfo);
    }
  }

  public int addList(ListData lst, ListFormatOverride override)
  {
    int lsid = lst.getLsid();
    while (_listMap.get(Integer.valueOf(lsid)) != null)
    {
      lsid = lst.resetListID();
      override.setLsid(lsid);
    }
    _listMap.put(Integer.valueOf(lsid), lst);
    _overrideList.add(override);
    return lsid;
  }

  public void writeListDataTo(HWPFOutputStream tableStream)
    throws IOException
  {

    Integer[] intList = (Integer[])_listMap.keySet().toArray(new Integer[0]);

    // use this stream as a buffer for the levels since their size varies.
    ByteArrayOutputStream levelBuf = new ByteArrayOutputStream();

    // use a byte array for the lists because we know their size.
    byte[] listBuf = new byte[intList.length * LIST_DATA_SIZE];

    byte[] shortHolder = new byte[2];
    LittleEndian.putShort(shortHolder, (short)intList.length);
    tableStream.write(shortHolder);

    for (int x = 0; x < intList.length; x++)
    {
      ListData lst = (ListData)_listMap.get(intList[x]);
      tableStream.write(lst.toByteArray());
      ListLevel[] lvls = lst.getLevels();
      for (int y = 0; y < lvls.length; y++)
      {
        levelBuf.write(lvls[y].toByteArray());
      }
    }
    tableStream.write(levelBuf.toByteArray());
  }

  public void writeListOverridesTo(HWPFOutputStream tableStream)
    throws IOException
  {

    // use this stream as a buffer for the levels since their size varies.
    ByteArrayOutputStream levelBuf = new ByteArrayOutputStream();

    int size = _overrideList.size();

    byte[] intHolder = new byte[4];
    LittleEndian.putInt(intHolder, size);
    tableStream.write(intHolder);

    for (int x = 0; x < size; x++)
    {
      ListFormatOverride lfo = (ListFormatOverride)_overrideList.get(x);
      tableStream.write(lfo.toByteArray());
      ListFormatOverrideLevel[] lfolvls = lfo.getLevelOverrides();
      for (int y = 0; y < lfolvls.length; y++)
      {
        levelBuf.write(lfolvls[y].toByteArray());
      }
    }
    tableStream.write(levelBuf.toByteArray());

  }

  public ListFormatOverride getOverride(int lfoIndex)
  {
    return (ListFormatOverride)_overrideList.get(lfoIndex - 1);
  }

  public int getOverrideIndexFromListID(int lstid)
  {
    int returnVal = -1;
    int size = _overrideList.size();
    for (int x = 0; x < size; x++)
    {
      ListFormatOverride next = (ListFormatOverride)_overrideList.get(x);
      if (next.getLsid() == lstid)
      {
        // 1-based index I think
        returnVal = x+1;
        break;
      }
    }
    if (returnVal == -1)
    {
      throw new NoSuchElementException("No list found with the specified ID");
    }
    return returnVal;
  }

  public ListLevel getLevel(int listID, int level)
  {
    ListData lst = (ListData)_listMap.get(Integer.valueOf(listID));
    if(level < lst.numLevels()) {
    	ListLevel lvl = lst.getLevels()[level];
    	return lvl;
    }
	log.log(POILogger.WARN, "Requested level " + level + " which was greater than the maximum defined (" + lst.numLevels() + ")");
	return null;
  }

  public ListData getListData(int listID)
  {
    return (ListData) _listMap.get(Integer.valueOf(listID));
  }

  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    ListTables tables = (ListTables)obj;

    if (_listMap.size() == tables._listMap.size())
    {
      Iterator it = _listMap.keySet().iterator();
      while (it.hasNext())
      {
        Object key = it.next();
        ListData lst1 = (ListData)_listMap.get(key);
        ListData lst2 = (ListData)tables._listMap.get(key);
        if (!lst1.equals(lst2))
        {
          return false;
        }
      }
      int size = _overrideList.size();
      if (size == tables._overrideList.size())
      {
        for (int x = 0; x < size; x++)
        {
          if (!_overrideList.get(x).equals(tables._overrideList.get(x)))
          {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }
}
