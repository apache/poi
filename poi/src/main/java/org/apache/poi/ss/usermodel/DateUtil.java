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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.util.LocaleUtil;

/**
 * Contains methods for dealing with Excel dates.
 */
public class DateUtil {
    // FIXME this should be changed to private and the class marked final once HSSFDateUtil can be removed
    protected DateUtil() {
        // no instances of this class
    }

    public static final int SECONDS_PER_MINUTE = 60;
    public static final int MINUTES_PER_HOUR = 60;
    public static final int HOURS_PER_DAY = 24;
    public static final int SECONDS_PER_DAY = (HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE);

    // used to specify that date is invalid
    private static final int BAD_DATE         = -1;
    public static final long DAY_MILLISECONDS = SECONDS_PER_DAY * 1000L;



    private static final BigDecimal BD_NANOSEC_DAY = BigDecimal.valueOf(SECONDS_PER_DAY * 1e9);
    private static final BigDecimal BD_MILISEC_RND = BigDecimal.valueOf(0.5 * 1e6);
    private static final BigDecimal BD_SECOND_RND = BigDecimal.valueOf(0.5 * 1e9);

    private static final Pattern TIME_SEPARATOR_PATTERN = Pattern.compile(":");

    /**
     * The following patterns are used in {@link #isADateFormat(int, String)}
     */
    private static final Pattern date_ptrn1 = Pattern.compile("^\\[\\$\\-.*?\\]");
    private static final Pattern date_ptrn2 = Pattern.compile("^\\[[a-zA-Z]+\\]");
    private static final Pattern date_ptrn3a = Pattern.compile("[yYmMdDhHsS]");
    // add "\u5e74 \u6708 \u65e5" for Chinese/Japanese date format:2017 \u5e74 2 \u6708 7 \u65e5
    private static final Pattern date_ptrn3b = Pattern.compile("^[\\[\\]yYmMdDhHsS\\-T/\u5e74\u6708\u65e5,. :\"\\\\]+0*[ampAMP/]*$");
    //  elapsed time patterns: [h],[m] and [s]
    private static final Pattern date_ptrn4 = Pattern.compile("^\\[([hH]+|[mM]+|[sS]+)\\]");

    // for format which start with "[DBNum1]" or "[DBNum2]" or "[DBNum3]" could be a Chinese date
    private static final Pattern date_ptrn5 = Pattern.compile("^\\[DBNum(1|2|3)\\]");

    private static final DateTimeFormatter dateTimeFormats = new DateTimeFormatterBuilder()
            .appendPattern("[dd MMM[ yyyy]][[ ]h:m[:s] a][[ ]H:m[:s]]")
            .appendPattern("[[yyyy ]dd-MMM[-yyyy]][[ ]h:m[:s] a][[ ]H:m[:s]]")
            .appendPattern("[M/dd[/yyyy]][[ ]h:m[:s] a][[ ]H:m[:s]]")
            .appendPattern("[[yyyy/]M/dd][[ ]h:m[:s] a][[ ]H:m[:s]]")
            .parseDefaulting(ChronoField.YEAR_OF_ERA, LocaleUtil.getLocaleCalendar().get(Calendar.YEAR))
            .toFormatter();

    /**
     * Convert a Java Date (at UTC) to LocalDateTime.
     * @param date the date
     * @return LocalDateTime instance
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(TimeZone.getTimeZone("UTC").toZoneId()) // java.util.Date uses UTC
                .toLocalDateTime();
    }

    /**
     * Convert a Java Calendar (at UTC) to LocalDateTime.
     * @param date the date
     * @return LocalDateTime instance
     */
    public static LocalDateTime toLocalDateTime(Calendar date) {
        return date.toInstant()
                .atZone(TimeZone.getTimeZone("UTC").toZoneId()) // java.util.Date uses UTC
                .toLocalDateTime();
    }

    /**
     * Given a LocalDate, converts it into a double representing its internal Excel representation,
     *   which is the number of days since 1/1/1900. Fractional days represent hours, minutes, and seconds.
     *
     * @return Excel representation of Date (-1 if error - test for error by checking for less than 0.1)
     * @param  date the Date
     */
    public static double getExcelDate(LocalDate date) {
        return getExcelDate(date, false);
    }

    /**
     * Given a LocalDate, converts it into a double representing its internal Excel representation,
     *   which is the number of days since 1/1/1900. Fractional days represent hours, minutes, and seconds.
     *
     * @return Excel representation of Date (-1 if error - test for error by checking for less than 0.1)
     * @param date the Date
     * @param use1904windowing Should 1900 or 1904 date windowing be used?
     */
    public static double getExcelDate(LocalDate date, boolean use1904windowing) {
        int year = date.getYear();
        int dayOfYear = date.getDayOfYear();
        int hour = 0;
        int minute = 0;
        int second = 0;
        int milliSecond = 0;

        return internalGetExcelDate(year, dayOfYear, hour, minute, second, milliSecond, use1904windowing);
    }

