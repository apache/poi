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

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.*;

import java.util.regex.Pattern;

/**
 * Implementation for Excel IMAGINARY() function.
 * <p>
 * <b>Syntax</b>:<br> <b>IMAGINARY  </b>(<b>Inumber</b>)<br>
 * <p>
 * Returns the imaginary coefficient of a complex number in x + yi or x + yj text format.
 * <p>
 * Inumber     is a complex number for which you want the imaginary coefficient.
 * <p>
 * Remarks
 * <ul>
 * <li>Use COMPLEX to convert real and imaginary coefficients into a complex number.</li>
 * </ul>
 */
public class Imaginary extends Fixed1ArgFunction implements FreeRefFunction {

    public static final FreeRefFunction instance = new Imaginary();

    public static final String GROUP1_REAL_SIGN_REGEX = "([+-]?)";
    public static final String GROUP2_REAL_INTEGER_OR_DOUBLE_REGEX = "([0-9]+\\.[0-9]+|[0-9]*)";
    public static final String GROUP3_IMAGINARY_SIGN_REGEX = "([+-]?)";
    public static final String GROUP4_IMAGINARY_INTEGER_OR_DOUBLE_REGEX = "([0-9]+\\.[0-9]+|[0-9]*)";
    public static final String GROUP5_IMAGINARY_GROUP_REGEX = "([ij]?)";

    public static final Pattern COMPLEX_NUMBER_PATTERN
            = Pattern.compile(GROUP1_REAL_SIGN_REGEX + GROUP2_REAL_INTEGER_OR_DOUBLE_REGEX +
            GROUP3_IMAGINARY_SIGN_REGEX + GROUP4_IMAGINARY_INTEGER_OR_DOUBLE_REGEX + GROUP5_IMAGINARY_GROUP_REGEX);

