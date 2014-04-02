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

package org.apache.poi.ss.examples;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Excel Conditional Formatting -- Examples
 *
 * <p>
 *   Based on the code snippets from http://www.contextures.com/xlcondformat03.html
 * </p>
 *
 * @author Yegor Kozlov
 */
public class ConditionalFormats {

    public static void main(String[] args) throws IOException {
        Workbook wb;

        if(args.length > 0 && args[0].equals("-xls")) wb = new HSSFWorkbook();
        else wb = new XSSFWorkbook();

        sameCell(wb.createSheet("Same Cell"));
        multiCell(wb.createSheet("MultiCell"));
        errors(wb.createSheet("Errors"));
        hideDupplicates(wb.createSheet("Hide Dups"));
        formatDuplicates(wb.createSheet("Duplicates"));
        inList(wb.createSheet("In List"));
        expiry(wb.createSheet("Expiry"));
        shadeAlt(wb.createSheet("Shade Alt"));
        shadeBands(wb.createSheet("Shade Bands"));

        // Write the output to a file
        String file = "cf-poi.xls";
        if(wb instanceof XSSFWorkbook) file += "x";
        FileOutputStream out = new FileOutputStream(file);
        wb.write(out);
        out.close();

    }

    /**
     * Highlight cells based on their values
     */
    static void sameCell(Sheet sheet) {
        sheet.createRow(0).createCell(0).setCellValue(84);
        sheet.createRow(1).createCell(0).setCellValue(74);
        sheet.createRow(2).createCell(0).setCellValue(50);
        sheet.createRow(3).createCell(0).setCellValue(51);
        sheet.createRow(4).createCell(0).setCellValue(49);
        sheet.createRow(5).createCell(0).setCellValue(41);

        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        // Condition 1: Cell Value Is   greater than  70   (Blue Fill)
        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(ComparisonOperator.GT, "70");
        PatternFormatting fill1 = rule1.createPatternFormatting();
        fill1.setFillBackgroundColor(IndexedColors.BLUE.index);
        fill1.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        // Condition 2: Cell Value Is  less than      50   (Green Fill)
        ConditionalFormattingRule rule2 = sheetCF.createConditionalFormattingRule(ComparisonOperator.LT, "50");
        PatternFormatting fill2 = rule2.createPatternFormatting();
        fill2.setFillBackgroundColor(IndexedColors.GREEN.index);
        fill2.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        CellRangeAddress[] regions = {
                CellRangeAddress.valueOf("A1:A6")
        };

        sheetCF.addConditionalFormatting(regions, rule1, rule2);

        sheet.getRow(0).createCell(2).setCellValue("<== Condition 1: Cell Value Is greater than 70 (Blue Fill)");
        sheet.getRow(4).createCell(2).setCellValue("<== Condition 2: Cell Value Is less than 50 (Green Fill)");
    }

    /**
     * Highlight multiple cells based on a formula
     */
    static void multiCell(Sheet sheet) {
        // header row
        Row row0 = sheet.createRow(0);
        row0.createCell(0).setCellValue("Units");
        row0.createCell(1).setCellValue("Cost");
        row0.createCell(2).setCellValue("Total");

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue(71);
        row1.createCell(1).setCellValue(29);
        row1.createCell(2).setCellValue(2059);

        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue(85);
        row2.createCell(1).setCellValue(29);
        row2.createCell(2).setCellValue(2059);

        Row row3 = sheet.createRow(3);
        row3.createCell(0).setCellValue(71);
        row3.createCell(1).setCellValue(29);
        row3.createCell(2).setCellValue(2059);

        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        // Condition 1: Formula Is   =$B2>75   (Blue Fill)
        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule("$A2>75");
        PatternFormatting fill1 = rule1.createPatternFormatting();
        fill1.setFillBackgroundColor(IndexedColors.BLUE.index);
        fill1.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        CellRangeAddress[] regions = {
                CellRangeAddress.valueOf("A2:C4")
        };

        sheetCF.addConditionalFormatting(regions, rule1);

        sheet.getRow(2).createCell(4).setCellValue("<== Condition 1: Formula Is =$B2>75   (Blue Fill)");
    }

