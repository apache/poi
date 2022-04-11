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

import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Test cases for ROUND(), ROUNDUP(), ROUNDDOWN()
 */
final class TestRoundFuncs {
    @Test
    void testRoundUp() {
        assertRoundUpEquals(797.40, 3987*0.2, 2, 1e-10);
    }

    @Test
    void testRoundDown() {
        assertRoundDownEquals(797.40, 3987*0.2, 2, 1e-10);
    }

    @Test
    void testRound() {
        assertRoundEquals(797.40, 3987*0.2, 2, 1e-10);
    }

    @Test
    void testRoundDownWithStringArg() {
        ValueEval strArg = new StringEval("abc");
        ValueEval[] args = { strArg, new NumberEval(2), };
        ValueEval result = NumericFunction.ROUNDDOWN.evaluate(args, -1, (short)-1);
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }

    @Test
    void testGithub321() throws IOException {
        try (HSSFWorkbook hssfWorkbook = new HSSFWorkbook()) {
            HSSFSheet hssfSheet = hssfWorkbook.createSheet();
            HSSFRow hssfRow = hssfSheet.createRow(0);
            HSSFCell hssfCell = hssfRow.createCell(0);
            hssfCell.setCellValue(2.05d);
            FormulaEvaluator formulaEvaluator = hssfWorkbook.getCreationHelper().createFormulaEvaluator();
            HSSFCell formulaCell = hssfRow.createCell(1);
            assertDouble(formulaEvaluator, formulaCell, "ROUND(A1, 1)", 2.1d, 1E-25);
        }
    }

    @Test
    void testRoundUpWithStringArg() {
        ValueEval strArg = new StringEval("abc");
        ValueEval[] args = { strArg, new NumberEval(2), };
        ValueEval result = NumericFunction.ROUNDUP.evaluate(args, -1, (short)-1);
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }



    private static void assertRoundFuncEquals(Function func, double expected, double number, double places, double tolerance) {
        ValueEval[] args = { new NumberEval( number ), new NumberEval(places), };
        NumberEval result = (NumberEval) func.evaluate(args, -1, (short)-1);
        assertEquals(expected, result.getNumberValue(), tolerance);
    }

    private static void assertRoundEquals(double expected, double number, double places, double tolerance) {
        TestRoundFuncs.assertRoundFuncEquals(NumericFunction.ROUND, expected, number, places, tolerance);
    }

    private static void assertRoundUpEquals(double expected, double number, double places, double tolerance) {
        TestRoundFuncs.assertRoundFuncEquals(NumericFunction.ROUNDUP, expected, number, places, tolerance);
    }

    private static void assertRoundDownEquals(double expected, double number, double places, double tolerance) {
        TestRoundFuncs.assertRoundFuncEquals(NumericFunction.ROUNDDOWN, expected, number, places, tolerance);
    }
}
