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

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test case for CONCAT() - based on https://support.microsoft.com/en-us/office/concat-function-9b1a9a3f-94ff-41af-9736-694cbd6b4ca2?ui=en-us&rs=en-us&ad=us
 */
final class TestConcat {

    @Test
    void testConcatWithStrings() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(0);
            confirmResult(fe, cell, "CONCAT(\"The\",\" \",\"sun\",\" \",\"will\",\" \",\"come\",\" \",\"up\",\" \",\"tomorrow.\")",
                    "The sun will come up tomorrow.");
        }
    }

    @Test
    void testConcatWithColumns() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(0);
            confirmResult(fe, cell, "CONCAT(B:B, C:C)", "A’sa1a2a4a5a6a7B’sb1b2b4b5b6b7");
        }
    }

    @Test
    void testConcatWithCellRefs() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(0);
            confirmResult(fe, cell, "CONCAT(B2:C8)", "a1b1a2b2a4b4a5b5a6b6a7b7");
        }
    }

    private HSSFWorkbook initWorkbook1() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, null, "A’s", "B’s");
        for (int i = 1; i <= 7; i++) {
            if (i != 3) {
                addRow(sheet, i, null, "a" + i, "b" + i);
            }
        }
        return wb;
    }

    private void addRow(HSSFSheet sheet, int rownum, String... values) {
        HSSFRow row = sheet.createRow(rownum);
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i]);
        }
    }

    private static void confirmResult(HSSFFormulaEvaluator fe, HSSFCell cell, String formulaText, String expectedResult) {
        cell.setCellFormula(formulaText);
        fe.notifyUpdateCell(cell);
        CellValue result = fe.evaluate(cell);
        assertEquals(result.getCellType(), CellType.STRING);
        assertEquals(expectedResult, result.getStringValue());
    }
}