    /**
     *  Use Excel conditional formatting to check for errors,
     *  and change the font colour to match the cell colour.
     *  In this example, if formula result is  #DIV/0! then it will have white font colour.
     */
    static void errors(Sheet sheet) {
        sheet.createRow(0).createCell(0).setCellValue(84);
        sheet.createRow(1).createCell(0).setCellValue(0);
        sheet.createRow(2).createCell(0).setCellFormula("ROUND(A1/A2,0)");
        sheet.createRow(3).createCell(0).setCellValue(0);
        sheet.createRow(4).createCell(0).setCellFormula("ROUND(A6/A4,0)");
        sheet.createRow(5).createCell(0).setCellValue(41);

        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        // Condition 1: Formula Is   =ISERROR(C2)   (White Font)
        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule("ISERROR(A1)");
        FontFormatting font = rule1.createFontFormatting();
        font.setFontColorIndex(IndexedColors.WHITE.index);

        CellRangeAddress[] regions = {
                CellRangeAddress.valueOf("A1:A6")
        };

        sheetCF.addConditionalFormatting(regions, rule1);

        sheet.getRow(2).createCell(1).setCellValue("<== The error in this cell is hidden. Condition: Formula Is   =ISERROR(C2)   (White Font)");
        sheet.getRow(4).createCell(1).setCellValue("<== The error in this cell is hidden. Condition: Formula Is   =ISERROR(C2)   (White Font)");
    }

    /**
     * Use Excel conditional formatting to hide the duplicate values,
     * and make the list easier to read. In this example, when the table is sorted by Region,
     * the second (and subsequent) occurences of each region name will have white font colour.
     */
    static void hideDupplicates(Sheet sheet) {
        sheet.createRow(0).createCell(0).setCellValue("City");
        sheet.createRow(1).createCell(0).setCellValue("Boston");
        sheet.createRow(2).createCell(0).setCellValue("Boston");
        sheet.createRow(3).createCell(0).setCellValue("Chicago");
        sheet.createRow(4).createCell(0).setCellValue("Chicago");
        sheet.createRow(5).createCell(0).setCellValue("New York");

        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        // Condition 1: Formula Is   =A2=A1   (White Font)
        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule("A2=A1");
        FontFormatting font = rule1.createFontFormatting();
        font.setFontColorIndex(IndexedColors.WHITE.index);

        CellRangeAddress[] regions = {
                CellRangeAddress.valueOf("A2:A6")
        };

        sheetCF.addConditionalFormatting(regions, rule1);

        sheet.getRow(1).createCell(1).setCellValue("<== the second (and subsequent) " +
                "occurences of each region name will have white font colour.  " +
                "Condition: Formula Is   =A2=A1   (White Font)");
    }

    /**
     * Use Excel conditional formatting to highlight duplicate entries in a column.
     */
    static void formatDuplicates(Sheet sheet) {
        sheet.createRow(0).createCell(0).setCellValue("Code");
        sheet.createRow(1).createCell(0).setCellValue(4);
        sheet.createRow(2).createCell(0).setCellValue(3);
        sheet.createRow(3).createCell(0).setCellValue(6);
        sheet.createRow(4).createCell(0).setCellValue(3);
        sheet.createRow(5).createCell(0).setCellValue(5);
        sheet.createRow(6).createCell(0).setCellValue(8);
        sheet.createRow(7).createCell(0).setCellValue(0);
        sheet.createRow(8).createCell(0).setCellValue(2);
        sheet.createRow(9).createCell(0).setCellValue(8);
        sheet.createRow(10).createCell(0).setCellValue(6);

        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        // Condition 1: Formula Is   =A2=A1   (White Font)
        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule("COUNTIF($A$2:$A$11,A2)>1");
        FontFormatting font = rule1.createFontFormatting();
        font.setFontStyle(false, true);
        font.setFontColorIndex(IndexedColors.BLUE.index);

        CellRangeAddress[] regions = {
                CellRangeAddress.valueOf("A2:A11")
        };

        sheetCF.addConditionalFormatting(regions, rule1);

        sheet.getRow(2).createCell(1).setCellValue("<== Duplicates numbers in the column are highlighted.  " +
                "Condition: Formula Is =COUNTIF($A$2:$A$11,A2)>1   (Blue Font)");
    }

