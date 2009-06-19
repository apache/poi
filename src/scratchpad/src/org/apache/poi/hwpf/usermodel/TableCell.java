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

public final class TableCell
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

  /** Returns the TableCellDescriptor for this cell.*/
  public TableCellDescriptor getDescriptor(){
  	return _tcd;
  }

}
