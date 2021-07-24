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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;

/**
 * Test HSSFRow is okay.
 */
final class TestHSSFRow extends BaseTestRow {

    public TestHSSFRow() {
        super(HSSFITestDataProvider.instance);
    }

    @Test
    void testLastAndFirstColumns_bug46654() throws IOException {
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

        assertFalse(row.getFirstCellNum() == 2 && row.getLastCellNum() == 5, "Identified bug 46654a");

        assertEquals(COL_IX, row.getFirstCellNum());
        assertEquals(COL_IX + 1, row.getLastCellNum());
        row.removeCell(cell);
        assertEquals(-1, row.getFirstCellNum());
        assertEquals(-1, row.getLastCellNum());

        workbook.close();
    }

    @Test
    void testMoveCell() throws IOException {
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
        assertThrows(IllegalArgumentException.class, () -> row.moveCell(cellB2, (short)3));

        // Try to move one off a different row
        assertThrows(IllegalArgumentException.class, () -> row.moveCell(cellA2, (short)3));

        // Move somewhere spare
        assertNotNull(row.getCell(1));
        row.moveCell(cellB2, (short)5);
        assertNull(row.getCell(1));
        assertNotNull(row.getCell(5));

        assertEquals(5, cellB2.getColumnIndex());
        assertEquals(2, row.getFirstCellNum());
        assertEquals(6, row.getLastCellNum());

        workbook.close();
    }

    @Override
    @Test
    protected void testRowHeight() throws IOException{
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        HSSFRow row = sheet.createRow(0);

        assertEquals(row.getHeight(), sheet.getDefaultRowHeight());
        assertFalse(row.getRowRecord().getBadFontHeight());

        row.setHeight((short) 123);
        assertEquals(123, row.getHeight());
        assertTrue(row.getRowRecord().getBadFontHeight());

        row.setHeight((short) -1);
        assertEquals(row.getHeight(), sheet.getDefaultRowHeight());
        assertFalse(row.getRowRecord().getBadFontHeight());

        row.setHeight((short) 123);
        assertEquals(123, row.getHeight());
        assertTrue(row.getRowRecord().getBadFontHeight());

        row.setHeightInPoints(-1);
        assertEquals(row.getHeight(), sheet.getDefaultRowHeight());
        assertFalse(row.getRowRecord().getBadFontHeight());

        row.setHeightInPoints(432);
        assertEquals(432*20, row.getHeight());
        assertTrue(row.getRowRecord().getBadFontHeight());

        workbook.close();
    }

    @Test
    void testCopyRowFromExternalSheet() throws IOException {
        final HSSFWorkbook workbook = new HSSFWorkbook();
        final HSSFSheet srcSheet = workbook.createSheet("src");
        final HSSFSheet destSheet = workbook.createSheet("dest");
        workbook.createSheet("other");

        final Row srcRow = srcSheet.createRow(0);
        int col = 0;
        //Test 2D and 3D Ref Ptgs (Pxg for OOXML Workbooks)
        srcRow.createCell(col++).setCellFormula("B5");
        srcRow.createCell(col++).setCellFormula("src!B5");
        srcRow.createCell(col++).setCellFormula("dest!B5");
        srcRow.createCell(col++).setCellFormula("other!B5");

        //Test 2D and 3D Ref Ptgs with absolute row
        srcRow.createCell(col++).setCellFormula("B$5");
        srcRow.createCell(col++).setCellFormula("src!B$5");
        srcRow.createCell(col++).setCellFormula("dest!B$5");
        srcRow.createCell(col++).setCellFormula("other!B$5");

        //Test 2D and 3D Area Ptgs (Pxg for OOXML Workbooks)
        srcRow.createCell(col++).setCellFormula("SUM(B5:D$5)");
        srcRow.createCell(col++).setCellFormula("SUM(src!B5:D$5)");
        srcRow.createCell(col++).setCellFormula("SUM(dest!B5:D$5)");
        srcRow.createCell(col++).setCellFormula("SUM(other!B5:D$5)");

        //////////////////

        final int styleCount = workbook.getNumCellStyles();

        final HSSFRow destRow = destSheet.createRow(1);
        destRow.copyRowFrom(srcRow, new CellCopyPolicy());

        //////////////////

        //Test 2D and 3D Ref Ptgs (Pxg for OOXML Workbooks)
        col = 0;
        Cell cell = destRow.getCell(col++);
        assertNotNull(cell);
        assertEquals("B6", cell.getCellFormula(), "RefPtg");

        cell = destRow.getCell(col++);
        assertNotNull(cell);
        assertEquals("src!B6", cell.getCellFormula(), "Ref3DPtg");

        cell = destRow.getCell(col++);
        assertNotNull(cell);
        assertEquals("dest!B6", cell.getCellFormula(), "Ref3DPtg");

        cell = destRow.getCell(col++);
        assertNotNull(cell);
        assertEquals("other!B6", cell.getCellFormula(), "Ref3DPtg");

        /////////////////////////////////////////////

        //Test 2D and 3D Ref Ptgs with absolute row (Ptg row number shouldn't change)
        cell = destRow.getCell(col++);
        assertNotNull(cell);
        assertEquals("B$5", cell.getCellFormula(), "RefPtg");

        cell = destRow.getCell(col++);
        assertNotNull(cell);
        assertEquals("src!B$5", cell.getCellFormula(), "Ref3DPtg");

        cell = destRow.getCell(col++);
        assertNotNull(cell);
        assertEquals("dest!B$5", cell.getCellFormula(), "Ref3DPtg");

        cell = destRow.getCell(col++);
        assertNotNull(cell);
        assertEquals("other!B$5", cell.getCellFormula(), "Ref3DPtg");

        //////////////////////////////////////////

        //Test 2D and 3D Area Ptgs (Pxg for OOXML Workbooks)
        // Note: absolute row changes from last cell to first cell in order
        // to maintain topLeft:bottomRight order
        cell = destRow.getCell(col++);
        assertNotNull(cell);
        assertEquals("SUM(B$5:D6)", cell.getCellFormula(), "Area2DPtg");

        cell = destRow.getCell(col++);
        assertNotNull(cell);
        assertEquals("SUM(src!B$5:D6)", cell.getCellFormula(), "Area3DPtg");

        cell = destRow.getCell(col++);
        assertNotNull(cell);
        assertEquals("SUM(dest!B$5:D6)", cell.getCellFormula(), "Area3DPtg");

        cell = destRow.getCell(col++);
        assertNotNull(cell);
        assertEquals("SUM(other!B$5:D6)", cell.getCellFormula(), "Area3DPtg");

        assertEquals(styleCount, workbook.getNumCellStyles(), "no new styles should be added by copyRow");
        workbook.close();
    }
}
