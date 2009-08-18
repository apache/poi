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
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.poi.util.LittleEndian;

import org.apache.poi.hwpf.usermodel.ParagraphProperties;

public final class ParagraphSprmCompressor
{
  public ParagraphSprmCompressor()
  {
  }

  public static byte[] compressParagraphProperty(ParagraphProperties newPAP,
                                                 ParagraphProperties oldPAP)
  {
    List sprmList = new ArrayList();
    int size = 0;

    if (newPAP.getJc() != oldPAP.getJc())
    {
      size += SprmUtils.addSprm((short)0x2403, newPAP.getJc(), null, sprmList);
    }
    if (newPAP.getFKeep() != oldPAP.getFKeep())
    {
      size += SprmUtils.addSprm((short)0x2405, newPAP.getFKeep(), null, sprmList);
    }
    if (newPAP.getFKeepFollow() != oldPAP.getFKeepFollow())
    {
      size += SprmUtils.addSprm((short)0x2406, newPAP.getFKeepFollow(), null, sprmList);
    }
    if (newPAP.getFPageBreakBefore() != oldPAP.getFPageBreakBefore())
    {
      size += SprmUtils.addSprm((short)0x2407, newPAP.getFPageBreakBefore(), null, sprmList);
    }
    if (newPAP.getBrcl() != oldPAP.getBrcl())
    {
      size += SprmUtils.addSprm((short)0x2408, newPAP.getBrcl(), null, sprmList);
    }
    if (newPAP.getBrcp() != oldPAP.getBrcp())
    {
      size += SprmUtils.addSprm((short)0x2409, newPAP.getBrcp(), null, sprmList);
    }
    if (newPAP.getIlvl() != oldPAP.getIlvl())
    {
      size += SprmUtils.addSprm((short)0x260A, newPAP.getIlvl(), null, sprmList);
    }
    if (newPAP.getIlfo() != oldPAP.getIlfo())
    {
      size += SprmUtils.addSprm((short)0x460b, newPAP.getIlfo(), null, sprmList);
    }
    if (newPAP.getFNoLnn() != oldPAP.getFNoLnn())
    {
      size += SprmUtils.addSprm((short)0x240C, newPAP.getFNoLnn(), null, sprmList);
    }
    if (newPAP.getFSideBySide() != oldPAP.getFSideBySide())
    {
      size += SprmUtils.addSprm((short)0x2404, newPAP.getFSideBySide(), null, sprmList);
    }
    if (newPAP.getFNoAutoHyph() != oldPAP.getFNoAutoHyph())
    {
      size += SprmUtils.addSprm((short)0x242A, newPAP.getFNoAutoHyph(), null, sprmList);
    }
    if (newPAP.getFWidowControl() != oldPAP.getFWidowControl())
    {
      size += SprmUtils.addSprm((short)0x2431, newPAP.getFWidowControl(), null, sprmList);
    }
    if (newPAP.getItbdMac() != oldPAP.getItbdMac() ||
        !Arrays.equals(newPAP.getRgdxaTab(), oldPAP.getRgdxaTab()) ||
        !Arrays.equals(newPAP.getRgtbd(), oldPAP.getRgtbd()))
    {
      /** @todo revisit this */
//      byte[] oldTabArray = oldPAP.getRgdxaTab();
//      byte[] newTabArray = newPAP.getRgdxaTab();
//      byte[] newTabDescriptors = newPAP.getRgtbd();
//      byte[] varParam = new byte[2 + oldTabArray.length + newTabArray.length +
//                                 newTabDescriptors.length];
//      varParam[0] = (byte)(oldTabArray.length/2);
//      int offset = 1;
//      System.arraycopy(oldTabArray, 0, varParam, offset, oldTabArray.length);
//      offset += oldTabArray.length;
//      varParam[offset] = (byte)(newTabArray.length/2);
//      offset += 1;
//      System.arraycopy(newTabArray, 0, varParam, offset, newTabArray.length);
//      offset += newTabArray.length;
//      System.arraycopy(newTabDescriptors, 0, varParam, offset, newTabDescriptors.length);
//
//      size += SprmUtils.addSprm((short)0xC60D, 0, varParam, sprmList);
    }
    if (newPAP.getDxaRight() != oldPAP.getDxaRight())
    {
      size += SprmUtils.addSprm((short)0x840E, newPAP.getDxaRight(), null, sprmList);
    }
    if (newPAP.getDxaLeft() != oldPAP.getDxaLeft())
    {
      size += SprmUtils.addSprm((short)0x840F, newPAP.getDxaLeft(), null, sprmList);
    }
    if (newPAP.getDxaLeft1() != oldPAP.getDxaLeft1())
    {
      size += SprmUtils.addSprm((short)0x8411, newPAP.getDxaLeft1(), null, sprmList);
    }
    if (!newPAP.getLspd().equals(oldPAP.getLspd()))
    {
      byte[] buf = new byte[4];
      newPAP.getLspd().serialize(buf, 0);

      size += SprmUtils.addSprm((short)0x6412, LittleEndian.getInt(buf), null, sprmList);
    }
    if (newPAP.getDyaBefore() != oldPAP.getDyaBefore())
    {
      size += SprmUtils.addSprm((short)0xA413, newPAP.getDyaBefore(), null, sprmList);
    }
    if (newPAP.getDyaAfter() != oldPAP.getDyaAfter())
    {
      size += SprmUtils.addSprm((short)0xA414, newPAP.getDyaAfter(), null, sprmList);
    }
    if (newPAP.getDyaBefore() != oldPAP.getDyaBefore())
    {
      size += SprmUtils.addSprm((short)0x2404, newPAP.getDyaBefore(), null, sprmList);
    }
    if (newPAP.getFKinsoku() != oldPAP.getFKinsoku())
    {
      size += SprmUtils.addSprm((short)0x2433, newPAP.getDyaBefore(), null, sprmList);
    }
    if (newPAP.getFWordWrap() != oldPAP.getFWordWrap())
    {
      size += SprmUtils.addSprm((short)0x2434, newPAP.getFWordWrap(), null, sprmList);
    }
    if (newPAP.getFOverflowPunct() != oldPAP.getFOverflowPunct())
    {
      size += SprmUtils.addSprm((short)0x2435, newPAP.getFOverflowPunct(), null, sprmList);
    }
    if (newPAP.getFTopLinePunct() != oldPAP.getFTopLinePunct())
    {
      size += SprmUtils.addSprm((short)0x2436, newPAP.getFTopLinePunct(), null, sprmList);
    }
    if (newPAP.getFAutoSpaceDE() != oldPAP.getFAutoSpaceDE())
    {
      size += SprmUtils.addSprm((short)0x2437, newPAP.getFAutoSpaceDE(), null, sprmList);
    }
    if (newPAP.getFAutoSpaceDN() != oldPAP.getFAutoSpaceDN())
    {
      size += SprmUtils.addSprm((short)0x2438, newPAP.getFAutoSpaceDN(), null, sprmList);
    }
    if (newPAP.getWAlignFont() != oldPAP.getWAlignFont())
    {
      size += SprmUtils.addSprm((short)0x4439, newPAP.getWAlignFont(), null, sprmList);
    }
    if (newPAP.isFBackward() != oldPAP.isFBackward() ||
        newPAP.isFVertical() != oldPAP.isFVertical() ||
        newPAP.isFRotateFont() != oldPAP.isFRotateFont())
    {
      int val = 0;
      if (newPAP.isFBackward())
      {
        val |= 0x2;
      }
      if (newPAP.isFVertical())
      {
        val |= 0x1;
      }
      if (newPAP.isFRotateFont())
      {
        val |= 0x4;
      }
      size += SprmUtils.addSprm((short)0x443A, val, null, sprmList);
    }
    if (!Arrays.equals(newPAP.getAnld(), oldPAP.getAnld()))
    {
      size += SprmUtils.addSprm((short)0xC63E, 0, newPAP.getAnld(), sprmList);
    }
    if (newPAP.getFInTable() != oldPAP.getFInTable())
    {
      size += SprmUtils.addSprm((short)0x2416, newPAP.getFInTable(), null, sprmList);
    }
    if (newPAP.getFTtp() != oldPAP.getFTtp())
    {
      size += SprmUtils.addSprm((short)0x2417, newPAP.getFTtp(), null, sprmList);
    }
    if (newPAP.getWr() != oldPAP.getWr())
    {
      size += SprmUtils.addSprm((short)0x2423, newPAP.getWr(), null, sprmList);
    }
    if (newPAP.getFLocked() != oldPAP.getFLocked())
    {
      size += SprmUtils.addSprm((short)0x2430, newPAP.getFLocked(), null, sprmList);
    }
    if (newPAP.getDxaAbs() != oldPAP.getDxaAbs())
    {
      size += SprmUtils.addSprm((short)0x8418, newPAP.getDxaAbs(), null, sprmList);
    }
    if (newPAP.getDyaAbs() != oldPAP.getDyaAbs())
    {
      size += SprmUtils.addSprm((short)0x8419, newPAP.getDyaAbs(), null, sprmList);
    }
    if (newPAP.getDxaWidth() != oldPAP.getDxaWidth())
    {
      size += SprmUtils.addSprm((short)0x841A, newPAP.getDxaWidth(), null, sprmList);
    }
    if (!newPAP.getBrcTop().equals(oldPAP.getBrcTop()))
    {
      int brc = newPAP.getBrcTop().toInt();
      size += SprmUtils.addSprm((short)0x6424, brc, null, sprmList);
    }
    if (!newPAP.getBrcLeft().equals(oldPAP.getBrcLeft()))
    {
      int brc = newPAP.getBrcLeft().toInt();
      size += SprmUtils.addSprm((short)0x6425, brc, null, sprmList);
    }
    if (!newPAP.getBrcBottom().equals(oldPAP.getBrcBottom()))
    {
      int brc = newPAP.getBrcBottom().toInt();
      size += SprmUtils.addSprm((short)0x6426, brc, null, sprmList);
    }
    if (!newPAP.getBrcRight().equals(oldPAP.getBrcRight()))
    {
      int brc = newPAP.getBrcRight().toInt();
      size += SprmUtils.addSprm((short)0x6427, brc, null, sprmList);
    }
    if (newPAP.getBrcBar().equals(oldPAP.getBrcBar()))
    {
      int brc = newPAP.getBrcBar().toInt();
      size += SprmUtils.addSprm((short)0x6428, brc, null, sprmList);
    }
    if (newPAP.getDxaFromText() != oldPAP.getDxaFromText())
    {
      size += SprmUtils.addSprm((short)0x842F, newPAP.getDxaFromText(), null, sprmList);
    }
    if (newPAP.getDyaFromText() != oldPAP.getDyaFromText())
    {
      size += SprmUtils.addSprm((short)0x842E, newPAP.getDyaFromText(), null, sprmList);
    }
    if (newPAP.getDyaHeight() != oldPAP.getDyaHeight() ||
        newPAP.getFMinHeight() != oldPAP.getFMinHeight())
    {
      short val = (short)newPAP.getDyaHeight();
      if (newPAP.getFMinHeight() > 0)
      {
        val |= 0x8000;
      }
      size += SprmUtils.addSprm((short)0x442B, val, null, sprmList);
    }
    if (newPAP.getShd() != null && !newPAP.getShd().equals(oldPAP.getShd()))
    {
      size += SprmUtils.addSprm((short)0x442D, newPAP.getShd().toShort(), null, sprmList);
    }
    if (newPAP.getDcs() != null && !newPAP.getDcs().equals(oldPAP.getDcs()))
    {
      size += SprmUtils.addSprm((short)0x442C, newPAP.getDcs().toShort(), null, sprmList);
    }
    if (newPAP.getLvl() != oldPAP.getLvl())
    {
      size += SprmUtils.addSprm((short)0x2640, newPAP.getLvl(), null, sprmList);
    }
    if (newPAP.getFNumRMIns() != oldPAP.getFNumRMIns())
    {
      size += SprmUtils.addSprm((short)0x2443, newPAP.getFNumRMIns(), null, sprmList);
    }
    if (newPAP.getFPropRMark() != oldPAP.getFPropRMark() ||
        newPAP.getIbstPropRMark() != oldPAP.getIbstPropRMark() ||
        !newPAP.getDttmPropRMark().equals(oldPAP.getDttmPropRMark()))
    {
      byte[] buf = new byte[7];
      buf[0] = (byte)newPAP.getFPropRMark();
      LittleEndian.putShort(buf, 1, (short)newPAP.getIbstPropRMark());
      newPAP.getDttmPropRMark().serialize(buf, 3);
      size += SprmUtils.addSprm((short)0xC63F, 0, buf, sprmList);
    }
    if (!Arrays.equals(newPAP.getNumrm(), oldPAP.getNumrm()))
    {
      size += SprmUtils.addSprm((short)0xC645, 0, newPAP.getNumrm(), sprmList);
    }

    if (newPAP.getTableLevel() != oldPAP.getTableLevel())
    {
      size += SprmUtils.addSprm((short)0x6649, newPAP.getTableLevel(), null, sprmList);
    }

    if (newPAP.getEmbeddedCellMark() != oldPAP.getEmbeddedCellMark())
    {
      size += SprmUtils.addSprm((short)0x244b, newPAP.getEmbeddedCellMark(), null, sprmList);
    }

    if (newPAP.getFTtpEmbedded() != oldPAP.getFTtpEmbedded())
    {
      size += SprmUtils.addSprm((short)0x244c, newPAP.getFTtpEmbedded(), null, sprmList);
    }

    return SprmUtils.getGrpprl(sprmList, size);

  }
}
