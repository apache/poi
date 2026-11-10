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
import static org.apache.poi.ss.util.Utils.assertString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.EvalFactory;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Test;

/**
 * Test for the concatenation operator evaluator.
 */
final class TestConcatEval {

    @Test
    void testBasic() {
        assertEquals("ab", concat(new StringEval("a"), new StringEval("b")));
        assertEquals("a1", concat(new StringEval("a"), new NumberEval(1)));
        assertEquals("TRUEa", concat(BoolEval.TRUE, new StringEval("a")));
        assertEquals("a", concat(new StringEval("a"), BlankEval.instance));
        assertEquals("", concat(BlankEval.instance, BlankEval.instance));
    }

    private static String concat(ValueEval arg0, ValueEval arg1) {
        ValueEval result = ConcatEval.instance.evaluate(new ValueEval[] { arg0, arg1 }, 0, 0);
        return ((StringEval) result).getStringValue();
    }

    @Test
    void testArrayModeIsElementWise() {
        AreaEval area = EvalFactory.createAreaEval("B2:B4", new ValueEval[] {
                new StringEval("a"), new NumberEval(2), BlankEval.instance });
        ValueEval result = ((ConcatEval) ConcatEval.instance)
                .evaluateArray(new ValueEval[] { area, new StringEval("x") }, 0, 0);

        AreaEval resultArea = assertInstanceOf(AreaEval.class, result);
        assertEquals(3, resultArea.getHeight());
        assertEquals(1, resultArea.getWidth());
        assertEquals("ax", ((StringEval) resultArea.getRelativeValue(0, 0)).getStringValue());
        assertEquals("2x", ((StringEval) resultArea.getRelativeValue(1, 0)).getStringValue());
        assertEquals("x", ((StringEval) resultArea.getRelativeValue(2, 0)).getStringValue());
    }

    @Test
    void testInSpreadsheet() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet("Sheet1");
            String[] values = {"a", "b", "a", "c", "b", "a"};
            for (int i = 0; i < values.length; i++) {
                sheet.createRow(4 + i).createCell(0).setCellValue(values[i]);
            }
            HSSFCell cell = sheet.createRow(0).createCell(1);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);

            assertString(fe, cell, "A5&A6", "ab");
            assertString(fe, cell, "A5&\"\"", "a");
            // in an ordinary cell a range operand is implicitly intersected with the cell's row,
            // and row 1 is not inside A5:A10
            assertError(fe, cell, "A5:A10&\"\"", FormulaError.VALUE);

            // inside SUMPRODUCT the operator is applied element-wise (bug 62271): A5:A10&"" is the
            // array of texts, COUNTIF gives one count per text and 1/count sums to one per distinct value
            assertDouble(fe, cell, "SUMPRODUCT(1/COUNTIF(A5:A10,A5:A10&\"\"))", 3);
            assertDouble(fe, cell, "SUMPRODUCT((A5:A10&\"\"=\"a\")*1)", 3);
            assertDouble(fe, cell, "SUMPRODUCT((A5:A10&A5:A10=\"bb\")*1)", 2);

            // the same holds for an array formula
            sheet.setArrayFormula("SUM(1/COUNTIF(A5:A10,A5:A10&\"\"))", CellRangeAddress.valueOf("B2"));
            assertEquals(3, fe.evaluate(sheet.getRow(1).getCell(1)).getNumberValue(), 0);
        }
    }
}
