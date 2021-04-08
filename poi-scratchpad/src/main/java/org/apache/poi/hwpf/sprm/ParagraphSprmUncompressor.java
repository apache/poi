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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hwpf.model.TabDescriptor;
import org.apache.poi.hwpf.model.types.TBDAbstractType;
import org.apache.poi.hwpf.usermodel.BorderCode;
import org.apache.poi.hwpf.usermodel.DateAndTime;
import org.apache.poi.hwpf.usermodel.DropCapSpecifier;
import org.apache.poi.hwpf.usermodel.LineSpacingDescriptor;
import org.apache.poi.hwpf.usermodel.ParagraphProperties;
import org.apache.poi.hwpf.usermodel.ShadingDescriptor;
import org.apache.poi.hwpf.usermodel.ShadingDescriptor80;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

import static org.apache.logging.log4j.util.Unbox.box;

@Internal
public final class ParagraphSprmUncompressor extends SprmUncompressor {

    private static final Logger LOG = LogManager.getLogger(ParagraphSprmUncompressor.class);

  public ParagraphSprmUncompressor()
  {
  }

  public static ParagraphProperties uncompressPAP(ParagraphProperties parent,
                                                  byte[] grpprl,
                                                  int offset)
  {
    ParagraphProperties newProperties = parent.copy();
    SprmIterator sprmIt = new SprmIterator(grpprl, offset);

    while (sprmIt.hasNext())
    {
      SprmOperation sprm = sprmIt.next();

      // PAPXs can contain table sprms if the paragraph marks the end of a
      // table row
      if (sprm.getType() == SprmOperation.TYPE_PAP)
      {
          try
          {
              unCompressPAPOperation( newProperties, sprm );
          }
          catch ( Exception exc )
          {
            LOG.atError().withThrowable(exc).log("Unable to apply SPRM operation '{}'", box(sprm.getOperation()));
          }
      }
    }

    return newProperties;
  }