    /**
     * Given a LocalDateTime, converts it into a double representing its internal Excel representation,
     *   which is the number of days since 1/1/1900. Fractional days represent hours, minutes, and seconds.
     *
     * @return Excel representation of Date (-1 if error - test for error by checking for less than 0.1)
     * @param  date the Date
     */
    public static double getExcelDate(LocalDateTime date) {
        return getExcelDate(date, false);
    }

    /**
     * Given a LocalDateTime, converts it into a double representing its internal Excel representation,
     *   which is the number of days since 1/1/1900. Fractional days represent hours, minutes, and seconds.
     *
     * @return Excel representation of Date (-1 if error - test for error by checking for less than 0.1)
     * @param date the Date
     * @param use1904windowing Should 1900 or 1904 date windowing be used?
     */
    public static double getExcelDate(LocalDateTime date, boolean use1904windowing) {
        int year = date.getYear();
        int dayOfYear = date.getDayOfYear();
        int hour = date.getHour();
        int minute = date.getMinute();
        int second = date.getSecond();
        int milliSecond = date.getNano()/1_000_000;

        return internalGetExcelDate(year, dayOfYear, hour, minute, second, milliSecond, use1904windowing);
    }

    /**
     * Given a Date, converts it into a double representing its internal Excel representation,
     *   which is the number of days since 1/1/1900. Fractional days represent hours, minutes, and seconds.
     *
     * @return Excel representation of Date (-1 if error - test for error by checking for less than 0.1)
     * @param  date the Date
     */
    public static double getExcelDate(Date date) {
        return getExcelDate(date, false);
    }

    /**
     * Given a Date, converts it into a double representing its internal Excel representation,
     *   which is the number of days since 1/1/1900. Fractional days represent hours, minutes, and seconds.
     *
     * @return Excel representation of Date (-1 if error - test for error by checking for less than 0.1)
     * @param date the Date
     * @param use1904windowing Should 1900 or 1904 date windowing be used?
     */
    public static double getExcelDate(Date date, boolean use1904windowing) {
        Calendar calStart = LocaleUtil.getLocaleCalendar();
        calStart.setTime(date);
        int year = calStart.get(Calendar.YEAR);
        int dayOfYear = calStart.get(Calendar.DAY_OF_YEAR);
        int hour = calStart.get(Calendar.HOUR_OF_DAY);
        int minute = calStart.get(Calendar.MINUTE);
        int second = calStart.get(Calendar.SECOND);
        int milliSecond = calStart.get(Calendar.MILLISECOND);

        return internalGetExcelDate(year, dayOfYear, hour, minute, second, milliSecond, use1904windowing);
    }

    /**
     * Given a Date in the form of a Calendar, converts it into a double
     *  representing its internal Excel representation, which is the
     *  number of days since 1/1/1900. Fractional days represent hours,
     *  minutes, and seconds.
     *
     * @return Excel representation of Date (-1 if error - test for error by checking for less than 0.1)
     * @param date the Calendar holding the date to convert
     * @param use1904windowing Should 1900 or 1904 date windowing be used?
     */
    public static double getExcelDate(Calendar date, boolean use1904windowing) {
        int year = date.get(Calendar.YEAR);
        int dayOfYear = date.get(Calendar.DAY_OF_YEAR);
        int hour = date.get(Calendar.HOUR_OF_DAY);
        int minute = date.get(Calendar.MINUTE);
        int second = date.get(Calendar.SECOND);
        int milliSecond = date.get(Calendar.MILLISECOND);

        return internalGetExcelDate(year, dayOfYear, hour, minute, second, milliSecond, use1904windowing);
    }

    private static double internalGetExcelDate(int year, int dayOfYear, int hour, int minute, int second, int milliSecond, boolean use1904windowing) {
        if ((!use1904windowing && year < 1900) ||
            (use1904windowing && year < 1904))
        {
            return BAD_DATE;
        }

        // Because of daylight time saving we cannot use
        //     date.getTime() - calStart.getTimeInMillis()
        // as the difference in milliseconds between 00:00 and 04:00
        // can be 3, 4 or 5 hours but Excel expects it to always
        // be 4 hours.
        // E.g. 2004-03-28 04:00 CEST - 2004-03-28 00:00 CET is 3 hours
        // and 2004-10-31 04:00 CET - 2004-10-31 00:00 CEST is 5 hours
        double fraction = (((hour * 60.0
                             + minute
                            ) * 60.0 + second
                           ) * 1000.0 + milliSecond
                          ) / DAY_MILLISECONDS;

        double value = fraction + absoluteDay(year, dayOfYear, use1904windowing);

        if (!use1904windowing && value >= 60) {
            value++;
        } else if (use1904windowing) {
            value--;
        }

        return value;
    }

