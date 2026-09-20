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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.poi.ss.ITestDataProvider;
import org.junit.jupiter.api.Test;

/**
 * Tests that the {@link FormulaEvaluator} produces correct results after rows or columns have been
 * added or removed once the formulas have already been evaluated.
 * <p>
 * Rows and columns can be added or removed in place (creating and removing cells, leaving every
 * other cell where it is) or with {@link Sheet#shiftRows(int, int, int)} and
 * {@link Sheet#shiftColumns(int, int, int)}.
 * <p>
 * The evaluator only supports updating its caches for individual cell changes, reported with
 * {@link FormulaEvaluator#notifyUpdateCell(Cell)}, {@link FormulaEvaluator#notifySetFormula(Cell)}
 * and {@link FormulaEvaluator#notifyDeleteCell(Cell)}. In-place changes are a series of such cell
 * changes, so the tests report them cell by cell. A shift is a large operation: it moves cells to
 * new positions and rewrites the formulas (and defined names) that refer to them, on every sheet,
 * which leaves the caches wrong in ways the cell notifications cannot express. There is no
 * notification for that, so after a shift the tests call
 * {@link FormulaEvaluator#clearAllCachedResultValues()} (see {@link #afterShift()}) and every
 * formula is evaluated afresh.
 *
 * @see BaseTestFormulaEvaluatorFixture for the workbook the tests run against
 */
public abstract class BaseTestFormulaEvaluatorRowsAndColumns extends BaseTestFormulaEvaluatorFixture {

    protected BaseTestFormulaEvaluatorRowsAndColumns(ITestDataProvider testDataProvider) {
        super(testDataProvider);
    }

    /**
     * Called after {@link Sheet#shiftRows(int, int, int)} or {@link Sheet#shiftColumns(int, int, int)}.
     * <p>
     * The evaluator can update its caches for individual cell changes (the {@code notify*}
     * methods) but not for a large operation like a shift, which moves cells and rewrites
     * formulas across the whole workbook. The only correct thing to do is to discard every cached
     * result and let the formulas be re-evaluated.
     */
    protected void afterShift() {
        fe.clearAllCachedResultValues();
    }

    /** @return the row, creating it if the shift left no row object at that index */
    protected static Row rowAt(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        return row == null ? sheet.createRow(rowIndex) : row;
    }

    protected String formula(String ref) {
        return cell(ref).getCellFormula();
    }

    protected String nameFormula(String name) {
        return wb.getName(name).getRefersToFormula();
    }

    /** reports every cell of the row as deleted and removes the row, without shifting anything */
    protected void removeRowInPlace(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        for (Cell c : row) {
            fe.notifyDeleteCell(c);
        }
        sheet.removeRow(row);
    }

    /** reports every cell of the column as deleted and removes them, without shifting anything */
    protected void removeColumnInPlace(Sheet sheet, int columnIndex) {
        for (Row row : sheet) {
            Cell c = row.getCell(columnIndex);
            if (c != null) {
                fe.notifyDeleteCell(c);
                row.removeCell(c);
            }
        }
    }

    // --------------------------------------- rows and columns changed in place (no shifting)
    // Each of these is a series of individual cell changes, which the evaluator's cell-level
    // notifications cover, so nothing more than notify* is needed for the results to be right.

    @Test
    void removeDataRowInPlace() {
        // the Gadget row disappears, everything else stays where it is
        removeRowInPlace(data, 2);

        assertEquals(40, num("A6"), DELTA);
        assertEquals(14, num("C6"), DELTA);
        assertEquals(120, num("D6"), DELTA);
        assertEquals(80, num("A7"), DELTA);
        assertEquals(94, num("B7"), DELTA);
        assertEquals(1, num("C7"), DELTA);
        assertEquals(94.0 / 3, num("A8"), DELTA);
        assertEquals("small", str("B8"));
        assertEquals("Widget-small", str("C8"));
        assertEquals(94.0 / 3 + 80, num("A9"), DELTA);
        assertEquals(41, num("B9"), DELTA);
        assertEquals(1.5, num("D2"), DELTA);
        assertEquals(3.5, num("D4"), DELTA);
    }

    @Test
    void removeHeaderRowInPlace() {
        removeRowInPlace(data, 0);
        removeRowInPlace(prices, 0);
        assertInitialValues();
    }

