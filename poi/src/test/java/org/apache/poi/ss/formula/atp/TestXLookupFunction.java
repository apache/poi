
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
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testcase for function XLOOKUP()
 */
public class TestXLookupFunction {

    //https://support.microsoft.com/en-us/office/xlookup-function-b7fd680e-6d10-43e6-84f9-88eae8bf5929
    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertString(fe, cell, "XLOOKUP(F2,B2:B11,D2:D11)", "+55");
        }
    }

    @Test
    void testMicrosoftExample2() throws IOException {
        try (HSSFWorkbook wb = initWorkbook2()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            cell.setCellFormula("XLOOKUP(B2,B5:B14,C5:D14)");
            fe.notifyUpdateCell(cell);
            CellValue result = fe.evaluate(cell);
            //TODO add assertions
        }
    }

    @Test
    void testMicrosoftExample3() throws IOException {
        try (HSSFWorkbook wb = initWorkbook2()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertError(fe, cell, "XLOOKUP(999999,B2:B11,D2:D11)", FormulaError.NA);
            assertString(fe, cell, "XLOOKUP(999999,B2:B11,D2:D11,\"not found\")", "not found");
        }
    }

    private HSSFWorkbook initWorkbook1() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, null, "Country", "Abr", "Prefix");
        addRow(sheet, 1, null, "China", "CN", "+86", null, "Brazil");
        addRow(sheet, 2, null, "India", "IN", "+91");
        addRow(sheet, 3, null, "United States", "US", "+1");
        addRow(sheet, 4, null, "Indonesia", "ID", "+62");
        addRow(sheet, 5, null, "Brazil", "BR", "+55");
        addRow(sheet, 6, null, "Pakistan", "PK", "+92");
        addRow(sheet, 7, null, "Nigeria", "NG", "+234");
        addRow(sheet, 8, null, "Bangladesh", "BD", "+880");
        addRow(sheet, 9, null, "Russia", "RU", "+7");
        addRow(sheet, 10, null, "Mexico", "MX", "+52");
        return wb;
    }

    private HSSFWorkbook initWorkbook2() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, null, "Emp Id", "Employee Name", "Department");
        addRow(sheet, 1, null, 8389);
        addRow(sheet, 3, null, "Emp Id", "Employee Name", "Department");
        addRow(sheet, 4, null, 4390, "Ned Lanning", "Marketing");
        addRow(sheet, 5, null, 8604, "Margo Hendrix", "Sales");
        addRow(sheet, 6, null, 8389, "Dianne Pugh", "Finance");
        addRow(sheet, 7, null, 4937, "Earlene McCarty", "Accounting");
        addRow(sheet, 8, null, 8299, "Mia Arnold", "Operation");
        addRow(sheet, 9, null, 2643, "Jorge Fellows", "Executive");
        addRow(sheet, 10, null, 5243, "Rose Winters", "Sales");
        addRow(sheet, 11, null, 9693, "Carmela Hahn", "Finance");
        addRow(sheet, 12, null, 1636, "Delia Cochran", "Accounting");
        addRow(sheet, 13, null, 6703, "Marguerite Cervantes", "Marketing");
        return wb;
    }

}
