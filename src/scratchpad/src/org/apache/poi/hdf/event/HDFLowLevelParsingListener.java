package org.apache.poi.hdf.event;

import org.apache.poi.hdf.model.hdftypes.ChpxNode;
import org.apache.poi.hdf.model.hdftypes.PapxNode;
import org.apache.poi.hdf.model.hdftypes.SepxNode;
import org.apache.poi.hdf.model.hdftypes.TextPiece;
import org.apache.poi.hdf.model.hdftypes.DocumentProperties;

public interface HDFLowLevelParsingListener
{
  public void document(DocumentProperties dop);
  public void section(SepxNode sepx);
  public void paragraph(PapxNode papx);
  public void characterRun(ChpxNode chpx);
  public void text(TextPiece t);
}