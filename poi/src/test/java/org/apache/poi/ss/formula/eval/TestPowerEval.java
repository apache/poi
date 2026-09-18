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

package org.apache.poi.ss.formula.eval;

import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.apache.poi.ss.util.Utils.assertError;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.jupiter.api.Test;

/**
 * Tests for power operator evaluator.
 */
final class TestPowerEval {
    @Test
    void testPositiveValues() {
        confirm(0, 0, 1);
        confirm(1, 1, 0);
        confirm(9, 3, 2);
    }

    @Test
    void testNegativeValues() {
        confirm(-1, -1, 1);
        confirm(1, 1, -1);
        confirm(1, -10, 0);
        confirm((1.0/3), 3, -1);
    }

    @Test
    void testPositiveDecimalValues() {
        confirm(3, 27, (1/3.0));
    }

    @Test
    void testNegativeDecimalValues() {
        // Excel: a negative base with a non-integer exponent is #NUM!, the same as POWER(-27,1/3)
        // (LibreOffice returns -3 here, which is probably where bug 62121's expectation came from)
        confirmError(-27, (1/3.0));
        confirmError(-8, 0.5);
        confirmError(-8, (2/3.0));
        confirmError(-8, (4/3.0));
        confirm(-27, -27, 1);
        confirm(9, -3, 2);
        confirm(-27, -3, 3);
        confirm(0.25, -2, -2);
    }

    @Test
    void testErrorValues() {
        confirmError(-1.00001, 1.1);
    }

    @Test
    void testInSpreadSheet() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Sheet1");
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        cell.setCellFormula("B1^C1");
        row.createCell(1).setCellValue(-27);
        row.createCell(2).setCellValue((1/3.0));

        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        CellValue cv = fe.evaluate(cell);

        assertEquals(CellType.ERROR, cv.getCellType());
        assertEquals(FormulaError.NUM.getCode(), cv.getErrorValue());
    }

    /**
     * Excel's operator precedence puts negation above '^': -2^2 is (-2)^2 = 4.
     * The parser used to produce -(2^2).
     */
    @Test
    void testNegationBindsTighterThanPower() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
        cell.getRow().createCell(1).setCellValue(2);
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        assertDouble(fe, cell, "-2^2", 4);
        assertDouble(fe, cell, "-B1^2", 4);
        assertDouble(fe, cell, "-SUM(2)^2", 4);
        assertDouble(fe, cell, "-(2)^2", 4);
        assertDouble(fe, cell, "--2^2", 4);
        assertDouble(fe, cell, "+2^2", 4);
        assertDouble(fe, cell, "-2^2^3", 64);
        assertDouble(fe, cell, "-2^3", -8);
        // but binary minus is below '^', and parentheses do what they say
        assertDouble(fe, cell, "0-2^2", -4);
        assertDouble(fe, cell, "-(2^2)", -4);
        assertDouble(fe, cell, "(-2)^2", 4);
        assertDouble(fe, cell, "1-2^2", -3);
        // a sign in the exponent is unaffected
        assertDouble(fe, cell, "2^-2", 0.25);
        assertDouble(fe, cell, "2^-2^2", 0.0625);
        assertDouble(fe, cell, "-2^-2", 0.25);
        // negation still binds tighter than percent, and percent tighter than '^'
        assertDouble(fe, cell, "-2%", -0.02);
        assertDouble(fe, cell, "-2%^2", 0.0004);
        assertDouble(fe, cell, "2^200%", 4);
        // so -2^0.5 is (-2)^0.5, which is #NUM! in Excel, not -(2^0.5)
        assertError(fe, cell, "-2^0.5", FormulaError.NUM);
        assertError(fe, cell, "(-2)^0.5", FormulaError.NUM);
        assertError(fe, cell, "-27^(1/3)", FormulaError.NUM);
        assertDouble(fe, cell, "0-2^0.5", -Math.pow(2, 0.5));
        assertDouble(fe, cell, "0-27^(1/3)", -3);
        assertDouble(fe, cell, "-(27^(1/3))", -3);
    }

    private void confirm(double expected, double a, double b) {
        NumberEval result = (NumberEval) evaluate(a, b);

        assertEquals(expected, result.getNumberValue(), 0);
    }

    private void confirmError(double a, double b) {
        ErrorEval result = (ErrorEval) evaluate(a, b);

        assertEquals("#NUM!", result.getErrorString());
    }

    private static ValueEval evaluate(double... dArgs) {
        ValueEval[] evalArgs;
        evalArgs = new ValueEval[dArgs.length];
        for (int i = 0; i < evalArgs.length; i++) {
            evalArgs[i] = new NumberEval(dArgs[i]);
        }
        return EvalInstances.Power.evaluate(evalArgs, -1, (short) -1);
    }
}
