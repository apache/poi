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
}
