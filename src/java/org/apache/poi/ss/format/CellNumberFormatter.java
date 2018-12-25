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
package org.apache.poi.ss.format;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * This class implements printing out a value using a number format.
 */
public class CellNumberFormatter extends CellFormatter {
    private static final POILogger LOG = POILogFactory.getLogger(CellNumberFormatter.class);

    private final String desc;
    private final String printfFmt;
    private final double scale;
    private final Special decimalPoint;
    private final Special slash;
    private final Special exponent;
    private final Special numerator;
    private final Special afterInteger;
    private final Special afterFractional;
    private final boolean showGroupingSeparator;
    private final List<Special> specials = new ArrayList<>();
    private final List<Special> integerSpecials = new ArrayList<>();
    private final List<Special> fractionalSpecials = new ArrayList<>();
    private final List<Special> numeratorSpecials = new ArrayList<>();
    private final List<Special> denominatorSpecials = new ArrayList<>();
    private final List<Special> exponentSpecials = new ArrayList<>();
    private final List<Special> exponentDigitSpecials = new ArrayList<>();
    private final int maxDenominator;
    private final String numeratorFmt;
    private final String denominatorFmt;
    private final boolean improperFraction;
    private final DecimalFormat decimalFmt;

    // The CellNumberFormatter.simpleValue() method uses the SIMPLE_NUMBER
    // CellFormatter defined here. The CellFormat.GENERAL_FORMAT CellFormat
    // no longer uses the SIMPLE_NUMBER CellFormatter.
    // Note that the simpleValue()/SIMPLE_NUMBER CellFormatter format
    // ("#" for integer values, and "#.#" for floating-point values) is
    // different from the 'General' format for numbers ("#" for integer
    // values and "#.#########" for floating-point values).
    private final CellFormatter SIMPLE_NUMBER = new GeneralNumberFormatter(locale);

    private static class GeneralNumberFormatter extends CellFormatter {
        private GeneralNumberFormatter(Locale locale) {
            super(locale, "General");
        }

        public void formatValue(StringBuffer toAppendTo, Object value) {
            if (value == null) {
                return;
            }

            CellFormatter cf;
            if (value instanceof Number) {
                Number num = (Number) value;
                cf = (num.doubleValue() % 1.0 == 0) ? new CellNumberFormatter(locale, "#") :
                    new CellNumberFormatter(locale, "#.#");
            } else {
                cf = CellTextFormatter.SIMPLE_TEXT;
            }
            cf.formatValue(toAppendTo, value);
        }

        public void simpleValue(StringBuffer toAppendTo, Object value) {
            formatValue(toAppendTo, value);
        }
    }


    /**
     * This class is used to mark where the special characters in the format
     * are, as opposed to the other characters that are simply printed.
     */
    /* package */ static class Special {
        final char ch;
        int pos;

        Special(char ch, int pos) {
            this.ch = ch;
            this.pos = pos;
        }

        @Override
        public String toString() {
            return "'" + ch + "' @ " + pos;
        }
    }

    /**
     * Creates a new cell number formatter.
     *
     * @param format The format to parse.
     */
    public CellNumberFormatter(String format) {
        this(LocaleUtil.getUserLocale(), format);
    }

