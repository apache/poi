
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
package org.apache.poi.ss.formula.atp;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.addRow;
import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testcase for function PERCENTRANK.INC()
 */
public class TestPercentRankIncFunction {

    //https://support.microsoft.com/en-us/office/percentrank-inc-function-149592c9-00c0-49ba-86c1-c1f45b80463a
    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertDouble(fe, cell, "PERCENTRANK.INC(A2:A11,2)", 0.333);
            assertDouble(fe, cell, "PERCENTRANK.INC(A2:A11,4)", 0.555);
            assertDouble(fe, cell, "PERCENTRANK.INC(A2:A11,8)", 0.666);
            assertDouble(fe, cell, "PERCENTRANK.INC(A2:A11,8,2)", 0.66);
            assertDouble(fe, cell, "PERCENTRANK.INC(A2:A11,8,4)", 0.6666);
            assertDouble(fe, cell, "PERCENTRANK.INC(A2:A11,5)", 0.583);
            assertDouble(fe, cell, "PERCENTRANK.INC(A2:A11,5,5)", 0.58333);
            assertDouble(fe, cell, "PERCENTRANK.INC(A2:A11,1)", 0);
            assertDouble(fe, cell, "PERCENTRANK.INC(A2:A11,13)", 1);
        }
    }

    @Test
    void testErrorCases() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            confirmErrorResult(fe, cell, "PERCENTRANK.INC(A2:A11,0)", FormulaError.NA);
            confirmErrorResult(fe, cell, "PERCENTRANK.INC(A2:A11,100)", FormulaError.NA);
            confirmErrorResult(fe, cell, "PERCENTRANK.INC(B2:B11,100)", FormulaError.NUM);
            confirmErrorResult(fe, cell, "PERCENTRANK.INC(A2:A11,8,0)", FormulaError.NUM);
        }
    }

    private HSSFWorkbook initWorkbook1() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, "Data");
        addRow(sheet, 1, 13);
        addRow(sheet, 2, 12);
        addRow(sheet, 3, 11);
        addRow(sheet, 4, 8);
        addRow(sheet, 5, 4);
        addRow(sheet, 6, 3);
        addRow(sheet, 7, 2);
        addRow(sheet, 8, 1);
        addRow(sheet, 9, 1);
        addRow(sheet, 10, 1);
        return wb;
    }

    private static void confirmErrorResult(HSSFFormulaEvaluator fe, HSSFCell cell, String formulaText, FormulaError expectedResult) {
        cell.setCellFormula(formulaText);
        fe.notifyUpdateCell(cell);
        CellValue result = fe.evaluate(cell);
        assertEquals(expectedResult.getCode(), result.getErrorValue());
    }
}
