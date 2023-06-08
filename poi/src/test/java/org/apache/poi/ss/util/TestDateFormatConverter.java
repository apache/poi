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

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getDateTimeInstance;
import static java.text.DateFormat.getTimeInstance;
import static org.apache.poi.ss.util.DateFormatConverter.getPrefixForLocale;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.LocaleID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

final class TestDateFormatConverter {
    @ParameterizedTest
    @CsvSource({
        "true, false, " + DateFormat.DEFAULT + ", Default",
        "true, false, " + DateFormat.SHORT + ", Short",
        "true, false, " + DateFormat.MEDIUM + ", Medium",
        "true, false, " + DateFormat.LONG + ", Long",
        "true, false, " + DateFormat.FULL + ", Full",
        "true, true, " + DateFormat.DEFAULT + ", Default",
        "true, true, " + DateFormat.SHORT + ", Short",
        "true, true, " + DateFormat.MEDIUM + ", Medium",
        "true, true, " + DateFormat.LONG + ", Long",
        "true, true, " + DateFormat.FULL + ", Full",
        "false, true, " + DateFormat.DEFAULT + ", Default",
        "false, true, " + DateFormat.SHORT + ", Short",
        "false, true, " + DateFormat.MEDIUM + ", Medium",
        "false, true, " + DateFormat.LONG + ", Long",
        "false, true, " + DateFormat.FULL + ", Full"
    })
    void testJavaDateFormatsInExcel(boolean dates, boolean times, int style, String styleName ) throws Exception {
        String sheetName = (dates) ? ((times) ? "DateTimes" : "Dates") : "Times";
        String[] headers = {
            "locale", "DisplayName", "Excel " + styleName, "java.text.DateFormat",
            "Equals", "Java pattern", "Excel pattern"
        };

        Locale[] locales = Arrays.stream(DateFormat.getAvailableLocales())
            // only use locale with known LocaleIDs
            .filter(l -> !getPrefixForLocale(l).isEmpty() || Locale.ROOT.equals(l) || l.toLanguageTag().isEmpty())
            .sorted(Comparator.comparing(Locale::toString))
            .toArray(Locale[]::new);


        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);
            Row header = sheet.createRow(0);
            IntStream.range(0, headers.length).forEach(i -> header.createCell(i).setCellValue(headers[i]));

            int rowNum = 1;
            final Cell[] cell = new Cell[7];
            final Date date = new Date();

            for (Locale locale : locales) {
                DateFormat dateFormat = (dates)
                    ? (times ? getDateTimeInstance(style, style, locale) : getDateInstance(style, locale))
                    : getTimeInstance(style, locale);
                String javaDateFormatPattern = ((SimpleDateFormat) dateFormat).toPattern();
                String excelFormatPattern = DateFormatConverter.convert(locale, javaDateFormatPattern);

                Row row = sheet.createRow(rowNum++);
                IntStream.range(0, headers.length).forEach(i -> cell[i] = row.createCell(i));
                CellStyle cellStyle = workbook.createCellStyle();
                DataFormat poiFormat = workbook.createDataFormat();
                cellStyle.setDataFormat(poiFormat.getFormat(excelFormatPattern));

                cell[0].setCellValue(locale.toString());
                cell[1].setCellValue(locale.getDisplayName(Locale.ROOT));
                cell[2].setCellValue(date);
                cell[2].setCellStyle(cellStyle);
                cell[3].setCellValue(dateFormat.format(date));

                // the formula returns TRUE is the formatted date in column C equals to the string in column D
                cell[4].setCellFormula("TEXT(C" + rowNum + ",G" + rowNum + ")=D" + rowNum);
                cell[5].setCellValue(javaDateFormatPattern);
                cell[6].setCellValue(excelFormatPattern);
            }

            assertDoesNotThrow(() -> workbook.write(NullOutputStream.INSTANCE));
        }
    }

    @Test
    void testJDK8EmptyLocale() {
        // JDK 8 seems to add an empty locale-string to the list returned via DateFormat.getAvailableLocales()
        // therefore we now cater for this special locale as well
        String prefix = getPrefixForLocale(new Locale(""));
        assertEquals("", prefix);
    }

    @Test
    void testJDK11MyLocale() {
        DateFormat df = getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.forLanguageTag("my"));
        assertNotNull(df);
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

                String prefix = getPrefixForLocale(loc);
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
