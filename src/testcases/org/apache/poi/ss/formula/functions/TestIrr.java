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
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Tests for {@link Irr}
 *
 * @author Marcel May
 */
public final class TestIrr extends TestCase {

    public void testIrr() {
        // http://en.wikipedia.org/wiki/Internal_rate_of_return#Example
        double[] incomes = {-4000d, 1200d, 1410d, 1875d, 1050d};
        double irr = Irr.irr(incomes);
        double irrRounded = Math.round(irr * 1000d) / 1000d;
        assertEquals("irr", 0.143d, irrRounded);

        // http://www.techonthenet.com/excel/formulas/irr.php
        incomes = new double[]{-7500d, 3000d, 5000d, 1200d, 4000d};
        irr = Irr.irr(incomes);
        irrRounded = Math.round(irr * 100d) / 100d;
        assertEquals("irr", 0.28d, irrRounded);

        incomes = new double[]{-10000d, 3400d, 6500d, 1000d};
        irr = Irr.irr(incomes);
        irrRounded = Math.round(irr * 100d) / 100d;
        assertEquals("irr", 0.05, irrRounded);

        incomes = new double[]{100d, -10d, -110d};
        irr = Irr.irr(incomes);
        irrRounded = Math.round(irr * 100d) / 100d;
        assertEquals("irr", 0.1, irrRounded);

        incomes = new double[]{-70000d, 12000, 15000};
        irr = Irr.irr(incomes, -0.1);
        irrRounded = Math.round(irr * 100d) / 100d;
        assertEquals("irr", -0.44, irrRounded);
    }

    public void testEvaluateInSheet() {
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
        assertEquals(0.143d, Math.round(res * 1000d) / 1000d);
    }

    public void testIrrFromSpreadsheet(){
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("IrrNpvTestCaseData.xls");
        HSSFSheet sheet = wb.getSheet("IRR-NPV");
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        StringBuffer failures = new StringBuffer();
        int failureCount = 0;
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
                failures.append("Row[" + (cellA.getRowIndex() + 1)+ "]: " + cellA.getCellFormula() + " ");
                failures.append(e.getMessage());
                failureCount++;
            }

            HSSFCell cellC = row.getCell(2); //IRR-NPV relationship: NPV(IRR(values), values) = 0
            try {
                CellValue cv = fe.evaluate(cellC);
                assertEquals(0, cv.getNumberValue(), 0.0001);  // should agree within 0.01%
            } catch (Throwable e){
                if(failures.length() > 0) failures.append('\n');
                failures.append("Row[" + (cellC.getRowIndex() + 1)+ "]: " + cellC.getCellFormula() + " ");
                failures.append(e.getMessage());
                failureCount++;
            }
        }

        if(failures.length() > 0) {
            throw new AssertionFailedError(failureCount + " IRR assertions failed:\n" + failures);
        }

    }

    private static void assertFormulaResult(CellValue cv, HSSFCell cell){
        double actualValue = cv.getNumberValue();
        double expectedValue = cell.getNumericCellValue(); // cached formula result calculated by Excel
        assertEquals("Invalid formula result: " + cv, CellType.NUMERIC, cv.getCellType());
        assertEquals(expectedValue, actualValue, 1E-4); // should agree within 0.01%
    }
}
