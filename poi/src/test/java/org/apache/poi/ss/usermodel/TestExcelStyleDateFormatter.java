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

package org.apache.poi.ss.usermodel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.apache.poi.POITestCase;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TestExcelStyleDateFormatter {
    private static final String EXCEL_DATE_FORMAT = "MMMMM";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
    private static final int jreVersion = POITestCase.getJDKVersion();
    private static final String provider = System.getProperty("java.locale.providers");
    private static final FieldPosition fp = new FieldPosition(java.text.DateFormat.MONTH_FIELD);
    private static final ExcelStyleDateFormatter formatter = new ExcelStyleDateFormatter(EXCEL_DATE_FORMAT);

    /**
     * [Bug 60369] Month format 'MMMMM' issue with TEXT-formula and Java 8
     */
    @ParameterizedTest
    @MethodSource("initializeLocales")
    void test60369(Locale locale, String expected, Date d, int month) {
        // Let's iterate over the test setup.
        final StringBuffer sb = new StringBuffer();

        formatter.setDateFormatSymbols(DateFormatSymbols.getInstance(locale));
        String result = formatter.format(d, sb, fp).toString();
        String msg = "Failed testDates for locale " + locale + ", provider: " + provider +
                " and date " + d + ", having: " + result;

        int actIdx = localeIndex(locale);

        assertNotNull(result, msg);
        assertTrue(result.length() > actIdx, msg);
        assertEquals(expected.charAt(month), result.charAt(actIdx), msg);
    }

    /**
     * Depending on the JRE version, the provider setting and the locale, a different result
     * is expected and selected via an index
     */
    private static int localeIndex(Locale locale) {
        return jreVersion < 9 ||
            !locale.equals (Locale.CHINESE) ||
            (provider != null && (provider.startsWith("JRE") || provider.startsWith("COMPAT")))
            ? 0 : 1;
    }

    /**
     * Setting up the locale to be tested together with a list of asserted
     * unicode-formatted results and put them in a map.
     */
    public static Stream<Arguments> initializeLocales() throws ParseException {
        Object[][] locExps = {
            { Locale.GERMAN, "JFMAMJJASOND" },
            { new Locale("de", "AT"), "JFMAMJJASOND" },
            { Locale.UK, "JFMAMJJASOND" },
            { new Locale("en", "IN"), "JFMAMJJASOND" },
            { new Locale("in", "ID"), "JFMAMJJASOND" },
            { Locale.FRENCH, "jfmamjjasond" },
            { new Locale("ru", "RU"), "\u044f\u0444\u043c\u0430\u043c\u0438\u0438\u0430\u0441\u043e\u043d\u0434" },
            { Locale.CHINESE, localeIndex(Locale.CHINESE) == 0 ? "\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u5341\u5341" : "123456789111" },
            { new Locale("tr", "TR"), "\u004f\u015e\u004d\u004e\u004d\u0048\u0054\u0041\u0045\u0045\u004b\u0041" },
            { new Locale("hu", "HU"), "\u006a\u0066\u006d\u00e1\u006d\u006a\u006a\u0061\u0073\u006f\u006e\u0064" }
        };

        String[] dates = {
            "1980-01-12", "1995-02-11", "2045-03-10", "2016-04-09", "2017-05-08",
            "1945-06-07", "1998-07-06", "2099-08-05", "1988-09-04", "2023-10-03", "1978-11-02", "1890-12-01"
        };

        List<Arguments> list = new ArrayList<>(locExps.length * dates.length);
        for (Object[] locExp : locExps) {
            int month = 0;
            for (String date : dates) {
                list.add(Arguments.of(locExp[0], locExp[1], DATE_FORMAT.parse(date), month++));
            }
        }
        return list.stream();
    }

    @Test
    void testConstruct() {
        assertDoesNotThrow(() -> new ExcelStyleDateFormatter(EXCEL_DATE_FORMAT, LocaleUtil.getUserLocale()));
        assertDoesNotThrow(() -> new ExcelStyleDateFormatter(EXCEL_DATE_FORMAT));
    }

    @Test
    void testWithLocale() throws ParseException {
        Locale before = LocaleUtil.getUserLocale();
        try {
            LocaleUtil.setUserLocale(Locale.GERMAN);
            String dateStr = new ExcelStyleDateFormatter(EXCEL_DATE_FORMAT).format(
                    DATE_FORMAT.parse("2016-03-26"));
            assertEquals("M", dateStr);
        } finally {
            LocaleUtil.setUserLocale(before);
        }
    }

    @Test
    void testWithPattern() throws ParseException {
        String dateStr = new ExcelStyleDateFormatter("yyyy|" + EXCEL_DATE_FORMAT + "|").format(
                DATE_FORMAT.parse("2016-03-26"));
        assertEquals("2016|M|", dateStr);
    }
}
