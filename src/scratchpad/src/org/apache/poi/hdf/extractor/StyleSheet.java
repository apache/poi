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

package org.apache.poi.hdf.extractor;

/**
 * Comment me
 *
 * @author Ryan Ackley
 */
public final class StyleSheet {

  private static final int NIL_STYLE = 4095;
  private static final int PAP_TYPE = 1;
  private static final int CHP_TYPE = 2;
  private static final int SEP_TYPE = 4;
  private static final int TAP_TYPE = 5;
  //Vector _styleDescriptions;
  StyleDescription _nilStyle = new StyleDescription();
  StyleDescription[] _styleDescriptions;

  public StyleSheet(byte[] styleSheet)
  {
      int stshiLength = Utils.convertBytesToShort(styleSheet, 0);
      int stdCount = Utils.convertBytesToShort(styleSheet, 2);
      int baseLength = Utils.convertBytesToShort(styleSheet, 4);
      int[] rgftc = new int[3];

      rgftc[0] = Utils.convertBytesToInt(styleSheet, 14);
      rgftc[1] = Utils.convertBytesToInt(styleSheet, 18);
      rgftc[2] = Utils.convertBytesToInt(styleSheet, 22);

      int offset = 0;
      _styleDescriptions = new StyleDescription[stdCount];
      for(int x = 0; x < stdCount; x++)
      {
          int stdOffset = (2 + stshiLength) + offset;
          int stdSize = Utils.convertBytesToShort(styleSheet, stdOffset);
          if(stdSize > 0)
          {
              byte[] std = new byte[stdSize];

              //get past the size
              stdOffset += 2;
              System.arraycopy(styleSheet, stdOffset, std, 0, stdSize);
              StyleDescription aStyle = new StyleDescription(std, baseLength, true);

              _styleDescriptions[x] = aStyle;
          }


          offset += stdSize + 2;

      }
      for(int x = 0; x < _styleDescriptions.length; x++)
      {
          if(_styleDescriptions[x] != null)
          {
              createPap(x);
              createChp(x);
          }
      }
  }
  private void createPap(int istd)
  {
      StyleDescription sd = _styleDescriptions[istd];
      PAP pap = sd.getPAP();
      byte[] papx = sd.getPAPX();
      int baseIndex = sd.getBaseStyle();
      if(pap == null && papx != null)
      {
          PAP parentPAP = _nilStyle.getPAP();
          if(baseIndex != NIL_STYLE)
          {

              parentPAP = _styleDescriptions[baseIndex].getPAP();
              if(parentPAP == null)
              {
                  createPap(baseIndex);
                  parentPAP = _styleDescriptions[baseIndex].getPAP();
              }

          }

          pap = (PAP)uncompressProperty(papx, parentPAP, this);
          sd.setPAP(pap);
      }
  }
  private void createChp(int istd)
  {
      StyleDescription sd = _styleDescriptions[istd];
      CHP chp = sd.getCHP();
      byte[] chpx = sd.getCHPX();
      int baseIndex = sd.getBaseStyle();
      if(chp == null && chpx != null)
      {
          CHP parentCHP = _nilStyle.getCHP();
          if(baseIndex != NIL_STYLE)
          {

              parentCHP = _styleDescriptions[baseIndex].getCHP();
              if(parentCHP == null)
              {
                  createChp(baseIndex);
                  parentCHP = _styleDescriptions[baseIndex].getCHP();
              }

          }

          chp = (CHP)uncompressProperty(chpx, parentCHP, this);
          sd.setCHP(chp);
      }
  }
  public StyleDescription getStyleDescription(int x)
  {
      return _styleDescriptions[x];
  }
  static void doCHPOperation(CHP oldCHP, CHP newCHP, int operand, int param,
                             byte[] varParam, byte[] grpprl, int offset,
                             StyleSheet styleSheet)
  {
      switch(operand)
      {
          case 0:
               newCHP._fRMarkDel = getFlag(param);
               break;
          case 0x1:
               newCHP._fRMark = getFlag(param);
               break;
          case 0x2:
               break;
          case 0x3:
               newCHP._fcPic = param;
               newCHP._fSpec = true;
               break;
          case 0x4:
               newCHP._ibstRMark = (short)param;
               break;
          case 0x5:
               newCHP._dttmRMark[0] = Utils.convertBytesToShort(grpprl, (offset - 4));
               newCHP._dttmRMark[1] = Utils.convertBytesToShort(grpprl, (offset - 2));
               break;
          case 0x6:
               newCHP._fData = getFlag(param);
               break;
          case 0x7:
               //don't care about this
               break;
          case 0x8:
               short chsDiff = (short)((param & 0xff0000) >>> 8);
               newCHP._fChsDiff = getFlag(chsDiff);
               newCHP._chse = (short)(param & 0xffff);
               break;
          case 0x9:
               newCHP._fSpec = true;
               newCHP._ftcSym = Utils.convertBytesToShort(varParam, 0);
               newCHP._xchSym = Utils.convertBytesToShort(varParam, 2);
               break;
          case 0xa:
               newCHP._fOle2 = getFlag(param);
               break;
          case 0xb:
               //?
               break;
          case 0xc:
               newCHP._icoHighlight = (byte)param;
               newCHP._highlighted = getFlag(param);
               break;
          case 0xd:
               break;
          case 0xe:
               newCHP._fcObj = param;
               break;
          case 0xf:
               break;
          case 0x10:
               //?
               break;
          case 0x11:
               break;
          case 0x12:
               break;
          case 0x13:
               break;
          case 0x14:
               break;
          case 0x15:
               break;
          case 0x16:
               break;
          case 0x17:
               break;
          case 0x18:
               break;
          case 0x19:
               break;
          case 0x1a:
               break;
          case 0x1b:
               break;
          case 0x1c:
               break;
          case 0x1d:
               break;
          case 0x1e:
               break;
          case 0x1f:
               break;
          case 0x20:
               break;
          case 0x21:
               break;
          case 0x22:
               break;
          case 0x23:
               break;
          case 0x24:
               break;
          case 0x25:
               break;
          case 0x26:
               break;
          case 0x27:
               break;
          case 0x28:
               break;
          case 0x29:
               break;
          case 0x2a:
               break;
          case 0x2b:
               break;
          case 0x2c:
               break;
          case 0x2d:
               break;
          case 0x2e:
               break;
          case 0x2f:
               break;
          case 0x30:
               newCHP._istd = param;
               break;
          case 0x31:
               //permutation vector for fast saves who cares!
               break;
          case 0x32:
               newCHP._bold = false;
               newCHP._italic = false;
               newCHP._fOutline = false;
               newCHP._fStrike = false;
               newCHP._fShadow = false;
               newCHP._fSmallCaps = false;
               newCHP._fCaps = false;
               newCHP._fVanish = false;
               newCHP._kul = 0;
               newCHP._ico = 0;
               break;
          case 0x33:
               newCHP.copy(oldCHP);
               return;
          case 0x34:
               break;
          case 0x35:
               newCHP._bold = getCHPFlag((byte)param, oldCHP._bold);
               break;
          case 0x36:
               newCHP._italic = getCHPFlag((byte)param, oldCHP._italic);
               break;
          case 0x37:
               newCHP._fStrike = getCHPFlag((byte)param, oldCHP._fStrike);
               break;
          case 0x38:
               newCHP._fOutline = getCHPFlag((byte)param, oldCHP._fOutline);
               break;
          case 0x39:
               newCHP._fShadow = getCHPFlag((byte)param, oldCHP._fShadow);
               break;
          case 0x3a:
               newCHP._fSmallCaps = getCHPFlag((byte)param, oldCHP._fSmallCaps);
               break;
          case 0x3b:
               newCHP._fCaps = getCHPFlag((byte)param, oldCHP._fCaps);
               break;
          case 0x3c:
               newCHP._fVanish = getCHPFlag((byte)param, oldCHP._fVanish);
               break;
          case 0x3d:
               newCHP._ftc = (short)param;
               break;
          case 0x3e:
               newCHP._kul = (byte)param;
               break;
          case 0x3f:
               int hps = param & 0xff;
               if(hps != 0)
               {
                  newCHP._hps = hps;
               }
               byte cInc = (byte)(((byte)(param & 0xfe00) >>> 4) >> 1);
               if(cInc != 0)
               {
                  newCHP._hps = Math.max(newCHP._hps + (cInc * 2), 2);
               }
               byte hpsPos = (byte)((param & 0xff0000) >>> 8);
               if(hpsPos != 0x80)
               {
                  newCHP._hpsPos = hpsPos;
               }
               boolean fAdjust = (param & 0x0100) > 0;
               if(fAdjust && hpsPos != 128 && hpsPos != 0 && oldCHP._hpsPos == 0)
               {
                  newCHP._hps = Math.max(newCHP._hps + (-2), 2);
               }
               if(fAdjust && hpsPos == 0 && oldCHP._hpsPos != 0)
               {
                  newCHP._hps = Math.max(newCHP._hps + 2, 2);
               }
               break;
          case 0x40:
               newCHP._dxaSpace = param;
               break;
          case 0x41:
               newCHP._lidDefault = (short)param;
               break;
          case 0x42:
               newCHP._ico = (byte)param;
               break;
          case 0x43:
               newCHP._hps = param;
               break;
          case 0x44:
               byte hpsLvl = (byte)param;
               newCHP._hps = Math.max(newCHP._hps + (hpsLvl * 2), 2);
               break;
          case 0x45:
               newCHP._hpsPos = (short)param;
               break;
          case 0x46:
               if(param != 0)
               {
                  if(oldCHP._hpsPos == 0)
                  {
                      newCHP._hps = Math.max(newCHP._hps + (-2), 2);
                  }
               }
               else
               {
                  if(oldCHP._hpsPos != 0)
                  {
                      newCHP._hps = Math.max(newCHP._hps + 2, 2);
                  }
               }
               break;
          case 0x47:
               CHP genCHP = new CHP();
               genCHP._ftc = 4;
               genCHP = (CHP)uncompressProperty(varParam, genCHP, styleSheet);
               CHP styleCHP = styleSheet.getStyleDescription(oldCHP._baseIstd).getCHP();
               if(genCHP._bold == newCHP._bold)
               {
                  newCHP._bold = styleCHP._bold;
               }
               if(genCHP._italic == newCHP._italic)
               {
                  newCHP._italic = styleCHP._italic;
               }
               if(genCHP._fSmallCaps == newCHP._fSmallCaps)
               {
                  newCHP._fSmallCaps = styleCHP._fSmallCaps;
               }
               if(genCHP._fVanish == newCHP._fVanish)
               {
                  newCHP._fVanish = styleCHP._fVanish;
               }
               if(genCHP._fStrike == newCHP._fStrike)
               {
                  newCHP._fStrike = styleCHP._fStrike;
               }
               if(genCHP._fCaps == newCHP._fCaps)
               {
                  newCHP._fCaps = styleCHP._fCaps;
               }
               if(genCHP._ftcAscii == newCHP._ftcAscii)
               {
                  newCHP._ftcAscii = styleCHP._ftcAscii;
               }
               if(genCHP._ftcFE == newCHP._ftcFE)
               {
                  newCHP._ftcFE = styleCHP._ftcFE;
               }
               if(genCHP._ftcOther == newCHP._ftcOther)
               {
                  newCHP._ftcOther = styleCHP._ftcOther;
               }
               if(genCHP._hps == newCHP._hps)
               {
                  newCHP._hps = styleCHP._hps;
               }
               if(genCHP._hpsPos == newCHP._hpsPos)
               {
                  newCHP._hpsPos = styleCHP._hpsPos;
               }
               if(genCHP._kul == newCHP._kul)
               {
                  newCHP._kul = styleCHP._kul;
               }
               if(genCHP._dxaSpace == newCHP._dxaSpace)
               {
                  newCHP._dxaSpace = styleCHP._dxaSpace;
               }
               if(genCHP._ico == newCHP._ico)
               {
                  newCHP._ico = styleCHP._ico;
               }
               if(genCHP._lidDefault == newCHP._lidDefault)
               {
                  newCHP._lidDefault = styleCHP._lidDefault;
               }
               if(genCHP._lidFE == newCHP._lidFE)
               {
                  newCHP._lidFE = styleCHP._lidFE;
               }
               break;
          case 0x48:
               newCHP._iss = (byte)param;
               break;
          case 0x49:
               newCHP._hps = Utils.convertBytesToShort(varParam, 0);
               break;
          case 0x4a:
               int increment = Utils.convertBytesToShort(varParam, 0);
               newCHP._hps = Math.max(newCHP._hps + increment, 8);
               break;
          case 0x4b:
               newCHP._hpsKern = param;
               break;
          case 0x4c:
               doCHPOperation(oldCHP, newCHP, 0x47, param, varParam, grpprl, offset, styleSheet);
               break;
          case 0x4d:
               float percentage = param/100.0f;
               int add = (int)(percentage * newCHP._hps);
               newCHP._hps += add;
               break;
          case 0x4e:
               newCHP._ysr = (byte)param;
               break;
          case 0x4f:
               newCHP._ftcAscii = (short)param;
               break;
          case 0x50:
               newCHP._ftcFE = (short)param;
               break;
          case 0x51:
               newCHP._ftcOther = (short)param;
               break;
          case 0x52:
               break;
          case 0x53:
               newCHP._fDStrike = getFlag(param);
               break;
          case 0x54:
               newCHP._fImprint = getFlag(param);
               break;
          case 0x55:
               newCHP._fSpec = getFlag(param);
               break;
          case 0x56:
               newCHP._fObj = getFlag(param);
               break;
          case 0x57:
               newCHP._fPropMark = getFlag(varParam[0]);
               newCHP._ibstPropRMark = Utils.convertBytesToShort(varParam, 1);
               newCHP._dttmPropRMark = Utils.convertBytesToInt(varParam, 3);
               break;
          case 0x58:
               newCHP._fEmboss = getFlag(param);
               break;
          case 0x59:
               newCHP._sfxtText = (byte)param;
               break;
          case 0x5a:
               break;
          case 0x5b:
               break;
          case 0x5c:
               break;
          case 0x5d:
               break;
          case 0x5e:
               break;
          case 0x5f:
               break;
          case 0x60:
               break;
          case 0x61:
               break;
          case 0x62:
               newCHP._fDispFldRMark = getFlag(varParam[0]);
               newCHP._ibstDispFldRMark = Utils.convertBytesToShort(varParam, 1);
               newCHP._dttmDispFldRMark = Utils.convertBytesToInt(varParam, 3);
               System.arraycopy(varParam, 7, newCHP._xstDispFldRMark, 0, 32);
               break;
          case 0x63:
               newCHP._ibstRMarkDel = (short)param;
               break;
          case 0x64:
               newCHP._dttmRMarkDel[0] = Utils.convertBytesToShort(grpprl, offset - 4);
               newCHP._dttmRMarkDel[1] = Utils.convertBytesToShort(grpprl, offset - 2);
               break;
          case 0x65:
               newCHP._brc[0] = Utils.convertBytesToShort(grpprl, offset - 4);
               newCHP._brc[1] = Utils.convertBytesToShort(grpprl, offset - 2);
               break;
          case 0x66:
               newCHP._shd = (short)param;
               break;
          case 0x67:
               break;
          case 0x68:
               break;
          case 0x69:
               break;
          case 0x6a:
               break;
          case 0x6b:
               break;
          case 0x6c:
               break;
          case 0x6d:
               newCHP._lidDefault = (short)param;
               break;
          case 0x6e:
               newCHP._lidFE = (short)param;
               break;
          case 0x6f:
               newCHP._idctHint = (byte)param;
               break;
      }
  }

