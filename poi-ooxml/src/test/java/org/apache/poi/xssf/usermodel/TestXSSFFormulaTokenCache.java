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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationSheet;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.BaseTestFormulaEvaluatorFixture;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.junit.jupiter.api.Test;

/**
 * {@link XSSFEvaluationSheet} keeps the parsed tokens of every formula it has evaluated, so an
 * evaluator parses a formula once and not on every re-evaluation, and derives the cells of a
 * shared formula group from one parse of the master. Runs against the fixture workbook with
 * {@code D2:D4} and {@code A6:C6} stored as shared formulas.
 */
final class TestXSSFFormulaTokenCache extends BaseTestFormulaEvaluatorFixture {

    TestXSSFFormulaTokenCache() {
        super(XSSFITestDataProvider.instance);
    }

    @Override
    protected Workbook finishWorkbook(Workbook built) {
        return XSSFSharedFormulaFixture.shareFillDownFormulasAndReload(built);
    }

    private XSSFEvaluationWorkbook evaluationWorkbook() {
        return XSSFEvaluationWorkbook.create((XSSFWorkbook) wb);
    }

    private static EvaluationCell evalCell(XSSFEvaluationWorkbook ew, Workbook wb, Cell cell) {
        EvaluationSheet sheet = ew.getSheet(wb.getSheetIndex(cell.getSheet()));
        return sheet.getCell(cell.getRowIndex(), cell.getColumnIndex());
    }

    @Test
    void tokensAreParsedOnceAndKeptUntilTheCellIsNotified() {
        XSSFEvaluationWorkbook ew = evaluationWorkbook();
        Cell a7 = cell("A7");
        EvaluationCell ec = evalCell(ew, wb, a7);
        Ptg[] first = ew.getFormulaTokens(ec);
        assertSame(first, ew.getFormulaTokens(ec), "the same parse is handed out again");
        assertEquals("A6*2", FormulaRenderer.toFormulaString(ew, first));

        // a change the evaluator is not told about is not seen ...
        a7.setCellFormula("A6*3");
        assertSame(first, ew.getFormulaTokens(ec));

        // ... a notified one is
        ec.getSheet().notifyUpdateCell(a7.getRowIndex(), a7.getColumnIndex());
        Ptg[] second = ew.getFormulaTokens(ec);
        assertNotSame(first, second);
        assertEquals("A6*3", FormulaRenderer.toFormulaString(ew, second));
        assertSame(second, ew.getFormulaTokens(ec));

        // and everything goes with the cache
        ew.clearAllCachedResultValues();
        assertNotSame(second, ew.getFormulaTokens(evalCell(ew, wb, a7)));
    }

    @Test
    void notifiedThroughTheEvaluatorTheNewFormulaIsUsed() {
        // the whole chain: FormulaEvaluator -> WorkbookEvaluator -> EvaluationSheet.notifyUpdateCell
        assertEquals(120, num("A7"), DELTA);
        cell("A7").setCellFormula("A6*3");
        fe.notifySetFormula(cell("A7"));
        assertEquals(180, num("A7"), DELTA);
        assertEquals(201, num("B7"), DELTA);

        // an input change re-evaluates the formula from its cached tokens
        cell("A2").setCellValue(40);
        fe.notifyUpdateCell(cell("A2"));
        assertEquals(270, num("A7"), DELTA);
    }

    @Test
    void sharedFormulaCellsGetTheMasterTokensShiftedToTheirPosition() {
        XSSFEvaluationWorkbook ew = evaluationWorkbook();
        XSSFSharedFormulaFixture.assertSharedFormulas(wb);
        for (String ref : new String[]{"D2", "D3", "D4", "A6", "C6"}) {
            Cell cell = cell(ref);
            Ptg[] tokens = ew.getFormulaTokens(evalCell(ew, wb, cell));
            // the tokens derived from the master render to the formula the cell reports
            assertEquals(cell.getCellFormula(), FormulaRenderer.toFormulaString(ew, tokens), ref);
        }
        assertEquals("VLOOKUP(B3,Prices!$A$2:$B$4,2,FALSE)", cell("D3").getCellFormula());
        assertEquals("SUM(C2:C4)", cell("C6").getCellFormula());
    }

    @Test
    void aChangedSharedMasterIsParsedAgain() {
        // POI keeps a shared group together when its master cell gets a new formula: the group is
        // re-registered with the new text (a new master object), and the other cells of the
        // group now report the new formula, shifted to their position
        FormulaEvaluator fresh = wb.getCreationHelper().createFormulaEvaluator();
        cell("D2").setCellFormula("A2*100");
        fe.notifySetFormula(cell("D2"));
        assertEquals("A3*100", cell("D3").getCellFormula());

        // the notified cell is re-parsed
        assertEquals(1000, num("D2"), DELTA);
        // cells of the group that were not notified keep their cached results, as any formula
        // cell does
        assertEquals(2.5, num("D3"), DELTA);
        assertEquals(3.5, num("D4"), DELTA);

        // an evaluator that parses the group afresh takes the tokens from the new master
        assertEquals(2000, fresh.evaluate(cell("D3")).getNumberValue(), DELTA);
        assertEquals(3000, fresh.evaluate(cell("D4")).getNumberValue(), DELTA);
        XSSFEvaluationWorkbook ew = evaluationWorkbook();
        assertEquals("A4*100", FormulaRenderer.toFormulaString(ew, ew.getFormulaTokens(evalCell(ew, wb, cell("D4")))));
    }
}
