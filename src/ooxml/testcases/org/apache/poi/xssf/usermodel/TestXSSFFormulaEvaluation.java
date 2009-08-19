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

package org.apache.poi.xssf.usermodel;

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.XSSFTestDataSamples;

public final class TestXSSFFormulaEvaluation extends TestCase {
	@Override
	protected void setUp() {
        // Use system out logger
        System.setProperty(
                "org.apache.poi.util.POILogger",
                "org.apache.poi.util.SystemOutLogger"
        );
    }

    public void testSimpleArithmatic() {
        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet s = wb.createSheet();
        Row r = s.createRow(0);

        Cell c1 = r.createCell(0);
        c1.setCellFormula("1+5");
        assertEquals(0.0, c1.getNumericCellValue(), 0.0);

        Cell c2 = r.createCell(1);
        c2.setCellFormula("10/2");
        assertEquals(0.0, c2.getNumericCellValue(), 0.0);

        FormulaEvaluator fe = new XSSFFormulaEvaluator(wb);

        fe.evaluateFormulaCell(c1);
        fe.evaluateFormulaCell(c2);

        assertEquals(6.0, c1.getNumericCellValue(), 0.0001);
        assertEquals(5.0, c2.getNumericCellValue(), 0.0001);
    }

    public void testSumCount() {
        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet s = wb.createSheet();
        Row r = s.createRow(0);
        r.createCell(0).setCellValue(2.5);
        r.createCell(1).setCellValue(1.1);
        r.createCell(2).setCellValue(3.2);
        r.createCell(4).setCellValue(10.7);

        r = s.createRow(1);

        Cell c1 = r.createCell(0);
        c1.setCellFormula("SUM(A1:B1)");
        assertEquals(0.0, c1.getNumericCellValue(), 0.0);

        Cell c2 = r.createCell(1);
        c2.setCellFormula("SUM(A1:E1)");
        assertEquals(0.0, c2.getNumericCellValue(), 0.0);

        Cell c3 = r.createCell(2);
        c3.setCellFormula("COUNT(A1:A1)");
        assertEquals(0.0, c3.getNumericCellValue(), 0.0);

        Cell c4 = r.createCell(3);
        c4.setCellFormula("COUNTA(A1:E1)");
        assertEquals(0.0, c4.getNumericCellValue(), 0.0);


        // Evaluate and test
        FormulaEvaluator fe = new XSSFFormulaEvaluator(wb);

        fe.evaluateFormulaCell(c1);
        fe.evaluateFormulaCell(c2);
        fe.evaluateFormulaCell(c3);
        fe.evaluateFormulaCell(c4);

        assertEquals(3.6, c1.getNumericCellValue(), 0.0001);
        assertEquals(17.5, c2.getNumericCellValue(), 0.0001);
        assertEquals(1, c3.getNumericCellValue(), 0.0001);
        assertEquals(4, c4.getNumericCellValue(), 0.0001);
    }

    public void testSharedFormulas(){
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("shared_formulas.xlsx");

        XSSFSheet sheet = wb.getSheetAt(0);

        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        XSSFCell cell;

        cell = sheet.getRow(1).getCell(0);
        assertEquals("B2", cell.getCellFormula());
        assertEquals("ProductionOrderConfirmation", evaluator.evaluate(cell).getStringValue());

        cell = sheet.getRow(2).getCell(0);
        assertEquals("B3", cell.getCellFormula());
        assertEquals("RequiredAcceptanceDate", evaluator.evaluate(cell).getStringValue());

        cell = sheet.getRow(3).getCell(0);
        assertEquals("B4", cell.getCellFormula());
        assertEquals("Header", evaluator.evaluate(cell).getStringValue());

        cell = sheet.getRow(4).getCell(0);
        assertEquals("B5", cell.getCellFormula());
        assertEquals("UniqueDocumentNumberID", evaluator.evaluate(cell).getStringValue());
    }

    /**
     * Test creation / evaluation of formulas with sheet-level names
     */
    public void testSheetLevelFormulas(){
        XSSFWorkbook wb = new XSSFWorkbook();

        XSSFRow row;
        XSSFSheet sh1 = wb.createSheet("Sheet1");
        XSSFName nm1 = wb.createName();
        nm1.setNameName("sales_1");
        nm1.setSheetIndex(0);
        nm1.setRefersToFormula("Sheet1!$A$1");
        row = sh1.createRow(0);
        row.createCell(0).setCellValue(3);
        row.createCell(1).setCellFormula("sales_1");
        row.createCell(2).setCellFormula("sales_1*2");

        XSSFSheet sh2 = wb.createSheet("Sheet2");
        XSSFName nm2 = wb.createName();
        nm2.setNameName("sales_1");
        nm2.setSheetIndex(1);
        nm2.setRefersToFormula("Sheet2!$A$1");

        row = sh2.createRow(0);
        row.createCell(0).setCellValue(5);
        row.createCell(1).setCellFormula("sales_1");
        row.createCell(2).setCellFormula("sales_1*3");

        XSSFFormulaEvaluator evaluator = new XSSFFormulaEvaluator(wb);
        assertEquals(3.0, evaluator.evaluate(sh1.getRow(0).getCell(1)).getNumberValue(), 0.0);
        assertEquals(6.0, evaluator.evaluate(sh1.getRow(0).getCell(2)).getNumberValue(), 0.0);

        assertEquals(5.0, evaluator.evaluate(sh2.getRow(0).getCell(1)).getNumberValue(), 0.0);
        assertEquals(15.0, evaluator.evaluate(sh2.getRow(0).getCell(2)).getNumberValue(), 0.0);
    }
}
