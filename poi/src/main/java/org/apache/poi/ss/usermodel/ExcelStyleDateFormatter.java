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

import java.math.RoundingMode;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.poi.util.LocaleUtil;

/**
 * A wrapper around a {@link SimpleDateFormat} instance,
 * which handles a few Excel-style extensions that
 * are not supported by {@link SimpleDateFormat}.
 * Currently, the extensions are around the handling
 * of elapsed time, eg rendering 1 day 2 hours
 * as 26 hours.
 */
public class ExcelStyleDateFormatter extends SimpleDateFormat {
    public static final char MMMMM_START_SYMBOL = '\ue001';
    public static final char MMMMM_TRUNCATE_SYMBOL = '\ue002';
    public static final char H_BRACKET_SYMBOL = '\ue010';
    public static final char HH_BRACKET_SYMBOL = '\ue011';
    public static final char M_BRACKET_SYMBOL = '\ue012';
    public static final char MM_BRACKET_SYMBOL = '\ue013';
    public static final char S_BRACKET_SYMBOL = '\ue014';
    public static final char SS_BRACKET_SYMBOL = '\ue015';
    public static final char L_BRACKET_SYMBOL = '\ue016';
    public static final char LL_BRACKET_SYMBOL = '\ue017';

    private static final Pattern MMMMM_TRUNCATE_PATTERN = Pattern.compile(
            MMMMM_START_SYMBOL + "(\\p{L}|\\p{P})[\\p{L}\\p{P}]+" + MMMMM_TRUNCATE_SYMBOL);
    // seconds, or elapsed seconds ([s] or [ss]), followed by fractions of a second
    private static final String SECONDS = "([s" + S_BRACKET_SYMBOL + SS_BRACKET_SYMBOL + "])";
    private static final Pattern MILLIS_3_DIGITS_PATTERN = Pattern.compile(SECONDS + ".000");
    private static final Pattern MILLIS_2_DIGITS_PATTERN = Pattern.compile(SECONDS + ".00");
    private static final Pattern MILLIS_1_DIGIT_PATTERN = Pattern.compile(SECONDS + ".0");

    private static final DecimalFormat format1digit;
    private static final DecimalFormat format2digits;

    private static final DecimalFormat format3digit;
    private static final DecimalFormat format4digits;

    static {
        DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(Locale.ROOT);
        format1digit = new DecimalFormat("0", dfs);
        format2digits = new DecimalFormat("00", dfs);
        format3digit = new DecimalFormat("0", dfs);
        format4digits = new DecimalFormat("00", dfs);
        DataFormatter.setExcelStyleRoundingMode(format1digit, RoundingMode.DOWN);
        DataFormatter.setExcelStyleRoundingMode(format2digits, RoundingMode.DOWN);
        DataFormatter.setExcelStyleRoundingMode(format3digit);
        DataFormatter.setExcelStyleRoundingMode(format4digits);
    }

    {
        setTimeZone(LocaleUtil.getUserTimeZone());
    }

    private static final int SECONDS_PER_DAY = 24 * 60 * 60;

    private double dateToBeFormatted;

    /** the pattern {@link #roundToSeconds} was worked out for */
    private String roundToSecondsPattern;
    private boolean roundToSeconds;

    // no-arg constructor is private because of undefined super call with locale

    public ExcelStyleDateFormatter(String pattern) {
        super(processFormatPattern(pattern), LocaleUtil.getUserLocale());
    }

    public ExcelStyleDateFormatter(String pattern,
                                   DateFormatSymbols formatSymbols) {
        super(processFormatPattern(pattern), formatSymbols);
    }

    public ExcelStyleDateFormatter(String pattern, Locale locale) {
        super(processFormatPattern(pattern), locale);
    }

    /**
     * Takes a format String, and replaces Excel specific bits
     * with our detection sequences
     */
    private static String processFormatPattern(String f) {
        String t = f.replace("MMMMM", MMMMM_START_SYMBOL + "MMM" + MMMMM_TRUNCATE_SYMBOL);
        t = t.replace("[H]", String.valueOf(H_BRACKET_SYMBOL));
        t = t.replace("[HH]", String.valueOf(HH_BRACKET_SYMBOL));
        t = t.replace("[m]", String.valueOf(M_BRACKET_SYMBOL));
        t = t.replace("[mm]", String.valueOf(MM_BRACKET_SYMBOL));
        t = t.replace("[s]", String.valueOf(S_BRACKET_SYMBOL));
        t = t.replace("[ss]", String.valueOf(SS_BRACKET_SYMBOL));
        t = t.replace("T", "'T'");
        t = t.replace("''T''", "'T'");
        t = MILLIS_3_DIGITS_PATTERN.matcher(t).replaceAll("$1.SSS");
        t = MILLIS_2_DIGITS_PATTERN.matcher(t).replaceAll("$1." + LL_BRACKET_SYMBOL);
        t = MILLIS_1_DIGIT_PATTERN.matcher(t).replaceAll("$1." + L_BRACKET_SYMBOL);
        return t;
    }

