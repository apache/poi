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


package org.apache.poi.ss.tests.formula.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.SheetUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test the COUNTIFS() function
 */
class TestCountifs {

    private Workbook workbook;

    /**
     * initialize a workbook
     */
    @BeforeEach
    void before() {
        // not sure why we allow this, COUNTIFS() is only available
        // in OOXML, it was introduced with Office 2007
        workbook = new HSSFWorkbook();
    }

    /**
     * Close the workbook if needed
     */
    @AfterEach
    void after() {
        IOUtils.closeQuietly(workbook);
    }

    /**
     * Basic call
     */
    @Test
    void testCallFunction() {
        Sheet sheet = workbook.createSheet("test");
        Row row1 = sheet.createRow(0);
        Cell cellA1 = row1.createCell(0, CellType.FORMULA);
        Cell cellB1 = row1.createCell(1, CellType.NUMERIC);
        Cell cellC1 = row1.createCell(2, CellType.NUMERIC);
        Cell cellD1 = row1.createCell(3, CellType.NUMERIC);
        Cell cellE1 = row1.createCell(4, CellType.NUMERIC);
        cellB1.setCellValue(1);
        cellC1.setCellValue(1);
        cellD1.setCellValue(2);
        cellE1.setCellValue(4);

        cellA1.setCellFormula("COUNTIFS(B1:C1,1, D1:E1,2)");
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        CellValue evaluate = evaluator.evaluate(cellA1);
        assertEquals(1.0d, evaluate.getNumberValue(), 0.000000000000001);
    }

    /**
     * Test argument count check
     */
    @Test
    void testCallFunction_invalidArgs() {
        Sheet sheet = workbook.createSheet("test");
        Row row1 = sheet.createRow(0);
        Cell cellA1 = row1.createCell(0, CellType.FORMULA);
        cellA1.setCellFormula("COUNTIFS()");
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        CellValue evaluate = evaluator.evaluate(cellA1);
        assertEquals(15, evaluate.getErrorValue());
        cellA1.setCellFormula("COUNTIFS(A1:C1)");
        evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        evaluate = evaluator.evaluate(cellA1);
        assertEquals(15, evaluate.getErrorValue());
        cellA1.setCellFormula("COUNTIFS(A1:C1,2,2)");
        evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        evaluate = evaluator.evaluate(cellA1);
        assertEquals(15, evaluate.getErrorValue());
    }

    /**
     * the bug returned the wrong count, this verifies the fix
     */
    @Test
    void testBug56822() {
        workbook = XSSFTestDataSamples.openSampleWorkbook("56822-Countifs.xlsx");
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        Cell cell = SheetUtil.getCell(workbook.getSheetAt(0), 0, 3);
        assertNotNull(cell, "Test workbook missing cell D1");
        CellValue evaluate = evaluator.evaluate(cell);
        assertEquals(2.0d, evaluate.getNumberValue(), 0.00000000000001);
    }

    /**
     * Bug 70005 - SUM(COUNTIFS) with multiple values in criteria gives wrong result,
     * and uses wrong area check causing ERROR when formula cell is not in the data range rows.
     * Expected: A3=3.0, A4=4.0, D3=3.0, D4=4.0 (verified in Excel and LibreOffice Calc).
     */
    @Test
    void testBug70005() {
        workbook = XSSFTestDataSamples.openSampleWorkbook("70005-countifs.xlsx");
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        Sheet sheet = workbook.getSheetAt(0);

        // A3 and A4: formula cell is within the data area rows - wrong numeric result in buggy code
        Cell a3 = SheetUtil.getCell(sheet, 2, 0);
        assertNotNull(a3, "Test workbook missing cell A3");
        CellValue a3Value = evaluator.evaluate(a3);
        assertEquals(CellType.NUMERIC, a3Value.getCellType(), "A3 should be numeric, not an error");
        assertEquals(3.0, a3Value.getNumberValue(), 0.00000000000001, "A3: SUM(COUNTIFS) with multiple criteria should equal 3");

        Cell a4 = SheetUtil.getCell(sheet, 3, 0);
        assertNotNull(a4, "Test workbook missing cell A4");
        CellValue a4Value = evaluator.evaluate(a4);
        assertEquals(CellType.NUMERIC, a4Value.getCellType(), "A4 should be numeric, not an error");
        assertEquals(4.0, a4Value.getNumberValue(), 0.00000000000001, "A4: SUM(COUNTIFS) with multiple criteria should equal 4");

        // D3 and D4: formula cell is outside the data area rows - buggy code returns ERROR here
        Cell d3 = SheetUtil.getCell(sheet, 2, 3);
        assertNotNull(d3, "Test workbook missing cell D3");
        CellValue d3Value = evaluator.evaluate(d3);
        assertEquals(CellType.NUMERIC, d3Value.getCellType(), "D3 should be numeric, not an error");
        assertEquals(3.0, d3Value.getNumberValue(), 0.00000000000001, "D3: SUM(COUNTIFS) with formula cell outside data rows should equal 3");

        Cell d4 = SheetUtil.getCell(sheet, 3, 3);
        assertNotNull(d4, "Test workbook missing cell D4");
        CellValue d4Value = evaluator.evaluate(d4);
        assertEquals(CellType.NUMERIC, d4Value.getCellType(), "D4 should be numeric, not an error");
        assertEquals(4.0, d4Value.getNumberValue(), 0.00000000000001, "D4: SUM(COUNTIFS) with formula cell outside data rows should equal 4");
    }

