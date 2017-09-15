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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Common superclass for testing usermodel API for array formulas.<br>
 * Formula evaluation is not tested here.
 *
 * @author Yegor Kozlov
 * @author Josh Micich
 */
public abstract class BaseTestSheetUpdateArrayFormulas {
    protected final ITestDataProvider _testDataProvider;

    protected BaseTestSheetUpdateArrayFormulas(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    @Test
    public final void testAutoCreateOtherCells() throws IOException {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        Row row1 = sheet.createRow(0);
        Cell cellA1 = row1.createCell(0);
        Cell cellB1 = row1.createCell(1);
        String formula = "42";
        sheet.setArrayFormula(formula, CellRangeAddress.valueOf("A1:B2"));

        assertEquals(formula, cellA1.getCellFormula());
        assertEquals(formula, cellB1.getCellFormula());
        Row row2 = sheet.getRow(1);
        assertNotNull(row2);
        assertEquals(formula, row2.getCell(0).getCellFormula());
        assertEquals(formula, row2.getCell(1).getCellFormula());
        
        workbook.close();
    }
    /**
     *  Set single-cell array formula
     */
    @Test
    public final void testSetArrayFormula_singleCell() throws IOException {
        Cell[] cells;

        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();
        Cell cell = sheet.createRow(0).createCell(0);
        assertFalse(cell.isPartOfArrayFormulaGroup());
        try {
            cell.getArrayFormulaRange();
            fail("expected exception");
        } catch (IllegalStateException e){
            assertEquals("Cell A1 is not part of an array formula.", e.getMessage());
        }

        // row 3 does not yet exist
        assertNull(sheet.getRow(2));
        CellRangeAddress range = new CellRangeAddress(2, 2, 2, 2);
        cells = sheet.setArrayFormula("SUM(C11:C12*D11:D12)", range).getFlattenedCells();
        assertEquals(1, cells.length);
        // sheet.setArrayFormula creates rows and cells for the designated range
        assertNotNull(sheet.getRow(2));
        cell = sheet.getRow(2).getCell(2);
        assertNotNull(cell);

        assertTrue(cell.isPartOfArrayFormulaGroup());
        //retrieve the range and check it is the same
        assertEquals(range.formatAsString(), cell.getArrayFormulaRange().formatAsString());
        //check the formula
        assertEquals("SUM(C11:C12*D11:D12)", cell.getCellFormula());
        
        workbook.close();
    }

    /**
     * Set multi-cell array formula
     */
    @Test
    public final void testSetArrayFormula_multiCell() throws IOException {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();

        // multi-cell formula
        // rows 3-5 don't exist yet
        assertNull(sheet.getRow(3));
        assertNull(sheet.getRow(4));
        assertNull(sheet.getRow(5));

        CellRangeAddress range = CellRangeAddress.valueOf("C4:C6");
        Cell[] cells = sheet.setArrayFormula("SUM(A1:A3*B1:B3)", range).getFlattenedCells();
        assertEquals(3, cells.length);

        // sheet.setArrayFormula creates rows and cells for the designated range
        assertSame(cells[0], sheet.getRow(3).getCell(2));
        assertSame(cells[1], sheet.getRow(4).getCell(2));
        assertSame(cells[2], sheet.getRow(5).getCell(2));

        for(Cell acell : cells){
            assertTrue(acell.isPartOfArrayFormulaGroup());
            assertEquals(CellType.FORMULA, acell.getCellTypeEnum());
            assertEquals("SUM(A1:A3*B1:B3)", acell.getCellFormula());
            //retrieve the range and check it is the same
            assertEquals(range.formatAsString(), acell.getArrayFormulaRange().formatAsString());
        }
        
        workbook.close();
    }

    /**
     * Passing an incorrect formula to sheet.setArrayFormula
     *  should throw FormulaParseException
     */
    @Test
    public final void testSetArrayFormula_incorrectFormula() throws IOException {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();

        try {
            sheet.setArrayFormula("incorrect-formula(C11_C12*D11_D12)",
                    new CellRangeAddress(10, 10, 10, 10));
            fail("expected exception");
        } catch (FormulaParseException e){
            //expected exception
        }
        
        workbook.close();
    }

    /**
     * Calls of cell.getArrayFormulaRange and sheet.removeArrayFormula
     * on a not-array-formula cell throw IllegalStateException
     */
    @Test
    public final void testArrayFormulas_illegalCalls() throws IOException {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();

        Cell cell = sheet.createRow(0).createCell(0);
        assertFalse(cell.isPartOfArrayFormulaGroup());
        try {
            cell.getArrayFormulaRange();
            fail("expected exception");
        } catch (IllegalStateException e){
            assertEquals("Cell A1 is not part of an array formula.", e.getMessage());
        }

        try {
            sheet.removeArrayFormula(cell);
            fail("expected exception");
        } catch (IllegalArgumentException e){
            assertEquals("Cell A1 is not part of an array formula.", e.getMessage());
        }
        
        workbook.close();
    }

    /**
     * create and remove array formulas
     */
    @Test
    public final void testRemoveArrayFormula() throws IOException {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();

        CellRangeAddress range = new CellRangeAddress(3, 5, 2, 2);
        assertEquals("C4:C6", range.formatAsString());
        CellRange<?> cr = sheet.setArrayFormula("SUM(A1:A3*B1:B3)", range);
        assertEquals(3, cr.size());

        // remove the formula cells in C4:C6
        CellRange<?> dcells = sheet.removeArrayFormula(cr.getTopLeftCell());
        // removeArrayFormula should return the same cells as setArrayFormula
        assertArrayEquals(cr.getFlattenedCells(), dcells.getFlattenedCells());

        for(Cell acell : cr){
            assertFalse(acell.isPartOfArrayFormulaGroup());
            assertEquals(CellType.BLANK, acell.getCellTypeEnum());
        }

        // cells C4:C6 are not included in array formula,
        // invocation of sheet.removeArrayFormula on any of them throws IllegalArgumentException
        for(Cell acell : cr){
            try {
                sheet.removeArrayFormula(acell);
                fail("expected exception");
            } catch (IllegalArgumentException e){
                String ref = new CellReference(acell).formatAsString();
                assertEquals("Cell "+ref+" is not part of an array formula.", e.getMessage());
            }
        }
        
        workbook.close();
    }

    /**
     * Test that when reading a workbook from input stream, array formulas are recognized
     */
    @Test
    public final void testReadArrayFormula() throws IOException {
        Cell[] cells;

        Workbook workbook1 = _testDataProvider.createWorkbook();
        Sheet sheet1 = workbook1.createSheet();
        cells = sheet1.setArrayFormula("SUM(A1:A3*B1:B3)", CellRangeAddress.valueOf("C4:C6")).getFlattenedCells();
        assertEquals(3, cells.length);

        cells = sheet1.setArrayFormula("MAX(A1:A3*B1:B3)", CellRangeAddress.valueOf("A4:A6")).getFlattenedCells();
        assertEquals(3, cells.length);

        Sheet sheet2 = workbook1.createSheet();
        cells = sheet2.setArrayFormula("MIN(A1:A3*B1:B3)", CellRangeAddress.valueOf("D2:D4")).getFlattenedCells();
        assertEquals(3, cells.length);

        Workbook workbook2 = _testDataProvider.writeOutAndReadBack(workbook1);
        workbook1.close();
        sheet1 = workbook2.getSheetAt(0);
        for(int rownum=3; rownum <= 5; rownum++) {
            Cell cell1 = sheet1.getRow(rownum).getCell(2);
            assertTrue( cell1.isPartOfArrayFormulaGroup());

            Cell cell2 = sheet1.getRow(rownum).getCell(0);
            assertTrue( cell2.isPartOfArrayFormulaGroup());
        }

        sheet2 = workbook2.getSheetAt(1);
        for(int rownum=1; rownum <= 3; rownum++) {
            Cell cell1 = sheet2.getRow(rownum).getCell(3);
            assertTrue( cell1.isPartOfArrayFormulaGroup());
        }
        
        workbook2.close();
    }

    /**
     * Test that we can set pre-calculated formula result for array formulas
     */
    @Test
    public void testModifyArrayCells_setFormulaResult() throws IOException {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();

        //single-cell array formula
        CellRange<? extends Cell> srange =
                sheet.setArrayFormula("SUM(A4:A6,B4:B6)", CellRangeAddress.valueOf("B5"));
        Cell scell = srange.getTopLeftCell();
        assertEquals(CellType.FORMULA, scell.getCellTypeEnum());
        assertEquals(0.0, scell.getNumericCellValue(), 0);
        scell.setCellValue(1.1);
        assertEquals(1.1, scell.getNumericCellValue(), 0);

        //multi-cell array formula
        CellRange<? extends Cell> mrange =
                sheet.setArrayFormula("A1:A3*B1:B3", CellRangeAddress.valueOf("C1:C3"));
        for(Cell mcell : mrange){
            assertEquals(CellType.FORMULA, mcell.getCellTypeEnum());
            assertEquals(0.0, mcell.getNumericCellValue(), 0);
            double fmlaResult = 1.2;
            mcell.setCellValue(fmlaResult);
            assertEquals(fmlaResult, mcell.getNumericCellValue(), 0);
        }
        workbook.close();
    }

    @Test
    public void testModifyArrayCells_setCellType() throws IOException {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();

        // single-cell array formulas behave just like normal cells -
        // changing cell type removes the array formula and associated cached result
        CellRange<? extends Cell> srange =
                sheet.setArrayFormula("SUM(A4:A6,B4:B6)", CellRangeAddress.valueOf("B5"));
        Cell scell = srange.getTopLeftCell();
        assertEquals(CellType.FORMULA, scell.getCellTypeEnum());
        assertEquals(0.0, scell.getNumericCellValue(), 0);
        scell.setCellType(CellType.STRING);
        assertEquals(CellType.STRING, scell.getCellTypeEnum());
        scell.setCellValue("string cell");
        assertEquals("string cell", scell.getStringCellValue());

        //once you create a multi-cell array formula, you cannot change the type of its cells
        CellRange<? extends Cell> mrange =
                sheet.setArrayFormula("A1:A3*B1:B3", CellRangeAddress.valueOf("C1:C3"));
        for(Cell mcell : mrange){
            try {
                assertEquals(CellType.FORMULA, mcell.getCellTypeEnum());
                mcell.setCellType(CellType.NUMERIC);
                fail("expected exception");
            } catch (IllegalStateException e){
                CellReference ref = new CellReference(mcell);
                String msg = "Cell "+ref.formatAsString()+" is part of a multi-cell array formula. You cannot change part of an array.";
                assertEquals(msg, e.getMessage());
            }
            // a failed invocation of Cell.setCellType leaves the cell
            // in the state that it was in prior to the invocation
            assertEquals(CellType.FORMULA, mcell.getCellTypeEnum());
            assertTrue(mcell.isPartOfArrayFormulaGroup());
        }
        workbook.close();
    }

    @Test
    public void testModifyArrayCells_setCellFormula() throws IOException {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();

        CellRange<? extends Cell> srange =
                sheet.setArrayFormula("SUM(A4:A6,B4:B6)", CellRangeAddress.valueOf("B5"));
        Cell scell = srange.getTopLeftCell();
        assertEquals("SUM(A4:A6,B4:B6)", scell.getCellFormula());
        assertEquals(CellType.FORMULA, scell.getCellTypeEnum());
        assertTrue(scell.isPartOfArrayFormulaGroup());
        scell.setCellFormula("SUM(A4,A6)");
        //we are now a normal formula cell
        assertEquals("SUM(A4,A6)", scell.getCellFormula());
        assertFalse(scell.isPartOfArrayFormulaGroup());
        assertEquals(CellType.FORMULA, scell.getCellTypeEnum());
        //check that setting formula result works
        assertEquals(0.0, scell.getNumericCellValue(), 0);
        scell.setCellValue(33.0);
        assertEquals(33.0, scell.getNumericCellValue(), 0);

        //multi-cell array formula
        CellRange<? extends Cell> mrange =
                sheet.setArrayFormula("A1:A3*B1:B3", CellRangeAddress.valueOf("C1:C3"));
        for(Cell mcell : mrange){
            //we cannot set individual formulas for cells included in an array formula
            try {
                assertEquals("A1:A3*B1:B3", mcell.getCellFormula());
                mcell.setCellFormula("A1+A2");
                fail("expected exception");
            } catch (IllegalStateException e){
                CellReference ref = new CellReference(mcell);
                String msg = "Cell "+ref.formatAsString()+" is part of a multi-cell array formula. You cannot change part of an array.";
                assertEquals(msg, e.getMessage());
            }
            // a failed invocation of Cell.setCellFormula leaves the cell
            // in the state that it was in prior to the invocation
            assertEquals("A1:A3*B1:B3", mcell.getCellFormula());
            assertTrue(mcell.isPartOfArrayFormulaGroup());
        }
        workbook.close();
    }

    @Test
    public void testModifyArrayCells_removeCell() throws IOException {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();

        //single-cell array formulas behave just like normal cells
        CellRangeAddress cra = CellRangeAddress.valueOf("B5");
        CellRange<? extends Cell> srange =
                sheet.setArrayFormula("SUM(A4:A6,B4:B6)", cra);
        Cell scell = srange.getTopLeftCell();

        Row srow = sheet.getRow(cra.getFirstRow());
        assertSame(srow, scell.getRow());
        srow.removeCell(scell);
        assertNull(srow.getCell(cra.getFirstColumn()));

        //re-create the removed cell
        scell = srow.createCell(cra.getFirstColumn());
        assertEquals(CellType.BLANK, scell.getCellTypeEnum());
        assertFalse(scell.isPartOfArrayFormulaGroup());

        //we cannot remove cells included in a multi-cell array formula
        CellRange<? extends Cell> mrange =
                sheet.setArrayFormula("A1:A3*B1:B3", CellRangeAddress.valueOf("C1:C3"));
        for(Cell mcell : mrange){
            int columnIndex = mcell.getColumnIndex();
            Row mrow = mcell.getRow();
            try {
                mrow.removeCell(mcell);
                fail("expected exception");
            } catch (IllegalStateException e){
                CellReference ref = new CellReference(mcell);
                String msg = "Cell "+ref.formatAsString()+" is part of a multi-cell array formula. You cannot change part of an array.";
                assertEquals(msg, e.getMessage());
            }
            // a failed invocation of Row.removeCell leaves the row
            // in the state that it was in prior to the invocation
            assertSame(mcell, mrow.getCell(columnIndex));
            assertTrue(mcell.isPartOfArrayFormulaGroup());
            assertEquals(CellType.FORMULA, mcell.getCellTypeEnum());
        }
        
        workbook.close();
    }

    @Test
    public void testModifyArrayCells_removeRow() throws IOException {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();

        //single-cell array formulas behave just like normal cells
        CellRangeAddress cra = CellRangeAddress.valueOf("B5");
        CellRange<? extends Cell> srange =
                sheet.setArrayFormula("SUM(A4:A6,B4:B6)", cra);
        Cell scell = srange.getTopLeftCell();
        assertEquals(CellType.FORMULA, scell.getCellTypeEnum());

        Row srow = scell.getRow();
        assertSame(srow, sheet.getRow(cra.getFirstRow()));
        sheet.removeRow(srow);
        assertNull(sheet.getRow(cra.getFirstRow()));

        //re-create the removed row and cell
        scell = sheet.createRow(cra.getFirstRow()).createCell(cra.getFirstColumn());
        assertEquals(CellType.BLANK, scell.getCellTypeEnum());
        assertFalse(scell.isPartOfArrayFormulaGroup());

        //we cannot remove rows with cells included in a multi-cell array formula
        CellRange<? extends Cell> mrange =
                sheet.setArrayFormula("A1:A3*B1:B3", CellRangeAddress.valueOf("C1:C3"));
        for(Cell mcell : mrange){
            int columnIndex = mcell.getColumnIndex();
            Row mrow = mcell.getRow();
            try {
                sheet.removeRow(mrow);
                fail("expected exception");
            } catch (IllegalStateException e){
                // String msg = "Row[rownum="+mrow.getRowNum()+"] contains cell(s) included in a multi-cell array formula. You cannot change part of an array.";
                // assertEquals(msg, e.getMessage());
            }
            // a failed invocation of Row.removeCell leaves the row
            // in the state that it was in prior to the invocation
            assertSame(mrow, sheet.getRow(mrow.getRowNum()));
            assertSame(mcell, mrow.getCell(columnIndex));
            assertTrue(mcell.isPartOfArrayFormulaGroup());
            assertEquals(CellType.FORMULA, mcell.getCellTypeEnum());
        }
        
        workbook.close();
    }

    @Test
    public void testModifyArrayCells_mergeCellsSingle() throws IOException {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();
        assertEquals(0, sheet.getNumMergedRegions());

        //single-cell array formulas behave just like normal cells
        CellRange<? extends Cell> srange =
                sheet.setArrayFormula("SUM(A4:A6,B4:B6)", CellRangeAddress.valueOf("B5"));
        Cell scell = srange.getTopLeftCell();
        sheet.addMergedRegion(CellRangeAddress.valueOf("B5:C6"));
        //we are still an array formula
        assertEquals(CellType.FORMULA, scell.getCellTypeEnum());
        assertTrue(scell.isPartOfArrayFormulaGroup());
        assertEquals(1, sheet.getNumMergedRegions());
        
        workbook.close();
    }
    
    @Test
    public void testModifyArrayCells_mergeCellsMulti() throws IOException {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();
        int expectedNumMergedRegions = 0;
        assertEquals(expectedNumMergedRegions, sheet.getNumMergedRegions());

        // we cannot merge cells included in an array formula
        sheet.setArrayFormula("A1:A4*B1:B4", CellRangeAddress.valueOf("C2:F5"));
        for (String ref : Arrays.asList(
                "C2:F5", // identity
                "D3:E4", "B1:G6", // contains
                "B1:C2", "F1:G2", "F5:G6", "B5:C6", // 1x1 corner intersection
                "B1:C6", "B1:G2", "F1:G6", "B5:G6", // 1-row/1-column intersection
                "B1:D3", "E1:G3", "E4:G6", "B4:D6", // 2x2 corner intersection
                "B1:D6", "B1:G3", "E1:G6", "B4:G6"  // 2-row/2-column intersection
        )) {
            CellRangeAddress cra = CellRangeAddress.valueOf(ref);
            try {
                sheet.addMergedRegion(cra);
                fail("expected exception with ref " + ref);
            } catch (IllegalStateException e) {
                String msg = "The range "+cra.formatAsString()+" intersects with a multi-cell array formula. You cannot merge cells of an array.";
                assertEquals(msg, e.getMessage());
            }
        }
        //the number of merged regions remains the same
        assertEquals(expectedNumMergedRegions, sheet.getNumMergedRegions());
        
        // we can merge non-intersecting cells
        for (String ref : Arrays.asList(
                "C1:F1", //above
                "G2:G5", //right
                "C6:F6",  //bottom
                "B2:B5", "H7:J9")) {
            CellRangeAddress cra = CellRangeAddress.valueOf(ref);
            try {
                sheet.addMergedRegion(cra);
                expectedNumMergedRegions++;
                assertEquals(expectedNumMergedRegions, sheet.getNumMergedRegions());
            } catch (IllegalStateException e) {
                fail("did not expect exception with ref: " + ref + "\n" + e.getMessage());
            }
        }
        
        workbook.close();
    }

    @Test
    public void testModifyArrayCells_shiftRows() throws IOException {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();

        //single-cell array formulas behave just like normal cells - we can change the cell type
        CellRange<? extends Cell> srange =
                sheet.setArrayFormula("SUM(A4:A6,B4:B6)", CellRangeAddress.valueOf("B5"));
        Cell scell = srange.getTopLeftCell();
        assertEquals("SUM(A4:A6,B4:B6)", scell.getCellFormula());
        sheet.shiftRows(0, 0, 1);
        sheet.shiftRows(0, 1, 1);

        //we cannot set individual formulas for cells included in an array formula
        sheet.setArrayFormula("A1:A3*B1:B3", CellRangeAddress.valueOf("C1:C3"));

        try {
            sheet.shiftRows(0, 0, 1);
            fail("expected exception");
        } catch (IllegalStateException e){
            String msg = "Row[rownum=0] contains cell(s) included in a multi-cell array formula. You cannot change part of an array.";
            assertEquals(msg, e.getMessage());
        }
        /*
         TODO: enable shifting the whole array

        sheet.shiftRows(0, 2, 1);
        //the array C1:C3 is now C2:C4
        CellRangeAddress cra = CellRangeAddress.valueOf("C2:C4");
        for(Cell mcell : mrange){
            //TODO define equals and hashcode for CellRangeAddress
            assertEquals(cra.formatAsString(), mcell.getArrayFormulaRange().formatAsString());
            assertEquals("A2:A4*B2:B4", mcell.getCellFormula());
            assertTrue(mcell.isPartOfArrayFormulaGroup());
            assertEquals(CellType.FORMULA, mcell.getCellTypeEnum());
        }

        */
        workbook.close();
    }
    
    @Ignore
    @Test
    public void shouldNotBeAbleToCreateArrayFormulaOnPreexistingMergedRegion() throws IOException {
        /*
         *  m  = merged region
         *  f  = array formula
         *  fm = cell belongs to a merged region and an array formula (illegal, that's what this tests for)
         *  
         *   A  B  C
         * 1    f  f
         * 2    fm fm
         * 3    f  f
         */
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();
        
        CellRangeAddress mergedRegion = CellRangeAddress.valueOf("B2:C2");
        sheet.addMergedRegion(mergedRegion);
        CellRangeAddress arrayFormula = CellRangeAddress.valueOf("C1:C3");
        assumeTrue(mergedRegion.intersects(arrayFormula));
        assumeTrue(arrayFormula.intersects(mergedRegion));
        try {
            sheet.setArrayFormula("SUM(A1:A3)",  arrayFormula);
            fail("expected exception: should not be able to create an array formula that intersects with a merged region");
        } catch (IllegalStateException e) {
            // expected
        }
        
        workbook.close();
    }
}
