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

import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.poi.ss.ITestDataProvider;

/**
 * A base class for testing implementations of
 * {@link org.apache.poi.ss.usermodel.Row}
 */
public abstract class BaseTestRow extends TestCase {

    private final ITestDataProvider _testDataProvider;

    protected BaseTestRow(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    public final void testLastAndFirstColumns() {
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
    }

    /**
     * Make sure that there is no cross-talk between rows especially with getFirstCellNum and getLastCellNum
     * This test was added in response to bug report 44987.
     */
    public final void testBoundsInMultipleRows() {
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
    }

    public final void testRemoveCell() {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();
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

        workbook = _testDataProvider.writeOutAndReadBack(workbook);
        sheet = workbook.getSheetAt(0);
        row = sheet.getRow(0);
        assertEquals(-1, row.getLastCellNum());
        assertEquals(-1, row.getFirstCellNum());
        assertEquals(0, row.getPhysicalNumberOfCells());
    }

    public void baseTestRowBounds(int maxRowNum) {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();
        //Test low row bound
        sheet.createRow(0);
        //Test low row bound exception
        try {
            sheet.createRow(-1);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            // expected during successful test
            assertTrue(e.getMessage().startsWith("Invalid row number (-1)"));
        }

        //Test high row bound
        sheet.createRow(maxRowNum);
        //Test high row bound exception
        try {
            sheet.createRow(maxRowNum + 1);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            // expected during successful test
            assertEquals("Invalid row number ("+(maxRowNum + 1)+") outside allowable range (0.."+maxRowNum+")", e.getMessage());
        }
    }

    public void baseTestCellBounds(int maxCellNum) {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();

        Row row = sheet.createRow(0);
        //Test low cell bound
        try {
            row.createCell(-1);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            // expected during successful test
            assertTrue(e.getMessage().startsWith("Invalid column index (-1)"));
        }

        //Test high cell bound
        try {
            row.createCell(maxCellNum + 1);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            // expected during successful test
            assertTrue(e.getMessage().startsWith("Invalid column index ("+(maxCellNum+1)+")"));
        }
        for(int i=0; i < maxCellNum; i++){
            row.createCell(i);
        }
        assertEquals(maxCellNum, row.getPhysicalNumberOfCells());
        workbook = _testDataProvider.writeOutAndReadBack(workbook);
        sheet = workbook.getSheetAt(0);
        row = sheet.getRow(0);
        assertEquals(maxCellNum, row.getPhysicalNumberOfCells());
        for(int i=0; i < maxCellNum; i++){
            Cell cell = row.getCell(i);
            assertEquals(i, cell.getColumnIndex());
        }

    }

    /**
     * Prior to patch 43901, POI was producing files with the wrong last-column
     * number on the row
     */
    public final void testLastCellNumIsCorrectAfterAddCell_bug43901(){
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet("test");
        Row row = sheet.createRow(0);

        // New row has last col -1
        assertEquals(-1, row.getLastCellNum());
        if(row.getLastCellNum() == 0) {
            fail("Identified bug 43901");
        }

        // Create two cells, will return one higher
        //  than that for the last number
        row.createCell(0);
        assertEquals(1, row.getLastCellNum());
        row.createCell(255);
        assertEquals(256, row.getLastCellNum());
    }

    /**
     * Tests for the missing/blank cell policy stuff
     */
    public final void testGetCellPolicy() {
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
        row.createCell(4, Cell.CELL_TYPE_BLANK);
        row.createCell(5).setCellValue(4);

        // First up, no policy given, uses default
        assertEquals(Cell.CELL_TYPE_STRING,  row.getCell(0).getCellType());
        assertEquals(Cell.CELL_TYPE_NUMERIC, row.getCell(1).getCellType());
        assertEquals(null, row.getCell(2));
        assertEquals(null, row.getCell(3));
        assertEquals(Cell.CELL_TYPE_BLANK,   row.getCell(4).getCellType());
        assertEquals(Cell.CELL_TYPE_NUMERIC, row.getCell(5).getCellType());

        // RETURN_NULL_AND_BLANK - same as default
        assertEquals(Cell.CELL_TYPE_STRING,  row.getCell(0, Row.RETURN_NULL_AND_BLANK).getCellType());
        assertEquals(Cell.CELL_TYPE_NUMERIC, row.getCell(1, Row.RETURN_NULL_AND_BLANK).getCellType());
        assertEquals(null, row.getCell(2, Row.RETURN_NULL_AND_BLANK));
        assertEquals(null, row.getCell(3, Row.RETURN_NULL_AND_BLANK));
        assertEquals(Cell.CELL_TYPE_BLANK,   row.getCell(4, Row.RETURN_NULL_AND_BLANK).getCellType());
        assertEquals(Cell.CELL_TYPE_NUMERIC, row.getCell(5, Row.RETURN_NULL_AND_BLANK).getCellType());

        // RETURN_BLANK_AS_NULL - nearly the same
        assertEquals(Cell.CELL_TYPE_STRING,  row.getCell(0, Row.RETURN_BLANK_AS_NULL).getCellType());
        assertEquals(Cell.CELL_TYPE_NUMERIC, row.getCell(1, Row.RETURN_BLANK_AS_NULL).getCellType());
        assertEquals(null, row.getCell(2, Row.RETURN_BLANK_AS_NULL));
        assertEquals(null, row.getCell(3, Row.RETURN_BLANK_AS_NULL));
        assertEquals(null, row.getCell(4, Row.RETURN_BLANK_AS_NULL));
        assertEquals(Cell.CELL_TYPE_NUMERIC, row.getCell(5, Row.RETURN_BLANK_AS_NULL).getCellType());

        // CREATE_NULL_AS_BLANK - creates as needed
        assertEquals(Cell.CELL_TYPE_STRING,  row.getCell(0, Row.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(Cell.CELL_TYPE_NUMERIC, row.getCell(1, Row.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(Cell.CELL_TYPE_BLANK,   row.getCell(2, Row.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(Cell.CELL_TYPE_BLANK,   row.getCell(3, Row.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(Cell.CELL_TYPE_BLANK,   row.getCell(4, Row.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(Cell.CELL_TYPE_NUMERIC, row.getCell(5, Row.CREATE_NULL_AS_BLANK).getCellType());

        // Check created ones get the right column
        assertEquals(0, row.getCell(0, Row.CREATE_NULL_AS_BLANK).getColumnIndex());
        assertEquals(1, row.getCell(1, Row.CREATE_NULL_AS_BLANK).getColumnIndex());
        assertEquals(2, row.getCell(2, Row.CREATE_NULL_AS_BLANK).getColumnIndex());
        assertEquals(3, row.getCell(3, Row.CREATE_NULL_AS_BLANK).getColumnIndex());
        assertEquals(4, row.getCell(4, Row.CREATE_NULL_AS_BLANK).getColumnIndex());
        assertEquals(5, row.getCell(5, Row.CREATE_NULL_AS_BLANK).getColumnIndex());


        // Now change the cell policy on the workbook, check
        //  that that is now used if no policy given
        workbook.setMissingCellPolicy(Row.RETURN_BLANK_AS_NULL);

        assertEquals(Cell.CELL_TYPE_STRING,  row.getCell(0).getCellType());
        assertEquals(Cell.CELL_TYPE_NUMERIC, row.getCell(1).getCellType());
        assertEquals(null, row.getCell(2));
        assertEquals(null, row.getCell(3));
        assertEquals(null, row.getCell(4));
        assertEquals(Cell.CELL_TYPE_NUMERIC, row.getCell(5).getCellType());
    }

    public final void testRowHeight() {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();
        Row row1 = sheet.createRow(0);

        assertEquals(sheet.getDefaultRowHeight(), row1.getHeight());

        sheet.setDefaultRowHeightInPoints(20);
        row1.setHeight((short)-1); //reset the row height
        assertEquals(20.0f, row1.getHeightInPoints(), 0F);
        assertEquals(20*20, row1.getHeight());

        Row row2 = sheet.createRow(1);
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

        workbook = _testDataProvider.writeOutAndReadBack(workbook);
        sheet = workbook.getSheetAt(0);

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
    }

    /**
     * Test adding cells to a row in various places and see if we can find them again.
     */
    public final void testCellIterator() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);

        // One cell at the beginning
        Cell cell1 = row.createCell(1);
        Iterator<Cell> it = row.cellIterator();
        assertTrue(it.hasNext());
        assertTrue(cell1 == it.next());
        assertFalse(it.hasNext());

        // Add another cell at the end
        Cell cell2 = row.createCell(99);
        it = row.cellIterator();
        assertTrue(it.hasNext());
        assertTrue(cell1 == it.next());
        assertTrue(it.hasNext());
        assertTrue(cell2 == it.next());

        // Add another cell at the beginning
        Cell cell3 = row.createCell(0);
        it = row.cellIterator();
        assertTrue(it.hasNext());
        assertTrue(cell3 == it.next());
        assertTrue(it.hasNext());
        assertTrue(cell1 == it.next());
        assertTrue(it.hasNext());
        assertTrue(cell2 == it.next());

        // Replace cell1
        Cell cell4 = row.createCell(1);
        it = row.cellIterator();
        assertTrue(it.hasNext());
        assertTrue(cell3 == it.next());
        assertTrue(it.hasNext());
        assertTrue(cell4 == it.next());
        assertTrue(it.hasNext());
        assertTrue(cell2 == it.next());
        assertFalse(it.hasNext());

        // Add another cell, specifying the cellType
        Cell cell5 = row.createCell(2, Cell.CELL_TYPE_STRING);
        it = row.cellIterator();
        assertNotNull(cell5);
        assertTrue(it.hasNext());
        assertTrue(cell3 == it.next());
        assertTrue(it.hasNext());
        assertTrue(cell4 == it.next());
        assertTrue(it.hasNext());
        assertTrue(cell5 == it.next());
        assertTrue(it.hasNext());
        assertTrue(cell2 == it.next());
        assertEquals(Cell.CELL_TYPE_STRING, cell5.getCellType());
    }
}