    /**
     * Use Excel conditional formatting to highlight items that are in a list on the worksheet.
     */
    static void inList(Sheet sheet) {
        sheet.createRow(0).createCell(0).setCellValue("Codes");
        sheet.createRow(1).createCell(0).setCellValue("AA");
        sheet.createRow(2).createCell(0).setCellValue("BB");
        sheet.createRow(3).createCell(0).setCellValue("GG");
        sheet.createRow(4).createCell(0).setCellValue("AA");
        sheet.createRow(5).createCell(0).setCellValue("FF");
        sheet.createRow(6).createCell(0).setCellValue("XX");
        sheet.createRow(7).createCell(0).setCellValue("CC");

        sheet.getRow(0).createCell(2).setCellValue("Valid");
        sheet.getRow(1).createCell(2).setCellValue("AA");
        sheet.getRow(2).createCell(2).setCellValue("BB");
        sheet.getRow(3).createCell(2).setCellValue("CC");

        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        // Condition 1: Formula Is   =A2=A1   (White Font)
        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule("COUNTIF($C$2:$C$4,A2)");
        PatternFormatting fill1 = rule1.createPatternFormatting();
        fill1.setFillBackgroundColor(IndexedColors.LIGHT_BLUE.index);
        fill1.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        CellRangeAddress[] regions = {
                CellRangeAddress.valueOf("A2:A8")
        };

        sheetCF.addConditionalFormatting(regions, rule1);

        sheet.getRow(2).createCell(3).setCellValue("<== Use Excel conditional formatting to highlight items that are in a list on the worksheet");
    }

    /**
     *  Use Excel conditional formatting to highlight payments that are due in the next thirty days.
     *  In this example, Due dates are entered in cells A2:A4.
     */
    static void expiry(Sheet sheet) {
        CellStyle style = sheet.getWorkbook().createCellStyle();
        style.setDataFormat((short)BuiltinFormats.getBuiltinFormat("d-mmm"));

        sheet.createRow(0).createCell(0).setCellValue("Date");
        sheet.createRow(1).createCell(0).setCellFormula("TODAY()+29");
        sheet.createRow(2).createCell(0).setCellFormula("A2+1");
        sheet.createRow(3).createCell(0).setCellFormula("A3+1");

        for(int rownum = 1; rownum <= 3; rownum++) sheet.getRow(rownum).getCell(0).setCellStyle(style);

        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        // Condition 1: Formula Is   =A2=A1   (White Font)
        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule("AND(A2-TODAY()>=0,A2-TODAY()<=30)");
        FontFormatting font = rule1.createFontFormatting();
        font.setFontStyle(false, true);
        font.setFontColorIndex(IndexedColors.BLUE.index);

        CellRangeAddress[] regions = {
                CellRangeAddress.valueOf("A2:A4")
        };

        sheetCF.addConditionalFormatting(regions, rule1);

        sheet.getRow(0).createCell(1).setCellValue("Dates within the next 30 days are highlighted");
    }

    /**
     * Use Excel conditional formatting to shade alternating rows on the worksheet
     */
    static void shadeAlt(Sheet sheet) {
        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        // Condition 1: Formula Is   =A2=A1   (White Font)
        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule("MOD(ROW(),2)");
        PatternFormatting fill1 = rule1.createPatternFormatting();
        fill1.setFillBackgroundColor(IndexedColors.LIGHT_GREEN.index);
        fill1.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        CellRangeAddress[] regions = {
                CellRangeAddress.valueOf("A1:Z100")
        };

        sheetCF.addConditionalFormatting(regions, rule1);

        sheet.createRow(0).createCell(1).setCellValue("Shade Alternating Rows");
        sheet.createRow(1).createCell(1).setCellValue("Condition: Formula Is  =MOD(ROW(),2)   (Light Green Fill)");
    }

    /**
     * You can use Excel conditional formatting to shade bands of rows on the worksheet. 
     * In this example, 3 rows are shaded light grey, and 3 are left with no shading.
     * In the MOD function, the total number of rows in the set of banded rows (6) is entered.
     */
    static void shadeBands(Sheet sheet) {
        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule("MOD(ROW(),6)<3");
        PatternFormatting fill1 = rule1.createPatternFormatting();
        fill1.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.index);
        fill1.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        CellRangeAddress[] regions = {
                CellRangeAddress.valueOf("A1:Z100")
        };

        sheetCF.addConditionalFormatting(regions, rule1);

        sheet.createRow(0).createCell(1).setCellValue("Shade Bands of Rows");
        sheet.createRow(1).createCell(1).setCellValue("Condition: Formula Is  =MOD(ROW(),6)<2   (Light Grey Fill)");
    }
}
