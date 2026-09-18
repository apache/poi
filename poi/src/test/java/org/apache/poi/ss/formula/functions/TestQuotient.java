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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for {@link Quotient}
 */
class TestQuotient {
    private static ValueEval invokeValue(String numerator, String denominator) {
        ValueEval[] args = new ValueEval[]{new StringEval(numerator), new StringEval(denominator)};
        return new Quotient().evaluate(args, -1, -1);
    }

    private static void confirmValue(String msg, String numerator, String denominator, String expected) {
        ValueEval result = invokeValue(numerator, denominator);
        assertEquals(NumberEval.class, result.getClass());
        assertEquals(expected, ((NumberEval) result).getStringValue(), msg);
    }

    private static void confirmValueError(String msg, String numerator, String denominator, ErrorEval numError) {
        ValueEval result = invokeValue(numerator, denominator);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(numError, result, msg);
    }

    @Test
    void testBasic() {
        confirmValue("Integer portion of 5/2 (2)", "5", "2", "2");
        confirmValue("Integer portion of 4.5/3.1 (1)", "4.5", "3.1", "1");

        confirmValue("Integer portion of -10/3 (-3)", "-10", "3", "-3");
        confirmValue("Integer portion of -5.5/2 (-2)", "-5.5", "2", "-2");

        confirmValue("Integer portion of Pi/Avogadro (0)", "3.14159", "6.02214179E+23", "0");
    }

    @Test
    void testQuotientOutsideIntRange() {
        // used to escape as an IllegalArgumentException from MathUtil.safeDoubleToInt
        confirmValue("Integer portion of 5E9/2", "5000000000", "2", "2500000000");
        confirmValue("Integer portion of -5E9/2", "-5000000000", "2", "-2500000000");
        confirmValue("Integer portion of 1E20/3", "1E20", "3", "33333333333333300000");
        confirmValue("Integer portion of 2^53/1", "9007199254740992", "1", "9007199254740990");
        confirmValueError("quotient overflows a double", "1E308", "1E-10", ErrorEval.NUM_ERROR);
    }

    @Test
    void testErrors() {
        confirmValueError("numerator is nonnumeric", "ABCD", "", ErrorEval.VALUE_INVALID);
        confirmValueError("denominator is nonnumeric", "", "ABCD", ErrorEval.VALUE_INVALID);

        confirmValueError("dividing by zero", "3.14159", "0", ErrorEval.DIV_ZERO);
    }

    @Test
    void testWithCellRefs() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            addRow(sheet, 0, 5, 2);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertDouble(fe, cell, "QUOTIENT(A1, B1)", 2.0);
        }
    }
}