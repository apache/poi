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
import org.apache.poi.ss.usermodel.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Calendar;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

/**
 * A business plan demo
 *
 * @author Yegor Kozlov
 */
public class BusinessPlan {

    private static SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM");

    private static final String[] days = {
            "ID", "Project Name", "Owner", "Days", "Start", "End"};

    //sample data to fill the sheet.
    private static final String[][] data = {
            {"1.0", "Marketing Research Tactical Plan", "R. Ihrig", "70", "9-Jul", null,
                "x", "x", "x", "x", "x", "x", "x", "x", "x", "x", "x"},
            null,
            {"1.1", "Scope Definition Phase", "R. Ihrig", "10", "9-Jul", null,
                "x", "x", null, null,  null, null, null, null, null, null, null},
            {"1.1.1", "Define research objectives", "R. Ihrig", "3", "9-Jul", null,
                    "x", null, null, null,  null, null, null, null, null, null, null},
            {"1.1.2", "Define research requirements", "S. Abbas", "7", "10-Jul", null,
                "x", "x", null, null,  null, null, null, null, null, null, null},
            {"1.1.3", "Determine in-house resource or hire vendor", "R. Ihrig", "2", "15-Jul", null,
                "x", "x", null, null,  null, null, null, null, null, null, null},
            null,
            {"1.2", "Vendor Selection Phase", "R. Ihrig", "19", "19-Jul", null,
                null, "x", "x", "x",  "x", null, null, null, null, null, null},
            {"1.2.1", "Define vendor selection criteria", "R. Ihrig", "3", "19-Jul", null,
                null, "x", null, null,  null, null, null, null, null, null, null},
            {"1.2.2", "Develop vendor selection questionnaire", "S. Abbas, T. Wang", "2", "22-Jul", null,
                null, "x", "x", null,  null, null, null, null, null, null, null},
            {"1.2.3", "Develop Statement of Work", "S. Abbas", "4", "26-Jul", null,
                null, null, "x", "x",  null, null, null, null, null, null, null},
            {"1.2.4", "Evaluate proposal", "R. Ihrig, S. Abbas", "4", "2-Aug", null,
                null, null, null, "x",  "x", null, null, null, null, null, null},
            {"1.2.5", "Select vendor", "R. Ihrig", "1", "6-Aug", null,
                null, null, null, null,  "x", null, null, null, null, null, null},
            null,
            {"1.3", "Research Phase", "Y. Li", "47", "9-Aug", null,
                null, null, null, null,  "x", "x", "x", "x", "x", "x", "x"},
            {"1.3.1", "Develop market research information needs questionnaire", "Y. Li", "2", "9-Aug", null,
                null, null, null, null,  "x", null, null, null, null, null, null},
            {"1.3.2", "Interview marketing group for market research needs", "Y. Li", "2", "11-Aug", null,
                null, null, null, null,  "x", "x", null, null, null, null, null},
            {"1.3.3", "Document information needs", "Y. Li, S. Abbas", "1", "13-Aug", null,
                null, null, null, null,  null, "x", null, null, null, null, null},
    };

    public static void main(String[] args) throws Exception {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);

        XSSFWorkbook wb = new XSSFWorkbook();
        Map<String, XSSFCellStyle> styles = createStyles(wb);

        XSSFSheet sheet = wb.createSheet("Plan");