    @Test
    void addDataRowInPlaceThenExtendRanges() {
        // fill the spare row 5; the existing ranges stop at row 4 so nothing changes yet
        Row added = data.createRow(4);
        Cell a5 = added.createCell(0);
        a5.setCellValue(40);
        fe.notifyUpdateCell(a5);
        Cell b5 = added.createCell(1);
        b5.setCellValue("Widget");
        fe.notifyUpdateCell(b5);
        Cell c5 = added.createCell(2);
        c5.setCellValue(11);
        fe.notifyUpdateCell(c5);
        Cell d5 = added.createCell(3);
        d5.setCellFormula("VLOOKUP(B5,Prices!$A$2:$B$4,2,FALSE)");
        fe.notifySetFormula(d5);

        assertEquals(1.5, num("D5"), DELTA);
        assertInitialValues();

        // extend the totals over the new row
        cell("A6").setCellFormula("SUM(A2:A5)");
        fe.notifySetFormula(cell("A6"));
        cell("C6").setCellFormula("SUM(C2:C5)");
        fe.notifySetFormula(cell("C6"));
        cell("D6").setCellFormula("SUMPRODUCT(A2:A5,D2:D5)");
        fe.notifySetFormula(cell("D6"));
        cell("C7").setCellFormula("COUNTIF(A2:A5,\">15\")");
        fe.notifySetFormula(cell("C7"));

        assertEquals(100, num("A6"), DELTA);
        assertEquals(32, num("C6"), DELTA);
        assertEquals(230, num("D6"), DELTA);
        assertEquals(200, num("A7"), DELTA);
        assertEquals(232, num("B7"), DELTA);
        assertEquals(3, num("C7"), DELTA);
        assertEquals(232.0 / 3, num("A8"), DELTA);
        assertEquals(232.0 / 3 + 200, num("A9"), DELTA);
        assertEquals(103, num("B9"), DELTA);

        // the added row is a tracked input of the extended ranges
        a5.setCellValue(0);
        fe.notifyUpdateCell(a5);
        assertEquals(60, num("A6"), DELTA);
        assertEquals(170, num("D6"), DELTA);
    }

    @Test
    void addLookupRowInPlaceThenExtendLookupRange() {
        Row added = prices.createRow(4);
        Cell a5 = added.createCell(0);
        a5.setCellValue("Sprocket");
        fe.notifyUpdateCell(a5);
        Cell b5 = added.createCell(1);
        b5.setCellValue(9);
        fe.notifyUpdateCell(b5);

        cell("B2").setCellValue("Sprocket");
        fe.notifyUpdateCell(cell("B2"));
        // not in the lookup range yet
        assertEquals(FormulaError.NA, err("D2"));

        cell("D2").setCellFormula("VLOOKUP(B2,Prices!$A$2:$B$5,2,FALSE)");
        fe.notifySetFormula(cell("D2"));
        assertEquals(9, num("D2"), DELTA);
        assertEquals(245, num("D6"), DELTA);
    }

    @Test
    void removeCostColumnInPlace() {
        // takes the Cost values and the formulas in C6:C8 with it
        removeColumnInPlace(data, 2);
        assertNull(data.getRow(5).getCell(2));
        assertNull(data.getRow(7).getCell(2));

        // references into the removed column now see blanks
        assertEquals(120, num("B7"), DELTA);
        assertEquals(40, num("A8"), DELTA);
        assertEquals("small", str("B8"));
        assertEquals(160, num("A9"), DELTA);
        assertEquals(60, num("B9"), DELTA);
        // nothing else was touched
        assertEquals(60, num("A6"), DELTA);
        assertEquals(170, num("D6"), DELTA);

        // and the blanks are tracked: putting a cell back is picked up
        Cell c6 = data.getRow(5).createCell(2);
        c6.setCellValue(30);
        fe.notifyUpdateCell(c6);
        assertEquals(150, num("B7"), DELTA);
        assertEquals(50, num("A8"), DELTA);
        assertEquals("big", str("B8"));
    }

