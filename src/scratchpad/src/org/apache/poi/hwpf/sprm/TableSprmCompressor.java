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

import org.apache.poi.hwpf.usermodel.TableProperties;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.hwpf.usermodel.TableCellDescriptor;
import org.apache.poi.hwpf.usermodel.BorderCode;

import java.util.ArrayList;
import java.util.Arrays;

public final class TableSprmCompressor
{
  public TableSprmCompressor()
  {
  }
  public static byte[] compressTableProperty(TableProperties newTAP)
  {
    int size = 0;
    ArrayList sprmList = new ArrayList();

    if (newTAP.getJc() != 0)
    {
      size += SprmUtils.addSprm((short)0x5400, newTAP.getJc(), null, sprmList);
    }
    if (newTAP.getFCantSplit())
    {
      size += SprmUtils.addSprm((short)0x3403, 1, null, sprmList);
    }
    if (newTAP.getFTableHeader())
    {
      size += SprmUtils.addSprm((short)0x3404, 1, null, sprmList);
    }
    byte[] brcBuf = new byte[6 * BorderCode.SIZE];
    int offset = 0;
    newTAP.getBrcTop().serialize(brcBuf, offset);
    offset += BorderCode.SIZE;
    newTAP.getBrcLeft().serialize(brcBuf, offset);
    offset += BorderCode.SIZE;
    newTAP.getBrcBottom().serialize(brcBuf, offset);
    offset += BorderCode.SIZE;
    newTAP.getBrcRight().serialize(brcBuf, offset);
    offset += BorderCode.SIZE;
    newTAP.getBrcHorizontal().serialize(brcBuf, offset);
    offset += BorderCode.SIZE;
    newTAP.getBrcVertical().serialize(brcBuf, offset);
    byte[] compare = new byte[6 * BorderCode.SIZE];
    if (!Arrays.equals(brcBuf, compare))
    {
      size += SprmUtils.addSprm((short)0xD605, 0, brcBuf, sprmList);
    }
    if (newTAP.getDyaRowHeight() != 0)
    {
      size += SprmUtils.addSprm((short)0x9407, newTAP.getDyaRowHeight(), null, sprmList);
    }
    if (newTAP.getItcMac() > 0)
    {
      int itcMac = newTAP.getItcMac();
      byte[] buf = new byte[1 + (LittleEndian.SHORT_SIZE*(itcMac + 1)) + (TableCellDescriptor.SIZE*itcMac)];
      buf[0] = (byte)itcMac;

      short[] dxaCenters = newTAP.getRgdxaCenter();
      for (int x = 0; x < dxaCenters.length; x++)
      {
        LittleEndian.putShort(buf, 1 + (x * LittleEndian.SHORT_SIZE),
                              dxaCenters[x]);
      }

      TableCellDescriptor[] cellDescriptors = newTAP.getRgtc();
      for (int x = 0; x < cellDescriptors.length; x++)
      {
        cellDescriptors[x].serialize(buf,
          1+((itcMac+1)*LittleEndian.SHORT_SIZE)+(x*TableCellDescriptor.SIZE));
      }
      size += SprmUtils.addSpecialSprm((short)0xD608, buf, sprmList);

//      buf = new byte[(itcMac * ShadingDescriptor.SIZE) + 1];
//      buf[0] = (byte)itcMac;
//      ShadingDescriptor[] shds = newTAP.getRgshd();
//      for (int x = 0; x < itcMac; x++)
//      {
//        shds[x].serialize(buf, 1 + (x * ShadingDescriptor.SIZE));
//      }
//      size += SprmUtils.addSpecialSprm((short)0xD609, buf, sprmList);
    }
    if (newTAP.getTlp() != 0)
    {
      size += SprmUtils.addSprm((short)0x740a, newTAP.getTlp(), null, sprmList);
    }

    return SprmUtils.getGrpprl(sprmList, size);
  }
}
