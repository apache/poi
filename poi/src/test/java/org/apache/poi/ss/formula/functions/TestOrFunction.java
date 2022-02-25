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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.addRow;
import static org.apache.poi.ss.util.Utils.assertBoolean;
import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.apache.poi.ss.util.Utils.assertString;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link BooleanFunction#OR}
 */
final class TestOrFunction {

    private static final OperationEvaluationContext ec = new OperationEvaluationContext(null, null, 0, 0, 0, null);

    @Test
    void testMicrosoftExample0() throws IOException {
        //https://support.microsoft.com/en-us/office/or-function-7d17ad14-8700-4281-b308-00b131e22af0
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell = row.createCell(0);
            assertBoolean(fe, cell, "OR(TRUE,TRUE)", true);
            assertBoolean(fe, cell, "OR(TRUE,FALSE)", true);
            assertBoolean(fe, cell, "OR(1=1,2=2,3=3)", true);
            assertBoolean(fe, cell, "OR(1=2,2=3,3=4)", false);
        }
    }

    @Test
    void testMicrosoftExample1() throws IOException {
        //https://support.microsoft.com/en-us/office/or-function-7d17ad14-8700-4281-b308-00b131e22af0
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertBoolean(fe, cell, "OR(A2>1,A2<100)", true);
            assertDouble(fe, cell, "IF(OR(A2>1,A2<100),A3,\"The value is out of range\")", 100);
            assertString(fe, cell, "IF(OR(A2<0,A2>50),A2,\"The value is out of range\")", "The value is out of range");
        }
    }

    @Test
    void testMicrosoftExample2() throws IOException {
        //https://support.microsoft.com/en-us/office/or-function-7d17ad14-8700-4281-b308-00b131e22af0
        try (HSSFWorkbook wb = initWorkbook2()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(13).createCell(3);
            assertDouble(fe, cell, "IF(OR(B14>=$B$4,C14>=$B$5),B14*$B$6,0)", 314);
        }
    }

    @Test
    void testBug65915() throws IOException {
        //https://bz.apache.org/bugzilla/show_bug.cgi?id=65915
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell = row.createCell(0);
            assertDouble(fe, cell, "INDEX({1},1,IF(OR(FALSE,FALSE),1,1))", 1.0);
        }
    }

    @Test
    void testBug65915ArrayFunction() throws IOException {
        //https://bz.apache.org/bugzilla/show_bug.cgi?id=65915
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell = row.createCell(0);
            sheet.setArrayFormula("INDEX({1},1,IF(OR(FALSE,FALSE),0,1))", new CellRangeAddress(0, 0, 0, 0));
            fe.evaluateAll();
            assertEquals(1.0, cell.getNumericCellValue());
        }
    }

    @Test
    void testBug65915Array3Function() throws IOException {
        //https://bz.apache.org/bugzilla/show_bug.cgi?id=65915
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell = row.createCell(0);
            sheet.setArrayFormula("INDEX({1},1,IF(OR(1=2,2=3,3=4),0,1))", new CellRangeAddress(0, 0, 0, 0));
            fe.evaluateAll();
            assertEquals(1.0, cell.getNumericCellValue());
        }
    }

    private HSSFWorkbook initWorkbook1() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, "Values");
        addRow(sheet, 1, 50);
        addRow(sheet, 2, 100);
        return wb;
    }

    private HSSFWorkbook initWorkbook2() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, "Goals");
        addRow(sheet, 2, "Criteria", "Amount");
        addRow(sheet, 3, "Sales Goal", 8500);
        addRow(sheet, 4, "Account Goal", 5);
        addRow(sheet, 5, "Commission Rate", 0.02);
        addRow(sheet, 6, "Bonus Goal", 12500);
        addRow(sheet, 7, "Bonus %", 0.015);
        addRow(sheet, 9, "Commission Calculations");
        addRow(sheet, 11, "Salesperson", "Total Sales", "Accounts", "Commission", "Bonus");
        addRow(sheet, 12, "Millicent Shelton", 10260, 9);
        addRow(sheet, 13, "Miguel Ferrari", 15700, 7);
        return wb;
    }

}
