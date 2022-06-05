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
import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.addRow;
import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.apache.poi.ss.util.Utils.assertError;

/**
 * Tests for {@link Correl}
 */
final class TestCorrel {

    //https://support.microsoft.com/en-us/office/correl-function-995dcef7-0c0a-4bed-a3fb-239d7b68ca92
    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.getRow(0);
            HSSFCell cell = row.createCell(100);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertDouble(fe, cell, "CORREL(A2:A6,B2:B6)", 0.997054486, 0.0000000005);
        }
    }

    //https://support.microsoft.com/en-us/office/pearson-function-0c3e30fc-e5af-49c4-808a-3ef66e034c18
    @Test
    void testPearsonExample1() throws IOException {
        try (HSSFWorkbook wb = initWorkbook2()) {
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.getRow(0);
            HSSFCell cell = row.createCell(100);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertDouble(fe, cell, "CORREL(A2:A6,B2:B6)", 0.699379, 0.0000005);
            assertDouble(fe, cell, "PEARSON(A2:A6,B2:B6)", 0.699379, 0.0000005);
        }
    }

    @Test
    void testBlankValue() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1(null)) {
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.getRow(0);
            HSSFCell cell = row.createCell(100);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertDouble(fe, cell, "CORREL(A2:A6,B2:B6)", 0.9984884738, 0.0000000005);
        }
    }

    @Test
    void testStringValue() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1("string")) {
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.getRow(0);
            HSSFCell cell = row.createCell(100);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertDouble(fe, cell, "CORREL(A2:A6,B2:B6)", 0.9984884738, 0.0000000005);
        }
    }

    @Test
    void testMismatch() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.getRow(0);
            HSSFCell cell = row.createCell(100);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertError(fe, cell, "CORREL(A2:A6,B2:B5)", FormulaError.NA);
            assertError(fe, cell, "CORREL(A2:B6,B2:B6)", FormulaError.NA);
        }
    }

    private HSSFWorkbook initWorkbook1() {
        return initWorkbook1(Double.valueOf(15));
    }

    private HSSFWorkbook initWorkbook1(Object row4Data2) {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, "Data1", "Data2");
        addRow(sheet, 1, 3, 9);
        addRow(sheet, 2, 2, 7);
        addRow(sheet, 3, 4, 12);
        addRow(sheet, 4, 5, row4Data2);
        addRow(sheet, 5, 6, 17);
        return wb;
    }

    private HSSFWorkbook initWorkbook2() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, "Independent values", "Dependent values");
        addRow(sheet, 1, 9, 10);
        addRow(sheet, 2, 7, 6);
        addRow(sheet, 3, 5, 1);
        addRow(sheet, 4, 3, 5);
        addRow(sheet, 5, 1, 3);
        return wb;
    }
}
