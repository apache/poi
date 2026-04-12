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

package org.apache.poi.ss.formula.functions;

import static org.apache.poi.ss.util.Utils.addRow;
import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.NumericValueEval;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

/**
 * Test cases for SUMPRODUCT()
 */
final class TestSumproduct {

    private static ValueEval invokeSumproduct(ValueEval[] args) {
        // srcCellRow and srcCellColumn are ignored by SUMPRODUCT
        return new Sumproduct().evaluate(args, -1, (short)-1);
    }

    private static void confirmDouble(double expected, ValueEval actualEval) {
        assertTrue(actualEval instanceof NumericValueEval, "Expected numeric result");
        NumericValueEval nve = (NumericValueEval)actualEval;
        assertEquals(expected, nve.getNumberValue(), 0);
    }

    @Test
    void testScalarSimple() {

        RefEval refEval = EvalFactory.createRefEval("A1", new NumberEval(3));
        ValueEval[] args = {
            refEval,
            new NumberEval(2),
        };
        ValueEval result = invokeSumproduct(args);
        confirmDouble(6D, result);
    }

    @Test
    void testAreaSimple() {
        ValueEval[] aValues = {
            new NumberEval(2),
            new NumberEval(4),
            new NumberEval(5),
        };
        ValueEval[] bValues = {
            new NumberEval(3),
            new NumberEval(6),
            new NumberEval(7),
        };
        AreaEval aeA = EvalFactory.createAreaEval("A1:A3", aValues);
        AreaEval aeB = EvalFactory.createAreaEval("B1:B3", bValues);

        ValueEval[] args = { aeA, aeB, };
        ValueEval result = invokeSumproduct(args);
        confirmDouble(65D, result);
    }

    /**
     * For scalar products, the terms may be 1x1 area refs
     */
    @Test
    void testOneByOneArea() {

        AreaEval ae = EvalFactory.createAreaEval("A1:A1", new ValueEval[] { new NumberEval(7), });

        ValueEval[] args = {
                ae,
                new NumberEval(2),
            };
        ValueEval result = invokeSumproduct(args);
        confirmDouble(14D, result);
    }

    @Test
    void testMismatchAreaDimensions() {

        AreaEval aeA = EvalFactory.createAreaEval("A1:A3", new ValueEval[3]);
        AreaEval aeB = EvalFactory.createAreaEval("B1:D1", new ValueEval[3]);

        ValueEval[] args;
        args = new ValueEval[] { aeA, aeB, };
        assertEquals(ErrorEval.VALUE_INVALID, invokeSumproduct(args));

        args = new ValueEval[] { aeA, new NumberEval(5), };
        assertEquals(ErrorEval.VALUE_INVALID, invokeSumproduct(args));
    }

    @Test
    void testAreaWithErrorCell() {
        ValueEval[] aValues = {
            ErrorEval.REF_INVALID,
            null,
        };
        AreaEval aeA = EvalFactory.createAreaEval("A1:A2", aValues);
        AreaEval aeB = EvalFactory.createAreaEval("B1:B2", new ValueEval[2]);

        ValueEval[] args = { aeA, aeB, };
        assertEquals(ErrorEval.REF_INVALID, invokeSumproduct(args));
    }

