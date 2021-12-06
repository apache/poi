
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

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Locale;

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
            assertString(fe, cell, "XLOOKUP(\"Brazil\",B2:B11,D2:D11)", "+55");
            assertString(fe, cell, "XLOOKUP(\"brazil\",B2:B11,D2:D11)", "+55");
            //wildcard lookups
            assertString(fe, cell, "XLOOKUP(\"brazil\",B2:B11,D2:D11,,2)", "+55");
            assertString(fe, cell, "XLOOKUP(\"b*l\",B2:B11,D2:D11,,2)", "+55");
            assertString(fe, cell, "XLOOKUP(\"i???a\",B2:B11,D2:D11,,2)", "+91");
        }
    }

    @Test
    void testMicrosoftExample2() throws IOException {
        String formulaText = "XLOOKUP(B2,B5:B14,C5:D14)";
        try (HSSFWorkbook wb = initWorkbook2(8389)) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row1 = sheet.getRow(1);
            String col1 = CellReference.convertNumToColString(2);
            String col2 = CellReference.convertNumToColString(3);
            String cellRef = String.format(Locale.ENGLISH, "%s2:%s2", col1, col2);
            sheet.setArrayFormula(formulaText, CellRangeAddress.valueOf(cellRef));
            fe.evaluateAll();
            assertEquals("Dianne Pugh", row1.getCell(2).getStringCellValue());
            assertEquals("Finance", row1.getCell(3).getStringCellValue());
        }
    }

    @Test
    void testMicrosoftExample3() throws IOException {
        try (HSSFWorkbook wb = initWorkbook2(999999)) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertError(fe, cell, "XLOOKUP(B2,B5:B14,C5:D14)", FormulaError.NA);

            assertString(fe, cell, "XLOOKUP(B2,B5:B14,C5:C14,\"not found\")", "not found");

            String formulaText = "XLOOKUP(B2,B5:B14,C5:D14,\"not found\")";
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row1 = sheet.getRow(1);
            String col1 = CellReference.convertNumToColString(2);
            String col2 = CellReference.convertNumToColString(3);
            String cellRef = String.format(Locale.ENGLISH, "%s2:%s2", col1, col2);
            sheet.setArrayFormula(formulaText, CellRangeAddress.valueOf(cellRef));
            fe.evaluateAll();
            assertEquals("not found", row1.getCell(2).getStringCellValue());
            assertEquals("", row1.getCell(3).getStringCellValue());
        }
    }

    @Test
    void testMicrosoftExample4() throws IOException {
        try (HSSFWorkbook wb = initWorkbook4()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(1).createCell(6);
            assertDouble(fe, cell, "XLOOKUP(E2,C2:C7,B2:B7,0,1,1)", 0.24);
            assertDouble(fe, cell, "XLOOKUP(E2,C2:C7,B2:B7,0,1,-1)", 0.24);
        }
    }

    @Test
    void testMicrosoftExample5() throws IOException {
        try (HSSFWorkbook wb = initWorkbook5()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(2).createCell(3);
            assertDouble(fe, cell, "XLOOKUP(D2,$B6:$B17,$C6:$C17)", 25000);
            assertDouble(fe, cell, "XLOOKUP(D2,$B6:$B17,XLOOKUP($C3,$C5:$G5,$C6:$G17))", 25000);
        }
    }

    @Test
    void testBinarySearch() throws IOException {
        try (HSSFWorkbook wb = initWorkbook4()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(1).createCell(6);
            assertDouble(fe, cell, "XLOOKUP(E2,C2:C7,B2:B7,0,1,2)", 0.24);
            assertDouble(fe, cell, "XLOOKUP(39475,C2:C7,B2:B7,0,0,2)", 0.22);
            assertDouble(fe, cell, "XLOOKUP(39474,C2:C7,B2:B7,0,0,2)", 0);
        }
    }

    @Test
    void testReverseBinarySearch() throws IOException {
        try (HSSFWorkbook wb = initReverseWorkbook4()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(1).createCell(6);
            assertDouble(fe, cell, "XLOOKUP(E2,C2:C7,B2:B7,0,1,-2)", 0.24);
            assertDouble(fe, cell, "XLOOKUP(39475,C2:C7,B2:B7,0,0,-2)", 0.22);
            assertDouble(fe, cell, "XLOOKUP(39474,C2:C7,B2:B7,0,0,-2)", 0);
        }
    }

    @Test
    void testReverseBinarySearchWithInvalidValues() throws IOException {
        try (HSSFWorkbook wb = initReverseWorkbook4WithInvalidIncomes()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(1).createCell(6);
            assertDouble(fe, cell, "XLOOKUP(E2,C2:C7,B2:B7,0,1,-2)", 0.37);
            assertDouble(fe, cell, "XLOOKUP(9700,C2:C7,B2:B7,0,0,-2)", 0.1);
            assertDouble(fe, cell, "XLOOKUP(39474,C2:C7,B2:B7,0,0,-2)", 0);
        }
    }

    @Test
    void testMicrosoftExample6() throws IOException {
        try (HSSFWorkbook wb = initWorkbook6()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(2).createCell(3);
            assertDouble(fe, cell, "XLOOKUP(B3,B6:B10,E6:E10)", 75.28);
            assertDouble(fe, cell, "XLOOKUP(C3,B6:B10,E6:E10)", 17.25);
            assertDouble(fe, cell, "SUM(XLOOKUP(B3,B6:B10,E6:E10):XLOOKUP(C3,B6:B10,E6:E10))", 110.69);
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

    private HSSFWorkbook initWorkbook2(int empId) {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, null, "Emp Id", "Employee Name", "Department");
        addRow(sheet, 1, null, empId);
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

    private HSSFWorkbook initWorkbook4() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, null, "Tax Rate", "Max Income", null, "Income", "Tax Rate");
        addRow(sheet, 1, null, 0.10, 9700, null, 46523);
        addRow(sheet, 2, null, 0.22, 39475);
        addRow(sheet, 3, null, 0.24, 84200);
        addRow(sheet, 4, null, 0.32, 160726);
        addRow(sheet, 5, null, 0.35, 204100);
        addRow(sheet, 6, null, 0.37, 510300);
        return wb;
    }

    private HSSFWorkbook initReverseWorkbook4() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, null, "Tax Rate", "Max Income", null, "Income", "Tax Rate");
        addRow(sheet, 1, null, 0.37, 510300, null, 46523);
        addRow(sheet, 2, null, 0.35, 204100);
        addRow(sheet, 3, null, 0.32, 160726);
        addRow(sheet, 4, null, 0.24, 84200);
        addRow(sheet, 5, null, 0.22, 39475);
        addRow(sheet, 6, null, 0.10, 9700);
        return wb;
    }

    private HSSFWorkbook initReverseWorkbook4WithInvalidIncomes() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, null, "Tax Rate", "Max Income", null, "Income", "Tax Rate");
        addRow(sheet, 1, null, 0.37, 510300, null, 46523);
        addRow(sheet, 2, null, 0.35, "invalid");
        addRow(sheet, 3, null, 0.32, "invalid");
        addRow(sheet, 4, null, 0.24, "invalid");
        addRow(sheet, 5, null, 0.22, "invalid");
        addRow(sheet, 6, null, 0.10, 9700);
        return wb;
    }

    private HSSFWorkbook initWorkbook5() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0);
        addRow(sheet, 1, null, null, "Quarter", "Gross Profit", "Net Profit", "Profit %");
        addRow(sheet, 2, null, null, "Qtr1");
        addRow(sheet, 3);
        addRow(sheet, 4, null, "Income Statement", "Qtr1", "Qtr2", "Qtr3", "Qtr4", "Total");
        addRow(sheet, 5, null, "Total Sales", 50000, 78200, 89500, 91250, 308950);
        addRow(sheet, 6, null, "Cost of Sales", -25000, -42050, -59450, -60450, -186950);
        addRow(sheet, 7, null, "Gross Profit", 25000, 37150, -30050, -30450, 122000);
        addRow(sheet, 8);
        addRow(sheet, 9, null, "Depreciation", -899, -791, -202, -412, -2304);
        addRow(sheet, 10, null, "Interest", -513, -853, -150, -956, -2472);
        addRow(sheet, 11, null, "Earnings before Tax", 23588, 34506, 29698, 29432, 117224);
        addRow(sheet, 12);
        addRow(sheet, 13, null, "Tax", -4246, -6211, -5346, -5298, -21100);
        addRow(sheet, 14);
        addRow(sheet, 15, null, "Net Profit", 19342, 28293, 24352, 24134, 96124);
        addRow(sheet, 15, null, "Profit %", .293, .278, .234, .236, .269);
        return wb;
    }

    private HSSFWorkbook initWorkbook6() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0);
        addRow(sheet, 1, null, "Start", "End", "Total");
        addRow(sheet, 2, null, "Grape", "Banana");
        addRow(sheet, 3, null, "United States", "US", "+1");
        addRow(sheet, 4);
        addRow(sheet, 5, null, "Product", "Qty", "Price", "Total");
        addRow(sheet, 6, null, "Apple", 23, 0.52, 11.90);
        addRow(sheet, 7, null, "Grape", 98, 0.77, 75.28);
        addRow(sheet, 8, null, "Pear", 75, 0.24, 18.16);
        addRow(sheet, 9, null, "Banana", 95, 0.18, 17.25);
        addRow(sheet, 10, null, "Cherry", 42, 0.16, 6.80);
        return wb;
    }

}
