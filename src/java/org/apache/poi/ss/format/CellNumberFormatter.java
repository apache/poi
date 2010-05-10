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

import org.apache.poi.ss.format.CellFormatPart.PartHandler;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.BitSet;
import java.util.Collections;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;

/**
 * This class implements printing out a value using a number format.
 *
 * @author Ken Arnold, Industrious Media LLC
 */
public class CellNumberFormatter extends CellFormatter {
    private final String desc;
    private String printfFmt;
    private double scale;
    private Special decimalPoint;
    private Special slash;
    private Special exponent;
    private Special numerator;
    private Special afterInteger;
    private Special afterFractional;
    private boolean integerCommas;
    private final List<Special> specials;
    private List<Special> integerSpecials;
    private List<Special> fractionalSpecials;
    private List<Special> numeratorSpecials;
    private List<Special> denominatorSpecials;
    private List<Special> exponentSpecials;
    private List<Special> exponentDigitSpecials;
    private int maxDenominator;
    private String numeratorFmt;
    private String denominatorFmt;
    private boolean improperFraction;
    private DecimalFormat decimalFmt;

    static final CellFormatter SIMPLE_NUMBER = new CellFormatter("General") {
        public void formatValue(StringBuffer toAppendTo, Object value) {
            if (value == null)
                return;
            if (value instanceof Number) {
                Number num = (Number) value;
                if (num.doubleValue() % 1.0 == 0)
                    SIMPLE_INT.formatValue(toAppendTo, value);
                else
                    SIMPLE_FLOAT.formatValue(toAppendTo, value);
            } else {
                CellTextFormatter.SIMPLE_TEXT.formatValue(toAppendTo, value);
            }
        }

        public void simpleValue(StringBuffer toAppendTo, Object value) {
            formatValue(toAppendTo, value);
        }
    };

    private static final CellFormatter SIMPLE_INT = new CellNumberFormatter(
            "#");
    private static final CellFormatter SIMPLE_FLOAT = new CellNumberFormatter(
            "#.#");

    /**
     * This class is used to mark where the special characters in the format
     * are, as opposed to the other characters that are simply printed.
     */
    static class Special {
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
     * This class represents a single modification to a result string.  The way
     * this works is complicated, but so is numeric formatting.  In general, for
     * most formats, we use a DecimalFormat object that will put the string out
     * in a known format, usually with all possible leading and trailing zeros.
     * We then walk through the result and the orginal format, and note any
     * modifications that need to be made.  Finally, we go through and apply
     * them all, dealing with overlapping modifications.
     */
    static class StringMod implements Comparable<StringMod> {
        final Special special;
        final int op;
        CharSequence toAdd;
        Special end;
        boolean startInclusive;
        boolean endInclusive;

        public static final int BEFORE = 1;
        public static final int AFTER = 2;
        public static final int REPLACE = 3;

        private StringMod(Special special, CharSequence toAdd, int op) {
            this.special = special;
            this.toAdd = toAdd;
            this.op = op;
        }

        public StringMod(Special start, boolean startInclusive, Special end,
                boolean endInclusive, char toAdd) {
            this(start, startInclusive, end, endInclusive);
            this.toAdd = toAdd + "";
        }

        public StringMod(Special start, boolean startInclusive, Special end,
                boolean endInclusive) {
            special = start;
            this.startInclusive = startInclusive;
            this.end = end;
            this.endInclusive = endInclusive;
            op = REPLACE;
            toAdd = "";
        }

        public int compareTo(StringMod that) {
            int diff = special.pos - that.special.pos;
            if (diff != 0)
                return diff;
            else
                return op - that.op;
        }

        @Override
        public boolean equals(Object that) {
            try {
                return compareTo((StringMod) that) == 0;
            } catch (RuntimeException ignored) {
                // NullPointerException or CastException
                return false;
            }
        }

        @Override
        public int hashCode() {
            return special.hashCode() + op;
        }
    }

    private class NumPartHandler implements PartHandler {
        private char insertSignForExponent;

