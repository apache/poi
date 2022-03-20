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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Test;

class TestXSSFEvaluationWorkbook {

    @Test
    void testRefToBlankCellInArrayFormula() {
        Workbook wb = new XSSFWorkbook();

        FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
        verifySheet(wb, formulaEvaluator);

        verifySheet(wb, formulaEvaluator);

        wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
    }

    private void verifySheet(Workbook wb, FormulaEvaluator formulaEvaluator) {
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cellA1 = row.createCell(0);
        Cell cellB1 = row.createCell(1);
        Cell cellC1 = row.createCell(2);
        Row row2 = sheet.createRow(1);
        Cell cellA2 = row2.createCell(0);
        Cell cellB2 = row2.createCell(1);
        Cell cellC2 = row2.createCell(2);
        Row row3 = sheet.createRow(2);
        Cell cellA3 = row3.createCell(0);
        Cell cellB3 = row3.createCell(1);
        Cell cellC3 = row3.createCell(2);

        cellA1.setCellValue("1");
        // cell B1 intentionally left blank
        cellC1.setCellValue("3");

        cellA2.setCellFormula("A1");
        cellB2.setCellFormula("B1");
        cellC2.setCellFormula("C1");

        sheet.setArrayFormula("A1:C1", CellRangeAddress.valueOf("A3:C3"));

        formulaEvaluator.evaluateAll();

        assertEquals("1", cellA2.getStringCellValue());
        assertEquals(0,cellB2.getNumericCellValue(), 0.00001);
        assertEquals("3",cellC2.getStringCellValue());

        assertEquals("1", cellA3.getStringCellValue());
        assertEquals(0,cellB3.getNumericCellValue(), 0.00001);
        assertEquals("3",cellC3.getStringCellValue());
    }

}