    /**
     *  Given an Excel date with using 1900 date windowing, and
     *  converts it to a java.util.Date.
     *
     *  Excel Dates and Times are stored without any timezone
     *  information. If you know (through other means) that your file
     *  uses a different TimeZone to the system default, you can use
     *  this version of the getJavaDate() method to handle it.
     *
     *  @param date  The Excel date.
     *  @param tz The TimeZone to evaluate the date in
     *  @return Java representation of the date, or null if date is not a valid Excel date
     */
    public static Date getJavaDate(double date, TimeZone tz) {
       return getJavaDate(date, false, tz, false);
    }
    /**
     *  Given an Excel date with using 1900 date windowing, and
     *   converts it to a java.util.Date.
     *
     *  NOTE: If the default <code>TimeZone</code> in Java uses Daylight
     *  Saving Time then the conversion back to an Excel date may not give
     *  the same value, that is the comparison
     *  <CODE>excelDate == getExcelDate(getJavaDate(excelDate,false))</CODE>
     *  is not always true. For example if default timezone is
     *  <code>Europe/Copenhagen</code>, on 2004-03-28 the minute after
     *  01:59 CET is 03:00 CEST, if the excel date represents a time between
     *  02:00 and 03:00 then it is converted to past 03:00 summer time
     *
     *  @param date  The Excel date.
     *  @return Java representation of the date, or null if date is not a valid Excel date
     *  @see java.util.TimeZone
     */
    public static Date getJavaDate(double date) {
        return getJavaDate(date, false, null, false);
    }

    /**
     *  Given an Excel date with either 1900 or 1904 date windowing,
     *  converts it to a java.util.Date.
     *
     *  Excel Dates and Times are stored without any timezone
     *  information. If you know (through other means) that your file
     *  uses a different TimeZone to the system default, you can use
     *  this version of the getJavaDate() method to handle it.
     *
     *  @param date  The Excel date.
     *  @param tz The TimeZone to evaluate the date in
     *  @param use1904windowing  true if date uses 1904 windowing,
     *   or false if using 1900 date windowing.
     *  @return Java representation of the date, or null if date is not a valid Excel date
     */
    public static Date getJavaDate(double date, boolean use1904windowing, TimeZone tz) {
        return getJavaDate(date, use1904windowing, tz, false);
    }

    /**
     *  Given an Excel date with either 1900 or 1904 date windowing,
     *  converts it to a java.util.Date.
     *
     *  Excel Dates and Times are stored without any timezone
     *  information. If you know (through other means) that your file
     *  uses a different TimeZone to the system default, you can use
     *  this version of the getJavaDate() method to handle it.
     *
     *  @param date  The Excel date.
     *  @param tz The TimeZone to evaluate the date in
     *  @param use1904windowing  true if date uses 1904 windowing,
     *   or false if using 1900 date windowing.
     *  @param roundSeconds round to closest second
     *  @return Java representation of the date, or null if date is not a valid Excel date
     */
    public static Date getJavaDate(double date, boolean use1904windowing, TimeZone tz, boolean roundSeconds) {
        Calendar calendar = getJavaCalendar(date, use1904windowing, tz, roundSeconds);
        return calendar == null ? null : calendar.getTime();
    }

    /**
     *  Given an Excel date with either 1900 or 1904 date windowing,
     *  converts it to a java.util.Date.
     *
     *  NOTE: If the default <code>TimeZone</code> in Java uses Daylight
     *  Saving Time then the conversion back to an Excel date may not give
     *  the same value, that is the comparison
     *  <CODE>excelDate == getExcelDate(getJavaDate(excelDate,false))</CODE>
     *  is not always true. For example if default timezone is
     *  <code>Europe/Copenhagen</code>, on 2004-03-28 the minute after
     *  01:59 CET is 03:00 CEST, if the excel date represents a time between
     *  02:00 and 03:00 then it is converted to past 03:00 summer time
     *
     *  @param date  The Excel date.
     *  @param use1904windowing  true if date uses 1904 windowing,
     *   or false if using 1900 date windowing.
     *  @return Java representation of the date, or null if date is not a valid Excel date
     *  @see java.util.TimeZone
     */
    public static Date getJavaDate(double date, boolean use1904windowing) {
        return getJavaDate(date, use1904windowing, null, false);
    }

