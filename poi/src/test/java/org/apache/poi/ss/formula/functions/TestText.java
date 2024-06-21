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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.DateFormatSymbols;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Test;

/**
 * Test case for TEXT()
 */
final class TestText {
    private static final List<ErrorEval> EXCEL_ERRORS = Arrays.asList(ErrorEval.NULL_INTERSECTION, ErrorEval.DIV_ZERO,
            ErrorEval.VALUE_INVALID, ErrorEval.REF_INVALID, ErrorEval.NAME_INVALID, ErrorEval.NUM_ERROR, ErrorEval.NA);

    @Test
    void testTextWithStringFirstArg() {
        ValueEval strArg = new StringEval("abc");
        ValueEval formatArg = new StringEval("abc");
        ValueEval[] args = { strArg, formatArg };
        ValueEval result = TextFunction.TEXT.evaluate(args, -1, (short)-1);
        assertEquals(strArg.toString(), result.toString());
    }

    @Test
    void testTextWithDecimalFormatSecondArg() {
        ValueEval numArg = new NumberEval(321321.321);
        ValueEval formatArg = new StringEval("#,###.00000");
        ValueEval[] args = { numArg, formatArg };
        ValueEval result = TextFunction.TEXT.evaluate(args, -1, (short)-1);
        DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(LocaleUtil.getUserLocale());
        char groupSeparator = dfs.getGroupingSeparator();
        char decimalSeparator = dfs.getDecimalSeparator();
        ValueEval testResult = new StringEval("321" + groupSeparator + "321" + decimalSeparator + "32100");
        assertEquals(testResult.toString(), result.toString());
        numArg = new NumberEval(321.321);
        formatArg = new StringEval("00000.00000");
        args[0] = numArg;
        args[1] = formatArg;
        result = TextFunction.TEXT.evaluate(args, -1, (short)-1);
        testResult = new StringEval("00321" + decimalSeparator + "32100");
        assertEquals(testResult.toString(), result.toString());

        formatArg = new StringEval("$#.#");
        args[1] = formatArg;
        result = TextFunction.TEXT.evaluate(args, -1, (short)-1);
        testResult = new StringEval("$321" + decimalSeparator + "3");
        assertEquals(testResult.toString(), result.toString());
    }

    @Test
    void testTextWithFractionFormatSecondArg() {
        ValueEval numArg = new NumberEval(321.321);
        ValueEval formatArg = new StringEval("# #/#");
        ValueEval[] args = { numArg, formatArg };
        ValueEval result = TextFunction.TEXT.evaluate(args, -1, (short)-1);
        ValueEval testResult = new StringEval("321 1/3");
        assertEquals(testResult.toString(), result.toString());

        formatArg = new StringEval("# #/##");
        args[1] = formatArg;
        result = TextFunction.TEXT.evaluate(args, -1, (short)-1);
        testResult = new StringEval("321 26/81");
        assertEquals(testResult.toString(), result.toString());

        formatArg = new StringEval("#/##");
        args[1] = formatArg;
        result = TextFunction.TEXT.evaluate(args, -1, (short)-1);
        testResult = new StringEval("26027/81");
        assertEquals(testResult.toString(), result.toString());
    }

