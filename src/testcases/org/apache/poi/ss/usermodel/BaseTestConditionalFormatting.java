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

package org.apache.poi.ss.usermodel;

import junit.framework.TestCase;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * @author Dmitriy Kumshayev
 * @author Yegor Kozlov
 */
public abstract class BaseTestConditionalFormatting extends TestCase {
    private final ITestDataProvider _testDataProvider;

    public BaseTestConditionalFormatting(ITestDataProvider testDataProvider){
        _testDataProvider = testDataProvider;
    }

    public void testBasic() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sh = wb.createSheet();
        SheetConditionalFormatting sheetCF = sh.getSheetConditionalFormatting();

        assertEquals(0, sheetCF.getNumConditionalFormattings());
        try {
            assertNull(sheetCF.getConditionalFormattingAt(0));
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("Specified CF index 0 is outside the allowable range"));
        }

        try {
            sheetCF.removeConditionalFormatting(0);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("Specified CF index 0 is outside the allowable range"));
        }

        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule("1");
        ConditionalFormattingRule rule2 = sheetCF.createConditionalFormattingRule("2");
        ConditionalFormattingRule rule3 = sheetCF.createConditionalFormattingRule("3");
        ConditionalFormattingRule rule4 = sheetCF.createConditionalFormattingRule("4");
        try {
            sheetCF.addConditionalFormatting(null, rule1);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("regions must not be null"));
        }
        try {
            sheetCF.addConditionalFormatting(
                    new CellRangeAddress[]{ CellRangeAddress.valueOf("A1:A3") },
                    (ConditionalFormattingRule)null);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("cfRules must not be null"));
        }

        try {
            sheetCF.addConditionalFormatting(
                    new CellRangeAddress[]{ CellRangeAddress.valueOf("A1:A3") },
                    new ConditionalFormattingRule[0]);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("cfRules must not be empty"));
        }

        try {
            sheetCF.addConditionalFormatting(
                    new CellRangeAddress[]{ CellRangeAddress.valueOf("A1:A3") },
                    new ConditionalFormattingRule[]{rule1, rule2, rule3, rule4});
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("Number of rules must not exceed 3"));
        }
    }

    /**
     * Test format conditions based on a boolean formula
     */
    public void testBooleanFormulaConditions() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sh = wb.createSheet();
        SheetConditionalFormatting sheetCF = sh.getSheetConditionalFormatting();

        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule("SUM(A1:A5)>10");
        assertEquals(ConditionalFormattingRule.CONDITION_TYPE_FORMULA, rule1.getConditionType());
        assertEquals("SUM(A1:A5)>10", rule1.getFormula1());
        int formatIndex1 = sheetCF.addConditionalFormatting(
                new CellRangeAddress[]{
                        CellRangeAddress.valueOf("B1"),
                        CellRangeAddress.valueOf("C3"),
                }, rule1);
        assertEquals(0, formatIndex1);
        assertEquals(1, sheetCF.getNumConditionalFormattings());
        CellRangeAddress[] ranges1 = sheetCF.getConditionalFormattingAt(formatIndex1).getFormattingRanges();
        assertEquals(2, ranges1.length);
        assertEquals("B1", ranges1[0].formatAsString());
        assertEquals("C3", ranges1[1].formatAsString());

        // adjacent address are merged
        int formatIndex2 = sheetCF.addConditionalFormatting(
                new CellRangeAddress[]{
                        CellRangeAddress.valueOf("B1"),
                        CellRangeAddress.valueOf("B2"),
                        CellRangeAddress.valueOf("B3"),
                }, rule1);
        assertEquals(1, formatIndex2);
        assertEquals(2, sheetCF.getNumConditionalFormattings());
        CellRangeAddress[] ranges2 = sheetCF.getConditionalFormattingAt(formatIndex2).getFormattingRanges();
        assertEquals(1, ranges2.length);
        assertEquals("B1:B3", ranges2[0].formatAsString());
    }

    public void testSingleFormulaConditions() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sh = wb.createSheet();
        SheetConditionalFormatting sheetCF = sh.getSheetConditionalFormatting();

        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(
                ComparisonOperator.EQUAL, "SUM(A1:A5)+10");
        assertEquals(ConditionalFormattingRule.CONDITION_TYPE_CELL_VALUE_IS, rule1.getConditionType());
        assertEquals("SUM(A1:A5)+10", rule1.getFormula1());
        assertEquals(ComparisonOperator.EQUAL, rule1.getComparisonOperation());

        ConditionalFormattingRule rule2 = sheetCF.createConditionalFormattingRule(
                ComparisonOperator.NOT_EQUAL, "15");
        assertEquals(ConditionalFormattingRule.CONDITION_TYPE_CELL_VALUE_IS, rule2.getConditionType());
        assertEquals("15", rule2.getFormula1());
        assertEquals(ComparisonOperator.NOT_EQUAL, rule2.getComparisonOperation());

        ConditionalFormattingRule rule3 = sheetCF.createConditionalFormattingRule(
                ComparisonOperator.NOT_EQUAL, "15");
        assertEquals(ConditionalFormattingRule.CONDITION_TYPE_CELL_VALUE_IS, rule3.getConditionType());
        assertEquals("15", rule3.getFormula1());
        assertEquals(ComparisonOperator.NOT_EQUAL, rule3.getComparisonOperation());

        ConditionalFormattingRule rule4 = sheetCF.createConditionalFormattingRule(
                ComparisonOperator.GT, "0");
        assertEquals(ConditionalFormattingRule.CONDITION_TYPE_CELL_VALUE_IS, rule4.getConditionType());
        assertEquals("0", rule4.getFormula1());
        assertEquals(ComparisonOperator.GT, rule4.getComparisonOperation());

        ConditionalFormattingRule rule5 = sheetCF.createConditionalFormattingRule(
                ComparisonOperator.LT, "0");
        assertEquals(ConditionalFormattingRule.CONDITION_TYPE_CELL_VALUE_IS, rule5.getConditionType());
        assertEquals("0", rule5.getFormula1());
        assertEquals(ComparisonOperator.LT, rule5.getComparisonOperation());

        ConditionalFormattingRule rule6 = sheetCF.createConditionalFormattingRule(
                ComparisonOperator.GE, "0");
        assertEquals(ConditionalFormattingRule.CONDITION_TYPE_CELL_VALUE_IS, rule6.getConditionType());
        assertEquals("0", rule6.getFormula1());
        assertEquals(ComparisonOperator.GE, rule6.getComparisonOperation());

        ConditionalFormattingRule rule7 = sheetCF.createConditionalFormattingRule(
                ComparisonOperator.LE, "0");
        assertEquals(ConditionalFormattingRule.CONDITION_TYPE_CELL_VALUE_IS, rule7.getConditionType());
        assertEquals("0", rule7.getFormula1());
        assertEquals(ComparisonOperator.LE, rule7.getComparisonOperation());

        ConditionalFormattingRule rule8 = sheetCF.createConditionalFormattingRule(
                ComparisonOperator.BETWEEN, "0", "5");
        assertEquals(ConditionalFormattingRule.CONDITION_TYPE_CELL_VALUE_IS, rule8.getConditionType());
        assertEquals("0", rule8.getFormula1());
        assertEquals("5", rule8.getFormula2());
        assertEquals(ComparisonOperator.BETWEEN, rule8.getComparisonOperation());

        ConditionalFormattingRule rule9 = sheetCF.createConditionalFormattingRule(
                ComparisonOperator.NOT_BETWEEN, "0", "5");
        assertEquals(ConditionalFormattingRule.CONDITION_TYPE_CELL_VALUE_IS, rule9.getConditionType());
        assertEquals("0", rule9.getFormula1());
        assertEquals("5", rule9.getFormula2());
        assertEquals(ComparisonOperator.NOT_BETWEEN, rule9.getComparisonOperation());
    }

    public void testCopy() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet1 = wb.createSheet();
        Sheet sheet2 = wb.createSheet();
        SheetConditionalFormatting sheet1CF = sheet1.getSheetConditionalFormatting();
        SheetConditionalFormatting sheet2CF = sheet2.getSheetConditionalFormatting();
        assertEquals(0, sheet1CF.getNumConditionalFormattings());
        assertEquals(0, sheet2CF.getNumConditionalFormattings());

        ConditionalFormattingRule rule1 = sheet1CF.createConditionalFormattingRule(
                ComparisonOperator.EQUAL, "SUM(A1:A5)+10");

        ConditionalFormattingRule rule2 = sheet1CF.createConditionalFormattingRule(
                ComparisonOperator.NOT_EQUAL, "15");

        // adjacent address are merged
        int formatIndex = sheet1CF.addConditionalFormatting(
                new CellRangeAddress[]{
                        CellRangeAddress.valueOf("A1:A5"),
                        CellRangeAddress.valueOf("C1:C5")
                }, rule1, rule2);
        assertEquals(0, formatIndex);
        assertEquals(1, sheet1CF.getNumConditionalFormattings());

        assertEquals(0, sheet2CF.getNumConditionalFormattings());
        sheet2CF.addConditionalFormatting(sheet1CF.getConditionalFormattingAt(formatIndex));
        assertEquals(1, sheet2CF.getNumConditionalFormattings());

        ConditionalFormatting sheet2cf = sheet2CF.getConditionalFormattingAt(0);
        assertEquals(2, sheet2cf.getNumberOfRules());
        assertEquals("SUM(A1:A5)+10", sheet2cf.getRule(0).getFormula1());
        assertEquals(ComparisonOperator.EQUAL, sheet2cf.getRule(0).getComparisonOperation());
        assertEquals(ConditionalFormattingRule.CONDITION_TYPE_CELL_VALUE_IS, sheet2cf.getRule(0).getConditionType());
        assertEquals("15", sheet2cf.getRule(1).getFormula1());
        assertEquals(ComparisonOperator.NOT_EQUAL, sheet2cf.getRule(1).getComparisonOperation());
        assertEquals(ConditionalFormattingRule.CONDITION_TYPE_CELL_VALUE_IS, sheet2cf.getRule(1).getConditionType());
    }

    public void testRemove() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet1 = wb.createSheet();
        SheetConditionalFormatting sheetCF = sheet1.getSheetConditionalFormatting();
        assertEquals(0, sheetCF.getNumConditionalFormattings());

        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(
                ComparisonOperator.EQUAL, "SUM(A1:A5)");

        // adjacent address are merged
        int formatIndex = sheetCF.addConditionalFormatting(
                new CellRangeAddress[]{
                        CellRangeAddress.valueOf("A1:A5")
                }, rule1);
        assertEquals(0, formatIndex);
        assertEquals(1, sheetCF.getNumConditionalFormattings());
        sheetCF.removeConditionalFormatting(0);
        assertEquals(0, sheetCF.getNumConditionalFormattings());
        try {
            assertNull(sheetCF.getConditionalFormattingAt(0));
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("Specified CF index 0 is outside the allowable range"));
        }

        formatIndex = sheetCF.addConditionalFormatting(
                new CellRangeAddress[]{
                        CellRangeAddress.valueOf("A1:A5")
                }, rule1);
        assertEquals(0, formatIndex);
        assertEquals(1, sheetCF.getNumConditionalFormattings());
        sheetCF.removeConditionalFormatting(0);
        assertEquals(0, sheetCF.getNumConditionalFormattings());
        try {
            assertNull(sheetCF.getConditionalFormattingAt(0));
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("Specified CF index 0 is outside the allowable range"));
        }
    }
    
    public void testCreateCF() {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();
        String formula = "7";

        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(formula);
        FontFormatting fontFmt = rule1.createFontFormatting();
        fontFmt.setFontStyle(true, false);

        BorderFormatting bordFmt = rule1.createBorderFormatting();
        bordFmt.setBorderBottom(BorderFormatting.BORDER_THIN);
        bordFmt.setBorderTop(BorderFormatting.BORDER_THICK);
        bordFmt.setBorderLeft(BorderFormatting.BORDER_DASHED);
        bordFmt.setBorderRight(BorderFormatting.BORDER_DOTTED);

        PatternFormatting patternFmt = rule1.createPatternFormatting();
        patternFmt.setFillBackgroundColor(IndexedColors.YELLOW.index);


        ConditionalFormattingRule rule2 = sheetCF.createConditionalFormattingRule(ComparisonOperator.BETWEEN, "1", "2");
        ConditionalFormattingRule [] cfRules =
        {
            rule1, rule2
        };

        short col = 1;
        CellRangeAddress [] regions = {
            new CellRangeAddress(0, 65535, col, col)
        };

        sheetCF.addConditionalFormatting(regions, cfRules);
        sheetCF.addConditionalFormatting(regions, cfRules);

        // Verification
        assertEquals(2, sheetCF.getNumConditionalFormattings());
        sheetCF.removeConditionalFormatting(1);
        assertEquals(1, sheetCF.getNumConditionalFormattings());
        ConditionalFormatting cf = sheetCF.getConditionalFormattingAt(0);
        assertNotNull(cf);

        regions = cf.getFormattingRanges();
        assertNotNull(regions);
        assertEquals(1, regions.length);
        CellRangeAddress r = regions[0];
        assertEquals(1, r.getFirstColumn());
        assertEquals(1, r.getLastColumn());
        assertEquals(0, r.getFirstRow());
        assertEquals(65535, r.getLastRow());

        assertEquals(2, cf.getNumberOfRules());

        rule1 = cf.getRule(0);
        assertEquals("7",rule1.getFormula1());
        assertNull(rule1.getFormula2());

        FontFormatting    r1fp = rule1.getFontFormatting();
        assertNotNull(r1fp);

        assertTrue(r1fp.isItalic());
        assertFalse(r1fp.isBold());

        BorderFormatting  r1bf = rule1.getBorderFormatting();
        assertNotNull(r1bf);
        assertEquals(BorderFormatting.BORDER_THIN, r1bf.getBorderBottom());
        assertEquals(BorderFormatting.BORDER_THICK,r1bf.getBorderTop());
        assertEquals(BorderFormatting.BORDER_DASHED,r1bf.getBorderLeft());
        assertEquals(BorderFormatting.BORDER_DOTTED,r1bf.getBorderRight());

        PatternFormatting r1pf = rule1.getPatternFormatting();
        assertNotNull(r1pf);
//        assertEquals(IndexedColors.YELLOW.index,r1pf.getFillBackgroundColor());

        rule2 = cf.getRule(1);
        assertEquals("2",rule2.getFormula2());
        assertEquals("1",rule2.getFormula1());
    }

    public void testClone() {

        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
        String formula = "7";

        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(formula);
        FontFormatting fontFmt = rule1.createFontFormatting();
        fontFmt.setFontStyle(true, false);

        PatternFormatting patternFmt = rule1.createPatternFormatting();
        patternFmt.setFillBackgroundColor(IndexedColors.YELLOW.index);


        ConditionalFormattingRule rule2 = sheetCF.createConditionalFormattingRule(ComparisonOperator.BETWEEN, "1", "2");
        ConditionalFormattingRule [] cfRules =
        {
            rule1, rule2
        };

        short col = 1;
        CellRangeAddress [] regions = {
            new CellRangeAddress(0, 65535, col, col)
        };

        sheetCF.addConditionalFormatting(regions, cfRules);

        try {
            wb.cloneSheet(0);
        } catch (RuntimeException e) {
            if (e.getMessage().indexOf("needs to define a clone method") > 0) {
                fail("Indentified bug 45682");
            }
            throw e;
        }
        assertEquals(2, wb.getNumberOfSheets());
    }

    public void testShiftRows() {

        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();

        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(
                ComparisonOperator.BETWEEN, "SUM(A10:A15)", "1+SUM(B16:B30)");
        FontFormatting fontFmt = rule1.createFontFormatting();
        fontFmt.setFontStyle(true, false);

        PatternFormatting patternFmt = rule1.createPatternFormatting();
        patternFmt.setFillBackgroundColor(IndexedColors.YELLOW.index);
        ConditionalFormattingRule [] cfRules = { rule1, };

        CellRangeAddress [] regions = {
            new CellRangeAddress(2, 4, 0, 0), // A3:A5
        };
        sheetCF.addConditionalFormatting(regions, cfRules);

        // This row-shift should destroy the CF region
        sheet.shiftRows(10, 20, -9);
        assertEquals(0, sheetCF.getNumConditionalFormattings());

        // re-add the CF
        sheetCF.addConditionalFormatting(regions, cfRules);

        // This row shift should only affect the formulas
        sheet.shiftRows(14, 17, 8);
        ConditionalFormatting cf = sheetCF.getConditionalFormattingAt(0);
        assertEquals("SUM(A10:A23)", cf.getRule(0).getFormula1());
        assertEquals("1+SUM(B24:B30)", cf.getRule(0).getFormula2());

        sheet.shiftRows(0, 8, 21);
        cf = sheetCF.getConditionalFormattingAt(0);
        assertEquals("SUM(A10:A21)", cf.getRule(0).getFormula1());
        assertEquals("1+SUM(#REF!)", cf.getRule(0).getFormula2());
    }

    protected void testRead(String filename){
        Workbook wb = _testDataProvider.openSampleWorkbook(filename);
        Sheet sh = wb.getSheet("CF");
        SheetConditionalFormatting sheetCF = sh.getSheetConditionalFormatting();
        assertEquals(3, sheetCF.getNumConditionalFormattings());

        ConditionalFormatting cf1 = sheetCF.getConditionalFormattingAt(0);
        assertEquals(2, cf1.getNumberOfRules());

        CellRangeAddress[] regions1 = cf1.getFormattingRanges();
        assertEquals(1, regions1.length);
        assertEquals("A1:A8", regions1[0].formatAsString());

        // CF1 has two rules: values less than -3 are bold-italic red, values greater than 3 are green
        ConditionalFormattingRule rule1 = cf1.getRule(0);
        assertEquals(ConditionalFormattingRule.CONDITION_TYPE_CELL_VALUE_IS, rule1.getConditionType());
        assertEquals(ComparisonOperator.GT, rule1.getComparisonOperation());
        assertEquals("3", rule1.getFormula1());
        assertNull(rule1.getFormula2());
        // fills and borders are not set
        assertNull(rule1.getPatternFormatting());
        assertNull(rule1.getBorderFormatting());

        FontFormatting fmt1 = rule1.getFontFormatting();
//        assertEquals(IndexedColors.GREEN.index, fmt1.getFontColorIndex());
        assertTrue(fmt1.isBold());
        assertFalse(fmt1.isItalic());

        ConditionalFormattingRule rule2 = cf1.getRule(1);
        assertEquals(ConditionalFormattingRule.CONDITION_TYPE_CELL_VALUE_IS, rule2.getConditionType());
        assertEquals(ComparisonOperator.LT, rule2.getComparisonOperation());
        assertEquals("-3", rule2.getFormula1());
        assertNull(rule2.getFormula2());
        assertNull(rule2.getPatternFormatting());
        assertNull(rule2.getBorderFormatting());

        FontFormatting fmt2 = rule2.getFontFormatting();
//        assertEquals(IndexedColors.RED.index, fmt2.getFontColorIndex());
        assertTrue(fmt2.isBold());
        assertTrue(fmt2.isItalic());


        ConditionalFormatting cf2 = sheetCF.getConditionalFormattingAt(1);
        assertEquals(1, cf2.getNumberOfRules());
        CellRangeAddress[] regions2 = cf2.getFormattingRanges();
        assertEquals(1, regions2.length);
        assertEquals("B9", regions2[0].formatAsString());

        ConditionalFormattingRule rule3 = cf2.getRule(0);
        assertEquals(ConditionalFormattingRule.CONDITION_TYPE_FORMULA, rule3.getConditionType());
        assertEquals(ComparisonOperator.NO_COMPARISON, rule3.getComparisonOperation());
        assertEquals("$A$8>5", rule3.getFormula1());
        assertNull(rule3.getFormula2());

        FontFormatting fmt3 = rule3.getFontFormatting();
//        assertEquals(IndexedColors.RED.index, fmt3.getFontColorIndex());
        assertTrue(fmt3.isBold());
        assertTrue(fmt3.isItalic());

        PatternFormatting fmt4 = rule3.getPatternFormatting();
//        assertEquals(IndexedColors.LIGHT_CORNFLOWER_BLUE.index, fmt4.getFillBackgroundColor());
//        assertEquals(IndexedColors.AUTOMATIC.index, fmt4.getFillForegroundColor());
        assertEquals(PatternFormatting.NO_FILL, fmt4.getFillPattern());
        // borders are not set
        assertNull(rule3.getBorderFormatting());

        ConditionalFormatting cf3 = sheetCF.getConditionalFormattingAt(2);
        CellRangeAddress[] regions3 = cf3.getFormattingRanges();
        assertEquals(1, regions3.length);
        assertEquals("B1:B7", regions3[0].formatAsString());
        assertEquals(2, cf3.getNumberOfRules());

        ConditionalFormattingRule rule4 = cf3.getRule(0);
        assertEquals(ConditionalFormattingRule.CONDITION_TYPE_CELL_VALUE_IS, rule4.getConditionType());
        assertEquals(ComparisonOperator.LE, rule4.getComparisonOperation());
        assertEquals("\"AAA\"", rule4.getFormula1());
        assertNull(rule4.getFormula2());

        ConditionalFormattingRule rule5 = cf3.getRule(1);
        assertEquals(ConditionalFormattingRule.CONDITION_TYPE_CELL_VALUE_IS, rule5.getConditionType());
        assertEquals(ComparisonOperator.BETWEEN, rule5.getComparisonOperation());
        assertEquals("\"A\"", rule5.getFormula1());
        assertEquals("\"AAA\"", rule5.getFormula2());
    }


    public void testCreateFontFormatting() {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();

        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(ComparisonOperator.EQUAL, "7");
        FontFormatting fontFmt = rule1.createFontFormatting();
        assertFalse(fontFmt.isItalic());
        assertFalse(fontFmt.isBold());
        fontFmt.setFontStyle(true, true);
        assertTrue(fontFmt.isItalic());
        assertTrue(fontFmt.isBold());

        assertEquals(-1, fontFmt.getFontHeight()); // not modified
        fontFmt.setFontHeight(200);
        assertEquals(200, fontFmt.getFontHeight()); 
        fontFmt.setFontHeight(100);
        assertEquals(100, fontFmt.getFontHeight());

        assertEquals(FontFormatting.SS_NONE, fontFmt.getEscapementType());
        fontFmt.setEscapementType(FontFormatting.SS_SUB);
        assertEquals(FontFormatting.SS_SUB, fontFmt.getEscapementType());
        fontFmt.setEscapementType(FontFormatting.SS_NONE);
        assertEquals(FontFormatting.SS_NONE, fontFmt.getEscapementType());
        fontFmt.setEscapementType(FontFormatting.SS_SUPER);
        assertEquals(FontFormatting.SS_SUPER, fontFmt.getEscapementType());

        assertEquals(FontFormatting.U_NONE, fontFmt.getUnderlineType());
        fontFmt.setUnderlineType(FontFormatting.U_SINGLE);
        assertEquals(FontFormatting.U_SINGLE, fontFmt.getUnderlineType());
        fontFmt.setUnderlineType(FontFormatting.U_NONE);
        assertEquals(FontFormatting.U_NONE, fontFmt.getUnderlineType());
        fontFmt.setUnderlineType(FontFormatting.U_DOUBLE);
        assertEquals(FontFormatting.U_DOUBLE, fontFmt.getUnderlineType());

        assertEquals(-1, fontFmt.getFontColorIndex());
        fontFmt.setFontColorIndex(IndexedColors.RED.index);
        assertEquals(IndexedColors.RED.index, fontFmt.getFontColorIndex());
        fontFmt.setFontColorIndex(IndexedColors.AUTOMATIC.index);
        assertEquals(IndexedColors.AUTOMATIC.index, fontFmt.getFontColorIndex());
        fontFmt.setFontColorIndex(IndexedColors.BLUE.index);
        assertEquals(IndexedColors.BLUE.index, fontFmt.getFontColorIndex());

        ConditionalFormattingRule [] cfRules = { rule1 };

        CellRangeAddress [] regions = { CellRangeAddress.valueOf("A1:A5") };

        sheetCF.addConditionalFormatting(regions, cfRules);

        // Verification
        ConditionalFormatting cf = sheetCF.getConditionalFormattingAt(0);
        assertNotNull(cf);

        assertEquals(1, cf.getNumberOfRules());

        FontFormatting  r1fp = cf.getRule(0).getFontFormatting();
        assertNotNull(r1fp);

        assertTrue(r1fp.isItalic());
        assertTrue(r1fp.isBold());
        assertEquals(FontFormatting.SS_SUPER, r1fp.getEscapementType());
        assertEquals(FontFormatting.U_DOUBLE, r1fp.getUnderlineType());
        assertEquals(IndexedColors.BLUE.index, r1fp.getFontColorIndex());

    }

    public void testCreatePatternFormatting() {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();

        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(ComparisonOperator.EQUAL, "7");
        PatternFormatting patternFmt = rule1.createPatternFormatting();

        assertEquals(0, patternFmt.getFillBackgroundColor());
        patternFmt.setFillBackgroundColor(IndexedColors.RED.index);
        assertEquals(IndexedColors.RED.index, patternFmt.getFillBackgroundColor());

        assertEquals(0, patternFmt.getFillForegroundColor());
        patternFmt.setFillForegroundColor(IndexedColors.BLUE.index);
        assertEquals(IndexedColors.BLUE.index, patternFmt.getFillForegroundColor());

        assertEquals(PatternFormatting.NO_FILL, patternFmt.getFillPattern());
        patternFmt.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        assertEquals(PatternFormatting.SOLID_FOREGROUND, patternFmt.getFillPattern());
        patternFmt.setFillPattern(PatternFormatting.NO_FILL);
        assertEquals(PatternFormatting.NO_FILL, patternFmt.getFillPattern());
        patternFmt.setFillPattern(PatternFormatting.BRICKS);
        assertEquals(PatternFormatting.BRICKS, patternFmt.getFillPattern());

        ConditionalFormattingRule [] cfRules = { rule1 };

        CellRangeAddress [] regions = { CellRangeAddress.valueOf("A1:A5") };

        sheetCF.addConditionalFormatting(regions, cfRules);

        // Verification
        ConditionalFormatting cf = sheetCF.getConditionalFormattingAt(0);
        assertNotNull(cf);

        assertEquals(1, cf.getNumberOfRules());

        PatternFormatting  r1fp = cf.getRule(0).getPatternFormatting();
        assertNotNull(r1fp);

        assertEquals(IndexedColors.RED.index, r1fp.getFillBackgroundColor());
        assertEquals(IndexedColors.BLUE.index, r1fp.getFillForegroundColor());
        assertEquals(PatternFormatting.BRICKS, r1fp.getFillPattern());
    }

    public void testCreateBorderFormatting() {
        Workbook workbook = _testDataProvider.createWorkbook();
        Sheet sheet = workbook.createSheet();

        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(ComparisonOperator.EQUAL, "7");
        BorderFormatting borderFmt = rule1.createBorderFormatting();

        assertEquals(BorderFormatting.BORDER_NONE, borderFmt.getBorderBottom());
        borderFmt.setBorderBottom(BorderFormatting.BORDER_DOTTED);
        assertEquals(BorderFormatting.BORDER_DOTTED, borderFmt.getBorderBottom());
        borderFmt.setBorderBottom(BorderFormatting.BORDER_NONE);
        assertEquals(BorderFormatting.BORDER_NONE, borderFmt.getBorderBottom());
        borderFmt.setBorderBottom(BorderFormatting.BORDER_THICK);
        assertEquals(BorderFormatting.BORDER_THICK, borderFmt.getBorderBottom());

        assertEquals(BorderFormatting.BORDER_NONE, borderFmt.getBorderTop());
        borderFmt.setBorderTop(BorderFormatting.BORDER_DOTTED);
        assertEquals(BorderFormatting.BORDER_DOTTED, borderFmt.getBorderTop());
        borderFmt.setBorderTop(BorderFormatting.BORDER_NONE);
        assertEquals(BorderFormatting.BORDER_NONE, borderFmt.getBorderTop());
        borderFmt.setBorderTop(BorderFormatting.BORDER_THICK);
        assertEquals(BorderFormatting.BORDER_THICK, borderFmt.getBorderTop());

        assertEquals(BorderFormatting.BORDER_NONE, borderFmt.getBorderLeft());
        borderFmt.setBorderLeft(BorderFormatting.BORDER_DOTTED);
        assertEquals(BorderFormatting.BORDER_DOTTED, borderFmt.getBorderLeft());
        borderFmt.setBorderLeft(BorderFormatting.BORDER_NONE);
        assertEquals(BorderFormatting.BORDER_NONE, borderFmt.getBorderLeft());
        borderFmt.setBorderLeft(BorderFormatting.BORDER_THIN);
        assertEquals(BorderFormatting.BORDER_THIN, borderFmt.getBorderLeft());

        assertEquals(BorderFormatting.BORDER_NONE, borderFmt.getBorderRight());
        borderFmt.setBorderRight(BorderFormatting.BORDER_DOTTED);
        assertEquals(BorderFormatting.BORDER_DOTTED, borderFmt.getBorderRight());
        borderFmt.setBorderRight(BorderFormatting.BORDER_NONE);
        assertEquals(BorderFormatting.BORDER_NONE, borderFmt.getBorderRight());
        borderFmt.setBorderRight(BorderFormatting.BORDER_HAIR);
        assertEquals(BorderFormatting.BORDER_HAIR, borderFmt.getBorderRight());

        ConditionalFormattingRule [] cfRules = { rule1 };

        CellRangeAddress [] regions = { CellRangeAddress.valueOf("A1:A5") };

        sheetCF.addConditionalFormatting(regions, cfRules);

        // Verification
        ConditionalFormatting cf = sheetCF.getConditionalFormattingAt(0);
        assertNotNull(cf);

        assertEquals(1, cf.getNumberOfRules());

        BorderFormatting  r1fp = cf.getRule(0).getBorderFormatting();
        assertNotNull(r1fp);
        assertEquals(BorderFormatting.BORDER_THICK, r1fp.getBorderBottom());
        assertEquals(BorderFormatting.BORDER_THICK, r1fp.getBorderTop());
        assertEquals(BorderFormatting.BORDER_THIN, r1fp.getBorderLeft());
        assertEquals(BorderFormatting.BORDER_HAIR, r1fp.getBorderRight());

    }
}
