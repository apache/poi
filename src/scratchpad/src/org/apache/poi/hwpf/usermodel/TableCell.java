package org.apache.poi.hwpf.usermodel;


public class TableCell
  extends Range
{
  int _levelNum;

  public TableCell(int startIdx, int endIdx, TableRow parent, int levelNum, TableCellDescriptor tcd)
  {
    super(startIdx, endIdx, Range.PARAGRAPH_INDEX, parent);
    _levelNum = levelNum;
  }


}
