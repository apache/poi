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

package org.apache.poi.ss.usermodel;


import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.formula.EvaluationConditionalFormatRule;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConditionalFormattingEvalTest {

    private XSSFWorkbook wb;
    private Sheet sheet;
    private XSSFFormulaEvaluator formulaEval;
    private ConditionalFormattingEvaluator cfe;
    private CellReference ref;
    private List<EvaluationConditionalFormatRule> rules;

    @Before
    public void openWB() {
        wb = XSSFTestDataSamples.openSampleWorkbook("ConditionalFormattingSamples.xlsx");
        formulaEval = new XSSFFormulaEvaluator(wb);
        cfe = new ConditionalFormattingEvaluator(wb, formulaEval);
    }
    
    @After
    public void closeWB() {
        formulaEval = null;
        cfe = null;
        ref = null;
        rules = null;
        try {
            if (wb != null) wb.close();
        } catch (IOException e) {
            // keep going, this shouldn't cancel things
            e.printStackTrace();
        }
    }

    @Test
    public void testFormattingEvaluation() {
        sheet = wb.getSheet("Products1");
        
        getRulesFor(12, 1);
        assertEquals("wrong # of rules for " + ref, 1, rules.size());
        assertEquals("wrong bg color for " + ref, "FFFFEB9C", getColor(rules.get(0).getRule().getPatternFormatting().getFillBackgroundColorColor()));
        assertFalse("should not be italic " + ref, rules.get(0).getRule().getFontFormatting().isItalic());
        
        getRulesFor(16, 3);
        assertEquals("wrong # of rules for " + ref, 1, rules.size());
        assertEquals("wrong bg color for " + ref, 0.7999816888943144d, getTint(rules.get(0).getRule().getPatternFormatting().getFillBackgroundColorColor()), 0.000000000000001);
        
        getRulesFor(12, 3);
        assertEquals("wrong # of rules for " + ref, 0, rules.size());
        
        sheet = wb.getSheet("Products2");
        
        getRulesFor(15,1);
        assertEquals("wrong # of rules for " + ref, 1, rules.size());
        assertEquals("wrong bg color for " + ref, "FFFFEB9C", getColor(rules.get(0).getRule().getPatternFormatting().getFillBackgroundColorColor()));

        getRulesFor(20,3);
        assertEquals("wrong # of rules for " + ref, 0, rules.size());

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
        assertEquals("wrong # of rules for " + ref, 0, rules.size());
        
        getRulesFor(20,3);
        assertEquals("wrong # of rules for " + ref, 1, rules.size());
        assertEquals("wrong bg color for " + ref, 0.7999816888943144d, getTint(rules.get(0).getRule().getPatternFormatting().getFillBackgroundColorColor()), 0.000000000000001);
        
        getRulesFor(20,1);
        assertEquals("wrong # of rules for " + ref, 1, rules.size());
        assertEquals("wrong bg color for " + ref, "FFFFEB9C", getColor(rules.get(0).getRule().getPatternFormatting().getFillBackgroundColorColor()));
        
        sheet = wb.getSheet("Book tour");
        
        getRulesFor(8,2);
        assertEquals("wrong # of rules for " + ref, 1, rules.size());
        
    }

    @Test
    public void testFormattingOnUndefinedCell() throws Exception {
        wb = XSSFTestDataSamples.openSampleWorkbook("conditional_formatting_with_formula_on_second_sheet.xlsx");
        formulaEval = new XSSFFormulaEvaluator(wb);
        cfe = new ConditionalFormattingEvaluator(wb, formulaEval);

        sheet = wb.getSheet("Sales Plan");
        getRulesFor(9,2);
        assertNotEquals("No rules for " + ref, 0, rules.size());
        assertEquals("wrong bg color for " + ref, "FFFFFF00", getColor(rules.get(0).getRule().getPatternFormatting().getFillBackgroundColorColor()));
    }
    
    @Test
    public void testRepeatedEval() throws Exception {
        wb = XSSFTestDataSamples.openSampleWorkbook("test_conditional_formatting.xlsx");
        formulaEval = new XSSFFormulaEvaluator(wb);
        cfe = new ConditionalFormattingEvaluator(wb, formulaEval);

        sheet = wb.getSheetAt(0);
        try {
            getRulesFor(2, 1);
            fail("Got rules when an unsupported function error was expected.");
        } catch (NotImplementedException e) {
            // expected
        }

        try {
            getRulesFor(2, 1);
            fail("Got rules the second time when an unsupported function error was expected.");
        } catch (NotImplementedException e) {
            // expected
        }
        
    }
    
    @Test
    public void testCellValueIsWrongType() throws Exception {
        wb = XSSFTestDataSamples.openSampleWorkbook("conditional_formatting_cell_is.xlsx");
        formulaEval = new XSSFFormulaEvaluator(wb);
        cfe = new ConditionalFormattingEvaluator(wb, formulaEval);

        sheet = wb.getSheetAt(1);
        
        assertEquals("wrong # of matching rules", 1, getRulesFor(3, 1).size());
    }
    
    @Test
    public void testRangeCondition() throws Exception {
        wb = XSSFTestDataSamples.openSampleWorkbook("conditional_formatting_multiple_ranges.xlsx");
        formulaEval = new XSSFFormulaEvaluator(wb);
        cfe = new ConditionalFormattingEvaluator(wb, formulaEval);
        
        sheet = wb.getSheetAt(0);
        
        assertEquals("wrong # of matching rules", 0, getRulesFor(0, 0).size());
        assertEquals("wrong # of matching rules", 0, getRulesFor(1, 0).size());
        assertEquals("wrong # of matching rules", 0, getRulesFor(2, 0).size());
        assertEquals("wrong # of matching rules", 1, getRulesFor(3, 0).size());
        assertEquals("wrong # of matching rules", 0, getRulesFor(0, 1).size());
        assertEquals("wrong # of matching rules", 0, getRulesFor(1, 1).size());
        assertEquals("wrong # of matching rules", 1, getRulesFor(2, 1).size());
        assertEquals("wrong # of matching rules", 1, getRulesFor(3, 1).size());
        assertEquals("wrong # of matching rules", 1, getRulesFor(0, 3).size());
        assertEquals("wrong # of matching rules", 0, getRulesFor(1, 3).size());
        assertEquals("wrong # of matching rules", 1, getRulesFor(2, 3).size());
        assertEquals("wrong # of matching rules", 0, getRulesFor(0, 6).size());
        assertEquals("wrong # of matching rules", 0, getRulesFor(3, 6).size());
        assertEquals("wrong # of matching rules", 0, getRulesFor(2, 6).size());
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