    /**
     * Creates a new cell number formatter.
     *
     * @param locale The locale to use.
     * @param format The format to parse.
     */
    public CellNumberFormatter(Locale locale, String format) {
        super(locale, format);

        CellNumberPartHandler ph = new CellNumberPartHandler();
        StringBuffer descBuf = CellFormatPart.parseFormat(format, CellFormatType.NUMBER, ph);

        exponent = ph.getExponent();
        specials.addAll(ph.getSpecials());
        improperFraction = ph.isImproperFraction();

        // These are inconsistent settings, so ditch 'em
        if ((ph.getDecimalPoint() != null || ph.getExponent() != null) && ph.getSlash() != null) {
            slash = null;
            numerator = null;
        } else {
            slash = ph.getSlash();
            numerator = ph.getNumerator();
        }

        final int precision = interpretPrecision(ph.getDecimalPoint(), specials);
        int fractionPartWidth = 0;
        if (ph.getDecimalPoint() != null) {
            fractionPartWidth = 1 + precision;
            if (precision == 0) {
                // This means the format has a ".", but that output should have no decimals after it.
                // We just stop treating it specially
                specials.remove(ph.getDecimalPoint());
                decimalPoint = null;
            } else {
                decimalPoint = ph.getDecimalPoint();
            }
        } else {
            decimalPoint = null;
        }

        if (decimalPoint != null) {
            afterInteger = decimalPoint;
        } else if (exponent != null) {
            afterInteger = exponent;
        } else if (numerator != null) {
            afterInteger = numerator;
        } else {
            afterInteger = null;
        }

        if (exponent != null) {
            afterFractional = exponent;
        } else if (numerator != null) {
            afterFractional = numerator;
        } else {
            afterFractional = null;
        }

        double[] scaleByRef = {ph.getScale()};
        showGroupingSeparator = interpretIntegerCommas(descBuf, specials, decimalPoint, integerEnd(), fractionalEnd(), scaleByRef);
        if (exponent == null) {
            scale = scaleByRef[0];
        } else {
            // in "e" formats,% and trailing commas have no scaling effect
            scale = 1;
        }

        if (precision != 0) {
            // TODO: if decimalPoint is null (-> index == -1), return the whole list?
            fractionalSpecials.addAll(specials.subList(specials.indexOf(decimalPoint) + 1, fractionalEnd()));
        }

        if (exponent != null) {
            int exponentPos = specials.indexOf(exponent);
            exponentSpecials.addAll(specialsFor(exponentPos, 2));
            exponentDigitSpecials.addAll(specialsFor(exponentPos + 2));
        }

        if (slash != null) {
            if (numerator != null) {
                numeratorSpecials.addAll(specialsFor(specials.indexOf(numerator)));
            }

            denominatorSpecials.addAll(specialsFor(specials.indexOf(slash) + 1));
            if (denominatorSpecials.isEmpty()) {
                // no denominator follows the slash, drop the fraction idea
                numeratorSpecials.clear();
                maxDenominator = 1;
                numeratorFmt = null;
                denominatorFmt = null;
            } else {
                maxDenominator = maxValue(denominatorSpecials);
                numeratorFmt = singleNumberFormat(numeratorSpecials);
                denominatorFmt = singleNumberFormat(denominatorSpecials);
            }
        } else {
            maxDenominator = 1;
            numeratorFmt = null;
            denominatorFmt = null;
        }

        integerSpecials.addAll(specials.subList(0, integerEnd()));

        if (exponent == null) {
            StringBuffer fmtBuf = new StringBuffer("%");

            int integerPartWidth = calculateIntegerPartWidth();
            int totalWidth = integerPartWidth + fractionPartWidth;

            fmtBuf.append('0').append(totalWidth).append('.').append(precision);

            fmtBuf.append("f");
            printfFmt = fmtBuf.toString();
            decimalFmt = null;
        } else {
            StringBuffer fmtBuf = new StringBuffer();
            boolean first = true;
            if (integerSpecials.size() == 1) {
                // If we don't do this, we get ".6e5" instead of "6e4"
                fmtBuf.append("0");
                first = false;
            } else
                for (Special s : integerSpecials) {
                    if (isDigitFmt(s)) {
                        fmtBuf.append(first ? '#' : '0');
                        first = false;
                    }
                }
            if (fractionalSpecials.size() > 0) {
                fmtBuf.append('.');
                for (Special s : fractionalSpecials) {
                    if (isDigitFmt(s)) {
                        if (!first)
                            fmtBuf.append('0');
                        first = false;
                    }
                }
            }
            fmtBuf.append('E');
            placeZeros(fmtBuf, exponentSpecials.subList(2, exponentSpecials.size()));
            decimalFmt = new DecimalFormat(fmtBuf.toString(), getDecimalFormatSymbols());
            printfFmt = null;
        }

        desc = descBuf.toString();
    }

