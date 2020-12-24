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

package org.apache.poi.ss.util;

import java.text.DateFormatSymbols;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.util.LocaleUtil;

/**
 * Parser for java dates.
 */
public class DateParser {
    protected DateParser() {
        // enforcing singleton
    }


    private enum Format {
        YMD_DASHES("^(\\d{4})-(\\w+)-(\\d{1,2})( .*)?$", "ymd"),
        DMY_DASHES("^(\\d{1,2})-(\\w+)-(\\d{4})( .*)?$", "dmy"),
        MD_DASHES("^(\\w+)-(\\d{1,2})( .*)?$", "md"),
        MDY_SLASHES("^(\\w+)/(\\d{1,2})/(\\d{4})( .*)?$", "mdy"),
        YMD_SLASHES("^(\\d{4})/(\\w+)/(\\d{1,2})( .*)?$", "ymd"),
        MD_SLASHES("^(\\w+)/(\\d{1,2})( .*)?$", "md");

        private Pattern pattern;
        private boolean hasYear;
        private int yearIndex;
        private int monthIndex;
        private int dayIndex;

        Format(String patternString, String groupOrder) {
            this.pattern = Pattern.compile(patternString);
            this.hasYear = groupOrder.contains("y");
            if (hasYear) {
                yearIndex = groupOrder.indexOf("y");
            }
            monthIndex = groupOrder.indexOf("m");
            dayIndex = groupOrder.indexOf("d");
        }

    }

    private static int parseMonth(String monthPart) {
        try {
            return Integer.parseInt(monthPart);
        } catch (NumberFormatException ignored) {
        }


        String[] months = DateFormatSymbols.getInstance(LocaleUtil.getUserLocale()).getMonths();
        for (int month = 0; month < months.length; ++month) {
            if (months[month].toLowerCase(LocaleUtil.getUserLocale()).startsWith(monthPart.toLowerCase(LocaleUtil.getUserLocale()))) {
                return month + 1;
            }
        }
        return -1;
    }

    /**
     * Parses a date from a string.
     *
     * @param strVal a string with a date pattern.
     * @return a date parsed from argument.
     * @throws EvaluationException exception upon parsing.
     */
    public static LocalDate parseLocalDate(String strVal) throws EvaluationException {
        for (Format format : Format.values()) {
            Matcher matcher = format.pattern.matcher(strVal);
            if (matcher.find()) {
                MatchResult matchResult = matcher.toMatchResult();
                List<String> groups = new ArrayList<>();
                for (int i = 1; i <= matchResult.groupCount(); ++i) {
                    groups.add(matchResult.group(i));
                }
                int year = format.hasYear
                    ? Integer.parseInt(groups.get(format.yearIndex))
                    : LocalDate.now(LocaleUtil.getUserTimeZone().toZoneId()).getYear();
                int month = parseMonth(groups.get(format.monthIndex));
                int day = Integer.parseInt(groups.get(format.dayIndex));
                try {
                    return LocalDate.of(year, month, day);
                } catch (DateTimeException e) {
                    throw new DateTimeException("Failed to parse date-string " + strVal);
                }

            }
        }

        throw new EvaluationException(ErrorEval.VALUE_INVALID);
    }

    public static Calendar parseDate(String strVal) throws EvaluationException {
        LocalDate date = parseLocalDate(strVal);
        return makeDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    /**
     * @param month 1-based
     */
    private static Calendar makeDate(int year, int month, int day) throws EvaluationException {
        if (month < 1 || month > 12) {
            throw new EvaluationException(ErrorEval.VALUE_INVALID);
        }
        Calendar cal = LocaleUtil.getLocaleCalendar(year, month - 1, 1, 0, 0, 0);
        if (day < 1 || day > cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            throw new EvaluationException(ErrorEval.VALUE_INVALID);
        }
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal;
    }

}
