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

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;

import org.apache.poi.ss.format.CellFormatPart.PartHandler;
import org.apache.poi.ss.format.CellNumberFormatter.Special;
import org.apache.poi.util.Internal;

/**
 * Internal helper class for CellNumberFormatter
 */
@Internal
public class CellNumberPartHandler implements PartHandler {
    private char insertSignForExponent;
    private double scale = 1;
    private Special decimalPoint;
    private Special slash;
    private Special exponent;
    private Special numerator;
    private final List<Special> specials = new LinkedList<>();
    private boolean improperFraction;
    /** the digits of a fixed denominator, e.g. "4" for "# ?/4", while they are being read */
    private StringBuilder fixedDenominatorDigits;
    private boolean afterSlash;
    private int fixedDenominator;
    private Special fixedDenominatorEnd;

    @Override
    public String handlePart(Matcher m, String part, CellFormatType type, StringBuffer descBuf) {
        int pos = descBuf.length();
        char firstCh = part.charAt(0);
        if (handleFixedDenominator(part, pos)) {
            // a digit of a fixed denominator is shown as it is
            return part;
        }
        switch (firstCh) {
        case 'e':
        case 'E':
            // See comment in writeScientific -- exponent handling is complex.
            // (1) When parsing the format, remove the sign from after the 'e' and
            // put it before the first digit of the exponent.
            if (exponent == null && !specials.isEmpty()) {
                exponent = new Special('.', pos);
                specials.add(exponent);
                insertSignForExponent = part.charAt(1);
                return part.substring(0, 1);
            }
            break;

        case '0':
        case '?':
        case '#':
            if (insertSignForExponent != '\0') {
                specials.add(new Special(insertSignForExponent, pos));
                descBuf.append(insertSignForExponent);
                insertSignForExponent = '\0';
                pos++;
            }
            for (int i = 0; i < part.length(); i++) {
                char ch = part.charAt(i);
                specials.add(new Special(ch, pos + i));
            }
            break;

        case '.':
            if (decimalPoint == null && !specials.isEmpty()) {
                decimalPoint = new Special('.', pos);
                specials.add(decimalPoint);
            }
            break;

        case '/':
            //!! This assumes there is a numerator and a denominator, but these are actually optional
            if (slash == null && !specials.isEmpty()) {
                numerator = previousNumber();
                // If the first number in the whole format is the numerator, the
                // entire number should be printed as an improper fraction
                improperFraction |= (numerator == firstDigit(specials));
                slash = new Special('.', pos);
                specials.add(slash);
                afterSlash = true;
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

    /**
     * A denominator given as a number, e.g. "# ?/4", is a fixed denominator: the fraction is
     * shown with that denominator, which is shown as it is. Excel reads all the digits of it,
     * including any zeros (which would otherwise be digit placeholders), e.g. "?/10".
     *
     * @return whether the part is a digit of a fixed denominator
     */
    private boolean handleFixedDenominator(String part, int pos) {
        boolean digits = part.chars().allMatch(Character::isDigit);
        if (afterSlash) {
            afterSlash = false;
            if (digits && part.charAt(0) != '0') {
                fixedDenominatorDigits = new StringBuilder(part);
                fixedDenominatorEnd = new Special(part.charAt(part.length() - 1), pos + part.length() - 1);
                return true;
            }
        } else if (fixedDenominatorDigits != null) {
            if (digits) {
                fixedDenominatorDigits.append(part);
                fixedDenominatorEnd = new Special(part.charAt(part.length() - 1), pos + part.length() - 1);
                return true;
            }
            finishFixedDenominator();
        }
        return false;
    }

    private void finishFixedDenominator() {
        if (fixedDenominatorDigits != null) {
            try {
                fixedDenominator = Integer.parseInt(fixedDenominatorDigits.toString());
                specials.add(fixedDenominatorEnd);
            } catch (NumberFormatException e) {
                // too big to be a denominator: leave it as text
                fixedDenominatorEnd = null;
            }
            fixedDenominatorDigits = null;
        }
    }

    /**
     * @return the fixed denominator of a fraction, e.g. 4 for "# ?/4", or 0 if there is none
     */
    int getFixedDenominator() {
        finishFixedDenominator();
        return fixedDenominator;
    }

    /**
     * @return the marker for the last digit of the fixed denominator, or null if there is none
     */
    Special getFixedDenominatorEnd() {
        finishFixedDenominator();
        return fixedDenominator > 0 ? fixedDenominatorEnd : null;
    }

    public double getScale() {
        return scale;
    }

    public Special getDecimalPoint() {
        return decimalPoint;
    }

    public Special getSlash() {
        return slash;
    }

    public Special getExponent() {
        return exponent;
    }

    public Special getNumerator() {
        return numerator;
    }

    public List<Special> getSpecials() {
        finishFixedDenominator();
        return specials;
    }

    public boolean isImproperFraction() {
        return improperFraction;
    }

    private Special previousNumber() {
        ListIterator<Special> it = specials.listIterator(specials.size());
        while (it.hasPrevious()) {
            Special s = it.previous();
            if (isDigitFmt(s)) {
                Special last = s;
                while (it.hasPrevious()) {
                    s = it.previous();
                    // it has to be continuous digits
                    if (last.pos - s.pos > 1 || !isDigitFmt(s)) {
                        break;
                    }
                    last = s;
                }
                return last;
            }
        }
        return null;
    }

    private static boolean isDigitFmt(Special s) {
        return s.ch == '0' || s.ch == '?' || s.ch == '#';
    }

    private static Special firstDigit(List<Special> specials) {
        for (Special s : specials) {
            if (isDigitFmt(s)) {
                return s;
            }
        }
        return null;
    }
}
