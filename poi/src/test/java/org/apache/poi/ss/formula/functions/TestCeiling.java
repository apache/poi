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
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.apache.poi.ss.util.Utils.assertError;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for Ceiling function
 */
final class TestCeiling {

    //https://support.microsoft.com/en-us/office/ceiling-function-0a5cd7c8-0720-4f0a-bd2c-c943e510899f
    @Test
    void testMicrosoftExamples() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell = row.createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertDouble(fe, cell, "CEILING(2.5, 1)", 3.0, 0.00000000000001);
            assertDouble(fe, cell, "CEILING(-2.5, -2)", -4.0, 0.00000000000001);
            assertDouble(fe, cell, "CEILING(-2.5, 2)", -2.0, 0.00000000000001);
            assertDouble(fe, cell, "CEILING(1.5, 0.1)", 1.5, 0.00000000000001);
            assertDouble(fe, cell, "CEILING(0.234, 0.01)", 0.24, 0.00000000000001);
        }
    }

    @Test
    void testInvalid() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell = row.createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertThrows(FormulaParseException.class, () ->
                assertError(fe, cell, "CEILING()", FormulaError.VALUE));
        }
    }

    @Test
    void testInvalidNum() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell = row.createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertError(fe, cell, "CEILING(\"abc\", \"def\")", FormulaError.VALUE);
        }
    }

    @Test
    void testCeilingActsOnExcelsFifteenDigitView() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            // just above an integer in binary, but the integer at 15 significant digits
            assertDouble(fe, cell, "CEILING(2490400.0000000005,1)", 2490400.0, 0);
            assertDouble(fe, cell, "CEILING(2490400.0000000005,100)", 2490400.0, 0);
            assertDouble(fe, cell, "CEILING(-2490400.0000000005,-1)", -2490400.0, 0);
            assertDouble(fe, cell, "CEILING(3.0000000000000004,1)", 3.0, 0);
            assertDouble(fe, cell, "CEILING(0.1*3,0.1)", 0.3, 0);
            assertDouble(fe, cell, "CEILING(0.1+0.2,0.1)", 0.3, 0);
            assertDouble(fe, cell, "CEILING(1.1/0.1,1)", 11.0, 0);
            // and just below, likewise
            assertDouble(fe, cell, "CEILING(2490399.9999999995,1)", 2490400.0, 0);
            assertDouble(fe, cell, "CEILING(0.7/0.1,1)", 7.0, 0);
            // values that differ within 15 significant digits are rounded up normally
            assertDouble(fe, cell, "CEILING(2490400.00000001,1)", 2490401.0, 0);
            assertDouble(fe, cell, "CEILING(3.00000000000001,1)", 4.0, 0);
            // exact binary fractions are never approximated
            assertDouble(fe, cell, "CEILING(2.5,1)", 3.0, 0);
            assertDouble(fe, cell, "CEILING(2.5,0.5)", 2.5, 0);
            assertDouble(fe, cell, "CEILING(1E15+0.5,1)", 1E15 + 1, 0);
        }
    }
}
