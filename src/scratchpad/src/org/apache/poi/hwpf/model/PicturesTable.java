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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Picture;

import java.util.List;
import java.util.ArrayList;


/**
 * Holds information about all pictures embedded in Word Document either via "Insert -> Picture -> From File" or via
 * clipboard. Responsible for images extraction and determining whether some document�s piece contains embedded image.
 * Analyzes raw data bytestream �Data� (where Word stores all embedded objects) provided by HWPFDocument.
 *
 * Word stores images as is within so called "Data stream" - the stream within a Word docfile containing various data
 * that hang off of characters in the main stream. For example, binary data describing in-line pictures and/or
 * formfields an also embedded objects-native data. Word picture structures are concatenated one after the other in
 * the data stream if the document contains pictures.
 * Data stream is easily reachable via HWPFDocument._dataStream property.
 * A picture is represented in the document text stream as a special character, an Unicode \u0001 whose
 * CharacterRun.isSpecial() returns true. The file location of the picture in the Word binary file is accessed
 * via CharacterRun.getPicOffset(). The CharacterRun.getPicOffset() is a byte offset into the data stream.
 * Beginning at the position recorded in picOffset, a header data structure, will be stored.
 *
 * @author Dmitry Romanov
 */
public class PicturesTable
{
  static final int TYPE_IMAGE = 0x08;
  static final int TYPE_IMAGE_WORD2000 = 0x00;
  static final int TYPE_IMAGE_PASTED_FROM_CLIPBOARD = 0xA;
  static final int TYPE_IMAGE_PASTED_FROM_CLIPBOARD_WORD2000 = 0x2;
  static final int TYPE_HORIZONTAL_LINE = 0xE;
  static final int BLOCK_TYPE_OFFSET = 0xE;
  static final int MM_MODE_TYPE_OFFSET = 0x6;

  private byte[] _dataStream;

  /** @link dependency
   * @stereotype instantiate*/
  /*# Picture lnkPicture; */

  /**
   *
   * @param _dataStream
   */
  public PicturesTable(byte[] _dataStream)
  {
    this._dataStream = _dataStream;
  }

  /**
   * determines whether specified CharacterRun contains reference to a picture
   * @param run
   */
  public boolean hasPicture(CharacterRun run) {
    if (run.isSpecialCharacter() && !run.isObj() && !run.isOle2() && !run.isData() && "\u0001".equals(run.text())) {
      return isBlockContainsImage(run.getPicOffset());
    }
    return false;
  }

  /**
   * determines whether specified CharacterRun contains reference to a picture
   * @param run
  */
  public boolean hasHorizontalLine(CharacterRun run) {
    if (run.isSpecialCharacter() && "\u0001".equals(run.text())) {
      return isBlockContainsHorizontalLine(run.getPicOffset());
    }
    return false;
  }

  private boolean isPictureRecognized(short blockType, short mappingModeOfMETAFILEPICT) {
    return (blockType == TYPE_IMAGE || blockType == TYPE_IMAGE_PASTED_FROM_CLIPBOARD || (blockType==TYPE_IMAGE_WORD2000 && mappingModeOfMETAFILEPICT==0x64) || (blockType==TYPE_IMAGE_PASTED_FROM_CLIPBOARD_WORD2000 && mappingModeOfMETAFILEPICT==0x64));
  }

  private static short getBlockType(byte[] dataStream, int pictOffset) {
    return LittleEndian.getShort(dataStream, pictOffset + BLOCK_TYPE_OFFSET);
  }

  private static short getMmMode(byte[] dataStream, int pictOffset) {
    return LittleEndian.getShort(dataStream, pictOffset + MM_MODE_TYPE_OFFSET);
  }

  /**
   * Returns picture object tied to specified CharacterRun
   * @param run
   * @param fillBytes if true, Picture will be returned with filled byte array that represent picture's contents. If you don't want
   * to have that byte array in memory but only write picture's contents to stream, pass false and then use Picture.writeImageContent
   * @see Picture#writeImageContent(java.io.OutputStream)
   * @return a Picture object if picture exists for specified CharacterRun, null otherwise. PicturesTable.hasPicture is used to determine this.
   * @see #hasPicture(org.apache.poi.hwpf.usermodel.CharacterRun) 
   */
  public Picture extractPicture(CharacterRun run, boolean fillBytes) {
    if (hasPicture(run)) {
      return new Picture(run.getPicOffset(), _dataStream, fillBytes);
    }
    return null;
  }

  /**
   * @return a list of Picture objects found in current document
   */
  public List getAllPictures() {
    ArrayList pictures = new ArrayList();
    
    int pos = 0;
    boolean atEnd = false;
    
    while(pos<_dataStream.length && !atEnd) {
      if (isBlockContainsImage(pos)) {
        pictures.add(new Picture(pos, _dataStream, false));
      }
      
      int skipOn = LittleEndian.getInt(_dataStream, pos);
      if(skipOn <= 0) { atEnd = true; }
      pos += skipOn;
    }
    
    return pictures;
  }

  private boolean isBlockContainsImage(int i)
  {
    return isPictureRecognized(getBlockType(_dataStream, i), getMmMode(_dataStream, i));
  }

  private boolean isBlockContainsHorizontalLine(int i)
  {
    return getBlockType(_dataStream, i)==TYPE_HORIZONTAL_LINE && getMmMode(_dataStream, i)==0x64;
  }

}