    /** Minimum valid case: a single criteria range/criteria pair. */
    @Test
    void testSingleCriteriaPair() {
        Sheet sheet = workbook.createSheet("test");
        Row row = sheet.createRow(0);
        Cell formulaCell = row.createCell(0, CellType.FORMULA);
        row.createCell(1).setCellValue(1.0);
        row.createCell(2).setCellValue(2.0);
        row.createCell(3).setCellValue(1.0);

        formulaCell.setCellFormula("COUNTIFS(B1:D1,1)");
        CellValue result = workbook.getCreationHelper().createFormulaEvaluator().evaluate(formulaCell);
        assertEquals(2.0d, result.getNumberValue(), 0.000000000000001);
    }

    /** When no cell matches the criteria, COUNTIFS must return 0. */
    @Test
    void testReturnsZeroWhenNothingMatches() {
        Sheet sheet = workbook.createSheet("test");
        Row row = sheet.createRow(0);
        Cell formulaCell = row.createCell(0, CellType.FORMULA);
        row.createCell(1).setCellValue(5.0);
        row.createCell(2).setCellValue(6.0);

        formulaCell.setCellFormula("COUNTIFS(B1:C1,99)");
        CellValue result = workbook.getCreationHelper().createFormulaEvaluator().evaluate(formulaCell);
        assertEquals(0.0d, result.getNumberValue(), 0.000000000000001);
    }

    /** When every cell in the range matches, COUNTIFS returns the range size. */
    @Test
    void testAllCellsMatchCriteria() {
        Sheet sheet = workbook.createSheet("test");
        Row row = sheet.createRow(0);
        Cell formulaCell = row.createCell(0, CellType.FORMULA);
        row.createCell(1).setCellValue(3.0);
        row.createCell(2).setCellValue(3.0);
        row.createCell(3).setCellValue(3.0);

        formulaCell.setCellFormula("COUNTIFS(B1:D1,3)");
        CellValue result = workbook.getCreationHelper().createFormulaEvaluator().evaluate(formulaCell);
        assertEquals(3.0d, result.getNumberValue(), 0.000000000000001);
    }

    /** String criteria must match cell values exactly (but case-insensitively). */
    @Test
    void testStringCriteriaMatching() {
        Sheet sheet = workbook.createSheet("test");
        Row row = sheet.createRow(0);
        Cell formulaCell = row.createCell(0, CellType.FORMULA);
        row.createCell(1).setCellValue("apple");
        row.createCell(2).setCellValue("banana");
        row.createCell(3).setCellValue("apple");

        formulaCell.setCellFormula("COUNTIFS(B1:D1,\"apple\")");
        CellValue result = workbook.getCreationHelper().createFormulaEvaluator().evaluate(formulaCell);
        assertEquals(2.0d, result.getNumberValue(), 0.000000000000001);
    }

    /** String matching in COUNTIFS is case-insensitive, matching Excel behaviour. */
    @Test
    void testCaseInsensitiveStringCriteria() {
        Sheet sheet = workbook.createSheet("test");
        Row row = sheet.createRow(0);
        Cell formulaCell = row.createCell(0, CellType.FORMULA);
        row.createCell(1).setCellValue("Apple");
        row.createCell(2).setCellValue("APPLE");
        row.createCell(3).setCellValue("banana");

        formulaCell.setCellFormula("COUNTIFS(B1:D1,\"apple\")");
        CellValue result = workbook.getCreationHelper().createFormulaEvaluator().evaluate(formulaCell);
        assertEquals(2.0d, result.getNumberValue(), 0.000000000000001);
    }