    @Test
    void addColumnInPlaceAndUseItFromExistingFormula() {
        double[] discounts = {0.1, 0.2, 0.3};
        for (int r = 1; r <= 3; r++) {
            Cell c = data.getRow(r).createCell(4);
            c.setCellValue(discounts[r - 1]);
            fe.notifyUpdateCell(c);
        }
        Cell e6 = data.getRow(5).createCell(4);
        e6.setCellFormula("SUMPRODUCT(A2:A4,E2:E4)");
        fe.notifySetFormula(e6);
        assertEquals(14, num("E6"), DELTA);
        assertInitialValues();

        cell("B7").setCellFormula("A7+C6-E6");
        fe.notifySetFormula(cell("B7"));
        assertEquals(127, num("B7"), DELTA);
        assertEquals(127.0 / 3, num("A8"), DELTA);
        assertEquals("big", str("B8"));

        cell("E2").setCellValue(0.5);
        fe.notifyUpdateCell(cell("E2"));
        assertEquals(18, num("E6"), DELTA);
        assertEquals(123, num("B7"), DELTA);
        assertEquals(41, num("A8"), DELTA);
    }

    // ------------------------------------------------------------------ rows shifted
    // Shifting moves cells and rewrites formulas and names throughout the workbook. That is beyond
    // what the cell-level notifications can describe, so these tests clear the whole cache
    // (afterShift) and check that the fresh evaluation reflects the rewritten formulas.

    @Test
    void deleteHeaderRowShiftsEverythingUp() {
        data.shiftRows(1, data.getLastRowNum(), -1);
        afterShift();

        assertEquals("SUM(A1:A3)", formula("A5"));
        assertEquals("VLOOKUP(B1,Prices!$A$2:$B$4,2,FALSE)", formula("D1"));
        assertEquals("Data!$A$5", nameFormula("Total"));

        assertEquals(1.5, num("D1"), DELTA);
        assertEquals(60, num("A5"), DELTA);
        assertEquals(21, num("C5"), DELTA);
        assertEquals(170, num("D5"), DELTA);
        assertEquals(120, num("A6"), DELTA);
        assertEquals(141, num("B6"), DELTA);
        assertEquals(2, num("C6"), DELTA);
        assertEquals(47, num("A7"), DELTA);
        assertEquals("big", str("B7"));
        assertEquals("Widget-big", str("C7"));
        assertEquals(167, num("A8"), DELTA);
        assertEquals(62, num("B8"), DELTA);

        // cell notification keeps working at the new positions
        cell("A1").setCellValue(40);
        fe.notifyUpdateCell(cell("A1"));
        assertEquals(90, num("A5"), DELTA);
        assertEquals(247, num("A8"), DELTA);
        assertEquals(93, num("B8"), DELTA);
    }

    @Test
    void deleteSpareRowMovesFormulaBlockWithoutChangingResults() {
        data.shiftRows(5, data.getLastRowNum(), -1);
        afterShift();

        // the data rows did not move, so the ranges are unchanged
        assertEquals("SUM(A2:A4)", formula("A5"));
        assertEquals("A5*2", formula("A6"));
        assertEquals("Data!$A$5", nameFormula("Total"));

        assertEquals(60, num("A5"), DELTA);
        assertEquals(21, num("C5"), DELTA);
        assertEquals(170, num("D5"), DELTA);
        assertEquals(120, num("A6"), DELTA);
        assertEquals(141, num("B6"), DELTA);
        assertEquals(2, num("C6"), DELTA);
        assertEquals(47, num("A7"), DELTA);
        assertEquals("big", str("B7"));
        assertEquals("Widget-big", str("C7"));
        assertEquals(167, num("A8"), DELTA);
        assertEquals(62, num("B8"), DELTA);
    }

    @Test
    void deleteDataRowShrinksRanges() {
        // delete the Gadget row
        data.shiftRows(3, data.getLastRowNum(), -1);
        afterShift();

        assertEquals("SUM(A2:A3)", formula("A5"));
        assertEquals("SUMPRODUCT(A2:A3,D2:D3)", formula("D5"));
        assertEquals("COUNTIF(A2:A3,\">15\")", formula("C6"));
        assertEquals("Gizmo", cell("B3").getStringCellValue());

        assertEquals(1.5, num("D2"), DELTA);
        assertEquals(3.5, num("D3"), DELTA);
        assertEquals(40, num("A5"), DELTA);
        assertEquals(14, num("C5"), DELTA);
        assertEquals(120, num("D5"), DELTA);
        assertEquals(80, num("A6"), DELTA);
        assertEquals(94, num("B6"), DELTA);
        assertEquals(1, num("C6"), DELTA);
        assertEquals(94.0 / 3, num("A7"), DELTA);
        assertEquals("small", str("B7"));
        assertEquals("Widget-small", str("C7"));
        assertEquals(94.0 / 3 + 80, num("A8"), DELTA);
        assertEquals(41, num("B8"), DELTA);
    }