    @Test
    void testTextWithDateFormatSecondArg() {
        TimeZone userTZ = LocaleUtil.getUserTimeZone();
        LocaleUtil.setUserTimeZone(TimeZone.getTimeZone("CET"));
        try {
            // Test with Java style M=Month
            ValueEval numArg = new NumberEval(321.321);
            ValueEval formatArg = new StringEval("dd:MM:yyyy hh:mm:ss");
            ValueEval[] args = { numArg, formatArg };
            ValueEval result = TextFunction.TEXT.evaluate(args, -1, -1);
            ValueEval testResult = new StringEval("16:11:1900 07:42:14");
            assertEquals(testResult.toString(), result.toString());

            // Excel also supports "m before h is month"
            formatArg = new StringEval("dd:mm:yyyy hh:mm:ss");
            args[1] = formatArg;
            result = TextFunction.TEXT.evaluate(args, -1, -1);
            testResult = new StringEval("16:11:1900 07:42:14");
            assertEquals(testResult.toString(), result.toString());

            // Excel also supports ".SSS"
            formatArg = new StringEval("dd:mm:yyyy hh:mm:ss.SSS");
            args[1] = formatArg;
            result = TextFunction.TEXT.evaluate(args, -1, -1);
            testResult = new StringEval("16:11:1900 07:42:14.014");
            assertEquals(testResult.toString(), result.toString());

            // this line is intended to compute how "November" would look like in the current locale
            // update: now the locale will be (if not set otherwise) always Locale.getDefault() (see LocaleUtil)
            DateFormatSymbols dfs = DateFormatSymbols.getInstance(LocaleUtil.getUserLocale());
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM", dfs);
            sdf.setTimeZone(LocaleUtil.getUserTimeZone());
            String november = sdf.format(LocaleUtil.getLocaleCalendar(2015,10,1).getTime());

            // Again with Java style
            formatArg = new StringEval("MMMM dd, yyyy");
            args[1] = formatArg;
            result = TextFunction.TEXT.evaluate(args, -1, -1);
            testResult = new StringEval(november + " 16, 1900");
            assertEquals(testResult.toString(), result.toString());

            // And Excel style
            formatArg = new StringEval("mmmm dd, yyyy");
            args[1] = formatArg;
            result = TextFunction.TEXT.evaluate(args, -1, -1);
            testResult = new StringEval(november + " 16, 1900");
            assertEquals(testResult.toString(), result.toString());
        } finally {
            LocaleUtil.setUserTimeZone(userTZ);
        }
    }

    @Test
    void testTextWithISODateTimeFormatSecondArg() {
        ValueEval numArg = new NumberEval(321.321);
        ValueEval formatArg = new StringEval("yyyy-mm-ddThh:MM:ss");
        ValueEval[] args = { numArg, formatArg };
        ValueEval result = TextFunction.TEXT.evaluate(args, -1, -1);
        ValueEval testResult = new StringEval("1900-11-16T07:42:14");
        assertEquals(testResult.toString(), result.toString());

        // test milliseconds
        formatArg = new StringEval("yyyy-mm-ddThh:MM:ss.000");
        args[1] = formatArg;
        result = TextFunction.TEXT.evaluate(args, -1, -1);
        testResult = new StringEval("1900-11-16T07:42:14.400");
        assertEquals(testResult.toString(), result.toString());
    }

    // Test cases from the workbook attached to the bug 67475 which were OK

    @Test
    void testTextVariousValidNumberFormats() {
        // negative values: 3 decimals
        testText(new NumberEval(-123456.789012345), new StringEval("#0.000"), "-123456.789");
        // no decimals
        testText(new NumberEval(-123456.789012345), new StringEval("000000"), "-123457");
        // common format - more digits
        testText(new NumberEval(-123456.789012345), new StringEval("00.0000"), "-123456.7890");
        // common format - less digits
        testText(new NumberEval(-12.78), new StringEval("00000.000000"), "-00012.780000");
        // half up
        testText(new NumberEval(-0.56789012375), new StringEval("#0.0000000000"), "-0.5678901238");
        // half up
        testText(new NumberEval(-0.56789012385), new StringEval("#0.0000000000"), "-0.5678901239");
        // positive values: 3 decimals
        testText(new NumberEval(123456.789012345), new StringEval("#0.000"), "123456.789");
        // no decimals
        testText(new NumberEval(123456.789012345), new StringEval("000000"), "123457");
        // common format - more digits
        testText(new NumberEval(123456.789012345), new StringEval("00.0000"), "123456.7890");
        // common format - less digits
        testText(new NumberEval(12.78), new StringEval("00000.000000"), "00012.780000");
        // half up
        testText(new NumberEval(0.56789012375), new StringEval("#0.0000000000"), "0.5678901238");
        // half up
        testText(new NumberEval(0.56789012385), new StringEval("#0.0000000000"), "0.5678901239");
    }

    @Test
    void testTextBlankTreatedAsZero() {
        testText(BlankEval.instance, new StringEval("#0.000"), "0.000");
    }

