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

package org.apache.poi.hdf.model.hdftypes;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hdf.model.hdftypes.definitions.TCAbstractType;

/**
 * Represents a document's stylesheet. A word documents formatting is stored as
 * compressed styles that are based on styles contained in the stylesheet. This
 * class also contains static utility functions to uncompress different
 * formatting properties.
 *
 * @author Ryan Ackley
 */
public final class StyleSheet implements HDFType {

  private static final int NIL_STYLE = 4095;
  private static final int PAP_TYPE = 1;
  private static final int CHP_TYPE = 2;
  private static final int SEP_TYPE = 4;
  private static final int TAP_TYPE = 5;
  //Vector _styleDescriptions;
  StyleDescription _nilStyle = new StyleDescription();
  StyleDescription[] _styleDescriptions;

  /**
   * StyleSheet constructor. Loads a document's stylesheet information,
   *
   * @param styleSheet A byte array containing a document's raw stylesheet
   *        info. Found by using FileInformationBlock.getFcStshf() and
   *        FileInformationBLock.getLcbStshf()
   */
  public StyleSheet(byte[] styleSheet)
  {
      int stshiLength = LittleEndian.getShort(styleSheet, 0);
      int stdCount = LittleEndian.getShort(styleSheet, 2);
      int baseLength = LittleEndian.getShort(styleSheet, 4);
      int[] rgftc = new int[3];

      rgftc[0] = LittleEndian.getInt(styleSheet, 14);
      rgftc[1] = LittleEndian.getInt(styleSheet, 18);
      rgftc[2] = LittleEndian.getInt(styleSheet, 22);

      int offset = 0;
      _styleDescriptions = new StyleDescription[stdCount];
      for(int x = 0; x < stdCount; x++)
      {
          int stdOffset = (2 + stshiLength) + offset;
          int stdSize = LittleEndian.getShort(styleSheet, stdOffset);
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
  /**
   * Creates a PartagraphProperties object from a papx stored in the
   * StyleDescription at the index istd in the StyleDescription array. The PAP
   * is placed in the StyleDescription at istd after its been created. Not
   * every StyleDescription will contain a papx. In these cases this function
   * does nothing
   *
   * @param istd The index of the StyleDescription to create the
   *        ParagraphProperties  from (and also place the finished PAP in)
   */
  private void createPap(int istd)
  {
      StyleDescription sd = _styleDescriptions[istd];
      ParagraphProperties pap = sd.getPAP();
      byte[] papx = sd.getPAPX();
      int baseIndex = sd.getBaseStyle();
      if(pap == null && papx != null)
      {
          ParagraphProperties parentPAP = _nilStyle.getPAP();
          if(baseIndex != NIL_STYLE)
          {

              parentPAP = _styleDescriptions[baseIndex].getPAP();
              if(parentPAP == null)
              {
                  createPap(baseIndex);
                  parentPAP = _styleDescriptions[baseIndex].getPAP();
              }

          }

          pap = (ParagraphProperties)uncompressProperty(papx, parentPAP, this);
          sd.setPAP(pap);
      }
  }
  /**
   * Creates a CharacterProperties object from a chpx stored in the
   * StyleDescription at the index istd in the StyleDescription array. The
   * CharacterProperties object is placed in the StyleDescription at istd after
   * its been created. Not every StyleDescription will contain a chpx. In these
   * cases this function does nothing.
   *
   * @param istd The index of the StyleDescription to create the
   *        CharacterProperties object from.
   */
  private void createChp(int istd)
  {
      StyleDescription sd = _styleDescriptions[istd];
      CharacterProperties chp = sd.getCHP();
      byte[] chpx = sd.getCHPX();
      int baseIndex = sd.getBaseStyle();
      if(chp == null && chpx != null)
      {
          CharacterProperties parentCHP = _nilStyle.getCHP();
          if(baseIndex != NIL_STYLE)
          {

              parentCHP = _styleDescriptions[baseIndex].getCHP();
              if(parentCHP == null)
              {
                  createChp(baseIndex);
                  parentCHP = _styleDescriptions[baseIndex].getCHP();
              }

          }

          chp = (CharacterProperties)uncompressProperty(chpx, parentCHP, this);
          sd.setCHP(chp);
      }
  }

  /**
   * Gets the StyleDescription at index x.
   *
   * @param x the index of the desired StyleDescription.
   */
  public StyleDescription getStyleDescription(int x)
  {
      return _styleDescriptions[x];
  }

  /**
   * Used in decompression of a chpx. This performs an operation defined by
   * a single sprm.
   *
   * @param oldCHP The base CharacterProperties.
   * @param newCHP The current CharacterProperties.
   * @param operand The operand defined by the sprm (See Word file format spec)
   * @param param The parameter defined by the sprm (See Word file format spec)
   * @param varParam The variable length parameter defined by the sprm. (See
   *        Word file format spec)
   * @param grpprl The entire chpx that this operation is a part of.
   * @param offset The offset in the grpprl of the next sprm
   * @param styleSheet The StyleSheet for this document.
   */
  static void doCHPOperation(CharacterProperties oldCHP, CharacterProperties newCHP,
                             int operand, int param,
                             byte[] varParam, byte[] grpprl, int offset,
                             StyleSheet styleSheet)
  {
      switch(operand)
      {
          case 0:
               newCHP.setFRMarkDel(getFlag(param));
               break;
          case 0x1:
               newCHP.setFRMark(getFlag(param));
               break;
          case 0x2:
               break;
          case 0x3:
               newCHP.setFcPic(param);
               newCHP.setFSpec(true);
               break;
          case 0x4:
               newCHP.setIbstRMark((short)param);
               break;
          case 0x5:
               short[] dttmRMark = new short[2];
               dttmRMark[0] = LittleEndian.getShort(grpprl, (offset - 4));
               dttmRMark[1] = LittleEndian.getShort(grpprl, (offset - 2));
               newCHP.setDttmRMark(dttmRMark);
               break;
          case 0x6:
               newCHP.setFData(getFlag(param));
               break;
          case 0x7:
               //don't care about this
               break;
          case 0x8:
               short chsDiff = (short)((param & 0xff0000) >>> 8);
               newCHP.setFChsDiff(getFlag(chsDiff));
               newCHP.setChse((short)(param & 0xffff));
               break;
          case 0x9:
               newCHP.setFSpec(true);
               newCHP.setFtcSym(LittleEndian.getShort(varParam, 0));
               newCHP.setXchSym(LittleEndian.getShort(varParam, 2));
               break;
          case 0xa:
               newCHP.setFOle2(getFlag(param));
               break;
          case 0xb:
               //?
               break;
          case 0xc:
               newCHP.setIcoHighlight((byte)param);
               newCHP.setFHighlight(getFlag(param));
               break;
          case 0xd:
               break;
          case 0xe:
               newCHP.setFcObj(param);
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
               newCHP.setIstd(param);
               break;
          case 0x31:
               //permutation vector for fast saves, who cares!
               break;
          case 0x32:
               newCHP.setFBold(false);
               newCHP.setFItalic(false);
               newCHP.setFOutline(false);
               newCHP.setFStrike(false);
               newCHP.setFShadow(false);
               newCHP.setFSmallCaps(false);
               newCHP.setFCaps(false);
               newCHP.setFVanish(false);
               newCHP.setKul((byte)0);
               newCHP.setIco((byte)0);
               break;
          case 0x33:
               try
               {
                   newCHP = (CharacterProperties)oldCHP.clone();
               }
               catch(CloneNotSupportedException e)
               {
                   //do nothing
               }
               return;
          case 0x34:
               break;
          case 0x35:
               newCHP.setFBold(getCHPFlag((byte)param, oldCHP.isFBold()));
               break;
          case 0x36:
               newCHP.setFItalic(getCHPFlag((byte)param, oldCHP.isFItalic()));
               break;
          case 0x37:
               newCHP.setFStrike(getCHPFlag((byte)param, oldCHP.isFStrike()));
               break;
          case 0x38:
               newCHP.setFOutline(getCHPFlag((byte)param, oldCHP.isFOutline()));
               break;
          case 0x39:
               newCHP.setFShadow(getCHPFlag((byte)param, oldCHP.isFShadow()));
               break;
          case 0x3a:
               newCHP.setFSmallCaps(getCHPFlag((byte)param, oldCHP.isFSmallCaps()));
               break;
          case 0x3b:
               newCHP.setFCaps(getCHPFlag((byte)param, oldCHP.isFCaps()));
               break;
          case 0x3c:
               newCHP.setFVanish(getCHPFlag((byte)param, oldCHP.isFVanish()));
               break;
          case 0x3d:
               newCHP.setFtcAscii((short)param);
               break;
          case 0x3e:
               newCHP.setKul((byte)param);
               break;
          case 0x3f:
               int hps = param & 0xff;
               if(hps != 0)
               {
                  newCHP.setHps(hps);
               }
               byte cInc = (byte)(((byte)(param & 0xfe00) >>> 4) >> 1);
               if(cInc != 0)
               {
                  newCHP.setHps(Math.max(newCHP.getHps() + (cInc * 2), 2));
               }
               byte hpsPos = (byte)((param & 0xff0000) >>> 8);
               if(hpsPos != 0x80)
               {
                  newCHP.setHpsPos(hpsPos);
               }
               boolean fAdjust = (param & 0x0100) > 0;
               if(fAdjust && hpsPos != 128 && hpsPos != 0 && oldCHP.getHpsPos() == 0)
               {
                  newCHP.setHps(Math.max(newCHP.getHps() + (-2), 2));
               }
               if(fAdjust && hpsPos == 0 && oldCHP.getHpsPos() != 0)
               {
                  newCHP.setHps(Math.max(newCHP.getHps() + 2, 2));
               }
               break;
          case 0x40:
               newCHP.setDxaSpace(param);
               break;
          case 0x41:
               newCHP.setLidDefault((short)param);
               break;
          case 0x42:
               newCHP.setIco((byte)param);
               break;
          case 0x43:
               newCHP.setHps(param);
               break;
          case 0x44:
               byte hpsLvl = (byte)param;
               newCHP.setHps(Math.max(newCHP.getHps() + (hpsLvl * 2), 2));
               break;
          case 0x45:
               newCHP.setHpsPos((short)param);
               break;
          case 0x46:
               if(param != 0)
               {
                  if(oldCHP.getHpsPos() == 0)
                  {
                      newCHP.setHps(Math.max(newCHP.getHps() + (-2), 2));
                  }
               }
               else
               {
                  if(oldCHP.getHpsPos() != 0)
                  {
                      newCHP.setHps(Math.max(newCHP.getHps() + 2, 2));
                  }
               }
               break;
          case 0x47:
               CharacterProperties genCHP = new CharacterProperties();
               genCHP.setFtcAscii(4);
               genCHP = (CharacterProperties)uncompressProperty(varParam, genCHP, styleSheet);
               CharacterProperties styleCHP = styleSheet.getStyleDescription(oldCHP.getBaseIstd()).getCHP();
               if(genCHP.isFBold() == newCHP.isFBold())
               {
                  newCHP.setFBold(styleCHP.isFBold());
               }
               if(genCHP.isFItalic() == newCHP.isFItalic())
               {
                  newCHP.setFItalic(styleCHP.isFItalic());
               }
               if(genCHP.isFSmallCaps() == newCHP.isFSmallCaps())
               {
                  newCHP.setFSmallCaps(styleCHP.isFSmallCaps());
               }
               if(genCHP.isFVanish() == newCHP.isFVanish())
               {
                  newCHP.setFVanish(styleCHP.isFVanish());
               }
               if(genCHP.isFStrike() == newCHP.isFStrike())
               {
                  newCHP.setFStrike(styleCHP.isFStrike());
               }
               if(genCHP.isFCaps() == newCHP.isFCaps())
               {
                  newCHP.setFCaps(styleCHP.isFCaps());
               }
               if(genCHP.getFtcAscii() == newCHP.getFtcAscii())
               {
                  newCHP.setFtcAscii(styleCHP.getFtcAscii());
               }
               if(genCHP.getFtcFE() == newCHP.getFtcFE())
               {
                  newCHP.setFtcFE(styleCHP.getFtcFE());
               }
               if(genCHP.getFtcOther() == newCHP.getFtcOther())
               {
                  newCHP.setFtcOther(styleCHP.getFtcOther());
               }
               if(genCHP.getHps() == newCHP.getHps())
               {
                  newCHP.setHps(styleCHP.getHps());
               }
               if(genCHP.getHpsPos() == newCHP.getHpsPos())
               {
                  newCHP.setHpsPos(styleCHP.getHpsPos());
               }
               if(genCHP.getKul() == newCHP.getKul())
               {
                  newCHP.setKul(styleCHP.getKul());
               }
               if(genCHP.getDxaSpace() == newCHP.getDxaSpace())
               {
                  newCHP.setDxaSpace(styleCHP.getDxaSpace());
               }
               if(genCHP.getIco() == newCHP.getIco())
               {
                  newCHP.setIco(styleCHP.getIco());
               }
               if(genCHP.getLidDefault() == newCHP.getLidDefault())
               {
                  newCHP.setLidDefault(styleCHP.getLidDefault());
               }
               if(genCHP.getLidFE() == newCHP.getLidFE())
               {
                  newCHP.setLidFE(styleCHP.getLidFE());
               }
               break;
          case 0x48:
               newCHP.setIss((byte)param);
               break;
          case 0x49:
               newCHP.setHps(LittleEndian.getShort(varParam, 0));
               break;
          case 0x4a:
               int increment = LittleEndian.getShort(varParam, 0);
               newCHP.setHps(Math.max(newCHP.getHps() + increment, 8));
               break;
          case 0x4b:
               newCHP.setHpsKern(param);
               break;
          case 0x4c:
               doCHPOperation(oldCHP, newCHP, 0x47, param, varParam, grpprl, offset, styleSheet);
               break;
          case 0x4d:
               float percentage = param/100.0f;
               int add = (int)(percentage * newCHP.getHps());
               newCHP.setHps(newCHP.getHps() + add);
               break;
          case 0x4e:
               newCHP.setYsr((byte)param);
               break;
          case 0x4f:
               newCHP.setFtcAscii((short)param);
               break;
          case 0x50:
               newCHP.setFtcFE((short)param);
               break;
          case 0x51:
               newCHP.setFtcOther((short)param);
               break;
          case 0x52:
               break;
          case 0x53:
               newCHP.setFDStrike(getFlag(param));
               break;
          case 0x54:
               newCHP.setFImprint(getFlag(param));
               break;
          case 0x55:
               newCHP.setFSpec(getFlag(param));
               break;
          case 0x56:
               newCHP.setFObj(getFlag(param));
               break;
          case 0x57:
               newCHP.setFPropMark(varParam[0]);
               newCHP.setIbstPropRMark(LittleEndian.getShort(varParam, 1));
               newCHP.setDttmPropRMark(LittleEndian.getInt(varParam, 3));
               break;
          case 0x58:
               newCHP.setFEmboss(getFlag(param));
               break;
          case 0x59:
               newCHP.setSfxtText((byte)param);
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
               byte[] xstDispFldRMark = new byte[32];
               newCHP.setFDispFldRMark(varParam[0]);
               newCHP.setIbstDispFldRMark(LittleEndian.getShort(varParam, 1));
               newCHP.setDttmDispFldRMark(LittleEndian.getInt(varParam, 3));
               System.arraycopy(varParam, 7, xstDispFldRMark, 0, 32);
               newCHP.setXstDispFldRMark(xstDispFldRMark);
               break;
          case 0x63:
               newCHP.setIbstRMarkDel((short)param);
               break;
          case 0x64:
               short[] dttmRMarkDel = new short[2];
               dttmRMarkDel[0] = LittleEndian.getShort(grpprl, offset - 4);
               dttmRMarkDel[1] = LittleEndian.getShort(grpprl, offset - 2);
               newCHP.setDttmRMarkDel(dttmRMarkDel);
               break;
          case 0x65:
               short[] brc = new short[2];
               brc[0] = LittleEndian.getShort(grpprl, offset - 4);
               brc[1] = LittleEndian.getShort(grpprl, offset - 2);
               newCHP.setBrc(brc);
               break;
          case 0x66:
               newCHP.setShd((short)param);
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
               newCHP.setLidDefault((short)param);
               break;
          case 0x6e:
               newCHP.setLidFE((short)param);
               break;
          case 0x6f:
               newCHP.setIdctHint((byte)param);
               break;
      }
  }

  /**
   * Used to uncompress a property stored in a grpprl. These include
   * CharacterProperties, ParagraphProperties, TableProperties, and
   * SectionProperties.
   *
   * @param grpprl The compressed form of the property.
   * @param parent The base property of the property.
   * @param styleSheet The document's stylesheet.
   *
   * @return An object that should be casted to the appropriate property.
   */
  public static Object uncompressProperty(byte[] grpprl, Object parent, StyleSheet styleSheet)
  {
    return uncompressProperty(grpprl, parent, styleSheet, true);
  }

  /**
   * Used to uncompress a property stored in a grpprl. These include
   * CharacterProperties, ParagraphProperties, TableProperties, and
   * SectionProperties.
   *
   * @param grpprl The compressed form of the property.
   * @param parent The base property of the property.
   * @param styleSheet The document's stylesheet.
   *
   * @return An object that should be casted to the appropriate property.
   */
  public static Object uncompressProperty(byte[] grpprl, Object parent, StyleSheet styleSheet, boolean doIstd)
  {
      Object newProperty = null;
      int offset = 0;
      int propertyType = PAP_TYPE;


      if(parent instanceof ParagraphProperties)
      {
          try
          {
              newProperty = ((ParagraphProperties)parent).clone();
          }
          catch(Exception e){}
          if(doIstd)
          {
            ((ParagraphProperties)newProperty).setIstd(LittleEndian.getShort(grpprl, 0));

            offset = 2;
          }
      }
      else if(parent instanceof CharacterProperties)
      {
          try
          {
              newProperty = ((CharacterProperties)parent).clone();
              ((CharacterProperties)newProperty).setBaseIstd(((CharacterProperties)parent).getIstd());
          }
          catch(Exception e){}
          propertyType = CHP_TYPE;
      }
      else if(parent instanceof SectionProperties)
      {
          newProperty = parent;
          propertyType = SEP_TYPE;
      }
      else if(parent instanceof TableProperties)
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
          short sprm = LittleEndian.getShort(grpprl, offset);
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
                   param = LittleEndian.getShort(grpprl, offset);
                   break;
              case 3:
                   opSize = 4;
                   param = LittleEndian.getInt(grpprl, offset);
                   break;
              case 4:
              case 5:
                   opSize = 2;
                   param = LittleEndian.getShort(grpprl, offset);
                   break;
              case 6://variable size

                   //there is one sprm that is a very special case
                   if(sprm != (short)0xd608)
                   {
                     opSize = LittleEndian.getUnsignedByte(grpprl, offset);
                     offset++;
                   }
                   else
                   {
                     opSize = LittleEndian.getShort(grpprl, offset) - 1;
                     offset += 2;
                   }
                   varParam = new byte[opSize];
                   System.arraycopy(grpprl, offset, varParam, 0, opSize);

                   break;
              case 7:
                   opSize = 3;
                   byte threeByteInt[] = new byte[4];
                   threeByteInt[0] = grpprl[offset];
                   threeByteInt[1] = grpprl[offset + 1];
                   threeByteInt[2] = grpprl[offset + 2];
                   threeByteInt[3] = (byte)0;
                   param = LittleEndian.getInt(threeByteInt, 0);
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
                     doPAPOperation((ParagraphProperties)newProperty, operand,
                                    param, varParam, grpprl,
                                    offset, spra);
                   }
                   break;
              case CHP_TYPE:

                   doCHPOperation((CharacterProperties)parent,
                                  (CharacterProperties)newProperty,
                                  operand, param, varParam,
                                  grpprl, offset, styleSheet);
                   break;
              case SEP_TYPE:

                   doSEPOperation((SectionProperties)newProperty, operand, param, varParam);
                   break;
              case TAP_TYPE:
                   if(type == 5)
                   {
                     doTAPOperation((TableProperties)newProperty, operand, param, varParam);
                   }
                   break;
          }


      }
      return newProperty;

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
  static void doPAPOperation(ParagraphProperties newPAP, int operand, int param,
                             byte[] varParam, byte[] grpprl, int offset,
                             int spra)
  {
      switch(operand)
      {
          case 0:
               newPAP.setIstd(param);
               break;
          case 0x1:
               //permuteIstd(newPAP, varParam);
               break;
          case 0x2:
               if(newPAP.getIstd() <=9 || newPAP.getIstd() >=1)
               {
                  newPAP.setIstd(newPAP.getIstd() + param);
                  if(param > 0)
                  {
                      newPAP.setIstd(Math.max(newPAP.getIstd(), 9));
                  }
                  else
                  {
                      newPAP.setIstd(Math.min(newPAP.getIstd(), 1));
                  }
               }
               break;
          case 0x3:
               newPAP.setJc((byte)param);
               break;
          case 0x4:
               newPAP.setFSideBySide((byte)param);
               break;
          case 0x5:
               newPAP.setFKeep((byte)param);
               break;
          case 0x6:
               newPAP.setFKeepFollow((byte)param);
               break;
          case 0x7:
               newPAP.setFPageBreakBefore((byte)param);
               break;
          case 0x8:
               newPAP.setBrcl((byte)param);
               break;
          case 0x9:
               newPAP.setBrcp((byte)param);
               break;
          case 0xa:
               newPAP.setIlvl((byte)param);
               break;
          case 0xb:
               newPAP.setIlfo(param);
               break;
          case 0xc:
               newPAP.setFNoLnn((byte)param);
               break;
          case 0xd:
               /**@todo handle tabs*/
               break;
          case 0xe:
               newPAP.setDxaRight(param);
               break;
          case 0xf:
               newPAP.setDxaLeft(param);
               break;
          case 0x10:
               newPAP.setDxaLeft(newPAP.getDxaLeft() + param);
               newPAP.setDxaLeft(Math.max(0, newPAP.getDxaLeft()));
               break;
          case 0x11:
               newPAP.setDxaLeft1(param);
               break;
          case 0x12:
               short[] lspd = newPAP.getLspd();
               lspd[0] = LittleEndian.getShort(grpprl, offset - 4);
               lspd[1] = LittleEndian.getShort(grpprl, offset - 2);
               break;
          case 0x13:
               newPAP.setDyaBefore(param);
               break;
          case 0x14:
               newPAP.setDyaAfter(param);
               break;
          case 0x15:
               /**@todo handle tabs*/
               break;
          case 0x16:
               newPAP.setFInTable((byte)param);
               break;
          case 0x17:
               newPAP.setFTtp((byte)param);
               break;
          case 0x18:
               newPAP.setDxaAbs(param);
               break;
          case 0x19:
               newPAP.setDyaAbs(param);
               break;
          case 0x1a:
               newPAP.setDxaWidth(param);
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
               newPAP.setDxaFromText(param);
               break;
          case 0x23:
               newPAP.setWr((byte)param);
               break;
          case 0x24:
               short[] brcTop = newPAP.getBrcTop();
               brcTop[0] = LittleEndian.getShort(grpprl, offset - 4);
               brcTop[1] = LittleEndian.getShort(grpprl, offset - 2);
               break;
          case 0x25:
               short[] brcLeft = newPAP.getBrcLeft();
               brcLeft[0] = LittleEndian.getShort(grpprl, offset - 4);
               brcLeft[1] = LittleEndian.getShort(grpprl, offset - 2);
               break;
          case 0x26:
               short[] brcBottom = newPAP.getBrcBottom();
               brcBottom[0] = LittleEndian.getShort(grpprl, offset - 4);
               brcBottom[1] = LittleEndian.getShort(grpprl, offset - 2);
               break;
          case 0x27:
               short[] brcRight = newPAP.getBrcRight();
               brcRight[0] = LittleEndian.getShort(grpprl, offset - 4);
               brcRight[1] = LittleEndian.getShort(grpprl, offset - 2);
               break;
          case 0x28:
               short[] brcBetween = newPAP.getBrcBetween();
               brcBetween[0] = LittleEndian.getShort(grpprl, offset - 4);
               brcBetween[1] = LittleEndian.getShort(grpprl, offset - 2);
               break;
          case 0x29:
               short[] brcBar = newPAP.getBrcBar();
               brcBar[0] = LittleEndian.getShort(grpprl, offset - 4);
               brcBar[1] = LittleEndian.getShort(grpprl, offset - 2);
               break;
          case 0x2a:
               newPAP.setFNoAutoHyph((byte)param);
               break;
          case 0x2b:
               newPAP.setDyaHeight(param);
               break;
          case 0x2c:
               newPAP.setDcs((short)param);
               break;
          case 0x2d:
               newPAP.setShd((short)param);
               break;
          case 0x2e:
               newPAP.setDyaFromText(param);
               break;
          case 0x2f:
               newPAP.setDxaFromText(param);
               break;
          case 0x30:
               newPAP.setFLocked((byte)param);
               break;
          case 0x31:
               newPAP.setFWidowControl((byte)param);
               break;
          case 0x32:
               //undocumented
               break;
          case 0x33:
               newPAP.setFKinsoku((byte)param);
               break;
          case 0x34:
               newPAP.setFWordWrap((byte)param);
               break;
          case 0x35:
               newPAP.setFOverflowPunct((byte)param);
               break;
          case 0x36:
               newPAP.setFTopLinePunct((byte)param);
               break;
          case 0x37:
               newPAP.setFAutoSpaceDE((byte)param);
               break;
          case 0x38:
               newPAP.setFAutoSpaceDN((byte)param);
               break;
          case 0x39:
               newPAP.setWAlignFont(param);
               break;
          case 0x3a:
               newPAP.setFontAlign((short)param);
               break;
          case 0x3b:
               //obsolete
               break;
          case 0x3e:
               newPAP.setAnld(varParam);
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
                  newPAP.setNumrm(varParam);
               }
               else
               {
                  /**@todo handle large PAPX from data stream*/
               }
               break;

          case 0x47:
               newPAP.setFUsePgsuSettings((byte)param);
               break;
          case 0x48:
               newPAP.setFAdjustRight((byte)param);
               break;
          default:
               break;
      }
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
  static void doTAPOperation(TableProperties newTAP, int operand, int param, byte[] varParam)
  {
      switch(operand)
      {
          case 0:
               newTAP.setJc((short)param);
               break;
          case 0x01:
          {
               short[] rgdxaCenter = newTAP.getRgdxaCenter();
               short itcMac = newTAP.getItcMac();
               int adjust = param - (rgdxaCenter[0] + newTAP.getDxaGapHalf());
               for(int x = 0; x < itcMac; x++)
               {
                  rgdxaCenter[x] += adjust;
               }
               break;
          }
          case 0x02:
          {
               short[] rgdxaCenter = newTAP.getRgdxaCenter();
               if(rgdxaCenter != null)
               {
                 int adjust = newTAP.getDxaGapHalf() - param;
                 rgdxaCenter[0] += adjust;
               }
               newTAP.setDxaGapHalf(param);
               break;
          }
          case 0x03:
               newTAP.setFCantSplit(getFlag(param));
               break;
          case 0x04:
               newTAP.setFTableHeader(getFlag(param));
               break;
          case 0x05:
          {
               short[] brcTop = newTAP.getBrcTop();
               short[] brcLeft = newTAP.getBrcLeft();
               short[] brcBottom = newTAP.getBrcBottom();
               short[] brcRight = newTAP.getBrcRight();
               short[] brcVertical = newTAP.getBrcVertical();
               short[] brcHorizontal = newTAP.getBrcHorizontal();

               brcTop[0] = LittleEndian.getShort(varParam, 0);
               brcTop[1] = LittleEndian.getShort(varParam, 2);

               brcLeft[0] = LittleEndian.getShort(varParam, 4);
               brcLeft[1] = LittleEndian.getShort(varParam, 6);

               brcBottom[0] = LittleEndian.getShort(varParam, 8);
               brcBottom[1] = LittleEndian.getShort(varParam, 10);

               brcRight[0] = LittleEndian.getShort(varParam, 12);
               brcRight[1] = LittleEndian.getShort(varParam, 14);

               brcHorizontal[0] = LittleEndian.getShort(varParam, 16);
               brcHorizontal[1] = LittleEndian.getShort(varParam, 18);

               brcVertical[0] = LittleEndian.getShort(varParam, 20);
               brcVertical[1] = LittleEndian.getShort(varParam, 22);
               break;
          }
          case 0x06:
               //obsolete, used in word 1.x
               break;
          case 0x07:
               newTAP.setDyaRowHeight(param);
               break;
          case 0x08:
          {
               short[] rgdxaCenter = new short[varParam[0] + 1];
               TableCellDescriptor[] rgtc = new TableCellDescriptor[varParam[0]];
               short itcMac = varParam[0];
               //I use varParam[0] and newTAP._itcMac interchangably
               newTAP.setItcMac(itcMac);
               newTAP.setRgdxaCenter(rgdxaCenter) ;
               newTAP.setRgtc(rgtc);

               for(int x = 0; x < itcMac; x++)
               {
                 rgdxaCenter[x] = LittleEndian.getShort(varParam , 1 + (x * 2));
                 rgtc[x] = TableCellDescriptor.convertBytesToTC(varParam, 1 + ((itcMac + 1) * 2) + (x * 20));
               }
               rgdxaCenter[itcMac] = LittleEndian.getShort(varParam , 1 + (itcMac * 2));
               break;
          }
          case 0x09:
               /** @todo handle cell shading*/
               break;
          case 0x0a:
               /** @todo handle word defined table styles*/
               break;
          case 0x20:
          {
               TCAbstractType[] rgtc = newTAP.getRgtc();

               for(int x = varParam[0]; x < varParam[1]; x++)
               {

                 if((varParam[2] & 0x08) > 0)
                 {
                   short[] brcRight = rgtc[x].getBrcRight();
                   brcRight[0] = LittleEndian.getShort(varParam, 6);
                   brcRight[1] = LittleEndian.getShort(varParam, 8);
                 }
                 else if((varParam[2] & 0x04) > 0)
                 {
                   short[] brcBottom = rgtc[x].getBrcBottom();
                   brcBottom[0] = LittleEndian.getShort(varParam, 6);
                   brcBottom[1] = LittleEndian.getShort(varParam, 8);
                 }
                 else if((varParam[2] & 0x02) > 0)
                 {
                   short[] brcLeft = rgtc[x].getBrcLeft();
                   brcLeft[0] = LittleEndian.getShort(varParam, 6);
                   brcLeft[1] = LittleEndian.getShort(varParam, 8);
                 }
                 else if((varParam[2] & 0x01) > 0)
                 {
                   short[] brcTop = rgtc[x].getBrcTop();
                   brcTop[0] = LittleEndian.getShort(varParam, 6);
                   brcTop[1] = LittleEndian.getShort(varParam, 8);
                 }
               }
               break;
          }
          case 0x21:
               int index = (param & 0xff000000) >> 24;
               int count = (param & 0x00ff0000) >> 16;
               int width = (param & 0x0000ffff);
               int itcMac = newTAP.getItcMac();

               short[] rgdxaCenter = new short[itcMac + count + 1];
               TableCellDescriptor[] rgtc = new TableCellDescriptor[itcMac + count];
               if(index >= itcMac)
               {
                 index = itcMac;
                 System.arraycopy(newTAP.getRgdxaCenter(), 0, rgdxaCenter, 0, itcMac + 1);
                 System.arraycopy(newTAP.getRgtc(), 0, rgtc, 0, itcMac);
               }
               else
               {
                 //copy rgdxaCenter
                 System.arraycopy(newTAP.getRgdxaCenter(), 0, rgdxaCenter, 0, index + 1);
                 System.arraycopy(newTAP.getRgdxaCenter(), index + 1, rgdxaCenter, index + count, itcMac - (index));
                 //copy rgtc
                 System.arraycopy(newTAP.getRgtc(), 0, rgtc, 0, index);
                 System.arraycopy(newTAP.getRgtc(), index, rgtc, index + count, itcMac - index);
               }

               for(int x = index; x < index + count; x++)
               {
                 rgtc[x] = new TableCellDescriptor();
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
  /**
   * Used in decompression of a sepx. This performs an operation defined by
   * a single sprm.
   *
   * @param newSEP The SectionProperty to perfrom the operation on.
   * @param operand The operation to perform.
   * @param param The operation's parameter.
   * @param varParam The operation variable length parameter.
   */
  static void doSEPOperation(SectionProperties newSEP, int operand, int param, byte[] varParam)
  {
      switch(operand)
      {
          case 0:
               newSEP.setCnsPgn((byte)param);
               break;
          case 0x1:
               newSEP.setIHeadingPgn((byte)param);
               break;
          case 0x2:
               newSEP.setOlstAnm(varParam);
               break;
          case 0x3:
               //not quite sure
               break;
          case 0x4:
               //not quite sure
               break;
          case 0x5:
               newSEP.setFEvenlySpaced(getFlag(param));
               break;
          case 0x6:
               newSEP.setFUnlocked(getFlag(param));
               break;
          case 0x7:
               newSEP.setDmBinFirst((short)param);
               break;
          case 0x8:
               newSEP.setDmBinOther((short)param);
               break;
          case 0x9:
               newSEP.setBkc((byte)param);
               break;
          case 0xa:
               newSEP.setFTitlePage(getFlag(param));
               break;
          case 0xb:
               newSEP.setCcolM1((short)param);
               break;
          case 0xc:
               newSEP.setDxaColumns(param);
               break;
          case 0xd:
               newSEP.setFAutoPgn(getFlag(param));
               break;
          case 0xe:
               newSEP.setNfcPgn((byte)param);
               break;
          case 0xf:
               newSEP.setDyaPgn((short)param);
               break;
          case 0x10:
               newSEP.setDxaPgn((short)param);
               break;
          case 0x11:
               newSEP.setFPgnRestart(getFlag(param));
               break;
          case 0x12:
               newSEP.setFEndNote(getFlag(param));
               break;
          case 0x13:
               newSEP.setLnc((byte)param);
               break;
          case 0x14:
               newSEP.setGrpfIhdt((byte)param);
               break;
          case 0x15:
               newSEP.setNLnnMod((short)param);
               break;
          case 0x16:
               newSEP.setDxaLnn(param);
               break;
          case 0x17:
               newSEP.setDyaHdrTop(param);
               break;
          case 0x18:
               newSEP.setDyaHdrBottom(param);
               break;
          case 0x19:
               newSEP.setFLBetween(getFlag(param));
               break;
          case 0x1a:
               newSEP.setVjc((byte)param);
               break;
          case 0x1b:
               newSEP.setLnnMin((short)param);
               break;
          case 0x1c:
               newSEP.setPgnStart((short)param);
               break;
          case 0x1d:
               newSEP.setDmOrientPage((byte)param);
               break;
          case 0x1e:
               //nothing
               break;
          case 0x1f:
               newSEP.setXaPage(param);
               break;
          case 0x20:
               newSEP.setYaPage(param);
               break;
          case 0x21:
               newSEP.setDxaLeft(param);
               break;
          case 0x22:
               newSEP.setDxaRight(param);
               break;
          case 0x23:
               newSEP.setDyaTop(param);
               break;
          case 0x24:
               newSEP.setDyaBottom(param);
               break;
          case 0x25:
               newSEP.setDzaGutter(param);
               break;
          case 0x26:
               newSEP.setDmPaperReq((short)param);
               break;
          case 0x27:
               newSEP.setFPropMark(getFlag(varParam[0]));
               break;
          case 0x28:
               break;
          case 0x29:
               break;
          case 0x2a:
               break;
          case 0x2b:
               short[] brcTop = newSEP.getBrcTop();
               brcTop[0] = (short)(param & 0xffff);
               brcTop[1] = (short)((param & 0xffff0000) >> 16);
               break;
          case 0x2c:
               short[] brcLeft = newSEP.getBrcLeft();
               brcLeft[0] = (short)(param & 0xffff);
               brcLeft[1] = (short)((param & 0xffff0000) >> 16);
               break;
          case 0x2d:
               short[] brcBottom = newSEP.getBrcBottom();
               brcBottom[0] = (short)(param & 0xffff);
               brcBottom[1] = (short)((param & 0xffff0000) >> 16);
               break;
          case 0x2e:
               short[] brcRight = newSEP.getBrcRight();
               brcRight[0] = (short)(param & 0xffff);
               brcRight[1] = (short)((param & 0xffff0000) >> 16);
               break;
          case 0x2f:
               newSEP.setPgbProp(param);
               break;
          case 0x30:
               newSEP.setDxtCharSpace(param);
               break;
          case 0x31:
               newSEP.setDyaLinePitch(param);
               break;
          case 0x33:
               newSEP.setWTextFlow((short)param);
               break;
          default:
               break;
      }

  }
  /**
   * Converts an byte value into a boolean. The byte parameter can be 1,0, 128,
   * or 129. if it is 128, this function returns the same value as oldVal. If
   * it is 129, this function returns !oldVal. This is used for certain sprms
   *
   * @param x The byte value to convert.
   * @param oldVal The old boolean value.
   *
   * @return A boolean whose value depends on x and oldVal.
   */
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

  /**
   * Converts an int into a boolean. If the int is non-zero, it returns true.
   * Otherwise it returns false.
   *
   * @param x The int to convert.
   *
   * @return A boolean whose value depends on x.
   */
  public static boolean getFlag(int x)
  {
      return x != 0;
  }
}