    /**
     *  Given an Excel date with using 1900 date windowing, and
     *   converts it to a java.time.LocalDateTime.
     *
     *  NOTE: If the default <code>TimeZone</code> in Java uses Daylight
     *  Saving Time then the conversion back to an Excel date may not give
     *  the same value, that is the comparison
     *  <CODE>excelDate == getExcelDate(getLocalDateTime(excelDate,false))</CODE>
     *  is not always true. For example if default timezone is
     *  <code>Europe/Copenhagen</code>, on 2004-03-28 the minute after
     *  01:59 CET is 03:00 CEST, if the excel date represents a time between
     *  02:00 and 03:00 then it is converted to past 03:00 summer time
     *
     *  @param date  The Excel date.
     *  @return Java representation of the date, or null if date is not a valid Excel date
     *  @see java.util.TimeZone
     */
    public static LocalDateTime getLocalDateTime(double date) {
        return getLocalDateTime(date, false, false);
    }

    /**
     *  Given an Excel date with either 1900 or 1904 date windowing,
     *  converts it to a java.time.LocalDateTime.
     *
     *  Excel Dates and Times are stored without any timezone
     *  information. If you know (through other means) that your file
     *  uses a different TimeZone to the system default, you can use
     *  this version of the getJavaDate() method to handle it.
     *
     *  @param date  The Excel date.
     *  @param use1904windowing  true if date uses 1904 windowing,
     *   or false if using 1900 date windowing.
     *  @return Java representation of the date, or null if date is not a valid Excel date
     */
    public static LocalDateTime getLocalDateTime(double date, boolean use1904windowing) {
        return getLocalDateTime(date, use1904windowing, false);
    }

    /**
     *  Given an Excel date with either 1900 or 1904 date windowing,
     *  converts it to a java.time.LocalDateTime.
     *
     *  Excel Dates and Times are stored without any timezone
     *  information. If you know (through other means) that your file
     *  uses a different TimeZone to the system default, you can use
     *  this version of the getJavaDate() method to handle it.
     *
     *  @param date  The Excel date.
     *  @param use1904windowing  true if date uses 1904 windowing,
     *   or false if using 1900 date windowing.
     *  @param roundSeconds round to closest second
     *  @return Java representation of the date, or null if date is not a valid Excel date
     */
    @SuppressWarnings("squid:S2111")
    public static LocalDateTime getLocalDateTime(double date, boolean use1904windowing, boolean roundSeconds) {
        if (!isValidExcelDate(date)) {
            return null;
        }

        BigDecimal bd = new BigDecimal(date);

        int wholeDays = bd.intValue();

        int startYear = 1900;
        int dayAdjust = -1; // Excel thinks 2/29/1900 is a valid date, which it isn't
        if (use1904windowing) {
            startYear = 1904;
            dayAdjust = 1; // 1904 date windowing uses 1/2/1904 as the first day
        }
        else if (wholeDays < 61) {
            // Date is prior to 3/1/1900, so adjust because Excel thinks 2/29/1900 exists
            // If Excel date == 2/29/1900, will become 3/1/1900 in Java representation
            dayAdjust = 0;
        }

        LocalDateTime ldt = LocalDateTime.of(startYear, 1, 1, 0, 0);
        ldt = ldt.plusDays(wholeDays+dayAdjust-1L);

        long nanosTime =
            bd.subtract(BigDecimal.valueOf(wholeDays))
            .multiply(BD_NANOSEC_DAY)
            .add(roundSeconds ? BD_SECOND_RND : BD_MILISEC_RND)
            .longValue();

        ldt = ldt.plusNanos(nanosTime);
        ldt = ldt.truncatedTo(roundSeconds ? ChronoUnit.SECONDS : ChronoUnit.MILLIS);

        return ldt;
    }

    public static void setCalendar(Calendar calendar, int wholeDays,
            int millisecondsInDay, boolean use1904windowing, boolean roundSeconds) {
        int startYear = 1900;
        int dayAdjust = -1; // Excel thinks 2/29/1900 is a valid date, which it isn't
        if (use1904windowing) {
            startYear = 1904;
            dayAdjust = 1; // 1904 date windowing uses 1/2/1904 as the first day
        }
        else if (wholeDays < 61) {
            // Date is prior to 3/1/1900, so adjust because Excel thinks 2/29/1900 exists
            // If Excel date == 2/29/1900, will become 3/1/1900 in Java representation
            dayAdjust = 0;
        }
        calendar.set(startYear,0, wholeDays + dayAdjust, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, millisecondsInDay);
        if (calendar.get(Calendar.MILLISECOND) == 0) {
            calendar.clear(Calendar.MILLISECOND);
        }
        if (roundSeconds) {
            calendar.add(Calendar.MILLISECOND, 500);
            calendar.clear(Calendar.MILLISECOND);
        }
    }


