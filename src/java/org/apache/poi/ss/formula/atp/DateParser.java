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

package org.apache.poi.ss.formula.atp;

import java.util.Calendar;
import java.util.regex.Pattern;

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.util.LocaleUtil;

/**
 * Parser for java dates.
 */
public class DateParser {
    private DateParser() {
        // enforcing singleton
    }

    /**
     * Parses a date from a string.
     * 
     * @param strVal a string with a date pattern.
     * @return a date parsed from argument.
     * @throws EvaluationException exception upon parsing.
     */
    public static Calendar parseDate(String strVal) throws EvaluationException {
        String[] parts = Pattern.compile("/").split(strVal);
        if (parts.length != 3) {
            throw new EvaluationException(ErrorEval.VALUE_INVALID);
        }
        String part2 = parts[2];
        int spacePos = part2.indexOf(' ');
        if (spacePos > 0) {
            // drop time portion if present
            part2 = part2.substring(0, spacePos);
        }
        int f0;
        int f1;
        int f2;
        try {
            f0 = Integer.parseInt(parts[0]);
            f1 = Integer.parseInt(parts[1]);
            f2 = Integer.parseInt(part2);
        } catch (NumberFormatException e) {
            throw new EvaluationException(ErrorEval.VALUE_INVALID);
        }
        if (f0 < 0 || f1 < 0 || f2 < 0 || (f0 > 12 && f1 > 12 && f2 > 12)) {
            // easy to see this cannot be a valid date
            throw new EvaluationException(ErrorEval.VALUE_INVALID);
        }

        if (f0 >= 1900 && f0 < 9999) {
            // when 4 digit value appears first, the format is YYYY/MM/DD, regardless of OS settings
            return makeDate(f0, f1, f2);
        }
        // otherwise the format seems to depend on OS settings (default date format)
//        if (false) {
//            // MM/DD/YYYY is probably a good guess, if the in the US
//            return makeDate(f2, f0, f1);
//        }
        // TODO - find a way to choose the correct date format
        throw new RuntimeException("Unable to determine date format for text '" + strVal + "'");
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
