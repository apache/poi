package org.apache.poi.hwpf.usermodel;

import java.util.ArrayList;

public class Table
  extends Range
{
  ArrayList _rows;

  Table(int startIdx, int endIdx, Range parent, int levelNum)
  {
    super(startIdx, endIdx, Range.PARAGRAPH_INDEX, parent);
    _rows = new ArrayList();
    int numParagraphs = numParagraphs();

    int rowStart = 0;
    int rowEnd = 0;

    while (rowEnd < numParagraphs)
    {
      Paragraph p = getParagraph(rowEnd);
      rowEnd++;
      if (p.isTableRowEnd() && p.getTableLevel() == levelNum)
      {
        _rows.add(new TableRow(rowStart, rowEnd, this, levelNum));
        rowStart = rowEnd;
      }
    }
  }

  public int numRows()
  {
    return _rows.size();
  }

  public TableRow getRow(int index)
  {
    return (TableRow)_rows.get(index);
  }
}
