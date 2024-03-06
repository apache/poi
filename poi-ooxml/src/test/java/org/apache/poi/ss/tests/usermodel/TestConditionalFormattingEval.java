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


import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.formula.EvaluationConditionalFormatRule;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TestConditionalFormattingEval {

    @Test
    void testFormattingEvaluation() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("ConditionalFormattingSamples.xlsx")) {
            Sheet sheet = wb.getSheet("Products1");
            XSSFFormulaEvaluator formulaEval = new XSSFFormulaEvaluator(wb);
            ConditionalFormattingEvaluator cfe = new ConditionalFormattingEvaluator(wb, formulaEval);

            RuleResult result = getRuleResultFor(12, 1, sheet, cfe);
            assertEquals(1, result.getRules().size(), "wrong # of rules for " + result.getRef());
            assertEquals("FFFFEB9C", getColor(result.getRules().get(0).getRule().getPatternFormatting().getFillBackgroundColorColor()), "wrong bg color for " + result.getRef());
            assertFalse(result.getRules().get(0).getRule().getFontFormatting().isItalic(), "should not be italic " + result.getRef());

            result = getRuleResultFor(16, 3, sheet, cfe);
            assertEquals(1, result.getRules().size(), "wrong # of rules for " + result.getRef());
            assertEquals(0.7999816888943144d, getTint(result.getRules().get(0).getRule().getPatternFormatting().getFillBackgroundColorColor()), 0.000000000000001, "wrong bg color for " + result.getRef());

            result = getRuleResultFor(12, 3, sheet, cfe);
            assertEquals(0, result.getRules().size(), "wrong # of rules for " + result.getRef());

            sheet = wb.getSheet("Products2");

            result = getRuleResultFor(15, 1, sheet, cfe);
            assertEquals(1, result.getRules().size(), "wrong # of rules for " + result.getRef());
            assertEquals("FFFFEB9C", getColor(result.getRules().get(0).getRule().getPatternFormatting().getFillBackgroundColorColor()), "wrong bg color for " + result.getRef());

            result = getRuleResultFor(20, 3, sheet, cfe);
            assertEquals(0, result.getRules().size(), "wrong # of rules for " + result.getRef());

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
            result = getRuleResultFor(15, 1, sheet, cfe);
            assertEquals(0, result.getRules().size(), "wrong # of rules for " + result.getRef());

            result = getRuleResultFor(20, 3, sheet, cfe);
            assertEquals(1, result.getRules().size(), "wrong # of rules for " + result.getRef());
            assertEquals(0.7999816888943144d, getTint(result.getRules().get(0).getRule().getPatternFormatting().getFillBackgroundColorColor()), 0.000000000000001, "wrong bg color for " + result.getRef());

            result = getRuleResultFor(20, 1, sheet, cfe);
            assertEquals(1, result.getRules().size(), "wrong # of rules for " + result.getRef());
            assertEquals("FFFFEB9C", getColor(result.getRules().get(0).getRule().getPatternFormatting().getFillBackgroundColorColor()), "wrong bg color for " + result.getRef());

            sheet = wb.getSheet("Book tour");

            result = getRuleResultFor(8, 2, sheet, cfe);
            assertEquals(1, result.getRules().size(), "wrong # of rules for " + result.getRef());

            sheet = wb.getSheet("Compare to totals");
            result = getRuleResultFor(3, 2, sheet, cfe);
            assertEquals(1, result.getRules().size(), "wrong # of rules for " + result.getRef());
            assertEquals("FFFF0000", getColor(result.getRules().get(0).getRule().getFontFormatting().getFontColor()), "wrong fg color for " + result.getRef());
            result = getRuleResultFor(3, 3, sheet, cfe);
            assertEquals(0, result.getRules().size(), "wrong # of rules for " + result.getRef());
            result = getRuleResultFor(15, 4, sheet, cfe);
            assertEquals(0, result.getRules().size(), "wrong # of rules for " + result.getRef());
            result = getRuleResultFor(16, 1, sheet, cfe);
            assertEquals(1, result.getRules().size(), "wrong # of rules for " + result.getRef());
            assertEquals("FFFF0000", getColor(result.getRules().get(0).getRule().getFontFormatting().getFontColor()), "wrong fg color for " + result.getRef());

            sheet = wb.getSheet("Products3");
            sheet.getRow(8).getCell(0).setCellValue(new Date());
            result = getRuleResultFor(8, 0, sheet, cfe);
            assertEquals(1, result.getRules().size(), "wrong # of rules for " + result.getRef());
            result = getRuleResultFor(8, 3, sheet, cfe);
            assertEquals(1, result.getRules().size(), "wrong # of rules for " + result.getRef());

            sheet = wb.getSheet("Customers2");
            result = getRuleResultFor(3, 0, sheet, cfe);
            assertEquals(0, result.getRules().size(), "wrong # of rules for " + result.getRef());
        }
    }

    @Test
    void testFormattingOnUndefinedCell() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("conditional_formatting_with_formula_on_second_sheet.xlsx")) {
            XSSFFormulaEvaluator formulaEval = new XSSFFormulaEvaluator(wb);
            ConditionalFormattingEvaluator cfe = new ConditionalFormattingEvaluator(wb, formulaEval);

            Sheet sheet = wb.getSheet("Sales Plan");
            RuleResult result = getRuleResultFor(9, 2, sheet, cfe);
            assertNotEquals(0, result.getRules().size(), "No rules for " + result.getRef());
            assertEquals("FFFFFF00", getColor(
                    result.getRules().get(0).getRule().getPatternFormatting().getFillBackgroundColorColor()),
                    "wrong bg color for " + result.getRef());
        }
    }

    @Test
    void testRepeatedEval() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("test_conditional_formatting.xlsx")) {
            XSSFFormulaEvaluator formulaEval = new XSSFFormulaEvaluator(wb);
            ConditionalFormattingEvaluator cfe = new ConditionalFormattingEvaluator(wb, formulaEval);

            Sheet sheet = wb.getSheetAt(0);
            assertEquals(0, getRulesFor(2, 1, sheet, cfe).size(), "no rules should apply");

            assertEquals(0, getRulesFor(2, 1, sheet, cfe).size(), "no rules should apply");
        }
    }

    @Test
    void testCellValueIsWrongType() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("conditional_formatting_cell_is.xlsx")) {
            XSSFFormulaEvaluator formulaEval = new XSSFFormulaEvaluator(wb);
            ConditionalFormattingEvaluator cfe = new ConditionalFormattingEvaluator(wb, formulaEval);

            Sheet sheet = wb.getSheetAt(1);

            assertEquals(1, getRulesFor(3, 1, sheet, cfe).size(), "wrong # of matching rules");
        }
    }

    @Test
    void testRangeCondition() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("conditional_formatting_multiple_ranges.xlsx")) {
            XSSFFormulaEvaluator formulaEval = new XSSFFormulaEvaluator(wb);
            ConditionalFormattingEvaluator cfe = new ConditionalFormattingEvaluator(wb, formulaEval);

            Sheet sheet = wb.getSheetAt(0);

            assertEquals(0, getRulesFor(0, 0, sheet, cfe).size(), "wrong # of matching rules");
            assertEquals(0, getRulesFor(1, 0, sheet, cfe).size(), "wrong # of matching rules");
            assertEquals(0, getRulesFor(2, 0, sheet, cfe).size(), "wrong # of matching rules");
            assertEquals(1, getRulesFor(3, 0, sheet, cfe).size(), "wrong # of matching rules");
            assertEquals(0, getRulesFor(0, 1, sheet, cfe).size(), "wrong # of matching rules");
            assertEquals(0, getRulesFor(1, 1, sheet, cfe).size(), "wrong # of matching rules");
            assertEquals(1, getRulesFor(2, 1, sheet, cfe).size(), "wrong # of matching rules");
            assertEquals(1, getRulesFor(3, 1, sheet, cfe).size(), "wrong # of matching rules");
            assertEquals(1, getRulesFor(0, 3, sheet, cfe).size(), "wrong # of matching rules");
            assertEquals(0, getRulesFor(1, 3, sheet, cfe).size(), "wrong # of matching rules");
            assertEquals(1, getRulesFor(2, 3, sheet, cfe).size(), "wrong # of matching rules");
            assertEquals(0, getRulesFor(0, 6, sheet, cfe).size(), "wrong # of matching rules");
            assertEquals(0, getRulesFor(3, 6, sheet, cfe).size(), "wrong # of matching rules");
            assertEquals(0, getRulesFor(2, 6, sheet, cfe).size(), "wrong # of matching rules");
        }
    }

    private RuleResult getRuleResultFor(int row, int col, Sheet sheet, ConditionalFormattingEvaluator cfe) {
        CellReference ref = new CellReference(sheet.getSheetName(), row, col, false, false);
        return new RuleResult(ref, cfe.getConditionalFormattingForCell(ref));
    }

    private List<EvaluationConditionalFormatRule> getRulesFor(
            int row, int col, Sheet sheet, ConditionalFormattingEvaluator cfe) {
        return getRuleResultFor(row, col, sheet, cfe).getRules();
    }

    private String getColor(Color color) {
        final XSSFColor c = XSSFColor.toXSSFColor(color);
        return c.getARGBHex();
    }

    private double getTint(Color color) {
        final XSSFColor c = XSSFColor.toXSSFColor(color);
        return c.getTint();
    }

    private static class RuleResult {
        private final CellReference ref;
        private final List<EvaluationConditionalFormatRule> rules;

        public RuleResult(CellReference ref, List<EvaluationConditionalFormatRule> rules) {
            this.ref = ref;
            this.rules = rules;
        }

        CellReference getRef() {
            return ref;
        }

        List<EvaluationConditionalFormatRule> getRules() {
            return rules;
        }
    }
}
