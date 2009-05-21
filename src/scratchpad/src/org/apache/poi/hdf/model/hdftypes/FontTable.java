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

package org.apache.poi.hdf.model.hdftypes;

import org.apache.poi.util.LittleEndian;
/**
 * Comment me
 *
 * @author Ryan Ackley
 */

public final class FontTable implements HDFType
{
  String[] fontNames;

  public FontTable(byte[] fontTable)
  {
    int size = LittleEndian.getShort(fontTable, 0);
    fontNames = new String[size];

    int currentIndex = 4;
    for(int x = 0; x < size; x++)
    {
      byte ffnLength = fontTable[currentIndex];

      int nameOffset = currentIndex + 40;
      StringBuffer nameBuf = new StringBuffer();
      //char ch = Utils.getUnicodeCharacter(fontTable, nameOffset);
      char ch = (char)LittleEndian.getShort(fontTable, nameOffset);
      while(ch != '\0')
      {
        nameBuf.append(ch);
        nameOffset += 2;
        ch = (char)LittleEndian.getShort(fontTable, nameOffset);
      }
      fontNames[x] = nameBuf.toString();
      currentIndex += ffnLength + 1;
    }

  }
  public String getFont(int index)
  {
    return fontNames[index];
  }
}
