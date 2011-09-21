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

import org.apache.poi.hwpf.model.io.HWPFOutputStream;
import org.apache.poi.hwpf.sprm.CharacterSprmUncompressor;
import org.apache.poi.hwpf.sprm.ParagraphSprmUncompressor;
import org.apache.poi.hwpf.usermodel.CharacterProperties;
import org.apache.poi.hwpf.usermodel.ParagraphProperties;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * Represents a document's stylesheet. A word documents formatting is stored as
 * compressed styles that are based on styles contained in the stylesheet. This
 * class also contains static utility functions to uncompress different
 * formatting properties.
 * <p>
 * Fields documentation is quotes from Microsoft Office Word 97-2007 Binary File
 * Format (.doc) Specification, page 36 of 210
 * 
 * @author Ryan Ackley
 */
@Internal
public final class StyleSheet implements HDFType {

  public static final int NIL_STYLE = 4095;
  private static final int PAP_TYPE = 1;
  private static final int CHP_TYPE = 2;
  private static final int SEP_TYPE = 4;
  private static final int TAP_TYPE = 5;

    @Deprecated
    private final static ParagraphProperties NIL_PAP = new ParagraphProperties();
    @Deprecated
    private final static CharacterProperties NIL_CHP = new CharacterProperties();

  private final static byte[] NIL_CHPX = new byte[] {};
  private final static byte[] NIL_PAPX = new byte[] {0, 0};

    /**
     * Size of the STSHI structure
     */
    private int _cbStshi;

    /**
     * General information about a stylesheet
     */
    private Stshif _stshif;
    
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
        _cbStshi = LittleEndian.getShort( tableStream, offset );
        offset += LittleEndian.SHORT_SIZE;

        /*
         * Count of styles in stylesheet
         * 
         * The number of styles in this style sheet. There will be stshi.cstd
         * (cbSTD, STD) pairs in the file following the STSHI. Note: styles can
         * be empty, i.e. cbSTD==0.
         */
        
        _stshif = new Stshif( tableStream, offset );
        offset += Stshif.getSize();

        // shall we discard cbLSD and mpstilsd?
        
      offset = startOffset + LittleEndian.SHORT_SIZE + _cbStshi;
      _styleDescriptions = new StyleDescription[_stshif.getCstd()];
      for(int x = 0; x < _stshif.getCstd(); x++)
      {
          int stdSize = LittleEndian.getShort(tableStream, offset);
          //get past the size
          offset += 2;
          if(stdSize > 0)
          {
              //byte[] std = new byte[stdSize];

              StyleDescription aStyle = new StyleDescription(tableStream,
                      _stshif.getCbSTDBaseInFile(), offset, true);

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

        /*
         * we don't support 2003 Word extensions in STSHI (but may be we should
         * at least not delete them, shouldn't we?), so our structure is always
         * 18 bytes in length -- sergey
         */
        this._cbStshi = 18;

    // add two bytes so we can prepend the stylesheet w/ its size
    byte[] buf = new byte[_cbStshi + 2];

    LittleEndian.putUShort(buf, offset, (short)_cbStshi);
    offset += LittleEndian.SHORT_SIZE;

    _stshif.setCstd( _styleDescriptions.length );
    _stshif.serialize( buf, offset );
    offset += Stshif.getSize();

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

    if (ss._stshif.equals( this._stshif ) && ss._cbStshi == _cbStshi)
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
  @Deprecated
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

          if (parentPAP == null) {
              parentPAP = new ParagraphProperties();
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
  @Deprecated
  private void createChp(int istd)
  {
      StyleDescription sd = _styleDescriptions[istd];
      CharacterProperties chp = sd.getCHP();
      byte[] chpx = sd.getCHPX();
      int baseIndex = sd.getBaseStyle();
      
      if(baseIndex == istd) {
         // Oh dear, this isn't allowed...
         // The word file seems to be corrupted
         // Switch to using the nil style so that
         //  there's a chance we can read it
         baseIndex = NIL_STYLE;
      }
      
      // Build and decompress the Chp if required 
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
     * @param styleIndex
     *            the index of the desired StyleDescription.
     */
    public StyleDescription getStyleDescription( int styleIndex )
    {
        return _styleDescriptions[styleIndex];
    }

    @Deprecated
    public CharacterProperties getCharacterStyle( int styleIndex )
    {
        if ( styleIndex == NIL_STYLE )
        {
            return NIL_CHP;
        }

        if ( styleIndex >= _styleDescriptions.length )
        {
            return NIL_CHP;
        }

        return ( _styleDescriptions[styleIndex] != null ? _styleDescriptions[styleIndex]
                .getCHP() : NIL_CHP );
    }

    @Deprecated
    public ParagraphProperties getParagraphStyle( int styleIndex )
    {
        if ( styleIndex == NIL_STYLE )
        {
            return NIL_PAP;
        }

        if ( styleIndex >= _styleDescriptions.length )
        {
            return NIL_PAP;
        }

        if ( _styleDescriptions[styleIndex] == null )
        {
            return NIL_PAP;
        }

        if ( _styleDescriptions[styleIndex].getPAP() == null )
        {
            return NIL_PAP;
        }

        return _styleDescriptions[styleIndex].getPAP();
    }

    public byte[] getCHPX( int styleIndex )
    {
        if ( styleIndex == NIL_STYLE )
        {
            return NIL_CHPX;
        }

        if ( styleIndex >= _styleDescriptions.length )
        {
            return NIL_CHPX;
        }

        if ( _styleDescriptions[styleIndex] == null )
        {
            return NIL_CHPX;
        }

        if ( _styleDescriptions[styleIndex].getCHPX() == null )
        {
            return NIL_CHPX;
        }

        return _styleDescriptions[styleIndex].getCHPX();
    }

    public byte[] getPAPX( int styleIndex )
    {
        if ( styleIndex == NIL_STYLE )
        {
            return NIL_PAPX;
        }

        if ( styleIndex >= _styleDescriptions.length )
        {
            return NIL_PAPX;
        }

        if ( _styleDescriptions[styleIndex] == null )
        {
            return NIL_PAPX;
        }

        if ( _styleDescriptions[styleIndex].getPAPX() == null )
        {
            return NIL_PAPX;
        }

        return _styleDescriptions[styleIndex].getPAPX();
    }
}
