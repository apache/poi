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
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.addRow;
import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link TDist}
 */
final class TestTDist {

    private static final OperationEvaluationContext ec = new OperationEvaluationContext(null, null, 0, 0, 0, null);

    @Test
    void testBasic() {
        confirmValue("5.968191467", "8", "1", 0.00016754180265310392);
        confirmValue("5.968191467", "8", "2", 0.00033508360530620784);
        confirmValue("5.968191467", "8.2", "2.2", 0.00033508360530620784);
        confirmValue("5.968191467", "8.9", "2.9", 0.00033508360530620784);
    }

    @Test
    void testInvalid() {
        confirmInvalidError("A1","B2","C2");
        confirmInvalidError("5.968191467","8","C2");
        confirmInvalidError("5.968191467","B2","2");
        confirmInvalidError("A1","8","2");
    }

    @Test
    void testNumError() {
        confirmNumError("5.968191467", "8", "0");
        confirmNumError("-5.968191467", "8", "2");
        confirmNumError("5.968191467", "-8", "2");
    }

    //https://support.microsoft.com/en-us/office/tdist-function-630a7695-4021-4853-9468-4a1f9dcdd192
    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            addRow(sheet, 0, "Data", "Description");
            addRow(sheet, 1, 1.959999998, "Value at which to evaluate the distribution");
            addRow(sheet, 2, 60, "Degrees of freedom");
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertDouble(fe, cell, "TDIST(A2,A3,2)", 0.054644930, 0.000001);
            assertDouble(fe, cell, "TDIST(A2,A3,1)", 0.027322465, 0.000001);
        }
    }

    private static ValueEval invokeValue(String number1, String number2, String number3) {
        ValueEval[] args = new ValueEval[] { new StringEval(number1), new StringEval(number2), new StringEval(number3)};
        return TDist.instance.evaluate(args, ec);
    }

    private static void confirmValue(String number1, String number2, String number3, double expected) {
        ValueEval result = invokeValue(number1, number2, number3);
        assertEquals(NumberEval.class, result.getClass());
        assertEquals(expected, ((NumberEval) result).getNumberValue(), 0.0);
    }

    private static void confirmInvalidError(String number1, String number2, String number3) {
        ValueEval result = invokeValue(number1, number2, number3);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }

    private static void confirmNumError(String number1, String number2, String number3) {
        ValueEval result = invokeValue(number1, number2, number3);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.NUM_ERROR, result);
    }

}
