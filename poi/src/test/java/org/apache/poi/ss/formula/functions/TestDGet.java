
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
import static org.apache.poi.ss.util.Utils.assertError;

/**
 * Testcase for function DGET()
 */
public class TestDGet {

    //https://support.microsoft.com/en-us/office/dget-function-455568bf-4eef-45f7-90f0-ec250d00892e
    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1(false, "=Apple")) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertError(fe, cell, "DGET(A5:E11, \"Yield\", A1:A3)", FormulaError.NUM);
            assertDouble(fe, cell, "DGET(A5:E11, \"Yield\", A1:F3)", 10);
            assertDouble(fe, cell, "DGET(A5:E11, 4, A1:F3)", 10);
        }
    }

    @Test
    void testMicrosoftExample1CaseInsensitive() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1(false, "=apple")) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertError(fe, cell, "DGET(A5:E11, \"Yield\", A1:A3)", FormulaError.NUM);
            assertDouble(fe, cell, "DGET(A5:E11, \"Yield\", A1:F3)", 10);
            assertDouble(fe, cell, "DGET(A5:E11, 4, A1:F3)", 10);
        }
    }

    @Test
    void testMicrosoftExample1StartsWith() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1(false, "App")) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertError(fe, cell, "DGET(A5:E11, \"Yield\", A1:A3)", FormulaError.NUM);
            assertDouble(fe, cell, "DGET(A5:E11, \"Yield\", A1:F3)", 10);
            assertDouble(fe, cell, "DGET(A5:E11, 4, A1:F3)", 10);
        }
    }

    @Test
    void testMicrosoftExample1StartsWithLowercase() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1(false, "app")) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertError(fe, cell, "DGET(A5:E11, \"Yield\", A1:A3)", FormulaError.NUM);
            assertDouble(fe, cell, "DGET(A5:E11, \"Yield\", A1:F3)", 10);
            assertDouble(fe, cell, "DGET(A5:E11, 4, A1:F3)", 10);
        }
    }

    @Test
    void testMicrosoftExample1Wildcard() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1(false, "A*le")) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertError(fe, cell, "DGET(A5:E11, \"Yield\", A1:A3)", FormulaError.NUM);
            assertDouble(fe, cell, "DGET(A5:E11, \"Yield\", A1:F3)", 10);
            assertDouble(fe, cell, "DGET(A5:E11, 4, A1:F3)", 10);
        }
    }

    @Test
    void testMicrosoftExample1WildcardLowercase() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1(false, "a*le")) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertError(fe, cell, "DGET(A5:E11, \"Yield\", A1:A3)", FormulaError.NUM);
            assertDouble(fe, cell, "DGET(A5:E11, \"Yield\", A1:F3)", 10);
            assertDouble(fe, cell, "DGET(A5:E11, 4, A1:F3)", 10);
        }
    }

    @Test
    void testMicrosoftExample1AppleWildcardNoMatch() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1(false, "A*x")) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertError(fe, cell, "DGET(A5:E11, \"Yield\", A1:A3)", FormulaError.NUM);
            assertError(fe, cell, "DGET(A5:E11, \"Yield\", A1:F3)", FormulaError.VALUE);
        }
    }

    @Test
    void testMicrosoftExample1Variant() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1(true, "=Apple")) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertDouble(fe, cell, "DGET(A5:E11, \"Yield\", A1:F3)", 6);
            assertDouble(fe, cell, "DGET(A5:E11, 4, A1:F3)", 6);
        }
    }

    private HSSFWorkbook initWorkbook1(boolean adjustAppleCondition, String appleCondition) {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, "Tree", "Height", "Age", "Yield", "Profit", "Height");
        if (adjustAppleCondition) {
            addRow(sheet, 1, appleCondition, ">=8", null, null, null, "<12");
        } else {
            addRow(sheet, 1, appleCondition, ">10", null, null, null, "<16");
        }
        addRow(sheet, 2, "Pear", ">12");
        addRow(sheet, 3);
        addRow(sheet, 4, "Tree", "Height", "Age", "Yield", "Profit");
        addRow(sheet, 5, "Apple", 18, 20, 14, 105);
        addRow(sheet, 6, "Pear", 12, 12, 10, 96);
        addRow(sheet, 7, "Cherry", 13, 14, 9, 105);
        addRow(sheet, 8, "Apple", 14, null, 10, 75);
        addRow(sheet, 9, "Pear", 9, 8, 8, 77);
        addRow(sheet, 10, "Apple", 8, 9, 6, 45);
        return wb;
    }
}
