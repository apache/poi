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
import static org.apache.poi.ss.util.Utils.assertString;

/**
 * Functions given an argument of a type they did not expect used to escape the evaluator with a
 * ClassCastException, IllegalArgumentException or IllegalStateException; Excel either accepts the
 * value (a number as a logical, a single value as an array, a one-cell area as a value) or reports #VALUE!.
 */
final class TestArgumentTypes {

    private static HSSFCell formulaCell(HSSFWorkbook wb) {
        HSSFSheet sheet = wb.createSheet();
        HSSFRow row = sheet.createRow(0);
        row.createCell(0).setCellValue("101");
        row.createCell(1).setCellValue(5);
        sheet.createRow(1).createCell(0).setCellValue("11");
        return sheet.createRow(9).createCell(0);
    }

    @Test
    void testCountFunctionsNeedARange() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = formulaCell(wb);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertError(fe, cell, "COUNTIF(5,5)", FormulaError.VALUE);
            assertError(fe, cell, "COUNTIF(\"abc\",\"abc\")", FormulaError.VALUE);
            assertError(fe, cell, "COUNTBLANK(5)", FormulaError.VALUE);
            assertError(fe, cell, "COUNTBLANK(TRUE)", FormulaError.VALUE);
            assertDouble(fe, cell, "COUNTIF(A1:B2,5)", 1, 0);
            assertDouble(fe, cell, "COUNTBLANK(A1:B2)", 1, 0);
        }
    }

    @Test
    void testIndexOfASingleValue() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = formulaCell(wb);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertDouble(fe, cell, "INDEX(5,1)", 5, 0);
            assertDouble(fe, cell, "INDEX(5,1,1)", 5, 0);
            assertDouble(fe, cell, "INDEX(5,0)", 5, 0);
            assertString(fe, cell, "INDEX(\"abc\",1)", "abc");
            assertError(fe, cell, "INDEX(5,2)", FormulaError.REF);
            assertError(fe, cell, "INDEX(5,1,2)", FormulaError.REF);
            assertError(fe, cell, "INDEX(NA(),1)", FormulaError.NA);
        }
    }

    @Test
    void testIfsCoercesTheLogicalTest() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = formulaCell(wb);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertString(fe, cell, "IFS(1,\"a\")", "a");
            assertString(fe, cell, "IFS(0,\"a\",2.5,\"b\")", "b");
            assertString(fe, cell, "IFS(B1=5,\"a\")", "a");
            assertString(fe, cell, "IFS(\"TRUE\",\"a\")", "a");
            assertError(fe, cell, "IFS(0,\"a\")", FormulaError.NA);
            assertError(fe, cell, "IFS(FALSE,\"a\")", FormulaError.NA);
            assertError(fe, cell, "IFS(\"abc\",\"a\")", FormulaError.VALUE);
            assertError(fe, cell, "IFS(NA(),\"a\")", FormulaError.NA);
        }
    }

    @Test
    void testPoissonCumulativeFlag() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = formulaCell(wb);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertDouble(fe, cell, "POISSON(2,3,TRUE)", 0.423190081126869, 1E-14);
            assertDouble(fe, cell, "POISSON(2,3,1)", 0.423190081126869, 1E-14);
            assertDouble(fe, cell, "POISSON(2,3,0)", 0.224041807655388, 1E-14);
            assertDouble(fe, cell, "POISSON.DIST(2,3,1)", 0.423190081126869, 1E-14);
            assertError(fe, cell, "POISSON(2,3,\"abc\")", FormulaError.VALUE);
            assertError(fe, cell, "POISSON(2,3,NA())", FormulaError.NA);
        }
    }

    @Test
    void testMatrixFunctionsWithAScalarFirstArgument() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = formulaCell(wb);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            // 1x1 times 1x3 is 1x3; a plain cell shows its first element rather than throwing
            assertDouble(fe, cell, "MMULT(2,{1,2,3})", 2, 0);
            assertDouble(fe, cell, "MMULT(2,3)", 6, 0);
        }
    }

    @Test
    void testScalarArgumentsGivenAsAreaOrArray() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = formulaCell(wb);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            // a one-element array is its value
            assertDouble(fe, cell, "BIN2DEC({101})", 5, 0);
            assertDouble(fe, cell, "HEX2DEC({\"FF\"})", 255, 0);
            assertDouble(fe, cell, "OCT2DEC({\"17\"})", 15, 0);
            assertDouble(fe, cell, "FACTDOUBLE({5})", 15, 0);
            assertString(fe, cell, "COMPLEX(1,1,{\"j\"})", "1+j");
            assertDouble(fe, cell, "WORKDAY(1,{5})", 8, 0);
            assertDouble(fe, cell, "WORKDAY.INTL(1,{5})", 8, 0);
            // a reference to one cell is its value
            assertDouble(fe, cell, "BIN2DEC(A1)", 5, 0);
            assertDouble(fe, cell, "FACTDOUBLE(B1)", 15, 0);
            // a multi-cell area that does not intersect the formula's row is #VALUE!
            assertError(fe, cell, "BIN2DEC(A1:A2)", FormulaError.VALUE);
            assertError(fe, cell, "HEX2DEC(A1:A2)", FormulaError.VALUE);
            assertError(fe, cell, "OCT2DEC(A1:A2)", FormulaError.VALUE);
            assertError(fe, cell, "FACTDOUBLE(A1:A2)", FormulaError.VALUE);
            assertError(fe, cell, "COMPLEX(1,1,A1:A2)", FormulaError.VALUE);
            assertError(fe, cell, "WORKDAY(1,A1:A2)", FormulaError.VALUE);
            assertError(fe, cell, "WORKDAY.INTL(1,A1:A2)", FormulaError.VALUE);
            // an error argument is passed through
            assertError(fe, cell, "FACTDOUBLE(NA())", FormulaError.NA);
            assertError(fe, cell, "BIN2DEC(NA())", FormulaError.NA);
        }
    }
}
