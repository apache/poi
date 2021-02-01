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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link org.apache.poi.ss.formula.functions.Mirr}
 *
 * @author Carlos Delgado (carlos dot del dot est at gmail dot com)
 * @author Cedric Walter (cedric dot walter at gmail dot com)
 * @see org.apache.poi.ss.formula.functions.TestIrr
 */
final class TestMirr {

    @Test
    void testMirr() throws EvaluationException {
        Mirr mirr = new Mirr();
        double mirrValue;

        double financeRate = 0.12;
        double reinvestRate = 0.1;
        double[] values = {-120000d, 39000d, 30000d, 21000d, 37000d, 46000d, reinvestRate, financeRate};
        // MIRR should not failed with these parameters
        mirrValue = mirr.evaluate(values);
        assertEquals(0.126094130366, mirrValue, 0.0000000001);

        reinvestRate = 0.05;
        financeRate = 0.08;
        values = new double[]{-7500d, 3000d, 5000d, 1200d, 4000d, reinvestRate, financeRate};
        // MIRR should not failed with these parameters
        mirrValue = mirr.evaluate(values);
        assertEquals(0.18736225093, mirrValue, 0.0000000001);

        reinvestRate = 0.065;
        financeRate = 0.1;
        values = new double[]{-10000, 3400d, 6500d, 1000d, reinvestRate, financeRate};
        // MIRR should not failed with these parameters
        mirrValue = mirr.evaluate(values);
        assertEquals(0.07039493966, mirrValue, 0.0000000001);

        reinvestRate = 0.07;
        financeRate = 0.01;
        values = new double[]{-10000d, -3400d, -6500d, -1000d, reinvestRate, financeRate};
        // MIRR should not failed with these parameters
        mirrValue = mirr.evaluate(values);
        assertEquals(-1, mirrValue, 0.0);

    }

    @Test
    void testMirrErrors_expectDIV0() {
        Mirr mirr = new Mirr();

        double reinvestRate = 0.05;
        double financeRate = 0.08;
        double[] incomes = {120000d, 39000d, 30000d, 21000d, 37000d, 46000d, reinvestRate, financeRate};

        EvaluationException e = assertThrows(EvaluationException.class, () -> mirr.evaluate(incomes));
        assertEquals(ErrorEval.DIV_ZERO, e.getErrorEval());
    }

    @Test
    void testEvaluateInSheet() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Sheet1");
        HSSFRow row = sheet.createRow(0);

        row.createCell(0).setCellValue(-7500d);
        row.createCell(1).setCellValue(3000d);
        row.createCell(2).setCellValue(5000d);
        row.createCell(3).setCellValue(1200d);
        row.createCell(4).setCellValue(4000d);

        row.createCell(5).setCellValue(0.05d);
        row.createCell(6).setCellValue(0.08d);

        HSSFCell cell = row.createCell(7);
        cell.setCellFormula("MIRR(A1:E1, F1, G1)");

        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        fe.clearAllCachedResultValues();
        fe.evaluateFormulaCell(cell);
        double res = cell.getNumericCellValue();
        assertEquals(0.18736225093, res, 0.00000001);
    }

    @Test
    void testMirrFromSpreadsheet() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("mirrTest.xls");
        HSSFSheet sheet = wb.getSheet("Mirr");
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        int failureCount = 0;
        int[] resultRows = {9, 19, 29, 45};

        for (int rowNum : resultRows) {
            HSSFRow row = sheet.getRow(rowNum);
            HSSFCell cellA = row.getCell(0);
            try {
                CellValue cv = fe.evaluate(cellA);
                assertFormulaResult(cv, cellA);
            } catch (Throwable e) {
                failureCount++;
            }
        }

        HSSFRow row = sheet.getRow(37);
        HSSFCell cellA = row.getCell(0);
        CellValue cv = fe.evaluate(cellA);
        assertEquals(ErrorEval.DIV_ZERO.getErrorCode(), cv.getErrorValue());

        assertEquals(0, failureCount, "IRR assertions failed");
    }

    private static void assertFormulaResult(CellValue cv, HSSFCell cell) {
        double actualValue = cv.getNumberValue();
        double expectedValue = cell.getNumericCellValue(); // cached formula result calculated by Excel
        assertEquals(CellType.NUMERIC, cv.getCellType(), "Invalid formula result: " + cv);
        assertEquals(expectedValue, actualValue, 1E-8);
    }
}