  /**
   * Performs an operation on a ParagraphProperties object. Used to uncompress
   * from a papx.
   *
   * @param newPAP The ParagraphProperties object to perform the operation on.
   * @param sprm sn SPRM operation.
   */
  static void unCompressPAPOperation (ParagraphProperties newPAP, SprmOperation sprm)
  {
    switch (sprm.getOperation())
    {
      case 0:
        newPAP.setIstd (sprm.getOperand());
        break;
      case 0x1:

        // Used only for piece table grpprl's not for PAPX
//        int istdFirst = LittleEndian.getShort (varParam, 2);
//        int istdLast = LittleEndian.getShort (varParam, 4);
//        if ((newPAP.getIstd () > istdFirst) || (newPAP.getIstd () <= istdLast))
//        {
//          permuteIstd (newPAP, varParam, opSize);
//        }
        break;
      case 0x2:
        if (newPAP.getIstd () <= 9 || newPAP.getIstd () >= 1)
        {
          byte paramTmp = (byte) sprm.getOperand();
          newPAP.setIstd (newPAP.getIstd () + paramTmp);
          newPAP.setLvl ((byte) (newPAP.getLvl () + paramTmp));

          if (((paramTmp >> 7) & 0x01) == 1)
          {
            newPAP.setIstd (Math.max (newPAP.getIstd (), 1));
          }
          else
          {
            newPAP.setIstd (Math.min (newPAP.getIstd (), 9));
          }

        }
        break;
      case 0x3:
        // Physical justification of the paragraph
        newPAP.setJc ((byte) sprm.getOperand());
        break;
      case 0x4:
        newPAP.setFSideBySide (sprm.getOperand() != 0);
        break;
      case 0x5:
        newPAP.setFKeep (sprm.getOperand() != 0);
        break;
      case 0x6:
        newPAP.setFKeepFollow (sprm.getOperand() != 0);
        break;
      case 0x7:
        newPAP.setFPageBreakBefore (sprm.getOperand() != 0);
        break;
      case 0x8:
        newPAP.setBrcl ((byte) sprm.getOperand());
        break;
      case 0x9:
        newPAP.setBrcp ((byte) sprm.getOperand());
        break;
      case 0xa:
        newPAP.setIlvl ((byte) sprm.getOperand());
        break;
        case 0xb:
            /* sprmPIlfo -- 0x460B */
            newPAP.setIlfo( sprm.getOperandShortSigned() );
            break;
      case 0xc:
        newPAP.setFNoLnn (sprm.getOperand() != 0);
        break;
      case 0xd:
        // handle tabs . variable parameter. seperate processing needed
        handleTabs(newPAP, sprm);
        break;
      case 0xe:
        newPAP.setDxaRight (sprm.getOperand());
        break;
      case 0xf:
        newPAP.setDxaLeft (sprm.getOperand());
        break;
      case 0x10:

        // sprmPNest is only stored in grpprls linked to a piece table.
        newPAP.setDxaLeft (newPAP.getDxaLeft () + sprm.getOperand());
        newPAP.setDxaLeft (Math.max (0, newPAP.getDxaLeft ()));
        break;
      case 0x11:
        newPAP.setDxaLeft1 (sprm.getOperand());
        break;
      case 0x12:
        newPAP.setLspd(new LineSpacingDescriptor(sprm.getGrpprl(), sprm.getGrpprlOffset()));
        break;
      case 0x13:
        newPAP.setDyaBefore (sprm.getOperand());
        break;
      case 0x14:
        newPAP.setDyaAfter (sprm.getOperand());
        break;
      case 0x15:
        // fast saved only
        //applySprmPChgTabs (newPAP, varParam, opSize);
        break;
        case 0x16:
            // sprmPFInTable -- 0x2416
            newPAP.setFInTable( sprm.getOperand()  != 0);
            break;
      case 0x17:
        newPAP.setFTtp ( sprm.getOperand() != 0);
        break;
      case 0x18:
        newPAP.setDxaAbs (sprm.getOperand());
        break;
      case 0x19:
        newPAP.setDyaAbs (sprm.getOperand());
        break;
      case 0x1a:
        newPAP.setDxaWidth (sprm.getOperand());
        break;
      case 0x1b:
        byte param = (byte)sprm.getOperand();
        // TODO: handle paragraph postioning
        byte pcVert = (byte) ((param & 0x0c) >> 2);
        byte pcHorz = (byte) (param & 0x03);
        if (pcVert != 3)
        {
          newPAP.setPcVert (pcVert);
        }
        if (pcHorz != 3)
        {
          newPAP.setPcHorz (pcHorz);
        }
        break;

        // BrcXXX1 is older Version. Brc is used
        // case 0x1c:
        // newPAP.setBrcTop1((short)param);
        // break;
        // case 0x1d:
        // newPAP.setBrcLeft1((short)param);
        // break;
        // case 0x1e:
        // newPAP.setBrcBottom1((short)param);
        // break;
        // case 0x1f:
        // newPAP.setBrcRight1((short)param);
        // break;
        // case 0x20:
        // newPAP.setBrcBetween1((short)param);
        // break;
        // case 0x21:
        // newPAP.setBrcBar1((byte)param);
        // break;

      case 0x22:
        newPAP.setDxaFromText (sprm.getOperand());
        break;
      case 0x23:
        newPAP.setWr((byte)sprm.getOperand());
        break;
      case 0x24:
        newPAP.setBrcTop(new BorderCode(sprm.getGrpprl(), sprm.getGrpprlOffset()));
        break;
      case 0x25:
        newPAP.setBrcLeft(new BorderCode(sprm.getGrpprl(), sprm.getGrpprlOffset()));
        break;
      case 0x26:
        newPAP.setBrcBottom (new BorderCode(sprm.getGrpprl(), sprm.getGrpprlOffset()));
        break;
      case 0x27:
        newPAP.setBrcRight (new BorderCode(sprm.getGrpprl(), sprm.getGrpprlOffset()));
        break;
      case 0x28:
        newPAP.setBrcBetween (new BorderCode(sprm.getGrpprl(), sprm.getGrpprlOffset()));
        break;
      case 0x29:
        newPAP.setBrcBar (new BorderCode(sprm.getGrpprl(), sprm.getGrpprlOffset()));
        break;
      case 0x2a:
        newPAP.setFNoAutoHyph (sprm.getOperand() != 0);
        break;
      case 0x2b:
        newPAP.setDyaHeight (sprm.getOperand());
        break;
      case 0x2c:
        newPAP.setDcs (new DropCapSpecifier((short)sprm.getOperand()));
        break;
        case 0x2d:
            newPAP.setShd( new ShadingDescriptor80( (short) sprm.getOperand() )
                    .toShadingDescriptor() );
            break;
      case 0x2e:
        newPAP.setDyaFromText (sprm.getOperand());
        break;
      case 0x2f:
        newPAP.setDxaFromText (sprm.getOperand());
        break;
      case 0x30:
        newPAP.setFLocked (sprm.getOperand() != 0);
        break;
      case 0x31:
        newPAP.setFWidowControl (sprm.getOperand() != 0);
        break;
      case 0x33:
        newPAP.setFKinsoku (sprm.getOperand() != 0);
        break;
      case 0x34:
        newPAP.setFWordWrap (sprm.getOperand() != 0);
        break;
      case 0x35:
        newPAP.setFOverflowPunct (sprm.getOperand() != 0);
        break;
      case 0x36:
        newPAP.setFTopLinePunct (sprm.getOperand() != 0);
        break;
      case 0x37:
        newPAP.setFAutoSpaceDE (sprm.getOperand() != 0);
        break;
      case 0x38:
        newPAP.setFAutoSpaceDN (sprm.getOperand() != 0);
        break;
      case 0x39:
        newPAP.setWAlignFont (sprm.getOperand());
        break;
      case 0x3a:
        newPAP.setFontAlign ((short) sprm.getOperand());
        break;
      case 0x3b:
        //obsolete
        break;
      case 0x3e: {
        // TODO: REMOVEME
        byte[] buf = Arrays.copyOfRange(sprm.getGrpprl(), sprm.getGrpprlOffset(), sprm.getGrpprlOffset() + (sprm.size() - 3));
        newPAP.setAnld(buf);
        break;
      }
      case 0x3f:
        //don't really need this. spec is confusing regarding this
        //sprm
          byte[] varParam = sprm.getGrpprl();
          int offset = sprm.getGrpprlOffset();
          newPAP.setFPropRMark (varParam[offset]  != 0 );
          newPAP.setIbstPropRMark (LittleEndian.getShort (varParam, offset + 1));
          newPAP.setDttmPropRMark (new DateAndTime(varParam, offset + 3));
        break;
      case 0x40:
        // This condition commented out, as Word seems to set outline levels even for
        //  paragraph with other styles than Heading 1..9, even though specification
        //  does not say so. See bug 49820 for discussion.
        //if (newPAP.getIstd () < 1 && newPAP.getIstd () > 9)
        {
          newPAP.setLvl ((byte) sprm.getOperand());
        }
        break;
      case 0x41:
        // sprmPFBiDi
        newPAP.setFBiDi(sprm.getOperand() != 0);
        break;
      case 0x43:

        //pap.fNumRMIns
        newPAP.setFNumRMIns (sprm.getOperand() != 0);
        break;
      case 0x44:

        //undocumented
        break;
      case 0x45:
        if (sprm.getSizeCode() == 6)
        {
          byte[] buf = new byte[sprm.size() - 3];
          System.arraycopy(buf, 0, sprm.getGrpprl(), sprm.getGrpprlOffset(), buf.length);
          newPAP.setNumrm (buf);
        }
        else
        {
          /**@todo handle large PAPX from data stream*/
        }
        break;

      case 0x47:
        newPAP.setFUsePgsuSettings (sprm.getOperand() != 0);
        break;
      case 0x48:
        newPAP.setFAdjustRight (sprm.getOperand() != 0);
        break;
        case 0x49:
            // sprmPItap -- 0x6649
            newPAP.setItap( sprm.getOperand() );
            break;
        case 0x4a:
            // sprmPDtap -- 0x664a
            newPAP.setItap( (byte) ( newPAP.getItap() + sprm.getOperand() ) );
            break;
        case 0x4b:
            // sprmPFInnerTableCell -- 0x244b
            newPAP.setFInnerTableCell( sprm.getOperand()  != 0);
            break;
        case 0x4c:
            // sprmPFInnerTtp -- 0x244c
            newPAP.setFTtpEmbedded( sprm.getOperand()  != 0);
            break;
        case 0x4d:
            // sprmPShd -- 0xc64d
            ShadingDescriptor shadingDescriptor = new ShadingDescriptor(
                    sprm.getGrpprl(), 3 );
            newPAP.setShading( shadingDescriptor );
            break;
        case 0x5d:
            // sprmPDxaRight -- 0x845d
            newPAP.setDxaRight( sprm.getOperand() );
            break;
        case 0x5e:
            // sprmPDxaLeft -- 0x845e
            newPAP.setDxaLeft( sprm.getOperand() );
            break;
        case 0x60:
            // sprmPDxaLeft1 -- 0x8460
            newPAP.setDxaLeft1( sprm.getOperand() );
            break;
      case 0x61:
        // sprmPJc
        newPAP.setJustificationLogical((byte) sprm.getOperand());
        break;
      case 0x67:
          // sprmPRsid -- 0x6467
          newPAP.setRsid( sprm.getOperand() );
          break;
        default:
            LOG.atDebug().log("Unknown PAP sprm ignored: {}", sprm);
            break;
        }
  }