  static Object uncompressProperty(byte[] grpprl, Object parent, StyleSheet styleSheet)
  {
    return uncompressProperty(grpprl, parent, styleSheet, true);
  }


  static Object uncompressProperty(byte[] grpprl, Object parent, StyleSheet styleSheet, boolean doIstd)
  {
      Object newProperty = null;
      int offset = 0;
      int propertyType = PAP_TYPE;


      if(parent instanceof PAP)
      {
          try
          {
              newProperty = ((PAP)parent).clone();
          }
          catch(Exception e){}
          if(doIstd)
          {
            ((PAP)newProperty)._istd = Utils.convertBytesToShort(grpprl, 0);

            offset = 2;
          }
      }
      else if(parent instanceof CHP)
      {
          try
          {
              newProperty = ((CHP)parent).clone();
              ((CHP)newProperty)._baseIstd = ((CHP)parent)._istd;
          }
          catch(Exception e){}
          propertyType = CHP_TYPE;
      }
      else if(parent instanceof SEP)
      {
          newProperty = parent;
          propertyType = SEP_TYPE;
      }
      else if(parent instanceof TAP)
      {
          newProperty = parent;
          propertyType = TAP_TYPE;
          offset = 2;//because this is really just a papx
      }
      else
      {
          return null;
      }

      while(offset < grpprl.length)
      {
          short sprm = Utils.convertBytesToShort(grpprl, offset);
          offset += 2;

          byte spra = (byte)((sprm & 0xe000) >> 13);
          int opSize = 0;
          int param = 0;
          byte[] varParam = null;

          switch(spra)
          {
              case 0:
              case 1:
                   opSize = 1;
                   param = grpprl[offset];
                   break;
              case 2:
                   opSize = 2;
                   param = Utils.convertBytesToShort(grpprl, offset);
                   break;
              case 3:
                   opSize = 4;
                   param = Utils.convertBytesToInt(grpprl, offset);
                   break;
              case 4:
              case 5:
                   opSize = 2;
                   param = Utils.convertBytesToShort(grpprl, offset);
                   break;
              case 6://variable size

                   //there is one sprm that is a very special case
                   if(sprm != (short)0xd608)
                   {
                     opSize = Utils.convertUnsignedByteToInt(grpprl[offset]);
                     offset++;
                   }
                   else
                   {
                     opSize = Utils.convertBytesToShort(grpprl, offset) - 1;
                     offset += 2;
                   }
                   varParam = new byte[opSize];
                   System.arraycopy(grpprl, offset, varParam, 0, opSize);

                   break;
              case 7:
                   opSize = 3;
                   param = Utils.convertBytesToInt((byte)0, grpprl[offset + 2], grpprl[offset + 1], grpprl[offset]);
                   break;
              default:
                  throw new RuntimeException("unrecognized pap opcode");
          }

          offset += opSize;
          short operand = (short)(sprm & 0x1ff);
          byte type = (byte)((sprm & 0x1c00) >> 10);
          switch(propertyType)
          {
              case PAP_TYPE:
                   if(type == 1)//papx stores TAP sprms along with PAP sprms
                   {
                     doPAPOperation((PAP)newProperty, operand, param, varParam, grpprl,
                                    offset, spra);
                   }
                   break;
              case CHP_TYPE:

                   doCHPOperation((CHP)parent, (CHP)newProperty, operand, param, varParam,
                                  grpprl, offset, styleSheet);
                   break;
              case SEP_TYPE:

                   doSEPOperation((SEP)newProperty, operand, param, varParam);
                   break;
              case TAP_TYPE:
                   if(type == 5)
                   {
                     doTAPOperation((TAP)newProperty, operand, param, varParam);
                   }
                   break;
          }


      }
      return newProperty;

  }
  static void doPAPOperation(PAP newPAP, int operand, int param,
                             byte[] varParam, byte[] grpprl, int offset,
                             int spra)
  {
      switch(operand)
      {
          case 0:
               newPAP._istd = param;
               break;
          case 0x1:
               //permuteIstd(newPAP, varParam);
               break;
          case 0x2:
               if(newPAP._istd <=9 || newPAP._istd >=1)
               {
                  newPAP._istd += param;
                  if(param > 0)
                  {
                      newPAP._istd = Math.max(newPAP._istd, 9);
                  }
                  else
                  {
                      newPAP._istd = Math.min(newPAP._istd, 1);
                  }
               }
               break;
          case 0x3:
               newPAP._jc = (byte)param;
               break;
          case 0x4:
               newPAP._fSideBySide = (byte)param;
               break;
          case 0x5:
               newPAP._fKeep = (byte)param;
               break;
          case 0x6:
               newPAP._fKeepFollow = (byte)param;
               break;
          case 0x7:
               newPAP._fPageBreakBefore = (byte)param;
               break;
          case 0x8:
               newPAP._brcl = (byte)param;
               break;
          case 0x9:
               newPAP._brcp = (byte)param;
               break;
          case 0xa:
               newPAP._ilvl = (byte)param;
               break;
          case 0xb:
               newPAP._ilfo = param;
               break;
          case 0xc:
               newPAP._fNoLnn = (byte)param;
               break;
          case 0xd:
               /**@todo handle tabs*/
               break;
          case 0xe:
               newPAP._dxaRight = param;
               break;
          case 0xf:
               newPAP._dxaLeft = param;
               break;
          case 0x10:
               newPAP._dxaLeft += param;
               newPAP._dxaLeft = Math.max(0, newPAP._dxaLeft);
               break;
          case 0x11:
               newPAP._dxaLeft1 = param;
               break;
          case 0x12:
               newPAP._lspd[0] = Utils.convertBytesToShort(grpprl, offset - 4);
               newPAP._lspd[1] = Utils.convertBytesToShort(grpprl, offset - 2);
               break;
          case 0x13:
               newPAP._dyaBefore = param;
               break;
          case 0x14:
               newPAP._dyaAfter = param;
               break;
          case 0x15:
               /**@todo handle tabs*/
               break;
          case 0x16:
               newPAP._fInTable = (byte)param;
               break;
          case 0x17:
               newPAP._fTtp =(byte)param;
               break;
          case 0x18:
               newPAP._dxaAbs = param;
               break;
          case 0x19:
               newPAP._dyaAbs = param;
               break;
          case 0x1a:
               newPAP._dxaWidth = param;
               break;
          case 0x1b:
               /** @todo handle paragraph postioning*/
               /*byte pcVert = (param & 0x0c) >> 2;
               byte pcHorz = param & 0x03;
               if(pcVert != 3)
               {
                  newPAP._pcVert = pcVert;
               }
               if(pcHorz != 3)
               {
                  newPAP._pcHorz = pcHorz;
               }*/
               break;
          case 0x1c:
               newPAP._brcTop1 = (short)param;
               break;
          case 0x1d:
               newPAP._brcLeft1 = (short)param;
               break;
          case 0x1e:
               newPAP._brcBottom1 = (short)param;
               break;
          case 0x1f:
               newPAP._brcRight1 = (short)param;
               break;
          case 0x20:
               newPAP._brcBetween1 = (short)param;
               break;
          case 0x21:
               newPAP._brcBar1 = (byte)param;
               break;
          case 0x22:
               newPAP._dxaFromText = param;
               break;
          case 0x23:
               newPAP._wr = (byte)param;
               break;
          case 0x24:
               newPAP._brcTop[0] = Utils.convertBytesToShort(grpprl, offset - 4);
               newPAP._brcTop[1] = Utils.convertBytesToShort(grpprl, offset - 2);
               break;
          case 0x25:
               newPAP._brcLeft[0] = Utils.convertBytesToShort(grpprl, offset - 4);
               newPAP._brcLeft[1] = Utils.convertBytesToShort(grpprl, offset - 2);
               break;
          case 0x26:
               newPAP._brcBottom[0] = Utils.convertBytesToShort(grpprl, offset - 4);
               newPAP._brcBottom[1] = Utils.convertBytesToShort(grpprl, offset - 2);
               break;
          case 0x27:
               newPAP._brcRight[0] = Utils.convertBytesToShort(grpprl, offset - 4);
               newPAP._brcRight[1] = Utils.convertBytesToShort(grpprl, offset - 2);
               break;
          case 0x28:
               newPAP._brcBetween[0] = Utils.convertBytesToShort(grpprl, offset - 4);
               newPAP._brcBetween[1] = Utils.convertBytesToShort(grpprl, offset - 2);
               break;
          case 0x29:
               newPAP._brcBar[0] = Utils.convertBytesToShort(grpprl, offset - 4);
               newPAP._brcBar[1] = Utils.convertBytesToShort(grpprl, offset - 2);
               break;
          case 0x2a:
               newPAP._fNoAutoHyph = (byte)param;
               break;
          case 0x2b:
               newPAP._dyaHeight = param;
               break;
          case 0x2c:
               newPAP._dcs = param;
               break;
          case 0x2d:
               newPAP._shd = param;
               break;
          case 0x2e:
               newPAP._dyaFromText = param;
               break;
          case 0x2f:
               newPAP._dxaFromText = param;
               break;
          case 0x30:
               newPAP._fLocked = (byte)param;
               break;
          case 0x31:
               newPAP._fWindowControl = (byte)param;
               break;
          case 0x32:
               //undocumented
               break;
          case 0x33:
               newPAP._fKinsoku = (byte)param;
               break;
          case 0x34:
               newPAP._fWordWrap = (byte)param;
               break;
          case 0x35:
               newPAP._fOverflowPunct = (byte)param;
               break;
          case 0x36:
               newPAP._fTopLinePunct = (byte)param;
               break;
          case 0x37:
               newPAP._fAutoSpaceDE = (byte)param;
               break;
          case 0x38:
               newPAP._fAutoSpaceDN = (byte)param;
               break;
          case 0x39:
               newPAP._wAlignFont = param;
               break;
          case 0x3a:
               newPAP._fontAlign = (short)param;
               break;
          case 0x3b:
               //obsolete
               break;
          case 0x3e:
               newPAP._anld = varParam;
               break;
          case 0x3f:
               //don't really need this. spec is confusing regarding this
               //sprm
               break;
          case 0x40:
               //newPAP._lvl = param;
               break;
          case 0x41:
               //?
               break;
          case 0x43:
               //?
              break;
          case 0x44:
               //?
               break;
          case 0x45:
               if(spra == 6)
               {
                  newPAP._numrm = varParam;
               }
               else
               {
                  /**@todo handle large PAPX from data stream*/
               }
               break;

          case 0x47:
               newPAP._fUsePgsuSettings = (byte)param;
               break;
          case 0x48:
               newPAP._fAdjustRight = (byte)param;
               break;
          default:
               break;
      }
  }
  static void doTAPOperation(TAP newTAP, int operand, int param, byte[] varParam)
  {
      switch(operand)
      {
          case 0:
               newTAP._jc = (short)param;
               break;
          case 0x01:
          {
               int adjust = param - (newTAP._rgdxaCenter[0] + newTAP._dxaGapHalf);
               for(int x = 0; x < newTAP._itcMac; x++)
               {
                  newTAP._rgdxaCenter[x] += adjust;
               }
               break;
          }
          case 0x02:
               if(newTAP._rgdxaCenter != null)
               {
                 int adjust = newTAP._dxaGapHalf - param;
                 newTAP._rgdxaCenter[0] += adjust;
               }
               newTAP._dxaGapHalf = param;
               break;
          case 0x03:
               newTAP._fCantSplit = getFlag(param);
               break;
          case 0x04:
               newTAP._fTableHeader = getFlag(param);
               break;
          case 0x05:

               newTAP._brcTop[0] = Utils.convertBytesToShort(varParam, 0);
               newTAP._brcTop[1] = Utils.convertBytesToShort(varParam, 2);

               newTAP._brcLeft[0] = Utils.convertBytesToShort(varParam, 4);
               newTAP._brcLeft[1] = Utils.convertBytesToShort(varParam, 6);

               newTAP._brcBottom[0] = Utils.convertBytesToShort(varParam, 8);
               newTAP._brcBottom[1] = Utils.convertBytesToShort(varParam, 10);

               newTAP._brcRight[0] = Utils.convertBytesToShort(varParam, 12);
               newTAP._brcRight[1] = Utils.convertBytesToShort(varParam, 14);

               newTAP._brcHorizontal[0] = Utils.convertBytesToShort(varParam, 16);
               newTAP._brcHorizontal[1] = Utils.convertBytesToShort(varParam, 18);

               newTAP._brcVertical[0] = Utils.convertBytesToShort(varParam, 20);
               newTAP._brcVertical[1] = Utils.convertBytesToShort(varParam, 22);
               break;
          case 0x06:
               //obsolete, used in word 1.x
               break;
          case 0x07:
               newTAP._dyaRowHeight = param;
               break;
          case 0x08:
               //I use varParam[0] and newTAP._itcMac interchangably
               newTAP._itcMac = varParam[0];
               newTAP._rgdxaCenter = new short[varParam[0] + 1];
               newTAP._rgtc = new TC[varParam[0]];

               for(int x = 0; x < newTAP._itcMac; x++)
               {
                 newTAP._rgdxaCenter[x] = Utils.convertBytesToShort(varParam , 1 + (x * 2));
                 newTAP._rgtc[x] = TC.convertBytesToTC(varParam, 1 + ((varParam[0] + 1) * 2) + (x * 20));
               }
               newTAP._rgdxaCenter[newTAP._itcMac] = Utils.convertBytesToShort(varParam , 1 + (newTAP._itcMac * 2));
               break;
          case 0x09:
               /** @todo handle cell shading*/
               break;
          case 0x0a:
               /** @todo handle word defined table styles*/
               break;
          case 0x20:
               for(int x = varParam[0]; x < varParam[1]; x++)
               {
                 if((varParam[2] & 0x08) > 0)
                 {
                   newTAP._rgtc[x]._brcRight[0] = Utils.convertBytesToShort(varParam, 6);
                   newTAP._rgtc[x]._brcRight[1] = Utils.convertBytesToShort(varParam, 8);
                 }
                 else if((varParam[2] & 0x04) > 0)
                 {
                   newTAP._rgtc[x]._brcBottom[0] = Utils.convertBytesToShort(varParam, 6);
                   newTAP._rgtc[x]._brcBottom[1] = Utils.convertBytesToShort(varParam, 8);
                 }
                 else if((varParam[2] & 0x02) > 0)
                 {
                   newTAP._rgtc[x]._brcLeft[0] = Utils.convertBytesToShort(varParam, 6);
                   newTAP._rgtc[x]._brcLeft[1] = Utils.convertBytesToShort(varParam, 8);
                 }
                 else if((varParam[2] & 0x01) > 0)
                 {
                   newTAP._rgtc[x]._brcTop[0] = Utils.convertBytesToShort(varParam, 6);
                   newTAP._rgtc[x]._brcTop[1] = Utils.convertBytesToShort(varParam, 8);
                 }
               }
               break;
          case 0x21:
               int index = (param & 0xff000000) >> 24;
               int count = (param & 0x00ff0000) >> 16;
               int width = (param & 0x0000ffff);

               short[] rgdxaCenter = new short[newTAP._itcMac + count + 1];
               TC[] rgtc = new TC[newTAP._itcMac + count];
               if(index >= newTAP._itcMac)
               {
                 index = newTAP._itcMac;
                 System.arraycopy(newTAP._rgdxaCenter, 0, rgdxaCenter, 0, newTAP._itcMac + 1);
                 System.arraycopy(newTAP._rgtc, 0, rgtc, 0, newTAP._itcMac);
               }
               else
               {
                 //copy rgdxaCenter
                 System.arraycopy(newTAP._rgdxaCenter, 0, rgdxaCenter, 0, index + 1);
                 System.arraycopy(newTAP._rgdxaCenter, index + 1, rgdxaCenter, index + count, (newTAP._itcMac) - (index));
                 //copy rgtc
                 System.arraycopy(newTAP._rgtc, 0, rgtc, 0, index);
                 System.arraycopy(newTAP._rgtc, index, rgtc, index + count, newTAP._itcMac - index);
               }

               for(int x = index; x < index + count; x++)
               {
                 rgtc[x] = new TC();
                 rgdxaCenter[x] = (short)(rgdxaCenter[x-1] + width);
               }
               rgdxaCenter[index + count] = (short)(rgdxaCenter[(index + count)-1] + width);
               break;
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
  static void doSEPOperation(SEP newSEP, int operand, int param, byte[] varParam)
  {
      switch(operand)
      {
          case 0:
               newSEP._cnsPgn = (byte)param;
               break;
          case 0x1:
               newSEP._iHeadingPgn = (byte)param;
               break;
          case 0x2:
               newSEP._olstAnn = varParam;
               break;
          case 0x3:
               //not quite sure
               break;
          case 0x4:
               //not quite sure
               break;
          case 0x5:
               newSEP._fEvenlySpaced = getFlag(param);
               break;
          case 0x6:
               newSEP._fUnlocked = getFlag(param);
               break;
          case 0x7:
               newSEP._dmBinFirst = (short)param;
               break;
          case 0x8:
               newSEP._dmBinOther = (short)param;
               break;
          case 0x9:
               newSEP._bkc = (byte)param;
               break;
          case 0xa:
               newSEP._fTitlePage = getFlag(param);
               break;
          case 0xb:
               newSEP._ccolM1 = (short)param;
               break;
          case 0xc:
               newSEP._dxaColumns = param;
               break;
          case 0xd:
               newSEP._fAutoPgn = getFlag(param);
               break;
          case 0xe:
               newSEP._nfcPgn = (byte)param;
               break;
          case 0xf:
               newSEP._dyaPgn = (short)param;
               break;
          case 0x10:
               newSEP._dxaPgn = (short)param;
               break;
          case 0x11:
               newSEP._fPgnRestart = getFlag(param);
               break;
          case 0x12:
               newSEP._fEndNote = getFlag(param);
               break;
          case 0x13:
               newSEP._lnc = (byte)param;
               break;
          case 0x14:
               newSEP._grpfIhdt = (byte)param;
               break;
          case 0x15:
               newSEP._nLnnMod = (short)param;
               break;
          case 0x16:
               newSEP._dxaLnn = param;
               break;
          case 0x17:
               newSEP._dyaHdrTop = param;
               break;
          case 0x18:
               newSEP._dyaHdrBottom = param;
               break;
          case 0x19:
               newSEP._fLBetween = getFlag(param);
               break;
          case 0x1a:
               newSEP._vjc = (byte)param;
               break;
          case 0x1b:
               newSEP._lnnMin = (short)param;
               break;
          case 0x1c:
               newSEP._pgnStart = (short)param;
               break;
          case 0x1d:
               newSEP._dmOrientPage = (byte)param;
               break;
          case 0x1e:
               //nothing
               break;
          case 0x1f:
               newSEP._xaPage = param;
               break;
          case 0x20:
               newSEP._yaPage = param;
               break;
          case 0x21:
               newSEP._dxaLeft = param;
               break;
          case 0x22:
               newSEP._dxaRight = param;
               break;
          case 0x23:
               newSEP._dyaTop = param;
               break;
          case 0x24:
               newSEP._dyaBottom = param;
               break;
          case 0x25:
               newSEP._dzaGutter = param;
               break;
          case 0x26:
               newSEP._dmPaperReq = (short)param;
               break;
          case 0x27:
               newSEP._fPropMark = getFlag(varParam[0]);
               break;
          case 0x28:
               break;
          case 0x29:
               break;
          case 0x2a:
               break;
          case 0x2b:
               newSEP._brcTop[0] = (short)(param & 0xffff);
               newSEP._brcTop[1] = (short)((param & 0xffff0000) >> 16);
               break;
          case 0x2c:
               newSEP._brcLeft[0] = (short)(param & 0xffff);
               newSEP._brcLeft[1] = (short)((param & 0xffff0000) >> 16);
               break;
          case 0x2d:
               newSEP._brcBottom[0] = (short)(param & 0xffff);
               newSEP._brcBottom[1] = (short)((param & 0xffff0000) >> 16);
               break;
          case 0x2e:
               newSEP._brcRight[0] = (short)(param & 0xffff);
               newSEP._brcRight[1] = (short)((param & 0xffff0000) >> 16);
               break;
          case 0x2f:
               newSEP._pgbProp = (short)param;
               break;
          case 0x30:
               newSEP._dxtCharSpace = param;
               break;
          case 0x31:
               newSEP._dyaLinePitch = param;
               break;
          case 0x33:
               newSEP._wTextFlow = (short)param;
               break;
          default:
               break;
      }

  }
  private static boolean getCHPFlag(byte x, boolean oldVal)
  {
      switch(x)
      {
          case 0:
               return false;
          case 1:
               return true;
          case (byte)0x80:
               return oldVal;
          case (byte)0x81:
               return !oldVal;
          default:
               return false;
      }
  }
  public static boolean getFlag(int x)
  {
      return x != 0;
  }
}
