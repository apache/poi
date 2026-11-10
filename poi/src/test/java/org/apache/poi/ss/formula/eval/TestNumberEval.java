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

package org.apache.poi.ss.formula.eval;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.ptg.IntPtg;
import org.apache.poi.ss.formula.ptg.NumberPtg;
import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.apache.poi.ss.util.Utils.assertError;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestNumberEval {

    /**
     * Excel has no subnormal numbers: anything closer to zero than Double.MIN_NORMAL is zero to it
     */
    @Test
    void testSubnormalsAreFlushedToZero() {
        for (double d : new double[]{Double.MIN_VALUE, -Double.MIN_VALUE, 1E-308, -1E-308, Math.nextDown(Double.MIN_NORMAL)}) {
            assertEquals(0.0, new NumberEval(d).getNumberValue(), 0, Double.toString(d));
            assertEquals("0", new NumberEval(d).getStringValue(), Double.toString(d));
            assertEquals(0.0, new NumberEval(new NumberPtg(d)).getNumberValue(), 0, Double.toString(d));
        }
        // the smallest normal double and everything above it is kept as it is
        for (double d : new double[]{Double.MIN_NORMAL, -Double.MIN_NORMAL, 1E-300, 1, Double.MAX_VALUE}) {
            assertEquals(d, new NumberEval(d).getNumberValue(), 0, Double.toString(d));
            assertEquals(d, new NumberEval(new NumberPtg(d)).getNumberValue(), 0, Double.toString(d));
        }
        assertEquals(42, new NumberEval(new IntPtg(42)).getNumberValue(), 0);
    }

    @Test
    void testSignOfZeroIsKept() {
        assertTrue(Double.compare(-0.0, new NumberEval(-0.0).getNumberValue()) == 0);
        assertTrue(Double.compare(0.0, new NumberEval(0.0).getNumberValue()) == 0);
    }

    @Test
    void testSubnormalFormulaResults() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            // the literal itself, and functions whose result underflows
            assertDouble(fe, cell, "1E-308", 0.0, 0);
            assertDouble(fe, cell, "-1E-308", 0.0, 0);
            assertDouble(fe, cell, "ABS(1E-308)", 0.0, 0);
            assertDouble(fe, cell, "RADIANS(1E-307)", 0.0, 0);
            assertDouble(fe, cell, "EXP(-709)", 0.0, 0);
            assertDouble(fe, cell, "POWER(1E-200,2)", 0.0, 0);
            assertDouble(fe, cell, "SQRT(1E-308)", 0.0, 0);
            // and so a subnormal divisor is a zero divisor
            assertError(fe, cell, "MOD(1,1E-308)", FormulaError.DIV0);
            assertError(fe, cell, "1/1E-308", FormulaError.DIV0);
            // the smallest normal double survives
            assertDouble(fe, cell, "2.2250738585072014E-308", Double.MIN_NORMAL, 0);
        }
    }
}