    /**
     * Get EXCEL date as Java Calendar (with default time zone).
     * This is like {@link #getJavaDate(double)} but returns a Calendar object.
     *  @param date  The Excel date.
     *  @return Java representation of the date, or null if date is not a valid Excel date
     */
    public static Calendar getJavaCalendar(double date) {
        return getJavaCalendar(date, false, null, false);
    }

    /**
     * Get EXCEL date as Java Calendar (with default time zone).
     * This is like {@link #getJavaDate(double, boolean)} but returns a Calendar object.
     *  @param date  The Excel date.
     *  @param use1904windowing  true if date uses 1904 windowing,
     *   or false if using 1900 date windowing.
     *  @return Java representation of the date, or null if date is not a valid Excel date
     */
    public static Calendar getJavaCalendar(double date, boolean use1904windowing) {
        return getJavaCalendar(date, use1904windowing, null, false);
    }

    /**
     * Get EXCEL date as Java Calendar with UTC time zone.
     * This is similar to {@link #getJavaDate(double, boolean)} but returns a
     * Calendar object that has UTC as time zone, so no daylight saving hassle.
     * @param date  The Excel date.
     * @param use1904windowing  true if date uses 1904 windowing,
     *  or false if using 1900 date windowing.
     * @return Java representation of the date in UTC, or null if date is not a valid Excel date
     */
    public static Calendar getJavaCalendarUTC(double date, boolean use1904windowing) {
    	return getJavaCalendar(date, use1904windowing, LocaleUtil.TIMEZONE_UTC, false);
    }


    /**
     * Get EXCEL date as Java Calendar with given time zone.
     * @param date  The Excel date.
     * @param use1904windowing  true if date uses 1904 windowing,
     *  or false if using 1900 date windowing.
     * @param timeZone The TimeZone to evaluate the date in
     * @return Java representation of the date, or null if date is not a valid Excel date
     */
    public static Calendar getJavaCalendar(double date, boolean use1904windowing, TimeZone timeZone) {
        return getJavaCalendar(date, use1904windowing, timeZone, false);
    }

    /**
     * Get EXCEL date as Java Calendar with given time zone.
     * @param date  The Excel date.
     * @param use1904windowing  true if date uses 1904 windowing,
     *  or false if using 1900 date windowing.
     * @param timeZone The TimeZone to evaluate the date in
     * @param roundSeconds round to closest second
     * @return Java representation of the date, or null if date is not a valid Excel date
     */
    public static Calendar getJavaCalendar(double date, boolean use1904windowing, TimeZone timeZone, boolean roundSeconds) {
        if (!isValidExcelDate(date)) {
            return null;
        }
        int wholeDays = (int)Math.floor(date);
        int millisecondsInDay = (int)((date - wholeDays) * DAY_MILLISECONDS + 0.5);
        Calendar calendar;
        if (timeZone != null) {
            calendar = LocaleUtil.getLocaleCalendar(timeZone);
        } else {
            calendar = LocaleUtil.getLocaleCalendar(); // using default time-zone
        }
        setCalendar(calendar, wholeDays, millisecondsInDay, use1904windowing, roundSeconds);
        return calendar;
    }

    // variables for performance optimization:
    // avoid re-checking DataUtil.isADateFormat(int, String) if a given format
    // string represents a date format if the same string is passed multiple times.
    // see https://issues.apache.org/bugzilla/show_bug.cgi?id=55611
    private static ThreadLocal<Integer> lastFormatIndex = ThreadLocal.withInitial(() -> -1);
    private static ThreadLocal<String> lastFormatString = new ThreadLocal<>();
    private static ThreadLocal<Boolean> lastCachedResult = new ThreadLocal<>();

    private static boolean isCached(String formatString, int formatIndex) {
        return formatIndex == lastFormatIndex.get()
                && formatString.equals(lastFormatString.get());
    }

    private static void cache(String formatString, int formatIndex, boolean cached) {
        if (formatString == null || "".equals(formatString)) {
            lastFormatString.remove();
        } else {
            lastFormatString.set(formatString);
        }
        if (formatIndex == -1) {
            lastFormatIndex.remove();
        } else {
            lastFormatIndex.set(formatIndex);
        }
        lastCachedResult.set(cached);
    }

