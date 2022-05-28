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
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.addRow;
import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.apache.poi.ss.util.Utils.assertError;

/**
 * Tests for {@link Poisson}
 */
final class TestPoissonDist {

    private static final OperationEvaluationContext ec = new OperationEvaluationContext(null, null, 0, 0, 0, null);

    //https://support.microsoft.com/en-us/office/poisson-dist-function-8fe148ff-39a2-46cb-abf3-7772695d9636
    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            addRow(sheet, 0, "Data", "Description");
            addRow(sheet, 1, 2);
            addRow(sheet, 2, 5);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertDouble(fe, cell, "POISSON.DIST(A2,A3,TRUE)", 0.12465201948308113, 0.00000000000001);
            assertDouble(fe, cell, "POISSON.DIST(A2,A3,FALSE)", 0.08422433748856833, 0.00000000000001);
            assertDouble(fe, cell, "POISSON(A2,A3,TRUE)", 0.12465201948308113, 0.00000000000001);
            assertDouble(fe, cell, "POISSON(A2,A3,FALSE)", 0.08422433748856833, 0.00000000000001);
            assertDouble(fe, cell, "POISSON(2.1,5,FALSE)", 0.08422433748856833, 0.00000000000001);
            assertDouble(fe, cell, "POISSON(2.9,5,FALSE)", 0.08422433748856833, 0.00000000000001);
        }
    }

    @Test
    void testInvalid() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            addRow(sheet, 0, "Data", "Description");
            addRow(sheet, 1, 2);
            addRow(sheet, 2, 5);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertError(fe, cell, "POISSON.DIST(A2,A3)", FormulaError.VALUE);
            assertError(fe, cell, "POISSON.DIST(\"abc\",A3,TRUE)", FormulaError.VALUE);
            assertError(fe, cell, "POISSON.DIST(A2,\"A3\",TRUE)", FormulaError.VALUE);
            assertError(fe, cell, "POISSON.DIST(-1,A3,TRUE)", FormulaError.NUM);
            assertError(fe, cell, "POISSON.DIST(A2,-5,TRUE)", FormulaError.NUM);
        }
    }

}
