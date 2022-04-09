/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.formula.functions;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.apache.poi.ss.util.Utils.addRow;
import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.apache.poi.ss.util.Utils.assertError;

/**
 * Test cases for RATE()
 */
final class TestRate {

    @Test
    void testMicrosoftExample1() throws Exception {
        //https://support.microsoft.com/en-us/office/rate-function-9f665657-4a7e-4bb7-a030-83fc59e748ce
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            addRow(sheet, 0, "Data", "Description");
            addRow(sheet, 1, 4, "Years of loan");
            addRow(sheet, 2, -200, "Monthly payment");
            addRow(sheet, 3, 8000, "Amount of the loan");
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = sheet.getRow(0).createCell(100);
            assertDouble(fe, cell, "RATE(A2*12, A3, A4)", 0.007701472, 0.000001);
            assertDouble(fe, cell, "RATE(A2*12, A3, A4)*12", 0.09241767, 0.000001);
        }
    }

    // https://wiki.documentfoundation.org/Documentation/Calc_Functions/RATE
    @Test
    void testLibreOfficeExample1() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = row.createCell(0);
            assertDouble(fe, cell, "RATE(3,-10,900,1,0,0.5)", -0.7634, 0.0001);
        }
    }

    // https://wiki.documentfoundation.org/Documentation/Calc_Functions/RATE
    @Test
    void testLibreOfficeExample2() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = row.createCell(0);
            assertDouble(fe, cell, "RATE(3,-10,900)", -0.7563, 0.0001);
        }
    }

    /**
     * LibreOffice and Excel return #!NUM, but POI and NumPy return a result
     */
    // https://wiki.documentfoundation.org/Documentation/Calc_Functions/RATE
    @Disabled("test fails - see related issue")
    @Test
    void testLibreOfficeInfeasibleSolution() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = row.createCell(0);
            assertDouble(fe, cell, "RATE(3,10,900,1,0,0.5)", -0.7634, 0.0001);
        }
    }

    /**
     * See https://github.com/numpy/numpy-financial/blob/d02edfb65dcdf23bd571c2cded7fcd4a0528c6af/numpy_financial/tests/test_financial.py#L126
     */
    @Test
    void testNumPyExample1() throws Exception {
        double[] expected = {-0.39920185, -0.02305873, -0.41818459, 0.26513414};
        double nper = 2;
        double pmt = 0;
        double[] pv = {-593.06, -4725.38, -662.05, -428.78};
        double[] fv = {214.07, 4509.97, 224.11, 686.29};
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            for(int i = 0; i < pv.length; i++) {
                String fmla = String.format(Locale.ROOT, "RATE(%2f, %2f, %2f, %2f, 0, 0.1)", nper, pmt, pv[i], fv[i]);
                HSSFCell cell = row.createCell(i);
                assertDouble(fe, cell, fmla, expected[i], 1e-8);
            }
        }
    }

    @Test
    void testNumPyExample2() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = row.createCell(0);
            assertDouble(fe, cell, "RATE(10, 0, -3500, 10000.0, 0, 0.1)", 0.1106908537142689284704528100, 1E-6);
        }
    }

    /**
     * RATE will return NaN, if the Newton Raphson method cannot find a
     * feasible rate within the required tolerance or number of iterations.
     * This can occur if both `pmt` and `pv` have the same sign, as it is
     * impossible to repay a loan by making further withdrawals.
     *
     * See https://github.com/numpy/numpy-financial/blob/d02edfb65dcdf23bd571c2cded7fcd4a0528c6af/numpy_financial/tests/test_financial.py#L113
     */
    @Test
    void testNumPyInfeasibleSolution1() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = row.createCell(0);
            assertError(fe, cell, "RATE(12, 400, 10000, 5000.0, 0, 0.1)", FormulaError.NUM);
        }
    }

    // https://github.com/numpy/numpy-financial/blob/d02edfb65dcdf23bd571c2cded7fcd4a0528c6af/numpy_financial/tests/test_financial.py#L126
    @Test
    void testNumPyInfeasibleSolution2() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = row.createCell(0);
            assertError(fe, cell, "RATE(2, 0, -13.65, -329.67, 0, 0.1)", FormulaError.NUM);
        }
    }

    @Test
    void testBug65988() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = row.createCell(0);
            assertDouble(fe, cell, "RATE(360.0,6.56,-2000.0)", 0.0009480170844060, 0.000001);
        }
    }
}