    private DecimalFormatSymbols getDecimalFormatSymbols() {
        return DecimalFormatSymbols.getInstance(locale);
    }
    
    private static void placeZeros(StringBuffer sb, List<Special> specials) {
        for (Special s : specials) {
            if (isDigitFmt(s)) {
                sb.append('0');
            }
        }
    }

    private static CellNumberStringMod insertMod(Special special, CharSequence toAdd, int where) {
        return new CellNumberStringMod(special, toAdd, where);
    }

    private static CellNumberStringMod deleteMod(Special start, boolean startInclusive, Special end, boolean endInclusive) {
        return new CellNumberStringMod(start, startInclusive, end, endInclusive);
    }

    private static CellNumberStringMod replaceMod(Special start, boolean startInclusive, Special end, boolean endInclusive, char withChar) {
        return new CellNumberStringMod(start, startInclusive, end, endInclusive, withChar);
    }

    private static String singleNumberFormat(List<Special> numSpecials) {
        return "%0" + numSpecials.size() + "d";
    }

    private static int maxValue(List<Special> s) {
        return (int) Math.round(Math.pow(10, s.size()) - 1);
    }

    private List<Special> specialsFor(int pos, int takeFirst) {
        if (pos >= specials.size()) {
            return Collections.emptyList();
        }
        ListIterator<Special> it = specials.listIterator(pos + takeFirst);
        Special last = it.next();
        int end = pos + takeFirst;
        while (it.hasNext()) {
            Special s = it.next();
            if (!isDigitFmt(s) || s.pos - last.pos > 1)
                break;
            end++;
            last = s;
        }
        return specials.subList(pos, end + 1);
    }

    private List<Special> specialsFor(int pos) {
        return specialsFor(pos, 0);
    }

    private static boolean isDigitFmt(Special s) {
        return s.ch == '0' || s.ch == '?' || s.ch == '#';
    }

    private int calculateIntegerPartWidth() {
        int digitCount = 0;
        for (Special s : specials) {
            //!! Handle fractions: The previous set of digits before that is the numerator, so we should stop short of that
            if (s == afterInteger) {
                break;
            } else if (isDigitFmt(s)) {
                digitCount++;
            }
        }
        return digitCount;
    }

    private static int interpretPrecision(Special decimalPoint, List<Special> specials) {
        int idx = specials.indexOf(decimalPoint);
        int precision = 0;
        if (idx != -1) {
            // skip over the decimal point itself
            ListIterator<Special> it = specials.listIterator(idx+1);
            while (it.hasNext()) {
                Special s = it.next();
                if (!isDigitFmt(s)) {
                    break;
                }
                precision++;
            }
        }
        return precision;
    }

    private static boolean interpretIntegerCommas
        (StringBuffer sb, List<Special> specials, Special decimalPoint, int integerEnd, int fractionalEnd, double[] scale) {
        // In the integer part, commas at the end are scaling commas; other commas mean to show thousand-grouping commas
        ListIterator<Special> it = specials.listIterator(integerEnd);

        boolean stillScaling = true;
        boolean integerCommas = false;
        while (it.hasPrevious()) {
            Special s = it.previous();
            if (s.ch != ',') {
                stillScaling = false;
            } else {
                if (stillScaling) {
                    scale[0] /= 1000;
                } else {
                    integerCommas = true;
                }
            }
        }

        if (decimalPoint != null) {
            it = specials.listIterator(fractionalEnd);
            while (it.hasPrevious()) {
                Special s = it.previous();
                if (s.ch != ',') {
                    break;
                } else {
                    scale[0] /= 1000;
                }
            }
        }

        // Now strip them out -- we only need their interpretation, not their presence
        it = specials.listIterator();
        int removed = 0;
        while (it.hasNext()) {
            Special s = it.next();
            s.pos -= removed;
            if (s.ch == ',') {
                removed++;
                it.remove();
                sb.deleteCharAt(s.pos);
            }
        }

        return integerCommas;
    }

