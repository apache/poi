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

import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.BorderCode;
import org.apache.poi.hwpf.usermodel.DateAndTime;
import org.apache.poi.hwpf.usermodel.LineSpacingDescriptor;
import org.apache.poi.util.LittleEndian;

import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Collections;
import java.util.ArrayList;

public class ParagraphSprmUncompressor
  extends SprmUncompressor
{
  public ParagraphSprmUncompressor()
  {
  }

  public static Paragraph uncompressPAP(Paragraph parent,
                                                  byte[] grpprl,
                                                  int offset)
  {
    Paragraph newProperties = null;
    try
    {
      newProperties = (Paragraph) parent.clone();
    }
    catch (CloneNotSupportedException cnse)
    {
      throw new RuntimeException("There is no way this exception should happen!!");
    }
    SprmIterator sprmIt = new SprmIterator(grpprl, offset);

    while (sprmIt.hasNext())
    {
      SprmOperation sprm = (SprmOperation)sprmIt.next();
      unCompressPAPOperation(newProperties, sprm);
    }

    return newProperties;
  }

  /**
   * Performs an operation on a ParagraphProperties object. Used to uncompress
   * from a papx.
   *
   * @param newPAP The ParagraphProperties object to perform the operation on.
   * @param operand The operand that defines the operation.
   * @param param The operation's parameter.
   * @param varParam The operation's variable length parameter.
   * @param grpprl The original papx.
   * @param offset The current offset in the papx.
   * @param spra A part of the sprm that defined this operation.
   */
  static void unCompressPAPOperation (Paragraph newPAP, SprmOperation sprm)
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
        newPAP.setJc ((byte) sprm.getOperand());
        break;
      case 0x4:
        newPAP.setFSideBySide ((byte) sprm.getOperand());
        break;
      case 0x5:
        newPAP.setFKeep ((byte) sprm.getOperand());
        break;
      case 0x6:
        newPAP.setFKeepFollow ((byte) sprm.getOperand());
        break;
      case 0x7:
        newPAP.setFPageBreakBefore ((byte) sprm.getOperand());
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
        newPAP.setIlfo (sprm.getOperand());
        break;
      case 0xc:
        newPAP.setFNoLnn ((byte) sprm.getOperand());
        break;
      case 0xd:
        /**handle tabs . variable parameter. seperate processing needed*/
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
        newPAP.setFInTable ((byte) sprm.getOperand());
        break;
      case 0x17:
        newPAP.setFTtp ((byte) sprm.getOperand());
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
        /** @todo handle paragraph postioning*/
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
      case 0x1c:

        //newPAP.setBrcTop1((short)param);
        break;
      case 0x1d:

        //newPAP.setBrcLeft1((short)param);
        break;
      case 0x1e:

        //newPAP.setBrcBottom1((short)param);
        break;
      case 0x1f:

        //newPAP.setBrcRight1((short)param);
        break;
      case 0x20:

        //newPAP.setBrcBetween1((short)param);
        break;
      case 0x21:

        //newPAP.setBrcBar1((byte)param);
        break;
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
        newPAP.setFNoAutoHyph ((byte) sprm.getOperand());
        break;
      case 0x2b:
        newPAP.setDyaHeight (sprm.getOperand());
        break;
      case 0x2c:
        newPAP.setDcs ((short) sprm.getOperand());
        break;
      case 0x2d:
        newPAP.setShd ((short) sprm.getOperand());
        break;
      case 0x2e:
        newPAP.setDyaFromText (sprm.getOperand());
        break;
      case 0x2f:
        newPAP.setDxaFromText (sprm.getOperand());
        break;
      case 0x30:
        newPAP.setFLocked ((byte) sprm.getOperand());
        break;
      case 0x31:
        newPAP.setFWidowControl ((byte) sprm.getOperand());
        break;
      case 0x32:

        //undocumented
        break;
      case 0x33:
        newPAP.setFKinsoku ((byte) sprm.getOperand());
        break;
      case 0x34:
        newPAP.setFWordWrap ((byte) sprm.getOperand());
        break;
      case 0x35:
        newPAP.setFOverflowPunct ((byte) sprm.getOperand());
        break;
      case 0x36:
        newPAP.setFTopLinePunct ((byte) sprm.getOperand());
        break;
      case 0x37:
        newPAP.setFAutoSpaceDE ((byte) sprm.getOperand());
        break;
      case 0x38:
        newPAP.setFAutoSpaceDN ((byte) sprm.getOperand());
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
      case 0x3e:
      {
        byte[] buf = new byte[sprm.size() - 3];
        System.arraycopy(buf, 0, sprm.getGrpprl(), sprm.getGrpprlOffset(),
                         buf.length);
        newPAP.setAnld(buf);
        break;
      }
      case 0x3f:
        //don't really need this. spec is confusing regarding this
        //sprm
        try
        {
          byte[] varParam = sprm.getGrpprl();
          int offset = sprm.getGrpprlOffset();
          newPAP.setFPropRMark ((int) varParam[offset]);
          newPAP.setIbstPropRMark ((int) LittleEndian.getShort (varParam, offset + 1));
          newPAP.setDttmPropRMark (new DateAndTime(varParam, offset + 3));
        }
        catch (Exception e)
        {
          e.printStackTrace ();
        }
        break;
      case 0x40:

        //newPAP._lvl = param;
        if (newPAP.getIstd () >= 1 && newPAP.getIstd () <= 9)
        {
          newPAP.setIlvl ((byte) sprm.getOperand());
        }
        break;
      case 0x41:

        // undocumented
        break;
      case 0x43:

        //pap.fNumRMIns
        newPAP.setFNumRMIns ((byte) sprm.getOperand());
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
        newPAP.setFUsePgsuSettings ((byte) sprm.getOperand());
        break;
      case 0x48:
        newPAP.setFAdjustRight ((byte) sprm.getOperand());
        break;
      default:
        break;
    }
  }

  private static void handleTabs(Paragraph pap, SprmOperation sprm)
  {
    byte[] grpprl = sprm.getGrpprl();
    int offset = sprm.getGrpprlOffset();
    int delSize = grpprl[offset++];
    int[] tabPositions = pap.getRgdxaTab();
    byte[] tabDescriptors = pap.getRgtbd();

    HashMap tabMap = new HashMap();
    for (int x = 0; x < tabPositions.length; x++)
    {
      tabMap.put(new Integer(tabPositions[x]), new Byte(tabDescriptors[x]));
    }

    for (int x = 0; x < delSize; x++)
    {
      tabMap.remove(new Integer(LittleEndian.getInt(grpprl, offset)));
      offset += LittleEndian.INT_SIZE;;
    }

    int addSize = grpprl[offset++];
    for (int x = 0; x < addSize; x++)
    {
      Integer key = new Integer(LittleEndian.getInt(grpprl, offset));
      Byte val = new Byte(grpprl[(LittleEndian.INT_SIZE * (addSize - x)) + x]);
      tabMap.put(key, val);
      offset += LittleEndian.INT_SIZE;
    }

    tabPositions = new int[tabMap.size()];
    tabDescriptors = new byte[tabPositions.length];
    ArrayList list = new ArrayList();

    Iterator keyIT = tabMap.keySet().iterator();
    while (keyIT.hasNext())
    {
      list.add(keyIT.next());
    }
    Collections.sort(list);

    for (int x = 0; x < tabPositions.length; x++)
    {
      Integer key = ((Integer)list.get(x));
      tabPositions[x] = key.intValue();
      tabDescriptors[x] = ((Byte)tabMap.get(key)).byteValue();
    }

    pap.setRgdxaTab(tabPositions);
    pap.setRgtbd(tabDescriptors);
  }

