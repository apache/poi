package org.apache.poi.hdf.event;

import org.apache.poi.hdf.model.hdftypes.ChpxNode;
import org.apache.poi.hdf.model.hdftypes.PapxNode;
import org.apache.poi.hdf.model.hdftypes.SepxNode;
import org.apache.poi.hdf.model.hdftypes.TextPiece;
import org.apache.poi.hdf.model.hdftypes.DocumentProperties;
import org.apache.poi.hdf.model.hdftypes.FontTable;
import org.apache.poi.hdf.model.hdftypes.ListTables;
import org.apache.poi.hdf.model.hdftypes.StyleSheet;

public interface HDFLowLevelParsingListener
{
  public void document(DocumentProperties dop);
  public void section(SepxNode sepx);
  public void paragraph(PapxNode papx);
  public void characterRun(ChpxNode chpx);
  public void text(TextPiece t);
  public void fonts(FontTable fontTbl);
  public void lists(ListTables listTbl);
  public void styleSheet(StyleSheet stsh);
}