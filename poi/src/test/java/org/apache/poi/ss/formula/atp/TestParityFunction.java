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

package org.apache.poi.ss.formula.atp;

import static org.apache.poi.ss.util.Utils.assertBoolean;
import static org.apache.poi.ss.util.Utils.assertError;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Testcase for 'Analysis Toolpak' functions ISEVEN() and ISODD()
 */
class TestParityFunction {

    @Test
    void testISEVEN() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            //https://support.microsoft.com/en-us/office/iseven-function-aa15929a-d77b-4fbb-92f4-2f479af55356
            assertBoolean(fe, cell, "ISEVEN(-1)", false);
            assertBoolean(fe, cell, "ISEVEN(2.5)", true);
            assertBoolean(fe, cell, "ISEVEN(5)", false);
            assertBoolean(fe, cell, "ISEVEN(0)", true);
            assertBoolean(fe, cell, "ISEVEN(-2.9)", true);
            assertBoolean(fe, cell, "ISEVEN(-3.1)", false);
            assertBoolean(fe, cell, "ISEVEN(TRUE)", false);
            assertBoolean(fe, cell, "ISEVEN(\"4.7\")", true);
            assertError(fe, cell, "ISEVEN(\"abc\")", FormulaError.VALUE);
            // truncates on Excel's 15-digit view: 2490399.9999999995 is 2490400 to Excel
            assertBoolean(fe, cell, "ISEVEN(880000000*0.00849/3)", true);
            assertBoolean(fe, cell, "ISEVEN(2.9999999999999996)", false);
            assertBoolean(fe, cell, "ISEVEN(-2.9999999999999996)", false);
            assertBoolean(fe, cell, "ISEVEN(2.99999999999999)", true);
            // beyond the long range
            assertBoolean(fe, cell, "ISEVEN(1E19)", true);
            assertBoolean(fe, cell, "ISEVEN(-1E19)", true);
            assertBoolean(fe, cell, "ISEVEN(2^53-1)", false);
        }
    }

    @Test
    void testISODD() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            //https://support.microsoft.com/en-us/office/isodd-function-1208a56d-4f10-4f44-a5fc-648cafd6c07a
            assertBoolean(fe, cell, "ISODD(-1)", true);
            assertBoolean(fe, cell, "ISODD(2.5)", false);
            assertBoolean(fe, cell, "ISODD(5)", true);
            assertBoolean(fe, cell, "ISODD(0)", false);
            assertBoolean(fe, cell, "ISODD(-2.9)", false);
            assertBoolean(fe, cell, "ISODD(-3.1)", true);
            assertError(fe, cell, "ISODD(\"abc\")", FormulaError.VALUE);
            // truncates on Excel's 15-digit view
            assertBoolean(fe, cell, "ISODD(880000000*0.00849/3)", false);
            assertBoolean(fe, cell, "ISODD(2.9999999999999996)", true);
            assertBoolean(fe, cell, "ISODD(-2.9999999999999996)", true);
            assertBoolean(fe, cell, "ISODD(2.99999999999999)", false);
            // beyond the long range
            assertBoolean(fe, cell, "ISODD(1E19)", false);
            assertBoolean(fe, cell, "ISODD(2^53-1)", true);
        }
    }
}