//  private static void handleTabsAgain(ParagraphProperties pap, SprmOperation sprm)
//  {
//    byte[] grpprl = sprm.getGrpprl();
//    int offset = sprm.getGrpprlOffset();
//    int delSize = grpprl[offset++];
//    int[] tabPositions = pap.getRgdxaTab();
//    byte[] tabDescriptors = pap.getRgtbd();
//
//    HashMap tabMap = new HashMap();
//    for (int x = 0; x < tabPositions.length; x++)
//    {
//      tabMap.put(new Integer(tabPositions[x]), new Byte(tabDescriptors[x]));
//    }
//
//    for (int x = 0; x < delSize; x++)
//    {
//      tabMap.remove(new Integer(LittleEndian.getInt(grpprl, offset)));
//      offset += LittleEndian.INT_SIZE;;
//    }
//
//    int addSize = grpprl[offset++];
//    for (int x = 0; x < addSize; x++)
//    {
//      Integer key = new Integer(LittleEndian.getInt(grpprl, offset));
//      Byte val = new Byte(grpprl[(LittleEndian.INT_SIZE * (addSize - x)) + x]);
//      tabMap.put(key, val);
//      offset += LittleEndian.INT_SIZE;
//    }
//
//    tabPositions = new int[tabMap.size()];
//    tabDescriptors = new byte[tabPositions.length];
//    ArrayList list = new ArrayList();
//
//    Iterator keyIT = tabMap.keySet().iterator();
//    while (keyIT.hasNext())
//    {
//      list.add(keyIT.next());
//    }
//    Collections.sort(list);
//
//    for (int x = 0; x < tabPositions.length; x++)
//    {
//      Integer key = ((Integer)list.get(x));
//      tabPositions[x] = key.intValue();
//      tabDescriptors[x] = ((Byte)tabMap.get(key)).byteValue();
//    }
//
//    pap.setRgdxaTab(tabPositions);
//    pap.setRgtbd(tabDescriptors);
//  }

}
