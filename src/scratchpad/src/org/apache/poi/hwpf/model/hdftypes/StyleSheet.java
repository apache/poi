

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


package org.apache.poi.hwpf.model.hdftypes;

import java.util.*;
import java.io.IOException;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hwpf.model.io.HWPFOutputStream;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
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

public class StyleSheet implements HDFType
{

  private static final int NIL_STYLE = 4095;
  private static final int PAP_TYPE = 1;
  private static final int CHP_TYPE = 2;
  private static final int SEP_TYPE = 4;
  private static final int TAP_TYPE = 5;


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
   */
  public StyleSheet(byte[] tableStream, int offset)
  {
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

      offset = (LittleEndian.SHORT_SIZE + _stshiLength);
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

          LittleEndian.putShort(sizeHolder, (short)(std.length));
          out.write(sizeHolder);
          out.write(std);
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
      Paragraph pap = sd.getPAP();
      byte[] papx = sd.getPAPX();
      int baseIndex = sd.getBaseStyle();
      if(pap == null && papx != null)
      {
          Paragraph parentPAP = new Paragraph();
          if(baseIndex != NIL_STYLE)
          {

              parentPAP = _styleDescriptions[baseIndex].getPAP();
              if(parentPAP == null)
              {
                  createPap(baseIndex);
                  parentPAP = _styleDescriptions[baseIndex].getPAP();
              }

          }

          pap = (Paragraph)ParagraphSprmUncompressor.uncompressPAP(parentPAP, papx, 2);
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
      CharacterRun chp = sd.getCHP();
      byte[] chpx = sd.getCHPX();
      int baseIndex = sd.getBaseStyle();
      if(chp == null && chpx != null)
      {
          CharacterRun parentCHP = new CharacterRun();
          if(baseIndex != NIL_STYLE)
          {

              parentCHP = _styleDescriptions[baseIndex].getCHP();
              if(parentCHP == null)
              {
                  createChp(baseIndex);
                  parentCHP = _styleDescriptions[baseIndex].getCHP();
              }

          }

          chp = (CharacterRun)CharacterSprmUncompressor.uncompressCHP(parentCHP, chpx, 0);
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

  public CharacterRun getCharacterStyle(int x)
  {
    return (_styleDescriptions[x] != null ? _styleDescriptions[x].getCHP() : null);
  }

  public Paragraph getParagraphStyle(int x)
  {
    return (_styleDescriptions[x] != null ? _styleDescriptions[x].getPAP() : null);
  }

}
