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

package org.apache.poi.hssf.usermodel;

import junit.framework.AssertionFailedError;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.record.ArrayRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.hssf.record.aggregates.RowRecordsAggregate;
import org.apache.poi.hssf.record.aggregates.SharedValueManager;
import org.apache.poi.hssf.record.aggregates.TestSharedValueManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Test array formulas in HSSF
 *
 * @author Yegor Kozlov
 * @author Josh Micich
 */
public final class TestHSSFSheetUpdateArrayFormulas extends BaseTestSheetUpdateArrayFormulas {

    public TestHSSFSheetUpdateArrayFormulas() {
        super(HSSFITestDataProvider.instance);
    }

    // Test methods common with XSSF are in superclass
    // Local methods here test HSSF-specific details of updating array formulas

    public void testHSSFSetArrayFormula_singleCell() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Sheet1");

        CellRangeAddress range = new CellRangeAddress(2, 2, 2, 2);
        HSSFCell[] cells = sheet.setArrayFormula("SUM(C11:C12*D11:D12)", range).getFlattenedCells();
        assertEquals(1, cells.length);

        // sheet.setArrayFormula creates rows and cells for the designated range
        assertNotNull(sheet.getRow(2));
        HSSFCell cell = sheet.getRow(2).getCell(2);
        assertNotNull(cell);

        assertTrue(cell.isPartOfArrayFormulaGroup());
        //retrieve the range and check it is the same
        assertEquals(range.formatAsString(), cell.getArrayFormulaRange().formatAsString());

        FormulaRecordAggregate agg = (FormulaRecordAggregate)cell.getCellValueRecord();
        assertEquals(range.formatAsString(), agg.getArrayFormulaRange().formatAsString());
        assertTrue(agg.isPartOfArrayFormula());
    }

    /**
     * Makes sure the internal state of HSSFSheet is consistent after removing array formulas
     */
    public void testAddRemoveArrayFormulas_recordUpdates() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet("Sheet1");

        CellRange<HSSFCell> cr = s.setArrayFormula("123", CellRangeAddress.valueOf("B5:C6"));
        Record[] recs;
        int ix;
        recs = RecordInspector.getRecords(s, 0);
        ix = findRecordOfType(recs, ArrayRecord.class, 0);
        confirmRecordClass(recs, ix-1, FormulaRecord.class);
        confirmRecordClass(recs, ix+1, FormulaRecord.class);
        confirmRecordClass(recs, ix+2, FormulaRecord.class);
        confirmRecordClass(recs, ix+3, FormulaRecord.class);
        // just one array record
        assertTrue(findRecordOfType(recs, ArrayRecord.class, ix+1) < 0);

        s.removeArrayFormula(cr.getTopLeftCell());

        // Make sure the array formula has been removed properly

        recs = RecordInspector.getRecords(s, 0);
        assertTrue(findRecordOfType(recs, ArrayRecord.class, 0) < 0);
        assertTrue(findRecordOfType(recs, FormulaRecord.class, 0) < 0);
        RowRecordsAggregate rra = s.getSheet().getRowsAggregate();
        SharedValueManager svm = TestSharedValueManager.extractFromRRA(rra);
        if (svm.getArrayRecord(4, 1) != null) {
            throw new AssertionFailedError("Array record was not cleaned up properly.");
        }
    }

    private static void confirmRecordClass(Record[] recs, int index, Class<? extends Record> cls) {
        if (recs.length <= index) {
            throw new AssertionFailedError("Expected (" + cls.getName() + ") at index "
                    + index + " but array length is " + recs.length + ".");
        }
        assertEquals(cls, recs[index].getClass());
    }
    /**
     * @return <tt>-1<tt> if not found
     */
    private static int findRecordOfType(Record[] recs, Class<?> type, int fromIndex) {
        for (int i=fromIndex; i<recs.length; i++) {
            if (type.isInstance(recs[i])) {
                return i;
            }
        }
        return -1;
    }
}
