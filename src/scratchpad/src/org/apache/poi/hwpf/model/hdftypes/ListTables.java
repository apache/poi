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

package org.apache.poi.hwpf.model.hdftypes;

import org.apache.poi.util.LittleEndian;

import org.apache.poi.hwpf.model.io.*;

import java.util.HashMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ListTables
{
  private static final int LIST_DATA_SIZE = 28;
  private static final int LIST_FORMAT_OVERRIDE_SIZE = 16;

  HashMap listMap = new HashMap();
  HashMap overrideMap = new HashMap();

  public ListTables(byte[] tableStream, int lstOffset, int lfoOffset)
  {
    // get the list data
    int length = LittleEndian.getShort(tableStream, lstOffset);
    lstOffset += LittleEndian.SHORT_SIZE;
    int levelOffset = lstOffset + (length * LIST_DATA_SIZE);

    for (int x = 0; x < length; x++)
    {
      ListData lst = new ListData(tableStream, lstOffset);
      listMap.put(new Integer(lst.getLsid()), lst);
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
    int lfolvlOffset = LIST_FORMAT_OVERRIDE_SIZE * length + 4;
    for (int x = 0; x < length; x++)
    {
      ListFormatOverride lfo = new ListFormatOverride(tableStream, lfoOffset);
      lfoOffset += LIST_FORMAT_OVERRIDE_SIZE;
      int num = lfo.numOverrides();
      for (int y = 0; y < num; y++)
      {
        ListFormatOverrideLevel lfolvl = new ListFormatOverrideLevel(tableStream, lfolvlOffset);
        lfo.setOverride(y, lfolvl);
        lfolvlOffset += lfolvl.getSizeInBytes();
      }
      overrideMap.put(new Integer(lfo.getLsid()), lfo);
    }
  }

  public void writeListDataTo(HWPFOutputStream tableStream)
    throws IOException
  {

    Integer[] intList = (Integer[])listMap.keySet().toArray(new Integer[0]);

    // use this stream as a buffer for the levels since their size varies.
    ByteArrayOutputStream levelBuf = new ByteArrayOutputStream();

    // use a byte array for the lists because we know their size.
    byte[] listBuf = new byte[intList.length * LIST_DATA_SIZE];


    for (int x = 0; x < intList.length; x++)
    {
      ListData lst = (ListData)listMap.get(intList[x]);
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
    Integer[] intList = (Integer[])overrideMap.keySet().toArray(new Integer[0]);

    // use this stream as a buffer for the levels since their size varies.
    ByteArrayOutputStream levelBuf = new ByteArrayOutputStream();

    // use a byte array for the lists because we know their size.
    byte[] overrideBuf = new byte[intList.length * LIST_FORMAT_OVERRIDE_SIZE];


    for (int x = 0; x < intList.length; x++)
    {
      ListFormatOverride lfo = (ListFormatOverride)overrideMap.get(intList[x]);
      tableStream.write(lfo.toByteArray());
      ListFormatOverrideLevel[] lfolvls = lfo.getLevelOverrides();
      for (int y = 0; y < lfolvls.length; y++)
      {
        levelBuf.write(lfolvls[y].toByteArray());
      }
    }
    tableStream.write(levelBuf.toByteArray());

  }
}
