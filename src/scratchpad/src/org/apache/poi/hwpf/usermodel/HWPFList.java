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

package org.apache.poi.hwpf.usermodel;

import org.apache.poi.hwpf.model.ListLevel;
import org.apache.poi.hwpf.model.ListData;
import org.apache.poi.hwpf.model.ListFormatOverride;
import org.apache.poi.hwpf.model.StyleSheet;

import org.apache.poi.hwpf.sprm.CharacterSprmCompressor;
import org.apache.poi.hwpf.sprm.ParagraphSprmCompressor;

/**
 * This class is used to create a list in a Word document. It is used in
 * conjunction with {@link
 * org.apache.poi.hwpf.HWPFDocument#registerList(HWPFList) registerList} in
 * {@link org.apache.poi.hwpf.HWPFDocument HWPFDocument}.
 *
 * In Word, lists are not ranged entities, meaning you can't actually add one
 * to the document. Lists only act as properties for list entries. Once you
 * register a list, you can add list entries to a document that are a part of
 * the list.
 *
 * The only benefit of this that I see, is that you can add a list entry
 * anywhere in the document and continue numbering from the previous list.
 *
 * @author Ryan Ackley
 */
public final class HWPFList {
  private ListData _listData;
  private ListFormatOverride _override;
  private boolean _registered;
  private StyleSheet _styleSheet;

  /**
   *
   * @param numbered true if the list should be numbered; false if it should be
   *        bulleted.
   * @param styleSheet The document's stylesheet.
   */
  public HWPFList(boolean numbered, StyleSheet styleSheet)
  {
    _listData = new ListData((int)(Math.random() * System.currentTimeMillis()), numbered);
    _override = new ListFormatOverride(_listData.getLsid());
    _styleSheet = styleSheet;
  }

  /**
   * Sets the character properties of the list numbers.
   *
   * @param level the level number that the properties should apply to.
   * @param chp The character properties.
   */
  public void setLevelNumberProperties(int level, CharacterProperties chp)
  {
    ListLevel listLevel = _listData.getLevel(level);
    int styleIndex = _listData.getLevelStyle(level);
    CharacterProperties base = _styleSheet.getCharacterStyle(styleIndex);

    byte[] grpprl = CharacterSprmCompressor.compressCharacterProperty(chp, base);
    listLevel.setNumberProperties(grpprl);
  }

  /**
   * Sets the paragraph properties for a particular level of the list.
   *
   * @param level The level number.
   * @param pap The paragraph properties
   */
  public void setLevelParagraphProperties(int level, ParagraphProperties pap)
  {
    ListLevel listLevel = _listData.getLevel(level);
    int styleIndex = _listData.getLevelStyle(level);
    ParagraphProperties base = _styleSheet.getParagraphStyle(styleIndex);

    byte[] grpprl = ParagraphSprmCompressor.compressParagraphProperty(pap, base);
    listLevel.setLevelProperties(grpprl);
  }

  public void setLevelStyle(int level, int styleIndex)
  {
    _listData.setLevelStyle(level, styleIndex);
  }

  public ListData getListData()
  {
    return _listData;
  }

  public ListFormatOverride getOverride()
  {
    return _override;
  }

}
