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

import org.apache.poi.ss.ITestDataProvider;
import org.junit.jupiter.api.Test;

/**
 * Tests that the {@link FormulaEvaluator} result cache is invalidated correctly when the
 * workbook is modified and the evaluator is told about it via
 * {@link FormulaEvaluator#notifyUpdateCell(Cell)}, {@link FormulaEvaluator#notifySetFormula(Cell)}
 * and {@link FormulaEvaluator#notifyDeleteCell(Cell)}.
 *
 * @see BaseTestFormulaEvaluatorFixture for the workbook the tests run against
 */
public abstract class BaseTestFormulaEvaluatorCacheNotification extends BaseTestFormulaEvaluatorFixture {

    protected BaseTestFormulaEvaluatorCacheNotification(ITestDataProvider testDataProvider) {
        super(testDataProvider);
    }

    @Test
    void updatePlainNumberPropagatesThroughWholeChain() {
        cell("A2").setCellValue(40);

        // the cache still serves the previous result until the evaluator is told about the change
        assertEquals(60, num("A6"), DELTA);

        fe.notifyUpdateCell(cell("A2"));

        assertEquals(90, num("A6"), DELTA);
        assertEquals(180, num("A7"), DELTA);
        assertEquals(201, num("B7"), DELTA);
        assertEquals(67, num("A8"), DELTA);
        assertEquals("big", str("B8"));
        assertEquals("Widget-big", str("C8"));
        assertEquals(247, num("A9"), DELTA);
        // range-based formulas over A2:A4
        assertEquals(215, num("D6"), DELTA);
        assertEquals(3, num("C7"), DELTA);
        // via the defined name
        assertEquals(93, num("B9"), DELTA);
        // not downstream of A2
        assertEquals(21, num("C6"), DELTA);
        assertEquals(1.5, num("D2"), DELTA);
    }

    @Test
    void updateLookupKeyReroutesLookupAndStringFormula() {
        cell("B2").setCellValue("Gizmo");
        fe.notifyUpdateCell(cell("B2"));

        assertEquals(3.5, num("D2"), DELTA);
        assertEquals(190, num("D6"), DELTA);
        assertEquals("Gizmo-big", str("C8"));
        // numeric chain untouched
        assertEquals(167, num("A9"), DELTA);
    }

    @Test
    void updateValueOnOtherSheetPropagatesAcrossSheets() {
        cell("Prices!B2").setCellValue(2);
        fe.notifyUpdateCell(cell("Prices!B2"));

        assertEquals(2, num("D2"), DELTA);
        assertEquals(175, num("D6"), DELTA);
        // other lookups and the rest of the chain are untouched
        assertEquals(2.5, num("D3"), DELTA);
        assertEquals(167, num("A9"), DELTA);
    }

    @Test
    void lookupMissBecomesErrorThenRecovers() {
        cell("B2").setCellValue("Sprocket");
        fe.notifyUpdateCell(cell("B2"));

        assertEquals(FormulaError.NA, err("D2"));
        assertEquals(FormulaError.NA, err("D6"));
        assertEquals("Sprocket-big", str("C8"));

        // add the missing key to the lookup table
        cell("Prices!A2").setCellValue("Sprocket");
        fe.notifyUpdateCell(cell("Prices!A2"));

        assertEquals(1.5, num("D2"), DELTA);
        assertEquals(170, num("D6"), DELTA);
    }

    @Test
    void updateHeaderCellDoesNotDisturbAnything() {
        cell("A1").setCellValue("Quantity");
        fe.notifyUpdateCell(cell("A1"));
        cell("Prices!A1").setCellValue("Product");
        fe.notifyUpdateCell(cell("Prices!A1"));
        assertInitialValues();
    }

    @Test
    void changeFormulaInMiddleOfChain() {
        Cell a7 = cell("A7");
        a7.setCellFormula("A6*3");
        fe.notifySetFormula(a7);

        // upstream of the change is unaffected
        assertEquals(60, num("A6"), DELTA);
        assertEquals(62, num("B9"), DELTA);
        // the changed cell and everything downstream is recalculated
        assertEquals(180, num("A7"), DELTA);
        assertEquals(201, num("B7"), DELTA);
        assertEquals(67, num("A8"), DELTA);
        assertEquals(247, num("A9"), DELTA);
    }