    @Test
    void testTextStrangeFormat() {
        // number 0
        testText(new NumberEval(-123456.789012345), new NumberEval(0), "-123457");
        // negative number with few zeros
        testText(new NumberEval(-123456.789012345), new NumberEval(-0.0001), "--123456.7891");
        // format starts with "."
        testText(new NumberEval(0.0123), new StringEval(".000"), ".012");
        // one zero negative
        testText(new NumberEval(1001.202), new NumberEval(-8808), "-8810018");
        // format contains 0
        testText(new NumberEval(43368.0), new NumberEval(909), "9433689");
    }

    @Test
    void testTextErrorAsFormat() {
        for (ErrorEval errorEval : EXCEL_ERRORS) {
            testText(new NumberEval(3.14), errorEval, errorEval);
            testText(BoolEval.TRUE, errorEval, errorEval);
            testText(BoolEval.FALSE, errorEval, errorEval);
        }
    }

    @Test
    void testTextErrorAsValue() {
        for (ErrorEval errorEval : EXCEL_ERRORS) {
            testText(errorEval, new StringEval("#0.000"), errorEval);
            testText(errorEval, new StringEval("yyyymmmdd"), errorEval);
        }
    }

    // Test cases from the workbook attached to the bug 67475 which were failing and are fixed by the patch

    @Test
    void testTextEmptyStringWithDateFormat() {
        testText(new StringEval(""), new StringEval("yyyymmmdd"), "");
    }

    @Test
    void testTextAnyTextWithDateFormat() {
        testText(new StringEval("anyText"), new StringEval("yyyymmmdd"), "anyText");
    }

    @Test
    void testTextBooleanWithDateFormat() {
        testText(BoolEval.TRUE, new StringEval("yyyymmmdd"), BoolEval.TRUE.getStringValue());
        testText(BoolEval.FALSE, new StringEval("yyyymmmdd"), BoolEval.FALSE.getStringValue());
    }

    @Test
    void testTextNumberWithBooleanFormat() {
        testText(new NumberEval(43368), BoolEval.TRUE, ErrorEval.VALUE_INVALID);
        testText(new NumberEval(43368), BoolEval.FALSE, ErrorEval.VALUE_INVALID);

        testText(new NumberEval(3.14), BoolEval.TRUE, ErrorEval.VALUE_INVALID);
        testText(new NumberEval(3.14), BoolEval.FALSE, ErrorEval.VALUE_INVALID);
    }

    @Test
    void testTextEmptyStringWithNumberFormat() {
        testText(new StringEval(""), new StringEval("#0.000"), "");
    }

    @Test
    void testTextAnyTextWithNumberFormat() {
        testText(new StringEval("anyText"), new StringEval("#0.000"), "anyText");
    }

    @Test
    void testTextBooleanWithNumberFormat() {
        testText(BoolEval.TRUE, new StringEval("#0.000"), BoolEval.TRUE.getStringValue());
        testText(BoolEval.FALSE, new StringEval("#0.000"), BoolEval.FALSE.getStringValue());
    }

    @Test
    void testTextMMM() {
        LocalDate ld = LocalDate.parse("2022-02-28");
        testText(new NumberEval(DateUtil.getExcelDate(ld)), new StringEval("MMM"), "Feb");
    }

    @Test
    void testTextMMMStringInput() {
        //  https://bz.apache.org/bugzilla/show_bug.cgi?id=67475
        String dateInput = "02/28/2022";
        testText(new StringEval(dateInput), new StringEval("MMM"), "Feb");
    }

    private void testText(ValueEval valueArg, ValueEval formatArg, String expectedResult) {
        ValueEval[] args = { valueArg, formatArg };
        ValueEval result = TextFunction.TEXT.evaluate(args, -1, -1);

        assertTrue(result instanceof StringEval, "Expected StringEval got " + result.getClass().getSimpleName());
        assertEquals(expectedResult, ((StringEval) result).getStringValue());
    }

    private void testText(ValueEval valueArg, ValueEval formatArg, ErrorEval expectedResult) {
        ValueEval[] args = { valueArg, formatArg };
        ValueEval result = TextFunction.TEXT.evaluate(args, -1, -1);

        assertTrue(result instanceof ErrorEval, "Expected ErrorEval got " + result.getClass().getSimpleName());
        assertEquals(expectedResult, result);
    }
}
