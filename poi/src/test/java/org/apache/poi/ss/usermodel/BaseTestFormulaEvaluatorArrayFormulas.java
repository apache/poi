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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.Test;

/**
 * Tests formula evaluation of array formulas (what Excel shows as {@code {=...}}): one formula
 * shared by a range of cells, evaluated in array mode so that an area operand is not reduced to
 * the single value on the formula's row, and with each cell of the range receiving the element
 * of the result that matches its position.
 * <p>
 * The evaluator treats every cell of the range as a formula cell of its own (all of them hold the
 * same tokens), so the cache notifications work per cell exactly as for plain formulas.
 *
 * @see BaseTestFormulaEvaluatorFixture for the workbook the tests run against
 */
public abstract class BaseTestFormulaEvaluatorArrayFormulas extends BaseTestFormulaEvaluatorFixture {

    protected BaseTestFormulaEvaluatorArrayFormulas(ITestDataProvider testDataProvider) {
        super(testDataProvider);
    }

    /** puts an array formula over the given range of the Data sheet and tells the evaluator about every cell */
    protected CellRange<? extends Cell> arrayFormula(String formula, String range) {
        CellRange<? extends Cell> cells = data.setArrayFormula(formula, CellRangeAddress.valueOf(range));
        for (Cell c : cells) {
            fe.notifySetFormula(c);
        }
        return cells;
    }

    /** puts a plain formula into a (possibly new) cell of the Data sheet and tells the evaluator */
    protected Cell formula(String ref, String formula) {
        CellReference cr = new CellReference(ref);
        Row row = data.getRow(cr.getRow());
        if (row == null) {
            row = data.createRow(cr.getRow());
        }
        Cell cell = row.createCell(cr.getCol());
        cell.setCellFormula(formula);
        fe.notifySetFormula(cell);
        return cell;
    }

    private void assertArrayCell(String ref, String formula, String range) {
        Cell c = cell(ref);
        assertTrue(c.isPartOfArrayFormulaGroup(), ref + " is part of the array formula");
        assertEquals(CellType.FORMULA, c.getCellType(), ref + " is a formula cell");
        assertEquals(formula, c.getCellFormula(), ref + " formula text");
        assertEquals(range, c.getArrayFormulaRange().formatAsString(), ref + " array range");
    }

    @Test
    void multiCellArrayFormulaGivesEachCellItsElement() {
        arrayFormula("A2:A4*C2:C4", "E2:E4");
        for (String ref : new String[]{"E2", "E3", "E4"}) {
            assertArrayCell(ref, "A2:A4*C2:C4", "E2:E4");
        }
        assertEquals(50, num("E2"), DELTA);
        assertEquals(140, num("E3"), DELTA);
        assertEquals(270, num("E4"), DELTA);

        // evaluateAll writes the result of each cell into that cell
        fe.evaluateAll();
        assertEquals(CellType.NUMERIC, cell("E3").getCachedFormulaResultType());
        assertEquals(50, cell("E2").getNumericCellValue(), DELTA);
        assertEquals(140, cell("E3").getNumericCellValue(), DELTA);
        assertEquals(270, cell("E4").getNumericCellValue(), DELTA);
    }

    @Test
    void singleCellArrayFormulaAggregatesWhereAPlainFormulaCannot() {
        // in an array formula the areas are multiplied element-wise and the products summed ...
        arrayFormula("SUM(A2:A4*C2:C4)", "E6");
        assertArrayCell("E6", "SUM(A2:A4*C2:C4)", "E6");
        assertEquals(460, num("E6"), DELTA);

        // ... whereas a plain formula picks the single value of each area on its own row, and
        // row 7 lies outside both areas
        formula("E7", "SUM(A2:A4*C2:C4)");
        assertEquals(FormulaError.VALUE, err("E7"));
        // on a row inside the areas the plain formula sees just that row's values
        formula("F3", "SUM(A2:A4*C2:C4)");
        assertEquals(140, num("F3"), DELTA);
    }

