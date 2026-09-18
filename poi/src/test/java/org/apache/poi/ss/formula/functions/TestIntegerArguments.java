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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.apache.poi.ss.util.Utils.assertError;
import static org.apache.poi.ss.util.Utils.assertString;

/**
 * Integer arguments beyond the int range used to escape the evaluator as an
 * IllegalArgumentException from MathUtil.safeDoubleToInt; Excel reports an error value.
 */
final class TestIntegerArguments {

    @Test
    void testNumericAndDateFunctions() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertError(fe, cell, "DAY(1E308)", FormulaError.NUM);
            assertError(fe, cell, "MONTH(1E10)", FormulaError.NUM);
            assertError(fe, cell, "YEAR(1E10)", FormulaError.NUM);
            assertError(fe, cell, "HOUR(1E10)", FormulaError.NUM);
            assertError(fe, cell, "DATE(1E10,1,1)", FormulaError.NUM);
            assertError(fe, cell, "DATE(2020,1E10,1)", FormulaError.NUM);
            assertError(fe, cell, "DATE(2020,1,1E10)", FormulaError.NUM);
            assertError(fe, cell, "EDATE(1,1E10)", FormulaError.NUM);
            assertError(fe, cell, "EOMONTH(1,1E10)", FormulaError.NUM);
            assertError(fe, cell, "YEARFRAC(1E10,1)", FormulaError.NUM);
            assertError(fe, cell, "TIME(1E10,0,0)", FormulaError.NUM);
            assertError(fe, cell, "LARGE({1,2,3},1E10)", FormulaError.NUM);
            assertError(fe, cell, "SMALL({1,2,3},-1E10)", FormulaError.NUM);
            assertError(fe, cell, "RANK(1,{1,2,3},1E10)", FormulaError.NUM);
            assertError(fe, cell, "POISSON(1E10,1,TRUE)", FormulaError.NUM);
            assertError(fe, cell, "ADDRESS(1E10,1)", FormulaError.NUM);
            assertError(fe, cell, "ADDRESS(1,1,1E10)", FormulaError.NUM);
            assertError(fe, cell, "SUBTOTAL(1E10,{1,2})", FormulaError.NUM);
            assertError(fe, cell, "FIXED(1,1E10)", FormulaError.NUM);
            assertError(fe, cell, "DOLLAR(1,1E10)", FormulaError.VALUE);
            assertError(fe, cell, "DOLLAR(1,-1E10)", FormulaError.NUM);
            // the same functions still work at the edge of the int range
            assertDouble(fe, cell, "LARGE({1,2,3},2.5)", 1, 0);
        }
    }

    @Test
    void testTextAndLookupFunctions() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertError(fe, cell, "CHAR(1E308)", FormulaError.VALUE);
            assertError(fe, cell, "LEFT(\"abc\",1E10)", FormulaError.VALUE);
            assertError(fe, cell, "RIGHT(\"abc\",1E10)", FormulaError.VALUE);
            assertError(fe, cell, "MID(\"abc\",1E10,1)", FormulaError.VALUE);
            assertError(fe, cell, "MID(\"abc\",1,1E10)", FormulaError.VALUE);
            assertError(fe, cell, "FIND(\"a\",\"abc\",1E10)", FormulaError.VALUE);
            assertError(fe, cell, "SEARCH(\"a\",\"abc\",1E10)", FormulaError.VALUE);
            assertError(fe, cell, "REPLACE(\"abc\",1E10,1,\"x\")", FormulaError.VALUE);
            assertError(fe, cell, "SUBSTITUTE(\"abc\",\"a\",\"x\",1E10)", FormulaError.VALUE);
            assertError(fe, cell, "REPT(\"x\",1E10)", FormulaError.VALUE);
            assertError(fe, cell, "CHOOSE(1E10,1,2)", FormulaError.VALUE);
            assertError(fe, cell, "CHOOSE(-1E10,1,2)", FormulaError.VALUE);
            assertError(fe, cell, "ROMAN(1E10)", FormulaError.VALUE);
            assertString(fe, cell, "LEFT(\"abc\",2147483647)", "abc");
        }
    }

    @Test
    void testComplexRendersLargeParts() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertString(fe, cell, "COMPLEX(1E10,1)", "10000000000+i");
            assertString(fe, cell, "COMPLEX(3,-1E10)", "3-10000000000i");
            assertString(fe, cell, "COMPLEX(1E21,2E21)", "1E+21+2E+21i");
            assertString(fe, cell, "COMPLEX(0.1+0.2,0.5)", "0.3+0.5i");
        }
    }
}
