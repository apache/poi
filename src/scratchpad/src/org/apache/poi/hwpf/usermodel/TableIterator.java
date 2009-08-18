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


public final class TableIterator
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
