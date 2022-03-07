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

import org.apache.poi.ss.usermodel.CellType;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestFormulaEval {
    @Test
    void testCircularRef() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell = row.createCell(0);
            cell.setCellFormula("A1");
            XSSFFormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
            // the following assert should probably be NUMERIC not ERROR (from testing in Excel itself)
            assertEquals(CellType.ERROR, formulaEvaluator.evaluateFormulaCell(cell));

            cell.setCellFormula(null);
            formulaEvaluator.notifyUpdateCell(cell);
            //the following assert should probably be BLANK not ERROR
            assertEquals(CellType.ERROR, cell.getCellType());
        }
    }

    @Test
    void testCircularRef2() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell0 = row.createCell(0);
            XSSFCell cell1 = row.createCell(1);
            cell0.setCellFormula("B1");
            cell1.setCellFormula("A1");
            XSSFFormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
            formulaEvaluator.evaluateAll();

            cell0.setCellFormula(null);
            cell1.setCellFormula(null);
            formulaEvaluator.notifyUpdateCell(cell0);
            formulaEvaluator.notifyUpdateCell(cell1);
            //the following asserts should probably be BLANK not ERROR
            assertEquals(CellType.ERROR, cell0.getCellType());
            assertEquals(CellType.ERROR, cell1.getCellType());
        }
    }
}
