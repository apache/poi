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
import org.apache.poi.hwpf.usermodel.TableCellDescriptor;
import org.apache.poi.hwpf.usermodel.BorderCode;

import org.apache.poi.util.LittleEndian;

public final class TableSprmUncompressor
  extends SprmUncompressor
{
  public TableSprmUncompressor()
  {
  }

  public static TableProperties uncompressTAP(byte[] grpprl,
                                                  int offset)
  {
    TableProperties newProperties = new TableProperties();

    SprmIterator sprmIt = new SprmIterator(grpprl, offset);

    while (sprmIt.hasNext())
    {
      SprmOperation sprm = sprmIt.next();

      //TAPXs are actually PAPXs so we have to make sure we are only trying to
      //uncompress the right type of sprm.
      if (sprm.getType() == SprmOperation.TAP_TYPE)
      {
        unCompressTAPOperation(newProperties, sprm);
      }
    }

    return newProperties;
  }

  /**
   * Used to uncompress a table property. Performs an operation defined
   * by a sprm stored in a tapx.
   *
   * @param newTAP The TableProperties object to perform the operation on.
   * @param operand The operand that defines this operation.
   * @param param The parameter for this operation.
   * @param varParam Variable length parameter for this operation.
   */
  static void unCompressTAPOperation (TableProperties newTAP, SprmOperation sprm)
  {
    switch (sprm.getOperation())
    {
      case 0:
        newTAP.setJc ((short) sprm.getOperand());
        break;
      case 0x01:
      {
        short[] rgdxaCenter = newTAP.getRgdxaCenter ();
        short itcMac = newTAP.getItcMac ();
        int adjust = sprm.getOperand() - (rgdxaCenter[0] + newTAP.getDxaGapHalf ());
        for (int x = 0; x < itcMac; x++)
        {
          rgdxaCenter[x] += adjust;
        }
        break;
      }
      case 0x02:
      {
        short[] rgdxaCenter = newTAP.getRgdxaCenter ();
        if (rgdxaCenter != null)
        {
          int adjust = newTAP.getDxaGapHalf () - sprm.getOperand();
          rgdxaCenter[0] += adjust;
        }
        newTAP.setDxaGapHalf (sprm.getOperand());
        break;
      }
      case 0x03:
        newTAP.setFCantSplit (getFlag(sprm.getOperand()));
        break;
      case 0x04:
        newTAP.setFTableHeader (getFlag (sprm.getOperand()));
        break;
      case 0x05:
      {
        byte[] buf = sprm.getGrpprl();
        int offset = sprm.getGrpprlOffset();
        newTAP.setBrcTop(new BorderCode(buf, offset));
        offset += BorderCode.SIZE;
        newTAP.setBrcLeft(new BorderCode(buf, offset));
        offset += BorderCode.SIZE;
        newTAP.setBrcBottom(new BorderCode(buf, offset));
        offset += BorderCode.SIZE;
        newTAP.setBrcRight(new BorderCode(buf, offset));
        offset += BorderCode.SIZE;
        newTAP.setBrcHorizontal(new BorderCode(buf, offset));
        offset += BorderCode.SIZE;
        newTAP.setBrcVertical(new BorderCode(buf, offset));
        break;
      }
      case 0x06:

        //obsolete, used in word 1.x
        break;
      case 0x07:
        newTAP.setDyaRowHeight (sprm.getOperand());
        break;
      case 0x08:
      {
        byte[] grpprl = sprm.getGrpprl();
        int offset = sprm.getGrpprlOffset();
        short itcMac = grpprl[offset];
        short[] rgdxaCenter = new short[itcMac + 1];
        TableCellDescriptor[] rgtc = new TableCellDescriptor[itcMac];
        //I use varParam[0] and newTAP._itcMac interchangably
        newTAP.setItcMac (itcMac);
        newTAP.setRgdxaCenter (rgdxaCenter);
        newTAP.setRgtc (rgtc);

        // get the rgdxaCenters
        for (int x = 0; x < itcMac; x++)
        {
          rgdxaCenter[x] = LittleEndian.getShort (grpprl, offset + (1 + (x * 2)));
        }

        // only try to get the TC entries if they exist...
        int endOfSprm = offset+sprm.size()-6; // -2 bytes for sprm - 2 for size short - 2 to correct offsets being 0 based
        int startOfTCs = offset + (1 + (itcMac + 1) * 2);

        boolean hasTCs = startOfTCs < endOfSprm;

        for (int x = 0; x < itcMac; x++)
        {
          // Sometimes, the grpprl does not contain data at every offset. I have no idea why this happens.
          if(hasTCs && offset + (1 + ( (itcMac + 1) * 2) + (x * 20)) < grpprl.length)
            rgtc[x] = TableCellDescriptor.convertBytesToTC(grpprl,
               offset + (1 + ( (itcMac + 1) * 2) + (x * 20)));
          else
            rgtc[x] = new TableCellDescriptor();
        }

        rgdxaCenter[itcMac] = LittleEndian.getShort (grpprl, offset + (1 + (itcMac * 2)));
        break;
      }
      case 0x09:

        /** @todo handle cell shading*/
        break;
      case 0x0a:

        /** @todo handle word defined table styles*/
        break;
      case 0x20:
//      {
//        TableCellDescriptor[] rgtc = newTAP.getRgtc();
//
//        for (int x = varParam[0]; x < varParam[1]; x++)
//        {
//
//          if ((varParam[2] & 0x08) > 0)
//          {
//            short[] brcRight = rgtc[x].getBrcRight ();
//            brcRight[0] = LittleEndian.getShort (varParam, 6);
//            brcRight[1] = LittleEndian.getShort (varParam, 8);
//          }
//          else if ((varParam[2] & 0x04) > 0)
//          {
//            short[] brcBottom = rgtc[x].getBrcBottom ();
//            brcBottom[0] = LittleEndian.getShort (varParam, 6);
//            brcBottom[1] = LittleEndian.getShort (varParam, 8);
//          }
//          else if ((varParam[2] & 0x02) > 0)
//          {
//            short[] brcLeft = rgtc[x].getBrcLeft ();
//            brcLeft[0] = LittleEndian.getShort (varParam, 6);
//            brcLeft[1] = LittleEndian.getShort (varParam, 8);
//          }
//          else if ((varParam[2] & 0x01) > 0)
//          {
//            short[] brcTop = rgtc[x].getBrcTop ();
//            brcTop[0] = LittleEndian.getShort (varParam, 6);
//            brcTop[1] = LittleEndian.getShort (varParam, 8);
//          }
//        }
//        break;
//      }
        break;
      case 0x21:
      {
        int param = sprm.getOperand();
        int index = (param & 0xff000000) >> 24;
        int count = (param & 0x00ff0000) >> 16;
        int width = (param & 0x0000ffff);
        int itcMac = newTAP.getItcMac();

        short[] rgdxaCenter = new short[itcMac + count + 1];
        TableCellDescriptor[] rgtc = new TableCellDescriptor[itcMac + count];
        if (index >= itcMac)
        {
          index = itcMac;
          System.arraycopy(newTAP.getRgdxaCenter(), 0, rgdxaCenter, 0,
                           itcMac + 1);
          System.arraycopy(newTAP.getRgtc(), 0, rgtc, 0, itcMac);
        }
        else
        {
          //copy rgdxaCenter
          System.arraycopy(newTAP.getRgdxaCenter(), 0, rgdxaCenter, 0,
                           index + 1);
          System.arraycopy(newTAP.getRgdxaCenter(), index + 1, rgdxaCenter,
                           index + count, itcMac - (index));
          //copy rgtc
          System.arraycopy(newTAP.getRgtc(), 0, rgtc, 0, index);
          System.arraycopy(newTAP.getRgtc(), index, rgtc, index + count,
                           itcMac - index);
        }

        for (int x = index; x < index + count; x++)
        {
          rgtc[x] = new TableCellDescriptor();
          rgdxaCenter[x] = (short)(rgdxaCenter[x - 1] + width);
        }
        rgdxaCenter[index +
          count] = (short)(rgdxaCenter[(index + count) - 1] + width);
        break;
      }
      /**@todo handle table sprms from complex files*/
      case 0x22:
      case 0x23:
      case 0x24:
      case 0x25:
      case 0x26:
      case 0x27:
      case 0x28:
      case 0x29:
      case 0x2a:
      case 0x2b:
      case 0x2c:
        break;
      default:
        break;
    }
  }



}