    public static final int GROUP1_REAL_SIGN = 1;
    public static final int GROUP2_IMAGINARY_INTEGER_OR_DOUBLE = 2;
    public static final int GROUP3_IMAGINARY_SIGN = 3;
    public static final int GROUP4_IMAGINARY_INTEGER_OR_DOUBLE = 4;

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval inumberVE) {
        ValueEval veText1;
        try {
            veText1 = OperandResolver.getSingleValue(inumberVE, srcRowIndex, srcColumnIndex);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
        String iNumber = OperandResolver.coerceValueToString(veText1);

        String[] groups = parseComplexNumber(iNumber);

        String imaginary = "";
        if (groups != null) {
            String imaginaryGroup = groups[5];
            boolean hasImaginaryPart = imaginaryGroup.equals("i") || imaginaryGroup.equals("j");

            if (imaginaryGroup.isEmpty()) {
                return new StringEval(String.valueOf(0));
            }

            if (hasImaginaryPart) {
                String sign = "";
                String imaginarySign = groups[GROUP3_IMAGINARY_SIGN];
                if (imaginarySign.length() != 0 && !(imaginarySign.equals("+"))) {
                    sign = imaginarySign;
                }

                String groupImaginaryNumber = groups[GROUP4_IMAGINARY_INTEGER_OR_DOUBLE];
                if (groupImaginaryNumber.length() != 0) {
                    imaginary = sign + groupImaginaryNumber;
                } else {
                    imaginary = sign + "1";
                }
            }
        } else {
            return ErrorEval.NUM_ERROR;
        }

        return new StringEval(imaginary);
    }

    /**
     * Gives the same result as matching {@link #COMPLEX_NUMBER_PATTERN}, without the regular expression:
     * its groups can split a run of digits in many ways, so matching text that is not a complex number
     * took polynomial time. This tries the same splits in the same order as the regular expression
     * backtracking does, but checks each one in constant time.
     *
     * @return the groups that {@link java.util.regex.Matcher#group(int)} would return, indexed by group number
     *  (index 0 is the whole text), or {@code null} if the text does not match the pattern
     */
    static String[] parseComplexNumber(String text) {
        int n = text.length();
        // digitsEnd[i] is the end of the run of digits that starts at i
        int[] digitsEnd = new int[n + 1];
        digitsEnd[n] = n;
        for (int i = n - 1; i >= 0; i--) {
            digitsEnd[i] = isDigit(text.charAt(i)) ? digitsEnd[i + 1] : i;
        }
        // group 1: [+-]? tries the sign first
        boolean hasRealSign = n > 0 && isSign(text.charAt(0));
        for (int realStart = hasRealSign ? 1 : 0; realStart >= 0; realStart--) {
            // group 2: [0-9]+\.[0-9]+ with fewer and fewer digits after the '.', then [0-9]* with fewer and fewer digits
            int pointEnd = decimalPointEnd(text, realStart, digitsEnd);
            if (pointEnd >= 0) {
                for (int realEnd = digitsEnd[pointEnd]; realEnd > pointEnd; realEnd--) {
                    String[] groups = parseImaginaryPart(text, realStart, realEnd, digitsEnd);
                    if (groups != null) {
                        return groups;
                    }
                }
            }
            for (int realEnd = digitsEnd[realStart]; realEnd >= realStart; realEnd--) {
                String[] groups = parseImaginaryPart(text, realStart, realEnd, digitsEnd);
                if (groups != null) {
                    return groups;
                }
            }
        }
        return null;
    }

    /** groups 3 to 5, for groups 1 and 2 ending at {@code realStart} and {@code realEnd} */
    private static String[] parseImaginaryPart(String text, int realStart, int realEnd, int[] digitsEnd) {
        int n = text.length();
        // group 3: [+-]? tries the sign first
        boolean hasImaginarySign = realEnd < n && isSign(text.charAt(realEnd));
        for (int imaginaryStart = hasImaginarySign ? realEnd + 1 : realEnd; imaginaryStart >= realEnd; imaginaryStart--) {
            // group 4 is followed by [ij]? and the end of the text, and neither starts with a digit,
            // so of all the ways group 4 can match, only the longest one of each alternative can succeed
            int imaginaryEnd = -1;
            int pointEnd = decimalPointEnd(text, imaginaryStart, digitsEnd);
            if (pointEnd >= 0 && isImaginaryUnitAndEnd(text, digitsEnd[pointEnd])) {
                imaginaryEnd = digitsEnd[pointEnd];
            } else if (isImaginaryUnitAndEnd(text, digitsEnd[imaginaryStart])) {
                imaginaryEnd = digitsEnd[imaginaryStart];
            }
            if (imaginaryEnd >= 0) {
                return new String[] {
                        text,
                        text.substring(0, realStart),
                        text.substring(realStart, realEnd),
                        text.substring(realEnd, imaginaryStart),
                        text.substring(imaginaryStart, imaginaryEnd),
                        text.substring(imaginaryEnd)
                };
            }
        }
        return null;
    }

    /**
     * @return the index after the '.' if {@code [0-9]+\.[0-9]} matches at {@code start}, otherwise -1
     */
    private static int decimalPointEnd(String text, int start, int[] digitsEnd) {
        int pointIndex = digitsEnd[start];
        if (pointIndex > start && pointIndex + 1 < text.length() && text.charAt(pointIndex) == '.'
                && isDigit(text.charAt(pointIndex + 1))) {
            return pointIndex + 1;
        }
        return -1;
    }

    /** whether {@code ([ij]?)} followed by the end of the text matches at {@code index} */
    private static boolean isImaginaryUnitAndEnd(String text, int index) {
        int n = text.length();
        return index == n || (index == n - 1 && (text.charAt(index) == 'i' || text.charAt(index) == 'j'));
    }

    private static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private static boolean isSign(char ch) {
        return ch == '+' || ch == '-';
    }

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length != 1) {
            return ErrorEval.VALUE_INVALID;
        }
        return evaluate(ec.getRowIndex(), ec.getColumnIndex(), args[0]);
    }
}
