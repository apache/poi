
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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.addRow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testcase for function TEXTJOIN()
 */
public class TestTextJoinFunction {

    private Workbook wb;
    private Sheet sheet;
    private FormulaEvaluator evaluator;
    private Cell textCell1;
    private Cell textCell2;
    private Cell numericCell1;
    private Cell numericCell2;
    private Cell blankCell;
    private Cell formulaCell;

    @BeforeEach
    public void setUp() throws Exception {
        wb = new HSSFWorkbook();
        evaluator = wb.getCreationHelper().createFormulaEvaluator();

        sheet = wb.createSheet("TextJoin");
        Row row = sheet.createRow(0);

        textCell1 = row.createCell(0);
        textCell1.setCellValue("One");

        textCell2 = row.createCell(1);
        textCell2.setCellValue("Two");

        blankCell = row.createCell(2);
        blankCell.setBlank();

        numericCell1 = row.createCell(3);
        numericCell1.setCellValue(1);

        numericCell2 = row.createCell(4);
        numericCell2.setCellValue(2);

        formulaCell = row.createCell(100, CellType.FORMULA);
    }

    @Test
    public void testJoinSingleLiteralText() {
        evaluator.clearAllCachedResultValues();
        formulaCell.setCellFormula("TEXTJOIN(\",\", true, \"Text\")");
        evaluator.evaluateFormulaCell(formulaCell);
        assertEquals("Text", formulaCell.getStringCellValue());
    }

    @Test
    public void testJoinMultipleLiteralText() {
        evaluator.clearAllCachedResultValues();
        formulaCell.setCellFormula("TEXTJOIN(\",\", true, \"One\", \"Two\", \"Three\")");
        evaluator.evaluateFormulaCell(formulaCell);
        assertEquals("One,Two,Three", formulaCell.getStringCellValue());
    }

    @Test
    public void testJoinLiteralTextAndNumber() {
        evaluator.clearAllCachedResultValues();
        formulaCell.setCellFormula("TEXTJOIN(\",\", true, \"Text\", 1)");
        evaluator.evaluateFormulaCell(formulaCell);
        assertEquals("Text,1", formulaCell.getStringCellValue());
    }

    @Test
    public void testJoinEmptyStringIncludeEmpty() {
        evaluator.clearAllCachedResultValues();
        formulaCell.setCellFormula("TEXTJOIN(\",\", false, \"A\", \"\", \"B\")");
        evaluator.evaluateFormulaCell(formulaCell);
        assertEquals("A,,B", formulaCell.getStringCellValue());
    }

    @Test
    public void testJoinEmptyStringIgnoreEmpty() {
        evaluator.clearAllCachedResultValues();
        formulaCell.setCellFormula("TEXTJOIN(\",\", true, \"A\", \"\", \"B\")");
        evaluator.evaluateFormulaCell(formulaCell);
        assertEquals("A,B", formulaCell.getStringCellValue());
    }

    @Test
    public void testJoinEmptyStringsIncludeEmpty() {
        evaluator.clearAllCachedResultValues();
        formulaCell.setCellFormula("TEXTJOIN(\",\", false, \"\", \"\")");
        evaluator.evaluateFormulaCell(formulaCell);
        assertEquals(",", formulaCell.getStringCellValue());
    }

    @Test
    public void testJoinEmptyStringsIgnoreEmpty() {
        evaluator.clearAllCachedResultValues();
        formulaCell.setCellFormula("TEXTJOIN(\",\", true, \"\", \"\")");
        evaluator.evaluateFormulaCell(formulaCell);
        assertEquals("", formulaCell.getStringCellValue());
    }

    @Test
    public void testJoinTextCellValues() {
        evaluator.clearAllCachedResultValues();
        formulaCell.setCellFormula("TEXTJOIN(\",\", true, A1, B1)");
        evaluator.evaluateFormulaCell(formulaCell);
        assertEquals("One,Two", formulaCell.getStringCellValue());
    }

    @Test
    public void testJoinNumericCellValues() {
        evaluator.clearAllCachedResultValues();
        formulaCell.setCellFormula("TEXTJOIN(\",\", true, D1, E1)");
        evaluator.evaluateFormulaCell(formulaCell);
        assertEquals("1,2", formulaCell.getStringCellValue());
    }

    @Test
    public void testJoinBlankCellIncludeEmpty() {
        evaluator.clearAllCachedResultValues();
        formulaCell.setCellFormula("TEXTJOIN(\",\", false, A1, C1, B1)");
        evaluator.evaluateFormulaCell(formulaCell);
        assertEquals("One,,Two", formulaCell.getStringCellValue());
    }

