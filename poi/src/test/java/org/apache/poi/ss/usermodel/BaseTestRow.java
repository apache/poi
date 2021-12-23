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

package org.apache.poi.ss.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Iterator;
import java.util.Spliterator;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.junit.jupiter.api.Test;

/**
 * A base class for testing implementations of
 * {@link org.apache.poi.ss.usermodel.Row}
 */
public abstract class BaseTestRow {

    protected final ITestDataProvider _testDataProvider;

    protected BaseTestRow(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    @Test
    void testLastAndFirstColumns() throws IOException {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();
        Row row = sheet.createRow(0);
        assertEquals(-1, row.getFirstCellNum());
        assertEquals(-1, row.getLastCellNum());

        //getting cells from an empty row should returns null
        for(int i=0; i < 10; i++) assertNull(row.getCell(i));

        row.createCell(2);
        assertEquals(2, row.getFirstCellNum());
        assertEquals(3, row.getLastCellNum());

        row.createCell(1);
        assertEquals(1, row.getFirstCellNum());
        assertEquals(3, row.getLastCellNum());

        // check the exact case reported in 'bug' 43901 - notice that the cellNum is '0' based
        row.createCell(3);
        assertEquals(1, row.getFirstCellNum());
        assertEquals(4, row.getLastCellNum());
        workbook.close();
    }

    /**
     * Make sure that there is no cross-talk between rows especially with getFirstCellNum and getLastCellNum
     * This test was added in response to bug report 44987.
     */
    @Test
    void testBoundsInMultipleRows() throws IOException {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();
        Row rowA = sheet.createRow(0);

        rowA.createCell(10);
        rowA.createCell(5);
        assertEquals(5, rowA.getFirstCellNum());
        assertEquals(11, rowA.getLastCellNum());

        Row rowB = sheet.createRow(1);
        rowB.createCell(15);
        rowB.createCell(30);
        assertEquals(15, rowB.getFirstCellNum());
        assertEquals(31, rowB.getLastCellNum());

        assertEquals(5, rowA.getFirstCellNum());
        assertEquals(11, rowA.getLastCellNum());
        rowA.createCell(50);
        assertEquals(51, rowA.getLastCellNum());

        assertEquals(31, rowB.getLastCellNum());
        workbook.close();
    }

    @Test
    void testRemoveCell() throws IOException {
        Workbook wb1 = _testDataProvider.createWorkbook();
        {
            Sheet sheet = wb1.createSheet();
            Row row = sheet.createRow(0);

            assertEquals(0, row.getPhysicalNumberOfCells());
            assertEquals(-1, row.getLastCellNum());
            assertEquals(-1, row.getFirstCellNum());

            row.createCell(1);
            assertEquals(2, row.getLastCellNum());
            assertEquals(1, row.getFirstCellNum());
            assertEquals(1, row.getPhysicalNumberOfCells());
            row.createCell(3);
            assertEquals(4, row.getLastCellNum());
            assertEquals(1, row.getFirstCellNum());
            assertEquals(2, row.getPhysicalNumberOfCells());
            row.removeCell(row.getCell(3));
            assertEquals(2, row.getLastCellNum());
            assertEquals(1, row.getFirstCellNum());
            assertEquals(1, row.getPhysicalNumberOfCells());
            row.removeCell(row.getCell(1));
            assertEquals(-1, row.getLastCellNum());
            assertEquals(-1, row.getFirstCellNum());
            assertEquals(0, row.getPhysicalNumberOfCells());
        }

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();

        {
            Sheet sheet = wb2.getSheetAt(0);
            Row row = sheet.getRow(0);
            assertEquals(-1, row.getLastCellNum());
            assertEquals(-1, row.getFirstCellNum());
            assertEquals(0, row.getPhysicalNumberOfCells());
        }

        wb2.close();
    }

    @Test
    void testRowBounds() throws IOException {
        int maxRowNum = _testDataProvider.getSpreadsheetVersion().getLastRowIndex();
        try (Workbook workbook = _testDataProvider.createWorkbook()) {
            Sheet sheet = workbook.createSheet();
            //Test low row bound
            sheet.createRow(0);
            //Test low row bound exception
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> sheet.createRow(-1));
            assertTrue(e.getMessage().startsWith("Invalid row number (-1)"));

            //Test high row bound
            sheet.createRow(maxRowNum);
            //Test high row bound exception
            e = assertThrows(IllegalArgumentException.class, () -> sheet.createRow(maxRowNum + 1));
            assertEquals("Invalid row number (" + (maxRowNum + 1) + ") outside allowable range (0.." + maxRowNum + ")", e.getMessage());
        }
    }

