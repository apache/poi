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

import junit.framework.TestCase;
import junit.framework.AssertionFailedError;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.CellValue;

/**
 * Tests for {@link Npv}
 *
 * @author Marcel May
 * @see <a href="http://office.microsoft.com/en-us/excel-help/npv-HP005209199.aspx">Excel Help</a>
 */
public final class TestNpv extends TestCase {

    public void testEvaluateInSheetExample2() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Sheet1");
        HSSFRow row = sheet.createRow(0);

        sheet.createRow(1).createCell(0).setCellValue(0.08d);
        sheet.createRow(2).createCell(0).setCellValue(-40000d);
        sheet.createRow(3).createCell(0).setCellValue(8000d);
        sheet.createRow(4).createCell(0).setCellValue(9200d);
        sheet.createRow(5).createCell(0).setCellValue(10000d);
        sheet.createRow(6).createCell(0).setCellValue(12000d);
        sheet.createRow(7).createCell(0).setCellValue(14500d);

        HSSFCell cell = row.createCell(8);
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);

        // Enumeration
        cell.setCellFormula("NPV(A2, A4,A5,A6,A7,A8)+A3");
        fe.clearAllCachedResultValues();
        fe.evaluateFormulaCell(cell);
        double res = cell.getNumericCellValue();
        assertEquals(1922.06d, Math.round(res * 100d) / 100d);

        // Range
        cell.setCellFormula("NPV(A2, A4:A8)+A3");

        fe.clearAllCachedResultValues();
        fe.evaluateFormulaCell(cell);
        res = cell.getNumericCellValue();
        assertEquals(1922.06d, Math.round(res * 100d) / 100d);
    }

    /**
     * evaluate formulas with NPV and compare the result with
     * the cached formula result pre-calculated by Excel
     */
    public void testNpvFromSpreadsheet(){
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("IrrNpvTestCaseData.xls");
        HSSFSheet sheet = wb.getSheet("IRR-NPV");
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        StringBuffer failures = new StringBuffer();
        int failureCount = 0;
        // TODO YK: Formulas in rows 16 and 17 operate with ArrayPtg which isn't yet supported
        // FormulaEvaluator as of r1041407 throws "Unexpected ptg class (org.apache.poi.ss.formula.ptg.ArrayPtg)"
        for(int rownum = 9; rownum <= 15; rownum++){
            HSSFRow row = sheet.getRow(rownum);
            HSSFCell cellB = row.getCell(1);
            try {
                CellValue cv = fe.evaluate(cellB);
                assertFormulaResult(cv, cellB);
            } catch (Throwable e){
                if(failures.length() > 0) failures.append('\n');
                failures.append("Row[" + (cellB.getRowIndex() + 1)+ "]: " + cellB.getCellFormula() + " ");
                failures.append(e.getMessage());
                failureCount++;
            }
        }

        if(failures.length() > 0) {
            throw new AssertionFailedError(failureCount + " IRR evaluations failed:\n" + failures);
        }
    }

    private static void assertFormulaResult(CellValue cv, HSSFCell cell){
        double actualValue = cv.getNumberValue();
        double expectedValue = cell.getNumericCellValue(); // cached formula result calculated by Excel
        assertEquals(expectedValue, actualValue, 1E-4); // should agree within 0.01%
    }
}
