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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Tests for {@link org.apache.poi.ss.formula.functions.Mirr}
 *
 * @author Carlos Delgado (carlos dot del dot est at gmail dot com)
 * @author Cedric Walter (cedric dot walter at gmail dot com)
 * @see {@link org.apache.poi.ss.formula.functions.TestIrr}
 */
public final class TestMirr extends TestCase {

    public void testMirr() {
        Mirr mirr = new Mirr();
        double mirrValue;

        double financeRate = 0.12;
        double reinvestRate = 0.1;
        double[] values = {-120000d, 39000d, 30000d, 21000d, 37000d, 46000d, reinvestRate, financeRate};
        try {
            mirrValue = mirr.evaluate(values);
        } catch (EvaluationException e) {
            throw new AssertionFailedError("MIRR should not failed with these parameters" + e);
        }
        assertEquals("mirr", 0.126094130366, mirrValue, 0.0000000001);

        reinvestRate = 0.05;
        financeRate = 0.08;
        values = new double[]{-7500d, 3000d, 5000d, 1200d, 4000d, reinvestRate, financeRate};
        try {
            mirrValue = mirr.evaluate(values);
        } catch (EvaluationException e) {
            throw new AssertionFailedError("MIRR should not failed with these parameters" + e);
        }
        assertEquals("mirr", 0.18736225093, mirrValue, 0.0000000001);

        reinvestRate = 0.065;
        financeRate = 0.1;
        values = new double[]{-10000, 3400d, 6500d, 1000d, reinvestRate, financeRate};
        try {
            mirrValue = mirr.evaluate(values);
        } catch (EvaluationException e) {
            throw new AssertionFailedError("MIRR should not failed with these parameters" + e);
        }
        assertEquals("mirr", 0.07039493966, mirrValue, 0.0000000001);

        reinvestRate = 0.07;
        financeRate = 0.01;
        values = new double[]{-10000d, -3400d, -6500d, -1000d, reinvestRate, financeRate};
        try {
            mirrValue = mirr.evaluate(values);
        } catch (EvaluationException e) {
            throw new AssertionFailedError("MIRR should not failed with these parameters" + e);
        }
        assertEquals("mirr", -1, mirrValue, 0.0);

    }

    public void testMirrErrors_expectDIV0() {
        Mirr mirr = new Mirr();

        double reinvestRate = 0.05;
        double financeRate = 0.08;
        double[] incomes = {120000d, 39000d, 30000d, 21000d, 37000d, 46000d, reinvestRate, financeRate};
        try {
            mirr.evaluate(incomes);
        } catch (EvaluationException e) {
            assertEquals(ErrorEval.DIV_ZERO, e.getErrorEval());
            return;
        }
        throw new AssertionFailedError("MIRR should failed with all these positives values");
    }


    public void testEvaluateInSheet() {
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

    public void testMirrFromSpreadsheet() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("mirrTest.xls");
        HSSFSheet sheet = wb.getSheet("Mirr");
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        StringBuilder failures = new StringBuilder();
        int failureCount = 0;
        int[] resultRows = {9, 19, 29, 45};

        for (int rowNum : resultRows) {
            HSSFRow row = sheet.getRow(rowNum);
            HSSFCell cellA = row.getCell(0);
            try {
                CellValue cv = fe.evaluate(cellA);
                assertFormulaResult(cv, cellA);
            } catch (Throwable e) {
                if (failures.length() > 0) failures.append('\n');
                failures.append("Row[").append(cellA.getRowIndex() + 1).append("]: ").append(cellA.getCellFormula()).append(" ");
                failures.append(e.getMessage());
                failureCount++;
            }
        }

        HSSFRow row = sheet.getRow(37);
        HSSFCell cellA = row.getCell(0);
        CellValue cv = fe.evaluate(cellA);
        assertEquals(ErrorEval.DIV_ZERO.getErrorCode(), cv.getErrorValue());

        if (failures.length() > 0) {
            throw new AssertionFailedError(failureCount + " IRR assertions failed:\n" + failures);
        }

    }

    private static void assertFormulaResult(CellValue cv, HSSFCell cell) {
        double actualValue = cv.getNumberValue();
        double expectedValue = cell.getNumericCellValue(); // cached formula result calculated by Excel
        assertEquals("Invalid formula result: " + cv, CellType.NUMERIC, cv.getCellType());
        assertEquals(expectedValue, actualValue, 1E-8);
    }
}
