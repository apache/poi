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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.hwpf.usermodel.ParagraphProperties;
import org.apache.poi.util.LittleEndian;

public final class ParagraphSprmCompressor
{
  public ParagraphSprmCompressor()
  {
  }

  public static byte[] compressParagraphProperty(ParagraphProperties newPAP,
                                                 ParagraphProperties oldPAP)
  {
    // page numbers links to Word97-2007BinaryFileFormat(doc)Specification.pdf, accessible from microsoft.com 

    List sprmList = new ArrayList();
    int size = 0;

    // Page 50 of public specification begins
    if (newPAP.getIstd() != oldPAP.getIstd())
    {
      // sprmPIstd 
      size += SprmUtils.addSprm((short)0x4600, newPAP.getIstd(), null, sprmList);
    }
    if (newPAP.getJc() != oldPAP.getJc())
    {
      // sprmPJc80 
      size += SprmUtils.addSprm((short)0x2403, newPAP.getJc(), null, sprmList);
    }
    if (newPAP.getFSideBySide() != oldPAP.getFSideBySide())
    {
      // sprmPFSideBySide 
      size += SprmUtils.addSprm((short)0x2404, newPAP.getFSideBySide(), null, sprmList);
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
    if (newPAP.getItbdMac() != oldPAP.getItbdMac() ||
            !Arrays.equals(newPAP.getRgdxaTab(), oldPAP.getRgdxaTab()) ||
            !Arrays.equals(newPAP.getRgtbd(), oldPAP.getRgtbd()))
        {
          /** @todo revisit this */
//          byte[] oldTabArray = oldPAP.getRgdxaTab();
//          byte[] newTabArray = newPAP.getRgdxaTab();
//          byte[] newTabDescriptors = newPAP.getRgtbd();
//          byte[] varParam = new byte[2 + oldTabArray.length + newTabArray.length +
//                                     newTabDescriptors.length];
//          varParam[0] = (byte)(oldTabArray.length/2);
//          int offset = 1;
//          System.arraycopy(oldTabArray, 0, varParam, offset, oldTabArray.length);
//          offset += oldTabArray.length;
//          varParam[offset] = (byte)(newTabArray.length/2);
//          offset += 1;
//          System.arraycopy(newTabArray, 0, varParam, offset, newTabArray.length);
//          offset += newTabArray.length;
//          System.arraycopy(newTabDescriptors, 0, varParam, offset, newTabDescriptors.length);
    //
//          size += SprmUtils.addSprm((short)0xC60D, 0, varParam, sprmList);
    }
    if (newPAP.getDxaLeft() != oldPAP.getDxaLeft())
    {
      // sprmPDxaLeft80 
      size += SprmUtils.addSprm((short)0x840F, newPAP.getDxaLeft(), null, sprmList);
    }

    // Page 51 of public specification begins
    if (newPAP.getDxaLeft1() != oldPAP.getDxaLeft1())
    {
      // sprmPDxaLeft180 
      size += SprmUtils.addSprm((short)0x8411, newPAP.getDxaLeft1(), null, sprmList);
    }
    if (newPAP.getDxaRight() != oldPAP.getDxaRight())
    {
      // sprmPDxaRight80  
      size += SprmUtils.addSprm((short)0x840E, newPAP.getDxaRight(), null, sprmList);
    }
    if (newPAP.getDxcLeft() != oldPAP.getDxcLeft())
    {
      // sprmPDxcLeft
      size += SprmUtils.addSprm((short)0x4456, newPAP.getDxcLeft(), null, sprmList);
    }
    if (newPAP.getDxcLeft1() != oldPAP.getDxcLeft1())
    {
      // sprmPDxcLeft1
      size += SprmUtils.addSprm((short)0x4457, newPAP.getDxcLeft1(), null, sprmList);
    }
    if (newPAP.getDxcRight() != oldPAP.getDxcRight())
    {
      // sprmPDxcRight
      size += SprmUtils.addSprm((short)0x4455, newPAP.getDxcRight(), null, sprmList);
    }
    if (!newPAP.getLspd().equals(oldPAP.getLspd()))
    {
      // sprmPDyaLine
      byte[] buf = new byte[4];
      newPAP.getLspd().serialize(buf, 0);

      size += SprmUtils.addSprm((short)0x6412, LittleEndian.getInt(buf), null, sprmList);
    }
    if (newPAP.getDyaBefore() != oldPAP.getDyaBefore())
    {
      // sprmPDyaBefore
      size += SprmUtils.addSprm((short)0xA413, newPAP.getDyaBefore(), null, sprmList);
    }
    if (newPAP.getDyaAfter() != oldPAP.getDyaAfter())
    {
      // sprmPDyaAfter
      size += SprmUtils.addSprm((short)0xA414, newPAP.getDyaAfter(), null, sprmList);
    }
    if (newPAP.getFDyaBeforeAuto() != oldPAP.getFDyaBeforeAuto())
    {
      // sprmPFDyaBeforeAuto
      size += SprmUtils.addSprm((short)0x245B, newPAP.getFDyaBeforeAuto(), null, sprmList);
    }
    if (newPAP.getFDyaAfterAuto() != oldPAP.getFDyaAfterAuto())
    {
      // sprmPFDyaAfterAuto
      size += SprmUtils.addSprm((short)0x245C, newPAP.getFDyaAfterAuto(), null, sprmList);
    }
    if (newPAP.getFInTable() != oldPAP.getFInTable())
    {
      // sprmPFInTable
      size += SprmUtils.addSprm((short)0x2416, newPAP.getFInTable(), null, sprmList);
    }
    if (newPAP.getFTtp() != oldPAP.getFTtp())
    {
      // sprmPFTtp
      size += SprmUtils.addSprm((short)0x2417, newPAP.getFTtp(), null, sprmList);
    }
    if (newPAP.getDxaAbs() != oldPAP.getDxaAbs())
    {
      // sprmPDxaAbs
      size += SprmUtils.addSprm((short)0x8418, newPAP.getDxaAbs(), null, sprmList);
    }
    if (newPAP.getDyaAbs() != oldPAP.getDyaAbs())
    {
      // sprmPDyaAbs
      size += SprmUtils.addSprm((short)0x8419, newPAP.getDyaAbs(), null, sprmList);
    }
    if (newPAP.getDxaWidth() != oldPAP.getDxaWidth())
    {
      // sprmPDxaWidth
      size += SprmUtils.addSprm((short)0x841A, newPAP.getDxaWidth(), null, sprmList);
    }
    
    // Page 52 of public specification begins
    if (newPAP.getWr() != oldPAP.getWr())
    {
      size += SprmUtils.addSprm((short)0x2423, newPAP.getWr(), null, sprmList);
    }

    if (newPAP.getBrcBar().equals(oldPAP.getBrcBar()))
    {
      // XXX: sprm code 0x6428 is sprmPBrcBetween80, but accessed property linked to sprmPBrcBar80 (0x6629)
      int brc = newPAP.getBrcBar().toInt();
      size += SprmUtils.addSprm((short)0x6428, brc, null, sprmList);
    }
    if (!newPAP.getBrcBottom().equals(oldPAP.getBrcBottom()))
    {
      // sprmPBrcBottom80  
      int brc = newPAP.getBrcBottom().toInt();
      size += SprmUtils.addSprm((short)0x6426, brc, null, sprmList);
    }
    if (!newPAP.getBrcLeft().equals(oldPAP.getBrcLeft()))
    {
      // sprmPBrcLeft80  
      int brc = newPAP.getBrcLeft().toInt();
      size += SprmUtils.addSprm((short)0x6425, brc, null, sprmList);
    }

    // Page 53 of public specification begins
    if (!newPAP.getBrcRight().equals(oldPAP.getBrcRight()))
    {
      // sprmPBrcRight80
      int brc = newPAP.getBrcRight().toInt();
      size += SprmUtils.addSprm((short)0x6427, brc, null, sprmList);
    }
    if (!newPAP.getBrcTop().equals(oldPAP.getBrcTop()))
    {
      // sprmPBrcTop80 
      int brc = newPAP.getBrcTop().toInt();
      size += SprmUtils.addSprm((short)0x6424, brc, null, sprmList);
    }
    if (newPAP.getFNoAutoHyph() != oldPAP.getFNoAutoHyph())
    {
      size += SprmUtils.addSprm((short)0x242A, newPAP.getFNoAutoHyph(), null, sprmList);
    }
    if (newPAP.getDyaHeight() != oldPAP.getDyaHeight() ||
            newPAP.getFMinHeight() != oldPAP.getFMinHeight())
    {
      // sprmPWHeightAbs
      short val = (short)newPAP.getDyaHeight();
      if (newPAP.getFMinHeight() > 0)
      {
        val |= 0x8000;
      }
      size += SprmUtils.addSprm((short)0x442B, val, null, sprmList);
    }
    if (newPAP.getDcs() != null && !newPAP.getDcs().equals(oldPAP.getDcs()))
    {
      // sprmPDcs 
      size += SprmUtils.addSprm((short)0x442C, newPAP.getDcs().toShort(), null, sprmList);
    }
    if (newPAP.getShd() != null && !newPAP.getShd().equals(oldPAP.getShd()))
    {
      // sprmPShd80 
      size += SprmUtils.addSprm((short)0x442D, newPAP.getShd().toShort(), null, sprmList);
    }
    if (newPAP.getDyaFromText() != oldPAP.getDyaFromText())
    {
      // sprmPDyaFromText
      size += SprmUtils.addSprm((short)0x842E, newPAP.getDyaFromText(), null, sprmList);
    }
    if (newPAP.getDxaFromText() != oldPAP.getDxaFromText())
    {
      // sprmPDxaFromText
      size += SprmUtils.addSprm((short)0x842F, newPAP.getDxaFromText(), null, sprmList);
    }
    if (newPAP.getFLocked() != oldPAP.getFLocked())
    {
      // sprmPFLocked
      size += SprmUtils.addSprm((short)0x2430, newPAP.getFLocked(), null, sprmList);
    }
    if (newPAP.getFWidowControl() != oldPAP.getFWidowControl())
    {
      // sprmPFWidowControl
      size += SprmUtils.addSprm((short)0x2431, newPAP.getFWidowControl(), null, sprmList);
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

    // Page 54 of public specification begins
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
      // sprmPAnld80 
      size += SprmUtils.addSprm((short)0xC63E, 0, newPAP.getAnld(), sprmList);
    }
    if (newPAP.getFPropRMark() != oldPAP.getFPropRMark() ||
            newPAP.getIbstPropRMark() != oldPAP.getIbstPropRMark() ||
            !newPAP.getDttmPropRMark().equals(oldPAP.getDttmPropRMark()))
    {
      // sprmPPropRMark
      byte[] buf = new byte[7];
      buf[0] = (byte)newPAP.getFPropRMark();
      LittleEndian.putShort(buf, 1, (short)newPAP.getIbstPropRMark());
      newPAP.getDttmPropRMark().serialize(buf, 3);
      size += SprmUtils.addSprm((short)0xC63F, 0, buf, sprmList);
    }
    if (newPAP.getLvl() != oldPAP.getLvl())
    {
      // sprmPOutLvl 
      size += SprmUtils.addSprm((short)0x2640, newPAP.getLvl(), null, sprmList);
    }
    if (newPAP.getFBiDi() != oldPAP.getFBiDi())
    {
      // sprmPFBiDi 
      size += SprmUtils.addSprm((short)0x2441, newPAP.getFBiDi(), null, sprmList);
    }
    if (newPAP.getFNumRMIns() != oldPAP.getFNumRMIns())
    {
      // sprmPFNumRMIns 
      size += SprmUtils.addSprm((short)0x2443, newPAP.getFNumRMIns(), null, sprmList);
    }
    if (!Arrays.equals(newPAP.getNumrm(), oldPAP.getNumrm()))
    {
      // sprmPNumRM
      size += SprmUtils.addSprm((short)0xC645, 0, newPAP.getNumrm(), sprmList);
    }
    if (newPAP.getFInnerTableCell() != oldPAP.getFInnerTableCell())
    {
      // sprmPFInnerTableCell
      size += SprmUtils.addSprm((short)0x244b, newPAP.getFInnerTableCell(), null, sprmList);
    }
    if (newPAP.getFTtpEmbedded() != oldPAP.getFTtpEmbedded())
    {
      // sprmPFInnerTtp 
      size += SprmUtils.addSprm((short)0x244c, newPAP.getFTtpEmbedded(), null, sprmList);
    }

    // Page 55 of public specification begins
    if (newPAP.getItap() != oldPAP.getItap())
    {
      // sprmPItap
      size += SprmUtils.addSprm((short)0x6649, newPAP.getItap(), null, sprmList);
    }

    return SprmUtils.getGrpprl(sprmList, size);

  }
}