    @Test
    public void testJoinBlankCellIgnoreEmpty() {
        evaluator.clearAllCachedResultValues();
        formulaCell.setCellFormula("TEXTJOIN(\",\", true, A1, C1, B1)");
        evaluator.evaluateFormulaCell(formulaCell);
        assertEquals("One,Two", formulaCell.getStringCellValue());
    }

    @Test
    public void testNoTextArgument() {
        evaluator.clearAllCachedResultValues();
        formulaCell.setCellFormula("TEXTJOIN(\",\", true)");
        evaluator.evaluateFormulaCell(formulaCell);
        assertEquals(CellType.ERROR, formulaCell.getCachedFormulaResultType());
        assertEquals(ErrorEval.VALUE_INVALID.getErrorCode(), formulaCell.getErrorCellValue());
    }

    //https://support.microsoft.com/en-us/office/textjoin-function-357b449a-ec91-49d0-80c3-0e8fc845691c
    @Test
    void testMicrosoftExample1() throws IOException {
        try (HSSFWorkbook wb = initWorkbook1()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            confirmResult(fe, cell, "TEXTJOIN(\", \", TRUE, A2:A8)",
                    "US Dollar, Australian Dollar, Chinese Yuan, Hong Kong Dollar, Israeli Shekel, South Korean Won, Russian Ruble");
        }
    }

    @Test
    void testMicrosoftExample2() throws IOException {
        try (HSSFWorkbook wb = initWorkbook2()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            confirmResult(fe, cell, "TEXTJOIN(\", \", TRUE, A2:B8)",
                    "a1, b1, a2, b2, a4, b4, a5, b5, a6, b6, a7, b7");
            confirmResult(fe, cell, "TEXTJOIN(\", \", FALSE, A2:B8)",
                    "a1, b1, a2, b2, , , a4, b4, a5, b5, a6, b6, a7, b7");
        }
    }

    @Test
    void testMicrosoftExample3() throws IOException {
        try (HSSFWorkbook wb = initWorkbook3()) {
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            confirmResult(fe, cell, "TEXTJOIN(A8:D8, TRUE, A2:D7)",
                    "Tulsa,OK,74133,US;Seattle,WA,98109,US;Iselin,NJ,08830,US;Fort Lauderdale,FL,33309,US;Tempe,AZ,85285,US;end");
            confirmResult(fe, cell, "TEXTJOIN(, TRUE, A2:D7)",
                    "TulsaOK74133USSeattleWA98109USIselinNJ08830USFort LauderdaleFL33309USTempeAZ85285USend");
        }
    }

    private HSSFWorkbook initWorkbook1() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, "Currency");
        addRow(sheet, 1, "US Dollar");
        addRow(sheet, 2, "Australian Dollar");
        addRow(sheet, 3, "Chinese Yuan");
        addRow(sheet, 4, "Hong Kong Dollar");
        addRow(sheet, 5, "Israeli Shekel");
        addRow(sheet, 6, "South Korean Won");
        addRow(sheet, 7, "Russian Ruble");
        return wb;
    }

    private HSSFWorkbook initWorkbook2() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, "A’s", "B’s");
        for (int i = 1; i <= 7; i++) {
            if (i != 3) {
                addRow(sheet, i, "a" + i, "b" + i);
            }
        }
        return wb;
    }

    private HSSFWorkbook initWorkbook3() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        addRow(sheet, 0, "City", "State", "Postcode", "Country");
        addRow(sheet, 1, "Tulsa", "OK", "74133", "US");
        addRow(sheet, 2, "Seattle", "WA", "98109", "US");
        addRow(sheet, 3, "Iselin", "NJ", "08830", "US");
        addRow(sheet, 4, "Fort Lauderdale", "FL", "33309", "US");
        addRow(sheet, 5, "Tempe", "AZ", "85285", "US");
        addRow(sheet, 6, "end");
        addRow(sheet, 7, ",", ",", ",", ";");
        return wb;
    }

    private static void confirmResult(HSSFFormulaEvaluator fe, HSSFCell cell, String formulaText, String expectedResult) {
        cell.setCellFormula(formulaText);
        fe.notifyUpdateCell(cell);
        CellValue result = fe.evaluate(cell);
        assertEquals(CellType.STRING, result.getCellType());
        assertEquals(expectedResult, result.getStringValue());
    }
}
