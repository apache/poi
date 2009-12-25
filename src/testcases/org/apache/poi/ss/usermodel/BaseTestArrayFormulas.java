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

import junit.framework.TestCase;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import java.util.Iterator;
import java.util.Arrays;

/**
 * Common superclass for testing usermodel API for array formulas
 *
 * @author Yegor Kozlov
 */
public abstract class BaseTestArrayFormulas extends TestCase {

    /**
     * @return an object that provides test data in HSSF / XSSF specific way
     */
    protected abstract ITestDataProvider getTestDataProvider();


    /**
     *  Set single-cell array formula
     */
    public void testSetArrayFormula_singleCell() {
        Workbook workbook = getTestDataProvider().createWorkbook();
        Sheet sheet = workbook.createSheet();

        // row 3 does not yet exist
        assertNull(sheet.getRow(2));
        CellRangeAddress range = new CellRangeAddress(2, 2, 2, 2);
        Cell[] cells = sheet.setArrayFormula("SUM(C11:C12*D11:D12)", range);
        assertEquals(1, cells.length);
        // sheet.setArrayFormula creates rows and cells for the designated range
        assertNotNull(sheet.getRow(2));
        Cell cell = sheet.getRow(2).getCell(2);
        assertNotNull(cell);

        assertTrue(cell.isPartOfArrayFormulaGroup());
        //retrieve the range and check it is the same
        assertEquals(range.formatAsString(), cell.getArrayFormulaRange().formatAsString());
        //check the formula
        assertEquals("SUM(C11:C12*D11:D12)", cell.getCellFormula());
    }

    /**
     * Set multi-cell array formula
     */
    public void testSetArrayFormula_multiCell() {
        Workbook workbook = getTestDataProvider().createWorkbook();
        Sheet sheet = workbook.createSheet();

        // multi-cell formula
        // rows 3-5 don't exist yet
        assertNull(sheet.getRow(3));
        assertNull(sheet.getRow(4));
        assertNull(sheet.getRow(5));

        CellRangeAddress range = new CellRangeAddress(3, 5, 2, 2);
        assertEquals("C4:C6", range.formatAsString());
        Cell[] cells = sheet.setArrayFormula("SUM(A1:A3*B1:B3)", range);
        assertEquals(3, cells.length);

        // sheet.setArrayFormula creates rows and cells for the designated range
        assertSame(cells[0], sheet.getRow(3).getCell(2));
        assertSame(cells[1], sheet.getRow(4).getCell(2));
        assertSame(cells[2], sheet.getRow(5).getCell(2));

        for(Cell acell : cells){
            assertTrue(acell.isPartOfArrayFormulaGroup());
            assertEquals(Cell.CELL_TYPE_FORMULA, acell.getCellType());
            assertEquals("SUM(A1:A3*B1:B3)", acell.getCellFormula());
            //retrieve the range and check it is the same
            assertEquals(range.formatAsString(), acell.getArrayFormulaRange().formatAsString());
        }
    }

    /**
     * Passing an incorrect formula to sheet.setArrayFormula
     *  should throw FormulaParseException
     */
    public void testSetArrayFormula_incorrectFormula() {
        Workbook workbook = getTestDataProvider().createWorkbook();
        Sheet sheet = workbook.createSheet();

        try {
            sheet.setArrayFormula("incorrect-formula(C11_C12*D11_D12)",
                    new CellRangeAddress(10, 10, 10, 10));
            fail("expected exception");
        } catch (FormulaParseException e){
            //expected exception
        }
    }

    /**
     * Calls of cell.getArrayFormulaRange and sheet.removeArrayFormula
     * on a not-array-formula cell throw IllegalStateException
     */
    public void testArrayFormulas_illegalCalls() {
        Workbook workbook = getTestDataProvider().createWorkbook();
        Sheet sheet = workbook.createSheet();

        Cell cell = sheet.createRow(0).createCell(0);
        assertFalse(cell.isPartOfArrayFormulaGroup());
        try {
            CellRangeAddress range = cell.getArrayFormulaRange();
            fail("expected exception");
        } catch (IllegalStateException e){
            assertEquals("Cell A1 is not part of an array formula", e.getMessage());
        }

        try {
            sheet.removeArrayFormula(cell);
            fail("expected exception");
        } catch (IllegalArgumentException e){
            assertEquals("Cell A1 is not part of an array formula", e.getMessage());
        }
    }

    /**
     * create and remove array formulas
     */
    public void testRemoveArrayFormula() {
        Workbook workbook = getTestDataProvider().createWorkbook();
        Sheet sheet = workbook.createSheet();

        CellRangeAddress range = new CellRangeAddress(3, 5, 2, 2);
        assertEquals("C4:C6", range.formatAsString());
        Cell[] cells = sheet.setArrayFormula("SUM(A1:A3*B1:B3)", range);
        assertEquals(3, cells.length);

        // remove the formula cells in C4:C6
        Cell[] dcells = sheet.removeArrayFormula(cells[0]);
        // removeArrayFormula should return the same cells as setArrayFormula
        assertTrue(Arrays.equals(cells, dcells));

        for(Cell acell : cells){
            assertFalse(acell.isPartOfArrayFormulaGroup());
            assertEquals(Cell.CELL_TYPE_BLANK, acell.getCellType());
        }

        // cells C4:C6 are not included in array formula,
        // invocation of sheet.removeArrayFormula on any of them throws IllegalArgumentException
        for(Cell acell : cells){
            try {
                sheet.removeArrayFormula(acell);
                fail("expected exception");
            } catch (IllegalArgumentException e){
                String ref = new CellReference(acell).formatAsString();
                assertEquals("Cell "+ref+" is not part of an array formula", e.getMessage());
            }
        }
    }

    /**
     * Test that when reading a workbook from input stream, array formulas are recognized
     */
    public void testReadArrayFormula() {
        Cell[] cells;

        Workbook workbook = getTestDataProvider().createWorkbook();
        Sheet sheet1 = workbook.createSheet();
        cells = sheet1.setArrayFormula("SUM(A1:A3*B1:B3)", CellRangeAddress.valueOf("C4:C6"));
        assertEquals(3, cells.length);

        cells = sheet1.setArrayFormula("MAX(A1:A3*B1:B3)", CellRangeAddress.valueOf("A4:A6"));
        assertEquals(3, cells.length);

        Sheet sheet2 = workbook.createSheet();
        cells = sheet2.setArrayFormula("MIN(A1:A3*B1:B3)", CellRangeAddress.valueOf("D2:D4"));
        assertEquals(3, cells.length);

        workbook = getTestDataProvider().writeOutAndReadBack(workbook);
        sheet1 = workbook.getSheetAt(0);
        for(int rownum=3; rownum <= 5; rownum++) {
            Cell cell1 = sheet1.getRow(rownum).getCell(2);
            assertTrue( cell1.isPartOfArrayFormulaGroup());

            Cell cell2 = sheet1.getRow(rownum).getCell(0);
            assertTrue( cell2.isPartOfArrayFormulaGroup());
        }

        sheet2 = workbook.getSheetAt(1);
        for(int rownum=1; rownum <= 3; rownum++) {
            Cell cell1 = sheet2.getRow(rownum).getCell(3);
            assertTrue( cell1.isPartOfArrayFormulaGroup());
        }
    }
}