    /**
     * Given a format ID and its format String, will check to see if the
     *  format represents a date format or not.
     * Firstly, it will check to see if the format ID corresponds to an
     *  internal excel date format (eg most US date formats)
     * If not, it will check to see if the format string only contains
     *  date formatting characters (ymd-/), which covers most
     *  non US date formats.
     *
     * @param numFmt The number format index and string expression, or null if not specified
     * @return true if it is a valid date format, false if not or null
     * @see #isInternalDateFormat(int)
     */
    public static boolean isADateFormat(ExcelNumberFormat numFmt) {

        if (numFmt == null) {
            return false;
        }

        return isADateFormat(numFmt.getIdx(), numFmt.getFormat());
    }

    /**
     * Given a format ID and its format String, will check to see if the
     *  format represents a date format or not.
     * Firstly, it will check to see if the format ID corresponds to an
     *  internal excel date format (eg most US date formats)
     * If not, it will check to see if the format string only contains
     *  date formatting characters (ymd-/), which covers most
     *  non US date formats.
     *
     * @param formatIndex The index of the format, eg from ExtendedFormatRecord.getFormatIndex
     * @param formatString The format string, eg from FormatRecord.getFormatString
     * @return true if it is a valid date format, false if not or null
     * @see #isInternalDateFormat(int)
     */
    public static boolean isADateFormat(int formatIndex, String formatString) {

        // First up, is this an internal date format?
        if(isInternalDateFormat(formatIndex)) {
            cache(formatString, formatIndex, true);
            return true;
        }

        // If we didn't get a real string, don't even cache it as we can always find this out quickly
        if(formatString == null || formatString.length() == 0) {
            return false;
        }

        // check the cache first
        if (isCached(formatString, formatIndex)) {
            return lastCachedResult.get();
        }

        String fs = formatString;
        /*if (false) {
            // Normalize the format string. The code below is equivalent
            // to the following consecutive regexp replacements:

             // Translate \- into just -, before matching
             fs = fs.replaceAll("\\\\-","-");
             // And \, into ,
             fs = fs.replaceAll("\\\\,",",");
             // And \. into .
             fs = fs.replaceAll("\\\\\\.",".");
             // And '\ ' into ' '
             fs = fs.replaceAll("\\\\ "," ");

             // If it end in ;@, that's some crazy dd/mm vs mm/dd
             //  switching stuff, which we can ignore
             fs = fs.replaceAll(";@", "");

             // The code above was reworked as suggested in bug 48425:
             // simple loop is more efficient than consecutive regexp replacements.
        }*/
        final int length = fs.length();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = fs.charAt(i);
            if (i < length - 1) {
                char nc = fs.charAt(i + 1);
                if (c == '\\') {
                    switch (nc) {
                        case '-':
                        case ',':
                        case '.':
                        case ' ':
                        case '\\':
                            // skip current '\' and continue to the next char
                            continue;
                    }
                } else if (c == ';' && nc == '@') {
                    i++;
                    // skip ";@" duplets
                    continue;
                }
            }
            sb.append(c);
        }
        fs = sb.toString();

        // short-circuit if it indicates elapsed time: [h], [m] or [s]
        if(date_ptrn4.matcher(fs).matches()){
            cache(formatString, formatIndex, true);
            return true;
        }
        // If it starts with [DBNum1] or [DBNum2] or [DBNum3]
        // then it could be a Chinese date
        fs = date_ptrn5.matcher(fs).replaceAll("");
        // If it starts with [$-...], then could be a date, but
        //  who knows what that starting bit is all about
        fs = date_ptrn1.matcher(fs).replaceAll("");
        // If it starts with something like [Black] or [Yellow],
        //  then it could be a date
        fs = date_ptrn2.matcher(fs).replaceAll("");
        // You're allowed something like dd/mm/yy;[red]dd/mm/yy
        //  which would place dates before 1900/1904 in red
        // For now, only consider the first one
        final int separatorIndex = fs.indexOf(';');
        if(0 < separatorIndex && separatorIndex < fs.length()-1) {
           fs = fs.substring(0, separatorIndex);
        }

        // Ensure it has some date letters in it
        // (Avoids false positives on the rest of pattern 3)
        if (! date_ptrn3a.matcher(fs).find()) {
           return false;
        }

        // If we get here, check it's only made up, in any case, of:
        //  y m d h s - \ / , . : [ ] T
        // optionally followed by AM/PM

