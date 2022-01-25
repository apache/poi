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
 * Tests for {@link NormSInv}
 */
final class TestNormSInv {

    private static final OperationEvaluationContext ec = new OperationEvaluationContext(null, null, 0, 0, 0, null);

    @Test
    void testBasic() {
        confirmValue("0.9088", 1.3334);
    }

    @Test
    void testInvalid() {
        confirmInvalidError("A1");
    }

    @Test
    void testNumError() {
        confirmNumError("0");
        confirmNumError("-0.5");
        confirmNumError("1");
        confirmNumError("1.5");
    }

    //https://support.microsoft.com/en-us/office/normsinv-function-8d1bce66-8e4d-4f3b-967c-30eed61f019d</li>
    //https://support.microsoft.com/en-us/office/norm-s-inv-function-d6d556b4-ab7f-49cd-b526-5a20918452b1</li>
    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = row.createCell(0);
            assertDouble(fe, cell, "NORMSINV(0.9088)", 1.3334, 0.00001);
            assertDouble(fe, cell, "NORM.S.INV(0.9088)", 1.3334, 0.00001);
        }
    }

    private static ValueEval invokeValue(String number1) {
        ValueEval[] args = new ValueEval[] { new StringEval(number1)};
        return NormSInv.instance.evaluate(args, ec);
    }

    private static void confirmValue(String number1, double expected) {
        ValueEval result = invokeValue(number1);
        assertEquals(NumberEval.class, result.getClass());
        assertEquals(expected, ((NumberEval) result).getNumberValue(), 0.00001);
    }

    private static void confirmInvalidError(String number1) {
        ValueEval result = invokeValue(number1);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }

    private static void confirmNumError(String number1) {
        ValueEval result = invokeValue(number1);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.NUM_ERROR, result);
    }

}
