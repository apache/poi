package org.apache.poi.hwpf.usermodel;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.ListTables;
import org.apache.poi.hwpf.model.ListLevel;
import org.apache.poi.hwpf.model.ListData;
import org.apache.poi.hwpf.model.ListFormatOverride;
import org.apache.poi.hwpf.model.StyleSheet;

import org.apache.poi.hwpf.sprm.CharacterSprmCompressor;
import org.apache.poi.hwpf.sprm.ParagraphSprmCompressor;


public class List
{
  private ListData _listData;
  private ListFormatOverride _override;
  private boolean _registered;
  private StyleSheet _styleSheet;

  public List(boolean numbered, StyleSheet styleSheet)
  {
    _listData = new ListData((int)(Math.random() * (double)System.currentTimeMillis()), numbered);
    _override = new ListFormatOverride(_listData.getLsid());
    _styleSheet = styleSheet;
  }

  public void setLevelNumberProperties(int level, CharacterProperties chp)
  {
    ListLevel listLevel = _listData.getLevel(level);
    int styleIndex = _listData.getLevelStyle(level);
    CharacterProperties base = _styleSheet.getCharacterStyle(styleIndex);

    byte[] grpprl = CharacterSprmCompressor.compressCharacterProperty(chp, base);
    listLevel.setNumberProperties(grpprl);
  }

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
