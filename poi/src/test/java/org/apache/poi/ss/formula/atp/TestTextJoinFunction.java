
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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    private Cell textCell3;
    private Cell numericCell1;
    private Cell numericCell2;
    private Cell blankCell;
    private Cell emptyCell;
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

}
