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

import static org.junit.Assert.assertEquals;

import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.util.LocaleUtil;
import org.junit.Test;

public class TestExcelStyleDateFormatter {
    private static final String EXCEL_DATE_FORMAT = "MMMMM";

    /**
     * [Bug 60369] Month format 'MMMMM' issue with TEXT-formula and Java 8
     */
    @Test
    public void test60369() throws ParseException {
        Map<Locale, List<String>> testMap = initializeLocales();

        // We have to set up dates as well.
        SimpleDateFormat testDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ROOT);
        List<Date> testDates = Arrays.asList(
                testDateFormat.parse("12.01.1980"),
                testDateFormat.parse("11.02.1995"),
                testDateFormat.parse("10.03.2045"),
                testDateFormat.parse("09.04.2016"),
                testDateFormat.parse("08.05.2017"),
                testDateFormat.parse("07.06.1945"),
                testDateFormat.parse("06.07.1998"),
                testDateFormat.parse("05.08.2099"),
                testDateFormat.parse("04.09.1988"),
                testDateFormat.parse("03.10.2023"),
                testDateFormat.parse("02.11.1978"),
                testDateFormat.parse("01.12.1890"));

        // Let's iterate over the test setup.
        for (Locale locale : testMap.keySet()) {
            ExcelStyleDateFormatter formatter = new ExcelStyleDateFormatter(EXCEL_DATE_FORMAT, new DateFormatSymbols(locale));
            for (int i = 0; i < testDates.size(); i++) {
                // Call the method to be tested!
                String result =
                        formatter.format(testDates.get(i),
                                new StringBuffer(),
                                new FieldPosition(java.text.DateFormat.MONTH_FIELD)).toString();
                //System.err.println(result +  " - " + getUnicode(result.charAt(0)));
                assertEquals("Failed for locale " + locale + ", provider: " + System.getProperty("java.locale.providers") +
                        " and date " + testDates.get(i) + ", having: " + result,
                        getUnicode(testMap.get(locale).get(i).charAt(0)), getUnicode(result.charAt(0)));
            }
        }
    }

    private Map<Locale, List<String>> initializeLocales() {
        // Setting up the locale to be tested together with a list of asserted unicode-formatted results and put them in a map.
        Locale germanLocale = Locale.GERMAN;
        List<String> germanResultList = Arrays.asList("\u004a", "\u0046", "\u004d", "\u0041", "\u004d",
                "\u004a", "\u004a", "\u0041", "\u0053", "\u004f", "\u004e", "\u0044");

        Locale russianLocale = new Locale("ru", "RU");
        List<String> russianResultList = Arrays.asList("\u044f", "\u0444", "\u043c", "\u0430", "\u043c",
                "\u0438", "\u0438", "\u0430", "\u0441", "\u043e", "\u043d", "\u0434");

        Locale austrianLocale = new Locale("de", "AT");
        List<String> austrianResultList = Arrays.asList("\u004a", "\u0046", "\u004d", "\u0041", "\u004d",
                "\u004a", "\u004a", "\u0041", "\u0053", "\u004f", "\u004e", "\u0044");

        Locale englishLocale = Locale.UK;
        List<String> englishResultList = Arrays.asList("\u004a", "\u0046", "\u004d", "\u0041", "\u004d",
                "\u004a", "\u004a", "\u0041", "\u0053", "\u004f", "\u004e", "\u0044");

        Locale frenchLocale = Locale.FRENCH;
        List<String> frenchResultList = Arrays.asList("\u006a", "\u0066", "\u006d", "\u0061", "\u006d",
                "\u006a", "\u006a", "\u0061", "\u0073", "\u006f", "\u006e", "\u0064");

        Locale chineseLocale = Locale.CHINESE;
        List<String> chineseResultList = Arrays.asList("\u4e00", "\u4e8c", "\u4e09", "\u56db", "\u4e94",
                "\u516d", "\u4e03", "\u516b", "\u4e5d", "\u5341", "\u5341", "\u5341");

        Locale turkishLocale = new Locale("tr", "TR");
        List<String> turkishResultList = Arrays.asList("\u004f", "\u015e", "\u004d", "\u004e", "\u004d",
                "\u0048", "\u0054", "\u0041", "\u0045", "\u0045", "\u004b", "\u0041");

        Locale hungarianLocale = new Locale("hu", "HU");
        List<String> hungarianResultList = Arrays.asList("\u006a", "\u0066", "\u006d", "\u00e1", "\u006d",
                "\u006a", "\u006a", "\u0061", "\u0073", "\u006f", "\u006e", "\u0064");

        Locale indianLocale = new Locale("en", "IN");
        List<String> indianResultList = Arrays.asList("\u004a", "\u0046", "\u004d", "\u0041", "\u004d",
                "\u004a", "\u004a", "\u0041", "\u0053", "\u004f", "\u004e", "\u0044");

        Locale indonesianLocale = new Locale("in", "ID");
        List<String> indonesianResultList = Arrays.asList("\u004a", "\u0046", "\u004d", "\u0041", "\u004d",
                "\u004a", "\u004a", "\u0041", "\u0053", "\u004f", "\u004e", "\u0044");


        Map<Locale, List<String>> testMap = new HashMap<>();
        testMap.put(germanLocale,        germanResultList);
        testMap.put(russianLocale,        russianResultList);
        testMap.put(austrianLocale,        austrianResultList);
        testMap.put(englishLocale,        englishResultList);
        testMap.put(frenchLocale,        frenchResultList);
        testMap.put(chineseLocale,        chineseResultList);
        testMap.put(turkishLocale,        turkishResultList);
        testMap.put(hungarianLocale,    hungarianResultList);
        testMap.put(indianLocale,        indianResultList);
        testMap.put(indonesianLocale,    indonesianResultList);

        return testMap;
    }

    private String getUnicode(char c) {
        return "\\u" + Integer.toHexString(c | 0x10000).substring(1);
    }

    @Test
    public void testConstruct() {
        new ExcelStyleDateFormatter(EXCEL_DATE_FORMAT, LocaleUtil.getUserLocale());
        new ExcelStyleDateFormatter(EXCEL_DATE_FORMAT);
    }

    @Test
    public void testWithLocale() throws ParseException {
        Locale before = LocaleUtil.getUserLocale();
        try {
            LocaleUtil.setUserLocale(Locale.GERMAN);
            String dateStr = new ExcelStyleDateFormatter(EXCEL_DATE_FORMAT).format(
                    new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse("2016-03-26"));
            assertEquals("M", dateStr);
        } finally {
            LocaleUtil.setUserLocale(before);
        }
    }

    @Test
    public void testWithPattern() throws ParseException {
        String dateStr = new ExcelStyleDateFormatter("yyyy|" + EXCEL_DATE_FORMAT + "|").format(
                new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse("2016-03-26"));
        assertEquals("2016|M|", dateStr);
    }
}