        public String handlePart(Matcher m, String part, CellFormatType type,
                StringBuffer desc) {
            int pos = desc.length();
            char firstCh = part.charAt(0);
            switch (firstCh) {
            case 'e':
            case 'E':
                // See comment in writeScientific -- exponent handling is complex.
                // (1) When parsing the format, remove the sign from after the 'e' and
                // put it before the first digit of the exponent.
                if (exponent == null && specials.size() > 0) {
                    specials.add(exponent = new Special('.', pos));
                    insertSignForExponent = part.charAt(1);
                    return part.substring(0, 1);
                }
                break;

            case '0':
            case '?':
            case '#':
                if (insertSignForExponent != '\0') {
                    specials.add(new Special(insertSignForExponent, pos));
                    desc.append(insertSignForExponent);
                    insertSignForExponent = '\0';
                    pos++;
                }
                for (int i = 0; i < part.length(); i++) {
                    char ch = part.charAt(i);
                    specials.add(new Special(ch, pos + i));
                }
                break;

            case '.':
                if (decimalPoint == null && specials.size() > 0)
                    specials.add(decimalPoint = new Special('.', pos));
                break;

            case '/':
                //!! This assumes there is a numerator and a denominator, but these are actually optional
                if (slash == null && specials.size() > 0) {
                    numerator = previousNumber();
                    // If the first number in the whole format is the numerator, the
                    // entire number should be printed as an improper fraction
                    if (numerator == firstDigit(specials))
                        improperFraction = true;
                    specials.add(slash = new Special('.', pos));
                }
                break;

            case '%':
                // don't need to remember because we don't need to do anything with these
                scale *= 100;
                break;

            default:
                return null;
            }
            return part;
        }
    }

