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
package org.apache.poi.xssf.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.apache.poi.ss.usermodel.BaseTestConditionalFormatting;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.ExtendedColor;
import org.apache.poi.ss.usermodel.FontFormatting;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.junit.jupiter.api.Test;

/**
 * XSSF-specific Conditional Formatting tests
 */
class TestXSSFConditionalFormatting extends BaseTestConditionalFormatting {

    public TestXSSFConditionalFormatting(){
        super(XSSFITestDataProvider.instance);
    }

    @Override
    protected void assertColor(String hexExpected, Color actual) {
        assertNotNull(actual, "Color must be given");
        XSSFColor color = (XSSFColor)actual;
        if (hexExpected.length() == 8) {
            assertEquals(hexExpected, color.getARGBHex());
        } else {
            assertEquals(hexExpected, color.getARGBHex().substring(2));
        }
    }

    @Test
    void testRead() throws IOException {
        testRead("WithConditionalFormatting.xlsx");
    }

    @Test
    void testReadOffice2007() throws IOException {
        testReadOffice2007("NewStyleConditionalFormattings.xlsx");
    }

    private final static java.awt.Color PEAK_ORANGE = new java.awt.Color(255, 239, 221);

    @Test
    void testFontFormattingColor() {
        XSSFWorkbook wb = XSSFITestDataProvider.instance.createWorkbook();
        final Sheet sheet = wb.createSheet();

        final SheetConditionalFormatting formatting = sheet.getSheetConditionalFormatting();

        // the conditional formatting is not automatically added when it is created...
        assertEquals(0, formatting.getNumConditionalFormattings());
        ConditionalFormattingRule formattingRule = formatting.createConditionalFormattingRule("A1");
        assertEquals(0, formatting.getNumConditionalFormattings());

        // adding the formatting makes it available
        int idx = formatting.addConditionalFormatting(new CellRangeAddress[] {}, formattingRule);

        // verify that it can be accessed now
        assertEquals(0, idx);
        assertEquals(1, formatting.getNumConditionalFormattings());
        assertEquals(1, formatting.getConditionalFormattingAt(idx).getNumberOfRules());

        // this is confusing: the rule is not connected to the sheet, changes are not applied
        // so we need to use setRule() explicitly!
        FontFormatting fontFmt = formattingRule.createFontFormatting();
        assertNotNull(formattingRule.getFontFormatting());
        assertEquals(1, formatting.getConditionalFormattingAt(idx).getNumberOfRules());
        formatting.getConditionalFormattingAt(idx).setRule(0, formattingRule);
        assertNotNull(formatting.getConditionalFormattingAt(idx).getRule(0).getFontFormatting());

        fontFmt.setFontStyle(true, false);

        assertEquals(-1, fontFmt.getFontColorIndex());

        //fontFmt.setFontColorIndex((short)11);
        final ExtendedColor extendedColor = new XSSFColor(PEAK_ORANGE, wb.getStylesSource().getIndexedColors());
        fontFmt.setFontColor(extendedColor);

        PatternFormatting patternFmt = formattingRule.createPatternFormatting();
        assertNotNull(patternFmt);
        patternFmt.setFillBackgroundColor(extendedColor);

        assertEquals(1, formatting.getConditionalFormattingAt(0).getNumberOfRules());
        assertNotNull(formatting.getConditionalFormattingAt(0).getRule(0).getFontFormatting());
        assertNotNull(formatting.getConditionalFormattingAt(0).getRule(0).getFontFormatting().getFontColor());
        assertNotNull(formatting.getConditionalFormattingAt(0).getRule(0).getPatternFormatting().getFillBackgroundColorColor());

        checkFontFormattingColorWriteOutAndReadBack(wb, extendedColor);
    }

    private void checkFontFormattingColorWriteOutAndReadBack(Workbook wb, ExtendedColor extendedColor) {
        Workbook wbBack = XSSFITestDataProvider.instance.writeOutAndReadBack(wb);
        assertNotNull(wbBack);

        assertEquals(1, wbBack.getSheetAt(0).getSheetConditionalFormatting().getNumConditionalFormattings());
        final ConditionalFormatting formattingBack = wbBack.getSheetAt(0).getSheetConditionalFormatting().getConditionalFormattingAt(0);
        assertEquals(1, wbBack.getSheetAt(0).getSheetConditionalFormatting().getConditionalFormattingAt(0).getNumberOfRules());
        final ConditionalFormattingRule ruleBack = formattingBack.getRule(0);
        final FontFormatting fontFormattingBack = ruleBack.getFontFormatting();
        assertNotNull(formattingBack);
        assertNotNull(fontFormattingBack.getFontColor());
        assertEquals(extendedColor, fontFormattingBack.getFontColor());
        assertEquals(extendedColor, ruleBack.getPatternFormatting().getFillBackgroundColorColor());
    }

    @Override
    protected boolean applyLimitOf3() {
        return false;
    }
}