    @Test
    void testMicrosoftExample1() throws Exception {
        //https://support.microsoft.com/en-us/office/sumproduct-function-16753e75-9f68-4874-94ac-4d2145a2fd2e
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertDouble(fe, cell, "SUMPRODUCT(C2:C5,D2:D5)", 78.97);
        }
    }

    @Test
    void testMicrosoftExample3() throws Exception {
        //https://support.microsoft.com/en-us/office/sumproduct-function-16753e75-9f68-4874-94ac-4d2145a2fd2e
        try (HSSFWorkbook wb = initWorkbook3()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(11).createCell(3);
            assertDouble(fe, cell, "SUMPRODUCT((B2:B9=B12)*(C2:C9=C12)*D2:D9)", 5249);
        }
    }

    /**
     * Bug 60848 - SUMPRODUCT with unary minus on an array argument fails when the
     * range does not intersect the formula cell's row/column.
     * e.g. =SUMPRODUCT(--(B5:B20)) evaluated in cell A3.
     */
    @Test
    void testBug60848_unaryMinusArrayFirstArg() throws Exception {
        // Formula cell is in row 0 (A1), data is in rows 4-19 (B5:B20 equivalent).
        // Without the fix, implicit intersection fails because row 0 is outside B5:B20.
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            // put values in B5:B8 (rows 4-7, col 1)
            sheet.createRow(4).createCell(1).setCellValue(1.0);
            sheet.createRow(5).createCell(1).setCellValue(2.0);
            sheet.createRow(6).createCell(1).setCellValue(3.0);
            sheet.createRow(7).createCell(1).setCellValue(4.0);
            // formula cell at A1 (row 0) - outside the referenced range rows 4-7
            HSSFCell formulaCell = sheet.createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            // double-negation of {1,2,3,4} = {1,2,3,4}, sum = 10
            assertDouble(fe, formulaCell, "SUMPRODUCT(--(B5:B8))", 10.0);
        }
    }

    @Test
    void testUnaryMinusArrayArgument() throws Exception {
        // =SUMPRODUCT(--(A1:A3)) where A1:A3 = {1, 2, 3} → double-negated = {1,2,3} → sum = 6
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            addRow(sheet, 0, 1.0);
            addRow(sheet, 1, 2.0);
            addRow(sheet, 2, 3.0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = sheet.getRow(0).createCell(5);
            assertDouble(fe, cell, "SUMPRODUCT(--(A1:A3))", 6.0);
        }
    }

    @Test
    void testUnaryMinusWithComparisonArrayArg() throws Exception {
        // =SUMPRODUCT(--(A1:A4>=3)) where A1:A4 = {1,2,3,4} → {FALSE,FALSE,TRUE,TRUE} → {0,0,1,1} → sum = 2
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            addRow(sheet, 0, 1.0);
            addRow(sheet, 1, 2.0);
            addRow(sheet, 2, 3.0);
            addRow(sheet, 3, 4.0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = sheet.getRow(0).createCell(5);
            assertDouble(fe, cell, "SUMPRODUCT(--(A1:A4>=3))", 2.0);
        }
    }

    @Test
    void testUnaryMinusArrayFirstArgWithSecondArrayArg() throws Exception {
        // =SUMPRODUCT(--(A1:A4>=2), B1:B4) where A={1,2,3,4}, B={10,20,30,40}
        // A>=2 → {F,T,T,T} → {0,1,1,1}, multiplied by B = 0+20+30+40 = 90
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            addRow(sheet, 0, 1.0, 10.0);
            addRow(sheet, 1, 2.0, 20.0);
            addRow(sheet, 2, 3.0, 30.0);
            addRow(sheet, 3, 4.0, 40.0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = sheet.getRow(0).createCell(5);
            assertDouble(fe, cell, "SUMPRODUCT(--(A1:A4>=2),B1:B4)", 90.0);
        }
    }

    @Test
    void testArrayArgAsSecondArg() throws Exception {
        // =SUMPRODUCT(B1:B4,--(A1:A4>=2)) - -- as second arg (this worked before the fix too)
        // Same data as above, result should also be 90
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            addRow(sheet, 0, 1.0, 10.0);
            addRow(sheet, 1, 2.0, 20.0);
            addRow(sheet, 2, 3.0, 30.0);
            addRow(sheet, 3, 4.0, 40.0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = sheet.getRow(0).createCell(5);
            assertDouble(fe, cell, "SUMPRODUCT(B1:B4,--(A1:A4>=2))", 90.0);
        }
    }

    /**
     * Comment in bug 65907: "even the B2:B9=B12 bit seems to cause issues in POI - this is
     * evaluated before SUMPRODUCT function code is called and POI does not seem to know how
     * to interpret this and returns an ErrorEval."
     * Tests SUMPRODUCT with a range=cellref comparison multiplied by another range.
     */
    @Test
    void testRangeEqualsCellRefMultipliedByRange() throws Exception {
        // =SUMPRODUCT((A1:A4=B1)*C1:C4) where A={1,2,1,2}, B1=1, C={10,20,30,40}
        // A=B1 → {T,F,T,F} → {1,0,1,0} * {10,20,30,40} = 10+0+30+0 = 40
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            addRow(sheet, 0, 1.0, 1.0, 10.0);  // A1=1, B1=1, C1=10
            addRow(sheet, 1, 2.0, null, 20.0);  // A2=2,      C2=20
            addRow(sheet, 2, 1.0, null, 30.0);  // A3=1,      C3=30
            addRow(sheet, 3, 2.0, null, 40.0);  // A4=2,      C4=40
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = sheet.getRow(0).createCell(5);
            assertDouble(fe, cell, "SUMPRODUCT((A1:A4=B1)*C1:C4)", 40.0);
        }
    }

    /**
     * RouSi's case from bug 65907: =SUMPRODUCT(($N$6:$N$26="镀锌板")*($M$6:$M$26))
     * was failing because OperandResolver.chooseSingleElementFromAreaInternal threw
     * EvaluationException.invalidValue() when the formula cell row was outside the
     * referenced range. Tests SUMPRODUCT with a range="string literal" comparison.
     */
    @Test
    void testRangeEqualsStringLiteralMultipliedByRange() throws Exception {
        // =SUMPRODUCT((A1:A4="yes")*B1:B4) where A={"yes","no","yes","no"}, B={10,20,30,40}
        // A="yes" → {T,F,T,F} → {1,0,1,0} * {10,20,30,40} = 10+0+30+0 = 40
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            addRow(sheet, 0, "yes", 10.0);
            addRow(sheet, 1, "no",  20.0);
            addRow(sheet, 2, "yes", 30.0);
            addRow(sheet, 3, "no",  40.0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = sheet.getRow(0).createCell(5);
            assertDouble(fe, cell, "SUMPRODUCT((A1:A4=\"yes\")*B1:B4)", 40.0);
        }
    }

    /**
     * RouSi's exact pattern with a non-ASCII string literal (Chinese characters),
     * where the formula cell is in row 0 and the data range starts at row 5,
     * so the formula cell is outside the referenced range (the original failure mode).
     */
    @Test
    void testRangeEqualsNonAsciiStringOutOfRange() throws Exception {
        // Mirrors =SUMPRODUCT(($N$6:$N$26="镀锌板")*($M$6:$M$26)) with formula cell at row 0.
        // N6:N9 = {"镀锌板","other","镀锌板","other"}, M6:M9 = {100,200,300,400}
        // match → {1,0,1,0} * {100,200,300,400} = 100+0+300+0 = 400
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            // data in rows 5-8 (N6:N9, M6:M9 in 1-based Excel terms)
            sheet.createRow(5).createCell(13).setCellValue("\u9547\u950c\u677f"); // 镀锌板 col N
            sheet.getRow(5).createCell(12).setCellValue(100.0);                   // col M
            sheet.createRow(6).createCell(13).setCellValue("other");
            sheet.getRow(6).createCell(12).setCellValue(200.0);
            sheet.createRow(7).createCell(13).setCellValue("\u9547\u950c\u677f");
            sheet.getRow(7).createCell(12).setCellValue(300.0);
            sheet.createRow(8).createCell(13).setCellValue("other");
            sheet.getRow(8).createCell(12).setCellValue(400.0);
            // formula cell at row 0 - outside the referenced range rows 5-8
            HSSFCell formulaCell = sheet.createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertDouble(fe, formulaCell,
                    "SUMPRODUCT((N6:N9=\"\u9547\u950c\u677f\")*M6:M9)", 400.0);
        }
    }

    private HSSFWorkbook initWorkbook1() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, null , "Item", "Cost per unit", "Quantity");
        addRow(sheet, 1, null, "Green Tea", 3.25, 9);
        addRow(sheet, 2, null, "Chai", 2.20, 7);
        addRow(sheet, 3, null, "Mint", 4.20, 3);
        addRow(sheet, 4, null, "Ginger", 3.62, 6);
        return wb;
    }

    private HSSFWorkbook initWorkbook3() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, null , "Region", "Item", "Sales");
        addRow(sheet, 1, null, "North", "Apples", 2763);
        addRow(sheet, 2, null, "South", "Pears", 9359);
        addRow(sheet, 3, null, "East", "Cherries", 3830);
        addRow(sheet, 4, null, "West", "Bananas", 8720);
        addRow(sheet, 5, null, "North", "Pears", 1873);
        addRow(sheet, 6, null, "South", "Apples", 4065);
        addRow(sheet, 7, null, "East", "Cherries", 1419);
        addRow(sheet, 8, null, "West", "Bananas", 7173);
        addRow(sheet, 9);
        addRow(sheet, 10, null , "Region", "Item", "Sales");
        addRow(sheet, 11, null , "East", "Cherries");
        return wb;
    }
}
