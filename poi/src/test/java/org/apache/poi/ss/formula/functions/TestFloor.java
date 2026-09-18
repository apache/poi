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
 * Tests for Floor function
 */
final class TestFloor {

    //https://support.microsoft.com/en-us/office/floor-function-14bb497c-24f2-4e04-b327-b0b4de5a8886
    @Test
    void testMicrosoftExamples() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell = row.createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertDouble(fe, cell, "FLOOR(3.7,2)", 2.0, 0.00000000000001);
            assertDouble(fe, cell, "FLOOR(-2.5,-2)", -2.0, 0.00000000000001);
            assertError(fe, cell, "FLOOR(2.5,-2)", FormulaError.NUM);
            assertDouble(fe, cell, "FLOOR(1.58,0.1)", 1.5, 0.00000000000001);
            assertDouble(fe, cell, "FLOOR(0.234,0.01)", 0.23, 0.00000000000001);
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
                assertError(fe, cell, "FLOOR()", FormulaError.VALUE));
        }
    }

    @Test
    void testInvalidNum() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell = row.createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertError(fe, cell, "FLOOR(\"abc\", \"def\")", FormulaError.VALUE);
        }
    }

    @Test
    void testFloorActsOnExcelsFifteenDigitView() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            // 880000000*0.00849/3 is 2490399.9999999995 in binary, 2490400 to Excel
            assertDouble(fe, cell, "FLOOR(880000000*0.00849/3,1)", 2490400.0, 0);
            assertDouble(fe, cell, "FLOOR(2490399.9999999995,1)", 2490400.0, 0);
            assertDouble(fe, cell, "FLOOR(2490399.9999999995,100)", 2490400.0, 0);
            assertDouble(fe, cell, "FLOOR(-2490399.9999999995,-1)", -2490400.0, 0);
            assertDouble(fe, cell, "FLOOR(0.7/0.1,1)", 7.0, 0);
            assertDouble(fe, cell, "FLOOR(0.3-0.1-0.1,0.1)", 0.1, 0);
            assertDouble(fe, cell, "FLOOR(0.1+0.2,0.1)", 0.3, 0);
            assertDouble(fe, cell, "FLOOR(4.35*100,1)", 435.0, 0);
            // the significance is seen the same way: 0.1*3 is 0.3, not 0.30000000000000004
            assertDouble(fe, cell, "FLOOR(0.9,0.1*3)", 0.9, 0);
            assertDouble(fe, cell, "FLOOR(1.6,0.7+0.1)", 1.6, 0);
            assertDouble(fe, cell, "FLOOR(-0.9,-(0.1*3))", -0.9, 0);
            // values that differ within 15 significant digits are floored normally
            assertDouble(fe, cell, "FLOOR(2490399.99999999,1)", 2490399.0, 0);
            assertDouble(fe, cell, "FLOOR(2.99999999999999,1)", 2.0, 0);
            // exact binary fractions are never approximated
            assertDouble(fe, cell, "FLOOR(2.5,1)", 2.0, 0);
            assertDouble(fe, cell, "FLOOR(2.5,0.5)", 2.5, 0);
            assertDouble(fe, cell, "FLOOR(1E15+0.5,1)", 1E15, 0);
        }
    }
}