  private static void handleTabs(ParagraphProperties pap, SprmOperation sprm)
  {
    byte[] grpprl = sprm.getGrpprl();
    int offset = sprm.getGrpprlOffset();
    int delSize = grpprl[offset++];
    int[] tabPositions = pap.getRgdxaTab();
    TabDescriptor[] tabDescriptors = pap.getRgtbd();

    Map<Integer, TabDescriptor> tabMap = new HashMap<>();
    for (int x = 0; x < tabPositions.length; x++)
    {
      tabMap.put(tabPositions[x], tabDescriptors[x]);
    }

    for (int x = 0; x < delSize; x++)
    {
      tabMap.remove((int) LittleEndian.getShort(grpprl, offset));
      offset += LittleEndianConsts.SHORT_SIZE;
    }

    int addSize = grpprl[offset++];
    int start = offset;
    for (int x = 0; x < addSize; x++)
    {
      Integer key = (int) LittleEndian.getShort(grpprl, offset);
      TabDescriptor val = new TabDescriptor( grpprl, start + ((TBDAbstractType.getSize() * addSize) + x) );
      tabMap.put(key, val);
      offset += LittleEndianConsts.SHORT_SIZE;
    }

    tabPositions = new int[tabMap.size()];
    tabDescriptors = new TabDescriptor[tabPositions.length];

    List<Integer> list = new ArrayList<>(tabMap.keySet());
    Collections.sort(list);

    for (int x = 0; x < tabPositions.length; x++)
    {
      Integer key = list.get(x);
      tabPositions[x] = key;
      if (tabMap.containsKey( key ))
          tabDescriptors[x] = tabMap.get(key);
      else
          tabDescriptors[x] = new TabDescriptor();
    }

    pap.setRgdxaTab(tabPositions);
    pap.setRgtbd(tabDescriptors);
  }
}
