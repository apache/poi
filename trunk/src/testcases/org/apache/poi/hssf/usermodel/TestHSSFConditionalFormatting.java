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

package org.apache.poi.hssf.usermodel;


import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.Test;

/**
 * HSSF-specific Conditional Formatting tests
 */
public final class TestHSSFConditionalFormatting extends BaseTestConditionalFormatting {
    public TestHSSFConditionalFormatting(){
        super(HSSFITestDataProvider.instance);
    }
    protected void assertColour(String hexExpected, Color actual) {
        assertNotNull("Colour must be given", actual);
        if (actual instanceof HSSFColor) {
            HSSFColor colour = (HSSFColor)actual;
            assertEquals(hexExpected, colour.getHexString());
        } else {
            HSSFExtendedColor colour = (HSSFExtendedColor)actual;
            if (hexExpected.length() == 8) {
                assertEquals(hexExpected, colour.getARGBHex());
            } else {
                assertEquals(hexExpected, colour.getARGBHex().substring(2));
            }
        }
    }

    @Test
    public void testRead() throws IOException {
        testRead("WithConditionalFormatting.xls");
    }
    
    @Test
    public void testReadOffice2007() throws IOException {
        testReadOffice2007("NewStyleConditionalFormattings.xls");
    }

    @Test
    public void test53691() throws IOException {
        SheetConditionalFormatting cf;
        final Workbook wb = HSSFITestDataProvider.instance.openSampleWorkbook("53691.xls");
        /*
        FileInputStream s = new FileInputStream("C:\\temp\\53691bbadfixed.xls");
        try {
            wb = new HSSFWorkbook(s);
        } finally {
            s.close();
        }

        wb.removeSheetAt(1);*/
        
        // initially it is good
        writeTemp53691(wb, "agood");
        
        // clone sheet corrupts it
        Sheet sheet = wb.cloneSheet(0);
        writeTemp53691(wb, "bbad");

        // removing the sheet makes it good again
        wb.removeSheetAt(wb.getSheetIndex(sheet));
        writeTemp53691(wb, "cgood");
        
        // cloning again and removing the conditional formatting makes it good again
        sheet = wb.cloneSheet(0);
        removeConditionalFormatting(sheet);        
        writeTemp53691(wb, "dgood");
        
        // cloning the conditional formatting manually makes it bad again
        cf = sheet.getSheetConditionalFormatting();
        SheetConditionalFormatting scf = wb.getSheetAt(0).getSheetConditionalFormatting();
        for (int j = 0; j < scf.getNumConditionalFormattings(); j++) {
            cf.addConditionalFormatting(scf.getConditionalFormattingAt(j));
        }        
        writeTemp53691(wb, "ebad");

        // remove all conditional formatting for comparing BIFF output
        removeConditionalFormatting(sheet);        
        removeConditionalFormatting(wb.getSheetAt(0));        
        writeTemp53691(wb, "fgood");
        
        wb.close();
    }
    
    private void removeConditionalFormatting(Sheet sheet) {
        SheetConditionalFormatting cf = sheet.getSheetConditionalFormatting();
        for (int j = 0; j < cf.getNumConditionalFormattings(); j++) {
            cf.removeConditionalFormatting(j);
        }
    }

    private void writeTemp53691(Workbook wb, @SuppressWarnings("UnusedParameters") String suffix) throws IOException {
        // assert that we can write/read it in memory
        Workbook wbBack = HSSFITestDataProvider.instance.writeOutAndReadBack(wb);
        assertNotNull(wbBack);
        wbBack.close();
    }


    @SuppressWarnings("deprecation")
    @Test
    public void test52122() throws Exception {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Conditional Formatting Test");
        sheet.setColumnWidth(0, 256 * 10);
        sheet.setColumnWidth(1, 256 * 10);
        sheet.setColumnWidth(2, 256 * 10);

        // Create some content.
        // row 0
        Row row = sheet.createRow(0);

        Cell cell0 = row.createCell(0);
        cell0.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC);
        cell0.setCellValue(100);

        Cell cell1 = row.createCell(1);
        cell1.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC);
        cell1.setCellValue(120);

        Cell cell2 = row.createCell(2);
        cell2.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC);
        cell2.setCellValue(130);

        // row 1
        row = sheet.createRow(1);

        cell0 = row.createCell(0);
        cell0.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC);
        cell0.setCellValue(200);

        cell1 = row.createCell(1);
        cell1.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC);
        cell1.setCellValue(220);

        cell2 = row.createCell(2);
        cell2.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC);
        cell2.setCellValue(230);

        // row 2
        row = sheet.createRow(2);

        cell0 = row.createCell(0);
        cell0.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC);
        cell0.setCellValue(300);

        cell1 = row.createCell(1);
        cell1.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC);
        cell1.setCellValue(320);

        cell2 = row.createCell(2);
        cell2.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC);
        cell2.setCellValue(330);

        // Create conditional formatting, CELL1 should be yellow if CELL0 is not blank.
        SheetConditionalFormatting formatting = sheet.getSheetConditionalFormatting();

        ConditionalFormattingRule rule = formatting.createConditionalFormattingRule("$A$1>75");

        PatternFormatting pattern = rule.createPatternFormatting();
        pattern.setFillBackgroundColor(IndexedColors.BLUE.index);
        pattern.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        CellRangeAddress[] range = {CellRangeAddress.valueOf("B2:C2")};
        CellRangeAddress[] range2 = {CellRangeAddress.valueOf("B1:C1")};

        formatting.addConditionalFormatting(range, rule);
        formatting.addConditionalFormatting(range2, rule);

        // Write file.
        /*FileOutputStream fos = new FileOutputStream("c:\\temp\\52122_conditional-sheet.xls");
        try {
            workbook.write(fos);
        } finally {
            fos.close();
        }*/

        Workbook wbBack = HSSFTestDataSamples.writeOutAndReadBack((HSSFWorkbook)workbook);
        Sheet sheetBack = wbBack.getSheetAt(0);
        final SheetConditionalFormatting sheetConditionalFormattingBack = sheetBack.getSheetConditionalFormatting();
        assertNotNull(sheetConditionalFormattingBack);
        final ConditionalFormatting formattingBack = sheetConditionalFormattingBack.getConditionalFormattingAt(0);
        assertNotNull(formattingBack);
        final ConditionalFormattingRule ruleBack = formattingBack.getRule(0);
        assertNotNull(ruleBack);
        final PatternFormatting patternFormattingBack1 = ruleBack.getPatternFormatting();
        assertNotNull(patternFormattingBack1);
        assertEquals(IndexedColors.BLUE.index, patternFormattingBack1.getFillBackgroundColor());
        assertEquals(PatternFormatting.SOLID_FOREGROUND, patternFormattingBack1.getFillPattern());
    }
}
