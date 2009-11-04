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

package org.apache.poi.hwpf.model;

import java.io.IOException;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hwpf.model.io.HWPFOutputStream;
import org.apache.poi.hwpf.usermodel.CharacterProperties;
import org.apache.poi.hwpf.usermodel.ParagraphProperties;
import org.apache.poi.hwpf.sprm.ParagraphSprmUncompressor;
import org.apache.poi.hwpf.sprm.CharacterSprmUncompressor;

/**
 * Represents a document's stylesheet. A word documents formatting is stored as
 * compressed styles that are based on styles contained in the stylesheet. This
 * class also contains static utility functions to uncompress different
 * formatting properties.
 *
 * @author Ryan Ackley
 */
public final class StyleSheet implements HDFType {

  public static final int NIL_STYLE = 4095;
  private static final int PAP_TYPE = 1;
  private static final int CHP_TYPE = 2;
  private static final int SEP_TYPE = 4;
  private static final int TAP_TYPE = 5;


  private final static ParagraphProperties NIL_PAP = new ParagraphProperties();
  private final static CharacterProperties NIL_CHP = new CharacterProperties();

  private int _stshiLength;
  private int _baseLength;
  private int _flags;
  private int _maxIndex;
  private int _maxFixedIndex;
  private int _stylenameVersion;
  private int[] _rgftc;

  StyleDescription[] _styleDescriptions;