        //turn off gridlines
        sheet.setDisplayGridlines(false);
        sheet.setPrintGridlines(false);
        XSSFPrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setOrientation(PrintOrientation.LANDSCAPE);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);

        //the header row: centered text in 48pt font
        XSSFRow headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(12.75f);
        for (int i = 0; i < days.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(days[i]);
            cell.setCellStyle(styles.get("header"));
        }
        //columns for 11 weeks starting from 9-Jul
        calendar.setTime(fmt.parse("9-Jul"));
        calendar.set(Calendar.YEAR, year);
        for (int i = 0; i < 11; i++) {
            XSSFCell cell = headerRow.createCell(days.length + i);
            cell.setCellValue(calendar);
            cell.setCellStyle(styles.get("header_date"));
            calendar.roll(Calendar.WEEK_OF_YEAR, true);
        }

        sheet.createFreezePane(0, 1);
        XSSFRow row;
        XSSFCell cell;

        int rownum = 1;
        for (int i = 0; i < data.length; i++, rownum++) {
            row = sheet.createRow(rownum);
            if(data[i] == null) continue;

            for (int j = 0; j < data[i].length; j++) {
                cell = row.createCell(j);
                String styleName;
                boolean isHeader = i == 0 || data[i-1] == null;
                switch(j){
                    case 0:
                        if(isHeader) {
                            styleName = "cell_b";
                            cell.setCellValue(Double.parseDouble(data[i][j]));
                        } else {
                            styleName = "cell_normal";
                            cell.setCellValue(data[i][j]);
                        }
                        break;
                    case 1:
                        if(isHeader) {
                            styleName = i == 0 ? "cell_h" : "cell_bb";
                        } else {
                            styleName = "cell_indented";
                        }
                        cell.setCellValue(data[i][j]);
                        break;
                    case 2:
                        styleName = isHeader ? "cell_b" : "cell_normal";
                        cell.setCellValue(data[i][j]);
                        break;
                    case 3:
                        styleName = isHeader ? "cell_b_centered" : "cell_normal_centered";
                        cell.setCellValue(Integer.parseInt(data[i][j]));
                        break;
                    case 4: {
                        calendar.setTime(fmt.parse(data[i][j]));
                        calendar.set(Calendar.YEAR, year);
                        cell.setCellValue(calendar);
                        styleName = isHeader ? "cell_b_date" : "cell_normal_date";
                        break;
                    }
                    case 5: {
                        int r = rownum + 1;
                        String fmla = "IF(AND(D"+r+",E"+r+"),E"+r+"+D"+r+",\"\")";
                        cell.setCellFormula(fmla);
                        styleName = isHeader ? "cell_bg" : "cell_g";
                        break;
                    }
                    default:
                        styleName = data[i][j] != null ? "cell_blue" : "cell_normal";
                }

                cell.setCellStyle(styles.get(styleName));
            }
        }


        sheet.groupRow(5, 7);
        sheet.groupRow(10, 14);
        sheet.groupRow(17, 19);

        sheet.setColumnWidth(0, 256*6);
        sheet.setColumnWidth(1, 256*33);
        sheet.setColumnWidth(2, 256*20);
        sheet.setZoom(75);


        // Write the output to a file
        FileOutputStream out = new FileOutputStream("xssf-plan.xlsx");
        wb.write(out);
        out.close();
    }

    /**
     * cell styles used for formatting calendar sheets
     */
    private static Map<String, XSSFCellStyle> createStyles(XSSFWorkbook wb){
        Map<String, XSSFCellStyle> styles = new HashMap<String, XSSFCellStyle>();
        XSSFDataFormat df = wb.createDataFormat();

        XSSFCellStyle style;
        XSSFFont headerFont = wb.createFont();
        headerFont.setBold(true);
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(204, 204, 255)));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(headerFont);
        styles.put("header", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(204, 204, 255)));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(headerFont);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("header_date", style);

        XSSFFont font1 = wb.createFont();
        font1.setBold(true);
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setFont(font1);
        styles.put("cell_b", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFont(font1);
        styles.put("cell_b_centered", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setFont(font1);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("cell_b_date", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setFont(font1);
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(228, 228, 228)));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("cell_g", style);

        XSSFFont font2 = wb.createFont();
        font2.setColor(IndexedColors.BLUE.getIndex());
        font2.setBold(true);
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setFont(font2);
        styles.put("cell_bb", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setFont(font1);
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(228, 228, 228)));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("cell_bg", style);

        XSSFFont font3 = wb.createFont();
        font3.setFontHeightInPoints((short)14);
        font3.setColor(IndexedColors.DARK_BLUE.getIndex());
        font3.setBold(true);
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setFont(font3);
        style.setWrapText(true);
        styles.put("cell_h", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setWrapText(true);
        styles.put("cell_normal", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setWrapText(true);
        styles.put("cell_normal_centered", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setWrapText(true);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("cell_normal_date", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setIndention((short)1);
        style.setWrapText(true);
        styles.put("cell_indented", style);

        style = createBorderedStyle(wb);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("cell_blue", style);

        return styles;
    }

    private static XSSFCellStyle createBorderedStyle(XSSFWorkbook wb){
        XSSFCellStyle style = wb.createCellStyle();
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        return style;
    }
}
