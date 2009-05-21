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

package org.apache.poi.hwpf.sprm;

import java.util.List;

import org.apache.poi.util.LittleEndian;


public final class SprmUtils
{
  public SprmUtils()
  {
  }

  public static byte[] shortArrayToByteArray(short[] convert)
  {
    byte[] buf = new byte[convert.length * LittleEndian.SHORT_SIZE];

    for (int x = 0; x < convert.length; x++)
    {
      LittleEndian.putShort(buf, x * LittleEndian.SHORT_SIZE, convert[x]);
    }

    return buf;
  }

  public static int addSpecialSprm(short instruction, byte[] varParam, List list)
  {
    byte[] sprm = new byte[varParam.length + 4];
    System.arraycopy(varParam, 0, sprm, 4, varParam.length);
    LittleEndian.putShort(sprm, instruction);
    LittleEndian.putShort(sprm, 2, (short)(varParam.length + 1));
    list.add(sprm);
    return sprm.length;
  }

  public static int addSprm(short instruction, int param, byte[] varParam, List list)
  {
    int type = (instruction & 0xe000) >> 13;

    byte[] sprm = null;
    switch(type)
    {
      case 0:
      case 1:
        sprm = new byte[3];
        sprm[2] = (byte)param;
        break;
      case 2:
        sprm = new byte[4];
        LittleEndian.putShort(sprm, 2, (short)param);
        break;
      case 3:
        sprm = new byte[6];
        LittleEndian.putInt(sprm, 2, param);
        break;
      case 4:
      case 5:
        sprm = new byte[4];
        LittleEndian.putShort(sprm, 2, (short)param);
        break;
      case 6:
        sprm = new byte[3 + varParam.length];
        sprm[2] = (byte)varParam.length;
        System.arraycopy(varParam, 0, sprm, 3, varParam.length);
        break;
      case 7:
        sprm = new byte[5];
        // this is a three byte int so it has to be handled special
        byte[] temp = new byte[4];
        LittleEndian.putInt(temp, 0, param);
        System.arraycopy(temp, 0, sprm, 2, 3);
        break;
      default:
        //should never happen
        break;
    }
    LittleEndian.putShort(sprm, 0, instruction);
    list.add(sprm);
    return sprm.length;
  }

  public static byte[] getGrpprl(List sprmList, int size)
  {
    // spit out the final grpprl
    byte[] grpprl = new byte[size];
    int listSize = sprmList.size() - 1;
    int index = 0;
    for (; listSize >= 0; listSize--)
    {
      byte[] sprm = (byte[])sprmList.remove(0);
      System.arraycopy(sprm, 0, grpprl, index, sprm.length);
      index += sprm.length;
    }

    return grpprl;

  }

  public static int convertBrcToInt(short[] brc)
  {
    byte[] buf = new byte[4];
    LittleEndian.putShort(buf, brc[0]);
    LittleEndian.putShort(buf, LittleEndian.SHORT_SIZE, brc[1]);
    return LittleEndian.getInt(buf);
  }
}
