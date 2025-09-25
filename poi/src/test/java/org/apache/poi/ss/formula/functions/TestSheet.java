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
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TestSheet {

    private static final OperationEvaluationContext ec = new OperationEvaluationContext(null, null, 2, 0, 2, null);

    @Test
    void testSheetFunctionWithRealWorkbook() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            // Add three sheets: Sheet1, Sheet2, Sheet3
            HSSFSheet sheet1 = wb.createSheet("Sheet1");
            HSSFSheet sheet2 = wb.createSheet("Sheet2");
            HSSFSheet sheet3 = wb.createSheet("Sheet3");

            // Add data
            sheet1.createRow(0).createCell(0).setCellValue(123); // A1 in Sheet1
            sheet2.createRow(1).createCell(0).setCellValue(456); // A2 in Sheet2

            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);

            // Define formulas and expected results
            String[] formulas = {
                    "SHEET()",
                    "SHEET(A1)",
                    "SHEET(A1:B5)",
                    "SHEET(Sheet2!A2)",
                    "SHEET(\"Sheet3\")",
                    "SHEET(\"invalid\")"
            };

            Object[] expected = {
                    1.0, // current sheet
                    1.0, // A1 in same sheet
                    1.0, // A1:B5 in same sheet
                    2.0, // Sheet2!A2
                    3.0, // Sheet3
                    FormulaError.NA.getCode() // unknown sheet → #N/A
            };

            // Write formulas to separate cells and evaluate
            HSSFRow formulaRow = sheet1.createRow(1);
            for (int i = 0; i < formulas.length; i++) {
                String formula = formulas[i];
                HSSFCell cell = formulaRow.createCell(i);
                cell.setCellFormula(formula);
                CellType resultType = fe.evaluateFormulaCell(cell);

                if (expected[i] instanceof Double) {
                    assertEquals(CellType.NUMERIC, resultType,
                            "Unexpected cell type for formula: " + formula);
                    assertEquals((Double) expected[i], cell.getNumericCellValue(),
                            "Unexpected numeric result for formula: " + formula);
                } else if (expected[i] instanceof Byte) {
                    assertEquals(CellType.ERROR, resultType,
                            "Unexpected cell type for formula: " + formula);
                    assertEquals((byte) expected[i], cell.getErrorCellValue(),
                            "Unexpected error code for formula: " + formula);
                }
            }
        }
    }
}
