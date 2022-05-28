
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
 * Testcase for functions: STDEV(), STDEVP(), STDEV.S(), STDEV.P(), STDEVA(), STDEVPA()
 */
public class TestStdev {

    //https://support.microsoft.com/en-us/office/stdevp-function-1f7c1c88-1bec-4422-8242-e9f7dc8bb195
    //https://support.microsoft.com/en-us/office/stdev-p-function-6e917c05-31a0-496f-ade7-4f4e7462f285
    //https://support.microsoft.com/en-us/office/stdev-s-function-7d69cf97-0c1f-4acf-be27-f3e83904cc23
    //https://support.microsoft.com/en-us/office/stdeva-function-5ff38888-7ea5-48de-9a6d-11ed73b29e9d
    //https://support.microsoft.com/en-us/office/stdevpa-function-5578d4d6-455a-4308-9991-d405afe2c28c
    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(12);
            assertDouble(fe, cell, "STDEVP(A3:A12)", 26.0545581424825, 0.00000000001);
            assertDouble(fe, cell, "STDEV.P(A3:A12)", 26.0545581424825, 0.00000000001);
            assertDouble(fe, cell, "STDEVPA(A3:A12)", 26.0545581424825, 0.00000000001);
            assertDouble(fe, cell, "STDEV(A3:A12)", 27.4639157198435, 0.00000000001);
            assertDouble(fe, cell, "STDEV.S(A3:A12)", 27.4639157198435, 0.00000000001);
            assertDouble(fe, cell, "STDEVA(A3:A12)", 27.4639157198435, 0.00000000001);
        }
    }

    @Test
    void testBooleans() throws IOException {
        try (HSSFWorkbook wb = initWorkbook2()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(12);
            assertDouble(fe, cell, "STDEVP(A2:A7)", 3.082207001484488, 0.00000000001);
            assertDouble(fe, cell, "STDEV.P(A2:A7)", 3.082207001484488, 0.00000000001);
            assertDouble(fe, cell, "STDEVPA(A2:A7)", 3.975620147292188, 0.00000000001);
            assertDouble(fe, cell, "STDEV(A2:A7)", 3.559026084010437, 0.00000000001);
            assertDouble(fe, cell, "STDEV.S(A2:A7)", 3.559026084010437, 0.00000000001);
            assertDouble(fe, cell, "STDEVA(A2:A7)", 4.355073669487885, 0.00000000001);
        }
    }

    @Test
    void testStringsWithNums() throws IOException {
        try (HSSFWorkbook wb = initWorkbook3()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(12);
            assertDouble(fe, cell, "STDEVA(A2:A7)", 4.546060565661952, 0.00000000001);
            assertDouble(fe, cell, "STDEV(A2:A7)", 3.559026084010437, 0.00000000001);
            assertDouble(fe, cell, "STDEVPA(A2:A7)", 4.149966532662911, 0.00000000001);
            assertDouble(fe, cell, "STDEVP(A2:A7)", 3.082207001484488, 0.00000000001);
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