    private int integerEnd() {
        return (afterInteger == null) ? specials.size() : specials.indexOf(afterInteger);
    }

    private int fractionalEnd() {
        return (afterFractional == null) ? specials.size() : specials.indexOf(afterFractional);
    }

    /** {@inheritDoc} */
    public void formatValue(StringBuffer toAppendTo, Object valueObject) {
        double value = ((Number) valueObject).doubleValue();
        value *= scale;

        // For negative numbers:
        // - If the cell format has a negative number format, this method
        // is called with a positive value and the number format has
        // the negative formatting required, e.g. minus sign or brackets.
        // - If the cell format does not have a negative number format,
        // this method is called with a negative value and the number is
        // formatted with a minus sign at the start.
        boolean negative = value < 0;
        if (negative)
            value = -value;

        // Split out the fractional part if we need to print a fraction
        double fractional = 0;
        if (slash != null) {
            if (improperFraction) {
                fractional = value;
                value = 0;
            } else {
                fractional = value % 1.0;
                //noinspection SillyAssignment
                value = (long) value;
            }
        }

        Set<CellNumberStringMod> mods = new TreeSet<>();
        StringBuffer output = new StringBuffer(localiseFormat(desc));

        if (exponent != null) {
            writeScientific(value, output, mods);
        } else if (improperFraction) {
            writeFraction(value, null, fractional, output, mods);
        } else {
            StringBuffer result = new StringBuffer();
            Formatter f = new Formatter(result, locale);
            try {
                f.format(locale, printfFmt, value);
            } finally {
                f.close();
            }

            if (numerator == null) {
                writeFractional(result, output);
                writeInteger(result, output, integerSpecials, mods, showGroupingSeparator);
            } else {
                writeFraction(value, result, fractional, output, mods);
            }
        }

        DecimalFormatSymbols dfs = getDecimalFormatSymbols();
        String groupingSeparator = Character.toString(dfs.getGroupingSeparator());

        // Now strip out any remaining '#'s and add any pending text ...
        Iterator<CellNumberStringMod> changes = mods.iterator();
        CellNumberStringMod nextChange = (changes.hasNext() ? changes.next() : null);
        // records chars already deleted
        BitSet deletedChars = new BitSet();
        int adjust = 0;
        for (Special s : specials) {
            int adjustedPos = s.pos + adjust;
            if (!deletedChars.get(s.pos) && output.charAt(adjustedPos) == '#') {
                output.deleteCharAt(adjustedPos);
                adjust--;
                deletedChars.set(s.pos);
            }
            while (nextChange != null && s == nextChange.getSpecial()) {
                int lenBefore = output.length();
                int modPos = s.pos + adjust;
                switch (nextChange.getOp()) {
                case CellNumberStringMod.AFTER:
                    // ignore adding a comma after a deleted char (which was a '#')
                    if (nextChange.getToAdd().equals(groupingSeparator) && deletedChars.get(s.pos)) {
                        break;
                    }
                    output.insert(modPos + 1, nextChange.getToAdd());
                    break;
                case CellNumberStringMod.BEFORE:
                    output.insert(modPos, nextChange.getToAdd());
                    break;

                case CellNumberStringMod.REPLACE:
                    // delete starting pos in original coordinates
                    int delPos = s.pos;
                    if (!nextChange.isStartInclusive()) {
                        delPos++;
                        modPos++;
                    }

                    // Skip over anything already deleted
                    while (deletedChars.get(delPos)) {
                        delPos++;
                        modPos++;
                    }

                    // delete end point in original
                    int delEndPos = nextChange.getEnd().pos;
                    if (nextChange.isEndInclusive()) {
                        delEndPos++;
                    }

                    // delete end point in current
                    int modEndPos = delEndPos + adjust;

                    if (modPos < modEndPos) {
                        if ("".equals(nextChange.getToAdd())) {
                            output.delete(modPos, modEndPos);
                        }
                        else {
                            char fillCh = nextChange.getToAdd().charAt(0);
                            for (int i = modPos; i < modEndPos; i++) {
                                output.setCharAt(i, fillCh);
                            }
                        }
                        deletedChars.set(delPos, delEndPos);
                    }
                    break;

                default:
                    throw new IllegalStateException("Unknown op: " + nextChange.getOp());
                }
                adjust += output.length() - lenBefore;

                nextChange = (changes.hasNext()) ? changes.next() : null;
            }
        }

        // Finally, add it to the string
        if (negative) {
            toAppendTo.append('-');
        }
        toAppendTo.append(output);
    }

