package org.apache.poi.hdf.event;

import org.apache.poi.hdf.model.hdftypes.SectionProperties;
import org.apache.poi.hdf.model.hdftypes.CharacterProperties;
import org.apache.poi.hdf.model.hdftypes.ParagraphProperties;
import org.apache.poi.hdf.model.hdftypes.TableProperties;
import org.apache.poi.hdf.model.hdftypes.DocumentProperties;

public interface HDFParsingListener
{
  public void document(DocumentProperties dop);
  public void section(SectionProperties sep, int start, int end);
  public void paragraph(ParagraphProperties pap, int start, int end);
  public void listEntry(String bulletText, CharacterProperties bulletProperties, ParagraphProperties pap, int start, int end);
  public void paragraphInTableRow(ParagraphProperties pap, int start, int end);
  public void characterRun(CharacterProperties chp, String text, int start, int end);
  public void tableRowEnd(TableProperties tap, int start, int end);
  public void header(int sectionIndex, int type);
  public void footer(int sectionIndex, int type);
}