    @Test
    void insertRowInsideDataRangeGrowsRanges() {
        // insert a new data row before the Gizmo row
        data.shiftRows(3, data.getLastRowNum(), 1);
        Row inserted = rowAt(data, 3);
        inserted.createCell(0).setCellValue(40);
        inserted.createCell(1).setCellValue("Widget");
        inserted.createCell(2).setCellValue(11);
        inserted.createCell(3).setCellFormula("VLOOKUP(B4,Prices!$A$2:$B$4,2,FALSE)");
        afterShift();

        assertEquals("SUM(A2:A5)", formula("A7"));
        assertEquals("SUMPRODUCT(A2:A5,D2:D5)", formula("D7"));
        assertEquals("COUNTIF(A2:A5,\">15\")", formula("C8"));
        assertEquals("Data!$A$7", nameFormula("Total"));

        assertEquals(1.5, num("D4"), DELTA);
        assertEquals(3.5, num("D5"), DELTA);
        assertEquals(100, num("A7"), DELTA);
        assertEquals(32, num("C7"), DELTA);
        assertEquals(230, num("D7"), DELTA);
        assertEquals(200, num("A8"), DELTA);
        assertEquals(232, num("B8"), DELTA);
        assertEquals(3, num("C8"), DELTA);
        assertEquals(232.0 / 3, num("A9"), DELTA);
        assertEquals("big", str("B9"));
        assertEquals("Widget-big", str("C9"));
        assertEquals(232.0 / 3 + 200, num("A10"), DELTA);
        assertEquals(103, num("B10"), DELTA);

        // the inserted row is a real input now
        cell("A4").setCellValue(0);
        fe.notifyUpdateCell(cell("A4"));
        assertEquals(60, num("A7"), DELTA);
        assertEquals(170, num("D7"), DELTA);
        assertEquals(62, num("B10"), DELTA);
    }

    @Test
    void insertRowBelowDataRangeDoesNotGrowRanges() {
        // insert at the spare row: the data block is untouched, so the ranges must not grow
        data.shiftRows(4, data.getLastRowNum(), 1);
        Row inserted = rowAt(data, 4);
        inserted.createCell(0).setCellValue(99);
        inserted.createCell(1).setCellValue("Gizmo");
        inserted.createCell(2).setCellValue(99);
        afterShift();

        assertEquals("SUM(A2:A4)", formula("A7"));
        assertEquals("SUMPRODUCT(A2:A4,D2:D4)", formula("D7"));
        assertEquals("Data!$A$7", nameFormula("Total"));

        assertEquals(60, num("A7"), DELTA);
        assertEquals(21, num("C7"), DELTA);
        assertEquals(170, num("D7"), DELTA);
        assertEquals(120, num("A8"), DELTA);
        assertEquals(141, num("B8"), DELTA);
        assertEquals(2, num("C8"), DELTA);
        assertEquals(47, num("A9"), DELTA);
        assertEquals("big", str("B9"));
        assertEquals("Widget-big", str("C9"));
        assertEquals(167, num("A10"), DELTA);
        assertEquals(62, num("B10"), DELTA);
    }

    @Test
    void deleteRowOnLookupSheetShrinksLookupRange() {
        // delete the Gadget row from Prices
        prices.shiftRows(3, prices.getLastRowNum(), -1);
        afterShift();

        assertEquals("VLOOKUP(B2,Prices!$A$2:$B$3,2,FALSE)", formula("D2"));
        assertEquals("Widget", cell("Prices!A2").getStringCellValue());
        assertEquals("Gizmo", cell("Prices!A3").getStringCellValue());

        assertEquals(1.5, num("D2"), DELTA);
        assertEquals(FormulaError.NA, err("D3"));
        assertEquals(3.5, num("D4"), DELTA);
        assertEquals(FormulaError.NA, err("D6"));
        // the rest of the chain does not depend on the lookups
        assertEquals(167, num("A9"), DELTA);
        assertEquals(62, num("B9"), DELTA);
    }