  /**
   * StyleSheet constructor. Loads a document's stylesheet information,
   *
   * @param tableStream A byte array containing a document's raw stylesheet
   *        info. Found by using FileInformationBlock.getFcStshf() and
   *        FileInformationBLock.getLcbStshf()
   */
  public StyleSheet(byte[] tableStream, int offset)
  {
      int startOffset = offset;
      _stshiLength = LittleEndian.getShort(tableStream, offset);
      offset += LittleEndian.SHORT_SIZE;
      int stdCount = LittleEndian.getShort(tableStream, offset);
      offset += LittleEndian.SHORT_SIZE;
      _baseLength = LittleEndian.getShort(tableStream, offset);
      offset += LittleEndian.SHORT_SIZE;
      _flags = LittleEndian.getShort(tableStream, offset);
      offset += LittleEndian.SHORT_SIZE;
      _maxIndex = LittleEndian.getShort(tableStream, offset);
      offset += LittleEndian.SHORT_SIZE;
      _maxFixedIndex = LittleEndian.getShort(tableStream, offset);
      offset += LittleEndian.SHORT_SIZE;
      _stylenameVersion = LittleEndian.getShort(tableStream, offset);
      offset += LittleEndian.SHORT_SIZE;

      _rgftc = new int[3];
      _rgftc[0] = LittleEndian.getShort(tableStream, offset);
      offset += LittleEndian.SHORT_SIZE;
      _rgftc[1] = LittleEndian.getShort(tableStream, offset);
      offset += LittleEndian.SHORT_SIZE;
      _rgftc[2] = LittleEndian.getShort(tableStream, offset);
      offset += LittleEndian.SHORT_SIZE;

      offset = startOffset + LittleEndian.SHORT_SIZE + _stshiLength;
      _styleDescriptions = new StyleDescription[stdCount];
      for(int x = 0; x < stdCount; x++)
      {
          int stdSize = LittleEndian.getShort(tableStream, offset);
          //get past the size
          offset += 2;
          if(stdSize > 0)
          {
              //byte[] std = new byte[stdSize];

              StyleDescription aStyle = new StyleDescription(tableStream,
                _baseLength, offset, true);

              _styleDescriptions[x] = aStyle;
          }

          offset += stdSize;

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

  public void writeTo(HWPFOutputStream out)
    throws IOException
  {
    int offset = 0;
    // add two bytes so we can prepend the stylesheet w/ its size
    byte[] buf = new byte[_stshiLength + 2];
    LittleEndian.putShort(buf, offset, (short)_stshiLength);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putShort(buf, offset, (short)_styleDescriptions.length);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putShort(buf, offset, (short)_baseLength);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putShort(buf, offset, (short)_flags);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putShort(buf, offset, (short)_maxIndex);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putShort(buf, offset, (short)_maxFixedIndex);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putShort(buf, offset, (short)_stylenameVersion);
    offset += LittleEndian.SHORT_SIZE;

    LittleEndian.putShort(buf, offset, (short)_rgftc[0]);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putShort(buf, offset, (short)_rgftc[1]);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putShort(buf, offset, (short)_rgftc[2]);

    out.write(buf);

    byte[] sizeHolder = new byte[2];
    for (int x = 0; x < _styleDescriptions.length; x++)
    {
      if(_styleDescriptions[x] != null)
      {
          byte[] std = _styleDescriptions[x].toByteArray();

          // adjust the size so it is always on a word boundary
          LittleEndian.putShort(sizeHolder, (short)((std.length) + (std.length % 2)));
          out.write(sizeHolder);
          out.write(std);

          // Must always start on a word boundary.
          if (std.length % 2 == 1)
          {
            out.write('\0');
          }
      }
      else
      {
        sizeHolder[0] = 0;
        sizeHolder[1] = 0;
        out.write(sizeHolder);
      }
    }
  }
  public boolean equals(Object o)
  {
    StyleSheet ss = (StyleSheet)o;

    if (ss._baseLength == _baseLength && ss._flags == _flags &&
        ss._maxFixedIndex ==_maxFixedIndex && ss._maxIndex == _maxIndex &&
        ss._rgftc[0] == _rgftc[0] && ss._rgftc[1] == _rgftc[1] &&
        ss._rgftc[2] == _rgftc[2] && ss._stshiLength == _stshiLength &&
        ss._stylenameVersion == _stylenameVersion)
    {
      if (ss._styleDescriptions.length == _styleDescriptions.length)
      {
        for (int x = 0; x < _styleDescriptions.length; x++)
        {
          // check for null
          if (ss._styleDescriptions[x] != _styleDescriptions[x])
          {
            // check for equality
            if (!ss._styleDescriptions[x].equals(_styleDescriptions[x]))
            {
              return false;
            }
          }
        }
        return true;
      }
    }
    return false;
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
          ParagraphProperties parentPAP = new ParagraphProperties();
          if(baseIndex != NIL_STYLE)
          {

              parentPAP = _styleDescriptions[baseIndex].getPAP();
              if(parentPAP == null) {
                  if(baseIndex == istd) {
                      // Oh dear, style claims that it is its own parent
                      throw new IllegalStateException("Pap style " + istd + " claimed to have itself as its parent, which isn't allowed");
                  }
                  // Create the parent style
                  createPap(baseIndex);
                  parentPAP = _styleDescriptions[baseIndex].getPAP();
              }

          }

          pap = ParagraphSprmUncompressor.uncompressPAP(parentPAP, papx, 2);
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
          CharacterProperties parentCHP = new CharacterProperties();
          if(baseIndex != NIL_STYLE)
          {

              parentCHP = _styleDescriptions[baseIndex].getCHP();
              if(parentCHP == null)
              {
                  createChp(baseIndex);
                  parentCHP = _styleDescriptions[baseIndex].getCHP();
              }

          }

          chp = CharacterSprmUncompressor.uncompressCHP(parentCHP, chpx, 0);
          sd.setCHP(chp);
      }
  }

  /**
   * Gets the number of styles in the style sheet.
   * @return The number of styles in the style sheet.
   */
  public int numStyles() {
      return _styleDescriptions.length;
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

  public CharacterProperties getCharacterStyle(int x)
  {
    if (x == NIL_STYLE)
    {
      return NIL_CHP;
    }
    return (_styleDescriptions[x] != null ? _styleDescriptions[x].getCHP() : null);
  }

  public ParagraphProperties getParagraphStyle(int x)
  {
    if (x == NIL_STYLE)
    {
      return NIL_PAP;
    }
    return (_styleDescriptions[x] != null ? _styleDescriptions[x].getPAP() : null);
  }

}
