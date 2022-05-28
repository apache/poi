
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
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.addRow;
import static org.apache.poi.ss.util.Utils.assertDouble;

/**
 * Testcase for AVERAGEA() functions
 */
public class TestAverageA {

    //https://support.microsoft.com/en-us/office/averagea-function-f5f84098-d453-4f4c-bbba-3d2c66356091
    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(12);
            assertDouble(fe, cell, "AVERAGEA(A2:A6)", 5.6, 0.00000000001);
            assertDouble(fe, cell, "AVERAGEA(A2:A5,A7)", 5.6, 0.00000000001);
            assertDouble(fe, cell, "AVERAGE(A2:A6)", 7, 0.00000000001);
        }
    }

    @Test
    void testBooleans() throws IOException {
        try (HSSFWorkbook wb = initWorkbook2()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(12);
            assertDouble(fe, cell, "AVERAGEA(A2:A7)", 4.833333333333333, 0.00000000001);
            assertDouble(fe, cell, "AVERAGE(A2:A7)", 7, 0.00000000001);
        }
    }

    @Test
    void testStringsWithNums() throws IOException {
        try (HSSFWorkbook wb = initWorkbook3()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(12);
            assertDouble(fe, cell, "AVERAGEA(A2:A7)", 4.666666666666667, 0.00000000001);
            assertDouble(fe, cell, "AVERAGE(A2:A7)", 7, 0.00000000001);
        }
    }

    private HSSFWorkbook initWorkbook1() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, "Data");
        addRow(sheet, 1, 10);
        addRow(sheet, 2, 7);
        addRow(sheet, 3, 9);
        addRow(sheet, 4, 2);
        addRow(sheet, 5, "Not available");
        addRow(sheet, 6, "Formula");
        return wb;
    }

    private HSSFWorkbook initWorkbook2() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, "Data");
        addRow(sheet, 1, 10);
        addRow(sheet, 2, 7);
        addRow(sheet, 3, 9);
        addRow(sheet, 4, 2);
        addRow(sheet, 5, true);
        addRow(sheet, 6, false);
        return wb;
    }

    private HSSFWorkbook initWorkbook3() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, "Data");
        addRow(sheet, 1, 10);
        addRow(sheet, 2, 7);
        addRow(sheet, 3, 9);
        addRow(sheet, 4, 2);
        addRow(sheet, 5, "4.5");
        addRow(sheet, 6, "14");
        return wb;
    }
}
