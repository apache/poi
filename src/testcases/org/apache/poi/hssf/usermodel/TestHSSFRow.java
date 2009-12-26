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
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.ss.usermodel.BaseTestRow;
import org.apache.poi.ss.SpreadsheetVersion;

/**
 * Test HSSFRow is okay.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestHSSFRow extends BaseTestRow {

    public TestHSSFRow() {
        super(HSSFITestDataProvider.instance);
    }

    public void testRowBounds() {
        baseTestRowBounds(SpreadsheetVersion.EXCEL97.getLastRowIndex());
    }

    public void testCellBounds() {
        baseTestCellBounds(SpreadsheetVersion.EXCEL97.getLastColumnIndex());
    }

    public void testLastAndFirstColumns_bug46654() {
        int ROW_IX = 10;
        int COL_IX = 3;
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Sheet1");
        RowRecord rowRec = new RowRecord(ROW_IX);
        rowRec.setFirstCol((short)2);
        rowRec.setLastCol((short)5);

        BlankRecord br = new BlankRecord();
        br.setRow(ROW_IX);
        br.setColumn((short)COL_IX);

        sheet.getSheet().addValueRecord(ROW_IX, br);
        HSSFRow row = new HSSFRow(workbook, sheet, rowRec);
        HSSFCell cell = row.createCellFromRecord(br);

        if (row.getFirstCellNum() == 2 && row.getLastCellNum() == 5) {
            throw new AssertionFailedError("Identified bug 46654a");
        }
        assertEquals(COL_IX, row.getFirstCellNum());
        assertEquals(COL_IX + 1, row.getLastCellNum());
        row.removeCell(cell);
        assertEquals(-1, row.getFirstCellNum());
        assertEquals(-1, row.getLastCellNum());
    }

    public void testMoveCell() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        HSSFRow row = sheet.createRow(0);
        HSSFRow rowB = sheet.createRow(1);

        HSSFCell cellA2 = rowB.createCell(0);
        assertEquals(0, rowB.getFirstCellNum());
        assertEquals(0, rowB.getFirstCellNum());

        assertEquals(-1, row.getLastCellNum());
        assertEquals(-1, row.getFirstCellNum());
        HSSFCell cellB2 = row.createCell(1);
        row.createCell(2); // C2
        row.createCell(3); // D2

        assertEquals(1, row.getFirstCellNum());
        assertEquals(4, row.getLastCellNum());

        // Try to move to somewhere else that's used
        try {
            row.moveCell(cellB2, (short)3);
            fail("IllegalArgumentException should have been thrown");
        } catch(IllegalArgumentException e) {
            // expected during successful test
        }

        // Try to move one off a different row
        try {
            row.moveCell(cellA2, (short)3);
            fail("IllegalArgumentException should have been thrown");
        } catch(IllegalArgumentException e) {
            // expected during successful test
        }

        // Move somewhere spare
        assertNotNull(row.getCell(1));
        row.moveCell(cellB2, (short)5);
        assertNull(row.getCell(1));
        assertNotNull(row.getCell(5));

        assertEquals(5, cellB2.getColumnIndex());
        assertEquals(2, row.getFirstCellNum());
        assertEquals(6, row.getLastCellNum());
    }
}