    /** Comparison-operator string criteria: >, >=, <, <=, <> */
    @Test
    void testComparisonOperatorCriteria() {
        Sheet sheet = workbook.createSheet("test");
        Row row = sheet.createRow(0);
        Cell formulaCell = row.createCell(0, CellType.FORMULA);
        row.createCell(1).setCellValue(1.0);
        row.createCell(2).setCellValue(3.0);
        row.createCell(3).setCellValue(5.0);
        row.createCell(4).setCellValue(7.0);

        formulaCell.setCellFormula("COUNTIFS(B1:E1,\">3\")");
        assertEquals(2.0d, workbook.getCreationHelper().createFormulaEvaluator().evaluate(formulaCell).getNumberValue(), 0.000000000000001);

        formulaCell.setCellFormula("COUNTIFS(B1:E1,\">=3\")");
        assertEquals(3.0d, workbook.getCreationHelper().createFormulaEvaluator().evaluate(formulaCell).getNumberValue(), 0.000000000000001);

        formulaCell.setCellFormula("COUNTIFS(B1:E1,\"<5\")");
        assertEquals(2.0d, workbook.getCreationHelper().createFormulaEvaluator().evaluate(formulaCell).getNumberValue(), 0.000000000000001);

        formulaCell.setCellFormula("COUNTIFS(B1:E1,\"<>3\")");
        assertEquals(3.0d, workbook.getCreationHelper().createFormulaEvaluator().evaluate(formulaCell).getNumberValue(), 0.000000000000001);
    }

    /** Wildcard characters * (zero-or-more) and ? (exactly one) in string criteria. */
    @Test
    void testWildcardCriteria() {
        Sheet sheet = workbook.createSheet("test");
        Row row = sheet.createRow(0);
        Cell formulaCell = row.createCell(0, CellType.FORMULA);
        row.createCell(1).setCellValue("apple");
        row.createCell(2).setCellValue("application");
        row.createCell(3).setCellValue("banana");
        row.createCell(4).setCellValue("apt");

        // "app*" matches "apple" and "application"
        formulaCell.setCellFormula("COUNTIFS(B1:E1,\"app*\")");
        assertEquals(2.0d, workbook.getCreationHelper().createFormulaEvaluator().evaluate(formulaCell).getNumberValue(), 0.000000000000001);

        // "ap?" matches exactly 3-character strings starting with "ap": only "apt"
        formulaCell.setCellFormula("COUNTIFS(B1:E1,\"ap?\")");
        assertEquals(1.0d, workbook.getCreationHelper().createFormulaEvaluator().evaluate(formulaCell).getNumberValue(), 0.000000000000001);
    }

    /**
     * Multi-row ranges: AND logic must hold for every row position across all criteria ranges.
     * Only rows where every criterion is satisfied contribute to the count.
     */
    @Test
    void testMultiRowRangeWithAndLogic() {
        Sheet sheet = workbook.createSheet("test");
        Row row0 = sheet.createRow(0);
        Row row1 = sheet.createRow(1);
        Row row2 = sheet.createRow(2);

        Cell formulaCell = row0.createCell(0, CellType.FORMULA);
        // row 0: B=1, C=10  both criteria match  → counted
        // row 1: B=1, C=20  second doesn't match → not counted
        // row 2: B=2, C=10  first doesn't match  → not counted
        row0.createCell(1).setCellValue(1.0);  row0.createCell(2).setCellValue(10.0);
        row1.createCell(1).setCellValue(1.0);  row1.createCell(2).setCellValue(20.0);
        row2.createCell(1).setCellValue(2.0);  row2.createCell(2).setCellValue(10.0);

        formulaCell.setCellFormula("COUNTIFS(B1:B3,1,C1:C3,10)");
        CellValue result = workbook.getCreationHelper().createFormulaEvaluator().evaluate(formulaCell);
        assertEquals(1.0d, result.getNumberValue(), 0.000000000000001);
    }

