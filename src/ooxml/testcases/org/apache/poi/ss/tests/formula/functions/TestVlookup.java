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
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * Test the VLOOKUP function
 */
class TestVlookup {

    @Test
    void testFullColumnAreaRef61841() throws IOException {
        try (Workbook wb = XSSFTestDataSamples.openSampleWorkbook("VLookupFullColumn.xlsx")) {
            FormulaEvaluator feval = wb.getCreationHelper().createFormulaEvaluator();
            feval.evaluateAll();
            assertEquals("Value1", feval.evaluate(wb.getSheetAt(0).getRow(3).getCell(1)).getStringValue(),
                "Wrong lookup value");
            assertEquals(CellType.ERROR, feval.evaluate(wb.getSheetAt(0).getRow(4).getCell(1)).getCellType(),
                "Lookup should return #N/A");
        }
    }

    @Test
    void bug62275_true() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);

            Cell cell = row.createCell(0);
            cell.setCellFormula("vlookup(A2,B1:B5,2,true)");

            CreationHelper createHelper = wb.getCreationHelper();
            FormulaEvaluator eval = createHelper.createFormulaEvaluator();
            CellValue value = eval.evaluate(cell);

            assertFalse(value.getBooleanValue());
        }
    }

    @Test
    void bug62275_false() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);

            Cell cell = row.createCell(0);
            cell.setCellFormula("vlookup(A2,B1:B5,2,false)");

            CreationHelper crateHelper = wb.getCreationHelper();
            FormulaEvaluator eval = crateHelper.createFormulaEvaluator();
            CellValue value = eval.evaluate(cell);

            assertFalse(value.getBooleanValue());
        }
    }

    @Test
    void bug62275_empty_3args() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);

            Cell cell = row.createCell(0);
            cell.setCellFormula("vlookup(A2,B1:B5,2,)");

            CreationHelper crateHelper = wb.getCreationHelper();
            FormulaEvaluator eval = crateHelper.createFormulaEvaluator();
            CellValue value = eval.evaluate(cell);

            assertFalse(value.getBooleanValue());
        }
    }

    @Test
    void bug62275_empty_2args() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);

            Cell cell = row.createCell(0);
            cell.setCellFormula("vlookup(A2,B1:B5,,)");

            CreationHelper crateHelper = wb.getCreationHelper();
            FormulaEvaluator eval = crateHelper.createFormulaEvaluator();
            CellValue value = eval.evaluate(cell);

            assertFalse(value.getBooleanValue());
        }
    }

    @Test
    void bug62275_empty_1arg() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);

            Cell cell = row.createCell(0);
            cell.setCellFormula("vlookup(A2,,,)");

            CreationHelper crateHelper = wb.getCreationHelper();
            FormulaEvaluator eval = crateHelper.createFormulaEvaluator();
            CellValue value = eval.evaluate(cell);

            assertFalse(value.getBooleanValue());
        }
    }
}
