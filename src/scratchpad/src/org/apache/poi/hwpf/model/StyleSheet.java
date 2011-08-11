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


  private final static ParagraphProperties NIL_PAP = new ParagraphProperties();
  private final static CharacterProperties NIL_CHP = new CharacterProperties();

    /**
     * Size of the STSHI structure
     */
    private int _cbStshi;

    /**
     * Length of STD Base as stored in a file
     * <p>
     * "The STD structure (see below) is divided into a fixed-length "base", and
     * a variable length part. The stshi.cbSTDBaseInFile indicates the size in
     * bytes of the fixed-length base of the STD as it was written in this file.
     * If the STD base is grown in a future version, the file format doesn't
     * change, because the style sheet reader can discard parts it doesn't know
     * about, or use defaults if the file's STD is not as large as it was
     * expecting. (Currently, stshi.cbSTDBaseInFile is 8.)"
     */
    private int _cbSTDBaseInFile;

    /**
     * First bit - Are built-in stylenames stored?
     * <p>
     * "Previous versions of Word did not store the style name if the style was
     * a built-in style; Word 6.0 stores the style name for compatibility with
     * future versions. Note: the built-in style names may need to be
     * "regenerated" if the file is opened in a different language or if
     * stshi.nVerBuiltInNamesWhenSaved doesn't match the expected value."
     * <p>
     * other - Spare flags
     */
    private int _flags;

    /**
     * Max sti known when this file was written
     * <p>
     * "This indicates the last built-in style known to the version of Word that
     * saved this file."
     */
    private int _stiMaxWhenSaved;

    /**
     * How many fixed-index istds are there?
     * <p>
     * "Each array of styles has some fixed-index styles at the beginning. This
     * indicates the number of fixed-index positions reserved in the style sheet
     * when it was saved."
     */
    private int _istdMaxFixedWhenSaved;

    /**
     * Current version of built-in stylenames
     * <p>
     * "Since built-in style names are saved with the document, this provides a
     * way to see if the saved names are the same "version" as the names in the
     * version of Word that is loading the file. If not, the built-in style
     * names need to be "regenerated", i.e. the old names need to be replaced
     * with the new."
     */
    private int nVerBuiltInNamesWhenSaved;

    /**
     * rgftc used by StandardChpStsh for document
     * <p>
     * "This is a list of the default fonts for this style sheet. The first is
     * for ASCII characters (0-127), the second is for East Asian characters,
     * and the third is the default font for non-East Asian, non-ASCII text. See
     * notes on sprmCRgftcX for details."
     */
    private int[] _rgftcStandardChpStsh;

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
        int cstd = LittleEndian.getUShort( tableStream, offset );
        offset += LittleEndian.SHORT_SIZE;

        _cbSTDBaseInFile = LittleEndian.getUShort( tableStream, offset );
        offset += LittleEndian.SHORT_SIZE;

        _flags = LittleEndian.getShort( tableStream, offset );
        offset += LittleEndian.SHORT_SIZE;

        _stiMaxWhenSaved = LittleEndian.getUShort( tableStream, offset );
        offset += LittleEndian.SHORT_SIZE;

        _istdMaxFixedWhenSaved = LittleEndian.getUShort( tableStream, offset );
        offset += LittleEndian.SHORT_SIZE;

        nVerBuiltInNamesWhenSaved = LittleEndian.getUShort( tableStream, offset );
        offset += LittleEndian.SHORT_SIZE;

        _rgftcStandardChpStsh = new int[3];
        _rgftcStandardChpStsh[0] = LittleEndian.getShort( tableStream, offset );
        offset += LittleEndian.SHORT_SIZE;
        _rgftcStandardChpStsh[1] = LittleEndian.getShort( tableStream, offset );
        offset += LittleEndian.SHORT_SIZE;
        _rgftcStandardChpStsh[2] = LittleEndian.getShort( tableStream, offset );
        offset += LittleEndian.SHORT_SIZE;

        // shall we discard cbLSD and mpstilsd?
        
      offset = startOffset + LittleEndian.SHORT_SIZE + _cbStshi;
      _styleDescriptions = new StyleDescription[cstd];
      for(int x = 0; x < cstd; x++)
      {
          int stdSize = LittleEndian.getShort(tableStream, offset);
          //get past the size
          offset += 2;
          if(stdSize > 0)
          {
              //byte[] std = new byte[stdSize];

              StyleDescription aStyle = new StyleDescription(tableStream,
                _cbSTDBaseInFile, offset, true);

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
    LittleEndian.putUShort(buf, offset, (short)_styleDescriptions.length);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putUShort(buf, offset, (short)_cbSTDBaseInFile);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putShort(buf, offset, (short)_flags);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putUShort(buf, offset, (short)_stiMaxWhenSaved);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putUShort(buf, offset, (short)_istdMaxFixedWhenSaved);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putUShort(buf, offset, (short)nVerBuiltInNamesWhenSaved);
    offset += LittleEndian.SHORT_SIZE;

    LittleEndian.putShort(buf, offset, (short)_rgftcStandardChpStsh[0]);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putShort(buf, offset, (short)_rgftcStandardChpStsh[1]);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putShort(buf, offset, (short)_rgftcStandardChpStsh[2]);

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

    if (ss._cbSTDBaseInFile == _cbSTDBaseInFile && ss._flags == _flags &&
        ss._istdMaxFixedWhenSaved ==_istdMaxFixedWhenSaved && ss._stiMaxWhenSaved == _stiMaxWhenSaved &&
        ss._rgftcStandardChpStsh[0] == _rgftcStandardChpStsh[0] && ss._rgftcStandardChpStsh[1] == _rgftcStandardChpStsh[1] &&
        ss._rgftcStandardChpStsh[2] == _rgftcStandardChpStsh[2] && ss._cbStshi == _cbStshi &&
        ss.nVerBuiltInNamesWhenSaved == nVerBuiltInNamesWhenSaved)
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

    if (x>=_styleDescriptions.length) {
        return NIL_CHP;
    }

    return (_styleDescriptions[x] != null ? _styleDescriptions[x].getCHP() : NIL_CHP);
  }

  public ParagraphProperties getParagraphStyle(int x)
  {
    if (x == NIL_STYLE) {
        return NIL_PAP;
    }

    if (x >= _styleDescriptions.length) {
      return NIL_PAP;
    }

    if (_styleDescriptions[x]==null) {
      return NIL_PAP;
    }

    if (_styleDescriptions[x].getPAP()==null) {
      return NIL_PAP;
    }

    return _styleDescriptions[x].getPAP();
  }
}