    @Test
    void changeFormulaFlipsConditionalBranch() {
        Cell a8 = cell("A8");
        a8.setCellFormula("B7/10");
        fe.notifySetFormula(a8);

        assertEquals(14.1, num("A8"), DELTA);
        assertEquals("small", str("B8"));
        assertEquals("Widget-small", str("C8"));
        assertEquals(134.1, num("A9"), DELTA);
    }

    @Test
    void changeFormulaToReferenceDifferentCells() {
        // B9 no longer uses the name or C7, so changes to them must stop affecting it
        Cell b9 = cell("B9");
        b9.setCellFormula("D6*2");
        fe.notifySetFormula(b9);
        assertEquals(340, num("B9"), DELTA);

        cell("A2").setCellValue(16); // A6 66, C7 3, D6 179
        fe.notifyUpdateCell(cell("A2"));
        assertEquals(66, num("A6"), DELTA);
        assertEquals(3, num("C7"), DELTA);
        assertEquals(358, num("B9"), DELTA);
    }

    @Test
    void replaceFormulaWithPlainValue() {
        Cell a6 = cell("A6");
        // HSSF keeps a formula cell as a formula when setCellValue is called (it only sets the cached
        // result), so drop the formula explicitly to turn it into a plain value in both formats
        a6.removeFormula();
        a6.setCellValue(150);
        fe.notifyUpdateCell(a6);

        assertEquals(CellType.NUMERIC, a6.getCellType());
        assertEquals(300, num("A7"), DELTA);
        assertEquals(321, num("B7"), DELTA);
        assertEquals(107, num("A8"), DELTA);
        assertEquals("big", str("B8"));
        assertEquals(407, num("A9"), DELTA);
        // the name now resolves to a plain cell
        assertEquals(152, num("B9"), DELTA);

        // A6 is plain now, so its former inputs no longer affect the chain through it
        cell("A2").setCellValue(1000);
        fe.notifyUpdateCell(cell("A2"));
        assertEquals(300, num("A7"), DELTA);
        // but formulas that reference A2 directly still see the change
        assertEquals(1000 * 1.5 + 50 + 105, num("D6"), DELTA);
        assertEquals(3, num("C7"), DELTA);
        assertEquals(153, num("B9"), DELTA);
    }

    @Test
    void replacePlainValueWithFormula() {
        Cell c2 = cell("C2");
        c2.setCellFormula("A2/5");
        fe.notifySetFormula(c2);

        assertEquals(2, num("C2"), DELTA);
        assertEquals(18, num("C6"), DELTA);
        assertEquals(138, num("B7"), DELTA);
        assertEquals(46, num("A8"), DELTA);
        assertEquals(166, num("A9"), DELTA);

        // the new formula cell is now itself tracked as an input: changing A2 must flow through it
        cell("A2").setCellValue(50);
        fe.notifyUpdateCell(cell("A2"));
        assertEquals(10, num("C2"), DELTA);
        assertEquals(26, num("C6"), DELTA);
        assertEquals(100, num("A6"), DELTA);
        assertEquals(226, num("B7"), DELTA);
    }

    @Test
    void deleteCellUsedByRangeFormula() {
        Cell c3 = cell("C3");
        fe.notifyDeleteCell(c3);
        data.getRow(2).removeCell(c3);

        assertEquals(14, num("C6"), DELTA);
        assertEquals(134, num("B7"), DELTA);
        assertEquals(134.0 / 3, num("A8"), DELTA);
        assertEquals("big", str("B8"));
        assertEquals(134.0 / 3 + 120, num("A9"), DELTA);
    }

    @Test
    void deleteLookupKeyOnOtherSheet() {
        Cell gadget = cell("Prices!A3");
        fe.notifyDeleteCell(gadget);
        prices.getRow(2).removeCell(gadget);

        assertEquals(FormulaError.NA, err("D3"));
        assertEquals(FormulaError.NA, err("D6"));
        assertEquals(1.5, num("D2"), DELTA);
        assertEquals(3.5, num("D4"), DELTA);
    }

