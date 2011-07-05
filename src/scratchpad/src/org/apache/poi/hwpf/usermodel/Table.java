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

import java.util.ArrayList;

public final class Table extends Range
{
    private ArrayList<TableRow> _rows;

    private int _tableLevel;

    Table( int startIdx, int endIdx, Range parent, int levelNum )
    {
        super( startIdx, endIdx, Range.TYPE_PARAGRAPH, parent );
        _rows = new ArrayList<TableRow>();
        _tableLevel = levelNum;

        int rowStart = 0;
        int rowEnd = 0;

        int numParagraphs = numParagraphs();
        while ( rowEnd < numParagraphs )
        {
            Paragraph p = getParagraph( rowEnd );
            rowEnd++;
            if ( p.isTableRowEnd() && p.getTableLevel() == levelNum )
            {
                _rows.add( new TableRow( rowStart, rowEnd, this, levelNum ) );
                rowStart = rowEnd;
            }
        }
    }

    public TableRow getRow( int index )
    {
        return _rows.get( index );
    }

    public int getTableLevel()
    {
        return _tableLevel;
    }

    public int numRows()
    {
        return _rows.size();
    }

    public int type()
    {
        return TYPE_TABLE;
    }
}