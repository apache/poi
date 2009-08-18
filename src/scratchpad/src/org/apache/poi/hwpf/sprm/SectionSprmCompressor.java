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

import org.apache.poi.hwpf.usermodel.SectionProperties;
import org.apache.poi.util.LittleEndian;


public final class SectionSprmCompressor
{
  private final static SectionProperties DEFAULT_SEP = new SectionProperties();
  public SectionSprmCompressor()
  {
  }
  public static byte[] compressSectionProperty(SectionProperties newSEP)
  {
    int size = 0;
    ArrayList sprmList = new ArrayList();

    if (newSEP.getCnsPgn() != DEFAULT_SEP.getCnsPgn())
    {
      size += SprmUtils.addSprm((short)0x3000, newSEP.getCnsPgn(), null, sprmList);
    }
    if (newSEP.getIHeadingPgn() != DEFAULT_SEP.getIHeadingPgn())
    {
      size += SprmUtils.addSprm((short)0x3001, newSEP.getIHeadingPgn(), null, sprmList);
    }
    if (!Arrays.equals(newSEP.getOlstAnm(), DEFAULT_SEP.getOlstAnm()))
    {
      size += SprmUtils.addSprm((short)0xD202, 0, newSEP.getOlstAnm(), sprmList);
    }
    if (newSEP.getFEvenlySpaced() != DEFAULT_SEP.getFEvenlySpaced())
    {
      size += SprmUtils.addSprm((short)0x3005, newSEP.getFEvenlySpaced() ? 1 : 0, null, sprmList);
    }
    if (newSEP.getFUnlocked() != DEFAULT_SEP.getFUnlocked())
    {
      size += SprmUtils.addSprm((short)0x3006, newSEP.getFUnlocked() ? 1 :0, null, sprmList);
    }
    if (newSEP.getDmBinFirst() != DEFAULT_SEP.getDmBinFirst())
    {
      size += SprmUtils.addSprm((short)0x5007, newSEP.getDmBinFirst(), null, sprmList);
    }
    if (newSEP.getDmBinOther() != DEFAULT_SEP.getDmBinOther())
    {
      size += SprmUtils.addSprm((short)0x5008, newSEP.getDmBinOther(), null, sprmList);
    }
    if (newSEP.getBkc() != DEFAULT_SEP.getBkc())
    {
      size += SprmUtils.addSprm((short)0x3009, newSEP.getBkc(), null, sprmList);
    }
    if (newSEP.getFTitlePage() != DEFAULT_SEP.getFTitlePage())
    {
      size += SprmUtils.addSprm((short)0x300A, newSEP.getFTitlePage() ? 1 : 0, null, sprmList);
    }
    if (newSEP.getCcolM1() != DEFAULT_SEP.getCcolM1())
    {
      size += SprmUtils.addSprm((short)0x500B, newSEP.getCcolM1(), null, sprmList);
    }
    if (newSEP.getDxaColumns() != DEFAULT_SEP.getDxaColumns())
    {
      size += SprmUtils.addSprm((short)0x900C, newSEP.getDxaColumns(), null, sprmList);
    }
    if (newSEP.getFAutoPgn() != DEFAULT_SEP.getFAutoPgn())
    {
      size += SprmUtils.addSprm((short)0x300D, newSEP.getFAutoPgn() ? 1 : 0, null, sprmList);
    }
    if (newSEP.getNfcPgn() != DEFAULT_SEP.getNfcPgn())
    {
      size += SprmUtils.addSprm((short)0x300E, newSEP.getNfcPgn(), null, sprmList);
    }
    if (newSEP.getDyaPgn() != DEFAULT_SEP.getDyaPgn())
    {
      size += SprmUtils.addSprm((short)0xB00F, newSEP.getDyaPgn(), null, sprmList);
    }
    if (newSEP.getDxaPgn() != DEFAULT_SEP.getDxaPgn())
    {
      size += SprmUtils.addSprm((short)0xB010, newSEP.getDxaPgn(), null, sprmList);
    }
    if (newSEP.getFPgnRestart() != DEFAULT_SEP.getFPgnRestart())
    {
      size += SprmUtils.addSprm((short)0x3011, newSEP.getFPgnRestart() ? 1 : 0, null, sprmList);
    }
    if (newSEP.getFEndNote() != DEFAULT_SEP.getFEndNote())
    {
      size += SprmUtils.addSprm((short)0x3012, newSEP.getFEndNote() ? 1 : 0, null, sprmList);
    }
    if (newSEP.getLnc() != DEFAULT_SEP.getLnc())
    {
      size += SprmUtils.addSprm((short)0x3013, newSEP.getLnc(), null, sprmList);
    }
    if (newSEP.getGrpfIhdt() != DEFAULT_SEP.getGrpfIhdt())
    {
      size += SprmUtils.addSprm((short)0x3014, newSEP.getGrpfIhdt(), null, sprmList);
    }
    if (newSEP.getNLnnMod() != DEFAULT_SEP.getNLnnMod())
    {
      size += SprmUtils.addSprm((short)0x5015, newSEP.getNLnnMod(), null, sprmList);
    }
    if (newSEP.getDxaLnn() != DEFAULT_SEP.getDxaLnn())
    {
      size += SprmUtils.addSprm((short)0x9016, newSEP.getDxaLnn(), null, sprmList);
    }
    if (newSEP.getDyaHdrTop() != DEFAULT_SEP.getDyaHdrTop())
    {
      size += SprmUtils.addSprm((short)0xB017, newSEP.getDyaHdrTop(), null, sprmList);
    }
    if (newSEP.getDyaHdrBottom() != DEFAULT_SEP.getDyaHdrBottom())
    {
      size += SprmUtils.addSprm((short)0xB018, newSEP.getDyaHdrBottom(), null, sprmList);
    }
    if (newSEP.getFLBetween() != DEFAULT_SEP.getFLBetween())
    {
      size += SprmUtils.addSprm((short)0x3019, newSEP.getFLBetween() ? 1 : 0, null, sprmList);
    }
    if (newSEP.getVjc() != DEFAULT_SEP.getVjc())
    {
      size += SprmUtils.addSprm((short)0x301A, newSEP.getVjc(), null, sprmList);
    }
    if (newSEP.getLnnMin() != DEFAULT_SEP.getLnnMin())
    {
      size += SprmUtils.addSprm((short)0x501B, newSEP.getLnnMin(), null, sprmList);
    }
    if (newSEP.getPgnStart() != DEFAULT_SEP.getPgnStart())
    {
      size += SprmUtils.addSprm((short)0x501C, newSEP.getPgnStart(), null, sprmList);
    }
    if (newSEP.getDmOrientPage() != DEFAULT_SEP.getDmOrientPage())
    {
      size += SprmUtils.addSprm((short)0x301D, newSEP.getDmOrientPage(), null, sprmList);
    }
    if (newSEP.getXaPage() != DEFAULT_SEP.getXaPage())
    {
      size += SprmUtils.addSprm((short)0xB01F, newSEP.getXaPage(), null, sprmList);
    }
    if (newSEP.getYaPage() != DEFAULT_SEP.getYaPage())
    {
      size += SprmUtils.addSprm((short)0xB020, newSEP.getYaPage(), null, sprmList);
    }
    if (newSEP.getDxaLeft() != DEFAULT_SEP.getDxaLeft())
    {
      size += SprmUtils.addSprm((short)0xB021, newSEP.getDxaLeft(), null, sprmList);
    }
    if (newSEP.getDxaRight() != DEFAULT_SEP.getDxaRight())
    {
      size += SprmUtils.addSprm((short)0xB022, newSEP.getDxaRight(), null, sprmList);
    }
    if (newSEP.getDyaTop() != DEFAULT_SEP.getDyaTop())
    {
      size += SprmUtils.addSprm((short)0x9023, newSEP.getDyaTop(), null, sprmList);
    }
    if (newSEP.getDyaBottom() != DEFAULT_SEP.getDyaBottom())
    {
      size += SprmUtils.addSprm((short)0x9024, newSEP.getDyaBottom(), null, sprmList);
    }
    if (newSEP.getDzaGutter() != DEFAULT_SEP.getDzaGutter())
    {
      size += SprmUtils.addSprm((short)0xB025, newSEP.getDzaGutter(), null, sprmList);
    }
    if (newSEP.getDmPaperReq() != DEFAULT_SEP.getDmPaperReq())
    {
      size += SprmUtils.addSprm((short)0x5026, newSEP.getDmPaperReq(), null, sprmList);
    }
    if (newSEP.getFPropMark() != DEFAULT_SEP.getFPropMark() ||
        newSEP.getIbstPropRMark() != DEFAULT_SEP.getIbstPropRMark() ||
        !newSEP.getDttmPropRMark().equals(DEFAULT_SEP.getDttmPropRMark()))
    {
      byte[] buf = new byte[7];
      buf[0] = (byte)(newSEP.getFPropMark() ? 1 : 0);
      int offset = LittleEndian.BYTE_SIZE;
      LittleEndian.putShort(buf, (short)newSEP.getIbstPropRMark());
      offset += LittleEndian.SHORT_SIZE;
      newSEP.getDttmPropRMark().serialize(buf, offset);
      size += SprmUtils.addSprm((short)0xD227, -1, buf, sprmList);
    }
    if (!newSEP.getBrcTop().equals( DEFAULT_SEP.getBrcTop()))
    {
      size += SprmUtils.addSprm((short)0x702B, newSEP.getBrcTop().toInt(), null, sprmList);
    }
    if (!newSEP.getBrcLeft().equals(DEFAULT_SEP.getBrcLeft()))
    {
      size += SprmUtils.addSprm((short)0x702C, newSEP.getBrcLeft().toInt(), null, sprmList);
    }
    if (!newSEP.getBrcBottom().equals(DEFAULT_SEP.getBrcBottom()))
    {
      size += SprmUtils.addSprm((short)0x702D, newSEP.getBrcBottom().toInt(), null, sprmList);
    }
    if (!newSEP.getBrcRight().equals(DEFAULT_SEP.getBrcRight()))
    {
      size += SprmUtils.addSprm((short)0x702E, newSEP.getBrcRight().toInt(), null, sprmList);
    }
    if (newSEP.getPgbProp() != DEFAULT_SEP.getPgbProp())
    {
      size += SprmUtils.addSprm((short)0x522F, newSEP.getPgbProp(), null, sprmList);
    }
    if (newSEP.getDxtCharSpace() != DEFAULT_SEP.getDxtCharSpace())
    {
      size += SprmUtils.addSprm((short)0x7030, newSEP.getDxtCharSpace(), null, sprmList);
    }
    if (newSEP.getDyaLinePitch() != DEFAULT_SEP.getDyaLinePitch())
    {
      size += SprmUtils.addSprm((short)0x9031, newSEP.getDyaLinePitch(), null, sprmList);
    }
    if (newSEP.getClm() != DEFAULT_SEP.getClm())
    {
      size += SprmUtils.addSprm((short)0x5032, newSEP.getClm(), null, sprmList);
    }
    if (newSEP.getWTextFlow() != DEFAULT_SEP.getWTextFlow())
    {
      size += SprmUtils.addSprm((short)0x5033, newSEP.getWTextFlow(), null, sprmList);
    }

    return SprmUtils.getGrpprl(sprmList, size);
  }
}

