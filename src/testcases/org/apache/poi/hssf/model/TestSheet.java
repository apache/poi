
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.hssf.model;

import junit.framework.TestCase;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.DimensionsRecord;
import org.apache.poi.hssf.record.aggregates.RowRecordsAggregate;
import org.apache.poi.hssf.record.aggregates.ValueRecordsAggregate;
import org.apache.poi.hssf.record.aggregates.ColumnInfoRecordsAggregate;

import java.util.List;
import java.util.ArrayList;

/**
 * Unit test for the Sheet class.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestSheet extends TestCase
{
    public void testCreateSheet() throws Exception
    {
        // Check we're adding row and cell aggregates
        List records = new ArrayList();
        records.add( new BOFRecord() );
        records.add( new DimensionsRecord() );
        records.add( new EOFRecord() );
        Sheet sheet = Sheet.createSheet( records, 0, 0 );

        int pos = 0;
        assertTrue( sheet.records.get(pos++) instanceof BOFRecord );
        assertTrue( sheet.records.get(pos++) instanceof ColumnInfoRecordsAggregate );
        assertTrue( sheet.records.get(pos++) instanceof DimensionsRecord );
        assertTrue( sheet.records.get(pos++) instanceof RowRecordsAggregate );
        assertTrue( sheet.records.get(pos++) instanceof ValueRecordsAggregate );
        assertTrue( sheet.records.get(pos++) instanceof EOFRecord );
    }

}
