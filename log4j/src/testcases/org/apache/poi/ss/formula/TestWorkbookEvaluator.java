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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.MissingArgEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.ptg.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link WorkbookEvaluator}.
 *
 * @author Josh Micich
 */
class TestWorkbookEvaluator {

    private static final double EPSILON = 0.0000001;

    private static ValueEval evaluateFormula(Ptg[] ptgs) {
        HSSFWorkbook wb = new HSSFWorkbook();
        wb.createSheet().createRow(0).createCell(0);
        EvaluationWorkbook ewb = HSSFEvaluationWorkbook.create(wb);
        OperationEvaluationContext ec = new OperationEvaluationContext(null, ewb, 0, 0, 0, null);
        return new WorkbookEvaluator(null, null, null).evaluateFormula(ec, ptgs);
    }

    /**
     * Make sure that the evaluator can directly handle tAttrSum (instead of relying on re-parsing
     * the whole formula which converts tAttrSum to tFuncVar("SUM") )
     */
    @Test
    void testAttrSum() {

        Ptg[] ptgs = {
            new IntPtg(42),
            AttrPtg.SUM,
        };

        ValueEval result = evaluateFormula(ptgs);
        assertEquals(42, ((NumberEval)result).getNumberValue(), 0.0);
    }

    /**
     * Make sure that the evaluator can directly handle (deleted) ref error tokens
     * (instead of relying on re-parsing the whole formula which converts these
     * to the error constant #REF! )
     */
    @Test
    void testRefErr() {

        confirmRefErr(new RefErrorPtg());
        confirmRefErr(new AreaErrPtg());
        confirmRefErr(new DeletedRef3DPtg(0));
        confirmRefErr(new DeletedArea3DPtg(0));
    }
    private static void confirmRefErr(Ptg ptg) {
        Ptg[] ptgs = {
            ptg,
        };

        ValueEval result = evaluateFormula(ptgs);
        assertEquals(ErrorEval.REF_INVALID, result);
    }

    /**
     * Make sure that the evaluator can directly handle tAttrSum (instead of relying on re-parsing
     * the whole formula which converts tAttrSum to tFuncVar("SUM") )
     */
    @Test
    void testMemFunc() {
        Ptg[] ptgs = {
            new IntPtg(42),
            AttrPtg.SUM,
        };

        ValueEval result = evaluateFormula(ptgs);
        assertEquals(42, ((NumberEval)result).getNumberValue(), 0.0);
    }

    @Test
    void testEvaluateMultipleWorkbooks() {
        HSSFWorkbook wbA = HSSFTestDataSamples.openSampleWorkbook("multibookFormulaA.xls");
        HSSFWorkbook wbB = HSSFTestDataSamples.openSampleWorkbook("multibookFormulaB.xls");

        HSSFFormulaEvaluator evaluatorA = new HSSFFormulaEvaluator(wbA);
        HSSFFormulaEvaluator evaluatorB = new HSSFFormulaEvaluator(wbB);

        // Hook up the workbook evaluators to enable evaluation of formulas across books
        String[] bookNames = { "multibookFormulaA.xls", "multibookFormulaB.xls", };
        HSSFFormulaEvaluator[] evaluators = { evaluatorA, evaluatorB, };
        HSSFFormulaEvaluator.setupEnvironment(bookNames, evaluators);

        HSSFCell cell;

        HSSFSheet aSheet1 = wbA.getSheetAt(0);
        HSSFSheet bSheet1 = wbB.getSheetAt(0);

        // Simple case - single link from wbA to wbB
        confirmFormula(wbA, 0, 0, 0, "[multibookFormulaB.xls]BSheet1!B1");
        cell = aSheet1.getRow(0).getCell(0);
        confirmEvaluation(35, evaluatorA, cell);


        // more complex case - back link into wbA
        // [wbA]ASheet1!A2 references (among other things) [wbB]BSheet1!B2
        confirmFormula(wbA, 0, 1, 0, "[multibookFormulaB.xls]BSheet1!$B$2+2*A3");
        // [wbB]BSheet1!B2 references (among other things) [wbA]AnotherSheet!A1:B2
        confirmFormula(wbB, 0, 1, 1, "SUM([multibookFormulaA.xls]AnotherSheet!$A$1:$B$2)+B3");

        cell = aSheet1.getRow(1).getCell(0);
        confirmEvaluation(264, evaluatorA, cell);

        // change [wbB]BSheet1!B3 (from 50 to 60)
        HSSFCell cellB3 = bSheet1.getRow(2).getCell(1);
        cellB3.setCellValue(60);
        evaluatorB.notifyUpdateCell(cellB3);
        confirmEvaluation(274, evaluatorA, cell);

        // change [wbA]ASheet1!A3 (from 100 to 80)
        HSSFCell cellA3 = aSheet1.getRow(2).getCell(0);
        cellA3.setCellValue(80);
        evaluatorA.notifyUpdateCell(cellA3);
        confirmEvaluation(234, evaluatorA, cell);

        // change [wbA]AnotherSheet!A1 (from 2 to 3)
        HSSFCell cellA1 = wbA.getSheetAt(1).getRow(0).getCell(0);
        cellA1.setCellValue(3);
        evaluatorA.notifyUpdateCell(cellA1);
        confirmEvaluation(235, evaluatorA, cell);
    }

