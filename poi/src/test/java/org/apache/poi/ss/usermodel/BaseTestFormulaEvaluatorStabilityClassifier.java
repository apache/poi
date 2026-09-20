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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.formula.IStabilityClassifier;
import org.junit.jupiter.api.Test;

/**
 * Tests the effect of an {@link IStabilityClassifier} on the {@link FormulaEvaluator}.
 * <p>
 * The evaluator records, for every formula it evaluates, which cells the formula read, so that
 * {@link FormulaEvaluator#notifyUpdateCell(Cell)} on one of those cells can invalidate the cached
 * result. A classifier lets the caller declare cells "final" — promised not to change — and the
 * evaluator then skips recording them, which saves time and memory. The tests check that this
 * changes nothing about the results, and exactly what it changes about invalidation.
 *
 * @see BaseTestFormulaEvaluatorFixture for the workbook the tests run against
 */
public abstract class BaseTestFormulaEvaluatorStabilityClassifier extends BaseTestFormulaEvaluatorFixture {

    protected BaseTestFormulaEvaluatorStabilityClassifier(ITestDataProvider testDataProvider) {
        super(testDataProvider);
    }

    /** @return an evaluator for {@link #wb} that uses the given classifier ({@code null} for none) */
    protected abstract FormulaEvaluator createEvaluator(IStabilityClassifier classifier);

    /** replaces the fixture's evaluator with one using the given classifier and evaluates everything */
    private void useClassifier(IStabilityClassifier classifier) {
        fe = createEvaluator(classifier);
        fe.evaluateAll();
    }

    private int dataIndex() {
        return wb.getSheetIndex(data);
    }

    private int pricesIndex() {
        return wb.getSheetIndex(prices);
    }

    @Test
    void totallyImmutableGivesTheSameResults() {
        useClassifier(IStabilityClassifier.TOTALLY_IMMUTABLE);
        assertInitialValues();

        // and discarding the cache still works as the way to pick up changes
        cell("A2").setCellValue(40);
        cell("B2").setCellValue("Gizmo");
        fe.clearAllCachedResultValues();
        assertEquals(90, num("A6"), DELTA);
        assertEquals(3.5, num("D2"), DELTA);
        assertEquals(40 * 3.5 + 20 * 2.5 + 30 * 3.5, num("D6"), DELTA);
        assertEquals(93, num("B9"), DELTA);
    }

    @Test
    void totallyImmutableDoesNotTrackInputs() {
        useClassifier(IStabilityClassifier.TOTALLY_IMMUTABLE);

        // the caller promised that cells do not change, so the evaluator recorded no dependencies
        // and a notification about an input cannot reach the formulas that read it
        cell("A2").setCellValue(40);
        fe.notifyUpdateCell(cell("A2"));
        assertEquals(60, num("A6"), DELTA);
        assertEquals(167, num("A9"), DELTA);

        cell("Prices!B2").setCellValue(2);
        fe.notifyUpdateCell(cell("Prices!B2"));
        assertEquals(1.5, num("D2"), DELTA);

        // a notification about a formula cell still discards that cell's own cached result ...
        cell("A7").setCellFormula("A6*3");
        fe.notifySetFormula(cell("A7"));
        assertEquals(180, num("A7"), DELTA);
        // ... but nothing recorded that B7 read A7, so B7 keeps its old result
        assertEquals(141, num("B7"), DELTA);
    }

    @Test
    void selectiveClassifierTracksOnlyNonFinalCells() {
        // the lookup table is declared final, the data sheet is not
        useClassifier((sheetIndex, rowIndex, columnIndex) -> sheetIndex == pricesIndex());
        assertInitialValues();

        // changes on the data sheet are tracked and propagate through the whole chain
        cell("A2").setCellValue(40);
        fe.notifyUpdateCell(cell("A2"));
        assertEquals(90, num("A6"), DELTA);
        assertEquals(215, num("D6"), DELTA);
        assertEquals(247, num("A9"), DELTA);
        assertEquals(93, num("B9"), DELTA);

        cell("B2").setCellValue("Gizmo");
        fe.notifyUpdateCell(cell("B2"));
        assertEquals(3.5, num("D2"), DELTA);
        assertEquals("Gizmo-big", str("C8"));

        // changes on the final sheet are not
        cell("Prices!B4").setCellValue(10);
        fe.notifyUpdateCell(cell("Prices!B4"));
        assertEquals(3.5, num("D2"), DELTA);
        assertEquals(3.5, num("D4"), DELTA);
    }

    @Test
    void finalFormulaCellWithNonFinalInputsIsStillTracked() {
        // the formula block is declared final, the data rows are not: a final formula cell whose
        // inputs are tracked must itself stay tracked, otherwise the chain would break behind it
        int firstFormulaRow = 5;
        useClassifier((sheetIndex, rowIndex, columnIndex) ->
                sheetIndex != dataIndex() || rowIndex >= firstFormulaRow);
        assertInitialValues();

        cell("A2").setCellValue(40);
        fe.notifyUpdateCell(cell("A2"));
        assertEquals(90, num("A6"), DELTA);
        assertEquals(180, num("A7"), DELTA);
        assertEquals(201, num("B7"), DELTA);
        assertEquals(67, num("A8"), DELTA);
        assertEquals(247, num("A9"), DELTA);
        assertEquals(93, num("B9"), DELTA);
        assertEquals(215, num("D6"), DELTA);

        // whereas a formula cell whose inputs are all final has nothing recorded for it: with
        // column B declared final as well, D3 reads only final cells (B3 and the Prices sheet),
        // so a notification about B3 cannot reach it
        fe = createEvaluator((sheetIndex, rowIndex, columnIndex) ->
                sheetIndex != dataIndex() || columnIndex == 1 || rowIndex >= firstFormulaRow);
        fe.evaluateAll();
        cell("B3").setCellValue("Widget");
        fe.notifyUpdateCell(cell("B3"));
        assertEquals(2.5, num("D3"), DELTA);
    }

    @Test
    void classifierIsAskedAboutEveryCellRead() {
        Set<String> asked = new HashSet<>();
        useClassifier((sheetIndex, rowIndex, columnIndex) -> {
            asked.add(sheetIndex + ":" + rowIndex + ":" + columnIndex);
            return false;
        });
        assertInitialValues();

        int d = dataIndex();
        int p = pricesIndex();
        assertThat(asked, hasItems(
                // plain inputs on the data sheet
                d + ":1:0", // A2
                d + ":3:2", // C4
                // the lookup table, read through a cross-sheet area reference
                p + ":1:0", // Prices!A2
                p + ":3:1", // Prices!B4
                // formula cells are classified too, both when evaluated directly and via the defined name
                d + ":5:0", // A6
                d + ":7:1"  // B8
        ));
        // cells nothing refers to are never asked about
        assertThat("Data header", asked, not(hasItem(d + ":0:0")));
        assertThat("Prices header", asked, not(hasItem(p + ":0:0")));
        assertThat("spare row", asked, not(hasItem(d + ":4:0")));
        Set<String> sheets = new HashSet<>();
        for (String key : asked) {
            sheets.add(key.substring(0, key.indexOf(':')));
        }
        assertThat(sheets, everyItem(is(in(Arrays.asList(String.valueOf(d), String.valueOf(p))))));
    }
}
