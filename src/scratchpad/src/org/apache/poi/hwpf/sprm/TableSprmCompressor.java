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

package org.apache.poi.hwpf.sprm;

import org.apache.poi.hwpf.usermodel.TableProperties;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.hwpf.usermodel.TableCellDescriptor;
import org.apache.poi.hwpf.usermodel.ShadingDescriptor;
import org.apache.poi.hwpf.usermodel.BorderCode;

import java.util.ArrayList;
import java.util.Arrays;

public class TableSprmCompressor
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