    /**
     * Creates a new cell number formatter.
     *
     * @param format The format to parse.
     */
    public CellNumberFormatter(String format) {
        super(format);

        scale = 1;

        specials = new LinkedList<Special>();

        NumPartHandler partHandler = new NumPartHandler();
        StringBuffer descBuf = CellFormatPart.parseFormat(format,
                CellFormatType.NUMBER, partHandler);

        // These are inconsistent settings, so ditch 'em
        if ((decimalPoint != null || exponent != null) && slash != null) {
            slash = null;
            numerator = null;
        }

        interpretCommas(descBuf);

        int precision;
        int fractionPartWidth = 0;
        if (decimalPoint == null) {
            precision = 0;
        } else {
            precision = interpretPrecision();
            fractionPartWidth = 1 + precision;
            if (precision == 0) {
                // This means the format has a ".", but that output should have no decimals after it.
                // We just stop treating it specially
                specials.remove(decimalPoint);
                decimalPoint = null;
            }
        }

        if (precision == 0)
            fractionalSpecials = Collections.emptyList();
        else
            fractionalSpecials = specials.subList(specials.indexOf(
                    decimalPoint) + 1, fractionalEnd());
        if (exponent == null)
            exponentSpecials = Collections.emptyList();
        else {
            int exponentPos = specials.indexOf(exponent);
            exponentSpecials = specialsFor(exponentPos, 2);
            exponentDigitSpecials = specialsFor(exponentPos + 2);
        }

        if (slash == null) {
            numeratorSpecials = Collections.emptyList();
            denominatorSpecials = Collections.emptyList();
        } else {
            if (numerator == null)
                numeratorSpecials = Collections.emptyList();
            else
                numeratorSpecials = specialsFor(specials.indexOf(numerator));

            denominatorSpecials = specialsFor(specials.indexOf(slash) + 1);
            if (denominatorSpecials.isEmpty()) {
                // no denominator follows the slash, drop the fraction idea
                numeratorSpecials = Collections.emptyList();
            } else {
                maxDenominator = maxValue(denominatorSpecials);
                numeratorFmt = singleNumberFormat(numeratorSpecials);
                denominatorFmt = singleNumberFormat(denominatorSpecials);
            }
        }

        integerSpecials = specials.subList(0, integerEnd());

        if (exponent == null) {
            StringBuffer fmtBuf = new StringBuffer("%");

            int integerPartWidth = calculateIntegerPartWidth();
            int totalWidth = integerPartWidth + fractionPartWidth;

            fmtBuf.append('0').append(totalWidth).append('.').append(precision);

            fmtBuf.append("f");
            printfFmt = fmtBuf.toString();
        } else {
            StringBuffer fmtBuf = new StringBuffer();
            boolean first = true;
            List<Special> specialList = integerSpecials;
            if (integerSpecials.size() == 1) {
                // If we don't do this, we get ".6e5" instead of "6e4"
                fmtBuf.append("0");
                first = false;
            } else
                for (Special s : specialList) {
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
            placeZeros(fmtBuf, exponentSpecials.subList(2,
                    exponentSpecials.size()));
            decimalFmt = new DecimalFormat(fmtBuf.toString());
        }

        if (exponent != null)
            scale =
                    1;  // in "e" formats,% and trailing commas have no scaling effect

        desc = descBuf.toString();
    }

    private static void placeZeros(StringBuffer sb, List<Special> specials) {
        for (Special s : specials) {
            if (isDigitFmt(s))
                sb.append('0');
        }
    }

    private static Special firstDigit(List<Special> specials) {
        for (Special s : specials) {
            if (isDigitFmt(s))
                return s;
        }
        return null;
    }

    static StringMod insertMod(Special special, CharSequence toAdd, int where) {
        return new StringMod(special, toAdd, where);
    }

    static StringMod deleteMod(Special start, boolean startInclusive,
            Special end, boolean endInclusive) {

        return new StringMod(start, startInclusive, end, endInclusive);
    }

    static StringMod replaceMod(Special start, boolean startInclusive,
            Special end, boolean endInclusive, char withChar) {

        return new StringMod(start, startInclusive, end, endInclusive,
                withChar);
    }

    private static String singleNumberFormat(List<Special> numSpecials) {
        return "%0" + numSpecials.size() + "d";
    }

    private static int maxValue(List<Special> s) {
        return (int) Math.round(Math.pow(10, s.size()) - 1);
    }

    private List<Special> specialsFor(int pos, int takeFirst) {
        if (pos >= specials.size())
            return Collections.emptyList();
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

    private Special previousNumber() {
        ListIterator<Special> it = specials.listIterator(specials.size());
        while (it.hasPrevious()) {
            Special s = it.previous();
            if (isDigitFmt(s)) {
                Special numStart = s;
                Special last = s;
                while (it.hasPrevious()) {
                    s = it.previous();
                    if (last.pos - s.pos > 1) // it has to be continuous digits
                        break;
                    if (isDigitFmt(s))
                        numStart = s;
                    else
                        break;
                    last = s;
                }
                return numStart;
            }
        }
        return null;
    }

    private int calculateIntegerPartWidth() {
        ListIterator<Special> it = specials.listIterator();
        int digitCount = 0;
        while (it.hasNext()) {
            Special s = it.next();
            //!! Handle fractions: The previous set of digits before that is the numerator, so we should stop short of that
            if (s == afterInteger)
                break;
            else if (isDigitFmt(s))
                digitCount++;
        }
        return digitCount;
    }

    private int interpretPrecision() {
        if (decimalPoint == null) {
            return -1;
        } else {
            int precision = 0;
            ListIterator<Special> it = specials.listIterator(specials.indexOf(
                    decimalPoint));
            if (it.hasNext())
                it.next();  // skip over the decimal point itself
            while (it.hasNext()) {
                Special s = it.next();
                if (isDigitFmt(s))
                    precision++;
                else
                    break;
            }
            return precision;
        }
    }

    private void interpretCommas(StringBuffer sb) {
        // In the integer part, commas at the end are scaling commas; other commas mean to show thousand-grouping commas
        ListIterator<Special> it = specials.listIterator(integerEnd());

        boolean stillScaling = true;
        integerCommas = false;
        while (it.hasPrevious()) {
            Special s = it.previous();
            if (s.ch != ',') {
                stillScaling = false;
            } else {
                if (stillScaling) {
                    scale /= 1000;
                } else {
                    integerCommas = true;
                }
            }
        }

        if (decimalPoint != null) {
            it = specials.listIterator(fractionalEnd());
            while (it.hasPrevious()) {
                Special s = it.previous();
                if (s.ch != ',') {
                    break;
                } else {
                    scale /= 1000;
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
    }

    private int integerEnd() {
        if (decimalPoint != null)
            afterInteger = decimalPoint;
        else if (exponent != null)
            afterInteger = exponent;
        else if (numerator != null)
            afterInteger = numerator;
        else
            afterInteger = null;
        return afterInteger == null ? specials.size() : specials.indexOf(
                afterInteger);
    }

    private int fractionalEnd() {
        int end;
        if (exponent != null)
            afterFractional = exponent;
        else if (numerator != null)
            afterInteger = numerator;
        else
            afterFractional = null;
        end = afterFractional == null ? specials.size() : specials.indexOf(
                afterFractional);
        return end;
    }

    /** {@inheritDoc} */
    public void formatValue(StringBuffer toAppendTo, Object valueObject) {
        double value = ((Number) valueObject).doubleValue();
        value *= scale;

        // the '-' sign goes at the front, always, so we pick it out
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

        Set<StringMod> mods = new TreeSet<StringMod>();
        StringBuffer output = new StringBuffer(desc);

        if (exponent != null) {
            writeScientific(value, output, mods);
        } else if (improperFraction) {
            writeFraction(value, null, fractional, output, mods);
        } else {
            StringBuffer result = new StringBuffer();
            Formatter f = new Formatter(result);
            f.format(LOCALE, printfFmt, value);

            if (numerator == null) {
                writeFractional(result, output);
                writeInteger(result, output, integerSpecials, mods,
                        integerCommas);
            } else {
                writeFraction(value, result, fractional, output, mods);
            }
        }

        // Now strip out any remaining '#'s and add any pending text ...
        ListIterator<Special> it = specials.listIterator();
        Iterator<StringMod> changes = mods.iterator();
        StringMod nextChange = (changes.hasNext() ? changes.next() : null);
        int adjust = 0;
        BitSet deletedChars = new BitSet(); // records chars already deleted
        while (it.hasNext()) {
            Special s = it.next();
            int adjustedPos = s.pos + adjust;
            if (!deletedChars.get(s.pos) && output.charAt(adjustedPos) == '#') {
                output.deleteCharAt(adjustedPos);
                adjust--;
                deletedChars.set(s.pos);
            }
            while (nextChange != null && s == nextChange.special) {
                int lenBefore = output.length();
                int modPos = s.pos + adjust;
                int posTweak = 0;
                switch (nextChange.op) {
                case StringMod.AFTER:
                    // ignore adding a comma after a deleted char (which was a '#')
                    if (nextChange.toAdd.equals(",") && deletedChars.get(s.pos))
                        break;
                    posTweak = 1;
                    //noinspection fallthrough
                case StringMod.BEFORE:
                    output.insert(modPos + posTweak, nextChange.toAdd);
                    break;

                case StringMod.REPLACE:
                    int delPos =
                            s.pos; // delete starting pos in original coordinates
                    if (!nextChange.startInclusive) {
                        delPos++;
                        modPos++;
                    }

                    // Skip over anything already deleted
                    while (deletedChars.get(delPos)) {
                        delPos++;
                        modPos++;
                    }

                    int delEndPos =
                            nextChange.end.pos; // delete end point in original
                    if (nextChange.endInclusive)
                        delEndPos++;

                    int modEndPos =
                            delEndPos + adjust; // delete end point in current

                    if (modPos < modEndPos) {
                        if (nextChange.toAdd == "")
                            output.delete(modPos, modEndPos);
                        else {
                            char fillCh = nextChange.toAdd.charAt(0);
                            for (int i = modPos; i < modEndPos; i++)
                                output.setCharAt(i, fillCh);
                        }
                        deletedChars.set(delPos, delEndPos);
                    }
                    break;

                default:
                    throw new IllegalStateException(
                            "Unknown op: " + nextChange.op);
                }
                adjust += output.length() - lenBefore;

                if (changes.hasNext())
                    nextChange = changes.next();
                else
                    nextChange = null;
            }
        }

        // Finally, add it to the string
        if (negative)
            toAppendTo.append('-');
        toAppendTo.append(output);
    }

    private void writeScientific(double value, StringBuffer output,
            Set<StringMod> mods) {

        StringBuffer result = new StringBuffer();
        FieldPosition fractionPos = new FieldPosition(
                DecimalFormat.FRACTION_FIELD);
        decimalFmt.format(value, result, fractionPos);
        writeInteger(result, output, integerSpecials, mods, integerCommas);
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
        * <p/>
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
        if (expSignRes == '-' || expSignFmt == '+')
            mods.add(replaceMod(expSign, true, expSign, true, expSignRes));
        else
            mods.add(deleteMod(expSign, true, expSign, true));

        StringBuffer exponentNum = new StringBuffer(result.substring(
                signPos + 1));
        writeInteger(exponentNum, output, exponentDigitSpecials, mods, false);
    }

    private void writeFraction(double value, StringBuffer result,
            double fractional, StringBuffer output, Set<StringMod> mods) {

        // Figure out if we are to suppress either the integer or fractional part.
        // With # the suppressed part is removed; with ? it is replaced with spaces.
        if (!improperFraction) {
            // If fractional part is zero, and numerator doesn't have '0', write out
            // only the integer part and strip the rest.
            if (fractional == 0 && !hasChar('0', numeratorSpecials)) {
                writeInteger(result, output, integerSpecials, mods, false);

                Special start = integerSpecials.get(integerSpecials.size() - 1);
                Special end = denominatorSpecials.get(
                        denominatorSpecials.size() - 1);
                if (hasChar('?', integerSpecials, numeratorSpecials,
                        denominatorSpecials)) {
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
                boolean allZero = (value == 0 && fractional == 0);
                boolean willShowFraction = fractional != 0 || hasChar('0',
                        numeratorSpecials);
                boolean removeBecauseZero = allZero && (hasOnly('#',
                        integerSpecials) || !hasChar('0', numeratorSpecials));
                boolean removeBecauseFraction =
                        !allZero && value == 0 && willShowFraction && !hasChar(
                                '0', integerSpecials);
                if (removeBecauseZero || removeBecauseFraction) {
                    Special start = integerSpecials.get(
                            integerSpecials.size() - 1);
                    if (hasChar('?', integerSpecials, numeratorSpecials)) {
                        mods.add(replaceMod(start, true, numerator, false,
                                ' '));
                    } else {
                        mods.add(deleteMod(start, true, numerator, false));
                    }
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
                Fraction frac = new Fraction(fractional, maxDenominator);
                n = frac.getNumerator();
                d = frac.getDenominator();
            }
            if (improperFraction)
                n += Math.round(value * d);
            writeSingleInteger(numeratorFmt, n, output, numeratorSpecials,
                    mods);
            writeSingleInteger(denominatorFmt, d, output, denominatorSpecials,
                    mods);
        } catch (RuntimeException ignored) {
            ignored.printStackTrace();
        }
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

    private static boolean hasOnly(char ch, List<Special>... numSpecials) {
        for (List<Special> specials : numSpecials) {
            for (Special s : specials) {
                if (s.ch != ch) {
                    return false;
                }
            }
        }
        return true;
    }

    private void writeSingleInteger(String fmt, int num, StringBuffer output,
            List<Special> numSpecials, Set<StringMod> mods) {

        StringBuffer sb = new StringBuffer();
        Formatter formatter = new Formatter(sb);
        formatter.format(LOCALE, fmt, num);
        writeInteger(sb, output, numSpecials, mods, false);
    }

    private void writeInteger(StringBuffer result, StringBuffer output,
            List<Special> numSpecials, Set<StringMod> mods,
            boolean showCommas) {

        int pos = result.indexOf(".") - 1;
        if (pos < 0) {
            if (exponent != null && numSpecials == integerSpecials)
                pos = result.indexOf("E") - 1;
            else
                pos = result.length() - 1;
        }

        int strip;
        for (strip = 0; strip < pos; strip++) {
            char resultCh = result.charAt(strip);
            if (resultCh != '0' && resultCh != ',')
                break;
        }

        ListIterator<Special> it = numSpecials.listIterator(numSpecials.size());
        boolean followWithComma = false;
        Special lastOutputIntegerDigit = null;
        int digit = 0;
        while (it.hasPrevious()) {
            char resultCh;
            if (pos >= 0)
                resultCh = result.charAt(pos);
            else {
                // If result is shorter than field, pretend there are leading zeros
                resultCh = '0';
            }
            Special s = it.previous();
            followWithComma = showCommas && digit > 0 && digit % 3 == 0;
            boolean zeroStrip = false;
            if (resultCh != '0' || s.ch == '0' || s.ch == '?' || pos >= strip) {
                zeroStrip = s.ch == '?' && pos < strip;
                output.setCharAt(s.pos, (zeroStrip ? ' ' : resultCh));
                lastOutputIntegerDigit = s;
            }
            if (followWithComma) {
                mods.add(insertMod(s, zeroStrip ? " " : ",", StringMod.AFTER));
                followWithComma = false;
            }
            digit++;
            --pos;
        }
        StringBuffer extraLeadingDigits = new StringBuffer();
        if (pos >= 0) {
            // We ran out of places to put digits before we ran out of digits; put this aside so we can add it later
            ++pos;  // pos was decremented at the end of the loop above when the iterator was at its end
            extraLeadingDigits = new StringBuffer(result.substring(0, pos));
            if (showCommas) {
                while (pos > 0) {
                    if (digit > 0 && digit % 3 == 0)
                        extraLeadingDigits.insert(pos, ',');
                    digit++;
                    --pos;
                }
            }
            mods.add(insertMod(lastOutputIntegerDigit, extraLeadingDigits,
                    StringMod.BEFORE));
        }
    }

    private void writeFractional(StringBuffer result, StringBuffer output) {
        int digit;
        int strip;
        ListIterator<Special> it;
        if (fractionalSpecials.size() > 0) {
            digit = result.indexOf(".") + 1;
            if (exponent != null)
                strip = result.indexOf("e") - 1;
            else
                strip = result.length() - 1;
            while (strip > digit && result.charAt(strip) == '0')
                strip--;
            it = fractionalSpecials.listIterator();
            while (it.hasNext()) {
                Special s = it.next();
                char resultCh = result.charAt(digit);
                if (resultCh != '0' || s.ch == '0' || digit < strip)
                    output.setCharAt(s.pos, resultCh);
                else if (s.ch == '?') {
                    // This is when we're in trailing zeros, and the format is '?'.  We still strip out remaining '#'s later
                    output.setCharAt(s.pos, ' ');
                }
                digit++;
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * For a number, this is <tt>"#"</tt> for integer values, and <tt>"#.#"</tt>
     * for floating-point values.
     */
    public void simpleValue(StringBuffer toAppendTo, Object value) {
        SIMPLE_NUMBER.formatValue(toAppendTo, value);
    }

    /**
     *  Based on org.apache.commons.math.fraction.Fraction from Apache Commons-Math.
     *  YK: The only reason of having this inner class is to avoid dependency on the Commons-Math jar.
     */
    private static class Fraction {
        /** The denominator. */
        private final int denominator;

        /** The numerator. */
        private final int numerator;

        /**
         * Create a fraction given the double value and either the maximum error
         * allowed or the maximum number of denominator digits.
         *
         * @param value the double value to convert to a fraction.
         * @param epsilon maximum error allowed.  The resulting fraction is within
         *        <code>epsilon</code> of <code>value</code>, in absolute terms.
         * @param maxDenominator maximum denominator value allowed.
         * @param maxIterations maximum number of convergents
         * @throws RuntimeException if the continued fraction failed to
         *         converge.
         */
        private Fraction(double value, double epsilon, int maxDenominator, int maxIterations)
        {
            long overflow = Integer.MAX_VALUE;
            double r0 = value;
            long a0 = (long)Math.floor(r0);
            if (a0 > overflow) {
                throw new IllegalArgumentException("Overflow trying to convert "+value+" to fraction ("+a0+"/"+1l+")");
            }

            // check for (almost) integer arguments, which should not go
            // to iterations.
            if (Math.abs(a0 - value) < epsilon) {
                this.numerator = (int) a0;
                this.denominator = 1;
                return;
            }

            long p0 = 1;
            long q0 = 0;
            long p1 = a0;
            long q1 = 1;

            long p2;
            long q2;

            int n = 0;
            boolean stop = false;
            do {
                ++n;
                double r1 = 1.0 / (r0 - a0);
                long a1 = (long)Math.floor(r1);
                p2 = (a1 * p1) + p0;
                q2 = (a1 * q1) + q0;
                if ((p2 > overflow) || (q2 > overflow)) {
                    throw new RuntimeException("Overflow trying to convert "+value+" to fraction ("+p2+"/"+q2+")");
                }

                double convergent = (double)p2 / (double)q2;
                if (n < maxIterations && Math.abs(convergent - value) > epsilon && q2 < maxDenominator) {
                    p0 = p1;
                    p1 = p2;
                    q0 = q1;
                    q1 = q2;
                    a0 = a1;
                    r0 = r1;
                } else {
                    stop = true;
                }
            } while (!stop);

            if (n >= maxIterations) {
                throw new RuntimeException("Unable to convert "+value+" to fraction after "+maxIterations+" iterations");
            }

            if (q2 < maxDenominator) {
                this.numerator = (int) p2;
                this.denominator = (int) q2;
            } else {
                this.numerator = (int) p1;
                this.denominator = (int) q1;
            }

        }

        /**
         * Create a fraction given the double value and maximum denominator.
         * <p>
         * References:
         * <ul>
         * <li><a href="http://mathworld.wolfram.com/ContinuedFraction.html">
         * Continued Fraction</a> equations (11) and (22)-(26)</li>
         * </ul>
         * </p>
         * @param value the double value to convert to a fraction.
         * @param maxDenominator The maximum allowed value for denominator
         * @throws RuntimeException if the continued fraction failed to
         *         converge
         */
        public Fraction(double value, int maxDenominator)
        {
           this(value, 0, maxDenominator, 100);
        }

        /**
         * Access the denominator.
         * @return the denominator.
         */
        public int getDenominator() {
            return denominator;
        }

        /**
         * Access the numerator.
         * @return the numerator.
         */
        public int getNumerator() {
            return numerator;
        }

    }

}