    @Test
    void arrayFormulaOverFormulaCellsFollowsTheChain() {
        // D2:D4 are VLOOKUP formulas, and F6 is a plain formula over the array formula's cells
        arrayFormula("A2:A4*D2:D4", "F2:F4");
        formula("F6", "SUM(F2:F4)");
        assertEquals(15, num("F2"), DELTA);
        assertEquals(50, num("F3"), DELTA);
        assertEquals(105, num("F4"), DELTA);
        assertEquals(num("D6"), num("F6"), DELTA);

        // a change at the far end of the chain propagates through the lookup, the array formula
        // and the plain formula reading it
        cell("Prices!B2").setCellValue(2);
        fe.notifyUpdateCell(cell("Prices!B2"));
        assertEquals(2, num("D2"), DELTA);
        assertEquals(20, num("F2"), DELTA);
        assertEquals(175, num("F6"), DELTA);
        assertEquals(175, num("D6"), DELTA);
    }

    @Test
    void notifyUpdateCellReachesEveryCellOfTheGroup() {
        arrayFormula("A2:A4*C2:C4", "E2:E4");
        formula("E6", "SUM(E2:E4)");
        assertEquals(460, num("E6"), DELTA);

        // without a notification the cached results are served
        cell("A3").setCellValue(25);
        assertEquals(140, num("E3"), DELTA);
        assertEquals(460, num("E6"), DELTA);

        // every cell of the group read A2:A4, so every one of them is invalidated
        fe.notifyUpdateCell(cell("A3"));
        assertEquals(50, num("E2"), DELTA);
        assertEquals(175, num("E3"), DELTA);
        assertEquals(270, num("E4"), DELTA);
        assertEquals(495, num("E6"), DELTA);

        cell("C4").setCellValue(10);
        fe.notifyUpdateCell(cell("C4"));
        assertEquals(300, num("E4"), DELTA);
        assertEquals(525, num("E6"), DELTA);
    }

    @Test
    void rangeLargerThanTheResultGivesNaInTheExtraCells() {
        // the result has three rows, the range four
        arrayFormula("A2:A4*C2:C4", "E2:E5");
        assertEquals(50, num("E2"), DELTA);
        assertEquals(140, num("E3"), DELTA);
        assertEquals(270, num("E4"), DELTA);
        assertEquals(FormulaError.NA, err("E5"));
    }

    @Test
    void singleColumnAndSingleRowResultsAreBroadcast() {
        // a one-column result fills every column of the range
        arrayFormula("A2:A4*C2:C4", "E2:F4");
        assertEquals(50, num("E2"), DELTA);
        assertEquals(50, num("F2"), DELTA);
        assertEquals(140, num("E3"), DELTA);
        assertEquals(140, num("F3"), DELTA);
        assertEquals(270, num("F4"), DELTA);

        // a one-row result fills every row of the range
        arrayFormula("{1,2,3}", "G6:I7");
        assertEquals(1, num("G6"), DELTA);
        assertEquals(3, num("I6"), DELTA);
        assertEquals(1, num("G7"), DELTA);
        assertEquals(3, num("I7"), DELTA);

        // and a scalar fills the whole range
        arrayFormula("SUM(A2:A4)", "G2:H3");
        assertEquals(60, num("G2"), DELTA);
        assertEquals(60, num("H3"), DELTA);
    }

    @Test
    void constantArraysFillTheRangeByPosition() {
        arrayFormula("{1;2;3}", "E2:E4");
        assertEquals(1, num("E2"), DELTA);
        assertEquals(2, num("E3"), DELTA);
        assertEquals(3, num("E4"), DELTA);

        arrayFormula("{10,\"x\";TRUE,4}", "F6:G7");
        assertEquals(10, num("F6"), DELTA);
        assertEquals("x", str("G6"));
        assertEquals(CellType.BOOLEAN, fe.evaluate(cell("F7")).getCellType());
        assertTrue(fe.evaluate(cell("F7")).getBooleanValue());
        assertEquals(4, num("G7"), DELTA);

        // constants can be mixed with cell areas
        arrayFormula("A2:A4*{1;10;100}", "H2:H4");
        assertEquals(10, num("H2"), DELTA);
        assertEquals(200, num("H3"), DELTA);
        assertEquals(3000, num("H4"), DELTA);
    }

