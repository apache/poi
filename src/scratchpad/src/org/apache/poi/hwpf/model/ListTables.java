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

import org.apache.poi.hwpf.model.io.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ListTables
{
  private static final int LIST_DATA_SIZE = 28;
  private static final int LIST_FORMAT_OVERRIDE_SIZE = 16;

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
      _listMap.put(new Integer(lst.getLsid()), lst);
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
    while (_listMap.get(new Integer(lsid)) != null)
    {
      lsid = lst.resetListID();
      override.setLsid(lsid);
    }
    _listMap.put(new Integer(lsid), lst);
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
    ListData lst = (ListData)_listMap.get(new Integer(listID));
    ListLevel lvl = lst.getLevels()[level];
    return lvl;
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
