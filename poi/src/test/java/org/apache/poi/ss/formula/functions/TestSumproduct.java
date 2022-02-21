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
import org.junit.jupiter.api.Disabled;
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

    @Disabled("https://bz.apache.org/bugzilla/show_bug.cgi?id=65907")
    @Test
    void testMicrosoftExample3() throws Exception {
        //https://support.microsoft.com/en-us/office/sumproduct-function-16753e75-9f68-4874-94ac-4d2145a2fd2e
        try (HSSFWorkbook wb = initWorkbook3()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(11).createCell(3);
            assertDouble(fe, cell, "SUMPRODUCT((B2:B9=B12)*(C2:C9=C12)*D2:D9)", 5249);
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
