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


package org.apache.poi.ss.tests.formula.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.SheetUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

/**
 * Test the COUNTIFS() function
 */
class TestCountifs {

    private Workbook workbook;

    /**
     * initialize a workbook
     */
    @BeforeEach
    void before() {
        // not sure why we allow this, COUNTIFS() is only available
        // in OOXML, it was introduced with Office 2007
        workbook = new HSSFWorkbook();
    }

    /**
     * Close the workbook if needed
     */
    @AfterEach
    void after() {
        IOUtils.closeQuietly(workbook);
    }

    /**
     * Basic call
     */
    @Test
    void testCallFunction() {
        Sheet sheet = workbook.createSheet("test");
        Row row1 = sheet.createRow(0);
        Cell cellA1 = row1.createCell(0, CellType.FORMULA);
        Cell cellB1 = row1.createCell(1, CellType.NUMERIC);
        Cell cellC1 = row1.createCell(2, CellType.NUMERIC);
        Cell cellD1 = row1.createCell(3, CellType.NUMERIC);
        Cell cellE1 = row1.createCell(4, CellType.NUMERIC);
        cellB1.setCellValue(1);
        cellC1.setCellValue(1);
        cellD1.setCellValue(2);
        cellE1.setCellValue(4);

        cellA1.setCellFormula("COUNTIFS(B1:C1,1, D1:E1,2)");
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        CellValue evaluate = evaluator.evaluate(cellA1);
        assertEquals(1.0d, evaluate.getNumberValue(), 0.000000000000001);
    }

    /**
     * Test argument count check
     */
    @Test
    void testCallFunction_invalidArgs() {
        Sheet sheet = workbook.createSheet("test");
        Row row1 = sheet.createRow(0);
        Cell cellA1 = row1.createCell(0, CellType.FORMULA);
        cellA1.setCellFormula("COUNTIFS()");
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        CellValue evaluate = evaluator.evaluate(cellA1);
        assertEquals(15, evaluate.getErrorValue());
        cellA1.setCellFormula("COUNTIFS(A1:C1)");
        evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        evaluate = evaluator.evaluate(cellA1);
        assertEquals(15, evaluate.getErrorValue());
        cellA1.setCellFormula("COUNTIFS(A1:C1,2,2)");
        evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        evaluate = evaluator.evaluate(cellA1);
        assertEquals(15, evaluate.getErrorValue());
    }

    /**
     * the bug returned the wrong count, this verifies the fix
     */
    @Test
    void testBug56822() {
        workbook = XSSFTestDataSamples.openSampleWorkbook("56822-Countifs.xlsx");
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        Cell cell = SheetUtil.getCell(workbook.getSheetAt(0), 0, 3);
        assertNotNull(cell, "Test workbook missing cell D1");
        CellValue evaluate = evaluator.evaluate(cell);
        assertEquals(2.0d, evaluate.getNumberValue(), 0.00000000000001);
    }

    /**
     * Bug 70005 - SUM(COUNTIFS) with multiple values in criteria gives wrong result,
     * and uses wrong area check causing ERROR when formula cell is not in the data range rows.
     * Expected: A3=3.0, A4=4.0, D3=3.0, D4=4.0 (verified in Excel and LibreOffice Calc).
     */
    @Test
    void testBug70005() {
        workbook = XSSFTestDataSamples.openSampleWorkbook("70005-countifs.xlsx");
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        Sheet sheet = workbook.getSheetAt(0);

        // A3 and A4: formula cell is within the data area rows - wrong numeric result in buggy code
        Cell a3 = SheetUtil.getCell(sheet, 2, 0);
        assertNotNull(a3, "Test workbook missing cell A3");
        CellValue a3Value = evaluator.evaluate(a3);
        assertEquals(CellType.NUMERIC, a3Value.getCellType(), "A3 should be numeric, not an error");
        assertEquals(3.0, a3Value.getNumberValue(), 0.00000000000001, "A3: SUM(COUNTIFS) with multiple criteria should equal 3");

        Cell a4 = SheetUtil.getCell(sheet, 3, 0);
        assertNotNull(a4, "Test workbook missing cell A4");
        CellValue a4Value = evaluator.evaluate(a4);
        assertEquals(CellType.NUMERIC, a4Value.getCellType(), "A4 should be numeric, not an error");
        assertEquals(4.0, a4Value.getNumberValue(), 0.00000000000001, "A4: SUM(COUNTIFS) with multiple criteria should equal 4");

        // D3 and D4: formula cell is outside the data area rows - buggy code returns ERROR here
        Cell d3 = SheetUtil.getCell(sheet, 2, 3);
        assertNotNull(d3, "Test workbook missing cell D3");
        CellValue d3Value = evaluator.evaluate(d3);
        assertEquals(CellType.NUMERIC, d3Value.getCellType(), "D3 should be numeric, not an error");
        assertEquals(3.0, d3Value.getNumberValue(), 0.00000000000001, "D3: SUM(COUNTIFS) with formula cell outside data rows should equal 3");

        Cell d4 = SheetUtil.getCell(sheet, 3, 3);
        assertNotNull(d4, "Test workbook missing cell D4");
        CellValue d4Value = evaluator.evaluate(d4);
        assertEquals(CellType.NUMERIC, d4Value.getCellType(), "D4 should be numeric, not an error");
        assertEquals(4.0, d4Value.getNumberValue(), 0.00000000000001, "D4: SUM(COUNTIFS) with formula cell outside data rows should equal 4");
    }
}
