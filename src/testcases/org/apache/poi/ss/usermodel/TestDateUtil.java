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

import static java.util.Calendar.AUGUST;
import static java.util.Calendar.FEBRUARY;
import static java.util.Calendar.JANUARY;
import static java.util.Calendar.JULY;
import static java.util.Calendar.MARCH;
import static java.util.Calendar.MAY;
import static java.util.Calendar.OCTOBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestDateUtil {

    static TimeZone userTimeZone;

    @BeforeAll
    public static void setCEST() {
        userTimeZone = LocaleUtil.getUserTimeZone();
        LocaleUtil.setUserTimeZone(TimeZone.getTimeZone("CEST"));
    }

    @AfterAll
    public static void resetTimeZone() {
        LocaleUtil.setUserTimeZone(userTimeZone);
    }

    @Test
    void getJavaDate_InvalidValue() {
        final double dateValue = -1;
        final TimeZone tz = LocaleUtil.getUserTimeZone();
        final boolean use1904windowing = false;
        final boolean roundSeconds = false;

        assertNull(DateUtil.getJavaDate(dateValue));
        assertNull(DateUtil.getJavaDate(dateValue, tz));
        assertNull(DateUtil.getJavaDate(dateValue, use1904windowing));
        assertNull(DateUtil.getJavaDate(dateValue, use1904windowing, tz));
        assertNull(DateUtil.getJavaDate(dateValue, use1904windowing, tz, roundSeconds));
    }

    @Test
    void getJavaDate_ValidValue() {
        final double dateValue = 0;
        final TimeZone tz = LocaleUtil.getUserTimeZone();
        final boolean use1904windowing = false;
        final boolean roundSeconds = false;

        Calendar calendar = LocaleUtil.getLocaleCalendar(1900, 0, 0);
        Date date = calendar.getTime();

        assertEquals(date, DateUtil.getJavaDate(dateValue));
        assertEquals(date, DateUtil.getJavaDate(dateValue, tz));
        assertEquals(date, DateUtil.getJavaDate(dateValue, use1904windowing));
        assertEquals(date, DateUtil.getJavaDate(dateValue, use1904windowing, tz));
        assertEquals(date, DateUtil.getJavaDate(dateValue, use1904windowing, tz, roundSeconds));
    }

    @Test
    void getJavaCalendar_InvalidValue() {
        final double dateValue = -1;
        final TimeZone tz = LocaleUtil.getUserTimeZone();
        final boolean use1904windowing = false;
        final boolean roundSeconds = false;

        assertNull(DateUtil.getJavaCalendar(dateValue));
        assertNull(DateUtil.getJavaCalendar(dateValue, use1904windowing));
        assertNull(DateUtil.getJavaCalendar(dateValue, use1904windowing, tz));
        assertNull(DateUtil.getJavaCalendar(dateValue, use1904windowing, tz, roundSeconds));
    }

    @Test
    void getJavaCalendar_ValidValue() {
        final double dateValue = 0;
        final TimeZone tz = LocaleUtil.getUserTimeZone();
        final boolean use1904windowing = false;
        final boolean roundSeconds = false;

        Calendar expCal = LocaleUtil.getLocaleCalendar(1900, 0, 0);

        Calendar[] actCal = {
                DateUtil.getJavaCalendar(dateValue),
                DateUtil.getJavaCalendar(dateValue, use1904windowing),
                DateUtil.getJavaCalendar(dateValue, use1904windowing, tz),
                DateUtil.getJavaCalendar(dateValue, use1904windowing, tz, roundSeconds)
        };
        assertEquals(expCal, actCal[0]);
        assertEquals(expCal, actCal[1]);
        assertEquals(expCal, actCal[2]);
        assertEquals(expCal, actCal[3]);
    }

    @Test
    void getLocalDateTime_InvalidValue() {
        final double dateValue = -1;
        final boolean use1904windowing = false;
        final boolean roundSeconds = false;

        assertNull(DateUtil.getLocalDateTime(dateValue));
        assertNull(DateUtil.getLocalDateTime(dateValue, use1904windowing));
        assertNull(DateUtil.getLocalDateTime(dateValue, use1904windowing, roundSeconds));
    }

    @Test
    void getLocalDateTime_ValidValue() {
        final double dateValue = 0;
        final boolean use1904windowing = false;
        final boolean roundSeconds = false;

        // note that the Date and Calendar examples use a zero day of month which is invalid in LocalDateTime
        LocalDateTime date = LocalDateTime.of(1899, 12, 31, 0, 0);

        assertEquals(date, DateUtil.getLocalDateTime(dateValue));
        assertEquals(date, DateUtil.getLocalDateTime(dateValue, use1904windowing));
        assertEquals(date, DateUtil.getLocalDateTime(dateValue, use1904windowing, roundSeconds));
    }

    @Test
    void isADateFormat() {
        // Cell content 2016-12-8 as an example
        // Cell show "12/8/2016"
        assertTrue(DateUtil.isADateFormat(14, "m/d/yy"));
        // Cell show "Thursday, December 8, 2016"
        assertTrue(DateUtil.isADateFormat(182, "[$-F800]dddd\\,\\ mmmm\\ dd\\,\\ yyyy"));
        // Cell show "12/8"
        assertTrue(DateUtil.isADateFormat(183, "m/d;@"));
        // Cell show "12/08/16"
        assertTrue(DateUtil.isADateFormat(184, "mm/dd/yy;@"));
        // Cell show "8-Dec-16"
        assertTrue(DateUtil.isADateFormat(185, "[$-409]d\\-mmm\\-yy;@"));
        // Cell show "D-16"
        assertTrue(DateUtil.isADateFormat(186, "[$-409]mmmmm\\-yy;@"));

        // Cell show "2016年12月8日"
        assertTrue(DateUtil.isADateFormat(165, "yyyy\"\u5e74\"m\"\u6708\"d\"\u65e5\";@"));
        // Cell show "2016年12月"
        assertTrue(DateUtil.isADateFormat(164, "yyyy\"\u5e74\"m\"\u6708\";@"));
        // Cell show "12月8日"
        assertTrue(DateUtil.isADateFormat(168, "m\"\u6708\"d\"\u65e5\";@"));
        // Cell show "十二月八日"
        assertTrue(DateUtil.isADateFormat(181, "[DBNum1][$-404]m\"\u6708\"d\"\u65e5\";@"));
        // Cell show "贰零壹陆年壹拾贰月捌日"
        assertTrue(DateUtil.isADateFormat(177, "[DBNum2][$-804]yyyy\"\u5e74\"m\"\u6708\"d\"\u65e5\";@"));
        // Cell show "２０１６年１２月８日"
        assertTrue(DateUtil.isADateFormat(178, "[DBNum3][$-804]yyyy\"\u5e74\"m\"\u6708\"d\"\u65e5\";@"));
    }
    /**
     * Checks the date conversion functions in the DateUtil class.
     */
    @Test
    void dateConversion() {

        // Iterating over the hours exposes any rounding issues.
        Calendar cal = LocaleUtil.getLocaleCalendar(2002,JANUARY,1,0,1,1);
        for (int hour = 0; hour < 24; hour++) {
            double excelDate = DateUtil.getExcelDate(cal.getTime(), false);

            assertEquals(cal.getTime().getTime(), DateUtil.getJavaDate(excelDate, false).getTime(),
                "getJavaDate: Checking hour = " + hour);

            LocalDateTime ldt = LocalDateTime.ofInstant(cal.toInstant(), cal.getTimeZone().toZoneId());
            assertEquals(ldt, DateUtil.getLocalDateTime(excelDate, false),
                "getLocalDateTime: Checking hour = " + hour);

            cal.add(Calendar.HOUR_OF_DAY, 1);
        }

        // check 1900 and 1904 date windowing conversions
        double excelDate = 36526.0;
        // with 1900 windowing, excelDate is Jan. 1, 2000
        // with 1904 windowing, excelDate is Jan. 2, 2004
        cal.set(2000,JANUARY,1,0,0,0); // Jan. 1, 2000
        Date dateIf1900 = cal.getTime();
        cal.add(Calendar.YEAR,4); // now Jan. 1, 2004
        cal.add(Calendar.DATE,1); // now Jan. 2, 2004
        Date dateIf1904 = cal.getTime();
        // 1900 windowing
        assertEquals(dateIf1900.getTime(), DateUtil.getJavaDate(excelDate,false).getTime(),
            "Checking 1900 Date Windowing");
        // 1904 windowing
        assertEquals(dateIf1904.getTime(), DateUtil.getJavaDate(excelDate,true).getTime(),
            "Checking 1904 Date Windowing");
        // 1900 windowing (LocalDateTime)
        assertEquals(LocalDateTime.of(2000,1,1,0,0), DateUtil.getLocalDateTime(excelDate,false),
            "Checking 1900 Date Windowing");
        // 1904 windowing (LocalDateTime)
        assertEquals(LocalDateTime.of(2004,1,2,0,0), DateUtil.getLocalDateTime(excelDate,true),
            "Checking 1904 Date Windowing");
    }

    /**
     * Checks the conversion of a java.util.date to Excel on a day when
     * Daylight Saving Time starts.
     */
    @Test
    void excelConversionOnDSTStart() {
        Calendar cal = LocaleUtil.getLocaleCalendar(2004,MARCH,28,0,0,0);
        for (int hour = 0; hour < 24; hour++) {

            // Skip 02:00 CET as that is the Daylight change time
            // and Java converts it automatically to 03:00 CEST
            if (hour == 2) {
                continue;
            }

            cal.set(Calendar.HOUR_OF_DAY, hour);
            Date javaDate = cal.getTime();
            double excelDate = DateUtil.getExcelDate(javaDate, false);
            double difference = excelDate - Math.floor(excelDate);
            int differenceInHours = (int) (difference * 24 * 60 + 0.5) / 60;

            assertEquals(hour, differenceInHours, "Checking " + hour + " hour on Daylight Saving Time start date");
            assertEquals(javaDate.getTime(), DateUtil.getJavaDate(excelDate, false).getTime(),
                "Checking " + hour + " hour on Daylight Saving Time start date");

            // perform the same checks with LocalDateTime
            LocalDateTime localDate = LocalDateTime.of(2004,3,28,hour,0,0);
            double excelLocalDate = DateUtil.getExcelDate(localDate, false);
            double differenceLocalDate = excelLocalDate - Math.floor(excelLocalDate);
            int differenceLocalDateInHours = (int) (differenceLocalDate * 24 * 60 + 0.5) / 60;

            assertEquals(hour, differenceLocalDateInHours,
                "Checking " + hour + " hour on Daylight Saving Time start date (LocalDateTime)");
            assertEquals(localDate, DateUtil.getLocalDateTime(excelLocalDate, false),
                "Checking " + hour + " hour on Daylight Saving Time start date (LocalDateTime)");
        }
    }

    /**
     * Checks the conversion of an Excel date to a java.util.date on a day when
     * Daylight Saving Time starts.
     */
    @Test
    void javaConversionOnDSTStart() {
        Calendar cal = LocaleUtil.getLocaleCalendar(2004,MARCH,28,0,0,0);
        double excelDate = DateUtil.getExcelDate(cal.getTime(), false);
        double oneHour = 1.0 / 24;
        double oneMinute = oneHour / 60;
        for (int hour = 0; hour < 24; hour++, excelDate += oneHour) {

            // Skip 02:00 CET as that is the Daylight change time
            // and Java converts it automatically to 03:00 CEST
            if (hour == 2) {
                continue;
            }

            cal.set(Calendar.HOUR_OF_DAY, hour);
            Date javaDate = DateUtil.getJavaDate(excelDate, false);
            double actDate = DateUtil.getExcelDate(javaDate, false);
            assertEquals(excelDate, actDate, oneMinute,
                "Checking " + hour + " hours on Daylight Saving Time start date");

            // perform the same check with LocalDateTime
            cal.set(Calendar.HOUR_OF_DAY, hour);
            LocalDateTime localDate = DateUtil.getLocalDateTime(excelDate, false);
            double actLocalDate = DateUtil.getExcelDate(localDate, false);
            assertEquals(excelDate, actLocalDate, oneMinute,
                "Checking " + hour + " hours on Daylight Saving Time start date (LocalDateTime)");
        }
    }

    /**
     * Checks the conversion of a java.util.Date to Excel on a day when
     * Daylight Saving Time ends.
     */
    @Test
    void excelConversionOnDSTEnd() {
        Calendar cal = LocaleUtil.getLocaleCalendar(2004,OCTOBER,31,0,0,0);
        for (int hour = 0; hour < 24; hour++) {
            cal.set(Calendar.HOUR_OF_DAY, hour);
            Date javaDate = cal.getTime();
            double excelDate = DateUtil.getExcelDate(javaDate, false);
            double difference = excelDate - Math.floor(excelDate);
            int differenceInHours = (int) (difference * 24 * 60 + 0.5) / 60;
            assertEquals(hour, differenceInHours,
                "Checking " + hour + " hour on Daylight Saving Time end date");
            assertEquals(javaDate.getTime(), DateUtil.getJavaDate(excelDate, false).getTime(),
                "Checking " + hour + " hour on Daylight Saving Time start date");

            // perform the same checks using LocalDateTime
            LocalDateTime localDate = LocalDateTime.of(2004,10,31,hour,0,0);
            double excelLocalDate = DateUtil.getExcelDate(localDate, false);
            int differenceLocalDateInHours = (int) (difference * 24 * 60 + 0.5) / 60;
            assertEquals(hour, differenceLocalDateInHours,
                "Checking " + hour + " hour on Daylight Saving Time end date (LocalDateTime)");
            assertEquals(localDate, DateUtil.getLocalDateTime(excelLocalDate, false),
                "Checking " + hour + " hour on Daylight Saving Time start date (LocalDateTime)");
        }
    }

    /**
     * Checks the conversion of an Excel date to java.util.Date on a day when
     * Daylight Saving Time ends.
     */
    @Test
    void javaConversionOnDSTEnd() {
        Calendar cal = LocaleUtil.getLocaleCalendar(2004,OCTOBER,31,0,0,0);
        double excelDate = DateUtil.getExcelDate(cal.getTime(), false);
        double oneHour = 1.0 / 24;
        double oneMinute = oneHour / 60;
        for (int hour = 0; hour < 24; hour++, excelDate += oneHour) {
            cal.set(Calendar.HOUR_OF_DAY, hour);
            Date javaDate = DateUtil.getJavaDate(excelDate, false);
            assertEquals(excelDate, DateUtil.getExcelDate(javaDate, false), oneMinute,
                "Checking " + hour + " hours on Daylight Saving Time start date");

            // perform the same checks using LocalDateTime
            LocalDateTime localDate = DateUtil.getLocalDateTime(excelDate, false);
            assertEquals(excelDate, DateUtil.getExcelDate(localDate, false), oneMinute,
                "Checking " + hour + " hours on Daylight Saving Time start date");
        }
    }

    /**
     * Tests that we deal with time-zones properly
     */
    @Test
    void calendarConversion() {
        TimeZone userTZ = LocaleUtil.getUserTimeZone();
        LocaleUtil.setUserTimeZone(TimeZone.getTimeZone("CET"));
        try {
            Calendar cal = LocaleUtil.getLocaleCalendar(2002,JANUARY,1,12,1,1);
            Date expected = cal.getTime();

            // Iterating over the hours exposes any rounding issues.
            for (int hour = -12; hour <= 12; hour++)
            {
                String id = "GMT" + (hour < 0 ? "" : "+") + hour + ":00";
                cal.setTimeZone(TimeZone.getTimeZone(id));
                cal.set(Calendar.HOUR_OF_DAY, 12);
                double excelDate = DateUtil.getExcelDate(cal, false);
                Date javaDate = DateUtil.getJavaDate(excelDate);

                // Should match despite time-zone
                assertEquals(expected.getTime(), javaDate.getTime(), "Checking timezone " + id);
            }

            // Check that the timezone aware getter works correctly
            TimeZone cet = TimeZone.getTimeZone("Europe/Copenhagen");
            TimeZone ldn = TimeZone.getTimeZone("Europe/London");

            // 12:45 on 27th April 2012
            double excelDate = 41026.53125;

            // Same, no change
            assertEquals(
                DateUtil.getJavaDate(excelDate, false).getTime(),
                DateUtil.getJavaDate(excelDate, false, cet).getTime()
            );

            // London vs Copenhagen, should differ by an hour
            Date cetDate = DateUtil.getJavaDate(excelDate, false);
            Date ldnDate = DateUtil.getJavaDate(excelDate, false, ldn);
            assertEquals(ldnDate.getTime() - cetDate.getTime(), 60*60*1000);
        } finally {
            LocaleUtil.setUserTimeZone(userTZ);
        }
    }

    /**
     * Tests that we correctly detect date formats as such
     */
    @Test
    void identifyDateFormats() {
        // First up, try with a few built in date formats
        short[] builtins = new short[] { 0x0e, 0x0f, 0x10, 0x16, 0x2d, 0x2e };
        for (short builtin : builtins) {
            String formatStr = HSSFDataFormat.getBuiltinFormat(builtin);
            assertTrue( DateUtil.isInternalDateFormat(builtin) );
            assertTrue( DateUtil.isADateFormat(builtin,formatStr) );
        }

        // Now try a few built-in non date formats
        builtins = new short[] { 0x01, 0x02, 0x17, 0x1f, 0x30 };
        for (short builtin : builtins) {
            String formatStr = HSSFDataFormat.getBuiltinFormat(builtin);
            assertFalse( DateUtil.isInternalDateFormat(builtin) );
            assertFalse( DateUtil.isADateFormat(builtin,formatStr) );
        }

        // Now for some non-internal ones
        // These come after the real ones
        int numBuiltins = HSSFDataFormat.getNumberOfBuiltinBuiltinFormats();
        assertTrue(numBuiltins < 60);
        short formatId = 60;
        assertFalse( DateUtil.isInternalDateFormat(formatId) );

        // Valid ones first
        String[] formats = new String[] {
                "yyyy-mm-dd", "yyyy/mm/dd", "yy/mm/dd", "yy/mmm/dd",
                "dd/mm/yy", "dd/mm/yyyy", "dd/mmm/yy",
                "dd-mm-yy", "dd-mm-yyyy",
                "DD-MM-YY", "DD-mm-YYYY",
                "dd\\-mm\\-yy", // Sometimes escaped
                "dd.mm.yyyy", "dd\\.mm\\.yyyy",
                "dd\\ mm\\.yyyy AM", "dd\\ mm\\.yyyy pm",
                "dd\\ mm\\.yyyy\\-dd", "[h]:mm:ss",
                "mm/dd/yy", "\"mm\"/\"dd\"/\"yy\"",
                "m\\/d\\/yyyy",

                // These crazy ones are valid
                "yyyy-mm-dd;@", "yyyy/mm/dd;@",
                "dd-mm-yy;@", "dd-mm-yyyy;@",
                // These even crazier ones are also valid
                // (who knows what they mean though...)
                "[$-F800]dddd\\,\\ mmm\\ dd\\,\\ yyyy",
                "[$-F900]ddd/mm/yyy",
                // These ones specify colours, who knew that was allowed?
                "[BLACK]dddd/mm/yy",
                "[yeLLow]yyyy-mm-dd"
        };
        for (String format : formats) {
            assertTrue(DateUtil.isADateFormat(formatId, format), format + " is a date format");
        }

        // Then time based ones too
        formats = new String[] {
                "yyyy-mm-dd hh:mm:ss", "yyyy/mm/dd HH:MM:SS",
                "mm/dd HH:MM", "yy/mmm/dd SS",
                "mm/dd HH:MM AM", "mm/dd HH:MM am",
                "mm/dd HH:MM PM", "mm/dd HH:MM pm",
                "m/d/yy h:mm AM/PM",
                "hh:mm:ss", "hh:mm:ss.0", "mm:ss.0",
                //support elapsed time [h],[m],[s]
                "[hh]", "[mm]", "[ss]", "[SS]", "[red][hh]"
        };
        for (String format : formats) {
            assertTrue(DateUtil.isADateFormat(formatId, format), format + " is a datetime format");
        }

        // Then invalid ones
        formats = new String[] {
                "yyyy*mm*dd",
                "0.0", "0.000",
                "0%", "0.0%",
                "[]Foo", "[BLACK]0.00%",
                "[ms]", "[Mh]",
                "", null
        };
        for (String format : formats) {
            assertFalse(DateUtil.isADateFormat(formatId, format), format + " is not a date or datetime format");
        }

        // And these are ones we probably shouldn't allow,
        //  but would need a better regexp

        // formats = new String[] {
        //         "yyyy:mm:dd",
        // };
        // for (String format : formats) {
        //         assertFalse( DateUtil.isADateFormat(formatId, format) );
        // }
    }

    @Test
    void excelDateBorderCases() throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        df.setTimeZone(LocaleUtil.getUserTimeZone());

        Date date1 = df.parse("1900-01-01");
        assertEquals(1.0, DateUtil.getExcelDate(date1), 0.00001);
        assertEquals(1.0, DateUtil.getExcelDate(DateUtil.toLocalDateTime(date1)), 0.00001);
        Date date31 = df.parse("1900-01-31");
        assertEquals(31.0, DateUtil.getExcelDate(date31), 0.00001);
        assertEquals(31.0, DateUtil.getExcelDate(DateUtil.toLocalDateTime(date31)), 0.00001);
        Date date32 = df.parse("1900-02-01");
        assertEquals(32.0, DateUtil.getExcelDate(date32), 0.00001);
        assertEquals(32.0, DateUtil.getExcelDate(DateUtil.toLocalDateTime(date32)), 0.00001);
        Date dateMinus1 = df.parse("1899-12-31");
        assertEquals(/* BAD_DATE! */ -1.0, DateUtil.getExcelDate(dateMinus1), 0.00001);
        assertEquals(/* BAD_DATE! */ -1.0, DateUtil.getExcelDate(DateUtil.toLocalDateTime(dateMinus1)), 0.00001);
    }

    @Test
    void dateBug_2Excel() {
        assertEquals(59.0, DateUtil.getExcelDate(createDate(1900, FEBRUARY, 28), false), 0.00001);
        assertEquals(61.0, DateUtil.getExcelDate(createDate(1900, MARCH, 1), false), 0.00001);

        assertEquals(37315.00, DateUtil.getExcelDate(createDate(2002, FEBRUARY, 28), false), 0.00001);
        assertEquals(37316.00, DateUtil.getExcelDate(createDate(2002, MARCH, 1), false), 0.00001);
        assertEquals(37257.00, DateUtil.getExcelDate(createDate(2002, JANUARY, 1), false), 0.00001);
        assertEquals(38074.00, DateUtil.getExcelDate(createDate(2004, MARCH, 28), false), 0.00001);

        // perform the same checks using LocalDateTime
        assertEquals(59.0, DateUtil.getExcelDate(LocalDateTime.of(1900, 2, 28, 0,0), false), 0.00001);
        assertEquals(61.0, DateUtil.getExcelDate(LocalDateTime.of(1900, 3, 1, 0,0), false), 0.00001);

        assertEquals(37315.00, DateUtil.getExcelDate(LocalDateTime.of(2002, 2, 28, 0,0), false), 0.00001);
        assertEquals(37316.00, DateUtil.getExcelDate(LocalDateTime.of(2002, 3, 1, 0,0), false), 0.00001);
        assertEquals(37257.00, DateUtil.getExcelDate(LocalDateTime.of(2002, 1, 1, 0,0), false), 0.00001);
        assertEquals(38074.00, DateUtil.getExcelDate(LocalDateTime.of(2004, 3, 28, 0,0), false), 0.00001);
    }

    @Test
    void dateBug_2Java() {
        assertEquals(createDate(1900, FEBRUARY, 28), DateUtil.getJavaDate(59.0, false));
        assertEquals(createDate(1900, MARCH, 1), DateUtil.getJavaDate(61.0, false));

        assertEquals(createDate(2002, FEBRUARY, 28), DateUtil.getJavaDate(37315.00, false));
        assertEquals(createDate(2002, MARCH, 1), DateUtil.getJavaDate(37316.00, false));
        assertEquals(createDate(2002, JANUARY, 1), DateUtil.getJavaDate(37257.00, false));
        assertEquals(createDate(2004, MARCH, 28), DateUtil.getJavaDate(38074.00, false));

        // perform the same checks using LocalDateTime
        assertEquals(LocalDateTime.of(1900, 2, 28, 0, 0), DateUtil.getLocalDateTime(59.0, false));
        assertEquals(LocalDateTime.of(1900, 3, 1, 0, 0), DateUtil.getLocalDateTime(61.0, false));

        assertEquals(LocalDateTime.of(2002, 2, 28, 0, 0), DateUtil.getLocalDateTime(37315.00, false));
        assertEquals(LocalDateTime.of(2002, 3, 1, 0, 0), DateUtil.getLocalDateTime(37316.00, false));
        assertEquals(LocalDateTime.of(2002, 1, 1, 0, 0), DateUtil.getLocalDateTime(37257.00, false));
        assertEquals(LocalDateTime.of(2004, 3, 28, 0, 0), DateUtil.getLocalDateTime(38074.00, false));
    }

    @Test
    void date1904() {
        assertEquals(createDate(1904, JANUARY, 2), DateUtil.getJavaDate(1.0, true));
        assertEquals(createDate(1904, JANUARY, 1), DateUtil.getJavaDate(0.0, true));
        assertEquals(0.0, DateUtil.getExcelDate(createDate(1904, JANUARY, 1), true), 0.00001);
        assertEquals(1.0, DateUtil.getExcelDate(createDate(1904, JANUARY, 2), true), 0.00001);

        assertEquals(createDate(1998, JULY, 5), DateUtil.getJavaDate(35981, false));
        assertEquals(createDate(1998, JULY, 5), DateUtil.getJavaDate(34519, true));

        assertEquals(35981.0, DateUtil.getExcelDate(createDate(1998, JULY, 5), false), 0.00001);
        assertEquals(34519.0, DateUtil.getExcelDate(createDate(1998, JULY, 5), true), 0.00001);

        // perform the same checks using LocalDateTime
        assertEquals(LocalDateTime.of(1904, 1, 2, 0, 0), DateUtil.getLocalDateTime(1.0, true));
        assertEquals(LocalDateTime.of(1904, 1, 1, 0, 0), DateUtil.getLocalDateTime(0.0, true));
        assertEquals(0.0, DateUtil.getExcelDate(LocalDateTime.of(1904, 1, 1, 0, 0), true), 0.00001);
        assertEquals(1.0, DateUtil.getExcelDate(LocalDateTime.of(1904, 1, 2, 0, 0), true), 0.00001);

        assertEquals(LocalDateTime.of(1998, 7, 5, 0, 0), DateUtil.getLocalDateTime(35981, false));
        assertEquals(LocalDateTime.of(1998, 7, 5, 0, 0), DateUtil.getLocalDateTime(34519, true));

        assertEquals(35981.0, DateUtil.getExcelDate(LocalDateTime.of(1998, 7, 5, 0, 0), false), 0.00001);
        assertEquals(34519.0, DateUtil.getExcelDate(LocalDateTime.of(1998, 7, 5, 0, 0), true), 0.00001);
    }

    /**
     * @param month zero based
     * @param day one based
     */
    private static Date createDate(int year, int month, int day) {
        return createDate(year, month, day, 0, 0);
    }

    /**
     * @param month zero based
     * @param day one based
     */
    private static Date createDate(int year, int month, int day, int hour, int minute) {
        Calendar c = LocaleUtil.getLocaleCalendar(year, month, day, hour, minute, 0);
        return c.getTime();
    }

    /**
     * Check if DateUtil.getAbsoluteDay works as advertised.
     */
    @Test
    void absoluteDay() {
        // 1 Jan 1900 is 1 day after 31 Dec 1899
        Calendar cal = LocaleUtil.getLocaleCalendar(1900,JANUARY,1,0,0,0);
        assertEquals(1, DateUtil.absoluteDay(cal, false), "Checking absolute day (1 Jan 1900)");
        LocalDateTime ldt = LocalDateTime.of(1900,1,1,0,0,0);
        assertEquals(1, DateUtil.absoluteDay(ldt, false), "Checking absolute day (1 Jan 1900) (LocalDateTime)");
        // 1 Jan 1901 is 366 days after 31 Dec 1899
        ldt = LocalDateTime.of(1901,1,1,0,0,0);
        cal.set(1901,JANUARY,1,0,0,0);
        assertEquals(366, DateUtil.absoluteDay(ldt, false), "Checking absolute day (1 Jan 1901) (LocalDateTime)");
    }

    @Test
    void absoluteDayYearTooLow() {
        Calendar cal = LocaleUtil.getLocaleCalendar(1899,JANUARY,1,0,0,0);
        assertThrows(IllegalArgumentException.class, () -> DateUtil.absoluteDay(cal, false));

        cal.set(1903,JANUARY,1,0,0,0);
        assertThrows(IllegalArgumentException.class, () -> DateUtil.absoluteDay(cal, true));

        // same for LocalDateTime
        assertThrows(IllegalArgumentException.class, () -> DateUtil.absoluteDay(LocalDateTime.of(1899,1,1,0,0,0), false));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.absoluteDay(LocalDateTime.of(1903,1,1,0,0,0), true));
    }

    @Test
    void convertTime() {

        final double delta = 1E-7; // a couple of digits more accuracy than strictly required
        assertEquals(0.5, DateUtil.convertTime("12:00"), delta);
        assertEquals(2.0/3, DateUtil.convertTime("16:00"), delta);
        assertEquals(0.0000116, DateUtil.convertTime("0:00:01"), delta);
        assertEquals(0.7330440, DateUtil.convertTime("17:35:35"), delta);
    }

    @Test
    void parseDate() {
        assertEquals(createDate(2008, AUGUST, 3), DateUtil.parseYYYYMMDDDate("2008/08/03"));
        assertEquals(createDate(1994, MAY, 1), DateUtil.parseYYYYMMDDDate("1994/05/01"));
    }

    /**
     * Ensure that date values *with* a fractional portion get the right time of day
     */
    @Test
    void convertDateTime() {
        // Excel day 30000 is date 18-Feb-1982
        // 0.7 corresponds to time 16:48:00
        Date actual = DateUtil.getJavaDate(30000.7);
        Date expected = createDate(1982, 1, 18, 16, 48);
        assertEquals(expected, actual);

        // note that months in Calendar are zero-based, in LocalDateTime one-based
        LocalDateTime actualLocalDate = DateUtil.getLocalDateTime(30000.7);
        LocalDateTime expectedLocalDate = LocalDateTime.of(1982, 2, 18, 16, 48, 0);
        assertEquals(expectedLocalDate, actualLocalDate);
    }

    /**
     * User reported a datetime issue in POI-2.5:
     *  Setting Cell's value to Jan 1, 1900 without a time doesn't return the same value set to
     */
    @Test
    void bug19172() throws IOException {
        try (HSSFWorkbook workbook = new HSSFWorkbook()) {
            HSSFSheet sheet = workbook.createSheet();
            HSSFCell cell = sheet.createRow(0).createCell(0);

            // A pseudo special Excel dates
            Calendar cal = LocaleUtil.getLocaleCalendar(1900, JANUARY, 1);

            Date valueToTest = cal.getTime();

            cell.setCellValue(valueToTest);

            Date returnedValue = cell.getDateCellValue();

            assertEquals(valueToTest.getTime(), returnedValue.getTime());
        }
    }

    /**
     * DateUtil.isCellFormatted(Cell) should not true for a numeric cell
     * that's formatted as ".0000"
     */
    @Test
    void bug54557() {
        final String format = ".0000";
        boolean isDateFormat = DateUtil.isADateFormat(165, format);

        assertFalse(isDateFormat);
    }

    @Test
    void bug56269() {
        double excelFraction = 41642.45833321759d;
        Calendar calNoRound = DateUtil.getJavaCalendar(excelFraction, false);
        assertEquals(10, calNoRound.get(Calendar.HOUR));
        assertEquals(59, calNoRound.get(Calendar.MINUTE));
        assertEquals(59, calNoRound.get(Calendar.SECOND));
        Calendar calRound = DateUtil.getJavaCalendar(excelFraction, false, null, true);
        assertNotNull(calRound);
        assertEquals(11, calRound.get(Calendar.HOUR));
        assertEquals(0, calRound.get(Calendar.MINUTE));
        assertEquals(0, calRound.get(Calendar.SECOND));

        LocalDateTime ldtNoRound = DateUtil.getLocalDateTime(excelFraction, false);
        assertEquals(10, ldtNoRound.getHour());
        assertEquals(59, ldtNoRound.getMinute());
        assertEquals(59, ldtNoRound.getSecond());
        LocalDateTime ldtRound = DateUtil.getLocalDateTime(excelFraction, false, true);
        assertNotNull(ldtRound);
        assertEquals(11, ldtRound.getHour());
        assertEquals(0, ldtRound.getMinute());
        assertEquals(0, ldtRound.getSecond());
    }
}
