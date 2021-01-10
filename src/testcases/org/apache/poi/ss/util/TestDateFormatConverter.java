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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.LocaleID;
import org.apache.poi.util.TempFile;
import org.junit.jupiter.api.Test;

final class TestDateFormatConverter {
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
    void testJavaDateFormatsInExcel() throws Exception {
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
    void testJDK8EmptyLocale() {
        // JDK 8 seems to add an empty locale-string to the list returned via DateFormat.getAvailableLocales()
        // therefore we now cater for this special locale as well
        DateFormatConverter.getPrefixForLocale(new Locale(""));
    }

    @Test
    void testJDK11MyLocale() {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.forLanguageTag("my"));
    }

    @Test
    void testAllKnownLocales() {
        Pattern p = Pattern.compile("\\[\\$-(\\p{XDigit}+)]");

        Set<String> excludeList = Stream.of(
            "sd-Deva", "tzm-Arab", "fuv", "plt", "yue", "tdd-Tale", "tdd",
            "khb-Talu", "khb", "qps", "ja-Ploc", "dz", "tmz", "ar-Ploc"
        ).collect(Collectors.toSet());

        for (LocaleID lid : LocaleID.values()) {
            final String langTag = lid.getLanguageTag();

            if (langTag.isEmpty() || lid.getWindowsId().startsWith("invalid")) {
                continue;
            }

            // test all from variant to parent locales
            String cmpTag = (langTag.indexOf('_') > 0) ? langTag.replace('_','-') : langTag;
            for (int idx = langTag.length(); idx > 0; idx = cmpTag.lastIndexOf('-', idx-1)) {
                final String partTag = langTag.substring(0, idx);

                Locale loc = Locale.forLanguageTag(partTag);
                assertNotNull(loc, "Invalid language tag: "+partTag);

                if (excludeList.contains(partTag)) {
                    continue;
                }

                String prefix = DateFormatConverter.getPrefixForLocale(loc);
                assertNotNull(prefix, "Prefix not found - language tag: "+partTag);
                assertNotEquals("", prefix, "Prefix not found - language tag: "+partTag);
                Matcher m = p.matcher(prefix);
                assertTrue(m.matches(), "Invalid prefix: "+prefix);

                LocaleID partLid = LocaleID.lookupByLanguageTag(partTag);
                assertNotNull(partLid, "LocaleID not found for part: "+partTag);
                assertEquals(partLid.getLcid(), Integer.parseInt(m.group(1), 16));
            }
        }
    }
}
