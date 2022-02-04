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
 * Tests for {@link BesselJ}
 */
final class TestBesselJ {

    private static final OperationEvaluationContext ec = new OperationEvaluationContext(null, null, 0, 0, 0, null);

    @Test
    void testInvalid() {
        confirmInvalidError("A1","B2");
    }

    @Test
    void testNumError() {
        confirmNumError("22.5","-40");
    }

    //https://support.microsoft.com/en-us/office/besselj-function-839cb181-48de-408b-9d80-bd02982d94f7
    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = row.createCell(0);
            //this tolerance is too high but commons-math3 and excel don't match up as closely as we'd like
            double tolerance = 0.000001;
            assertDouble(fe, cell, "BESSELJ(1.9, 2)", 0.329925829, tolerance);
            assertDouble(fe, cell, "BESSELJ(1.9, 2.5)", 0.329925829, tolerance);
            assertDouble(fe, cell, "BESSELJ(12.4,7)", -0.217156767, tolerance);
        }
    }

    private static ValueEval invokeValue(String number1, String number2) {
        ValueEval[] args = new ValueEval[] { new StringEval(number1), new StringEval(number2) };
        return BesselJ.instance.evaluate(args, ec);
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

}
