package org.apache.poi.hwpf.usermodel;

public class TableCell
  extends Range
{
  private int _levelNum;
  private TableCellDescriptor _tcd;
  private int _leftEdge;
  private int _width;

  public TableCell(int startIdx, int endIdx, TableRow parent, int levelNum, TableCellDescriptor tcd, int leftEdge, int width)
  {
    super(startIdx, endIdx, Range.TYPE_PARAGRAPH, parent);
    _tcd = tcd;
    _leftEdge = leftEdge;
    _width = width;
    _levelNum = levelNum;
  }

  public boolean isFirstMerged()
  {
    return _tcd.isFFirstMerged();
  }

  public boolean isMerged()
  {
    return _tcd.isFMerged();
  }

  public boolean isVertical()
  {
    return _tcd.isFVertical();
  }

  public boolean isBackward()
  {
    return _tcd.isFBackward();
  }

    public boolean isRotateFont()
  {
    return _tcd.isFRotateFont();
  }

  public boolean isVerticallyMerged()
  {
    return _tcd.isFVertMerge();
  }

  public boolean isFirstVerticallyMerged()
  {
    return _tcd.isFVertRestart();
  }

  public byte getVertAlign()
  {
    return _tcd.getVertAlign();
  }

  public BorderCode getBrcTop()
  {
    return _tcd.getBrcTop();
  }

  public BorderCode getBrcBottom()
  {
    return _tcd.getBrcBottom();
  }

  public BorderCode getBrcLeft()
  {
    return _tcd.getBrcLeft();
  }

  public BorderCode getBrcRight()
  {
    return _tcd.getBrcRight();
  }

  public int getLeftEdge() // twips
  {
    return _leftEdge;
  }

  public int getWidth() // twips
  {
    return _width;
  }

}
