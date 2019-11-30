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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.formula.EvaluationConditionalFormatRule;
import org.apache.poi.ss.formula.WorkbookEvaluatorProvider;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ColorScaleFormatting;
import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.ConditionalFormattingThreshold.RangeType;
import org.apache.poi.ss.usermodel.DataBarFormatting;
import org.apache.poi.ss.usermodel.ExtendedColor;
import org.apache.poi.ss.usermodel.FontFormatting;
import org.apache.poi.ss.usermodel.IconMultiStateFormatting;
import org.apache.poi.ss.usermodel.IconMultiStateFormatting.IconSet;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Excel Conditional Formatting -- Examples
 *
 * <p>
 *   Partly based on the code snippets from
 *   http://www.contextures.com/xlcondformat03.html
 * </p>
 */
public class ConditionalFormats {

    /**
     * generates a sample workbook with conditional formatting,
     * and prints out a summary of applied formats for one sheet
     * @param args pass "-xls" to generate an HSSF workbook, default is XSSF
     */
    public static void main(String[] args) throws IOException {
        final boolean isHSSF = args.length > 0 && args[0].equals("-xls");
        try (Workbook wb = isHSSF ? new HSSFWorkbook() : new XSSFWorkbook()) {

            sameCell(wb.createSheet("Same Cell"));
            multiCell(wb.createSheet("MultiCell"));
            overlapping(wb.createSheet("Overlapping"));
            errors(wb.createSheet("Errors"));
            hideDupplicates(wb.createSheet("Hide Dups"));
            formatDuplicates(wb.createSheet("Duplicates"));
            inList(wb.createSheet("In List"));
            expiry(wb.createSheet("Expiry"));
            shadeAlt(wb.createSheet("Shade Alt"));
            shadeBands(wb.createSheet("Shade Bands"));
            iconSets(wb.createSheet("Icon Sets"));
            colourScales(wb.createSheet("Colour Scales"));
            dataBars(wb.createSheet("Data Bars"));

            // print overlapping rule results
            evaluateRules(wb, "Overlapping");

            // Write the output to a file
            String file = "cf-poi.xls";
            if (wb instanceof XSSFWorkbook) {
                file += "x";
            }
            try (FileOutputStream out = new FileOutputStream(file)) {
                wb.write(out);
            }
            System.out.println("Generated: " + file);
        }
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
     * Multiple conditional formatting rules can apply to
     *  one cell, some combining, some beating others.
     * Done in order of the rules added to the
     *  SheetConditionalFormatting object
     */
    static void overlapping(Sheet sheet) {
        for (int i=0; i<40; i++) {
            int rn = i+1;
            Row r = sheet.createRow(i);
            r.createCell(0).setCellValue("This is row " + rn + " (" + i + ")");
            String str = "";
            if (rn%2 == 0) {
                str = str + "even ";
            }
            if (rn%3 == 0) {
                str = str + "x3 ";
            }
            if (rn%5 == 0) {
                str = str + "x5 ";
            }
            if (rn%10 == 0) {
                str = str + "x10 ";
            }
            if (str.length() == 0) {
                str = "nothing special...";
            }
            r.createCell(1).setCellValue("It is " + str);
        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        sheet.getRow(1).createCell(3).setCellValue("Even rows are blue");
        sheet.getRow(2).createCell(3).setCellValue("Multiples of 3 have a grey background");
        sheet.getRow(4).createCell(3).setCellValue("Multiples of 5 are bold");
        sheet.getRow(9).createCell(3).setCellValue("Multiples of 10 are red (beats even)");

        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        // Condition 1: Row divides by 10, red (will beat #1)
        ConditionalFormattingRule rule1 =
                sheetCF.createConditionalFormattingRule("MOD(ROW(),10)=0");
        FontFormatting font1 = rule1.createFontFormatting();
        font1.setFontColorIndex(IndexedColors.RED.index);

        // Condition 2: Row is even, blue
        ConditionalFormattingRule rule2 =
                sheetCF.createConditionalFormattingRule("MOD(ROW(),2)=0");
        FontFormatting font2 = rule2.createFontFormatting();
        font2.setFontColorIndex(IndexedColors.BLUE.index);

        // Condition 3: Row divides by 5, bold
        ConditionalFormattingRule rule3 =
                sheetCF.createConditionalFormattingRule("MOD(ROW(),5)=0");
        FontFormatting font3 = rule3.createFontFormatting();
        font3.setFontStyle(false, true);

        // Condition 4: Row divides by 3, grey background
        ConditionalFormattingRule rule4 =
                sheetCF.createConditionalFormattingRule("MOD(ROW(),3)=0");
        PatternFormatting fill4 = rule4.createPatternFormatting();
        fill4.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.index);
        fill4.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        // Apply
        CellRangeAddress[] regions = {
                CellRangeAddress.valueOf("A1:F41")
        };

        sheetCF.addConditionalFormatting(regions, rule1);
        sheetCF.addConditionalFormatting(regions, rule2);
        sheetCF.addConditionalFormatting(regions, rule3);
        sheetCF.addConditionalFormatting(regions, rule4);
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

        for(int rownum = 1; rownum <= 3; rownum++) {
            sheet.getRow(rownum).getCell(0).setCellStyle(style);
        }

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

    /**
     * Icon Sets / Multi-States allow you to have icons shown which vary
     *  based on the values, eg Red traffic light / Yellow traffic light /
     *  Green traffic light
     */
    static void iconSets(Sheet sheet) {
        sheet.createRow(0).createCell(0).setCellValue("Icon Sets");
        Row r = sheet.createRow(1);
        r.createCell(0).setCellValue("Reds");
        r.createCell(1).setCellValue(0);
        r.createCell(2).setCellValue(0);
        r.createCell(3).setCellValue(0);
        r = sheet.createRow(2);
        r.createCell(0).setCellValue("Yellows");
        r.createCell(1).setCellValue(5);
        r.createCell(2).setCellValue(5);
        r.createCell(3).setCellValue(5);
        r = sheet.createRow(3);
        r.createCell(0).setCellValue("Greens");
        r.createCell(1).setCellValue(10);
        r.createCell(2).setCellValue(10);
        r.createCell(3).setCellValue(10);

        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        CellRangeAddress[] regions = { CellRangeAddress.valueOf("B1:B4") };
        ConditionalFormattingRule rule1 =
                sheetCF.createConditionalFormattingRule(IconSet.GYR_3_TRAFFIC_LIGHTS);
        IconMultiStateFormatting im1 = rule1.getMultiStateFormatting();
        im1.getThresholds()[0].setRangeType(RangeType.MIN);
        im1.getThresholds()[1].setRangeType(RangeType.PERCENT);
        im1.getThresholds()[1].setValue(33d);
        im1.getThresholds()[2].setRangeType(RangeType.MAX);
        sheetCF.addConditionalFormatting(regions, rule1);

        regions = new CellRangeAddress[] { CellRangeAddress.valueOf("C1:C4") };
        ConditionalFormattingRule rule2 =
                sheetCF.createConditionalFormattingRule(IconSet.GYR_3_FLAGS);
        IconMultiStateFormatting im2 = rule1.getMultiStateFormatting();
        im2.getThresholds()[0].setRangeType(RangeType.PERCENT);
        im2.getThresholds()[0].setValue(0d);
        im2.getThresholds()[1].setRangeType(RangeType.PERCENT);
        im2.getThresholds()[1].setValue(33d);
        im2.getThresholds()[2].setRangeType(RangeType.PERCENT);
        im2.getThresholds()[2].setValue(67d);
        sheetCF.addConditionalFormatting(regions, rule2);

        regions = new CellRangeAddress[] { CellRangeAddress.valueOf("D1:D4") };
        ConditionalFormattingRule rule3 =
                sheetCF.createConditionalFormattingRule(IconSet.GYR_3_SYMBOLS_CIRCLE);
        IconMultiStateFormatting im3 = rule1.getMultiStateFormatting();
        im3.setIconOnly(true);
        im3.getThresholds()[0].setRangeType(RangeType.MIN);
        im3.getThresholds()[1].setRangeType(RangeType.NUMBER);
        im3.getThresholds()[1].setValue(3d);
        im3.getThresholds()[2].setRangeType(RangeType.NUMBER);
        im3.getThresholds()[2].setValue(7d);
        sheetCF.addConditionalFormatting(regions, rule3);
    }

    /**
     * Color Scales / Colour Scales / Colour Gradients allow you shade the
     *  background colour of the cell based on the values, eg from Red to
     *  Yellow to Green.
     */
    static void colourScales(Sheet sheet) {
        sheet.createRow(0).createCell(0).setCellValue("Colour Scales");
        Row r = sheet.createRow(1);
        r.createCell(0).setCellValue("Red-Yellow-Green");
        for (int i=1; i<=7; i++) {
            r.createCell(i).setCellValue((i-1)*5.0);
        }
        r = sheet.createRow(2);
        r.createCell(0).setCellValue("Red-White-Blue");
        for (int i=1; i<=9; i++) {
            r.createCell(i).setCellValue((i-1)*5.0);
        }
        r = sheet.createRow(3);
        r.createCell(0).setCellValue("Blue-Green");
        for (int i=1; i<=16; i++) {
            r.createCell(i).setCellValue((i-1));
        }
        sheet.setColumnWidth(0, 5000);

        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        CellRangeAddress[] regions = { CellRangeAddress.valueOf("B2:H2") };
        ConditionalFormattingRule rule1 =
                sheetCF.createConditionalFormattingColorScaleRule();
        ColorScaleFormatting cs1 = rule1.getColorScaleFormatting();
        cs1.getThresholds()[0].setRangeType(RangeType.MIN);
        cs1.getThresholds()[1].setRangeType(RangeType.PERCENTILE);
        cs1.getThresholds()[1].setValue(50d);
        cs1.getThresholds()[2].setRangeType(RangeType.MAX);
        ((ExtendedColor)cs1.getColors()[0]).setARGBHex("FFF8696B");
        ((ExtendedColor)cs1.getColors()[1]).setARGBHex("FFFFEB84");
        ((ExtendedColor)cs1.getColors()[2]).setARGBHex("FF63BE7B");
        sheetCF.addConditionalFormatting(regions, rule1);

        regions = new CellRangeAddress[] { CellRangeAddress.valueOf("B3:J3") };
        ConditionalFormattingRule rule2 =
                sheetCF.createConditionalFormattingColorScaleRule();
        ColorScaleFormatting cs2 = rule2.getColorScaleFormatting();
        cs2.getThresholds()[0].setRangeType(RangeType.MIN);
        cs2.getThresholds()[1].setRangeType(RangeType.PERCENTILE);
        cs2.getThresholds()[1].setValue(50d);
        cs2.getThresholds()[2].setRangeType(RangeType.MAX);
        ((ExtendedColor)cs2.getColors()[0]).setARGBHex("FFF8696B");
        ((ExtendedColor)cs2.getColors()[1]).setARGBHex("FFFCFCFF");
        ((ExtendedColor)cs2.getColors()[2]).setARGBHex("FF5A8AC6");
        sheetCF.addConditionalFormatting(regions, rule2);

        regions = new CellRangeAddress[] { CellRangeAddress.valueOf("B4:Q4") };
        ConditionalFormattingRule rule3=
                sheetCF.createConditionalFormattingColorScaleRule();
        ColorScaleFormatting cs3 = rule3.getColorScaleFormatting();
        cs3.setNumControlPoints(2);
        cs3.getThresholds()[0].setRangeType(RangeType.MIN);
        cs3.getThresholds()[1].setRangeType(RangeType.MAX);
        ((ExtendedColor)cs3.getColors()[0]).setARGBHex("FF5A8AC6");
        ((ExtendedColor)cs3.getColors()[1]).setARGBHex("FF63BE7B");
        sheetCF.addConditionalFormatting(regions, rule3);
    }

    /**
     * DataBars / Data-Bars allow you to have bars shown vary
     *  based on the values, from full to empty
     */
    static void dataBars(Sheet sheet) {
        sheet.createRow(0).createCell(0).setCellValue("Data Bars");
        Row r = sheet.createRow(1);
        r.createCell(1).setCellValue("Green Positive");
        r.createCell(2).setCellValue("Blue Mix");
        r.createCell(3).setCellValue("Red Negative");
        r = sheet.createRow(2);
        r.createCell(1).setCellValue(0);
        r.createCell(2).setCellValue(0);
        r.createCell(3).setCellValue(0);
        r = sheet.createRow(3);
        r.createCell(1).setCellValue(5);
        r.createCell(2).setCellValue(-5);
        r.createCell(3).setCellValue(-5);
        r = sheet.createRow(4);
        r.createCell(1).setCellValue(10);
        r.createCell(2).setCellValue(10);
        r.createCell(3).setCellValue(-10);
        r = sheet.createRow(5);
        r.createCell(1).setCellValue(5);
        r.createCell(2).setCellValue(5);
        r.createCell(3).setCellValue(-5);
        r = sheet.createRow(6);
        r.createCell(1).setCellValue(20);
        r.createCell(2).setCellValue(-10);
        r.createCell(3).setCellValue(-20);
        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 5000);
        sheet.setColumnWidth(2, 5000);
        sheet.setColumnWidth(3, 5000);

        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        ExtendedColor color = sheet.getWorkbook().getCreationHelper().createExtendedColor();
        color.setARGBHex("FF63BE7B");
        CellRangeAddress[] regions = { CellRangeAddress.valueOf("B2:B7") };
        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(color);
        DataBarFormatting db1 = rule1.getDataBarFormatting();
        db1.getMinThreshold().setRangeType(RangeType.MIN);
        db1.getMaxThreshold().setRangeType(RangeType.MAX);
        sheetCF.addConditionalFormatting(regions, rule1);

        color = sheet.getWorkbook().getCreationHelper().createExtendedColor();
        color.setARGBHex("FF5A8AC6");
        regions = new CellRangeAddress[] { CellRangeAddress.valueOf("C2:C7") };
        ConditionalFormattingRule rule2 = sheetCF.createConditionalFormattingRule(color);
        DataBarFormatting db2 = rule2.getDataBarFormatting();
        db2.getMinThreshold().setRangeType(RangeType.MIN);
        db2.getMaxThreshold().setRangeType(RangeType.MAX);
        sheetCF.addConditionalFormatting(regions, rule2);

        color = sheet.getWorkbook().getCreationHelper().createExtendedColor();
        color.setARGBHex("FFF8696B");
        regions = new CellRangeAddress[] { CellRangeAddress.valueOf("D2:D7") };
        ConditionalFormattingRule rule3 = sheetCF.createConditionalFormattingRule(color);
        DataBarFormatting db3 = rule3.getDataBarFormatting();
        db3.getMinThreshold().setRangeType(RangeType.MIN);
        db3.getMaxThreshold().setRangeType(RangeType.MAX);
        sheetCF.addConditionalFormatting(regions, rule3);
    }

    /**
     * Print out a summary of the conditional formatting rules applied to cells on the given sheet.
     * Only cells with a matching rule are printed, and for those, all matching rules are sumarized.
     */
    static void evaluateRules(Workbook wb, String sheetName) {
        final WorkbookEvaluatorProvider wbEvalProv = (WorkbookEvaluatorProvider) wb.getCreationHelper().createFormulaEvaluator();
        final ConditionalFormattingEvaluator cfEval = new ConditionalFormattingEvaluator(wb, wbEvalProv);
        // if cell values have changed, clear cached format results
        cfEval.clearAllCachedValues();

        final Sheet sheet = wb.getSheet(sheetName);
        for (Row r : sheet) {
            for (Cell c : r) {
                final List<EvaluationConditionalFormatRule> rules = cfEval.getConditionalFormattingForCell(c);
                // check rules list for null, although current implementation will return an empty list, not null, then do what you want with results
                if (rules == null || rules.isEmpty()) {
                    continue;
                }
                final CellReference ref = ConditionalFormattingEvaluator.getRef(c);
                if (rules.isEmpty()) {
                    continue;
                }

                System.out.println("\n"
                  + ref.formatAsString()
                  + " has conditional formatting.");

                for (EvaluationConditionalFormatRule rule : rules) {
                    ConditionalFormattingRule cf = rule.getRule();

                    StringBuilder b = new StringBuilder();
                    b.append("\tRule ")
                     .append(rule.getFormattingIndex())
                     .append(": ");

                    // check for color scale
                    if (cf.getColorScaleFormatting() != null) {
                        b.append("\n\t\tcolor scale (caller must calculate bucket)");
                    }
                    // check for data bar
                    if (cf.getDataBarFormatting() != null) {
                        b.append("\n\t\tdata bar (caller must calculate bucket)");
                    }
                    // check for icon set
                    if (cf.getMultiStateFormatting() != null) {
                        b.append("\n\t\ticon set (caller must calculate icon bucket)");
                    }
                    // check for fill
                    if (cf.getPatternFormatting() != null) {
                        final PatternFormatting fill = cf.getPatternFormatting();
                        b.append("\n\t\tfill pattern ")
                         .append(fill.getFillPattern())
                         .append(" color index ")
                         .append(fill.getFillBackgroundColor());
                    }
                    // font stuff
                    if (cf.getFontFormatting() != null) {
                        final FontFormatting ff = cf.getFontFormatting();
                        b.append("\n\t\tfont format ")
                         .append("color index ")
                         .append(ff.getFontColorIndex());
                        if (ff.isBold()) {
                            b.append(" bold");
                        }
                        if (ff.isItalic()) {
                            b.append(" italic");
                        }
                        if (ff.isStruckout()) {
                            b.append(" strikeout");
                        }
                        b.append(" underline index ")
                         .append(ff.getUnderlineType());
                    }

                    System.out.println(b);
                }
            }
        }
    }
}
