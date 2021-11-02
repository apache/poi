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

package org.apache.poi.ss.tests.formula.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.TextFunction;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

public final class TestProper {
    private Cell cell11;
    private FormulaEvaluator evaluator;

    @Test
    void testValidHSSF() {
        HSSFWorkbook wb = new HSSFWorkbook();
        evaluator = new HSSFFormulaEvaluator(wb);

        confirm(wb);
    }

    @Test
    void testValidXSSF() {
        XSSFWorkbook wb = new XSSFWorkbook();
        evaluator = new XSSFFormulaEvaluator(wb);

        confirm(wb);
    }

    private void confirm(Workbook wb) {
        Sheet sheet = wb.createSheet("new sheet");
        cell11 = sheet.createRow(0).createCell(0);

        confirm("PROPER(\"hi there\")", "Hi There"); //simple case
        confirm("PROPER(\"what's up\")", "What'S Up"); //apostrophes are treated as word breaks
        confirm("PROPER(\"I DON'T TH!NK SO!\")", "I Don'T Th!Nk So!"); //capitalization is ignored, special punctuation is treated as a word break
        confirm("PROPER(\"dr\u00dcb\u00f6'\u00e4 \u00e9lo\u015f|\u00eb\u00e8 \")", "Dr\u00fcb\u00f6'\u00c4 \u00c9lo\u015f|\u00cb\u00e8 ");
        confirm("PROPER(\"hi123 the123re\")", "Hi123 The123Re"); //numbers are treated as word breaks
        confirm("PROPER(\"-\")", "-"); //nothing happens with ascii punctuation that is not upper or lower case
        confirm("PROPER(\"!\u00a7$\")", "!\u00a7$"); //nothing happens with unicode punctuation (section sign) that is not upper or lower case
        confirm("PROPER(\"/&%\")", "/&%"); //nothing happens with ascii punctuation that is not upper or lower case
        confirm("PROPER(\"Apache POI\")", "Apache Poi"); //acronyms are not special
        confirm("PROPER(\"  hello world\")", "  Hello World"); //leading whitespace is ignored

        final String scharfes = "\u00df"; //German lowercase eszett, scharfes s, sharp s
        confirm("PROPER(\"stra"+scharfes+"e\")", "Stra"+scharfes+"e");
        assertTrue(Character.isLetter(scharfes.charAt(0)));

        // CURRENTLY FAILS: result: "SSUnd"+scharfes
        // LibreOffice 5.0.3.2 behavior: "Sund"+scharfes
        // Excel 2013 behavior: ???
        confirm("PROPER(\""+scharfes+"und"+scharfes+"\")", "SSund"+scharfes);

        // also test longer string
        StringBuilder builder = new StringBuilder("A");
        StringBuilder expected = new StringBuilder("A");
        for(int i = 1;i < 254;i++) {
            builder.append((char)(65 + (i % 26)));
            expected.append((char)(97 + (i % 26)));
        }
        confirm("PROPER(\"" + builder + "\")", expected.toString());
    }

    private void confirm(String formulaText, String expectedResult) {
        cell11.setCellFormula(formulaText);
        evaluator.clearAllCachedResultValues();
        CellValue cv = evaluator.evaluate(cell11);
        assertEquals(CellType.STRING, cv.getCellType(), "Wrong result type");
        String actualValue = cv.getStringValue();
        assertEquals(expectedResult, actualValue);
    }

    @Test
    void test() {
        checkProper("", "");
        checkProper("a", "A");
        checkProper("abc", "Abc");
        checkProper("abc abc", "Abc Abc");
        checkProper("abc/abc", "Abc/Abc");
        checkProper("ABC/ABC", "Abc/Abc");
        checkProper("aBc/ABC", "Abc/Abc");
        checkProper("aBc@#$%^&*()_+=-ABC", "Abc@#$%^&*()_+=-Abc");
        checkProper("aBc25aerg/ABC", "Abc25Aerg/Abc");
        checkProper("aBc/\u00C4\u00F6\u00DF\u00FC/ABC", "Abc/\u00C4\u00F6\u00DF\u00FC/Abc");  // Some German umlauts with uppercase first letter is not changed
        checkProper("\u00FC", "\u00DC");
        checkProper("\u00DC", "\u00DC");
        checkProper("\u00DF", "SS");    // German "scharfes s" is uppercased to "SS"
        checkProper("\u00DFomesing", "SSomesing");    // German "scharfes s" is uppercased to "SS"
        checkProper("aBc/\u00FC\u00C4\u00F6\u00DF\u00FC/ABC", "Abc/\u00DC\u00E4\u00F6\u00DF\u00FC/Abc");  // Some German umlauts with lowercase first letter is changed to uppercase
    }

    @Test
    void testMicroBenchmark() {
        ValueEval strArg = new StringEval("some longer text that needs a number of replacements to check for runtime of different implementations");
        // long start = System.currentTimeMillis();
        for(int i = 0;i < 300000;i++) {
            final ValueEval ret = TextFunction.PROPER.evaluate(new ValueEval[]{strArg}, 0, 0);
            assertEquals("Some Longer Text That Needs A Number Of Replacements To Check For Runtime Of Different Implementations", ((StringEval)ret).getStringValue());
        }
        // Took approx. 600ms on a decent Laptop in July 2016
        //System.out.println("Took: " + (System.currentTimeMillis() - start) + "ms");
    }

    private void checkProper(String input, String expected) {
        ValueEval strArg = new StringEval(input);
        final ValueEval ret = TextFunction.PROPER.evaluate(new ValueEval[]{strArg}, 0, 0);
        assertEquals(expected, ((StringEval)ret).getStringValue());
    }
}