    @Test
    protected void testCellBounds() throws IOException {
        int maxCellNum = _testDataProvider.getSpreadsheetVersion().getLastColumnIndex();
        try (Workbook wb1 = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb1.createSheet();

            Row row1 = sheet.createRow(0);
            //Test low cell bound
            IllegalArgumentException e;
            e = assertThrows(IllegalArgumentException.class, () -> row1.createCell(-1));
            assertTrue(e.getMessage().startsWith("Invalid column index (-1)"));

            //Test high cell bound
            e = assertThrows(IllegalArgumentException.class, () -> row1.createCell(maxCellNum + 1));
            assertTrue(e.getMessage().startsWith("Invalid column index (" + (maxCellNum + 1) + ")"));

            for (int i = 0; i < maxCellNum; i++) {
                row1.createCell(i);
            }
            assertEquals(maxCellNum, row1.getPhysicalNumberOfCells());

            try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1)) {
                sheet = wb2.getSheetAt(0);
                Row row2 = sheet.getRow(0);
                assertEquals(maxCellNum, row2.getPhysicalNumberOfCells());
                for (int i = 0; i < maxCellNum; i++) {
                    Cell cell = row2.getCell(i);
                    assertEquals(i, cell.getColumnIndex());
                }
            }
        }
    }

    /**
     * Prior to patch 43901, POI was producing files with the wrong last-column
     * number on the row
     */
    @Test
    void testLastCellNumIsCorrectAfterAddCell_bug43901() throws IOException {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet("test");
        Row row = sheet.createRow(0);

        // New row has last col -1
        assertEquals(-1, row.getLastCellNum());
        assertNotEquals(0, row.getLastCellNum(), "Identified bug 43901");

        // Create two cells, will return one higher
        //  than that for the last number
        row.createCell(0);
        assertEquals(1, row.getLastCellNum());
        row.createCell(255);
        assertEquals(256, row.getLastCellNum());
        workbook.close();
    }

    /**
     * Tests for the missing/blank cell policy stuff
     */
    @Test
    void testGetCellPolicy() throws IOException {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet("test");
        Row row = sheet.createRow(0);

        // 0 -> string
        // 1 -> num
        // 2 missing
        // 3 missing
        // 4 -> blank
        // 5 -> num
        row.createCell(0).setCellValue("test");
        row.createCell(1).setCellValue(3.2);
        row.createCell(4, CellType.BLANK);
        row.createCell(5).setCellValue(4);

        // First up, no policy given, uses default
        assertEquals(CellType.STRING,  row.getCell(0).getCellType());
        assertEquals(CellType.NUMERIC, row.getCell(1).getCellType());
        assertNull(row.getCell(2));
        assertNull(row.getCell(3));
        assertEquals(CellType.BLANK,   row.getCell(4).getCellType());
        assertEquals(CellType.NUMERIC, row.getCell(5).getCellType());

        // RETURN_NULL_AND_BLANK - same as default
        assertEquals(CellType.STRING,  row.getCell(0, MissingCellPolicy.RETURN_NULL_AND_BLANK).getCellType());
        assertEquals(CellType.NUMERIC, row.getCell(1, MissingCellPolicy.RETURN_NULL_AND_BLANK).getCellType());
        assertNull(row.getCell(2, MissingCellPolicy.RETURN_NULL_AND_BLANK));
        assertNull(row.getCell(3, MissingCellPolicy.RETURN_NULL_AND_BLANK));
        assertEquals(CellType.BLANK,   row.getCell(4, MissingCellPolicy.RETURN_NULL_AND_BLANK).getCellType());
        assertEquals(CellType.NUMERIC, row.getCell(5, MissingCellPolicy.RETURN_NULL_AND_BLANK).getCellType());

        // RETURN_BLANK_AS_NULL - nearly the same
        assertEquals(CellType.STRING,  row.getCell(0, MissingCellPolicy.RETURN_BLANK_AS_NULL).getCellType());
        assertEquals(CellType.NUMERIC, row.getCell(1, MissingCellPolicy.RETURN_BLANK_AS_NULL).getCellType());
        assertNull(row.getCell(2, MissingCellPolicy.RETURN_BLANK_AS_NULL));
        assertNull(row.getCell(3, MissingCellPolicy.RETURN_BLANK_AS_NULL));
        assertNull(row.getCell(4, MissingCellPolicy.RETURN_BLANK_AS_NULL));
        assertEquals(CellType.NUMERIC, row.getCell(5, MissingCellPolicy.RETURN_BLANK_AS_NULL).getCellType());

        // CREATE_NULL_AS_BLANK - creates as needed
        assertEquals(CellType.STRING,  row.getCell(0, MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(CellType.NUMERIC, row.getCell(1, MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(CellType.BLANK,   row.getCell(2, MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(CellType.BLANK,   row.getCell(3, MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(CellType.BLANK,   row.getCell(4, MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(CellType.NUMERIC, row.getCell(5, MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellType());

        // Check created ones get the right column
        assertEquals(0, row.getCell(0, MissingCellPolicy.CREATE_NULL_AS_BLANK).getColumnIndex());
        assertEquals(1, row.getCell(1, MissingCellPolicy.CREATE_NULL_AS_BLANK).getColumnIndex());
        assertEquals(2, row.getCell(2, MissingCellPolicy.CREATE_NULL_AS_BLANK).getColumnIndex());
        assertEquals(3, row.getCell(3, MissingCellPolicy.CREATE_NULL_AS_BLANK).getColumnIndex());
        assertEquals(4, row.getCell(4, MissingCellPolicy.CREATE_NULL_AS_BLANK).getColumnIndex());
        assertEquals(5, row.getCell(5, MissingCellPolicy.CREATE_NULL_AS_BLANK).getColumnIndex());


        // Now change the cell policy on the workbook, check
        //  that that is now used if no policy given
        workbook.setMissingCellPolicy(MissingCellPolicy.RETURN_BLANK_AS_NULL);

        assertEquals(CellType.STRING,  row.getCell(0).getCellType());
        assertEquals(CellType.NUMERIC, row.getCell(1).getCellType());
        assertNull(row.getCell(2));
        assertNull(row.getCell(3));
        assertNull(row.getCell(4));
        assertEquals(CellType.NUMERIC, row.getCell(5).getCellType());

        workbook.close();
    }

    @Test
    protected void testRowHeight() throws IOException {
        Workbook wb1 = _testDataProvider.createWorkbook();
        Sheet sheet = wb1.createSheet();
        Row row1 = sheet.createRow(0);

        assertEquals(sheet.getDefaultRowHeight(), row1.getHeight());

        sheet.setDefaultRowHeightInPoints(20);
        row1.setHeight((short)-1); //reset the row height
        assertEquals(20.0f, row1.getHeightInPoints(), 0F);
        assertEquals(20*20, row1.getHeight());

        Row row2 = sheet.createRow(1);
        assertEquals(sheet.getDefaultRowHeight(), row2.getHeight());
        row2.setHeight((short)310);
        assertEquals(310, row2.getHeight());
        assertEquals(310F/20, row2.getHeightInPoints(), 0F);

        Row row3 = sheet.createRow(2);
        row3.setHeightInPoints(25.5f);
        assertEquals((short)(25.5f*20), row3.getHeight());
        assertEquals(25.5f, row3.getHeightInPoints(), 0F);

        Row row4 = sheet.createRow(3);
        assertFalse(row4.getZeroHeight());
        row4.setZeroHeight(true);
        assertTrue(row4.getZeroHeight());

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
        sheet = wb2.getSheetAt(0);

        row1 = sheet.getRow(0);
        row2 = sheet.getRow(1);
        row3 = sheet.getRow(2);
        row4 = sheet.getRow(3);
        assertEquals(20.0f, row1.getHeightInPoints(), 0F);
        assertEquals(20*20, row1.getHeight());

        assertEquals(310, row2.getHeight());
        assertEquals(310F/20, row2.getHeightInPoints(), 0F);

        assertEquals((short)(25.5f*20), row3.getHeight());
        assertEquals(25.5f, row3.getHeightInPoints(), 0F);

        assertFalse(row1.getZeroHeight());
        assertFalse(row2.getZeroHeight());
        assertFalse(row3.getZeroHeight());
        assertTrue(row4.getZeroHeight());
        wb2.close();
    }

    /**
     * Test adding cells to a row in various places and see if we can find them again.
     */
    @Test
    void testCellIterator() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);

        // One cell at the beginning
        Cell cell1 = row.createCell(1);
        Iterator<Cell> it = row.cellIterator();
        assertTrue(it.hasNext());
        assertSame(cell1, it.next());
        assertFalse(it.hasNext());

        // Add another cell at the end
        Cell cell2 = row.createCell(99);
        it = row.cellIterator();
        assertTrue(it.hasNext());
        assertSame(cell1, it.next());
        assertTrue(it.hasNext());
        assertSame(cell2, it.next());

        // Add another cell at the beginning
        Cell cell3 = row.createCell(0);
        it = row.cellIterator();
        assertTrue(it.hasNext());
        assertSame(cell3, it.next());
        assertTrue(it.hasNext());
        assertSame(cell1, it.next());
        assertTrue(it.hasNext());
        assertSame(cell2, it.next());

        // Replace cell1
        Cell cell4 = row.createCell(1);
        it = row.cellIterator();
        assertTrue(it.hasNext());
        assertSame(cell3, it.next());
        assertTrue(it.hasNext());
        assertSame(cell4, it.next());
        assertTrue(it.hasNext());
        assertSame(cell2, it.next());
        assertFalse(it.hasNext());

        // Add another cell, specifying the cellType
        Cell cell5 = row.createCell(2, CellType.STRING);
        it = row.cellIterator();
        assertNotNull(cell5);
        assertTrue(it.hasNext());
        assertSame(cell3, it.next());
        assertTrue(it.hasNext());
        assertSame(cell4, it.next());
        assertTrue(it.hasNext());
        assertSame(cell5, it.next());
        assertTrue(it.hasNext());
        assertSame(cell2, it.next());
        assertEquals(CellType.STRING, cell5.getCellType());
        wb.close();
    }

    /**
     * Test adding cells to a row in various places and see if we can find them again.
     */
    @Test
    void testSpliterator() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);

        // One cell at the beginning
        Cell cell1 = row.createCell(1);
        Spliterator<Cell> split = row.spliterator();
        assertTrue(split.tryAdvance(cell -> assertSame(cell1, cell)));
        assertFalse(split.tryAdvance(cell -> fail()));

        // Add another cell at the end
        Cell cell2 = row.createCell(99);
        split = row.spliterator();
        assertTrue(split.tryAdvance(cell -> assertSame(cell1, cell)));
        assertTrue(split.tryAdvance(cell -> assertSame(cell2, cell)));

        // Add another cell at the beginning
        Cell cell3 = row.createCell(0);
        split = row.spliterator();
        assertTrue(split.tryAdvance(cell -> assertSame(cell3, cell)));
        assertTrue(split.tryAdvance(cell -> assertSame(cell1, cell)));
        assertTrue(split.tryAdvance(cell -> assertSame(cell2, cell)));

        // Replace cell1
        Cell cell4 = row.createCell(1);
        split = row.spliterator();
        assertTrue(split.tryAdvance(cell -> assertSame(cell3, cell)));
        assertTrue(split.tryAdvance(cell -> assertSame(cell4, cell)));
        assertTrue(split.tryAdvance(cell -> assertSame(cell2, cell)));
        assertFalse(split.tryAdvance(cell -> fail()));

        // Add another cell, specifying the cellType
        Cell cell5 = row.createCell(2, CellType.STRING);
        split = row.spliterator();
        assertNotNull(cell5);
        assertTrue(split.tryAdvance(cell -> assertSame(cell3, cell)));
        assertTrue(split.tryAdvance(cell -> assertSame(cell4, cell)));
        assertTrue(split.tryAdvance(cell -> assertSame(cell5, cell)));
        assertTrue(split.tryAdvance(cell -> assertSame(cell2, cell)));
        assertEquals(CellType.STRING, cell5.getCellType());
        wb.close();
    }

    @Test
    void testRowStyle() throws IOException {
       Workbook wb1 = _testDataProvider.createWorkbook();
       Sheet sheet = wb1.createSheet("test");
       Row row1 = sheet.createRow(0);
       Row row2 = sheet.createRow(1);

       // Won't be styled currently
        assertFalse(row1.isFormatted());
        assertFalse(row2.isFormatted());
        assertNull(row1.getRowStyle());
        assertNull(row2.getRowStyle());

       // Style one
       CellStyle style = wb1.createCellStyle();
       style.setDataFormat((short)4);
       row2.setRowStyle(style);

       // Check
        assertFalse(row1.isFormatted());
        assertTrue(row2.isFormatted());
        assertNull(row1.getRowStyle());
       assertEquals(style, row2.getRowStyle());

       // Save, load and re-check
       Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
       wb1.close();

       sheet = wb2.getSheetAt(0);

       row1 = sheet.getRow(0);
       row2 = sheet.getRow(1);
       style = wb2.getCellStyleAt(style.getIndex());

        assertFalse(row1.isFormatted());
        assertTrue(row2.isFormatted());
        assertNull(row1.getRowStyle());
       assertEquals(style, row2.getRowStyle());
       assertEquals(4, style.getDataFormat());

       wb2.close();
    }

    @Test
    protected void testCellShiftingRight() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet("sheet1");
        Row row = sheet.createRow(0);
        row.createCell(0, CellType.NUMERIC).setCellValue(0);
        row.createCell(1, CellType.NUMERIC).setCellValue(1);
        row.createCell(2, CellType.NUMERIC).setCellValue(2);//C
        row.createCell(3, CellType.NUMERIC).setCellValue(3);//D
        row.createCell(4, CellType.NUMERIC).setCellValue(4);//E
        row.createCell(5, CellType.NUMERIC).setCellValue(5);//F
        row.createCell(6, CellType.NUMERIC).setCellValue(6);//G

        assertThrows(IllegalArgumentException.class, () -> row.shiftCellsLeft(6, 4, 2),  "range [6-4] is illegal");
        row.shiftCellsRight(2, 4, 1);
        //should be [0.0, 1.0, null, 2.0, 3.0, 4.0, 6.0, null]

        Cell h1 = row.getCell(7);
        assertNull(h1);
        Cell g1 = row.getCell(6);
        assertEquals(6, g1.getNumericCellValue(), 0.01);
        Cell f1 = row.getCell(5);
        assertEquals(4, f1.getNumericCellValue(), 0.01);
        Cell e1 = row.getCell(4);
        assertEquals(3, e1.getNumericCellValue(), 0.01);
        Cell d1 = row.getCell(3);
        assertEquals(2, d1.getNumericCellValue(), 0.01);
        Cell c1 = row.getCell(2);
        assertNull(c1);
    }

    @Test
    protected void testCellShiftingLeft() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet("sheet1");
        Row row = sheet.createRow(0);
        row.createCell(0, CellType.NUMERIC).setCellValue(0);
        row.createCell(1, CellType.NUMERIC).setCellValue(1);
        row.createCell(2, CellType.NUMERIC).setCellValue(2);//C
        row.createCell(3, CellType.NUMERIC).setCellValue(3);//D
        row.createCell(4, CellType.NUMERIC).setCellValue(4);//E
        row.createCell(5, CellType.NUMERIC).setCellValue(5);//F
        row.createCell(6, CellType.NUMERIC).setCellValue(6);//G

        assertThrows(IllegalArgumentException.class, () -> row.shiftCellsLeft(4, 6, -2), "step = -1 is illegal");
        row.shiftCellsLeft(4, 6, 2);
        //should be [0.0, 1.0, 4.0, 5.0, 6.0, null, null, null]

        Cell b1 = row.getCell(1);
        assertEquals(1, b1.getNumericCellValue(), 0.01);
        Cell c1 = row.getCell(2);
        assertEquals(4, c1.getNumericCellValue(), 0.01);
        Cell d1 = row.getCell(3);
        assertEquals(5, d1.getNumericCellValue(), 0.01);
        Cell e1 = row.getCell(4);
        assertEquals(6, e1.getNumericCellValue(), 0.01);
        Cell f1 = row.getCell(5);
        assertNull(f1);
    }

    @Test
    void testLastRowEmptySheet() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet("sheet1");

        assertEquals(-1, sheet.getLastRowNum(), "Sheet without rows should return -1 as lastRowNum");
        Row row = sheet.createRow(0);
        assertNotNull(row);

        assertEquals(0, sheet.getLastRowNum(), "Sheet with one row should return 0 as lastRowNum");
    }

    @Test
    void testFirstRowEmptySheet() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet("sheet1");

        assertEquals(-1, sheet.getFirstRowNum(), "Sheet without rows should return -1 as firstRowNum");
        Row row = sheet.createRow(0);
        assertNotNull(row);

        assertEquals(0, sheet.getFirstRowNum(), "Sheet with one row should return 0 as firstRowNum");
    }
}
