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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.POITestCase;
import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.record.ArrayRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.hssf.record.aggregates.RowRecordsAggregate;
import org.apache.poi.hssf.record.aggregates.SharedValueManager;
import org.apache.poi.ss.usermodel.BaseTestSheetUpdateArrayFormulas;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Test;

/**
 * Test array formulas in HSSF
 */
final class TestHSSFSheetUpdateArrayFormulas extends BaseTestSheetUpdateArrayFormulas {

    public TestHSSFSheetUpdateArrayFormulas() {
        super(HSSFITestDataProvider.instance);
    }

    // Test methods common with XSSF are in superclass
    // Local methods here test HSSF-specific details of updating array formulas
    @Test
    void testHSSFSetArrayFormula_singleCell() throws IOException {
        try (HSSFWorkbook workbook = new HSSFWorkbook()) {
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

            FormulaRecordAggregate agg = (FormulaRecordAggregate) cell.getCellValueRecord();
            assertEquals(range.formatAsString(), agg.getArrayFormulaRange().formatAsString());
            assertTrue(agg.isPartOfArrayFormula());
        }
    }

    /**
     * Makes sure the internal state of HSSFSheet is consistent after removing array formulas
     */
    @Test
    void testAddRemoveArrayFormulas_recordUpdates() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet s = wb.createSheet("Sheet1");

            CellRange<HSSFCell> cr = s.setArrayFormula("123", CellRangeAddress.valueOf("B5:C6"));
            List<org.apache.poi.hssf.record.Record> recs = new ArrayList<>();
            s.getSheet().visitContainedRecords(recs::add, 0);

            int ix = 0;
            for (org.apache.poi.hssf.record.Record r : recs) {
                if (r instanceof ArrayRecord) break;
                ix++;
            }

            assertNotEquals(ix, recs.size());
            Class<?>[] act = recs.subList(ix-1, ix+4).stream().map(Object::getClass).toArray(Class[]::new);
            Class<?>[] exp = { FormulaRecord.class, ArrayRecord.class, FormulaRecord.class, FormulaRecord.class, FormulaRecord.class };
            assertArrayEquals(exp, act);

            // just one array record
            assertFalse(recs.subList(ix + 1, recs.size()).stream().anyMatch(r -> r instanceof ArrayRecord));

            s.removeArrayFormula(cr.getTopLeftCell());

            // Make sure the array formula has been removed properly
            recs.clear();
            s.getSheet().visitContainedRecords(recs::add, 0);

            assertFalse(recs.stream().anyMatch(r -> r instanceof ArrayRecord || r instanceof FormulaRecord));
            RowRecordsAggregate rra = s.getSheet().getRowsAggregate();

            SharedValueManager svm = POITestCase.getFieldValue(RowRecordsAggregate.class, rra, SharedValueManager.class, "_sharedValueManager");
            assertNull(svm.getArrayRecord(4, 1), "Array record was not cleaned up properly.");
        }
    }
}