    private static void confirmEvaluation(double expectedValue, HSSFFormulaEvaluator fe, HSSFCell cell) {
        assertEquals(expectedValue, fe.evaluate(cell).getNumberValue(), 0.0);
    }

    private static void confirmFormula(HSSFWorkbook wb, int sheetIndex, int rowIndex, int columnIndex,
            String expectedFormula) {
        HSSFCell cell = wb.getSheetAt(sheetIndex).getRow(rowIndex).getCell(columnIndex);
        assertEquals(expectedFormula, cell.getCellFormula());
    }

    /**
     * This test makes sure that any {@link MissingArgEval} that propagates to
     * the result of a function gets translated to {@link BlankEval}.
     */
    @Test
    void testMissingArg() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Sheet1");
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        cell.setCellFormula("1+IF(1,,)");
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        assertDoesNotThrow(() -> fe.evaluate(cell), "Missing arg result not being handled correctly.");

        CellValue cv = fe.evaluate(cell);
        assertEquals(CellType.NUMERIC, cv.getCellType());
        // adding blank to 1.0 gives 1.0
        assertEquals(1.0, cv.getNumberValue(), 0.0);

        // check with string operand
        cell.setCellFormula("\"abc\"&IF(1,,)");
        fe.notifySetFormula(cell);
        cv = fe.evaluate(cell);
        assertEquals(CellType.STRING, cv.getCellType());
        // adding blank to "abc" gives "abc"
        assertEquals("abc", cv.getStringValue());

