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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link DollarDe}
 */
final class TestDollarDe {

    private static final OperationEvaluationContext ec = new OperationEvaluationContext(null, null, 0, 0, 0, null);

    @Test
    void testInvalid() {
        confirmInvalidError("A1","B2");
    }

    @Test
    void testNumError() {
        confirmNumError("22.5","-40");
    }

    @Test
    void testDiv0() {
        confirmDiv0("22.5","0");
        confirmDiv0("22.5","0.9");
        confirmDiv0("22.5","-0.9");
    }

    //https://support.microsoft.com/en-us/office/dollarde-function-db85aab0-1677-428a-9dfd-a38476693427
    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = row.createCell(0);
            double tolerance = 0.000000000000001;
            assertDouble(fe, cell, "DOLLARDE(1.02,16)", 1.125, tolerance);
            assertDouble(fe, cell, "DOLLARDE(1.02,16.9)", 1.125, tolerance);
            assertDouble(fe, cell, "DOLLARDE(1.32,16)", 3.0, tolerance);
            assertDouble(fe, cell, "DOLLARDE(-1.02,16)", -1.125, tolerance);
            assertDouble(fe, cell, "DOLLARDE(1.1,32)", 1.3125, tolerance);
            assertDouble(fe, cell, "DOLLARDE(1.1,32.1)", 1.3125, tolerance);
            assertDouble(fe, cell, "DOLLARDE(1.0,32)", 1.0, tolerance);
            assertDouble(fe, cell, "DOLLARDE(1.000001,32)", 1.000003125, tolerance);
        }
    }

    private static ValueEval invokeValue(String number1, String number2) {
        ValueEval[] args = new ValueEval[] { new StringEval(number1), new StringEval(number2) };
        return DollarDe.instance.evaluate(args, ec);
    }

    private static void confirmValue(String number1, String number2, double expected) {
        ValueEval result = invokeValue(number1, number2);
        assertEquals(NumberEval.class, result.getClass());
        assertEquals(expected, ((NumberEval) result).getNumberValue(), 0.00000000000001);
    }

    private static void confirmInvalidError(String number1, String number2) {
        ValueEval result = invokeValue(number1, number2);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }

    private static void confirmNumError(String number1, String number2) {
        ValueEval result = invokeValue(number1, number2);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.NUM_ERROR, result);
    }

    private static void confirmDiv0(String number1, String number2) {
        ValueEval result = invokeValue(number1, number2);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.DIV_ZERO, result);
    }

}
