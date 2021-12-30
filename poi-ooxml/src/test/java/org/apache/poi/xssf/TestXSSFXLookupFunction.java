
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
package org.apache.poi.xssf;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Locale;

import static org.apache.poi.ss.util.Utils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testcase for function XLOOKUP()
 */
class TestXSSFXLookupFunction {

    //https://support.microsoft.com/en-us/office/xlookup-function-b7fd680e-6d10-43e6-84f9-88eae8bf5929

    @Test
    void testMicrosoftExample2() throws IOException {
        String formulaText = "XLOOKUP(B2,B5:B14,C5:D14)";
        try (XSSFWorkbook wb = initWorkbook2()) {
            XSSFFormulaEvaluator fe = new XSSFFormulaEvaluator(wb);
            XSSFSheet sheet = wb.getSheetAt(0);
            XSSFRow row1 = sheet.getRow(1);
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
    void testXLookupFile() throws Exception {
        try (XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("xlookup.xlsx")) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            XSSFFormulaEvaluator fe = new XSSFFormulaEvaluator(workbook);
            XSSFRow row1 = sheet.getRow(1);
            assertEquals("Dianne Pugh", row1.getCell(2).getStringCellValue());
            assertEquals("Finance", row1.getCell(3).getStringCellValue());
            fe.evaluateAll();
            row1 = sheet.getRow(1);
            assertEquals("Dianne Pugh", row1.getCell(2).getStringCellValue());
            assertEquals("Finance", row1.getCell(3).getStringCellValue());
        }
    }

    private XSSFWorkbook initWorkbook2() {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
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
