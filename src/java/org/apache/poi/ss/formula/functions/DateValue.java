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

package org.apache.poi.ss.formula.functions;

import java.text.DateFormatSymbols;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.LocaleUtil;

/**
 * Implementation for the DATEVALUE() Excel function.<p>
 *
 * <b>Syntax:</b><br>
 * <b>DATEVALUE</b>(<b>date_text</b>)<p>
 * <p>
 * The <b>DATEVALUE</b> function converts a date that is stored as text to a serial number that Excel
 * recognizes as a date. For example, the formula <b>=DATEVALUE("1/1/2008")</b> returns 39448, the
 * serial number of the date 1/1/2008. Remember, though, that your computer's system date setting may
 * cause the results of a <b>DATEVALUE</b> function to vary from this example
 * <p>
 * The <b>DATEVALUE</b> function is helpful in cases where a worksheet contains dates in a text format
 * that you want to filter, sort, or format as dates, or use in date calculations.
 * <p>
 * To view a date serial number as a date, you must apply a date format to the cell. Find links to more
 * information about displaying numbers as dates in the See Also section.
 *
 * @author Milosz Rembisz
 */
public class DateValue extends Fixed1ArgFunction {

    private enum Format {
        YMD_DASHES("^(\\d{4})-(\\w+)-(\\d{1,2})$", "ymd"),
        DMY_DASHES("^(\\d{1,2})-(\\w+)-(\\d{4})$", "dmy"),
        MD_DASHES("^(\\w+)-(\\d{1,2})$", "md"),
        MDY_SLASHES("^(\\w+)/(\\d{1,2})/(\\d{4})$", "mdy"),
        YMD_SLASHES("^(\\d{4})/(\\w+)/(\\d{1,2})$", "ymd"),
        MD_SLASHES("^(\\w+)/(\\d{1,2})$", "md");

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

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval dateTextArg) {
        try {
            String dateText = OperandResolver.coerceValueToString(
                    OperandResolver.getSingleValue(dateTextArg, srcRowIndex, srcColumnIndex));

            if (dateText == null || dateText.isEmpty()) {
                return BlankEval.instance;
            }

            for (Format format : Format.values()) {
                Matcher matcher = format.pattern.matcher(dateText);
                if (matcher.find()) {
                    MatchResult matchResult = matcher.toMatchResult();
                    List<String> groups = new ArrayList<>();
                    for (int i = 1; i <= matchResult.groupCount(); ++i) {
                        groups.add(matchResult.group(i));
                    }
                    int year = format.hasYear
                            ? Integer.valueOf(groups.get(format.yearIndex))
                            : LocalDate.now(LocaleUtil.getUserTimeZone().toZoneId()).getYear();
                    int month = parseMonth(groups.get(format.monthIndex));
                    int day = Integer.valueOf(groups.get(format.dayIndex));
                    return new NumberEval(DateUtil.getExcelDate(LocalDate.of(year, month, day)));

                }
            }
        } catch (DateTimeException e) {
            return ErrorEval.VALUE_INVALID;
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }

        return ErrorEval.VALUE_INVALID;
    }

    private int parseMonth(String monthPart) {
        try {
            return Integer.valueOf(monthPart);
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
}