        // check CHOOSE()
        cell.setCellFormula("\"abc\"&CHOOSE(2,5,,9)");
        fe.notifySetFormula(cell);
        cv = fe.evaluate(cell);
        assertEquals(CellType.STRING, cv.getCellType());
        // adding blank to "abc" gives "abc"
        assertEquals("abc", cv.getStringValue());
    }

    /**
     * Functions like IF, INDIRECT, INDEX, OFFSET etc can return AreaEvals which
     * should be dereferenced by the evaluator
     */
    @Test
    void testResultOutsideRange() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            Cell cell = wb.createSheet("Sheet1").createRow(0).createCell(0);
            cell.setCellFormula("D2:D5"); // IF(TRUE,D2:D5,D2) or  OFFSET(D2:D5,0,0) would work too
            FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
            CellValue cv;
            try {
                cv = fe.evaluate(cell);
            } catch (IllegalArgumentException e) {
                if ("Specified row index (0) is outside the allowed range (1..4)".equals(e.getMessage())) {
                    fail("Identified bug in result dereferencing");
                }
                throw new RuntimeException(e);
            }
            assertEquals(CellType.ERROR, cv.getCellType());
            assertEquals(ErrorEval.VALUE_INVALID.getErrorCode(), cv.getErrorValue());

            // verify circular refs are still detected properly
            fe.clearAllCachedResultValues();
            cell.setCellFormula("OFFSET(A1,0,0)");
            cv = fe.evaluate(cell);
            assertEquals(CellType.ERROR, cv.getCellType());
            assertEquals(ErrorEval.CIRCULAR_REF_ERROR.getErrorCode(), cv.getErrorValue());
        }
    }


    /**
     * formulas with defined names.
     */
    @Test
    void testNamesInFormulas() throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("Sheet1");

        Name name1 = wb.createName();
        name1.setNameName("aConstant");
        name1.setRefersToFormula("3.14");

        Name name2 = wb.createName();
        name2.setNameName("aFormula");
        name2.setRefersToFormula("SUM(Sheet1!$A$1:$A$3)");

        Name name3 = wb.createName();
        name3.setNameName("aSet");
        name3.setRefersToFormula("Sheet1!$A$2:$A$4");

        Name name4 = wb.createName();
        name4.setNameName("offsetFormula");
        name4.setRefersToFormula("OFFSET(Sheet1!$A$1:$A$4,2,0,2,1)");

        Name name5 = wb.createName();
        name5.setNameName("rowFormula");
        name5.setRefersToFormula("ROW()");

        Row row0 = sheet.createRow(0);
        Row row1 = sheet.createRow(1);
        Row row2 = sheet.createRow(2);
        Row row3 = sheet.createRow(3);
        Row row4 = sheet.createRow(4);
        Row row5 = sheet.createRow(5);

        row0.createCell(0).setCellValue(2);
        row1.createCell(0).setCellValue(5);
        row2.createCell(0).setCellValue(3);
        row3.createCell(0).setCellValue(7);

        row0.createCell(2).setCellFormula("aConstant");
        row1.createCell(2).setCellFormula("aFormula");
        row2.createCell(2).setCellFormula("SUM(aSet)");
        row3.createCell(2).setCellFormula("aConstant+aFormula+SUM(aSet)");
        row4.createCell(2).setCellFormula("SUM(offsetFormula)");
        row5.createCell(2).setCellFormula("rowFormula");

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
        assertEquals(3.14, fe.evaluate(row0.getCell(2)).getNumberValue(), EPSILON);
        assertEquals(10.0, fe.evaluate(row1.getCell(2)).getNumberValue(), EPSILON);
        assertEquals(15.0, fe.evaluate(row2.getCell(2)).getNumberValue(), EPSILON);
        assertEquals(28.14, fe.evaluate(row3.getCell(2)).getNumberValue(), EPSILON);
        assertEquals(10.0, fe.evaluate(row4.getCell(2)).getNumberValue(), EPSILON);
        assertEquals(6.0, fe.evaluate(row5.getCell(2)).getNumberValue(), EPSILON);

        wb.close();
    }

    @Test
    void testIgnoreMissingWorkbooks() {
        // TODO: update this test for meaningful functional behavior
        WorkbookEvaluator evaluator = new WorkbookEvaluator(null, null, null);
        assertFalse(evaluator.isIgnoreMissingWorkbooks());

        evaluator.setIgnoreMissingWorkbooks(true);
        assertTrue(evaluator.isIgnoreMissingWorkbooks());

        evaluator.setIgnoreMissingWorkbooks(false);
        assertFalse(evaluator.isIgnoreMissingWorkbooks());
    }

    @Test
    void testDebugEvaluationOutputForNextEval() {
        // TODO: update this test for meaningful functional behavior
        WorkbookEvaluator evaluator = new WorkbookEvaluator(null, null, null);
        assertFalse(evaluator.isDebugEvaluationOutputForNextEval());

        evaluator.setDebugEvaluationOutputForNextEval(true);
        assertTrue(evaluator.isDebugEvaluationOutputForNextEval());

        evaluator.setDebugEvaluationOutputForNextEval(false);
        assertFalse(evaluator.isDebugEvaluationOutputForNextEval());
    }

