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

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for {@link Irr}
 */
final class TestIrr {

    @Test
    void testIrr() {
        // http://en.wikipedia.org/wiki/Internal_rate_of_return#Example
        double[] incomes = {-4000d, 1200d, 1410d, 1875d, 1050d};
        double irr = Irr.irr(incomes);
        double irrRounded = Math.round(irr * 1000d) / 1000d;
        assertEquals(0.143d, irrRounded, 0);

        // http://www.techonthenet.com/excel/formulas/irr.php
        incomes = new double[]{-7500d, 3000d, 5000d, 1200d, 4000d};
        irr = Irr.irr(incomes);
        irrRounded = Math.round(irr * 100d) / 100d;
        assertEquals(0.28d, irrRounded, 0);

        incomes = new double[]{-10000d, 3400d, 6500d, 1000d};
        irr = Irr.irr(incomes);
        irrRounded = Math.round(irr * 100d) / 100d;
        assertEquals(0.05, irrRounded, 0);

        incomes = new double[]{100d, -10d, -110d};
        irr = Irr.irr(incomes);
        irrRounded = Math.round(irr * 100d) / 100d;
        assertEquals(0.1, irrRounded, 0);

        incomes = new double[]{-70000d, 12000, 15000};
        irr = Irr.irr(incomes, -0.1);
        irrRounded = Math.round(irr * 100d) / 100d;
        assertEquals(-0.44, irrRounded, 0);
    }

    @Test
    void testEvaluateInSheet() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Sheet1");
        HSSFRow row = sheet.createRow(0);

        row.createCell(0).setCellValue(-4000d);
        row.createCell(1).setCellValue(1200d);
        row.createCell(2).setCellValue(1410d);
        row.createCell(3).setCellValue(1875d);
        row.createCell(4).setCellValue(1050d);

        HSSFCell cell = row.createCell(5);
        cell.setCellFormula("IRR(A1:E1)");

        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        fe.clearAllCachedResultValues();
        fe.evaluateFormulaCell(cell);
        double res = cell.getNumericCellValue();
        assertEquals(0.143d, Math.round(res * 1000d) / 1000d, 0);
    }

    @Test
    void testIrrFromSpreadsheet(){
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("IrrNpvTestCaseData.xls");
        HSSFSheet sheet = wb.getSheet("IRR-NPV");
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        StringBuilder failures = new StringBuilder();
        // TODO YK: Formulas in rows 16 and 17 operate with ArrayPtg which isn't yet supported
        // FormulaEvaluator as of r1041407 throws "Unexpected ptg class (org.apache.poi.ss.formula.ptg.ArrayPtg)"
        for(int rownum = 9; rownum <= 15; rownum++){
            HSSFRow row = sheet.getRow(rownum);
            HSSFCell cellA = row.getCell(0);
            try {
                CellValue cv = fe.evaluate(cellA);
                assertFormulaResult(cv, cellA);
            } catch (Throwable e){
                if(failures.length() > 0) failures.append('\n');
                failures.append("Row[").append(cellA.getRowIndex() + 1).append("]: ").append(cellA.getCellFormula()).append(" ");
                failures.append(e.getMessage());
            }

            HSSFCell cellC = row.getCell(2); //IRR-NPV relationship: NPV(IRR(values), values) = 0
            try {
                CellValue cv = fe.evaluate(cellC);
                assertEquals(0, cv.getNumberValue(), 0.0001);  // should agree within 0.01%
            } catch (Throwable e){
                if(failures.length() > 0) failures.append('\n');
                failures.append("Row[").append(cellC.getRowIndex() + 1).append("]: ").append(cellC.getCellFormula()).append(" ");
                failures.append(e.getMessage());
            }
        }
        assertEquals(0, failures.length(), "IRR assertions failed");
    }

    @Test
    void testMicrosoftExample() throws IOException {
        https://support.microsoft.com/en-us/office/irr-function-64925eaa-9988-495b-b290-3ad0c163c1bc
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            addRow(sheet, 0, "Data", "Description");
            addRow(sheet, 1, -70000, "Initial cost of a business");
            addRow(sheet, 2, 12000, "Net income for the first year");
            addRow(sheet, 3, 15000, "Net income for the second year");
            addRow(sheet, 4, 18000, "Net income for the third year");
            addRow(sheet, 5, 21000, "Net income for the fourth year");
            addRow(sheet, 6, 26000, "Net income for the fifth year");
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            final double tolerance = 1E-4;
            assertDouble(fe, cell, "IRR(A2:A6)", -0.02124484827341093, tolerance);
            assertDouble(fe, cell, "IRR(A2:A7)", 0.08663094803653162, tolerance);
            assertDouble(fe, cell, "IRR(A2:A4,-0.1)", -0.44350694133474067, tolerance);
        }
    }

    @Test
    void bug64137() {
        double[] incomes = {-30000.0, -49970.7425, 29.2575, 146.2875, 380.34749999999997, 581.5, 581.5,
                731.4374999999999, 731.4374999999999, 731.4374999999999, 877.725, 877.725, 877.725, 1024.0125,
                1024.0125, 1024.0125, 1170.3, 1170.3, 1170.3, 1170.3, 1316.5874999999999, 1316.5874999999999,
                1316.5874999999999, 1316.5874999999999, 1462.8749999999998, 1462.8749999999998, 1462.8749999999998,
                1462.8749999999998, 1462.8749999999998, 1462.8749999999998, 1462.8749999999998, 1462.8749999999998,
                1462.8749999999998, 1462.8749999999998, 1462.8749999999998, 1462.8749999999998, 1462.8749999999998,
                1462.8749999999998, 1462.8749999999998, 1462.8749999999998, 1462.8749999999998, 1462.8749999999998,
                1462.8749999999998, 1462.8749999999998, 1462.8749999999998, 10000.0};
        double result = Irr.irr(incomes);
        assertEquals(-0.009463562705856032, result, 1E-4); // should agree within 0.01%
    }

    private static void assertFormulaResult(CellValue cv, HSSFCell cell){
        double actualValue = cv.getNumberValue();
        double expectedValue = cell.getNumericCellValue(); // cached formula result calculated by Excel
        assertEquals(CellType.NUMERIC, cv.getCellType(), "Invalid formula result: " + cv);
        assertEquals(expectedValue, actualValue, 1E-4); // should agree within 0.01%
    }
}
