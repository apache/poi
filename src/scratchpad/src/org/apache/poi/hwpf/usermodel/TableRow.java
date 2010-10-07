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

import org.apache.poi.hwpf.sprm.TableSprmUncompressor;

public final class TableRow
  extends Paragraph
{
  private final static char TABLE_CELL_MARK = '\u0007';

  private final static short SPRM_TJC = 0x5400;
  private final static short SPRM_DXAGAPHALF = (short)0x9602;
  private final static short SPRM_FCANTSPLIT = 0x3403;
  private final static short SPRM_FTABLEHEADER = 0x3404;
  private final static short SPRM_DYAROWHEIGHT = (short)0x9407;

  int _levelNum;
  private TableProperties _tprops;
  private TableCell[] _cells;

  public TableRow(int startIdx, int endIdx, Table parent, int levelNum)
  {
    super(startIdx, endIdx, parent);

    _tprops = TableSprmUncompressor.uncompressTAP(_papx.toByteArray(), 2);
    _levelNum = levelNum;
    _cells = new TableCell[_tprops.getItcMac()];

    int start = 0;
    int end = 0;

    for (int cellIndex = 0; cellIndex < _cells.length; cellIndex++)
    {
      Paragraph p = getParagraph(start);
      String s = p.text();

      while (! ( (s.charAt(s.length() - 1) == TABLE_CELL_MARK) ||
                p.isEmbeddedCellMark() && p.getTableLevel() == levelNum))
      {
        end++;
        p = getParagraph(end);
        s = p.text();
      }

      // Create it for the correct paragraph range
      _cells[cellIndex] = new TableCell(start, end, this, levelNum,
                                        _tprops.getRgtc()[cellIndex],
                                        _tprops.getRgdxaCenter()[cellIndex],
                                        _tprops.getRgdxaCenter()[cellIndex+1]-_tprops.getRgdxaCenter()[cellIndex]);
      // Now we've decided where everything is, tweak the
      //  record of the paragraph end so that the
      //  paragraph level counts work
      // This is a bit hacky, we really need a better fix...
      _cells[cellIndex]._parEnd++;
      
      // Next!
      end++;
      start = end;
    }
  }

  public int getRowJustification()
  {
    return _tprops.getJc();
  }

  public void setRowJustification(int jc)
  {
    _tprops.setJc(jc);
    _papx.updateSprm(SPRM_TJC, (short)jc);
  }

  public int getGapHalf()
  {
    return _tprops.getDxaGapHalf();
  }

  public void setGapHalf(int dxaGapHalf)
  {
    _tprops.setDxaGapHalf(dxaGapHalf);
    _papx.updateSprm(SPRM_DXAGAPHALF, (short)dxaGapHalf);
  }

  public int getRowHeight()
  {
    return _tprops.getDyaRowHeight();
  }

  public void setRowHeight(int dyaRowHeight)
  {
    _tprops.setDyaRowHeight(dyaRowHeight);
    _papx.updateSprm(SPRM_DYAROWHEIGHT, (short)dyaRowHeight);
  }

  public boolean cantSplit()
  {
    return _tprops.getFCantSplit();
  }

  public void setCantSplit(boolean cantSplit)
  {
    _tprops.setFCantSplit(cantSplit);
    _papx.updateSprm(SPRM_FCANTSPLIT, (byte)(cantSplit ? 1 : 0));
  }

  public boolean isTableHeader()
  {
    return _tprops.getFTableHeader();
  }

  public void setTableHeader(boolean tableHeader)
  {
    _tprops.setFTableHeader(tableHeader);
    _papx.updateSprm(SPRM_FTABLEHEADER, (byte)(tableHeader ? 1 : 0));
  }

  public int numCells()
  {
    return _cells.length;
  }

  public TableCell getCell(int index)
  {
    return _cells[index];
  }
  
  public BorderCode getTopBorder() {
    return _tprops.getBrcBottom();
  }
  
  public BorderCode getBottomBorder() {
    return _tprops.getBrcBottom();
  }
  
  public BorderCode getLeftBorder() {
    return _tprops.getBrcLeft();
  }
  
  public BorderCode getRightBorder() {
    return _tprops.getBrcRight();
  }
  
  public BorderCode getHorizontalBorder() {
    return _tprops.getBrcHorizontal();
  }
  
  public BorderCode getVerticalBorder() {
    return _tprops.getBrcVertical();
  }
  
  public BorderCode getBarBorder() {
    throw new UnsupportedOperationException("not applicable for TableRow");
  }
  
}