// Test IF-Equals Formula Evaluation (bug 58591)

    private Workbook testIFEqualsFormulaEvaluation_setup(String formula, CellType a1CellType) {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("IFEquals");
        Row row = sheet.createRow(0);
        Cell A1 = row.createCell(0);
        Cell B1 = row.createCell(1);
        Cell C1 = row.createCell(2);
        Cell D1 = row.createCell(3);

        switch (a1CellType) {
            case NUMERIC:
                A1.setCellValue(1.0);
                // "A1=1" should return true
                break;
            case STRING:
                A1.setCellValue("1");
                // "A1=1" should return false
                // "A1=\"1\"" should return true
                break;
            case BOOLEAN:
                A1.setCellValue(true);
                // "A1=1" should return true
                break;
            case FORMULA:
                A1.setCellFormula("1");
                // "A1=1" should return true
                break;
            case BLANK:
                A1.setCellValue((String) null);
                // "A1=1" should return false
                break;
            default:
                throw new IllegalArgumentException("unexpected cell type: " + a1CellType);
        }
        B1.setCellValue(2.0);
        C1.setCellValue(3.0);
        D1.setCellFormula(formula);

        return wb;
    }

    private void testIFEqualsFormulaEvaluation_teardown(Workbook wb) {
        assertDoesNotThrow(wb::close, "Unable to close workbook");
    }



    private void testIFEqualsFormulaEvaluation_evaluate(
        String formula, CellType cellType, String expectedFormula, double expectedResult) {
        Workbook wb = testIFEqualsFormulaEvaluation_setup(formula, cellType);
        Cell D1 = wb.getSheet("IFEquals").getRow(0).getCell(3);

        FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();
        CellValue result = eval.evaluate(D1);

        // Call should not modify the contents
        assertEquals(CellType.FORMULA, D1.getCellType());
        assertEquals(expectedFormula, D1.getCellFormula());

        assertEquals(CellType.NUMERIC, result.getCellType());
        assertEquals(expectedResult, result.getNumberValue(), EPSILON);

        testIFEqualsFormulaEvaluation_teardown(wb);
    }

    private void testIFEqualsFormulaEvaluation_eval(
            final String formula, final CellType cellType, final String expectedFormula, final double expectedValue) {
        testIFEqualsFormulaEvaluation_evaluate(formula, cellType, expectedFormula, expectedValue);
        testIFEqualsFormulaEvaluation_evaluateFormulaCell(formula, cellType, expectedFormula, expectedValue);
        testIFEqualsFormulaEvaluation_evaluateInCell(formula, cellType, expectedValue);
        testIFEqualsFormulaEvaluation_evaluateAll(formula, cellType, expectedFormula, expectedValue);
        testIFEqualsFormulaEvaluation_evaluateAllFormulaCells(formula, cellType, expectedFormula, expectedValue);
    }

    @Test
    void testIFEqualsFormulaEvaluation_NumericLiteral() {
        final String formula = "IF(A1=1, 2, 3)";
        final CellType cellType = CellType.NUMERIC;
        final String expectedFormula = "IF(A1=1,2,3)";
        final double expectedValue = 2.0;
        testIFEqualsFormulaEvaluation_eval(formula, cellType, expectedFormula, expectedValue);
    }

    @Test
    void testIFEqualsFormulaEvaluation_Numeric() {
        final String formula = "IF(A1=1, B1, C1)";
        final CellType cellType = CellType.NUMERIC;
        final String expectedFormula = "IF(A1=1,B1,C1)";
        final double expectedValue = 2.0;
        testIFEqualsFormulaEvaluation_eval(formula, cellType, expectedFormula, expectedValue);
    }

    @Test
    void testIFEqualsFormulaEvaluation_NumericCoerceToString() {
        final String formula = "IF(A1&\"\"=\"1\", B1, C1)";
        final CellType cellType = CellType.NUMERIC;
        final String expectedFormula = "IF(A1&\"\"=\"1\",B1,C1)";
        final double expectedValue = 2.0;
        testIFEqualsFormulaEvaluation_eval(formula, cellType, expectedFormula, expectedValue);
    }

    @Test
    void testIFEqualsFormulaEvaluation_String() {
        final String formula = "IF(A1=1, B1, C1)";
        final CellType cellType = CellType.STRING;
        final String expectedFormula = "IF(A1=1,B1,C1)";
        final double expectedValue = 3.0;
        testIFEqualsFormulaEvaluation_eval(formula, cellType, expectedFormula, expectedValue);
    }

    @Test
    void testIFEqualsFormulaEvaluation_StringCompareToString() {
        final String formula = "IF(A1=\"1\", B1, C1)";
        final CellType cellType = CellType.STRING;
        final String expectedFormula = "IF(A1=\"1\",B1,C1)";
        final double expectedValue = 2.0;
        testIFEqualsFormulaEvaluation_eval(formula, cellType, expectedFormula, expectedValue);
    }

    @Test
    void testIFEqualsFormulaEvaluation_StringCoerceToNumeric() {
        final String formula = "IF(A1+0=1, B1, C1)";
        final CellType cellType = CellType.STRING;
        final String expectedFormula = "IF(A1+0=1,B1,C1)";
        final double expectedValue = 2.0;
        testIFEqualsFormulaEvaluation_eval(formula, cellType, expectedFormula, expectedValue);
    }

    @Disabled("Bug 58591: this test currently fails")
    @Test
    void testIFEqualsFormulaEvaluation_Boolean() {
        final String formula = "IF(A1=1, B1, C1)";
        final CellType cellType = CellType.BOOLEAN;
        final String expectedFormula = "IF(A1=1,B1,C1)";
        final double expectedValue = 2.0;
        testIFEqualsFormulaEvaluation_eval(formula, cellType, expectedFormula, expectedValue);
    }

    @Disabled("Bug 58591: this test currently fails")
    @Test
    void testIFEqualsFormulaEvaluation_BooleanSimple() {
        final String formula = "3-(A1=1)";
        final CellType cellType = CellType.BOOLEAN;
        final String expectedFormula = "3-(A1=1)";
        final double expectedValue = 2.0;
        testIFEqualsFormulaEvaluation_eval(formula, cellType, expectedFormula, expectedValue);
    }

    @Test
    void testIFEqualsFormulaEvaluation_Formula() {
        final String formula = "IF(A1=1, B1, C1)";
        final CellType cellType = CellType.FORMULA;
        final String expectedFormula = "IF(A1=1,B1,C1)";
        final double expectedValue = 2.0;
        testIFEqualsFormulaEvaluation_eval(formula, cellType, expectedFormula, expectedValue);
    }

    @Test
    void testIFEqualsFormulaEvaluation_Blank() {
        final String formula = "IF(A1=1, B1, C1)";
        final CellType cellType = CellType.BLANK;
        final String expectedFormula = "IF(A1=1,B1,C1)";
        final double expectedValue = 3.0;
        testIFEqualsFormulaEvaluation_eval(formula, cellType, expectedFormula, expectedValue);
    }

    @Test
    void testIFEqualsFormulaEvaluation_BlankCompareToZero() {
        final String formula = "IF(A1=0, B1, C1)";
        final CellType cellType = CellType.BLANK;
        final String expectedFormula = "IF(A1=0,B1,C1)";
        final double expectedValue = 2.0;
        testIFEqualsFormulaEvaluation_eval(formula, cellType, expectedFormula, expectedValue);
    }

    @Disabled("Bug 58591: this test currently fails")
    @Test
    void testIFEqualsFormulaEvaluation_BlankInverted() {
        final String formula = "IF(NOT(A1)=1, B1, C1)";
        final CellType cellType = CellType.BLANK;
        final String expectedFormula = "IF(NOT(A1)=1,B1,C1)";
        final double expectedValue = 2.0;
        testIFEqualsFormulaEvaluation_eval(formula, cellType, expectedFormula, expectedValue);
    }

    @Disabled("Bug 58591: this test currently fails")
    @Test
    void testIFEqualsFormulaEvaluation_BlankInvertedSimple() {
        final String formula = "3-(NOT(A1)=1)";
        final CellType cellType = CellType.BLANK;
        final String expectedFormula = "3-(NOT(A1)=1)";
        final double expectedValue = 2.0;
        testIFEqualsFormulaEvaluation_eval(formula, cellType, expectedFormula, expectedValue);
    }


    private void testIFEqualsFormulaEvaluation_evaluateFormulaCell(
            String formula, CellType cellType, String expectedFormula, double expectedResult) {
        Workbook wb = testIFEqualsFormulaEvaluation_setup(formula, cellType);
        Cell D1 = wb.getSheet("IFEquals").getRow(0).getCell(3);

        FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();
        CellType resultCellType = eval.evaluateFormulaCell(D1);

        // Call should modify the contents, but leave the formula intact
        assertEquals(CellType.FORMULA, D1.getCellType());
        assertEquals(expectedFormula, D1.getCellFormula());
        assertEquals(CellType.NUMERIC, resultCellType);
        assertEquals(CellType.NUMERIC, D1.getCachedFormulaResultType());
        assertEquals(expectedResult, D1.getNumericCellValue(), EPSILON);

        testIFEqualsFormulaEvaluation_teardown(wb);
    }

    private void testIFEqualsFormulaEvaluation_evaluateInCell(
            String formula, CellType cellType, double expectedResult) {
        Workbook wb = testIFEqualsFormulaEvaluation_setup(formula, cellType);
        Cell D1 = wb.getSheet("IFEquals").getRow(0).getCell(3);

        FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();
        Cell result = eval.evaluateInCell(D1);

        // Call should modify the contents and replace the formula with the result
        // returns the same cell that was provided as an argument so that calls can be chained.
        assertSame(D1, result);
        assertThrows(IllegalStateException.class, D1::getCellFormula, "cell formula should be overwritten with formula result");
        assertEquals(CellType.NUMERIC, D1.getCellType());
        assertEquals(expectedResult, D1.getNumericCellValue(), EPSILON);

        testIFEqualsFormulaEvaluation_teardown(wb);
    }

    private void testIFEqualsFormulaEvaluation_evaluateAll(
            String formula, CellType cellType, String expectedFormula, double expectedResult) {
        Workbook wb = testIFEqualsFormulaEvaluation_setup(formula, cellType);
        Cell D1 = wb.getSheet("IFEquals").getRow(0).getCell(3);

        FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();
        eval.evaluateAll();

        // Call should modify the contents
        assertEquals(CellType.FORMULA, D1.getCellType());
        assertEquals(expectedFormula, D1.getCellFormula());

        assertEquals(CellType.NUMERIC, D1.getCachedFormulaResultType());
        assertEquals(expectedResult, D1.getNumericCellValue(), EPSILON);

        testIFEqualsFormulaEvaluation_teardown(wb);
    }

    private void testIFEqualsFormulaEvaluation_evaluateAllFormulaCells(
            String formula, CellType cellType, String expectedFormula, double expectedResult) {
        Workbook wb = testIFEqualsFormulaEvaluation_setup(formula, cellType);
        Cell D1 = wb.getSheet("IFEquals").getRow(0).getCell(3);

        HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);

        // Call should modify the contents
        assertEquals(CellType.FORMULA, D1.getCellType());
        // whitespace gets deleted because formula is parsed and re-rendered
        assertEquals(expectedFormula, D1.getCellFormula());

        assertEquals(CellType.NUMERIC, D1.getCachedFormulaResultType());
        assertEquals(expectedResult, D1.getNumericCellValue(), EPSILON);

        testIFEqualsFormulaEvaluation_teardown(wb);
    }

    @Test
    void testRefToBlankCellInArrayFormula() {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cellA1 = row.createCell(0);
        Cell cellB1 = row.createCell(1);
        Cell cellC1 = row.createCell(2);
        Row row2 = sheet.createRow(1);
        Cell cellA2 = row2.createCell(0);
        Cell cellB2 = row2.createCell(1);
        Cell cellC2 = row2.createCell(2);
        Row row3 = sheet.createRow(2);
        Cell cellA3 = row3.createCell(0);
        Cell cellB3 = row3.createCell(1);
        Cell cellC3 = row3.createCell(2);

        cellA1.setCellValue("1");
        // cell B1 intentionally left blank
        cellC1.setCellValue("3");

        cellA2.setCellFormula("A1");
        cellB2.setCellFormula("B1");
        cellC2.setCellFormula("C1");

        sheet.setArrayFormula("A1:C1", CellRangeAddress.valueOf("A3:C3"));

        wb.getCreationHelper().createFormulaEvaluator().evaluateAll();

        assertEquals(cellA2.getStringCellValue(), "1");
        assertEquals(cellB2.getNumericCellValue(),0, 0.00001);
        assertEquals(cellC2.getStringCellValue(),"3");

        assertEquals(cellA3.getStringCellValue(), "1");
        assertEquals(cellB3.getNumericCellValue(),0, 0.00001);
        assertEquals(cellC3.getStringCellValue(),"3");
    }
}
