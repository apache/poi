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

package org.apache.poi.ss.formula;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.NameXPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.Test;

/**
 * {@code SUBTOTAL} has to know, for every formula cell in its range, whether that formula is
 * itself a {@code SUBTOTAL} (nested subtotals are skipped). That used to fetch and scan the
 * tokens of every such cell on every evaluation; the answer is now kept with the cell's cache
 * entry until the cell is notified as changed.
 */
class TestSubTotalCellCheckCached {

    /** counts how often the tokens of each cell are asked for */
    private static final class CountingWorkbook implements EvaluationWorkbook {
        private final EvaluationWorkbook _wb;
        final Map<String, Integer> tokenFetches = new TreeMap<>();

        CountingWorkbook(EvaluationWorkbook wb) {
            _wb = wb;
        }

        @Override
        public Ptg[] getFormulaTokens(EvaluationCell cell) {
            String ref = new CellReference(cell.getRowIndex(), cell.getColumnIndex()).formatAsString();
            tokenFetches.merge(ref, 1, Integer::sum);
            return _wb.getFormulaTokens(cell);
        }

        @Override public String getSheetName(int sheetIndex) { return _wb.getSheetName(sheetIndex); }
        @Override public int getSheetIndex(EvaluationSheet sheet) { return _wb.getSheetIndex(sheet); }
        @Override public int getSheetIndex(String sheetName) { return _wb.getSheetIndex(sheetName); }
        @Override public EvaluationSheet getSheet(int sheetIndex) { return _wb.getSheet(sheetIndex); }
        @Override public ExternalSheet getExternalSheet(int externSheetIndex) { return _wb.getExternalSheet(externSheetIndex); }
        @Override public ExternalSheet getExternalSheet(String firstSheetName, String lastSheetName, int externalWorkbookNumber) {
            return _wb.getExternalSheet(firstSheetName, lastSheetName, externalWorkbookNumber);
        }
        @Override public int convertFromExternSheetIndex(int externSheetIndex) { return _wb.convertFromExternSheetIndex(externSheetIndex); }
        @Override public ExternalName getExternalName(int externSheetIndex, int externNameIndex) { return _wb.getExternalName(externSheetIndex, externNameIndex); }
        @Override public ExternalName getExternalName(String nameName, String sheetName, int externalWorkbookNumber) {
            return _wb.getExternalName(nameName, sheetName, externalWorkbookNumber);
        }
        @Override public EvaluationName getName(NamePtg namePtg) { return _wb.getName(namePtg); }
        @Override public EvaluationName getName(String name, int sheetIndex) { return _wb.getName(name, sheetIndex); }
        @Override public String resolveNameXText(NameXPtg ptg) { return _wb.resolveNameXText(ptg); }
        @Override public UDFFinder getUDFFinder() { return _wb.getUDFFinder(); }
        @Override public SpreadsheetVersion getSpreadsheetVersion() { return _wb.getSpreadsheetVersion(); }
        @Override public void clearAllCachedResultValues() { _wb.clearAllCachedResultValues(); }
    }

    private static double num(WorkbookEvaluator we, EvaluationWorkbook wb, Cell cell) {
        EvaluationCell ec = wb.getSheet(0).getCell(cell.getRowIndex(), cell.getColumnIndex());
        ValueEval ve = we.evaluate(ec);
        return ((NumberEval) ve).getNumberValue();
    }

    @Test
    void subtotalAsksAboutEachFormulaCellInItsRangeOnce() {
        HSSFWorkbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        // A1:A4 plain values, A5 a plain formula, A6 a nested SUBTOTAL, A7 the SUBTOTAL under test
        for (int r = 0; r < 4; r++) {
            sheet.createRow(r).createCell(0).setCellValue(r + 1);
        }
        Cell a5 = sheet.createRow(4).createCell(0);
        a5.setCellFormula("A1*10");
        Cell a6 = sheet.createRow(5).createCell(0);
        a6.setCellFormula("SUBTOTAL(9,A1:A2)");
        Cell a7 = sheet.createRow(6).createCell(0);
        a7.setCellFormula("SUBTOTAL(9,A1:A6)");
        Row r8 = sheet.createRow(7);
        Cell a8 = r8.createCell(0);
        a8.setCellFormula("A7+1");

        CountingWorkbook cw = new CountingWorkbook(HSSFEvaluationWorkbook.create(wb));
        WorkbookEvaluator we = new WorkbookEvaluator(cw, null, null);

        // 1+2+3+4 + 10 (A5), the nested subtotal A6 is skipped
        assertEquals(20, num(we, cw, a7), 0);
        // each formula cell in the range had its tokens fetched twice: once to evaluate it, once
        // for the nested-SUBTOTAL check
        assertThat(cw.tokenFetches, hasEntry("A5", 2));
        assertThat(cw.tokenFetches, hasEntry("A6", 2));
        assertThat(cw.tokenFetches, hasEntry("A7", 1));
        cw.tokenFetches.clear();

        // A7 is re-evaluated after it is notified; the cells in its range keep their answer
        we.notifyUpdateCell(cw.getSheet(0).getCell(6, 0));
        assertEquals(20, num(we, cw, a7), 0);
        assertThat(cw.tokenFetches, hasEntry("A7", 1));
        assertThat(cw.tokenFetches, not(hasKey("A5")));
        assertThat(cw.tokenFetches, not(hasKey("A6")));
        cw.tokenFetches.clear();

        // a served result asks nothing at all
        assertEquals(21, num(we, cw, a8), 0);
        assertEquals(20, num(we, cw, a7), 0);
        assertThat(cw.tokenFetches, hasEntry("A8", 1));
        assertThat(cw.tokenFetches, not(hasKey("A7")));
        cw.tokenFetches.clear();

        // once a cell in the range is notified, its formula is looked at again: A6 becomes a plain
        // formula and counts, A5 becomes a SUBTOTAL and is skipped
        a6.setCellFormula("A1*100");
        we.notifyUpdateCell(cw.getSheet(0).getCell(5, 0));
        a5.setCellFormula("SUBTOTAL(9,A1:A2)");
        we.notifyUpdateCell(cw.getSheet(0).getCell(4, 0));
        assertEquals(1 + 2 + 3 + 4 + 100, num(we, cw, a7), 0);
        assertThat(cw.tokenFetches, hasEntry("A5", 2));
        assertThat(cw.tokenFetches, hasEntry("A6", 2));
        cw.tokenFetches.clear();

        // and clearing the whole cache starts over
        we.clearAllCachedResultValues();
        assertEquals(110, num(we, cw, a7), 0);
        assertThat(cw.tokenFetches, hasEntry("A5", 2));
        assertThat(cw.tokenFetches, hasEntry("A6", 2));
        assertThat(cw.tokenFetches, hasEntry("A7", 1));

        cw.tokenFetches.clear();
        assertEquals(110, num(we, cw, a7), 0);
        assertThat(cw.tokenFetches, anEmptyMap());
    }
}
