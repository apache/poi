
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
import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.addRow;
import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.apache.poi.ss.util.Utils.assertDoubleAndDisplay;
import static org.apache.poi.ss.util.Utils.assertError;

/**
 * Testcase for function DVAR() and DVARP()
 */
public class TestDVar {

    //https://support.microsoft.com/en-us/office/dvar-function-d6747ca9-99c7-48bb-996e-9d7af00f3ed1
    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(12);
            assertDouble(fe, cell, "DVAR(A4:E10, \"Yield\", A1:A3)", 8.8, 0.0000000001);
            assertDouble(fe, cell, "DVAR(A4:E10, \"Yield\", A12:A13)", 1.0, 0.0000000001);
            assertDouble(fe, cell, "DVAR(A4:E10, \"Yield\", B12:C13)", 10.9166666667, 0.0000000001);
        }
    }

    //https://support.microsoft.com/en-us/office/dvarp-function-eb0ba387-9cb7-45c8-81e9-0394912502fc
    @Test
    void testDVARPMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(12);
            assertDouble(fe, cell, "DVARP(A4:E10, \"Yield\", A1:A3)", 7.04, 0.0000000001);
            assertDouble(fe, cell, "DVARP(A4:E10, \"Yield\", A12:A13)", 0.666666666666667, 0.0000000001);
            assertDouble(fe, cell, "DVARP(A4:E10, \"Yield\", B12:C13)", 8.1875, 0.0000000001);
        }
    }

    @Test
    void testResultIsNotRoundedToFifteenDigits() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(12);
            // yields 14, 9, 10, 6: the full IEEE 754 result, which Excel displays as 10.9166666666667
            assertDoubleAndDisplay(fe, cell, "DVAR(A4:E10, \"Yield\", B12:C13)", 10.916666666666666, "10.9166666666667");
            assertDoubleAndDisplay(fe, cell, "DVARP(A4:E10, \"Yield\", B12:C13)", 8.1875, "8.1875");
            assertDoubleAndDisplay(fe, cell, "DVARP(A4:E10, \"Yield\", A12:A13)", 2.0 / 3, "0.666666666666667");
        }
    }

    @Test
    void testTooFewRecords() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(12);
            // exactly one record matches (Tree = Cherry), none matches Tree = Plum
            addRow(wb.getSheetAt(0), 20, "Tree", "Tree");
            addRow(wb.getSheetAt(0), 21, "Cherry", "Plum");
            // used to put Infinity (later NaN) in the cell rather than an error
            assertError(fe, cell, "DVAR(A4:E10, \"Yield\", A21:A22)", FormulaError.DIV0);
            assertError(fe, cell, "DVAR(A4:E10, \"Yield\", B21:B22)", FormulaError.DIV0);
            assertDouble(fe, cell, "DVARP(A4:E10, \"Yield\", A21:A22)", 0.0);
            assertError(fe, cell, "DVARP(A4:E10, \"Yield\", B21:B22)", FormulaError.DIV0);
        }
    }

    private HSSFWorkbook initWorkbook1() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, "Tree", "Height", "Age", "Yield", "Profit", "Height");
        addRow(sheet, 1, "=Apple", ">10", null, null, null, "<16");
        addRow(sheet, 2, "=Pear");
        addRow(sheet, 3, "Tree", "Height", "Age", "Yield", "Profit");
        addRow(sheet, 4, "Apple", 18, 20, 14, 105);
        addRow(sheet, 5, "Pear", 12, 12, 10, 96);
        addRow(sheet, 6, "Cherry", 13, 14, 9, 105);
        addRow(sheet, 7, "Apple", 14, 15, 10, 75);
        addRow(sheet, 8, "Pear", 9, 8, 8, 77);
        addRow(sheet, 9, "Apple", 8, 9, 6, 45);
        addRow(sheet, 10);
        addRow(sheet, 11, "Tree", "Height", "Height");
        addRow(sheet, 12, "<>Apple", "<>12", "<>9");
        return wb;
    }
}
