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
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.MissingArgEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestProduct {
    @Test
    void missingArgsAreIgnored() {
        ValueEval result = getInstance().evaluate(new ValueEval[]{new NumberEval(2.0), MissingArgEval.instance}, 0, 0);
        assertTrue(result instanceof NumberEval);
        assertEquals(2, ((NumberEval)result).getNumberValue(), 0);
    }

    /**
     * Actually PRODUCT() requires at least one arg but the checks are performed elsewhere.
     * However, PRODUCT(,) is a valid call (which should return 0). So it makes sense to
     * assert that PRODUCT() is also 0 (at least, nothing explodes).
     */
    @Test
    void missingArgEvalReturns0() {
        ValueEval result = getInstance().evaluate(new ValueEval[0], 0, 0);
        assertTrue(result instanceof NumberEval);
        assertEquals(0, ((NumberEval)result).getNumberValue(), 0);
    }

    @Test
    void twoMissingArgEvalsReturn0() {
        ValueEval result = getInstance().evaluate(new ValueEval[]{MissingArgEval.instance, MissingArgEval.instance}, 0, 0);
        assertTrue(result instanceof NumberEval);
        assertEquals(0, ((NumberEval)result).getNumberValue(), 0);
    }

    @Test
    void acceptanceTest() {
        final ValueEval[] args = {
                new NumberEval(2.0),
                MissingArgEval.instance,
                new StringEval("6"),
                BoolEval.TRUE};
        ValueEval result = getInstance().evaluate(args, 0, 0);
        assertTrue(result instanceof NumberEval);
        assertEquals(12, ((NumberEval)result).getNumberValue(), 0);
    }

    @Test
    void test2Booleans() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row0 = sheet.createRow(0);
            HSSFRow row1 = sheet.createRow(1);
            row0.createCell(0).setCellValue(true);
            row0.createCell(1).setCellValue(true);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = row1.createCell(0);
            assertDouble(fe, cell, "PRODUCT(TRUE,TRUE)", 1.0, 0.000001);
            assertDouble(fe, cell, "PRODUCT(A1:B1)", 0.0, 0.000001);
            assertDouble(fe, cell, "PRODUCT(A1,B1)", 0.0, 0.000001);
        }
    }

    private static Function getInstance() {
        return AggregateFunction.PRODUCT;
    }
}