    @Test
    void insertRowOnLookupSheetGrowsLookupRange() {
        prices.shiftRows(2, prices.getLastRowNum(), 1);
        Row inserted = rowAt(prices, 2);
        inserted.createCell(0).setCellValue("Sprocket");
        inserted.createCell(1).setCellValue(9);
        afterShift();

        assertEquals("VLOOKUP(B2,Prices!$A$2:$B$5,2,FALSE)", formula("D2"));
        assertInitialValues();

        // the new lookup row is usable
        cell("B2").setCellValue("Sprocket");
        fe.notifyUpdateCell(cell("B2"));
        assertEquals(9, num("D2"), DELTA);
        assertEquals(245, num("D6"), DELTA);
        assertEquals("Sprocket-big", str("C8"));
    }

    // --------------------------------------------------------------- columns shifted

    @Test
    void insertColumnInsideDataMovesReferences() {
        // insert a column between Item and Cost
        data.shiftColumns(2, 3, 1);
        for (int r = 1; r <= 3; r++) {
            rowAt(data, r).createCell(2).setCellValue("note");
        }
        afterShift();

        assertEquals("SUM(D2:D4)", formula("D6"));
        assertEquals("SUMPRODUCT(A2:A4,E2:E4)", formula("E6"));
        assertEquals("A7+D6", formula("B7"));
        assertEquals("CONCATENATE(B2,\"-\",B8)", formula("D8"));
        assertEquals("VLOOKUP(B2,Prices!$A$2:$B$4,2,FALSE)", formula("E2"));

        assertEquals(1.5, num("E2"), DELTA);
        assertEquals(60, num("A6"), DELTA);
        assertEquals(21, num("D6"), DELTA);
        assertEquals(170, num("E6"), DELTA);
        assertEquals(120, num("A7"), DELTA);
        assertEquals(141, num("B7"), DELTA);
        assertEquals(2, num("D7"), DELTA);
        assertEquals(47, num("A8"), DELTA);
        assertEquals("big", str("B8"));
        assertEquals("Widget-big", str("D8"));
        assertEquals(167, num("A9"), DELTA);
        assertEquals(62, num("B9"), DELTA);

        cell("D2").setCellValue(15);
        fe.notifyUpdateCell(cell("D2"));
        assertEquals(31, num("D6"), DELTA);
        assertEquals(151, num("B7"), DELTA);
    }

    @Test
    void deleteColumnReferencedByFormulasGivesRefError() {
        // delete the Cost column: the Price column moves into it
        data.shiftColumns(3, 3, -1);
        afterShift();

        assertEquals("SUMPRODUCT(A2:A4,C2:C4)", formula("C6"));
        assertEquals("A7+#REF!", formula("B7"));
        assertEquals("Total+#REF!", formula("B9"));
        // the Cost formulas that lived in column C were overwritten
        assertNull(data.getRow(6).getCell(2));
        assertNull(data.getRow(7).getCell(2));

        assertEquals(1.5, num("C2"), DELTA);
        assertEquals(2.5, num("C3"), DELTA);
        assertEquals(3.5, num("C4"), DELTA);
        assertEquals(60, num("A6"), DELTA);
        assertEquals(170, num("C6"), DELTA);
        assertEquals(120, num("A7"), DELTA);
        assertEquals(FormulaError.REF, err("B7"));
        assertEquals(FormulaError.REF, err("A8"));
        assertEquals(FormulaError.REF, err("B8"));
        assertEquals(FormulaError.REF, err("A9"));
        assertEquals(FormulaError.REF, err("B9"));
    }

    @Test
    void insertColumnOnLookupSheetMovesLookupRange() {
        prices.shiftColumns(0, 1, 1);
        afterShift();

        assertEquals("VLOOKUP(B2,Prices!$B$2:$C$4,2,FALSE)", formula("D2"));
        assertEquals("Widget", cell("Prices!B2").getStringCellValue());
        assertInitialValues();

        cell("Prices!C2").setCellValue(2);
        fe.notifyUpdateCell(cell("Prices!C2"));
        assertEquals(2, num("D2"), DELTA);
        assertEquals(175, num("D6"), DELTA);
    }
}
