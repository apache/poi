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

import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.apache.poi.ss.util.Utils.assertError;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for {@link FactDouble}
 */
final class TestFactDouble {

    @Test
    void testFACTDOUBLE() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            //https://support.microsoft.com/en-us/office/factdouble-function-e67697ac-d214-48eb-b7b7-cce2589ecac8
            assertDouble(fe, cell, "FACTDOUBLE(6)", 48.0, 0);
            assertDouble(fe, cell, "FACTDOUBLE(7)", 105.0, 0);
            assertDouble(fe, cell, "FACTDOUBLE(0)", 1.0, 0);
            assertDouble(fe, cell, "FACTDOUBLE(1)", 1.0, 0);
            assertDouble(fe, cell, "FACTDOUBLE(7.9)", 105.0, 0);
            assertDouble(fe, cell, "FACTDOUBLE(-0.5)", 1.0, 0);
            assertError(fe, cell, "FACTDOUBLE(-1)", FormulaError.NUM);
            assertError(fe, cell, "FACTDOUBLE(\"abc\")", FormulaError.VALUE);
            // truncates on Excel's 15-digit view: 4.999999999999999 is 5 to Excel
            assertDouble(fe, cell, "FACTDOUBLE(5-0.0000000000000009)", 15.0, 0);
            assertDouble(fe, cell, "FACTDOUBLE(4.99999999999999)", 8.0, 0);
            // results beyond the long range used to be silently wrapped (FACTDOUBLE(50) was garbage)
            assertDouble(fe, cell, "FACTDOUBLE(50)", 5.204698426366666E32, 0);
            assertDouble(fe, cell, "FACTDOUBLE(300)", 8.154414069380594E307, 0);
            assertDouble(fe, cell, "FACTDOUBLE(299)", 3.753274111571926E306, 0);
            // too large for a double, or for the int range
            assertError(fe, cell, "FACTDOUBLE(301)", FormulaError.NUM);
            assertError(fe, cell, "FACTDOUBLE(1E10)", FormulaError.NUM);
            assertError(fe, cell, "FACTDOUBLE(-1E10)", FormulaError.NUM);
        }
    }
}
