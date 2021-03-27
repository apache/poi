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

package org.apache.poi.ss.tests.usermodel;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Date;
import java.util.List;

import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.formula.EvaluationConditionalFormatRule;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestConditionalFormattingEval {

    private XSSFWorkbook wb;
    private Sheet sheet;
    private XSSFFormulaEvaluator formulaEval;
    private ConditionalFormattingEvaluator cfe;
    private CellReference ref;
    private List<EvaluationConditionalFormatRule> rules;

    @BeforeEach
    void openWB() {
        wb = XSSFTestDataSamples.openSampleWorkbook("ConditionalFormattingSamples.xlsx");
        formulaEval = new XSSFFormulaEvaluator(wb);
        cfe = new ConditionalFormattingEvaluator(wb, formulaEval);
    }

    @AfterEach
    void closeWB() {
        formulaEval = null;
        cfe = null;
        ref = null;
        rules = null;
        IOUtils.closeQuietly(wb);
    }

    @Test
    void testFormattingEvaluation() {
        sheet = wb.getSheet("Products1");

        getRulesFor(12, 1);
        assertEquals(1, rules.size(), "wrong # of rules for " + ref);
        assertEquals("FFFFEB9C", getColor(rules.get(0).getRule().getPatternFormatting().getFillBackgroundColorColor()), "wrong bg color for " + ref);
        assertFalse(rules.get(0).getRule().getFontFormatting().isItalic(), "should not be italic " + ref);

        getRulesFor(16, 3);
        assertEquals(1, rules.size(), "wrong # of rules for " + ref);
        assertEquals(0.7999816888943144d, getTint(rules.get(0).getRule().getPatternFormatting().getFillBackgroundColorColor()), 0.000000000000001, "wrong bg color for " + ref);

        getRulesFor(12, 3);
        assertEquals(0, rules.size(), "wrong # of rules for " + ref);

        sheet = wb.getSheet("Products2");

        getRulesFor(15,1);
        assertEquals(1, rules.size(), "wrong # of rules for " + ref);
        assertEquals("FFFFEB9C", getColor(rules.get(0).getRule().getPatternFormatting().getFillBackgroundColorColor()), "wrong bg color for " + ref);

        getRulesFor(20,3);
        assertEquals(0, rules.size(), "wrong # of rules for " + ref);

        // now change a cell value that's an input for the rules
        Cell cell = sheet.getRow(1).getCell(6);
        cell.setCellValue("Dairy");
        formulaEval.notifyUpdateCell(cell);
        cell = sheet.getRow(4).getCell(6);
        cell.setCellValue(500);
        formulaEval.notifyUpdateCell(cell);
        // need to throw away all evaluations, since we don't know how value changes may have affected format formulas
        cfe.clearAllCachedValues();

        // test that the conditional validation evaluations changed
        getRulesFor(15,1);
        assertEquals(0, rules.size(), "wrong # of rules for " + ref);

        getRulesFor(20,3);
        assertEquals(1, rules.size(), "wrong # of rules for " + ref);
        assertEquals(0.7999816888943144d, getTint(rules.get(0).getRule().getPatternFormatting().getFillBackgroundColorColor()), 0.000000000000001, "wrong bg color for " + ref);

        getRulesFor(20,1);
        assertEquals(1, rules.size(), "wrong # of rules for " + ref);
        assertEquals("FFFFEB9C", getColor(rules.get(0).getRule().getPatternFormatting().getFillBackgroundColorColor()), "wrong bg color for " + ref);

        sheet = wb.getSheet("Book tour");

        getRulesFor(8,2);
        assertEquals(1, rules.size(), "wrong # of rules for " + ref);

        sheet = wb.getSheet("Compare to totals");
        getRulesFor(3, 2);
        assertEquals(1, rules.size(), "wrong # of rules for " + ref);
        assertEquals("FFFF0000", getColor(rules.get(0).getRule().getFontFormatting().getFontColor()), "wrong fg color for " + ref);
        getRulesFor(3, 3);
        assertEquals(0, rules.size(), "wrong # of rules for " + ref);
        getRulesFor(15, 4);
        assertEquals(0, rules.size(), "wrong # of rules for " + ref);
        getRulesFor(16, 1);
        assertEquals(1, rules.size(), "wrong # of rules for " + ref);
        assertEquals("FFFF0000", getColor(rules.get(0).getRule().getFontFormatting().getFontColor()), "wrong fg color for " + ref);

        sheet = wb.getSheet("Products3");
        sheet.getRow(8).getCell(0).setCellValue(new Date());
        getRulesFor(8, 0);
        assertEquals(1, rules.size(), "wrong # of rules for " + ref);
        getRulesFor(8, 3);
        assertEquals(1, rules.size(), "wrong # of rules for " + ref);

        sheet = wb.getSheet("Customers2");
        getRulesFor(3, 0);
        assertEquals(0, rules.size(), "wrong # of rules for " + ref);
    }

    @Test
    void testFormattingOnUndefinedCell() {
        wb = XSSFTestDataSamples.openSampleWorkbook("conditional_formatting_with_formula_on_second_sheet.xlsx");
        formulaEval = new XSSFFormulaEvaluator(wb);
        cfe = new ConditionalFormattingEvaluator(wb, formulaEval);

        sheet = wb.getSheet("Sales Plan");
        getRulesFor(9,2);
        assertNotEquals(0, rules.size(), "No rules for " + ref);
        assertEquals("FFFFFF00", getColor(rules.get(0).getRule().getPatternFormatting().getFillBackgroundColorColor()), "wrong bg color for " + ref);
    }

    @Test
    void testRepeatedEval() {
        wb = XSSFTestDataSamples.openSampleWorkbook("test_conditional_formatting.xlsx");
        formulaEval = new XSSFFormulaEvaluator(wb);
        cfe = new ConditionalFormattingEvaluator(wb, formulaEval);

        sheet = wb.getSheetAt(0);
        assertEquals(0, getRulesFor(2, 1).size(), "no rules should apply");

        assertEquals(0, getRulesFor(2, 1).size(), "no rules should apply");

    }

    @Test
    void testCellValueIsWrongType() {
        wb = XSSFTestDataSamples.openSampleWorkbook("conditional_formatting_cell_is.xlsx");
        formulaEval = new XSSFFormulaEvaluator(wb);
        cfe = new ConditionalFormattingEvaluator(wb, formulaEval);

        sheet = wb.getSheetAt(1);

        assertEquals(1, getRulesFor(3, 1).size(), "wrong # of matching rules");
    }

    @Test
    void testRangeCondition() {
        wb = XSSFTestDataSamples.openSampleWorkbook("conditional_formatting_multiple_ranges.xlsx");
        formulaEval = new XSSFFormulaEvaluator(wb);
        cfe = new ConditionalFormattingEvaluator(wb, formulaEval);

        sheet = wb.getSheetAt(0);

        assertEquals(0, getRulesFor(0, 0).size(), "wrong # of matching rules");
        assertEquals(0, getRulesFor(1, 0).size(), "wrong # of matching rules");
        assertEquals(0, getRulesFor(2, 0).size(), "wrong # of matching rules");
        assertEquals(1, getRulesFor(3, 0).size(), "wrong # of matching rules");
        assertEquals(0, getRulesFor(0, 1).size(), "wrong # of matching rules");
        assertEquals(0, getRulesFor(1, 1).size(), "wrong # of matching rules");
        assertEquals(1, getRulesFor(2, 1).size(), "wrong # of matching rules");
        assertEquals(1, getRulesFor(3, 1).size(), "wrong # of matching rules");
        assertEquals(1, getRulesFor(0, 3).size(), "wrong # of matching rules");
        assertEquals(0, getRulesFor(1, 3).size(), "wrong # of matching rules");
        assertEquals(1, getRulesFor(2, 3).size(), "wrong # of matching rules");
        assertEquals(0, getRulesFor(0, 6).size(), "wrong # of matching rules");
        assertEquals(0, getRulesFor(3, 6).size(), "wrong # of matching rules");
        assertEquals(0, getRulesFor(2, 6).size(), "wrong # of matching rules");
    }

    private List<EvaluationConditionalFormatRule> getRulesFor(int row, int col) {
        ref = new CellReference(sheet.getSheetName(), row, col, false, false);
        return rules = cfe.getConditionalFormattingForCell(ref);
    }

    private String getColor(Color color) {
        final XSSFColor c = XSSFColor.toXSSFColor(color);
        return c.getARGBHex();
    }

    private double getTint(Color color) {
        final XSSFColor c = XSSFColor.toXSSFColor(color);
        return c.getTint();
    }
}
