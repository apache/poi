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

    private boolean _rowsFound = false;

    private int _tableLevel;

    Table( int startIdxInclusive, int endIdxExclusive, Range parent,
            int levelNum )
    {
        super( startIdxInclusive, endIdxExclusive, parent );
        _tableLevel = levelNum;
        initRows();
    }

    public TableRow getRow( int index )
    {
        initRows();
        return _rows.get( index );
    }

    public int getTableLevel()
    {
        return _tableLevel;
    }

    private void initRows()
    {
        if ( _rowsFound )
            return;

        _rows = new ArrayList<TableRow>();
        int rowStart = 0;
        int rowEnd = 0;

        int numParagraphs = numParagraphs();
        while ( rowEnd < numParagraphs )
        {
            Paragraph startRowP = getParagraph( rowStart );
            Paragraph endRowP = getParagraph( rowEnd );
            rowEnd++;
            if ( endRowP.isTableRowEnd()
                    && endRowP.getTableLevel() == _tableLevel )
            {
                _rows.add( new TableRow( startRowP.getStartOffset(), endRowP
                        .getEndOffset(), this, _tableLevel ) );
                rowStart = rowEnd;
            }
        }
        _rowsFound = true;
    }

    public int numRows()
    {
        initRows();
        return _rows.size();
    }

    @Override
    protected void reset()
    {
        _rowsFound = false;
    }

    public int type()
    {
        return TYPE_TABLE;
    }
}