        boolean result = date_ptrn3b.matcher(fs).matches();
        cache(formatString, formatIndex, result);
        return result;
    }

    /**
     * Given a format ID this will check whether the format represents
     *  an internal excel date format or not.
     * @see #isADateFormat(int, java.lang.String)
     */
    public static boolean isInternalDateFormat(int format) {
            switch(format) {
                // Internal Date Formats as described on page 427 in
                // Microsoft Excel Dev's Kit...
                case 0x0e:
                case 0x0f:
                case 0x10:
                case 0x11:
                case 0x12:
                case 0x13:
                case 0x14:
                case 0x15:
                case 0x16:
                case 0x2d:
                case 0x2e:
                case 0x2f:
                    return true;
            }
       return false;
    }

    /**
     *  Check if a cell contains a date
     *  Since dates are stored internally in Excel as double values
     *  we infer it is a date if it is formatted as such.
     * @param cell
     * @return true if it looks like a date
     *  @see #isADateFormat(int, String)
     *  @see #isInternalDateFormat(int)
     */
    public static boolean isCellDateFormatted(Cell cell) {
        return isCellDateFormatted(cell, null);
    }

    /**
     *  Check if a cell contains a date
     *  Since dates are stored internally in Excel as double values
     *  we infer it is a date if it is formatted as such.
     *  Format is determined from applicable conditional formatting, if
     *  any, or cell style.
     * @param cell
     * @param cfEvaluator if available, or null
     * @return true if it looks like a date
     *  @see #isADateFormat(int, String)
     *  @see #isInternalDateFormat(int)
     */
    public static boolean isCellDateFormatted(Cell cell, ConditionalFormattingEvaluator cfEvaluator) {
        if (cell == null) {
            return false;
        }
        boolean bDate = false;

        double d = cell.getNumericCellValue();
        if ( DateUtil.isValidExcelDate(d) ) {
            ExcelNumberFormat nf = ExcelNumberFormat.from(cell, cfEvaluator);
            if(nf==null) {
                return false;
            }
            bDate = isADateFormat(nf);
        }
        return bDate;
    }

    /**
     *  Check if a cell contains a date, checking only for internal
     *   excel date formats.
     *  As Excel stores a great many of its dates in "non-internal"
     *   date formats, you will not normally want to use this method.
     *  @see #isADateFormat(int,String)
     *  @see #isInternalDateFormat(int)
     */
    public static boolean isCellInternalDateFormatted(Cell cell) {
        if (cell == null) {
            return false;
        }
        boolean bDate = false;

        double d = cell.getNumericCellValue();
        if ( DateUtil.isValidExcelDate(d) ) {
            CellStyle style = cell.getCellStyle();
            int i = style.getDataFormat();
            bDate = isInternalDateFormat(i);
        }
        return bDate;
    }


    /**
     * Given a double, checks if it is a valid Excel date.
     *
     * @return true if valid
     * @param  value the double value
     */

    public static boolean isValidExcelDate(double value)
    {
        return (value > -Double.MIN_VALUE);
    }

    /**
     * Given a Calendar, return the number of days since 1900/12/31.
     *
     * @return days number of days since 1900/12/31
     * @param  cal the Calendar
     * @exception IllegalArgumentException if date is invalid
     */
    protected static int absoluteDay(Calendar cal, boolean use1904windowing)
    {
        return absoluteDay(cal.get(Calendar.YEAR), cal.get(Calendar.DAY_OF_YEAR), use1904windowing);
    }

    /**
     * Given a LocalDateTime, return the number of days since 1900/12/31.
     *
     * @return days number of days since 1900/12/31
     * @param  date the Date
     * @exception IllegalArgumentException if date is invalid
     */
    protected static int absoluteDay(LocalDateTime date, boolean use1904windowing)
    {
        return absoluteDay(date.getYear(), date.getDayOfYear(), use1904windowing);
    }

    /**
     * Given a year and day of year, return the number of days since 1900/12/31.
     *
     * @return days number of days since 1900/12/31
     * @param  dayOfYear the day of the year
     * @param  year the year
     * @exception IllegalArgumentException if date is invalid
     */
    private static int absoluteDay(int year, int dayOfYear, boolean use1904windowing) {
        return dayOfYear + daysInPriorYears(year, use1904windowing);
    }

    /**
     * Return the number of days in prior years since 1900
     *
     * @return    days  number of days in years prior to yr.
     * @param     yr    a year (1900 < yr < 4000)
     * @param use1904windowing
     * @exception IllegalArgumentException if year is outside of range.
     */

    static int daysInPriorYears(int yr, boolean use1904windowing)
    {
        if ((!use1904windowing && yr < 1900) || (use1904windowing && yr < 1904)) {
            throw new IllegalArgumentException("'year' must be 1900 or greater");
        }

        int yr1  = yr - 1;
        int leapDays =   yr1 / 4   // plus julian leap days in prior years
                       - yr1 / 100 // minus prior century years
                       + yr1 / 400 // plus years divisible by 400
                       - 460;      // leap days in previous 1900 years

        return 365 * (yr - (use1904windowing ? 1904 : 1900)) + leapDays;
    }

    // set HH:MM:SS fields of cal to 00:00:00:000
    private static Calendar dayStart(final Calendar cal)
    {
        cal.get(Calendar
            .HOUR_OF_DAY);   // force recalculation of internal fields
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.get(Calendar
            .HOUR_OF_DAY);   // force recalculation of internal fields
        return cal;
    }


    @SuppressWarnings("serial")
    private static final class FormatException extends Exception {
        public FormatException(String msg) {
            super(msg);
        }
    }

    /**
     * Converts a string of format "HH:MM" or "HH:MM:SS" to its (Excel) numeric equivalent
     *
     * @return a double between 0 and 1 representing the fraction of the day
     */
    public static double convertTime(String timeStr) {
        try {
            return convertTimeInternal(timeStr);
        } catch (FormatException e) {
            String msg = "Bad time format '" + timeStr
                + "' expected 'HH:MM' or 'HH:MM:SS' - " + e.getMessage();
            throw new IllegalArgumentException(msg);
        }
    }
    private static double convertTimeInternal(String timeStr) throws FormatException {
        int len = timeStr.length();
        if (len < 4 || len > 8) {
            throw new FormatException("Bad length");
        }
        String[] parts = TIME_SEPARATOR_PATTERN.split(timeStr);

        String secStr;
        switch (parts.length) {
            case 2: secStr = "00"; break;
            case 3: secStr = parts[2]; break;
            default:
                throw new FormatException("Expected 2 or 3 fields but got (" + parts.length + ")");
        }
        String hourStr = parts[0];
        String minStr = parts[1];
        int hours = parseInt(hourStr, "hour", HOURS_PER_DAY);
        int minutes = parseInt(minStr, "minute", MINUTES_PER_HOUR);
        int seconds = parseInt(secStr, "second", SECONDS_PER_MINUTE);

        double totalSeconds = seconds + (minutes + (hours * 60.0)) * 60.0;
        return totalSeconds / (SECONDS_PER_DAY);
    }
    /**
     * Converts a string of format "YYYY/MM/DD" to its (Excel) numeric equivalent
     *
     * @return a double representing the (integer) number of days since the start of the Excel epoch
     */
    public static Date parseYYYYMMDDDate(String dateStr) {
        try {
            return parseYYYYMMDDDateInternal(dateStr);
        } catch (FormatException e) {
            String msg = "Bad time format " + dateStr
                + " expected 'YYYY/MM/DD' - " + e.getMessage();
            throw new IllegalArgumentException(msg);
        }
    }
    private static Date parseYYYYMMDDDateInternal(String timeStr) throws FormatException {
        if(timeStr.length() != 10) {
            throw new FormatException("Bad length");
        }

        String yearStr = timeStr.substring(0, 4);
        String monthStr = timeStr.substring(5, 7);
        String dayStr = timeStr.substring(8, 10);
        int year = parseInt(yearStr, "year", Short.MIN_VALUE, Short.MAX_VALUE);
        int month = parseInt(monthStr, "month", 1, 12);
        int day = parseInt(dayStr, "day", 1, 31);

        Calendar cal = LocaleUtil.getLocaleCalendar(year, month-1, day);
        return cal.getTime();
    }
    private static int parseInt(String strVal, String fieldName, int rangeMax) throws FormatException {
        return parseInt(strVal, fieldName, 0, rangeMax-1);
    }

    private static int parseInt(String strVal, String fieldName, int lowerLimit, int upperLimit) throws FormatException {
        int result;
        try {
            result = Integer.parseInt(strVal);
        } catch (NumberFormatException e) {
            throw new FormatException("Bad int format '" + strVal + "' for " + fieldName + " field");
        }
        if (result < lowerLimit || result > upperLimit) {
            throw new FormatException(fieldName + " value (" + result
                    + ") is outside the allowable range(0.." + upperLimit + ")");
        }
        return result;
    }

    public static Double parseDateTime(String str){
        TemporalAccessor tmp = dateTimeFormats.parse(str.replaceAll("\\s+", " "));
        LocalTime time = tmp.query(TemporalQueries.localTime());
        LocalDate date = tmp.query(TemporalQueries.localDate());
        if(time == null && date == null) return null;

        double tm = 0;
        if(date != null) {
            Date d = Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            tm = DateUtil.getExcelDate(d);
        }
        if(time != null) tm += 1.0*time.toSecondOfDay()/SECONDS_PER_DAY;

        return tm;
    }
}
