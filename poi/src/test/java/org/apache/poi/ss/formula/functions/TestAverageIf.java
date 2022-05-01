/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.formula.functions;

import static org.apache.poi.ss.util.Utils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.NumericValueEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.junit.jupiter.api.Test;

import java.io.IOException;


/**
 * Test cases for AVERAGEIF()
 */
final class TestAverageIf {

    private static final OperationEvaluationContext EC = new OperationEvaluationContext(null, null, 0, 0, 0, null);

    private static ValueEval invokeAverageif(ValueEval[] args) {
        return new AverageIf().evaluate(args, EC);
    }

    private static void confirmDouble(double expected, ValueEval actualEval) {
        assertTrue(actualEval instanceof NumericValueEval, "Expected numeric result");
        NumericValueEval nve = (NumericValueEval) actualEval;
        assertEquals(expected, nve.getNumberValue(), 0);
    }

    private static void confirm(double expectedResult, ValueEval[] args) {
        confirmDouble(expectedResult, invokeAverageif(args));
    }

    private static void confirmError(ErrorEval errorEval, ValueEval[] args) {
        ValueEval actualEval = invokeAverageif(args);
        assertEquals(errorEval, actualEval);
    }

    /**
     * Example 1 from
     * https://support.microsoft.com/en-us/office/averageif-function-faec8e2e-0dec-4308-af69-f5576d8ac642
     */
    @Test
    void testExample1() {
        ValueEval[] b2b5 = new ValueEval[]{
                new NumberEval(7000),
                new NumberEval(14000),
                new NumberEval(21000),
                new NumberEval(28000)
        };

        ValueEval[] args;
        // "=AVERAGEIF(B2:B5, "<23000")"
        args = new ValueEval[]{
                EvalFactory.createAreaEval("B2:B5", b2b5),
                new StringEval("<23000")
        };
        confirm(14000, args);

        ValueEval[] a2a5 = new ValueEval[]{
                new NumberEval(100000),
                new NumberEval(200000),
                new NumberEval(300000),
                new NumberEval(400000)
        };
        // "=AVERAGEIF(A2:A5, "<250000", A2:A5)"
        args = new ValueEval[]{
                EvalFactory.createAreaEval("A2:A5", a2a5),
                new StringEval("<250000"),
                EvalFactory.createAreaEval("A2:A5", a2a5)
        };
        confirm(150000, args);

        // "=AVERAGEIF(A2:A5, "<95000")"
        args = new ValueEval[]{
                EvalFactory.createAreaEval("A2:A5", a2a5),
                new StringEval("<95000"),
                EvalFactory.createAreaEval("A2:A5", a2a5)
        };

        confirmError(ErrorEval.DIV_ZERO, args);

        // "=AVERAGEIF(A2:A5, "<95000", B2:B5 )"
        args = new ValueEval[]{
                EvalFactory.createAreaEval("A2:A5", a2a5),
                new StringEval(">250000"),
                EvalFactory.createAreaEval("B2:B5", b2b5)
        };
        confirm(24500, args);
    }

    @Test
    void testExample2() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            addRow(sheet, 0, "Region", "Profits (Thousands)");
            addRow(sheet, 1, "East", 45678);
            addRow(sheet, 2, "West", 23789);
            addRow(sheet, 3, "North", -4789);
            addRow(sheet, 4, "South (New Office)", 0);
            addRow(sheet, 5, "Midwest", 9678);
            FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
            HSSFCell testCell = sheet.getRow(0).createCell(100);
            assertDouble(formulaEvaluator, testCell, "AVERAGEIF(A2:A6,\"=*West\",B2:B6)", 16733.5);
            assertDouble(formulaEvaluator, testCell, "AVERAGEIF(A2:A6,\"<>*(New Office)\",B2:B6)", 18589);
        }
    }

}