    private void writeScientific(double value, StringBuffer output, Set<CellNumberStringMod> mods) {

        StringBuffer result = new StringBuffer();
        FieldPosition fractionPos = new FieldPosition(DecimalFormat.FRACTION_FIELD);
        decimalFmt.format(value, result, fractionPos);
        writeInteger(result, output, integerSpecials, mods, showGroupingSeparator);
        writeFractional(result, output);

        /*
        * Exponent sign handling is complex.
        *
        * In DecimalFormat, you never put the sign in the format, and the sign only
        * comes out of the format if it is negative.
        *
        * In Excel, you always say whether to always show the sign ("e+") or only
        * show negative signs ("e-").
        *
        * Also in Excel, where you put the sign in the format is NOT where it comes
        * out in the result.  In the format, the sign goes with the "e"; in the
        * output it goes with the exponent value.  That is, if you say "#e-|#" you
        * get "1e|-5", not "1e-|5". This makes sense I suppose, but it complicates
        * things.
        *
        * Finally, everything else in this formatting code assumes that the base of
        * the result is the original format, and that starting from that situation,
        * the indexes of the original special characters can be used to place the new
        * characters.  As just described, this is not true for the exponent's sign.
        * <p>
        * So here is how we handle it:
        *
        * (1) When parsing the format, remove the sign from after the 'e' and put it
        * before the first digit of the exponent (where it will be shown).
        *
        * (2) Determine the result's sign.
        *
        * (3) If it's missing, put the sign into the output to keep the result
        * lined up with the output. (In the result, "after the 'e'" and "before the
        * first digit" are the same because the result has no extra chars to be in
        * the way.)
        *
        * (4) In the output, remove the sign if it should not be shown ("e-" was used
        * and the sign is negative) or set it to the correct value.
        */

        // (2) Determine the result's sign.
        int ePos = fractionPos.getEndIndex();
        int signPos = ePos + 1;
        char expSignRes = result.charAt(signPos);
        if (expSignRes != '-') {
            // not a sign, so it's a digit, and therefore a positive exponent
            expSignRes = '+';
            // (3) If it's missing, put the sign into the output to keep the result
            // lined up with the output.
            result.insert(signPos, '+');
        }

        // Now the result lines up like it is supposed to with the specials' indexes
        ListIterator<Special> it = exponentSpecials.listIterator(1);
        Special expSign = it.next();
        char expSignFmt = expSign.ch;

        // (4) In the output, remove the sign if it should not be shown or set it to
        // the correct value.
        if (expSignRes == '-' || expSignFmt == '+') {
            mods.add(replaceMod(expSign, true, expSign, true, expSignRes));
        } else {
            mods.add(deleteMod(expSign, true, expSign, true));
        }

        StringBuffer exponentNum = new StringBuffer(result.substring(signPos + 1));
        writeInteger(exponentNum, output, exponentDigitSpecials, mods, false);
    }