    @Test
    void ifInsideAnArrayFormulaEvaluatesElementWise() {
        // IF is otherwise evaluated lazily on the single value of its condition; in an array
        // formula the condition is an array and both branches are evaluated for every element
        arrayFormula("SUM(IF(A2:A4>15,A2:A4,0))", "E6");
        assertEquals(50, num("E6"), DELTA);
        arrayFormula("SUM(IF(B2:B4=\"Gadget\",C2:C4,0))", "E7");
        assertEquals(7, num("E7"), DELTA);

        arrayFormula("IF(A2:A4>15,\"big\",\"small\")", "E2:E4");
        assertEquals("small", str("E2"));
        assertEquals("big", str("E3"));
        assertEquals("big", str("E4"));

        // and the results track the inputs
        cell("A2").setCellValue(16);
        fe.notifyUpdateCell(cell("A2"));
        assertEquals("big", str("E2"));
        assertEquals(66, num("E6"), DELTA);
    }

    @Test
    void matrixFunctionsProduceArrays() {
        arrayFormula("TRANSPOSE(A2:A4)", "E2:G2");
        assertEquals(10, num("E2"), DELTA);
        assertEquals(20, num("F2"), DELTA);
        assertEquals(30, num("G2"), DELTA);

        // (1x3) x (3x1) = (1x1): the dot product of quantities and costs
        arrayFormula("MMULT(TRANSPOSE(A2:A4),C2:C4)", "E6");
        assertEquals(460, num("E6"), DELTA);

        // (3x1) x (1x3) = (3x3)
        arrayFormula("MMULT(A2:A4,TRANSPOSE(C2:C4))", "E7:G9");
        assertEquals(50, num("E7"), DELTA);   // 10*5
        assertEquals(90, num("G7"), DELTA);   // 10*9
        assertEquals(140, num("F8"), DELTA);  // 20*7
        assertEquals(150, num("E9"), DELTA);  // 30*5
        assertEquals(270, num("G9"), DELTA);  // 30*9
    }

    @Test
    void crossSheetAndNamedReferencesWorkInsideArrayFormulas() {
        // the Prices rows happen to be in the same order as the Data rows
        arrayFormula("A2:A4*Prices!B2:B4", "E2:E4");
        assertEquals(15, num("E2"), DELTA);
        assertEquals(50, num("E3"), DELTA);
        assertEquals(105, num("E4"), DELTA);

        // Total is Data!$A$6, a single cell, so it is a scalar in the array operation
        arrayFormula("A2:A4/Total", "F2:F4");
        assertEquals(10.0 / 60, num("F2"), DELTA);
        assertEquals(20.0 / 60, num("F3"), DELTA);
        assertEquals(30.0 / 60, num("F4"), DELTA);

        cell("Prices!B4").setCellValue(4);
        fe.notifyUpdateCell(cell("Prices!B4"));
        assertEquals(120, num("E4"), DELTA);
        assertEquals(15, num("E2"), DELTA);

        cell("A2").setCellValue(40);
        fe.notifyUpdateCell(cell("A2"));
        assertEquals(90, num("A6"), DELTA);
        assertEquals(40.0 / 90, num("F2"), DELTA);
        assertEquals(30.0 / 90, num("F4"), DELTA);
    }

