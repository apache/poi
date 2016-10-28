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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * XSSF-specific Conditional Formatting tests
 */
public class TestXSSFConditionalFormatting extends BaseTestConditionalFormatting {
    public TestXSSFConditionalFormatting(){
        super(XSSFITestDataProvider.instance);
    }

    @Override
    protected void assertColour(String hexExpected, Color actual) {
        assertNotNull("Colour must be given", actual);
        XSSFColor colour = (XSSFColor)actual;
        if (hexExpected.length() == 8) {
            assertEquals(hexExpected, colour.getARGBHex());
        } else {
            assertEquals(hexExpected, colour.getARGBHex().substring(2));
        }
    }

    @Test
    public void testRead() throws IOException {
        testRead("WithConditionalFormatting.xlsx");
    }
    
    @Test
    public void testReadOffice2007() throws IOException {
        testReadOffice2007("NewStyleConditionalFormattings.xlsx");
    }

    @Test
    public void testSetCellRangeAddress() throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        final XSSFSheet sheet = wb.createSheet("S1");
        final XSSFSheetConditionalFormatting cf = sheet.getSheetConditionalFormatting();
        assertEquals(0, cf.getNumConditionalFormattings());
        ExtendedColor color = wb.getCreationHelper().createExtendedColor();
        color.setARGBHex("FF63BE7B");
        ConditionalFormattingRule rule1 = cf.createConditionalFormattingRule(color);
        DataBarFormatting db1 = rule1.getDataBarFormatting();
        db1.getMinThreshold().setRangeType(ConditionalFormattingThreshold.RangeType.MIN);
        db1.getMaxThreshold().setRangeType(ConditionalFormattingThreshold.RangeType.MAX);

        cf.addConditionalFormatting(new CellRangeAddress[] {
                CellRangeAddress.valueOf("A1:A5")
        }, rule1);

        assertEquals(1, cf.getNumConditionalFormattings());
        XSSFConditionalFormatting readCf = cf.getConditionalFormattingAt(0);
        CellRangeAddress[] formattingRanges = readCf.getFormattingRanges();
        assertEquals(1, formattingRanges.length);
        CellRangeAddress formattingRange = formattingRanges[0];
        assertEquals("A1:A5", formattingRange.formatAsString());

        readCf.setFormattingRanges(new CellRangeAddress[] {
                CellRangeAddress.valueOf("A1:A6")
        });

        readCf = cf.getConditionalFormattingAt(0);
        formattingRanges = readCf.getFormattingRanges();
        assertEquals(1, formattingRanges.length);
        formattingRange = formattingRanges[0];
        assertEquals("A1:A6", formattingRange.formatAsString());
    }
}