    @SuppressWarnings("unchecked")
    private void writeFraction(double value, StringBuffer result,
            double fractional, StringBuffer output, Set<CellNumberStringMod> mods) {

        // Figure out if we are to suppress either the integer or fractional part.
        // With # the suppressed part is removed; with ? it is replaced with spaces.
        if (!improperFraction) {
            // If fractional part is zero, and numerator doesn't have '0', write out
            // only the integer part and strip the rest.
            if (fractional == 0 && !hasChar('0', numeratorSpecials)) {
                writeInteger(result, output, integerSpecials, mods, false);

                Special start = lastSpecial(integerSpecials);
                Special end = lastSpecial(denominatorSpecials);
                if (hasChar('?', integerSpecials, numeratorSpecials, denominatorSpecials)) {
                    //if any format has '?', then replace the fraction with spaces
                    mods.add(replaceMod(start, false, end, true, ' '));
                } else {
                    // otherwise, remove the fraction
                    mods.add(deleteMod(start, false, end, true));
                }

                // That's all, just return
                return;
            } else {
                // New we check to see if we should remove the integer part
                boolean numNoZero = !hasChar('0', numeratorSpecials);
                boolean intNoZero = !hasChar('0', integerSpecials);
                boolean intOnlyHash = integerSpecials.isEmpty() || (integerSpecials.size() == 1 && hasChar('#', integerSpecials));

                boolean removeBecauseZero     = fractional == 0 && (intOnlyHash || numNoZero);
                boolean removeBecauseFraction = fractional != 0 && intNoZero;

                if (value == 0 && (removeBecauseZero || removeBecauseFraction)) {
                    Special start = lastSpecial(integerSpecials);
                    boolean hasPlaceHolder = hasChar('?', integerSpecials, numeratorSpecials);
                    CellNumberStringMod sm = hasPlaceHolder
                        ? replaceMod(start, true, numerator, false, ' ')
                        : deleteMod(start, true, numerator, false);
                    mods.add(sm);
                } else {
                    // Not removing the integer part -- print it out
                    writeInteger(result, output, integerSpecials, mods, false);
                }
            }
        }

        // Calculate and print the actual fraction (improper or otherwise)
        try {
            int n;
            int d;
            // the "fractional % 1" captures integer values in improper fractions
            if (fractional == 0 || (improperFraction && fractional % 1 == 0)) {
                // 0 as a fraction is reported by excel as 0/1
                n = (int) Math.round(fractional);
                d = 1;
            } else {
                SimpleFraction frac = SimpleFraction.buildFractionMaxDenominator(fractional, maxDenominator);
                n = frac.getNumerator();
                d = frac.getDenominator();
            }
            if (improperFraction) {
                n += Math.round(value * d);
            }
            writeSingleInteger(numeratorFmt, n, output, numeratorSpecials, mods);
            writeSingleInteger(denominatorFmt, d, output, denominatorSpecials, mods);
        } catch (RuntimeException ignored) {
            LOG.log(POILogger.ERROR, "error while fraction evaluation", ignored);
        }
    }
    