    /**
     * Used to let us know what the date being
     * formatted is, in Excel terms, which we
     * may wish to use when handling elapsed
     * times.
     */
    public void setDateToBeFormatted(double date) {
        this.dateToBeFormatted = date;
    }

    @Override
    public StringBuffer format(Date date, StringBuffer paramStringBuffer,
                               FieldPosition paramFieldPosition) {
        // Excel rounds to the nearest second when it shows seconds but no fractions of a second,
        // e.g. hh:mm:ss shows 00:04:37.638 as 00:04:38
        double elapsed = dateToBeFormatted;
        if (showsWholeSeconds()) {
            if (date != null) {
                date = new Date(Math.round(date.getTime() / 1000.0) * 1000);
            }
            elapsed = Math.round(elapsed * SECONDS_PER_DAY) / (double) SECONDS_PER_DAY;
        }

        // Do the normal format
        String s = super.format(date, paramStringBuffer, paramFieldPosition).toString();

        // Now handle our special cases
        if (s.indexOf(MMMMM_START_SYMBOL) != -1) {
            s = MMMMM_TRUNCATE_PATTERN.matcher(s).replaceAll("$1");
        }

        if (s.indexOf(H_BRACKET_SYMBOL) != -1 ||
                s.indexOf(HH_BRACKET_SYMBOL) != -1) {
            float hours = (float) elapsed * 24;

            s = s.replace(
                    String.valueOf(H_BRACKET_SYMBOL),
                    format1digit.format(hours)
            );
            s = s.replace(
                    String.valueOf(HH_BRACKET_SYMBOL),
                    format2digits.format(hours)
            );
        }

        if (s.indexOf(M_BRACKET_SYMBOL) != -1 ||
                s.indexOf(MM_BRACKET_SYMBOL) != -1) {
            float minutes = (float) elapsed * 24 * 60;
            s = s.replace(
                    String.valueOf(M_BRACKET_SYMBOL),
                    format1digit.format(minutes)
            );
            s = s.replace(
                    String.valueOf(MM_BRACKET_SYMBOL),
                    format2digits.format(minutes)
            );
        }
        if (s.indexOf(S_BRACKET_SYMBOL) != -1 ||
                s.indexOf(SS_BRACKET_SYMBOL) != -1) {
            float seconds = (float) (elapsed * 24 * 60 * 60);
            s = s.replace(
                    String.valueOf(S_BRACKET_SYMBOL),
                    format1digit.format(seconds)
            );
            s = s.replace(
                    String.valueOf(SS_BRACKET_SYMBOL),
                    format2digits.format(seconds)
            );
        }

        if (s.indexOf(L_BRACKET_SYMBOL) != -1 ||
                s.indexOf(LL_BRACKET_SYMBOL) != -1) {
            float millisTemp = (float) ((dateToBeFormatted - Math.floor(dateToBeFormatted)) * 24 * 60 * 60);
            float millis = (millisTemp - (int) millisTemp);
            s = s.replace(
                    String.valueOf(L_BRACKET_SYMBOL),
                    format3digit.format(millis * 10.0)
            );
            s = s.replace(
                    String.valueOf(LL_BRACKET_SYMBOL),
                    format4digits.format(millis * 100.0)
            );
        }

        return new StringBuffer(s);
    }

    /**
     * Whether the pattern shows seconds, or elapsed seconds, but no fractions of a second
     */
    private boolean showsWholeSeconds() {
        String pattern = toPattern();
        if (!pattern.equals(roundToSecondsPattern)) {
            boolean seconds = false;
            boolean fractions = false;
            boolean quoted = false;
            for (int i = 0; i < pattern.length(); i++) {
                char c = pattern.charAt(i);
                if (c == '\'') {
                    quoted = !quoted;
                } else if (!quoted) {
                    seconds |= c == 's' || c == S_BRACKET_SYMBOL || c == SS_BRACKET_SYMBOL;
                    fractions |= c == 'S' || c == L_BRACKET_SYMBOL || c == LL_BRACKET_SYMBOL;
                }
            }
            roundToSeconds = seconds && !fractions;
            roundToSecondsPattern = pattern;
        }
        return roundToSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ExcelStyleDateFormatter other)) {
            return false;
        }

        return dateToBeFormatted == other.dateToBeFormatted;
    }

    @Override
    public int hashCode() {
        return Double.valueOf(dateToBeFormatted).hashCode();
    }
}
