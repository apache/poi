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

import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Map;
import java.util.HashMap;

/**
 * A  monthly calendar created using Apache POI. Each month is on a separate sheet.
 * This is a version of org.apache.poi.ss.examples.CalendarDemo that demonstrates
 * some XSSF features not avaiable when using common HSSF-XSSF interfaces.
 *
 * <pre>
 * Usage:
 * CalendarDemo <year>
 * </pre>
 *
 * @author Yegor Kozlov
 */
public class CalendarDemo {

    private static final String[] days = {
            "Sunday", "Monday", "Tuesday",
            "Wednesday", "Thursday", "Friday", "Saturday"};

    private static final String[]  months = {
            "January", "February", "March","April", "May", "June","July", "August",
            "September","October", "November", "December"};

    public static void main(String[] args) throws Exception {

        Calendar calendar = Calendar.getInstance();
        if(args.length > 0) calendar.set(Calendar.YEAR, Integer.parseInt(args[0]));

        int year = calendar.get(Calendar.YEAR);

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Map<String, XSSFCellStyle> styles = createStyles(wb);

            for (int month = 0; month < 12; month++) {
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                //create a sheet for each month
                XSSFSheet sheet = wb.createSheet(months[month]);

                //turn off gridlines
                sheet.setDisplayGridlines(false);
                sheet.setPrintGridlines(false);
                XSSFPrintSetup printSetup = sheet.getPrintSetup();
                printSetup.setOrientation(PrintOrientation.LANDSCAPE);
                sheet.setFitToPage(true);
                sheet.setHorizontallyCenter(true);

                //the header row: centered text in 48pt font
                XSSFRow headerRow = sheet.createRow(0);
                headerRow.setHeightInPoints(80);
                XSSFCell titleCell = headerRow.createCell(0);
                titleCell.setCellValue(months[month] + " " + year);
                titleCell.setCellStyle(styles.get("title"));
                sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$N$1"));

                //header with month titles
                XSSFRow monthRow = sheet.createRow(1);
                for (int i = 0; i < days.length; i++) {
                    //for compatibility with HSSF we have to set column width in units of 1/256th of a character width
                    sheet.setColumnWidth(i * 2, 5 * 256); //the column is 5 characters wide
                    sheet.setColumnWidth(i * 2 + 1, 13 * 256); //the column is 13 characters wide
                    sheet.addMergedRegion(new CellRangeAddress(1, 1, i * 2, i * 2 + 1));
                    XSSFCell monthCell = monthRow.createCell(i * 2);
                    monthCell.setCellValue(days[i]);
                    monthCell.setCellStyle(styles.get("month"));
                }

                int cnt = 1, day = 1;
                int rownum = 2;
                for (int j = 0; j < 6; j++) {
                    XSSFRow row = sheet.createRow(rownum++);
                    row.setHeightInPoints(100);
                    for (int i = 0; i < days.length; i++) {
                        XSSFCell dayCell_1 = row.createCell(i * 2);
                        XSSFCell dayCell_2 = row.createCell(i * 2 + 1);

                        int day_of_week = calendar.get(Calendar.DAY_OF_WEEK);
                        if (cnt >= day_of_week && calendar.get(Calendar.MONTH) == month) {
                            dayCell_1.setCellValue(day);
                            calendar.set(Calendar.DAY_OF_MONTH, ++day);

                            if (i == 0 || i == days.length - 1) {
                                dayCell_1.setCellStyle(styles.get("weekend_left"));
                                dayCell_2.setCellStyle(styles.get("weekend_right"));
                            } else {
                                dayCell_1.setCellStyle(styles.get("workday_left"));
                                dayCell_2.setCellStyle(styles.get("workday_right"));
                            }
                        } else {
                            dayCell_1.setCellStyle(styles.get("grey_left"));
                            dayCell_2.setCellStyle(styles.get("grey_right"));
                        }
                        cnt++;
                    }
                    if (calendar.get(Calendar.MONTH) > month) break;
                }
            }

            // Write the output to a file
            try (FileOutputStream out = new FileOutputStream("calendar-" + year + ".xlsx")) {
                wb.write(out);
            }

        }
    }

    /**
     * cell styles used for formatting calendar sheets
     */
    private static Map<String, XSSFCellStyle> createStyles(XSSFWorkbook wb){
        Map<String, XSSFCellStyle> styles = new HashMap<>();

        XSSFCellStyle style;
        XSSFFont titleFont = wb.createFont();
        titleFont.setFontHeightInPoints((short)48);
        titleFont.setColor(new XSSFColor(new java.awt.Color(39, 51, 89), wb.getStylesSource().getIndexedColors()));
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFont(titleFont);
        styles.put("title", style);

        XSSFFont monthFont = wb.createFont();
        monthFont.setFontHeightInPoints((short)12);
        monthFont.setColor(new XSSFColor(new java.awt.Color(255, 255, 255), wb.getStylesSource().getIndexedColors()));
        monthFont.setBold(true);
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(39, 51, 89), wb.getStylesSource().getIndexedColors()));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(monthFont);
        styles.put("month", style);

        XSSFFont dayFont = wb.createFont();
        dayFont.setFontHeightInPoints((short)14);
        dayFont.setBold(true);
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(228, 232, 243), wb.getStylesSource().getIndexedColors()));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(new XSSFColor(new java.awt.Color(39, 51, 89), wb.getStylesSource().getIndexedColors()));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(new XSSFColor(new java.awt.Color(39, 51, 89), wb.getStylesSource().getIndexedColors()));
        style.setFont(dayFont);
        styles.put("weekend_left", style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(228, 232, 243), wb.getStylesSource().getIndexedColors()));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(new XSSFColor(new java.awt.Color(39, 51, 89), wb.getStylesSource().getIndexedColors()));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(new XSSFColor(new java.awt.Color(39, 51, 89), wb.getStylesSource().getIndexedColors()));
        styles.put("weekend_right", style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setBorderLeft(BorderStyle.THIN);
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255), wb.getStylesSource().getIndexedColors()));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setLeftBorderColor(new XSSFColor(new java.awt.Color(39, 51, 89), wb.getStylesSource().getIndexedColors()));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(new XSSFColor(new java.awt.Color(39, 51, 89), wb.getStylesSource().getIndexedColors()));
        style.setFont(dayFont);
        styles.put("workday_left", style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255), wb.getStylesSource().getIndexedColors()));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(new XSSFColor(new java.awt.Color(39, 51, 89), wb.getStylesSource().getIndexedColors()));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(new XSSFColor(new java.awt.Color(39, 51, 89), wb.getStylesSource().getIndexedColors()));
        styles.put("workday_right", style);

        style = wb.createCellStyle();
        style.setBorderLeft(BorderStyle.THIN);
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(234, 234, 234), wb.getStylesSource().getIndexedColors()));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(new XSSFColor(new java.awt.Color(39, 51, 89), wb.getStylesSource().getIndexedColors()));
        styles.put("grey_left", style);

        style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(234, 234, 234), wb.getStylesSource().getIndexedColors()));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(new XSSFColor(new java.awt.Color(39, 51, 89), wb.getStylesSource().getIndexedColors()));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(new XSSFColor(new java.awt.Color(39, 51, 89), wb.getStylesSource().getIndexedColors()));
        styles.put("grey_right", style);

        return styles;
    }
}
