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

package org.apache.poi.xssf.usermodel;

import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.TestXSSFCell.DummySharedStringSource;
import org.apache.poi.xssf.model.SharedStringsTable;

/**
 * Tests for XSSFRow
 */
public final class TestXSSFRow extends TestCase {

    /**
     * Test adding cells to a row in various places and see if we can find them again.
     */
    public void testAddAndIterateCells() {
        XSSFRow row = new XSSFRow(createParentObjects());

        // One cell at the beginning
        Cell cell1 = row.createCell((short) 1);
        Iterator<Cell> it = row.cellIterator();
        assertTrue(it.hasNext());
        assertTrue(cell1 == it.next());
        assertFalse(it.hasNext());

        // Add another cell at the end
        Cell cell2 = row.createCell((short) 99);
        it = row.cellIterator();
        assertTrue(it.hasNext());
        assertTrue(cell1 == it.next());
        assertTrue(it.hasNext());
        assertTrue(cell2 == it.next());

        // Add another cell at the beginning
        Cell cell3 = row.createCell((short) 0);
        it = row.cellIterator();
        assertTrue(it.hasNext());
        assertTrue(cell3 == it.next());
        assertTrue(it.hasNext());
        assertTrue(cell1 == it.next());
        assertTrue(it.hasNext());
        assertTrue(cell2 == it.next());

        // Replace cell1
        Cell cell4 = row.createCell((short) 1);
        it = row.cellIterator();
        assertTrue(it.hasNext());
        assertTrue(cell3 == it.next());
        assertTrue(it.hasNext());
        assertTrue(cell4 == it.next());
        assertTrue(it.hasNext());
        assertTrue(cell2 == it.next());
        assertFalse(it.hasNext());

        // Add another cell, specifying the cellType
        Cell cell5 = row.createCell((short) 2, Cell.CELL_TYPE_STRING);
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

    public void testGetCell() {
        XSSFRow row = getSampleRow();

        assertNotNull(row.getCell((short) 2));
        assertNotNull(row.getCell((short) 3));
        assertNotNull(row.getCell((short) 4));
        // cell3 may have been created as CELL_TYPE_NUMERIC, but since there is no numeric
        // value set yet, its cell type is classified as 'blank'
        assertEquals(Cell.CELL_TYPE_BLANK, row.getCell((short) 3).getCellType());
        assertNull(row.getCell((short) 5));
    }

    public void testGetPhysicalNumberOfCells() {
        XSSFRow row = getSampleRow();
        assertEquals(7, row.getPhysicalNumberOfCells());
    }

    public void testGetFirstCellNum() {
        // Test a row with some cells
        XSSFRow row = getSampleRow();
        assertFalse(row.getFirstCellNum() == (short) 0);
        assertEquals((short) 2, row.getFirstCellNum());

        // Test after removing the first cell
        Cell cell = row.getCell((short) 2);
        row.removeCell(cell);
        assertFalse(row.getFirstCellNum() == (short) 2);

        // Test a row without cells
        XSSFRow emptyRow = new XSSFRow(createParentObjects());
        assertEquals(-1, emptyRow.getFirstCellNum());
    }

    public void testLastCellNum() {
        XSSFRow row = getSampleRow();
        assertEquals(100, row.getLastCellNum());

        Cell cell = row.getCell((short) 100);
        row.removeCell(cell);
        assertFalse(row.getLastCellNum() == (short) 100);
    }

    public void testRemoveCell() {
        XSSFRow row = getSampleRow();

        // Test removing the first cell
        Cell firstCell = row.getCell((short) 2);
        assertNotNull(firstCell);
        assertEquals(7, row.getPhysicalNumberOfCells());
        row.removeCell(firstCell);
        assertEquals(6, row.getPhysicalNumberOfCells());
        firstCell = row.getCell((short) 2);
        assertNull(firstCell);

        // Test removing the last cell
        Cell lastCell = row.getCell((short) 100);
        row.removeCell(lastCell);
    }

    public void testGetSetHeight() {
        XSSFRow row = getSampleRow();
        // I assume that "ht" attribute value is in 'points', please verify that
        // Test that no rowHeight is set
        assertEquals((short) -1, row.getHeight());
        // Set a rowHeight in twips (1/20th of a point) and test the new value
        row.setHeight((short) 240);
        assertEquals((short) 240, row.getHeight());
        assertEquals(12F, row.getHeightInPoints());
        // Set a new rowHeight in points and test the new value
        row.setHeightInPoints(13F);
        assertEquals((float) 13, row.getHeightInPoints());
        assertEquals((short) 260, row.getHeight());
    }

    public void testGetSetZeroHeight() throws Exception {
        XSSFRow row = getSampleRow();
        assertFalse(row.getZeroHeight());
        row.setZeroHeight(true);
        assertTrue(row.getZeroHeight());
    }

    /**
     * Tests for the missing/blank cell policy stuff
     */
    public void testGetCellPolicy() throws Exception {
        XSSFRow row = new XSSFRow(createParentObjects());

        // 0 -> string
        // 1 -> num
        // 2 missing
        // 3 missing
        // 4 -> blank
        // 5 -> num
        row.createCell((short)0).setCellValue(new XSSFRichTextString("test"));
        row.createCell((short)1).setCellValue(3.2);
        row.createCell((short)4, Cell.CELL_TYPE_BLANK);
        row.createCell((short)5).setCellValue(4);

        // First up, no policy
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
        assertEquals(Cell.CELL_TYPE_STRING,  row.getCell(0, XSSFRow.RETURN_BLANK_AS_NULL).getCellType());
        assertEquals(Cell.CELL_TYPE_NUMERIC, row.getCell(1, XSSFRow.RETURN_BLANK_AS_NULL).getCellType());
        assertEquals(null, row.getCell(2, XSSFRow.RETURN_BLANK_AS_NULL));
        assertEquals(null, row.getCell(3, XSSFRow.RETURN_BLANK_AS_NULL));
        assertEquals(null, row.getCell(4, XSSFRow.RETURN_BLANK_AS_NULL));
        assertEquals(Cell.CELL_TYPE_NUMERIC, row.getCell(5, XSSFRow.RETURN_BLANK_AS_NULL).getCellType());

        // CREATE_NULL_AS_BLANK - creates as needed
        assertEquals(Cell.CELL_TYPE_STRING,  row.getCell(0, XSSFRow.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(Cell.CELL_TYPE_NUMERIC, row.getCell(1, XSSFRow.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(Cell.CELL_TYPE_BLANK,   row.getCell(2, XSSFRow.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(Cell.CELL_TYPE_BLANK,   row.getCell(3, XSSFRow.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(Cell.CELL_TYPE_BLANK,   row.getCell(4, XSSFRow.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(Cell.CELL_TYPE_NUMERIC, row.getCell(5, XSSFRow.CREATE_NULL_AS_BLANK).getCellType());

        // Check created ones get the right column
        assertEquals((short)0, row.getCell(0, XSSFRow.CREATE_NULL_AS_BLANK).getCellNum());
        assertEquals((short)1, row.getCell(1, XSSFRow.CREATE_NULL_AS_BLANK).getCellNum());
        assertEquals((short)2, row.getCell(2, XSSFRow.CREATE_NULL_AS_BLANK).getCellNum());
        assertEquals((short)3, row.getCell(3, XSSFRow.CREATE_NULL_AS_BLANK).getCellNum());
        assertEquals((short)4, row.getCell(4, XSSFRow.CREATE_NULL_AS_BLANK).getCellNum());
        assertEquals((short)5, row.getCell(5, XSSFRow.CREATE_NULL_AS_BLANK).getCellNum());
    }

    /**
     * Method that returns a row with some sample cells
     * @return row
     */
    private static XSSFRow getSampleRow() {
        XSSFRow row = new XSSFRow(createParentObjects());
        row.createCell((short) 2);
        row.createCell((short) 3, Cell.CELL_TYPE_NUMERIC);
        row.createCell((short) 4);
        row.createCell((short) 6);
        row.createCell((short) 7);
        row.createCell((short) 8);
        row.createCell((short) 100);
        return row;
    }

    private static XSSFSheet createParentObjects() {
        XSSFWorkbook wb = new XSSFWorkbook();
        wb.setSharedStringSource(new SharedStringsTable());
        return new XSSFSheet(wb);
    }
}
