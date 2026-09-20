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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.Test;

/**
 * Tests formula evaluation against hidden rows. Hidden rows are the one piece of sheet state
 * besides cell contents that the evaluator consults: {@code SUBTOTAL} with a function code above
 * 100 skips values in hidden rows (via {@code EvaluationSheet.isRowHidden}), while every other
 * function, and {@code SUBTOTAL} with codes 1-11, counts them.
 * <p>
 * Hiding a row changes results without any cell changing, so no {@code notify*} call describes
 * it; like a shift, it needs {@link FormulaEvaluator#clearAllCachedResultValues()} (see
 * {@link #afterVisibilityChange()}).
 *
 * @see BaseTestFormulaEvaluatorFixture for the workbook the tests run against
 */
public abstract class BaseTestFormulaEvaluatorHiddenRows extends BaseTestFormulaEvaluatorFixture {

    protected BaseTestFormulaEvaluatorHiddenRows(ITestDataProvider testDataProvider) {
        super(testDataProvider);
    }

    /**
     * Called after rows were hidden or shown. Visibility is not a cell change, so there is no
     * notification for it and the cached results have to be discarded.
     */
    protected void afterVisibilityChange() {
        fe.clearAllCachedResultValues();
    }

    /** puts a formula into a (possibly new) cell of the Data sheet and tells the evaluator */
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

    protected void hideRow(Sheet sheet, int rowIndex, boolean hidden) {
        sheet.getRow(rowIndex).setZeroHeight(hidden);
    }

    @Test
    void subtotalAboveHundredSkipsHiddenRowsOnlyAfterCacheIsCleared() {
        formula("E6", "SUBTOTAL(109,A2:A4)");
        formula("E7", "SUBTOTAL(9,A2:A4)");
        assertEquals(60, num("E6"), DELTA);
        assertEquals(60, num("E7"), DELTA);

        // hide the Gadget row
        hideRow(data, 2, true);
        // nothing in the workbook's cells changed, so the cached results are still served
        assertEquals(60, num("E6"), DELTA);

        afterVisibilityChange();
        assertEquals(40, num("E6"), DELTA);
        // codes 1-11 count hidden rows
        assertEquals(60, num("E7"), DELTA);
        // and so does everything else
        assertEquals(60, num("A6"), DELTA);
        assertEquals(21, num("C6"), DELTA);
        assertEquals(170, num("D6"), DELTA);
        assertEquals(2, num("C7"), DELTA);

        hideRow(data, 2, false);
        afterVisibilityChange();
        assertEquals(60, num("E6"), DELTA);
    }

    @Test
    void everyAggregateCodeAboveHundredSkipsHiddenRows() {
        formula("E2", "SUBTOTAL(101,A2:A4)"); // AVERAGE
        formula("E3", "SUBTOTAL(102,A2:A4)"); // COUNT
        formula("E4", "SUBTOTAL(103,B2:B4)"); // COUNTA
        formula("E6", "SUBTOTAL(104,A2:A4)"); // MAX
        formula("E7", "SUBTOTAL(105,A2:A4)"); // MIN
        formula("E8", "SUBTOTAL(106,C2:C4)"); // PRODUCT
        formula("E9", "SUBTOTAL(109,A2:A4)"); // SUM
        assertEquals(20, num("E2"), DELTA);
        assertEquals(3, num("E3"), DELTA);
        assertEquals(3, num("E4"), DELTA);
        assertEquals(30, num("E6"), DELTA);
        assertEquals(10, num("E7"), DELTA);
        assertEquals(315, num("E8"), DELTA);
        assertEquals(60, num("E9"), DELTA);

        // hide the Widget row (10, "Widget", 5)
        hideRow(data, 1, true);
        afterVisibilityChange();
        assertEquals(25, num("E2"), DELTA);
        assertEquals(2, num("E3"), DELTA);
        assertEquals(2, num("E4"), DELTA);
        assertEquals(30, num("E6"), DELTA);
        assertEquals(20, num("E7"), DELTA);
        assertEquals(63, num("E8"), DELTA);
        assertEquals(50, num("E9"), DELTA);

        // hide the Gizmo row too (30, "Gizmo", 9)
        hideRow(data, 3, true);
        afterVisibilityChange();
        assertEquals(20, num("E2"), DELTA);
        assertEquals(1, num("E3"), DELTA);
        assertEquals(1, num("E4"), DELTA);
        assertEquals(20, num("E6"), DELTA);
        assertEquals(20, num("E7"), DELTA);
        assertEquals(7, num("E8"), DELTA);
        assertEquals(20, num("E9"), DELTA);
    }

