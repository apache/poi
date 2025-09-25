
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
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;

import static org.apache.poi.ss.util.Utils.addRow;
import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.apache.poi.ss.util.Utils.assertError;

/**
 * Testcase for DAYS() functions
 */
public class TestDays {

    @BeforeAll
    static void setUp() {
        LocaleUtil.setUserLocale(Locale.US);
    }

    @AfterAll
    static void tearDown() {
        LocaleUtil.setUserLocale(null);
    }

    //https://support.microsoft.com/en-us/office/days-function-57740535-d549-4395-8728-0f07bff0b9df
    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(12);
            assertDouble(fe, cell, "DAYS(\"15-MAR-2021\",\"1-FEB-2021\")", 42, 0.00000000001);
            assertDouble(fe, cell, "DAYS(A2,A3)", 364, 0.00000000001);
            assertDouble(fe, cell, "DAYS(\"1-FEB-2021\", \"15-MAR-2021\")", -42, 0.00000000001);
        }
    }

    @Test
    void testInvalid() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(12);
            assertError(fe, cell, "DAYS(\"15-XYZ\",\"1-FEB-2021\")", FormulaError.VALUE);
            assertError(fe, cell, "DAYS(\"15-MAR-2021\",\"1-XYZ\")", FormulaError.VALUE);
            assertError(fe, cell, "DAYS(\"15-MAR-2021\")", FormulaError.VALUE);
        }
    }

    private HSSFWorkbook initWorkbook1() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, "Data");
        addRow(sheet, 1, DateUtil.getExcelDate(LocalDate.parse("2021-12-31")));
        addRow(sheet, 2, DateUtil.getExcelDate(LocalDate.parse("2021-01-01")));
        return wb;
    }
}
