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

package org.apache.poi.ss.util;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.TempFile;
import org.junit.Ignore;
import org.junit.Test;

public final class TestDateFormatConverter {
    private void outputLocaleDataFormats( Date date, boolean dates, boolean times, int style, String styleName ) throws Exception {
        try (Workbook workbook = new HSSFWorkbook()) {
            String sheetName;
            if (dates) {
                if (times) {
                    sheetName = "DateTimes";
                } else {
                    sheetName = "Dates";
                }
            } else {
                sheetName = "Times";
            }
            Sheet sheet = workbook.createSheet(sheetName);
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("locale");
            header.createCell(1).setCellValue("DisplayName");
            header.createCell(2).setCellValue("Excel " + styleName);
            header.createCell(3).setCellValue("java.text.DateFormat");
            header.createCell(4).setCellValue("Equals");
            header.createCell(5).setCellValue("Java pattern");
            header.createCell(6).setCellValue("Excel pattern");

            int rowNum = 1;
            for (Locale locale : DateFormat.getAvailableLocales()) {
                try {
                    Row row = sheet.createRow(rowNum++);

                    row.createCell(0).setCellValue(locale.toString());
                    row.createCell(1).setCellValue(locale.getDisplayName(Locale.ROOT));

                    DateFormat dateFormat;
                    if (dates) {
                        if (times) {
                            dateFormat = DateFormat.getDateTimeInstance(style, style, locale);
                        } else {
                            dateFormat = DateFormat.getDateInstance(style, locale);
                        }
                    } else {
                        dateFormat = DateFormat.getTimeInstance(style, locale);
                    }

                    Cell cell = row.createCell(2);

                    cell.setCellValue(date);
                    CellStyle cellStyle = row.getSheet().getWorkbook().createCellStyle();

                    String javaDateFormatPattern = ((SimpleDateFormat) dateFormat).toPattern();
                    String excelFormatPattern = DateFormatConverter.convert(locale, javaDateFormatPattern);

                    DataFormat poiFormat = row.getSheet().getWorkbook().createDataFormat();
                    cellStyle.setDataFormat(poiFormat.getFormat(excelFormatPattern));
                    row.createCell(3).setCellValue(dateFormat.format(date));

                    cell.setCellStyle(cellStyle);

                    // the formula returns TRUE is the formatted date in column C equals to the string in column D
                    row.createCell(4).setCellFormula("TEXT(C" + rowNum + ",G" + rowNum + ")=D" + rowNum);
                    row.createCell(5).setCellValue(javaDateFormatPattern);
                    row.createCell(6).setCellValue(excelFormatPattern);
                } catch (Exception e) {
                    // this can be removed after https://bugs.openjdk.java.net/browse/JDK-8209047 is available
                    // in JDK 11 ea > 26
                    if(locale.toString().startsWith("my") &&
                            e.getMessage().contains("Illegal pattern character 'B'") &&
                                    System.getProperty("java.version").startsWith("11")) {
                        System.out.println("DateFormat.getDateTimeInstance() fails for Malaysian Locale on JDK 11, submitted bug report to Oracle");
                        continue;
                    }

                    throw new RuntimeException(
                            "Failed for locale: " + locale + " and style " + style + "\n" +
                            "Having locales: " + Arrays.toString(DateFormat.getAvailableLocales()), e);
                }
            }

            File outputFile = TempFile.createTempFile("Locale" + sheetName + styleName, ".xlsx");
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                workbook.write(outputStream);
            }

            //System.out.println("Open " + outputFile.getAbsolutePath() + " in Excel");
        }
    }

    @Test
    public void testJavaDateFormatsInExcel() throws Exception {
        Date date = new Date();

        outputLocaleDataFormats(date, true, false, DateFormat.DEFAULT, "Default" );
        outputLocaleDataFormats(date, true, false, DateFormat.SHORT, "Short" );
        outputLocaleDataFormats(date, true, false, DateFormat.MEDIUM, "Medium" );
        outputLocaleDataFormats(date, true, false, DateFormat.LONG, "Long" );
        outputLocaleDataFormats(date, true, false, DateFormat.FULL, "Full" );

        outputLocaleDataFormats(date, true, true, DateFormat.DEFAULT, "Default" );
        outputLocaleDataFormats(date, true, true, DateFormat.SHORT, "Short" );
        outputLocaleDataFormats(date, true, true, DateFormat.MEDIUM, "Medium" );
        outputLocaleDataFormats(date, true, true, DateFormat.LONG, "Long" );
        outputLocaleDataFormats(date, true, true, DateFormat.FULL, "Full" );

        outputLocaleDataFormats(date, false, true, DateFormat.DEFAULT, "Default" );
        outputLocaleDataFormats(date, false, true, DateFormat.SHORT, "Short" );
        outputLocaleDataFormats(date, false, true, DateFormat.MEDIUM, "Medium" );
        outputLocaleDataFormats(date, false, true, DateFormat.LONG, "Long" );
        outputLocaleDataFormats(date, false, true, DateFormat.FULL, "Full" );
    }

    @Test
    public void testJDK8EmptyLocale() {
        // JDK 8 seems to add an empty locale-string to the list returned via DateFormat.getAvailableLocales()
        // therefore we now cater for this special locale as well
        DateFormatConverter.getPrefixForLocale(new Locale(""));
    }

    @Ignore("Fails on JDK 11, submitted as ID : 9056763")
    @Test
    public void testJDK11MyLocale() {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.forLanguageTag("my"));
    }
}
