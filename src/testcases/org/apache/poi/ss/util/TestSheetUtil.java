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

package org.apache.poi.ss.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

/**
 * Tests SheetUtil.
 *
 * @see org.apache.poi.ss.util.SheetUtil
 */
final class TestSheetUtil {
    @Test
    void testCellWithMerges() throws Exception {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet s = wb.createSheet();

            // Create some test data
            Row r2 = s.createRow(1);
            r2.createCell(0).setCellValue(10);
            r2.createCell(1).setCellValue(11);
            Row r3 = s.createRow(2);
            r3.createCell(0).setCellValue(20);
            r3.createCell(1).setCellValue(21);

            assertEquals(0, s.addMergedRegion(new CellRangeAddress(2, 3, 0, 0)));
            assertEquals(1, s.addMergedRegion(new CellRangeAddress(2, 2, 1, 4)));

            // With a cell that isn't defined, we'll get null
            assertNull(SheetUtil.getCellWithMerges(s, 0, 0));

            // With a cell that's not in a merged region, we'll get that
            Cell cell = SheetUtil.getCellWithMerges(s, 1, 0);
            assertNotNull(cell);
            assertEquals(10.0, cell.getNumericCellValue(), 0.01);
            cell = SheetUtil.getCellWithMerges(s, 1, 1);
            assertNotNull(cell);
            assertEquals(11.0, cell.getNumericCellValue(), 0.01);

            // With a cell that's the primary one of a merged region, we get that cell
            cell = SheetUtil.getCellWithMerges(s, 2, 0);
            assertNotNull(cell);
            assertEquals(20.0, cell.getNumericCellValue(), 0.01);
            cell = SheetUtil.getCellWithMerges(s, 2, 1);
            assertNotNull(cell);
            assertEquals(21., cell.getNumericCellValue(), 0.01);

            // With a cell elsewhere in the merged region, get top-left
            cell = SheetUtil.getCellWithMerges(s, 3, 0);
            assertNotNull(cell);
            assertEquals(20.0, cell.getNumericCellValue(), 0.01);
            cell = SheetUtil.getCellWithMerges(s, 2, 2);
            assertNotNull(cell);
            assertEquals(21.0, cell.getNumericCellValue(), 0.01);
            cell = SheetUtil.getCellWithMerges(s, 2, 3);
            assertNotNull(cell);
            assertEquals(21.0, cell.getNumericCellValue(), 0.01);
            assertNotNull(cell);
            cell = SheetUtil.getCellWithMerges(s, 2, 4);
            assertNotNull(cell);
            assertEquals(21.0, cell.getNumericCellValue(), 0.01);
        }
    }

    @Test
    void testCanComputeWidthHSSF() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            // cannot check on result because on some machines we get back false here!
            SheetUtil.canComputeColumnWidth(wb.getFontAt(0));
        }
    }

    @Test
    void testGetCellWidthEmpty() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet("sheet");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);

            // no contents: cell.setCellValue("sometext");
            assertEquals(-1.0, SheetUtil.getCellWidth(cell, 1, null, true), 0.01);
        }
    }

    @Test
    void testGetCellWidthString() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet("sheet");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);

            cell.setCellValue("sometext");

            assertTrue(SheetUtil.getCellWidth(cell, 1, null, true) > 0);
        }
    }

    @Test
    void testGetCellWidthNumber() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet("sheet");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);

            cell.setCellValue(88.234);

            assertTrue(SheetUtil.getCellWidth(cell, 1, null, true) > 0);
        }
    }

    @Test
    void testGetCellWidthBoolean() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet("sheet");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);

            cell.setCellValue(false);

            assertTrue(SheetUtil.getCellWidth(cell, 1, null, false) > 0);
        }
    }

    @Test
    void testGetColumnWidthString() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet("sheet");
            Row row = sheet.createRow(0);
            sheet.createRow(1);
            sheet.createRow(2);
            Cell cell = row.createCell(0);

            cell.setCellValue("sometext");

            assertTrue(SheetUtil.getColumnWidth(sheet, 0, true) > 0, "Having some width for rows with actual cells");
            assertEquals(-1.0, SheetUtil.getColumnWidth(sheet, 0, true, 1, 2), 0.01, "Not having any widht for rows with all empty cells");
        }
    }
}
