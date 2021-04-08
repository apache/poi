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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
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
        confirm(-3, -27, (1/3.0));
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

        assertEquals(CellType.NUMERIC, cv.getCellType());
        assertEquals(-3.0, cv.getNumberValue(), 0);
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
