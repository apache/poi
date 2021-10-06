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

import static org.apache.poi.ss.util.Utils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.NumericValueEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Test cases for SUMIF()
 */
final class TestSumif {
    private static final NumberEval _30 = new NumberEval(30);
    private static final NumberEval _40 = new NumberEval(40);
    private static final NumberEval _50 = new NumberEval(50);
    private static final NumberEval _60 = new NumberEval(60);

    @Test
    void testBasic() {
        ValueEval[] arg0values = new ValueEval[] { _30, _30, _40, _40, _50, _50  };
        ValueEval[] arg2values = new ValueEval[] { _30, _40, _50, _60, _60, _60 };

        AreaEval arg0;
        AreaEval arg2;

        arg0 = EvalFactory.createAreaEval("A3:B5", arg0values);
        arg2 = EvalFactory.createAreaEval("D1:E3", arg2values);

        confirm(60.0, arg0, new NumberEval(30.0));
        confirm(70.0, arg0, new NumberEval(30.0), arg2);
        confirm(100.0, arg0, new StringEval(">45"));
        confirm(100.0, arg0, new StringEval(">=45"));
        confirm(100.0, arg0, new StringEval(">=50.0"));
        confirm(140.0, arg0, new StringEval("<45"));
        confirm(140.0, arg0, new StringEval("<=45"));
        confirm(140.0, arg0, new StringEval("<=40.0"));
        confirm(160.0, arg0, new StringEval("<>40.0"));
        confirm(80.0, arg0, new StringEval("=40.0"));
    }

    /**
     * test for bug observed near svn r882931
     */
    @Test
    void testCriteriaArgRange() {
        ValueEval[] arg0values = new ValueEval[] { _50, _60, _50, _50, _50, _30,  };
        ValueEval[] arg1values = new ValueEval[] { _30, _40, _50, _60,  };

        AreaEval arg0;
        AreaEval arg1;
        ValueEval ve;

        arg0 = EvalFactory.createAreaEval("A3:B5", arg0values);
        arg1 = EvalFactory.createAreaEval("A2:D2", arg1values); // single row range

        ve = invokeSumif(0, 2, arg0, arg1);  // invoking from cell C1
        if (ve instanceof NumberEval) {
            NumberEval ne = (NumberEval) ve;
            assertNotEquals(30.0, ne.getNumberValue(), "identified error in SUMIF - criteria arg not evaluated properly");
        }

        confirmDouble(200, ve);

        arg0 = EvalFactory.createAreaEval("C1:D3", arg0values);
        arg1 = EvalFactory.createAreaEval("B1:B4", arg1values); // single column range

        ve = invokeSumif(3, 0, arg0, arg1); // invoking from cell A4

        confirmDouble(60, ve);
    }

    @Test
    void testEvaluateException() {
        assertEquals(ErrorEval.VALUE_INVALID, invokeSumif(-1, -1, BlankEval.instance, new NumberEval(30.0)));
        assertEquals(ErrorEval.VALUE_INVALID, invokeSumif(-1, -1, BlankEval.instance, new NumberEval(30.0), new NumberEval(30.0)));
        assertEquals(ErrorEval.VALUE_INVALID, invokeSumif(-1, -1, new NumberEval(30.0), BlankEval.instance, new NumberEval(30.0)));
        assertEquals(ErrorEval.VALUE_INVALID, invokeSumif(-1, -1, new NumberEval(30.0), new NumberEval(30.0), BlankEval.instance));
    }

    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertDouble(fe, cell, "SUMIF(A2:A5,\">160000\",B2:B5)", 63000);
            assertDouble(fe, cell, "SUMIF(A2:A5,\">160000\")", 900000);
            assertDouble(fe, cell, "SUMIF(A2:A5,300000,B2:B5)", 21000);
            assertDouble(fe, cell, "SUMIF(A2:A5,\">\" & C2,B2:B5)", 49000);
        }
    }

    @Test
    void testMicrosoftExample1WithNA() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1WithNA()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertError(fe, cell, "SUMIF(A2:A6,\">160000\",B2:B6)", FormulaError.NA);
        }
    }

    @Test
    void testMicrosoftExample1WithBooleanAndString() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1WithBooleanAndString()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertDouble(fe, cell, "SUMIF(A2:A7,\">160000\",B2:B7)", 63000);
        }
    }

    @Test
    void testMicrosoftExample2() throws IOException {
        try (HSSFWorkbook wb = initWorkbook2()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertDouble(fe, cell, "SUMIF(A2:A7,\"Fruits\",C2:C7)", 2000);
            assertDouble(fe, cell, "SUMIF(A2:A7,\"Vegetables\",C2:C7)", 12000);
            assertDouble(fe, cell, "SUMIF(B2:B7,\"*es\",C2:C7)", 4300);
            assertDouble(fe, cell, "SUMIF(A2:A7,\"\",C2:C7)", 400);
        }
    }

    //see https://support.microsoft.com/en-us/office/sumif-function-169b8c99-c05c-4483-a712-1697a653039b
    private HSSFWorkbook initWorkbook1() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, "Property Value", "Commission", "Data");
        addRow(sheet, 1, 100000, 7000, 250000);
        addRow(sheet, 2, 200000, 14000);
        addRow(sheet, 3, 300000, 21000);
        addRow(sheet, 4, 400000, 28000);
        return wb;
    }

    private HSSFWorkbook initWorkbook1WithNA() {
        HSSFWorkbook wb = initWorkbook1();
        HSSFSheet sheet = wb.getSheetAt(0);
        addRow(sheet, 5, 500000, FormulaError.NA);
        return wb;
    }

    private HSSFWorkbook initWorkbook1WithBooleanAndString() {
        HSSFWorkbook wb = initWorkbook1();
        HSSFSheet sheet = wb.getSheetAt(0);
        addRow(sheet, 5, 500000, true);
        addRow(sheet, 6, 600000, "abc");
        return wb;
    }

    private HSSFWorkbook initWorkbook2() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, "Category", "Food", "Sales");
        addRow(sheet, 1, "Vegetables", "Tomatoes", 2300);
        addRow(sheet, 2, "Vegetables", "Celery", 5500);
        addRow(sheet, 3, "Fruits", "Oranges", 800);
        addRow(sheet, 4, null, "Butter", 400);
        addRow(sheet, 5, "Vegetables", "Carrots", 4200);
        addRow(sheet, 6, "Fruits", "Apples", 1200);
        return wb;
    }

    private static void confirm(double expectedResult, ValueEval...args) {
        confirmDouble(expectedResult, invokeSumif(-1, -1, args));
    }

    private static ValueEval invokeSumif(int rowIx, int colIx, ValueEval...args) {
        return new Sumif().evaluate(args, rowIx, colIx);
    }

    private static void confirmDouble(double expected, ValueEval actualEval) {
        assertTrue(actualEval instanceof NumericValueEval, "Expected numeric result");
        NumericValueEval nve = (NumericValueEval)actualEval;
        assertEquals(expected, nve.getNumberValue(), 0);
    }
}
