package org.apache.poi.hwpf.usermodel;


import java.util.ArrayList;

public class TableIterator
{
  Range _range;
  int _index;
  int _levelNum;

  TableIterator(Range range, int levelNum)
  {
    _range = range;
    _index = 0;
    _levelNum = levelNum;
  }

  public TableIterator(Range range)
  {
    this(range, 1);
  }


  public boolean hasNext()
  {
    int numParagraphs = _range.numParagraphs();
    for (;_index < numParagraphs; _index++)
    {
      Paragraph paragraph = _range.getParagraph(_index);
      if (paragraph.isInTable() && paragraph.getTableLevel() == _levelNum)
      {
        return true;
      }
    }
    return false;
  }

  public Table next()
  {
    int numParagraphs = _range.numParagraphs();
    int numRows = 0;
    int startIndex = _index;
    int endIndex = _index;

    for (;_index < numParagraphs; _index++)
    {
      Paragraph paragraph = _range.getParagraph(_index);
      if (!paragraph.isInTable() || paragraph.getTableLevel() < _levelNum)
      {
        endIndex = _index;
        break;
      }
    }
    return new Table(startIndex, endIndex, _range, _levelNum);
  }

}
