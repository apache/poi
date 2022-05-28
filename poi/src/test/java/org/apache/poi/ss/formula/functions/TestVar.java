
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
 * Testcase for functions: VAR.S(), VAR.P(), VARA(), VARPA()
 */
public class TestVar {

    //https://support.microsoft.com/en-us/office/var-s-function-913633de-136b-449d-813e-65a00b2b990b
    //https://support.microsoft.com/en-us/office/var-p-function-73d1285c-108c-4843-ba5d-a51f90656f3a
    //https://support.microsoft.com/en-us/office/vara-function-3de77469-fa3a-47b4-85fd-81758a1e1d07
    //https://support.microsoft.com/en-us/office/varpa-function-59a62635-4e89-4fad-88ac-ce4dc0513b96
    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(12);
            assertDouble(fe, cell, "VARP(A3:A12)", 678.84, 0.00000000001);
            assertDouble(fe, cell, "VAR.P(A3:A12)", 678.84, 0.00000000001);
            assertDouble(fe, cell, "VARPA(A3:A12)", 678.84, 0.00000000001);
            assertDouble(fe, cell, "VAR(A3:A12)", 754.26667, 0.00005);
            assertDouble(fe, cell, "VAR.S(A3:A12)", 754.26667, 0.00005);
            assertDouble(fe, cell, "VARA(A3:A12)", 754.26667, 0.00005);
        }
    }

    @Test
    void testBooleans() throws IOException {
        try (HSSFWorkbook wb = initWorkbook2()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(12);
            assertDouble(fe, cell, "VARP(A2:A7)", 9.5, 0.00000000001);
            assertDouble(fe, cell, "VAR.P(A2:A7)", 9.5, 0.00000000001);
            assertDouble(fe, cell, "VARPA(A2:A7)", 15.805555555555557, 0.00000000001);
            assertDouble(fe, cell, "VAR(A2:A7)", 12.666666666666666, 0.00000000001);
            assertDouble(fe, cell, "VAR.S(A2:A7)", 12.666666666666666, 0.00000000001);
            assertDouble(fe, cell, "VARA(A2:A7)", 18.96666666666667, 0.00000000001);
        }
    }

    @Test
    void testStringsWithNums() throws IOException {
        try (HSSFWorkbook wb = initWorkbook3()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(12);
            assertDouble(fe, cell, "VARA(A2:A7)", 20.666666666666668, 0.00000000001);
            assertDouble(fe, cell, "VAR(A2:A7)", 12.666666666666666, 0.00000000001);
            assertDouble(fe, cell, "VARPA(A2:A7)", 17.222222222222225, 0.00000000001);
            assertDouble(fe, cell, "VARP(A2:A7)", 9.5, 0.00000000001);
        }
    }

    private HSSFWorkbook initWorkbook1() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, "Data");
        addRow(sheet, 1, "Strength");
        addRow(sheet, 2, 1345);
        addRow(sheet, 3, 1301);
        addRow(sheet, 4, 1368);
        addRow(sheet, 5, 1322);
        addRow(sheet, 6, 1310);
        addRow(sheet, 7, 1370);
        addRow(sheet, 8, 1318);
        addRow(sheet, 9, 1350);
        addRow(sheet, 10, 1303);
        addRow(sheet, 11, 1299);
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
