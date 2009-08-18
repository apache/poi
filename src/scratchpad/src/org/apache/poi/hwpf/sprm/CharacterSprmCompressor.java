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

import org.apache.poi.hwpf.usermodel.CharacterProperties;
import org.apache.poi.util.LittleEndian;

public final class CharacterSprmCompressor
{
  public CharacterSprmCompressor()
  {
  }
  public static byte[] compressCharacterProperty(CharacterProperties newCHP, CharacterProperties oldCHP)
  {
    ArrayList sprmList = new ArrayList();
    int size = 0;

    if (newCHP.isFRMarkDel() != oldCHP.isFRMarkDel())
    {
      int value = 0;
      if (newCHP.isFRMarkDel())
      {
        value = 0x01;
      }
      size += SprmUtils.addSprm((short)0x0800, value, null, sprmList);
    }
    if (newCHP.isFRMark() != oldCHP.isFRMark())
    {
      int value = 0;
      if (newCHP.isFRMark())
      {
        value = 0x01;
      }
      size += SprmUtils.addSprm((short)0x0801, value, null, sprmList);
    }
    if (newCHP.isFFldVanish() != oldCHP.isFFldVanish())
    {
      int value = 0;
      if (newCHP.isFFldVanish())
      {
        value = 0x01;
      }
      size += SprmUtils.addSprm((short)0x0802, value, null, sprmList);
    }
    if (newCHP.isFSpec() != oldCHP.isFSpec() || newCHP.getFcPic() != oldCHP.getFcPic())
    {
      size += SprmUtils.addSprm((short)0x6a03, newCHP.getFcPic(), null, sprmList);
    }
    if (newCHP.getIbstRMark() != oldCHP.getIbstRMark())
    {
       size += SprmUtils.addSprm((short)0x4804, newCHP.getIbstRMark(), null, sprmList);
    }
    if (!newCHP.getDttmRMark().equals(oldCHP.getDttmRMark()))
    {
      byte[] buf = new byte[4];
      newCHP.getDttmRMark().serialize(buf, 0);

      size += SprmUtils.addSprm((short)0x6805, LittleEndian.getInt(buf), null, sprmList);
    }
    if (newCHP.isFData() != oldCHP.isFData())
    {
      int value = 0;
      if (newCHP.isFData())
      {
        value = 0x01;
      }
      size += SprmUtils.addSprm((short)0x0806, value, null, sprmList);
    }
    if (newCHP.isFSpec() && newCHP.getFtcSym() != 0)
    {
       byte[] varParam = new byte[4];
       LittleEndian.putShort(varParam, 0, (short)newCHP.getFtcSym());
       LittleEndian.putShort(varParam, 2, (short)newCHP.getXchSym());

       size += SprmUtils.addSprm((short)0x6a09, 0, varParam, sprmList);
    }
    if (newCHP.isFOle2() != newCHP.isFOle2())
    {
      int value = 0;
      if (newCHP.isFOle2())
      {
        value = 0x01;
      }
      size += SprmUtils.addSprm((short)0x080a, value, null, sprmList);
    }
    if (newCHP.getIcoHighlight() != oldCHP.getIcoHighlight())
    {
      size += SprmUtils.addSprm((short)0x2a0c, newCHP.getIcoHighlight(), null, sprmList);
    }
    if (newCHP.getFcObj() != oldCHP.getFcObj())
    {
      size += SprmUtils.addSprm((short)0x680e, newCHP.getFcObj(), null, sprmList);
    }
    if (newCHP.getIstd() != oldCHP.getIstd())
    {
      size += SprmUtils.addSprm((short)0x4a30, newCHP.getIstd(), null, sprmList);
    }
    if (newCHP.isFBold() != oldCHP.isFBold())
    {
      int value = 0;
      if (newCHP.isFBold())
      {
        value = 0x01;
      }
      size += SprmUtils.addSprm((short)0x0835, value, null, sprmList);
    }
    if (newCHP.isFItalic() != oldCHP.isFItalic())
    {
      int value = 0;
      if (newCHP.isFItalic())
      {
        value = 0x01;
      }
      size += SprmUtils.addSprm((short)0x0836, value, null, sprmList);
    }
    if (newCHP.isFStrike() != oldCHP.isFStrike())
    {
      int value = 0;
      if (newCHP.isFStrike())
      {
        value = 0x01;
      }
      size += SprmUtils.addSprm((short)0x0837, value, null, sprmList);
    }
    if (newCHP.isFOutline() != oldCHP.isFOutline())
    {
      int value = 0;
      if (newCHP.isFOutline())
      {
        value = 0x01;
      }
      size += SprmUtils.addSprm((short)0x0838, value, null, sprmList);
    }
    if (newCHP.isFShadow() != oldCHP.isFShadow())
    {
      int value = 0;
      if (newCHP.isFShadow())
      {
        value = 0x01;
      }
      size += SprmUtils.addSprm((short)0x0839, value, null, sprmList);
    }
    if (newCHP.isFSmallCaps() != oldCHP.isFSmallCaps())
    {
      int value = 0;
      if (newCHP.isFSmallCaps())
      {
        value = 0x01;
      }
      size += SprmUtils.addSprm((short)0x083a, value, null, sprmList);
    }
    if (newCHP.isFCaps() != oldCHP.isFCaps())
    {
      int value = 0;
      if (newCHP.isFCaps())
      {
        value = 0x01;
      }
      size += SprmUtils.addSprm((short)0x083b, value, null, sprmList);
    }
    if (newCHP.isFVanish() != oldCHP.isFVanish())
    {
      int value = 0;
      if (newCHP.isFVanish())
      {
        value = 0x01;
      }
      size += SprmUtils.addSprm((short)0x083c, value, null, sprmList);
    }
    if (newCHP.getKul() != oldCHP.getKul())
    {
      size += SprmUtils.addSprm((short)0x2a3e, newCHP.getKul(), null, sprmList);
    }
    if (newCHP.getDxaSpace() != oldCHP.getDxaSpace())
    {
      size += SprmUtils.addSprm((short)0x8840, newCHP.getDxaSpace(), null, sprmList);
    }
    if (newCHP.getIco() != oldCHP.getIco())
    {
      size += SprmUtils.addSprm((short)0x2a42, newCHP.getIco(), null, sprmList);
    }
    if (newCHP.getHps() != oldCHP.getHps())
    {
      size += SprmUtils.addSprm((short)0x4a43, newCHP.getHps(), null, sprmList);
    }
    if (newCHP.getHpsPos() != oldCHP.getHpsPos())
    {
      size += SprmUtils.addSprm((short)0x4845, newCHP.getHpsPos(), null, sprmList);
    }
    if (newCHP.getHpsKern() != oldCHP.getHpsKern())
    {
      size += SprmUtils.addSprm((short)0x484b, newCHP.getHpsKern(), null, sprmList);
    }
    if (newCHP.getYsr() != oldCHP.getYsr())
    {
      size += SprmUtils.addSprm((short)0x484e, newCHP.getYsr(), null, sprmList);
    }
    if (newCHP.getFtcAscii() != oldCHP.getFtcAscii())
    {
      size += SprmUtils.addSprm((short)0x4a4f, newCHP.getFtcAscii(), null, sprmList);
    }
    if (newCHP.getFtcFE() != oldCHP.getFtcFE())
    {
      size += SprmUtils.addSprm((short)0x4a50, newCHP.getFtcFE(), null, sprmList);
    }
    if (newCHP.getFtcOther() != oldCHP.getFtcOther())
    {
      size += SprmUtils.addSprm((short)0x4a51, newCHP.getFtcOther(), null, sprmList);
    }

    if (newCHP.isFDStrike() != oldCHP.isFDStrike())
    {
      int value = 0;
      if (newCHP.isFDStrike())
      {
        value = 0x01;
      }
      size += SprmUtils.addSprm((short)0x2a53, value, null, sprmList);
    }
    if (newCHP.isFImprint() != oldCHP.isFImprint())
    {
      int value = 0;
      if (newCHP.isFImprint())
      {
        value = 0x01;
      }
      size += SprmUtils.addSprm((short)0x0854, value, null, sprmList);
    }
    if (newCHP.isFSpec() != oldCHP.isFSpec())
    {
      int value = 0;
      if (newCHP.isFSpec())
      {
        value = 0x01;
      }
      size += SprmUtils.addSprm((short)0x0855, value, null, sprmList);
    }
    if (newCHP.isFObj() != oldCHP.isFObj())
    {
      int value = 0;
      if (newCHP.isFObj())
      {
        value = 0x01;
      }
      size += SprmUtils.addSprm((short)0x0856, value, null, sprmList);
    }
    if (newCHP.isFEmboss() != oldCHP.isFEmboss())
    {
      int value = 0;
      if (newCHP.isFEmboss())
      {
        value = 0x01;
      }
      size += SprmUtils.addSprm((short)0x0858, value, null, sprmList);
    }
    if (newCHP.getSfxtText() != oldCHP.getSfxtText())
    {
      size += SprmUtils.addSprm((short)0x2859, newCHP.getSfxtText(), null, sprmList);
    }
    if (newCHP.getIco24() != oldCHP.getIco24())
    {
      if(newCHP.getIco24() != -1) // don't add a sprm if we're looking at an ico = Auto
        size += SprmUtils.addSprm((short)0x6870, newCHP.getIco24(), null, sprmList);
    }

    return SprmUtils.getGrpprl(sprmList, size);
  }



}
