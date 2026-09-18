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
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.io.IOException;
import java.util.Locale;

import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.apache.poi.ss.util.Utils.assertString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.apache.poi.ss.usermodel.FormulaError;
import static org.apache.poi.ss.util.Utils.assertError;

@Isolated // modifies the default locale and we don't want to affect other tests running in parallel
final class TestNumericFunction {

    @Test
    void testINT() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        assertDouble(fe, cell, "INT(880000000.0001)", 880000000.0, 0);
        //the following INT(-880000000.0001) resulting in -880000001.0 has been observed in excel
        //see also https://support.microsoft.com/en-us/office/int-function-a6c4af9e-356d-4369-ab6a-cb1fd9d343ef
        assertDouble(fe, cell, "INT(-880000000.0001)", -880000001.0, 0);
        // bug 65792: Excel calculates in IEEE 754 double precision (880000000*0.00849 is
        // 7471199.999999999) and displays 15 significant digits, so the product shows as
        // 7471200 and INT acts on that 15-digit view
        assertDouble(fe, cell, "880000000*0.00849", 7471199.999999999, 0);
        assertEquals("7471200", NumberToTextConverter.toText(7471199.999999999));
        assertDouble(fe, cell, "880000000*0.00849/3", 2490399.9999999995, 0);
        assertEquals("2490400", NumberToTextConverter.toText(2490399.9999999995));
        assertDouble(fe, cell, "INT(880000000*0.00849/3)", 2490400.0, 0);
        assertDouble(fe, cell, "INT(2490399.9999999995)", 2490400.0, 0);
        // but values that are exactly representable are not approximated
        assertDouble(fe, cell, "INT(2490399.5)", 2490399.0, 0);
        assertDouble(fe, cell, "INT(1E+20)", 1E+20, 0);
    }

    @Test
    void testMultiply() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            // plain IEEE 754 multiplication, as in Excel; Excel displays the 15-digit view
            assertDouble(fe, cell, "1.2*SQRT(5.678)", 2.859426515929374, 0);
            assertEquals("2.85942651592937", NumberToTextConverter.toText(2.859426515929374));
        }
    }

    @Test
    void testSIGN() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        //https://support.microsoft.com/en-us/office/sign-function-109c932d-fcdc-4023-91f1-2dd0e916a1d8
        assertDouble(fe, cell, "SIGN(10)", 1.0, 0);
        assertDouble(fe, cell, "SIGN(4-4)", 0.0, 0);
        assertDouble(fe, cell, "SIGN(-0.00001)", -1.0, 0);
    }

    @Test
    void testDOLLAR() {
        Locale defaultLocale = LocaleUtil.getUserLocale();
        try {
            LocaleUtil.setUserLocale(new Locale.Builder().setLanguage("en").setRegion("US").build());
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            //https://support.microsoft.com/en-us/office/dollar-function-a6cd05d9-9740-4ad3-a469-8109d18ff611
            assertString(fe, cell, "DOLLAR(1234.567,2)", "$1,234.57");
            assertString(fe, cell, "DOLLAR(-1234.567,0)", "($1,235)");
            assertString(fe, cell, "DOLLAR(-1234.567,-2)", "($1,200)");
            assertString(fe, cell, "DOLLAR(-0.123,4)", "($0.1230)");
            assertString(fe, cell, "DOLLAR(99.888)", "$99.89");
            assertString(fe, cell, "DOLLAR(123456789.567,2)", "$123,456,789.57");
        } finally {
            LocaleUtil.setUserLocale(defaultLocale);
        }
    }

    @Test
    void testDOLLARIreland() {
        Locale defaultLocale = LocaleUtil.getUserLocale();
        try {
            LocaleUtil.setUserLocale(new Locale.Builder().setLanguage("en").setRegion("IE").build());
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertString(fe, cell, "DOLLAR(1234.567,2)", "€1,234.57");
            assertString(fe, cell, "DOLLAR(-1234.567,2)", "-€1,234.57");
        } finally {
            LocaleUtil.setUserLocale(defaultLocale);
        }
    }

    @Test
    void testDOLLARSpain() {
        Locale defaultLocale = LocaleUtil.getUserLocale();
        try {
            LocaleUtil.setUserLocale(new Locale.Builder().setLanguage("es").setRegion("ES").build());
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertString(fe, cell, "DOLLAR(1234.567,2)", "1.234,57 €");
            assertString(fe, cell, "DOLLAR(-1234.567,2)", "-1.234,57 €");
        } finally {
            LocaleUtil.setUserLocale(defaultLocale);
        }
    }

    @Test
    void testDOLLARJapan() {
        Locale defaultLocale = LocaleUtil.getUserLocale();
        try {
            LocaleUtil.setUserLocale(new Locale.Builder().setLanguage("ja").setRegion("JP").build());
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertString(fe, cell, "DOLLAR(1234.567,2)", "￥1,234.57");
            assertString(fe, cell, "DOLLAR(-1234.567,2)", "-￥1,234.57");
        } finally {
            LocaleUtil.setUserLocale(defaultLocale);
        }
    }

    @Test
    @Disabled("fails on some Java Runtimes (kr apprears at start of result in some JREs)")
    void testDOLLARDenmark() {
        Locale defaultLocale = LocaleUtil.getUserLocale();
        try {
            LocaleUtil.setUserLocale(new Locale.Builder().setLanguage("da").setRegion("DK").build());
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            assertString(fe, cell, "DOLLAR(1234.567,2)", "1.234,57 kr.");
            assertString(fe, cell, "DOLLAR(-1234.567,2)", "-1.234,57 kr.");
        } finally {
            LocaleUtil.setUserLocale(defaultLocale);
        }
    }

    @Test
    void testINTMicrosoftExamples() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        //https://support.microsoft.com/en-us/office/int-function-a6c4af9e-356d-4369-ab6a-cb1fd9d343ef
        assertDouble(fe, cell, "INT(8.9)", 8.0, 0);
        assertDouble(fe, cell, "INT(-8.9)", -9.0, 0);
        assertDouble(fe, cell, "19.5-INT(19.5)", 0.5, 0);
    }

    @Test
    void testINTActsOnExcelsFifteenDigitView() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        // just below an integer in binary, but the integer at 15 significant digits
        assertDouble(fe, cell, "INT(2.9999999999999996)", 3.0, 0);
        assertDouble(fe, cell, "INT(-2.9999999999999996)", -3.0, 0);
        assertDouble(fe, cell, "INT(9.999999999999999)", 10.0, 0);
        assertDouble(fe, cell, "INT(0.7/0.1)", 7.0, 0);
        assertDouble(fe, cell, "INT(0.1*3*10)", 3.0, 0);
        assertDouble(fe, cell, "INT(4.35*100)", 435.0, 0);
        assertDouble(fe, cell, "INT(1.005*1000)", 1005.0, 0);
        // just above an integer stays that integer
        assertDouble(fe, cell, "INT(3.0000000000000004)", 3.0, 0);
        assertDouble(fe, cell, "INT(-3.0000000000000004)", -3.0, 0);
        // values that differ from an integer within 15 significant digits are truncated normally
        assertDouble(fe, cell, "INT(2.99999999999999)", 2.0, 0);
        assertDouble(fe, cell, "INT(-2.99999999999999)", -3.0, 0);
        assertDouble(fe, cell, "INT(0.999999999999999)", 0.0, 0);
        // exact binary fractions are never approximated
        assertDouble(fe, cell, "INT(2.5)", 2.0, 0);
        assertDouble(fe, cell, "INT(-2.5)", -3.0, 0);
        assertDouble(fe, cell, "INT(0.0625)", 0.0, 0);
        assertDouble(fe, cell, "INT(1E15+0.5)", 1E15, 0);
        assertDouble(fe, cell, "INT(-1E15-0.5)", -1E15 - 1, 0);
    }

    @Test
    void testINTLargeAndSpecialValues() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        // integers beyond the precision of a double pass through unchanged (no clamping at 2^63)
        assertDouble(fe, cell, "INT(1E20)", 1E20, 0);
        assertDouble(fe, cell, "INT(-1E20)", -1E20, 0);
        assertDouble(fe, cell, "INT(1E308)", 1E308, 0);
        assertDouble(fe, cell, "INT(2^53)", Math.pow(2, 53), 0);
        assertDouble(fe, cell, "INT(0)", 0.0, 0);
        assertDouble(fe, cell, "INT(-0.5)", -1.0, 0);
        assertDouble(fe, cell, "INT(1E-300)", 0.0, 0);
        assertDouble(fe, cell, "INT(-1E-300)", -1.0, 0);
        // coercion follows the usual rules
        assertDouble(fe, cell, "INT(TRUE)", 1.0, 0);
        assertDouble(fe, cell, "INT(\"3.7\")", 3.0, 0);
        assertError(fe, cell, "INT(\"abc\")", FormulaError.VALUE);
        assertError(fe, cell, "INT(1/0)", FormulaError.DIV0);
    }

    @Test
    void testFACTOutOfIntRange() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        assertDouble(fe, cell, "FACT(170)", 7.257415615307994E306, 0);
        assertDouble(fe, cell, "FACT(170.9)", 7.257415615307994E306, 0);
        assertDouble(fe, cell, "FACT(-0.5)", 1.0, 0);
        assertError(fe, cell, "FACT(171)", FormulaError.NUM);
        assertError(fe, cell, "FACT(-1)", FormulaError.NUM);
        // used to escape as an IllegalArgumentException from MathUtil.safeDoubleToInt
        assertError(fe, cell, "FACT(1E10)", FormulaError.NUM);
        assertError(fe, cell, "FACT(1E308)", FormulaError.NUM);
        assertError(fe, cell, "FACT(-1E10)", FormulaError.NUM);
    }

    @Test
    void testROUNDWithHugeDigitCount() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        // digit counts beyond the int range used to escape as an IllegalArgumentException,
        // and counts within it but in the hundreds of millions made BigDecimal.setScale run for minutes
        for (String digits : new String[] {"400", "1000", "100000000", "2147483647", "1E10", "1E308"}) {
            assertDouble(fe, cell, "ROUND(1.5," + digits + ")", 1.5, 0);
            assertDouble(fe, cell, "ROUNDUP(1.5," + digits + ")", 1.5, 0);
            assertDouble(fe, cell, "ROUNDDOWN(1.5," + digits + ")", 1.5, 0);
            assertDouble(fe, cell, "TRUNC(1.5," + digits + ")", 1.5, 0);
            assertDouble(fe, cell, "ROUND(1.5,-" + digits + ")", 0.0, 0);
            assertDouble(fe, cell, "ROUNDDOWN(1.5,-" + digits + ")", 0.0, 0);
            assertDouble(fe, cell, "TRUNC(1.5,-" + digits + ")", 0.0, 0);
            assertError(fe, cell, "ROUNDUP(1.5,-" + digits + ")", FormulaError.NUM);
        }
        // the extremes of Excel's number range are still rounded correctly (Excel has no subnormals)
        assertDouble(fe, cell, "ROUND(2.3E-308,400)", 2.3E-308, 0);
        assertDouble(fe, cell, "ROUND(2.3E-308,307)", 0.0, 0);
        assertDouble(fe, cell, "ROUND(1.5E308,-307)", 1.5E308, 0);
        assertDouble(fe, cell, "ROUND(1.5E308,-309)", 0.0, 0);
    }
}