    @Test
    void subtotalOverSingleCellReferencesSkipsHiddenRows() {
        // single-cell arguments take a different path from areas inside SUBTOTAL
        formula("E6", "SUBTOTAL(109,A2,A3,A4)");
        formula("E7", "SUBTOTAL(9,A2,A3,A4)");
        formula("E8", "SUBTOTAL(103,B2,B3,B4)");
        assertEquals(60, num("E6"), DELTA);

        hideRow(data, 1, true);
        afterVisibilityChange();
        assertEquals(50, num("E6"), DELTA);
        assertEquals(60, num("E7"), DELTA);
        assertEquals(2, num("E8"), DELTA);
    }

    @Test
    void subtotalSkipsHiddenRowsOnOtherSheets() {
        formula("E6", "SUBTOTAL(109,Prices!B2:B4)");
        formula("E7", "SUBTOTAL(103,Prices!A2:A4)");
        assertEquals(7.5, num("E6"), DELTA);
        assertEquals(3, num("E7"), DELTA);

        hideRow(prices, 2, true);
        afterVisibilityChange();
        assertEquals(5, num("E6"), DELTA);
        assertEquals(2, num("E7"), DELTA);
        // hidden rows on the Data sheet do not affect a range on the Prices sheet
        hideRow(data, 2, true);
        afterVisibilityChange();
        assertEquals(5, num("E6"), DELTA);
        // and the lookups still see the hidden Prices row: VLOOKUP does not care about visibility
        assertEquals(2.5, num("D3"), DELTA);
    }

    @Test
    void hiddenRowResultsFlowThroughDependentFormulas() {
        formula("E6", "SUBTOTAL(109,A2:A4)");
        formula("E7", "E6*2");
        formula("E8", "IF(E7>100,\"big\",\"small\")");
        assertEquals(120, num("E7"), DELTA);
        assertEquals("big", str("E8"));

        hideRow(data, 3, true);
        afterVisibilityChange();
        assertEquals(30, num("E6"), DELTA);
        assertEquals(60, num("E7"), DELTA);
        assertEquals("small", str("E8"));

        // cell notifications keep working alongside: a visible input changes
        cell("A2").setCellValue(60);
        fe.notifyUpdateCell(cell("A2"));
        assertEquals(80, num("E6"), DELTA);
        assertEquals("big", str("E8"));
        // a change in the hidden row is tracked too, it just does not count
        cell("A4").setCellValue(1000);
        fe.notifyUpdateCell(cell("A4"));
        assertEquals(80, num("E6"), DELTA);
        assertEquals(1080, num("A6"), DELTA);
    }

    @Test
    void nestedSubtotalsAreIgnoredWhetherHiddenOrNot() {
        formula("E2", "SUBTOTAL(109,A2:A2)");
        formula("E3", "SUBTOTAL(109,A3:A3)");
        formula("E4", "A4");
        // a SUBTOTAL over a range containing other SUBTOTALs ignores them, so only E4 counts
        formula("E6", "SUBTOTAL(109,E2:E4)");
        formula("E7", "SUBTOTAL(9,E2:E4)");
        assertEquals(30, num("E6"), DELTA);
        assertEquals(30, num("E7"), DELTA);

        hideRow(data, 3, true);
        afterVisibilityChange();
        assertEquals(0, num("E6"), DELTA);
        assertEquals(30, num("E7"), DELTA);
    }

    @Test
    void collapsedOutlineGroupHidesItsRows() {
        formula("E6", "SUBTOTAL(109,A2:A4)");
        formula("E7", "SUBTOTAL(9,A2:A4)");
        assertEquals(60, num("E6"), DELTA);

        // collapsing a group hides its rows, which is what SUBTOTAL 1xx looks at
        data.groupRow(1, 2);
        data.setRowGroupCollapsed(1, true);
        assertTrue(data.getRow(1).getZeroHeight());
        assertTrue(data.getRow(2).getZeroHeight());
        afterVisibilityChange();
        assertEquals(30, num("E6"), DELTA);
        assertEquals(60, num("E7"), DELTA);
    }

    @Test
    void hiddenColumnsAndMissingRowsAreNotHiddenRows() {
        // SUBTOTAL only ever skips hidden rows, never hidden columns
        formula("E6", "SUBTOTAL(109,A2:A4)");
        data.setColumnHidden(0, true);
        afterVisibilityChange();
        assertEquals(60, num("E6"), DELTA);

        // a row that does not exist (the spare row 5) is blank, not hidden
        formula("E7", "SUBTOTAL(109,A2:A5)");
        formula("E8", "SUBTOTAL(102,A2:A5)");
        assertEquals(60, num("E7"), DELTA);
        assertEquals(3, num("E8"), DELTA);
    }
}