    /** Three criteria pairs — AND logic across all three must hold. */
    @Test
    void testThreeCriteriaPairs() {
        Sheet sheet = workbook.createSheet("test");
        Row row0 = sheet.createRow(0);
        Row row1 = sheet.createRow(1);
        Row row2 = sheet.createRow(2);

        Cell formulaCell = row0.createCell(0, CellType.FORMULA);
        // row 0: B=1, C=2, D=3  all three match  → counted
        // row 1: B=1, C=2, D=9  third doesn't match → not counted
        // row 2: B=1, C=9, D=3  second doesn't match → not counted
        row0.createCell(1).setCellValue(1.0); row0.createCell(2).setCellValue(2.0); row0.createCell(3).setCellValue(3.0);
        row1.createCell(1).setCellValue(1.0); row1.createCell(2).setCellValue(2.0); row1.createCell(3).setCellValue(9.0);
        row2.createCell(1).setCellValue(1.0); row2.createCell(2).setCellValue(9.0); row2.createCell(3).setCellValue(3.0);

        formulaCell.setCellFormula("COUNTIFS(B1:B3,1,C1:C3,2,D1:D3,3)");
        CellValue result = workbook.getCreationHelper().createFormulaEvaluator().evaluate(formulaCell);
        assertEquals(1.0d, result.getNumberValue(), 0.000000000000001);
    }

    /**
     * Criteria ranges with incompatible shapes (different row/column counts) must return
     * VALUE_INVALID (error code 15).
     */
    @Test
    void testMismatchedCriteriaRangeSizesReturnsError() {
        Sheet sheet = workbook.createSheet("test");
        Row row0 = sheet.createRow(0);
        Row row1 = sheet.createRow(1);
        Cell formulaCell = row0.createCell(0, CellType.FORMULA);
        row0.createCell(1).setCellValue(1.0); row0.createCell(2).setCellValue(1.0);
        row1.createCell(1).setCellValue(2.0);

        // B1:C1 is 1×2, B1:B2 is 2×1 — incompatible shapes
        formulaCell.setCellFormula("COUNTIFS(B1:C1,1,B1:B2,1)");
        CellValue result = workbook.getCreationHelper().createFormulaEvaluator().evaluate(formulaCell);
        assertEquals(15, result.getErrorValue());
    }

    /**
     * Empty-string criteria ("") must match blank cells, exercising the BlankEval branch in
     * StringOperandMatcher.
     */
    @Test
    void testBlankCellsMatchedByEmptyCriteria() {
        Sheet sheet = workbook.createSheet("test");
        Row row = sheet.createRow(0);
        Cell formulaCell = row.createCell(0, CellType.FORMULA);
        row.createCell(1).setCellValue(1.0);   // B1 non-blank
        row.createCell(2, CellType.BLANK);     // C1 blank
        row.createCell(3).setCellValue(2.0);   // D1 non-blank
        row.createCell(4, CellType.BLANK);     // E1 blank

        formulaCell.setCellFormula("COUNTIFS(B1:E1,\"\")");
        CellValue result = workbook.getCreationHelper().createFormulaEvaluator().evaluate(formulaCell);
        assertEquals(2.0d, result.getNumberValue(), 0.000000000000001);
    }

    /**
     * A single-cell range reference (RefEval) must be accepted as a criteria range by
     * convertRangeArg, which converts it to a 1×1 AreaEval.
     */
    @Test
    void testSingleCellRange() {
        Sheet sheet = workbook.createSheet("test");
        Row row = sheet.createRow(0);
        Cell formulaCell = row.createCell(0, CellType.FORMULA);
        row.createCell(1).setCellValue(5.0);  // B1 = 5

        formulaCell.setCellFormula("COUNTIFS(B1,5)");
        CellValue result = workbook.getCreationHelper().createFormulaEvaluator().evaluate(formulaCell);
        assertEquals(1.0d, result.getNumberValue(), 0.000000000000001);
    }

    /**
     * Criteria supplied as a cell reference (RefEval) is dereferenced before matching,
     * exercising the evaluateCriteriaArg path in Countif.createCriteriaPredicate.
     */
    @Test
    void testCriteriaFromCellReference() {
        Sheet sheet = workbook.createSheet("test");
        Row row = sheet.createRow(0);
        Cell formulaCell = row.createCell(0, CellType.FORMULA); // A1
        row.createCell(1).setCellValue(1.0);  // B1
        row.createCell(2).setCellValue(2.0);  // C1
        row.createCell(3).setCellValue(1.0);  // D1
        row.createCell(4).setCellValue(1.0);  // E1 — used as criteria value

        formulaCell.setCellFormula("COUNTIFS(B1:D1,E1)");
        CellValue result = workbook.getCreationHelper().createFormulaEvaluator().evaluate(formulaCell);
        assertEquals(2.0d, result.getNumberValue(), 0.000000000000001);
    }
}