    private String localiseFormat(String format) {
        DecimalFormatSymbols dfs = getDecimalFormatSymbols();
        if(format.contains(",") && dfs.getGroupingSeparator() != ',') {
            if(format.contains(".") && dfs.getDecimalSeparator() != '.') {
                format = replaceLast(format, "\\.", "[DECIMAL_SEPARATOR]");
                format = format.replace(',', dfs.getGroupingSeparator())
                        .replace("[DECIMAL_SEPARATOR]", Character.toString(dfs.getDecimalSeparator()));
            } else {
                format = format.replace(',', dfs.getGroupingSeparator());
            }
        } else if(format.contains(".") && dfs.getDecimalSeparator() != '.') {
            format = format.replace('.', dfs.getDecimalSeparator());
        }
        return format;
    }
    
    
    private static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }

    private static boolean hasChar(char ch, List<Special>... numSpecials) {
        for (List<Special> specials : numSpecials) {
            for (Special s : specials) {
                if (s.ch == ch) {
                    return true;
                }
            }
        }
        return false;
    }

    private void writeSingleInteger(String fmt, int num, StringBuffer output, List<Special> numSpecials, Set<CellNumberStringMod> mods) {

        StringBuffer sb = new StringBuffer();
        Formatter formatter = new Formatter(sb, locale);
        try {
            formatter.format(locale, fmt, num);
        } finally {
            formatter.close();
        }
        writeInteger(sb, output, numSpecials, mods, false);
    }

    private void writeInteger(StringBuffer result, StringBuffer output,
            List<Special> numSpecials, Set<CellNumberStringMod> mods,
            boolean showGroupingSeparator) {

        DecimalFormatSymbols dfs = getDecimalFormatSymbols();
        String decimalSeparator = Character.toString(dfs.getDecimalSeparator());
        String groupingSeparator = Character.toString(dfs.getGroupingSeparator());

        int pos = result.indexOf(decimalSeparator) - 1;
        if (pos < 0) {
            if (exponent != null && numSpecials == integerSpecials) {
                pos = result.indexOf("E") - 1;
            } else {
                pos = result.length() - 1;
            }
        }

        int strip;
        for (strip = 0; strip < pos; strip++) {
            char resultCh = result.charAt(strip);
            if (resultCh != '0' && resultCh != dfs.getGroupingSeparator()) {
                break;
            }
        }

        ListIterator<Special> it = numSpecials.listIterator(numSpecials.size());
        boolean followWithGroupingSeparator = false;
        Special lastOutputIntegerDigit = null;
        int digit = 0;
        while (it.hasPrevious()) {
            char resultCh;
            if (pos >= 0) {
                resultCh = result.charAt(pos);
            } else {
                // If result is shorter than field, pretend there are leading zeros
                resultCh = '0';
            }
            Special s = it.previous();
            followWithGroupingSeparator = showGroupingSeparator && digit > 0 && digit % 3 == 0;
            boolean zeroStrip = false;
            if (resultCh != '0' || s.ch == '0' || s.ch == '?' || pos >= strip) {
                zeroStrip = s.ch == '?' && pos < strip;
                output.setCharAt(s.pos, (zeroStrip ? ' ' : resultCh));
                lastOutputIntegerDigit = s;
            }
            if (followWithGroupingSeparator) {
                mods.add(insertMod(s, zeroStrip ? " " : groupingSeparator, CellNumberStringMod.AFTER));
                followWithGroupingSeparator = false;
            }
            digit++;
            --pos;
        }
        StringBuffer extraLeadingDigits = new StringBuffer();
        if (pos >= 0) {
            // We ran out of places to put digits before we ran out of digits; put this aside so we can add it later
            // pos was decremented at the end of the loop above when the iterator was at its end
            ++pos;
            extraLeadingDigits = new StringBuffer(result.substring(0, pos));
            if (showGroupingSeparator) {
                while (pos > 0) {
                    if (digit > 0 && digit % 3 == 0) {
                        extraLeadingDigits.insert(pos, groupingSeparator);
                    }
                    digit++;
                    --pos;
                }
            }
            mods.add(insertMod(lastOutputIntegerDigit, extraLeadingDigits, CellNumberStringMod.BEFORE));
        }
    }

    private void writeFractional(StringBuffer result, StringBuffer output) {
        int digit;
        int strip;
        if (fractionalSpecials.size() > 0) {
            String decimalSeparator = Character.toString(getDecimalFormatSymbols().getDecimalSeparator());
            digit = result.indexOf(decimalSeparator) + 1;
            if (exponent != null) {
                strip = result.indexOf("e") - 1;
            } else {
                strip = result.length() - 1;
            }

            while (strip > digit && result.charAt(strip) == '0') {
                strip--;
            }

            for (Special s : fractionalSpecials) {
                char resultCh = result.charAt(digit);
                if (resultCh != '0' || s.ch == '0' || digit < strip) {
                    output.setCharAt(s.pos, resultCh);
                } else if (s.ch == '?') {
                    // This is when we're in trailing zeros, and the format is '?'.
                    // We still strip out remaining '#'s later
                    output.setCharAt(s.pos, ' ');
                }
                digit++;
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * For a number, this is <tt>"#"</tt> for integer values, and <tt>"#.#"</tt>
     * for floating-point values.
     */
    public void simpleValue(StringBuffer toAppendTo, Object value) {
        SIMPLE_NUMBER.formatValue(toAppendTo, value);
    }

    private static Special lastSpecial(List<Special> s)  {
        return s.get(s.size() - 1);
    }
}
