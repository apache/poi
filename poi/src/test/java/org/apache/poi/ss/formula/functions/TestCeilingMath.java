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

import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.apache.poi.ss.util.Utils.assertError;

/**
 * Tests for {@link CeilingMath}
 */
final class TestCeilingMath {

    //https://support.microsoft.com/en-us/office/ceiling-math-function-80f95d2f-b499-4eee-9f16-f795a8e306c8
    @Test
    void testMicrosoftExamples() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell = row.createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertDouble(fe, cell, "CEILING.MATH(24.3,5)", 25.0, 0.00000000000001);
            assertDouble(fe, cell, "CEILING.MATH(6.7)", 7.0, 0.00000000000001);
            assertDouble(fe, cell, "CEILING.MATH(-8.1,2)", -8.0, 0.00000000000001);
            assertDouble(fe, cell, "CEILING.MATH(-5.5,2,-1)", -6.0, 0.00000000000001);

            assertDouble(fe, cell, "CEILING.MATH(2.5,-2)", 4.0, 0.00000000000001);
            assertDouble(fe, cell, "CEILING.MATH(-2.5,-2)", -2.0, 0.00000000000001);
            assertDouble(fe, cell, "CEILING.MATH(-2.5,-2,-1)", -4.0, 0.00000000000001);
            assertDouble(fe, cell, "CEILING.MATH(0.234, 0.01)", 0.24, 0.00000000000001);
        }
    }

    @Test
    void testInvalid() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell = row.createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertError(fe, cell, "CEILING.MATH()", FormulaError.VALUE);
        }
    }

    @Test
    void testNumError() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell = row.createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertError(fe, cell, "CEILING.MATH(\"abc\")", FormulaError.VALUE);
        }
    }

    @Test
    void testActsOnExcelsFifteenDigitView() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            // values a hair off an integer in binary are that integer at 15 significant digits
            assertDouble(fe, cell, "CEILING.MATH(2490399.9999999995)", 2490400.0, 0);
            assertDouble(fe, cell, "CEILING.MATH(2490400.0000000005)", 2490400.0, 0);
            assertDouble(fe, cell, "CEILING.MATH(-2490399.9999999995)", -2490400.0, 0);
            assertDouble(fe, cell, "CEILING.MATH(-2490400.0000000005)", -2490400.0, 0);
            assertDouble(fe, cell, "CEILING.MATH(880000000*0.00849/3)", 2490400.0, 0);
            assertDouble(fe, cell, "CEILING.MATH(0.7/0.1)", 7.0, 0);
            assertDouble(fe, cell, "CEILING.MATH(1.1/0.1)", 11.0, 0);
            assertDouble(fe, cell, "CEILING.MATH(0.1*3,0.1)", 0.3, 0);
            assertDouble(fe, cell, "CEILING.MATH(0.3-0.1-0.1,0.1)", 0.1, 0);
            assertDouble(fe, cell, "CEILING.MATH(2490399.9999999995,100)", 2490400.0, 0);
            // values that differ within 15 significant digits are rounded normally
            assertDouble(fe, cell, "CEILING.MATH(2490399.99999999)", 2490400.0, 0);
            assertDouble(fe, cell, "CEILING.MATH(2490400.00000001)", 2490401.0, 0);
            assertDouble(fe, cell, "CEILING.MATH(-2490399.99999999)", -2490399.0, 0);
            // exact binary fractions are never approximated
            assertDouble(fe, cell, "CEILING.MATH(2.5)", 3.0, 0);
            assertDouble(fe, cell, "CEILING.MATH(-2.5)", -2.0, 0);
            assertDouble(fe, cell, "CEILING.MATH(2.5,0.5)", 2.5, 0);
            assertDouble(fe, cell, "CEILING.MATH(1E15+0.5)", 1E15 + 1, 0);
            assertDouble(fe, cell, "CEILING.MATH(1E20)", 1E20, 0);
            // arguments are coerced as numbers, not via their text
            assertDouble(fe, cell, "CEILING.MATH(TRUE)", 1.0, 0);
            assertDouble(fe, cell, "CEILING.MATH(\"2.5\")", 3.0, 0);
            assertDouble(fe, cell, "CEILING.MATH(D1)", 0.0, 0);
        }
    }

    @Test
    void testModeWithFifteenDigitView() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            // mode <> 0 rounds negative numbers away from zero; the 15-digit view is applied first
            assertDouble(fe, cell, "CEILING.MATH(-2490399.9999999995,1,-1)", -2490400.0, 0);
            assertDouble(fe, cell, "CEILING.MATH(-2490400.0000000005,1,-1)", -2490400.0, 0);
            assertDouble(fe, cell, "CEILING.MATH(-2490400.00000001,1,-1)", -2490401.0, 0);
            assertDouble(fe, cell, "CEILING.MATH(-2.5,1,-1)", -3.0, 0);
            assertDouble(fe, cell, "CEILING.MATH(-0.1*3,0.1,-1)", -0.3, 0);
        }
    }
}
