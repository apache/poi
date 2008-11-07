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
package org.apache.poi.xssf.usermodel.examples;

import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.*;

import java.util.Map;
import java.util.HashMap;
import java.io.FileOutputStream;

/**
 * A weekly timesheet created using Apache POI.
 *
 * @author Yegor Kozlov
 */
public class TimesheetDemo {
    private static final String[] titles = {
            "Person",	"ID", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun",
            "Total\nHrs", "Overtime\nHrs", "Regular\nHrs"
    };

    private static Object[][] sample_data = {
            {"Yegor Kozlov", "YK", 5.0, 8.0, 10.0, 5.0, 5.0, 7.0, 6.0},
            {"Gisella Bronsetti", "GB", 4.0, 3.0, 1.0, 3.5, null, null, 4.0},
    };

    public static void main(String[] args) throws Exception {

        XSSFWorkbook wb = new XSSFWorkbook();
        Map<String, XSSFCellStyle> styles = createStyles(wb);

        XSSFSheet sheet = wb.createSheet("Timesheet");
        XSSFPrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setOrientation(PrintOrientation.LANDSCAPE);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);

        //title row
        XSSFRow titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(45);
        XSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Weekly Timesheet");
        titleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$L$1"));

        //header row
        XSSFRow headerRow = sheet.createRow(1);
        headerRow.setHeightInPoints(40);
        XSSFCell headerCell;
        for (int i = 0; i < titles.length; i++) {
            headerCell = headerRow.createCell(i);
            headerCell.setCellValue(titles[i]);
            headerCell.setCellStyle(styles.get("header"));
        }

        int rownum = 2;
        for (int i = 0; i < 10; i++) {
            XSSFRow row = sheet.createRow(rownum++);
            for (int j = 0; j < titles.length; j++) {
                XSSFCell cell = row.createCell(j);
                if(j == 9){
                    //the 10th cell contains sum over week days, e.g. SUM(C3:I3)
                    String ref = "C" +rownum+ ":I" + rownum;
                    cell.setCellFormula("SUM("+ref+")");
                    cell.setCellStyle(styles.get("formula"));
                } else if (j == 11){
                    cell.setCellFormula("J" +rownum+ "-K" + rownum);
                    cell.setCellStyle(styles.get("formula"));
                } else {
                    cell.setCellStyle(styles.get("cell"));
                }
            }
        }

        //row with totals below
        XSSFRow sumRow = sheet.createRow(rownum++);
        sumRow.setHeightInPoints(35);
        XSSFCell cell;
        cell = sumRow.createCell(0);
        cell.setCellStyle(styles.get("formula"));
        cell = sumRow.createCell(1);
        cell.setCellValue("Total Hrs:");
        cell.setCellStyle(styles.get("formula"));

        for (int j = 2; j < 12; j++) {
            cell = sumRow.createCell(j);
            String ref = (char)('A' + j) + "3:" + (char)('A' + j) + "12";
            cell.setCellFormula("SUM(" + ref + ")");
            if(j >= 9) cell.setCellStyle(styles.get("formula_2"));
            else cell.setCellStyle(styles.get("formula"));
        }
        rownum++;
        sumRow = sheet.createRow(rownum++);
        sumRow.setHeightInPoints(25);
        cell = sumRow.createCell(0);
        cell.setCellValue("Total Regular Hours");
        cell.setCellStyle(styles.get("formula"));
        cell = sumRow.createCell(1);
        cell.setCellFormula("L13");
        cell.setCellStyle(styles.get("formula_2"));
        sumRow = sheet.createRow(rownum++);
        sumRow.setHeightInPoints(25);
        cell = sumRow.createCell(0);
        cell.setCellValue("Total Overtime Hours");
        cell.setCellStyle(styles.get("formula"));
        cell = sumRow.createCell(1);
        cell.setCellFormula("K13");
        cell.setCellStyle(styles.get("formula_2"));

        //set sample data
        for (int i = 0; i < sample_data.length; i++) {
            XSSFRow row = sheet.getRow(2 + i);
            for (int j = 0; j < sample_data[i].length; j++) {
                if(sample_data[i][j] == null) continue;

                if(sample_data[i][j] instanceof String) {
                    row.getCell(j).setCellValue((String)sample_data[i][j]);
                } else {
                    row.getCell(j).setCellValue((Double)sample_data[i][j]);
                }
            }
        }

        //finally set column widths
        sheet.setColumnWidth(0, 30*256);
        for (int i = 2; i < 9; i++) {
            sheet.setColumnWidth(i, 6*256);
        }

        // Write the output to a file
        FileOutputStream out = new FileOutputStream("ooxml-timesheet.xlsx");
        wb.write(out);
        out.close();
    }

    private static Map<String, XSSFCellStyle> createStyles(XSSFWorkbook wb){
        Map<String, XSSFCellStyle> styles = new HashMap<String, XSSFCellStyle>();
        XSSFCellStyle style;
        XSSFFont titleFont = wb.createFont();
        titleFont.setFontHeightInPoints((short)18);
        titleFont.setBold(true);
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(234, 234, 234)));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(titleFont);
        styles.put("title", style);

        XSSFFont monthFont = wb.createFont();
        monthFont.setFontHeightInPoints((short)11);
        monthFont.setColor(new XSSFColor(new java.awt.Color(255, 255, 255)));
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(102, 102, 102)));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(monthFont);
        style.setWrapText(true);
        styles.put("header", style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setWrapText(true);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        styles.put("cell", style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(234, 234, 234)));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setDataFormat(wb.createDataFormat().getFormat("0.00"));
        styles.put("formula", style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(192, 192, 192)));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setDataFormat(wb.createDataFormat().getFormat("0.00"));
        styles.put("formula_2", style);

        return styles;
    }
}
