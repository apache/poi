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

package org.apache.poi.hssf.usermodel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestHSSFFormulaParser {

    @Test
    void testQuotedSheetNamesReference() throws IOException {
        // quoted sheet names bug fix
        // see TestXSSFFormulaEvaluator equivalent which behaves a little differently
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet1 = wb.createSheet("Sheet1");
            Sheet sheet2 = wb.createSheet("Sheet2");
            Sheet sheet3 = wb.createSheet("Sheet 3");
            Sheet sheet4 = wb.createSheet("Sheet4>");

            Row tempRow = sheet1.createRow(0);
            tempRow.createCell(0).setCellValue(1);
            tempRow.createCell(1).setCellValue(2);

            tempRow = sheet2.createRow(0);
            tempRow.createCell(0).setCellValue(3);
            tempRow.createCell(1).setCellValue(4);

            tempRow = sheet3.createRow(0);
            tempRow.createCell(0).setCellValue(5);
            tempRow.createCell(1).setCellValue(6);

            tempRow = sheet4.createRow(0);
            tempRow.createCell(0).setCellValue(5);
            tempRow.createCell(1).setCellValue(6);

            Cell cell = tempRow.createCell(2);

            // unquoted sheet names
            String formula = "SUM(Sheet1:Sheet2!A1:B1)";
            cell.setCellFormula(formula);
            String cellFormula = cell.getCellFormula();
            assertEquals(formula, cellFormula);

            // quoted sheet names with no space
            cell = tempRow.createCell(3);
            formula = "SUM('Sheet1:Sheet2'!A1:B1)";
            cell.setCellFormula(formula);
            cellFormula = cell.getCellFormula();
            assertEquals("SUM(Sheet1:Sheet2!A1:B1)", cellFormula);

            // quoted sheet names with space
            cell = tempRow.createCell(4);
            formula = "SUM('Sheet1:Sheet 3'!A1:B1)";
            cell.setCellFormula(formula);
            cellFormula = cell.getCellFormula();
            assertEquals(formula, cellFormula);

            // quoted sheet names with special character
            cell = tempRow.createCell(5);
            formula = "SUM('Sheet1:Sheet4>'!A1:B1)";
            cell.setCellFormula(formula);
            cellFormula = cell.getCellFormula();
            assertEquals(formula, cellFormula);

            // quoted sheet names with special character #2
//            cell = tempRow.createCell(6);
//            formula = "SUM('Sheet 3:Sheet4>'!A1:B1)";
//            cell.setCellFormula(formula);
//            cellFormula = cell.getCellFormula();
//            assertEquals(formula, cellFormula);
        }
    }

}