    @Test
    void replacingAnArrayFormulaNeedsNotifySetFormula() {
        arrayFormula("A2:A4*C2:C4", "E2:E4");
        assertEquals(50, num("E2"), DELTA);

        // setting a new array formula over the same range changes the cells but the evaluator
        // still holds the old results until told
        CellRange<? extends Cell> cells = data.setArrayFormula("A2:A4+C2:C4", CellRangeAddress.valueOf("E2:E4"));
        assertEquals("A2:A4+C2:C4", cell("E3").getCellFormula());
        assertEquals(50, num("E2"), DELTA);

        for (Cell c : cells) {
            fe.notifySetFormula(c);
        }
        assertEquals(15, num("E2"), DELTA);
        assertEquals(27, num("E3"), DELTA);
        assertEquals(39, num("E4"), DELTA);
        for (String ref : new String[]{"E2", "E3", "E4"}) {
            assertArrayCell(ref, "A2:A4+C2:C4", "E2:E4");
        }

        // the replacement did not leave the old array formula behind: one removal clears the range
        for (Cell c : data.removeArrayFormula(cell("E4"))) {
            assertFalse(c.isPartOfArrayFormulaGroup(), c.getAddress() + " is no longer part of an array formula");
            assertEquals(CellType.BLANK, c.getCellType(), c.getAddress() + " is blank");
            fe.notifyUpdateCell(c);
        }
        formula("E6", "COUNT(E2:E4)");
        assertEquals(0, num("E6"), DELTA);
    }

    @Test
    void removingAnArrayFormulaBlanksItsCells() {
        arrayFormula("A2:A4*D2:D4", "F2:F4");
        formula("F6", "SUM(F2:F4)");
        formula("F7", "COUNT(F2:F4)");
        assertEquals(170, num("F6"), DELTA);
        assertEquals(3, num("F7"), DELTA);

        // removal leaves blank cells behind, which is a cell change the evaluator can be told about
        CellRange<? extends Cell> removed = data.removeArrayFormula(cell("F3"));
        for (Cell c : removed) {
            assertFalse(c.isPartOfArrayFormulaGroup());
            assertEquals(CellType.BLANK, c.getCellType());
            fe.notifyUpdateCell(c);
        }
        assertEquals(0, num("F6"), DELTA);
        assertEquals(0, num("F7"), DELTA);

        // the range can be reused for a plain formula
        formula("F2", "A2*2");
        assertEquals(20, num("F2"), DELTA);
        assertFalse(cell("F2").isPartOfArrayFormulaGroup());
        assertEquals(20, num("F6"), DELTA);
        assertEquals(1, num("F7"), DELTA);
    }

    @Test
    void arrayFormulasSurviveWriteOutAndReadBack() throws IOException {
        arrayFormula("A2:A4*C2:C4", "E2:E4");
        arrayFormula("SUM(A2:A4*D2:D4)", "E6");
        arrayFormula("IF(A2:A4>15,\"big\",\"small\")", "F2:F4");
        fe.evaluateAll();

        try (Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb)) {
            Sheet data2 = wb2.getSheet("Data");
            Cell e3 = data2.getRow(2).getCell(4);
            assertTrue(e3.isPartOfArrayFormulaGroup());
            assertEquals("E2:E4", e3.getArrayFormulaRange().formatAsString());
            assertEquals("A2:A4*C2:C4", e3.getCellFormula());
            Cell e6 = data2.getRow(5).getCell(4);
            assertTrue(e6.isPartOfArrayFormulaGroup());
            assertEquals("E6", e6.getArrayFormulaRange().formatAsString());
            assertEquals("SUM(A2:A4*D2:D4)", e6.getCellFormula());
            Cell f4 = data2.getRow(3).getCell(5);
            assertEquals("F2:F4", f4.getArrayFormulaRange().formatAsString());

            // the results written by evaluateAll were saved with the cells
            assertEquals(140, e3.getNumericCellValue(), DELTA);
            assertEquals(170, e6.getNumericCellValue(), DELTA);
            assertEquals("big", f4.getStringCellValue());

            // and a fresh evaluator on the reloaded workbook computes the same
            FormulaEvaluator fe2 = wb2.getCreationHelper().createFormulaEvaluator();
            assertEquals(50, fe2.evaluate(data2.getRow(1).getCell(4)).getNumberValue(), DELTA);
            assertEquals(140, fe2.evaluate(e3).getNumberValue(), DELTA);
            assertEquals(270, fe2.evaluate(data2.getRow(3).getCell(4)).getNumberValue(), DELTA);
            assertEquals(170, fe2.evaluate(e6).getNumberValue(), DELTA);
            assertEquals("small", fe2.evaluate(data2.getRow(1).getCell(5)).getStringValue());
            assertEquals("big", fe2.evaluate(f4).getStringValue());
        }
    }
}
