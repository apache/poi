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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.CacheAreaEval;
import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.apache.poi.ss.util.Utils.assertError;
import static org.apache.poi.ss.util.Utils.assertString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Arguments that used to escape the evaluator as an exception, or produce an infinite result,
 * where Excel reports an error value.
 */
final class TestErrorValuesInsteadOfExceptions {

    @Test
    void testRept() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            // NegativeArraySizeException
            assertError(fe, cell, "REPT(\"x\",-1)", FormulaError.VALUE);
            // a 1 GB StringBuilder; Excel caps the result at 32,767 characters
            assertError(fe, cell, "REPT(\"x\",1E9)", FormulaError.VALUE);
            assertError(fe, cell, "REPT(\"ab\",16384)", FormulaError.VALUE);
            assertEquals(32766, ((org.apache.poi.ss.usermodel.CellValue) evaluate(fe, cell, "REPT(\"ab\",16383)")).getStringValue().length());
            assertString(fe, cell, "REPT(\"x\",2.9)", "xx");
            assertString(fe, cell, "REPT(\"x\",0)", "");
            assertString(fe, cell, "REPT(\"x\",{3})", "xxx");
        }
    }

    private static Object evaluate(HSSFFormulaEvaluator fe, HSSFCell cell, String formula) {
        cell.setCellFormula(formula);
        fe.notifyUpdateCell(cell);
        return fe.evaluate(cell);
    }

    @Test
    void testAddress() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            // ADDRESS(0,1) used to give "$A", ADDRESS(-1,-1) an IllegalArgumentException
            assertError(fe, cell, "ADDRESS(0,1)", FormulaError.VALUE);
            assertError(fe, cell, "ADDRESS(1,0)", FormulaError.VALUE);
            assertError(fe, cell, "ADDRESS(-1,-1)", FormulaError.VALUE);
            assertString(fe, cell, "ADDRESS(1,1)", "$A$1");
        }
    }

    @Test
    void testMinverseOfASingularMatrix() {
        ValueEval[] zeros = {NumberEval.ZERO, NumberEval.ZERO, NumberEval.ZERO, NumberEval.ZERO};
        assertEquals(ErrorEval.NUM_ERROR, MatrixFunction.MINVERSE.evaluate(
                new ValueEval[]{new CacheAreaEval(0, 0, 1, 1, zeros)}, 0, 0));
        // {1,2;2,4} is singular too, though rounding let a QR decomposition "invert" it
        ValueEval[] dependent = {new NumberEval(1), new NumberEval(2), new NumberEval(2), new NumberEval(4)};
        assertEquals(ErrorEval.NUM_ERROR, MatrixFunction.MINVERSE.evaluate(
                new ValueEval[]{new CacheAreaEval(0, 0, 1, 1, dependent)}, 0, 0));
        // a regular matrix still inverts: {1,2;3,4}^-1 = {-2,1;1.5,-0.5}
        ValueEval[] regular = {new NumberEval(1), new NumberEval(2), new NumberEval(3), new NumberEval(4)};
        ValueEval result = MatrixFunction.MINVERSE.evaluate(new ValueEval[]{new CacheAreaEval(0, 0, 1, 1, regular)}, 0, 0);
        AreaEval inverse = assertInstanceOf(AreaEval.class, result);
        assertEquals(-2, ((NumberEval) inverse.getRelativeValue(0, 0)).getNumberValue(), 1E-12);
        assertEquals(1, ((NumberEval) inverse.getRelativeValue(0, 1)).getNumberValue(), 1E-12);
        assertEquals(1.5, ((NumberEval) inverse.getRelativeValue(1, 0)).getNumberValue(), 1E-12);
        assertEquals(-0.5, ((NumberEval) inverse.getRelativeValue(1, 1)).getNumberValue(), 1E-12);
    }

    @Test
    void testInfiniteResults() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertError(fe, cell, "SQRTPI(1E308)", FormulaError.NUM);
            // these used to be Infinity: beyond the long range the integer part saturated at 9.2E18
            assertDouble(fe, cell, "DOLLARDE(1E308,1E308)", 1E308, 0);
            assertDouble(fe, cell, "DOLLARFR(1E308,1E308)", 1E308, 0);
            assertDouble(fe, cell, "DOLLARDE(1E19,16)", 1E19, 0);
            assertDouble(fe, cell, "DOLLARFR(1E19,16)", 1E19, 0);
            assertDouble(fe, cell, "DOLLARDE(1E308,16)", 1E308, 0);
            assertDouble(fe, cell, "DOLLARDE(-1.02,16)", -1.125, 0);
            assertDouble(fe, cell, "DOLLARFR(-1.125,16)", -1.02, 0);
        }
    }
}
