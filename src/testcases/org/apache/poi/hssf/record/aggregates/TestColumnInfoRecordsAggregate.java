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

package org.apache.poi.hssf.record.aggregates;

import junit.framework.TestCase;
import org.apache.poi.hssf.record.ColumnInfoRecord;

/**
 * @author Glen Stampoultzis
 */
public final class TestColumnInfoRecordsAggregate extends TestCase
{
    ColumnInfoRecordsAggregate columnInfoRecordsAggregate;

    public void testGetRecordSize() throws Exception
    {
        columnInfoRecordsAggregate = new ColumnInfoRecordsAggregate();
        columnInfoRecordsAggregate.insertColumn( createColumn( (short)1, (short)3 ));
        columnInfoRecordsAggregate.insertColumn( createColumn( (short)4, (short)7 ));
        columnInfoRecordsAggregate.insertColumn( createColumn( (short)8, (short)8 ));
//        columnInfoRecordsAggregate.setColumn( (short)2, new Short( (short)200 ), new Integer( 1 ), new Boolean( true ), null);
        columnInfoRecordsAggregate.groupColumnRange( (short)2, (short)5, true );
        assertEquals(6, columnInfoRecordsAggregate.getNumColumns());

        assertEquals(columnInfoRecordsAggregate.getRecordSize(), columnInfoRecordsAggregate.serialize().length);

        columnInfoRecordsAggregate = new ColumnInfoRecordsAggregate();
        columnInfoRecordsAggregate.groupColumnRange( (short)3, (short)6, true );

        assertEquals(columnInfoRecordsAggregate.getRecordSize(), serializedSize());
    }

    private int serializedSize()
    {
        return columnInfoRecordsAggregate.serialize(0, new byte[columnInfoRecordsAggregate.getRecordSize()]);
    }

    private ColumnInfoRecord createColumn( short firstCol, short lastCol )
    {
        ColumnInfoRecord columnInfoRecord = new ColumnInfoRecord( );
        columnInfoRecord.setFirstColumn(firstCol);
        columnInfoRecord.setLastColumn(lastCol);
        return columnInfoRecord;
    }
}