    @Test
    void blankCellReferencedByFormulaBecomesValue() {
        // F2 references the spare row 5, which has no cells yet
        Cell f2 = data.getRow(1).createCell(5);
        f2.setCellFormula("A2+A5");
        fe.notifySetFormula(f2);
        assertEquals(10, num("F2"), DELTA);

        Cell a5 = data.createRow(4).createCell(0);
        a5.setCellValue(5);
        fe.notifyUpdateCell(a5);
        assertEquals(15, num("F2"), DELTA);

        // and back to blank again
        fe.notifyDeleteCell(a5);
        data.getRow(4).removeCell(a5);
        assertEquals(10, num("F2"), DELTA);
    }

    @Test
    void severalChangesBeforeReevaluation() {
        cell("A2").setCellValue(1);
        fe.notifyUpdateCell(cell("A2"));
        cell("A3").setCellValue(2);
        fe.notifyUpdateCell(cell("A3"));
        cell("A4").setCellValue(3);
        fe.notifyUpdateCell(cell("A4"));
        cell("A7").setCellFormula("A6*10");
        fe.notifySetFormula(cell("A7"));
        cell("Prices!B4").setCellValue(10);
        fe.notifyUpdateCell(cell("Prices!B4"));

        assertEquals(6, num("A6"), DELTA);
        assertEquals(60, num("A7"), DELTA);
        assertEquals(81, num("B7"), DELTA);
        assertEquals(0, num("C7"), DELTA);
        assertEquals(27, num("A8"), DELTA);
        assertEquals("small", str("B8"));
        assertEquals("Widget-small", str("C8"));
        assertEquals(87, num("A9"), DELTA);
        assertEquals(6, num("B9"), DELTA);
        assertEquals(10, num("D4"), DELTA);
        assertEquals(1.5 + 5 + 30, num("D6"), DELTA);
    }

    @Test
    void notifyWithUnchangedValueKeepsDependentsValid() {
        // setting the same value again must not break anything, and the dependents must still be right
        cell("A2").setCellValue(10);
        fe.notifyUpdateCell(cell("A2"));
        cell("B2").setCellValue("Widget");
        fe.notifyUpdateCell(cell("B2"));
        assertInitialValues();
    }

    @Test
    void clearAllCachedResultValuesIsAnAlternativeToNotify() {
        cell("A2").setCellValue(40);
        cell("A7").setCellFormula("A6*3");
        cell("B2").setCellValue("Gizmo");
        cell("Prices!B4").setCellValue(4);

        fe.clearAllCachedResultValues();

        assertEquals(90, num("A6"), DELTA);
        assertEquals(270, num("A7"), DELTA);
        assertEquals(291, num("B7"), DELTA);
        assertEquals(97, num("A8"), DELTA);
        assertEquals("Gizmo-big", str("C8"));
        assertEquals(367, num("A9"), DELTA);
        assertEquals(93, num("B9"), DELTA);
        assertEquals(4, num("D2"), DELTA);
        assertEquals(40 * 4 + 50 + 30 * 4, num("D6"), DELTA);
    }

    @Test
    void evaluateAllAfterNotifyWritesUpdatedValuesIntoCells() {
        cell("A2").setCellValue(40);
        fe.notifyUpdateCell(cell("A2"));

        fe.evaluateAll();

        assertEquals(90, cell("A6").getNumericCellValue(), DELTA);
        assertEquals(180, cell("A7").getNumericCellValue(), DELTA);
        assertEquals(201, cell("B7").getNumericCellValue(), DELTA);
        assertEquals(3, cell("C7").getNumericCellValue(), DELTA);
        assertEquals(67, cell("A8").getNumericCellValue(), DELTA);
        assertEquals("big", cell("B8").getStringCellValue());
        assertEquals(247, cell("A9").getNumericCellValue(), DELTA);
        assertEquals(93, cell("B9").getNumericCellValue(), DELTA);
        assertEquals(215, cell("D6").getNumericCellValue(), DELTA);
    }
}
